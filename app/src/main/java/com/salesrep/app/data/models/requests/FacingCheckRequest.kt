package com.salesrep.app.data.models.requests

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Attachment(
    val file_base64: String?= null,
    val file_name: String?=null,
    var file: String?=null,
    var description : String ?= null,
): Parcelable