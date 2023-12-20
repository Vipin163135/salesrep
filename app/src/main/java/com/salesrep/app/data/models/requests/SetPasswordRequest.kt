package com.salesrep.app.data.models.requests

import com.google.gson.annotations.SerializedName

data class SetPasswordRequest(
    @SerializedName("method")
    val method: String,
    @SerializedName("service")
    val service: String,
    @SerializedName("parameters")
    val parameters: PassParameters
)

data class PassParameters(
    @SerializedName("data")
    val setPassData : SetPassData
)

data class SetPassData(
    val password: String,
    val code: String,
    val email: String
)

