package com.salesrep.app.ui.reconcilation.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.salesrep.app.R
import com.salesrep.app.data.models.response.GetTeamValueReconcilation
import com.salesrep.app.databinding.ItemStockProductBinding
import com.salesrep.app.databinding.ItemValueReconcilationBinding
import com.salesrep.app.ui.productStock.adapters.StockProductAdapter
import com.salesrep.app.ui.productStock.adapters.StockProductQtyAdapter

class ValueReconcilationAdapter (
    val context: Context
) :
    RecyclerView.Adapter<ValueReconcilationAdapter.ViewHolder>() {

    private var movementList = listOf<GetTeamValueReconcilation>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemValueReconcilationBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_value_reconcilation, parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(movementList[position])
    }

    override fun getItemCount(): Int {
        return movementList.size
    }

    fun notifyData(list: List<GetTeamValueReconcilation>?) {
        this.movementList= listOf()
        list?.let {
            this.movementList= it
            notifyDataSetChanged()
        }
    }


    inner class ViewHolder(val binding: ItemValueReconcilationBinding) : RecyclerView.ViewHolder(binding.root) {
        val context = binding.root.context
        fun bind(valueTemplate: GetTeamValueReconcilation) {
            binding.tvTotal.text = String.format("%.2f",valueTemplate.Record.amount?:0.0)
            binding.tvCurrency.text= valueTemplate.Record.currency
            binding.tvPaymentMethod.text= valueTemplate.Record.lov_payment_method
        }
    }
}