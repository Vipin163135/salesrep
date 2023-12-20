package com.salesrep.app.dao

import androidx.room.*
import com.salesrep.app.data.models.SaveOrderTemplate

@Dao
interface SaveOrderDao  {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: List<SaveOrderTemplate>?): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(orderData: SaveOrderTemplate?): Long

    @Query("SELECT * FROM savedOrder WHERE routeId LIKE :routeId AND taskId LIKE :taskId AND accountId LIKE :accountId AND lov_order_type LIKE :type" )
    suspend fun getCustomerOrders(routeId: Int? ,taskId:Int?,accountId:Int?,type: String?) : List<SaveOrderTemplate>

    @Query("SELECT * FROM savedOrder WHERE routeId LIKE :routeId AND lov_order_type LIKE :type" )
    suspend fun getSalesOrders(routeId: Int? ,type: String?) : List<SaveOrderTemplate>

    @Query("SELECT * FROM savedOrder WHERE routeId LIKE :routeId AND taskId LIKE :taskId AND accountId LIKE :accountId AND lov_order_type LIKE :type")
    suspend fun getReturnOrders(routeId: Int? ,taskId:Int?,accountId:Int?,type:String?) : List<SaveOrderTemplate>

    @Query("SELECT * FROM savedOrder WHERE orderId LIKE :orderId LIMIT 1")
    suspend fun getOrder(orderId: Int? ) : List<SaveOrderTemplate>

    @Update
    suspend fun update(obj: List<SaveOrderTemplate>)

    @Transaction
    suspend fun insertOrUpdate(objList: List<SaveOrderTemplate>?) {
        val insertResult = insert(objList)
        val updateList = mutableListOf<SaveOrderTemplate>()

        for (i in insertResult.indices) {
            if (insertResult[i] == -1L)
                updateList.add(objList?.get(i) ?: return)
        }
        if (updateList.isNotEmpty())
            update(updateList)
    }

    @Delete
    suspend fun deleteOrder(orderData: SaveOrderTemplate?)

    @Query("DELETE FROM savedOrder")
    suspend fun clearAll()

    @Query("DELETE FROM savedOrder WHERE routeId LIKE :routeId AND taskId LIKE :taskId AND accountId LIKE :accountId AND lov_order_type LIKE :type")
    suspend fun deleteOrders(routeId: Int? ,taskId:Int?,accountId:Int?,type : String)


}