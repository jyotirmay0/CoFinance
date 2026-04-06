package com.finance.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finance.app.domain.model.BalanceSummary
import com.finance.app.domain.model.Bill
import com.finance.app.domain.model.Goal
import com.finance.app.domain.model.Transaction
import com.finance.app.domain.usecase.GetBalanceSummaryUseCase
import com.finance.app.domain.usecase.GetBillsUseCase
import com.finance.app.domain.usecase.GetGoalUseCase
import com.finance.app.domain.usecase.GetTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val balanceSummary: BalanceSummary = BalanceSummary(0.0, 0.0, 0.0),
    val recentTransactions: List<Transaction> = emptyList(),
    val recentBills: List<Bill> = emptyList(),
    val activeGoal: Goal? = null,
    val monthlyChangePercent: Double = 0.0,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getBalanceSummaryUseCase: GetBalanceSummaryUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getGoalUseCase: GetGoalUseCase,
    private val getBillsUseCase: GetBillsUseCase
) : ViewModel() {

    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<HomeUiState> = combine(
        getBalanceSummaryUseCase(),
        getTransactionsUseCase.recent(limit = 5),
        getGoalUseCase(),
        getBillsUseCase()
    ) { summary, recent, goal, bills ->
        HomeUiState(
            isLoading = false,
            balanceSummary = summary,
            recentTransactions = recent,
            recentBills = bills,
            activeGoal = goal,
            monthlyChangePercent = calculateMonthlyChange(summary),
            error = null
        )
    }
        .catch { e ->
            _error.update { e.message ?: "An unexpected error occurred" }
            emit(HomeUiState(isLoading = false, error = e.message))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(isLoading = true)
        )

    private fun calculateMonthlyChange(summary: BalanceSummary): Double {
        // Placeholder — in a full app this would compare current vs previous month
        return if (summary.totalIncome > 0)
            ((summary.totalBalance / summary.totalIncome) * 100).coerceIn(-99.0, 999.0)
        else 0.0
    }
}