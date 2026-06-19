package com.example.gymmentor.timer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.example.gymmentor.data.ExerciseData.AppDatabase
import com.example.gymmentor.data.ExerciseData.Exercise
import com.example.gymmentor.data.ExerciseData.RoutineExercise
import kotlinx.coroutines.launch

class ExerciseLibraryActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Kết nối hai đầu cổng DAO database
        val database = AppDatabase.getDatabase(this)


        setContent {
            val context = LocalContext.current
            val database = AppDatabase.getDatabase(context)


            var databaseExercises by remember { mutableStateOf<List<Exercise>>(emptyList()) }
            val coroutineScope = rememberCoroutineScope()
            var streakCount by remember { mutableStateOf(0) }

            LaunchedEffect(Unit) {
                databaseExercises = database.exerciseDao().getAllExercise()

                val currentStreak = database.workoutDao().getUserStreak()
                if (currentStreak != null) {
                    streakCount = currentStreak.streakCount
                }
            }

            var selectedCategory by remember { mutableStateOf("Tất cả") }
            var showAddDialog by remember { mutableStateOf(false) }
            var selectedExerciseForRoutine by remember { mutableStateOf<Exercise?>(null) }
            var selectedExerciseForEdit by remember { mutableStateOf<Exercise?>(null) }

            val filterExercises = if (selectedCategory == "Tất cả") {
                databaseExercises
            } else {
                databaseExercises.filter { it.category == selectedCategory }
            }
                .sortedBy { if (it.type == "COMPOUND") 0 else 1 }

            Scaffold(
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { showAddDialog = true },
                        containerColor = Color(0xFFFFA500),
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
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Text(
                            text = "THƯ VIỆN BÀI TẬP",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )


                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .background(Color(0xFF1C1C1E), shape = RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "🔥", fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "$streakCount Buổi",
                                    color = Color(0xFFFF8C00),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1
                                )
                            }


                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF2C2C2E), shape = RoundedCornerShape(6.dp))
                                    .clickable {
                                        context.startActivity(Intent(context, RoutineListActivity::class.java))
                                        Toast.makeText(context, "Mở danh sách Gói tập", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Gói Tập 📋",
                                    color = Color.Yellow,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    CategorySelector(
                        categories = listOf("Tất cả", "Ngực", "Lưng", "Chân", "Vai", "Tay"),
                        selectedCategory = selectedCategory,
                        onCategorySelected = { selectedCategory = it }
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(filterExercises) { exercise ->
                            ExerciseCard(
                                exercise = exercise,
                                onClick = {
                                    val intent = Intent(this@ExerciseLibraryActivity, ExerciseDetailActivity::class.java).apply {
                                        putExtra("EXERCISE_ID", exercise.id)
                                        putExtra("EXERCISE_NAME", exercise.name)
                                        putExtra("IS_CUSTOM", exercise.isCustom)
                                    }
                                    startActivity(intent)
                                },
                                onAddToRoutineClick = { selectedExerciseForRoutine = exercise },

                                onDeleteClick = {
                                    coroutineScope.launch {
                                        database.exerciseDao().deleteCustomExerciseById(exercise.id)
                                        databaseExercises = database.exerciseDao().getAllExercise()
                                        Toast.makeText(context, "Đã xóa bài tập khỏi hệ thống!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onEditClick = { selectedExerciseForEdit = exercise }

                            )
                        }
                    }
                }
            }


            if (showAddDialog) {
                AddExerciseDialog(
                    onDimiss = { showAddDialog = false },
                    onSave = { newExercise ->
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
                        database.exerciseDao().insertSingleExercise(newExercise)
                        showAddDialog = false
                        recreate()
                    }
                )
            }

            if (selectedExerciseForRoutine != null) {
                val routinesByFlow by database.workoutDao().getAllRoutines().collectAsState(initial = emptyList())

                Dialog(onDismissRequest = { selectedExerciseForRoutine = null }) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "THÊM BÀI [${selectedExerciseForRoutine?.name?.uppercase()}] VÀO GÓI:",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )

                            if (routinesByFlow.isEmpty()) {
                                Text("Bạn chưa tạo gói tập nào dưới máy ảo. Hãy tạo gói trước!", color = Color.Gray, fontSize = 13.sp)
                            } else {
                                LazyColumn(
                                    modifier = Modifier.heightIn(max = 200.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(routinesByFlow) { routine ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color(0xFF2C2C2E), shape = RoundedCornerShape(8.dp))
                                                .clickable {
                                                    val currentExercise = selectedExerciseForRoutine

                                                    if (currentExercise != null) {
                                                        coroutineScope.launch {
                                                            val isExist = database.workoutDao().isExerciseInRoutine(routine.id, currentExercise.id)

                                                            if (isExist > 0) {
                                                                Toast.makeText(context, "Bài [${currentExercise.name}] đã có trong gói này rồi bác ơi!", Toast.LENGTH_SHORT).show()
                                                            } else {
                                                                database.workoutDao().addExerciseToRoutine(
                                                                    RoutineExercise(
                                                                        routineId = routine.id,
                                                                        exerciseId = currentExercise.id,
                                                                        targetSets = 4
                                                                    )
                                                                )
                                                                Toast.makeText(context, "Đã nhặt vào gói: ${routine.name}", Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                    }
                                                    selectedExerciseForRoutine = null // Đóng dialog
                                                }
                                                .padding(14.dp)
                                        ) {
                                            Text(text = "📋  ${routine.name}", color = Color.Yellow, fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }

                            TextButton(
                                onClick = { selectedExerciseForRoutine = null },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Đóng", color = Color.Red)
                            }
                        }
                    }
                }
            }

            if (selectedExerciseForEdit != null) {
                EditExerciseDialog(
                    exercise = selectedExerciseForEdit!!,
                    onDimiss = { selectedExerciseForEdit = null },
                    onSave = { updatedExercise ->
                        if (updatedExercise.mainImage.startsWith("content://")) {
                            try {
                                context.contentResolver.takePersistableUriPermission(
                                    Uri.parse(updatedExercise.mainImage),
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                                )
                            } catch (e: Exception) { e.printStackTrace() }
                        }
                        coroutineScope.launch {
                            database.exerciseDao().insertSingleExercise(updatedExercise)
                            databaseExercises = database.exerciseDao().getAllExercise()
                            selectedExerciseForEdit = null
                            Toast.makeText(context, "Đã cập nhật bài tập thành công!", Toast.LENGTH_SHORT).show()
                        }
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

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedLabelColor = Color.Yellow,
        unfocusedLabelColor = Color.LightGray,
        focusedBorderColor = Color.Red,
        unfocusedBorderColor = Color.DarkGray
    )

    Dialog(onDismissRequest = onDimiss){
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("THÊM BÀI TẬP MỚI CỦA BẠN", color = Color.Red, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                item {
                    Text("Ảnh minh họa bài tập:", color = Color.Gray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
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
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop // Ép ảnh vừa vặn khung bo góc
                                )
                            } else {
                                Text("Chưa có", color = Color.DarkGray, fontSize = 11.sp)
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {name = it},
                        label = { Text("Tên bài tập") },
                        colors = textFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = guide,
                        onValueChange = {guide = it},
                        label = { Text("Mô tả ngắn / Hướng dẫn") },
                        colors = textFieldColors,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text("Nhóm cơ: ", color = Color.Gray, fontSize = 14.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)){
                        items(listOf("Ngực", "Lưng", "Chân", "Vai", "Tay")) { cat ->
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
                            }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
                        ) { Text("Lưu Bài", color = Color.White)}
                    }
                }
            }
        }
    }
}

@Composable
fun EditExerciseDialog(
    exercise: Exercise,
    onDimiss: () -> Unit,
    onSave: (Exercise) -> Unit
) {
    // Đổ ngược dữ liệu hiện tại của bài tập vào trạng thái ban đầu
    var name by remember { mutableStateOf(exercise.name) }
    var category by remember { mutableStateOf(exercise.category) }
    var type by remember { mutableStateOf(exercise.type) }
    var guide by remember { mutableStateOf(exercise.guide) }
    var starRate by remember { mutableStateOf(exercise.starRate) }

    // Xử lý Uri ảnh cũ nếu có
    var selectedImageUri by remember { mutableStateOf<Uri?>(if (exercise.mainImage.startsWith("content://")) Uri.parse(exercise.mainImage) else null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> if (uri != null) selectedImageUri = uri }

    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
        focusedLabelColor = Color.Yellow, unfocusedLabelColor = Color.LightGray,
        focusedBorderColor = Color.Red, unfocusedBorderColor = Color.DarkGray
    )

    Dialog(onDismissRequest = onDimiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            LazyColumn(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { Text("CHỈNH SỬA BÀI TẬP", color = Color(0xFFFFA500), fontSize = 18.sp, fontWeight = FontWeight.Bold) }

                item {
                    Text("Ảnh minh họa bài tập:", color = Color.Gray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = { galleryLauncher.launch("image/*") }, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
                            Text("Đổi ảnh từ máy", color = Color.White)
                        }
                        Box(modifier = Modifier.size(60.dp).background(Color.Black, shape = RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                            if (selectedImageUri != null) {
                                Image(painter = rememberAsyncImagePainter(model = selectedImageUri), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                            } else if (exercise.mainImage.isNotBlank() && !exercise.mainImage.startsWith("content://")) {
                                val context = LocalContext.current
                                val id = context.resources.getIdentifier(exercise.mainImage, "drawable", context.packageName)
                                if (id != 0) Image(painter = painterResource(id = id), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = androidx.compose.ui.layout.ContentScale.Crop)
                            } else { Text("Chưa có", color = Color.DarkGray, fontSize = 11.sp) }
                        }
                    }
                }

                item { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tên bài tập") }, colors = textFieldColors, modifier = Modifier.fillMaxWidth()) }
                item { OutlinedTextField(value = guide, onValueChange = { guide = it }, label = { Text("Mô tả ngắn / Hướng dẫn") }, colors = textFieldColors, modifier = Modifier.fillMaxWidth()) }

                item {
                    Text("Nhóm cơ: ", color = Color.Gray, fontSize = 14.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(listOf("Ngực", "Lưng", "Chân", "Vai", "Tay")) { cat ->
                            FilterChip(selected = category == cat, onClick = { category = cat }, label = { Text(cat) })
                        }
                    }
                }

                item {
                    Text("Dạng bài tập", color = Color.Gray, fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("COMPOUND", "ISOLATION").forEach { t ->
                            FilterChip(selected = type == t, onClick = { type = t }, label = { Text(t) })
                        }
                    }
                }

                item {
                    Text("Đánh giá độ hiệu quả:", color = Color.Gray, fontSize = 14.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        for (i in 1..5) {
                            Text(text = if (i <= starRate) "★" else "☆", color = Color(0xFFFFD700), fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { starRate = i })
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = onDimiss) { Text("Hủy", color = Color.Gray) }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (name.isNotBlank()) {
                                    // Giữ nguyên ID gốc của bài tập để Room cập nhật đè (Update) thay vì tạo mới
                                    val updated = exercise.copy(
                                        name = name, category = category, guide = guide, type = type,
                                        mainImage = selectedImageUri?.toString() ?: exercise.mainImage,
                                        starRate = starRate
                                    )
                                    onSave(updated)
                                }
                            }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500))
                        ) { Text("Cập Nhật", color = Color.White) }
                    }
                }
            }
        }
    }
}

