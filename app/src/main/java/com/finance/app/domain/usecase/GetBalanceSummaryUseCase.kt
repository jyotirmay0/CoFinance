package com.finance.app.domain.usecase

import com.finance.app.domain.model.BalanceSummary
import com.finance.app.domain.model.TransactionType
import com.finance.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import javax.inject.Inject

class GetBalanceSummaryUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(): Flow<BalanceSummary> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfMonth = cal.timeInMillis

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val endOfMonth = cal.timeInMillis

        return repository.getTransactionsForMonth(startOfMonth, endOfMonth)
            .map { monthTx ->
                val income = monthTx.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                val expense = monthTx.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                BalanceSummary(
                    totalBalance = income - expense,
                    totalIncome = income,
                    totalExpense = expense
                )
            }
    }
}