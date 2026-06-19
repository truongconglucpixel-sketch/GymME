// com/example/gymmentor/dashboard/SkillTreeComponents.kt
package com.example.gymmentor.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

// ─── NodeCard ────────────────────────────────────────────
@Composable
fun NodeCard(node: SkillNode, onClick: () -> Unit) {
    val accentColor = when (node.state) {
        NodeState.MASTERED    -> GymColors.Neon
        NodeState.IN_PROGRESS -> GymColors.Cyan
        NodeState.AVAILABLE   -> GymColors.Amber
        NodeState.LOCKED      -> GymColors.Locked
    }
    val bgColor = when (node.state) {
        NodeState.MASTERED    -> GymColors.Neon.copy(alpha = 0.08f)
        NodeState.IN_PROGRESS -> GymColors.Cyan.copy(alpha = 0.06f)
        NodeState.AVAILABLE   -> GymColors.Amber.copy(alpha = 0.06f)
        NodeState.LOCKED      -> GymColors.SurfaceVariant
    }
    val isClickable = node.state != NodeState.LOCKED

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(
                width = if (node.state == NodeState.IN_PROGRESS) 1.5.dp else 0.5.dp,
                color = accentColor.copy(alpha = if (node.state == NodeState.LOCKED) 0.3f else 0.7f),
                shape = RoundedCornerShape(12.dp)
            )
            .then(
                if (isClickable) Modifier.clickable { onClick() }
                else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // State icon circle
        StateIcon(state = node.state, progress = node.progressFraction, color = accentColor)

        // Name + difficulty + XP
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = node.name,
                color = if (node.state == NodeState.LOCKED) GymColors.TextHint else GymColors.TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 3.dp)
            ) {
                DifficultyPill(node.difficulty, node.state)
                if (node.state == NodeState.IN_PROGRESS) {
                    Text(
                        text = "${node.currentXp} / ${node.targetXp} XP",
                        color = GymColors.Cyan,
                        fontSize = 11.sp
                    )
                }
                if (node.streakDays > 0) {
                    Text(
                        text = "🔥 ${node.streakDays}d",
                        color = Color(0xFFFF6B35),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Right side
        when (node.state) {
            NodeState.MASTERED -> Text(
                text = "Lv.${node.level}",
                color = GymColors.Neon,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            NodeState.IN_PROGRESS -> Text(
                text = "Lv.${node.level}",
                color = GymColors.Cyan,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            NodeState.AVAILABLE -> Icon(
                Icons.Default.PlayArrow,
                contentDescription = null,
                tint = GymColors.Amber,
                modifier = Modifier.size(20.dp)
            )
            NodeState.LOCKED -> Icon(
                Icons.Default.Lock,
                contentDescription = null,
                tint = GymColors.TextHint,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ─── State Icon ───────────────────────────────────────────
@Composable
fun StateIcon(state: NodeState, progress: Float, color: Color) {
    Box(
        modifier = Modifier.size(40.dp),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            NodeState.IN_PROGRESS -> {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(36.dp),
                    strokeWidth = 2.5.dp,
                    color = color,
                    trackColor = GymColors.Border
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    color = color,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            NodeState.MASTERED -> {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f))
                        .border(1.dp, color, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, null, tint = color, modifier = Modifier.size(18.dp))
                }
            }
            NodeState.AVAILABLE -> {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.12f))
                        .border(1.dp, color.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PlayArrow, null, tint = color, modifier = Modifier.size(18.dp))
                }
            }
            NodeState.LOCKED -> {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(GymColors.SurfaceVariant)
                        .border(1.dp, GymColors.Border, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Lock, null, tint = GymColors.TextHint, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

// ─── Connector Line ───────────────────────────────────────
@Composable
fun ConnectorLine(fromState: NodeState, toState: NodeState) {
    val color = when {
        fromState == NodeState.MASTERED && toState == NodeState.MASTERED -> GymColors.Neon
        fromState == NodeState.MASTERED || fromState == NodeState.IN_PROGRESS -> GymColors.Cyan.copy(alpha = 0.5f)
        else -> GymColors.Border
    }
    val isDashed = toState == NodeState.LOCKED

    Box(
        modifier = Modifier
            .width(2.dp)
            .height(28.dp)
            .background(
                if (!isDashed) color
                else Color.Transparent
            )
    ) {
        if (isDashed) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val dashHeight = 6.dp.toPx()
                val gapHeight = 4.dp.toPx()
                var y = 0f
                while (y < size.height) {
                    drawRect(
                        color = color,
                        topLeft = Offset(0f, y),
                        size = androidx.compose.ui.geometry.Size(size.width, minOf(dashHeight, size.height - y))
                    )
                    y += dashHeight + gapHeight
                }
            }
        }
    }
}

// ─── Difficulty Pill ─────────────────────────────────────
@Composable
fun DifficultyPill(difficulty: Difficulty, nodeState: NodeState) {
    val (label, color) = when (difficulty) {
        Difficulty.BEGINNER     -> "Beginner"     to Color(0xFF4CAF50)
        Difficulty.INTERMEDIATE -> "Intermediate" to Color(0xFFFF9800)
        Difficulty.ADVANCED     -> "Advanced"     to Color(0xFFFF5252)
    }
    val alpha = if (nodeState == NodeState.LOCKED) 0.3f else 1f

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f * alpha))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            color = color.copy(alpha = alpha),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// ─── Node Detail Dialog ───────────────────────────────────
@Composable
fun NodeDetailSheet(
    node: SkillNode,
    justLeveledUp: Boolean,
    justUnlocked: String?,
    onDismiss: () -> Unit,
    onTrainNow: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .background(GymColors.Surface)
                    .clickable(enabled = false) {}   // block click-through
                    .padding(bottom = 32.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Handle bar
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .size(width = 40.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(GymColors.Border)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(Modifier.height(16.dp))

                // Notifications
                AnimatedVisibility(
                    visible = justLeveledUp,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    NotificationBanner(
                        text = "⚡ Level up! Now Lv.${node.level}",
                        color = GymColors.Neon
                    )
                }
                AnimatedVisibility(
                    visible = justUnlocked != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    NotificationBanner(
                        text = "🔓 New skill unlocked!",
                        color = GymColors.Amber
                    )
                }

                // Header
                DetailHeader(node)

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    color = GymColors.Border
                )

                // Stats row
                StatRow(node)

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    color = GymColors.Border
                )

                // Tip
                if (node.tip.isNotBlank()) {
                    TipCard(node.tip)
                    Spacer(Modifier.height(16.dp))
                }

                // Exercises
                ExerciseList(node.exercises)

                Spacer(Modifier.height(24.dp))

                // XP progress bar
                XpProgressSection(node)

                Spacer(Modifier.height(20.dp))

                // CTA button
                TrainButton(node = node, onTrainNow = onTrainNow)
            }
        }
    }
}

