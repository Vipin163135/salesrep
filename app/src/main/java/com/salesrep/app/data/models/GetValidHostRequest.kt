package com.salesrep.app.data.models

import com.google.gson.annotations.SerializedName

data class GetValidHostRequest (
    @SerializedName("method")
    val method: String,
    @SerializedName("service")
    val service: String
    )