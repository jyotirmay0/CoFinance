package com.finance.app.domain.repository

import com.finance.app.domain.model.Bill
import kotlinx.coroutines.flow.Flow

interface BillRepository {
    fun getAllBills(): Flow<List<Bill>>
    suspend fun getBillById(id: Int): Bill?
    suspend fun upsertBill(bill: Bill): Long
    suspend fun deleteBill(id: Int)
}
