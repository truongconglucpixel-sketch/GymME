package com.example.gymmentor.timer

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.gymmentor.data.AppDatabase
import com.example.gymmentor.data.ExerciseWithTargetSets
import kotlinx.coroutines.launch

class RoutineDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Nhận ID và Tên gói tập được truyền sang từ màn hình danh sách
        val routineId = intent.getIntExtra("ROUTINE_ID", 0)
        val routineName = intent.getStringExtra("ROUTINE_NAME") ?: "CHI TIẾT GÓI TẬP"
        val database = AppDatabase.getDatabase(this)

        setContent {
            // 🚨 LẮNG NGHE ĐỘNG: Lấy toàn bộ bài tập thuộc gói kèm số sets thời gian thực từ câu lệnh INNER JOIN
            val exercisesWithSets by database.workoutDao().getExercisesFromRoutine(routineId)
                .collectAsState(initial = emptyList())
            val coroutineScope = rememberCoroutineScope()
            val context = LocalContext.current

            val totalSets = exercisesWithSets.sumOf { it.targetSets }

            Scaffold(
                containerColor = Color.Black
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .padding(16.dp)
                        .padding(paddingValues)
                ) {
                    // Thớt trên: Tiêu đề gói tập và thống kê tổng quan
                    Text(
                        text = routineName.uppercase(),
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        Text(text = "Tổng số bài: ", color = Color.Gray, fontSize = 14.sp)
                        Text(
                            text = "${exercisesWithSets.size}",
                            color = Color.Yellow,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = "  |  Tổng số Sets: ", color = Color.Gray, fontSize = 14.sp)
                        Text(
                            text = "$totalSets Sets",
                            color = Color.Red,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Thớt giữa: Danh sách bài tập nằm trong giỏ
                    if (exercisesWithSets.isEmpty()) {
                        Box(
                            modifier = Modifier.weight(1f).fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Gói tập đang trống rỗng.\nQuay lại Thư viện bấm nút [+] để nhặt bài vào đây nhé!",
                                color = Color.DarkGray,
                                fontSize = 14.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(exercisesWithSets) { item ->
                                ExerciseInRoutineCard(
                                    item = item,
                                    onIncreaseSets = {
                                        coroutineScope.launch {
                                            database.workoutDao().updateTargetSets(
                                                routineId,
                                                item.exerciseId,
                                                item.targetSets + 1
                                            )
                                        }
                                    },
                                    onDecreaseSets = {
                                        if (item.targetSets > 1) { // Đảm bảo không hạ xuống 0 sets
                                            coroutineScope.launch {
                                                database.workoutDao().updateTargetSets(
                                                    routineId,
                                                    item.exerciseId,
                                                    item.targetSets - 1
                                                )
                                            }
                                        }
                                    },
                                    onDeleteClick = {
                                        coroutineScope.launch {
                                            database.workoutDao().removeExerciseFromRoutine(
                                                routineId,
                                                item.exerciseId
                                            )
                                        }
                                        Toast.makeText(
                                            context,
                                            "Đã xóa khỏi gói",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Thớt dưới: Chiếc nút "BẮT ĐẦU BUỔI TẬP" quyền lực
                    Button(
                        onClick = {
                            if (exercisesWithSets.isNotEmpty()) {
                                val intent =
                                    Intent(context, LiveWorkoutActivity::class.java).apply {
                                        putExtra("ROUTINE_ID", routineId)
                                        putExtra("ROUTINE_NAME", routineName)
                                    }
                                context.startActivity(intent)
                                Toast.makeText(
                                    context,
                                    "💥 Bắt đầu buổi tập: Đốt cháy cơ bắp nào!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    "Vui lòng thêm bài tập trước khi bắt đầu",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(55.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (exercisesWithSets.isNotEmpty()) Color.Red else Color.DarkGray
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "BẮT ĐẦU BUỔI TẬP 🏋️‍♂️",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun ExerciseInRoutineCard(
        item: ExerciseWithTargetSets,
        onIncreaseSets: () -> Unit,
        onDecreaseSets: () -> Unit,
        onDeleteClick: () -> Unit
    ) {
        val context = LocalContext.current

        // Logic load ảnh giống hệt bên Thư viện bài tập của bác
        val isUriImage = remember(item.mainImage) {
            item.mainImage.startsWith("content://") || item.mainImage.startsWith("file://")
        }
        val imageResId = remember(item.mainImage) {
            if (!isUriImage && item.mainImage.isNotBlank()) context.resources.getIdentifier(
                item.mainImage,
                "drawable",
                context.packageName
            ) else 0
        }
        val imagePainter =
            if (isUriImage) rememberAsyncImagePainter(model = item.mainImage) else painterResource(
                id = if (imageResId != 0) imageResId else android.R.drawable.ic_menu_gallery
            )

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = imagePainter, contentDescription = null,
                    modifier = Modifier.size(65.dp)
                        .background(Color.DarkGray, shape = RoundedCornerShape(8.dp))
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = item.name,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )

                        // Nút xóa bài khỏi gói nhỏ gọn ở góc
                        Text(
                            text = "✕",
                            color = Color.DarkGray,
                            fontSize = 16.sp,
                            modifier = Modifier.clickable { onDeleteClick() }
                                .padding(horizontal = 4.dp)
                        )
                    }

                    Text(
                        text = item.type,
                        color = if (item.type == "COMPOUND") Color.Yellow else Color.Cyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 🚨 BỘ ĐIỀU CHỈNH SỐ SETS THÔNG MINH (CÁCH B)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = "Số hiệp mục tiêu:", color = Color.Gray, fontSize = 13.sp)

                        // Nút trừ [-]
                        Box(
                            modifier = Modifier.size(26.dp)
                                .background(Color(0xFF2C2C2E), CircleShape)
                                .clickable { onDecreaseSets() }, contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "-",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Số Sets hiện tại hiển thị vàng Gold lấp lánh
                        Text(
                            text = "${item.targetSets}",
                            color = Color(0xFFFFD700),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )

                        // Nút cộng [+]
                        Box(
                            modifier = Modifier.size(26.dp)
                                .background(Color(0xFF2C2C2E), CircleShape)
                                .clickable { onIncreaseSets() }, contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "+",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}