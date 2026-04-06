package com.finance.app.domain.usecase

import com.finance.app.domain.model.Goal
import com.finance.app.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetGoalUseCase @Inject constructor(
    private val repository: GoalRepository
) {
    operator fun invoke(): Flow<Goal?> = repository.getActiveGoal()

    fun all(): Flow<List<Goal>> = repository.getAllGoals()
}