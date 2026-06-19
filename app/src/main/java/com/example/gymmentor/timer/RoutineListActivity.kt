package com.example.gymmentor.timer

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.gymmentor.data.ExerciseData.AppDatabase
import com.example.gymmentor.data.ExerciseData.WorkoutRoutine
import kotlinx.coroutines.launch

class RoutineListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(this)

        setContent {
            // Lấy danh sách gói tập từ DB, tự động vẽ lại khi có thay đổi
            val routinesByFlow by database.workoutDao().getAllRoutines().collectAsState(initial = emptyList())
            var showCreateDialog by remember { mutableStateOf(false) }
            val coroutineScope = rememberCoroutineScope()
            val context = LocalContext.current

            Scaffold(
                // Nút tạo gói tập mới màu Vàng đặc trưng
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { showCreateDialog = true },
                        containerColor = Color(0xFFFFD700), // Màu Gold
                        contentColor = Color.Black
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("+", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("GÓI MỚI", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    Text("GIÁO ÁN TẬP LUYỆN", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("Quản lý các gói tập của bạn", color = Color.Gray, fontSize = 14.sp)

                    Spacer(modifier = Modifier.height(20.dp))

                    if (routinesByFlow.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Chưa có giáo án nào.\nHãy tạo gói 'Push Day' hoặc 'Leg Day' nhé!",
                                color = Color.DarkGray, fontSize = 15.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(routinesByFlow) { routine ->
                                RoutineRowCard(
                                    routine = routine,
                                    onClick = {
                                        val intent = Intent(context, RoutineDetailActivity::class.java).apply {
                                            putExtra("ROUTINE_ID", routine.id)
                                            putExtra("ROUTINE_NAME", routine.name)
                                        }
                                        context.startActivity(intent)
                                    },
                                    onDeleteClick = {
                                        coroutineScope.launch {
                                            database.workoutDao().deleteRoutineById(routine.id)
                                        }
                                        Toast.makeText(context, "Đã xóa gói", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // DIALOG TẠO TÊN GÓI TẬP MỚI
            if (showCreateDialog) {
                var routineName by remember { mutableStateOf("") }

                Dialog(onDismissRequest = { showCreateDialog = false }) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("TÊN BUỔI TẬP", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                            OutlinedTextField(
                                value = routineName,
                                onValueChange = { routineName = it },
                                placeholder = { Text("Ví dụ: Ngực - Tay sau", color = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFFFFD700)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = { showCreateDialog = false }) {
                                    Text("HỦY", color = Color.Gray)
                                }
                                Button(
                                    onClick = {
                                        if (routineName.isNotBlank()) {
                                            coroutineScope.launch {
                                                database.workoutDao().insertRoutine(
                                                    WorkoutRoutine(
                                                        name = routineName
                                                    )
                                                )
                                            }
                                            showCreateDialog = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                                ) {
                                    Text("XÁC NHẬN", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RoutineRowCard(routine: WorkoutRoutine, onClick: () -> Unit, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = routine.name.uppercase(), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                Text(text = "Bấm để chỉnh sửa Sets bài tập", color = Color(0xFFFFD700), fontSize = 12.sp)
            }

            // Nút xóa (Icon Thùng rác)
            IconButton(onClick = onDeleteClick) {
                Text("🗑️", fontSize = 20.sp)
            }
        }
    }
}