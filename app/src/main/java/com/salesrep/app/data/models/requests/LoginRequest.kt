package com.salesrep.app.data.models.requests

import com.google.gson.annotations.SerializedName

data class LoginRequest(

    @SerializedName("method")
    val method: String,
    @SerializedName("service")
    val service: String,
    @SerializedName("parameters")
    val parameters: LoginParameters
)

data class LoginParameters(
    @SerializedName("device_token")
    val device_token: String,
    @SerializedName("device_type")
    val  device_type : String
    //device

)
