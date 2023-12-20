package com.salesrep.app.data.models.inventory

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.junit.runners.ParentRunner

@Parcelize
data class InvMovementData(
    var commit_date: String?=null,
    var created: String?=null,
    var from_invbinid: Int?=null,
    var from_invlocid: Int?=null,
    var id: Int?=null,
    var integration_id: String?=null,
    var lov_invmovement_status: String?=null,
    var lov_invmovement_type: String?=null,
    var name: String?=null,
    var to_invbinid: Int?=null,
    var to_invlocid: Int?=null,
    ):Parcelable