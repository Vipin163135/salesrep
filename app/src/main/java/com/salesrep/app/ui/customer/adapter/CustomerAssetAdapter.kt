package com.salesrep.app.ui.customer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.salesrep.app.R
import com.salesrep.app.data.models.AssetTemplate
import com.salesrep.app.databinding.ItemCustomerAssetBinding
import com.salesrep.app.databinding.ItemCustomerOrderBinding
import com.salesrep.app.util.visible

class CustomerAssetAdapter (val context: Context,
                            val assetList : List<AssetTemplate>) :
    RecyclerView.Adapter<CustomerAssetAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding: ItemCustomerAssetBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_customer_asset, parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(assetList[position])

    }

    override fun getItemCount(): Int {
        return assetList.size
    }
//
//    fun notifyData(list: List<CartProduct>?) {
//        this.assetList.clear()
//        list?.let {
//            this.assetList.addAll(it)
//            notifyDataSetChanged()
//        }
//    }



    inner class ViewHolder(val binding: ItemCustomerAssetBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(asset: AssetTemplate) {

            binding.tvName.text = asset.Product?.name

            binding.tvInstallDate.text = asset.Asset?.activationdate.toString().split(" ")[0]
            binding.tvTite.text= asset.Product?.title
            binding.tvStatus.text= asset.Asset?.lov_asset_status
            binding.tvSerialNum.text= asset.Asset?.serialnum


        }
    }

}