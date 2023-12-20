package com.salesrep.app.ui.productStock.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.salesrep.app.R
import com.salesrep.app.data.models.inventory.StockProductQtyData
import com.salesrep.app.databinding.ItemStockQtyHeaderBinding

class StockProductQtyAdapter (
    val context: Context,
    val isHeader: Boolean,
    var stockQtyList : ArrayList<StockProductQtyData>
) :
    RecyclerView.Adapter<StockProductQtyAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemStockQtyHeaderBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_stock_qty_header, parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(stockQtyList[position])

    }

    override fun getItemCount(): Int {
        return stockQtyList.size
    }


    inner class ViewHolder(val binding: ItemStockQtyHeaderBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(stockQty: StockProductQtyData) {
            if (isHeader){
                binding.tvValue.text=stockQty?.title
            }else{
                binding.tvValue.text=stockQty?.qty
            }
        }
    }
}