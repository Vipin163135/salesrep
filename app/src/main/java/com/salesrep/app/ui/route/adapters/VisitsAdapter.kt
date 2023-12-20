package com.salesrep.app.ui.route.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.salesrep.app.R
import com.salesrep.app.data.models.response.GetRouteAccountResponse
import com.salesrep.app.util.*
import kotlinx.android.synthetic.main.item_route.view.*

class VisitsAdapter(
    val context: Context,
    private val isEditable: Int,
    private val callback: (GetRouteAccountResponse?) -> Unit,
    private val statusCallback: (Int, GetRouteAccountResponse?) -> Unit
) :
    RecyclerView.Adapter<VisitsAdapter.ViewHolder>() {

    private var routeList = ArrayList<GetRouteAccountResponse>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_route, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(routeList[position])
        holder.itemView.rootView.setOnClickListener {
            callback(routeList[position])
        }
        holder.itemView.tvStart.setOnClickListener {
            Log.e("CurrentStatus: ", "${routeList[position].Visit?.Activity?.lov_activity_status}")
            if (isEditable == 1) {
                showAlertDialog(holder.itemView.context, R.string.message_alert, R.string.start_route_first)
            }else if (isEditable == 0) {

            }else {
                when (routeList[position].Visit?.Activity?.lov_activity_status) {
                    RouteStatus.STATUS_PENDING, RouteStatus.STATUS_SKIPPED -> {
                        routeList[position].Visit?.Activity?.lov_activity_status =
                            RouteStatus.STATUS_IN_PROGRESS
                        routeList[position].Visit?.Activity?.actual_startdate =
                            DateUtils.getCurrentDateWithFormat(DateFormat.DATE_FORMAT_RENEW)
                        statusCallback(position, routeList[position])
                    }
                    else -> {

                    }
                }
            }
//            else {
//                showAlertDialog(context, R.string.message_alert, R.string.start_route_first)
//            }
        }
        holder.itemView.tvSkip.setOnClickListener {
            Log.e("CurrentStatus: ", "${routeList[position].Visit?.Activity?.lov_activity_status}")
            if (isEditable == 1) {
                showAlertDialog(holder.itemView.context, R.string.message_alert, R.string.start_route_first)
            }else if (isEditable == 0) {

            }else {
                when (routeList[position].Visit?.Activity?.lov_activity_status) {
                    RouteStatus.STATUS_PENDING -> {
//                        if (routeList[position].Visit?.Activity?.required_flg == true ) {
//                            showAlertDialog(context, R.string.message_alert, R.string.cannot_skip)
//                        } else {
                            routeList[position].Visit?.Activity?.lov_activity_status =
                                RouteStatus.STATUS_SKIPPED
                            statusCallback(position, routeList[position])
                        routeList[position].Visit?.Activity?.lov_activity_status =
                            RouteStatus.STATUS_PENDING

//                        }
                    }
                    RouteStatus.STATUS_SKIPPED -> {
                        routeList[position].Visit?.Activity?.lov_activity_status =
                            RouteStatus.STATUS_NOT_STARTED
                        statusCallback(position, routeList[position])
                    }
                    else -> {

                    }
                }
            }
//            else {
//                showAlertDialog(context, R.string.message_alert, R.string.start_route_first)
//            }
        }
        holder.itemView.tvEnd.setOnClickListener {
            if (isEditable == 1) {
                showAlertDialog(holder.itemView.context, R.string.message_alert, R.string.start_route_first)
            }else if (isEditable == 0) {

            }else { when (routeList[position].Visit?.Activity?.lov_activity_status) {
                    RouteStatus.STATUS_IN_PROGRESS -> {
                        var isTaskRequired = false
                        routeList[position].Visit?.Tasks?.forEach {
                            if (it.Activity?.required_flg == true && ((it.Activity?.lov_activity_status== RouteStatus.STATUS_PENDING) || (it.Activity?.lov_activity_status== RouteStatus.STATUS_IN_PROGRESS) )) {
                                isTaskRequired = true
                            }
                        }

                        if (!isTaskRequired) {
                            routeList[position].Visit?.Activity?.lov_activity_status =
                                RouteStatus.STATUS_COMPLETED
                            routeList[position].Visit?.Activity?.actual_enddate =
                                DateUtils.getCurrentDateWithFormat(DateFormat.DATE_FORMAT_RENEW)
                            statusCallback(position, routeList[position])
                        } else {
                            showAlertDialog(
                                context,
                                R.string.message_alert,
                                R.string.complete_task_first
                            )

                        }
                    }
                    else -> {

                    }
                }
            }
//            else {
//                showAlertDialog(context, R.string.message_alert, R.string.start_route_first)
//
//            }
        }
    }


    override fun getItemCount(): Int {
        return routeList.size
    }

    fun notifyData(list: ArrayList<GetRouteAccountResponse>?) {
        this.routeList.clear()
        list?.let {
            this.routeList.addAll(it)
            notifyDataSetChanged()
        }
    }

    fun notifyDataItem(obj: GetRouteAccountResponse?, pos: Int) {
        obj?.let { this.routeList.set(pos, it) }
        notifyItemChanged(pos)
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(route: GetRouteAccountResponse) {
//            itemView.tvNum.text = "${adapterPosition + 1}"
            itemView.tvAstric.isVisible= route.Visit?.Activity?.required_flg == true
            itemView.tvName.text = "${route.Account?.accountname}"
            itemView.tvNum.text = "${route.Account?.orderseq}"

            when (route.Visit?.Activity?.lov_activity_status) {
                RouteStatus.STATUS_IN_PROGRESS -> {
                    itemView.tvEnd.visible()
                    itemView.tvSkip.gone()
                    itemView.tvStart.gone()
                }
                RouteStatus.STATUS_COMPLETED, RouteStatus.STATUS_NOT_STARTED, RouteStatus.STATUS_CANCELLED -> {
                    itemView.tvEnd.visible()
                    itemView.tvEnd.text = itemView.context.getString(R.string.ended)
                    itemView.tvSkip.gone()
                    itemView.tvStart.gone()
                }
                RouteStatus.STATUS_SKIPPED -> {
                    itemView.tvSkip.visible()
                    itemView.tvStart.visible()
                    itemView.tvEnd.gone()
                    itemView.tvStart.text = itemView.context.getString(R.string.start)
                    itemView.tvSkip.text = itemView.context.getString(R.string.not_started)
                }
                else -> {
                    itemView.tvStart.text = itemView.context.getString(R.string.start)
                    itemView.tvSkip.text = itemView.context.getString(R.string.skip)
                    itemView.tvEnd.gone()
                    itemView.tvSkip.visible()
                    itemView.tvStart.visible()

                }
            }


        }
    }
}



