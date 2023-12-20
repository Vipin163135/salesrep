package com.salesrep.app.data.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AssetTemplate(
    val Asset:AssetData?= null,
    val Product:ProductTemplate?= null
):Parcelable

@Parcelize
data class AssetData(
    val activationdate:String?= null,
    val created:String?= null,
    val end_date:String?= null,
    val id: Int?=null,
    val lov_asset_status:String?= null,
    val lov_asset_type:String?= null,
    val serialnum:String?= null,
    val start_date: String?=null
):Parcelable