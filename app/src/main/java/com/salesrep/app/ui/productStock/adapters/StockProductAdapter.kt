package com.salesrep.app.ui.productStock.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.salesrep.app.R
import com.salesrep.app.data.models.inventory.StockProductData
import com.salesrep.app.databinding.ItemStockProductBinding

class StockProductAdapter (
    val context: Context) :
    RecyclerView.Adapter<StockProductAdapter.ViewHolder>() {

    private var movementList = listOf<StockProductData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemStockProductBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_stock_product, parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(movementList[position])
    }

    override fun getItemCount(): Int {
        return movementList.size
    }

    fun notifyData(list: List<StockProductData>?) {
        this.movementList= listOf()
        list?.let {
            this.movementList= it
            notifyDataSetChanged()
        }
    }


    inner class ViewHolder(val binding: ItemStockProductBinding) : RecyclerView.ViewHolder(binding.root) {
        val context = binding.root.context
        fun bind(movementTemplate: StockProductData) {
            binding.data = movementTemplate
            binding.tvTotal.text= movementTemplate.total
            binding.rvInventory.adapter= StockProductQtyAdapter(context,false,movementTemplate.qtyList ?: arrayListOf())
        }
    }
}