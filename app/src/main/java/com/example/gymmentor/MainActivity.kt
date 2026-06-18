package com.example.gymmentor

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymmentor.calculator.CalculatorActivity
<<<<<<< Updated upstream
import com.example.gymmentor.timer.ExerciseLibraryActivity // Import màn hình của bạn
=======
import com.example.gymmentor.profile.WorkoutHistoryActivity
import com.example.gymmentor.timer.ExerciseLibraryActivity
import kotlin.math.cos
import kotlin.math.sin

// Model cho Daily Missions
data class Mission(val id: Int, val title: String, val isChecked: Boolean)
>>>>>>> Stashed changes

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "GYM MENTOR HUB",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = { /* TODO: Mở Dashboard */ },
                    modifier = Modifier.fillMaxWidth().height(55.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text("1. DASHBOARD & SKILL TREE (Người 1)")
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val intent = Intent(this@MainActivity, ExerciseLibraryActivity::class.java)
                        startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth().height(55.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("2. THƯ VIỆN BÀI TẬP & TIMER", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val intent = Intent(this@MainActivity, CalculatorActivity::class.java)
                        startActivity(intent) },
                    modifier = Modifier.fillMaxWidth().height(55.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text("3. SMART CALCULATORS (1RM, Plates, FFMI)", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { /* TODO: Mở Profile */ },
                    modifier = Modifier.fillMaxWidth().height(55.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text("4. USER PROFILE & HISTORY (Người 4)")
                }
            }
        }
    }
<<<<<<< Updated upstream
}
=======
}

// ========== HERO SECTION ==========
@Composable
fun HeroSection(
    missions: List<Mission>,
    onMissionChecked: (Mission) -> Unit,
    onRadarClick: () -> Unit
) {
    Column {
        // Radar Chart Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                .clickable { onRadarClick() }
                .padding(16.dp)
        ) {
            RadarChart(
                data = mapOf(
                    "Ngực" to 0.8f,
                    "Lưng" to 0.6f,
                    "Chân" to 0.9f,
                    "Tay" to 0.5f,
                    "Core" to 0.7f
                ),
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Daily Missions
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "📋 Nhiệm vụ hôm nay",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            missions.take(3).forEach { mission ->
                MissionCard(
                    mission = mission,
                    onChecked = { onMissionChecked(mission) }
                )
            }
        }
    }
}

@Composable
fun MissionCard(mission: Mission, onChecked: () -> Unit) {
    val animatedColor by animateColorAsState(
        targetValue = if (mission.isChecked) Color(0xFF00FF88) else Color.Transparent,
        label = "missionColor"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
            .clickable { onChecked() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(animatedColor, CircleShape)
                .border(
                    width = 2.dp,
                    color = if (mission.isChecked) Color(0xFF00FF88) else Color.Gray,
                    shape = CircleShape
                )
        ) {
            if (mission.isChecked) {
                Icon(
                    imageVector = Icons.Default.PlayArrow, // Checkmark giả định
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp).align(Alignment.Center)
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = mission.title,
            color = if (mission.isChecked) Color(0xFF00FF88) else Color.White,
            fontSize = 14.sp,
            textDecoration = if (mission.isChecked) TextDecoration.LineThrough else null
        )
    }
}

// ========== RADAR CHART (CUSTOM DRAW) ==========
@Composable
fun RadarChart(data: Map<String, Float>, modifier: Modifier = Modifier) {
    val vertices = data.keys.toList()
    val values = data.values.toList()
    val count = vertices.size

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = minOf(size.width, size.height) / 2 * 0.8f

        // Vẽ trục và nhãn
        for (i in 0 until count) {
            val angle = 2 * Math.PI * i / count - Math.PI / 2
            val endX = center.x + radius * cos(angle).toFloat()
            val endY = center.y + radius * sin(angle).toFloat()
            drawLine(
                color = Color(0xFF444444),
                start = center,
                end = Offset(endX, endY),
                strokeWidth = 2f
            )
            
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 24f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
                val labelX = center.x + (radius + 25) * cos(angle).toFloat()
                val labelY = center.y + (radius + 25) * sin(angle).toFloat()
                drawText(vertices[i], labelX, labelY + 10, paint)
            }
        }

        // Vẽ các mức lưới
        val gridLevels = 4
        for (level in 1..gridLevels) {
            val r = radius * level / gridLevels
            val path = Path()
            for (i in 0 until count) {
                val angle = 2 * Math.PI * i / count - Math.PI / 2
                val x = center.x + r * cos(angle).toFloat()
                val y = center.y + r * sin(angle).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path, Color(0xFF333333), style = Stroke(width = 1f))
        }

        // Vẽ vùng dữ liệu
        val dataPath = Path()
        for (i in 0 until count) {
            val angle = 2 * Math.PI * i / count - Math.PI / 2
            val r = radius * values[i].coerceIn(0f, 1f)
            val x = center.x + r * cos(angle).toFloat()
            val y = center.y + r * sin(angle).toFloat()
            if (i == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
        }
        dataPath.close()
        drawPath(dataPath, Color(0xFF00FF88).copy(alpha = 0.3f), style = Fill)
        drawPath(dataPath, Color(0xFF00FF88), style = Stroke(width = 4f))
    }
}

// ========== CORE NAVIGATION (DÙNG COLUMN + ROW THAY CHO GRID ĐỂ FIX SCROLL) ==========
@Composable
fun CoreNavigation() {
    val context = LocalContext.current
    val navItems = listOf(
        NavItem("Skill Tree", "2 điểm kỹ năng", Icons.Outlined.Whatshot) {
            try {
                context.startActivity(Intent(context, com.example.gymmentor.dashboard.DashboardActivity::class.java))
            } catch (e: Exception) {}
        },
        NavItem("Thư viện", "24 bài tập mới", Icons.Outlined.Whatshot) {
            context.startActivity(Intent(context, ExerciseLibraryActivity::class.java))
        },
        NavItem("Calculators", "1RM, Plate, FFMI", Icons.Outlined.Whatshot) {
            context.startActivity(Intent(context, CalculatorActivity::class.java))
        },
        NavItem("Lịch sử", "Xem tiến trình", Icons.Outlined.Whatshot) {
            context.startActivity(Intent(context, WorkoutHistoryActivity::class.java))
        }
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        navItems.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowItems.forEach { item ->
                    Box(modifier = Modifier.weight(1f)) {
                        NavigationCard(item)
                    }
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun NavigationCard(item: NavItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                tint = Color(0xFF00FF88),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Text(
                text = item.subtitle,
                color = Color.Gray,
                fontSize = 11.sp,
                maxLines = 1
            )
        }
    }
}

data class NavItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)
>>>>>>> Stashed changes
