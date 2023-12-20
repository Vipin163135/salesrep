package com.salesrep.app.data.models

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class PaginationTemplate(
        @SerializedName("count")
        @Expose
        val count: Int,
        @SerializedName("page")
        @Expose
        val page: Int,
        @SerializedName("pages")
        @Expose
        val pages: Int,
        @SerializedName("size")
        @Expose
        val size: Int
):Parcelable
