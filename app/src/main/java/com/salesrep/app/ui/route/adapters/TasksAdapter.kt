package com.salesrep.app.ui.route.adapters

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.salesrep.app.R
import com.salesrep.app.data.models.ActivityTemplate
import com.salesrep.app.data.models.response.TaskData
import com.salesrep.app.util.*
import kotlinx.android.synthetic.main.item_route.view.*

class TasksAdapter(
    val context: Context,
    private var isEditable: Boolean,
    private val statusCallback: (Int, TaskData?) -> Unit,
    private val onTaskClick: (Int,TaskData?) -> Unit,
    val isRouteEditable: Int?=-1
) :
    RecyclerView.Adapter<TasksAdapter.ViewHolder>() {

    private var taskList = ArrayList<TaskData>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_route, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(taskList[position],isEditable,isRouteEditable?:-1,statusCallback,onTaskClick)
        holder.itemView.setOnClickListener {
            if (isEditable)
                onTaskClick(position,taskList[position])
        }
    }

    override fun getItemCount(): Int {
        return taskList.size
    }

    fun notifyEditable(isEditable: Boolean) {
        this.isEditable = isEditable
        notifyDataSetChanged()
    }

    fun notifyData(list: ArrayList<TaskData>?) {
        this.taskList.clear()
        list?.let {
            this.taskList.addAll(it)
            notifyDataSetChanged()
        }
    }

    fun notifyDataItem(obj: TaskData?, pos: Int) {
        obj?.let { this.taskList.set(pos, it) }
        notifyItemChanged(pos)
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(
            task: TaskData,
            isEditable: Boolean,
            isRouteEditable: Int,
            statusCallback: (Int, TaskData?) -> Unit,
            onTaskClick: (Int, TaskData?) -> Unit
        ) {
            itemView.tvNum.text = "${adapterPosition + 1}."
            itemView.tvAstric.isVisible = task.Activity?.required_flg == true
            itemView.tvName.text = "${task?.Activity?.title}"
            itemView.tvNum.setTextColor(Color.BLACK)
            itemView.tvName.setTextColor(Color.BLACK)

            when (task.Activity?.lov_activity_status) {
                RouteStatus.STATUS_IN_PROGRESS -> {
                    itemView.tvEnd.visible()
                    itemView.tvEnd.setTextColor(Color.WHITE)
                    itemView.tvEnd.backgroundTintList = itemView.context.resources.getColorStateList(R.color.red);
                    itemView.tvSkip.gone()
                    itemView.tvStart.gone()
                }
                RouteStatus.STATUS_COMPLETED-> {
                    itemView.tvEnd.visible()
                    itemView.tvEnd.setTextColor(Color.WHITE)
                    itemView.tvEnd.text = itemView.context.getString(R.string.ended)
                    itemView.tvEnd.backgroundTintList = itemView.context.resources.getColorStateList(R.color.red);
                    itemView.tvSkip.gone()
                    itemView.tvStart.gone()
                }

                RouteStatus.STATUS_SKIPPED, RouteStatus.STATUS_CANCELLED->{
                    itemView.tvEnd.visible()
                    itemView.tvEnd.text = itemView.context.getString(R.string.skipped)
                    itemView.tvEnd.setTextColor(itemView.context.resources.getColor(R.color.blue_004))
                    itemView.tvEnd.backgroundTintList = itemView.context.resources.getColorStateList(R.color.grey_7);
//                   setBackgroundResource(itemView.context.getDrawable(R.drawable.bg_light_grey_8dp))
                    itemView.tvSkip.gone()
                    itemView.tvStart.gone()
                }

//                RouteStatus.STATUS_SKIPPED -> {
//                    itemView.tvSkip.visible()
//                    itemView.tvStart.visible()
//                    itemView.tvEnd.gone()
//                    itemView.tvStart.text = itemView.context.getString(R.string.start)
//                    itemView.tvSkip.text = itemView.context.getString(R.string.not_started)
//                }
                else -> {
                    itemView.tvStart.text = itemView.context.getString(R.string.start)
                    itemView.tvSkip.text = itemView.context.getString(R.string.skip)
                    itemView.tvEnd.gone()
                    itemView.tvSkip.visible()
                    itemView.tvStart.visible()
                }
            }

            itemView.tvStart.setOnClickListener {
                Log.e("CurrentStatus: ", "${task.Activity?.lov_activity_status}")
                if (isRouteEditable == 1) {
                    showAlertDialog(itemView.context, R.string.message_alert, R.string.start_route_first)
                }else if (isRouteEditable == 0) {

                }else if (isEditable) {
                    when (task.Activity?.lov_activity_status) {
                        RouteStatus.STATUS_PENDING, RouteStatus.STATUS_SKIPPED -> {
                            task.Activity?.lov_activity_status =
                                RouteStatus.STATUS_IN_PROGRESS
                            task.Activity?.actual_startdate =
                                DateUtils.getCurrentDateWithFormat(DateFormat.DATE_FORMAT_RENEW)
                            statusCallback(position, task)
                            onTaskClick(adapterPosition,task)
                        }
                        else -> {

                        }
                    }
                } else {
                    showAlertDialog(itemView.context, R.string.message_alert, R.string.start_activity_first)
                }
            }
            itemView.tvSkip.setOnClickListener {
                Log.e("CurrentStatus: ", "${task.Activity?.lov_activity_status}")
                if (isRouteEditable == 1) {
                    showAlertDialog(itemView.context, R.string.message_alert, R.string.start_route_first)
                }else if (isRouteEditable == 0) {
//                    showAlertDialog(itemView.context, R.string.message_alert, R.string.start_route_first)
                }else  if (isEditable) {
                    when (task.Activity?.lov_activity_status) {
                        RouteStatus.STATUS_PENDING, RouteStatus.STATUS_NOT_STARTED -> {
//                        if (task?.Activity?.required_flg == true) {
//                            showAlertDialog(context, R.string.message_alert, R.string.cannot_skip)
//                        } else {
                            task.Activity?.lov_activity_status =
                                RouteStatus.STATUS_SKIPPED
                            statusCallback(position, task)
                            task.Activity?.lov_activity_status =
                                RouteStatus.STATUS_PENDING
//                        }
                        }
//                    RouteStatus.STATUS_SKIPPED -> {
//                        task.Activity?.lov_activity_status =
//                            RouteStatus.STATUS_NOT_STARTED
//                        statusCallback(position, task.Activity)
//                    }
                        else -> {

                        }
                    }
                } else {
                    showAlertDialog(itemView.context, R.string.message_alert, R.string.start_activity_first)
                }
            }
            itemView.tvEnd.setOnClickListener {
                if (isRouteEditable == 1) {
                    showAlertDialog(itemView.context, R.string.message_alert, R.string.start_route_first)
                }else if (isRouteEditable == 0) {
//                    showAlertDialog(itemView.context, R.string.message_alert, R.string.start_route_first)
                }else if (isEditable) {
                    when (task.Activity?.lov_activity_status) {
                        RouteStatus.STATUS_IN_PROGRESS -> {

                            task.Activity?.lov_activity_status =
                                RouteStatus.STATUS_COMPLETED
                            task.Activity?.actual_enddate =
                                DateUtils.getCurrentDateWithFormat(DateFormat.DATE_FORMAT_RENEW)
                            statusCallback(position, task)
                        }
                        else -> {
                        }
                    }
                } else {
                    showAlertDialog(itemView.context, R.string.message_alert, R.string.start_activity_first)
                }
            }
        }
    }
}


