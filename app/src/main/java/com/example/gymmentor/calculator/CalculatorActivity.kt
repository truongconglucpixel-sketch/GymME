package com.example.gymmentor.calculator

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

class CalculatorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CalculatorScreen()
        }
    }
}

@Composable
fun CalculatorScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("GymCalculatorPrefs", Context.MODE_PRIVATE) }

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("1RM", "PLATES", "BMI / FFMI")

    var weight1RM by remember { mutableStateOf(prefs.getString("weight1RM", "") ?: "") }
    var reps1RM by remember { mutableStateOf(prefs.getString("reps1RM", "") ?: "") }
    var targetWeightPlate by remember { mutableStateOf(prefs.getString("targetWeightPlate", "") ?: "") }
    var barWeightPlate by remember { mutableStateOf(prefs.getString("barWeightPlate", "20") ?: "20") }
    var weightBio by remember { mutableStateOf(prefs.getString("weightBio", "") ?: "") }
    var heightBio by remember { mutableStateOf(prefs.getString("heightBio", "") ?: "") }
    var bodyFatBio by remember { mutableStateOf(prefs.getString("bodyFatBio", "") ?: "") }
    var isMale by remember { mutableStateOf(prefs.getBoolean("isMale", true)) }

    val saveValue = { key: String, value: String -> prefs.edit().putString(key, value).apply() }
    val saveBool = { key: String, value: Boolean -> prefs.edit().putBoolean(key, value).apply() }

    Column(
        modifier = Modifier.fillMaxSize().background(Color.Black).padding(16.dp)
    ) {
        Text(
            text = "SMART CALCULATORS",
            color = Color.Red,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp, top = 24.dp)
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Black,
            contentColor = Color.Red,
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = FontWeight.Bold, color = if(selectedTab == index) Color.Red else Color.Gray) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        when (selectedTab) {
            0 -> OneRepMaxCalculator(
                weight1RM, reps1RM,
                onWeightChange = { weight1RM = it; saveValue("weight1RM", it) },
                onRepsChange = { reps1RM = it; saveValue("reps1RM", it) }
            )
            1 -> PlateCalculator(
                targetWeightPlate, barWeightPlate,
                onTargetChange = { targetWeightPlate = it; saveValue("targetWeightPlate", it) },
                onBarChange = { barWeightPlate = it; saveValue("barWeightPlate", it) }
            )
            2 -> BmiFfmiCalculator(
                weightBio, heightBio, bodyFatBio, isMale,
                onWeightChange = { weightBio = it; saveValue("weightBio", it) },
                onHeightChange = { heightBio = it; saveValue("heightBio", it) },
                onBfChange = { bodyFatBio = it; saveValue("bodyFatBio", it) },
                onGenderChange = { isMale = it; saveBool("isMale", it) }
            )
        }
    }
}

