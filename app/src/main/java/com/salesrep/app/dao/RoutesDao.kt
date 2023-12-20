package com.salesrep.app.dao

import androidx.paging.PagingSource
import androidx.room.*
import androidx.room.Dao
import com.salesrep.app.data.models.response.GetHomeRoutesResponse

@Dao
interface RoutesDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(list: ArrayList<GetHomeRoutesResponse>?): List<Long>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(routeData: GetHomeRoutesResponse?): Long

  @Query("SELECT * FROM routes")
  suspend fun getAllRoutes() : List<GetHomeRoutesResponse>

  @Update
  suspend fun update(obj: List<GetHomeRoutesResponse>)

  @Transaction
  suspend fun insertOrUpdate(objList: ArrayList<GetHomeRoutesResponse>?) {
    val insertResult = insert(objList)
    val updateList = mutableListOf<GetHomeRoutesResponse>()

    for (i in insertResult.indices) {
      if (insertResult[i] == -1L)
        updateList.add(objList?.get(i) ?: return)
    }
    if (updateList.isNotEmpty())
      update(updateList)
  }

  @Delete
  suspend fun deleteRoute(routeData: GetHomeRoutesResponse?)

  @Query("DELETE FROM routes")
  suspend fun clearAll()

}