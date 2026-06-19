package com.example.gymmentor.data.ExerciseData

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_history")
data class WorkoutHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    @ColumnInfo(name = "routine_name") val routineName: String,

    @ColumnInfo(name = "logged_at") val loggedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "total_exercises") val totalExercises: Int
)