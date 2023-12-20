package com.salesrep.app.ui.payment.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.salesrep.app.R
import com.salesrep.app.data.models.OrderPaymentTemplate
import com.salesrep.app.databinding.ItemAddPaymentAdapterBinding
import com.salesrep.app.util.gone
import kotlinx.android.synthetic.main.item_add_payment_adapter.view.*
import kotlinx.android.synthetic.main.item_add_payment_adapter.view.tvAmount
import kotlinx.android.synthetic.main.item_add_payment_adapter.view.tvNum
import kotlinx.android.synthetic.main.item_add_payment_adapter.view.tvStatus
import kotlinx.android.synthetic.main.item_add_payment_adapter.view.tvType
import kotlinx.android.synthetic.main.item_payment_allocation.view.*


class AddedPaymentsAdapter(
    var context: Context,
    val currencySymbol: String,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var paymentMethodList = ArrayList<OrderPaymentTemplate>()

    inner class ViewHolderCard(itemView: ItemAddPaymentAdapterBinding) : RecyclerView.ViewHolder(itemView.root) {
        fun bind(payment: OrderPaymentTemplate) {
//            itemView.tvNum.text = "${1001+adapterPosition}"
            itemView.tvNum.text = payment.Payment?.integration_id
            itemView.tvType.text = if(payment.Payment?.payment_type.isNullOrEmpty()) payment.Payment?.lov_payment_method else payment.Payment?.payment_type

            itemView.tvAmount.text =  String.format(itemView.context.getString(R.string.cart_price_text),currencySymbol,payment.Payment?.amount)
            itemView.tvStatus.text =  "Used"
        //            itemView.chkNum.isChecked =  adapterPosition==selectedPos

//            itemView.chkNum.isChecked= payment.isSelected==true
//            if (isSelectable==true){
//                itemView.chkNum.visible()
//            }else{
//            itemView.chkNum.gone()
//            }

        }
    }

    override fun getItemCount(): Int {
        return paymentMethodList.size
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = paymentMethodList[position]
        when (holder) {
            is ViewHolderCard -> {
                holder.bind(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return ViewHolderCard(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.item_add_payment_adapter, parent, false
            )
        )
    }

    fun addList(list: ArrayList<OrderPaymentTemplate>) {
        this.paymentMethodList.clear()
        this.paymentMethodList.addAll(list)
        notifyDataSetChanged()
    }


//    fun notifyItemRemove(removepos: Int) {
//        this.paymentMethodList.removeAt(removepos)
//        notifyItemRemove(removepos)
//    }

}