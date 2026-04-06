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
import java.util.Calendar
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
        val twelveMonthsAgo = Calendar.getInstance().apply {
            add(Calendar.MONTH, -11)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis

        return combine(
            repository.getAllTransactions(),
            repository.getExpensesBetween(currentWeekStart, currentWeekEnd),
            repository.getExpensesBetween(previousWeekStart, previousWeekEnd),
            repository.getFilteredTransactions(startDate = twelveMonthsAgo, endDate = now)
        ) { all, currentWeekTx, previousWeekTx, last12MonthsTx ->

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

            // 4. Monthly trends (last 12 months, ordered correctly)
            val monthFormatter = SimpleDateFormat("MMM", Locale.getDefault())
            val calendar = Calendar.getInstance()
            
            val monthlyTrends = (0..11).map { monthsAgo ->
                val cal = (calendar.clone() as Calendar).apply {
                    add(Calendar.MONTH, -monthsAgo)
                }
                val label = monthFormatter.format(cal.time)
                val startOfMonth = DateUtils.startOfMonth(cal.timeInMillis)
                val endOfMonth = DateUtils.endOfMonth(cal.timeInMillis)
                
                val monthTx = last12MonthsTx.filter { it.date in startOfMonth..endOfMonth }
                
                MonthlyTrend(
                    monthLabel = label,
                    totalIncome = monthTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amount },
                    totalExpense = monthTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                )
            }.reversed() // From oldest to newest

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