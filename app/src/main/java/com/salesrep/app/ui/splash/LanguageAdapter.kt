package com.salesrep.app.ui.splash

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.salesrep.app.R
import com.salesrep.app.data.appConfig.AvailableLang

import kotlinx.android.synthetic.main.item_language.view.*

class LanguageAdapter (
    private val callBack: onLanguageSelect
) :
    RecyclerView.Adapter<LanguageAdapter.ViewHolder>() {
    private var adapterList: ArrayList<AvailableLang> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_language,
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

        fun bind(langObj: AvailableLang) = with(itemView) {
            tvName.text = langObj.name

            if (langObj.isSelected){
                tvName.setBackgroundResource(R.drawable.background_blue_navy_curved_12dp)
            }else{
                tvName.setBackgroundResource(R.drawable.background_blue_light_curved_12dp)
            }

            tvName.setOnClickListener {
                callBack.onLanguageSelect(adapterPosition)
            }
        }
    }

    fun notifyAdapter( list: ArrayList<AvailableLang>){
        this.adapterList.clear()
        this.adapterList.addAll(list)
        notifyDataSetChanged()
    }

    interface onLanguageSelect{
        fun onLanguageSelect(position: Int)
    }
}