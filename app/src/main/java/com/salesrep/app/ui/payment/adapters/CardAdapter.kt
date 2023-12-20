package com.salesrep.app.ui.payment.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.salesrep.app.R
import com.salesrep.app.data.models.Paymentprofiles
import com.salesrep.app.databinding.ItemCardBinding
import com.salesrep.app.databinding.ItemPaymentHeaderBinding
import com.salesrep.app.databinding.ItemPaymentOtherBinding
import com.salesrep.app.ui.payment.PaymentFragment
import com.salesrep.app.util.gone
import com.salesrep.app.util.loadImage
import com.salesrep.app.util.visible
import kotlinx.android.synthetic.main.item_card.view.*
import kotlinx.android.synthetic.main.item_payment_header.view.*
import kotlinx.android.synthetic.main.item_payment_other.view.*

class CardAdapter (
    var context: Context,
    var fragment: PaymentFragment,
    var isCheckVisible: Boolean = false
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var selectedPos= -1
    private var paymentMethodList = ArrayList<Paymentprofiles>()

    inner class ViewHolderCard(itemView: ItemCardBinding) : RecyclerView.ViewHolder(itemView.root){
        fun bind(payment: Paymentprofiles) {
            itemView.tvCardHolder.text= payment.Paymentprofile?.cardHolder
            itemView.tvCardNum.text= context.getString(R.string.card_num,payment.Paymentprofile?.cardNumber)
            itemView.tvCardExp.text= "${payment.Paymentprofile?.cardExpirationYear}/${payment.Paymentprofile?.cardExpirationMonth}"
            loadImage(
                itemView.context,
                itemView.cardImg,
                payment.Paymentprofile?.icon,
                payment.Paymentprofile?.icon,
                null
            )

            if(isCheckVisible) {
                itemView.checkCard.visible()
                itemView.checkCard.isChecked = adapterPosition==selectedPos

                itemView.rootView.setOnClickListener {
                    fragment.onClickCard(adapterPosition)
                }
            }else{
                itemView.checkCard.gone()
            }
            itemView.ivDeleteCard?.setOnClickListener {
                fragment.onDeleteCardClicked(adapterPosition)
            }
        }
    }

    inner class ViewHolderOther(itemView: ItemPaymentOtherBinding) : RecyclerView.ViewHolder(itemView.root){
        fun bind(payment: Paymentprofiles) {
            itemView.tvName.text= payment.Paymentprofile?.cardHolder
//            itemView.tvName.text= payment.Paymentprofile?.lov_paymentprofile_gateway
            loadImage(
                itemView.context,
                itemView.ivIcon,
                payment.Paymentprofile?.icon,
                payment.Paymentprofile?.icon,
                context.resources.getDrawable(R.drawable.ic_payments)
            )

            if(isCheckVisible) {
                itemView.chkPayment.visible()

                itemView.chkPayment.isChecked = adapterPosition==selectedPos

                itemView.rootView.setOnClickListener {
                    fragment.onClickCard(adapterPosition)
                }
            }else{
                itemView.chkPayment.gone()
            }

            itemView.ivDelete?.setOnClickListener {
                fragment.onDeleteCardClicked(adapterPosition)
            }
        }

    }

    inner class ViewHolderHeader(itemView: ItemPaymentHeaderBinding) : RecyclerView.ViewHolder(itemView.root){
        fun bind(payment: Paymentprofiles) {
            itemView.tvHeader.text= payment.title
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
            is ViewHolderOther -> {
                holder.bind(item)
            }
            is ViewHolderHeader -> {
                holder.bind(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType){
            1 -> {
                ViewHolderCard(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_card, parent, false
                    )
                )
            }
            2-> {
                ViewHolderOther(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_payment_other, parent, false
                    )
                )
            }
            else ->
                ViewHolderHeader(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.item_payment_header, parent, false
                    )
                )
        }
    }

    override fun getItemViewType(position: Int): Int {
        return paymentMethodList[position].type
    }

    fun addList(list: ArrayList<Paymentprofiles>, selectedPos:Int) {
        this.paymentMethodList.clear()
        this.paymentMethodList.addAll(list)
        this.selectedPos= selectedPos
        notifyDataSetChanged()
    }

//    fun notifyItemRemove(removepos: Int) {
//        this.paymentMethodList.removeAt(removepos)
//        notifyItemRemove(removepos)
//    }

}