package com.salesrep.app.ui.inventory

import android.annotation.SuppressLint
import android.os.Bundle
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
import com.salesrep.app.R
import com.salesrep.app.base.BaseFragment
import com.salesrep.app.dao.ChangeStatusMovementDao
import com.salesrep.app.dao.CreateMovementDao
import com.salesrep.app.dao.InventoryDao
import com.salesrep.app.data.models.ProductTemplate
import com.salesrep.app.data.models.inventory.*
import com.salesrep.app.data.models.response.GetFormCatalogResponse
import com.salesrep.app.data.network.responseUtil.ApisRespHandler
import com.salesrep.app.data.network.responseUtil.Status
import com.salesrep.app.data.repos.UserRepository
import com.salesrep.app.databinding.FragmentProductMovementBinding
import com.salesrep.app.ui.dialogs.ProgressDialog
import com.salesrep.app.ui.inventory.adapters.MovementProductsAdapter
import com.salesrep.app.ui.stock.ChooseProductFragment
import com.salesrep.app.util.*
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@AndroidEntryPoint
class ProductMovementFragment : BaseFragment<FragmentProductMovementBinding>() {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var inventoryDao: InventoryDao

    @Inject
    lateinit var createMovementDao: CreateMovementDao

    @Inject
    lateinit var changeStatusMovementDao: ChangeStatusMovementDao

    private lateinit var binding: FragmentProductMovementBinding
    override val viewModel by viewModels<InventoryViewModel>()
    private lateinit var progressDialog: ProgressDialog
    private var productList: ArrayList<InvMovementProductData>? = null

    private var inventory: GetTeamInventoryResponse? = null
    private var myInventory: GetTeamInventoryResponse? = null
    private var fromLoc: InventoryTemplate? = null
    private var selectedBin: InvBinTemplate? = null
    private var toLoc: InventoryTemplate? = null
    private var isFromSelected = false
    private var isCreated = false

    private var dbOrderId: Long? = null
    private lateinit var productAdapter: MovementProductsAdapter


    private val isNew by lazy {
        arguments?.getBoolean(
            DataTransferKeys.KEY_IS_NEW, false
        ) ?: false
    }

    private var movementData: InvMovementTemplate? = null

