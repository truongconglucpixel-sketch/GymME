package com.example.gymmentor.profile

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymmentor.data.profiledata.BodyStats
import com.example.gymmentor.data.profiledata.ProfileDatabase
import com.example.gymmentor.data.profiledata.User
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(db: ProfileDatabase) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val userState by db.userDao().getUserFlow().collectAsState(initial = null)

    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var bodyFat by remember { mutableStateOf("") }

    LaunchedEffect(userState) {
        userState?.let {
            name = it.name
            age = it.age.toString()
            height = it.height.toString()
            weight = it.weight.toString()
            bodyFat = it.bodyFat.toString()
        }
    }

    val bmi = remember(weight, height) {
        val h = height.toFloatOrNull()?.div(100f) ?: 0f
        val w = weight.toFloatOrNull() ?: 0f
        if (h > 0f) w / (h * h) else 0f
    }

    val ffmi = remember(weight, height, bodyFat) {
        val hInMeters = height.toFloatOrNull()?.div(100f) ?: 0f
        val w = weight.toFloatOrNull() ?: 0f
        val fatPercent = bodyFat.toFloatOrNull() ?: 0f

        if (hInMeters > 0f && fatPercent > 0f && fatPercent < 100f) {
            val leanWeight = w * (1f - (fatPercent / 100f))
            val rawFfmi = leanWeight / (hInMeters * hInMeters)
            rawFfmi + 6.1f * (1.8f - hInMeters)
        } else {
            0f
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = name, onValueChange = { name = it }, label = { Text("Họ và tên") },
            modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFF1E88E5), focusedLabelColor = Color(0xFF1E88E5), unfocusedLabelColor = Color.Gray)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = age, onValueChange = { age = it }, label = { Text("Tuổi") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFF1E88E5), focusedLabelColor = Color(0xFF1E88E5), unfocusedLabelColor = Color.Gray)
            )
            OutlinedTextField(
                value = bodyFat, onValueChange = { bodyFat = it }, label = { Text("% Mỡ (Body Fat)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFF1E88E5), focusedLabelColor = Color(0xFF1E88E5), unfocusedLabelColor = Color.Gray)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = height, onValueChange = { height = it }, label = { Text("Chiều cao (cm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFF1E88E5), focusedLabelColor = Color(0xFF1E88E5), unfocusedLabelColor = Color.Gray)
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = weight, onValueChange = { weight = it }, label = { Text("Cân nặng (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFF1E88E5), focusedLabelColor = Color(0xFF1E88E5), unfocusedLabelColor = Color.Gray)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)), modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "CHỈ SỐ BMI", color = Color.Gray, fontSize = 11.sp)
                    Text(text = if (bmi > 0) "%.1f".format(bmi) else "--", color = Color.Green, fontSize = 26.sp, fontWeight = FontWeight.Black)
                }
            }
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)), modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "CHỈ SỐ KHỐI CƠ NẠC (FFMI)", color = Color.Gray, fontSize = 11.sp)
                    Text(text = if (ffmi > 0) "%.1f".format(ffmi) else "Nhập % mỡ", color = Color.Cyan, fontSize = 26.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                scope.launch {
                    val wF = weight.toFloatOrNull() ?: 0f
                    val hF = height.toFloatOrNull() ?: 0f
                    val bF = bodyFat.toFloatOrNull() ?: 0f

                    db.userDao().insertOrUpdate(
                        User(id = 1, name = name, age = age.toIntOrNull() ?: 0, height = hF, weight = wF, bodyFat = bF)
                    )
                    db.bodyStatsDao().insert(
                        BodyStats(
                            weight = wF,
                            height = hF,
                            bodyFat = bF,
                            date = System.currentTimeMillis())
                    )

                    // ✅ Đã đổi text thông báo theo yêu cầu của pro
                    Toast.makeText(context, "Thông tin đã được lưu nha pro", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
        ) {
            Text("LƯU THÔNG TIN", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}