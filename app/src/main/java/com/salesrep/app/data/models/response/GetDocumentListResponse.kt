package com.salesrep.app.data.models.response

import android.os.Parcelable
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.salesrep.app.data.models.PaginationTemplate
import kotlinx.android.parcel.Parcelize

@Parcelize
data class GetDocumentListResponse(
    val pagination: PaginationTemplate,
    val rows: List<GetDocumentListData>
):Parcelable

@Parcelize
data class GetDocumentListData(
    val Attachment: DocAttachment
):Parcelable

@Parcelize
data class DocAttachment(
    val created: String?=null,
    val file: String,
    val file_ext: String,
    val file_name: String,
    val file_size: Int,
    val id: Int,
    val lov_attachment_category: String
):Parcelable
