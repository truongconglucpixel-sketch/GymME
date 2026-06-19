// DashboardScreen.kt
package com.gymmentor.ui.dashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// ---------- Data Models ----------
enum class NodeType { ROOT, CHILD }

enum class NodeState {
    LOCKED, IN_PROGRESS, MASTERED
}

data class SkillNode(
    val id: String,
    val name: String,
    val type: NodeType,
    val state: NodeState,
    val currentXp: Int = 0,
    val targetXp: Int = 100,
    val children: List<SkillNode> = emptyList()
)

// ---------- Mock Data ----------
fun getMockSkillTree(): List<SkillNode> {
    return listOf(
        SkillNode(
            id = "chest",
            name = "Chest",
            type = NodeType.ROOT,
            state = NodeState.IN_PROGRESS,
            children = listOf(
                SkillNode(
                    id = "bench_press",
                    name = "Bench Press",
                    type = NodeType.CHILD,
                    state = NodeState.IN_PROGRESS,
                    currentXp = 450,
                    targetXp = 1000
                ),
                SkillNode(
                    id = "incline_press",
                    name = "Incline Press",
                    type = NodeType.CHILD,
                    state = NodeState.LOCKED,
                    currentXp = 0,
                    targetXp = 800
                ),
                SkillNode(
                    id = "dumbbell_fly",
                    name = "Dumbbell Fly",
                    type = NodeType.CHILD,
                    state = NodeState.MASTERED,
                    currentXp = 1000,
                    targetXp = 1000
                )
            )
        ),
        SkillNode(
            id = "back",
            name = "Back",
            type = NodeType.ROOT,
            state = NodeState.LOCKED,
            children = listOf(
                SkillNode(
                    id = "pull_up",
                    name = "Pull-Up",
                    type = NodeType.CHILD,
                    state = NodeState.LOCKED
                ),
                SkillNode(
                    id = "row",
                    name = "Barbell Row",
                    type = NodeType.CHILD,
                    state = NodeState.LOCKED
                )
            )
        ),
        SkillNode(
            id = "legs",
            name = "Legs",
            type = NodeType.ROOT,
            state = NodeState.IN_PROGRESS,
            children = listOf(
                SkillNode(
                    id = "squat",
                    name = "Squat",
                    type = NodeType.CHILD,
                    state = NodeState.IN_PROGRESS,
                    currentXp = 200,
                    targetXp = 900
                ),
                SkillNode(
                    id = "deadlift",
                    name = "Deadlift",
                    type = NodeType.CHILD,
                    state = NodeState.MASTERED,
                    currentXp = 1000,
                    targetXp = 1000
                )
            )
        )
    )
}

// ---------- Main Screen ----------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    val skillTree = remember { getMockSkillTree() }
    // Track expanded state per root node id
    val expandedMap = remember { mutableStateMapOf<String, Boolean>().apply {
        skillTree.forEach { put(it.id, true) } // all expanded by default
    } }

    // Bottom sheet state
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedChild by remember { mutableStateOf<SkillNode?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Main background
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFF121212))
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Optional header
            item {
                Text(
                    text = "MUSCLE SKILL TREE",
                    color = Color(0xFF39FF14),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }

            items(skillTree) { root ->
                RootNode(
                    node = root,
                    isExpanded = expandedMap[root.id] ?: true,
                    onToggle = { expandedMap[root.id] = !(expandedMap[root.id] ?: true) },
                    onChildClick = { child ->
                        selectedChild = child
                        coroutineScope.launch { sheetState.show() }
                    }
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }

        // Bottom Sheet for child details
        if (selectedChild != null) {
            ModalBottomSheet(
                onDismissRequest = {
                    coroutineScope.launch { sheetState.hide() }
                    selectedChild = null
                },
                sheetState = sheetState,
                containerColor = Color(0xFF1E1E1E),
                tonalElevation = 0.dp,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ) {
                selectedChild?.let { child ->
                    ChildDetailSheet(child = child, onTrainClick = {
                        // Handle Train Now
                        coroutineScope.launch { sheetState.hide() }
                        selectedChild = null
                    })
                }
            }
        }
    }
}

