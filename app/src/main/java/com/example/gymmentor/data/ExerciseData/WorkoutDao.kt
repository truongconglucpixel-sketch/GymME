package com.example.gymmentor.data.ExerciseData

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    @Insert
    suspend fun insertRoutine(routine: WorkoutRoutine): Long

    @Query("SELECT * FROM workout_routines ORDER BY created_at DESC")
    fun getAllRoutines(): Flow<List<WorkoutRoutine>>

    @Query("DELETE FROM workout_routines WHERE id = :routineId")
    suspend fun deleteRoutineById(routineId: Int)

    // Chi tiết gói tập
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addExerciseToRoutine(routineExercise: RoutineExercise)

    @Query("DELETE From routine_exercises Where routine_id = :routineId and exercise_id = :exerciseId")
    suspend fun removeExerciseFromRoutine(routineId: Int, exerciseId: Int)

    @Query("Update routine_exercises SET target_sets = :newSets Where routine_id = :routineId and exercise_id = :exerciseId")
    suspend fun updateTargetSets(routineId: Int, exerciseId: Int, newSets: Int)

    // 🚨 ĐÃ FIX TRIỆT ĐỂ: Thêm 'AS mainImage' và 'AS isCustom' để Room map đúng vào Data Class của bác!
    @Query("""
        SELECT 
            e.id AS exerciseId, 
            e.name AS name, 
            e.main_image AS mainImage, 
            e.type AS type, 
            e.guide AS guide, 
            r.target_sets AS targetSets, 
            e.is_custom AS isCustom
        FROM routine_exercises r         
        INNER JOIN exercises e ON r.exercise_id = e.id
        WHERE r.routine_id = :routineId
    """)
    fun getExercisesFromRoutine(routineId: Int): Flow<List<ExerciseWithTargetSets>>

    @Query("SELECT * FROM user_streaks WHERE id = 1")
    suspend fun getUserStreak(): UserStreak?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserStreak(streak: UserStreak)

    //check xem đã có bài trong gói chưa
    @Query("SELECT COUNT(*) FROM routine_exercises WHERE routine_id = :routineId AND exercise_id = :exerciseId")
    suspend fun isExerciseInRoutine(routineId: Int, exerciseId: Int): Int

    @Insert
    suspend fun   insertHistory(history: WorkoutHistory)

    @Query("SELECT * FROM workout_history ORDER BY logged_at DESC")
    fun getAllHistory(): Flow<List<WorkoutHistory>>
}