package com.finance.app.data.repository

import com.finance.app.data.local.dao.BillDao
import com.finance.app.data.local.entity.BillEntity
import com.finance.app.domain.model.Bill
import com.finance.app.domain.repository.BillRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BillRepositoryImpl @Inject constructor(
    private val billDao: BillDao
) : BillRepository {

    override fun getAllBills(): Flow<List<Bill>> {
        return billDao.getAllBills().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getBillById(id: Int): Bill? {
        return billDao.getBillById(id)?.toDomainModel()
    }

    override suspend fun upsertBill(bill: Bill): Long {
        return billDao.insertBill(bill.toEntity())
    }

    override suspend fun deleteBill(id: Int) {
        billDao.deleteBillById(id)
    }

    private fun BillEntity.toDomainModel(): Bill {
        return Bill(
            id = id,
            name = name,
            amount = amount,
            dueDate = dueDate,
            isAutoPay = isAutoPay,
            isPaid = isPaid
        )
    }

    private fun Bill.toEntity(): BillEntity {
        return BillEntity(
            id = id,
            name = name,
            amount = amount,
            dueDate = dueDate,
            isAutoPay = isAutoPay,
            isPaid = isPaid
        )
    }
}
