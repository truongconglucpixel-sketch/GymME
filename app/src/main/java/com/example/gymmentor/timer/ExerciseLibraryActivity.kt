package com.example.gymmentor.timer

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter

class ExerciseLibraryActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. KẾT NỐI DATABASE: Gọi két sắt Room lấy dữ liệu thật dưới máy lên
        val databaseExercises = AppDatabase.getDatabase(this).exerciseDao().getAllExercise()

        setContent {
            var selectedCategory by remember { mutableStateOf("Tất cả") }
            // 🚨 BIẾN CÔNG TẮC: Để quản lý việc ẩn/hiện cái hộp thoại thêm bài tập
            var showAddDialog by remember { mutableStateOf(false) }
            val context = LocalContext.current

            val filterExercises = if (selectedCategory == "Tất cả") {
                databaseExercises
            } else {
                databaseExercises.filter { it.category == selectedCategory }
            }
                .sortedBy { if (it.type == "COMPOUND") 0 else 1 }

            // Dùng Scaffold để đính kèm nút dấu cộng nổi (FAB) chuẩn UI Android
            androidx.compose.material3.Scaffold(
                floatingActionButton = {
                    androidx.compose.material3.FloatingActionButton(
                        onClick = { showAddDialog = true }, // Bấm vào là bật Dialog lên liền
                        containerColor = Color.Red,
                        contentColor = Color.White
                    ) {
                        Text("+", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .padding(paddingValues) // Tránh đè lên nút nổi
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

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(filterExercises) { exercise ->
                            ExerciseCard(exercise = exercise, onClick = {
                                val intent = Intent(this@ExerciseLibraryActivity, ExerciseDetailActivity::class.java).apply {
                                    putExtra("EXERCISE_ID", exercise.id)
                                    putExtra("EXERCISE_NAME", exercise.name)
                                }
                                startActivity(intent)
                            })
                        }
                    }
                }
            }

            // 🚨 THỰC THI LỆNH LƯU: Nếu công tắc mở, ta lôi Dialog ra xài
            if (showAddDialog) {
                AddExerciseDialog(
                    onDimiss = { showAddDialog = false },
                    onSave = { newExercise ->
                        // 🚨 MẸO: Nếu user chọn ảnh thật (có đường dẫn content://), ta xin quyền đọc file dài hạn để app ko bị mất ảnh sau khi khởi động lại
                        if (newExercise.mainImage.startsWith("content://")) {
                            try {
                                context.contentResolver.takePersistableUriPermission(
                                    Uri.parse(newExercise.mainImage),
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                        // Sau đó lưu DB như bình thường
                        AppDatabase.getDatabase(context).exerciseDao().insertSingleExercise(newExercise)
                        showAddDialog = false
                        recreate()
                    }
                )
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
fun AddExerciseDialog(
    onDimiss: () -> Unit,
    onSave: (Exercise) -> Unit
){
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Ngực") }
    var type by remember { mutableStateOf("COMPOUND") }
    var guide by remember { mutableStateOf("") }
    var starRate by remember { mutableStateOf(5) }

    // 🚨 BIẾN LƯU ĐƯỜNG DẪN ẢNH ĐƯỢC CHỌN TỪ GALLERY
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // 🚨 BỘ PHÓNG VÀO GALLERY ĐIỆN THOẠI ĐỂ NHẶT ẢNH
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri // Nhặt được ảnh thì gán vào biến
    }

    // Định nghĩa bảng màu chuẩn cho Ô nhập liệu trên nền ĐEN
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,      // Chữ gõ vào lúc đang chọn: MÀU TRẮNG
        unfocusedTextColor = Color.White,    // Chữ gõ vào lúc bình thường: MÀU TRẮNG
        focusedLabelColor = Color.Yellow,    // Chữ nhãn (Label) lúc chọn: MÀU VÀNG
        unfocusedLabelColor = Color.LightGray, // Chữ nhãn lúc bình thường: MÀU XÁM SÁNG
        focusedBorderColor = Color.Red,      // Viền ô lúc chọn: MÀU ĐỎ
        unfocusedBorderColor = Color.DarkGray // Viền ô lúc bình thường: MÀU XÁM TỐI
    )

    Dialog(onDismissRequest = onDimiss){
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            LazyColumn( // Đổi thành LazyColumn đề phòng chọn ảnh to quá làm tràn màn hình
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("THÊM BÀI TẬP MỚI CỦA BẠN", color = Color.Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                // 📸 KHỐI CHỌN ẢNH VÀ HIỂN THỊ XEM TRƯỚC (PREVIEW)
                item {
                    Text("Ảnh minh họa bài tập:", color = Color.Gray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Nút bấm kích hoạt mở Gallery điện thoại
                        Button(
                            onClick = { galleryLauncher.launch("image/*") }, // Chỉ lọc lấy file ảnh
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                        ) {
                            Text("Chọn ảnh từ máy", color = Color.White)
                        }

                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(Color.Black, shape = RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedImageUri != null) {

                                Image(
                                    painter = rememberAsyncImagePainter(model = selectedImageUri),
                                    contentDescription = "Ảnh xem trước",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop // Ép ảnh vừa vặn khung bo góc cho đẹp
                                )
                            } else {
                                Text("Chưa có", color = Color.DarkGray, fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Ô nhập tên (ĐÃ FIX MÀU CHỮ)
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {name = it},
                        label = { Text("Tên bài tập") },
                        colors = textFieldColors, // 🚨 ĐÃ ÉP MÀU TRẮNG
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Ô nhập hướng dẫn ngắn (ĐÃ FIX MÀU CHỮ)
                item {
                    OutlinedTextField(
                        value = guide,
                        onValueChange = {guide = it},
                        label = { Text("Mô tả ngắn / Hướng dẫn") },
                        colors = textFieldColors, // 🚨 ĐÃ ÉP MÀU TRẮNG
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text("Nhóm cơ: ", color = Color.Gray, fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)){
                        listOf("Ngực", "Lưng", "Chân").forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat) }
                            )
                        }
                    }
                }

                item {
                    Text("Dạng bài tập", color = Color.Gray, fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("COMPOUND", "ISOLATION").forEach { t ->
                            FilterChip(
                                selected = type == t,
                                onClick = { type = t },
                                label = { Text(t) }
                            )
                        }
                    }
                }

                item {
                    Text("Đánh giá độ hiệu quả:", color = Color.Gray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        for (i in 1..5){
                            Text(
                                text = if (i <= starRate) "★" else "☆",
                                color = Color(0xFFFFD700),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable {
                                    starRate = i
                                }
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        TextButton(onClick = onDimiss) {
                            Text("Hủy", color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    val newExercise = Exercise(
                                        name = name,
                                        category = category,
                                        guide = guide,
                                        type = type,
                                        mainImage = selectedImageUri?.toString() ?: "",
                                        starRate = starRate,
                                        isCustom = true
                                    )
                                    onSave(newExercise)
                                }
                            }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) { Text("Lưu Bài", color = Color.White)}
                    }
                }
            }
        }
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

    val isUriImage = remember(exercise.mainImage) {
        exercise.mainImage.startsWith("content://") || exercise.mainImage.startsWith("file://") }

    val imageResId = remember(exercise.mainImage) {
        if (!isUriImage && exercise.mainImage.isNotBlank()){
        context.resources.getIdentifier(exercise.mainImage, "drawable", context.packageName)
    } else {
        0
        }
    }

    val imagePainter = if (isUriImage){
        rememberAsyncImagePainter(model = exercise.mainImage)
    } else {
        painterResource(id = if (imageResId != 0) imageResId else android.R.drawable.ic_menu_gallery)
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
                painter = imagePainter,
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        exercise.name,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f) // Ép tên bài tập co giãn để nhường chỗ cho các Tag
                    )

                    // Khối chứa các nhãn Tag ở bên phải
                    Row(verticalAlignment = Alignment.CenterVertically) {

                        // 🚨 NHÃN "TỰ THÊM": Chỉ hiện khi bài tập đó do user tự chế
                        if (exercise.isCustom) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFFF8C00), shape = RoundedCornerShape(4.dp)) // Màu cam đậm rực rỡ
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "TỰ THÊM",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp)) // Cách ra một chút với nhãn type
                        }

                        // Nhãn Compound / Isolation cũ của bác
                        Text(
                            text = exercise.type,
                            color = if (exercise.type == "COMPOUND") Color.Yellow else Color.Cyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
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
                        painter = imagePainter,
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