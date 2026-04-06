package com.finance.app.domain.usecase

import com.finance.app.domain.model.Transaction
import com.finance.app.domain.repository.TransactionRepository
import javax.inject.Inject

class AddTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(transaction: Transaction): Result<Long> {
        return try {
            require(transaction.amount > 0) { "Amount must be greater than zero" }
            require(transaction.notes.length <= 200) { "Notes must be under 200 characters" }
            val id = repository.insertTransaction(transaction)
            Result.success(id)
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to save transaction. Please try again."))
        }
    }
}