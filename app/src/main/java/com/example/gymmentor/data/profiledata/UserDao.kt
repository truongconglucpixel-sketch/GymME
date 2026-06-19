package com.example.gymmentor.data.profiledata

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(user: User)

    @Query("SELECT * FROM profile_users WHERE id = 1")
    fun getUserFlow(): Flow<User?>
}