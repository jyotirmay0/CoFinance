package com.finance.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finance.app.domain.model.Category
import com.finance.app.domain.model.Transaction
import com.finance.app.domain.model.TransactionType
import com.finance.app.domain.usecase.AddTransactionUseCase
import com.finance.app.domain.usecase.DeleteTransactionUseCase
import com.finance.app.domain.usecase.GetTransactionsUseCase
import com.finance.app.domain.usecase.SearchTransactionsUseCase
import com.finance.app.domain.usecase.UpdateTransactionUseCase
import com.finance.app.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionFilter(
    val type: TransactionType? = null,
    val category: Category? = null,
    val startDate: Long? = null,
    val endDate: Long? = null
)

sealed class TransactionUiEvent {
    data object Idle : TransactionUiEvent()
    data object SaveSuccess : TransactionUiEvent()
    data object DeleteSuccess : TransactionUiEvent()
    data class Error(val message: String) : TransactionUiEvent()
}

data class TransactionListUiState(
    val isLoading: Boolean = true,
    val transactions: List<Transaction> = emptyList(),
    val searchQuery: String = "",
    val filter: TransactionFilter = TransactionFilter(),
    val error: String? = null
)

data class AddEditUiState(
    val isLoading: Boolean = false,
    val transaction: Transaction? = null,
    val amount: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val category: Category = Category.FOOD,
    val date: Long = System.currentTimeMillis(),
    val notes: String = "",
    val amountError: String? = null,
    val categoryError: String? = null,
    val isSaving: Boolean = false,
    val event: TransactionUiEvent = TransactionUiEvent.Idle
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val searchTransactionsUseCase: SearchTransactionsUseCase,
    private val addTransactionUseCase: AddTransactionUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val repository: TransactionRepository
) : ViewModel() {


    private val _searchQuery = MutableStateFlow("")
    private val _filter = MutableStateFlow(TransactionFilter())

    val listUiState: StateFlow<TransactionListUiState> = combine(
        _searchQuery.debounce(300),
        _filter
    ) { query, filter -> Pair(query, filter) }
        .flatMapLatest { (query, filter) ->
            if (query.isNotBlank()) {
                searchTransactionsUseCase(query).map { transactions ->
                    TransactionListUiState(
                        isLoading = false,
                        transactions = transactions,
                        searchQuery = query,
                        filter = filter
                    )
                }
            } else {
                getTransactionsUseCase.filtered(
                    type = filter.type?.name,
                    category = filter.category?.name,
                    startDate = filter.startDate,
                    endDate = filter.endDate
                ).map { transactions ->
                    TransactionListUiState(
                        isLoading = false,
                        transactions = transactions,
                        searchQuery = query,
                        filter = filter
                    )
                }
            }
        }
        .catch { e ->
            emit(
                TransactionListUiState(
                    isLoading = false,
                    error = e.message ?: "Failed to load transactions"
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TransactionListUiState(isLoading = true)
        )


    private val _addEditState = MutableStateFlow(AddEditUiState())
    val addEditState: StateFlow<AddEditUiState> = _addEditState.asStateFlow()


    fun onSearchQueryChange(query: String) {
        _searchQuery.update { query }
    }

    fun onFilterChange(filter: TransactionFilter) {
        _filter.update { filter }
    }

    fun clearFilters() {
        _searchQuery.update { "" }
        _filter.update { TransactionFilter() }
    }


    fun loadTransaction(id: Long) {
        viewModelScope.launch {
            _addEditState.update { it.copy(isLoading = true) }
            try {
                val transaction = repository.getTransactionById(id)
                if (transaction != null) {
                    _addEditState.update {
                        it.copy(
                            isLoading = false,
                            transaction = transaction,
                            amount = transaction.amount.toString(),
                            type = transaction.type,
                            category = transaction.category,
                            date = transaction.date,
                            notes = transaction.notes
                        )
                    }
                } else {
                    _addEditState.update {
                        it.copy(
                            isLoading = false,
                            event = TransactionUiEvent.Error("Transaction not found")
                        )
                    }
                }
            } catch (e: Exception) {
                _addEditState.update {
                    it.copy(
                        isLoading = false,
                        event = TransactionUiEvent.Error(
                            e.message ?: "Failed to load transaction"
                        )
                    )
                }
            }
        }
    }

    fun onAmountChange(value: String) {
        _addEditState.update {
            it.copy(amount = value, amountError = null)
        }
    }

    fun onTypeChange(type: TransactionType) {
        val defaultCategory = if (type == TransactionType.INCOME)
            Category.SALARY else Category.FOOD
        _addEditState.update {
            it.copy(type = type, category = defaultCategory)
        }
    }

    fun onCategoryChange(category: Category) {
        _addEditState.update {
            it.copy(category = category, categoryError = null)
        }
    }

    fun onDateChange(date: Long) {
        _addEditState.update { it.copy(date = date) }
    }

    fun onNotesChange(notes: String) {
        _addEditState.update { it.copy(notes = notes) }
    }

    fun saveTransaction() {
        val state = _addEditState.value
        if (!validateForm(state)) return

        val amount = state.amount.toDoubleOrNull() ?: return
        val transaction = Transaction(
            id = state.transaction?.id ?: 0L,
            amount = amount,
            type = state.type,
            category = state.category,
            date = state.date,
            notes = state.notes.trim()
        )

        viewModelScope.launch {
            _addEditState.update { it.copy(isSaving = true) }
            val result = if (state.transaction == null) {
                addTransactionUseCase(transaction)
            } else {
                updateTransactionUseCase(transaction)
            }
            result.fold(
                onSuccess = {
                    _addEditState.update {
                        it.copy(isSaving = false, event = TransactionUiEvent.SaveSuccess)
                    }
                },
                onFailure = { e ->
                    _addEditState.update {
                        it.copy(
                            isSaving = false,
                            event = TransactionUiEvent.Error(
                                e.message ?: "Failed to save transaction"
                            )
                        )
                    }
                }
            )
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            val result = deleteTransactionUseCase(transaction)
            result.fold(
                onSuccess = {
                    _addEditState.update {
                        it.copy(event = TransactionUiEvent.DeleteSuccess)
                    }
                },
                onFailure = { e ->
                    _addEditState.update {
                        it.copy(
                            event = TransactionUiEvent.Error(
                                e.message ?: "Failed to delete transaction"
                            )
                        )
                    }
                }
            )
        }
    }

    fun resetEvent() {
        _addEditState.update { it.copy(event = TransactionUiEvent.Idle) }
    }

    fun resetAddEditState() {
        _addEditState.update { AddEditUiState() }
    }


    private fun validateForm(state: AddEditUiState): Boolean {
        var isValid = true
        val amount = state.amount.toDoubleOrNull()

        if (state.amount.isBlank()) {
            _addEditState.update { it.copy(amountError = "Please enter an amount") }
            isValid = false
        } else if (amount == null || amount <= 0) {
            _addEditState.update {
                it.copy(amountError = "Amount must be greater than zero")
            }
            isValid = false
        }

        return isValid
    }
}