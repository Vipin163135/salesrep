package com.salesrep.app.data.network.responseUtil

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ApiGeneralResponse<out T>(


    @SerializedName("Appservice")
        @Expose
        val appservice: AppService,
    @SerializedName("return")
        @Expose
        val returnData: T? = null

)