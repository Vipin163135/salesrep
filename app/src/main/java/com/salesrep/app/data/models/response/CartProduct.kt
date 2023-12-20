package com.salesrep.app.data.models.response

import android.os.Parcelable
import com.salesrep.app.data.models.*
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RepriceOrderResponse(
    val CartProducts: ArrayList<CartProduct>?=null,
    val Address: AddressTemplate?=null,
    val Order: OrderTemplate?=null,
    var Account: AccountTemplate?=null,
    var Paymentprofiles : ArrayList<Paymentprofiles>?= null,
    var PendingPayments : ArrayList<CreatePaymentTemplate>?= null

) : Parcelable

@Parcelize
data class CartProduct(
    val OrdersProduct: OrdersProduct?,
    val PricelistProduct: PricelistProduct?,
    val Product: ProductTemplate?,
    val Promotions: List<PromotionListObject>?,
    val RedeemablePointproduct: RedeemablePointProduct?,
    val Discounts: List<RedeemDiscount>?,
    var isSelected: Boolean? = false
) : Parcelable

@Parcelize
data class OrdersProduct(

    val adjusted_price: Double? = 0.0,
    val adjusted_total: Double? = 0.0,
    val adult_base_price: Double? = 0.0,
    var product_qty: Double? = 0.0,
    var base_price: Double? = 0.0,
    val breakage_amt: Double? = 0.0,
    val breakage_percent: Double? = 0.0,
    val calc_componenttotal: Double? = 0.0,
    val created: String?=null,
    val currency: String?= "",
    val product_name: String?=null,
    val currency_rate: Double? = 0.0,
    val day_base_price: Double? = 0.0,
    val delivery_date: String?="",
    val disc_amt: Double? = 0.0,
    var disc_percent: Double? = 0.0,
    val enddate: String?="",
    val extended_price: Double? = 0.0,
    val id: Int?=null,
    val lov_item_type: String?=null,
    val lov_return_reason: String?=null,
    val lov_return_status: String?=null,
    val lov_return_tag: String?=null,
    val name: String?=null,
    val order_id: Int?=null,
    val org_currency: String?=null,
    val org_currency_rate: Double? = 0.0,
    val organization_id: Int?=null,
    val pricelist_id: Int?=null,
    val product_id: Int?=null,
    val promo_flg: Boolean?=false,
    val promodisc_amount: Double? = 0.0,
    val promodisc_perc: Double? = 0.0,
    val resource_id: Int?=null,
    val startdate: String?=null,
    val stock_chk: Int?=null,
    var subtotal: Double?=0.0,
    val suggested_qty: Int?=null,
    var tax_amt: Double? = 0.0,
    val tax_amt1: Double? = 0.0,
    val tax_amt2: Double? = 0.0,
    val tax_amt3: Double? = 0.0,
    val tax_amt4: Double? = 0.0,
    val tax_percent: Double? = 0.0,
    val tax_percent1: Double? = 0.0,
    val tax_percent2: Double? = 0.0,
    val tax_percent3: Double? = 0.0,
    val tax_percent4: Double? = 0.0,
    var total: Double? = 0.0,
    val total_gross_price: Double? = 0.0,
    val total_gross_disc_percent: Double?=0.0,
    val gross_direct_disc_amount: Double? = 0.0,
    val weighted_qty: Double?=0.0
) : Parcelable

@Parcelize
data class PromotionListObject(
    val OrderProductsPromo: PromotionTemplate?,
    val Tmplpromo: PromotionTemplate?,
    val Pointproduct: PromotionTemplate?
) : Parcelable

@Parcelize
data class PricelistProduct(
    val application_id: Int?=null,
    val created: String?=null,
    val created_by: Int?=null,
    val created_by_position: Int?=null,
    val currency: String?=null,
    val disc_enddt: String?=null,
    val disc_percent: Double? = 0.0,
    val disc_startdt: String?=null,
    val enddt: String?=null,
    val id: Int?=null,
    val lov_status: String?=null,
    val manual_discount_flg: Boolean?=false,
    val name: String? = "",
    val net_price: Double? = 0.0,
    val organization_id: Int?=null,
    val percentage: Double? = 0.0,
    val percentage_flg: Boolean?=false,
    val priceinpoints: Int?=null,
    val pricelist_id: Int?=null,
    val product_id: Int?=null,
    val salescommission_percent: Double? = 0.0,
    val startdt: String?=null,
    var tax: Double? = 0.0,
    val tax1: Double? = 0.0,
    val tax2: Double? = 0.0,
    val tax3: Double? = 0.0,
    val tax4: Double? = 0.0,
    val tax_method: String?=null,
    val total_price: Double? = 0.0,
    val unit_price: Double? = 0.0,
    val unit_price1: Double? = 0.0,
    val unit_price2: Double? = 0.0,
    val unit_price3: Double? = 0.0,
    val unit_price4: Double? = 0.0,
    val updated: String?=null) : Parcelable

@Parcelize
data class RedeemablePointProduct(
    val Pointproduct: PointProduct?,
    val PricelistProduct: PricelistProductRedeem?
) : Parcelable

@Parcelize
data class PointProduct(
    val id: Int?=null,
    val name: String?=null,
    val title: String?
) : Parcelable

@Parcelize
data class PricelistProductRedeem(
    val equity_amt: Double?=0.0,
    val id: Int?=null,
    val max_allowed_points: Double?=0.0,
    val available_points: Double?=0.0,
    val total_price: Double?
) : Parcelable

@Parcelize
data class RedeemDiscount(
    val OrderProductsDiscount: OrderProductsDiscount?) : Parcelable

@Parcelize
data class OrderProductsDiscount(
    val application_id: Int?=null,
    val created: String?=null,
    val created_by_position: Int?=null,
    val currency: String?=null,
    val description: String?=null,
    val discount_amount: Double?=0.0,
    val discount_percent: Double? = 0.0,
    val id: Int?=null,
    val lov_discount_option: String?=null,
    val lov_discount_type: String?=null,
    val manual_flg: Boolean?=false,
    val name: String?=null,
    val order_id: Int?=null,
    val orders_product_id: Int?=null,
    val organization_id: Int?=null,
    val points_qty: Int?=null,
    val pricelist_id: Int?=null,
    val product_id: Int?=null,
    val title: String?=null,
    val total_percent_amount: Double? = 0.0,
    val updated: String?=null,
    val updated_by: Int?=null,
) : Parcelable


