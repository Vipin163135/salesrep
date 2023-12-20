package com.salesrep.app.ui.inventory

import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import com.github.fragivity.navigator
import com.github.fragivity.pop
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.salesrep.app.R
import com.salesrep.app.base.BaseFragment
import com.salesrep.app.data.models.inventory.InventoryDataObject
import com.salesrep.app.databinding.FragmentAddStockBinding
import com.salesrep.app.ui.home.HomeViewModel
import com.salesrep.app.ui.inventory.adapters.SelectProductAdapter
import com.salesrep.app.ui.stock.ChooseProductAdapter
import com.salesrep.app.util.*
import java.lang.reflect.Type
import javax.inject.Inject

class SelectProductFragment : BaseFragment<FragmentAddStockBinding>() {

    @Inject
    lateinit var prefsManager: PrefsManager

    private lateinit var binding: FragmentAddStockBinding
    override val viewModel by viewModels<HomeViewModel>()
    private var strSearch: String = ""

    private lateinit var productAdapter: SelectProductAdapter
    private  val productList by lazy { arguments?.getParcelableArrayList<InventoryDataObject>(DataTransferKeys.KEY_SELECTED_BIN_PRODUCTS) }
    private var searchProductList = arrayListOf<InventoryDataObject>()

    override fun getLayoutResId(): Int {
        return R.layout.fragment_add_stock
    }

    override fun onCreateView(
        instance: Bundle?,
        binding: FragmentAddStockBinding
    ) {
        this.binding = binding

        initialize()
        listeners()
    }

    private fun listeners() {
        binding.tvBack.setOnClickListener {
            navigator.pop()
        }

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
            strSearch = ""
            resetSearch()
            it?.hideKeyboard()
        }

        binding.etSearch.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH && !TextUtils.isEmpty(binding.etSearch.text)) {
                strSearch = binding.etSearch.text.toString()
                setSearchList(strSearch)
                v.hideKeyboard()
                return@OnEditorActionListener true
            }
            false
        })

    }

    private fun setSearchList(strSearch: String) {
        searchProductList.clear()
        productList?.forEach { product ->
            if ((product.Product.integration_num?.contains(strSearch, true) == true) ||
                (product.Product.title?.contains(strSearch, true) == true)
            ) {
                searchProductList.add(product)
            }
        }
        productAdapter.notifyData(searchProductList)
    }

    private fun resetSearch() {
        searchProductList.clear()
        searchProductList.addAll(productList!!)
        productAdapter.notifyData(searchProductList)

    }

    private fun initialize() {
//        val gson = prefsManager.getString(PrefsManager.TEAM_PRODUCTS, "")
//        if (!gson.isNullOrEmpty()) {
//
//            val listType: Type = object : TypeToken<ArrayList<InventoryDataObject?>?>() {}.type
//            productList = Gson().fromJson<ArrayList<InventoryDataObject>>(gson, listType)

            productAdapter = SelectProductAdapter(requireContext(), ::onProductClick)
            binding.rvCustomers.adapter = productAdapter
            resetSearch()
//        }


    }


    private fun onProductClick(product: InventoryDataObject?) {
        setFragmentResult(
            AppRequestCode.SELECT_PRODUCT_REQUEST,
            bundleOf(
                Pair(DataTransferKeys.KEY_PRODUCTS, product),
            )
        )
        navigator.pop()
    }
}