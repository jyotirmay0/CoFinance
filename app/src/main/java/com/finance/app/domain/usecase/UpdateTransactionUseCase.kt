package com.finance.app.domain.usecase

import com.finance.app.domain.model.Transaction
import com.finance.app.domain.repository.TransactionRepository
import javax.inject.Inject

class UpdateTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transaction: Transaction): Result<Unit> {
        return try {
            require(transaction.amount > 0) { "Amount must be greater than zero" }
            require(transaction.id > 0) { "Invalid transaction ID" }
            repository.updateTransaction(transaction)
            Result.success(Unit)
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to update transaction. Please try again."))
        }
    }
}