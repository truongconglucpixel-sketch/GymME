package com.example.gymmentor.timer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class ExerciseDetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. NHẬN DỮ LIỆU TỪ THƯ VIỆN GỬI SANG
        val exerciseId = intent.getIntExtra("EXERCISE_ID", 1)
        val exerciseName = intent.getStringExtra("EXERCISE_NAME") ?: "CHI TIẾT BÀI TẬP"

        // 2. TRUY VẤN DATABASE: Lấy danh sách các bước hướng dẫn theo ID bài tập
        val guideList = AppDatabase.getDatabase(this).exerciseDao().getGuidesForExercise(exerciseId)

        setContent {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(16.dp)
            ) {
                // TIÊU ĐỀ BÀI TẬP TO RỰC RỠ
                Text(
                    text = exerciseName.uppercase(),
                    color = Color.Red,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = "HƯỚNG DẪN KỸ THUẬT FORM CHUẨN",
                    color = Color.Yellow,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 3. DANH SÁCH CUỘN CÁC BƯỚC TẬP LUYỆN
                if (guideList.isEmpty()) {
                    // Trường hợp bài tập chưa được bơm dữ liệu bước chi tiết
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Dữ liệu hướng dẫn đang được cập nhật...", color = Color.Gray, fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(guideList) { step ->
                            DetailStepCard(step = step)
                        }
                    }
                }
            }
        }
    }
}

// COMPONENT VẼ TỪNG THẺ BƯỚC TẬP LÀM ĐỒ ÁN NHÌN CỰC "NÉT"
@Composable
fun DetailStepCard(step: ExerciseGuide) {
    val context = LocalContext.current

    // Tự động tìm ID ảnh trong drawable bằng chuỗi String lưu ở DB
    val imageResId = remember(step.imageName) {
        context.resources.getIdentifier(step.imageName, "drawable", context.packageName)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
        shape = RoundedCornerShape(16.dp), // Bo góc sâu nhìn cho hiện đại, chuẩn UI 2026
        modifier = Modifier.fillMaxWidth()
    ) {
        // Đổi từ Row sang Column để xếp dọc như một bài viết/blog
        Column(modifier = Modifier.fillMaxWidth()) {

            // 📸 1. ẢNH MINH HỌA TO FULL CHIỀU NGANG (NẰM TRÊN)
            Image(
                painter = painterResource(id = if (imageResId != 0) imageResId else android.R.drawable.ic_menu_gallery),
                contentDescription = "Hình ảnh minh họa bài viết",
                contentScale = ContentScale.Crop, // Ép ảnh cắt cúp vừa vặn khung hình không bị méo tỉ lệ
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp) // Tăng chiều cao lên 200dp để ảnh nhìn to, rõ, lực hơn hẳn
                    .background(Color.DarkGray)
            )

            // KHỐI CHỨA NỘI DUNG CHỮ (NẰM DƯỚI)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp) // Khoảng cách đệm xung quanh chữ cho dễ đọc
            ) {
                // Tiêu đề phân đoạn bước tập
                Text(
                    text = "BƯỚC ${step.stepNumber}:",
                    color = Color.Red,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ✍️ 2. MÔ TẢ DÀI DẰNG DẶC (Tha hồ hiển thị bài viết dài)
                Text(
                    text = step.instruction,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 22.sp // Tăng khoảng cách giữa các dòng để đọc văn bản dài không bị mỏi mắt
                )
            }
        }
    }
}