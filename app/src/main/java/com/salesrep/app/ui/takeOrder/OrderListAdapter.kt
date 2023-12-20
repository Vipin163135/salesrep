package com.salesrep.app.ui.takeOrder

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.salesrep.app.R
import com.salesrep.app.data.models.response.OrderListObject
import com.salesrep.app.databinding.ItemOrdersBinding
import com.salesrep.app.util.gone
import com.salesrep.app.util.visible

class OrderListAdapter (
    val context: Context,
    val isReturn: Boolean?= false,
    val onClickOrder: (OrderListObject) -> Unit,
    val orderList : List<OrderListObject>,
    val currencySymbol: String,
) :
    RecyclerView.Adapter<OrderListAdapter.ViewHolder>() {

//    var orderList = listOf<OrderListObject>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemOrdersBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_orders, parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(orderList[position])
    }

    override fun getItemCount(): Int {
        return orderList.size
    }

//    fun notifyData(list: List<OrderListObject>?) {
//        this.orderList= listOf()
//        list?.let {
//            this.orderList= it
//            notifyDataSetChanged()
//        }
//    }

    
    
    inner class ViewHolder(val binding: ItemOrdersBinding) : RecyclerView.ViewHolder(binding.root) {

        val context= binding.root.context
        fun bind(
            order: OrderListObject
            ) {

            binding.tvOrderNum.text = order.Order?.name

            if(order.Order?.delivery_date.isNullOrEmpty()){
                binding.tvDeliveryDate.text = "N/A"
            }else {
                binding.tvDeliveryDate.text = order.Order?.delivery_date.toString().split(" ")[0]
            }

            if(order.Order?.confirmation_date.isNullOrEmpty()){
                binding.tvConfDate.text = "N/A"
            }else {
                binding.tvConfDate.text = order.Order?.confirmation_date.toString().split(" ")[0]
            }

            binding.tvStatus.text= order.Order?.lov_order_status
            binding.tvTotal.visible()
            binding.tvTotal.text = context.getString(
                R.string.cart_price_text,
                currencySymbol, (order.Order?.total ?: 0.0)
            )
            binding.tvDueAmount.text = context.getString(
                R.string.cart_price_text,
                currencySymbol, (order.Order?.total_due ?: 0.0)
            )

            if (isReturn == true){
                binding.llDueAmount.gone()
            }else{
                binding.llDueAmount.visible()
            }
            binding.root.setOnClickListener {
                onClickOrder(order)
            }
        }

    }
}
