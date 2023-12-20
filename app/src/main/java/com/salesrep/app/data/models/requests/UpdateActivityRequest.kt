package com.salesrep.app.data.models.requests

import androidx.room.Entity
import androidx.room.PrimaryKey

data class UpdateActivityRequest(
    val activities: ArrayList<UpdateRouteActivity>
)

@Entity(tableName = "updateActivity")
data class UpdateRouteActivity(
    val Activity: RouteActivityData,
    @PrimaryKey(autoGenerate = true)
    val id: Int?=null
)

data class RouteActivityData(
    val actual_enddate: String?= null,
    val actual_startdate: String?= null,
    val description: String?= "",
    val id: Int?= null,
    val integration_id: String?= null,
    val latitude: Double?= null,
    val longitude: Double?= null,
    val lov_route_exec_status_reason: String?= "",
    val lov_activity_status: String,
    val Attachments: ArrayList<Attachment>?=null,
    val ActivityProducts : ArrayList<UpdateProductData>?= null
)
data class UpdateProductData(
    val lov_product_location: String?= null,
    val lov_product_status: String?= null,
    val lov_invbinproduct_status: String?= null,
    val lov_product_uom: String?= null,
    val product_integration_num: String?= null,
    val product_title: String?= null,
    val product_id: Int?= null,
    val product_qty: Double?= null
)