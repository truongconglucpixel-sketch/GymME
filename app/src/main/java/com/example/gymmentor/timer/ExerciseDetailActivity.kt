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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.example.gymmentor.data.AppDatabase
import com.example.gymmentor.data.ExerciseGuide

class ExerciseDetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val exerciseId = intent.getIntExtra("EXERCISE_ID", 1)
        val exerciseName = intent.getStringExtra("EXERCISE_NAME") ?: "CHI TIẾT BÀI TẬP"

        // 🚨 CHÚ Ý: Nhận thêm trạng thái tự chế từ Thư viện quăng sang (mặc định bài gốc là false)
        val isCustomExercise = intent.getBooleanExtra("IS_CUSTOM", false)

        val database = AppDatabase.getDatabase(this)

        setContent {
            // Biến trạng thái chứa danh sách các bước hướng dẫn
            var guideList by remember { mutableStateOf<List<ExerciseGuide>>(emptyList()) }
            var showAddGuideDialog by remember { mutableStateOf(false) }

            // Lôi dữ liệu hướng dẫn lên ngay khi mở màn hình
            LaunchedEffect(Unit) {
                guideList = database.exerciseDao().getGuidesForExercise(exerciseId)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(16.dp)
            ) {
                // HÀNG TIÊU ĐỀ CHÍNH
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = exerciseName.uppercase(),
                            color = Color.Red,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "HƯỚNG DẪN KỸ THUẬT FORM CHUẨN",
                            color = Color.Yellow,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // 🚨 ĐẶC QUYỀN: Chỉ có bài tập CUSTOM mới lộ diện chiếc nút thêm bước này!
                    if (isCustomExercise) {
                        Button(
                            onClick = { showAddGuideDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8C00)), // Màu cam Custom
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("+ Bước", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // DANH SÁCH CUỘN CÁC BƯỚC TẬP LUYỆN
                if (guideList.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Chưa có bước hướng dẫn nào.\nBấm [+ Bước] để tự chế kỹ thuật bác ơi!", color = Color.Gray, fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.weight(1f).fillMaxWidth()
                    ) {
                        items(guideList) { step ->
                            DetailStepCard(step = step)
                        }
                    }
                }
            }

            if (showAddGuideDialog) {
                AddGuideStepDialog(
                    nextStepNumber = guideList.size + 1, // Tự động tính toán số bước tiếp theo (vd: Bước 4)
                    onDismiss = { showAddGuideDialog = false },
                    onSave = { instructionText, imageUriString ->
                        // Chạy lệnh chèn dữ liệu thô xuống bảng hướng dẫn
                        database.exerciseDao().insertSingleGuide(
                            ExerciseGuide(
                                exerciseId = exerciseId,
                                imageName = imageUriString, // Lưu chuỗi Uri ảnh nhặt từ máy dưới dạng text
                                instruction = instructionText,
                                stepNumber = guideList.size + 1
                            )
                        )
                        // Tải lại danh sách bước mới lập tức lên màn hình
                        guideList = database.exerciseDao().getGuidesForExercise(exerciseId)
                        showAddGuideDialog = false
                    }
                )
            }
        }
    }
}

// COMPONENT VẼ TỪNG THẺ BƯỚC TẬP (HỖ TRỢ CẢ ẢNH GỐC VÀ ẢNH GALLERY CỦA USER)
@Composable
fun DetailStepCard(step: ExerciseGuide) {
    val context = LocalContext.current

    // Check xem ảnh này là Uri từ máy người dùng chọn hay là tên ảnh chuỗi drawable gốc hệ thống
    val isUriImage = remember(step.imageName) {
        step.imageName.startsWith("content://") || step.imageName.startsWith("file://")
    }

    val imagePainter = if (isUriImage) {
        rememberAsyncImagePainter(model = step.imageName)
    } else {
        val imageResId = context.resources.getIdentifier(step.imageName, "drawable", context.packageName)
        painterResource(id = if (imageResId != 0) imageResId else android.R.drawable.ic_menu_gallery)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = imagePainter,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color.DarkGray)
            )

            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    text = "BƯỚC ${step.stepNumber}:",
                    color = Color.Red,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = step.instruction,
                    color = Color.White,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

// HỘP THOẠI DIALOG NHẬP BƯỚC HƯỚNG DẪN CUSTOM
@Composable
fun AddGuideStepDialog(
    nextStepNumber: Int,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    val context = LocalContext.current
    var instruction by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Bộ chọn ảnh từ máy
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            // Cấp quyền đọc ảnh dài hạn cho Uri để không bị mất ảnh khi khởi động lại máy ảo
            try {
                context.contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "THÊM BƯỚC HƯỚNG DẪN SỐ $nextStepNumber", color = Color.Red, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                // Mục chọn ảnh minh họa bước tập
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { galleryLauncher.launch("image/*") }, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
                        Text("Chọn ảnh bước")
                    }
                    Box(modifier = Modifier.size(50.dp).background(Color.Black, shape = RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) {
                        if (selectedImageUri != null) {
                            Image(painter = rememberAsyncImagePainter(selectedImageUri), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        } else {
                            Text("Trống", color = Color.Gray, fontSize = 11.sp)
                        }
                    }
                }

                // Ô nhập nội dung kỹ thuật động tác
                OutlinedTextField(
                    value = instruction,
                    onValueChange = { instruction = it },
                    label = { Text("Mô tả kỹ thuật động tác của bước này") },
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedLabelColor = Color.Yellow, focusedBorderColor = Color.Red),
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Hủy", color = Color.Gray) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (instruction.isNotBlank()) {
                                onSave(instruction, selectedImageUri?.toString() ?: "")
                            } else {
                                Toast.makeText(context, "Bác vui lòng nhập nội dung mô tả nhé!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) { Text("Lưu Bước", color = Color.White) }
                }
            }
        }
    }
}