@Composable
fun OneRepMaxCalculator(weight: String, reps: String, onWeightChange: (String) -> Unit, onRepsChange: (String) -> Unit) {
    val result = remember(weight, reps) {
        val w = weight.toDoubleOrNull() ?: 0.0
        val r = reps.toDoubleOrNull() ?: 0.0
        if (r > 0) w * (1 + r / 30.0) else null
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(value = weight, onValueChange = onWeightChange, label = { Text("Trọng lượng (kg)", color = Color.Gray) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Red, unfocusedBorderColor = Color.DarkGray, focusedTextColor = Color.White, unfocusedTextColor = Color.White))
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = reps, onValueChange = onRepsChange, label = { Text("Số lần lặp (reps)", color = Color.Gray) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Red, unfocusedBorderColor = Color.DarkGray, focusedTextColor = Color.White, unfocusedTextColor = Color.White))
        
        result?.let {
            Spacer(modifier = Modifier.height(32.dp))
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))) {
                Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Ước tính 1RM của bạn:", color = Color.White, fontSize = 16.sp)
                    Text("${it.roundToInt()} kg", color = Color.Red, fontSize = 56.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
fun PlateCalculator(target: String, bar: String, onTargetChange: (String) -> Unit, onBarChange: (String) -> Unit) {
    val platesPerSide = remember(target, bar) {
        val t = target.toDoubleOrNull() ?: 0.0
        val b = bar.toDoubleOrNull() ?: 20.0
        var remainingPerSide = (t - b) / 2.0
        
        if (remainingPerSide > 0) {
            val availablePlates = listOf(25.0, 20.0, 15.0, 10.0, 5.0, 2.5, 1.25)
            val resultPlates = mutableMapOf<Double, Int>()
            for (plate in availablePlates) {
                val count = (remainingPerSide / plate).toInt()
                if (count > 0) {
                    resultPlates[plate] = count
                    remainingPerSide -= count * plate
                    remainingPerSide = Math.round(remainingPerSide * 100.0) / 100.0
                }
            }
            resultPlates
        } else emptyMap()
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(value = target, onValueChange = onTargetChange, label = { Text("Tổng trọng lượng mục tiêu (kg)", color = Color.Gray) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Red, unfocusedBorderColor = Color.DarkGray, focusedTextColor = Color.White, unfocusedTextColor = Color.White))
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = bar, onValueChange = onBarChange, label = { Text("Trọng lượng thanh đòn (kg)", color = Color.Gray) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Red, unfocusedBorderColor = Color.DarkGray, focusedTextColor = Color.White, unfocusedTextColor = Color.White))

        if (platesPerSide.isNotEmpty()) {
            Spacer(modifier = Modifier.height(32.dp))
            Text("Số lượng tạ mỗi bên (tối ưu nhất):", color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            platesPerSide.forEach { (plate, count) ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2E))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(12.dp).background(Color.Red, RoundedCornerShape(2.dp)))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = "$plate kg", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Text(text = "x $count", color = Color.Red, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun BmiFfmiCalculator(weight: String, height: String, bf: String, isMale: Boolean, onWeightChange: (String) -> Unit, onHeightChange: (String) -> Unit, onBfChange: (String) -> Unit, onGenderChange: (Boolean) -> Unit) {
    val results = remember(weight, height, bf) {
        val w = weight.toDoubleOrNull() ?: 0.0
        val hCm = height.toDoubleOrNull() ?: 0.0
        val hM = hCm / 100.0
        val b = bf.toDoubleOrNull() ?: 0.0
        
        if (hM > 0 && w > 0) {
            val bmi = w / (hM * hM)
            val leanWeight = w * (1 - (b / 100.0))
            val ffmi = leanWeight / (hM * hM)
            val adjFfmi = ffmi + 6.1 * (1.8 - hM)
            Triple(bmi, ffmi, adjFfmi)
        } else null
    }

    Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
            RadioButton(selected = isMale, onClick = { onGenderChange(true) }, colors = RadioButtonDefaults.colors(selectedColor = Color.Red, unselectedColor = Color.Gray))
            Text("Nam", color = Color.White, modifier = Modifier.padding(end = 16.dp))
            RadioButton(selected = !isMale, onClick = { onGenderChange(false) }, colors = RadioButtonDefaults.colors(selectedColor = Color.Red, unselectedColor = Color.Gray))
            Text("Nữ", color = Color.White)
        }

        OutlinedTextField(value = weight, onValueChange = onWeightChange, label = { Text("Cân nặng (kg)", color = Color.Gray) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Red, unfocusedBorderColor = Color.DarkGray, focusedTextColor = Color.White, unfocusedTextColor = Color.White))
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = height, onValueChange = onHeightChange, label = { Text("Chiều cao (cm)", color = Color.Gray) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Red, unfocusedBorderColor = Color.DarkGray, focusedTextColor = Color.White, unfocusedTextColor = Color.White))
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(value = bf, onValueChange = onBfChange, label = { Text("Tỷ lệ mỡ (%)", color = Color.Gray) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Red, unfocusedBorderColor = Color.DarkGray, focusedTextColor = Color.White, unfocusedTextColor = Color.White))

        results?.let { (bmi, ffmi, adj) ->
            Spacer(modifier = Modifier.height(32.dp))
            
            // BMI Results
            Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("BMI: ${"%.1f".format(bmi)}", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Phân loại: ${getBMICategory(bmi)}", color = Color.Red, fontSize = 14.sp)
                }
            }

            // FFMI Results
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E))) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("FFMI: ${"%.2f".format(ffmi)}", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Hiệu chỉnh: ${"%.2f".format(adj)}", color = Color.Red, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Đánh giá: ${getFFMICategory(ffmi)}", color = Color.White, fontSize = 14.sp)
                }
            }

            // High Body Fat Warning
            val bfValue = bf.toDoubleOrNull() ?: 0.0
            val isHighBf = if (isMale) bfValue > 25 else bfValue > 32
            if (isHighBf) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF441111))) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("💡 LỜI KHUYÊN:", color = Color.Red, fontWeight = FontWeight.ExtraBold)
                        Text(text = "Tỷ lệ mỡ của bạn khá cao ($bfValue%). Bạn nên ưu tiên thâm hụt calo nhẹ và tập Cardio để giảm mỡ trước khi xả cơ.", color = Color.White, fontSize = 14.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
        Text("THANG ĐO THAM KHẢO", color = Color.Red, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        // BMI Scale Card
        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF111111))) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Chỉ số BMI (Cân nặng / Chiều cao)", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                ScaleRow("Dưới 18.5", "Gầy")
                ScaleRow("18.5 - 24.9", "Bình thường")
                ScaleRow("25.0 - 29.9", "Thừa cân")
                ScaleRow("Trên 30.0", "Béo phì")
            }
        }

        // FFMI Scale Card
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF111111))) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Chỉ số FFMI (Cơ bắp tự nhiên)", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                ScaleRow("Dưới 18", "Dưới trung bình")
                ScaleRow("18 - 20", "Trung bình")
                ScaleRow("20 - 22", "Khá (Tập luyện tốt)")
                ScaleRow("22 - 25", "Xuất sắc")
                ScaleRow("Trên 25", "Vượt giới hạn tự nhiên")
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun ScaleRow(range: String, category: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(range, color = Color.Gray, fontSize = 13.sp)
        Text(category, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

fun getBMICategory(bmi: Double): String {
    return when {
        bmi < 18.5 -> "Gầy (Underweight)"
        bmi < 25.0 -> "Bình thường (Healthy)"
        bmi < 30.0 -> "Thừa cân (Overweight)"
        else -> "Béo phì (Obese)"
    }
}

fun getFFMICategory(ffmi: Double): String {
    return when {
        ffmi < 18 -> "Dưới trung bình"
        ffmi < 20 -> "Trung bình"
        ffmi < 22 -> "Khá (Tập luyện tốt)"
        ffmi < 25 -> "Xuất sắc (Giới hạn tự nhiên)"
        else -> "Vượt ngưỡng tự nhiên"
    }
}
