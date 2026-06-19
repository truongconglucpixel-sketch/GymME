package com.example.gymmentor.dashboard

// ui/skilltree/SkillTreeViewModel.kt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SkillTreeViewModel : ViewModel() {

    private val repository = SkillTreeRepository()

    private val _uiState = MutableStateFlow(SkillTreeUiState(isLoading = true))
    val uiState: StateFlow<SkillTreeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val trees = repository.getSkillTrees()
            _uiState.update {
                it.copy(trees = trees, isLoading = false)
            }
        }
    }

    fun onEvent(event: SkillTreeEvent) {
        when (event) {
            is SkillTreeEvent.SelectNode -> {
                _uiState.update { it.copy(selectedNode = event.node) }
            }
            is SkillTreeEvent.DismissNode -> {
                _uiState.update { it.copy(selectedNode = null, justLeveledUp = false) }
            }
            is SkillTreeEvent.SelectGroup -> {
                _uiState.update {
                    it.copy(selectedGroup = event.group, selectedNode = null)
                }
            }
            is SkillTreeEvent.AddXp -> {
                val state = _uiState.value
                val oldNode = state.trees
                    .flatMap { it.nodes }
                    .find { it.id == event.nodeId }
                val oldLevel = oldNode?.level ?: 0

                val newTrees = repository.addXpToNode(
                    state.trees, event.nodeId, event.amount
                )
                val newNode = newTrees
                    .flatMap { it.nodes }
                    .find { it.id == event.nodeId }
                val newLevel = newNode?.level ?: 0

                // Tìm node vừa được unlock
                val oldIds = state.trees.flatMap { it.nodes }
                    .filter { it.state == NodeState.LOCKED }.map { it.id }.toSet()
                val newIds = newTrees.flatMap { it.nodes }
                    .filter { it.state == NodeState.LOCKED }.map { it.id }.toSet()
                val justUnlocked = (oldIds - newIds).firstOrNull()

                _uiState.update {
                    it.copy(
                        trees = newTrees,
                        selectedNode = newNode,
                        justLeveledUp = newLevel > oldLevel,
                        justUnlocked = justUnlocked
                    )
                }
            }
            is SkillTreeEvent.LogSession -> {
                onEvent(SkillTreeEvent.AddXp(nodeId = event.nodeId, amount = 150))
            }
        }
    }

    fun getCurrentTree(): MuscleGroupTree? {
        return _uiState.value.trees.find {
            it.group == _uiState.value.selectedGroup
        }
    }
}