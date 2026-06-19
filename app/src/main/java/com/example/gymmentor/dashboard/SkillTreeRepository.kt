package com.example.gymmentor.dashboard


class SkillTreeRepository {

    fun getSkillTrees(): List<MuscleGroupTree> = listOf(
        buildChestTree(),
        buildBackTree(),
        buildLegsTree(),
        buildArmsTree()
    )

    // Gọi khi user tap "Train Now" — cộng XP và tính lại state
    fun addXpToNode(
        trees: List<MuscleGroupTree>,
        nodeId: String,
        xpAmount: Int
    ): List<MuscleGroupTree> {
        val updated = trees.map { tree ->
            tree.copy(nodes = tree.nodes.map { node ->
                if (node.id == nodeId) {
                    val newXp = (node.currentXp + xpAmount).coerceAtMost(node.targetXp)
                    val newState = when {
                        newXp >= node.targetXp -> NodeState.MASTERED
                        newXp > 0              -> NodeState.IN_PROGRESS
                        else                   -> node.state
                    }
                    node.copy(currentXp = newXp, state = newState, totalSessions = node.totalSessions + 1)
                } else node
            })
        }
        // Sau khi update, tính lại unlock
        return recalculateUnlocks(updated)
    }

    // Node AVAILABLE nếu requiredNodeId == null hoặc node đó đã MASTERED
    private fun recalculateUnlocks(trees: List<MuscleGroupTree>): List<MuscleGroupTree> {
        val masteredIds = trees.flatMap { it.nodes }
            .filter { it.state == NodeState.MASTERED }
            .map { it.id }
            .toSet()

        return trees.map { tree ->
            tree.copy(nodes = tree.nodes.map { node ->
                if (node.state == NodeState.LOCKED) {
                    val canUnlock = node.requiredNodeId == null ||
                            node.requiredNodeId in masteredIds
                    if (canUnlock) node.copy(state = NodeState.AVAILABLE) else node
                } else node
            })
        }
    }

    // ─── Mock data ───────────────────────────────────────

    private fun buildChestTree() = MuscleGroupTree(
        group = MuscleGroup.CHEST,
        nodes = listOf(
            SkillNode(
                id = "push_up",
                name = "Push-up",
                muscleGroup = MuscleGroup.CHEST,
                state = NodeState.MASTERED,
                difficulty = Difficulty.BEGINNER,
                currentXp = 1000, targetXp = 1000,
                totalSessions = 18,
                tip = "Giữ thân thẳng, hạ ngực chạm sàn.",
                exercises = listOf(
                    Exercise("pu1", "Standard push-up", "4×15", true),
                    Exercise("pu2", "Wide-grip push-up", "3×12", true),
                    Exercise("pu3", "Diamond push-up", "3×10", true)
                )
            ),
            SkillNode(
                id = "dumbbell_fly",
                name = "Dumbbell Fly",
                muscleGroup = MuscleGroup.CHEST,
                state = NodeState.MASTERED,
                difficulty = Difficulty.BEGINNER,
                currentXp = 1000, targetXp = 1000,
                totalSessions = 12,
                requiredNodeId = "push_up",
                tip = "Co cơ ngực ở điểm đỉnh, đừng khóa khuỷu.",
                exercises = listOf(
                    Exercise("df1", "Flat dumbbell fly", "3×12", true),
                    Exercise("df2", "Incline fly 30°", "3×10", true)
                )
            ),
            SkillNode(
                id = "bench_press",
                name = "Bench Press",
                muscleGroup = MuscleGroup.CHEST,
                state = NodeState.IN_PROGRESS,
                difficulty = Difficulty.INTERMEDIATE,
                currentXp = 450, targetXp = 1000,
                streakDays = 4, totalSessions = 7,
                requiredNodeId = "push_up",
                tip = "Vai kẹp ra sau, hạ tạ chậm 3 giây.",
                exercises = listOf(
                    Exercise("bp1", "Warm-up 50% 1RM", "2×15", true),
                    Exercise("bp2", "Working sets", "4×8", true),
                    Exercise("bp3", "Tempo bench 3-1-1", "3×6", false),
                    Exercise("bp4", "Paused bench", "3×5", false)
                )
            ),
            SkillNode(
                id = "cable_fly",
                name = "Cable Fly",
                muscleGroup = MuscleGroup.CHEST,
                state = NodeState.LOCKED,
                difficulty = Difficulty.INTERMEDIATE,
                currentXp = 0, targetXp = 800,
                requiredNodeId = "bench_press",
                tip = "Căng cơ full range, không swing người.",
                exercises = listOf(
                    Exercise("cf1", "High cable fly", "3×15", false),
                    Exercise("cf2", "Low cable fly", "3×12", false),
                    Exercise("cf3", "Crossover", "3×10", false)
                )
            ),
            SkillNode(
                id = "incline_bench",
                name = "Incline Bench",
                muscleGroup = MuscleGroup.CHEST,
                state = NodeState.LOCKED,
                difficulty = Difficulty.ADVANCED,
                currentXp = 0, targetXp = 1200,
                requiredNodeId = "bench_press",
                tip = "Góc 30-45°, không quá dốc sẽ chuyển sang vai.",
                exercises = listOf(
                    Exercise("ib1", "Incline barbell press", "4×8", false),
                    Exercise("ib2", "Incline dumbbell press", "3×10", false),
                    Exercise("ib3", "Incline fly", "3×12", false)
                )
            )
        )
    )

