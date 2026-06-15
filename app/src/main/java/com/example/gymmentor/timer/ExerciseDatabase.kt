package com.example.gymmentor.timer

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "guide") val guide: String,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "main_image") val mainImage: String,
    @ColumnInfo(name = "star_rate") val starRate: Int,
    @ColumnInfo(name = "is_custom") val isCustom: Boolean = false // Số sao đánh giá
)

@Entity(tableName = "workout_routines")
data class WorkoutRoutine(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "routine_exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutRoutine::class,
            parentColumns = ["id"],
            childColumns = ["routine_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exercise_id"],
            onDelete = ForeignKey.CASCADE
        )
    ], indices = [
        Index(value = ["routine_id", "exercise_id"], unique = true)
    ]
)
data class RoutineExercise(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "routine_id") val routineId: Int,
    @ColumnInfo(name = "exercise_id") val exerciseId: Int,
    @ColumnInfo(name = "target_sets") val targetSets: Int = 4
)

@Entity(tableName = "user_streaks")
data class UserStreak(
    @PrimaryKey val id: Int = 1,
    @ColumnInfo(name = "streak_count") val streakCount: Int = 0,
    @ColumnInfo(name = "last_workout_date") val lastWorkoutDate: Long = 0
)

@Entity(
    tableName = "exercise_guides",
    foreignKeys = [
        ForeignKey(
            entity = Exercise::class,
            parentColumns = ["id"],
            childColumns = ["exercise_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["exercise_id"])]
)
data class ExerciseGuide(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "exercise_id") val exerciseId: Int,
    @ColumnInfo(name = "image_name") val imageName: String,
    @ColumnInfo(name = "instruction") val instruction: String,
    @ColumnInfo(name = "step_number") val stepNumber: Int
)

data class ExerciseWithTargetSets(
    val exerciseId: Int,
    val name: String,
    val mainImage: String,
    val type: String,
    val guide: String,
    val targetSets: Int,
    val isCustom: Int
)

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercises")
    fun getAllExercise(): List<Exercise>

    @Query("SELECT * FROM exercises WHERE category = :cat")
    fun getExerciseByCategory(cat: String): List<Exercise>

    @Query("SELECT * FROM exercise_guides WHERE exercise_id = :exerciseId ORDER BY step_number ASC")
    fun getGuidesForExercise(exerciseId: Int): List<ExerciseGuide>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSingleExercise(exercise: Exercise): Long
}

// 🚨 ĐÃ FIX: Khai báo đầy đủ cả 2 bảng và nâng lên version 3
@Database(
    entities = [
        Exercise::class,
        WorkoutRoutine::class,
        RoutineExercise::class,
        UserStreak::class,
        ExerciseGuide::class 
    ],
    version = 7,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun workoutDao(): WorkoutDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gym_mentor_db"
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // 🚨 ĐÃ FIX: Điền đầy đủ dữ liệu cho cả 7 cột của bài tập để không bị nổ SQL
                            db.execSQL("INSERT INTO exercises (id, name, category, guide, type, main_image, star_rate, is_custom) VALUES (1, 'Bench Press', 'Ngực', 'Nằm đẩy tạ đòn phát triển ngực dày.', 'COMPOUND', 'cover_bench', 5, 0)")
                            db.execSQL("INSERT INTO exercises (id, name, category, guide, type, main_image, star_rate, is_custom) VALUES (2, 'Dumbbell Press', 'Ngực', 'Đẩy tạ đôi ghế dốc lên ăn vào ngực trên.', 'ISOLATION', 'cover_incline', 4, 0)")
                            db.execSQL("INSERT INTO exercises (id, name, category, guide, type, main_image, star_rate, is_custom) VALUES (3, 'Pull-up', 'Lưng', 'Hít xà đơn phát triển độ rộng của lưng X-Frame.', 'COMPOUND', 'cover_pullup', 5, 0)")
                            db.execSQL("INSERT INTO exercises (id, name, category, guide, type, main_image, star_rate, is_custom) VALUES (4, 'Barbell Row', 'Lưng', 'Chèo tạ đòn giúp lưng dày và khỏe.', 'COMPOUND', 'cover_row', 4, 0)")
                            db.execSQL("INSERT INTO exercises (id, name, category, guide, type, main_image, star_rate, is_custom) VALUES (5, 'Squat', 'Chân', 'Gánh tạ đòn - Vua của các bài tập chân.', 'COMPOUND', 'cover_squat', 5, 0)")

                            // Điền dữ liệu mẫu cho các bước hướng dẫn (giữ nguyên)
                            db.execSQL("INSERT INTO exercise_guides (exercise_id, image_name, instruction, step_number) VALUES (1, 'bench_setup', 'Nằm ngửa trên ghế bả vai ép chặt xuống mặt yên, chân đặt vững trên sàn.', 1)")
                            db.execSQL("INSERT INTO exercise_guides (exercise_id, image_name, instruction, step_number) VALUES (1, 'bench_down', 'Hít sâu, hạ thanh đòn có kiểm soát xuống vị trí ngang ngực (cách ngực 1-2cm).', 2)")
                            db.execSQL("INSERT INTO exercise_guides (exercise_id, image_name, instruction, step_number) VALUES (1, 'bench_up', 'Thở ra, dùng lực cơ ngực đẩy mạnh thanh đòn về vị trí ban đầu.', 3)")
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}