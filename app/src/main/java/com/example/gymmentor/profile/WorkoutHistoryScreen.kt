package com.example.gymmentor.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymmentor.data.ExerciseData.AppDatabase
import com.example.gymmentor.data.ExerciseData.WorkoutHistory
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WorkoutHistoryScreen() {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val historyList by database.workoutDao().getAllHistory().collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (historyList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Chưa cày buổi tạ nào hết bác ơi!\nVào Gói Tập triển ngay phát nào 🔥",
                    color = Color.DarkGray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(historyList) { history ->
                    // 🚨 ĐÃ ĐỔI: Gọi tên hàm card mới đổi
                    HistoryItemWorkoutCard(history = history)
                }
            }
        }
    }
}

// 🚨 ĐÃ ĐỔI TÊN HÀM: Tránh trùng lặp với file Activity cũ
@Composable
fun HistoryItemWorkoutCard(history: WorkoutHistory) {
    // 🚨 ĐÃ ĐỔI TÊN HÀM: Gọi hàm format thời gian mới đổi tên
    val formattedDate = rememberWorkoutFormattedDate(history.loggedAt)

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "📋  ${history.routineName.uppercase()}",
                    color = Color.Yellow,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Đã hoàn thành: ${history.totalExercises} bài tập",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formattedDate.first, // Ăn chặt thuộc tính .first ngon lành
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formattedDate.second, // Ăn chặt thuộc tính .second ngon lành
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

// 🚨 ĐÃ ĐỔI TÊN HÀM: Định dạng ngày giờ độc quyền cho Screen này
@Composable
fun rememberWorkoutFormattedDate(timestamp: Long): Pair<String, String> {
    return remember(timestamp) {
        val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateStr = sdfDate.format(Date(timestamp))
        val timeStr = sdfTime.format(Date(timestamp))
        Pair(dateStr, timeStr)
    }
}