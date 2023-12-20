package com.salesrep.app.dao

import androidx.room.*
import com.salesrep.app.data.models.requests.UpdateSurvey

@Dao
interface UpdateSurveyDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: List<UpdateSurvey>?): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(surveyData: UpdateSurvey?) : Long

    @Query("SELECT * FROM UpdateSurvey")
    suspend fun getAllSurveys() : List<UpdateSurvey>

    @Update
    suspend fun update(obj: List<UpdateSurvey>)

    @Transaction
    suspend fun insertOrUpdate(objList: List<UpdateSurvey>?) {
        val insertResult = insert(objList)
        val updateList = mutableListOf<UpdateSurvey>()

        for (i in insertResult.indices) {
            if (insertResult[i] == -1L)
                updateList.add(objList?.get(i) ?: return)
        }
        if (updateList.isNotEmpty())
            update(updateList)
    }

    @Delete
    suspend fun deleteRoute(surveyData: UpdateSurvey?)

    @Query("DELETE FROM UpdateSurvey")
    suspend fun clearAll()

}