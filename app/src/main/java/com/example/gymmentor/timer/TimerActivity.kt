package com.example.gymmentor.timer

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class TimerActivity : ComponentActivity(){

    private var countDownTimer: CountDownTimer? = null

    private var timeLeftInMillis by mutableStateOf(60000L)
    private var timerRunning by mutableStateOf(false)
    private var buttonText by mutableStateOf("Bắt đầu nghỉ")

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContent {
            TimerScreen(
                timeLeft = timeLeftInMillis,
                btnText = buttonText,
                onButtonClick = {
                    if (timerRunning) stopTimer() else starTimer(timeLeftInMillis)
                }
            )
        }
    }

    private fun starTimer(duration: Long){
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(duration, 1000){
            override fun onTick(millisUntilFinished: Long){
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

    private fun stopTimer(){
        countDownTimer?.cancel()

        timerRunning = false
        buttonText = "Tiếp tục"
    }

    override fun onPause(){
        super.onPause()
        val prefs = getSharedPreferences("GymMentorPrefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        val endTime = System.currentTimeMillis() + timeLeftInMillis
        editor.putLong("endTime", endTime)
        editor.putBoolean("timerRunning", timerRunning)
        editor.putLong("timeLeft", timeLeftInMillis)
        editor.apply()

        countDownTimer?.cancel()
    }

    override fun onResume(){
        super.onResume()
        val prefs = getSharedPreferences("GymMentorPrefs", Context.MODE_PRIVATE)
        timerRunning = prefs.getBoolean("timerRunning", false)
        timeLeftInMillis = prefs.getLong("timerLeft", 60000L)

        if (timerRunning){
            val endTime = prefs.getLong("endTime", 0L)

            timeLeftInMillis = endTime - System.currentTimeMillis()

            if (timeLeftInMillis < 0){
                timeLeftInMillis = 0
                timerRunning = false
                buttonText = "Vào hiệp thôi!"
            } else {
                starTimer(timeLeftInMillis)
            }
        } else {
            buttonText = if (timeLeftInMillis == 0L) "Vào hiệp thôi!" else "Bắt đầu nghỉ"
        }
    }
}

@Composable
fun TimerScreen(
    timeLeft: Long,
    btnText: String,
    onButtonClick: () -> Unit
) {
    val minutes = (timeLeft / 1000) / 60
    val seconds = (timeLeft / 1000) % 60
    val timeString = String.format("%02d:%02d", minutes, seconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black), // Nền đen tuyền Starboy
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = timeString,
            color = Color.White,
            fontSize = 70.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(40.dp))
        Button(onClick = onButtonClick) {
            Text(text = btnText, fontSize = 20.sp)
        }
    }
}

@Preview(name = "Starboy Timer - Đang chạy nghỉ", showBackground = true)
@Composable
fun TimerScreenRunningPreview() {
    // Truyền dữ liệu giả (Placeholder) vào để xem trước lúc timer đang chạy
    TimerScreen(
        timeLeft = 45000L, // Thử xem số 45 giây hiện lên thế nào
        btnText = "DỪNG",
        onButtonClick = {} // Hàm rỗng, preview chỉ để ngắm chứ không cần bấm logic
    )
}

@Preview(name = "Starboy Timer - Hết giờ nghỉ", showBackground = true)
@Composable
fun TimerScreenFinishedPreview() {
    // Thử xem giao diện lúc hết giờ nghỉ sẽ trông ra sao
    TimerScreen(
        timeLeft = 0L,
        btnText = "VÀO HIỆP THÔI!",
        onButtonClick = {}
    )
}