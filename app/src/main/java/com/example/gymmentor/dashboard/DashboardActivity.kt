package com.example.gymmentor.dashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.gymmentor.ui.theme.GymMentorTheme // Đảm bảo import đúng đường dẫn Theme của nhóm

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Bao bọc giao diện bằng Theme để nhận các giá trị màu Neon & Dark mode
            GymMentorTheme {
                DashboardScreen()
            }
        }
    }
}