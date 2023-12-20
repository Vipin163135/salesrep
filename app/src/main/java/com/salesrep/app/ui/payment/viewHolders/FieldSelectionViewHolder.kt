package com.salesrep.app.ui.payment.viewHolders

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.RecyclerView
import com.salesrep.app.data.models.FieldItem
import com.salesrep.app.data.models.FieldOption
import kotlinx.android.synthetic.main.item_field_selection_type.view.*

class FieldSelectionViewHolder (itemView: View, var activity: Activity, var slug: String? = null) :
    RecyclerView.ViewHolder(itemView),
    AdapterView.OnItemSelectedListener {


    private var spinnerSelection: ArrayAdapter<FieldOption>? = null
    val context: Context? = itemView.context
    private var valuesList = ArrayList<FieldOption>()

    init {
        spinnerSelection = ArrayAdapter(
            context as Context, android.R.layout.simple_spinner_item,
            valuesList
        )
        spinnerSelection?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        itemView.spinner?.adapter = spinnerSelection

        itemView.spinner?.onItemSelectedListener = this

        itemView.tvText?.setOnClickListener {
            itemView.spinner?.performClick()
        }
    }

    fun bind(item: FieldItem, slugSelected: String? = null) = with(itemView) {
        slug = slugSelected
        tvName?.text = item.value

        valuesList.addAll(item.options )
        val pos = valuesList.indexOfFirst { it.isSelected == true }
        if (pos != -1)
            spinner?.setSelection(pos)
        else
            spinner?.setSelection(0)
        spinnerSelection?.notifyDataSetChanged()

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        //do nothing
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        itemView.tvText?.text = valuesList[position].value

        (0 until valuesList.size)
            .forEach { i ->
                valuesList[i].isSelected = i == position
            }
    }

}