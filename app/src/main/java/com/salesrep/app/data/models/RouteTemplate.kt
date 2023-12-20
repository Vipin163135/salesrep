package com.salesrep.app.data.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RouteTemplate(
    val accounts_qty: Int?=null,
    val id: Int?=null,
    val name: String?=null,
    val title: String?=null,
    val today_route_flg: Int?=null,
    val visit_day_name: String?=null,
    val visit_day_number: Int?=null,
    val starttime: String?=null,
    val endtime: String?=null,
    val actual_enddate: String?=null,
    val actual_startdate: String?=null,
    val avg_duration: Int?=null,
    val planned_startdate: String?=null,
    var status: String?=null,
    var polyline: String?=null,
    var trip_distance: Double?=null,
    var trip_duration: Double?=null

):Parcelable