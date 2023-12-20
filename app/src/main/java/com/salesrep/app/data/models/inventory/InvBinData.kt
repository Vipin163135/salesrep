package com.salesrep.app.data.models.inventory

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class InvBinData(
    val id: Int?=null,
    val lov_bin_type: String?=null,
    val name: String?=null,
    val title: String?=null
):Parcelable