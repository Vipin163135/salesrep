package com.salesrep.app.dao

import androidx.room.*
import com.salesrep.app.data.models.inventory.MovementCancelData

@Dao
interface ChangeStatusMovementDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: List<MovementCancelData>?): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(routeData: MovementCancelData?)

    @Query("SELECT * FROM movementStatus where movementStatus LIKE :status ")
    suspend fun getCancelMovements(status: String) : List<MovementCancelData>

    @Query("SELECT * FROM movementStatus where movementStatus LIKE :status ")
    suspend fun getCommitMovements(status: String) : List<MovementCancelData>

    @Update
    suspend fun update(obj: List<MovementCancelData>)

    @Transaction
    suspend fun insertOrUpdate(objList: List<MovementCancelData>?) {
        val insertResult = insert(objList)
        val updateList = mutableListOf<MovementCancelData>()

        for (i in insertResult.indices) {
            if (insertResult[i] == -1L)
                updateList.add(objList?.get(i) ?: return)
        }
        if (updateList.isNotEmpty())
            update(updateList)
    }

    @Query("DELETE FROM movementStatus where movementStatus LIKE :status")
    suspend fun deleteCommitMovement(status: String )

    @Query("DELETE FROM movementStatus where movementStatus LIKE :status")
    suspend fun deleteCancelMovement(status: String )

    @Query("DELETE FROM movementStatus")
    suspend fun clearAll()

}