// ─── Detail Header ────────────────────────────────────────
@Composable
fun DetailHeader(node: SkillNode) {
    val accentColor = when (node.state) {
        NodeState.MASTERED    -> GymColors.Neon
        NodeState.IN_PROGRESS -> GymColors.Cyan
        NodeState.AVAILABLE   -> GymColors.Amber
        NodeState.LOCKED      -> GymColors.TextSecondary
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        StateIcon(node.state, node.progressFraction, accentColor)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = node.name,
                color = GymColors.TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                DifficultyPill(node.difficulty, node.state)
                Text(
                    text = node.muscleGroup.displayName,
                    color = GymColors.TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
        // State badge
        val (badgeLabel, badgeColor) = when (node.state) {
            NodeState.MASTERED    -> "MASTERED"   to GymColors.Neon
            NodeState.IN_PROGRESS -> "ACTIVE"     to GymColors.Cyan
            NodeState.AVAILABLE   -> "AVAILABLE"  to GymColors.Amber
            NodeState.LOCKED      -> "LOCKED"     to GymColors.TextSecondary
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(badgeColor.copy(alpha = 0.15f))
                .border(0.5.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(badgeLabel, color = badgeColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ─── Stat Row ─────────────────────────────────────────────
@Composable
fun StatRow(node: SkillNode) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard("Level", "Lv.${node.level}", GymColors.Neon, modifier = Modifier.weight(1f))
        StatCard("Sessions", "${node.totalSessions}", GymColors.Cyan, modifier = Modifier.weight(1f))
        StatCard(
            label = "Streak",
            value = if (node.streakDays > 0) "${node.streakDays}d 🔥" else "—",
            color = Color(0xFFFF6B35),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(GymColors.SurfaceVariant)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = color, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(label, color = GymColors.TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(top = 2.dp))
    }
}

// ─── Tip Card ─────────────────────────────────────────────
@Composable
fun TipCard(tip: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(GymColors.Amber.copy(alpha = 0.08f))
            .border(0.5.dp, GymColors.Amber.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("💡", fontSize = 16.sp)
        Text(
            text = tip,
            color = GymColors.Amber.copy(alpha = 0.9f),
            fontSize = 13.sp,
            lineHeight = 20.sp
        )
    }
}

// ─── Exercise List ────────────────────────────────────────
@Composable
fun ExerciseList(exercises: List<Exercise>) {
    if (exercises.isEmpty()) return
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "EXERCISES",
            color = GymColors.TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        exercises.forEach { ex ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(GymColors.SurfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = if (ex.isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (ex.isDone) GymColors.Neon else GymColors.TextHint,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = ex.name,
                    color = if (ex.isDone) GymColors.TextPrimary else GymColors.TextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = ex.sets,
                    color = GymColors.Cyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ─── XP Progress ─────────────────────────────────────────
@Composable
fun XpProgressSection(node: SkillNode) {
    val animatedProgress by animateFloatAsState(
        targetValue = node.progressFraction,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "xp_progress"
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("XP Progress", color = GymColors.TextSecondary, fontSize = 12.sp)
            Text(
                "${node.currentXp} / ${node.targetXp} XP",
                color = GymColors.Neon,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(GymColors.Border)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .background(
                        Brush.horizontalGradient(
                            listOf(GymColors.Cyan, GymColors.Neon)
                        )
                    )
            )
        }
    }
}

// ─── Train Button ─────────────────────────────────────────
@Composable
fun TrainButton(node: SkillNode, onTrainNow: () -> Unit) {
    val isMastered = node.state == NodeState.MASTERED
    Button(
        onClick = onTrainNow,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isMastered) GymColors.Neon.copy(alpha = 0.15f) else GymColors.Neon,
            contentColor = if (isMastered) GymColors.Neon else Color.Black
        ),
        shape = RoundedCornerShape(10.dp),
        border = if (isMastered) BorderStroke(1.dp, GymColors.Neon.copy(alpha = 0.5f)) else null
    ) {
        Text(
            text = if (isMastered) "TRAIN AGAIN  +150 XP" else "TRAIN NOW  +150 XP",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

// ─── Notification Banner ──────────────────────────────────
@Composable
fun NotificationBanner(text: String, color: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .border(0.5.dp, color.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(text, color = color, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}