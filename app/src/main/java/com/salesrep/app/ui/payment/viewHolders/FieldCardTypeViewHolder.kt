package com.salesrep.app.ui.payment.viewHolders

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import com.salesrep.app.R
import com.salesrep.app.data.models.FieldItem
import com.salesrep.app.data.models.FieldOption
import com.salesrep.app.ui.payment.adapters.CardTypeAdapter
import com.salesrep.app.util.getDrawable
import com.salesrep.app.util.gone
import com.salesrep.app.util.loadImage
import com.salesrep.app.util.visible
import kotlinx.android.synthetic.main.item_field_selection_type_card.view.*

class FieldCardTypeViewHolder (itemView: View, var activity: Activity) :
    RecyclerView.ViewHolder(itemView),
    AdapterView.OnItemSelectedListener {


    private var spinnerSelection: CardTypeAdapter? = null
    val context: Context? = itemView.context
    private var valuesList = ArrayList<FieldOption>()

    init {
        spinnerSelection = CardTypeAdapter(activity, R.layout.item_field_selection_item,itemView.tvText.id,valuesList)
//        spinnerSelection?.addColors(valuesList)
//        spinnerSelection?.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        itemView.spinner?.adapter = spinnerSelection

        itemView.spinner?.onItemSelectedListener = this

        itemView.tvText?.setOnClickListener {
            itemView.spinner?.performClick()
        }
    }

    fun bind(item: FieldItem) = with(itemView) {
//        slug = slugSelected
        tvName?.text = item.value
        valuesList.clear()
        valuesList.addAll(item.options ?: arrayListOf())
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
        if (position !=-1){
            if (valuesList[position]?.icon!=null) {
                itemView.ivIcon.visible()
                loadImage(
                    itemView.context,
                    itemView.ivIcon,
                    valuesList[position].icon,
                    valuesList[position].icon,
                    getDrawable(itemView.context, R.drawable.card_gradiant)
                )
//                itemView.tvText?.setTextColor(Color.parseColor(valuesList[position]?.value))

            }else{
                itemView.ivIcon.gone()
            }
        }else{
            itemView.ivIcon.gone()
        }

        (0 until valuesList.size)
            .forEach { i ->
                valuesList[i].isSelected = i == position
            }
    }
}