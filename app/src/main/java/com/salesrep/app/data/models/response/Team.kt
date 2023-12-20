package com.salesrep.app.data.models.response

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Team(
    val address: String,
    val agreed_min_required_total: String,
    val alias: String,
    val billing_name: String,
    val billing_rfc: String,
    val cf_Integration_id1: Int,
    val created: String,
    val created_by: Int,
    val employee_id: Int?,
    val id: Int,
    val lov_team_status: String,
    val lov_team_type: String,
    val name: String,
    val phone: String,
    val phone_ext: String,
    val pricelist_id: Int,
    val salesoffice: String,
    val sequence_id: Int,
    val title: String,
    val updated: String,
    val user_id: Int
):Parcelable