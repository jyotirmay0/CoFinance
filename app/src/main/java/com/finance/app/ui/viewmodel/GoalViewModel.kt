package com.finance.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finance.app.domain.model.Goal
import com.finance.app.domain.model.GoalType
import com.finance.app.domain.usecase.GetGoalUseCase
import com.finance.app.domain.usecase.UpsertGoalUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class GoalUiEvent {
    data object Idle : GoalUiEvent()
    data object SaveSuccess : GoalUiEvent()
    data object DeleteSuccess : GoalUiEvent()
    data class Error(val message: String) : GoalUiEvent()
}

sealed class GoalUiState {
    data object Loading : GoalUiState()
    data class HasGoals(val goals: List<Goal>) : GoalUiState()
    data object NoGoals : GoalUiState()
    data class Error(val message: String) : GoalUiState()
}

data class GoalFormState(
    val name: String = "",
    val targetAmount: String = "",
    val currentAmount: String = "",
    val type: GoalType = GoalType.OTHER,
    val deadline: Long? = null,
    val nameError: String? = null,
    val targetError: String? = null,
    val currentError: String? = null,
    val isSaving: Boolean = false,
    val event: GoalUiEvent = GoalUiEvent.Idle
)

@HiltViewModel
class GoalViewModel @Inject constructor(
    private val getGoalUseCase: GetGoalUseCase,
    private val upsertGoalUseCase: UpsertGoalUseCase
) : ViewModel() {


    val goalUiState: StateFlow<GoalUiState> = getGoalUseCase.all()
        .map { goals ->
            if (goals.isEmpty()) GoalUiState.NoGoals
            else GoalUiState.HasGoals(goals)
        }
        .catch { e ->
            emit(GoalUiState.Error(e.message ?: "Failed to load goals"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = GoalUiState.Loading
        )


    private val _formState = MutableStateFlow(GoalFormState())
    val formState: StateFlow<GoalFormState> = _formState.asStateFlow()


    fun prefillForm(goal: Goal) {
        _formState.update {
            it.copy(
                name = goal.name,
                targetAmount = goal.targetAmount.toString(),
                currentAmount = goal.currentAmount.toString(),
                type = goal.type,
                deadline = goal.deadline
            )
        }
    }

    fun onNameChange(value: String) {
        _formState.update { it.copy(name = value, nameError = null) }
    }

    fun onTargetAmountChange(value: String) {
        _formState.update { it.copy(targetAmount = value, targetError = null) }
    }

    fun onCurrentAmountChange(value: String) {
        _formState.update { it.copy(currentAmount = value, currentError = null) }
    }

    fun onTypeChange(value: GoalType) {
        _formState.update { it.copy(type = value) }
    }

    fun onDeadlineChange(value: Long?) {
        _formState.update { it.copy(deadline = value) }
    }

    fun saveGoal(existingId: Long = 0L) {
        if (!validateForm()) return
        val state = _formState.value

        val goal = Goal(
            id = existingId,
            name = state.name.trim(),
            targetAmount = state.targetAmount.toDouble(),
            currentAmount = state.currentAmount.toDoubleOrNull() ?: 0.0,
            type = state.type,
            deadline = state.deadline
        )

        viewModelScope.launch {
            _formState.update { it.copy(isSaving = true) }
            upsertGoalUseCase(goal).fold(
                onSuccess = {
                    _formState.update {
                        it.copy(isSaving = false, event = GoalUiEvent.SaveSuccess)
                    }
                },
                onFailure = { e ->
                    _formState.update {
                        it.copy(
                            isSaving = false,
                            event = GoalUiEvent.Error(
                                e.message ?: "Failed to save goal"
                            )
                        )
                    }
                }
            )
        }
    }

    fun deleteGoal(id: Long) {
        viewModelScope.launch {
            upsertGoalUseCase.delete(id).fold(
                onSuccess = {
                    _formState.update {
                        it.copy(event = GoalUiEvent.DeleteSuccess)
                    }
                },
                onFailure = { e ->
                    _formState.update {
                        it.copy(
                            event = GoalUiEvent.Error(
                                e.message ?: "Failed to delete goal"
                            )
                        )
                    }
                }
            )
        }
    }

    fun resetEvent() {
        _formState.update { it.copy(event = GoalUiEvent.Idle) }
    }

    fun resetForm() {
        _formState.update { GoalFormState() }
    }


    private fun validateForm(): Boolean {
        val state = _formState.value
        var isValid = true

        if (state.name.isBlank()) {
            _formState.update { it.copy(nameError = "Goal name cannot be empty") }
            isValid = false
        }

        val target = state.targetAmount.toDoubleOrNull()
        if (target == null || target <= 0) {
            _formState.update {
                it.copy(targetError = "Target must be greater than zero")
            }
            isValid = false
        }

        val current = state.currentAmount.toDoubleOrNull()
        if (current != null && target != null && current > target) {
            _formState.update {
                it.copy(currentError = "Current amount cannot exceed target")
            }
            isValid = false
        }

        return isValid
    }
}