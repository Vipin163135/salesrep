package com.salesrep.app.data.models.requests

data class ApiRequest<out T>(
        val params: T? = null
)
