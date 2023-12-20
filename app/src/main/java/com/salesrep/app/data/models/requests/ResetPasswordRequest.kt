package com.salesrep.app.data.models.requests

import com.google.gson.annotations.SerializedName

data class ResetPasswordRequest(
    @SerializedName("method")
    val method: String,
    @SerializedName("service")
    val service: String,
    @SerializedName("parameters")
    val parameters: ResetPassParameters
)

data class ResetPassParameters(
    @SerializedName("data")
    val resetPassData : ResetPassData
)

data class ResetPassData(
    val email: String
)
