package com.finance.app.domain.model

data class Goal(
    val id: Long = 0,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val type: GoalType = GoalType.OTHER,
    val deadline: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    val progressPercent: Float
        get() = if (targetAmount > 0)
            (currentAmount / targetAmount).coerceIn(0.0, 1.0).toFloat()
        else 0f

    val isCompleted: Boolean
        get() = currentAmount >= targetAmount

    val remainingAmount: Double
        get() = (targetAmount - currentAmount).coerceAtLeast(0.0)
}