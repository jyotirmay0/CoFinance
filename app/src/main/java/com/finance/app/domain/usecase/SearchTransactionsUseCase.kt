package com.finance.app.domain.usecase

import com.finance.app.domain.model.Transaction
import com.finance.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class SearchTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(query: String): Flow<List<Transaction>> {
        if (query.isBlank()) return repository.getAllTransactions()
        return repository.searchTransactions(query.trim())
    }
}