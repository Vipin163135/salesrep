package com.salesrep.app.ui.returnOrder

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.github.fragivity.dialog.showDialog
import com.github.fragivity.navigator
import com.github.fragivity.pop
import com.github.fragivity.push
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.salesrep.app.R
import com.salesrep.app.base.BaseFragment
import com.salesrep.app.dao.CreateOrderDao
import com.salesrep.app.dao.InventoryDao
import com.salesrep.app.dao.RouteActivityDao
import com.salesrep.app.dao.SaveOrderDao
import com.salesrep.app.data.models.*
import com.salesrep.app.data.models.inventory.*
import com.salesrep.app.data.models.requests.CreateOrderData
import com.salesrep.app.data.models.requests.CreateOrderProduct
import com.salesrep.app.data.models.requests.CreateOrderTemplate
import com.salesrep.app.data.models.requests.RevertPaymentData
import com.salesrep.app.data.models.response.*
import com.salesrep.app.data.network.responseUtil.ApisRespHandler
import com.salesrep.app.data.network.responseUtil.Status
import com.salesrep.app.data.repos.UserRepository
import com.salesrep.app.databinding.FragmentTakeOrderNewBinding
import com.salesrep.app.ui.dialogs.ProgressDialog
import com.salesrep.app.ui.home.HomeViewModel
import com.salesrep.app.ui.payment.CancelPaymentDialog
import com.salesrep.app.ui.payment.PaymentFragment
import com.salesrep.app.ui.payment.PaymentViewModel
import com.salesrep.app.ui.payment.adapters.PaymentProfilesAdapter
import com.salesrep.app.ui.route.CancelRouteDialog
import com.salesrep.app.ui.stock.ChooseProductFragment
import com.salesrep.app.ui.takeOrder.PrintOrderFragment
import com.salesrep.app.ui.takeOrder.TakeOrderAdapter
import com.salesrep.app.ui.takeOrder.TakeOrderFragment.Companion.ON_ORDER_STATUS_CHANGE
import com.salesrep.app.ui.takeOrder.TakeOrderProductsAdapter
import com.salesrep.app.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_payment_options.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import java.lang.reflect.Type
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@AndroidEntryPoint
class ReturnOrderFragment : BaseFragment<FragmentTakeOrderNewBinding>() {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var createOrderDao: CreateOrderDao

    @Inject
    lateinit var saveOrderDao: SaveOrderDao

    @Inject
    lateinit var routeActivityDao: RouteActivityDao

    private lateinit var binding: FragmentTakeOrderNewBinding
    override val viewModel by viewModels<HomeViewModel>()
    val activityViewModel by activityViewModels<HomeViewModel>()
    private lateinit var progressDialog: ProgressDialog
    private lateinit var productList: List<ProductAssortment>
    private lateinit var orderProductList: ArrayList<ProductAssortment>
    private var confirmOrderProductList = arrayListOf<ProductAssortment>()

    private var totalAmount: Double = 0.0
    private var totalDueAmount: Double = 0.0
    private var totalTax: Double = 0.0
    private var totalDiscount: Double = 0.0
    private var subTotalAmount: Double = 0.0

    private lateinit var productAdapter: TakeOrderAdapter
    private var currentPriceList: GetTeamPricelistResponse? = null

    private lateinit var repriceProductAdapter: TakeOrderProductsAdapter

    private var status: String = "01 - Pending"
    private var integrationId: String? = null
    private var reason: String? = null
    val paymentViewModel by viewModels<PaymentViewModel>()
    private var availableCredit: Double = 0.0
    private var isOpened = false

    private lateinit var products: GetRouteAccountResponse
    private var dbOrderId: Long? = null

    private var inventory: GetTeamInventoryResponse? = null
    private val isCompleted by lazy {
        arguments?.getBoolean(
            DataTransferKeys.KEY_IS_COMPLETED, false
        )
    }

    @Inject
    lateinit var inventoryDao: InventoryDao

    private val isNew by lazy {
        arguments?.getBoolean(
            DataTransferKeys.KEY_IS_NEW, false
        ) ?: false
    }

    private val order by lazy {
        arguments?.getParcelable<OrderListObject>(
            DataTransferKeys.KEY_ORDER_ID
        )
    }

    companion object {
        const val ON_RETURN_ORDER_STATUS_CHANGE = "ON_RETURN_ORDER_STATUS_CHANGE"
    }

    private var savedOrderList: List<SaveOrderTemplate>? = null
    private lateinit var createPaymentTemplate: CreatePaymentTemplate

    override fun getLayoutResId(): Int {
        return R.layout.fragment_take_order_new
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFragmentResultListener(AppRequestCode.SELECT_PRODUCT_REQUEST) { key, bundle ->
            if (bundle.get(DataTransferKeys.KEY_PRODUCTS) != null) {
                val product = bundle.getParcelable<ProductTemplate>(DataTransferKeys.KEY_PRODUCTS)

                val priceListObj =
                    currentPriceList?.Products?.find { it.Product?.integration_num == product?.integration_num }

                if (priceListObj != null) {
                    val assortObj = ProductAssortment(priceListObj?.PricelistProduct, product)

                    if (DateUtils.isValidDiscount(
                            priceListObj.PricelistProduct?.disc_startdt,
                            priceListObj.PricelistProduct?.disc_enddt
                        )
                    ) {
                        assortObj.AccountProduct?.apply_direct_disc =
                            priceListObj.PricelistProduct?.disc_percent

                    }
                    assortObj.AccountProduct?.tax_total= priceListObj.PricelistProduct?.tax_total
                    assortObj.AccountProduct?.net_price= priceListObj.PricelistProduct?.net_price
                    orderProductList.add(assortObj)
//                    orderProductList.add(ProductAssortment(null, product))
//                orderProductList= list.toList()
                    productAdapter.notifyData(orderProductList)
//                    productAdapter.notifyData(orderProductList)
                    binding.rvCustomers.visible()
                }
            }
        }

        setFragmentResultListener(AppRequestCode.ORDER_RETURN_REASON) { key, bundle ->
            if (bundle.get(DataTransferKeys.KEY_REASON) != null) {
                reason = bundle.getString(DataTransferKeys.KEY_REASON)
                status = "02 - Confirmed"
                isOpened = false
                binding.tvPrint.visible()
                binding.tvSaveOrder.gone()
                createOrderAPi()
                binding.tvCusomerName.text = status
                binding.tvViewPayments.gone()
            }
        }

    }

