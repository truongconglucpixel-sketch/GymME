package com.example.gymmentor.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DashboardScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // 1. Header: Hiển thị Streak
        StreakHeader(streakDays = 5) // Số liệu cứng để test UI

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Daily Missions
        Text(
            text = "Daily Missions",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        MissionItem(title = "Hoàn thành 30 phút Cardio", isCompleted = false)
        MissionItem(title = "Log cân nặng hôm nay", isCompleted = true)

        Spacer(modifier = Modifier.height(32.dp))

        // 3. Khu vực Skill Tree
        Text(
            text = "Muscle Skill Tree",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
        SkillTreeComponent()
    }
}

@Composable
fun StreakHeader(streakDays: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = "🔥", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$streakDays Day Streak",
            color = MaterialTheme.colorScheme.tertiary,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun MissionItem(title: String, isCompleted: Boolean) {
    // Phân tích: Cần dùng Card hoặc Box để tạo UI cho từng task.
    // Xử lý logic đổi màu NeonGreen nếu isCompleted == true.
    // ... Cần triển khai thêm UI chi tiết tại đây.
}