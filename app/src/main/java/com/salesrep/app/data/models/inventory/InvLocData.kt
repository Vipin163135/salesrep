package com.salesrep.app.data.models.inventory

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class InvLocData(
    val id: Int?=null,
    val lov_invloc_status: String?=null,
    val lov_invloc_type: String?=null,
    val name: String?=null,
    val store_id: Int?=null,
    val title: String?=null
):Parcelable