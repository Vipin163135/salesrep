package com.salesrep.app.ui.customer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.salesrep.app.R
import com.salesrep.app.data.models.response.CartProduct
import com.salesrep.app.data.models.response.OrderListObject
import com.salesrep.app.data.models.response.ProductAssortment
import com.salesrep.app.databinding.ItemCustomerOrderBinding
import com.salesrep.app.databinding.ItemCustomerProductBinding
import com.salesrep.app.util.gone
import com.salesrep.app.util.visible

class CustomerOrderAdapter (val context: Context,
                            val onClickOrder: (OrderListObject) -> Unit,
                            val orderList : List<OrderListObject>
                            ) :
    RecyclerView.Adapter<CustomerOrderAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding: ItemCustomerOrderBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_customer_order, parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(orderList[position])

    }

    override fun getItemCount(): Int {
        return orderList.size
    }
//
//    fun notifyData(list: List<CartProduct>?) {
//        this.orderList.clear()
//        list?.let {
//            this.orderList.addAll(it)
//            notifyDataSetChanged()
//        }
//    }



    inner class ViewHolder(val binding: ItemCustomerOrderBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(order: OrderListObject) {

            binding.tvSku.text = order.Order?.name

            binding.tvDeliveryDate.text = order.Order?.delivery_date.toString().split(" ")[0]
            binding.tvStatus.text= order.Order?.lov_order_status
            binding.tvTotal.visible()
            binding.tvTotal.text = context.getString(
                R.string.cart_price_text,
                "$", (order.Order?.total ?: 0.0)
            )

            binding.root.setOnClickListener {
                onClickOrder(order)
            }

        }
    }

}