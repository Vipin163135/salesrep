package com.salesrep.app.data.models.requests

import com.google.gson.annotations.SerializedName


data class ChangePasswordRequest(
    @SerializedName("data")
    val requestData: RequestData
)

data class RequestData(
    val newPassword: String,
    val oldPassword: String
)