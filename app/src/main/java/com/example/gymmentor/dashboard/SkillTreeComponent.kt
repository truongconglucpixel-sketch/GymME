package com.example.gymmentor.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SkillTreeComponent() {
    // Phân tích: Sử dụng Canvas để vẽ các node (Core, Upper Body, Lower Body)
    // và các đường line nối (Neon color) để thể hiện sự tiến cấp (progression).
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        // Ví dụ vẽ một đường nối cơ bản
        drawLine(
            color = Color(0xFF39FF14), // NeonGreen
            start = Offset(x = size.width / 2, y = 50f),
            end = Offset(x = size.width / 4, y = 200f),
            strokeWidth = 5f
        )
        // Yêu cầu tiếp theo: Tính toán tọa độ (x, y) động cho các Node dựa trên
        // dữ liệu level của user do Người 4 cung cấp từ Database Room.
    }
}