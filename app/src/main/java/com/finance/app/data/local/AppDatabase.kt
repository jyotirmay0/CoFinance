package com.finance.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.finance.app.data.local.dao.GoalDao
import com.finance.app.data.local.dao.TransactionDao
import com.finance.app.data.local.entity.GoalEntity
import com.finance.app.data.local.entity.TransactionEntity

@Database(
    entities = [TransactionEntity::class, GoalEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun goalDao(): GoalDao

    companion object {
        const val DATABASE_NAME = "finance_companion_db"
    }
}