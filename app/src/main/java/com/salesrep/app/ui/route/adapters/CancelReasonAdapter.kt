package com.salesrep.app.ui.route.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.salesrep.app.R
import com.salesrep.app.data.models.response.RouteStatusReasonsModel
import kotlinx.android.synthetic.main.item_rejection_reason.view.*

class CancelReasonAdapter (
    private val callBack: onReasonSelectListener
) :
    RecyclerView.Adapter<CancelReasonAdapter.ViewHolder>() {
    private var adapterList: ArrayList<RouteStatusReasonsModel> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_rejection_reason,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(adapterList[position])
    }

    override fun getItemCount(): Int {
        return adapterList?.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(langObj: RouteStatusReasonsModel) = with(itemView) {
            itemView.rblanguage.text = langObj.value

            itemView.rblanguage.isChecked = langObj.isSelected==true

            itemView.rootView.setOnClickListener {
                callBack.onReasonSelect(adapterPosition)
            }
        }
    }

    fun notifyAdapter( list: List<RouteStatusReasonsModel>?){
        this.adapterList.clear()
        list?.let { this.adapterList.addAll(it) }
        notifyDataSetChanged()
    }

    interface onReasonSelectListener{
        fun onReasonSelect(position: Int)
    }

}