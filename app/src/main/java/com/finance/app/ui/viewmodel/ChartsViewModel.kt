package com.finance.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.finance.app.domain.model.CategorySpend
import com.finance.app.domain.model.Transaction
import com.finance.app.domain.model.TransactionType
import com.finance.app.domain.repository.TransactionRepository
import com.finance.app.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

enum class ChartPeriod {
    WEEK, MONTH, YEAR, CUSTOM
}

data class ChartsUiState(
    val isLoading: Boolean = true,
    val selectedPeriod: ChartPeriod = ChartPeriod.MONTH,
    val selectedDateLabel: String = "",
    val totalAmount: Double = 0.0,
    val categoryBreakdown: List<CategorySpend> = emptyList(),
    val availableDates: List<String> = emptyList(),
    val selectedDateIndex: Int = 0,
    val customStartDate: Long? = null,
    val customEndDate: Long? = null
)

@HiltViewModel
class ChartsViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(ChartPeriod.MONTH)
    val selectedPeriod = _selectedPeriod.asStateFlow()

    private val _dateOffset = MutableStateFlow(0) // 0 is current, -1 is previous, etc.
    val dateOffset = _dateOffset.asStateFlow()

    private val _customDateRange = MutableStateFlow<Pair<Long, Long>?>(null)
    val customDateRange = _customDateRange.asStateFlow()

    val uiState: StateFlow<ChartsUiState> = combine(
        _selectedPeriod,
        _dateOffset,
        _customDateRange,
        repository.getAllTransactions()
    ) { period, offset, customRange, allTransactions ->
        val calendar = Calendar.getInstance()
        val (start, end, label) = if (period == ChartPeriod.CUSTOM && customRange != null) {
            Triple(customRange.first, customRange.second, 
                "${DateUtils.formatDate(customRange.first)} - ${DateUtils.formatDate(customRange.second)}")
        } else {
            getPeriodRange(calendar, period, offset)
        }

        val filteredTx = allTransactions.filter { 
            it.type == TransactionType.EXPENSE && 
            it.date >= start && 
            it.date <= end 
        }

        val total = filteredTx.sumOf { it.amount }
        val breakdown = filteredTx.groupBy { it.category }
            .map { (category, list) ->
                val sum = list.sumOf { it.amount }
                CategorySpend(
                    category = category,
                    amount = sum,
                    percentage = if (total > 0) ((sum / total) * 100).toFloat() else 0f
                )
            }
            .sortedByDescending { it.amount }

        ChartsUiState(
            isLoading = false,
            selectedPeriod = period,
            selectedDateLabel = label,
            totalAmount = total,
            categoryBreakdown = breakdown,
            availableDates = if (period == ChartPeriod.CUSTOM) emptyList() else getAvailableDates(period),
            selectedDateIndex = 12 + offset,
            customStartDate = customRange?.first,
            customEndDate = customRange?.second
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ChartsUiState()
    )

    fun onPeriodSelected(period: ChartPeriod) {
        _selectedPeriod.value = period
        _dateOffset.value = 0
    }

    fun onDateOffsetChanged(offset: Int) {
        _dateOffset.value = offset
    }

    fun onCustomDateRangeSelected(start: Long, end: Long) {
        _customDateRange.value = Pair(start, end)
        _selectedPeriod.value = ChartPeriod.CUSTOM
    }

    private fun getPeriodRange(cal: Calendar, period: ChartPeriod, offset: Int): Triple<Long, Long, String> {
        val calendar = cal.clone() as Calendar
        return when (period) {
            ChartPeriod.WEEK -> {
                calendar.add(Calendar.WEEK_OF_YEAR, offset)
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis
                
                calendar.add(Calendar.DAY_OF_WEEK, 6)
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val end = calendar.timeInMillis
                val label = "Week of " + SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(start))
                Triple(start, end, label)
            }
            ChartPeriod.MONTH -> {
                calendar.add(Calendar.MONTH, offset)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                val start = calendar.timeInMillis
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                val end = calendar.timeInMillis
                val label = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date(start))
                Triple(start, end, label)
            }
            ChartPeriod.YEAR -> {
                calendar.add(Calendar.YEAR, offset)
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis
                
                calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR))
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val end = calendar.timeInMillis
                val label = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date(start))
                Triple(start, end, label)
            }
            ChartPeriod.CUSTOM -> Triple(0L, 0L, "") // Handled in combine block
        }
    }

    private fun getAvailableDates(period: ChartPeriod): List<String> {
        val calendar = Calendar.getInstance()
        val format = when (period) {
            ChartPeriod.WEEK -> "MMM d"
            ChartPeriod.MONTH -> "MMM yyyy"
            ChartPeriod.YEAR -> "yyyy"
            ChartPeriod.CUSTOM -> "MMM d, yyyy"
        }
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        
        return (-12..12).map { offset ->
            val cal = calendar.clone() as Calendar
            when (period) {
                ChartPeriod.WEEK -> cal.add(Calendar.WEEK_OF_YEAR, offset)
                ChartPeriod.MONTH -> cal.add(Calendar.MONTH, offset)
                ChartPeriod.YEAR -> cal.add(Calendar.YEAR, offset)
                ChartPeriod.CUSTOM -> { /* No-op for scroller */ }
            }
            sdf.format(cal.time)
        }
    }
}
