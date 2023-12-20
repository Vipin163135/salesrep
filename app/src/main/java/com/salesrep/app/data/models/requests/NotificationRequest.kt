package com.salesrep.app.data.models.requests

import com.google.gson.annotations.SerializedName

data class NotificationRequest (

    @SerializedName("method")
    val method: String,
    @SerializedName("service")
    val service: String
)
