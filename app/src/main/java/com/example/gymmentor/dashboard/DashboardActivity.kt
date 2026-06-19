package com.example.gymmentor.dashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.gymmentor.ui.theme.GymMentorTheme

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GymMentorTheme {
                SkillTreeScreen()   // ← đổi từ DashboardScreen() sang đây
            }
        }
    }
}