    override fun onCreateView(
        instance: Bundle?,
        binding: FragmentTakeOrderNewBinding
    ) {
        this.binding = binding
        progressDialog = ProgressDialog(requireActivity())
        binding.tvNoData.gone()
        totalAmount = 0.0
        totalDueAmount = 0.0
        totalTax = 0.0
        totalDiscount = 0.0
        subTotalAmount = 0.0

        binding.viewFlipper.displayedChild = 0
        binding.llDueAmount.gone()
        binding.tvConfimDate.text = getString(R.string.return_date)
        binding.tvPrepare.gone()
        binding.tvBack.text = getString(R.string.return_order)
        if (isNew) {
            initialize()
        } else {
            lifecycleScope.launchWhenCreated {
                products = arguments?.getParcelable(
                    DataTransferKeys.KEY_ACCOUNT_DETAIL
                ) ?: GetRouteAccountResponse()
                initialize()
                if (order == null) {
                    savedOrderList = saveOrderDao.getReturnOrders(
                        products.routeId,
                        products.Visit?.Activity?.id,
                        products.Account?.id,
                        "Return"
                    )
                } else {
                    val savedOrder = SaveOrderTemplate(
                        Order = order?.Order,
                        Paymentprofiles = products.Paymentprofiles,
                        PendingPayments = order?.PendingPayments,
                        Promotions = products.Promotions,
                        CartProducts = order?.CartProducts,
                        ProductAssortment = order?.CartProducts ?: order?.ProductAssortment,
                        routeId = products?.routeId,
                        accountId = products?.Account?.id,
                        taskId = products.Visit?.Activity?.id,
                        id = order?.id
                    )
                    savedOrderList = listOf(savedOrder)
                }

                if (!savedOrderList.isNullOrEmpty()) {
                    setLatestOrderData(savedOrderList!!.last())
                }

                if (isCompleted==true){
                    binding.llOrderOption.gone()
                    binding.tvPrepare.gone()
                    binding.tvSaveOrder.gone()
                }
            }
        }
        listeners()
        bindObservers()
    }

