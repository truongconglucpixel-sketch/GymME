package com.example.gymmentor.data.profiledata

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile_users")
data class User(
    @PrimaryKey
    val id: Int = 1, // Luôn cố định là 1 để chỉ lưu duy nhất 1 hồ sơ của chủ máy
    val name: String,
    val age: Int,
    val height: Float,
    val weight: Float,
    val bodyFat: Float
)