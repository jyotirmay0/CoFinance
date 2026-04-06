package com.finance.app.domain.repository

import com.finance.app.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<Transaction>>
    fun getRecentTransactions(limit: Int = 5): Flow<List<Transaction>>
    fun getFilteredTransactions(
        type: String? = null,
        category: String? = null,
        startDate: Long? = null,
        endDate: Long? = null
    ): Flow<List<Transaction>>
    fun searchTransactions(query: String): Flow<List<Transaction>>
    fun getTotalIncome(): Flow<Double>
    fun getTotalExpense(): Flow<Double>
    fun getTotalExpenseBetween(startDate: Long, endDate: Long): Flow<Double>
    fun getExpensesBetween(startDate: Long, endDate: Long): Flow<List<Transaction>>
    fun getTransactionsForMonth(startOfMonth: Long, endOfMonth: Long): Flow<List<Transaction>>
    suspend fun getTransactionById(id: Long): Transaction?
    suspend fun insertTransaction(transaction: Transaction): Long
    suspend fun updateTransaction(transaction: Transaction)
    suspend fun deleteTransaction(transaction: Transaction)
}