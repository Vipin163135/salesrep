package com.salesrep.app.data.models.inventory

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize


@Parcelize
@Entity(tableName = "inventory")
class GetTeamInventoryResponse(
    @PrimaryKey(autoGenerate = true)
    var id:Long?=null,
    val Van: InventoryTemplate?=null,
    val Warehouses : ArrayList<InventoryTemplate>?=null,
    val Movements : ArrayList<InvMovementTemplate>?=null
):Parcelable