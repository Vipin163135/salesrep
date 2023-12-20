package com.salesrep.app.dao

import androidx.room.*
import com.salesrep.app.data.models.CreatePaymentTemplate
import com.salesrep.app.data.models.inventory.CreateMovementRequest
import com.salesrep.app.data.models.inventory.MovementListData


@Dao
interface CreateMovementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: List<MovementListData>?): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(routeData: MovementListData?)

    @Query("SELECT * FROM createMovement where isUpdateApi LIKE :isUpdate ")
    suspend fun getCreateMovements(isUpdate: Boolean) : List<MovementListData>

    @Query("SELECT * FROM createMovement where isUpdateApi LIKE :isUpdate ")
    suspend fun getUpdateMovements(isUpdate: Boolean) : List<MovementListData>

    @Update
    suspend fun update(obj: List<MovementListData>)

    @Transaction
    suspend fun insertOrUpdate(objList: List<MovementListData>?) {
        val insertResult = insert(objList)
        val updateList = mutableListOf<MovementListData>()

        for (i in insertResult.indices) {
            if (insertResult[i] == -1L)
                updateList.add(objList?.get(i) ?: return)
        }
        if (updateList.isNotEmpty())
            update(updateList)
    }

    @Query("DELETE FROM createMovement where isUpdateApi LIKE :isUpdate")
    suspend fun deleteUpdateMovement(isUpdate: Boolean? )

    @Query("DELETE FROM createMovement where isUpdateApi LIKE :isUpdate")
    suspend fun deleteCreateMovement(isUpdate: Boolean? )

    @Query("DELETE FROM createMovement")
    suspend fun clearAll()


}