package com.salesrep.app.ui.inventory.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.salesrep.app.R
import com.salesrep.app.data.models.inventory.InvMovementTemplate
import com.salesrep.app.data.models.requests.UpdateProductData
import com.salesrep.app.data.models.response.OrderListObject
import com.salesrep.app.data.models.response.ProductAssortment
import com.salesrep.app.data.models.response.StatusModel
import com.salesrep.app.databinding.ItemInventoryBinding
import com.salesrep.app.databinding.ItemRouteStockBinding
import com.salesrep.app.ui.stock.ProductAdapter

class MovementsAdapter(
    val context: Context,
    val onClickItem: (InvMovementTemplate) -> Unit
) :
    RecyclerView.Adapter<MovementsAdapter.ViewHolder>() {

    private var movementList = listOf<InvMovementTemplate>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemInventoryBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_inventory, parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(movementList[position])

    }

    override fun getItemCount(): Int {
        return movementList.size
    }

    fun notifyData(list: List<InvMovementTemplate>?) {
        this.movementList= listOf()
        list?.let {
            this.movementList= it
            notifyDataSetChanged()
        }
    }


    inner class ViewHolder(val binding: ItemInventoryBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(movementTemplate: InvMovementTemplate) {
            binding.data = movementTemplate
            binding.tvCommitDate.text= (movementTemplate.Invmovement?.commit_date?:"").split(" ")[0]
            binding.tvDate.text= (movementTemplate.Invmovement?.created?:"").split(" ")[0]
            binding.root.setOnClickListener {
                onClickItem(movementTemplate)
            }
        }
    }
}
