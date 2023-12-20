package com.salesrep.app.data.models.requests

data class GetTeamRouteRequest(
    val team_id: Int,
    val  reportDate: String?= null
)