package com.salesrep.app.data.models.inventory

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ManufacturerTemplate(
    val accountname: String?= null,
    val alias: String?= null,
    val id: Int?= null
):Parcelable