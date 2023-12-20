package com.salesrep.app.data.appConfig

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName


data class AppConfigResponse(
        @SerializedName("type")
        @Expose
        var type: String? = null,

        @SerializedName("message")
        @Expose
        var message: String? = null,

        @SerializedName("success")
        @Expose
        var success: Boolean = false,

        @SerializedName("data")
        @Expose
        var data: AppConfigData? = null
)