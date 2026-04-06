package com.finance.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finance.app.domain.model.Bill
import com.finance.app.domain.usecase.GetBillsUseCase
import com.finance.app.domain.usecase.UpsertBillUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BillsUiState(
    val isLoading: Boolean = true,
    val bills: List<Bill> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class BillsViewModel @Inject constructor(
    getBillsUseCase: GetBillsUseCase,
    private val upsertBillUseCase: UpsertBillUseCase
) : ViewModel() {

    val uiState: StateFlow<BillsUiState> = getBillsUseCase()
        .map { bills ->
            BillsUiState(isLoading = false, bills = bills, error = null)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BillsUiState(isLoading = true)
        )

    fun markPaid(bill: Bill) {
        viewModelScope.launch {
            upsertBillUseCase(bill.copy(isPaid = true))
        }
    }

    fun deleteBill(id: Int) {
        viewModelScope.launch {
            upsertBillUseCase.delete(id)
        }
    }

    fun addBill(bill: Bill) {
        viewModelScope.launch {
            upsertBillUseCase(bill)
        }
    }
}
