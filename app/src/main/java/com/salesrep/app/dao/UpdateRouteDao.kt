package com.salesrep.app.dao

import androidx.room.*
import com.salesrep.app.data.models.requests.UpdateRoute


@Dao
interface UpdateRouteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: List<UpdateRoute>?): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(routeData: UpdateRoute?) : Long

    @Query("SELECT * FROM updateRoute")
    suspend fun getAllActivity() : List<UpdateRoute>

    @Update
    suspend fun update(obj: List<UpdateRoute>)

    @Transaction
    suspend fun insertOrUpdate(objList: List<UpdateRoute>?) {
        val insertResult = insert(objList)
        val updateList = mutableListOf<UpdateRoute>()

        for (i in insertResult.indices) {
            if (insertResult[i] == -1L)
                updateList.add(objList?.get(i) ?: return)
        }
        if (updateList.isNotEmpty())
            update(updateList)
    }

    @Delete
    suspend fun deleteRoute(routeData: UpdateRoute?)

    @Query("DELETE FROM updateRoute")
    suspend fun clearAll()

}