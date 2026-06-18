package com.example.gymmentor.timer

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymmentor.data.AppDatabase
import com.example.gymmentor.data.ExerciseGuide
import com.example.gymmentor.data.UserStreak
import com.example.gymmentor.data.WorkoutHistory
import kotlinx.coroutines.launch

class LiveWorkoutActivity : ComponentActivity() {

    private var countDownTimer: CountDownTimer? = null
    private val defaultRestTime = 60000L
    private var timeLeftInMillis by mutableStateOf(defaultRestTime)
    private var timerRunning by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val routineId = intent.getIntExtra("ROUTINE_ID", 0)
        val routineName = intent.getStringExtra("ROUTINE_NAME") ?: "BUỔI TẬP"
        val database = AppDatabase.getDatabase(this)

        setContent {
            // Giỏ hàng
            val exercisesInRoutine by database.workoutDao().getExercisesFromRoutine(routineId).collectAsState(initial = emptyList())
            // Bài thứ mấy
            var currentExerciseIndex by remember {mutableStateOf(0)}
            // Số hiệp hiện tại
            var currentSet by remember { mutableStateOf(1) }
            // Danh sách hướng dẫn
            var currentGuideSteps by remember { mutableStateOf<List<ExerciseGuide>>(emptyList()) }

            val coroutineScope = rememberCoroutineScope()
            val context = LocalContext.current

            LaunchedEffect(exercisesInRoutine, currentExerciseIndex) {
                if(exercisesInRoutine.isNotEmpty() && currentExerciseIndex < exercisesInRoutine.size){
                    val exerciseId = exercisesInRoutine[currentExerciseIndex].exerciseId
                    // Lôi dữ liệu ra
                    currentGuideSteps = database.exerciseDao().getGuidesForExercise(exerciseId)
                }
            }

            Scaffold(
                containerColor = Color.Black
            ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(16.dp)
                    .padding(paddingValues)
            ){
                Row(
                    modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = routineName.uppercase(), color = Color.Gray, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    if (exercisesInRoutine.isNotEmpty()){
                        Text(
                            text = "Bài: ${currentExerciseIndex + 1}/${exercisesInRoutine.size}",
                            color = Color.Yellow, fontSize = 14.sp, fontWeight = FontWeight.Bold
                        )
                    }
                }
                if (exercisesInRoutine.isNotEmpty() && currentExerciseIndex < exercisesInRoutine.size) {
                    val currentExercise = exercisesInRoutine[currentExerciseIndex]

                    // TÊN BÀI TẬP TO RỰC RỠ (Bê từ ExerciseDetailActivity)
                    Text(
                        text = currentExercise.name.uppercase(),
                        color = Color.Red, fontSize = 24.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                    )

                    // 🚨 KHỐI TIMER ĐẾM NGƯỢC THỜI GIAN NGHỈ (Hợp thể từ TimerActivity)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1C1C1E), shape = RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "HIỆP: $currentSet / ${currentExercise.targetSets}",
                                    color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold
                                )

                                // Chuỗi chu kỳ đồng hồ đếm ngược số
                                val minutes = (timeLeftInMillis / 1000) / 60
                                val seconds = (timeLeftInMillis / 1000) % 60
                                Text(
                                    text = String.format("%02d:%02d", minutes, seconds),
                                    color = if (timerRunning) Color.Green else Color.LightGray,
                                    fontSize = 28.sp, fontWeight = FontWeight.Black
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Hàng nút chỉnh thời gian nhanh (Bê nguyên xì từ TimerActivity của bác sang)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val btnMod = Modifier.weight(1f).height(38.dp)
                                Box(modifier = btnMod.background(Color(0xFF2C2C2E), RoundedCornerShape(8.dp)).clickable { changeTime(-10000L) }, contentAlignment = Alignment.Center) { Text("-10s", color = Color.White, fontSize = 12.sp) }
                                Box(modifier = btnMod.background(Color(0xFF2C2C2E), RoundedCornerShape(8.dp)).clickable { changeTime(10000L) }, contentAlignment = Alignment.Center) { Text("+10s", color = Color.White, fontSize = 12.sp) }

                                // Nút HOÀN THÀNH HIỆP TẬP / KÍCH HOẠT TIMER NGHỈ
                                Button(
                                    onClick = {
                                        if (timerRunning) {
                                            stopTimer() // Đang nghỉ mà muốn tập luôn thì ấn Dừng nghỉ
                                        } else {
                                            // Nếu chưa hết số hiệp mục tiêu của bài này
                                            if (currentSet < currentExercise.targetSets) {
                                                currentSet++
                                                startTimer(defaultRestTime) // Kích hoạt đếm ngược thời gian nghỉ liền
                                            } else {
                                                // Nếu đã cày xong hiệp cuối cùng (vd: 4/4) -> Đổi bài tập kế tiếp
                                                if (currentExerciseIndex < exercisesInRoutine.size - 1) {
                                                    currentExerciseIndex++
                                                    currentSet = 1 // Reset về hiệp 1 cho bài mới
                                                    stopTimer()
                                                    timeLeftInMillis = defaultRestTime
                                                    Toast.makeText(context, "Qua bài tiếp theo!", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    coroutineScope.launch{
                                                        val currentMillis = System.currentTimeMillis()

                                                        var currentStreak = database.workoutDao().getUserStreak()

                                                        val updatedStreak = if (currentStreak == null){
                                                            UserStreak(
                                                                id = 1,
                                                                streakCount = 1,
                                                                lastWorkoutDate = currentMillis
                                                            )
                                                        } else {
                                                            currentStreak.copy(streakCount = currentStreak.streakCount + 1, lastWorkoutDate = currentMillis)
                                                        }

                                                        database.workoutDao().saveUserStreak(updatedStreak)

                                                        database.workoutDao().insertHistory(
                                                            WorkoutHistory(
                                                                routineName = routineName,
                                                                loggedAt = currentMillis,
                                                                totalExercises = exercisesInRoutine.size
                                                            )
                                                        )
                                                    }
                                                    Toast.makeText(context, "🔥 QUÁ ĐỈNH! BẠN ĐÃ HOÀN THÀNH BUỔI TẬP!", Toast.LENGTH_LONG).show()
                                                    finish()
                                                }
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(2f).height(38.dp)
                                ) {
                                    Text(
                                        text = if (timerRunning) "BỎ QUA NGHỈ" else if (currentSet == currentExercise.targetSets) "XONG BÀI" else "XONG SET",
                                        fontSize = 12.sp, fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "HƯỚNG DẪN KỸ THUẬT FORM CHUẨN", color = Color.Yellow, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Thớt 3: Hiển thị các bước kỹ thuật (Bê nguyên xì giao diện DetailStepCard của bác sang)
                    if (currentGuideSteps.isEmpty()) {
                        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("Đang nạp hướng dẫn kỹ thuật...", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(currentGuideSteps) { step ->
                                val imgResId = remember(step.imageName) {
                                    context.resources.getIdentifier(step.imageName, "drawable", context.packageName)
                                }
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Image(
                                            painter = painterResource(id = if (imgResId != 0) imgResId else android.R.drawable.ic_menu_gallery),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxWidth().height(160.dp).background(Color.DarkGray)
                                        )
                                        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                                            Text(text = "BƯỚC ${step.stepNumber}:", color = Color.Red, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(text = step.instruction, color = Color.White, fontSize = 13.sp, lineHeight = 18.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

    // --- CÁC HÀM ĐIỀU KHIỂN TIMING LÕI CỦA BÁC (Đã đồng bộ) ---
    private fun startTimer(duration: Long) {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
            }
            override fun onFinish() {
                timerRunning = false
                timeLeftInMillis = defaultRestTime
            }
        }.start()
        timerRunning = true
    }

    private fun stopTimer() {
        countDownTimer?.cancel()
        timerRunning = false
    }

    private fun changeTime(amountInMillis: Long) {
        val newTime = timeLeftInMillis + amountInMillis
        timeLeftInMillis = if (newTime < 0) 0L else newTime
        if (timerRunning) startTimer(timeLeftInMillis)
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}