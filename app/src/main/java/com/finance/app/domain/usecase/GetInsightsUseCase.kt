package com.finance.app.domain.usecase

import com.finance.app.domain.model.Category
import com.finance.app.domain.model.CategorySpend
import com.finance.app.domain.model.InsightData
import com.finance.app.domain.model.MonthlyTrend
import com.finance.app.domain.model.TransactionType
import com.finance.app.domain.repository.TransactionRepository
import com.finance.app.utils.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

class GetInsightsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(): Flow<InsightData> {
        val now = System.currentTimeMillis()
        val currentWeekStart = DateUtils.startOfWeek(now)
        val currentWeekEnd = DateUtils.endOfWeek(now)
        val previousWeekStart = DateUtils.startOfPreviousWeek(now)
        val previousWeekEnd = DateUtils.endOfPreviousWeek(now)
        val sixMonthsAgo = DateUtils.sixMonthsAgo(now)

        return combine(
            repository.getAllTransactions(),
            repository.getExpensesBetween(currentWeekStart, currentWeekEnd),
            repository.getExpensesBetween(previousWeekStart, previousWeekEnd),
            repository.getExpensesBetween(sixMonthsAgo, now)
        ) { all, currentWeekTx, previousWeekTx, last6MonthsTx ->

            // 1. Category breakdown (expenses only)
            val expenseTransactions = all.filter { it.type == TransactionType.EXPENSE }
            val totalExpense = expenseTransactions.sumOf { it.amount }

            val categoryBreakdown = expenseTransactions
                .groupBy { it.category }
                .map { (category, txList) ->
                    val sum = txList.sumOf { it.amount }
                    CategorySpend(
                        category = category,
                        amount = sum,
                        percentage = if (totalExpense > 0)
                            ((sum / totalExpense) * 100).toFloat() else 0f
                    )
                }
                .sortedByDescending { it.amount }

            // 2. Highest spending category
            val highestSpending = categoryBreakdown.firstOrNull()

            // 3. Weekly comparison
            val currentWeekTotal = currentWeekTx.sumOf { it.amount }
            val previousWeekTotal = previousWeekTx.sumOf { it.amount }
            val weeklyChangePercent = if (previousWeekTotal > 0)
                ((currentWeekTotal - previousWeekTotal) / previousWeekTotal) * 100
            else if (currentWeekTotal > 0) 100.0
            else 0.0

            // 4. Monthly trends (last 6 months)
            val monthFormatter = SimpleDateFormat("MMM", Locale.getDefault())
            val monthlyTrends = last6MonthsTx
                .groupBy { tx ->
                    monthFormatter.format(tx.date)
                }
                .map { (month, txList) ->
                    MonthlyTrend(
                        monthLabel = month,
                        totalIncome = txList
                            .filter { it.type == TransactionType.INCOME }
                            .sumOf { it.amount },
                        totalExpense = txList
                            .filter { it.type == TransactionType.EXPENSE }
                            .sumOf { it.amount }
                    )
                }
                .takeLast(6)

            InsightData(
                highestSpendingCategory = highestSpending,
                currentWeekExpense = currentWeekTotal,
                previousWeekExpense = previousWeekTotal,
                weeklyChangePercent = weeklyChangePercent,
                monthlyTrends = monthlyTrends,
                categoryBreakdown = categoryBreakdown
            )
        }
    }
}