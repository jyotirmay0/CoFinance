package com.finance.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val type: String,         // "INCOME" or "EXPENSE"
    val category: String,
    val date: Long,           // epoch millis
    val notes: String,
    val createdAt: Long = System.currentTimeMillis()
)