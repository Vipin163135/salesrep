package com.salesrep.app.ui.payment.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.salesrep.app.R
import com.salesrep.app.data.models.PaymentMethodsTemplate
import com.salesrep.app.util.getDrawable
import com.salesrep.app.util.loadImage

class PaymentMethodsAdapter  (
    context: Context, resource: Int,
    textViewResourceId: Int,
    objects: ArrayList<PaymentMethodsTemplate>
) : ArrayAdapter<PaymentMethodsTemplate>(context, resource, textViewResourceId, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return rowview(position, convertView, parent)!!
    }

    override fun getDropDownView(
        position: Int,
        convertView: View?,
        parent: ViewGroup?
    ): View? {
        return rowview(position, convertView, parent)
    }


    private fun rowview(position: Int, convertView: View?, parent: ViewGroup?): View? {

        val rowItem: PaymentMethodsTemplate? = getItem(position)
        val holder: viewHolder
        var rowview = convertView
        if (rowview == null) {
            holder = viewHolder()
            val flater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            rowview = flater.inflate(R.layout.item_field_selection_item, null, false)
            holder.text1 = rowview.findViewById(android.R.id.text1)
            holder.ivIcon = rowview.findViewById(R.id.ivIcon)
            rowview.tag = holder
        } else {
            holder = rowview.tag as viewHolder
        }

        holder.text1?.text=(rowItem?.value)
        holder.ivIcon?.let {
            loadImage(
                context,
                it,
                rowItem?.icon,
                rowItem?.icon,
                getDrawable(context, R.drawable.card_gradiant)
            )
        }
        return rowview
    }

    private class viewHolder {
        var text1: TextView? = null
        var ivIcon: ImageView? = null
    }

}