package com.salesrep.app.data.models.inventory

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class InvBinTemplate(
    val Invbin: InvBinData?=null,
    var Inventory : ArrayList<InventoryDataObject>?= null
):Parcelable