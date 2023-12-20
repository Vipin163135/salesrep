package com.salesrep.app.ui.changeLanguage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.salesrep.app.R
import com.salesrep.app.data.appConfig.AvailableLang
import kotlinx.android.synthetic.main.item_choose_language.view.*

class ChooseLanguageAdapter (
private val callBack: onLanguageSelect
) :
RecyclerView.Adapter<ChooseLanguageAdapter.ViewHolder>() {
    private var adapterList: ArrayList<AvailableLang> = arrayListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_choose_language,
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
            rblanguage.text = langObj.name

            rblanguage.isChecked = langObj.isSelected

            itemView.rootView.setOnClickListener {
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