    override fun getLayoutResId(): Int {
        return R.layout.fragment_product_movement
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFragmentResultListener(AppRequestCode.SELECT_PRODUCT_REQUEST) { key, bundle ->
            if (bundle.get(DataTransferKeys.KEY_PRODUCTS) != null) {
                val product =
                    bundle.getParcelable<InventoryDataObject>(DataTransferKeys.KEY_PRODUCTS)
                if (productList == null) {
                    productList = arrayListOf()
                }
                val isPresent = productList?.find { product?.Product?.id == it.Product?.id }
                if (isPresent == null) {
                    val invBinTemplate = product?.InvbinsProduct
                    if (invBinTemplate != null) {
                        invBinTemplate.qty = (product.Product.min_qty ?: 1.0).toString()
                    }
                    productList?.add(
                        InvMovementProductData(
                            product?.Product,
                            invBinTemplate ?: InvBinProductsData(
                                qty = (product?.Product?.min_qty ?: 1.0).toString(),
                                invbin_id = product?.InvbinsProduct?.invbin_id
                            ),
                            Manufacturer = product?.Manufacturer
                        )
                    )

//                } else {
//                    productList?.add(
//                        InvMovementProductData(
//                            product?.Product,
//                            InvBinProductsData())
//                    )
//                }
                    productAdapter.notifyData(productList)
                }
            }
        }

        setFragmentResultListener(AppRequestCode.SELECT_MOVEMENT_TYPE_REQUEST) { key, bundle ->
            if (bundle.get(DataTransferKeys.SELECT_MOVEMENT_TYPE) != null) {
                val invTemplate =
                    bundle.getParcelable<InventoryTemplate>(DataTransferKeys.SELECT_MOVEMENT_TYPE)
                if (isFromSelected) {
                    if (toLoc == invTemplate) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.already_selected_to),
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        fromLoc = invTemplate
                        binding.tvFrom.text = fromLoc?.Invloc?.title
                        binding.tvFromType.text = fromLoc?.Invloc?.lov_invloc_type
                    }
                } else {
                    if (fromLoc == invTemplate) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.already_selected_from),
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        toLoc = invTemplate
                        binding.tvTo.text = toLoc?.Invloc?.title
                        binding.tvToType.text = toLoc?.Invloc?.lov_invloc_type
                    }
                }
            }
        }

        setFragmentResultListener(AppRequestCode.SELECT_MOVEMENT_BIN_REQUEST) { key, bundle ->
            if (bundle.get(DataTransferKeys.SELECT_MOVEMENT_BIN) != null) {
                val invBinTemplate =
                    bundle.getParcelable<InvBinTemplate>(DataTransferKeys.SELECT_MOVEMENT_BIN)

                selectedBin = invBinTemplate
                binding.tvVanBin.text = selectedBin?.Invbin?.title

            }
        }

    }

    override fun onCreateView(
        instance: Bundle?,
        binding: FragmentProductMovementBinding
    ) {
        this.binding = binding
        progressDialog = ProgressDialog(requireActivity())
        initialize()
        listeners()
        bindObservers()
    }


    /// ghghghgh
    private fun listeners() {
        binding.tvBack.setOnClickListener {
            navigator.pop()
        }

        binding.tvPrepare.setOnClickListener {
            if (binding.tvPrepare.text == getString(R.string.save)) {
                createMovementApi()
                onTotalChange()
            } else if (binding.tvPrepare.text == getString(R.string.prepare)) {
                if (!productList.isNullOrEmpty()) {
                    AlertDialogUtil.instance.createOkCancelDialog(
                        requireContext(),
                        R.string.alert,
                        R.string.prepare_alert,
                        R.string.ok,
                        R.string.cancel,
                        true,
                        object : AlertDialogUtil.OnOkCancelDialogListener {
                            override fun onOkButtonClicked() {
                                updateStatusMovementApi("Prepare")
//                                onTotalChange()
                            }

                            override fun onCancelButtonClicked() {

                            }

                        }
                    ).show()
                } else {
                    updateStatusMovementApi("Prepare")
//                onTotalChange()
                }
            }
        }

        binding.tvConfirm.setOnClickListener {
            if (productList.isNullOrEmpty()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.please_add_products),
                    Toast.LENGTH_LONG
                ).show()
            } else if (binding.tvConfirm.text == getString(R.string.confirm)) {
                var errorType = -1
                var errorMsg = ""

                errorType = isValidInventory()

                if (errorType >= 0) {
                } else {
                    updateMovementApi("Confirmed")
//                changeVanQty()
                    onTotalChange()
                }
            } else if (binding.tvConfirm.text == getString(R.string.commit)) {
                updateStatusMovementApi("Commited")
                changeVanQty()
                onTotalChange()
            }
        }

        binding.tvFrom.setOnClickListener {
            if (isNew && !isCreated) {
                isFromSelected = true
                setFromToList()
            }
        }
        binding.tvVanBin.setOnClickListener {
            if (isNew && !isCreated)
                setBinFromVan()
        }

        binding.tvTo.setOnClickListener {
            if (isNew && !isCreated) {
                isFromSelected = false
                setFromToList()
            }
        }

        binding.tvAddProduct.setOnClickListener {
            val binProductList = arrayListOf<InventoryDataObject>()

            if (fromLoc?.Invloc?.lov_invloc_type == "Van") {
                binProductList.addAll(selectedBin?.Inventory ?: arrayListOf())
            } else {
                if (selectedBin?.Invbin?.title == "DEFAULT") {
                    fromLoc?.Bins?.get(0)?.Inventory?.forEach { inventoryData ->
                        if ((inventoryData.Manufacturer?.alias == null) || (inventoryData.Manufacturer.alias == selectedBin?.Invbin?.title)) {
                            binProductList.add(inventoryData)
                        }
                    }
                } else {
                    fromLoc?.Bins?.get(0)?.Inventory?.forEach { inventoryData ->
                        if ((inventoryData.Manufacturer?.alias == selectedBin?.Invbin?.title)) {
                            binProductList.add(inventoryData)
                        }
                    }
                }
            }
            navigator.push(SelectProductFragment::class) {
                this.arguments =
                    bundleOf(Pair(DataTransferKeys.KEY_SELECTED_BIN_PRODUCTS, binProductList))
            }
        }

        binding.tvEnd.setOnClickListener {
            if (!movementData?.Invmovement?.lov_invmovement_status.isNullOrEmpty()) {
                updateStatusMovementApi("Cancel")
                onTotalChange()
            } else {
                navigator.pop()
            }
        }
    }

    private fun setBinFromVan() {
        if (fromLoc != null && toLoc != null) {
            var binList = arrayListOf<InvBinTemplate>()
            if (fromLoc?.Invloc?.lov_invloc_type == "Van") {
                binList.addAll(fromLoc?.Bins ?: arrayListOf())
            } else {
                binList.addAll(toLoc?.Bins ?: arrayListOf())
            }

            selectBinDialog(binList)
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.please_select_from_to),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun isValidInventory(): Int {
        var errorType = -1
        var errorMsg = ""

        if (fromLoc != null) {

            if (fromLoc?.Invloc?.lov_invloc_type == "Van") {
                productList?.forEach { product ->

                    val bin = myInventory?.Van?.Bins?.find { it.Invbin?.id == product.InvmovementProduct?.invbin_id }
//                        ?.find { it.Invbin?.id == product.InvmovementProduct?.invbin_id }

//                    fromLoc?.Bins?.forEach { bin ->
                    if (bin != null) {
                        val index =
                            bin.Inventory?.indexOfFirst { it.Product.id == product.Product?.id }
                        if (index != null && index > -1) {
                            if (bin.Inventory?.get(index)?.InvbinsProduct?.lov_invbinproduct_status != product.InvmovementProduct?.lov_invbinproduct_status) {
                                errorType = 1
                                errorMsg = getString(R.string.product_can_have_quality,
                                    product.Product?.title,
                                    bin.Inventory?.get(
                                        index
                                    )?.InvbinsProduct?.lov_invbinproduct_status
                                )

//                                "Product ${} can only have quality ${}"
                            } else if ((bin.Inventory?.get(index)?.InvbinsProduct?.qty
                                    ?: "0.0").toDouble() < (product.InvmovementProduct?.qty
                                    ?: "0.0").toDouble()
                            ) {
                                errorType = 2
                                getString(R.string.product_max_quality)
                                errorMsg =  getString(R.string.product_max_quality,
                                    product.Product?.title,
                                    bin.Inventory?.get(
                                        index
                                    )?.InvbinsProduct?.qty
                                )

                            }

                        } else {
                            errorMsg = getString(R.string.product_not_available_in_van,
                                fromLoc?.Invloc?.title)
                        }
                    } else {
                        errorType = 3
                        errorMsg = getString(R.string.bin_not_available,
                            fromLoc?.Invloc?.title)
                    }
                }
            }

        }

        if (errorType >= 0) {
            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
        }

        return errorType
    }


    private fun changeVanQty() {

//        val vanProducts = inventory?.Van?.Bins?.get(0)?.Inventory
        val binIndex =
            inventory?.Van?.Bins?.indexOfFirst { it.Invbin?.id == selectedBin?.Invbin?.id }

        productList?.forEach { productAssortment ->

            if (binIndex != null && binIndex > -1) {
                val vanProducts = inventory?.Van?.Bins!![binIndex].Inventory

                vanProducts?.forEach { inventoryDataObject ->

                    if ((productAssortment.Product?.id == inventoryDataObject.Product.id)
                        && (inventoryDataObject.InvbinsProduct.lov_invbinproduct_status == "Good")
                    ) {
                        var qty = (inventoryDataObject.InvbinsProduct.qty ?: "0.0").toDouble()
                        if (fromLoc?.Invloc?.lov_invloc_type == "Van") {
                            qty -= (productAssortment.InvmovementProduct?.qty ?: "0.0").toDouble()
                        } else {
                            qty += (productAssortment.InvmovementProduct?.qty ?: "0.0").toDouble()
                        }
                        inventoryDataObject.InvbinsProduct.qty = qty.toString()
                    }
                }
                inventory?.Van?.Bins?.get(binIndex)?.Inventory = vanProducts
            }
        }

//        if (inventory != null) {
//            lifecycleScope.launchWhenCreated {
//                inventoryDao.clearAll()
//                inventoryDao.insert(inventory)
//            }
//        }

    }


    private fun createMovementApi() {

        val createMovementData = InvMovementData()
        createMovementData.id = null
        createMovementData.integration_id = Calendar.getInstance().timeInMillis.toString()
        createMovementData.created = binding.tvCreated.text.toString()
        createMovementData.from_invlocid = fromLoc?.Invloc?.id
        createMovementData.to_invlocid = toLoc?.Invloc?.id
        createMovementData.lov_invmovement_type = "Standard"
        createMovementData.lov_invmovement_status = "Pending"
        createMovementData.name = binding.tvName.text.toString()

        var fromBinSelected = InvBinData()
        var toBinSelected = InvBinData()

        if (fromLoc?.Invloc?.lov_invloc_type == "Van") {
            createMovementData.from_invbinid = selectedBin?.Invbin?.id
            createMovementData.to_invbinid = toLoc?.Bins?.get(0)?.Invbin?.id
            fromBinSelected = selectedBin?.Invbin!!
            toBinSelected = toLoc?.Bins?.get(0)?.Invbin!!
        } else {
            createMovementData.from_invbinid = fromLoc?.Bins?.get(0)?.Invbin?.id
            createMovementData.to_invbinid = selectedBin?.Invbin?.id
            toBinSelected = selectedBin?.Invbin!!
            fromBinSelected = fromLoc?.Bins?.get(0)?.Invbin!!
        }
        binding.rvProducts.visible()

        productList = arrayListOf()
        productAdapter.notifyData(productList)

        this.movementData = InvMovementTemplate(
            createMovementData,
            fromLoc?.Invloc,
            fromBinSelected,
            toLoc?.Invloc,
            toBinSelected,
            productList
        )

        setMovementData(movementData)

        val movementListData = MovementListData(
            Movement = createMovementData,
            products = arrayListOf(),
            isUpdateApi = false
        )

        if (isConnectedToInternet(requireContext(), false)) {
            viewModel.createMovementApi(requireContext(), arrayListOf(movementListData))
        } else {

            lifecycleScope.launchWhenCreated {
                createMovementDao.insert(movementListData)
                isCreated = true
                binding.tvVanBin.isClickable = false
                binding.tvVanBin.isFocusable = false
                binding.tvFrom.isClickable = false
                binding.tvFrom.isFocusable = false
                binding.tvTo.isClickable = false
                binding.tvTo.isFocusable = false
            }
        }
    }

    private fun updateMovementApi(status: String) {

        val updateMovementData = InvMovementData()
        updateMovementData.id = movementData?.Invmovement?.id
        updateMovementData.integration_id = movementData?.Invmovement?.integration_id
        updateMovementData.lov_invmovement_type = "Standard"
        updateMovementData.lov_invmovement_status = status
        if (fromLoc != null) {
            updateMovementData.from_invlocid = fromLoc?.Invloc?.id
        }
        if (toLoc != null)
            updateMovementData.to_invlocid = toLoc?.Invloc?.id

        if (fromLoc?.Invloc?.lov_invloc_type == "Van") {
            updateMovementData.from_invbinid = selectedBin?.Invbin?.id
            updateMovementData.to_invbinid = toLoc?.Bins?.get(0)?.Invbin?.id
        } else {
            updateMovementData.from_invbinid = fromLoc?.Bins?.get(0)?.Invbin?.id
            updateMovementData.to_invbinid = selectedBin?.Invbin?.id
        }


        movementData?.Invmovement?.lov_invmovement_status = status

        setMovementData(movementData)

        val movementProductList = arrayListOf<MovementProduct>()
        productList?.forEach {
            movementProductList.add(
                MovementProduct(
                    product_id = it.Product!!.id!!,
                    lov_invbinproduct_status = it.InvmovementProduct?.lov_invbinproduct_status
                        ?: "Good",
                    qty = (it.InvmovementProduct?.qty ?: "0.0").toDouble()
                )
            )
        }

        val movementListData = MovementListData(
            Movement = updateMovementData,
            products = movementProductList,
            isUpdateApi = true
        )
        if (isConnectedToInternet(requireContext(), false)) {
            viewModel.updateMovementApi(requireContext(), arrayListOf(movementListData))
        } else {
            lifecycleScope.launchWhenCreated {
                createMovementDao.insert(movementListData)
            }
        }
    }

    private fun updateStatusMovementApi(status: String) {

        val updateMovementData = MovementCancelData()
        updateMovementData.objectId = movementData?.Invmovement?.id
        updateMovementData.integration_id = movementData?.Invmovement?.integration_id
        when (status) {
            "Prepare" -> {
                updateMovementData.type = "Delivery"

                if (isConnectedToInternet(requireContext(), false)) {
                    viewModel.prepareMovementApi(requireContext(), updateMovementData)
                }
            }
            "Commited" -> {
                this.movementData?.Invmovement?.lov_invmovement_status = "Commited"
                setMovementData(movementData)
                if (isConnectedToInternet(requireContext(), false)) {
                    viewModel.commitMovementApi(requireContext(), arrayListOf(updateMovementData))
                } else {
                    updateMovementData.movementStatus = "Commited"
                    lifecycleScope.launchWhenCreated {
                        changeStatusMovementDao.insert(updateMovementData)
                    }
                }
            }
            "Cancel" -> {
                this.movementData?.Invmovement?.lov_invmovement_status = "Cancelled"
                setMovementData(movementData)
                if (isConnectedToInternet(requireContext(), false)) {
                    viewModel.cancelMovementApi(requireContext(), arrayListOf(updateMovementData))
                } else {
                    updateMovementData.movementStatus = "Cancelled"
                    lifecycleScope.launchWhenCreated {
                        changeStatusMovementDao.insert(updateMovementData)
                    }
                }
            }
        }
    }

    private fun setFromToList() {
        val inventoryList = arrayListOf<InventoryTemplate>()
        inventory?.let {
            inventoryList.add(it.Van!!)
            inventoryList.addAll(it.Warehouses!!)
        }
        navigator.showDialog(
            FromToDialog::class,
            bundleOf(
                Pair(
                    DataTransferKeys.KEY_MOVEMENT_DATA,
                    inventoryList
                )
            )
        )
    }

    private fun selectBinDialog(binList: ArrayList<InvBinTemplate>) {
        navigator.showDialog(
            SelectBinDialog::class,
            bundleOf(
                Pair(
                    DataTransferKeys.KEY_MOVEMENT_DATA,
                    binList
                )
            )
        )
    }


    private fun initialize() {
        lifecycleScope.launchWhenCreated {

            myInventory = inventoryDao.getAllInventory().lastOrNull()

            inventory = inventoryDao.getAllInventory().lastOrNull()

            if (!isNew) {
                val inventoryList = arrayListOf<InventoryTemplate>()

                inventory?.let {
                    inventoryList.add(it.Van!!)
                    inventoryList.addAll(it.Warehouses!!)
                }
                inventoryList.forEach {
                    if (it.Invloc?.id == movementData?.Frominvloc?.id) {
                        fromLoc = it
                    }
                    if (it.Invloc?.id == movementData?.Toinvloc?.id) {
                        toLoc = it
                    }
                }

                if (fromLoc?.Invloc?.lov_invloc_type == "Van") {
//                    selectedBin = InvBinTemplate(movementData?.Frominvbin)
                    fromLoc?.Bins?.forEach {
                        if(it.Invbin?.id==movementData?.Frominvbin?.id){
                            selectedBin=it
                        }
                    }
                } else {
                    toLoc?.Bins?.forEach {
                        if(it.Invbin?.id==movementData?.Frominvbin?.id){
                            selectedBin=it
                        }
                    }
//                    selectedBin = InvBinTemplate(movementData?.Toinvbin)
                }



            } else {
            }

        }

        val team = userRepository?.getTeam()?.Team

        val formCatalog = prefsManager.getObject(
            PrefsManager.APP_FORM_CATALOG,
            GetFormCatalogResponse::class.java
        )
//        val reasons= Gson().fromJson(gson,GetFormCatalogResponse::class.java)
        val productStatus = formCatalog?.ProductStatuses

        productAdapter = MovementProductsAdapter(
            requireContext(),
            ::onTotalChange,
            ::onDeleteProduct,
            productStatus ?: listOf()
        )
        binding.rvProducts.adapter = productAdapter
        binding.tvTerritory.text = team?.name
        if (isNew) {
            binding.tvPrepare.text = getString(R.string.save)

            binding.tvVanBin.isClickable = true
            binding.tvVanBin.isFocusable = true
            binding.tvFrom.isClickable = true
            binding.tvFrom.isFocusable = true
            binding.tvTo.isClickable = true
            binding.tvTo.isFocusable = true

            binding.tvConfirm.gone()
            binding.tvEnd.gone()
            binding.tvAddProduct.gone()
            binding.rvProducts.gone()
            binding.tvCreated.text =
                DateUtils.getCurrentDateWithFormat(DateFormat.YEAR_MON_DATE_FORMAT)
            binding.tvName.text = "${userRepository.getTeam()?.Team?.id}:${
                DateUtils.getCurrentDateWithFormat(DateFormat.DATE_FORMAT_YMDHMS)
            }"
        } else {
            movementData = arguments?.getParcelable<InvMovementTemplate>(
                DataTransferKeys.KEY_MOVEMENT_DATA
            )
            binding.tvPrepare.text = getString(R.string.prepare)

            binding.tvVanBin.isClickable = false
            binding.tvVanBin.isFocusable = false
            binding.tvFrom.isClickable = false
            binding.tvFrom.isFocusable = false
            binding.tvTo.isClickable = false
            binding.tvTo.isFocusable = false

            binding.tvConfirm.visible()
            binding.tvEnd.visible()
            binding.tvAddProduct.visible()
            binding.rvProducts.visible()

            productList = movementData?.Products
            productAdapter.notifyData(productList)
            setMovementData(movementData)
        }
    }

    private fun setMovementData(movementData: InvMovementTemplate?) {
        binding.tvName.text = movementData?.Invmovement?.name
        binding.tvCreated.text = (movementData?.Invmovement?.created ?: "").split(" ")[0]
        binding.tvTo.text = movementData?.Toinvloc?.title
        binding.tvToType.text = movementData?.Toinvloc?.lov_invloc_type
        binding.tvFrom.text = movementData?.Frominvloc?.title
        binding.tvFromType.text = movementData?.Frominvloc?.lov_invloc_type
        binding.tvConfirmed.text = (movementData?.Invmovement?.commit_date ?: "").split(" ")[0]
        binding.tvStatus.text = movementData?.Invmovement?.lov_invmovement_status


        when (movementData?.Invmovement?.lov_invmovement_status) {
            "Pending" -> {
                if (!isConnectedToInternet(requireContext(), false)) {
                    binding.tvPrepare.gone()
                    binding.tvConfirm.visible()
                    binding.tvEnd.visible()
                    binding.tvAddProduct.visible()
                    binding.rvProducts.visible()
                    productAdapter.notifyClickable(true)
                } else {
                    binding.tvPrepare.text = "Prepare"
                    binding.tvPrepare.visible()
                    binding.tvConfirm.visible()
                    binding.tvEnd.visible()
                    binding.tvAddProduct.visible()
                    binding.rvProducts.visible()
                    productAdapter.notifyClickable(true)
                }
            }
            "Confirmed" -> {

                binding.tvConfirm.text = getString(R.string.commit)
                binding.tvPrepare.gone()
                binding.tvConfirm.visible()
                binding.tvEnd.visible()
                binding.tvAddProduct.gone()
                binding.rvProducts.visible()
                productAdapter.notifyClickable(false)

            }
            "Commited" -> {
                binding.tvConfirm.text = getString(R.string.commit)
                binding.tvPrepare.gone()
                binding.tvConfirm.gone()
                binding.tvEnd.gone()
                binding.tvAddProduct.gone()
                binding.rvProducts.visible()
                productAdapter.notifyClickable(false)
            }
            "Cancelled" -> {
                binding.tvConfirm.text = getString(R.string.commit)
                binding.tvPrepare.gone()
                binding.tvConfirm.gone()
                binding.tvEnd.gone()
                binding.tvAddProduct.gone()
                binding.rvProducts.visible()
                productAdapter.notifyClickable(false)
            }
        }

        if (movementData?.Frominvloc?.lov_invloc_type == "Van") {
            binding.tvVanBin.text = movementData.Frominvbin?.title
        } else
            binding.tvVanBin.text = movementData?.Toinvbin?.title

    }

    fun onDeleteProduct(position: Int) {
        showDeleteDialog(position)
    }

    override fun onStop() {
        super.onStop()
        setFragmentResult(AppRequestCode.UPDATE_MOVEMENT_LIST_REQUEST, bundleOf())
    }

    fun onTotalChange() {
        if (inventory != null) {
            val index =
                inventory?.Movements?.indexOfFirst { it.Invmovement?.integration_id == movementData?.Invmovement?.integration_id }

            if (index != null && index > -1) {
                inventory!!.Movements!![index] = movementData!!
                inventory!!.Movements!![index].Products = productAdapter.getUpdateProducts()
                lifecycleScope.launchWhenCreated {
                    inventoryDao.insert(inventory)

                }
            } else {
                movementData?.Products = productAdapter.getUpdateProducts()
                inventory?.Movements?.add(0, movementData!!)
                lifecycleScope.launchWhenCreated {
                    inventoryDao.insert(inventory)
                }
            }
        }
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
                    productList?.removeAt(position)
                    productAdapter.notifyData(productList)
                    onTotalChange()
                }

                override fun onCancelButtonClicked() {
                }
            })
        alertDialog.show()
    }

    @SuppressLint("FragmentLiveDataObserve")
    private fun bindObservers() {
        viewModel.createMovementResponse.setObserver(
            viewLifecycleOwner,
            androidx.lifecycle.Observer {
                it ?: return@Observer
                when (it.status) {
                    Status.LOADING -> progressDialog.setLoading(true)
                    Status.SUCCESS -> {
                        progressDialog.setLoading(false)
                        isCreated = true

                        binding.tvVanBin.isClickable = false
                        binding.tvVanBin.isFocusable = false
                        binding.tvFrom.isClickable = false
                        binding.tvFrom.isFocusable = false
                        binding.tvTo.isClickable = false
                        binding.tvTo.isFocusable = false
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

        viewModel.updateMovementResponse.setObserver(
            viewLifecycleOwner,
            androidx.lifecycle.Observer {
                it ?: return@Observer
                when (it.status) {
                    Status.LOADING -> progressDialog.setLoading(true)
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

        viewModel.prepareMovementResponse.setObserver(
            viewLifecycleOwner,
            androidx.lifecycle.Observer {
                it ?: return@Observer
                when (it.status) {
                    Status.LOADING -> progressDialog.setLoading(true)
                    Status.SUCCESS -> {
                        progressDialog.setLoading(false)
                        if (!it.data?.Products.isNullOrEmpty()) {
                            this.movementData = it.data
                            this.productList = it.data?.Products
                            productAdapter.notifyData(productList)
                            setMovementData(movementData)
                            binding.tvPrepare.gone()
                            onTotalChange()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.no_product_found),
                                Toast.LENGTH_SHORT
                            ).show()
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

        viewModel.cancelMovementResponse.setObserver(
            viewLifecycleOwner,
            androidx.lifecycle.Observer {
                it ?: return@Observer
                when (it.status) {
                    Status.LOADING -> progressDialog.setLoading(true)
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


        viewModel.commitMovementResponse.setObserver(
            viewLifecycleOwner,
            androidx.lifecycle.Observer {
                it ?: return@Observer
                when (it.status) {
                    Status.LOADING -> progressDialog.setLoading(true)
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


    }

}