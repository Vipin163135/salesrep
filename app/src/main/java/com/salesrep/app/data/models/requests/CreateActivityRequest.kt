package com.salesrep.app.data.models.requests


data class CreateActivityRequest(
    val activities: ArrayList<CreateActivityData>
)

data class CreateActivityData(
    val Activity: CreateActivityTemplate
)

data class CreateActivityTemplate(
    val description: String?=null,
    val integration_id: String?=null,
    val latitude: Double?=null,
    val longitude: Double?=null,
    val lov_activity_status: String?=null,
    val lov_activity_type: String?=null,
    val visit_id: Int?=null
)