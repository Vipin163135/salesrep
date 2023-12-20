package com.salesrep.app.ui.home

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.fragivity.navigator
import com.github.fragivity.push
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.ktx.awaitMap
import com.salesrep.app.BuildConfig
import com.salesrep.app.R
import com.salesrep.app.base.BaseFragment
import com.salesrep.app.dao.RouteActivityDao
import com.salesrep.app.dao.RoutesDao
import com.salesrep.app.dao.UpdateRouteDao
import com.salesrep.app.data.appConfig.Crmapp
import com.salesrep.app.data.models.requests.RouteData
import com.salesrep.app.data.models.requests.UpdateRoute
import com.salesrep.app.data.models.response.GetFormCatalogResponse
import com.salesrep.app.data.models.response.GetRouteAccountResponse
import com.salesrep.app.data.models.response.Route
import com.salesrep.app.data.models.response.RouteStatusReasonsModel
import com.salesrep.app.data.network.responseUtil.ApisRespHandler
import com.salesrep.app.data.network.responseUtil.Status
import com.salesrep.app.data.repos.UserRepository
import com.salesrep.app.databinding.LayoutRouteMapBinding
import com.salesrep.app.services.TrackingService
import com.salesrep.app.ui.dialogs.ProgressDialog
import com.salesrep.app.ui.route.*
import com.salesrep.app.ui.route.adapters.CancelReasonAdapter
import com.salesrep.app.util.*
import com.salesrep.app.util.PermissionUtils
import com.salesrep.app.util.TrackConstants.ACTION_START_OR_RESUME_SERVICE
import com.salesrep.app.util.TrackConstants.ACTION_STOP_SERVICE
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONArray
import org.json.JSONObject
import permissions.dispatcher.*
import timber.log.Timber
import javax.inject.Inject

