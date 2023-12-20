package com.salesrep.app.ui.payment

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
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
import com.salesrep.app.dao.SaveOrderDao
import com.salesrep.app.data.models.*
import com.salesrep.app.data.models.response.*
import com.salesrep.app.data.network.responseUtil.ApisRespHandler
import com.salesrep.app.data.network.responseUtil.Status
import com.salesrep.app.data.repos.UserRepository
import com.salesrep.app.databinding.FragmentCollectPaymentBinding
import com.salesrep.app.ui.dialogs.ProgressDialog
import com.salesrep.app.ui.home.HomeViewModel
import com.salesrep.app.ui.payment.adapters.AddedPaymentsAdapter
import com.salesrep.app.ui.payment.adapters.PaymentOrderAdapter
import com.salesrep.app.ui.payment.adapters.CollectPaymentProfileAdapter
import com.salesrep.app.ui.takeOrder.TakeOrderFragment
import com.salesrep.app.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_payment_options.*
import java.lang.reflect.Type
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@AndroidEntryPoint
class PaymentCollectionFragment : BaseFragment<FragmentCollectPaymentBinding>() {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var saveOrderDao: SaveOrderDao


    override val viewModel by viewModels<HomeViewModel>()
    private lateinit var binding: FragmentCollectPaymentBinding

    private lateinit var accountDetail: GetRouteAccountResponse

    private var savedOrderList: ArrayList<OrderListObject>? = null

    private lateinit var progressDialog: ProgressDialog

    private lateinit var adapter: PaymentOrderAdapter
    private var isPendingSelected = true

    private lateinit var paymentProfilesAdapter: CollectPaymentProfileAdapter
    private lateinit var addedPaymentAdapter: AddedPaymentsAdapter
    val paymentViewModel by viewModels<PaymentViewModel>()
    private var availableCredit: Double = 0.0
    private var totalAmount: Double = 0.0
    private var currentPriceList: GetTeamPricelistResponse? = null

    private var createPaymentTemplate: CreatePaymentTemplate? = null

    private var createPaymentList = arrayListOf<CreatePaymentTemplate>()

    private var addedPaymentList = arrayListOf<OrderPaymentTemplate>()

    private var selectedPaymentProfile: Paymentprofiles? = null
    private var selectedpos = -1
    private val isCompleted by lazy {
        arguments?.getBoolean(
            DataTransferKeys.KEY_IS_COMPLETED, false
        )
    }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_collect_payment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(PaymentFragment.REQUEST_SELECT_PAYMENT) { key, bundle ->
            if (bundle.get(PaymentFragment.RESULT_SELECT_PAYMENT_DATA) != null) {
                selectedPaymentProfile =
                    bundle.getParcelable(PaymentFragment.RESULT_SELECT_PAYMENT_DATA)

                if (selectedPaymentProfile != null) {
                    binding.tvPaymentMethod.text =
                        selectedPaymentProfile?.Paymentprofile?.lov_paymentprofile_gateway
                } else {
                    binding.tvPaymentMethod.text = getString(R.string.cash)
                }
            } else {
                selectedPaymentProfile = null
                binding.tvPaymentMethod.text = getString(R.string.cash)
            }
        }

