package com.salesrep.app.ui.stock

import android.os.Bundle
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import com.github.fragivity.navigator
import com.github.fragivity.pop
import com.github.fragivity.push
import com.google.gson.Gson
import com.salesrep.app.R
import com.salesrep.app.base.BaseFragment
import com.salesrep.app.data.models.ProductTemplate
import com.salesrep.app.data.models.response.GetFormCatalogResponse
import com.salesrep.app.data.models.response.GetRouteAccountResponse
import com.salesrep.app.data.models.response.ProductAssortment
import com.salesrep.app.data.models.response.TaskData
import com.salesrep.app.data.repos.UserRepository
import com.salesrep.app.databinding.FragmentRouteStockBinding

import com.salesrep.app.ui.dialogs.ProgressDialog
import com.salesrep.app.ui.home.HomeViewModel
import com.salesrep.app.util.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RouteStockFragment : BaseFragment<FragmentRouteStockBinding>() {

    @Inject
    lateinit var prefsManager: PrefsManager

    @Inject
    lateinit var userRepository: UserRepository

    private lateinit var binding: FragmentRouteStockBinding
    override val viewModel by viewModels<HomeViewModel>()
    private lateinit var progressDialog: ProgressDialog
    private lateinit var productList: ArrayList<ProductAssortment>

    private lateinit var productAdapter: ProductAdapter
    private val productLocations by lazy {
        prefsManager.getObject(
            PrefsManager.APP_FORM_CATALOG,
            GetFormCatalogResponse::class.java
        )?.ProductLocations
    }

    private val products by lazy {
        arguments?.getParcelable<GetRouteAccountResponse>(
            DataTransferKeys.KEY_PRODUCTS
        )
    }
    private val task by lazy {
        arguments?.getParcelable<TaskData>(
            DataTransferKeys.KEY_TASK_DATA
        )
    }

    override fun getLayoutResId(): Int {
        return R.layout.fragment_route_stock
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(AppRequestCode.SELECT_PRODUCT_REQUEST) { key, bundle ->
            if (bundle.get(DataTransferKeys.KEY_PRODUCTS) != null) {
                val product= bundle.getParcelable<ProductTemplate>(DataTransferKeys.KEY_PRODUCTS)
                val existIndex= productList.indexOfFirst { it.Product?.id== product?.id }

                if (existIndex<0) {
                    val list = arrayListOf<ProductAssortment>()
                    list.addAll(productList)
                    list.add(ProductAssortment(null, product))
                    productList = list
                    productAdapter.notifyData(productList)
                    binding.rvCustomers.visible()
                }
            }
        }
    }

    override fun onCreateView(
        instance: Bundle?,
        binding: FragmentRouteStockBinding
    ) {
        this.binding = binding

        progressDialog = ProgressDialog(requireActivity())
        binding.tvNoData.gone()
        initialize()
        listeners()
    }

    private fun listeners() {
        binding.tvBack.setOnClickListener {
            navigator.pop()
        }
        binding.tvPrepare.setOnClickListener {
            prepareList()
//            if (!productList.isNullOrEmpty()) {
//                productAdapter.notifyData(productList)
//                binding.tvPrepare.gone()
                binding.rvCustomers.visible()
//            }
        }
        binding.tvAddProduct.setOnClickListener {
            navigator.push(ChooseProductFragment::class)
        }
        binding.tvEnd.setOnClickListener {
            if (productAdapter.getUpdateProducts().isNullOrEmpty()){
                Toast.makeText(requireContext(),getString(R.string.please_add_quantity_and_location),Toast.LENGTH_SHORT).show()
            }else {
                val json= Gson().toJson(productAdapter.getUpdateProducts())
                setFragmentResult(AppRequestCode.SELECT_PRODUCT_LIST_REQUEST,
                    bundleOf(
                    Pair(DataTransferKeys.KEY_PRODUCTS, json))
                )
                navigator.pop()
            }
        }
    }

    private fun prepareList() {
        productList= arrayListOf()
        products?.ProductAssortment?.let {
            productList.addAll(it)
        }
        productAdapter.notifyData(productList)
    }

    private fun initialize() {
        productList = arrayListOf()
        productAdapter = ProductAdapter(requireContext(), productLocations)
        binding.rvCustomers.adapter = productAdapter

        binding.tvCusomerName.text= products?.Account?.name
        binding.tvCusomerNum.text= products?.Account?.accountname


        if (!task?.ActivityProducts.isNullOrEmpty()) {
            task?.ActivityProducts?.forEach { activityProducts ->
                val productTemplate= ProductTemplate(
                    integration_num = activityProducts.Product?.integration_num,
                    title = activityProducts.Product?.title,
                    lov_product_status = activityProducts.ActivityProduct?.lov_product_status,
                    lov_product_uom = activityProducts.ActivityProduct?.lov_product_uom,
                    location = activityProducts.ActivityProduct?.lov_product_location,
                    qty = (activityProducts.ActivityProduct?.product_qty?:0.0).toString(),
                    id = activityProducts.Product?.id
                )
                val productAssortment= ProductAssortment(Product =  productTemplate)
                productList.add(productAssortment)
            }

            productAdapter.notifyData(productList)
            binding.rvCustomers.visible()
        }


        val isCompleted = arguments?.getBoolean(DataTransferKeys.KEY_IS_COMPLETED, false)
        if (isCompleted == true) {
            binding.tvAddProduct.gone()
            binding.tvEnd.gone()
            binding.tvPrepare.gone()
            productAdapter.notifyEditable(true)
        }
    }

}