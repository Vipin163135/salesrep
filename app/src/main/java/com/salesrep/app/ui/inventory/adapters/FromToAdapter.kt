package com.salesrep.app.ui.inventory.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.salesrep.app.R
import com.salesrep.app.data.models.inventory.InventoryTemplate
import com.salesrep.app.databinding.ItemFieldSelectionItemBinding
import com.salesrep.app.databinding.ItemInventoryBinding

class FromToAdapter (
    val context: Context,
    val onClickItem: (InventoryTemplate) -> Unit
) :
    RecyclerView.Adapter<FromToAdapter.ViewHolder>() {

    private var movementList = listOf<InventoryTemplate>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemFieldSelectionItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_field_selection_item, parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(movementList[position])

    }

    override fun getItemCount(): Int {
        return movementList.size
    }

    fun notifyData(list: List<InventoryTemplate>?) {
        this.movementList= listOf()
        list?.let {
            this.movementList= it
            notifyDataSetChanged()
        }
    }


    inner class ViewHolder(val binding: ItemFieldSelectionItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(movementTemplate: InventoryTemplate) {
            binding.text1.text= movementTemplate.Invloc?.title
            binding.root.setOnClickListener {
                onClickItem(movementTemplate)
            }
        }
    }
}
