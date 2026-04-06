package com.finance.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finance.app.domain.model.InsightData
import com.finance.app.domain.usecase.GetInsightsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed class InsightsUiState {
    data object Loading : InsightsUiState()
    data class Success(val data: InsightData) : InsightsUiState()
    data class Error(val message: String) : InsightsUiState()
    data object Empty : InsightsUiState()
}

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val getInsightsUseCase: GetInsightsUseCase
) : ViewModel() {

    val uiState: StateFlow<InsightsUiState> = getInsightsUseCase()
        .map { data ->
            if (data.categoryBreakdown.isEmpty() && data.monthlyTrends.isEmpty()) {
                InsightsUiState.Empty
            } else {
                InsightsUiState.Success(data)
            }
        }
        .catch { e ->
            emit(InsightsUiState.Error(e.message ?: "Failed to load insights"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = InsightsUiState.Loading
        )
}