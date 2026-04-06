package com.finance.app.domain.usecase

import com.finance.app.domain.model.BalanceSummary
import com.finance.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetBalanceSummaryUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(): Flow<BalanceSummary> =
        combine(
            repository.getTotalIncome(),
            repository.getTotalExpense()
        ) { income, expense ->
            BalanceSummary(
                totalBalance = income - expense,
                totalIncome = income,
                totalExpense = expense
            )
        }
}