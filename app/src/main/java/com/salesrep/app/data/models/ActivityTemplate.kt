package com.salesrep.app.data.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ActivityTemplate(
    val account_id: Int?= null,
    var actual_duration: Int?= null,
    var actual_enddate: String?= null,
    var actual_startdate: String?= null,
    var description: String?= null,
    val id: Int?= null,
    val integration_id: String?= null,
    val lov_activity_reason: String?= null,
    val required_flg: Boolean= false,
    var lov_activity_status: String?= null,
    val lov_activity_type: String?= null,
    val name: String?= null,
    val route_id: Int?= null,
    val team_id: Int?= null,
    val title: String,
    var latitude: Double?= 0.0,
    var longitude: Double?= 0.0):Parcelable