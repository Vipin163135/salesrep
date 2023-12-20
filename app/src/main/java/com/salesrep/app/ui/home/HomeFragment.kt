package com.salesrep.app.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.salesrep.app.R
import com.salesrep.app.base.BaseFragment
import com.salesrep.app.dao.*
import com.salesrep.app.data.appConfig.Crmapp
import com.salesrep.app.data.models.TrackTemplate
import com.salesrep.app.data.models.requests.CreateOrderTemplate
import com.salesrep.app.data.models.requests.UpdateRoute
import com.salesrep.app.data.models.requests.UpdateRouteActivity
import com.salesrep.app.data.models.requests.UpdateSurvey
import com.salesrep.app.data.models.response.GetHomeRoutesResponse
import com.salesrep.app.data.models.response.GetRouteAccountResponse
import com.salesrep.app.data.models.response.Route
import com.salesrep.app.data.network.responseUtil.ApisRespHandler
import com.salesrep.app.data.network.responseUtil.Status
import com.salesrep.app.data.repos.UserRepository
import com.salesrep.app.databinding.FragmentHomeBinding
import com.salesrep.app.ui.dialogs.ProgressDialog
import com.salesrep.app.ui.home.adapter.RoutePagerAdapter
import com.salesrep.app.ui.home.adapter.ViewPagerAdapter
import com.salesrep.app.util.*
import dagger.hilt.android.AndroidEntryPoint
import permissions.dispatcher.RuntimePermissions
import javax.inject.Inject


