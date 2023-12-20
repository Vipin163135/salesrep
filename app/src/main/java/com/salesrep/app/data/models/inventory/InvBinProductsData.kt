package com.salesrep.app.data.models.inventory

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class InvBinProductsData(
    val id: Int?=null,
    val invbin_id: Int?=null,
    val invloc_id: Int?=null,
    var lov_invbinproduct_status: String?=null,
    val product_id: Int?=null,
    val commited_flg: Boolean?=null,
    var qty: String?=null
):Parcelable