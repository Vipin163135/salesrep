package com.salesrep.app.ui.route

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.github.fragivity.dialog.showDialog
import com.github.fragivity.navigator
import com.github.fragivity.pop
import com.github.fragivity.push
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.maps.android.PolyUtil
import com.google.maps.android.ktx.awaitMap
import com.salesrep.app.BuildConfig
import com.salesrep.app.R
import com.salesrep.app.base.BaseFragment
import com.salesrep.app.dao.AppDatabase
import com.salesrep.app.dao.RouteActivityDao
import com.salesrep.app.dao.UpdateRouteDao
//import com.salesrep.app.dao.UserTrackDao
import com.salesrep.app.data.appConfig.Crmapp
import com.salesrep.app.data.models.AddressTemplate
import com.salesrep.app.data.models.requests.RouteData
import com.salesrep.app.data.models.requests.UpdateRoute
import com.salesrep.app.data.models.response.*
import com.salesrep.app.data.network.responseUtil.ApisRespHandler
import com.salesrep.app.data.network.responseUtil.Status
import com.salesrep.app.data.repos.UserRepository
import com.salesrep.app.databinding.FragmentNavigateBinding
import com.salesrep.app.services.TrackingService
import com.salesrep.app.ui.dialogs.ProgressDialog
import com.salesrep.app.ui.home.HomeViewModel
import com.salesrep.app.util.*
import com.salesrep.app.util.AppRequestCode.CANCEL_ROUTE_REQUEST
import com.salesrep.app.util.MarkerAnimation.calculateBearing
import com.salesrep.app.util.PermissionUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_navigate.*
import kotlinx.coroutines.launch
import org.json.JSONObject
import permissions.dispatcher.*
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList


