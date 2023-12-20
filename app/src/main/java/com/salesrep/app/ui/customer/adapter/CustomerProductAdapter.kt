package com.salesrep.app.ui.customer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.salesrep.app.R
import com.salesrep.app.data.models.response.ProductAssortment
import com.salesrep.app.databinding.ItemCustomerProductBinding

class CustomerProductAdapter (val context: Context) :
    RecyclerView.Adapter<CustomerProductAdapter.ViewHolder>() {

    private var productList = ArrayList<ProductAssortment>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding: ItemCustomerProductBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_customer_product, parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(productList[position])

    }

    override fun getItemCount(): Int {
        return productList.size
    }

    fun notifyData(list: List<ProductAssortment>?) {
        this.productList.clear()
        list?.let {
            this.productList.addAll(it)
            notifyDataSetChanged()
        }
    }



    inner class ViewHolder(val binding: ItemCustomerProductBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: ProductAssortment) {
            binding.data= product
        }
    }

}