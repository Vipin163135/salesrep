package com.salesrep.app.ui.takeOrder

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.salesrep.app.R
import com.salesrep.app.data.models.response.ProductAssortment
import com.salesrep.app.data.models.response.StatusModel
import com.salesrep.app.databinding.ItemProductsBinding
import com.salesrep.app.util.gone
import com.salesrep.app.util.visible
import java.util.ArrayList

class TakeOrderProductsAdapter (
    var context: Context,
    val pricelistCurrencySymbol: String
) : RecyclerView.Adapter<TakeOrderProductsAdapter.ViewHolder>() {

    var cartList = ArrayList<ProductAssortment>()

    inner class ViewHolder(val binding: ItemProductsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(cartItem: ProductAssortment) {
//            if (cartItem.Product!!.short_desc != null) {
//                val title = cartItem.Product!!.product_name!!.split("\n")
//                if (!title.isNullOrEmpty()) {
//                    binding.tvName.text = Html.fromHtml(title[0]).toString().trim()
//                } else {
//                    binding.tvName.text = ""
//                }
//            } else {
//                binding.tvName.text = ""
//            }
            binding.tvName.text = cartItem.Product!!.title
            binding.tvSku.text= context.getString(R.string.sku_value , cartItem.Product?.integration_num)
            binding.tvUom.text= context.getString(R.string.uom_value , cartItem.Product?.lov_product_uom_major)

            val netPrice= cartItem.PricelistProduct?.net_price ?:0.0
            val taxTotal= (cartItem.PricelistProduct?.tax_total ?:0.0)

            binding.tvPriceDiscounted.text = context.getString(
                R.string.cart_price_text,
                pricelistCurrencySymbol, (netPrice-taxTotal)
            )
            binding.tvPrice.text = context.getString(
                R.string.cart_price_text,
                pricelistCurrencySymbol, cartItem.OrdersProduct?.total_gross_price ?: 0.0
            )

            val discount= String.format("%.2f",
                (cartItem.OrdersProduct?.total_gross_disc_percent?:0.0)
            )
            if (discount .toDouble() > 0.0) {
                binding.tvDiscount.visible()
                binding.tvDiscount.text = "-" + context.getString(
                    R.string.discount_price,
                    (cartItem.OrdersProduct?.total_gross_disc_percent)
                ) + "%"
            } else {
                binding.tvDiscount.gone()
            }


//            if (cartItem.Product.payableinpoints == true) {
//                binding.ivRedeemPoints.visible()
//                binding.tvRedeemPoints.visible()
//            } else {
//                binding.ivRedeemPoints.gone()
//                binding.tvRedeemPoints.gone()
//            }

//            if ((cartItem.PricelistProduct?.total_price ?: 0.0) > 0.0) {
//                if ((cartItem.PricelistProduct?.total_price
//                        ?: 0.0) == (cartItem.PricelistProduct?.net_price ?: 0.0)
//                ) {
//
//                    binding.tvPrice.gone()
//                    binding.tvPriceDiscounted.text = context.getString(
//                        R.string.cart_price_text,
//                        pricelistCurrencySymbol, cartItem.PricelistProduct?.total_price ?: 0.0
//                    )
//
//                } else {
//                    binding.tvPrice.visible()
//                    binding.tvPrice.text = context.getString(
//                        R.string.cart_price_text,
//                        pricelistCurrencySymbol, cartItem.PricelistProduct?.total_price
//                    )
//                    binding.tvPrice.setPaintFlags(binding.tvPrice.getPaintFlags() or Paint.STRIKE_THRU_TEXT_FLAG)
//                }
//            } else {
//                binding.tvPrice.gone()
//            }
//            if ((cartItem.OrdersProduct.total ?: 0.0) > 0.0) {
            binding.tvTotal.visible()
            binding.tvTotal.text = context.getString(
                R.string.cart_price_text,
                pricelistCurrencySymbol, (cartItem.OrdersProduct?.total ?: 0.0)
            )
//            binding.tvSubTotal.text = context.getString(
//                R.string.cart_price_text,
//                pricelistCurrencySymbol, (cartItem.OrdersProduct?.subtotal ?: 0.0)
//            )
//            binding.tvTotalDiscount.text = context.getString(
//                R.string.cart_price_text,
//                pricelistCurrencySymbol, (cartItem.OrdersProduct?.gross_direct_disc_amount ?: 0.0)
//            )
//            binding.tvTax.text = context.getString(
//                R.string.cart_price_text,
//                pricelistCurrencySymbol, (cartItem.OrdersProduct?.tax_amt ?: 0.0)
//            )
//            } else {
//                binding.tvTotal.gone()
//            }

            binding.tvCount.text = "x${String.format("%.1f",cartItem.OrdersProduct!!.product_qty)} ${cartItem.Product?.lov_product_uom}"


            binding.tvQuantity.setText(cartItem.OrdersProduct.product_qty.toString())
            binding.ivMinus.alpha = 0.4f
            binding.ivPlus.alpha = 0.4f
            binding.tvQuantity.isFocusable=false
            binding.tvQuantity.isClickable=false

            binding.ivDelete.gone()


            if ((cartItem.OrdersProduct.disc_percent ?: 0.0) > 0.0) {
                binding.llDiscount.visible()
                binding.tvDiscountAmount.text ="$pricelistCurrencySymbol"+
                        String.format("%.2f", cartItem.OrdersProduct.gross_direct_disc_amount)
                binding.tvDiscountText.text = "$pricelistCurrencySymbol${
                    String.format("%.2f", cartItem.OrdersProduct.total_gross_disc_percent)
                }%${context.getString(R.string.direct_discount)}"
            } else {
                binding.llDiscount.gone()
            }

            val adapter = TakeOrderProductPromoAdapter(pricelistCurrencySymbol)
            binding.rvPromotions.adapter = adapter
            binding.rvPromotions.layoutManager = LinearLayoutManager(itemView.context)
            cartItem.Promotions?.map {
                it.OrderProductsPromo?.orderQty = cartItem.OrdersProduct.product_qty
            }
            adapter.addList(cartItem.Promotions?: arrayListOf())

//            if (!cartItem.Discounts.isNullOrEmpty()){
//                binding.rvRedeemPoints.visible()
//                val pointsAdapter = ProductAssortmentPointDiscountAdapter(callback, cartItem.Product, pricelistCurrencySymbol)
//                binding.rvRedeemPoints.adapter = pointsAdapter
//                binding.rvRedeemPoints.layoutManager = LinearLayoutManager(itemView.context)
//
//                pointsAdapter.addList(cartItem.Discounts)
//
//            }else{
//                binding.rvRedeemPoints.invisible()
//            }


//            binding.tvQuantity.addTextChangedListener(object : TextWatcher {
//                override fun afterTextChanged(s: Editable?) {
//                }
//
//                override fun beforeTextChanged(
//                    s: CharSequence?,
//                    start: Int,
//                    count: Int,
//                    after: Int
//                ) {
//                }
//
//                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                    if (!binding.tvQuantity.text.isNullOrEmpty() && binding.tvQuantity.text.toString()
//                            .toDouble() > 0.0
//                    ) {
//                        cartItem.Product.order_qty = binding.tvQuantity.text.toString()
//                        when {
//                            cartItem.Product.order_qty?.toDouble() ?: 0.0 <= (cartItem.Product.min_qty?:0.0) -> {
//                                binding.ivMinus.alpha = 0.4f
//                                binding.ivPlus.alpha = 1f
//                            }
//                            cartItem.Product.order_qty?.toDouble() ?: 0.0 >= (cartItem.Product.max_qty?:1000.0) -> {
//                                binding.ivMinus.alpha = 1f
//                                binding.ivPlus.alpha = 0.4f
//                            }
//                            else -> {
//                                binding.ivPlus.alpha = 1f
//                                binding.ivMinus.alpha = 1f
//                            }
//                        }
//                    } else {
//                        binding.ivPlus.alpha = 0.4f
//                        binding.ivMinus.alpha = 0.4f
//                    }
//                }
//            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemProductsBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_products, parent, false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return cartList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(cartList[position])
    }

    fun add(cartList: ArrayList<ProductAssortment>) {
        this.cartList.clear()
        this.cartList.addAll(cartList)
        notifyDataSetChanged()
    }

    fun getList(): ArrayList<ProductAssortment>{
       return this.cartList
    }

    private fun showAlert(stepQty: Double, orderQty: Double) {
        val builder = AlertDialog.Builder(context)
        val title = context.getString(R.string.order_qty_title, String.format("%.1f",stepQty))
        val message = context.getString(
            R.string.order_qty_example,
            String.format("%.1f",stepQty),
            String.format("%.1f",(stepQty * 2)) ,
            String.format("%.1f",(stepQty * 3)),
            String.format("%.1f",(stepQty * 4))
        )

        builder.setTitle(title)
        builder.setMessage(message)

        builder.setPositiveButton(R.string.ok) { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun checkOrderQty(stepQty: Double, orderQty: Double): Boolean {
        return (orderQty % stepQty )== 0.0
    }
}

//interface OnCartItemClicked {
//    fun onDeleteProduct(position: Int)
//    fun onDeleteDiscount(productTemplate: ProductTemplate)
//    fun onAddSubtractProduct(cartItem: ProductAssortment)
//}