    private fun setLatestOrderData(saveOrderTemplate: SaveOrderTemplate) {

        val formCatalog = prefsManager.getObject(
            PrefsManager.APP_FORM_CATALOG,
            GetFormCatalogResponse::class.java
        )
//        val reasons= Gson().fromJson(gson,GetFormCatalogResponse::class.java)
        val productStatus = formCatalog?.ProductStatuses


        products.Order = saveOrderTemplate.Order
//        products.ProductAssortment = saveOrderTemplate.ProductAssortment
//        products.Account = saveOrderTemplate.Account
        products.CartProducts = saveOrderTemplate.CartProducts
        products.Promotions = saveOrderTemplate.Promotions
        products.Paymentprofiles = saveOrderTemplate.Paymentprofiles
//        products.PendingPayments = saveOrderTemplate.PendingPayments

        dbOrderId = saveOrderTemplate.id

        initializeLabels()
        binding.tvCusomerName.text = products?.Order?.lov_order_status ?: "01 - Pending"

        status = products.Order?.lov_order_status ?: "01 - Pending"


        /*products.ProductAssortment?.forEach { assortProduct ->
            val cartProduct =
                products.CartProducts?.firstOrNull { it.Product?.id == assortProduct.Product?.id }
            if (cartProduct != null) {
                assortProduct.AccountProduct?.agreed_qty =
                    cartProduct.OrdersProduct?.product_qty.toString()
                assortProduct.AccountProduct?.apply_direct_disc =
                    cartProduct.OrdersProduct?.disc_percent
                assortProduct.AccountProduct?.total = cartProduct.OrdersProduct?.total?.toString()
                assortProduct.AccountProduct?.sub_total =
                    cartProduct.OrdersProduct?.subtotal?.toString()
                assortProduct.AccountProduct?.tax = cartProduct.PricelistProduct?.tax ?: 0.0
                assortProduct.AccountProduct?.net_price =
                    cartProduct.PricelistProduct?.net_price ?: 0.0
                assortProduct.Product?.min_qty = cartProduct.Product?.min_qty
                assortProduct.Product?.max_qty = cartProduct.Product?.max_qty
                assortProduct.Product?.order_qty = cartProduct.Product?.order_qty
                assortProduct.Product?.step_qty = cartProduct.Product?.step_qty ?: 1.0

                orderProductList.add(assortProduct)

            }
        }
//       orderProductList?.let {
//            orderProductList = arrayListOf()
        productAdapter = TakeOrderAdapter(
            requireContext(),
            ::onTotalChanged,
            ::onDeleteProduct,
            true,
            productStatus
        )
        binding.rvCustomers.adapter = productAdapter
        binding.rvCustomers.visible()
        productAdapter.notifyData(orderProductList)
        onTotalChanged(true)
//        }*/

        orderProductList = arrayListOf()
        products.CartProducts?.forEach { cartProduct ->

            var assortProduct =
                products.ProductAssortment?.firstOrNull { it.Product?.id == cartProduct.Product?.id }

            if (assortProduct != null) {
                assortProduct.AccountProduct?.agreed_qty =
                    cartProduct.OrdersProduct?.product_qty.toString()
                assortProduct.AccountProduct?.apply_direct_disc =
                    cartProduct.OrdersProduct?.disc_percent
                assortProduct.AccountProduct?.total =
                    cartProduct.OrdersProduct?.total?.toString()
                assortProduct.AccountProduct?.sub_total =
                    cartProduct.OrdersProduct?.subtotal?.toString()
                assortProduct.AccountProduct?.tax_total = cartProduct.PricelistProduct?.tax_total ?: 0.0
                assortProduct.AccountProduct?.net_price =
                    cartProduct.PricelistProduct?.net_price ?: 0.0
                assortProduct.Product?.min_qty = cartProduct.Product?.min_qty
                assortProduct.Product?.max_qty = cartProduct.Product?.max_qty
                assortProduct.Product?.order_qty = cartProduct.Product?.order_qty
                assortProduct.Product?.step_qty = cartProduct.Product?.step_qty ?: 1.0


                inventory?.Van?.Bins?.forEach { bin ->
                    bin.Inventory?.forEach { invBinproduct ->
                        if (invBinproduct.Product.id == cartProduct?.Product?.id) {
                            assortProduct?.InvbinsProduct = invBinproduct.InvbinsProduct
                        }
                    }
                }
                orderProductList.add(assortProduct)

            } else {
                assortProduct = ProductAssortment(AccountProductTemplate(), cartProduct.Product)
                assortProduct.AccountProduct?.agreed_qty =
                    cartProduct.OrdersProduct?.product_qty.toString()
                assortProduct.AccountProduct?.apply_direct_disc =
                    cartProduct.OrdersProduct?.disc_percent
                assortProduct.AccountProduct?.total =
                    cartProduct.OrdersProduct?.total?.toString()
                assortProduct.AccountProduct?.sub_total =
                    cartProduct.OrdersProduct?.subtotal?.toString()
                assortProduct.AccountProduct?.tax_total = cartProduct.PricelistProduct?.tax_total ?: 0.0
                assortProduct.AccountProduct?.net_price =
                    cartProduct.PricelistProduct?.net_price ?: 0.0
//                    assortProduct.Product?.min_qty = cartProduct.Product?.min_qty
//                    assortProduct.Product?.max_qty = cartProduct.Product?.max_qty
//                    assortProduct.Product?.order_qty = cartProduct.Product?.order_qty
//                    assortProduct.Product?.step_qty = cartProduct.Product?.step_qty ?: 1.0


                inventory?.Van?.Bins?.forEach { bin ->
                    bin.Inventory?.forEach { invBinproduct ->
                        if (invBinproduct.Product.id == cartProduct.Product?.id) {
                            assortProduct.InvbinsProduct = invBinproduct.InvbinsProduct
                        }
                    }
                }
                orderProductList.add(assortProduct)

            }

        }

        if (!orderProductList.isNullOrEmpty()) {
            productAdapter =
                TakeOrderAdapter(requireContext(), ::onTotalChanged, ::onDeleteProduct, currency_symbol = currentPriceList?.Pricelist?.currency_symbol?:"$")
            binding.rvCustomers.adapter = productAdapter
            binding.rvCustomers.visible()

            productAdapter.notifyIsInitialize(true)

            productAdapter.notifyData(orderProductList)
            onTotalChanged(true)
//                productAdapter.notifyIsInitialize(false)
        } else {
            products.ProductAssortment?.let {
                orderProductList = arrayListOf()
                orderProductList.addAll(it)
                productAdapter =
                    TakeOrderAdapter(requireContext(), ::onTotalChanged, ::onDeleteProduct, currency_symbol = currentPriceList?.Pricelist?.currency_symbol?:"$")
                binding.rvCustomers.adapter = productAdapter
                binding.rvCustomers.visible()
                productAdapter.notifyIsInitialize(false)
                productAdapter.notifyData(orderProductList)
                onTotalChanged(true)
            }
        }

        totalAmount = products.Order?.total ?: 0.0
        totalDueAmount = products.Order?.total ?: 0.0

        when (products.Order?.lov_order_status) {
            "05 - Closed" -> {
                status = "05 - Closed"
                binding.tvSaveOrder.gone()
                binding.tvPrint.visible()
                setOrderData()
                onTotalChanged(true)
                binding.viewFlipper.displayedChild = 1
                binding.llOrderOption.gone()
                binding.tvConfirm.text = getString(R.string.close)
                binding.tvAddProduct.alpha = 0.4f
                binding.tvEnd.alpha = 0.4f
                binding.tvAddProduct.isClickable = false
                binding.tvEnd.isClickable = false
                binding.tvViewPayments.visible()
            }
            "06 - Cancelled" -> {
                status = "06 - Cancelled"
                setOrderData()
                binding.tvPrint.visible()
                binding.tvSaveOrder.gone()
                binding.viewFlipper.displayedChild = 1
                binding.llOrderOption.gone()
                binding.tvConfirm.isClickable = false
                binding.tvAddProduct.isClickable = false
                binding.tvEnd.isClickable = false
                binding.llViewPayment.gone()
                binding.tvPrepare.gone()
            }
            "02 - Confirmed" -> {

                binding.tvSaveOrder.gone()
                binding.tvPrint.visible()
                when (userRepository.getTeam()?.Team?.lov_team_type) {
                    "Delivery",
                    "Presales",
                    "Trade" -> {
                        binding.llOrderOption.gone()
                        setOrderData()
                        binding.viewFlipper.displayedChild = 1
                        binding.llViewPayment.gone()
                        binding.tvPrepare.gone()
                    }
                    else -> {
                        setOrderData()
                        binding.viewFlipper.displayedChild = 1
                        binding.tvViewPayments.gone()
                        binding.tvPrepare.gone()
                    }
                }
            }
            "04 - Delivered" -> {
                binding.tvSaveOrder.gone()
                binding.tvPrint.visible()
                setOrderData()
                binding.tvPrepare.gone()
                binding.tvViewPayments.gone()
                binding.viewFlipper.displayedChild = 1
                binding.llOrderOption.gone()

            }
            else -> {


                when (userRepository.getTeam()?.Team?.lov_team_type) {
                    "Delivery",
                    "Trade" -> {
                        binding.tvPrint.gone()
                        binding.tvSaveOrder.gone()
                        binding.llOrderOption.gone()
                        setOrderData()
                        binding.viewFlipper.displayedChild = 1
                        binding.llViewPayment.gone()
                        binding.tvPrepare.gone()
                    }
                    else -> {
                        binding.tvPrint.gone()
                        binding.tvSaveOrder.visible()
                        binding.tvCusomerName.text = "01 - Pending"
                        status = "01 - Pending"
                        binding.tvConfirm.visible()
                        binding.rvCustomers.visible()
                        binding.viewFlipper.displayedChild = 0
                        binding.tvEnd.visible()
                        binding.tvConfirm.text = getString(R.string.confirm)
                        binding.llViewPayment.gone()
//                        binding.tvPrepare.visible()
                    }
                }

            }
        }


    }

