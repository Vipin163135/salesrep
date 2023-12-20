package com.salesrep.app.data.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AddressTemplate(
    val application_id: Int?,
    val city:  String?= "",
    val country:  String?= "",
    val country_flg:  String?= "",
    val created:  String?= "",
    val created_by: Int,
    val created_by_position: Int,
    val id: Int,
    val latitude: Double,
    val longitude: Double,
    val lov_address_type:  String?= "",
    val objectType:  String?= "",
    val organization_id: Int,
    val phone:  String?= "",
    val state:  String?= "",
    val street:  String?= "",
    val street_no:  String?= "",
    val suburb:  String?= "",
    val updated:  String?= "",
    val updated_by: Int,
    val zip:  String?= ""
):Parcelable