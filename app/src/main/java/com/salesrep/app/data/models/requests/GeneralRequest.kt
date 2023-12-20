package com.salesrep.app.data.models.requests

import com.google.gson.annotations.SerializedName

data class GeneralRequest<out T> (
    @SerializedName("method")
    val method: String,
    @SerializedName("service")
    val service: String,
    @SerializedName("parameters")
    val parameter: T? = null
)