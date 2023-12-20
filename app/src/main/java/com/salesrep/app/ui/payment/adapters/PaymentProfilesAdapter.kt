package com.salesrep.app.ui.payment.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.salesrep.app.R
import com.salesrep.app.data.models.CreatePaymentTemplate
import com.salesrep.app.databinding.ItemPaymentAllocationBinding
import com.salesrep.app.util.invisible
import com.salesrep.app.util.visible
import kotlinx.android.synthetic.main.item_payment_allocation.view.*


class PaymentProfilesAdapter(
    var context: Context,
     val onItemClick: (Int) -> Unit,
    val currency_symbol:String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var selectedPos = -1
    private var paymentMethodList = ArrayList<CreatePaymentTemplate>()
    var isSelectable: Boolean?= false

    inner class ViewHolderCard(itemView: ItemPaymentAllocationBinding) : RecyclerView.ViewHolder(itemView.root) {
        fun bind(payment: CreatePaymentTemplate) {
//            itemView.tvNum.text = "${1001+adapterPosition}"
            itemView.tvNum.text = payment.integration_id
            itemView.tvType.text = payment.payment_type
            itemView.tvStatus.text = payment.lov_payment_status
            itemView.tvAmount.text =  String.format(itemView.context.getString(R.string.cart_price_text),currency_symbol,payment.amount)
//            itemView.chkNum.isChecked =  adapterPosition==selectedPos

            itemView.chkNum.isChecked= payment.isSelected==true

            if (isSelectable==true){
                itemView.chkNum.visible()
            }else{
                itemView.chkNum.invisible()
            }
            itemView.rootView.setOnClickListener {
                if (payment.lov_payment_status!="Reverted" && isSelectable==true) {
                    onItemClick(adapterPosition)
                }
            }

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
                R.layout.item_payment_allocation, parent, false
            )
        )
    }

    fun addList(list: ArrayList<CreatePaymentTemplate>, selectedPos: Int, isSelectable:Boolean? ) {
        this.paymentMethodList.clear()
        this.paymentMethodList.addAll(list)
        this.selectedPos = selectedPos
        this.isSelectable= isSelectable
        notifyDataSetChanged()
    }

//    fun notifyItemRemove(removepos: Int) {
//        this.paymentMethodList.removeAt(removepos)
//        notifyItemRemove(removepos)
//    }

}