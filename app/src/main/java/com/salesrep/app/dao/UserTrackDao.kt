package com.salesrep.app.dao

import androidx.room.*
import com.salesrep.app.data.models.UserTrackTemplate

@Dao
interface UserTrackDao {

    @Query("SELECT * FROM userTrack")
    fun getAll(): List<UserTrackTemplate>

    @Query("SELECT * FROM userTrack WHERE id LIKE :routeId LIMIT 1")
    suspend fun findById(routeId: Int): UserTrackTemplate?

    @Insert
    fun insertAll(vararg users: UserTrackTemplate)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userTrackTemplate: UserTrackTemplate?)

    @Delete
    fun delete(userTrackTemplate: UserTrackTemplate)

    @Query("DELETE FROM savedOrder")
    suspend fun clearAll()

}