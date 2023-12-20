package com.salesrep.app.ui.customer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.salesrep.app.R
import com.salesrep.app.data.models.response.Route
import kotlinx.android.synthetic.main.item_customer_route.view.*

class CustomerRouteAdapter (val context: Context) :
    RecyclerView.Adapter<CustomerRouteAdapter.ViewHolder>() {

    private var routeList = ArrayList<Route>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_customer_route, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(routeList[position])

    }

    override fun getItemCount(): Int {
        return routeList.size
    }

    fun notifyData(list: List<Route>?) {
        this.routeList.clear()
        list?.let {
            this.routeList.addAll(it)
            notifyDataSetChanged()
        }
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(route: Route) {
            itemView.tvCode.text= "${route.Route?.id}"
            itemView.tvName.text= "${route.Route?.title}"
            itemView.tvDelivery.text= "${route.Route?.visit_day_name}"

        }
    }
}
