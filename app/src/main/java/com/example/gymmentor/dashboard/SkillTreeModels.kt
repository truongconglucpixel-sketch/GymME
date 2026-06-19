package com.example.gymmentor.dashboard


// ─── Enums ───────────────────────────────────────────────
enum class MuscleGroup(val displayName: String, val emoji: String) {
    CHEST("Chest", "💪"),
    BACK("Back", "🦾"),
    LEGS("Legs", "🦵"),
    ARMS("Arms & Shoulders", "🏋️")
}

enum class NodeState {
    LOCKED,       // chưa đủ điều kiện mở
    AVAILABLE,    // đủ điều kiện, chưa bắt đầu
    IN_PROGRESS,  // đang tập
    MASTERED      // đã thành thạo
}

enum class Difficulty { BEGINNER, INTERMEDIATE, ADVANCED }

// ─── Core models ─────────────────────────────────────────
data class Exercise(
    val id: String,
    val name: String,
    val sets: String,           // "4×8"
    val isDone: Boolean = false
)

data class SkillNode(
    val id: String,
    val name: String,
    val muscleGroup: MuscleGroup,
    val state: NodeState,
    val difficulty: Difficulty,
    val currentXp: Int = 0,
    val targetXp: Int = 1000,
    val streakDays: Int = 0,
    val totalSessions: Int = 0,
    val requiredNodeId: String? = null,   // node phải master trước
    val exercises: List<Exercise> = emptyList(),
    val tip: String = ""
) {
    val level: Int get() = when {
        currentXp >= targetXp            -> 5
        currentXp > targetXp * 0.7f     -> 4
        currentXp > targetXp * 0.4f     -> 3
        currentXp > targetXp * 0.15f    -> 2
        else                             -> 1
    }
    val progressFraction: Float get() =
        (currentXp.toFloat() / targetXp).coerceIn(0f, 1f)
}

data class MuscleGroupTree(
    val group: MuscleGroup,
    val nodes: List<SkillNode>   // theo thứ tự từ dễ → khó
)

// ─── UiState cho ViewModel ───────────────────────────────
data class SkillTreeUiState(
    val trees: List<MuscleGroupTree> = emptyList(),
    val selectedGroup: MuscleGroup = MuscleGroup.CHEST,
    val selectedNode: SkillNode? = null,
    val isLoading: Boolean = false,
    val justLeveledUp: Boolean = false,
    val justUnlocked: String? = null   // id của node vừa unlock
)

// ─── Events từ UI lên ViewModel ──────────────────────────
sealed class SkillTreeEvent {
    data class SelectNode(val node: SkillNode) : SkillTreeEvent()
    object DismissNode : SkillTreeEvent()
    data class SelectGroup(val group: MuscleGroup) : SkillTreeEvent()
    data class AddXp(val nodeId: String, val amount: Int) : SkillTreeEvent()
    data class LogSession(val nodeId: String) : SkillTreeEvent()
}