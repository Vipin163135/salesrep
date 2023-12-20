package com.salesrep.app.data.models.inventory

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class InvMovementTemplate(
    var Invmovement: InvMovementData? = null,
    val Frominvloc: InvLocData? = null,
    val Frominvbin: InvBinData? = null,
    val Toinvloc: InvLocData? = null,
    val Toinvbin: InvBinData? = null,
    var Products: ArrayList<InvMovementProductData>? = null,
):Parcelable