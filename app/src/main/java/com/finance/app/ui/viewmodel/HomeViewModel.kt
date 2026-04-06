package com.finance.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finance.app.domain.model.BalanceSummary
import com.finance.app.domain.model.Goal
import com.finance.app.domain.model.Transaction
import com.finance.app.domain.usecase.GetBalanceSummaryUseCase
import com.finance.app.domain.usecase.GetGoalUseCase
import com.finance.app.domain.usecase.GetTransactionsUseCase
import com.finance.app.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.util.Calendar
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val balanceSummary: BalanceSummary = BalanceSummary(0.0, 0.0, 0.0),
    val recentTransactions: List<Transaction> = emptyList(),
    val activeGoal: Goal? = null,
    val monthlyChangePercent: Double = 0.0,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getBalanceSummaryUseCase: GetBalanceSummaryUseCase,
    private val getTransactionsUseCase: GetTransactionsUseCase,
    private val getGoalUseCase: GetGoalUseCase
) : ViewModel() {

    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<HomeUiState> = combine(
        getBalanceSummaryUseCase(),
        getTransactionsUseCase.recent(limit = 5),
        getGoalUseCase.all()
    ) { summary, recent, allGoals ->
        // Pick priority goal: earliest deadline first, then highest progress
        val priorityGoal = allGoals
            .filter { !it.isCompleted }
            .sortedWith(
                compareBy<Goal> { it.deadline ?: Long.MAX_VALUE }
                    .thenByDescending { it.progressPercent }
            )
            .firstOrNull() ?: allGoals.firstOrNull()

        HomeUiState(
            isLoading = false,
            balanceSummary = summary,
            recentTransactions = recent,
            activeGoal = priorityGoal,
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
        return if (summary.totalIncome > 0)
            ((summary.totalBalance / summary.totalIncome) * 100).coerceIn(-99.0, 999.0)
        else 0.0
    }
}