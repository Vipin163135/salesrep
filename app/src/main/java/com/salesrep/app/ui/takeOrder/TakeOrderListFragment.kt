package com.salesrep.app.ui.takeOrder

import android.os.Bundle
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.github.fragivity.navigator
import com.github.fragivity.pop
import com.github.fragivity.push
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.salesrep.app.R
import com.salesrep.app.base.BaseFragment
import com.salesrep.app.dao.CreateOrderDao
import com.salesrep.app.dao.SaveOrderDao
import com.salesrep.app.data.models.SaveOrderTemplate
import com.salesrep.app.data.models.response.*
import com.salesrep.app.data.repos.UserRepository
import com.salesrep.app.databinding.FragmentOrderListBinding
import com.salesrep.app.databinding.FragmentTakeOrderNewBinding
import com.salesrep.app.ui.dialogs.ProgressDialog
import com.salesrep.app.ui.home.HomeViewModel
import com.salesrep.app.ui.payment.PaymentFragment
import com.salesrep.app.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.lang.reflect.Type
import javax.inject.Inject

@AndroidEntryPoint
class TakeOrderListFragment : BaseFragment<FragmentOrderListBinding>() {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var saveOrderDao: SaveOrderDao

    override val viewModel by viewModels<HomeViewModel>()
    private lateinit var binding: FragmentOrderListBinding

    private lateinit var accountDetail: GetRouteAccountResponse

    private var savedOrderList: ArrayList<OrderListObject>? = null

    private lateinit var progressDialog: ProgressDialog

    private lateinit var adapter: OrderListAdapter

    private var isCompleted: Boolean?= false

    override fun getLayoutResId(): Int {
        return R.layout.fragment_order_list
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFragmentResultListener(TakeOrderFragment.ON_ORDER_STATUS_CHANGE) { key, bundle ->
            initialize()
        }

    }

    override fun onCreateView(instance: Bundle?, binding: FragmentOrderListBinding) {
        this.binding = binding

        progressDialog = ProgressDialog(requireActivity())

//        adapter= OrderListAdapter(requireContext(),::OnClickOrder)
//        binding.rvOrders.adapter= adapter

        accountDetail = arguments?.getParcelable<GetRouteAccountResponse>(
            DataTransferKeys.KEY_PRODUCTS
        )!!
        savedOrderList = arrayListOf()

        accountDetail.Orders?.forEach {
            if (it.Order?.lov_order_type == "Sales")
                savedOrderList?.add(it)
        }

        binding.tvCusomerName.text = accountDetail.Account?.name
        binding.tvCusomerNum.text = accountDetail.Account?.accountname

        initialize()
        listeners()
        bindObservers()

        isCompleted= arguments?.getBoolean(DataTransferKeys.KEY_IS_COMPLETED,false)


        when (userRepository.getTeam()?.Team?.lov_team_type) {
            "Delivery",
            "Trade" -> {
                binding.tvNewOrder.gone()
            }
            else -> {
                binding.tvNewOrder.visible()
            }
        }


        if (isCompleted== true){
            binding.tvNewOrder.gone()
            binding.tvEnd.gone()
        }
    }

    private fun listeners() {
        binding.tvBack.setOnClickListener {
            navigator.popBackStack()
        }
        binding.tvEnd.setOnClickListener {

//            runBlocking { deleteOrder() }

//            setFragmentResult(AppRequestCode.CURRENT_TASK_ORDER_STATUS_CHANGED,
//                bundleOf()
//            )
            setFragmentResult(
                AppRequestCode.CURRENT_TASK_ORDER_STATUS_CHANGED,
                bundleOf()
            )
            navigator.pop()
        }
        binding.tvNewOrder.setOnClickListener {
            navigator.push(TakeOrderFragment::class) {
                this.arguments = bundleOf(
                    Pair(DataTransferKeys.KEY_ACCOUNT_DETAIL, accountDetail),
                    Pair(DataTransferKeys.KEY_IS_NEW, true)
                )
            }
        }
    }

    suspend fun deleteOrder() = coroutineScope {
        launch {
            saveOrderDao.deleteOrders(
                accountDetail?.routeId,
                accountDetail?.Visit?.Activity?.id,
                accountDetail?.Account?.id,
                "Sales"
            )
        }
    }

    private fun bindObservers() {

    }

    private fun initialize() {

        val gson = prefsManager.getString(PrefsManager.TEAM_PRICELIST, "")
        var currentPriceList:GetTeamPricelistResponse?= GetTeamPricelistResponse()
        if (!gson.isNullOrEmpty()) {

            val listType: Type = object : TypeToken<ArrayList<GetTeamPricelistResponse?>?>() {}.type
            val productPriceList =
                Gson().fromJson<ArrayList<GetTeamPricelistResponse>>(gson, listType)
            if (!productPriceList.isNullOrEmpty()){
                currentPriceList = productPriceList[0]
            }
        }
        lifecycleScope.launchWhenCreated {

            val orderList = saveOrderDao.getCustomerOrders(
                accountDetail?.routeId,
                accountDetail?.Visit?.Activity?.id,
                accountDetail?.Account?.id,
                "Sales"
            )
            val creditLimit = accountDetail.Account?.credit_limit ?: 0.0
            var availCredit = creditLimit ?: 0.0
            if (orderList.isNotEmpty()) {
                orderList.forEach { order ->
                    if (creditLimit > 0.0 && order.Order?.lov_order_status == "04 - Delivered") {
                        availCredit -= order.Order?.total_due ?: 0.0
                    }

                    val index =
                        savedOrderList?.indexOfFirst { it.Order?.id == order.Order?.id } ?: -1
                    if (index < 0) {
                        savedOrderList?.add(
                            OrderListObject(
                                Order = order.Order,
                                CartProducts = order.CartProducts,
                                ProductAssortment = order.ProductAssortment,
                                PendingPayments = order.PendingPayments
                                    ?: accountDetail.PendingPayments,
                                id = order.id
                            )
                        )
                    } else {
                        savedOrderList!![index] = OrderListObject(
                            Order = order.Order,
                            CartProducts = order.CartProducts,
                            ProductAssortment = order.ProductAssortment,
                            PendingPayments = order.PendingPayments
                                ?: accountDetail.PendingPayments,
                            id = order.id
                        )
                    }
                }
            }
            adapter = OrderListAdapter(
                requireContext(),
                false,
                ::OnClickOrder,
                savedOrderList ?: listOf(),
                currentPriceList?.Pricelist?.currency_symbol?:"$"
            )
            binding.rvOrders.adapter = adapter

            accountDetail.Account?.calc_available_credit = availCredit

        }

        binding.tvBack.text = getString(R.string.order_list)

    }

    private fun OnClickOrder(order: OrderListObject) {
        navigator.push(TakeOrderFragment::class) {
            this.arguments = bundleOf(
                Pair(DataTransferKeys.KEY_ACCOUNT_DETAIL, accountDetail),
                Pair(DataTransferKeys.KEY_ORDER_ID, order),
                Pair(DataTransferKeys.KEY_IS_COMPLETED, isCompleted)
            )
        }
    }


}