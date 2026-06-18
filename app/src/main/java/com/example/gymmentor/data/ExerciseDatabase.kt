package com.example.gymmentor.data

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSingleGuide(guide: ExerciseGuide): Long

    @Query("DELETE FROM exercises WHERE id = :exerciseId AND is_custom = 1")
    fun deleteCustomExerciseById(exerciseId: Int)
}


@Database(
    entities = [
        Exercise::class,
        WorkoutRoutine::class,
        RoutineExercise::class,
        UserStreak::class,
        ExerciseGuide::class,
        WorkoutHistory::class
    ],
    version = 9,
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
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            db.execSQL("INSERT INTO exercises (id, name, category, guide, type, main_image, star_rate, is_custom) VALUES (1, 'Bench Press', 'Ngực', 'Nằm đẩy tạ đòn phát triển ngực dày.', 'COMPOUND', 'cover_bench', 5, 0)")
                            db.execSQL("INSERT INTO exercises (id, name, category, guide, type, main_image, star_rate, is_custom) VALUES (2, 'Dumbbell Press', 'Ngực', 'Đẩy tạ đôi ghế dốc lên ăn vào ngực trên.', 'ISOLATION', 'cover_incline', 4, 0)")
                            db.execSQL("INSERT INTO exercises (id, name, category, guide, type, main_image, star_rate, is_custom) VALUES (3, 'Pull-up', 'Lưng', 'Hít xà đơn phát triển độ rộng của lưng X-Frame.', 'COMPOUND', 'cover_pullup', 5, 0)")
                            db.execSQL("INSERT INTO exercises (id, name, category, guide, type, main_image, star_rate, is_custom) VALUES (4, 'Barbell Row', 'Lưng', 'Chèo tạ đòn giúp lưng dày và khỏe.', 'COMPOUND', 'cover_row', 4, 0)")
                            db.execSQL("INSERT INTO exercises (id, name, category, guide, type, main_image, star_rate, is_custom) VALUES (5, 'Squat', 'Chân', 'Gánh tạ đòn - Vua của các bài tập chân.', 'COMPOUND', 'cover_squat', 5, 0)")
                            db.execSQL("INSERT INTO exercises (id, name, category, guide, type, main_image, star_rate, is_custom) VALUES (6, 'Lat Pulldown', 'Lưng', 'Ngồi kéo cáp phát triển độ rộng và độ dày cơ xô Lats.', 'COMPOUND', 'cover_lat_pulldown', 5, 0)")
                            db.execSQL("INSERT INTO exercises (id, name, category, guide, type, main_image, star_rate, is_custom) VALUES (7, 'Romanian Deadlift', 'Chân', 'Tập trung kéo giãn và xây dựng sức mạnh cho đùi sau và cơ mông săn chắc.', 'COMPOUND', 'cover_rdl', 4, 0)")

                             // NHÓM CƠ VAI (SHOULDERS)
                            db.execSQL("INSERT INTO exercises (id, name, category, guide, type, main_image, star_rate, is_custom) VALUES (8, 'Overhead Press', 'Vai', 'Đứng đẩy tạ đòn qua đầu giúp xây dựng bờ vai rộng bệ vệ và khỏe lực.', 'COMPOUND', 'cover_ohp', 5, 0)")
                            db.execSQL("INSERT INTO exercises (id, name, category, guide, type, main_image, star_rate, is_custom) VALUES (9, 'Dumbbell Lateral Raise', 'Vai', 'Dang tạ đôi sang hai bên cô lập cơ vai giữa, tạo độ tròn trịa quyến rũ cho cầu vai.', 'ISOLATION', 'cover_lateral_raise', 4, 0)")
                            // NHÓM CƠ TAY
                            db.execSQL("INSERT INTO exercises (id, name, category, guide, type, main_image, star_rate, is_custom) VALUES (10, 'Barbell Bicep Curl', 'Tay', 'Cuốn tạ đòn thẳng cô lập phát triển độ cao đỉnh cơ bắp tay trước.', 'ISOLATION', 'cover_bicep_curl', 4, 0)")


                            // Điền dữ liệu mẫu cho các bước hướng dẫn (giữ nguyên)
                            db.execSQL("INSERT INTO exercise_guides (exercise_id, image_name, instruction, step_number) VALUES (1, 'bench_setup', 'Nằm ngửa trên ghế bả vai ép chặt xuống mặt yên, chân đặt vững trên sàn.', 1)")
                            db.execSQL("INSERT INTO exercise_guides (exercise_id, image_name, instruction, step_number) VALUES (1, 'bench_down', 'Hít sâu, hạ thanh đòn có kiểm soát xuống vị trí ngang ngực (cách ngực 1-2cm).', 2)")
                            db.execSQL("INSERT INTO exercise_guides (exercise_id, image_name, instruction, step_number) VALUES (1, 'bench_up', 'Thở ra, dùng lực cơ ngực đẩy mạnh thanh đòn về vị trí ban đầu.', 3)")

                            // Lats Pulldown
                            db.execSQL("INSERT INTO exercise_guides (exercise_id, image_name, instruction, step_number) VALUES (6, 'lat_setup', 'Ngồi vững vàng vào máy, chỉnh đệm đùi chặt khít, chân đặt phẳng trên sàn. Hai tay bám thanh xà rộng hơn vai (Overhand Grip), hơi ngả người về sau (khoảng 10-15 độ), mở rộng lồng ngực đón tạ.', 1)")
                            db.execSQL("INSERT INTO exercise_guides (exercise_id, image_name, instruction, step_number) VALUES (6, 'lat_down', 'Thở ra, dùng bùa chú tư duy \"Kéo cùi chỏ xuống sườn\" chứ không dùng lực bắp tay kéo. Ưỡn ngực lên để đón thanh xà chạm nhẹ vào ngực trên. Ép chặt hai xương bả vai ra sau và giữ lại 1 nhịp ở đáy động tác. Không được ngả lưng ra sau quá nhiều.', 2)")
                            db.execSQL("INSERT INTO exercise_guides (exercise_id, image_name, instruction, step_number) VALUES (6, 'lat_up', 'Hít vào, từ từ nhả thanh xà ngược lên trên một cách chủ động và kiểm soát (không để tạ kéo giật đi tự do). Khi thanh xà lên đến đỉnh, rướn nhẹ vai lên để tạo cú giãn cơ xô (Big Stretch) tối đa trước khi vào hiệp tiếp theo.', 3)")

                            // --- BÀI 2: INCLINE DUMBBELL PRESS (ID = 2) ---
                            db.execSQL("INSERT INTO exercise_guides (exercise_id, image_name, instruction, step_number) VALUES (2, 'incline_setup', 'Chỉnh ghế dốc khoảng 30-45 độ. Ngồi vững, đặt 2 tạ đôi trên đùi, dùng đầu gối hất tạ lên đồng thời nằm ngửa ra ghế, giữ tạ sát ngực.', 1)")
                            db.execSQL("INSERT INTO exercise_guides (exercise_id, image_name, instruction, step_number) VALUES (2, 'incline_up', 'Thở ra, dùng cơ ngực trên đẩy mạnh tạ đôi thẳng lên theo đường vòng cung nhẹ, không để hai quả tạ đập vào nhau ở đỉnh.', 2)")
                            db.execSQL("INSERT INTO exercise_guides (exercise_id, image_name, instruction, step_number) VALUES (2, 'incline_down', 'Hít sâu, hạ tạ có kiểm soát xuống cho đến khi tạ ngang tầm ngực và cảm thấy cơ ngực trên được kéo giãn hết cỡ.', 3)")

// --- BÀI 3: PULL-UP (ID = 3) ---
                            db.execSQL("INSERT INTO exercise_guides (exercise_id, image_name, instruction, step_number) VALUES (3, 'pull_setup', 'Bám hai tay vào thanh xà rộng hơn vai, lòng bàn tay hướng về phía trước. Thả lỏng người treo tự do, ưỡn nhẹ ngực.', 1)")
                            db.execSQL("INSERT INTO exercise_guides (exercise_id, image_name, instruction, step_number) VALUES (3, 'pull_up', 'Thở ra, gồng cơ bụng, chủ động ép xương bả vai và kéo người lên cho đến khi cằm vượt qua thanh xà. Giữ khuỷu tay hướng xuống.', 2)")
                            db.execSQL("INSERT INTO exercise_guides (exercise_id, image_name, instruction, step_number) VALUES (3, 'pull_down', 'Hít vào, từ từ hạ thân người xuống một cách kiểm soát tuyệt đối cho đến khi tay thẳng trở lại tư thế ban đầu.', 3)")

// --- BÀI 4: BARBELL ROW (ID = 4) ---
                            db.execSQL("INSERT INTO exercise_guides (exercise_id, image_name, instruction, step_number) VALUES (4, 'row_setup', 'Đứng thẳng, hai chân rộng bằng vai. Cúi gập người từ hông xuống khoảng 45 độ, giữ lưng thẳng tuyệt đối, tay cầm thanh đòn buông thõng.', 1)")
                            db.execSQL("INSERT INTO exercise_guides (exercise_id, image_name, instruction, step_number) VALUES (4, 'row_up', 'Thở ra, dùng cơ lưng xô kéo thanh đòn về phía bụng dưới (gần rốn), ép chặt hai bả vai lại với nhau ở đỉnh chuyển động.', 2)")
                            db.execSQL("INSERT INTO exercise_guides (exercise_id, image_name, instruction, step_number) VALUES (4, 'row_down', 'Hít vào, từ từ duỗi tay hạ thanh đòn về vị trí ban đầu có kiểm soát, cảm nhận cơ lưng được kéo giãn.', 3)")

// --- BÀI 7: ROMANIAN DEADLIFT (ID = 7) ---
                            db.execSQL("INSERT INTO exercise_guides (exercise_id, image_name, instruction, step_number) VALUES (7, 'rdl_setup', 'Đứng thẳng giữ thanh đòn trước đùi, hai chân rộng bằng hông. Gồng chặt lõi cơ bụng, mở rộng ngực và bả vai.', 1)")
                            db.execSQL("INSERT INTO exercise_guides (exercise_id, image_name, instruction, step_number) VALUES (7, 'rdl_down', 'Hít vào, đẩy hông ra sau và từ từ hạ tạ dọc sát chân xuống qua đầu gối một chút. Giữ đầu gối hơi trùng, lưng thẳng, cảm nhận đùi sau căng căng.', 2)")
                            db.execSQL("INSERT INTO exercise_guides (exercise_id, image_name, instruction, step_number) VALUES (7, 'rdl_up', 'Thở ra, chủ động nhấn gót chân, đẩy hông về phía trước và siết chặt cơ mông để đứng thẳng người dậy.', 3)")

// --- BÀI 8: OVERHEAD PRESS (ID = 8) ---
                            db.execSQL("INSERT INTO exercise_guides (exercise_id, image_name, instruction, step_number) VALUES (8, 'ohp_setup', 'Đứng thẳng, hai chân bằng vai. Đặt thanh đòn ngang xương đòn (ngực trên), lòng bàn tay hướng ra trước, cùi chỏ hơi hướng về trước.', 1)")
                            db.execSQL("INSERT INTO exercise_guides (exercise_id, image_name, instruction, step_number) VALUES (8, 'ohp_up', 'Thở ra, gồng chặt mông bụng, đẩy tạ thẳng qua đầu. Hơi ngả đầu ra sau lúc tạ đi qua mặt, rồi đưa đầu về giữa khi tạ lên đỉnh.', 2)")
                            db.execSQL("INSERT INTO exercise_guides (exercise_id, image_name, instruction, step_number) VALUES (8, 'ohp_down', 'Hít vào, hạ thanh đòn có kiểm soát theo đường thẳng dọc sát mặt về lại vị trí ngực trên ban đầu.', 3)")

// --- BÀI 9: DUMBBELL LATERAL RAISE (ID = 9) ---
                            db.execSQL("INSERT INTO exercise_guides (exercise_id, image_name, instruction, step_number) VALUES (9, 'lateral_setup', 'Đứng thẳng, mỗi tay cầm một quả tạ đôi buông xuôi hai bên hông. Hơi cúi người về trước một chút, cùi chỏ hơi cong nhẹ.', 1)")
                            db.execSQL("INSERT INTO exercise_guides (exercise_id, image_name, instruction, step_number) VALUES (9, 'lateral_up', 'Thở ra, dùng cơ vai giữa chủ động dang hai tay sang hai bên cho đến khi cánh tay song song với mặt sàn. Giữ cùi chỏ cao hơn cổ tay.', 2)")
                            db.execSQL("INSERT INTO exercise_guides (exercise_id, image_name, instruction, step_number) VALUES (9, 'lateral_down', 'Hít vào, từ từ hạ tạ về vị trí ban đầu một cách chậm rãi để duy trì áp lực liên tục lên cơ vai.', 3)")

// --- BÀI 10: BARBELL BICEP CURL (ID = 10) ---
                            db.execSQL("INSERT INTO exercise_guides (exercise_id, image_name, instruction, step_number) VALUES (10, 'curl_setup', 'Đứng thẳng, hai tay cầm thanh đòn rộng bằng vai, lòng bàn tay hướng ra ngoài. Giữ cùi chỏ ép sát hai bên sườn.', 1)")
                            db.execSQL("INSERT INTO exercise_guides (exercise_id, image_name, instruction, step_number) VALUES (10, 'curl_up', 'Thở ra, cố định bả vai và cùi chỏ, dùng cơ bắp tay trước cuộn tạ lên phía ngực. Siết chặt bắp tay ở đỉnh 1 giây.', 2)")
                            db.execSQL("INSERT INTO exercise_guides (exercise_id, image_name, instruction, step_number) VALUES (10, 'curl_down', 'Hít vào, hạ tạ xuống từ từ có kiểm soát cho đến khi tay duỗi thẳng hoàn toàn.', 3)")

                            // --- BÀI 11: GOBLET SQUAT (ID = 11) ---
                            db.execSQL("INSERT INTO exercise_guides (exercise_id, image_name, instruction, step_number) VALUES (5, 'squat_setup', 'Đứng thẳng, hai chân mở rộng bằng vai hoặc hơn một chút, mũi chân hơi hướng ra ngoài 15 độ. Hai tay ôm chặt một quả tạ đôi trước ngực, tì sát vào người.', 1)")
                            db.execSQL("INSERT INTO exercise_guides (exercise_id, image_name, instruction, step_number) VALUES (5, 'squat_down', 'Hít vào, gồng chặt bụng, từ từ đẩy hông ra sau và hạ thấp trọng tâm xuống thẳng hàng giống như ngồi xổm bình thường, cho đến khi mông sâu xuống bằng hoặc qua đầu gối.', 2)")
                            db.execSQL("INSERT INTO exercise_guides (exercise_id, image_name, instruction, step_number) VALUES (5, 'squat_up', 'Thở ra, giữ lưng thẳng, dồn lực nhấn mạnh gót chân xuống sàn đẩy người đứng thẳng dậy mạnh mẽ về vị trí ban đầu.', 3)")

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