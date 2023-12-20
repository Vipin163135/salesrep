package com.salesrep.app.ui.customer

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.github.fragivity.navigator
import com.github.fragivity.popToRoot
import com.github.fragivity.push
import com.salesrep.app.R
import com.salesrep.app.base.BaseFragment
import com.salesrep.app.data.models.response.CustomerListModel
import com.salesrep.app.data.models.response.GetCustomerListResponse
import com.salesrep.app.data.network.responseUtil.ApisRespHandler
import com.salesrep.app.data.network.responseUtil.Status
import com.salesrep.app.data.repos.UserRepository
import com.salesrep.app.databinding.FragmentCustomerListBinding
import com.salesrep.app.ui.customer.adapter.CustomerListAdapter
import com.salesrep.app.ui.customer.add_customer.AddCustomerFragment
import com.salesrep.app.ui.dialogs.ProgressDialog
import com.salesrep.app.util.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CustomerListFragment : BaseFragment<FragmentCustomerListBinding>() {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    private lateinit var binding: FragmentCustomerListBinding
    override val viewModel by viewModels<CustomerViewModel>()
    private lateinit var progressDialog: ProgressDialog
    private lateinit var adapter: CustomerListAdapter

    private var page: Int = 1
    private var totalProducts: Int = 0
    private var strSearch: String = ""

     var customerList = ArrayList<CustomerListModel>()


    override fun getLayoutResId(): Int {
        return R.layout.fragment_customer_list
    }


    override fun onCreateView(
        instance: Bundle?,
        binding: FragmentCustomerListBinding
    ) {
        this.binding = binding

        progressDialog = ProgressDialog(requireActivity())
        initialize()
        bindObservers()
        listeners()

    }

    private fun listeners() {
        binding.ivBack.setOnClickListener {
            navigator.popToRoot()
        }

        binding.btnAdd.setOnClickListener {
            navigator.push(AddCustomerFragment::class)
        }


        binding.rvCustomers.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
//                totalItemCount = linearLayoutManager.itemCount
//                //last visible item position
//                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition()
                if ((totalProducts > customerList.size)  && !recyclerView.canScrollVertically(1)) {
                    page++
                    hitProductsApi()
                }
            }
        })

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
            page=1
            hitProductsApi()
            it?.hideKeyboard()
        }

        binding.etSearch.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH && !TextUtils.isEmpty(binding.etSearch.text)) {
                page=1
                strSearch= binding.etSearch.text.toString()
                hitProductsApi()
                v.hideKeyboard()
                return@OnEditorActionListener true
            }
            false
        })

    }



    private fun hitProductsApi() {
            viewModel.getCustomerListApi(requireContext(),  page, PER_PAGE, strSearch)
    }

    private fun initialize() {

        adapter= CustomerListAdapter(requireContext(),::onRouteClick)
        binding.rvCustomers.adapter= adapter
        strSearch= ""
        hitProductsApi()
        
    }

    @SuppressLint("FragmentLiveDataObserve")
    private fun bindObservers() {
        viewModel.getTeamAccountsResponse.setObserver(viewLifecycleOwner, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.LOADING -> progressDialog.setLoading(true)
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)
                    if (it.data != null) {
                        setList(it.data)
//                        if (page==1){
//                            totalProducts= it.data.pagination.size
//                        }
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

    private fun setList(data: GetCustomerListResponse) {
        if (page==1) {
            customerList.clear()
            totalProducts= data?.pagination?.size
        }
        data.rows.forEach {
            customerList.add(it)
        }
        adapter.notifyData(customerList)
    }

    private fun onRouteClick( customer: CustomerListModel){
        navigator.push(CustomerDetailFragment::class){
            arguments= bundleOf(Pair(DataTransferKeys.KEY_CUSTOMER_DETAIL,customer))
        }

    }

}