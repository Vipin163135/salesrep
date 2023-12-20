package com.salesrep.app.dao

import androidx.room.*
import com.salesrep.app.data.models.response.GetRouteAccountResponse

@Dao
interface RouteActivityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: List<GetRouteAccountResponse>?): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(routeData: GetRouteAccountResponse?):Long

    @Query("SELECT * FROM routeActivity")
    suspend fun getAllRoutes() : List<GetRouteAccountResponse>

    @Query("SELECT * FROM routeActivity where  routeId LIKE :id")
    suspend fun getCurrentRoutes(id: Int) : List<GetRouteAccountResponse>

//    @Query("UPDATE routeActivity SET  = :end_address WHERE id = :tid")
//    fun updateRouteActivity(user_id: String?, type: Int): PagingSource<Int, GetRouteAccountResponse>

    @Update
    suspend fun update(obj: List<GetRouteAccountResponse>)

    @Transaction
    suspend fun insertOrUpdate(objList: List<GetRouteAccountResponse>?) {
        val insertResult = insert(objList)
        val updateList = mutableListOf<GetRouteAccountResponse>()

        for (i in insertResult.indices) {
            if (insertResult[i] == -1L)
                updateList.add(objList?.get(i) ?: return)
        }
        if (updateList.isNotEmpty())
            update(updateList)
    }

    @Query("DELETE FROM routeActivity where  routeId LIKE :id")
    suspend fun deleteRoute(id: Int?)

    @Query("DELETE FROM routeActivity")
    suspend fun clearAll()

}