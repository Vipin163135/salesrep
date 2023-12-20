package com.salesrep.app.data.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class PromotionTemplate (
    val application_id: Int? = 0,
    val artwork: String?,
    val compound_flg: Boolean?,
    val created: String?,
    val currency: String?,
    val description: String?,
    val enddate: String?,
    val icon: String?,
    val front_image: String?,
    val back_image: String?,
    val id: Int? = 0,
    var orderQty: Double? = 0.0,
    val lov_promo_delivery_type: String?,
    val enforcement_startdate: String?,
    val enforcement_enddate: String?,
    val lov_promotion_rule_type: String?,
    val lov_promotion_status: String?,
    val lov_promotion_type: String?,
    val lov_promo_uom: String?,
    val max_amount: Double? = 0.0,
    val max_amount_account: Double? = 0.0,
    val name: String?,
    val organization_id: Int? = 0,
    val promocode: String?,
    val public_flg: Boolean?,
    val startdate: String?,
    val title: String?,
    val updated: String?,
    val order_id: Int?,
    val orders_product_id: Int?,
    val org_currency: String?,
    val product_id: Int?,
    val promo_given_amount: Double?,
    val given_gross_amount: Double?,
    val promo_given_percent: Double?,
    val promo_given_points : Int?,
    val tcalc_Ordernames: String?,
    val tcalc_total_promo_given_amount: Double?,
    val tmplpromo_id: Int?

):Parcelable
