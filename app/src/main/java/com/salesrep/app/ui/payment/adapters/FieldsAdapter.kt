package com.salesrep.app.ui.payment.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.salesrep.app.R
import com.salesrep.app.data.models.FieldItem
import com.salesrep.app.ui.payment.viewHolders.FieldCardTypeViewHolder
import com.salesrep.app.ui.payment.viewHolders.FieldInputViewHolder
import com.salesrep.app.ui.payment.viewHolders.FieldSelectionViewHolder

class FieldsAdapter (
    val context: Activity?,
    var isEdit: Boolean
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    companion object {
        const val NUMBER_INPUT = 1
        const val DROP_DOWN = 2
        const val TEXT_INPUT = 3
        const val CARD_TYPE = 4
    }

    private var list = ArrayList<FieldItem>()
    private val inflater = LayoutInflater.from(context)

    /**
     * set different viewHolder for different items
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when (viewType) {
            NUMBER_INPUT -> {
                FieldInputViewHolder(
                    inflater.inflate(
                        R.layout.item_field_input_type,
                        parent,
                        false
                    )
                )
            }
            TEXT_INPUT -> {
                FieldInputViewHolder(
                    inflater.inflate(
                        R.layout.item_field_input_type,
                        parent,
                        false
                    )
                )
            }
            DROP_DOWN -> {
                FieldCardTypeViewHolder(
                    inflater.inflate(
                        R.layout.item_field_selection_type_card,
                        parent,
                        false
                    ), context!!
                )
            }
            CARD_TYPE ->
                FieldCardTypeViewHolder(
                    inflater.inflate(
                        R.layout.item_field_selection_type_card,
                        parent,
                        false
                    ), context!!
                )
            else -> {
                FieldInputViewHolder(
                    inflater.inflate(
                        R.layout.item_field_input_type,
                        parent,
                        false
                    )
                )
            }
        }
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = list[position]
        when (holder) {
            is FieldInputViewHolder -> {
                holder.bind(item)
            }
            is FieldCardTypeViewHolder -> {
                holder.bind(item)
            }
            is FieldSelectionViewHolder -> {
                holder.bind(item)
            }

        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (list[position].type) {
            "number" -> NUMBER_INPUT
            "cardnumber" -> NUMBER_INPUT
            "select" ->
                if (list[position].key == "card_type")
                    CARD_TYPE
                else
                    DROP_DOWN
            "password" -> TEXT_INPUT
            "username" -> TEXT_INPUT
            "string" -> TEXT_INPUT
            else -> TEXT_INPUT

        }
    }

    fun addHomeList(list: List<FieldItem>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    fun getList(): ArrayList<FieldItem> {
        return list
    }

}