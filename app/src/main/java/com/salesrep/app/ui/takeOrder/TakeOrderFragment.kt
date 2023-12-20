package com.salesrep.app.ui.takeOrder

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.core.os.bundleOf
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
import com.salesrep.app.dao.SaveOrderDao
import com.salesrep.app.data.models.*
import com.salesrep.app.data.models.inventory.GetTeamInventoryResponse
import com.salesrep.app.data.models.inventory.InvBinProductsData
import com.salesrep.app.data.models.inventory.InventoryDataObject
import com.salesrep.app.data.models.inventory.ManufacturerTemplate
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
import com.salesrep.app.ui.inventory.SelectProductFragment
import com.salesrep.app.ui.payment.CancelPaymentDialog
import com.salesrep.app.ui.payment.PaymentFragment
import com.salesrep.app.ui.payment.PaymentViewModel
import com.salesrep.app.ui.payment.adapters.PaymentProfilesAdapter
import com.salesrep.app.ui.stock.ChooseProductFragment
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
class TakeOrderFragment : BaseFragment<FragmentTakeOrderNewBinding>() {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var createOrderDao: CreateOrderDao

    @Inject
    lateinit var saveOrderDao: SaveOrderDao

    private lateinit var binding: FragmentTakeOrderNewBinding
    override val viewModel by viewModels<HomeViewModel>()
    private lateinit var progressDialog: ProgressDialog
    private lateinit var productList: List<ProductAssortment>
    private lateinit var orderProductList: ArrayList<ProductAssortment>
    private var confirmOrderProductList = arrayListOf<ProductAssortment>()


    private var totalAmount: Double = 0.0
    private var totalTax: Double = 0.0
    private var totalDiscount: Double = 0.0
    private var subTotalAmount: Double = 0.0

    private lateinit var productAdapter: TakeOrderAdapter
    private var currentPriceList: GetTeamPricelistResponse? = null

    private lateinit var repriceProductAdapter: TakeOrderProductsAdapter

    private var status: String = "01 - Pending"
    private var integrationId: String? = null

    private lateinit var paymentProfilesAdapter: PaymentProfilesAdapter
    val paymentViewModel by viewModels<PaymentViewModel>()
    private var availableCredit: Double = 0.0

    private var createPaymentTemplate: CreatePaymentTemplate? = null

    private var createPaymentList = arrayListOf<CreatePaymentTemplate>()
    private var selectedPaymentProfile: Paymentprofiles? = null

    private lateinit var products: GetRouteAccountResponse
    private var dbOrderId: Long? = null
    private var isPendingSelected = false

    private val isNew by lazy {
        arguments?.getBoolean(
            DataTransferKeys.KEY_IS_NEW, false
        ) ?: false
    }

    private var isOpened = false
    private val order by lazy {
        arguments?.getParcelable<OrderListObject>(
            DataTransferKeys.KEY_ORDER_ID
        )
    }
    private val isCompleted by lazy {
        arguments?.getBoolean(
            DataTransferKeys.KEY_IS_COMPLETED, false
        )
    }

    private var inventory: GetTeamInventoryResponse? = null

    @Inject
    lateinit var inventoryDao: InventoryDao

    private var savedOrderList: List<SaveOrderTemplate>? = null

    companion object {
        const val ON_ORDER_STATUS_CHANGE = "ON_ORDER_STATUS_CHANGE"
    }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_take_order_new
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(AppRequestCode.SELECT_PRODUCT_REQUEST) { key, bundle ->
            if (bundle.get(DataTransferKeys.KEY_PRODUCTS) != null) {
                when (userRepository.getTeam()?.Team?.lov_team_type) {
                    "Delivery",
                    "Trade" -> {
                    }
                    "Presales" -> {

                        val product =
                            bundle.getParcelable<ProductTemplate>(DataTransferKeys.KEY_PRODUCTS)

                        val priceListObj =
                            currentPriceList?.Products?.find { it.Product?.integration_num == product?.integration_num }

                        if (priceListObj != null) {
                            val assortObj =
                                ProductAssortment(priceListObj?.PricelistProduct, product)

                            if (DateUtils.isValidDiscount(
                                    priceListObj.PricelistProduct?.disc_startdt,
                                    priceListObj.PricelistProduct?.disc_enddt
                                )
                            ) {

                                assortObj.AccountProduct?.apply_direct_disc =
                                    priceListObj.PricelistProduct?.disc_percent

                            }
                            orderProductList.add(assortObj)
//                    orderProductList.add(ProductAssortment(null, product))
//                orderProductList= list.toList()
                            productAdapter.notifyIsInitialize(false)
                            productAdapter.notifyData(orderProductList)
                            binding.rvCustomers.visible()
                        }
                    }
                    else -> {
                        val product =
                            bundle.getParcelable<InventoryDataObject>(DataTransferKeys.KEY_PRODUCTS)
                        val manufacturer = product?.Manufacturer
//                    bundle.getParcelable<ManufacturerTemplate>(DataTransferKeys.KEY_MANUFACTURER)

                        val productIndex =
                            orderProductList.indexOfFirst { it.Product?.id == product?.Product?.id }

                        if ((productIndex < 0)) {

                            val priceListObj =
                                currentPriceList?.Products?.find { it.Product?.integration_num == product?.Product?.integration_num }

                            priceListObj?.PricelistProduct?.agreed_qty =
                                (product?.Product?.min_qty ?: 1.0).toString()

                            if (priceListObj != null) {
                                val assortObj = ProductAssortment(
                                    priceListObj?.PricelistProduct,
                                    product?.Product,
                                    InvbinsProduct = product?.InvbinsProduct
                                )

                                if (DateUtils.isValidDiscount(
                                        priceListObj.PricelistProduct?.disc_startdt,
                                        priceListObj.PricelistProduct?.disc_enddt
                                    )
                                )

                                    assortObj.AccountProduct?.apply_direct_disc =
                                        priceListObj.PricelistProduct?.disc_percent
                                orderProductList.add(assortObj)
//                    orderProductList.add(ProductAssortment(null, product))
//                orderProductList= list.toList()
                                productAdapter.notifyIsInitialize(false)
                                productAdapter.notifyData(orderProductList)
                                binding.rvCustomers.visible()
                            }
                        }
                    }
                }
            }
        }

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

