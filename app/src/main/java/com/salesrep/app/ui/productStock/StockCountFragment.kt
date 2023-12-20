package com.salesrep.app.ui.productStock

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import androidx.core.app.ActivityCompat
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.print.PrintHelper
import com.github.fragivity.navigator
import com.github.fragivity.push
import com.salesrep.app.base.BaseFragment
import com.salesrep.app.R
import com.salesrep.app.dao.InventoryDao
import com.salesrep.app.data.models.inventory.GetTeamInventoryResponse
import com.salesrep.app.data.models.inventory.InvBinTemplate
import com.salesrep.app.data.models.inventory.StockProductData
import com.salesrep.app.data.models.inventory.StockProductQtyData
import com.salesrep.app.data.models.response.GetFormCatalogResponse
import com.salesrep.app.data.models.response.StatusModel
import com.salesrep.app.data.network.responseUtil.ApisRespHandler
import com.salesrep.app.data.network.responseUtil.Status
import com.salesrep.app.data.repos.UserRepository
import com.salesrep.app.databinding.FragmentStockCountBinding
import com.salesrep.app.ui.dialogs.ProgressDialog
import com.salesrep.app.ui.inventory.InventoryFragment
import com.salesrep.app.ui.inventory.InventoryViewModel
import com.salesrep.app.ui.productStock.adapters.BinSpinnerAdapter
import com.salesrep.app.ui.productStock.adapters.StockProductAdapter
import com.salesrep.app.ui.productStock.adapters.StockProductQtyAdapter
import com.salesrep.app.util.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.item_stock_print.view.*
import timber.log.Timber
import javax.inject.Inject


@AndroidEntryPoint
//@RuntimePermissions
class StockCountFragment : BaseFragment<FragmentStockCountBinding>() {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var inventoryDao: InventoryDao

    val GPS_PERMISSIONS_CODE = 1321
    val CONNECT_PERMISSIONS_CODE = 1322

    override val viewModel by viewModels<InventoryViewModel>()
    private lateinit var binding: FragmentStockCountBinding

    private var stockList: ArrayList<StockProductData>? = null
    private var productStatusList: List<StatusModel>? = null

    private lateinit var progressDialog: ProgressDialog

    private lateinit var adapter: StockProductAdapter

    override fun getLayoutResId(): Int {
        return R.layout.fragment_stock_count
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFragmentResultListener(AppRequestCode.UPDATE_MOVEMENT_LIST_REQUEST) { key, bundle ->
            initialize()
        }
    }

    override fun onCreateView(instance: Bundle?, binding: FragmentStockCountBinding) {
        this.binding = binding

        progressDialog = ProgressDialog(requireActivity())

        adapter = StockProductAdapter(requireContext())
        binding.rvInventory.adapter = adapter

        stockList = arrayListOf()

        val formCatalog = prefsManager.getObject(
            PrefsManager.APP_FORM_CATALOG,
            GetFormCatalogResponse::class.java
        )
//        val reasons= Gson().fromJson(gson,GetFormCatalogResponse::class.java)
        productStatusList = formCatalog?.ProductStatuses

        val stockProductQtyDataList = arrayListOf<StockProductQtyData>()

        productStatusList?.forEach {
            stockProductQtyDataList.add(
                StockProductQtyData(
                    title = it.value
                )
            )
        }
        binding.rvHeader.adapter =
            StockProductQtyAdapter(requireContext(), true, stockProductQtyDataList)

        binding.tvRepName.text = userRepository.getUser()?.name
        binding.tvTeamName.text = userRepository.getTeam()?.Team?.name

        initialize()
        listeners()
        bindObservers()

    }

