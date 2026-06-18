package com.example.gymmentor.timer

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

        val exerciseName = intent.getStringExtra("EXERCISE_NAME") ?: "ĐANG TẬP LUYỆN"

        setContent {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = exerciseName.uppercase(),
                    color = Color.Red,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(40.dp))

                TimerScreen(
                    timeLeft = timeLeftInMillis,
                    btnText = buttonText,
                    onButtonClick = { handleMainButtonClick() },
                    onMinDecreaseClick = { changeTime(-60000L) },
                    onDecreaseClick = { changeTime(-10000L) },
                    onIncreaseClick = { changeTime(10000L) },
                    onMinIncreaseClick = { changeTime(60000L) }
                )
            }
        }
    }

    private fun handleMainButtonClick() {
        if (timerRunning) {
            stopTimer()
        } else {
            if (timeLeftInMillis <= 0L) {
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
        editor.putLong("timeLeft", timeLeftInMillis)
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

    private fun changeTime(amountInMillis: Long) {
        val newTime = timeLeftInMillis + amountInMillis
        timeLeftInMillis = if (newTime < 0) 0L else newTime

        if (timerRunning) {
            startTimer(timeLeftInMillis)
        } else {
            if (timeLeftInMillis == 0L) buttonText = "Vào hiệp thôi!"
            else if (buttonText == "Vào hiệp thôi!") buttonText = "Bắt đầu nghỉ"
        }
    }
}

@Composable
fun TimerScreen(
    timeLeft: Long,
    btnText: String,
    onButtonClick: () -> Unit,
    onIncreaseClick: () -> Unit,
    onDecreaseClick: () -> Unit,
    onMinDecreaseClick: () -> Unit,
    onMinIncreaseClick: () -> Unit
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
            fontSize = 75.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(35.dp))

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TimeAdjustmentButton(text = "-60s", onClick = onMinDecreaseClick, modifier = Modifier.weight(1f))
            TimeAdjustmentButton(text = "-10s", onClick = onDecreaseClick, modifier = Modifier.weight(1f))
            TimeAdjustmentButton(text = "+10s", onClick = onIncreaseClick, modifier = Modifier.weight(1f))
            TimeAdjustmentButton(text = "+60s", onClick = onMinIncreaseClick, modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(45.dp))

        Button(
            onClick = onButtonClick,
            modifier = Modifier
                .width(220.dp)
                .height(55.dp),
            shape = RoundedCornerShape(28.dp), // Bo tròn mềm mại chuẩn UI high-tech
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text(text = btnText, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun RowScope.TimeAdjustmentButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(45.dp)
            .background(Color(0xFF2C2C2E), shape = RoundedCornerShape(10.dp)) // Nền xám tối sang trọng
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(name = "Starboy Timer - Đang chạy nghỉ", showBackground = true)
@Composable
fun TimerScreenRunningPreview() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(20.dp)
    ) {
        TimerScreen(
            timeLeft = 45000L,
            btnText = "DỪNG",
            onButtonClick = {},
            onMinDecreaseClick = {},
            onDecreaseClick = {},
            onIncreaseClick = {},
            onMinIncreaseClick = {}
        )
    }
}