package com.salesrep.app.ui.takeOrder

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.salesrep.app.R
import com.salesrep.app.data.models.response.PromotionListObject
import com.salesrep.app.databinding.ItemProductPromotionsBinding

class TakeOrderProductPromoAdapter(val pricelistCurrencySymbol: String) : RecyclerView.Adapter<TakeOrderProductPromoAdapter.ViewHolder>() {
    var promotionsList = ArrayList<PromotionListObject>()

    override fun getItemCount(): Int = promotionsList.size

    inner class ViewHolder(var binding: ItemProductPromotionsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(promotion: PromotionListObject) {
//            val discountCalc= promotion.orderQty?.let { promotion.promo_given_amount?.div(it) }
//            binding.tvDiscount.text= String.format("${promotion.orderQty} X %.2f = $%.2f",discountCalc,promotion.promo_given_amount)
            val type = promotion.Tmplpromo?.lov_promo_delivery_type

            if (type == "Loyalty points") {
                binding.tvDiscountText.text= "${promotion.Tmplpromo?.promocode}"
                binding.tvDiscount.text =
                    "${promotion.Tmplpromo?.max_amount} ${promotion.Tmplpromo?.title}"
            } else {
                binding.tvDiscountText.text= "${promotion.Tmplpromo?.promocode}"
                binding.tvDiscount.text = "$pricelistCurrencySymbol"+String.format("%.2f", promotion.Tmplpromo?.max_amount)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemProductPromotionsBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_product_promotions, parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (promotionsList!= null && promotionsList[position]!=null) {
            holder.bind(promotionsList[position])
        }
    }

    fun addList(list: List<PromotionListObject>) {
        promotionsList  = arrayListOf()
        list.forEach { promotionsList.add(it) }
        notifyDataSetChanged()
    }
}