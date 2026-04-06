package com.finance.app.domain.model

data class Transaction(
    val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val category: Category,
    val date: Long,
    val notes: String,
    val createdAt: Long = System.currentTimeMillis()
)