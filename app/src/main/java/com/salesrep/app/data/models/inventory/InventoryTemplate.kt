package com.salesrep.app.data.models.inventory

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class InventoryTemplate(
    val Invloc : InvLocData?= null,
    val Bins : ArrayList<InvBinTemplate>?=null
):Parcelable