    /// ghghghgh
    private fun listeners() {
        binding.tvBack.setOnClickListener {
            navigator.pop()
        }

        binding.tvPrint.setOnClickListener {
            navigator.push(PrintOrderFragment::class) {
                arguments = bundleOf(Pair(DataTransferKeys.KEY_ACCOUNT_DETAIL, products))
            }
        }

        binding.tvConfirm.setOnClickListener {

//            if (!orderProductList.isNullOrEmpty() && binding.tvConfirm.text == getString(R.string.prepare)) {
//
//                when (userRepository.getTeam()?.Team?.lov_team_type) {
//                    "Trade" -> {
//                    }
//                    else -> {
//                        productAdapter.notifyData(orderProductList)
//                        binding.tvConfirm.visible()
//                        binding.rvCustomers.visible()
//                        binding.tvEnd.visible()
//                        binding.tvConfirm.text = getString(R.string.confirm)
//                        binding.tvViewPayments.gone()
//                    }
//                }
//
//            } else
            if (binding.tvConfirm.text.toString() == getString(R.string.confirm)) {

                if (orderProductList.isNotEmpty()) {
                    when (userRepository.getTeam()?.Team?.lov_team_type) {
                        "Delivery",
                        "Trade" -> {
                        }
                        else -> {
                            if (checkForMinMax())
                                navigator.showDialog(
                                    ReturnReasonDialog::class,
                                    bundleOf()
                                )
                        }
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.please_add_products),
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else if (binding.tvConfirm.text.toString() == getString(R.string.deliver)) {
                when (userRepository.getTeam()?.Team?.lov_team_type) {
                    "Presales",
                    "Trade" -> {
                    }
                    else -> {
                        addToVan()
                        binding.tvPrint.visible()
                        binding.tvSaveOrder.gone()
                        status = "04 - Delivered"
                        updateOrderAPi("04 - Delivered")
                        binding.tvCusomerName.text = status
                        saveOrderToDb(status)
                        binding.tvViewPayments.gone()
                        binding.llOrderOption.gone()
                    }
                }

            } else {

            }
        }

        binding.tvAddProduct.setOnClickListener {
            when (status) {
                "01 - Pending" -> {
                    navigator.push(ChooseProductFragment::class)
                }
                "02 - Confirmed" -> {
                    binding.viewFlipper.displayedChild = 0
                    status = "01 - Pending"
                    binding.tvSaveOrder.visible()
                    binding.tvConfirm.text = getString(R.string.confirm)
                    binding.tvAddProduct.text = getString(R.string.add)
                    isOpened = true
                }
                else -> {

                }
            }
        }


        binding.tvEnd.setOnClickListener {
            when (status) {
                "01 - Pending" -> {
                    runBlocking {
//                        deleteOrder()
                        setFragmentResult(ON_ORDER_STATUS_CHANGE, bundleOf())
                        navigator.popBackStack()
                    }
                }
                "02 - Confirmed" -> {
                    binding.tvPrint.visible()
                    updateOrderAPi("06 - Cancelled")
                    saveOrderToDb("06 - Cancelled")
                }
                else -> {
                }
            }
        }

        binding.tvSaveOrder.setOnClickListener {
            if (!orderProductList.isNullOrEmpty() && (binding.tvConfirm.text.toString() == getString(
                    R.string.confirm
                ))
            ) {
                when (userRepository.getTeam()?.Team?.lov_team_type) {
                    "Delivery",
                    "Trade" -> {
                    }
                    else -> {
                        if (orderProductList.isNullOrEmpty()) {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.please_add_products),
                                Toast.LENGTH_LONG
                            ).show()
                        } else if (checkForMinMax()) {
                            status = "01 - Pending"
                            createOrderAPi()
                            binding.tvCusomerName.text = status
                            binding.llViewPayment.gone()
                            isOpened = false
                        }
                    }
                }

            } else if (orderProductList.isNullOrEmpty()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.please_add_products),
                    Toast.LENGTH_LONG
                ).show()
            }
        }


    }


    private fun checkForMinMax(): Boolean {
        orderProductList.forEach { productAssortment ->

            if ((productAssortment.AccountProduct?.agreed_qty
                    ?: "1.0").toDouble() > (productAssortment.Product?.max_qty ?: 10000000.0)
            ) {

                Toast.makeText(
                    requireContext(),
                    getString(R.string.product_max_allowed_quality,
                        productAssortment.Product?.title,
                        (productAssortment.Product?.max_qty ?: 10000000.0).toString()
                    ),
                    Toast.LENGTH_LONG
                ).show()
                return false
            } else if ((productAssortment.AccountProduct?.agreed_qty
                    ?: "1.0").toDouble() < (productAssortment.Product?.min_qty ?: 1.0)
            ) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.product_min_allowed_quality,
                        productAssortment.Product?.title,
                        (productAssortment.Product?.min_qty ?: 1.0).toString()
                    ),
                    Toast.LENGTH_LONG
                ).show()
                return false
            }
        }

        return true
    }


    private fun addToVan() {

//        val vanProducts = inventory?.Van?.Bins?.get(0)?.Inventory
        orderProductList.forEach { productAssortment ->
            var isPresent = false
            var binIndex: Int? = -1

            if (productAssortment.Manufacturer?.alias.isNullOrEmpty()) {
                binIndex = inventory?.Van?.Bins?.indexOfFirst { it.Invbin?.title == "DEFAULT" }
            } else
                binIndex =
                    inventory?.Van?.Bins?.indexOfFirst { it.Invbin?.id == productAssortment.Manufacturer?.id }

            if (binIndex != null && binIndex > -1) {
                val vanProducts = inventory?.Van?.Bins!![binIndex].Inventory

                vanProducts?.forEach { inventoryDataObject ->

                    if ((productAssortment.Product?.id == inventoryDataObject.Product.id)
                        && (productAssortment.Product?.lov_invbinproduct_status == inventoryDataObject.InvbinsProduct?.lov_invbinproduct_status)
                    ) {
                        var qty = (inventoryDataObject.InvbinsProduct.qty ?: "0.0").toDouble()
                        qty += if (isNew) {
                            (productAssortment.AccountProduct?.agreed_qty ?: "0.0").toDouble()
                        } else {
                            (productAssortment.Product?.order_qty ?: "0.0").toDouble()
                        }
                        inventoryDataObject.InvbinsProduct.qty = qty.toString()
                        isPresent = true
                    }
                }


                var addProduct = arrayListOf<InventoryDataObject>()
                if (!isPresent) {
                    addProduct.add(
                        InventoryDataObject(
                            Product = productAssortment?.Product!!,
                            InvbinsProduct = InvBinProductsData(
                                qty = productAssortment.Product?.order_qty,
                                lov_invbinproduct_status = productAssortment.Product?.lov_invbinproduct_status
                            )
                        )
                    )
                }

                if (addProduct.isNotEmpty()) {
                    vanProducts?.addAll(addProduct)
                }

                inventory?.Van?.Bins?.get(binIndex)?.Inventory = vanProducts

            } else if (!productAssortment.Manufacturer?.alias.isNullOrEmpty()) {

                var addProduct = arrayListOf<InventoryDataObject>()
                if (!isPresent) {
                    addProduct.add(
                        InventoryDataObject(
                            Product = productAssortment.Product!!,
                            InvbinsProduct = InvBinProductsData(
                                qty = productAssortment.Product?.order_qty,
                                lov_invbinproduct_status = productAssortment.Product?.lov_invbinproduct_status
                            )
                        )
                    )
                }

                if (addProduct.isNotEmpty()) {
                    val bin = InvBinTemplate(
                        InvBinData(
                            id = productAssortment.Manufacturer?.id,
                            title = productAssortment.Manufacturer?.alias,
                            name = productAssortment.Manufacturer?.accountname,
                            lov_bin_type = "New"
                        ),
                        addProduct
                    )
                    inventory?.Van?.Bins?.add(bin)
                }
            }
        }


        if (inventory != null) {
            lifecycleScope.launchWhenCreated {
                inventoryDao.clearAll()
                inventoryDao.insert(inventory)
            }
        }

    }

    suspend fun deleteOrder() = coroutineScope {
        launch {
            saveOrderDao.deleteOrders(
                products?.routeId,
                products?.Visit?.Activity?.id,
                products?.Account?.id,
                "Return"
            )
        }
    }

    private fun initialize() {
        orderProductList = arrayListOf()

        lifecycleScope.launchWhenCreated {
            inventory = inventoryDao.getAllInventory().lastOrNull()
        }

        products = arguments?.getParcelable<GetRouteAccountResponse>(
            DataTransferKeys.KEY_ACCOUNT_DETAIL
        ) ?: GetRouteAccountResponse()
        val formCatalog = prefsManager.getObject(
            PrefsManager.APP_FORM_CATALOG,
            GetFormCatalogResponse::class.java
        )
//        val reasons= Gson().fromJson(gson,GetFormCatalogResponse::class.java)
        val productStatus = formCatalog?.ProductStatuses

        val gson = prefsManager.getString(PrefsManager.TEAM_PRICELIST, "")
        if (!gson.isNullOrEmpty()) {

            val listType: Type = object : TypeToken<ArrayList<GetTeamPricelistResponse?>?>() {}.type
            val productPriceList =
                Gson().fromJson<ArrayList<GetTeamPricelistResponse>>(gson, listType)
            currentPriceList =
                productPriceList.find { products?.Pricelist?.id == it.Pricelist?.id }

        }

        orderProductList = arrayListOf()
        productAdapter = TakeOrderAdapter(
            requireContext(),
            ::onTotalChanged,
            ::onDeleteProduct,
            true,
            productStatus,
            currency_symbol = currentPriceList?.Pricelist?.currency_symbol?:"$"
        )
        binding.rvCustomers.adapter = productAdapter
        binding.rvCustomers.visible()

        binding.tvCusomerName.text = "01 - Pending"
        binding.tvConfirm.visible()
        binding.tvConfirm.text = getString(R.string.confirm)
        binding.llOrderOption.visible()
        binding.tvViewPayments.gone()
//        binding.rvCustomers.gone()
        binding.tvSaveOrder.visible()
        binding.tvEnd.alpha = 1.0f
        binding.tvAddProduct.alpha = 1.0f
        binding.tvAddProduct.text = getString(R.string.add_product)
        status = "01 - Pending"
        savedOrderList = null
        binding.viewFlipper.displayedChild = 0

        initializeLabels()


        if (isCompleted==true){
            binding.llOrderOption.gone()
            binding.tvPrepare.gone()
            binding.tvSaveOrder.gone()
        }
    }

    private fun initializeLabels() {

        binding.tvOrderNum.text = "${userRepository.getTeam()?.Team?.id}:${
            DateUtils.getCurrentDateWithFormat(DateFormat.DATE_FORMAT_YMDHMS)
        }"

        binding.tvCusName.text = products?.Account?.name.toString()
        binding.tvDeliveryDate.text = "N/A"
        binding.tvConfDate.text = "N/A"
        binding.tvCreditLimit.text = (products?.Account?.credit_limit ?: 0.0).toString()
        binding.tvAvailableCredit.text =
            (products?.Account?.calc_available_credit ?: 0.0).toString()
    }

    fun onTotalChanged(isInitialize: Boolean? = false) {
        if (status == "01 - Pending" || binding.tvAddProduct.text == getString(R.string.add)) {
            totalAmount = 0.0
            totalDueAmount = 0.0

            subTotalAmount = 0.0
            totalTax = 0.0
            totalDiscount = 0.0
            binding.tvSaveOrder.visible()

            orderProductList.forEach { product ->
                if ((product.AccountProduct?.sub_total ?: "0.0").toDouble() > 0.0) {
                    totalAmount += (product.AccountProduct?.total ?: "0.0").toDouble()
                    subTotalAmount += (product.AccountProduct?.sub_total ?: "0.0").toDouble()
                    totalTax += (product.AccountProduct?.tax_total ?: 0.0)

                    val netPrice = product.AccountProduct?.net_price ?: 0.0
                    val discPercent = product.AccountProduct?.apply_direct_disc ?: 0.0
                    val disAmnt = ((netPrice-(product.AccountProduct?.tax_total?:0.0) )* discPercent) / 100

                    totalDiscount += (disAmnt * (product.AccountProduct?.agreed_qty
                        ?: "1.0").toDouble())
//                    ((product.AccountProduct?.apply_promo_disc
//                    ?: 0.0) + (product.AccountProduct?.apply_direct_disc ?: 0.0))
                }
            }

            binding.tvTotal.text =
                String.format(getString(R.string.cart_price_text), currentPriceList?.Pricelist?.currency_symbol?:"$", totalAmount)
            totalDueAmount= totalAmount
            binding.tvDueAmount.text =
                String.format(getString(R.string.cart_price_text), currentPriceList?.Pricelist?.currency_symbol?:"$", totalAmount)
            binding.tvSubTotal.text =
                String.format(getString(R.string.cart_price_text), currentPriceList?.Pricelist?.currency_symbol?:"$", subTotalAmount)
            binding.tvTax.text =
                String.format(getString(R.string.cart_price_text), currentPriceList?.Pricelist?.currency_symbol?:"$", totalTax)
            binding.tvDiscount.text =
                String.format(getString(R.string.cart_price_text), currentPriceList?.Pricelist?.currency_symbol?:"$", totalDiscount)

//        if (isInitialize==false){
//            saveOrderToDb("01 - Pending")
//        }
        }
    }

    fun onDeleteProduct(position: Int) {
        showDeleteDialog(position)
    }

    private fun showDeleteDialog(position: Int) {
        val alertDialog = AlertDialogUtil.instance.createOkCancelDialog(
            requireContext(),
            R.string.remove_product,
            R.string.remove_product_text,
            R.string.yes, R.string.cancel,
            true,
            object : AlertDialogUtil.OnOkCancelDialogListener {
                override fun onOkButtonClicked() {
                    val cnfmOrderIndex =
                        confirmOrderProductList.indexOfFirst { orderProductList[position].Product?.id == it.Product?.id }

                    if (cnfmOrderIndex >= 0) {
                        confirmOrderProductList.removeAt(cnfmOrderIndex)
                    }

                    orderProductList.removeAt(position)
                    productAdapter.notifyItemRemoved(position)
                    onTotalChanged(false)
                }

                override fun onCancelButtonClicked() {
                }
            })
        alertDialog.show()
    }

    @SuppressLint("FragmentLiveDataObserve")
    private fun bindObservers() {
        viewModel.createOrderApiResponse.setObserver(
            viewLifecycleOwner,
            androidx.lifecycle.Observer {
                it ?: return@Observer
                when (it.status) {
                    Status.LOADING -> progressDialog.setLoading(true)
                    Status.SUCCESS -> {
                        progressDialog.setLoading(false)
                        if (it.data != null) {
                            products.Order?.lov_order_status = status

                            when (status) {
                                "01 - Pending" -> {
                                    binding.tvSaveOrder.visible()
                                    binding.viewFlipper.displayedChild = 0
                                    if (isConnectedToInternet(requireContext(), false)) {
                                        viewModel.repriceOrderApi(
                                            requireContext(),
                                            integrationId
                                        )
                                    }
                                }
                                "02 - Confirmed" -> {
                                    binding.tvSaveOrder.gone()
                                    viewModel.repriceOrderApi(
                                        requireContext(),
                                        integrationId
                                    )
                                }
                                "04 - Delivered" -> {
                                    binding.tvSaveOrder.gone()
                                    binding.tvViewPayments.gone()
                                    binding.llOrderOption.gone()
                                    binding.viewFlipper.displayedChild = 1

                                    updateCreditLimit()

                                }
                                "06 - Cancelled" -> {
//                                    status = "06 - Cancelled"
                                    binding.viewFlipper.displayedChild = 1
                                    binding.llOrderOption.gone()
                                    binding.tvSaveOrder.gone()
                                }

                                else -> {
                                    binding.tvSaveOrder.gone()
                                    binding.llOrderOption.gone()
                                    binding.viewFlipper.displayedChild = 1
                                }
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

        viewModel.repriceOrderApiResponse.setObserver(
            viewLifecycleOwner,
            androidx.lifecycle.Observer {
                it ?: return@Observer
                when (it.status) {
                    Status.LOADING -> progressDialog.setLoading(true)
                    Status.SUCCESS -> {
                        progressDialog.setLoading(false)
                        if (it.data != null) {
                            when (status) {
                                "01 - Pending" -> {
                                    binding.tvSaveOrder.visible()
                                    order?.let { orderListObject ->
                                        orderListObject.Order = it.data.Order
                                    }
                                    products.Order = it.data.Order
                                    products.CartProducts = it.data.CartProducts
                                    products.Promotions = it.data.Promotions
                                    setOrderData()
                                    binding.viewFlipper.displayedChild = 0
                                    saveOrderToDb("01 - Pending")
                                }
                                "02 - Confirmed" -> {
                                    order?.let { orderListObject ->
                                        orderListObject.Order = it.data.Order
                                    }
                                    products.Order = it.data.Order
                                    products.CartProducts = it.data.CartProducts
                                    products.Promotions = it.data.Promotions
                                    setOrderData()
                                    binding.viewFlipper.displayedChild = 1
                                    saveOrderToDb("02 - Confirmed")

                                    when (userRepository.getTeam()?.Team?.lov_team_type) {
                                        "Delivery",
                                        "Presales",
                                        "Trade" -> {
                                            binding.llOrderOption.gone()
                                        }
                                        else -> {
                                        }
                                    }
                                }

//                                "02 - Confirmed" -> {
//                                    products?.Order = it.data.Order
//                                    products?.CartProducts = it.data.CartProducts
//                                    products?.Promotions = it.data.Promotions
//                                    setOrderData()
//                                    binding.viewFlipper.displayedChild = 1
//                                    saveOrderToDb("02 - Confirmed")
//                                }


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

        paymentViewModel.createPaymentResponse.observe(this, androidx.lifecycle.Observer {
            it ?: return@Observer
            when (it.status) {
                Status.LOADING -> progressDialog.setLoading(true)
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)
                    addPendingPaymentDb()
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

    private fun addPendingPaymentDb() {
    }

    private fun updateCreditLimit() {
        createPaymentTemplate = CreatePaymentTemplate()

        createPaymentTemplate.payment_type = "Cash"

        createPaymentTemplate.amount = totalDueAmount
        if (products.Order?.id != null) {
            createPaymentTemplate.order_id = products?.Order?.id
        } else {
            createPaymentTemplate.order_integration_id = products.Order?.integration_id
        }

        createPaymentTemplate.integration_id =
            Calendar.getInstance().timeInMillis.toString()

        createPaymentTemplate.account_id = products.Account?.id

        createPaymentTemplate.isSelected = true
        createPendingPayment(createPaymentTemplate)

        if (isConnectedToInternet(requireContext(), false)) {
            paymentViewModel.createCreditNoteApi(
                requireContext(),
                arrayListOf(createPaymentTemplate!!)
            )
        }
    }

    private fun createPendingPayment(createPaymentTemplate: CreatePaymentTemplate){

        val pendingPaymentTemplate= PendingPaymentTemplate(createPaymentTemplate)
        activityViewModel.currentRoute.value?.find {
            it.Account?.id.toString() == products.Account?.id.toString()
        }.let {
           if( it?.PendingPayments.isNullOrEmpty()){
               it?.PendingPayments= arrayListOf(pendingPaymentTemplate)
           }else{
               it?.PendingPayments?.add(pendingPaymentTemplate)
           }

            lifecycleScope.launchWhenCreated {
                routeActivityDao.insert(it)
            }
        }

    }

    private fun saveOrderToDb(status: String) {
        val saveOrderTemplate = SaveOrderTemplate()

        if (dbOrderId != null) {
            saveOrderTemplate.id = dbOrderId
        }

        saveOrderTemplate.accountId = products?.Account?.id
        saveOrderTemplate.routeId = products?.routeId
        saveOrderTemplate.taskId = products?.Visit?.Activity?.id
        saveOrderTemplate.Order = products?.Order
        saveOrderTemplate.orderId = products?.Order?.id
        saveOrderTemplate.lov_order_type = OrderType.TYPE_RETURN
        saveOrderTemplate.lov_return_reason = reason
        saveOrderTemplate.ProductAssortment = products?.ProductAssortment
        saveOrderTemplate.Account = products?.Account
        saveOrderTemplate.Paymentprofiles = products?.Paymentprofiles
        saveOrderTemplate.Promotions = products?.Promotions

//        if (status == "01 - Pending") {
//            saveOrderTemplate.ProductAssortment = orderProductList
//        } else {
            saveOrderTemplate.CartProducts = confirmOrderProductList
            saveOrderTemplate.ProductAssortment = orderProductList
//        }

//        if (status == "04 - Delivered") {
//            saveOrderTemplate.Order?.total_due = calculateDue()
//        }
        saveOrderTemplate.Order?.lov_order_status = status
        saveOrderTemplate.lov_order_type = "Return"

        lifecycleScope.launchWhenCreated {
            dbOrderId = saveOrderDao.insert(saveOrderTemplate)
            setFragmentResult(ON_RETURN_ORDER_STATUS_CHANGE, bundleOf())
        }
    }

    private fun setOrderData() {

        confirmOrderProductList = arrayListOf()

        products.CartProducts?.let {
            repriceProductAdapter = TakeOrderProductsAdapter(requireContext(), currentPriceList?.Pricelist?.currency_symbol?:"$")
            binding.rvProducts.adapter = repriceProductAdapter
            confirmOrderProductList.addAll(it)
            repriceProductAdapter.add(confirmOrderProductList)
        }

        products.let {

            binding.tvTotal.text =
                String.format(getString(R.string.cart_price_text), currentPriceList?.Pricelist?.currency_symbol?:"$", it.Order?.total)
            binding.tvDueAmount.text =
                String.format(getString(R.string.cart_price_text), currentPriceList?.Pricelist?.currency_symbol?:"$", it.Order?.total_due)

            binding.tvSubTotal.text =
                String.format(getString(R.string.cart_price_text), currentPriceList?.Pricelist?.currency_symbol?:"$", it.Order?.subtotal)
            binding.tvTax.text =
                String.format(getString(R.string.cart_price_text), currentPriceList?.Pricelist?.currency_symbol?:"$", it.Order?.tax_amt)
            binding.tvDiscount.text =
                String.format(getString(R.string.cart_price_text), currentPriceList?.Pricelist?.currency_symbol?:"$", it.Order?.total_discount)

        }

        binding.rvProducts.visible()

        binding.tvCusomerName.text = products?.Order?.lov_order_status
        binding.tvOrderNum.text = products?.Order?.name.toString()
        binding.tvCusName.text = products?.Account?.name.toString()
        binding.tvDeliveryDate.text = products?.Order?.delivery_date.toString().split(" ")[0]
        binding.tvConfDate.text = products?.Order?.confirmation_date.toString().split(" ")[0]
        binding.tvCreditLimit.text = (products?.Account?.credit_limit ?: 0.0).toString()
//        binding.tvAvailableCredit.text =
//            (products?.Account?.calc_available_credit ?: 0.0).toString()
        val availcredit = (products?.Account?.calc_available_credit ?: 0.0) - calculateDue()
        binding.tvAvailableCredit.text = availcredit.toString()

        if (products?.Order?.delivery_date.isNullOrEmpty()) {
            binding.tvDeliveryDate.text = "N/A"
        } else {
            binding.tvDeliveryDate.text = products?.Order?.delivery_date.toString().split(" ")[0]
        }

        if (products.Order?.confirmation_date.isNullOrEmpty()) {
            binding.tvConfDate.text = "N/A"
        } else {
            binding.tvConfDate.text = products.Order?.confirmation_date.toString().split(" ")[0]
        }

        binding.tvCreditLimit.text = (products.Account?.credit_limit ?: 0.0).toString()
//        binding.tvAvailableCredit.text =
//            (products?.Account?.calc_available_credit ?: 0.0).toString()
//        val availcredit = (products?.Account?.calc_available_credit ?: 0.0) - calculateDue()
        binding.tvAvailableCredit.text =
            (products.Account?.calc_available_credit ?: 0.0).toString()


//        if (order?.Order?.lov_order_status != "01 - Pending" && status != "01 - Pending") {
        binding.tvAddProduct.text = getString(R.string.open)
        binding.tvConfirm.text = getString(R.string.deliver)
//        } else {
//
//            binding.viewFlipper.displayedChild = 1
//
////            if (calculateDue()>0.0){
//            binding.tvConfirm.text = getString(R.string.pay)
////            }else{
////                binding.tvConfirm.text = getString(R.string.paid)
////                binding.viewFlipper.displayedChild=2
////            }
//        }
//    }

//        } else {
//            binding.tvConfirm.text = getString(R.string.pay)
//            binding.viewFlipper.displayedChild = 2
//        }

    }

    private fun createOrderAPi() {
        val orderList = arrayListOf<CreateOrderTemplate>()
        totalAmount = 0.0

        orderProductList.forEach { product ->
            if ((product.AccountProduct?.sub_total ?: "0.0").toDouble() > 0.0) {
                totalAmount += (product.AccountProduct?.total ?: "0.0").toDouble()
            }
        }

        if (products.Order?.integration_id.isNullOrEmpty() || (isNew && !isOpened)) {
            integrationId = Calendar.getInstance().timeInMillis.toString()
        } else {
            integrationId = products.Order?.integration_id
        }

        val order = CreateOrderData(
            account_id = products?.Account?.id,
            integration_id = integrationId,
            lov_order_status = status,
            integration_total = totalAmount,
            lov_return_reason = reason,
            lov_order_type = "Return",
            tcp_activity_integration_id = products.Visit?.Activity?.integration_id
        )

        var productList = arrayListOf<CreateOrderProduct>()
        orderProductList.forEach {
            if ((it.AccountProduct?.agreed_qty ?: "0").toDouble() > 0.0) {
                productList.add(
                    CreateOrderProduct(
                        integration_total = (it.AccountProduct?.total ?: "0.0").toDouble(),
                        product_id = (it.Product?.id),
                        product_qty = (it.AccountProduct?.agreed_qty ?: "0.0").toDouble(),
                        lov_invbinproduct_status = it.Product?.lov_invbinproduct_status
                    )
                )
            }
        }
        orderList.add(
            CreateOrderTemplate(
                Order = order,
                products = productList
            )
        )

        if (isConnectedToInternet(requireContext(), false)) {
            if ((isNew && !isOpened))
                viewModel.createOrderApi(requireContext(), orderList)
            else
                viewModel.updateOrderApi(requireContext(), orderList)
        } else {
            lifecycleScope.launchWhenStarted {
                val result = createOrderDao.insert(orderList)
                Timber.e(result.toString())
//                saveOrderToDb(status)
                setLocalOrder()
            }
        }
    }

    private fun setLocalOrder() {
        val cartProducts = arrayListOf<ProductAssortment>()

        orderProductList.forEach { addedProduct ->
            val orderProductTemplate = OrdersProduct()
            orderProductTemplate.product_qty =
                (addedProduct.AccountProduct?.agreed_qty ?: "0.0").toDouble()
            orderProductTemplate.disc_percent = addedProduct.AccountProduct?.apply_direct_disc
            orderProductTemplate.total = (addedProduct.AccountProduct?.total ?: "0.0").toDouble()
            orderProductTemplate.subtotal =
                (addedProduct.AccountProduct?.sub_total ?: "0.0").toDouble()
            orderProductTemplate.base_price = addedProduct.AccountProduct?.net_price

            cartProducts.add(
                ProductAssortment(
                    OrdersProduct = orderProductTemplate,
                    PricelistProduct = addedProduct.AccountProduct,
                    Product = addedProduct.Product
                )
            )
        }

        val createdOrder = OrderTemplate()

        createdOrder.lov_order_status = status
        createdOrder.total = totalAmount
        createdOrder.total_discount = totalDiscount
        createdOrder.total_due = calculateDue()
        createdOrder.tax_amt = totalTax
        createdOrder.lov_order_type = "Sales"
        createdOrder.name = binding.tvOrderNum.text.toString()
        createdOrder.subtotal = subTotalAmount
        createdOrder.integration_id = integrationId
        createdOrder.id = products.Order?.id

        when (status) {
            "01 - Pending" -> {

                binding.tvSaveOrder.visible()
                order?.let { orderListObject ->
                    orderListObject.Order = createdOrder
                }
                products.Order = createdOrder
                products.CartProducts = cartProducts

                setOrderData()
                binding.viewFlipper.displayedChild = 0
                saveOrderToDb("01 - Pending")
            }
            "02 - Confirmed" -> {

                order?.let { orderListObject ->
                    orderListObject.Order = createdOrder
                }
                products.Order = createdOrder
                products.CartProducts = cartProducts

                setOrderData()
                binding.viewFlipper.displayedChild = 1
                saveOrderToDb("02 - Confirmed")

                when (userRepository.getTeam()?.Team?.lov_team_type) {
                    "Delivery",
                    "Presales",
                    "Trade" -> {
                        binding.llOrderOption.gone()
                    }
                    else -> {
                    }
                }
            }
        }

    }

    private fun updateOrderAPi(status: String) {
        val orderList = arrayListOf<CreateOrderTemplate>()
        totalAmount = 0.0

        integrationId = products.Order?.integration_id
        val order = CreateOrderData(
            id = products?.Order?.id ?: null,
            account_id = products.Account?.id,
            integration_id = products?.Order?.integration_id,
            lov_order_status = status,
            integration_total = totalAmount,
            lov_return_reason = reason,
            lov_order_type = "Return",
            tcp_activity_integration_id = products.Visit?.Activity?.integration_id
        )

        var productList = arrayListOf<CreateOrderProduct>()
        orderProductList.forEach { cartProduct ->
            if ((cartProduct.Product?.order_qty ?: "0").toDouble() > 0.0) {
                productList.add(
                    CreateOrderProduct(
                        integration_total = cartProduct.OrdersProduct?.total ?: 0.0,
                        product_id = (cartProduct.Product?.id),
//                        product_qty = cartProduct.Product?.order_qty?.toDouble() ?: 0.0,
                        product_qty = (cartProduct.AccountProduct?.agreed_qty ?: "0.0").toDouble(),
                        lov_invbinproduct_status = cartProduct.Product?.lov_invbinproduct_status
                    )
                )
            }
        }

        orderList.add(
            CreateOrderTemplate(
                Order = order,
                products = productList
            )
        )

        if (isConnectedToInternet(requireContext(), false)) {
            viewModel.updateOrderApi(requireContext(), orderList)
        } else {
            lifecycleScope.launchWhenStarted {
                val result = createOrderDao.insert(orderList)
                Timber.e(result.toString())
            }
        }


    }

    private fun calculateDue(): Double {
        val totalAmount = totalAmount
        var collectedAmount = 0.0

//        createPaymentList.forEach { payment ->
//            collectedAmount += payment.amount ?: 0.0
//        }

        val dueAmount = totalAmount - collectedAmount
        if (dueAmount < 0.05)
            return 0.0
        else
            return dueAmount
//        return totalAmount - collectedAmount
    }

}