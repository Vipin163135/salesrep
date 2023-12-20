package com.salesrep.app.ui.route

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.TextView
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
import com.google.android.material.tabs.TabLayoutMediator
import com.salesrep.app.R
import com.salesrep.app.base.BaseFragment
import com.salesrep.app.dao.RouteActivityDao
import com.salesrep.app.dao.SaveOrderDao
import com.salesrep.app.dao.UpdateActivityDao
import com.salesrep.app.data.models.ActivityTemplate
import com.salesrep.app.data.models.requests.RouteActivityData
import com.salesrep.app.data.models.requests.UpdateRouteActivity
import com.salesrep.app.data.models.response.GetRouteAccountResponse
import com.salesrep.app.data.network.responseUtil.ApisRespHandler
import com.salesrep.app.data.network.responseUtil.Status
import com.salesrep.app.data.repos.UserRepository
import com.salesrep.app.databinding.FragmentRoutesBinding
import com.salesrep.app.ui.dialogs.ProgressDialog
import com.salesrep.app.ui.home.HomeViewModel
import com.salesrep.app.ui.home.adapter.RoutePagerAdapter
import com.salesrep.app.ui.route.adapters.VisitsAdapter
import com.salesrep.app.util.*
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class RouteVisitFragment : BaseFragment< FragmentRoutesBinding>() {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var updateActivityDao: UpdateActivityDao

    @Inject
    lateinit var routeActivityDao: RouteActivityDao

    private lateinit var binding: FragmentRoutesBinding
    override val viewModel by activityViewModels<HomeViewModel>()
    private lateinit var progressDialog: ProgressDialog
    private lateinit var visitsAdapter: VisitsAdapter

    private var pendingList= arrayListOf<GetRouteAccountResponse>()
    private var isPendingSelected= true
    private var filteredRoutesList: ArrayList<GetRouteAccountResponse>?= null
    private var routesList: ArrayList<GetRouteAccountResponse>?= null
    private var strSearch: String = ""

    private val routeStatus by lazy { arguments?.getString(DataTransferKeys.KEY_STATUS) }
    private val routeName by lazy { arguments?.getString(DataTransferKeys.KEY_NAME) }
    private var selectedItem: GetRouteAccountResponse?=null
    private val jumpFrom by lazy { arguments?.getString(DataTransferKeys.KEY_FROM) }

    @Inject
    lateinit var savedOrderDao: SaveOrderDao

    override fun getLayoutResId(): Int {
        return R.layout.fragment_routes
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(AppRequestCode.CURRENT_ROUTE_ACTIVITY_STATUS_CHANGED) { key, bundle ->
            initialize()
            if (jumpFrom == "home") {
                setFragmentResult(
                    AppRequestCode.CURRENT_ROUTE_STATUS_CHANGED,
                    bundleOf(
                        Pair(DataTransferKeys.KEY_ROUTE_STATUS,""))
                )
            } else if (jumpFrom == "track") {
                setFragmentResult(
                    AppRequestCode.CURRENT_ROUTE_TRACK_STATUS_CHANGED,
                    bundleOf(
                        Pair(DataTransferKeys.KEY_ROUTE_STATUS, "")
                    )
                )
            }
        }

        setFragmentResultListener(AppRequestCode.CURRENT_ACTIVITY_STATUS_CHANGED) { key, bundle ->
            if (bundle.get(DataTransferKeys.KEY_CANCEL_STATUS) != null) {
                val status = bundle.getString(DataTransferKeys.KEY_CANCEL_STATUS, "")
                val reason = bundle.getString(DataTransferKeys.KEY_REASON)
                if (selectedItem!=null) {
                    selectedItem?.Visit?.Activity?.lov_activity_status = RouteStatus.STATUS_SKIPPED
                    setSelectedItemStatus(selectedItem)
                }

            }
        }
    }

    override fun onCreateView(
        instance: Bundle?,
        binding: FragmentRoutesBinding
    ) {
        this.binding = binding

        progressDialog = ProgressDialog(requireActivity())
        bindObservers()
        listeners()
        initialize()
    }

    private fun listeners() {
        binding.ivBack.setOnClickListener {
            navigator.pop()
        }
        binding.tvPending.setOnClickListener {
            if (!isPendingSelected){
                isPendingSelected=true
                notifyAdapter()
                binding.tvPending.setTextColor(requireContext().resources.getColor(R.color.colorPrimary))
                binding.tvAll.setTextColor(requireContext().resources.getColor(R.color.grey_6))
                binding.tvAll.background= null
                binding.tvPending.setBackgroundResource(R.drawable.bg_light_bule_12dp)
            }
        }

        binding.tvAll.setOnClickListener {
            if (isPendingSelected){
                isPendingSelected=false
                binding.tvAll.setTextColor(requireContext().resources.getColor(R.color.colorPrimary))
                binding.tvPending.setTextColor(requireContext().resources.getColor(R.color.grey_6))
                binding.tvPending.background= null
                binding.tvAll.setBackgroundResource(R.drawable.bg_light_bule_12dp)
                notifyAdapter()
            }
        }

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.isNullOrEmpty()) {
                    binding.ivCancelSearch.gone()
                } else {
                    binding.ivCancelSearch.visible()
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })


        binding.ivCancelSearch.setOnClickListener {
            binding.etSearch.setText("")
            strSearch=""
            resetSearch()
            it?.hideKeyboard()
        }

        binding.etSearch.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH && !TextUtils.isEmpty(binding.etSearch.text)) {
                strSearch= binding.etSearch.text.toString()
                setSearchList(strSearch)
                v.hideKeyboard()
                return@OnEditorActionListener true
            }
            false
        })

    }

    private fun initialize() {
//        val isEditable= routeStatus== RouteStatus.STATUS_IN_PROGRESS
        val isEditable= when(routeStatus){
            RouteStatus.STATUS_COMPLETED, RouteStatus.STATUS_SKIPPED,RouteStatus.STATUS_CANCELLED -> 0
            RouteStatus.STATUS_PENDING,RouteStatus.STATUS_NOT_STARTED -> 1
            else-> -1
        }
        visitsAdapter= VisitsAdapter(requireContext(),isEditable,::onRouteClick,::onRouteStatusClick)
        binding.rvTasks.adapter= visitsAdapter
        binding.tvTitle.text= routeName
        routesList= arrayListOf()
        filteredRoutesList= arrayListOf()
        pendingList= arrayListOf()
        viewModel.currentRoute.value?.forEach {
            if(it.Visit?.Activity?.lov_activity_status==RouteStatus.STATUS_PENDING || it.Visit?.Activity?.lov_activity_status==RouteStatus.STATUS_IN_PROGRESS ){
                pendingList.add(it)
            }

            routesList?.add(it)
        }
        filteredRoutesList?.addAll(routesList!!)
        notifyAdapter()
        routesList?.let { setRouteProgress(it) }

    }

    fun resetSearch(){
        filteredRoutesList= arrayListOf()
        pendingList= arrayListOf()
        routesList?.forEach {
            if(it.Visit?.Activity?.lov_activity_status==RouteStatus.STATUS_PENDING
                || it.Visit?.Activity?.lov_activity_status==RouteStatus.STATUS_IN_PROGRESS
            ){
                pendingList.add(it)
            }
        }
        filteredRoutesList?.addAll(routesList!!)
        notifyAdapter()
    }

    fun setSearchList(strSearch: String){
        filteredRoutesList= arrayListOf()
        pendingList= arrayListOf()
        routesList?.forEach {
            if (it.Account?.accountname?.contains(strSearch,true)==true || it.Account?.orderseq?.toString()?.contains(strSearch,true)==true) {
                filteredRoutesList?.add(it)

                if (it.Visit?.Activity?.lov_activity_status == RouteStatus.STATUS_PENDING
                    || it.Visit?.Activity?.lov_activity_status==RouteStatus.STATUS_IN_PROGRESS
                ) {
                    pendingList.add(it)
                }
            }
        }
        notifyAdapter()
    }

    fun setRouteProgress(arrayList: ArrayList<GetRouteAccountResponse>) {

        val progressAdapter= RoutePagerAdapter(requireContext(), arrayList, fragment = this, savedOrderDao = savedOrderDao)
        binding.viewPagerTop.adapter =progressAdapter
        progressAdapter.notifyGraph(viewModel.currentRouteDistance.value)

        val tabLayoutMediator = TabLayoutMediator(
            binding.tlImage, binding.viewPagerTop, true
        ) { tab, position -> }

        tabLayoutMediator.attach()
        viewModel.currentRouteDistance.value?.let {
            progressAdapter.notifyGraph(it)
        }
    }


    @SuppressLint("FragmentLiveDataObserve")
    private fun bindObservers() {

        viewModel.updateRouteApiResponse.setObserver(viewLifecycleOwner, Observer {
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
        viewModel.currentRoute.observe(viewLifecycleOwner, Observer {
            setRouteProgress(it)
        })
    }

    private fun onRouteClick( route: GetRouteAccountResponse?){
        val bundle = Bundle()
        selectedItem= route
        bundle.putParcelable(DataTransferKeys.KEY_TASKS,route)
        bundle.putString(DataTransferKeys.KEY_STATUS, route?.Visit?.Activity?.lov_activity_status)
        bundle.putString(DataTransferKeys.KEY_ROUTE_STATUS, routeStatus)
        navigator.push(TaskFragment::class){
            this.arguments= bundle
        }
    }

    private fun onRouteStatusClick( pos: Int, account: GetRouteAccountResponse?){
        selectedItem= account
        if (account?.Visit?.Activity?.lov_activity_status==RouteStatus.STATUS_PENDING ){
            pendingList.add(0,account)
            notifyAdapter()
        }else if (account?.Visit?.Activity?.lov_activity_status==RouteStatus.STATUS_SKIPPED){
            navigator.showDialog(
                CancelRouteDialog::class,
                bundleOf(Pair(DataTransferKeys.KEY_STATUS, RouteStatus.STATUS_SKIPPED),
                    Pair(DataTransferKeys.KEY_TYPE, "Activity"))
            )
        }else if (account?.Visit?.Activity?.lov_activity_status== RouteStatus.STATUS_IN_PROGRESS){
            setSelectedItemStatus(account)

            val bundle = Bundle()
            selectedItem= account
            bundle.putParcelable(DataTransferKeys.KEY_TASKS,account)
            bundle.putString(DataTransferKeys.KEY_STATUS, account?.Visit?.Activity?.lov_activity_status)
            bundle.putString(DataTransferKeys.KEY_ROUTE_STATUS, routeStatus)
            navigator.push(TaskFragment::class){
                this.arguments= bundle
            }
        }else{
          setSelectedItemStatus(account)

        }

    }

    private fun setSelectedItemStatus(account: GetRouteAccountResponse?,  reason: String? = null) {

        val removeIndex= pendingList.indexOfFirst { (it.Visit?.Activity?.id == account?.Visit?.Activity?.id)
                && (account?.Visit?.Activity?.lov_activity_status!= RouteStatus.STATUS_PENDING)
                && (account?.Visit?.Activity?.lov_activity_status!= RouteStatus.STATUS_IN_PROGRESS)
        }
        if (removeIndex>=0){
            pendingList.removeAt(removeIndex)
        }

        filteredRoutesList?.find { it.Visit?.Activity?.id == account?.Visit?.Activity?.id }?.Visit?.Activity?.lov_activity_status = account?.Visit?.Activity?.lov_activity_status!!
        routesList?.find { it.Visit?.Activity?.id == account.Visit?.Activity?.id }?.let {
            it.Visit?.Activity?.lov_activity_status = account.Visit?.Activity?.lov_activity_status!!
        }
        pendingList.find { it.Visit?.Activity?.id == account.Visit?.Activity?.id }?.let {
            it.Visit?.Activity?.lov_activity_status = account.Visit?.Activity?.lov_activity_status!!
        }
        notifyAdapter()
        viewModel.currentRoute.value= routesList


//        visitsAdapter.notifyDataItem(routesList?.get(pos),pos)
        if (jumpFrom == "home") {
            setFragmentResult(
                AppRequestCode.CURRENT_ROUTE_STATUS_CHANGED,
                bundleOf(
                    Pair(DataTransferKeys.KEY_ROUTE_STATUS,""))
            )
        } else if (jumpFrom == "track") {
            setFragmentResult(
                AppRequestCode.CURRENT_ROUTE_TRACK_STATUS_CHANGED,
                bundleOf(
                    Pair(DataTransferKeys.KEY_ROUTE_STATUS, "")
                )
            )
        }
        updateActivityStatus(account.Visit?.Activity?.lov_activity_status ?: "", account.Visit?.Activity,  reason)


    }
    private fun notifyAdapter(){
        if (isPendingSelected){
            visitsAdapter.notifyData(pendingList)
        }else{
            visitsAdapter.notifyData(filteredRoutesList)
        }
    }


    private fun updateActivityStatus(status: String, account: ActivityTemplate?, reason: String? = null){

         viewModel.currentRoute.value?.forEach {
            if (it.Visit?.Activity?.id== account?.id){
                it.Visit?.Activity= account
                lifecycleScope.launchWhenCreated {
                    routeActivityDao.insert(it)
                }
            }
        }

//       (requireActivity() as MainActivity).currentRoute=routesList

        val activityData= RouteActivityData(
            id = account?.id ?: 0,
            lov_activity_status = status,
            actual_enddate = account?.actual_enddate,
            lov_route_exec_status_reason = reason,
            actual_startdate = account?.actual_startdate,
            latitude = account?.latitude,
            longitude = account?.longitude
        )
        val routeList= arrayListOf<UpdateRouteActivity>()
        routeList.add(UpdateRouteActivity(activityData))
        if (isConnectedToInternet(requireContext(),false)) {
            viewModel.updateRouteActivityApi(requireContext(), routeList)
        }else{
            lifecycleScope.launchWhenCreated {
                val result=updateActivityDao.insert(routeList)
                Timber.e(result.toString())
//                viewModel.currentRoute.value.let {
//
//                    it?.forEach { account ->
////                        account.routeId = route?.Route?.id
//                        lifecycleScope.launchWhenCreated {
//                                routeActivityDao.deleteRoute(account.routeId)
//                            routeActivityDao.insert(account)
//                        }
//                    }
//                }
            }
        }
    }


}