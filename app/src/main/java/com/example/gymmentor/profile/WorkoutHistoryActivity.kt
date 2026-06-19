package com.example.gymmentor.profile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymmentor.data.ExerciseData.AppDatabase
import com.example.gymmentor.data.ExerciseData.WorkoutHistory
import java.text.SimpleDateFormat
import java.util.*

class WorkoutHistoryActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(this)

        setContent {
            // 🚨 HỨNG DỮ LIỆU ĐỘNG: Lắng nghe luồng dữ liệu Flow phát ra từ DB
            val historyList by database.workoutDao().getAllHistory().collectAsState(initial = emptyList())

            Scaffold(
                containerColor = Color.Black // Ép luôn nền đen cho toàn bộ màn hình
            ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                // TIÊU ĐỀ MÀN HÌNH NHẬT KÝ
                Text(
                    text = "NHẬT KÝ TẬP LUYỆN",
                    color = Color.Red,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "LỊCH SỬ KHÓA XÍCH CÀY TẠ CỦA BẠN",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(20.dp))

                // CHECK XEM CÓ DỮ LIỆU CHƯA
                if (historyList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Chưa cày buổi tạ nào hết bác ơi!\nVào Gói Tập triển ngay phát nào 🔥",
                            color = Color.DarkGray,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    // DANH SÁCH LỊCH SỬ CUỘN DỌC
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(historyList) { history ->
                            HistoryItemCard(history = history)
                        }
                    }
                }
            }
        }
    }
}
}

// 🎨 COMPONENT VẼ TỪNG THẺ LỊCH SỬ ĐẬM CHẤT GYM
@Composable
fun HistoryItemCard(history: WorkoutHistory) {
    // Hàm format chuyển từ dạng Long Timestamp (Milli giây) sang chuỗi ngày tháng dễ nhìn
    val formattedDate = rememberFormattedDate(history.loggedAt)

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)), // Nền xám đen sâu
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
            // KHỐI BÊN TRÁI: Tên gói tập + Số bài tập đã nuốt
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "📋  ${history.routineName.uppercase()}",
                    color = Color.Yellow, // Bật tông rực rỡ
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

            // KHỐI BÊN PHẢI: Ngày giờ hoàn thành hoàng đạo
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formattedDate.first, // Ngày (Ví dụ: 18 Th06, 2026)
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formattedDate.second, // Giờ (Ví dụ: 14:30)
                    color = Color.Gray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

// Hàm bổ trợ xử lý chuyển đổi thời gian thông minh
@Composable
fun rememberFormattedDate(timestamp: Long): Pair<String, String> {
    return androidx.compose.runtime.remember(timestamp) {
        val sdfDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val sdfTime = SimpleDateFormat("HH:mm", Locale.getDefault())
        val dateStr = sdfDate.format(Date(timestamp))
        val timeStr = sdfTime.format(Date(timestamp))
        Pair(dateStr, timeStr)
    }
}