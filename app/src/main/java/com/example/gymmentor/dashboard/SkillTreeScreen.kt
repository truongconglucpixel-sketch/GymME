// com/example/gymmentor/dashboard/SkillTreeScreen.kt
package com.example.gymmentor.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

// ─── Color palette (nền đen + neon) ──────────────────────
object GymColors {
    val Background     = Color(0xFF0A0A0A)
    val Surface        = Color(0xFF141414)
    val SurfaceVariant = Color(0xFF1C1C1C)
    val Border         = Color(0xFF2A2A2A)

    val Neon           = Color(0xFF39FF14)   // xanh lá neon — mastered
    val Cyan           = Color(0xFF00E5FF)   // cyan — in progress
    val Amber          = Color(0xFFFFB300)   // vàng — available
    val Locked         = Color(0xFF3A3A3A)

    val TextPrimary    = Color(0xFFF0F0F0)
    val TextSecondary  = Color(0xFF888888)
    val TextHint       = Color(0xFF4A4A4A)
}

// ─── Main Screen ─────────────────────────────────────────
@Composable
fun SkillTreeScreen(vm: SkillTreeViewModel = viewModel()) {
    val state by vm.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GymColors.Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SkillTreeHeader()
            GroupTabRow(
                selected = state.selectedGroup,
                onSelect = { vm.onEvent(SkillTreeEvent.SelectGroup(it)) }
            )

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GymColors.Neon)
                }
            } else {
                val tree = state.trees.find { it.group == state.selectedGroup }
                tree?.let {
                    NodeList(
                        tree = it,
                        onNodeClick = { node ->
                            if (node.state != NodeState.LOCKED) {
                                vm.onEvent(SkillTreeEvent.SelectNode(node))
                            }
                        }
                    )
                }
            }
        }

        // Bottom sheet node detail
        state.selectedNode?.let { node ->
            NodeDetailSheet(
                node = node,
                justLeveledUp = state.justLeveledUp,
                justUnlocked = state.justUnlocked,
                onDismiss = { vm.onEvent(SkillTreeEvent.DismissNode) },
                onTrainNow = { vm.onEvent(SkillTreeEvent.LogSession(node.id)) }
            )
        }
    }
}

// ─── Header ──────────────────────────────────────────────
@Composable
fun SkillTreeHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(GymColors.Surface)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Column {
            Text(
                text = "MUSCLE SKILL TREE",
                color = GymColors.Neon,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp
            )
            Text(
                text = "Tap a skill to view details & train",
                color = GymColors.TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

// ─── Group Tab Row ────────────────────────────────────────
@Composable
fun GroupTabRow(
    selected: MuscleGroup,
    onSelect: (MuscleGroup) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(GymColors.Surface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MuscleGroup.entries.forEach { group ->
            val isSelected = group == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) GymColors.Neon.copy(alpha = 0.12f)
                        else GymColors.SurfaceVariant
                    )
                    .border(
                        width = if (isSelected) 1.dp else 0.5.dp,
                        color = if (isSelected) GymColors.Neon else GymColors.Border,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onSelect(group) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = group.emoji, fontSize = 16.sp)
                    Text(
                        text = group.displayName.split(" ").first(),
                        color = if (isSelected) GymColors.Neon else GymColors.TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }
        }
    }
    HorizontalDivider(color = GymColors.Border, thickness = 0.5.dp)
}

// ─── Node List ────────────────────────────────────────────
@Composable
fun NodeList(
    tree: MuscleGroupTree,
    onNodeClick: (SkillNode) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        items(tree.nodes.size) { index ->
            val node = tree.nodes[index]
            val isLast = index == tree.nodes.lastIndex

            NodeRow(
                node = node,
                showConnector = !isLast,
                nextNodeState = if (!isLast) tree.nodes[index + 1].state else null,
                onClick = { onNodeClick(node) }
            )
        }
    }
}

// ─── Single Node Row (node + connector line) ──────────────
@Composable
fun NodeRow(
    node: SkillNode,
    showConnector: Boolean,
    nextNodeState: NodeState?,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        NodeCard(node = node, onClick = onClick)

        if (showConnector) {
            ConnectorLine(
                fromState = node.state,
                toState = nextNodeState ?: NodeState.LOCKED
            )
        }
    }
}