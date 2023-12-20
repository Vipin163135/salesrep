package com.salesrep.app.data.models.requests

import androidx.room.Entity
import androidx.room.PrimaryKey

data class UpdateRouteRequest(
    val routes: ArrayList<UpdateRoute>
)

@Entity(tableName = "updateRoute")
data class UpdateRoute(
    val Route: RouteData,
    @PrimaryKey val id: Int?= null
)

data class RouteData(
    val id: Int,
    val lov_route_exec_status: String?=null,
    val lov_route_exec_status_reason: String?= "",
    val startdate: String?= null,
    val enddate: String?= null,
    val trip: String?= null,
    val trip_distance: String?= null,
    val trip_duration: String?= null
)