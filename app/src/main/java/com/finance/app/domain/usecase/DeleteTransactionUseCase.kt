package com.finance.app.domain.usecase

import com.finance.app.domain.model.Transaction
import com.finance.app.domain.repository.TransactionRepository
import javax.inject.Inject

class DeleteTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transaction: Transaction): Result<Unit> {
        return try {
            repository.deleteTransaction(transaction)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to delete transaction. Please try again."))
        }
    }
}