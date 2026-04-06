package com.finance.app.domain.model

data class InsightData(
    val highestSpendingCategory: CategorySpend?,
    val currentWeekExpense: Double,
    val previousWeekExpense: Double,
    val weeklyChangePercent: Double,
    val monthlyTrends: List<MonthlyTrend>,
    val categoryBreakdown: List<CategorySpend>
)

data class CategorySpend(
    val category: Category,
    val amount: Double,
    val percentage: Float
)

data class MonthlyTrend(
    val monthLabel: String,   // e.g. "Jan", "Feb"
    val totalIncome: Double,
    val totalExpense: Double
)

data class BalanceSummary(
    val totalBalance: Double,
    val totalIncome: Double,
    val totalExpense: Double
)

data class WeeklyBarData(
    val dayLabel: String,
    val amount: Float
)