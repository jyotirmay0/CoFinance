package com.finance.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.finance.app.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): TransactionEntity?

    @Query(
        """
        SELECT * FROM transactions 
        WHERE (:type IS NULL OR type = :type)
        AND (:category IS NULL OR category = :category)
        AND (:startDate IS NULL OR date >= :startDate)
        AND (:endDate IS NULL OR date <= :endDate)
        ORDER BY date DESC
        """
    )
    fun getFilteredTransactions(
        type: String? = null,
        category: String? = null,
        startDate: Long? = null,
        endDate: Long? = null
    ): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT * FROM transactions
        WHERE (notes LIKE '%' || :query || '%'
        OR category LIKE '%' || :query || '%')
        ORDER BY date DESC
        """
    )
    fun searchTransactions(query: String): Flow<List<TransactionEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'INCOME'")
    fun getTotalIncome(): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'EXPENSE'")
    fun getTotalExpense(): Flow<Double>

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0) FROM transactions 
        WHERE type = 'EXPENSE' AND date >= :startDate AND date <= :endDate
        """
    )
    fun getTotalExpenseBetween(startDate: Long, endDate: Long): Flow<Double>

    @Query(
        """
        SELECT * FROM transactions 
        WHERE type = 'EXPENSE' AND date >= :startDate AND date <= :endDate
        ORDER BY date DESC
        """
    )
    fun getExpensesBetween(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY date DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int = 5): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT * FROM transactions 
        WHERE date >= :startOfMonth AND date <= :endOfMonth
        ORDER BY date DESC
        """
    )
    fun getTransactionsForMonth(startOfMonth: Long, endOfMonth: Long): Flow<List<TransactionEntity>>
}