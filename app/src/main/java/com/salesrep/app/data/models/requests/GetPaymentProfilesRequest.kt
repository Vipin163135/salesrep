package com.salesrep.app.data.models.requests

import com.google.gson.annotations.SerializedName

data class GetPaymentProfilesRequest(
    @SerializedName("account_id")
    val account_id: Int?
)