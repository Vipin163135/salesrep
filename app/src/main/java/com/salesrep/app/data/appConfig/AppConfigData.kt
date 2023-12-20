package com.salesrep.app.data.appConfig

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AppConfigData(
    @SerializedName("Crmapp")
    @Expose
    var crmapp: Crmapp? = null,

    @SerializedName("Config")
    @Expose
    var config: AppConfig? = null,


    @SerializedName("AvailableLangs")
    @Expose
    var availableLangs: ArrayList<AvailableLang>? = arrayListOf()

) : Parcelable
