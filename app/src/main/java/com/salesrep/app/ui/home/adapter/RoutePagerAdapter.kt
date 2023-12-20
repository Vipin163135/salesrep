package com.salesrep.app.ui.home.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.salesrep.app.R
import com.salesrep.app.dao.RouteActivityDao
import com.salesrep.app.dao.SaveOrderDao
import com.salesrep.app.data.models.response.GetRouteAccountResponse
import com.salesrep.app.data.models.response.OrderListObject
import com.salesrep.app.ui.takeOrder.OrderListAdapter
import com.salesrep.app.util.DateFormat
import com.salesrep.app.util.DateUtils
import com.salesrep.app.util.RouteStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.layout_route_graph.view.*
import kotlinx.android.synthetic.main.layout_route_visit_progress.view.*
import kotlinx.android.synthetic.main.layout_routes_progress.view.*
import javax.inject.Inject

class RoutePagerAdapter(
    val context: Context,
    val routeList: ArrayList<GetRouteAccountResponse>,
    val routeName: String? = "",
    val fragment: Fragment,
    val savedOrderDao: SaveOrderDao
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var distance: Double?= 0.0
    var duration: Double?= 0.0


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            1-> RouteViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.layout_routes_progress, parent, false
                )
            )
            2-> VisitViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.layout_route_visit_progress, parent, false
                )
            )
            3-> GraphViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.layout_route_graph, parent, false
                )
            )
            else ->  RouteSaleViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.layout_route_sales, parent, false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is RouteViewHolder -> holder.bind(routeList,routeName)
            is VisitViewHolder -> holder.bind(routeList)
            is GraphViewHolder -> holder.bind(routeList,distance,duration)
            is RouteSaleViewHolder -> holder.bind(routeList)
        }
//        holder.setImage(sliderItems.get(position));
//        if (position == sliderItems.size()- 2){
//            viewPager2.post(runnable);
//        }
    }

    override fun getItemCount(): Int {
        return 4
    }

    override fun getItemViewType(position: Int): Int {
        return when(position){
            0->1
            1->2
            2->3
            else->4
        }
    }

    fun notifyGraph(pair: Pair<Double, Double>?) {
        this.distance= (pair?.first )?: 0.0
        this.duration=(pair?.second )?: 0.0
        notifyItemChanged(2)
    }

    class RouteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(routeList: ArrayList<GetRouteAccountResponse>, routeName: String?) {
            itemView.arcProgress.isShowText = false
            if (!routeList.isNullOrEmpty()) {
                val totalVisits = routeList.size
                var completedVisits = 0

                routeList.forEach {
                    if (it.Visit?.Activity?.lov_activity_status != RouteStatus.STATUS_IN_PROGRESS
                        && it.Visit?.Activity?.lov_activity_status != RouteStatus.STATUS_PENDING
                    ) {
                        completedVisits++
                    }
                }
                val progress = ((completedVisits * 100) / totalVisits)
                itemView.tvRouteName.text = routeName
                itemView.arcProgress.progress = progress.toFloat()
                itemView.tvMinProg.text = "${progress}%"
                itemView.arcProgress.isShowText = false
                itemView.tvProgress.text = "${completedVisits}/${totalVisits}"

//            itemView.arc_img.text= "1234"

            }
        }
    }
    class GraphViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(
            routeList: ArrayList<GetRouteAccountResponse>,
            distance: Double?,
            duration: Double?
        ) {

            if (!routeList.isNullOrEmpty()) {
                val series: LineGraphSeries<DataPoint> = LineGraphSeries()
                val completedRouteList = arrayListOf<GetRouteAccountResponse>()
//            var totalTime:Long= 0
                routeList.forEach { visit ->
                    if (visit.Visit?.Activity?.lov_activity_status == RouteStatus.STATUS_COMPLETED) {
                        completedRouteList.add(visit)
                    }
                }

                completedRouteList.forEachIndexed { index, visit ->
                    val timeTaken = DateUtils.getTimeDifferenceInMins(
                        DateFormat.DATE_FORMAT_RENEW,
                        visit.Visit?.Activity?.actual_startdate,
                        visit.Visit?.Activity?.actual_enddate
                    )

                    series.appendData(
                        DataPoint(
                            index.toDouble(),
                            timeTaken.toDouble()
                        ),
                        false,
                        completedRouteList.size,
                        true
                    )
//                totalTime+=timeTaken

                }

                itemView.tvDuration.text = "${
                    String.format(
                        "%.2f",
                        ( ((duration ?: 1.0).toDouble() / 60.0)/routeList.size)
                    )
                } ${itemView.context.getString(R.string.mins)}"
                itemView.tvDistance.text = "${
                    String.format(
                        "%.2f",
                        ((distance?:1.0).times(1.6)/routeList.size)
                    )
                } ${itemView.context.getString(R.string.km)}"
                itemView.tvSpeed.text = "${
                    String.format(
                        "%.2f",
                        ((distance?.times(1.6) ?: 1.0) / ((duration ?: 0).toDouble() / 3660))
                    )
                } ${itemView.context.getString(R.string.km_hr)}"
                itemView.graphView.addSeries(series)
            }
        }
    }

    class VisitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(routeList: ArrayList<GetRouteAccountResponse>) {
            if (!routeList.isNullOrEmpty()) {
                val totalVisits = routeList.size
                var completedVisits = 0

                routeList.forEach {
                    if (it.Visit?.Activity?.lov_activity_status == RouteStatus.STATUS_COMPLETED) {
                        completedVisits++
                    }
                }
                val progress = ((completedVisits * 100) / totalVisits)
//            itemView.arc_img.text= "${completedVisits}/${totalVisits} pos"
                itemView.arc_img.progress = progress.toFloat()

//            itemView.arc_img.text= "1234"
            }
        }
    }

   inner class RouteSaleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(routeList: ArrayList<GetRouteAccountResponse>) {
            val totalVisits= routeList.size

            if (totalVisits>0) {
                fragment.lifecycleScope.launchWhenCreated {

                    val orderList = savedOrderDao.getSalesOrders(
                        routeList[0].routeId,
                        "Sales"
                    )

                    var completedVisits = 0
                    routeList.forEach {
                        if (it.Visit?.Activity?.lov_activity_status == RouteStatus.STATUS_COMPLETED) {

                            var noOfSales = 0
                            orderList.forEach {order->
                                if (order.Account?.id== it.Account?.id){
                                    if (order.Order?.lov_order_status=="04 - Delivered" || order.Order?.lov_order_status=="05 - Closed"){
                                        noOfSales++
                                    }
                                }
                            }

                            if (noOfSales>=1) {
                                completedVisits++
                            }
                        }
                    }

                    val progress=((completedVisits * 100)/totalVisits)
                    itemView.arc_img.text= "${completedVisits}/${totalVisits} pos"
                    itemView.arc_img.progress= progress.toFloat()
                }

//            itemView.arc_img.text= "1234"
            }
        }
    }


}

