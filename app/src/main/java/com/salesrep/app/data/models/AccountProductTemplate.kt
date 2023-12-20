package com.salesrep.app.data.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AccountProductTemplate(
    var agreed_qty: String= "0.0",
    val id: Int?= null,
    var benchmark_price_disc_percent: Double? = 0.0,
    var benchmark_price_disc_startdate: String? = null,
    var benchmark_price_disc_enddate: String? = null,
    var benchmark_price_disc_min_qty: Double? = null,
    var benchmark_price_disc_max_qty: Double? = null,
    var net_price: Double? = 0.0,
    var tax_total: Double? = 0.0,
    var disc_percent: Double? = 0.0,
    var disc_startdt: String? = null,
    var disc_enddt: String? = null,
    var apply_direct_disc: Double? = 0.0,
    var apply_promo_disc: Double? = 0.0,
    var apply_disc: Double? = 0.0,
    var sub_total: String? = "0.0",
    var total: String? = "0.0",
    val currency: String?=null,
    val enddt: String?=null,
    val startdt: String?=null,
    ):Parcelable