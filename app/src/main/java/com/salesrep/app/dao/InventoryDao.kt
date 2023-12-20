package com.salesrep.app.dao

import androidx.room.*
import com.salesrep.app.data.models.inventory.GetTeamInventoryResponse

@Dao
interface InventoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: ArrayList<GetTeamInventoryResponse>?): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(routeData: GetTeamInventoryResponse?): Long

    @Query("SELECT * FROM inventory")
    suspend fun getAllInventory(): List<GetTeamInventoryResponse>

    @Update
    suspend fun update(obj: List<GetTeamInventoryResponse>)

    @Transaction
    suspend fun insertOrUpdate(objList: ArrayList<GetTeamInventoryResponse>?) {
        val insertResult = insert(objList)
        val updateList = mutableListOf<GetTeamInventoryResponse>()

        for (i in insertResult.indices) {
            if (insertResult[i] == -1L)
                updateList.add(objList?.get(i) ?: return)
        }
        if (updateList.isNotEmpty())
            update(updateList)
    }

    @Delete
    suspend fun deleteRoute(routeData: GetTeamInventoryResponse?)

    @Query("DELETE FROM inventory")
    suspend fun clearAll()
}