    private fun listeners() {
        binding.tvBack.setOnClickListener {
            if (binding.viewFlipper.displayedChild==0) {
                navigator.popBackStack()
            }else{
                binding.viewFlipper.displayedChild=0
                binding.tvAddInventory.text= getString(R.string.create_product_movement)
            }
        }
        binding.tvCancelPrint.setOnClickListener {
            binding.viewFlipper.displayedChild=0
            binding.tvAddInventory.text= getString(R.string.create_product_movement)
            binding.tvCancelPrint.gone()
            binding.tvPrint.visible()
        }

        binding.llBin.setOnClickListener {
            binding.spinner.performClick()
        }

        binding.tvPrint.setOnClickListener {

            if (canConnect()) {

                if (stockList.isNullOrEmpty()) {

                } else {
                    setPrintData()
                    binding.viewFlipper.displayedChild=1
                    binding.tvAddInventory.text= getString(R.string.print)
                    binding.tvCancelPrint.visible()
                    binding.tvPrint.gone()
                   }
            }

//            getBluetoothWithPermissionCheck()
//            val printIntent= Intent(requireContext(),PrinterActivity::class.java)
//            requireActivity().startActivity(printIntent)

        }

        binding.tvAddInventory.setOnClickListener {
            if (binding.viewFlipper.displayedChild==0)
                navigator.push(InventoryFragment::class)
            else {
                val printHelper = PrintHelper(requireActivity())
                printHelper.setColorMode(PrintHelper.COLOR_MODE_COLOR);
                printHelper.setScaleMode(PrintHelper.SCALE_MODE_FIT);
                val totalHeight = binding.scrollView.getChildAt(0).height
                val totalWidth = binding.scrollView.getChildAt(0).width

                val screenImg = getBitmapFromView(binding.scrollView, totalHeight, totalWidth)

                printHelper.printBitmap("Stock -" + SystemClock.uptimeMillis(), screenImg);

            }
        }

        binding.tvBin.setOnClickListener {
            binding.spinner.performClick()
        }
    }

    private fun setPrintData() {
        binding.tvBinName.text= binding.tvBin.text.toString()
        binding.tvPrintTeamName.text= binding.tvTeamName.text.toString()
        binding.tvPrintVanName.text= binding.tvVanName.text.toString()
        binding.tvPrintRepName.text= binding.tvRepName.text.toString()
        binding.tvTotalProducts.text= binding.tvTotal.text.toString()
        binding.tvDateTime.text= DateUtils.getCurrentDateWithFormat(DateFormat.DATE_TIME_FORMAT)

        binding.llProducts.removeAllViews()
        stockList?.forEach {
            val child: View = layoutInflater.inflate(R.layout.item_stock_print, null)
            child.tvProductName.text= it.title
            child.tvTotalPros.text= it.total
            child.tvSerialNum.text= it.integration_num
            binding.llProducts.addView(child)
        }
    }


