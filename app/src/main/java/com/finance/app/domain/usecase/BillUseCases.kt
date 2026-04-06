package com.finance.app.domain.usecase

import com.finance.app.domain.model.Bill
import com.finance.app.domain.repository.BillRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBillsUseCase @Inject constructor(
    private val repository: BillRepository
) {
    operator fun invoke(): Flow<List<Bill>> {
        return repository.getAllBills()
    }
}

class UpsertBillUseCase @Inject constructor(
    private val repository: BillRepository
) {
    suspend operator fun invoke(bill: Bill): Result<Long> {
        return try {
            val id = repository.upsertBill(bill)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun delete(id: Int): Result<Unit> {
        return try {
            repository.deleteBill(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
