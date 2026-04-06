package com.finance.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val amount: Double,
    val dueDate: Long,
    val isAutoPay: Boolean = false,
    val isPaid: Boolean = false
)
