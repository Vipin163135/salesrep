package com.salesrep.app.ui.notifications

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.salesrep.app.R
import com.salesrep.app.data.models.NotificationData
import com.salesrep.app.data.models.response.GetNotificationListResponse
import com.salesrep.app.databinding.ItemNotficationBinding
import com.salesrep.app.util.DateFormat
import com.salesrep.app.util.DateUtils
import com.salesrep.app.util.invisible
import com.salesrep.app.util.visible

class NotificationAdapter(
    val context: Context,
    var callback: NotificationClick,
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {
    var notificationsList = ArrayList<NotificationData>()

    override fun getItemCount(): Int = notificationsList.size

    inner class ViewHolder(var binding: ItemNotficationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(notification: NotificationData) {

            binding.tvMessage.text = notification.previewtext
            binding.tvTitle.text = notification.subject
            binding.tvTime.text = DateUtils.dateFormatChange(DateFormat.DATE_FORMAT_RENEW,DateFormat.DELIVERY_DATE_FORMAT,notification.created)
            if (notification.read_dt.isNullOrEmpty()){
                binding.tvTitle.setTextColor(context.resources.getColor(R.color.blue_004))
                binding.ivProfile.visible()
                binding.llNotification.setBackgroundColor(context.resources.getColor(R.color.grey_6))
            }else{
                binding.llNotification.setBackgroundColor(context.resources.getColor(R.color.white))
                binding.ivProfile.invisible()
                binding.tvTitle.setTextColor(context.resources.getColor(R.color.grey_6))
            }

//            binding.tvDescription.text = Html.fromHtml(promotion.description)
////            binding.tvDescription.text = promotion.description
//            loadImage(
//                itemView.context,
//                binding.ivImage,
//                promotion.icon,
//                promotion.icon,
//                getDrawable(itemView.context, R.drawable.ic_promotion)
//            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemNotficationBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_notfication, parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (notificationsList!= null && notificationsList[position]!=null) {
            holder.bind(notificationsList[position])
            holder.itemView.setOnClickListener {
                callback.onNotificationClick(position,notificationsList[position])
            }
        }
    }

    fun addList(list: List<GetNotificationListResponse>) {
        notificationsList.clear()
        list.forEach { notificationsList.add(it.Notification) }
//        productsList.addAll()
        notifyDataSetChanged()
    }

    interface NotificationClick{
        fun onNotificationClick(pos:Int,notificationData: NotificationData)
    }

}