        setFragmentResultListener(AppRequestCode.REMOVE_PAYMENT_OPTION_REQUEST) { key, bundle ->
            if (bundle.get(DataTransferKeys.KEY_REASON) != null) {
                val reason = bundle.getString(DataTransferKeys.KEY_REASON)
                if (!createPaymentList.isNullOrEmpty()) {
                    var removePaymentList = arrayListOf<RevertPaymentData>()
                    createPaymentList.forEach {
                        if (it.isSelected == true) {
                            removePaymentList.add(
                                RevertPaymentData(
                                    integration_id = it.integration_id,
                                    lov_payment_cancelreason = reason
                                )
                            )
                        }
                    }
                    if (isConnectedToInternet(requireContext(), false))
                        paymentViewModel.removePaymentApi(requireContext(), removePaymentList)
                    else {

//                        createPaymentList.removeIf {
//                            it.isSelected == true
//
//                        }
                        createPaymentList.forEach {
                            if (it.isSelected == true) {
                                it.lov_payment_status = "Reverted"
                                it.isSelected = false
                            }
                        }
                        paymentProfilesAdapter.addList(createPaymentList, -1, true)
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
                        binding.etAmount.setText(
                            String.format(
                                getString(R.string.cart_price_text),
                                "",
                                calculateDue()
                            )
                        )
                        val availcredit = if (availableCredit > 0) {
                            availableCredit - calculateDue()
                        } else {
                            0.0
                        }
                        binding.tvAvailableCredit.text = availcredit.toString()

                        if (!createPaymentList.isNullOrEmpty() && binding.viewFlipper.displayedChild >= 2) {
                            binding.tvAddProduct.alpha = 0.4f
                            binding.tvEnd.alpha = 0.4f
                            binding.tvAddProduct.isClickable = false
                            binding.tvEnd.isClickable = false
                        } else {
                            binding.tvAddProduct.alpha = 1f
                            binding.tvEnd.alpha = 1f
                            binding.tvAddProduct.isClickable = true
                            binding.tvEnd.isClickable = true
                        }

                        if (availableCredit > calculateDue()) {
                            binding.tvAddNew.visible()
                            binding.tvConfirm.text = getString(R.string.paid)

                        } else if (calculateDue() > 0.0) {
                            binding.tvAddNew.visible()
                            binding.tvConfirm.text = getString(R.string.products)

//            binding.tvConfirm.text = getString(R.string.pay)
//                        binding.tvPaid.gone()
                        } else {
                            binding.tvAddNew.gone()
                            binding.tvConfirm.text = getString(R.string.paid)
                            //                        binding.tvPaid.visible()

                        }

                        saveOrderToDb(status)

                    }
                }
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
        totalTax = 0.0
        totalDiscount = 0.0
        subTotalAmount = 0.0

        binding.viewFlipper.displayedChild = 0

        if (isNew) {
            initialize()
        } else {
            lifecycleScope.launchWhenCreated {
                products = arguments?.getParcelable(
                    DataTransferKeys.KEY_ACCOUNT_DETAIL
                )!!
                initialize()

                if (order == null) {
                    savedOrderList = saveOrderDao.getCustomerOrders(
                        products.routeId,
                        products.Visit?.Activity?.id,
                        products.Account?.id,
                        "Sales"
                    )
                } else {
                    val savedOrder = SaveOrderTemplate(
                        Order = order?.Order,
                        Paymentprofiles = products?.Paymentprofiles,
                        PendingPayments = order?.PendingPayments,
                        Promotions = products?.Promotions,
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


    private fun removeFromVan() {

//        val vanProducts = inventory?.Van?.Bins?.get(0)?.Inventory

        orderProductList.forEach { productAssortment ->
            val binIndex =
                inventory?.Van?.Bins?.indexOfFirst { it.Invbin?.id == productAssortment.InvbinsProduct?.invbin_id }

            if (binIndex != null && binIndex > -1) {
                val vanProducts = inventory?.Van?.Bins!![binIndex].Inventory

                vanProducts?.forEach { inventoryDataObject ->

                    if ((productAssortment.Product?.id == inventoryDataObject.Product.id)
                        && (inventoryDataObject.InvbinsProduct.lov_invbinproduct_status == "Good")
                    ) {
                        var qty = (inventoryDataObject.InvbinsProduct.qty ?: "0.0").toDouble()
                        qty -= if (isNew) {
                            (productAssortment.AccountProduct?.agreed_qty ?: "0.0").toDouble()
                        } else {
                            (productAssortment.Product?.order_qty ?: "0.0").toDouble()
                        }
                        inventoryDataObject.InvbinsProduct.qty = qty.toString()
                    }
                }

                inventory?.Van?.Bins?.get(binIndex)?.Inventory = vanProducts

            }
        }

        if (inventory != null) {
            lifecycleScope.launchWhenCreated {
                inventoryDao.clearAll()
                inventoryDao.insert(inventory)
            }
        }
    }


    private fun checkFromVan(): Boolean {


        orderProductList.forEach { productAssortment ->
            var isPresent = false

            val bin =
                inventory?.Van?.Bins?.find { it.Invbin?.id == productAssortment.InvbinsProduct?.invbin_id }

            if (bin != null) {
                val vanProducts = bin.Inventory

                vanProducts?.forEach { inventoryDataObject ->
                    if ((productAssortment.Product?.id == inventoryDataObject.Product.id)
                        && (inventoryDataObject.InvbinsProduct.lov_invbinproduct_status == "Good")
                    ) {
                        var qty = (inventoryDataObject.InvbinsProduct.qty ?: "0.0").toDouble()
                        var order_qty =
                            (productAssortment.AccountProduct?.agreed_qty ?: "0.0").toDouble()
                        if (qty < order_qty) {

                            Toast.makeText(
                                requireContext(),
                                getString(
                                    R.string.product_max_quality,
                                    productAssortment.Product?.title,
                                    inventoryDataObject.InvbinsProduct.qty
                                ),
                                Toast.LENGTH_LONG
                            ).show()
                            return false
                        }
                        isPresent = true
                    } else {
//                    isPresent= false
                    }
                }
            }

            if (!isPresent) {

                Toast.makeText(
                    requireContext(),
                    getString(
                        R.string.product_not_available,
                        productAssortment.Product?.title
                    ),
                    Toast.LENGTH_LONG
                ).show()
                return false
            }
        }

        return true
    }

    private fun checkForMinMax(): Boolean {
        orderProductList.forEach { productAssortment ->

            if ((productAssortment.AccountProduct?.agreed_qty
                    ?: "1.0").toDouble() > (productAssortment.Product?.max_qty ?: 10000000.0)
            ) {
                Toast.makeText(
                    requireContext(),
                    getString(
                        R.string.product_max_allowed_quality,
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
                    getString(
                        R.string.product_min_allowed_quality,
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

    private fun setLatestOrderData(saveOrderTemplate: SaveOrderTemplate) {
//        products= arguments?.getParcelable(
//            DataTransferKeys.KEY_PRODUCTS
//        )!!

        var proAssort = products.ProductAssortment

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

//        if (status != "01 - Pending") {
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
                assortProduct.AccountProduct?.tax_total =
                    cartProduct.PricelistProduct?.tax_total ?: 0.0
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
                assortProduct.AccountProduct?.tax_total =
                    cartProduct.PricelistProduct?.tax_total ?: 0.0
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

//            products.ProductAssortment?.forEach { assortProduct ->
//                val cartProduct =
//                    products.CartProducts?.firstOrNull { it.Product?.id == assortProduct.Product?.id }
//                if (cartProduct != null) {
//                    assortProduct.AccountProduct?.agreed_qty =
//                        cartProduct.OrdersProduct?.product_qty.toString()
//                    assortProduct.AccountProduct?.apply_direct_disc =
//                        cartProduct.OrdersProduct?.disc_percent
//                    assortProduct.AccountProduct?.total =
//                        cartProduct.OrdersProduct?.total?.toString()
//                    assortProduct.AccountProduct?.sub_total =
//                        cartProduct.OrdersProduct?.subtotal?.toString()
//                    assortProduct.AccountProduct?.tax = cartProduct.PricelistProduct?.tax ?: 0.0
//                    assortProduct.AccountProduct?.net_price =
//                        cartProduct.PricelistProduct?.net_price ?: 0.0
//                    assortProduct.Product?.min_qty = cartProduct.Product?.min_qty
//                    assortProduct.Product?.max_qty = cartProduct.Product?.max_qty
//                    assortProduct.Product?.order_qty = cartProduct.Product?.order_qty
//                    assortProduct.Product?.step_qty = cartProduct.Product?.step_qty ?: 1.0
//                    inventory?.Van?.Bins?.forEach { bin ->
//                        bin.Inventory?.forEach { invBinproduct ->
//                            if (invBinproduct.Product.id == cartProduct?.Product?.id) {
//                                assortProduct.InvbinsProduct = invBinproduct.InvbinsProduct
//                            }
//                        }
//                    }
//                    orderProductList.add(assortProduct)
//                }
//            }

        if (!orderProductList.isNullOrEmpty()) {
            productAdapter =
                TakeOrderAdapter(
                    requireContext(),
                    ::onTotalChanged,
                    ::onDeleteProduct,
                    currency_symbol = currentPriceList?.Pricelist?.currency_symbol ?: "$"
                )
            binding.rvCustomers.adapter = productAdapter
            binding.rvCustomers.visible()
//                if (status=="01 - Pending"){
            productAdapter.notifyIsInitialize(true)
//                }else{
//                    productAdapter.notifyIsInitialize(false)
//                }
            productAdapter.notifyData(orderProductList)
            onTotalChanged(true)
//                productAdapter.notifyIsInitialize(false)
        } else {
            products.ProductAssortment?.let {
                orderProductList = arrayListOf()
                orderProductList.addAll(it)
                productAdapter = TakeOrderAdapter(
                    requireContext(),
                    ::onTotalChanged,
                    ::onDeleteProduct,
                    currency_symbol = currentPriceList?.Pricelist?.currency_symbol ?: "$"
                )
                binding.rvCustomers.adapter = productAdapter
                binding.rvCustomers.visible()
                productAdapter.notifyIsInitialize(false)
                productAdapter.notifyData(orderProductList)
                onTotalChanged(true)
            }
        }

//        }

//        else {
//            products.ProductAssortment?.let {
//                orderProductList = arrayListOf()
//                orderProductList.addAll(it)
//                productAdapter =
//                    TakeOrderAdapter(requireContext(), ::onTotalChanged, ::onDeleteProduct)
//                binding.rvCustomers.adapter = productAdapter
//                binding.rvCustomers.visible()
//                productAdapter.notifyData(orderProductList)
//                onTotalChanged(true)
//            }
//        }
        totalAmount = products.Order?.total ?: 0.0

        when (products.Order?.lov_order_status) {
            "05 - Closed" -> {
                binding.tvSaveOrder.gone()
                status = "05 - Closed"
                setOrderData()
                onTotalChanged(true)
                initializePaymentScreen()
                binding.viewFlipper.displayedChild = 1
                binding.llOrderOption.gone()
                binding.tvConfirm.text = getString(R.string.close)
                binding.tvAddProduct.alpha = 0.4f
                binding.tvEnd.alpha = 0.4f
                binding.tvAddProduct.isClickable = false
                binding.tvEnd.isClickable = false
                binding.llViewPayment.visible()
                binding.tvPrepare.gone()
                binding.tvPrint.visible()
            }
            "06 - Cancelled" -> {
                status = "06 - Cancelled"
                setOrderData()
                binding.tvSaveOrder.gone()
                binding.viewFlipper.displayedChild = 1
                binding.llOrderOption.gone()
                binding.tvConfirm.isClickable = false
                binding.tvAddProduct.isClickable = false
                binding.tvEnd.isClickable = false
                binding.llViewPayment.gone()
                binding.tvPrepare.gone()
                binding.tvPrint.gone()
            }
            "02 - Confirmed" -> {

                binding.tvPrint.visible()
                binding.tvSaveOrder.gone()
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
                        binding.llViewPayment.gone()
                        binding.tvPrepare.gone()
                    }
                }
            }
//            "03 - Ordered" -> {
//                setOrderData()
//                initializePaymentScreen()
//                binding.viewFlipper.displayedChild = 2
//                binding.llViewPayment.gone()
//            }
            "04 - Paid" -> {
                binding.tvPrint.visible()
                binding.tvSaveOrder.gone()
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
                        initializePaymentScreen()
                        binding.llViewPayment.gone()
                        binding.tvPrepare.gone()
                        if ((products?.Account?.credit_limit ?: 0.0) <= 0.0) {
                            binding.tvConfirm.text = getString(R.string.deliver)
                            binding.viewFlipper.displayedChild = 1
                            binding.tvAddProduct.alpha = 0.4f
                            binding.tvEnd.alpha = 0.4f
                            binding.tvAddProduct.isClickable = false
                            binding.tvEnd.isClickable = false
                        } else {
                            status = "05 - Closed"
                            binding.viewFlipper.displayedChild = 1
                            binding.llOrderOption.gone()
                            binding.tvConfirm.text = getString(R.string.close)
                            binding.tvAddProduct.alpha = 0.4f
                            binding.tvEnd.alpha = 0.4f
                            binding.tvAddProduct.isClickable = false
                            binding.tvEnd.isClickable = false
                            binding.llViewPayment.visible()
                        }
                    }
                }

            }
            "04 - Delivered" -> {
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
                        initializePaymentScreen()
                        binding.tvPrepare.gone()
                        binding.llViewPayment.gone()
                        if (calculateDue() <= 0.0) {
                            binding.viewFlipper.displayedChild = 1
                            binding.tvConfirm.text = getString(R.string.close)
                            binding.tvAddProduct.alpha = 0.4f
                            binding.tvEnd.alpha = 0.4f
                            binding.tvEnd.isClickable = false
                            binding.tvEnd.isClickable = false
                        } else {
                            binding.tvAddProduct.alpha = 0.4f
                            binding.tvEnd.alpha = 0.4f
                            binding.tvEnd.isClickable = false
                            binding.tvEnd.isClickable = false
                            binding.viewFlipper.displayedChild = 1
//                            binding.viewFlipper.displayedChild = 2
                        }

                    }
                }
            }
            else -> {
                binding.tvPrint.gone()
                when (userRepository.getTeam()?.Team?.lov_team_type) {
                    "Delivery",
                    "Trade" -> {
                        binding.tvSaveOrder.gone()
                        binding.llOrderOption.gone()
                        setOrderData()
                        binding.viewFlipper.displayedChild = 1
                        binding.llViewPayment.gone()
                        binding.tvPrepare.gone()
                    }
                    else -> {
                        binding.tvSaveOrder.visible()
                        binding.tvCusomerName.text = "01 - Pending"
                        status = "01 - Pending"
                        binding.tvConfirm.visible()
                        binding.rvCustomers.visible()
                        binding.viewFlipper.displayedChild = 0
//                        productAdapter.notifyIsInitialize(false)
//                        productAdapter.notifyData(orderProductList)
                        binding.tvEnd.visible()
                        binding.tvConfirm.text = getString(R.string.confirm)
                        binding.llViewPayment.gone()
                        binding.tvPrepare.visible()
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

        binding.tvPrepare.setOnClickListener {

            when (userRepository.getTeam()?.Team?.lov_team_type) {
                "Trade" -> {
                }
                else -> {
                    prepareList()
                    productAdapter.notifyIsInitialize(false)
                    productAdapter.notifyData(orderProductList)
                    binding.tvConfirm.visible()
                    binding.rvCustomers.visible()
                    binding.tvEnd.visible()
                    binding.tvConfirm.text = getString(R.string.confirm)
                    binding.llViewPayment.gone()
//                        binding.tvPrepare.gone()
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

        binding.tvConfirm.setOnClickListener {
            if (!orderProductList.isNullOrEmpty() && (binding.tvConfirm.text.toString() == getString(
                    R.string.confirm
                ))
            ) {

                when (userRepository.getTeam()?.Team?.lov_team_type) {
                    "Delivery",
                    "Trade" -> {
                    }
                    "Presales" -> {
                        if (orderProductList.isNullOrEmpty()) {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.please_add_products),
                                Toast.LENGTH_LONG
                            ).show()
                        } else if (checkForMinMax()) {
                            status = "02 - Confirmed"
                            binding.tvSaveOrder.gone()
                            binding.tvPrint.visible()
                            createOrderAPi()
                            binding.tvCusomerName.text = status
                            binding.llViewPayment.gone()
                            binding.tvPrepare.gone()
                            isOpened = false
                        }
                    }
                    else -> {
                        if (orderProductList.isNullOrEmpty()) {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.please_add_products),
                                Toast.LENGTH_LONG
                            ).show()
                        } else if (checkForMinMax() && checkFromVan()) {
                            status = "02 - Confirmed"
                            binding.tvSaveOrder.gone()
                            binding.tvPrint.visible()
                            createOrderAPi()
                            binding.tvCusomerName.text = status
                            binding.llViewPayment.gone()
                            binding.tvPrepare.gone()
                            isOpened = false
                        }
                    }
                }
            }
//            else if (binding.tvConfirm.text.toString() == getString(R.string.order)) {
//
//                when (userRepository.getTeam()?.Team?.lov_team_type) {
//                    "Presales",
//                    "Trade" -> {
//                    }
//                    else -> {
//                        status = "03 - Ordered"
//                        updateOrderAPi("03 - Ordered")
//                        binding.tvCusomerName.text = status
//                        saveOrderToDb(status)
//                        binding.llViewPayment.gone()
//                    }
//                }
//            }
            else if ((binding.tvConfirm.text.toString() == getString(R.string.paid))
                || binding.tvConfirm.text.toString() == getString(R.string.pay)
            ) {
                when (userRepository.getTeam()?.Team?.lov_team_type) {
                    "Presales",
                    "Trade" -> {
                    }
                    else -> {
                        binding.llViewPayment.gone()
                        binding.tvPrint.visible()
                        binding.tvPrepare.gone()
                        binding.tvSaveOrder.gone()
                        if (binding.viewFlipper.displayedChild == 2) {
                            if (availableCredit <= 0.0 && calculateDue() <= 0.0) {
                                status = "04 - Paid"
                                updateOrderAPi("04 - Paid")
                                if (!isConnectedToInternet(requireContext(), false)) {
                                    binding.tvConfirm.text = getString(R.string.deliver)
                                    binding.viewFlipper.displayedChild = 1
                                    binding.tvAddProduct.alpha = 0.4f
                                    binding.tvEnd.alpha = 0.4f
                                    binding.tvAddProduct.isClickable = false
                                    binding.tvEnd.isClickable = false
                                    binding.tvSaveOrder.gone()
                                }
                                binding.tvCusomerName.text = status
                                saveOrderToDb(status)
                            } else if (calculateDue() <= 0.0) {
                                status = "05 - Closed"
                                updateOrderAPi("05 - Closed")
                                if (!isConnectedToInternet(requireContext(), false)) {
                                    binding.tvSaveOrder.gone()
                                    status = "05 - Closed"
                                    binding.viewFlipper.displayedChild = 1
                                    binding.llOrderOption.gone()
                                    binding.tvConfirm.text = getString(R.string.close)
                                    binding.tvAddProduct.alpha = 0.4f
                                    binding.tvEnd.alpha = 0.4f
                                    binding.tvAddProduct.isClickable = false
                                    binding.tvEnd.isClickable = false
                                    binding.llViewPayment.visible()
                                    binding.tvPrepare.gone()
                                }
                                binding.tvCusomerName.text = status
                                saveOrderToDb(status)
                            }
                        } else {
                            setOrderData()
//                            initializePaymentScreen()
                            binding.viewFlipper.displayedChild = 2

                            if (calculateDue() > 0.0) {
                                binding.tvConfirm.text = getString(R.string.products)
//                binding.tvConfirm.text = getString(R.string.pay)
                            } else {
                                binding.tvConfirm.text = getString(R.string.paid)

                            }
                            if (status == "04 - Delivered") {
                                binding.tvAddProduct.alpha = 0.4f
                                binding.tvEnd.alpha = 0.4f
                                binding.tvAddProduct.isClickable = false
                                binding.tvEnd.isClickable = false
                            }

                        }

//                        if (isConnectedToInternet(requireContext(), false)) {
//                        }

                    }
                }
            } else if (binding.tvConfirm.text.toString() == getString(R.string.products)) {
                binding.tvPrepare.gone()
                binding.tvPrint.gone()
                binding.tvSaveOrder.gone()
                binding.llViewPayment.gone()
                binding.tvPrepare.gone()
                binding.tvConfirm.text = getString(R.string.pay)
                binding.viewFlipper.displayedChild = 1
//                        if (isConnectedToInternet(requireContext(), false)) {
//
//                        }
            } else if (binding.tvConfirm.text.toString() == getString(R.string.deliver)) {
                when (userRepository.getTeam()?.Team?.lov_team_type) {
                    "Presales",
                    "Trade" -> {

                    }
                    else -> {
                        binding.tvPrint.visible()
                        status = "04 - Delivered"
                        binding.tvSaveOrder.gone()
                        updateOrderAPi("04 - Delivered")
                        binding.tvCusomerName.text = status
                        saveOrderToDb(status)
                        removeFromVan()
                        binding.tvPrepare.gone()
                        binding.llViewPayment.gone()
                        if (!isConnectedToInternet(requireContext(), false)) {
                            if (calculateDue() <= 0.0) {
                                binding.viewFlipper.displayedChild = 1
                                binding.tvPrint.visible()
                                status = "05 - Closed"
                                binding.tvSaveOrder.gone()
                                binding.tvCusomerName.text = status
                                saveOrderToDb(status)
                                binding.tvPrepare.gone()
                                binding.llViewPayment.visible()
                                binding.llOrderOption.gone()
//                                binding.tvConfirm.text = getString(R.string.close)
//                                binding.tvAddProduct.alpha = 0.4f
//                                binding.tvEnd.alpha = 0.4f
//                                binding.tvEnd.isClickable = false
//                                binding.tvEnd.isClickable = false
                            } else {
                                binding.viewFlipper.displayedChild = 2
                            }
                        }

                    }
                }

            } else if (binding.tvConfirm.text.toString() == getString(R.string.close)) {
                when (userRepository.getTeam()?.Team?.lov_team_type) {
                    "Presales",
                    "Trade" -> {
                    }
                    else -> {
                        binding.tvPrint.visible()
                        status = "05 - Closed"
                        binding.tvSaveOrder.gone()
                        updateOrderAPi("05 - Closed")
                        binding.tvCusomerName.text = status
                        saveOrderToDb(status)
                        binding.tvPrepare.gone()
                        binding.llViewPayment.visible()
                        binding.llOrderOption.gone()
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

        binding.tvAddProduct.setOnClickListener {
            when (status) {
                "01 - Pending" -> {
                    when (userRepository.getTeam()?.Team?.lov_team_type) {
                        "Delivery",
                        "Trade",
                        "Presales" -> {
                            navigator.push(ChooseProductFragment::class)
                        }
                        else -> {
                            val binProductList = arrayListOf<InventoryDataObject>()

                            inventory?.Van?.Bins?.forEach { bin ->
                                bin.Inventory?.forEach {
                                    binProductList.add(it)
                                }
                            }

                            navigator.push(SelectProductFragment::class) {
                                this.arguments =
                                    bundleOf(
                                        Pair(
                                            DataTransferKeys.KEY_SELECTED_BIN_PRODUCTS,
                                            binProductList
                                        )
                                    )
                            }
                        }
                    }
//                    navigator.push(ChooseProductFragment::class)
                }
                "02 - Confirmed" -> {
                    binding.viewFlipper.displayedChild = 0
                    status = "01 - Pending"
                    binding.tvPrint.gone()
                    binding.tvSaveOrder.visible()
                    binding.tvConfirm.text = getString(R.string.confirm)
                    binding.tvAddProduct.text = getString(R.string.add)
                    binding.tvPrepare.visible()
                    isOpened = true
                }
                else -> {
                }
            }
        }

        binding.tvViewPayments.setOnClickListener {
            when (status) {
                "05 - Closed" -> {
                    if (!isPendingSelected) {
                        isPendingSelected = true
                        binding.tvViewPayments.setTextColor(requireContext().resources.getColor(R.color.colorPrimary))
                        binding.tvProducts.setTextColor(requireContext().resources.getColor(R.color.grey_6))
                        binding.tvProducts.background = null
                        binding.tvViewPayments.setBackgroundResource(R.drawable.bg_light_bule_12dp)
                        binding.viewFlipper.displayedChild = 2
                        binding.llPaymentButtons.gone()
                        paymentProfilesAdapter.addList(createPaymentList, -1, false)
                    }

                }
                else -> {
                }
            }
        }

        binding.tvProducts.setOnClickListener {
            when (status) {
                "05 - Closed" -> {
                    if (isPendingSelected) {
                        isPendingSelected = false
                        binding.tvProducts.setTextColor(requireContext().resources.getColor(R.color.colorPrimary))
                        binding.tvViewPayments.setTextColor(requireContext().resources.getColor(R.color.grey_6))
                        binding.tvViewPayments.background = null
                        binding.tvProducts.setBackgroundResource(R.drawable.bg_light_bule_12dp)
                        binding.viewFlipper.displayedChild = 1
                    }
                }
                else -> {
                }
            }
        }

        binding.tvEnd.setOnClickListener {
            when (status) {
                "01 - Pending" -> {
                    runBlocking {
                        deleteOrder()
                        setFragmentResult(ON_ORDER_STATUS_CHANGE, bundleOf())
                        navigator.popBackStack()
                    }
                }
                "02 - Confirmed" -> {
                    binding.tvPrint.visible()
                    status = "06 - Cancelled"
                    updateOrderAPi("06 - Cancelled")
                    binding.tvCusomerName.text = status
                    saveOrderToDb(status)
//                    removeFromVan()
                    binding.llOrderOption.gone()
                    binding.tvPrepare.gone()
                    binding.llViewPayment.gone()
                }
                else -> {
                }
            }
        }

        binding.tvAddNew.setOnClickListener {
            binding.tvPaymentNum.text = Calendar.getInstance().timeInMillis.toString()
            binding.viewFlipper.displayedChild = 3
        }

        binding.tvCancelNew.setOnClickListener {
            binding.viewFlipper.displayedChild = 2
        }

        binding.tvSelectPaymentMethod.setOnClickListener {
            navigator.push(PaymentFragment::class) {
                arguments = bundleOf(Pair(DataTransferKeys.KEY_ACCOUNT, products.Paymentprofiles))
            }
        }
        binding.tvPrint.setOnClickListener {
            navigator.showDialog(PrintOrderDialog::class,
                bundleOf(
                    Pair(DataTransferKeys.KEY_ACCOUNT_DETAIL, products),
                    Pair(DataTransferKeys.KEY_PAYMENT_LIST, createPaymentList),
                ))

//            navigator.push(PrintOrderFragment::class) {
//                arguments = bundleOf(Pair(DataTransferKeys.KEY_ACCOUNT_DETAIL, products))
//            }
        }

        binding.tvRevert.setOnClickListener {
            var isSelected = false

            createPaymentList.forEach {
                if (it.isSelected == true) {
                    isSelected = true
                }
            }

            if (isSelected) {
                navigator.showDialog(
                    CancelPaymentDialog::class
                )
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.select_payment_to_cancel),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        binding.tvSave.setOnClickListener {

            if (TextUtils.isEmpty(etAmount.text)) {
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
            } else if ((binding.etAmount.text.toString().toDouble() - 0.1) > calculateDue()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.amount_less_than_due),
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
                if (products.Order?.id != null) {
                    createPaymentTemplate?.order_id = products?.Order?.id
                } else {
                    createPaymentTemplate?.order_integration_id = products?.Order?.integration_id
                }

                createPaymentTemplate?.lov_payment_status = "Used"

                createPaymentTemplate?.integration_id =
                    if (binding.tvPaymentNum.text.isNullOrEmpty())
                        Calendar.getInstance().timeInMillis.toString()
                    else
                        binding.tvPaymentNum.text.toString()

                createPaymentTemplate?.account_id = products?.Account?.id

                createPaymentTemplate?.isSelected = true

                if (isConnectedToInternet(requireContext(), false)) {
                    paymentViewModel.createPaymentApi(
                        requireContext(),
                        arrayListOf(createPaymentTemplate!!)
                    )
                } else {
                    updatePaymentAdapter()
                    saveOrderToDb(status)
                }
            }
        }
    }


    suspend fun deleteOrder() = coroutineScope {
        launch {
            saveOrderDao.deleteOrders(
                products?.routeId,
                products?.Visit?.Activity?.id,
                products?.Account?.id,
                "Sales"
            )
        }
    }

    private fun initialize() {
        products = arguments?.getParcelable<GetRouteAccountResponse>(
            DataTransferKeys.KEY_ACCOUNT_DETAIL
        )!!

        lifecycleScope.launchWhenCreated {
            inventory = inventoryDao.getAllInventory().lastOrNull()

            if (inventory != null && !orderProductList.isNullOrEmpty()) {

                orderProductList.forEach { cartProduct ->

                    inventory?.Van?.Bins?.forEach { bin ->
                        bin.Inventory?.forEach { invBinproduct ->
                            if (invBinproduct.Product.id == cartProduct.Product?.id) {
                                cartProduct.InvbinsProduct = invBinproduct.InvbinsProduct
                            }
                        }
                    }
                }

                productAdapter.notifyIsInitialize(true)
                productAdapter.notifyData(orderProductList)
//                productAdapter.notifyIsInitialize(false)
            }
        }

        orderProductList = arrayListOf()

        val gson = prefsManager.getString(PrefsManager.TEAM_PRICELIST, "")
        if (!gson.isNullOrEmpty()) {

            val listType: Type = object : TypeToken<ArrayList<GetTeamPricelistResponse?>?>() {}.type
            val productPriceList =
                Gson().fromJson<ArrayList<GetTeamPricelistResponse>>(gson, listType)
            currentPriceList =
                productPriceList.find { products?.Pricelist?.id == it.Pricelist?.id }

        }

        availableCredit = products.Account?.calc_available_credit ?: 0.0


        productAdapter = TakeOrderAdapter(
            requireContext(),
            ::onTotalChanged,
            ::onDeleteProduct,
            currency_symbol = currentPriceList?.Pricelist?.currency_symbol ?: "$"
        )
        binding.rvCustomers.adapter = productAdapter

        binding.tvCusomerName.text = "01 - Pending"
        binding.tvConfirm.visible()
        binding.tvSaveOrder.visible()
        binding.tvConfirm.text = getString(R.string.confirm)
        binding.llOrderOption.visible()
        binding.llViewPayment.gone()
        binding.tvPrepare.visible()
        binding.rvCustomers.gone()
        binding.tvEnd.alpha = 1.0f
        binding.tvAddProduct.alpha = 1.0f
        binding.tvAddProduct.text = getString(R.string.add_product)
        status = "01 - Pending"
        savedOrderList = null
        binding.viewFlipper.displayedChild = 0
        binding.tvBack.text = getString(R.string.take_order)


        if (isCompleted==true){
            binding.llOrderOption.gone()
            binding.tvPrepare.gone()
            binding.tvSaveOrder.gone()
        }

        initializeLabels()
    }

    private fun prepareList() {
        orderProductList = arrayListOf()

        products.ProductAssortment?.let {
            productList = it
            productAdapter = TakeOrderAdapter(
                requireContext(),
                ::onTotalChanged,
                ::onDeleteProduct,
                currency_symbol = currentPriceList?.Pricelist?.currency_symbol ?: "$"
            )
            binding.rvCustomers.adapter = productAdapter
        }
        productList.forEach { assortObj ->
            val priceListObj =
                currentPriceList?.Products?.find { it.Product?.integration_num == assortObj.Product?.integration_num }
            if (priceListObj != null) {

                assortObj.AccountProduct?.tax_total = priceListObj.PricelistProduct?.tax_total
                assortObj.AccountProduct?.net_price = priceListObj.PricelistProduct?.net_price
                assortObj.Product?.min_qty = priceListObj.Product?.min_qty
                assortObj.Product?.max_qty = priceListObj.Product?.max_qty
                assortObj.AccountProduct?.agreed_qty =
                    (priceListObj.Product?.min_qty ?: 1.0).toString()

                assortObj.Product?.order_qty = priceListObj.Product?.order_qty
                assortObj.Product?.step_qty = priceListObj.Product?.step_qty

                if ((assortObj.AccountProduct?.benchmark_price_disc_percent
                        ?: 0.0) >= (priceListObj.PricelistProduct?.disc_percent ?: 0.0)
                ) {
                    if (DateUtils.isValidDiscount(
                            assortObj.AccountProduct?.benchmark_price_disc_startdate,
                            assortObj.AccountProduct?.benchmark_price_disc_enddate
                        )
                    )
                        assortObj.AccountProduct?.apply_direct_disc =
                            assortObj.AccountProduct?.benchmark_price_disc_percent

                } else {
                    if (DateUtils.isValidDiscount(
                            priceListObj.PricelistProduct?.disc_startdt,
                            priceListObj.PricelistProduct?.disc_enddt
                        )
                    )
                        assortObj.AccountProduct?.apply_direct_disc =
                            priceListObj.PricelistProduct?.disc_percent
                }

                products.Promotions?.forEach { promoData ->
                    if (promoData.Tmplpromo?.lov_promotion_class == "Product Promotion") {
                        val sourceProduct =
                            promoData.SourceProducts?.find { it.Product?.integration_num == assortObj.Product?.integration_num }
                        if (sourceProduct != null) {
                            assortObj.PromoRules = promoData.PromoRules
                            assortObj.Tmplpromo = promoData.Tmplpromo
                        }
                    } else if (promoData.Tmplpromo?.lov_promotion_class == "Order Promotion") {
                        val sourceProduct =
                            promoData.TargetProducts?.find { it.Product?.integration_num == assortObj.Product?.integration_num }
                        if (sourceProduct != null) {
                            assortObj.PromoRules = promoData.PromoRules
                            assortObj.Tmplpromo = promoData.Tmplpromo
                            assortObj.SourceProducts = promoData.SourceProducts
                            assortObj.TargetProducts = promoData.TargetProducts
                        }
                    }
                }


                inventory?.Van?.Bins?.forEach { bin ->
                    bin.Inventory?.forEach { invBinproduct ->
                        if (invBinproduct.Product.id == assortObj.Product?.id) {
                            assortObj.InvbinsProduct = invBinproduct.InvbinsProduct
                        }
                    }
                }
                orderProductList.add(assortObj)
            }
        }

    }

    private fun initializeLabels() {
        binding.tvOrderNum.text = "${userRepository.getTeam()?.Team?.id}:${
            DateUtils.getCurrentDateWithFormat(DateFormat.DATE_FORMAT_YMDHMS)
        }"
        binding.tvCusCode.text = products?.Account?.name.toString()
        binding.tvCusName.text = products?.Account?.accountname.toString()
        binding.tvDeliveryDate.text = "N/A"
        binding.tvConfDate.text = "N/A"
        binding.tvCreditLimit.text = (products?.Account?.credit_limit ?: 0.0).toString()
        binding.tvAvailableCredit.text =
            (products?.Account?.calc_available_credit ?: 0.0).toString()
    }

    fun onTotalChanged(isInitialize: Boolean? = false) {
        if (status == "01 - Pending" || binding.tvAddProduct.text == getString(R.string.add)) {
            totalAmount = 0.0
            subTotalAmount = 0.0
            totalTax = 0.0
            totalDiscount = 0.0
            binding.tvSaveOrder.visible()
            orderProductList.forEach { product ->
                if ((product.AccountProduct?.sub_total ?: "0.0").toDouble() > 0.0) {
                    totalAmount += (product.AccountProduct?.total ?: "0.0").toDouble()
                    subTotalAmount += (product.AccountProduct?.sub_total ?: "0.0").toDouble()
                    val taxTotal = (product.AccountProduct?.tax_total ?: 0.0)

                    val netPrice = product.AccountProduct?.net_price ?: 0.0
                    val discPercent = product.AccountProduct?.apply_direct_disc ?: 0.0
                    val disAmnt = ((netPrice - taxTotal) * discPercent) / 100
//                    val tax= (((product.AccountProduct?.total ?: "0.0").toDouble()-(disAmnt))*taxTotal)/100

                    totalTax += taxTotal

                    totalDiscount += (disAmnt * (product.AccountProduct?.agreed_qty
                        ?: "1.0").toDouble())

                }
            }

            binding.tvTotal.text =
                String.format(
                    getString(R.string.cart_price_text),
                    currentPriceList?.Pricelist?.currency_symbol ?: "$",
                    totalAmount
                )
            binding.tvDueAmount.text =
                String.format(
                    getString(R.string.cart_price_text),
                    currentPriceList?.Pricelist?.currency_symbol ?: "$",
                    totalAmount
                )
            binding.tvSubTotal.text =
                String.format(
                    getString(R.string.cart_price_text),
                    currentPriceList?.Pricelist?.currency_symbol ?: "$",
                    subTotalAmount
                )
            binding.tvTax.text =
                String.format(
                    getString(R.string.cart_price_text),
                    currentPriceList?.Pricelist?.currency_symbol ?: "$",
                    totalTax
                )
            binding.tvDiscount.text =
                String.format(
                    getString(R.string.cart_price_text),
                    currentPriceList?.Pricelist?.currency_symbol ?: "$",
                    totalDiscount
                )

            if (totalAmount > 0.0) {
                this.totalAmount = totalAmount
            }
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

                                    if (isConnectedToInternet(requireContext(), false)) {
                                        viewModel.repriceOrderApi(
                                            requireContext(),
                                            integrationId
                                        )
                                    }
                                }
//                                "03 - Ordered" -> {
//                                    initializePaymentScreen()
//                                    binding.viewFlipper.displayedChild = 2
//                                }
                                "04 - Paid" -> {
                                    binding.tvConfirm.text = getString(R.string.deliver)
                                    binding.viewFlipper.displayedChild = 1
                                    binding.tvAddProduct.alpha = 0.4f
                                    binding.tvEnd.alpha = 0.4f
                                    binding.tvAddProduct.isClickable = false
                                    binding.tvEnd.isClickable = false
                                    binding.tvSaveOrder.gone()
                                }
                                "04 - Delivered" -> {
                                    binding.tvSaveOrder.gone()
                                    binding.llViewPayment.gone()
                                    if (calculateDue() <= 0.0) {
//                                        binding.viewFlipper.displayedChild = 1
//                                        binding.tvConfirm.text = getString(R.string.close)
//                                        binding.tvAddProduct.alpha = 0.4f
//                                        binding.tvEnd.alpha = 0.4f
//                                        binding.tvEnd.isClickable = false
//                                        binding.tvEnd.isClickable = false

                                        binding.tvPrint.visible()
                                        status = "05 - Closed"
                                        binding.tvSaveOrder.gone()
                                        updateOrderAPi("05 - Closed")
                                        binding.tvCusomerName.text = status
                                        saveOrderToDb(status)
                                        binding.tvPrepare.gone()
                                        binding.llViewPayment.visible()
                                        binding.llOrderOption.gone()
                                    } else {
                                        initializePaymentScreen()
                                        binding.viewFlipper.displayedChild = 1
                                        binding.tvAddProduct.alpha = 0.4f
                                        binding.tvEnd.alpha = 0.4f
                                        binding.tvEnd.isClickable = false
                                        binding.tvEnd.isClickable = false
                                    }
                                    viewModel.orderDetailApi(requireContext(), integrationId)
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
                                    binding.tvConfirm.text = getString(R.string.close)
                                    binding.tvAddProduct.alpha = 0.4f
                                    binding.tvEnd.alpha = 0.4f
                                    binding.tvEnd.isClickable = false
                                    binding.tvEnd.isClickable = false
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

        viewModel.orderDetailApiResponse.setObserver(
            viewLifecycleOwner,
            androidx.lifecycle.Observer {
                it ?: return@Observer
                when (it.status) {
                    Status.LOADING -> progressDialog.setLoading(true)
                    Status.SUCCESS -> {
                        progressDialog.setLoading(false)
                        if (it.data != null) {
                            when (status) {
                                "04 - Delivered", "05 - Closed" -> {

                                    order?.let { orderListObject ->
                                        orderListObject.Order = it.data.Order
                                    }
                                    products.Order = it.data.Order
                                    products.CartProducts = it.data.CartProducts
                                    products.Promotions = it.data.Promotions
                                    setOrderDetail()
                                    saveOrderToDb(status)

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

        paymentViewModel.createPaymentResponse.observe(this, androidx.lifecycle.Observer {
            it ?: return@Observer
            when (it.status) {
                Status.LOADING -> progressDialog.setLoading(true)
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)
                    updatePaymentAdapter()
                    saveOrderToDb(status)
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

        paymentViewModel.revertPaymentResponse.observe(this, androidx.lifecycle.Observer {
            it ?: return@Observer
            when (it.status) {
                Status.LOADING -> progressDialog.setLoading(true)
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)


//                    createPaymentList.removeIf { it.isSelected == true }

                    createPaymentList.forEach {
                        if (it.isSelected == true) {
                            it.lov_payment_status = "Reverted"
                            it.isSelected = false
                        }
                    }
                    if (!createPaymentList.isNullOrEmpty() && binding.viewFlipper.displayedChild >= 2) {
                        binding.tvAddProduct.alpha = 0.4f
                        binding.tvEnd.alpha = 0.4f
                        binding.tvAddProduct.isClickable = false
                        binding.tvEnd.isClickable = false
                    } else {
                        binding.tvAddProduct.alpha = 1f
                        binding.tvEnd.alpha = 1f
                        binding.tvAddProduct.isClickable = true
                        binding.tvEnd.isClickable = true
                    }

                    paymentProfilesAdapter.addList(createPaymentList, -1, true)
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
                    binding.etAmount.setText(
                        String.format(
                            getString(R.string.cart_price_text),
                            "",
                            calculateDue()
                        )
                    )
                    val availcredit = if (availableCredit > 0) {
                        availableCredit - calculateDue()
                    } else {
                        0.0
                    }
                    binding.tvAvailableCredit.text = availcredit.toString()

                    if (availableCredit > calculateDue()) {
                        binding.tvAddNew.visible()
                        binding.tvConfirm.text = getString(R.string.paid)
                    } else if (calculateDue() > 0.0) {
                        binding.tvAddNew.visible()
                        binding.tvConfirm.text = getString(R.string.products)
//                        binding.tvPaid.gone()
                    } else {
                        binding.tvAddNew.gone()
                        binding.tvConfirm.text = getString(R.string.paid)
                        //                        binding.tvPaid.visible()
                    }
                    saveOrderToDb(status)
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


    private fun updatePaymentAdapter() {

        createPaymentList.add(createPaymentTemplate!!)

        paymentProfilesAdapter.addList(createPaymentList, -1, true)
        binding.viewFlipper.displayedChild = 2

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

        val availcredit = if (availableCredit > 0) {
            availableCredit - calculateDue()
        } else {
            0.0
        }
        binding.tvAvailableCredit.text = availcredit.toString()

        if (!createPaymentList.isNullOrEmpty() && binding.viewFlipper.displayedChild >= 2) {
            binding.tvAddProduct.alpha = 0.4f
            binding.tvEnd.alpha = 0.4f
            binding.tvAddProduct.isClickable = false
            binding.tvEnd.isClickable = false
        } else {
            binding.tvAddProduct.alpha = 1f
            binding.tvEnd.alpha = 1f
            binding.tvAddProduct.isClickable = true
            binding.tvEnd.isClickable = true
        }

        if (availableCredit > calculateDue()) {
            binding.tvAddNew.visible()
            binding.tvConfirm.text = getString(R.string.paid)
        } else if (calculateDue() > 0.0) {
            binding.tvAddNew.visible()
//            binding.tvConfirm.text = getString(R.string.pay)
//                        binding.tvPaid.gone()
        } else {
            binding.tvAddNew.gone()
            binding.tvConfirm.text = getString(R.string.paid)

            //                        binding.tvPaid.visible()
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
        saveOrderTemplate.ProductAssortment = products?.ProductAssortment
        saveOrderTemplate.Account = products?.Account
        saveOrderTemplate.Paymentprofiles = products?.Paymentprofiles
        saveOrderTemplate.Promotions = products?.Promotions
        saveOrderTemplate.lov_order_type = "Sales"

//        if (!createPaymentList.isNullOrEmpty()){
        var pendingPaymentList = arrayListOf<PendingPaymentTemplate>()
        createPaymentList.forEach { payment ->
            pendingPaymentList.add(
                PendingPaymentTemplate(Payment = payment)
            )
        }
        saveOrderTemplate.PendingPayments = pendingPaymentList
//        }else{
//            saveOrderTemplate.PendingPayments= products?.PendingPayments
//        }
//        if (status == "01 - Pending") {
        saveOrderTemplate.CartProducts = confirmOrderProductList
        saveOrderTemplate.ProductAssortment = orderProductList
//        } else {
//            saveOrderTemplate.CartProducts = confirmOrderProductList
//            saveOrderTemplate.ProductAssortment = orderProductList
//        }

        if (status == "04 - Paid" || status == "04 - Delivered") {
            saveOrderTemplate.Order?.total_due = calculateDue()
        }
        saveOrderTemplate.Order?.lov_order_status = status

        lifecycleScope.launchWhenCreated {
            dbOrderId = saveOrderDao.insert(saveOrderTemplate)
            setFragmentResult(ON_ORDER_STATUS_CHANGE, bundleOf())
        }
    }

    private fun setOrderData() {

        confirmOrderProductList = arrayListOf()
        products.CartProducts?.let {
            repriceProductAdapter = TakeOrderProductsAdapter(
                requireContext(),
                currentPriceList?.Pricelist?.currency_symbol ?: "$"
            )
            binding.rvProducts.adapter = repriceProductAdapter
            confirmOrderProductList.addAll(it)
            repriceProductAdapter.add(confirmOrderProductList)
        }
        products.let {
            totalAmount = it.Order?.total ?: 0.0

            binding.tvTotal.text =
                String.format(
                    getString(R.string.cart_price_text),
                    currentPriceList?.Pricelist?.currency_symbol ?: "$",
                    it.Order?.total
                )
            binding.tvDueAmount.text =
                String.format(
                    getString(R.string.cart_price_text),
                    currentPriceList?.Pricelist?.currency_symbol ?: "$",
                    it.Order?.total_due
                )

            binding.tvSubTotal.text =
                String.format(
                    getString(R.string.cart_price_text),
                    currentPriceList?.Pricelist?.currency_symbol ?: "$",
                    it.Order?.subtotal
                )
            binding.tvTax.text =
                String.format(
                    getString(R.string.cart_price_text),
                    currentPriceList?.Pricelist?.currency_symbol ?: "$",
                    it.Order?.tax_amt
                )
            binding.tvDiscount.text =
                String.format(
                    getString(R.string.cart_price_text),
                    currentPriceList?.Pricelist?.currency_symbol ?: "$",
                    it.Order?.total_discount
                )

//            binding.tvDueAmount.text =
//                String.format(getString(R.string.cart_price_text), currentPriceList?.Pricelist?.currency_symbol?:"$", calculateDue())
//            binding.tvCurrentDueAmount.text =
//                String.format(getString(R.string.cart_price_text), currentPriceList?.Pricelist?.currency_symbol?:"$", calculateDue())
//            binding.etAmount.setText(calculateDue().toString())
//            val availcredit = availableCredit - calculateDue()
//            binding.tvAvailableCredit.text = availcredit.toString()

        }

        binding.rvProducts.visible()

        binding.tvCusomerName.text = products?.Order?.lov_order_status
        binding.tvOrderNum.text = products?.Order?.name.toString()
        binding.tvCusName.text = products?.Account?.accountname.toString()
        binding.tvCusCode.text = products?.Account?.name.toString()
        if (products?.Order?.delivery_date.isNullOrEmpty()) {
            binding.tvDeliveryDate.text = "N/A"
        } else {
            binding.tvDeliveryDate.text = products?.Order?.delivery_date.toString().split(" ")[0]
        }

        if (products?.Order?.confirmation_date.isNullOrEmpty()) {
            binding.tvConfDate.text = "N/A"
        } else {
            binding.tvConfDate.text = products?.Order?.confirmation_date.toString().split(" ")[0]
        }

        binding.tvCreditLimit.text = (products?.Account?.credit_limit ?: 0.0).toString()
//        binding.tvAvailableCredit.text =
//            (products?.Account?.calc_available_credit ?: 0.0).toString()
//        val availcredit = (products?.Account?.calc_available_credit ?: 0.0) - calculateDue()
        binding.tvAvailableCredit.text =
            (products?.Account?.calc_available_credit ?: 0.0).toString()


        if (order?.Order?.lov_order_status != "01 - Pending" && status != "01 - Pending") {
            binding.tvAddProduct.text = getString(R.string.open)
            if (availableCredit > calculateDue()) {
                binding.tvConfirm.text = getString(R.string.deliver)
            } else {
                initializePaymentScreen()
                binding.viewFlipper.displayedChild = 2

//            if (calculateDue()>0.0){
                binding.tvConfirm.text = getString(R.string.pay)
//            }else{
//                binding.tvConfirm.text = getString(R.string.paid)
//                binding.viewFlipper.displayedChild=2
//            }
            }
        }


    }

    private fun setOrderDetail() {

        if (products?.Order?.delivery_date.isNullOrEmpty()) {
            binding.tvDeliveryDate.text = "N/A"
        } else {
            binding.tvDeliveryDate.text = products?.Order?.delivery_date.toString().split(" ")[0]
        }

        if (products?.Order?.confirmation_date.isNullOrEmpty()) {
            binding.tvConfDate.text = "N/A"
        } else {
            binding.tvConfDate.text = products?.Order?.confirmation_date.toString().split(" ")[0]
        }

        binding.tvCreditLimit.text = (products?.Account?.credit_limit ?: 0.0).toString()
//        binding.tvAvailableCredit.text =
//            (products?.Account?.calc_available_credit ?: 0.0).toString()
//        val availcredit = (products?.Account?.calc_available_credit ?: 0.0) - calculateDue()
        binding.tvAvailableCredit.text =
            (products?.Account?.calc_available_credit ?: 0.0).toString()

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
            integration_total = totalAmount
        )
        var productList = arrayListOf<CreateOrderProduct>()
        orderProductList.forEach {
            if ((it.AccountProduct?.agreed_qty ?: "0").toDouble() > 0.0) {
                productList.add(
                    CreateOrderProduct(
                        integration_total = (it.AccountProduct?.total ?: "0.0").toDouble(),
                        product_id = (it.Product?.id),
                        product_qty = (it.AccountProduct?.agreed_qty ?: "0.0").toDouble(),
                        lov_invbinproduct_status = "Good"
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
                setLocalOrder()
//                saveOrderToDb(status)
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
            orderProductTemplate.tax_amt = addedProduct.AccountProduct?.tax_total

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
//        totalAmount = 0.0

        integrationId = products?.Order?.integration_id
        val order = CreateOrderData(
            id = products?.Order?.id ?: null,
            account_id = products.Account?.id,
            integration_id = products?.Order?.integration_id,
            lov_order_status = status,
            integration_total = totalAmount
        )

        var productList = arrayListOf<CreateOrderProduct>()
//        if (status == "02 - Confirmed" || status == "01 - Pending") {
            orderProductList.forEach { cartProduct ->
                if ((cartProduct.Product?.order_qty ?: "0").toDouble() > 0.0) {
                    productList.add(
                        CreateOrderProduct(
                            integration_total = cartProduct.OrdersProduct?.total ?: 0.0,
                            product_id = (cartProduct.Product?.id),
                            product_qty = (cartProduct.AccountProduct?.agreed_qty ?: "0.0").toDouble(),
                            //cartProduct.Product?.order_qty?.toDouble() ?: 0.0,
                            lov_invbinproduct_status = "Good"
                        )
                    )
                }
            }
//        }

        orderList.add(
            CreateOrderTemplate(
                Order = order,
                products =
//                if ((order.lov_order_status == "02 - Confirmed")
//                    || (order.lov_order_status == "01 - Pending")
//                )
                    productList
//                else null
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


    private fun showAlertMessage() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.available_credit)
            .setMessage(R.string.available_credit_for_payment)
            .setPositiveButton(R.string.yes) { dialog, _ ->
                status = "04 - Delivered"
                updateOrderAPi("04 - Delivered")
                saveOrderToDb(status)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.no) { dialog, _ ->
                dialog.cancel()
            }
            .setCancelable(true)
            .show()
    }

    private fun initializePaymentScreen() {

        order?.let {
            totalAmount = it.Order?.total ?: 0.0
        }

        binding.rvPayments.visible()

        availableCredit = products?.Account?.calc_available_credit ?: 0.0
        binding.tvConfirm.text = getString(R.string.pay)
//        binding.tvAddProduct.text= getString(R.string.open)

        paymentProfilesAdapter = PaymentProfilesAdapter(
            requireContext(),
            ::OnItemClick,
            currentPriceList?.Pricelist?.currency_symbol ?: "$"
        )
        binding.rvPayments.adapter = paymentProfilesAdapter

        if (!order?.PendingPayments.isNullOrEmpty()) {
            createPaymentList = arrayListOf()

            order?.PendingPayments?.forEach {
                createPaymentList.add(it.Payment)
            }
            paymentProfilesAdapter.addList(createPaymentList!!, -1, true)
        }

        if (createPaymentList.isNotEmpty() && binding.viewFlipper.displayedChild >= 2) {
            binding.tvAddProduct.alpha = 0.4f
            binding.tvEnd.alpha = 0.4f
            binding.tvAddProduct.isClickable = false
            binding.tvEnd.isClickable = false
        } else {
            binding.tvAddProduct.alpha = 1f
            binding.tvEnd.alpha = 1f
            binding.tvAddProduct.isClickable = true
            binding.tvEnd.isClickable = true
        }

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
        val availcredit = if (availableCredit > 0) {
            availableCredit - calculateDue()
        } else {
            0.0
        }
        binding.tvAvailableCredit.text = availcredit.toString()
//            (products?.Account?.calc_available_credit ?: 0.0).toString()
    }

    private fun OnItemClick(pos: Int) {
        createPaymentList.mapIndexed { index, paymentProfileResponse ->
            if (index == pos) {
                paymentProfileResponse.isSelected = paymentProfileResponse.isSelected != true
            }
        }
        paymentProfilesAdapter.addList(createPaymentList, pos, true)
    }

    private fun calculateDue(): Double {
        val totalAmount = totalAmount
        var collectedAmount = 0.0

        createPaymentList.forEach { payment ->
            if (payment.lov_payment_status != "Reverted")
                collectedAmount += payment.amount ?: 0.0
        }

        val dueAmount = totalAmount - collectedAmount
        if (dueAmount < 0.05)
            return 0.0
        else
            return dueAmount
//        return totalAmount - collectedAmount
    }

}
