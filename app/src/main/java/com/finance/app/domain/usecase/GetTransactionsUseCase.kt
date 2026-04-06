package com.finance.app.domain.usecase

import com.finance.app.domain.model.Transaction
import com.finance.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(): Flow<List<Transaction>> =
        repository.getAllTransactions()

    fun recent(limit: Int = 5): Flow<List<Transaction>> =
        repository.getRecentTransactions(limit)

    fun filtered(
        type: String? = null,
        category: String? = null,
        startDate: Long? = null,
        endDate: Long? = null
    ): Flow<List<Transaction>> =
        repository.getFilteredTransactions(type, category, startDate, endDate)
}