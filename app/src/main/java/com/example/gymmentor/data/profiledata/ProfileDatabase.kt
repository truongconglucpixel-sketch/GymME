package com.example.gymmentor.data.profiledata

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [User::class, BodyStats::class],
    version = 1,
    exportSchema = false
)
abstract class ProfileDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun bodyStatsDao(): BodyStatsDao
}