package com.salesrep.app.ui.stock

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.salesrep.app.R
import com.salesrep.app.data.models.ProductTemplate
import com.salesrep.app.data.models.response.GetTeamProductsResponse
import com.salesrep.app.databinding.ItemAddProductBinding

class ChooseProductAdapter(
    val context: Context,
    val onProductClick: (GetTeamProductsResponse?) -> Unit
) :
    RecyclerView.Adapter<ChooseProductAdapter.ViewHolder>() {

    private var productList = listOf<GetTeamProductsResponse>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemAddProductBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_add_product, parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(productList[position])
        holder.itemView.rootView.setOnClickListener {
            onProductClick(productList[position])
        }
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    fun notifyData(list: List<GetTeamProductsResponse>?) {
        this.productList= listOf()
        list?.let {
            this.productList= it
            notifyDataSetChanged()
        }
    }

    class ViewHolder(val binding: ItemAddProductBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(productAssortment: GetTeamProductsResponse) {
            binding.tvCode.text= productAssortment.Product?.integration_num
            binding.tvName.text= productAssortment.Product?.title
        }
    }
}
