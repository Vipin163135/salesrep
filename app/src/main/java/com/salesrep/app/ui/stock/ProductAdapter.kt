package com.salesrep.app.ui.stock

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.salesrep.app.R
import com.salesrep.app.data.models.requests.UpdateProductData
import com.salesrep.app.data.models.response.ProductAssortment
import com.salesrep.app.data.models.response.StatusModel
import com.salesrep.app.databinding.ItemRouteStockBinding

class ProductAdapter(
    val context: Context,
    val productLocations: List<StatusModel>?
) :
    RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    private var productList = listOf<ProductAssortment>()
    private var isCompleted= false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemRouteStockBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_route_stock, parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(productList[position],productLocations)

    }

    override fun getItemCount(): Int {
        return productList.size
    }

    fun notifyData(list: List<ProductAssortment>?) {
        this.productList= listOf()
        list?.let {
            this.productList= it
            notifyDataSetChanged()
        }
    }
    fun notifyEditable(isComplete: Boolean) {
        isCompleted= isComplete
        notifyDataSetChanged()
    }
    fun  getUpdateProducts(): ArrayList<UpdateProductData>{
        var updateProductList= arrayListOf<UpdateProductData>()
        productList.forEach {
            if(!(it.Product?.qty.isNullOrEmpty()) && !(it.Product?.location.isNullOrEmpty())){
                val obj= UpdateProductData(
                    lov_product_location = it.Product?.location,
                    lov_product_uom = it.Product?.lov_product_uom,
                    lov_product_status = it.Product?.lov_product_status,
                    product_id = it.Product?.id,
                    product_title = it.Product?.title,
                    product_integration_num = it.Product?.integration_num,
                    product_qty = it.Product?.qty?.toDouble()
                    )
                updateProductList.add(obj)
            }
        }
        return updateProductList
    }

    inner class ViewHolder(val binding: ItemRouteStockBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(productAssortment: ProductAssortment, productLocations: List<StatusModel>?) {
            binding.data= productAssortment
            val spinnerList= arrayListOf<String>()
            productLocations?.forEach {
                spinnerList.add(it.value)
            }
            if (isCompleted){
                binding.tvQuantity.isFocusable=false
                binding.tvQuantity.isClickable=false
            }else{
                binding.tvQuantity.isFocusable=true
                binding.tvQuantity.isClickable=true
            }
            binding.tvLocation.setOnClickListener {
                if (!isCompleted) {
                    binding.locSpinner.performClick()
                }
            }
            val spinnerAdapter = ArrayAdapter(binding.root.context, android.R.layout.simple_list_item_1, spinnerList.toList())

            binding.locSpinner.adapter= spinnerAdapter

            binding.locSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    when (position){
                        -1 ->{

                        }
                        else ->{
                            binding.tvLocation.text= spinnerList[position]
                            productAssortment.Product?.location= spinnerList[position]
                        }
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        }
    }
}

