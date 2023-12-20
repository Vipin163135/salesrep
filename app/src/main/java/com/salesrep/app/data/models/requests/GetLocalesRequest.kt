package com.salesrep.app.data.models.requests

import com.google.gson.annotations.SerializedName

data class GetLocalesRequest(
    @SerializedName("class")
    val platform: String,
    @SerializedName("lang")
    val lang: String
)