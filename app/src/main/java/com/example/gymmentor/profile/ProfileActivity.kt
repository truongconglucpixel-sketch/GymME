package com.example.gymmentor.profile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gymmentor.data.profiledata.ProfileDatabase
import com.example.gymmentor.data.profiledata.ProfileDatabaseProvider
import kotlinx.coroutines.delay

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = ProfileDatabaseProvider.getDatabase(this)

        setContent {
            // Trạng thái kiểm soát việc hiển thị Màn hình chờ (Intro) hay Màn hình chính
            var showIntro by remember { mutableStateOf(true) }

            // Chạy đếm ngược 2 giây để hiển thị Slogan kỉ luật
            LaunchedEffect(Unit) {
                delay(2000)
                showIntro = false
            }

            Crossfade(targetState = showIntro, label = "ScreenTransition") { isIntro ->
                if (isIntro) {
                    // 🌟 GIAO DIỆN MÀN HÌNH CHỜ (INTRO SLOGAN)
                    Column(
                        modifier = Modifier.fillMaxSize().background(Color.Black).padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Khu vực hiển thị Logo app (Nếu chưa có file ảnh, hệ thống tạm ẩn hoặc bạn để icon mặc định)
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_compass), // Thay bằng R.drawable.ic_app_logo của bạn
                            contentDescription = "Logo",
                            tint = Color.Red,
                            modifier = Modifier.size(100.dp)
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                            text = "GYM MENTOR",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Slogan mang tính kỉ luật cao
                        Text(
                            text = "\"Động lực là thứ giúp bạn bắt đầu.\nKỷ luật mới là thứ giữ cho bạn tiếp tục tiến lên.\nCơn đau của sự kỷ luật nhẹ hơn nhiều cơn đau của sự hối hận!\"",
                            color = Color.LightGray,
                            fontSize = 16.sp,
                            fontStyle = FontStyle.Italic,
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                } else {
                    // 🏢 GIAO DIỆN CHÍNH (HỒ SƠ & LỊCH SỬ CHỈ SỐ)
                    MainProfileContent(db)
                }
            }
        }
    }
}

@Composable
fun MainProfileContent(db: ProfileDatabase) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("HỒ SƠ", "LỊCH SỬ CHỈ SỐ", "LỊCH SỬ TẬP LUYỆN")

    Column(modifier = Modifier.fillMaxSize().background(Color.Black).padding(16.dp)) {
        Text(
            text = "MY GYM PROFILE",
            color = Color(0xFF1E88E5),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp, top = 24.dp)
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Black,
            contentColor = Color(0xFF1E88E5),
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if(selectedTab == index) Color(0xFF1E88E5) else Color.Gray) }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        when (selectedTab) {
            0 -> ProfileScreen(db)
            1 -> BodyStatsHistoryScreen(db)
            2 -> WorkoutHistoryScreen()
        }
    }
}