package com.finance.app.data.repository

import com.finance.app.data.local.dao.TransactionDao
import com.finance.app.data.local.entity.TransactionEntity
import com.finance.app.domain.model.Category
import com.finance.app.domain.model.Transaction
import com.finance.app.domain.model.TransactionType
import com.finance.app.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val dao: TransactionDao
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<Transaction>> =
        dao.getAllTransactions().map { list -> list.map { it.toDomain() } }

    override fun getRecentTransactions(limit: Int): Flow<List<Transaction>> =
        dao.getRecentTransactions(limit).map { list -> list.map { it.toDomain() } }

    override fun getFilteredTransactions(
        type: String?,
        category: String?,
        startDate: Long?,
        endDate: Long?
    ): Flow<List<Transaction>> =
        dao.getFilteredTransactions(type, category, startDate, endDate)
            .map { list -> list.map { it.toDomain() } }

    override fun searchTransactions(query: String): Flow<List<Transaction>> =
        dao.searchTransactions(query).map { list -> list.map { it.toDomain() } }

    override fun getTotalIncome(): Flow<Double> = dao.getTotalIncome()

    override fun getTotalExpense(): Flow<Double> = dao.getTotalExpense()

    override fun getTotalExpenseBetween(startDate: Long, endDate: Long): Flow<Double> =
        dao.getTotalExpenseBetween(startDate, endDate)

    override fun getExpensesBetween(startDate: Long, endDate: Long): Flow<List<Transaction>> =
        dao.getExpensesBetween(startDate, endDate).map { list -> list.map { it.toDomain() } }

    override fun getTransactionsForMonth(
        startOfMonth: Long,
        endOfMonth: Long
    ): Flow<List<Transaction>> =
        dao.getTransactionsForMonth(startOfMonth, endOfMonth)
            .map { list -> list.map { it.toDomain() } }

    override suspend fun getTransactionById(id: Long): Transaction? =
        dao.getTransactionById(id)?.toDomain()

    override suspend fun insertTransaction(transaction: Transaction): Long =
        dao.insertTransaction(transaction.toEntity())

    override suspend fun updateTransaction(transaction: Transaction) =
        dao.updateTransaction(transaction.toEntity())

    override suspend fun deleteTransaction(transaction: Transaction) =
        dao.deleteTransaction(transaction.toEntity())

    // ── Mappers ──────────────────────────────────────────────────────────────

    private fun TransactionEntity.toDomain() = Transaction(
        id = id,
        amount = amount,
        type = TransactionType.valueOf(type),
        category = Category.valueOf(category),
        date = date,
        notes = notes,
        createdAt = createdAt
    )

    private fun Transaction.toEntity() = TransactionEntity(
        id = id,
        amount = amount,
        type = type.name,
        category = category.name,
        date = date,
        notes = notes,
        createdAt = createdAt
    )
}