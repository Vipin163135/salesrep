package com.salesrep.app.dao

import androidx.room.*
import com.salesrep.app.data.models.CreatePaymentTemplate


@Dao
interface CreatePaymentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: List<CreatePaymentTemplate>?): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(routeData: CreatePaymentTemplate?)

    @Query("SELECT * FROM createPayment")
    suspend fun getAllPayments() : List<CreatePaymentTemplate>

    @Query("SELECT * FROM createPayment WHERE order_id LIKE :orderId AND order_integration_id LIKE :integrationId")
    suspend fun getPayments(orderId: Int? ,integrationId:String?) : List<CreatePaymentTemplate>

    @Update
    suspend fun update(obj: List<CreatePaymentTemplate>)

    @Transaction
    suspend fun insertOrUpdate(objList: List<CreatePaymentTemplate>?) {
        val insertResult = insert(objList)
        val updateList = mutableListOf<CreatePaymentTemplate>()

        for (i in insertResult.indices) {
            if (insertResult[i] == -1L)
                updateList.add(objList?.get(i) ?: return)
        }
        if (updateList.isNotEmpty())
            update(updateList)
    }

    @Delete
    suspend fun deletePayment(routeData: CreatePaymentTemplate?)

    @Query("DELETE FROM createPayment")
    suspend fun clearAll()

}