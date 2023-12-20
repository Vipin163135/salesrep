package com.salesrep.app.ui.route

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.github.fragivity.navigator
import com.github.fragivity.pop
import com.github.fragivity.popToRoot
import com.github.fragivity.push
import com.salesrep.app.R
import com.salesrep.app.base.BaseFragment
import com.salesrep.app.data.models.response.GetHomeRoutesResponse
import com.salesrep.app.data.models.response.Route
import com.salesrep.app.data.network.responseUtil.ApisRespHandler
import com.salesrep.app.data.network.responseUtil.Status
import com.salesrep.app.data.repos.UserRepository
import com.salesrep.app.databinding.LayoutListingBinding
import com.salesrep.app.ui.dialogs.ProgressDialog
import com.salesrep.app.ui.home.HomeViewModel
import com.salesrep.app.ui.route.adapters.RouteListAdapter
import com.salesrep.app.util.AppRequestCode.REFRESH_HOME_REQUEST
import com.salesrep.app.util.DataTransferKeys
import com.salesrep.app.util.PrefsManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RouteListFragment : BaseFragment<LayoutListingBinding>() {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    private lateinit var binding: LayoutListingBinding
    override val viewModel by activityViewModels<HomeViewModel>()
    private lateinit var progressDialog: ProgressDialog
    private lateinit var adapter: RouteListAdapter
    
    
    var routeList = ArrayList<Route>()
    private lateinit var selectedRoute : Route


    override fun getLayoutResId(): Int {
        return R.layout.layout_listing
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                if (navigator.currentDestination?.label== "com.salesrep.app.ui.route.RouteListFragment") {
                    setFragmentResult(REFRESH_HOME_REQUEST, bundleOf())
                    navigator.popToRoot()
                } else {

                    navigator.pop()
                }
                // in here you can do logic when backPress is clicked
            }
        })

    }
    override fun onCreateView(
        instance: Bundle?,
        binding: LayoutListingBinding
    ) {
        this.binding = binding

        progressDialog = ProgressDialog(requireActivity())
        initialize()
        bindObservers()
        listeners()

    }

    private fun listeners() {
        binding.ivBack.setOnClickListener {
            setFragmentResult(REFRESH_HOME_REQUEST, bundleOf())
            navigator.popToRoot()
        }
    }

    
    private fun initialize() {

        adapter= RouteListAdapter(requireContext(),::onRouteClick)
        binding.rvItems.adapter= adapter
        val homeRoutes= prefsManager.getObject(PrefsManager.TEAM_ROUTES,GetHomeRoutesResponse::class.java)
        setList( homeRoutes?.AllRoutes)
    }

    @SuppressLint("FragmentLiveDataObserve")
    private fun bindObservers() {
        viewModel.getCurrentRouteAccountResponse.setObserver(viewLifecycleOwner, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.LOADING -> progressDialog.setLoading(true)
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)
                    if (it.data != null) {
                        viewModel.currentRoute.value = it.data
                        val bundle = Bundle()
                        bundle.putParcelable(DataTransferKeys.KEY_ROUTES, selectedRoute)
                        bundle.putString(DataTransferKeys.KEY_NAME, selectedRoute.Route?.title)
                        bundle.putParcelableArrayList(DataTransferKeys.KEY_CURRENT_ROUTE, it.data)
                        navigator.push(TrackFragment::class) {
                            this.arguments = bundle
                        }
                    } else {
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

    }

    private fun setList(data: List<Route>?) {
        data?.let { 
            routeList.addAll(it)
            adapter.notifyData(routeList)
        }
    }

    private fun onRouteClick( route: Route?){
        route?.let {
            selectedRoute= it
            viewModel.route.value = selectedRoute
            viewModel.getCurrentRouteAccountApi(requireContext(), route?.Route?.id)
        }
    }

}