@Composable
fun CategorySelector(categories: List<String>, selectedCategory: String, onCategorySelected: (String) -> Unit) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(end = 16.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory
            Box(
                modifier = Modifier
                    .background(if (isSelected) Color.Red else Color.DarkGray, shape = RoundedCornerShape(8.dp))
                    .clickable { onCategorySelected(category) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = category, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }
    }
}

@Composable
fun ExerciseCard(
    exercise: Exercise,
    onClick: () -> Unit,
    onAddToRoutineClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    var showOptionsDialog by remember { mutableStateOf(false) }

    val isUriImage = remember(exercise.mainImage) {
        exercise.mainImage.startsWith("content://") || exercise.mainImage.startsWith("file://") }

    val imageResId = remember(exercise.mainImage) {
        if (!isUriImage && exercise.mainImage.isNotBlank()){
            context.resources.getIdentifier(exercise.mainImage, "drawable", context.packageName)
        } else { 0 }
    }

    val imagePainter = if (isUriImage){
        rememberAsyncImagePainter(model = exercise.mainImage)
    } else {
        painterResource(id = if (imageResId != 0) imageResId else android.R.drawable.ic_menu_gallery)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onClick() },
                onLongClick = {
                    if (exercise.isCustom) {
                        showOptionsDialog = true
                    }
                }
            ),
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
                    .clickable { showDialog = true }
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
                        modifier = Modifier.weight(1f)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (exercise.isCustom) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFFF8C00), shape = RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "CUSTOM",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        Text(
                            text = exercise.type,
                            color = if (exercise.type == "COMPOUND") Color.Yellow else Color.Cyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Bọc Column vào weight(1f)
                    Column(modifier = Modifier.weight(1f)) {
                        StarRatingBar(rating = exercise.starRate)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = exercise.guide,
                            color = Color.Gray,
                            fontSize = 13.sp,
                            maxLines = 2
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.Red, shape = CircleShape)
                            .clickable { onAddToRoutineClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("+", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showOptionsDialog) {
        Dialog(onDismissRequest = { showOptionsDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2C2C2E)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "TÙY CHỌN BÀI TẬP: ${exercise.name.uppercase()}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    Button(
                        onClick = { showOptionsDialog = false; onEditClick() }, // Mở Dialog Sửa
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFA500)),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Chỉnh Sửa Kỹ Thuật ✏️", color = Color.White) }

                    Button(
                        onClick = { showOptionsDialog = false; showDeleteConfirm = true }, // Mở Dialog Xóa
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Xóa Bài Tập Khỏi Hệ Thống 🗑️", color = Color.White) }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = Color(0xFF1C1C1E),
            title = { Text("XÓA BÀI TẬP TỰ CHẾ", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = { Text("Bạn có chắc chắn muốn xóa bài [${exercise.name.uppercase()}] khỏi thư viện? Hành vi này sẽ xóa bài này khỏi tất cả các Gói tập hiện tại.", color = Color.White, fontSize = 14.sp) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteClick()
                        showDeleteConfirm = false
                    }
                ) { Text("XÓA BỎ", color = Color.Red, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("HỦY", color = Color.Gray) }
            }
        )
    }
    // Khối Dialog phóng to ảnh
    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }){
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
                        onClick = { showDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("Đóng", color = Color.White)
                    }
                }
            }
        }
    }
}