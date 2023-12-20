package com.salesrep.app.data.models.inventory

import androidx.room.Entity
import androidx.room.PrimaryKey

data class CancelMovementRequest(
    val movements: ArrayList<MovementCancelData>
)


@Entity(tableName = "movementStatus")
class MovementCancelData(
    @PrimaryKey
    var objectId: Int? = null,
    var type: String? = null,
    var integration_id: String? = null,
    var movementStatus: String? = null,
)
