package com.example.gymmentor.data.profiledata

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyStatsDao {
    @Insert
    suspend fun insert(stat: BodyStats)

    @Query("SELECT * FROM profile_body_stats ORDER BY date DESC")
    fun getAllStats(): Flow<List<BodyStats>>
}