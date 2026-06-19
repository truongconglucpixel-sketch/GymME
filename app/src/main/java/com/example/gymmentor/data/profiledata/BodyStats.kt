package com.example.gymmentor.data.profiledata

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile_body_stats")
data class BodyStats(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val weight: Float,
    val height: Float,
    val bodyFat: Float,
    val date: Long
)