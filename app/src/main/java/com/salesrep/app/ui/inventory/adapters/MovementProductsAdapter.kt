package com.salesrep.app.ui.inventory.adapters

import android.R.attr.country
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.salesrep.app.R
import com.salesrep.app.data.models.inventory.InvMovementProductData
import com.salesrep.app.data.models.requests.FormCatalogRequest
import com.salesrep.app.data.models.requests.UpdateProductData
import com.salesrep.app.data.models.response.StatusModel
import com.salesrep.app.data.network.responseUtil.Status
import com.salesrep.app.databinding.ItemProductMovementBinding
import com.salesrep.app.ui.customer.adapter.VisitDaysAdapter
import com.salesrep.app.util.gone
import com.salesrep.app.util.hideKeyboard
import com.salesrep.app.util.visible


class MovementProductsAdapter(
    val context: Context,
    val onTotalChanged: () -> Unit,
    val onDeleteProduct: (Int) -> Unit,
    val statusList: List<StatusModel>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var productList = arrayListOf<InvMovementProductData>()
    var isEditable= true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding: ItemProductMovementBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_product_movement, parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder)
            holder.bind(productList[position],onTotalChanged, onDeleteProduct)

    }

    override fun getItemCount(): Int {
        return productList.size
    }

    fun notifyData(list: ArrayList<InvMovementProductData>?) {
        this.productList= arrayListOf()
        list?.let {
            this.productList= it
            notifyDataSetChanged()
        }
    }

    fun notifyClickable(isEditable:Boolean){
        this.isEditable= isEditable
        notifyDataSetChanged()
    }
    fun  getUpdateProducts(): ArrayList<InvMovementProductData>{
        return productList
    }


    inner class ViewHolder(val binding: ItemProductMovementBinding) : RecyclerView.ViewHolder(binding.root) {

        val context= binding.root.context
        fun bind(
            productAssortment: InvMovementProductData,
            onTotalChanged: () -> Unit,
            onDeleteProduct: (Int) -> Unit
        ) {
//            binding.data= productAssortment
//            binding.tvDiscount.text= "${productAssortment.InvmovementProduct?.apply_direct_disc ?:0.0}%"
            val spinnerList= arrayListOf<String>()
            spinnerList.add(context.getString(R.string.select))

            binding.tvName.text= productAssortment.Product?.title
            binding.tvSku.text= context.getString(R.string.sku_value , productAssortment.Product?.integration_num)
            binding.tvUom.text= context.getString(R.string.uom_value , productAssortment.Product?.lov_product_uom)

            if (isEditable){
                binding.tvCondition.isClickable= true
                binding.tvQuantity.isClickable=true
                binding.tvQuantity.isFocusable=true
                binding.ivPlus.isClickable=true
                binding.ivMinus.isClickable=true
                binding.ivMinus.alpha = 1f
                binding.ivPlus.alpha = 1f
                binding.ivDelete.visible()
            }else{
                binding.tvCondition.isClickable= false
                binding.tvQuantity.isClickable=false
                binding.tvQuantity.isFocusable=false
                binding.ivPlus.isClickable=false
                binding.ivMinus.isClickable=false
                binding.ivMinus.alpha = 0.4f
                binding.ivPlus.alpha = 0.4f
                binding.ivDelete.gone()
            }

            binding.ivDelete.setOnClickListener {
                onDeleteProduct(adapterPosition)
            }

            binding.tvCondition.setOnClickListener {
                binding.spinner.performClick()
            }

//            val qltyList= context.resources.getStringArray(R.array.product_quality)


            val qualityAdapter = VisitDaysAdapter(
                context,
                R.layout.item_field_selection_item,
                binding.tvCondition.id,
                statusList
            )

//            val qualityAdapter: ArrayAdapter<*> = ArrayAdapter(context, android.R.layout.simple_spinner_item,qltyList)
            binding.spinner.adapter = qualityAdapter
            binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                    binding.tvCondition.text = statusList[position].value
                    productAssortment.InvmovementProduct?.lov_invbinproduct_status= statusList[position].code
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }

            binding.tvQuantity.addTextChangedListener( object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (!p0.isNullOrEmpty()){

                        if ((p0.toString().toDouble()?:0.0)>0.0) {
                            productAssortment.InvmovementProduct?.qty= p0.toString()
                        }else{
                            binding.tvQuantity.setText("1.0")
                        }

                        onTotalChanged()
                    }else{
                        onTotalChanged()
                        }
                }

                override fun afterTextChanged(p0: Editable?) {
                }
            })

            binding.ivMinus.setOnClickListener {
                itemView.hideKeyboard()

                val orderQty =
                        ((productAssortment.InvmovementProduct?.qty?.toDouble() ?: 0.0) - (productAssortment.Product?.step_qty?:1.0))
                    productAssortment.InvmovementProduct?.qty = orderQty.toString()

                if (orderQty>0.0) {
                    binding.tvQuantity.setText(orderQty.toString())
                }else{
                    binding.tvQuantity.setText("1.0")
                }
//                    binding.ivMinus?.alpha = 1f

//                    binding.ivMinus.backgroundTintList= ColorStateList.valueOf(Color.parseColor("#BCBCBC"))

//                } else {
//                    binding.ivMinus?.alpha = 0.4f
//                    binding.ivMinus.backgroundTintList= ColorStateList.valueOf(Color.parseColor("#FF3D3D"))

                }
//            }

            binding.ivPlus.setOnClickListener {
                itemView.hideKeyboard()
//                binding.ivMinus?.alpha = 1f
//                if (productAssortment.InvmovementProduct?.qty?.toDouble() ?: 0.0 < (productAssortment.Product?.max_qty?:1000.0)) {
                    val orderQty =
                        (productAssortment.InvmovementProduct?.qty?.toDouble() ?: 0.0) + (productAssortment.Product?.step_qty?: 1.0)
                    binding.tvQuantity.setText(orderQty.toString())
                    productAssortment.InvmovementProduct?.qty = orderQty.toString()

//                    binding.ivPlus.alpha = 1f
//                    binding.ivPlus.backgroundTintList= ColorStateList.valueOf(Color.parseColor("#BCBCBC"))
//                } else {
//                    binding.ivPlus.alpha = 0.4f
//                    binding.ivPlus.backgroundTintList= ColorStateList.valueOf(Color.parseColor("#FF3D3D"))

//                    binding.tvMinMax.visible()
//                    binding.tvMinMax.text=context.getString(R.string.reached_max_qty)

//                }
            }
            binding.tvQuantity.setText(productAssortment.InvmovementProduct?.qty)

        }
    }

}

