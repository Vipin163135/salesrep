package com.salesrep.app.data.network.responseUtil

data class ApiResponseData<out T>(
        val type: String? = null,
        val message: String? = null,
        val unreadQty: Int? = 0,
        val success: Boolean? = null,

        val data: T? = null,
)