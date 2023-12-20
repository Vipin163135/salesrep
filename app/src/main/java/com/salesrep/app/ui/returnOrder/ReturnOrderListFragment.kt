package com.salesrep.app.ui.returnOrder

import android.os.Bundle
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
import com.salesrep.app.dao.SaveOrderDao
import com.salesrep.app.data.models.response.GetRouteAccountResponse
import com.salesrep.app.data.models.response.GetTeamPricelistResponse
import com.salesrep.app.data.models.response.OrderListObject
import com.salesrep.app.data.repos.UserRepository
import com.salesrep.app.databinding.FragmentOrderListBinding
import com.salesrep.app.ui.dialogs.ProgressDialog
import com.salesrep.app.ui.home.HomeViewModel
import com.salesrep.app.ui.takeOrder.OrderListAdapter
import com.salesrep.app.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.lang.reflect.Type
import javax.inject.Inject


@AndroidEntryPoint
class ReturnOrderListFragment : BaseFragment<FragmentOrderListBinding>(){

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var saveOrderDao: SaveOrderDao

    override val viewModel by viewModels<HomeViewModel>()
    private lateinit var binding: FragmentOrderListBinding

    private lateinit var accountDetail : GetRouteAccountResponse

    private var savedOrderList: ArrayList<OrderListObject>? =null

    private lateinit var progressDialog: ProgressDialog
    private var isCompleted: Boolean?= false

    private lateinit var adapter: OrderListAdapter

    override fun getLayoutResId(): Int {
        return R.layout.fragment_order_list
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFragmentResultListener(ReturnOrderFragment.ON_RETURN_ORDER_STATUS_CHANGE) { key, bundle ->
            initialize()
        }
    }

    override fun onCreateView(instance: Bundle?, binding: FragmentOrderListBinding) {
        this.binding = binding

        progressDialog = ProgressDialog(requireActivity())

//        adapter= OrderListAdapter(requireContext(),::OnClickOrder)
//        binding.rvOrders.adapter= adapter

        accountDetail= arguments?.getParcelable<GetRouteAccountResponse>(
            DataTransferKeys.KEY_PRODUCTS
        )!!
        savedOrderList= arrayListOf()
        accountDetail.Orders?.forEach {
            if (it.Order?.lov_order_type=="Return")
                savedOrderList?.add(it)
        }
        binding.tvCusomerName.text = accountDetail.Account?.name
        binding.tvCusomerNum.text = accountDetail.Account?.accountname
        isCompleted= arguments?.getBoolean(DataTransferKeys.KEY_IS_COMPLETED,false)

        initialize()
        listeners()
        bindObservers()

        when (userRepository.getTeam()?.Team?.lov_team_type){
            "Delivery",
            "Trade" ->{
                binding.tvNewOrder.gone()
            }
            else ->{
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

//            runBlocking { deleteOrder()}
            setFragmentResult(
                AppRequestCode.CURRENT_TASK_ORDER_STATUS_CHANGED,
                bundleOf()
            )
            navigator.pop()
//            setFragmentResult(AppRequestCode.CURRENT_TASK_ORDER_STATUS_CHANGED,
//                bundleOf()
//            )
//            navigator.pop()
        }
        binding.tvNewOrder.setOnClickListener {
            navigator.push(ReturnOrderFragment::class) {
                this.arguments = bundleOf(
                    Pair(DataTransferKeys.KEY_ACCOUNT_DETAIL, accountDetail),
                    Pair(DataTransferKeys.KEY_IS_NEW,true)
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
                "Return"
            )
        }
    }

    private fun bindObservers() {

    }

    private fun initialize() {
        val gson = prefsManager.getString(PrefsManager.TEAM_PRICELIST, "")
        var currentPriceList: GetTeamPricelistResponse?= GetTeamPricelistResponse()
        if (!gson.isNullOrEmpty()) {

            val listType: Type = object : TypeToken<ArrayList<GetTeamPricelistResponse?>?>() {}.type
            val productPriceList =
                Gson().fromJson<ArrayList<GetTeamPricelistResponse>>(gson, listType)
            if (!productPriceList.isNullOrEmpty()){
                currentPriceList = productPriceList[0]
            }
        }

        lifecycleScope.launchWhenCreated {
            val orderList = saveOrderDao.getReturnOrders(
                accountDetail?.routeId,
                accountDetail?.Visit?.Activity?.id,
                accountDetail?.Account?.id,
                OrderType.TYPE_RETURN
            )

            if (orderList.isNotEmpty()) {
                orderList.forEach { order ->
                    val index= savedOrderList?.indexOfFirst { it.Order?.id== order.Order?.id } ?: -1
                    if (index < 0) {
                        savedOrderList?.add(
                            OrderListObject(
                                Order = order.Order,
                                CartProducts = order.CartProducts,
                                ProductAssortment = order.ProductAssortment,
                                PendingPayments = order.PendingPayments ?: accountDetail.PendingPayments ,
                                id = order.id,
                                lov_order_type = order.lov_order_type,
                                lov_return_reason = order.lov_return_reason,
                                tcp_activity_integration_id = order.tcp_activity_integration_id
                            )
                        )
                    }else{
                        savedOrderList!![index]=  OrderListObject(
                            Order = order.Order,
                            CartProducts = order.CartProducts,
                            ProductAssortment = order.ProductAssortment,
                            PendingPayments = order.PendingPayments ?: accountDetail.PendingPayments ,
                            id = order.id,
                            lov_order_type = order.lov_order_type,
                            lov_return_reason = order.lov_return_reason,
                            tcp_activity_integration_id = order.tcp_activity_integration_id
                        )
                    }
                }
            }

            binding.tvBack.text= getString(R.string.return_order_list)

            adapter= OrderListAdapter(requireContext(),
                true,
                ::OnClickOrder,
                savedOrderList ?: listOf(),
                currentPriceList?.Pricelist?.currency_symbol?:"$"
            )
            binding.rvOrders.adapter= adapter

        }
    }
    private fun OnClickOrder(order: OrderListObject) {
        navigator.push(ReturnOrderFragment::class) {
            this.arguments = bundleOf(
                Pair(DataTransferKeys.KEY_ACCOUNT_DETAIL, accountDetail),
                Pair(DataTransferKeys.KEY_ORDER_ID, order),
                Pair(DataTransferKeys.KEY_IS_COMPLETED, isCompleted)
            )
        }
    }
}