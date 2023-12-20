package com.salesrep.app.data.models.response

import com.salesrep.app.data.models.AddressTemplate
import com.salesrep.app.data.models.RouteTemplate

data class GetTeamRoutesResponse (
    val Route : RouteTemplate?= null,
    val Address: AddressTemplate?= null
)