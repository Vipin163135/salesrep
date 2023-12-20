package com.salesrep.app.ui.payment.adapters

import android.content.Context
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.salesrep.app.R
import com.salesrep.app.data.models.response.OrderListObject
import com.salesrep.app.databinding.ItemPaymentOrderBinding


class PaymentOrderAdapter(
    val context: Context,
    val onClickOrder: (OrderListObject) -> Unit,
    val currencySymbol: String
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var orderList = listOf<OrderListObject>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding: ItemPaymentOrderBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_payment_order, parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder)
            holder.bind(orderList[position])
    }

    override fun getItemCount(): Int {
        return orderList.size
    }

    fun notifyData(list: ArrayList<OrderListObject>?) {
        this.orderList= listOf()
        list?.let {
            this.orderList= it
            notifyDataSetChanged()
        }
    }



    inner class ViewHolder(val binding: ItemPaymentOrderBinding) : RecyclerView.ViewHolder(binding.root) {

        val context= binding.root.context
        fun bind(
            order: OrderListObject
        ) {

//            binding.tvOrderNum.text = order.Order?.name
//            binding.tvTotal.visible()
            val content = SpannableString(order.Order?.name)
            content.setSpan(UnderlineSpan(), 0, content.length, 0)
            binding.tvOrderNum.text= content
//            textView.setText(content)

            binding.tvTotal.text = context.getString(
                R.string.cart_price_text,
                currencySymbol, (order.Order?.total ?: 0.0)
            )
            binding.tvDueAmount.text = context.getString(
                R.string.cart_price_text,
                currencySymbol, (order.Order?.total_due ?: 0.0)
            )
            binding.tvDate.text= order.Order?.lov_order_status

            binding.tvOrderNum.setOnClickListener {
                onClickOrder(order)
            }

//            binding.root.setOnClickListener {
//                onClickOrder(order)
//            }
        }
    }
}
