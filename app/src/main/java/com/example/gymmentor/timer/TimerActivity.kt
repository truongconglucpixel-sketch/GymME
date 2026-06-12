package com.example.gymmentor.timer

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class TimerActivity : ComponentActivity() {

    private var countDownTimer: CountDownTimer? = null
    private val defaultRestTime = 60000L
    private var timeLeftInMillis by mutableStateOf(defaultRestTime)
    private var timerRunning by mutableStateOf(false)
    private var buttonText by mutableStateOf("Bắt đầu nghỉ")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Nhận tên bài tập từ ExerciseLibraryActivity gửi sang
        val exerciseName = intent.getStringExtra("EXERCISE_NAME") ?: "ĐANG TẬP LUYỆN"

        setContent {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Hiển thị tên bài tập ở thớt trên cho rực rỡ
                Text(
                    text = exerciseName.uppercase(),
                    color = Color.Red, // Màu đỏ Neon gắt bướng
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(30.dp))

                TimerScreen(
                    timeLeft = timeLeftInMillis,
                    btnText = buttonText,
                    onButtonClick = { handleMainButtonClick()},
                    onDecreaseClick = { changeTime(-10000L)},
                    onIncreaseClick = { changeTime(10000L)}
                )
            }
        }
    }

    private fun handleMainButtonClick() {
        if(timerRunning) {
            stopTimer()
        } else {
            if(timeLeftInMillis <= 0L) {
                timeLeftInMillis = defaultRestTime
                buttonText = "Bắt đầu nghỉ"
            } else startTimer(timeLeftInMillis)
        }
    }

    private fun startTimer(duration: Long) {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
            }

            override fun onFinish() {
                timerRunning = false
                buttonText = "Vào hiệp thôi!"
            }
        }.start()

        timerRunning = true
        buttonText = "Dừng"
    }

    private fun stopTimer() {
        countDownTimer?.cancel()
        timerRunning = false
        buttonText = "Tiếp tục"
    }

    override fun onPause() {
        super.onPause()
        val prefs = getSharedPreferences("GymMentorPrefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        val endTime = System.currentTimeMillis() + timeLeftInMillis
        editor.putLong("endTime", endTime)
        editor.putBoolean("timerRunning", timerRunning)
        editor.putLong("timeLeft", timeLeftInMillis) // Key là "timeLeft"
        editor.apply()

        countDownTimer?.cancel()
    }

    override fun onResume() {
        super.onResume()
        val prefs = getSharedPreferences("GymMentorPrefs", Context.MODE_PRIVATE)
        timerRunning = prefs.getBoolean("timerRunning", false)
        timeLeftInMillis = prefs.getLong("timeLeft", 60000L)

        if (timerRunning) {
            val endTime = prefs.getLong("endTime", 0L)
            timeLeftInMillis = endTime - System.currentTimeMillis()

            if (timeLeftInMillis < 0) {
                timeLeftInMillis = 0
                timerRunning = false
                buttonText = "Vào hiệp thôi!"
            } else {
                startTimer(timeLeftInMillis)
            }
        } else {
            buttonText = if (timeLeftInMillis == 0L) "Vào hiệp thôi!" else "Bắt đầu nghỉ"
        }
    }
    private fun changeTime(amountInMillis: Long){
        val newTime = timeLeftInMillis + amountInMillis
        timeLeftInMillis = if (newTime < 0) 0L else newTime

        if(timerRunning){
            startTimer(timeLeftInMillis)
        } else {
            if (timeLeftInMillis == 0L) buttonText = "Vào hiệp thôi"
            else if (buttonText == "Vào hiệp thôi")
                buttonText = "Bắt đầu nghỉ"
        }
    }
}


@Composable
fun TimerScreen(
    timeLeft: Long,
    btnText: String,
    onButtonClick: () -> Unit,
    onIncreaseClick: () -> Unit,
    onDecreaseClick: () -> Unit
) {
    val minutes = (timeLeft / 1000) / 60
    val seconds = (timeLeft / 1000) % 60
    val timeString = String.format("%02d:%02d", minutes, seconds)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = timeString,
            color = Color.White,
            fontSize = 70.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(20.dp))


        Row(
            horizontalArrangement = Arrangement.spacedBy(25.dp)
        ) {
            Button(
                onClick = onDecreaseClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Text("-10s", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Button(
                onClick = onIncreaseClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
            ) {
                Text("+10s", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(35.dp))

        }
        Button(onClick = onButtonClick,
            modifier = Modifier.width(200.dp).height((50.dp)),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
            Text(text = btnText, fontSize = 20.sp)
        }
    }
}

@Preview(name = "Starboy Timer - Đang chạy nghỉ", showBackground = true)
@Composable
fun TimerScreenRunningPreview() {
    TimerScreen(
        timeLeft = 45000L,
        btnText = "DỪNG",
        onButtonClick = {},
        onDecreaseClick = {},
        onIncreaseClick = {}
    )
}