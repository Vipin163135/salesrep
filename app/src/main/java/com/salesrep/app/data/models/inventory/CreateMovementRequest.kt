package com.salesrep.app.data.models.inventory

import androidx.room.Entity
import androidx.room.PrimaryKey

data class CreateMovementRequest(
    val movements: ArrayList<MovementListData>
)

@Entity(tableName = "createMovement")
data class MovementListData(
    @PrimaryKey(autoGenerate = true)
    var id:Long?=null,
    val Movement: InvMovementData,
    val products: ArrayList<MovementProduct>,
    var isUpdateApi: Boolean?= null
)

data class MovementProduct(
    val lov_invbinproduct_status: String,
    val product_id: Int,
    val qty: Double
)