package com.salesrep.app.ui.takeOrder

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.salesrep.app.R
import com.salesrep.app.data.models.PromoRule
import com.salesrep.app.data.models.Tmplpromo
import com.salesrep.app.data.models.requests.UpdateProductData
import com.salesrep.app.data.models.response.ProductAssortment
import com.salesrep.app.data.models.response.StatusModel
import com.salesrep.app.databinding.ItemProductsBinding
import com.salesrep.app.ui.customer.adapter.VisitDaysAdapter
import com.salesrep.app.util.DateUtils.isValidDiscount
import com.salesrep.app.util.gone
import com.salesrep.app.util.hideKeyboard
import com.salesrep.app.util.visible

class TakeOrderAdapter(
    val context: Context,
    val onTotalChanged: () -> Unit,
    val onDeleteProduct: (Int) -> Unit,
    val isReturn:Boolean?=false,
    val statusList: List<StatusModel>?=null,
    val currency_symbol:String
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var isInitialize:Boolean?=false

    var productList = listOf<ProductAssortment>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding: ItemProductsBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_products, parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder)
        holder.bind(productList[position], onTotalChanged,onDeleteProduct)

    }

    override fun getItemCount(): Int {
        return productList.size
    }

    fun notifyIsInitialize( isInitialize:Boolean?=false){
        this.isInitialize=isInitialize
    }

    fun notifyData(list: List<ProductAssortment>?) {
        this.productList= listOf()
        list?.let {
            this.productList= it
            notifyDataSetChanged()
        }
    }

    fun  getUpdateProducts(): ArrayList<UpdateProductData>{
        var updateProductList= arrayListOf<UpdateProductData>()
        productList.forEach {
            if(!(it.Product?.qty.isNullOrEmpty()) && !(it.Product?.location.isNullOrEmpty())){
                val obj= UpdateProductData(
                    lov_product_location = it.Product?.location,
                    lov_product_uom = it.Product?.lov_product_uom,
                    lov_product_status = it.Product?.lov_product_status,
                    product_id = it.Product?.id,
                    product_qty = it.Product?.qty?.toDouble()
                )
                updateProductList.add(obj)
            }
        }
        return updateProductList
    }

    fun notifyItem() {

    }

    inner class ViewHolder(val binding: ItemProductsBinding) : RecyclerView.ViewHolder(binding.root) {

        val context= binding.root.context
        fun bind(
            productAssortment: ProductAssortment,
            onTotalChanged: () -> Unit,
            onDeleteProduct: (Int) -> Unit
        ) {
//            binding.data= productAssortment
//            binding.tvDiscount.text= "${productAssortment.AccountProduct?.apply_direct_disc ?:0.0}%"
            val spinnerList= arrayListOf<String>()
            spinnerList.add(context.getString(R.string.select))

            binding.tvName.text= productAssortment.Product?.title
            binding.tvPriceDiscounted.text= context.getString(R.string.cart_price_text, currency_symbol,((productAssortment.AccountProduct?.net_price?:0.0)-(productAssortment.AccountProduct?.tax_total?:0.0)))
            binding.tvSku.text= context.getString(R.string.sku_value , productAssortment.Product?.integration_num)
            binding.tvUom.text= context.getString(R.string.uom_value , productAssortment.Product?.lov_product_uom)
            binding.tvCount.text = "x${productAssortment.AccountProduct?.agreed_qty} ${productAssortment.Product?.lov_product_uom}"

            binding.tvSubTotal.gone()
            binding.tvSubTotalTxt.gone()
            binding.tvTax.gone()
            binding.tvTaxTxt.gone()
            binding.tvTotalDiscount.gone()
            binding.tvTotalDiscountTxt.gone()

            val discount= String.format("%.2f",
                (productAssortment.AccountProduct?.apply_direct_disc?:0.0)
            )
            if (discount .toDouble() > 0.0) {
                binding.tvDiscount.visible()
                binding.tvDiscount.text = "-" + context.getString(
                    R.string.discount_price,
                    (productAssortment.AccountProduct?.apply_direct_disc ?:0.0)
                ) + "%"
            } else {
                binding.tvDiscount.gone()
            }

            binding.tvTotal.text= currency_symbol+productAssortment.AccountProduct?.total
            binding.ivDelete.setOnClickListener {
                it.hideKeyboard()
                onDeleteProduct(adapterPosition)
            }


            if (isReturn==true) {

                binding.rlCondition.visible()
                binding.tvCondition.setOnClickListener {
                    binding.spinner.performClick()
                }
                val qualityAdapter = VisitDaysAdapter(
                    context,
                    R.layout.item_field_selection_item,
                    binding.tvCondition.id,
                    statusList!!
                )

                binding.spinner.adapter = qualityAdapter
                binding.spinner.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            p0: AdapterView<*>?,
                            p1: View?,
                            position: Int,
                            p3: Long
                        ) {
                            binding.tvCondition.text = statusList[position].value
                            productAssortment.Product?.lov_invbinproduct_status =
                                statusList[position].code
                        }

                        override fun onNothingSelected(p0: AdapterView<*>?) {
                        }
                    }

            }else{
                binding.rlCondition.gone()
            }

            binding.tvQuantity.setOnFocusChangeListener { view, b ->
                if (b)
                    isInitialize=false
            }


            binding.tvQuantity.addTextChangedListener( object : TextWatcher{
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    if (!p0.isNullOrEmpty()){
                        val qty= p0.toString().toDouble()
                        val netPrice= productAssortment.AccountProduct?.net_price ?:0.0
                        val discPercent= productAssortment.AccountProduct?.apply_direct_disc ?:0.0
                        val taxTotal= (productAssortment.AccountProduct?.tax_total ?:0.0)
                        binding.tvCount.text = "x${String.format("%.1f",qty)} ${productAssortment?.Product?.lov_product_uom}"
                        productAssortment.AccountProduct?.agreed_qty = qty.toString()

//                        productAssortment.AccountProduct?.sub_total= qty*netPrice
                        productAssortment.AccountProduct?.sub_total=  String.format(binding.root.context.getString(R.string.decimal_2_format),
                        (qty*(netPrice-taxTotal))?:0.0)

                        val disAmnt= ((netPrice-taxTotal)*discPercent)/100
                        var total= ((qty*netPrice))-disAmnt

                        var promoDiscount=0.0

//                        val tax= ((total-(promoDiscount*qty))*taxTotal)/100

                        if (isInitialize==false) {
                            productAssortment.AccountProduct?.total = String.format(
                                binding.root.context.getString(R.string.decimal_2_format),
                                (total - (promoDiscount * qty)) ?: 0.0
                            )

//                        productAssortment.AccountProduct?.apply_promo_disc= (promoDiscount*qty)
//                        binding.tvSubTotal.text= productAssortment.AccountProduct?.sub_total
//                            String.format(binding.root.context.getString(R.string.decimal_2_format),
//                                productAssortment.AccountProduct?.sub_total ?: 0.0)
                            binding.tvTotal.text = currency_symbol + productAssortment.AccountProduct?.total
                        }else{
                            binding.tvTotal.text = currency_symbol + productAssortment.AccountProduct?.total
                        }
//                            String.format(binding.root.context.getString(R.string.decimal_2_format),
//                                productAssortment.AccountProduct?.total ?: 0.0)
                        if (qty >= (productAssortment.Product?.max_qty?:10000000.0)){
//                            binding.tvMinMax.visible()
//                            Toast.makeText(context,context.getString(R.string.reached_max_qty),Toast.LENGTH_SHORT).show()
//                            binding.tvMinMax.text=context.getString(R.string.reached_max_qty)
//                            binding.tvQuantity.setText((productAssortment.Product?.max_qty?:100.0).toString())
                        }else if(qty<(productAssortment.Product?.min_qty?:0.0)){
//                            binding.tvMinMax.visible()
//                            binding.tvMinMax.text=context.getString(R.string.reached_min_qty)
//                            Toast.makeText(context,context.getString(R.string.reached_min_qty),Toast.LENGTH_SHORT).show()
//                            binding.tvQuantity.setText((productAssortment.Product?.min_qty?:1.0).toString())
                        }else{
//                            binding.tvMinMax.gone()
                        }
                        onTotalChanged()

                    }else{
                        productAssortment.AccountProduct?.sub_total= "0.0"
                        productAssortment.AccountProduct?.total="0.0"
                        productAssortment.AccountProduct?.agreed_qty ="0.0"

//                        binding.tvSubTotal.text=  productAssortment.AccountProduct?.sub_total
//                            String.format(binding.root.context.getString(R.string.decimal_2_format),
//                                productAssortment.AccountProduct?.sub_total ?: 0.0)
                        binding.tvTotal.text= currency_symbol+productAssortment.AccountProduct?.total
                        binding.tvCount.text = "x${String.format("%.1f",0.0)} ${productAssortment?.Product?.lov_product_uom}"

//                            String.format(binding.root.context.getString(R.string.decimal_2_format),
//                                productAssortment.AccountProduct?.total ?: 0.0)
                        onTotalChanged()
                        binding.tvMinMax.gone()
                    }
                }

                override fun afterTextChanged(p0: Editable?) {

                }
            })

            binding.tvQuantity.setText(productAssortment.AccountProduct?.agreed_qty)

            binding.ivMinus.setOnClickListener {
                itemView.hideKeyboard()
                binding.ivPlus?.alpha = 1f
                isInitialize=false
                if (productAssortment.AccountProduct?.agreed_qty?.toDouble() ?: 0.0 > (productAssortment.Product?.min_qty?: 1.0)) {
                    val orderQty =
                        ((productAssortment.AccountProduct?.agreed_qty?.toDouble() ?: 0.0) - (productAssortment.Product?.step_qty?:1.0))
                    productAssortment.AccountProduct?.agreed_qty = orderQty.toString()
                    binding.tvQuantity.setText(orderQty.toString())
//                    if (checkOrderQty(productAssortment.Product?.step_qty?:1.0, orderQty)) {
//                        handler.removeCallbacks(runnable)
//                        handler.postDelayed(runnable, 2000)
//                    } else {
//                        showAlert(cartItem.Product.step_qty, orderQty)
//                    }
                    binding.ivMinus?.alpha = 1f
//                    binding.tvMinMax.gone()
                    binding.ivMinus.backgroundTintList= ColorStateList.valueOf(Color.parseColor("#BCBCBC"))
                    binding.ivPlus.backgroundTintList= ColorStateList.valueOf(Color.parseColor("#BCBCBC"))

                } else {
                    binding.ivMinus?.alpha = 0.4f
//                    binding.tvMinMax.visible()
                    binding.ivPlus.backgroundTintList= ColorStateList.valueOf(Color.parseColor("#BCBCBC"))
                    binding.ivMinus.backgroundTintList= ColorStateList.valueOf(Color.parseColor("#FF3D3D"))
//                    binding.tvMinMax.text=context.getString(R.string.reached_min_qty)
//                    if (orderQty >= (productAssortment.Product?.max_qty?:10000000.0)){
//                            binding.tvMinMax.visible()
//                            Toast.makeText(context,context.getString(R.string.reached_max_qty),Toast.LENGTH_SHORT).show()
//                            binding.tvMinMax.text=context.getString(R.string.reached_max_qty)
//                        binding.tvQuantity.setText((productAssortment.Product?.max_qty?:100.0).toString())
//                    }else if(orderQty<(productAssortment.Product?.min_qty?:0.0)){
//                            binding.tvMinMax.visible()
//                            binding.tvMinMax.text=context.getString(R.string.reached_min_qty)
//                            Toast.makeText(context,context.getString(R.string.reached_min_qty),Toast.LENGTH_SHORT).show()
                        binding.tvQuantity.setText((productAssortment.Product?.min_qty?:1.0).toString())
//                    }else{
//                            binding.tvMinMax.gone()
//                    }
                }
            }

            binding.ivPlus.setOnClickListener {
                itemView.hideKeyboard()
                binding.ivMinus?.alpha = 1f
                isInitialize=false
                if (productAssortment.AccountProduct?.agreed_qty?.toDouble() ?: 0.0 < (productAssortment.Product?.max_qty?:1000.0)) {
                    val orderQty =
                        (productAssortment.AccountProduct?.agreed_qty?.toDouble() ?: 0.0) + (productAssortment.Product?.step_qty?: 1.0)
                    binding.tvQuantity.setText(orderQty.toString())
                    productAssortment.AccountProduct?.agreed_qty = orderQty.toString()
//                    if (checkOrderQty(cartItem.Product.step_qty, orderQty)) {
//                        handler.removeCallbacks(runnable)
//                        handler.postDelayed(runnable, 2000)
//                    } else {
//                        showAlert(cartItem.Product.step_qty, orderQty)
//                    }
                    binding.tvMinMax.gone()

                    binding.ivPlus.alpha = 1f
                    binding.ivPlus.backgroundTintList= ColorStateList.valueOf(Color.parseColor("#BCBCBC"))
                    binding.ivMinus.backgroundTintList= ColorStateList.valueOf(Color.parseColor("#BCBCBC"))
                } else {
                    binding.ivPlus.alpha = 0.4f
                    binding.ivPlus.backgroundTintList= ColorStateList.valueOf(Color.parseColor("#FF3D3D"))
                    binding.ivMinus.backgroundTintList= ColorStateList.valueOf(Color.parseColor("#BCBCBC"))

//                    binding.tvMinMax.visible()
//                    binding.tvMinMax.text=context.getString(R.string.reached_max_qty)
                    binding.tvQuantity.setText((productAssortment.Product?.max_qty?:100.0).toString())

                }
            }


        }

        private fun checkOrderQty(stepQty: Double, orderQty: Double): Boolean {
            return (orderQty % stepQty )== 0.0
        }

       fun getPromoTag(
            promoRules: List<PromoRule>?,
            orderQty: Double?,
            tmplpromo: Tmplpromo?,
            total: Double
        ): PromoRule? {
            promoRules?.forEachIndexed { index, it ->
                if ((it.min_qty <= (orderQty ?: 0.0) && it.max_qty >= (orderQty ?: 0.0))
                    && (isValidDiscount(tmplpromo?.startdate,tmplpromo?.enddate))
                    && (((it.account_max_amt?:0.0)==0.0) || ((it.account_max_amt?:0.0)>=total))
                ){
                    return it
                }
            }
            return null
        }

       fun getAmountPromoTag(
            promoRules: List<PromoRule>?,
            tmplpromo: Tmplpromo?,
            total: Double
        ): PromoRule? {
            promoRules?.forEachIndexed { index, it ->
                if ((it.min_qty <= (total ?: 0.0) && it.max_qty >= (total ?: 0.0))
                    && (isValidDiscount(tmplpromo?.startdate,tmplpromo?.enddate))
                    && (((it.account_max_amt?:0.0)==0.0) || ((it.account_max_amt?:0.0)>=total))
                ){
                    return it
                }
            }
            return null
        }
    }
}

