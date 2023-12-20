package com.salesrep.app.data.models

import android.os.Parcelable
import com.salesrep.app.data.models.response.ProductAssortment
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PromotionData(
    val PromoRules: List<PromoRule>?=null,
    val SourceProducts: List<ProductAssortment>?=null,
    val TargetProducts: List<ProductAssortment>?=null,
    val Tmplpromo: Tmplpromo?=null
):Parcelable


@Parcelize
data class PromoRule(
    val account_due_amt: Double,
    val account_given_amt: Double,
    val account_max_amt: Double?=null,
    var amount: Double,
    val id: Int,
    val max_qty: Double,
    val min_qty: Double,
    val name: String
):Parcelable

@Parcelize
data class Tmplpromo(
    val application_id: Int,
    val back_image: String,
    val background_color: String,
    val cust_reward_text: String,
    val description: String,
    val enddate: String,
    val enforcement_enddate: String,
    val enforcement_startdate: String,
    val front_image: String,
    val icon: String,
    val id: Int,
    val lov_promo_delivery_type: String,
    val lov_promo_uom: String,
    val lov_promotion_class: String,
    val lov_promotion_rule_type: String,
    val lov_promotion_status: String,
    val lov_promotion_type: String,
    val max_amount: Double,
    val name: String,
    val organization_id: Int,
    val promocode: String,
    val public_flg: Boolean,
    val startdate: String,
    val title: String,
    ):Parcelable
