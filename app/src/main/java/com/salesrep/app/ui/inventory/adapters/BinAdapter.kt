package com.salesrep.app.ui.inventory.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.salesrep.app.R
import com.salesrep.app.data.models.inventory.InvBinTemplate
import com.salesrep.app.databinding.ItemFieldSelectionItemBinding

class BinAdapter (
    val context: Context,
    val onClickItem: (InvBinTemplate) -> Unit
) :
    RecyclerView.Adapter<BinAdapter.ViewHolder>() {

    private var movementList = listOf<InvBinTemplate>()

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

    fun notifyData(list: List<InvBinTemplate>?) {
        this.movementList= listOf()
        list?.let {
            this.movementList= it
            notifyDataSetChanged()
        }
    }


    inner class ViewHolder(val binding: ItemFieldSelectionItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(movementTemplate: InvBinTemplate) {
            binding.text1.text= movementTemplate.Invbin?.title
            binding.root.setOnClickListener {
                onClickItem(movementTemplate)
            }
        }
    }
}
