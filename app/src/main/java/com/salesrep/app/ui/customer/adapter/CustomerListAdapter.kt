package com.salesrep.app.ui.customer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.salesrep.app.R
import com.salesrep.app.data.models.response.CustomerListModel
import kotlinx.android.synthetic.main.item_customer.view.*

class CustomerListAdapter (val context: Context, private val callback: (CustomerListModel) -> Unit) :
    RecyclerView.Adapter<CustomerListAdapter.ViewHolder>() {

    private var routeList = ArrayList<CustomerListModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_customer, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(routeList[position])
        holder.itemView.rootView.setOnClickListener {
            callback(routeList[position])
        }
    }

    override fun getItemCount(): Int {
        return routeList.size
    }

    fun notifyData(list: List<CustomerListModel>?) {
        this.routeList.clear()
        list?.let {
            this.routeList.addAll(it)
            notifyDataSetChanged()
        }
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(route: CustomerListModel) {
            itemView.tvCode.text= "${route.Account?.customer_number?: ""}"
            itemView.tvName.text= "${route.Account?.accountname}"
            itemView.tvDelivery.text= "${route.Account?.delivery_day_name?: ""}"
            itemView.tvBalance.text= String.format("%.2f",route.Account?.calc_due_amount?: 0.0)

        }
    }
}