@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding>(), ViewPager.OnPageChangeListener {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var updateRouteDao: UpdateRouteDao

    @Inject
    lateinit var updateActivityDao: UpdateActivityDao

    @Inject
    lateinit var updateSurveyDao: UpdateSurveyDao

    @Inject
    lateinit var createOrderDao: CreateOrderDao

    @Inject
    lateinit var routesDao: RoutesDao

//    @Inject
//    lateinit var routeActivityDao: RouteActivityDao

    @Inject
    lateinit var savedOrderDao: SaveOrderDao


    private lateinit var binding: FragmentHomeBinding
    override val viewModel by activityViewModels<HomeViewModel>()
    val homeViewModel by activityViewModels<HomeViewModel>()
    private lateinit var progressDialog: ProgressDialog

    private var fragmentList = ArrayList<HomeRouteFragment>()
    private var tabList = ArrayList<String>()
    private var routeName: String? = ""
    private lateinit var progressAdapter: RoutePagerAdapter
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private var selectedPos: Int = -1
    private var isFirstTime = true
    var activityList = listOf<UpdateRouteActivity>()
    var routeUpdateList = listOf<UpdateRoute>()
    var surveyUpdateList = listOf<UpdateSurvey>()
    var createOrderList = arrayListOf<CreateOrderTemplate>()

    @Inject
    lateinit var userTrackDao: UserTrackDao
    private var userTrackLatLng = listOf<TrackTemplate>()
    var teamRoutes : GetHomeRoutesResponse? = null

    private val crmapp by lazy {
        prefsManager.getObject(
            PrefsManager.APP_CONFIG,
            Crmapp::class.java
        )
    }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_home
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(AppRequestCode.CURRENT_ROUTE_STATUS_CHANGED) { key, bundle ->
//            initialize()
            viewModel.currentRoute.value?.let { setRouteProgress(it) }
            if (binding.viewPager.currentItem >= 0) {
                fragmentList[binding.viewPager.currentItem].updateMarkers()
//                val fragment: Fragment? = viewPagerAdapter.getFragment(selectedPos)
//                fragment?.onResume()
            }
        }
        setFragmentResultListener(AppRequestCode.REFRESH_HOME_REQUEST) { key, bundle ->
//            initialize()
            initialize()
        }
        isFirstTime = true
    }

    override fun onCreateView(
        instance: Bundle?,
        binding: FragmentHomeBinding
    ) {
        this.binding = binding
        progressDialog = ProgressDialog(requireActivity())
        isFirstTime = true
        activityList = listOf()
        routeUpdateList = listOf()
        surveyUpdateList = listOf()
        initialize()
        bindObservers()
        listeners()
    }

    private fun listeners() {
        binding.btnNav.setOnClickListener {
            (requireActivity() as MainActivity).showMenu()
        }

        binding.tvDate.setOnClickListener {
        }

//        binding.ivCamera.setOnClickListener {
//            getStorageWithPermissionCheck()
//        }
    }

    private fun initialize() {
        binding.tvName.text =
            "${userRepository.getUser()?.calc_fullname}"
        binding.tvDate.text = DateUtils.getCurrentDateWithFormat(DateFormat.DATE_MON_YEAR_FORMAT)
        activityList = arrayListOf()
        routeUpdateList = arrayListOf()
        surveyUpdateList = arrayListOf()

        lifecycleScope.launchWhenCreated {
            teamRoutes = routesDao.getAllRoutes().lastOrNull()

            if (teamRoutes!= null ) {
//                viewModel.getTeamRoutesResponse.value= Resource.success(teamRoutes.lastOrNull())
                prefsManager.save(PrefsManager.TEAM_ROUTES, teamRoutes)
                teamRoutes?.TodayRoutes?.forEach {
                    if ((it.Route?.status==RouteStatus.STATUS_IN_PROGRESS)
                        ||(it.Route?.status==RouteStatus.STATUS_PAUSED)
                        ||(it.Route?.status==RouteStatus.STATUS_OFFROUTE)){
                        viewModel.isActiveRoute(true)
                    }
                }
//                teamRoutes?.AllRoutes?.forEach {
//                    if ((it.Route?.status==RouteStatus.STATUS_IN_PROGRESS)
//                        ||(it.Route?.status==RouteStatus.STATUS_PAUSED)
//                        ||(it.Route?.status==RouteStatus.STATUS_OFFROUTE)){
//                        viewModel.isActiveRoute(true)
//                    }
//                }
                val todayRoutes = teamRoutes?.TodayRoutes
                if (!todayRoutes.isNullOrEmpty()) {
                    val plannedDate= todayRoutes[0].Route?.planned_startdate ?: "2000-01-01 00:00:00"

                    if (DateUtils.compareDateWithTodayFormat(DateFormat.YEAR_MON_DATE_FORMAT,plannedDate)) {
                        viewModel.route.value = teamRoutes!!.TodayRoutes[0]
                        setRoutePager(teamRoutes!!.TodayRoutes)
                    }else{
                        getHomeData()
                    }
                }else{
                    getHomeData()
                }
            }else{
                getHomeData()
            }
        }

        viewModel.isActiveRoute(false)
        viewModel.getTeamProductsApi(requireContext())
        viewModel.getTeamPricelistApi(requireContext())

        setRouteProgress(arrayListOf())
    }

    private fun loadFromServer(){
        if (isConnectedToInternet(requireContext(), false)) {
            lifecycleScope.launchWhenCreated {
                activityList = updateActivityDao.getAllActivity()
                routeUpdateList = updateRouteDao.getAllActivity()
                surveyUpdateList= updateSurveyDao.getAllSurveys()
                createOrderList= createOrderDao.getAllOrders() as ArrayList<CreateOrderTemplate>

                if (!createOrderList.isNullOrEmpty()){
                    homeViewModel.createOrderApi(requireContext(),createOrderList)
                }

//                if (surveyUpdateList.isNullOrEmpty() && routeUpdateList.isNullOrEmpty() && activityList.isNullOrEmpty()) {
//                    } else
                if (!surveyUpdateList.isNullOrEmpty()) {
                    homeViewModel.updateSurveyApi(
                        requireContext(),
                        surveyUpdateList as ArrayList<UpdateSurvey>
                    )

                } else if (!routeUpdateList.isNullOrEmpty()) {
                    homeViewModel.updateRouteApi(
                        requireContext(),
                        routeUpdateList as ArrayList<UpdateRoute>
                    )
                } else if (!activityList.isNullOrEmpty()) {
                    homeViewModel.updateHomeRouteActivityApi(
                        requireContext(),
                        activityList as ArrayList<UpdateRouteActivity>
                    )
                }
            }
//            if ( prefsManager.getString(PrefsManager.TEAM_PRODUCTS,"").isNullOrEmpty())
             } else {

        }
    }

    private fun getHomeData(){
        viewModel.getTeamRoutesApi(requireContext())
        viewModel.getFormCatalogApi(requireContext())
    }

    @SuppressLint("FragmentLiveDataObserve")
    private fun bindObservers() {
        viewModel.getTeamRoutesResponse.setObserver(viewLifecycleOwner, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.LOADING -> progressDialog.setLoading(true)
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    if (it.data != null) {
                        lifecycleScope.launchWhenCreated {
                            routesDao.clearAll()
                            it.data.id= routesDao.insert(it.data)
                            teamRoutes= it.data
                            prefsManager.save(PrefsManager.TEAM_ROUTES, it.data)
                            if (!it.data.TodayRoutes.isNullOrEmpty()) {
                                viewModel.route.value = it.data.TodayRoutes[0]
                                setRoutePager(it.data.TodayRoutes)

                            }

                            teamRoutes?.TodayRoutes?.forEach {apiRoute->
                                if ((apiRoute.Route?.status==RouteStatus.STATUS_IN_PROGRESS)
                                    ||(apiRoute.Route?.status==RouteStatus.STATUS_PAUSED)
                                    ||(apiRoute.Route?.status==RouteStatus.STATUS_OFFROUTE)){
                                    viewModel.isActiveRoute(true)
                                }
                            }
//                            teamRoutes?.AllRoutes?.forEach {apiRoute->
//                                if ((apiRoute.Route?.status==RouteStatus.STATUS_IN_PROGRESS)
//                                    ||(apiRoute.Route?.status==RouteStatus.STATUS_PAUSED)
//                                    ||(apiRoute.Route?.status==RouteStatus.STATUS_OFFROUTE)){
//                                    viewModel.isActiveRoute(true)
//                                }
//                            }
                        }
                    }
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

        viewModel.getTeamProductsResponse.setObserver(viewLifecycleOwner, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.LOADING -> {}
                Status.SUCCESS -> {
                    val json= Gson().toJson(it.data)
                    prefsManager.save(PrefsManager.TEAM_PRODUCTS, json)
                }
                Status.ERROR -> {
                    ApisRespHandler.handleError(
                        it.error,
                        requireActivity(),
                        prefsManager = prefsManager
                    )
                }
            }
        })

        viewModel.getTeamPricelistResponse.setObserver(viewLifecycleOwner, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.LOADING -> {}
                Status.SUCCESS -> {
                    val json= Gson().toJson(it.data)
                    prefsManager.save(PrefsManager.TEAM_PRICELIST, json)
                }
                Status.ERROR -> {

                    ApisRespHandler.handleError(
                        it.error,
                        requireActivity(),
                        prefsManager = prefsManager
                    )

                }
            }
        })

        viewModel.currentRoute.observe(viewLifecycleOwner, Observer {
            setRouteProgress(it)
        })

        homeViewModel.updateRouteApiResponse.setObserver(viewLifecycleOwner, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.LOADING -> {
                    progressDialog.setLoading(true)
                }
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)
                    lifecycleScope.launchWhenCreated {
                        updateRouteDao.clearAll()
                    }
                    if (activityList.isNullOrEmpty()) {
//                        viewModel.getTeamRoutesApi(requireContext())
                    } else {
                        viewModel.updateHomeRouteActivityApi(
                            requireContext(),
                            activityList as ArrayList<UpdateRouteActivity>
                        )
                    }
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
        homeViewModel.updateRouteActivityApiResponse.setObserver(viewLifecycleOwner, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.LOADING -> {
                    progressDialog.setLoading(true)
                }
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    lifecycleScope.launchWhenCreated {
                        updateActivityDao.clearAll()
                    }
//                    viewModel.getTeamRoutesApi(requireContext())
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

        homeViewModel.updateSurveyApiResponse.setObserver(viewLifecycleOwner, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.LOADING -> {
                }
                Status.SUCCESS -> {
                    lifecycleScope.launchWhenCreated {
                        updateSurveyDao.clearAll()
                    }
                    if (routeUpdateList.isNullOrEmpty() && activityList.isNullOrEmpty()) {
//                        viewModel.getTeamRoutesApi(requireContext())
                    } else if (!routeUpdateList.isNullOrEmpty()) {
                        homeViewModel.updateRouteApi(
                            requireContext(),
                            routeUpdateList as ArrayList<UpdateRoute>
                        )
                    } else {
                        viewModel.updateHomeRouteActivityApi(
                            requireContext(),
                            activityList as ArrayList<UpdateRouteActivity>
                        )
                    }
                }
                Status.ERROR -> {
                    ApisRespHandler.handleError(
                        it.error,
                        requireActivity(),
                        prefsManager = prefsManager
                    )
                }
            }
        })

        viewModel.todayAllRoutes.observe(viewLifecycleOwner, Observer {
            if (isFirstTime) {
                viewModel.currentRoute.value = it[0]
                isFirstTime = false

//                selectedPos=0
            }
//            else{
//                viewModel.currentRoute.value= it[selectedPos]
//            }
        })

        viewModel.currentRouteDistance.observe(viewLifecycleOwner, Observer {
            progressAdapter.notifyGraph(it)
        })

        viewModel.route.observe(viewLifecycleOwner, Observer { route ->
            lifecycleScope.launchWhenCreated {
                val routeApiResp=routesDao.getAllRoutes().lastOrNull()
                if (routeApiResp!=null ){
                    var selectedRoute= routeApiResp.TodayRoutes.indexOfFirst{ it.Route?.id == route.Route?.id}

                    if (selectedRoute>=0 && (routeApiResp.TodayRoutes[selectedRoute].Route?.status != route.Route?.status)){
                        routeApiResp.TodayRoutes[selectedRoute]= route
                    }
//                    else{
                        selectedRoute= routeApiResp.AllRoutes.indexOfFirst { it.Route?.id == route.Route?.id}
                        if (selectedRoute>=0 && (routeApiResp.AllRoutes[selectedRoute].Route?.status != route.Route?.status)){
                            routeApiResp.AllRoutes[selectedRoute]= route
//                            routesDao.insert(routeApiResp)
                        }
//                    }
                    routesDao.insert(routeApiResp)

                }
            }
        })

    }

    private fun setRoutePager(todayRoutes: List<Route>) {

        tabList.clear()
        fragmentList.clear()
        //add tabs

        //add fragments
        todayRoutes.forEachIndexed { index, route ->
            fragmentList.add(HomeRouteFragment.newInstance(route = route, "Route ${index + 1}"))
            tabList.add("Route ${index + 1}")
            if (index == 0)
                routeName = route.Route?.title
        }

        viewPagerAdapter = ViewPagerAdapter(childFragmentManager, fragmentList, tabList)
        binding.viewPagerRoute.adapter = viewPagerAdapter
        binding.viewPagerRoute.offscreenPageLimit = 1
        binding.viewPagerRoute.addOnPageChangeListener(this);
        binding.tlRoutes.setupWithViewPager(binding.viewPagerRoute)

//        val tabLayoutMediator = TabLayoutMediator(
//            binding.tlImage, binding.viewPagerRoute, true
//        ) { tab, position -> }
//        tabLayoutMediator.attach()

    }

    fun setRouteProgress(arrayList: ArrayList<GetRouteAccountResponse>) {
        progressAdapter = RoutePagerAdapter(requireContext(), arrayList, routeName,this,savedOrderDao)
        binding.viewPager.adapter = progressAdapter
        val tabLayoutMediator = TabLayoutMediator(
            binding.tlImage, binding.viewPager, true
        ) { tab, position -> }
        tabLayoutMediator.attach()
        userTrackLatLng = listOf()

        if (viewModel.route.value?.Route?.status == RouteStatus.STATUS_COMPLETED
            || viewModel.route.value?.Route?.status == RouteStatus.STATUS_IN_PROGRESS
        ) {
            val routeId = viewModel.route.value?.Route?.id ?: -1
            if (routeId > -1)
                lifecycleScope.launchWhenCreated {
                    userTrackLatLng =
                        userTrackDao.findById(routeId)?.trackLocations ?: arrayListOf()
                }

            val distance = calculateTotalDistance(userTrackLatLng).toDouble()
            var endtime = viewModel.route.value?.Route?.actual_enddate
            var startTime = viewModel.route.value?.Route?.actual_startdate
            var duration = 0.0
            if (endtime == null || endtime.isEmpty()) {
                endtime = DateUtils.getCurrentDateWithFormat(DateFormat.DATE_FORMAT_RENEW)
            }
            duration =
                DateUtils.getTimeDifferenceInMins(DateFormat.DATE_FORMAT_RENEW, startTime, endtime)
                    .toDouble()

            Log.e("duration: ", duration.toString())

//        val duration=

            viewModel.currentRouteDistance.value = Pair(distance, duration)
        }
//        viewModel.currentRouteDistance.value?.let {
//            progressAdapter.notifyGraph(it)
//        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        selectedPos = position
        viewModel.currentRoute.value = (viewModel.todayAllRoutes.value?.get(position))
    }

    override fun onPageScrollStateChanged(state: Int) {
    }


    private val networkRequest = getNetworkRequest()

    private fun getNetworkRequest(): NetworkRequest {
        return NetworkRequest.Builder()
            .build()
    }

    val networkCallback = getNetworkCallBack()

    private fun getNetworkCallBack(): ConnectivityManager.NetworkCallback {
        return object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {    //when Wifi is on
                super.onAvailable(network)
                if (isConnectedToInternet(requireContext(),false)){
                    loadFromServer()
                }
//                Toast.makeText(requireContext(), "Wifi is on!", Toast.LENGTH_SHORT).show()
            }

            override fun onLost(network: Network) {    //when Wifi 【turns off】
                super.onLost(network)

//                Toast.makeText(requireContext(), "Wifi turns off!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun getConnectivityManager() = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager


    override fun onResume() {
        super.onResume()
        Log.e("Main Fragmemnt", "onResume: called")

        viewModel.currentRoute.value?.let {
            setRouteProgress(it)
        }

        getConnectivityManager().registerNetworkCallback(networkRequest, networkCallback)
    }

    override fun onPause() {    //stop monitoring when not fully visible
        super.onPause()

        getConnectivityManager().unregisterNetworkCallback(networkCallback)
    }

}
