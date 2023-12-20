package com.salesrep.app.dao

import androidx.room.*
import com.salesrep.app.data.models.requests.CreateOrderTemplate


@Dao
interface CreateOrderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: ArrayList<CreateOrderTemplate>?): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(routeData: CreateOrderTemplate?)

    @Query("SELECT * FROM createOrder")
    suspend fun getAllOrders() : List<CreateOrderTemplate>

    @Update
    suspend fun update(obj: ArrayList<CreateOrderTemplate>)

    @Transaction
    suspend fun insertOrUpdate(objList: ArrayList<CreateOrderTemplate>?) {
        val insertResult = insert(objList)
        val updateList = mutableListOf<CreateOrderTemplate>()

        for (i in insertResult.indices) {
            if (insertResult[i] == -1L)
                updateList.add(objList?.get(i) ?: return)
        }
        if (updateList.isNotEmpty())
            update(updateList as ArrayList<CreateOrderTemplate>)
    }

    @Delete
    suspend fun deleteRoute(routeData: CreateOrderTemplate?)

    @Query("DELETE FROM createOrder")
    suspend fun clearAll()

}