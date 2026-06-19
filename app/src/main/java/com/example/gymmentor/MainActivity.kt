package com.example.gymmentor

import android.content.Intent
import android.os.Bundle
import android.view.View
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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.gymmentor.calculator.CalculatorActivity
import com.example.gymmentor.profile.ProfileActivity
import com.example.gymmentor.profile.WorkoutHistoryActivity
import com.example.gymmentor.timer.ExerciseLibraryActivity
import kotlin.math.cos
import kotlin.math.sin

// Model cho Daily Missions
data class Mission(val id: Int, val title: String, val isChecked: Boolean)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.decorView.setBackgroundColor(android.graphics.Color.BLACK)

        val decorView = window.decorView
        val rootView = decorView.findViewById<View>(android.R.id.content)

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

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

    // Trạng thái streak & missions
    var streakDays by remember { mutableIntStateOf(14) }
    var isStreakClaimed by remember { mutableStateOf(false) }
    val xpProgress = 0.75f

    // THÊM MỚI: Trạng thái đăng nhập
    var isLoggedIn by remember { mutableStateOf(false) }
    var showLoginDialog by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("Guest") }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // ========== KHU VỰC 1: HEADER ==========
            // Truyền thêm trạng thái và sự kiện click
            HeaderSection(
                streakDays = streakDays,
                xpProgress = xpProgress,
                isLoggedIn = isLoggedIn,
                userName = userName,
                onProfileClick = {
                    if (!isLoggedIn) {
                        showLoginDialog = true // Mở dialog đăng nhập nếu chưa login
                    } else {
                        // Đã đăng nhập -> Mở trang Profile
                        try {
                            context.startActivity(Intent(context, ProfileActivity::class.java))
                        } catch (e: Exception) {}
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ========== KHU VỰC 2: HERO SECTION ==========
            HeroSection(
                missions = missions,
                onMissionChecked = { mission ->
                    val idx = missions.indexOfFirst { it.id == mission.id }
                    if (idx != -1) {
                        missions[idx] = mission.copy(isChecked = !mission.isChecked)
                        val isAllCompleted = missions.all { it.isChecked }
                        if (isAllCompleted && !isStreakClaimed) {
                            streakDays += 1
                            isStreakClaimed = true
                        } else if (!isAllCompleted && isStreakClaimed) {
                            streakDays -= 1
                            isStreakClaimed = false
                        }
                    }
                },
                onRadarClick = {
                    try {
                        context.startActivity(Intent(context, com.example.gymmentor.dashboard.DashboardActivity::class.java))
                    } catch (e: Exception) {}
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ========== KHU VỰC 3: CORE NAVIGATION ==========
            CoreNavigation()

            Spacer(modifier = Modifier.height(80.dp))
        }

        // ========== KHU VỰC 4: FLOATING ACTION BUTTON ==========
        FloatingActionButton(
            onClick = { /* TODO: Gọi timer/service */ },
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
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Quick Start Workout", fontWeight = FontWeight.Bold)
            }
        }

        // THÊM MỚI: Hiển thị Dialog Đăng nhập
        if (showLoginDialog) {
            LoginDialog(
                onDismiss = { showLoginDialog = false },
                onLoginSuccess = { inputName ->
                    userName = inputName
                    isLoggedIn = true
                    showLoginDialog = false
                }
            )
        }
    }
}

// ========== HEADER ==========
@Composable
fun HeaderSection(
    streakDays: Int,
    xpProgress: Float,
    isLoggedIn: Boolean,
    userName: String,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
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
                    .clickable { onProfileClick() } // Gọi hàm click ở đây
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawCircle(
                        color = if (isLoggedIn) Color(0xFF00FF88) else Color.Gray, // Đổi màu viền nếu chưa login
                        radius = size.minDimension / 2,
                        style = Stroke(width = 4f)
                    )
                    if (isLoggedIn) {
                        drawArc(
                            color = Color.DarkGray,
                            startAngle = -90f,
                            sweepAngle = 360f * (1 - xpProgress),
                            useCenter = false,
                            style = Stroke(width = 4f)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.Center)
                        .background(Color.DarkGray, CircleShape)
                ) {
                    // Lấy chữ cái đầu tiên của tên để làm Avatar
                    val initial = if (isLoggedIn && userName.isNotEmpty()) {
                        userName.first().uppercase()
                    } else {
                        "?" // Khách
                    }

                    Text(
                        text = initial,
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.White,
                        fontSize = 14.sp,
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
        NavItem("Profile", "Xem tiến trình", Icons.Outlined.Whatshot) {
            context.startActivity(Intent(context, ProfileActivity::class.java))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginDialog(
    onDismiss: () -> Unit,
    onLoginSuccess: (String) -> Unit
) {
    var inputName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text("Đăng nhập", color = Color.White, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = inputName,
                    onValueChange = { inputName = it },
                    label = { Text("Tên hiển thị (Username)", color = Color.Gray) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color(0xFF00FF88),     // Màu viền khi click vào
                        unfocusedIndicatorColor = Color.DarkGray,       // Màu viền bình thường
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Mật khẩu", color = Color.Gray) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color(0xFF00FF88),     // Màu viền khi click vào
                        unfocusedIndicatorColor = Color.DarkGray,       // Màu viền bình thường
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (inputName.isNotBlank()) {
                        onLoginSuccess(inputName)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF88))
            ) {
                Text("Vào tập", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = Color.Gray)
            }
        }
    )
}

data class NavItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)