        setFragmentResultListener(TakeOrderFragment.ON_ORDER_STATUS_CHANGE) { key, bundle ->
            initialize()
        }

    }

    override fun onCreateView(instance: Bundle?, binding: FragmentCollectPaymentBinding) {
        this.binding = binding

        progressDialog = ProgressDialog(requireActivity())

        initialize()
        listeners()
        observers()
    }

    @SuppressLint("FragmentLiveDataObserve")
    private fun observers() {
        paymentViewModel.createPaymentResponse.observe(this, androidx.lifecycle.Observer {
            it ?: return@Observer
            when (it.status) {
                Status.LOADING -> progressDialog.setLoading(true)
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)
                    updatePaymentAdapter()
                    savePendingPayments()
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

    private fun savePendingPayments() {
        var pendingPaymentList = arrayListOf<PendingPaymentTemplate>()
        createPaymentList.forEach { payment ->
            pendingPaymentList.add(
                PendingPaymentTemplate(Payment = payment)
            )
        }
        accountDetail.PendingPayments = pendingPaymentList
    }

    private fun listeners() {
        binding.tvBack.setOnClickListener {
            navigator.popBackStack()
        }

        binding.tvPay.setOnClickListener {
            if (!isPendingSelected) {
                isPendingSelected = true
                binding.tvPay.setTextColor(requireContext().resources.getColor(R.color.colorPrimary))
                binding.tvOrders.setTextColor(requireContext().resources.getColor(R.color.grey_6))
                binding.tvOrders.background = null
                binding.tvPay.setBackgroundResource(R.drawable.bg_light_bule_12dp)
                binding.viewFlipper.displayedChild = 0
            }
        }
        binding.tvOrders.setOnClickListener {
            if (isPendingSelected) {
                isPendingSelected = false
                binding.tvOrders.setTextColor(requireContext().resources.getColor(R.color.colorPrimary))
                binding.tvPay.setTextColor(requireContext().resources.getColor(R.color.grey_6))
                binding.tvPay.background = null
                binding.tvOrders.setBackgroundResource(R.drawable.bg_light_bule_12dp)
                binding.viewFlipper.displayedChild = 3
            }
        }

        binding.tvNew.setOnClickListener {

            if (!createPaymentList.isNullOrEmpty()) {
                binding.viewFlipper.displayedChild = 1
            } else {
//                binding.tvPaymentNum.text = (1001 + addedPaymentList.size).toString()
                binding.tvPaymentNum.text = Calendar.getInstance().timeInMillis.toString()

                binding.viewFlipper.displayedChild = 2
            }
        }

        binding.tvPrint.setOnClickListener {
            navigator.push(PrintPaymentCollectionFragment::class) {
                arguments = bundleOf(Pair(DataTransferKeys.KEY_ORDER_LIST, savedOrderList))
            }
        }

//        binding.tvAddNew.setOnClickListener {
//
////            if (createPaymentList.isNullOrEmpty()) {
////                binding.viewFlipper.displayedChild = 2
////            } else {
//                binding.tvPaymentNum.text = "1001"
//                binding.viewFlipper.displayedChild = 2
////            }
//        }

        binding.tvCancelNew.setOnClickListener {
            binding.viewFlipper.displayedChild = 1
        }

        binding.tvCancel.setOnClickListener {
            binding.viewFlipper.displayedChild = 0
        }

        binding.tvSelectPaymentMethod.setOnClickListener {
            navigator.push(PaymentFragment::class) {
                arguments =
                    bundleOf(Pair(DataTransferKeys.KEY_ACCOUNT, accountDetail.Paymentprofiles))
            }
        }

        binding.tvSelect.setOnClickListener {
            val index = selectedpos

            if (createPaymentList.isNotEmpty() && index > -1) {
                val selectedPayment = createPaymentList[index]

                createPaymentTemplate = selectedPayment.copy()

                var amount = (createPaymentTemplate?.amount ?: 0.0)
                var allocaionList = arrayListOf<OrderAllocations>()
                savedOrderList?.forEach { order ->
                    if (amount > 0.0 && (order.Order?.total_due ?: 0.0) > 0.0) {
                        val orderAmount = if (amount > (order.Order?.total_due ?: 0.0))
                            order.Order?.total_due
                        else
                            amount

                        allocaionList.add(
                            OrderAllocations(
                                amount = orderAmount ?: 0.0,
                                order_id = order.Order!!.id!!
                            )
                        )
                        amount -= (order.Order?.total_due ?: 0.0)

                        if (orderAmount == order.Order?.total_due) {
                            order.Order?.total_due = 0.0
                            order.Order?.lov_order_status = "05 - Closed"
                        } else {
                            order.Order?.total_due =
                                ((order.Order?.total_due ?: 0.0) - (orderAmount ?: 0.0))
                        }
                        if (order.Payments == null) {
                            order.Payments = arrayListOf()
                        }
                        order.Payments?.add(
                            OrderPaymentTemplate(
                                Payment = CreatePaymentTemplate(
                                    createPaymentTemplate?.id,
                                    createPaymentTemplate?.account_id,
                                    integration_id = createPaymentTemplate?.integration_id,
                                    amount = orderAmount,
                                    lov_payment_method = if (createPaymentTemplate?.payment_type.isNullOrEmpty()) createPaymentTemplate?.lov_payment_method else createPaymentTemplate?.payment_type,
                                    lov_payment_status = "Used"
                                )
                            )
                        )

                        if (order.PendingPayments == null) {
                            order.PendingPayments = arrayListOf()
                        }
                        order.PendingPayments?.add(
                            PendingPaymentTemplate(
                                Payment = CreatePaymentTemplate(
                                    createPaymentTemplate?.id,
                                    createPaymentTemplate?.account_id,
                                    integration_id = createPaymentTemplate?.integration_id,
                                    amount = orderAmount,
                                    payment_type = if (createPaymentTemplate?.payment_type.isNullOrEmpty()) createPaymentTemplate?.lov_payment_method else createPaymentTemplate?.payment_type,
                                    lov_payment_method = if (createPaymentTemplate?.payment_type.isNullOrEmpty()) createPaymentTemplate?.lov_payment_method else createPaymentTemplate?.payment_type,
                                    lov_payment_status = "Used"
                                )
                            )
                        )
                        saveOrderToDb(order)
                    }
                }

                if (amount > 0.0) {
                    createPaymentTemplate?.amount = calculateDue()
                    createPaymentList[index].amount = amount
                } else {
                    createPaymentList.removeAt(index)
                }
                paymentProfilesAdapter.addList(createPaymentList, -1, true)

                adapter.notifyData(savedOrderList)

                if (isConnectedToInternet(requireContext(), false)) {
                    paymentViewModel.createPaymentApi(
                        requireContext(),
                        arrayListOf(createPaymentTemplate!!)
                    )
                } else {
                    updatePaymentAdapter()
                    savePendingPayments()
                }

            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.please_select_payment),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        binding.tvSave.setOnClickListener {
            if (TextUtils.isEmpty(binding.etAmount.text)) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.pelase_enter_amount),
                    Toast.LENGTH_LONG
                ).show()
            } else if (binding.etAmount.text.toString().toDouble() == 0.0) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.pelase_enter_amount),
                    Toast.LENGTH_LONG
                ).show()
            } else {

                createPaymentTemplate = CreatePaymentTemplate()

                if (selectedPaymentProfile != null) {
                    createPaymentTemplate?.paymentprofile_id =
                        selectedPaymentProfile?.Paymentprofile?.id
                    createPaymentTemplate?.payment_type =
                        selectedPaymentProfile?.Paymentprofile?.lov_paymentprofile_gateway
                } else {
                    createPaymentTemplate?.payment_type = "Cash"
                }

                createPaymentTemplate?.amount = etAmount.text.toString().toDouble()
                if (accountDetail.Order?.id != null) {
                    createPaymentTemplate?.order_id = accountDetail.Order?.id
                } else {
                    createPaymentTemplate?.order_integration_id =
                        accountDetail.Order?.integration_id
                }

                createPaymentTemplate?.lov_payment_status = "Used"

                createPaymentTemplate?.integration_id =
                    if (binding.tvPaymentNum.text.isNullOrEmpty())
                        Calendar.getInstance().timeInMillis.toString()
                    else
                        binding.tvPaymentNum.text.toString()

                createPaymentTemplate?.account_id = accountDetail.Account?.id

                createPaymentTemplate?.isSelected = true

                var amount = etAmount.text.toString().toDouble()
                var allocaionList = arrayListOf<OrderAllocations>()
                savedOrderList?.forEach { order ->
                    if (amount > 0.0) {
                        val orderAmount =
                            if (amount > (String.format("%.2f", order.Order?.total_due ?: 0.0)
                                    .toDouble())
                            )
                                order.Order?.total_due
                            else
                                amount

                        allocaionList.add(
                            OrderAllocations(
                                amount = orderAmount ?: 0.0,
                                order_id = order.Order!!.id!!
                            )
                        )
                        amount -= (order.Order?.total_due ?: 0.0)

                        if (orderAmount == order.Order?.total_due) {
                            order.Order?.total_due = 0.0
                            order.Order?.lov_order_status = "05 - Closed"
                        } else {
                            order.Order?.total_due =
                                ((order.Order?.total_due ?: 0.0) - (orderAmount ?: 0.0))
                        }

                        if (order.Payments == null) {
                            order.Payments = arrayListOf()
                        }
                        order.Payments?.add(
                            OrderPaymentTemplate(
                                Payment = CreatePaymentTemplate(
                                    createPaymentTemplate?.id,
                                    createPaymentTemplate?.account_id,
                                    integration_id = createPaymentTemplate?.integration_id,
                                    amount = orderAmount,
                                    lov_payment_method = createPaymentTemplate?.payment_type,
                                    lov_payment_status = "Used"
                                )
                            )
                        )
                        if (order.PendingPayments == null) {
                            order.PendingPayments = arrayListOf()
                        }
                        order.PendingPayments?.add(
                            PendingPaymentTemplate(
                                Payment = CreatePaymentTemplate(
                                    createPaymentTemplate?.id,
                                    createPaymentTemplate?.account_id,
                                    integration_id = createPaymentTemplate?.integration_id,
                                    amount = orderAmount,
                                    payment_type = createPaymentTemplate?.payment_type,
                                    lov_payment_method = createPaymentTemplate?.payment_type,
                                    lov_payment_status = "Used"
                                )
                            )
                        )
                        saveOrderToDb(order)
                    }
                }
                adapter.notifyData(savedOrderList)


                if (isConnectedToInternet(requireContext(), false)) {

                    paymentViewModel.createPaymentApi(
                        requireContext(),
                        arrayListOf(createPaymentTemplate!!)
                    )

                } else {
                    updatePaymentAdapter()
                    savePendingPayments()
                }
            }
        }

        binding.tvEndOrder.setOnClickListener {
            setFragmentResult(
                AppRequestCode.CURRENT_TASK_ORDER_STATUS_CHANGED,
                bundleOf()
            )
            navigator.pop()
        }

    }

    private fun updatePaymentAdapter() {

        addedPaymentList.add(OrderPaymentTemplate(createPaymentTemplate!!))

        addedPaymentAdapter.addList(addedPaymentList)
        binding.viewFlipper.displayedChild = 0

        binding.tvDueAmount.text =
            String.format(
                getString(R.string.cart_price_text),
                currentPriceList?.Pricelist?.currency_symbol ?: "$",
                calculateDue()
            )
        binding.tvCurrentDueAmount.text =
            String.format(
                getString(R.string.cart_price_text),
                currentPriceList?.Pricelist?.currency_symbol ?: "$",
                calculateDue()
            )
        binding.etAmount.setText(calculateDue().toString())

//        val availcredit= availableCredit - calculateDue()
//        binding.tvAvailableCredit.text = availcredit.toString()
//        if (availableCredit > calculateDue()) {
//            binding.tvAddNew.visible()
//        } else if (calculateDue() > 0.0) {
//            binding.tvAddNew.visible()
//        } else {
//            binding.tvAddNew.gone()
//        }

        if (calculateDue() <= 0.0) {
            isPendingSelected = true
//            binding.tvPay.setTextColor(requireContext().resources.getColor(R.color.colorPrimary))
//            binding.tvOrders.setTextColor(requireContext().resources.getColor(R.color.grey_6))
//            binding.tvOrders.background = null
//            binding.tvOrders.gone()
            binding.tvNew.gone()
//            binding.llOption.background = null
//            binding.tvPay.setBackgroundResource(R.drawable.bg_light_bule_12dp)
//            binding.viewFlipper.displayedChild = 0

            binding.tvPay.setTextColor(requireContext().resources.getColor(R.color.colorPrimary))
            binding.tvOrders.setTextColor(requireContext().resources.getColor(R.color.grey_6))
            binding.tvOrders.background = null
            binding.tvPay.setBackgroundResource(R.drawable.bg_light_bule_12dp)
            binding.viewFlipper.displayedChild = 0
        }
    }

    private fun calculateDue(): Double {
        val totalAmount = totalAmount
        var collectedAmount = 0.0

        addedPaymentList.forEach { payment ->
            if (payment.Payment?.lov_payment_status != "Reverted") {
                collectedAmount += payment.Payment?.amount ?: 0.0
            }
        }

        val dueAmount = totalAmount - collectedAmount
        if (dueAmount < 0.05)
            return 0.0
        else
            return dueAmount
    }

    private fun saveOrderToDb(order: OrderListObject) {
        val saveOrderTemplate = SaveOrderTemplate()

//        if (dbOrderId!=null){
        saveOrderTemplate.id = order.id
//        }

        saveOrderTemplate.accountId = order?.Account?.id
        saveOrderTemplate.routeId = order?.routeId
        saveOrderTemplate.taskId = accountDetail?.Visit?.Activity?.id
        saveOrderTemplate.Order = order?.Order
        saveOrderTemplate.orderId = order?.Order?.id
        saveOrderTemplate.ProductAssortment = order?.ProductAssortment
        saveOrderTemplate.Account = order?.Account
        saveOrderTemplate.Paymentprofiles = order?.Paymentprofiles
        saveOrderTemplate.Promotions = order?.Promotions
        saveOrderTemplate.ProductAssortment = order.ProductAssortment
        saveOrderTemplate.CartProducts = order.CartProducts
        saveOrderTemplate.lov_order_type = "Sales"

//        if (!createPaymentList.isNullOrEmpty()){
//        var pendingPaymentList= arrayListOf<PendingPaymentTemplate>()
//        createPaymentList.forEach { payment ->
//            pendingPaymentList.add(
//                PendingPaymentTemplate(Payment = payment)
//            )
//        }
        saveOrderTemplate.PendingPayments = order.PendingPayments
        saveOrderTemplate.Payments = order.Payments
//        }else{
//            saveOrderTemplate.PendingPayments= products?.PendingPayments
//        }
//        if (status=="01 - Pending"){
////            saveOrderTemplate.CartProducts= orderProductList
//            saveOrderTemplate.ProductAssortment= orderProductList
//        }else {
//            saveOrderTemplate.CartProducts = confirmOrderProductList
//            saveOrderTemplate.ProductAssortment = orderProductList
//        }

//        if (status=="04 - Paid" || status=="03 - Ordered"){
//            saveOrderTemplate.Order?.total_due = calculateDue()
//        }

        lifecycleScope.launchWhenCreated {
            saveOrderDao.insert(saveOrderTemplate)
            setFragmentResult(TakeOrderFragment.ON_ORDER_STATUS_CHANGE, bundleOf())
        }

    }

    private fun initialize() {

        addedPaymentList= arrayListOf()
        createPaymentList= arrayListOf()
        savedOrderList= arrayListOf()

        val gson = prefsManager.getString(PrefsManager.TEAM_PRICELIST, "")
        if (!gson.isNullOrEmpty()) {

            val listType: Type = object : TypeToken<ArrayList<GetTeamPricelistResponse?>?>() {}.type
            val productPriceList =
                Gson().fromJson<ArrayList<GetTeamPricelistResponse>>(gson, listType)
            if (!productPriceList.isNullOrEmpty())
                currentPriceList = productPriceList[0]

        }

        adapter = PaymentOrderAdapter(
            requireContext(),
            ::OnClickOrder,
            currencySymbol = currentPriceList?.Pricelist?.currency_symbol ?: "$"
        )
        binding.rvOrders.adapter = adapter

        accountDetail = arguments?.getParcelable<GetRouteAccountResponse>(
            DataTransferKeys.KEY_PRODUCTS
        )!!

        savedOrderList = arrayListOf()
        accountDetail.Orders?.forEach {
            if ((((it.Order?.lov_order_status == "04 - Delivered") && (it.Order?.total_due
                    ?: 0.0) > 0.0) || (it.Order?.lov_order_status == "05 - Closed")) && (it.Order?.lov_order_type != "Return")
            ) {
                savedOrderList?.add(it)
            }
        }

        lifecycleScope.launchWhenCreated {

            val orderList = saveOrderDao.getCustomerOrders(
                accountDetail.routeId,
                accountDetail.Visit?.Activity?.id,
                accountDetail.Account?.id,
                "Sales"
            )

            if (orderList.isNotEmpty()) {
                orderList.forEach { order ->
                    val index =
                        savedOrderList?.indexOfFirst { it.Order?.id == order.Order?.id } ?: -1
                    if (index < 0) {
                        if (((order.Order?.lov_order_status == "04 - Delivered" && ((order.Order?.total_due
                                ?: 0.0) > 0.0)) || (order.Order?.lov_order_status == "05 - Closed")) && (order.Order?.lov_order_type != "Return")
                        ) {
                            savedOrderList?.add(
                                OrderListObject(
                                    Order = order.Order,
                                    CartProducts = order.CartProducts,
                                    ProductAssortment = order.ProductAssortment,
                                    PendingPayments = order.PendingPayments
                                        ?: accountDetail.PendingPayments,
                                    id = order.id,
                                    Paymentprofiles = order.Paymentprofiles,
                                    routeId = order.routeId,
                                    Promotions = order.Promotions,
                                    Account = order.Account
                                )
                            )
                        }

                    } else {
                        savedOrderList!![index] = OrderListObject(
                            Order = order.Order,
                            CartProducts = order.CartProducts,
                            ProductAssortment = order.ProductAssortment,
                            PendingPayments = order.PendingPayments
                                ?: accountDetail.PendingPayments,
                            id = order.id,
                            Paymentprofiles = order.Paymentprofiles,
                            routeId = order.routeId,
                            Promotions = order.Promotions,
                            Account = order.Account
                        )
                    }
                }
            }

            savedOrderList?.sortByDescending { it.Order?.confirmation_date }
            var dueAmount = 0.0
            var tlAmount = 0.0
            savedOrderList?.forEach {
                dueAmount += it.Order?.total_due ?: 0.0
                tlAmount += it.Order?.total ?: 0.0
            }
            adapter.notifyData(savedOrderList)
            binding.tvDueAmount.text =
                String.format(
                    getString(R.string.cart_price_text),
                    currentPriceList?.Pricelist?.currency_symbol ?: "$",
                    dueAmount
                )
            binding.tvCurrentDueAmount.text =
                String.format(
                    getString(R.string.cart_price_text),
                    currentPriceList?.Pricelist?.currency_symbol ?: "$",
                    dueAmount
                )
            binding.etAmount.setText(dueAmount.toString())
            totalAmount = tlAmount
            binding.tvOrderNum.text = savedOrderList?.size?.toString()
            initializePaymentScreen()
        }

        binding.tvCusName.text = accountDetail.Account?.accountname
        binding.tvCreditLimit.text = (accountDetail.Account?.credit_limit ?: 0.0).toString()
        binding.tvAvailableCredit.text =
            (accountDetail.Account?.calc_available_credit ?: 0.0).toString()

    }

    private fun OnClickOrder(order: OrderListObject) {
        navigator.push(TakeOrderFragment::class) {
            this.arguments = bundleOf(
                Pair(DataTransferKeys.KEY_ACCOUNT_DETAIL, accountDetail),
                Pair(DataTransferKeys.KEY_ORDER_ID, order),
                Pair(DataTransferKeys.KEY_IS_COMPLETED, order.Order?.lov_order_status == RouteStatus.STATUS_COMPLETED )
            )
        }
    }


    private fun initializePaymentScreen() {

        binding.rvPayments.visible()

        availableCredit = accountDetail.Account?.calc_available_credit ?: 0.0

        paymentProfilesAdapter = CollectPaymentProfileAdapter(
            requireContext(),
            ::OnItemClick,
            currencySymbol = currentPriceList?.Pricelist?.currency_symbol ?: "$"
        )
        addedPaymentAdapter = AddedPaymentsAdapter(
            requireContext(),
            currencySymbol = currentPriceList?.Pricelist?.currency_symbol ?: "$"
        )

        binding.rvPayments.adapter = paymentProfilesAdapter
        binding.rvClearedPayments.adapter = addedPaymentAdapter

        if (!accountDetail.PendingPayments.isNullOrEmpty()) {
            createPaymentList = arrayListOf()

            accountDetail.PendingPayments?.forEach {
                createPaymentList.add(it.Payment)
            }
            paymentProfilesAdapter.addList(createPaymentList, -1, true)
        }

        var availableAmount = 0.0

        createPaymentList.forEach {
            availableAmount += it.amount ?: 0.0
        }

        binding.tvCancel.visible()
        binding.tvSelect.visible()

        savedOrderList?.forEach {
            it.PendingPayments?.forEach { it1 ->
                if (it1.Payment.lov_payment_status != "Reverted")
                    addedPaymentList.add(
                        OrderPaymentTemplate(
                            Payment = it1.Payment
                        )
                    )
            }
        }

//        if (addedPaymentList.isNullOrEmpty()){
        addedPaymentAdapter.addList(addedPaymentList)
//        }else{
//
//        }
//        if (availableAmount > totalAmount) {
//            binding.tvNew.gone()
//        } else {
//            binding.tvNew.visible()
//        }

//        binding.tvDueAmount.text =
//            String.format(getString(R.string.cart_price_text), "$", calculateDue())
//        binding.tvCurrentDueAmount.text =
//            String.format(getString(R.string.cart_price_text), "$", calculateDue())
//        binding.etAmount.setText(calculateDue().toString())

        if (calculateDue() <= 0.0) {
            isPendingSelected = true
//            binding.tvPay.setTextColor(requireContext().resources.getColor(R.color.colorPrimary))
//            binding.tvOrders.setTextColor(requireContext().resources.getColor(R.color.grey_6))
//            binding.tvOrders.background = null
//            binding.tvOrders.gone()
            binding.tvNew.gone()
//            binding.llOption.background = null
//            binding.tvPay.setBackgroundResource(R.drawable.bg_light_bule_12dp)
//            binding.viewFlipper.displayedChild = 0

            binding.tvPay.setTextColor(requireContext().resources.getColor(R.color.colorPrimary))
            binding.tvOrders.setTextColor(requireContext().resources.getColor(R.color.grey_6))
            binding.tvOrders.background = null
            binding.tvPay.setBackgroundResource(R.drawable.bg_light_bule_12dp)
            binding.viewFlipper.displayedChild = 0
        }

        if (isCompleted == true) {
            binding.tvNew.gone()
            binding.tvEndOrder.gone()
        }
    }

    private fun OnItemClick(pos: Int) {
        selectedpos = pos
//        createPaymentList.mapIndexed { index, paymentProfileResponse ->
//            if (index == pos) {
//                paymentProfileResponse.isSelected = paymentProfileResponse.isSelected != true
//            } else {
//                paymentProfileResponse.isSelected = false
//            }
//        }
        paymentProfilesAdapter.addList(createPaymentList, pos, true)


//        if (isSelected){
//            binding.tvSelect.visible()
//        }else
//            binding.tvSelect.gone()

    }

}