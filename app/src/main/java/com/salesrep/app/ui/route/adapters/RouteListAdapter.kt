package com.salesrep.app.ui.route.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.salesrep.app.R
import com.salesrep.app.data.models.response.Route

import kotlinx.android.synthetic.main.item_route_accounts.view.*


class RouteListAdapter (val context: Context, private val callback: (Route?) -> Unit) :
    RecyclerView.Adapter<RouteListAdapter.ViewHolder>() {

    private var routeList = ArrayList<Route>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_route_accounts, parent, false
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

    fun notifyData(list: List<Route>?) {
        this.routeList.clear()
        list?.let {
            this.routeList.addAll(it)
            notifyDataSetChanged()
        }
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(route: Route) {
            itemView.tvDay.text= "${route.Route?.name}"
            itemView.tvTotalCustm.text= "${route.Route?.accounts_qty} ${itemView.context.getString(R.string.customers)}"
        }
    }
}
