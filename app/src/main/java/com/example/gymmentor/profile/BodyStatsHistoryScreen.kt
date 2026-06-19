package com.example.gymmentor.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymmentor.data.profiledata.ProfileDatabase
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BodyStatsHistoryScreen(db: ProfileDatabase) {
    val statsList by db.bodyStatsDao().getAllStats().collectAsState(initial = emptyList())

    if (statsList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text("Chưa có dữ liệu lịch sử. Hãy bấm lưu ở tab Hồ Sơ!", color = Color.Gray, fontSize = 14.sp)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(statsList) { stat ->

                // Tính toán nhanh chỉ số BMI và FFMI để hiển thị trong lịch sử (Mặc định FFMI giả định BodyFat là 15% nếu không lưu kèm)
                val hInMeters = stat.height / 100f
                val bmi = if (hInMeters > 0) stat.weight / (hInMeters * hInMeters) else 0f
                // Lấy đúng phần trăm mỡ lưu kèm trong dòng lịch sử đó (nếu bằng 0 thì dự phòng mức mỡ trung bình)
                val currentFat = if (stat.bodyFat > 0f) stat.bodyFat else 15f
                val leanWeight = stat.weight * (1f - (currentFat / 100f))
                // Ép công thức tính toán FFMI chuẩn chỉ
                val ffmi = if (hInMeters > 0) (leanWeight / (hInMeters * hInMeters)) + 6.1f * (1.8f - hInMeters) else 0f

                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF0D0D0D)), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = formatDate(stat.date),
                            color = Color(0xFF1E88E5),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Dòng 1: Cân nặng & Chiều cao
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Cân nặng: ${stat.weight} kg", color = Color.White, fontSize = 14.sp)
                            Text(text = "Chiều cao: ${stat.height} cm", color = Color.White, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))

                        // Dòng 2: Hiển thị thêm BMI & FFMI vào lịch sử theo yêu cầu
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "BMI: %.1f".format(bmi), color = Color.Green, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(text = "FFMI: %.1f".format(ffmi), color = Color.Cyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}