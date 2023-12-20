package com.salesrep.app.dao

import androidx.room.*
import com.salesrep.app.data.models.requests.UpdateRouteActivity

@Dao
interface UpdateActivityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: List<UpdateRouteActivity>?): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(routeData: UpdateRouteActivity?)

    @Query("SELECT * FROM updateActivity")
    suspend fun getAllActivity() : List<UpdateRouteActivity>

    @Update
    suspend fun update(obj: List<UpdateRouteActivity>)

    @Transaction
    suspend fun insertOrUpdate(objList: List<UpdateRouteActivity>?) {
        val insertResult = insert(objList)
        val updateList = mutableListOf<UpdateRouteActivity>()

        for (i in insertResult.indices) {
            if (insertResult[i] == -1L)
                updateList.add(objList?.get(i) ?: return)
        }
        if (updateList.isNotEmpty())
            update(updateList)
    }

    @Delete
    suspend fun deleteRoute(routeData: UpdateRouteActivity?)

    @Query("DELETE FROM updateActivity")
    suspend fun clearAll()

}