package com.example.gymmentor

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymmentor.calculator.CalculatorActivity
import com.example.gymmentor.profile.WorkoutHistoryActivity
import com.example.gymmentor.timer.ExerciseLibraryActivity
import kotlin.math.cos
import kotlin.math.sin

// Model cho Daily Missions
data class Mission(val id: Int, val title: String, val isChecked: Boolean)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Áp dụng theme tối
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = Color(0xFF121212),
                    surface = Color(0xFF1E1E1E),
                    primary = Color(0xFF00FF88),
                    onBackground = Color.White,
                    onSurface = Color.White
                )
            ) {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val context = LocalContext.current
    
    // Trạng thái cho Daily Missions
    val missions = remember {
        mutableStateListOf(
            Mission(1, "Hoàn thành 1 bài tập ngực", false),
            Mission(2, "Sử dụng Smart Calculator", false),
            Mission(3, "Tập trung 30 phút", false)
        )
    }

    // Trạng thái streak
    var streakDays by remember { mutableIntStateOf(14) }

    // XP progress
    val xpProgress = 0.75f

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()) // Đảm bảo toàn bộ màn hình có thể cuộn
        ) {
            // ========== KHU VỰC 1: HEADER ==========
            HeaderSection(streakDays, xpProgress)

            Spacer(modifier = Modifier.height(16.dp))

            // ========== KHU VỰC 2: HERO SECTION ==========
            HeroSection(
                missions = missions,
                onMissionChecked = { mission ->
                    val idx = missions.indexOfFirst { it.id == mission.id }
                    if (idx != -1) {
                        missions[idx] = mission.copy(isChecked = !mission.isChecked)
                    }
                },
                onRadarClick = {
                    // Chuyển sang Dashboard (Skill Tree)
                    try {
                        context.startActivity(Intent(context, com.example.gymmentor.dashboard.DashboardActivity::class.java))
                    } catch (e: Exception) {
                        // Tránh crash nếu Activity chưa được khai báo
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ========== KHU VỰC 3: CORE NAVIGATION ==========
            CoreNavigation()

            // Padding dưới cùng để không bị FAB che khuất nội dung cuối
            Spacer(modifier = Modifier.height(80.dp))
        }

        // ========== KHU VỰC 4: FLOATING ACTION BUTTON ==========
        FloatingActionButton(
            onClick = {
                // TODO: Gọi timer/service
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Color(0xFF00FF88),
            contentColor = Color.Black,
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Quick Start Workout", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ========== HEADER ==========
@Composable
fun HeaderSection(streakDays: Int, xpProgress: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // App Brand với hiệu ứng glow
        Text(
            text = "GYM MENTOR",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            style = LocalTextStyle.current.copy(
                shadow = Shadow(
                    color = Color(0xFF00FF88).copy(alpha = 0.3f),
                    offset = Offset(0f, 0f),
                    blurRadius = 12f
                )
            )
        )

        // Streak + Mini Profile
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val fireColor = if (streakDays > 0) Color(0xFFFF8800) else Color.Gray
                Icon(
                    imageVector = Icons.Outlined.Whatshot,
                    contentDescription = "Streak",
                    tint = fireColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$streakDays Days",
                    color = if (streakDays > 0) Color(0xFFFF8800) else Color.Gray,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Mini Profile Ring
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clickable { /* Mở Profile */ }
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawCircle(
                        color = Color(0xFF00FF88),
                        radius = size.minDimension / 2,
                        style = Stroke(width = 4f)
                    )
                    drawArc(
                        color = Color.DarkGray,
                        startAngle = -90f,
                        sweepAngle = 360f * (1 - xpProgress),
                        useCenter = false,
                        style = Stroke(width = 4f)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.Center)
                        .background(Color.Gray, CircleShape)
                ) {
                    // Placeholder cho Avatar
                    Text(
                        "GM", 
                        modifier = Modifier.align(Alignment.Center), 
                        color = Color.White, 
                        fontSize = 12.sp, 
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
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
