package com.finance.app.domain.usecase

import com.finance.app.domain.model.Goal
import com.finance.app.domain.repository.GoalRepository
import javax.inject.Inject

class UpsertGoalUseCase @Inject constructor(
    private val repository: GoalRepository
) {
    suspend operator fun invoke(goal: Goal): Result<Unit> {
        return try {
            require(goal.name.isNotBlank()) { "Goal name cannot be empty" }
            require(goal.targetAmount > 0) { "Target amount must be greater than zero" }
            require(goal.currentAmount >= 0) { "Current amount cannot be negative" }
            require(goal.currentAmount <= goal.targetAmount) {
                "Current amount cannot exceed target amount"
            }
            if (goal.id == 0L) repository.upsertGoal(goal)
            else repository.updateGoal(goal)
            Result.success(Unit)
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to save goal. Please try again."))
        }
    }

    suspend fun delete(id: Long): Result<Unit> {
        return try {
            repository.deleteGoal(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to delete goal."))
        }
    }
}