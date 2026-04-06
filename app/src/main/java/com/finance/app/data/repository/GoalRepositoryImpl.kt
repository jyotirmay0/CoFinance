package com.finance.app.data.repository

import com.finance.app.data.local.dao.GoalDao
import com.finance.app.data.local.entity.GoalEntity
import com.finance.app.domain.model.Goal
import com.finance.app.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GoalRepositoryImpl @Inject constructor(
    private val dao: GoalDao
) : GoalRepository {

    override fun getAllGoals(): Flow<List<Goal>> =
        dao.getAllGoals().map { list -> list.map { it.toDomain() } }

    override fun getActiveGoal(): Flow<Goal?> =
        dao.getActiveGoal().map { it?.toDomain() }

    override suspend fun getGoalById(id: Long): Goal? =
        dao.getGoalById(id)?.toDomain()

    override suspend fun upsertGoal(goal: Goal): Long =
        dao.insertGoal(goal.toEntity())

    override suspend fun updateGoal(goal: Goal) =
        dao.updateGoal(goal.toEntity())

    override suspend fun deleteGoal(id: Long) =
        dao.deleteGoalById(id)

    // ── Mappers ──────────────────────────────────────────────────────────────

    private fun GoalEntity.toDomain() = Goal(
        id = id,
        name = name,
        targetAmount = targetAmount,
        currentAmount = currentAmount,
        deadline = deadline,
        createdAt = createdAt
    )

    private fun Goal.toEntity() = GoalEntity(
        id = id,
        name = name,
        targetAmount = targetAmount,
        currentAmount = currentAmount,
        deadline = deadline,
        createdAt = createdAt
    )
}