// ---------- Root Node ----------
@Composable
fun RootNode(
    node: SkillNode,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onChildClick: (SkillNode) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Root card (hexagon-like shape with corners)
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clickable { onToggle() }
                .padding(2.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (node.state == NodeState.MASTERED) Color(0xFF39FF14) else Color(0xFF1E1E1E)
            ),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 18.dp, horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = node.name.uppercase(),
                    color = if (node.state == NodeState.MASTERED) Color.Black else Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = if (node.state == NodeState.MASTERED) Color.Black else Color(0xFF39FF14)
                )
            }
        }

        // Vertical connecting line from root to children
        if (node.children.isNotEmpty()) {
            // Line with optional glow if any child mastered
            val hasMastered = node.children.any { it.state == NodeState.MASTERED }
            val lineColor = when {
                hasMastered -> Color(0xFF39FF14)
                node.children.any { it.state == NodeState.IN_PROGRESS } -> Color(0xFF00FFFF)
                else -> Color.Gray
            }
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(if (isExpanded) 24.dp else 0.dp)
                    .background(
                        brush = if (hasMastered) Brush.verticalGradient(
                            colors = listOf(Color(0xFF39FF14), Color(0xFF39FF14).copy(alpha = 0.3f))
                        ) else SolidColor(lineColor)
                    )
                    .animateContentSize()
            )
        }

        // Children list (animated expand/collapse)
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
            ) + fadeIn(),
            exit = shrinkVertically(
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            ) + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, top = 8.dp) // indent
            ) {
                node.children.forEachIndexed { index, child ->
                    // Vertical line from previous child to current? We'll draw inside child.
                    ChildNode(
                        node = child,
                        isLast = index == node.children.lastIndex,
                        onChildClick = { onChildClick(child) }
                    )
                    if (index != node.children.lastIndex) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

// ---------- Child Node ----------
@Composable
fun ChildNode(
    node: SkillNode,
    isLast: Boolean,
    onChildClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChildClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Connecting line (vertical dashed or solid)
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(if (isLast) 28.dp else 48.dp)
                .then(
                    if (node.state == NodeState.LOCKED) {
                        Modifier.drawBehind {
                            drawLine(
                                color = Color.Gray,
                                start = Offset(size.width / 2, 0f),
                                end = Offset(size.width / 2, size.height),
                                strokeWidth = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                            )
                        }
                    } else {
                        Modifier.background(
                            brush = if (node.state == NodeState.MASTERED)
                                Brush.verticalGradient(listOf(Color(0xFF39FF14), Color(0xFF39FF14).copy(alpha = 0.5f)))
                            else if (node.state == NodeState.IN_PROGRESS)
                                Brush.verticalGradient(listOf(Color(0xFF00FFFF), Color(0xFF00FFFF).copy(alpha = 0.5f)))
                            else SolidColor(Color.Gray)
                        )
                    }
                )
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Child card
        Card(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = when (node.state) {
                    NodeState.MASTERED -> Color(0xFF39FF14)
                    NodeState.IN_PROGRESS -> Color(0xFF1E1E1E)
                    NodeState.LOCKED -> Color(0xFF2A2A2A)
                }
            ),
            elevation = CardDefaults.cardElevation(0.dp),
            border = if (node.state == NodeState.IN_PROGRESS)
                BorderStroke(2.dp, Color(0xFF00FFFF))
            else null
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Icon or progress indicator
                    when (node.state) {
                        NodeState.LOCKED -> {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = "Locked",
                                tint = Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        NodeState.IN_PROGRESS -> {
                            // Circular progress with neon color
                            val progress = node.currentXp.toFloat() / node.targetXp.toFloat()
                            CircularProgressIndicator(
                                progress = progress,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF00FFFF),
                                trackColor = Color.DarkGray
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        NodeState.MASTERED -> {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Mastered",
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }

                    Text(
                        text = node.name,
                        color = when (node.state) {
                            NodeState.MASTERED -> Color.Black
                            NodeState.IN_PROGRESS -> Color.White
                            NodeState.LOCKED -> Color.DarkGray
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Show XP for in-progress
                if (node.state == NodeState.IN_PROGRESS) {
                    Text(
                        text = "${node.currentXp}/${node.targetXp} XP",
                        color = Color(0xFF00FFFF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Light
                    )
                }
            }
        }
    }
}

// ---------- Child Detail Bottom Sheet ----------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildDetailSheet(
    child: SkillNode,
    onTrainClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = child.name.uppercase(),
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Level (mock)
        val level = when {
            child.currentXp >= child.targetXp -> 5
            child.currentXp > child.targetXp * 0.7f -> 4
            child.currentXp > child.targetXp * 0.4f -> 3
            child.currentXp > child.targetXp * 0.2f -> 2
            else -> 1
        }
        Text(
            text = "Level $level",
            color = Color(0xFF39FF14),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(12.dp))

        // XP Linear Progress
        val progress = (child.currentXp.toFloat() / child.targetXp.toFloat()).coerceIn(0f, 1f)
        LinearProgressIndicator(
            progress = { progress }, // Khuyên dùng dạng lambda { progress } cho Compose M3 mới nhất
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)), // <--- Di chuyển bo góc vào Modifier
            color = Color(0xFF39FF14),
            trackColor = Color.DarkGray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${child.currentXp} XP",
                color = Color.LightGray,
                fontSize = 14.sp
            )
            Text(
                text = "${child.targetXp} XP",
                color = Color.LightGray,
                fontSize = 14.sp
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Train Now button
        Button(
            onClick = onTrainClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF39FF14),
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(8.dp),
            elevation = ButtonDefaults.buttonElevation(0.dp)
        ) {
            Text(
                text = "TRAIN NOW",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
    }
}

// ---------- Preview ----------
@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun PreviewDashboard() {
    DashboardScreen()
}