    private fun buildBackTree() = MuscleGroupTree(
        group = MuscleGroup.BACK,
        nodes = listOf(
            SkillNode(
                id = "lat_pulldown",
                name = "Lat Pulldown",
                muscleGroup = MuscleGroup.BACK,
                state = NodeState.IN_PROGRESS,
                difficulty = Difficulty.BEGINNER,
                currentXp = 300, targetXp = 800,
                totalSessions = 6,
                tip = "Kéo xuống cằm, không nghiêng người ra sau.",
                exercises = listOf(
                    Exercise("lp1", "Wide-grip pulldown", "4×12", true),
                    Exercise("lp2", "Close-grip pulldown", "3×10", false)
                )
            ),
            SkillNode(
                id = "seated_row",
                name = "Seated Row",
                muscleGroup = MuscleGroup.BACK,
                state = NodeState.AVAILABLE,
                difficulty = Difficulty.BEGINNER,
                currentXp = 0, targetXp = 800,
                tip = "Kéo về rốn, giữ lưng thẳng.",
                exercises = listOf(
                    Exercise("sr1", "Cable seated row", "4×12", false),
                    Exercise("sr2", "Dumbbell row", "3×10", false)
                )
            ),
            SkillNode(
                id = "pull_up",
                name = "Pull-Up",
                muscleGroup = MuscleGroup.BACK,
                state = NodeState.LOCKED,
                difficulty = Difficulty.INTERMEDIATE,
                currentXp = 0, targetXp = 1000,
                requiredNodeId = "lat_pulldown",
                tip = "Dead hang đầy đủ, kéo ngực lên bar.",
                exercises = listOf(
                    Exercise("pu1", "Assisted pull-up", "3×8", false),
                    Exercise("pu2", "Bodyweight pull-up", "4×5", false),
                    Exercise("pu3", "Weighted pull-up", "3×5", false)
                )
            ),
            SkillNode(
                id = "deadlift",
                name = "Deadlift",
                muscleGroup = MuscleGroup.BACK,
                state = NodeState.LOCKED,
                difficulty = Difficulty.ADVANCED,
                currentXp = 0, targetXp = 1500,
                requiredNodeId = "pull_up",
                tip = "Lưng thẳng tuyệt đối, đẩy đất xuống, không kéo tạ lên.",
                exercises = listOf(
                    Exercise("dl1", "Romanian deadlift", "4×8", false),
                    Exercise("dl2", "Conventional deadlift", "4×5", false),
                    Exercise("dl3", "Sumo deadlift", "3×5", false)
                )
            )
        )
    )

