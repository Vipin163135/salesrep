package com.salesrep.app.data.appConfig

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AvailableLang (
        @SerializedName("code")
        @Expose
        var  code : String? = "",

        @SerializedName("name")
        @Expose
        var  name: String? = "",

        var isSelected: Boolean = false

): Parcelable