@RuntimePermissions
@AndroidEntryPoint
class HomeRouteFragment() : BaseFragment<LayoutRouteMapBinding>(),
    GoogleMap.OnInfoWindowClickListener, CancelReasonAdapter.onReasonSelectListener,
    GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var updateRouteDao: UpdateRouteDao

    @Inject
    lateinit var routeActivityDao: RouteActivityDao

    @Inject
    lateinit var routesDao: RoutesDao

    private lateinit var binding: LayoutRouteMapBinding
    override val viewModel by activityViewModels<HomeViewModel>()
    val homeViewModel by viewModels<HomeViewModel>()
    private lateinit var progressDialog: ProgressDialog
    private var map: GoogleMap? = null
    private var origin: LatLng? = null
    private var currentLoc: LatLng? = null
    private var destination: LatLng? = null
    private var latLngList = ArrayList<LatLng>()
    private var polyLineStringList: ArrayList<String>? = null
    private var selectedMarker: Marker? = null
    lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var line: Polyline? = null
    private val crmapp by lazy {
        prefsManager.getObject(
            PrefsManager.APP_CONFIG,
            Crmapp::class.java
        )
    }
    var totalDistance: Double? = null
    var totalDuration: Double? = null

    companion object {
        fun newInstance(route: Route, name: String): HomeRouteFragment {
            val fragment = HomeRouteFragment()
            val bundle = Bundle()
            bundle.putParcelable(DataTransferKeys.KEY_ROUTES, route)
            bundle.putString(DataTransferKeys.KEY_NAME, name)
            fragment.arguments = bundle
            return fragment
        }
    }

    private val route by lazy { arguments?.getParcelable<Route>(DataTransferKeys.KEY_ROUTES) }
    private val routeName by lazy { arguments?.getString(DataTransferKeys.KEY_NAME) }

    private var pageCount = 1
    private var currentPage = 1
    private var routePageCount = 1
    var routeStatusReasons: List<RouteStatusReasonsModel>? = null
    lateinit var adapter: CancelReasonAdapter
    var selectedReason: RouteStatusReasonsModel? = null
    var cancelDialog: Dialog? = null
    var currentRoute: ArrayList<GetRouteAccountResponse>? = null
    private var isSyncing = false

    override fun getLayoutResId(): Int {
        return R.layout.layout_route_map
    }

    override fun onCreateView(
        instance: Bundle?,
        binding: LayoutRouteMapBinding
    ) {
        this.binding = binding
        progressDialog = ProgressDialog(requireActivity())
        initialize()
        bindObservers()
        listeners()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setFragmentResultListener(AppRequestCode.CURRENT_ROUTE_STATUS_CHANGED) { key, bundle ->
////            initialize()
//                currentRoute=  viewModel.currentRoute.value
//                addMarkers(currentRoute!!)
//        }
    }

    private fun listeners() {
        binding.ivCustomers.setOnClickListener {
//            (requireActivity()as MainActivity).route= route
//            (requireActivity()as MainActivity).currentRoute= currentRoute
//            viewModel.currentRoute.value = currentRoute

            navigator.push(RouteVisitFragment::class) {
                val bundle = Bundle()
                bundle.putString(DataTransferKeys.KEY_FROM, "home")
                bundle.putString(DataTransferKeys.KEY_NAME, route?.Route?.title)
                bundle.putString(DataTransferKeys.KEY_STATUS, route?.Route?.status)
                this.arguments = bundle
            }
        }

        binding.imgSync.setOnClickListener {
            if (isConnectedToInternet(requireContext(), true)) {
                isSyncing = true
                homeViewModel.getRouteAccountApi(
                    requireContext(),
                    route?.Route?.id,
                    1
                )
            }
        }
        binding.btnStart.setOnClickListener {

            when (route?.Route?.status) {
                RouteStatus.STATUS_IN_PROGRESS -> {
                    showCancelDialog(RouteStatus.STATUS_PAUSED)
                }
                RouteStatus.STATUS_NOT_STARTED -> {

                    if (viewModel.isActiveRoute.value != true) {
                        if(origin!=null) {
                            origin?.let { origin ->
//                        if (calculateDistance(
//                                origin.latitude,
//                                origin.longitude,
//                                route!!.StartAddress!!.latitude,
//                                route!!.StartAddress!!.longitude
//                            ) > 100
//                        ) {
//                            Toast.makeText(
//                                requireContext(),
//                                getString(R.string.cannot_start),
//                                Toast.LENGTH_LONG
//                            ).show()
//                        } else {
                                isSyncing = false
                                homeViewModel.getRouteAccountApi(
                                    requireContext(),
                                    route?.Route?.id,
                                    routePageCount
                                )
//                        }
                                viewModel.isActiveRoute(true)
                            }
                        }else{
                            checkPermissions()
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.already_active_route),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                RouteStatus.STATUS_PAUSED -> {
                    updateRouteStatus(RouteStatus.STATUS_IN_PROGRESS)
                }
                RouteStatus.STATUS_COMPLETED, RouteStatus.STATUS_CANCELLED, RouteStatus.STATUS_SKIPPED -> {
                }
            }
        }

        binding.btnCancel.setOnClickListener {
            when (route?.Route?.status) {
                RouteStatus.STATUS_NOT_STARTED -> {
                    showCancelDialog(RouteStatus.STATUS_SKIPPED)
                }
                RouteStatus.STATUS_SKIPPED, RouteStatus.STATUS_COMPLETED, RouteStatus.STATUS_CANCELLED -> {
                }
                else -> {
                    var isTaskRequired = false
                    viewModel.currentRoute.value?.forEach { currentRoute ->
                        if (currentRoute.Visit?.Activity?.required_flg == true) {
                            isTaskRequired = true
                        } else {
                            currentRoute.Visit?.Tasks?.forEach { task ->
                                if (task.Activity?.required_flg == true) {
                                    isTaskRequired = true
                                }
                            }
                        }
                    }
                    if (isTaskRequired) {
                        origin?.let { origin ->
                            if (calculateDistance(
                                    origin.latitude,
                                    origin.longitude,
                                    route!!.EndAddress!!.latitude,
                                    route!!.EndAddress!!.longitude
                                ) < 100
                            ) {
                                updateRouteStatus(RouteStatus.STATUS_COMPLETED)
                            } else {
                                showCancelDialog(RouteStatus.STATUS_CANCELLED)
                            }
                        }
                        viewModel.isActiveRoute(false)

                    } else {
                        showAlertDialog(
                            requireContext(),
                            R.string.message_alert,
                            R.string.complete_task_first
                        )
                    }
//                    updateRouteStatus(RouteStatus.STATUS_COMPLETED)
                }
            }
        }

        binding.ivNav.setOnClickListener {
            val bundle = Bundle()
//            (requireActivity()as MainActivity).route= route
//            (requireActivity()as MainActivity).currentRoute= currentRoute
            viewModel.route.value = route
//            viewModel.currentRoute.value = currentRoute
            bundle.putString(DataTransferKeys.KEY_NAME, binding.tvRouteNum.text.toString())
//            bundle.putParcelable(DataTransferKeys.KEY_ROUTES, route)
//            bundle.putParcelableArrayList(DataTransferKeys.KEY_CURRENT_ROUTE,  viewModel.currentRoute.value)
            navigator.push(TrackFragment::class) {
                this.arguments = bundle
            }
        }

        binding.ivNavMarker.setOnClickListener {
            if (map != null && selectedMarker != null) {
                val lat = selectedMarker!!.position.latitude
                val lng = selectedMarker!!.position.longitude
                val title = selectedMarker!!.title
                openGoogleMaps(requireContext(), lat, lng, title)
            }
        }

        binding.ivRecenter.setOnClickListener {
            if (map != null && !latLngList.isNullOrEmpty())
                focusOnRoute()
        }
    }

    public fun updateMarkers(){
        binding.ivNavMarker.gone()
        currentRoute = viewModel.currentRoute.value
        if (!currentRoute.isNullOrEmpty())
            addMarkers(currentRoute!!,true)
    }
    override fun onResume() {
        super.onResume()

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(requireActivity()) { task ->
                    val mLastLocation: Location? = task.result
                    if (mLastLocation != null) {
                        val latLng = LatLng(mLastLocation.latitude, mLastLocation.longitude)
                        origin = latLng
                        currentLoc = latLng
                        map?.addMarker(
                            MarkerOptions().icon(
                                BitmapFromVector(
                                    requireContext(),
                                    R.drawable.ic_car
                                )
                            ).position(latLng).flat(true)
                        )
                    }
                }
            } else {
//                Toast.makeText(
//                    requireContext(),
//                    R.string.we_will_need_your_location,
//                    Toast.LENGTH_LONG
//                ).show()
//                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
//                if (requireActivity().packageName.equals(BuildConfig.APPLICATION_ID))
//                    startActivity(intent)
            }

        }

    }

    private fun initialize() {

        polyLineStringList = arrayListOf()
        lifecycleScope.launchWhenCreated {
            val mapFragment: SupportMapFragment? =
                childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
            val googleMap: GoogleMap? = mapFragment?.awaitMap()
            map = googleMap
            route?.Accounts?.let { addMarkers(it) }
            map?.uiSettings?.isMapToolbarEnabled = false
            map?.setOnInfoWindowClickListener(this@HomeRouteFragment)
            map?.setOnMarkerClickListener(this@HomeRouteFragment)
            map?.setOnMapClickListener(this@HomeRouteFragment)
        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        checkPermissions()

        binding.tvRouteName.text = route?.Route?.title ?: ""
        binding.tvRouteNum.text = routeName

        route?.Route?.status?.let { setCurrentStatus(it) }

        route?.Route?.polyline?.let { line ->
            if (line.isNotEmpty()) {
                val lineList = line.split(",")
                polyLineStringList?.addAll(lineList)
            }
        }
        route?.Route?.trip_distance?.let {
            totalDistance = it
        }
        route?.Route?.trip_duration?.let {
            totalDuration = it
        }

//        if (totalDistance!=null && totalDuration!=null){
//            viewModel.currentRouteDistance.value= Pair(totalDistance?:0.0,totalDuration?:0.0)
//        }
//        if (isConnectedToInternet(requireContext(),false)) {
//            homeViewModel.getRouteAccountApi(requireContext(), route?.Route?.id)
//        }else{

//        if (route?.Route?.status != RouteStatus.STATUS_NOT_STARTED) {
        lifecycleScope.launchWhenCreated {
            routeActivityDao.getCurrentRoutes(route?.Route?.id ?: -1).let {
                if (!it.isNullOrEmpty()) {
                    currentPage = 1
                    currentRoute = it as ArrayList<GetRouteAccountResponse>
                    var todayRoutes = viewModel.todayAllRoutes.value ?: arrayListOf()
                    todayRoutes.add(currentRoute!!)
                    viewModel.todayAllRoutes.value = todayRoutes
                    addMarkers(it)
                } else {
                    route?.Accounts?.let {

                        currentPage = 1
                        currentRoute = it
                        var todayRoutes = viewModel.todayAllRoutes.value ?: arrayListOf()
                        todayRoutes.add(currentRoute!!)
                        viewModel.todayAllRoutes.value = todayRoutes

                        lifecycleScope.launchWhenCreated {
                            routeActivityDao.deleteRoute(route?.Route?.id)
                        }
                        it.forEach { account ->
                            account.routeId = route?.Route?.id
                            lifecycleScope.launchWhenCreated {
//                                routeActivityDao.deleteRoute(account.routeId)
                                routeActivityDao.insert(account)
                            }
                        }
                    }
                }

                if ((route?.Route?.status != RouteStatus.STATUS_NOT_STARTED)
                    && (currentRoute?.get(0)?.ProductAssortment.isNullOrEmpty())
                    && (currentRoute?.get(0)?.Orders.isNullOrEmpty())
                    && (currentRoute?.get(0)?.PendingPayments.isNullOrEmpty())
                    && (currentRoute?.get(0)?.Promotions.isNullOrEmpty())
                    && (currentRoute?.get(0)?.Surveys.isNullOrEmpty())
                ) {
                    if (isConnectedToInternet(requireContext(), true)) {
                        isSyncing = true
                        homeViewModel.getRouteAccountApi(
                            requireContext(),
                            route?.Route?.id,
                            routePageCount
                        )
                    }
                }
            }
        }

//        }else {
//            }
//        }
//        }
    }


    private fun bindObservers() {

        homeViewModel.updateRouteApiResponse.setObserver(viewLifecycleOwner, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.LOADING -> {
                    progressDialog.setLoading(true)
                }
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)
                }
                Status.ERROR -> {
                    progressDialog.setLoading(false)
                    ApisRespHandler.handleError(
                        it.error,
                        requireActivity(),
                        prefsManager = prefsManager
                    )
                }
            }
        })

        viewModel.route.observe(viewLifecycleOwner, Observer {
            it ?: return@Observer
            if (it.Route?.id == route?.Route?.id) {
                setCurrentStatus(it.Route!!.status!!)
//                route?.Accounts?.let { it1 -> addMarkers(it1) }
            }
        })

        homeViewModel.getRouteAccountResponse.setObserver(viewLifecycleOwner, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.LOADING -> {
//                    binding.llProgress.visible()
                    progressDialog.setLoading(true)
//                    disableClicks()
                }
                Status.SUCCESS -> {
                    if (it.data != null) {
                        routePageCount++

                        it.data.rows?.forEach { account ->
                            val index =
                                currentRoute?.indexOfFirst { it.Account?.id == account.Account?.id }
                            if (index != null) {
                                if (index > -1) {
                                    currentRoute!![index] = account
                                } else {
                                    currentRoute?.add(account)
                                }
                            }
                        }

                        if (routePageCount < (it.data.pagination?.pages ?: 1)) {

//                            isSyncing = true
                            homeViewModel.getRouteAccountApi(
                                requireContext(),
                                route?.Route?.id,
                                routePageCount
                            )
                        } else {
                            progressDialog.setLoading(false)

//                            binding.llProgress.gone()
                            enableClicks()
                            currentPage = 1

                            lifecycleScope.launchWhenCreated {
                                routeActivityDao.deleteRoute(route?.Route?.id)
                            }
                            currentRoute!!.forEach { account ->
                                account.routeId = route?.Route?.id
                                lifecycleScope.launchWhenCreated {
                                    account.id = routeActivityDao.insert(account)
                                }
                            }

//                            if (route?.Route?.status == RouteStatus.STATUS_NOT_STARTED) {

                            var todayRoutes =
                                viewModel.todayAllRoutes.value ?: arrayListOf()
                            todayRoutes.add(currentRoute!!)
                            viewModel.todayAllRoutes.value = todayRoutes
                            viewModel.currentRoute.value = currentRoute

                            route?.Accounts = currentRoute
                            polyLineStringList = null

                            if (!isSyncing) {
                                updateRouteStatus(RouteStatus.STATUS_IN_PROGRESS)
                            }
                            route?.Accounts?.let { it1 -> addMarkers(it1) }


//                            }
                        }

//                        viewModel.currentRoute.value = currentRoute
//                        lifecycleScope.launchWhenCreated {
//                            viewModel.addRouteList(it.data)
//                        }

                    } else {
                    }
                }
                Status.ERROR -> {
//                    binding.llProgress.gone()
                    progressDialog.setLoading(true)
                    enableClicks()
                    ApisRespHandler.handleError(
                        it.error,
                        requireActivity(),
                        prefsManager = prefsManager
                    )
                }
            }
        })

        homeViewModel.getPolyLineResponse.setObserver(viewLifecycleOwner, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.LOADING -> {
//                    progressDialog.setLoading(true)
                }
                Status.SUCCESS -> {
//                    progressDialog.setLoading(false)
                    if (it.data.isNullOrEmpty()) {
                    } else {
                        val responsePolyline = it.data
                        val jsonRootObject = JSONObject(responsePolyline)
                        polyLine(jsonRootObject)
                        if (currentPage < pageCount) {
                            var wayString = ""
                            var origin: LatLng
                            var destination: LatLng
                            if (((currentPage + 1) * 25) < latLngList.size) {
                                destination = latLngList[(currentPage + 1) * 25]
                                origin = latLngList[(currentPage * 25)]
                                wayString = "optimize:false"
                                for (i in ((currentPage * 25)) until ((currentPage + 1) * 25)) {
                                    wayString += "|${latLngList[i].latitude},${latLngList[i].longitude}"
                                }
                            } else {
                                destination = LatLng(
                                    route!!.EndAddress!!.latitude,
                                    route!!.EndAddress!!.longitude
                                )
                                origin = latLngList[(currentPage * 25)]
                                val ways = "optimize:false"
                                for (i in ((currentPage * 25) + 2) until latLngList.size) {
                                    wayString += "|${latLngList[i].latitude},${latLngList[i].longitude}"
                                }
                                if (wayString.isNotBlank()) {
                                    wayString = ways + wayString
                                }
                            }

                            Log.e("wayPoints: ", wayString)
                            Log.e("origin: ", origin.toString())
                            Log.e("destination: ", destination.toString())

                            homeViewModel.drawPolyLineApi(
                                origin.latitude,
                                origin.longitude,
                                destination.latitude,
                                destination.longitude,
                                crmapp!!.gmapsid!!,
                                wayString
                            )
                        }
                        currentPage++
                    }
                }
                Status.ERROR -> {
//                    progressDialog.setLoading(false)
                    ApisRespHandler.handleError(
                        it.error,
                        requireActivity(),
                        prefsManager = prefsManager
                    )

                }
            }
        })
    }


    private fun disableClicks() {
        binding.btnCancel.alpha = 0.4f
        binding.btnStart.alpha = 0.4f
        binding.ivCustomers.alpha = 0.4f
        binding.ivNavMarker.alpha = 0.4f
        binding.tvRouteName.alpha = 0.4f
        binding.tvRouteNum.alpha = 0.4f
        binding.ivRecenter.alpha = 0.4f
        binding.imgSync.alpha = 0.4f
        binding.cvMap.alpha = 0.4f
        binding.ivNav.alpha = 0.4f
        binding.tvTodayRoute.alpha = 0.4f

        binding.btnCancel.isClickable = false
        binding.btnStart.isClickable = false
        binding.ivCustomers.isClickable = false
        binding.ivNavMarker.isClickable = false
        binding.tvRouteName.isClickable = false
        binding.tvRouteNum.isClickable = false
        binding.ivRecenter.isClickable = false
        binding.imgSync.isClickable = false
        binding.cvMap.isClickable = false
        binding.ivNav.isClickable = false
        binding.tvTodayRoute.isClickable = false
    }

    private fun enableClicks() {
        binding.btnCancel.alpha = 1f
        binding.btnStart.alpha = 1f
        binding.ivCustomers.alpha = 1f
        binding.ivNavMarker.alpha = 1f
        binding.tvRouteName.alpha = 1f
        binding.tvRouteNum.alpha = 1f
        binding.ivRecenter.alpha = 1f
        binding.imgSync.alpha = 1f
        binding.cvMap.alpha = 1f
        binding.ivNav.alpha = 1f
        binding.tvTodayRoute.alpha = 1f

        binding.btnCancel.isClickable = true
        binding.btnStart.isClickable = true
        binding.ivCustomers.isClickable = true
        binding.ivNavMarker.isClickable = true
        binding.tvRouteName.isClickable = true
        binding.tvRouteNum.isClickable = true
        binding.ivRecenter.isClickable = true
        binding.imgSync.isClickable = true
        binding.cvMap.isClickable = true
        binding.ivNav.isClickable = true
        binding.tvTodayRoute.isClickable = true

    }

    private fun updateRouteStatus(status: String, reason: String? = null) {
        var startDate = ""
        var endDate = ""
        if (!route?.Route?.actual_startdate.isNullOrEmpty()) {
            startDate = route!!.Route!!.actual_startdate!!
        } else if (status == RouteStatus.STATUS_IN_PROGRESS) {
            startDate = DateUtils.getCurrentDateWithFormat(DateFormat.DATE_FORMAT_RENEW)
        }
        if (!route?.Route?.actual_enddate.isNullOrEmpty()) {
            endDate = route!!.Route!!.actual_enddate!!
        } else if (status == RouteStatus.STATUS_COMPLETED || status == RouteStatus.STATUS_CANCELLED || status == RouteStatus.STATUS_SKIPPED) {
            endDate = DateUtils.getCurrentDateWithFormat(DateFormat.DATE_FORMAT_RENEW)
        }
        val routeData = RouteData(
            id = route?.Route?.id ?: 0,
            lov_route_exec_status = status,
            startdate = startDate,
            enddate = endDate,
            lov_route_exec_status_reason = reason
        )
        val routeList = arrayListOf<UpdateRoute>()
        routeList.add(UpdateRoute(routeData))
//        viewModel.updateRouteApi(requireContext(), routeList)
        route?.Route?.status = status
        viewModel.setSelectedRoute(route!!)
        if (status == RouteStatus.STATUS_IN_PROGRESS) {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
            val bundle = Bundle()
            bundle.putString(DataTransferKeys.KEY_NAME, binding.tvRouteNum.text.toString())
            navigator.push(TrackFragment::class) {
                this.arguments = bundle
            }
        } else if (status == RouteStatus.STATUS_CANCELLED || status == RouteStatus.STATUS_COMPLETED) {
            sendCommandToService(ACTION_STOP_SERVICE)
        }
        if (isConnectedToInternet(requireContext(), false)) {
            homeViewModel.updateRouteApi(requireContext(), routeList)
        } else {
            lifecycleScope.launchWhenCreated {
                val result = updateRouteDao.insert(routeList)
                Timber.e(result.toString())
            }
        }
        //        (requireActivity()as MainActivity).route= route
//        setCurrentStatus(status)
    }

    private fun updateRoutePolyline(routePolyline: String, distance: Double, duration: Double) {
        val routeData = RouteData(
            id = route?.Route?.id ?: 0,
            trip = routePolyline,
            lov_route_exec_status= route?.Route?.status,
            trip_distance = distance.toString(),
            trip_duration = duration.toString()
        )
        val routeList = arrayListOf<UpdateRoute>()
        routeList.add(UpdateRoute(routeData))
        if (isConnectedToInternet(requireContext(), false)) {
            homeViewModel.updateRouteApi(requireContext(), routeList)
        } else {
            lifecycleScope.launchWhenCreated {
                val result = updateRouteDao.insert(routeList)
                Timber.e(result.toString())
            }
        }
        route?.Route?.polyline = routePolyline
        route?.Route?.trip_distance = distance
        route?.Route?.trip_duration = duration
        viewModel.setSelectedRoute(route!!)
    }

    private fun setCurrentStatus(status: String) {
        Log.e("CurrentStatus: ", status)
        when (status) {
            RouteStatus.STATUS_IN_PROGRESS -> {
                binding.btnStart.text = getString(R.string.pause)
                binding.btnCancel.text = getString(R.string.end)
            }
            RouteStatus.STATUS_NOT_STARTED -> {
                binding.btnStart.text = getString(R.string.start)
                binding.btnCancel.text = getString(R.string.cancel)
            }
            RouteStatus.STATUS_PAUSED -> {
                binding.btnStart.text = getString(R.string.resume)
                binding.btnCancel.text = getString(R.string.end)
            }
            RouteStatus.STATUS_COMPLETED, RouteStatus.STATUS_CANCELLED -> {
//                binding.btnStart.invisible()
//                binding.btnCancel.invisible()
                binding.btnStart.text = getString(R.string.start)
                binding.btnStart.alpha = 0.5f
                binding.btnCancel.text = getString(R.string.route_ended)
            }
            RouteStatus.STATUS_SKIPPED -> {
                binding.btnStart.text = getString(R.string.start)
                binding.btnStart.alpha = 0.5f
                binding.btnCancel.text = getString(R.string.route_skipped)
            }
        }
    }

    private fun addMarkers(routes: ArrayList<GetRouteAccountResponse>, isRefresh:Boolean=false) {
        if (map != null && !routes.isNullOrEmpty()) {
            map?.clear()
            latLngList.clear()

            var wayString = "optimize:false"

            routes.forEachIndexed { index, route ->
                val latLng = LatLng(
                    route.DeliveryAddress?.latitude ?: 0.0,
                    route.DeliveryAddress?.longitude ?: 0.0
                )
                val icon =
                    if (route.Visit?.Activity?.lov_activity_status == RouteStatus.STATUS_COMPLETED
                        || route.Visit?.Activity?.lov_activity_status == RouteStatus.STATUS_CANCELLED
                    ) {
                        BitmapMarker(
                            requireContext(),
                            R.drawable.ic_flaggreen,
                            (route.Account?.orderseq ?: 0).toString()
                        )
                    } else if (route.Visit?.Activity?.lov_activity_status == RouteStatus.STATUS_NOT_STARTED) {
                        BitmapMarker(
                            requireContext(),
                            R.drawable.ic_flaggrey,
                            (route.Account?.orderseq ?: 0).toString()
                        )
                    } else {
                        BitmapMarker(
                            requireContext(),
                            R.drawable.ic_flag,
                            (route.Account?.orderseq ?: 0).toString()
                        )
                    }

                map?.addMarker(
                    MarkerOptions().position(latLng) // below line is use to add custom marker on our map.
                        .icon(icon)
                        .title(route.Account?.accountname)
                        .snippet(getAddress(route.DeliveryAddress))
                )?.tag = route.Account?.id.toString()

                latLngList.add(latLng)
                if (index < 25 && index < routes.size)
                    wayString += "|${latLng.latitude},${latLng.longitude}"
            }

//            currentLoc
//            map?.addMarker(MarkerOptions())
            if (currentLoc != null) {
                map?.addMarker(
                    MarkerOptions().icon(
                        BitmapFromVector(
                            requireContext(),
                            R.drawable.ic_car
                        )
                    ).position(currentLoc).flat(true)
                )
            } else {
            }

            pageCount = getPageCount()

            val origin = LatLng(route!!.StartAddress!!.latitude, route!!.StartAddress!!.longitude)
            val dest = LatLng(route!!.EndAddress!!.latitude, route!!.EndAddress!!.longitude)

            if (pageCount <= 1) {
                destination = LatLng(route!!.EndAddress!!.latitude, route!!.EndAddress!!.longitude)
            } else {
                destination = latLngList[25]
            }

            if (origin == dest && (route!!.Route!!.status == RouteStatus.STATUS_NOT_STARTED || route!!.Route!!.status == RouteStatus.STATUS_PENDING)) {
                map?.addMarker(
                    MarkerOptions().position(origin) // below line is use to add custom marker on our map.
                        .title(getString(R.string.start_point))
                        .icon(
                            BitmapFromVector(
                                requireContext(),
                                R.drawable.ic_green
                            )
                        )
                )
            } else if (origin == dest) {
                map?.addMarker(
                    MarkerOptions().position(dest) // below line is use to add custom marker on our map.
                        .title(getString(R.string.end_point))
                        .icon(
                            BitmapFromVector(
                                requireContext(),
                                R.drawable.ic_grey
                            )
                        )
                )
            } else {
                map?.addMarker(
                    MarkerOptions().position(origin) // below line is use to add custom marker on our map.
                        .title(getString(R.string.start_point))
                        .icon(
                            BitmapFromVector(
                                requireContext(),
                                R.drawable.ic_green
                            )
                        )
                )
                map?.addMarker(
                    MarkerOptions().position(dest) // below line is use to add custom marker on our map.
                        .title(getString(R.string.end_point))
                        .icon(
                            BitmapFromVector(
                                requireContext(),
                                R.drawable.ic_grey
                            )
                        )
                )
            }
            Log.e("wayPoints: ", wayString)
            Log.e("origin: ", origin.toString())
            Log.e("destination: ", destination.toString())

            if (origin != null && destination != null && polyLineStringList.isNullOrEmpty() && !isRefresh) {
                homeViewModel.drawPolyLineApi(
                    origin.latitude,
                    origin.longitude,
                    destination!!.latitude,
                    destination!!.longitude,
                    crmapp!!.gmapsid!!,
                    wayString
                )
            } else if (!polyLineStringList.isNullOrEmpty() || isRefresh) {
                showRoutePolyline()
            }

        }
    }

    private fun sendCommandToService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }

    private fun showRoutePolyline() {
        polyLineStringList?.forEach { encodedString ->
            val line = map?.addPolyline(
                PolylineOptions().addAll(decodePoly(encodedString)).width(15f)
                    .color(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                    .geodesic(true)
            )
        }
        focusOnRoute()
    }

    private fun getPageCount(): Int {
        var pages = 1
        if ((latLngList.size % 25) == 0)
            pages = latLngList.size / 25
        else
            pages = latLngList.size / 25 + 1
        return pages
    }

    fun polyLine(jsonRootObject: JSONObject) {

        val routeArray = jsonRootObject.getJSONArray("routes")
        if (routeArray.length() == 0) {
            return
        }
        val routes = routeArray.getJSONObject(0)
        val overviewPolyLines = routes.getJSONObject("overview_polyline")
        val encodedString = overviewPolyLines.getString("points")
        polyLineStringList?.add(encodedString)
        var totalDistance = 0.0
        var totalDuration = 0.0
        val legsArray = (routes.get("legs") as JSONArray)
        for (i in 0 until legsArray.length()) {
            val legsJsonObject = (((routes.get("legs") as JSONArray).get(i) as JSONObject))
            val distance = legsJsonObject.getJSONObject("distance");
            val duration = legsJsonObject.getJSONObject("duration");
            Timber.i("Distance String: $distance");
            val dist: Double = (distance.getInt("value")).toDouble()
            val dur: Double = (duration.getInt("value")).toDouble()
            totalDistance += (dist / 1000)
            totalDuration += dur
        }

        this.totalDistance = totalDistance
        this.totalDuration = totalDuration

        //        val dist =  (distance.getDouble("value"))
        Timber.e("Distance Double: $totalDistance")

        line = map?.addPolyline(
            PolylineOptions().addAll(decodePoly(encodedString)).width(15f)
                .color(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                .geodesic(true)
        )

        focusOnRoute()
        var updatePolyline = ""
        polyLineStringList?.forEach {
            updatePolyline += "$it,"
        }
        updatePolyline.removeSuffix(",")
        updateRoutePolyline(updatePolyline, totalDistance, totalDuration)
    }

    private fun focusOnRoute() {
        val builder = LatLngBounds.Builder()
        latLngList.forEach {
            builder.include(it)
        }
        val bounds = builder.build()
        val padding = 50
        /**create the camera with bounds and padding to set into map*/
        /**create the camera with bounds and padding to set into map */
        val cu = CameraUpdateFactory.newLatLngBounds(bounds, padding)
        map?.moveCamera(cu);
    }

    override fun onMarkerClick(p0: Marker): Boolean {
        selectedMarker = p0
        binding.ivNavMarker.visible()
        return false
    }

    override fun onMapClick(p0: LatLng) {
        selectedMarker = null
        binding.ivNavMarker.gone()
    }

    @SuppressLint("MissingPermission")
    private fun checkPermissions() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(requireActivity()) { task ->
                    val mLastLocation: Location? = task.result
                    if (mLastLocation != null) {
                        val latLng = LatLng(mLastLocation.latitude, mLastLocation.longitude)
                        origin = latLng
                        currentLoc = latLng
                        map?.addMarker(
                            MarkerOptions().icon(
                                BitmapFromVector(
                                    requireContext(),
                                    R.drawable.ic_car
                                )
                            ).position(latLng).flat(true)
                        )
                    }
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    R.string.we_will_need_your_location,
                    Toast.LENGTH_LONG
                ).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                if (requireActivity().packageName.equals(BuildConfig.APPLICATION_ID))
                    startActivity(intent)
            }

        } else {
            getLocationWithPermissionCheck()
        }
    }


    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
        Log.e("Child Fragment :", "onRequestPermissionsResult: $grantResults ")
    }


    @NeedsPermission(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )

    fun getLocation() {
        checkPermissions()
    }

    @OnShowRationale(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    fun showLocationRationale(request: PermissionRequest) {
        PermissionUtils.showRationalDialog(
            requireContext(),
            R.string.we_will_need_your_location,
            request
        )
    }

    @OnNeverAskAgain(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )

    fun onNeverAskAgainRationale() {
        PermissionUtils.showAppSettingsDialog(
            requireContext(), R.string.we_will_need_your_location
        )
    }

    @OnPermissionDenied(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )

    fun showDeniedForStorage() {
        PermissionUtils.showAppSettingsDialog(
            requireContext(), R.string.we_will_need_your_location
        )
    }

    override fun onInfoWindowClick(marker: Marker) {
        if (!(marker.title == getString(R.string.start_point) || marker.title == getString(R.string.end_point))) {
            val id = marker.tag
            val visits = viewModel.currentRoute.value?.find { it.Account?.id.toString() == id }

//        (requireActivity()as MainActivity).currentRoute= currentRoute
//        (requireActivity()as MainActivity).route= route

            val bundle = Bundle()
            bundle.putParcelable(DataTransferKeys.KEY_TASKS, visits)
            bundle.putString(DataTransferKeys.KEY_FROM, "home")
            bundle.putString(
                DataTransferKeys.KEY_STATUS,
                visits?.Visit?.Activity?.lov_activity_status
            )
            bundle.putString(DataTransferKeys.KEY_ROUTE_STATUS, route?.Route?.status)
            navigator.push(TaskFragment::class) {
                this.arguments = bundle
            }
        }
    }

//    override fun onCancelPauseClick(status: String, reason: String) {
//        updateRouteStatus(status,reason)
//    }

    private fun showCancelDialog(status: String) {
        if (cancelDialog == null) {
            val bottomSheet = layoutInflater.inflate(R.layout.dialog_rejection, null)
            cancelDialog = Dialog(requireContext(), R.style.CustomDialog)
            cancelDialog?.setContentView(bottomSheet)
            cancelDialog?.setCancelable(true)
        }
        val reasons = prefsManager.getObject(
            PrefsManager.APP_FORM_CATALOG,
            GetFormCatalogResponse::class.java
        )
        routeStatusReasons = reasons?.RouteStatusReasons
        val rvReasons = cancelDialog?.findViewById<RecyclerView>(R.id.rvOrders)
        rvReasons?.layoutManager = LinearLayoutManager(requireContext())
        adapter = CancelReasonAdapter(this)
        rvReasons?.adapter = adapter
        adapter.notifyAdapter(routeStatusReasons)
        val etReason = cancelDialog?.findViewById<EditText>(R.id.etReason)
        cancelDialog?.findViewById<AppCompatButton>(R.id.btnApply)?.setOnClickListener {
            if (selectedReason?.code == "Other") {
                if (ValidationUtils.isFieldNullOrEmpty(etReason?.text.toString())) {

                } else {
                    updateRouteStatus(status, etReason?.text.toString())
                    cancelDialog?.dismiss()
                }
            } else {
                updateRouteStatus(status, selectedReason?.code)
                cancelDialog?.dismiss()
            }
        }
        cancelDialog?.findViewById<ImageButton>(R.id.btnCancel)?.setOnClickListener {
            cancelDialog?.dismiss()
        }
        cancelDialog?.show()
    }

    override fun onReasonSelect(position: Int) {
        routeStatusReasons?.forEachIndexed { index, reason ->
            reason.isSelected = index == position
        }
        selectedReason = routeStatusReasons?.get(position)

        if (routeStatusReasons?.get(position)?.code == "Other") {
            cancelDialog?.findViewById<EditText>(R.id.etReason)?.visible()
        } else {
            cancelDialog?.findViewById<EditText>(R.id.etReason)?.gone()
        }
        adapter.notifyAdapter(routeStatusReasons)
    }
}