    private fun buildLegsTree() = MuscleGroupTree(
        group = MuscleGroup.LEGS,
        nodes = listOf(
            SkillNode(
                id = "goblet_squat",
                name = "Goblet Squat",
                muscleGroup = MuscleGroup.LEGS,
                state = NodeState.MASTERED,
                difficulty = Difficulty.BEGINNER,
                currentXp = 1000, targetXp = 1000,
                totalSessions = 15,
                tip = "Giữ dumbbell sát ngực, gối không vượt mũi chân.",
                exercises = listOf(
                    Exercise("gs1", "Goblet squat", "4×15", true),
                    Exercise("gs2", "Pause goblet squat", "3×10", true)
                )
            ),
            SkillNode(
                id = "leg_press",
                name = "Leg Press",
                muscleGroup = MuscleGroup.LEGS,
                state = NodeState.IN_PROGRESS,
                difficulty = Difficulty.BEGINNER,
                currentXp = 600, targetXp = 800,
                totalSessions = 10,
                requiredNodeId = "goblet_squat",
                tip = "Không khóa gối ở điểm đỉnh.",
                exercises = listOf(
                    Exercise("lp1", "Leg press 45°", "4×12", true),
                    Exercise("lp2", "Single leg press", "3×10", false)
                )
            ),
            SkillNode(
                id = "barbell_squat",
                name = "Barbell Squat",
                muscleGroup = MuscleGroup.LEGS,
                state = NodeState.LOCKED,
                difficulty = Difficulty.INTERMEDIATE,
                currentXp = 0, targetXp = 1200,
                requiredNodeId = "goblet_squat",
                tip = "High bar vs low bar — thử cả hai để biết cái nào phù hợp hông của bạn.",
                exercises = listOf(
                    Exercise("bs1", "High bar squat", "4×8", false),
                    Exercise("bs2", "Pause squat", "3×5", false),
                    Exercise("bs3", "Box squat", "3×8", false)
                )
            ),
            SkillNode(
                id = "romanian_dl",
                name = "Romanian DL",
                muscleGroup = MuscleGroup.LEGS,
                state = NodeState.LOCKED,
                difficulty = Difficulty.ADVANCED,
                currentXp = 0, targetXp = 1200,
                requiredNodeId = "barbell_squat",
                tip = "Đẩy hông ra sau, lưng thẳng, cảm hamstring kéo căng.",
                exercises = listOf(
                    Exercise("rd1", "Dumbbell RDL", "4×10", false),
                    Exercise("rd2", "Barbell RDL", "4×8", false)
                )
            )
        )
    )

    private fun buildArmsTree() = MuscleGroupTree(
        group = MuscleGroup.ARMS,
        nodes = listOf(
            SkillNode(
                id = "curl",
                name = "Dumbbell Curl",
                muscleGroup = MuscleGroup.ARMS,
                state = NodeState.IN_PROGRESS,
                difficulty = Difficulty.BEGINNER,
                currentXp = 200, targetXp = 800,
                totalSessions = 5,
                tip = "Không swing khuỷu, xoay cổ tay ở đỉnh.",
                exercises = listOf(
                    Exercise("c1", "Alternating curl", "4×12", true),
                    Exercise("c2", "Hammer curl", "3×12", false)
                )
            ),
            SkillNode(
                id = "tricep_pushdown",
                name = "Tricep Pushdown",
                muscleGroup = MuscleGroup.ARMS,
                state = NodeState.AVAILABLE,
                difficulty = Difficulty.BEGINNER,
                currentXp = 0, targetXp = 800,
                tip = "Khuỷu tay kẹp sát người, không mở ra.",
                exercises = listOf(
                    Exercise("tp1", "Rope pushdown", "4×15", false),
                    Exercise("tp2", "Bar pushdown", "3×12", false)
                )
            ),
            SkillNode(
                id = "barbell_curl",
                name = "Barbell Curl",
                muscleGroup = MuscleGroup.ARMS,
                state = NodeState.LOCKED,
                difficulty = Difficulty.INTERMEDIATE,
                currentXp = 0, targetXp = 1000,
                requiredNodeId = "curl",
                tip = "Grip rộng = bicep dài, grip hẹp = bicep ngắn.",
                exercises = listOf(
                    Exercise("bc1", "Barbell curl", "4×10", false),
                    Exercise("bc2", "EZ bar curl", "3×10", false),
                    Exercise("bc3", "21s technique", "3×1 set", false)
                )
            ),
            SkillNode(
                id = "overhead_press",
                name = "Overhead Press",
                muscleGroup = MuscleGroup.ARMS,
                state = NodeState.LOCKED,
                difficulty = Difficulty.ADVANCED,
                currentXp = 0, targetXp = 1200,
                requiredNodeId = "barbell_curl",
                tip = "Core siết chặt, không ưỡn lưng dưới.",
                exercises = listOf(
                    Exercise("op1", "Dumbbell OHP", "4×10", false),
                    Exercise("op2", "Barbell OHP", "4×8", false),
                    Exercise("op3", "Arnold press", "3×10", false)
                )
            )
        )
    )
}