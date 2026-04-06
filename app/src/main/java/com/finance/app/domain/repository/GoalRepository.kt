package com.finance.app.domain.repository

import com.finance.app.domain.model.Goal
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    fun getAllGoals(): Flow<List<Goal>>
    fun getActiveGoal(): Flow<Goal?>
    suspend fun getGoalById(id: Long): Goal?
    suspend fun upsertGoal(goal: Goal): Long
    suspend fun updateGoal(goal: Goal)
    suspend fun deleteGoal(id: Long)
}