    private fun bindObservers() {
        viewModel.getTeamInventoryResponse.setObserver(viewLifecycleOwner, Observer {
            it ?: return@Observer
            when (it.status) {
                Status.LOADING -> {
                    progressDialog.setLoading(true)
                }
                Status.SUCCESS -> {
                    progressDialog.setLoading(false)

                    prefsManager.save(PrefsManager.TEAM_INVENTORY, it.data)

                    savetoDb(it.data)
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

    private fun savetoDb(inventory: GetTeamInventoryResponse?) {
        lifecycleScope.launchWhenCreated {
            inventoryDao.clearAll()
            inventory?.id = inventoryDao.insert(inventory)

            if (!inventory?.Van?.Bins.isNullOrEmpty()) {
                binding.tvVanName.text = inventory?.Van?.Invloc?.title
                setBinList(inventory?.Van?.Bins)
//                setAdapterList(inventory?.Van?.Bins?.get(0))
            }
        }
    }

    private fun initialize() {

        if (!isConnectedToInternet(requireContext(), false)) {
            lifecycleScope.launchWhenCreated {
                val inventory = inventoryDao.getAllInventory().lastOrNull()

                if (!inventory?.Van?.Bins.isNullOrEmpty()) {
                    binding.tvVanName.text = inventory?.Van?.Invloc?.title
                    setBinList(inventory?.Van?.Bins)
//                    setAdapterList(inventory?.Van?.Bins?.get(0))
                }
            }
        } else {
            viewModel.getTeamInventoryApi(requireContext())
        }
    }

    private fun setBinList(bins: ArrayList<InvBinTemplate>?) {

        val binAdapter = BinSpinnerAdapter(
            requireContext(),
            R.layout.item_field_selection_item,
            binding.tvBin.id,
            bins!!
        )

//            val qualityAdapter: ArrayAdapter<*> = ArrayAdapter(context, android.R.layout.simple_spinner_item,qltyList)
        binding.spinner.adapter = binAdapter
        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                binding.tvBin.text = bins[position].Invbin?.title
                setAdapterList(bins[position])
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }
    }

    private fun setAdapterList(bin: InvBinTemplate?) {
        stockList = arrayListOf()

        bin?.Inventory?.forEach { inventoryDataObject ->
            val existingIndex = stockList?.indexOfFirst { it.id == inventoryDataObject.Product.id }

            if (existingIndex == null || existingIndex < 0) {
                val stockProduct = StockProductData(
                    id = inventoryDataObject?.Product?.id,
                    title = inventoryDataObject?.Product?.title,
                    integration_num = inventoryDataObject?.Product?.integration_num,
                    name = inventoryDataObject?.Product?.name,
                    lov_product_uom = inventoryDataObject?.Product?.lov_product_uom
                )

                val stockProductQtyDataList = arrayListOf<StockProductQtyData>()
                productStatusList?.forEach { statusModel ->
                    if (statusModel.code == inventoryDataObject?.InvbinsProduct?.lov_invbinproduct_status) {
                        stockProductQtyDataList.add(
                            StockProductQtyData(
                                qty = inventoryDataObject?.InvbinsProduct?.qty,
                                title = statusModel?.value
                            )
                        )
                    } else {
                        stockProductQtyDataList.add(
                            StockProductQtyData(
                                qty = "0",
                                title = statusModel?.value
                            )
                        )
                    }
                }
                stockProduct?.qtyList = stockProductQtyDataList
                stockProduct.total = getTotal(stockProduct.qtyList ?: arrayListOf())
                stockList?.add(stockProduct)

            } else {
                val stockProduct = stockList!![existingIndex]

                val stockProductQtyDataList = stockProduct.qtyList
                val qtyIndex =
                    stockProductQtyDataList?.indexOfFirst { it.title == inventoryDataObject?.InvbinsProduct?.lov_invbinproduct_status }
                if (qtyIndex != null && qtyIndex >= 0) {
                    stockProductQtyDataList[qtyIndex] = StockProductQtyData(
                        qty = inventoryDataObject?.InvbinsProduct?.qty
                    )
                }

                stockProduct.qtyList = stockProductQtyDataList
                stockProduct.total = getTotal(stockProduct.qtyList ?: arrayListOf())
                stockList!![existingIndex] = stockProduct
            }
        }

        adapter.notifyData(
            stockList
        )

        var total = 0.0
        stockList?.forEach { stock ->
            total += (if (stock.total.isNullOrEmpty()) "0.0" else stock.total)!!.toDouble()
        }
        binding.tvTotal.text = String.format("%.1f", total)
    }

    private fun getTotal(qtyList: ArrayList<StockProductQtyData>): String? {
        var total = 0.0

        qtyList.forEach { total += (it.qty ?: "0.0").toDouble() }

        return String.format("%.1f", total)
    }

    /**
     * - API < S
     * - Check ACCESS_COARSE_LOCATION and ACCESS_FINE_LOCATION permissions
     * - API < O
     * - Check has GPS
     * - Check GPS enabled
     * - API >= S
     * - Check BLUETOOTH_SCAN permission
     * - Check BLUETOOTH_CONNECT permission
     * - Check Bluetooth enabled
     */

    private fun canConnect(): Boolean {
        Timber.d("canConnect called")
        val deniedPermissions: MutableList<String> = ArrayList()
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            if (!checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION))
                deniedPermissions.add(
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )

            if (!checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) deniedPermissions.add(
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            if (deniedPermissions.isEmpty()) {
                true
//                if (!MmcDeviceCapabilities.hasLocationGps() //check if the device has GPS
//                    || Build.VERSION.SDK_INT < Build.VERSION_CODES.O || MmcDeviceCapabilities.isGpsEnabled()
//                ) { //check if the GPS is enabled
//                    if (MmcDeviceCapabilities.bluetoothEnabled()) //check if bluetooth is enabled
//                        true else {
//                        requestEnableBluetooth() //method to request enable bluetooth
//                        false
//                    }
//                } else {
//                    Timber.d("Request enable GPS")
//                    requestEnableGps() //method to request enable GPS (improving devices scan)
//                    false
//                }
            } else {
                Timber.d("Request GPS permissions")
                requestRuntimePermissions(
                    getString(R.string.gps_permission_request),
                    getString(R.string.gps_permission_request_relational),
                    GPS_PERMISSIONS_CODE,
                    *deniedPermissions.toTypedArray()
                )
                false
            }
        } else { // Build.VERSION_CODES.S or later
//            if (!checkPermission(Manifest.permission.BLUETOOTH))
//                deniedPermissions.add(Manifest.permission.BLUETOOTH)
            if (!checkPermission(Manifest.permission.BLUETOOTH_SCAN))
                deniedPermissions.add(Manifest.permission.BLUETOOTH_SCAN)
//            if (!checkPermission(Manifest.permission.BLUETOOTH_ADMIN))
//                deniedPermissions.add(Manifest.permission.BLUETOOTH_ADMIN)
            if (!checkPermission(Manifest.permission.BLUETOOTH_CONNECT))
                deniedPermissions.add(
                    Manifest.permission.BLUETOOTH_CONNECT
                )
            if (deniedPermissions.isEmpty()) {
                true
//                if (MmcDeviceCapabilities.bluetoothEnabled()) //check if bluetooth is enabled
//                true else {
//                requestEnableBluetooth() //method to request enable bluetooth
//                false
            } else {
                Timber.d("Request bluetooth permissions")
                requestRuntimePermissions(
                    getString(R.string.bluetooth_permission_request),
                    getString(R.string.bluetooth_permission_request_relational),
                    CONNECT_PERMISSIONS_CODE,
                    *deniedPermissions.toTypedArray()
                )
                false
            }
        }
    }

    /**
     * This method checks if a runtime permission has been granted.
     * @param permission The permission to check.
     * @return `TRUE` if the permission has been granted, `FALSE` otherwise.
     */
    private fun checkPermission(permission: String): Boolean {
        return (ActivityCompat.checkSelfPermission(requireActivity(), permission)
                == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestRuntimePermissions(
        title: String,
        description: String,
        requestCode: Int,
        vararg permissions: String
    ) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                requireActivity(),
                permissions[0]
            )
        ) {
            val builder: AlertDialog.Builder = AlertDialog.Builder(requireActivity())
            builder
                .setTitle(title)
                .setMessage(description)
                .setCancelable(false)
                .setNegativeButton(android.R.string.no) { dialog, id -> }
                .setPositiveButton(android.R.string.ok) { dialog, id ->
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        permissions,
                        requestCode
                    )
                }
                .show()
//            showDialog(builder) //method to show a dialog
        } else ActivityCompat.requestPermissions(requireActivity(), permissions, requestCode)
    }

//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        onRequestPermissionsResult(requestCode, grantResults)
//    }
//
//    @NeedsPermission(Manifest.permission.BLUETOOTH)
//    fun getBluetooth() {
//        val printIntent= Intent(requireContext(),PrinterActivity::class.java)
//        requireActivity().startActivity(printIntent)
//    }
//
//    @OnShowRationale(Manifest.permission.BLUETOOTH)
//    fun showBluetoothRationale(request: PermissionRequest) {
//        PermissionUtils.showRationalDialog(requireContext(),R.string.bluetooth_permission, request)
//    }
//    @OnNeverAskAgain(Manifest.permission.BLUETOOTH)
//    fun onNeverAskAgainRationale() {
//        PermissionUtils.showAppSettingsDialog(requireContext(), R.string.bluetooth_permission)
//    }
//    @OnPermissionDenied(Manifest.permission.BLUETOOTH)
//    fun showDeniedForBluetooth() {
//        PermissionUtils.showAppSettingsDialog(requireContext(), R.string.bluetooth_permission)
//    }
//,Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH_SCAN


    public fun getBitmapFromView(view: View, totalHeight: Int, totalWidth: Int): Bitmap {

        val height = Math.min(800, totalHeight);
        val percent = (height.toFloat() / totalHeight.toFloat())

        val canvasBitmap: Bitmap = Bitmap.createBitmap(
            (totalWidth * percent).toInt(),
            (totalHeight * percent).toInt(),
            Bitmap.Config.ARGB_8888
        );
        val canvas = Canvas(canvasBitmap);

        val bgDrawable = view.getBackground();
        if (bgDrawable != null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);

        canvas.save();
        canvas.scale(percent, percent);
        view.draw(canvas);
        canvas.restore();

        return canvasBitmap;

    }


}