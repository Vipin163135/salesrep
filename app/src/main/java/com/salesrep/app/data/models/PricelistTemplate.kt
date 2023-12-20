package com.salesrep.app.data.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PricelistTemplate(
    val id: Int,
    val name: String,
    val currency: String?= null,
    val currency_symbol: String?= null,
    val title: Int
):Parcelable