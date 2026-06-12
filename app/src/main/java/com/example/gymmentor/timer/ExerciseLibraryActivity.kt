package com.example.gymmentor.timer

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Dialog

class ExerciseLibraryActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. KẾT NỐI DATABASE: Gọi két sắt Room lấy dữ liệu thật dưới máy lên
        val databaseExercises = AppDatabase.getDatabase(this).exerciseDao().getAllExercise()

        setContent {
            var selectedCategory by remember { mutableStateOf("Tất cả") }

            // 2. LOGIC LỌC & THUẬT TOÁN SẮP XẾP THÔNG MINH
            val filterExercises = if (selectedCategory == "Tất cả") {
                databaseExercises
            } else {
                databaseExercises.filter { it.category == selectedCategory }
            }
                // 🔥 THUẬT TOÁN ĐỒ ÁN: Tự động đưa bài COMPOUND lên đầu, ISOLATION ra sau
                .sortedBy { if (it.type == "COMPOUND") 0 else 1 }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(16.dp)
            ) {
                Text("THƯ VIỆN BÀI TẬP", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                CategorySelector(
                    categories = listOf("Tất cả", "Ngực", "Lưng", "Chân"),
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it }
                )
                Spacer(modifier = Modifier.height(16.dp))

                // 3. HIỂN THỊ DANH SÁCH TỪ DATABASE
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(filterExercises) { exercise ->
                        ExerciseCard(exercise = exercise, onClick = {
                            val intent = Intent(
                                this@ExerciseLibraryActivity,
                                ExerciseDetailActivity::class.java
                            ).apply {
                                putExtra("EXERCISE_ID", exercise.id)
                                putExtra("EXERCISE_NAME", exercise.name)
                            }
                            startActivity(intent)
                        })
                    }
                }
            }
        }
    }
}

@Composable
fun StarRatingBar(rating: Int, maxStars: Int = 5) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)){
        for (i in 1..maxStars)
            Text(
                text = if (i <= rating)"★" else "☆",
                color = Color(0xFFFFD700),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
    }
}

@Composable
fun CategorySelector(categories: List<String>, selectedCategory: String, onCategorySelected: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        categories.forEach { category ->
            val isSelected = category == selectedCategory

            Box(
                modifier = Modifier
                    .background(
                        if (isSelected) Color.Red else Color.DarkGray,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clickable { onCategorySelected(category) }
            ) {
                Text(category, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ExerciseCard(exercise: Exercise, onClick: () -> Unit) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    val imageResId = remember(exercise.mainImage) {
        context.resources.getIdentifier(exercise.mainImage, "drawable", context.packageName)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically) {

            Image(
                painter = painterResource(id = if (imageResId != 0) imageResId else android.R.drawable.ic_menu_gallery),
                contentDescription = null,
                modifier = Modifier
                    .size(75.dp)
                    .background(Color.DarkGray, shape = RoundedCornerShape(8.dp))
                    .clickable {showDialog = true }
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        exercise.name,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = exercise.type,
                        color = if (exercise.type == "COMPOUND") Color.Yellow else Color.Cyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))

                StarRatingBar(rating = exercise.starRate)

                Spacer(modifier = Modifier.height(6.dp))
                Text(exercise.guide, color = Color.Gray, fontSize = 13.sp)
            }
        }
    }

    if (showDialog) {
        Dialog(onDismissRequest = {showDialog = false}){
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(16.dp)
            ){
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Text(
                        text = "ẢNH HƯỚNG DẪN: ${exercise.name.uppercase()}",
                        color = Color.Yellow,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Image(
                        painter = painterResource(id = if (imageResId != 0) imageResId else android.R.drawable.ic_menu_gallery),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(250.dp).background(Color.DarkGray)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showDialog = false},
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Đóng", color = Color.White)
                    }

                }
            }
        }
    }
}