@RuntimePermissions
@AndroidEntryPoint
class TrackFragment : BaseFragment<FragmentNavigateBinding>(),
    GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMarkerClickListener,
    GoogleMap.OnMapClickListener {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var updateRouteDao: UpdateRouteDao

//    @Inject
//    lateinit var userTrackDao: UserTrackDao

    private lateinit var binding: FragmentNavigateBinding
    override val viewModel by activityViewModels<HomeViewModel>()
    private lateinit var progressDialog: ProgressDialog
    private var map: GoogleMap? = null
    private var offRouteDialogShown = false

    private var origin: LatLng? = null
    private var destination: LatLng? = null
    private var latLngList = ArrayList<LatLng>()

    private var routePageCount = 1

//    private var userTrackLatLng= ArrayList<TrackTemplate>()

//            by lazy {
//                arguments?.getParcelableArrayList<GetRouteAccountResponse>(
//                    DataTransferKeys.KEY_CURRENT_ROUTE
//                )
//            }

    lateinit var mFusedLocationClient: FusedLocationProviderClient
    var currentLocationMarker: Marker? = null
    lateinit var currentLocation: Location
    var firstTimeFlag = true
    private var polyLineList: ArrayList<Polyline>? = null
    private var polyLineStringList: ArrayList<String>? = null
    private var startPolyLineStringList: ArrayList<String>? = null
    private var customerPolyLine: Polyline? = null
    private val crmapp by lazy {
        prefsManager.getObject(
            PrefsManager.APP_CONFIG,
            Crmapp::class.java
        )
    }
    private var selectedMarker: Marker? = null

    private val routeName by lazy { arguments?.getString(DataTransferKeys.KEY_NAME) }

//    private val currentRoute by lazy { arguments?.getParcelableArrayList<GetRouteAccountResponse>(DataTransferKeys.KEY_CURRENT_ROUTE) }
//    private val route by lazy { arguments?.getParcelable<Route>(DataTransferKeys.KEY_ROUTES) }
//    private var startPolylineObject: JSONObject? = null

    private var pageCount = 1
    private var currentPage = 1
    private var isOffRouteSubmitted = true
    private var isStartLineVisible = false
    private var isCustomerLineVisible = false
    private var routeId = -1

    @Inject
    lateinit var routeActivityDao: RouteActivityDao

    //    var currentRoute: ArrayList<>?= null

    override fun getLayoutResId(): Int {
        return R.layout.fragment_navigate
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(CANCEL_ROUTE_REQUEST) { key, bundle ->
            if (bundle.get(DataTransferKeys.KEY_CANCEL_STATUS) != null) {

                val status = bundle.getString(DataTransferKeys.KEY_CANCEL_STATUS, "")
                val reason = bundle.getString(DataTransferKeys.KEY_REASON)
                if (status == RouteStatus.STATUS_OFFROUTE) {
                    isOffRouteSubmitted = false
                    updateRouteStatus(RouteStatus.STATUS_PAUSED, reason)
                } else {
                    updateRouteStatus(status, reason)
                }
            }
        }


//        setFragmentResultListener(AppRequestCode.SELECT_CUSTOMER_NAV_REQUEST) { key, bundle ->
//            if (bundle.get(KEY_CUSTOMER_NAV) != null) {
//                val navRoute= bundle.getParcelable<GetRouteAccountResponse>(KEY_CUSTOMER_NAV)
//                isCustomerLineVisible=true
//                if (map!=null) {
//                    map?.clear()
//                    if(origin!=null) {
//                        currentLocationMarker = map?.addMarker(
//                            MarkerOptions().icon(
//                                BitmapFromVector(
//                                    requireContext(),
//                                    R.drawable.ic_car
//                                )
//                            ).position(origin).flat(true)
//                        )
//                    }else{
//                        currentLocationMarker=null
//                    }
//                    setCustomerPolyline(navRoute!!)
//                    binding.ivNav.visible()
//                    binding.ivNav.setImageResource(com.google.android.gms.location.places.R.drawable.places_ic_clear)
//                }
//            }
//        }

        setFragmentResultListener(AppRequestCode.CURRENT_ROUTE_TRACK_STATUS_CHANGED) { key, bundle ->
//            initialize()

            if (map != null) {
                map?.clear()
                if (origin != null) {
                    currentLocationMarker = map?.addMarker(
                        MarkerOptions().icon(
                            BitmapFromVector(
                                requireContext(),
                                R.drawable.ic_car
                            )
                        ).position(origin).flat(true)
                    )
                } else {
                    currentLocationMarker = null
                }
                viewModel.currentRoute.value?.let { addMarkers(it) }
            }
            setFragmentResult(
                AppRequestCode.CURRENT_ROUTE_STATUS_CHANGED,
                bundleOf(
                    Pair(DataTransferKeys.KEY_ROUTE_STATUS, "")
                )
            )
        }
    }

    override fun onCreateView(
        instance: Bundle?,
        binding: FragmentNavigateBinding
    ) {
        this.binding = binding

        progressDialog = ProgressDialog(requireActivity())
        initialize()
        polyLineList = arrayListOf()
        polyLineStringList = arrayListOf()
        startPolyLineStringList = arrayListOf()
        bindObservers()
        listeners()
    }

    private fun listeners() {
        binding.ivCustomers.setOnClickListener {
            val bundle = Bundle()
            bundle.putString(DataTransferKeys.KEY_STATUS, viewModel.route.value?.Route?.status)
            bundle.putString(DataTransferKeys.KEY_NAME, viewModel.route.value?.Route?.title)
            bundle.putString(DataTransferKeys.KEY_FROM, "track")
            bundle.putParcelableArrayList(
                DataTransferKeys.KEY_CURRENT_ROUTE,
                viewModel.currentRoute.value
            )
            navigator.push(RouteVisitFragment::class) {
                this.arguments = bundle
            }
        }


        binding.ivBack.setOnClickListener {
            navigator.pop()
        }


//        binding.ivNav.setOnClickListener {
//            if (map!=null) {
//                map?.clear()
//                if(origin!=null) {
//                    currentLocationMarker = map?.addMarker(
//                        MarkerOptions().icon(
//                            BitmapFromVector(
//                                requireContext(),
//                                R.drawable.ic_car
//                            )
//                        ).position(origin).flat(true)
//                    )
//                }else{
//                    currentLocationMarker=null
//                }
//                if (isStartLineVisible) {
//                    isStartLineVisible = false
//                    map?.uiSettings?.isMapToolbarEnabled= false
//                    map?.setOnMarkerClickListener(this)
//                    map?.setOnMapClickListener(this)
//                    map?.setOnInfoWindowClickListener(this@TrackFragment)
//                    viewModel.currentRoute.value?.let {
//                        currentPage = 1
//                        addMarkers(it)
//                        focusOnRoute()
//                    }
//                    binding.ivNav.setImageResource(R.drawable.ic_track)
////                }
////                else  if (isCustomerLineVisible) {
////                    isCustomerLineVisible = false
////                    map?.uiSettings?.isMapToolbarEnabled= false
////                    map?.setOnInfoWindowClickListener(this@TrackFragment)
////                    viewModel.currentRoute.value?.let {
////                        currentPage = 1
////                        addMarkers(it)
////                        focusOnRoute()
////                    }
////                    binding.ivNav.gone()
//                } else {
//                    isStartLineVisible = true
//                    setStartPolyline()
//                    focusOnStartRoute( viewModel.route.value!!.StartAddress!!)
//                    binding.ivNav.setImageResource(com.google.android.gms.location.places.R.drawable.places_ic_clear)
//                }
//            }
//        }
        binding.ivRecenter.setOnClickListener {
            if (map != null && currentLocation != null)
                animateCamera(currentLocation)
//            if (map != null && !latLngList.isNullOrEmpty())
//                focusOnRoute()
        }
        binding.ivShowRoute.setOnClickListener {
            if (map != null) {
                if (isStartLineVisible) {
                    isStartLineVisible = false
                    map?.uiSettings?.isMapToolbarEnabled = false
                    map?.setOnMarkerClickListener(this)
                    map?.setOnMapClickListener(this)
                    map?.setOnInfoWindowClickListener(this@TrackFragment)
                    viewModel.currentRoute.value?.let {
                        currentPage = 1
                        addMarkers(it)
                        focusOnRoute()
                    }
//                    binding.ivNav.setImageResource(R.drawable.ic_track)
                } else {
                    focusOnRoute()
                }
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
        binding.btnStart.setOnClickListener {
            when (viewModel.route.value?.Route?.status) {
                RouteStatus.STATUS_IN_PROGRESS -> {
                    navigator.showDialog(
                        CancelRouteDialog::class,
                        bundleOf(Pair(DataTransferKeys.KEY_STATUS, RouteStatus.STATUS_PAUSED))
                    )
                }
                RouteStatus.STATUS_NOT_STARTED -> {
                    if (viewModel.isActiveRoute.value != true) {
                        origin?.let { origin ->
//                        if (calculateDistance(
//                                origin.latitude,
//                                origin.longitude,
//                                viewModel.route.value!!.StartAddress!!.latitude,
//                                viewModel.route.value!!.StartAddress!!.longitude
//                            ) > 100
//                        ) {
//                            Toast.makeText(
//                                requireContext(),
//                                getString(R.string.cannot_start),
//                                Toast.LENGTH_LONG
//                            ).show()
//                        } else {
                            viewModel.getRouteAccountApi(
                                requireContext(),
                                routeId,
                                routePageCount
                            )

                            viewModel.isActiveRoute(true)

//                        }
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
//                    if (checkOutRoute())
                    updateRouteStatus(RouteStatus.STATUS_IN_PROGRESS)
//                    else {
//                        Toast.makeText(
//                            requireContext(),
//                            getString(R.string.only_resume_on_route),
//                            Toast.LENGTH_LONG
//                        ).show()
//                    }
                }
                RouteStatus.STATUS_COMPLETED, RouteStatus.STATUS_CANCELLED -> {
                }
                RouteStatus.STATUS_SKIPPED -> {
                    updateRouteStatus(RouteStatus.STATUS_NOT_STARTED)
                }
            }
        }

        binding.btnCancel.setOnClickListener {
            when (viewModel.route.value?.Route?.status) {

                RouteStatus.STATUS_NOT_STARTED -> {
                    navigator.showDialog(
                        CancelRouteDialog::class,
                        bundleOf(Pair(DataTransferKeys.KEY_STATUS, RouteStatus.STATUS_SKIPPED))
                    )
                }
                RouteStatus.STATUS_SKIPPED -> {
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
                                    viewModel.route.value!!.EndAddress!!.latitude,
                                    viewModel.route.value!!.EndAddress!!.longitude
                                ) < 100
                            ) {
                                updateRouteStatus(RouteStatus.STATUS_COMPLETED)
                                val trackLatlngList = arrayListOf<LatLng>()

//                                userTrackLatLng?.forEach {
//                                    trackLatlngList.add(it.latLng)
//                                }
//                                if (!trackLatlngList.isNullOrEmpty()){
//                                    handleGetDirectionsResult(trackLatlngList)
//                                }
                            } else {
                                navigator.showDialog(
                                    CancelRouteDialog::class,
                                    bundleOf(
                                        Pair(
                                            DataTransferKeys.KEY_STATUS,
                                            RouteStatus.STATUS_CANCELLED
                                        )
                                    )
                                )
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
                }
            }
//            navigator.showDialog(CancelRouteDialog::class)
        }
    }

//    private fun setStartPolyline() {
////        if (startPolylineObject != null) {
////            focusOnStartRoute()
////        } else {
//            if (origin != null) {
//
//                val originMarker = LatLng(
//                    viewModel.route.value!!.StartAddress!!.latitude,
//                    viewModel.route.value!!.StartAddress!!.longitude
//                )
//
//                map?.addMarker(
//                    MarkerOptions().position(originMarker) // below line is use to add custom marker on our map.
//                        .icon(
//                            BitmapFromVector(
//                                requireContext(),
//                                R.drawable.ic_green
//                            )
//                        )
//                )
//
//                if (startPolyLineStringList.isNullOrEmpty()) {
//                    destination = LatLng(
//                        viewModel.route.value!!.StartAddress!!.latitude,
//                        viewModel.route.value!!.StartAddress!!.longitude
//                    )
//                    if (origin != null && destination != null) {
//                        viewModel.drawStartPolyLineApi(
//                            origin!!.latitude,
//                            origin!!.longitude,
//                            destination!!.latitude,
//                            destination!!.longitude,
//                            crmapp!!.gmapsid!!,
//                            ""
//                        )
//                    }
//                }else{
//                    showStartPolyline()
//                }
//            }
//    }
//
//
//    private fun setCustomerPolyline(customer: GetRouteAccountResponse) {
////        if (startPolylineObject != null) {
////            focusOnStartRoute()
////        } else {
////        val customerOrigin =
//            if (origin != null) {
//
//                val destinMarker = customer.DeliveryAddress?.let {
//                    LatLng(
//                        it.latitude,
//                        it.longitude
//                    )
//                }
//
//                map?.addMarker(
//                    MarkerOptions().position(destinMarker) // below line is use to add custom marker on our map.
//                        .icon(
//                            BitmapMarker(
//                                requireContext(),
//                                R.drawable.ic_flagblue,
//                                (customer.Account?.orderseq ?: 0).toString()
//                            )
//                        )
//                )
//
//
//                    if (origin != null && destinMarker != null) {
//                        viewModel.drawStartPolyLineApi(
//                            origin!!.latitude,
//                            origin!!.longitude,
//                            destinMarker.latitude,
//                            destinMarker.longitude,
//                            crmapp!!.gmapsid!!,
//                            ""
//                        )
//                    }
//            }
//    }

    private fun initialize() {
        setRoute()
        if (map == null) {
            lifecycleScope.launchWhenCreated {
                val mapFragment: SupportMapFragment? =
                    childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
                val googleMap: GoogleMap? = mapFragment?.awaitMap()
                map = googleMap
                map?.uiSettings?.isMapToolbarEnabled = false
                map?.setOnMarkerClickListener(this@TrackFragment)
                map?.setOnMapClickListener(this@TrackFragment)
                map?.setOnInfoWindowClickListener(this@TrackFragment)
                viewModel.currentRoute.value?.let {
                    currentPage = 1
                    addMarkers(it)
                }
            }
        } else {
            viewModel.currentRoute.value?.let {
                currentPage = 1
                addMarkers(it)
            }
        }
        binding.tvRouteName.text = viewModel.route.value?.Route?.title ?: ""
        binding.tvRouteNum.text = routeName
//        val routeId= viewModel.route.value?.Route?.id ?: -1
//        if (routeId>-1)
//            lifecycleScope.launchWhenCreated {
////                userTrackLatLng= userTrackDao.findById(routeId)?.trackLocations ?: arrayListOf()
//            }
    }

    private fun setRoute() {
        viewModel.route.value?.Route?.status?.let { setCurrentStatus(it) }
    }

    @SuppressLint("FragmentLiveDataObserve")
    private fun bindObservers() {
//        viewModel.updateRouteApiResponse.setObserver(viewLifecycleOwner, Observer {
//            it ?: return@Observer
//            when (it.status) {
//                Status.LOADING -> {
//                    progressDialog.setLoading(true)
//                }
//                Status.SUCCESS -> {
//                    progressDialog.setLoading(false)
//                }
//                Status.ERROR -> {
//                    progressDialog.setLoading(false)
//                    ApisRespHandler.handleError(
//                        it.error,
//                        requireActivity(),
//                        prefsManager = prefsManager
//                    )
//                }
//            }
//        })

        viewModel.getPolyLineTrackResponse.setObserver(viewLifecycleOwner, Observer {
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
                        if (viewModel.route.value?.Route?.status != RouteStatus.STATUS_NOT_STARTED) {
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
                                        viewModel.route.value!!.EndAddress!!.latitude,
                                        viewModel.route.value!!.EndAddress!!.longitude
                                    )
                                    origin = latLngList[(currentPage * 25)]
                                    val ways = "optimize:false"
                                    for (i in ((currentPage * 25) + 1) until latLngList.size) {
                                        wayString += "|${latLngList[i].latitude},${latLngList[i].longitude}"
                                    }
                                    if (wayString.isNotBlank()) {
                                        wayString = ways + wayString
                                    }
                                }


                                Log.e("wayPoints: ", wayString)
                                Log.e("origin: ", origin.toString())
                                Log.e("destination: ", destination.toString())

                                viewModel.drawTrackPolyLineApi(
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
//        viewModel.getStartPolyLineResponse.setObserver(viewLifecycleOwner, Observer {
//            it ?: return@Observer
//            when (it.status) {
//                Status.LOADING -> {
////                    progressDialog.setLoading(true)
//                }
//                Status.SUCCESS -> {
////                    progressDialog.setLoading(false)
//                    if (it.data.isNullOrEmpty()) {
//
//                    } else {
//                        val responsePolyline = it.data
//                        val jsonRootObject = JSONObject(responsePolyline)
//                        startPolylineObject = jsonRootObject
//                        drawStartPolyLine(jsonRootObject)
//                    }
//                }
//                Status.ERROR -> {
////                    progressDialog.setLoading(false)
//                    ApisRespHandler.handleError(
//                        it.error,
//                        requireActivity(),
//                        prefsManager = prefsManager
//                    )
//                }
//            }
//        })

        viewModel.getRouteAccountResponse.setObserver(viewLifecycleOwner, Observer {
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
                                viewModel.currentRoute.value?.indexOfFirst { it.Account?.id == account.Account?.id }
                            if (index != null) {
                                if (index > -1) {
                                    viewModel.currentRoute.value!![index] = account
                                } else {
                                    viewModel.currentRoute.value!!.add(account)
                                }
                            }
                        }

                        if (routePageCount < (it.data.pagination?.pages ?: 1)) {
//                            isSyncing = true
                            viewModel.getRouteAccountApi(
                                requireContext(),
                                routeId,
                                routePageCount
                            )
                        } else {
                            progressDialog.setLoading(false)

//                            binding.llProgress.gone()
                            currentPage = 1

                            lifecycleScope.launchWhenCreated {
                                routeActivityDao.deleteRoute(routeId)
                            }
                            viewModel.currentRoute.value!!.forEach { account ->
                                account.routeId = routeId
                                lifecycleScope.launchWhenCreated {
                                    account.id = routeActivityDao.insert(account)
                                }
                            }

//                            if (route?.Route?.status == RouteStatus.STATUS_NOT_STARTED) {
//
//                            var todayRoutes =
//                                viewModel.todayAllRoutes.value ?: arrayListOf()
//                            todayRoutes.add(currentRoute!!)
//                            viewModel.todayAllRoutes.value = todayRoutes
//                            viewModel.currentRoute.value = currentRoute
//
//                            route?.Accounts = currentRoute
//                            polyLineStringList = null
//                            route?.Accounts?.let { it1 -> addMarkers(it1) }


                            isStartLineVisible = false
                            map?.uiSettings?.isMapToolbarEnabled = false
                            map?.setOnMarkerClickListener(this)
                            map?.setOnMapClickListener(this)
                            map?.setOnInfoWindowClickListener(this@TrackFragment)
                            viewModel.currentRoute.value?.let {
                                currentPage = 1
                                addMarkers(it)
                            }
//                            binding.ivNav.setImageResource(R.drawable.ic_track)
                            updateRouteStatus(RouteStatus.STATUS_IN_PROGRESS)
//                                updateRouteStatus(RouteStatus.STATUS_IN_PROGRESS)


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
//                    enableClicks()
                    ApisRespHandler.handleError(
                        it.error,
                        requireActivity(),
                        prefsManager = prefsManager
                    )
                }
            }
        })

    }

    private fun addMarkers(routes: ArrayList<GetRouteAccountResponse>) {
        if (map != null && !routes.isNullOrEmpty()) {

            latLngList.clear()
            var wayString = ""
//            origin = LatLng(22.13540338023894, -100.82486311634126)

            wayString = "optimize:false"
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

            pageCount = getPageCount()

            val origin = LatLng(
                viewModel.route.value!!.StartAddress!!.latitude,
                viewModel.route.value!!.StartAddress!!.longitude
            )
            val dest = LatLng(
                viewModel.route.value!!.EndAddress!!.latitude,
                viewModel.route.value!!.EndAddress!!.longitude
            )
            if (pageCount <= 1) {
                destination = LatLng(
                    viewModel.route.value!!.EndAddress!!.latitude,
                    viewModel.route.value!!.EndAddress!!.longitude
                )
            } else {
                destination = latLngList[25]
            }

            if (origin == dest && (viewModel.route.value!!.Route!!.status == RouteStatus.STATUS_NOT_STARTED || viewModel.route.value!!.Route!!.status == RouteStatus.STATUS_PENDING)) {
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

            if (origin != null && destination != null && polyLineList.isNullOrEmpty()) {
                viewModel.drawTrackPolyLineApi(
                    origin.latitude,
                    origin.longitude,
                    destination!!.latitude,
                    destination!!.longitude,
                    crmapp!!.gmapsid!!,
                    wayString
                )
            } else if (!polyLineList.isNullOrEmpty()) {
                showRoutePolyline()
            }
        }
    }

    private fun showRoutePolyline() {
        polyLineList?.clear()
        polyLineStringList?.forEach { encodedString ->
            val line = map?.addPolyline(
                PolylineOptions().addAll(decodePoly(encodedString)).width(15f)
                    .color(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                    .geodesic(true)
            )
            line?.let { polyLineList?.add(it) }
        }
    }

    private fun showStartPolyline() {
        startPolyLineStringList?.forEach { encodedString ->
            map?.addPolyline(
                PolylineOptions().addAll(decodePoly(encodedString)).width(15f)
                    .color(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                    .geodesic(true)
            )
        }
    }

    private fun getPageCount(): Int {
        var pages = 1
        if ((latLngList.size % 25) == 0)
            pages = latLngList.size / 25
        else
            pages = latLngList.size / 25 + 1
        return pages
    }

    private fun updateRouteStatus(status: String, reason: String? = null) {
        var startDate = ""
        var endDate = ""
        if (!viewModel.route.value?.Route?.actual_startdate.isNullOrEmpty()) {
            startDate = viewModel.route.value!!.Route!!.actual_startdate!!
        } else if (status == RouteStatus.STATUS_IN_PROGRESS) {
            startDate = DateUtils.getCurrentDateWithFormat(DateFormat.DATE_FORMAT_RENEW)
        }
        if (!viewModel.route.value?.Route?.actual_enddate.isNullOrEmpty()) {
            endDate = viewModel.route.value!!.Route!!.actual_enddate!!
        } else if (status == RouteStatus.STATUS_COMPLETED || status == RouteStatus.STATUS_CANCELLED || status == RouteStatus.STATUS_SKIPPED) {
            endDate = DateUtils.getCurrentDateWithFormat(DateFormat.DATE_FORMAT_RENEW)
        }


        val routeData = RouteData(
            id = viewModel.route.value?.Route?.id ?: 0,
            lov_route_exec_status = status,
            startdate = startDate,
            enddate = endDate,
            lov_route_exec_status_reason = reason
        )

        val routeList = arrayListOf<UpdateRoute>()
        routeList.add(UpdateRoute(routeData))
        if (status == RouteStatus.STATUS_IN_PROGRESS) {
            sendCommandToService(TrackConstants.ACTION_START_OR_RESUME_SERVICE)
        } else if (status == RouteStatus.STATUS_CANCELLED || status == RouteStatus.STATUS_COMPLETED || status == RouteStatus.STATUS_SKIPPED) {
            sendCommandToService(TrackConstants.ACTION_STOP_SERVICE)
        }
        if (isConnectedToInternet(requireContext(), false)) {
            viewModel.updateRouteApi(requireContext(), routeList)
        } else {
            lifecycleScope.launchWhenCreated {
                val result = updateRouteDao.insert(routeList)
                Timber.e(result.toString())
            }
        }
        val route = viewModel.route.value
        route?.Route?.status = status
        viewModel.setSelectedRoute(route!!)

        setCurrentStatus(status)
    }

    private fun sendCommandToService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            it.putExtra(DataTransferKeys.KEY_ROUTE_ID, routeId)
            requireContext().startService(it)
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

//        val legsJsonObject = (((routes.get("legs") as JSONArray).get(0) as JSONObject))
        val line = map?.addPolyline(
            PolylineOptions().addAll(decodePoly(encodedString)).width(15f)
                .color(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                .geodesic(true)
        )

        line?.let { polyLineList?.add(it) }

        if (!latLngList.isNullOrEmpty())
            focusOnRoute()
    }

    fun drawStartPolyLine(startPolylineObject: JSONObject) {
        val routeArray = startPolylineObject.getJSONArray("routes")
        if (routeArray.length() == 0) {
            return
        }
        val routes = routeArray.getJSONObject(0)
        val overviewPolyLines = routes.getJSONObject("overview_polyline")
        val encodedString = overviewPolyLines.getString("points")
        startPolyLineStringList?.add(encodedString)
//        val legsJsonObject = (((routes.get("legs") as JSONArray).get(0) as JSONObject))
        val line = map?.addPolyline(
            PolylineOptions().addAll(decodePoly(encodedString)).width(15f)
                .color(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
                .geodesic(true)
        )
        if (isCustomerLineVisible) {
            customerPolyLine = line
        }
        if (line != null && origin != null)
            focusOnStartRoute(viewModel.route.value!!.StartAddress!!)
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

    private fun focusOnStartRoute(addressTemplate: AddressTemplate) {
        val builder = LatLngBounds.Builder()

        builder.include(origin!!)
        builder.include(
            LatLng(
                addressTemplate.latitude,
                addressTemplate.longitude
            )
        )

        val bounds = builder.build()
        val padding = 100
        /**create the camera with bounds and padding to set into map*/
        /**create the camera with bounds and padding to set into map */
        val cu = CameraUpdateFactory.newLatLngBounds(bounds, padding)
        map?.moveCamera(cu)
    }

    override fun onResume() {
        super.onResume()
//        route= (requireActivity() as MainActivity).route
//        currentRoute=(requireActivity() as MainActivity).currentRoute
        binding.ivNavMarker.gone()
        if (isGooglePlayServicesAvailable()) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
            checkPermissions()
        }
    }

    private fun isGooglePlayServicesAvailable(): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val status = googleApiAvailability.isGooglePlayServicesAvailable(requireContext())
        if (ConnectionResult.SUCCESS == status) return true else {
            if (googleApiAvailability.isUserResolvableError(status))
                Toast.makeText(
                    requireContext(),
                    "Please Install google play services to use this application",
                    Toast.LENGTH_LONG
                ).show()
        }
        return false
    }

    @SuppressLint("MissingPermission")
    private fun checkPermissions() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            if (isLocationEnabled()) {
                //                mFusedLocationClient.lastLocation.addOnCompleteListener(requireActivity()) { task ->
//                    val mLastLocation: Location? = task.result
//                    if (mLastLocation != null) {
//                        val latLng = LatLng(mLastLocation.latitude, mLastLocation.longitude)
//                        origin = latLng
//                    }
//                }
                startCurrentLocationUpdates()
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

    @SuppressLint("MissingPermission")
    private fun startCurrentLocationUpdates() {
        val locationRequest: LocationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 1000
        locationRequest.fastestInterval = 500
        locationRequest.smallestDisplacement = 3f
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }
        mFusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult)
            if (locationResult?.lastLocation == null)
                return

            currentLocation = locationResult.lastLocation
            val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
            origin = latLng
            if (firstTimeFlag && map != null) {
                animateCamera(currentLocation);
                firstTimeFlag = false;
            } else if (map != null && map!!.cameraPosition.zoom == 20f) {
                animateCamera(currentLocation)
            }
            showMarker(currentLocation)
        }
    }

    private fun animateCamera(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        map?.animateCamera(
            CameraUpdateFactory.newCameraPosition(
                getCameraPositionWithBearing(latLng)
            )
        )
    }

    private fun getCameraPositionWithBearing(latLng: LatLng): CameraPosition {
        val bearing = currentLocationMarker?.position?.let { calculateBearing(it, latLng) }
        return if (bearing == null)
            CameraPosition.Builder().target(latLng).zoom(20f).build()
        else
            CameraPosition.Builder().target(latLng).bearing(bearing).zoom(20f).build()
    }

    private fun showMarker(currentLocation: Location) {
        val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)

        if (currentLocationMarker == null)
            currentLocationMarker = map?.addMarker(
                MarkerOptions().icon(
                    BitmapFromVector(
                        requireContext(),
                        R.drawable.ic_car
                    )
                ).position(latLng).flat(true)
            )
        else {
            val isMoved = (calculateDistance(
                currentLocationMarker!!.position.latitude,
                currentLocationMarker!!.position.longitude,
                latLng.latitude,
                latLng.longitude,
            ) > 3)

            if (isMoved) {
                MarkerAnimation.animateMarkerToGB(
                    currentLocationMarker!!,
                    latLng,
                    LatLngInterpolator.Spherical()
                )
                val bearing = calculateBearing(currentLocationMarker!!.position, latLng)
                currentLocationMarker!!.rotation = bearing
//                handleGetDirectionsResult(arrayListOf(currentLocationMarker!!.position,latLng))
                if (viewModel.route.value?.Route?.status == RouteStatus.STATUS_IN_PROGRESS
                    || viewModel.route.value?.Route?.status == RouteStatus.STATUS_PAUSED
                ) {
//                    userTrackLatLng.add(
//                        TrackTemplate(
//                            time= DateUtils.getCurrentDateWithFormat(DateFormat.DATE_FORMAT_RENEW),
//                            latLng = latLng
//                        )
//                    )
                }
            }
        }

//        if ((viewModel.route.value?.Route?.status == RouteStatus.STATUS_IN_PROGRESS)
//            && !checkOutRoute()
//            && isOffRouteSubmitted
//        ) {
//            if (!offRouteDialogShown && isVisible) {
//                createOffRouteDialog()
//                offRouteDialogShown = true
//            }
//        }

    }

    //
    fun handleGetDirectionsResult(directionPoints: ArrayList<LatLng>) {
        val rectLine = PolylineOptions().width(10f).color(Color.GREEN)
        var routePolyline: Polyline? = null
        for (i in 0 until directionPoints.size) {
            rectLine.add(directionPoints[i])
        }
        //clear the old line
        routePolyline?.remove()
        // mMap is the Map Object
        routePolyline = map?.addPolyline(rectLine)
    }

    private fun createOffRouteDialog() {
        AlertDialogUtil.instance.createOkCancelDialog(requireContext(),
            R.string.off_route_alert,
            R.string.off_route_msg,
            R.string.ok,
            0,
            false,
            object : AlertDialogUtil.OnOkCancelDialogListener {
                override fun onOkButtonClicked() {
                    navigator.showDialog(
                        CancelRouteDialog::class,
                        bundleOf(Pair(DataTransferKeys.KEY_STATUS, RouteStatus.STATUS_OFFROUTE))
                    )
                }

                override fun onCancelButtonClicked() {
                }
            }
        ).show()
    }

    private fun checkOutRoute(): Boolean {
        var isLocationOnPloyline = true
        polyLineList?.forEach { polyLn ->

            isLocationOnPloyline = PolyUtil.isLocationOnPath(
                origin,
                polyLn.points,
                polyLn.isGeodesic,
                100.0
            )
            if (!isLocationOnPloyline)
                return false
        }
        if (isCustomerLineVisible && customerPolyLine != null) {
            isLocationOnPloyline = PolyUtil.isLocationOnPath(
                origin,
                customerPolyLine!!.points,
                customerPolyLine!!.isGeodesic,
                100.0
            )
            if (!isLocationOnPloyline)
                return false
        }
        return isLocationOnPloyline
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun setCurrentStatus(status: String) {
        Log.e("CurrentStatus: ", status)
        when (status) {
            RouteStatus.STATUS_IN_PROGRESS -> {
                binding.btnStart.text = getString(R.string.pause)
                binding.btnCancel.text = getString(R.string.end)
//                binding.ivNav.gone()
                viewModel.isActiveRoute(true)
            }
            RouteStatus.STATUS_NOT_STARTED -> {
                binding.btnStart.text = getString(R.string.start)
                binding.btnCancel.text = getString(R.string.cancel)
//                binding.ivNav.visible()
            }
            RouteStatus.STATUS_PAUSED -> {
                binding.btnStart.text = getString(R.string.resume)
                binding.btnCancel.text = getString(R.string.end)
//                binding.ivNav.gone()
            }
            RouteStatus.STATUS_COMPLETED, RouteStatus.STATUS_CANCELLED -> {
//                binding.btnStart.invisible()
//                binding.btnCancel.invisible()
                binding.btnStart.text = getString(R.string.start)
                binding.btnStart.alpha = 0.5f
                binding.btnCancel.text = getString(R.string.route_ended)
//                binding.ivNav.gone()
                viewModel.isActiveRoute(false)
            }
            RouteStatus.STATUS_SKIPPED -> {
                binding.btnStart.text = getString(R.string.start)
                binding.btnStart.alpha = 0.5f
                binding.btnCancel.text = getString(R.string.route_skipped)
//                binding.ivNav.gone()
                viewModel.isActiveRoute(false)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    @NeedsPermission(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    fun getLocation() {
        checkPermissions()
    }

    @OnShowRationale(
        Manifest.permission.ACCESS_COARSE_LOCATION,
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
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    fun onNeverAskAgainRationale() {
        PermissionUtils.showAppSettingsDialog(
            requireContext(), R.string.we_will_need_your_location
        )
    }

    @OnPermissionDenied(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
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
            val bundle = Bundle()
            bundle.putParcelable(DataTransferKeys.KEY_TASKS, visits)
            bundle.putString(DataTransferKeys.KEY_FROM, "track")
            bundle.putString(
                DataTransferKeys.KEY_STATUS,
                visits?.Visit?.Activity?.lov_activity_status
            )
            bundle.putString(
                DataTransferKeys.KEY_ROUTE_STATUS,
                viewModel.route.value?.Route?.status
            )
            navigator.push(TaskFragment::class) {
                this.arguments = bundle
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        map?.setOnCameraMoveStartedListener(null)
        map?.setOnCameraIdleListener(null)
    }

    override fun onDestroy() {
        super.onDestroy()
//        lifecycleScope.launch {
//            userTrackDao.insert(UserTrackTemplate(
//                id= viewModel.route.value?.Route?.id ?: 0,
//                trackLocations = userTrackLatLng
//            ))
//        }
        if (mFusedLocationClient != null)
            mFusedLocationClient.removeLocationUpdates(locationCallback)
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
}
