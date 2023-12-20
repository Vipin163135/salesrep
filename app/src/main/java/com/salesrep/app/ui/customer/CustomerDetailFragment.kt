package com.salesrep.app.ui.customer

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.github.fragivity.navigator
import com.github.fragivity.pop
import com.github.fragivity.popToRoot
import com.github.fragivity.push
import com.salesrep.app.R
import com.salesrep.app.base.BaseFragment
import com.salesrep.app.data.models.response.*
import com.salesrep.app.data.network.responseUtil.ApisRespHandler
import com.salesrep.app.data.network.responseUtil.Status
import com.salesrep.app.data.repos.UserRepository
import com.salesrep.app.databinding.FragmentCustomerDetailBinding
import com.salesrep.app.databinding.FragmentCustomerListBinding
import com.salesrep.app.ui.customer.adapter.*
import com.salesrep.app.ui.customer.add_customer.AddCustomerFragment
import com.salesrep.app.ui.dialogs.ProgressDialog
import com.salesrep.app.ui.returnOrder.ReturnOrderFragment
import com.salesrep.app.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_select_team.*
import javax.inject.Inject

@AndroidEntryPoint
class CustomerDetailFragment : BaseFragment<FragmentCustomerDetailBinding>() {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    private lateinit var binding: FragmentCustomerDetailBinding
    override val viewModel by viewModels<CustomerViewModel>()
    private lateinit var progressDialog: ProgressDialog
    private val customerDetail by lazy {
        arguments?.getParcelable<CustomerListModel>(
            DataTransferKeys.KEY_CUSTOMER_DETAIL
        )
    }
    private var spinnerList = arrayListOf<String>()
    private var selectedPos = -1
    private lateinit var routeAdapter: CustomerRouteAdapter
    private lateinit var ordersAdapter: CustomerOrderAdapter
    private lateinit var merchandiseAdapter: CustomerAssetAdapter
    private lateinit var productsAdapter: CustomerProductAdapter

    override fun getLayoutResId(): Int {
        return R.layout.fragment_customer_detail
    }

    override fun onCreateView(
        instance: Bundle?,
        binding: FragmentCustomerDetailBinding
    ) {
        this.binding = binding

        progressDialog = ProgressDialog(requireActivity())
        initialize()
        listeners()
    }

    private fun listeners() {
        binding.tvBack.setOnClickListener {
            navigator.pop()
        }
        binding.llOption.setOnClickListener {
            binding.spinner.performClick()
        }
        binding.tvTeamName.setOnClickListener {
            binding.spinner.performClick()
        }
    }

    private fun initialize() {
        customerDetail?.let {
            binding.tvCode.text = it.Account?.customer_number.toString()
            binding.tvName.text = it.Account?.accountname.toString()
            binding.tvCreditLimit.text = String.format("%.2f", it.Account?.credit_limit ?: 0.0)
            binding.tvAvailableCredit.text =
                String.format("%.2f", it.Account?.calc_current_available_credit ?: 0.0)
            binding.tvOpenBalance.text = String.format("%.2f", it.Account?.calc_due_amount ?: 0.0)
            binding.tvDayOfVisit.text = it.Account?.calc_deliverydayname
            routeAdapter = CustomerRouteAdapter(requireContext())
            productsAdapter = CustomerProductAdapter(requireContext())
            binding.rvRoutes.adapter = routeAdapter
            binding.rvProducts.adapter = productsAdapter
            ordersAdapter= CustomerOrderAdapter(requireContext(),::OnClickOrder,it.Orders ?: listOf())
            merchandiseAdapter = CustomerAssetAdapter(requireContext(),it.Assets ?: listOf())
            binding.rvMerchandise.adapter= merchandiseAdapter

            binding.rvOrders.adapter= ordersAdapter
        }
        spinnerList.add("Product Assortment")
        spinnerList.add("Orders")
        spinnerList.add("Merchandise Material")
        spinnerList.add("Route")

        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            spinnerList.toList()
        )

        binding.spinner.adapter = spinnerAdapter

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedPos = position
                binding.tvTeamName.text = spinnerList.get(position)
                when (position) {
                    0 -> {
                        binding.viewFlipper.displayedChild = 0
                        productsAdapter.notifyData(customerDetail?.ProductAssortment)
                    }
                    1 -> {
                        binding.viewFlipper.displayedChild = 1
                    }
                    2 -> {
                        binding.viewFlipper.displayedChild = 2
                    }
                    else -> {

                        binding.viewFlipper.displayedChild = 3
                        routeAdapter.notifyData(customerDetail?.Routes)
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }


    private fun OnClickOrder(order: OrderListObject) {

        val accountDetail= GetRouteAccountResponse(
            Account= customerDetail?.Account
        )

        navigator.push(ReturnOrderFragment::class) {
            this.arguments = bundleOf(
                Pair(DataTransferKeys.KEY_ACCOUNT_DETAIL, accountDetail),
                Pair(DataTransferKeys.KEY_ORDER_ID, order)
            )
        }
    }

//    @SuppressLint("FragmentLiveDataObserve")
//    private fun bindObservers() {
//        viewModel.getTeamAccountsResponse.setObserver(viewLifecycleOwner, Observer {
//            it ?: return@Observer
//            when (it.status) {
//                Status.LOADING -> progressDialog.setLoading(true)
//                Status.SUCCESS -> {
//                    progressDialog.setLoading(false)
//                    if (it.data != null) {
//                        setList(it.data)
////                        if (page==1){
////                            totalProducts= it.data.pagination.size
////                        }
//                    }
//
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
//
//
//    }


}