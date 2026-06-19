package com.example.gymmentor.data.profiledata

import android.content.Context
import androidx.room.Room

object ProfileDatabaseProvider {
    @Volatile
    private var INSTANCE: ProfileDatabase? = null

    fun getDatabase(context: Context): ProfileDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                ProfileDatabase::class.java,
                "gym_profile_private_db" // Tên file database hoàn toàn riêng biệt
            )
                .fallbackToDestructiveMigration()
                .build()
            INSTANCE = instance
            instance
        }
    }
}