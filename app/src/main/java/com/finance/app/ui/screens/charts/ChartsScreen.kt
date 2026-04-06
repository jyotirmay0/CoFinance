package com.finance.app.ui.screens.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finance.app.domain.model.CategorySpend
import com.finance.app.ui.components.EmptyState
import com.finance.app.ui.components.FullScreenLoading
import com.finance.app.ui.theme.FinanceTheme
import com.finance.app.ui.theme.toColor
import com.finance.app.ui.viewmodel.ChartPeriod
import com.finance.app.ui.viewmodel.ChartsUiState
import com.finance.app.ui.viewmodel.ChartsViewModel
import com.finance.app.utils.CurrencyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartsScreen(
    viewModel: ChartsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }
    val dateRangePickerState = rememberDateRangePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            modifier = Modifier.fillMaxWidth(0.8f),
            confirmButton = {
                TextButton(
                    onClick = {
                        val start = dateRangePickerState.selectedStartDateMillis
                        val end = dateRangePickerState.selectedEndDateMillis
                        if (start != null && end != null) {
                            viewModel.onCustomDateRangeSelected(start, end)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Select")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                modifier = Modifier.heightIn(max = 480.dp),
                title = { Text("Select Date Range", modifier = Modifier.padding(16.dp)) },
                showModeToggle = false
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ChartsTopBar(
            onCalendarClick = { showDatePicker = true },
            selectedLabel = uiState.selectedDateLabel,
            isCustom = uiState.selectedPeriod == ChartPeriod.CUSTOM
        )
        
        PeriodSelector(
            selectedPeriod = uiState.selectedPeriod,
            onPeriodSelected = viewModel::onPeriodSelected
        )
        
        if (uiState.selectedPeriod != ChartPeriod.CUSTOM) {
            DateScroller(
                dates = uiState.availableDates,
                selectedDateIndex = uiState.selectedDateIndex,
                onDateSelected = { index -> 
                    viewModel.onDateOffsetChanged(index - 12) 
                }
            )
        } else {
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (uiState.isLoading) {
            FullScreenLoading()
        } else if (uiState.categoryBreakdown.isEmpty()) {
            EmptyState(
                icon = Icons.Default.CalendarMonth,
                title = "No data for this period",
                subtitle = "Try selecting a different date or period."
            )
        } else {
            ChartsContent(uiState = uiState)
        }
    }
}

@Composable
private fun ChartsTopBar(
    onCalendarClick: () -> Unit,
    selectedLabel: String,
    isCustom: Boolean
) {
    val financeColors = FinanceTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Expenses",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            if (isCustom) {
                Text(
                    text = selectedLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = financeColors.textSecondary
                )
            }
        }
        
        IconButton(onClick = onCalendarClick) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = "Calendar",
                tint = if (isCustom) MaterialTheme.colorScheme.primary else financeColors.textSecondary
            )
        }
    }
}

@Composable
private fun PeriodSelector(
    selectedPeriod: ChartPeriod,
    onPeriodSelected: (ChartPeriod) -> Unit
) {
    val financeColors = FinanceTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(financeColors.cardBackground)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ChartPeriod.entries.forEach { period ->
            val selected = period == selectedPeriod
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (selected) Color.White else Color.Transparent)
                    .clickable { onPeriodSelected(period) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when(period) {
                        ChartPeriod.WEEK -> "Week"
                        ChartPeriod.MONTH -> "Month"
                        ChartPeriod.YEAR -> "Year"
                        ChartPeriod.CUSTOM -> "Custom"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) Color.Black else financeColors.textSecondary,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun DateScroller(
    dates: List<String>,
    selectedDateIndex: Int,
    onDateSelected: (Int) -> Unit
) {
    val listState = rememberLazyListState()
    
    LaunchedEffect(selectedDateIndex) {
        listState.animateScrollToItem(maxOf(0, selectedDateIndex - 1))
    }

    LazyRow(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        itemsIndexed(dates) { index, date ->
            val selected = index == selectedDateIndex
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onDateSelected(index) }
            ) {
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selected) Color.White else FinanceTheme.colors.textSecondary,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                )
                if (selected) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(2.dp)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChartsContent(uiState: ChartsUiState) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DonutChart(
                    data = uiState.categoryBreakdown,
                    totalAmount = uiState.totalAmount,
                    modifier = Modifier.size(160.dp)
                )
                
                DonutLegend(data = uiState.categoryBreakdown.take(5))
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(3) { i ->
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (i == 0) Color.White else Color.White.copy(alpha = 0.3f))
                        )
                    }
                }
            }
        }

        items(uiState.categoryBreakdown) { spend ->
            CategoryBreakdownRow(spend = spend)
        }
        
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

@Composable
private fun DonutChart(
    data: List<CategorySpend>,
    totalAmount: Double,
    modifier: Modifier = Modifier
) {
    val animations = data.map { remember { Animatable(0f) } }
    
    LaunchedEffect(data) {
        animations.forEachIndexed { index, anim ->
            anim.animateTo(
                targetValue = data[index].percentage,
                animationSpec = tween(durationMillis = 800, delayMillis = index * 50)
            )
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            var startAngle = -90f
            animations.forEachIndexed { index, anim ->
                val sweepAngle = (anim.value / 100f) * 360f
                drawArc(
                    color = data[index].category.toColor(),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = 24.dp.toPx(), cap = StrokeCap.Round)
                )
                startAngle += sweepAngle
            }
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = CurrencyUtils.formatCompact(totalAmount),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

        }
    }
}

@Composable
private fun DonutLegend(data: List<CategorySpend>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        data.forEach { spend ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(spend.category.toColor())
                )
                Text(
                    text = spend.category.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = FinanceTheme.colors.textSecondary
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${String.format("%.1f", spend.percentage)}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun CategoryBreakdownRow(spend: CategorySpend) {
    val financeColors = FinanceTheme.colors
    val categoryColor = spend.category.toColor()
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(categoryColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = spend.category.icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = spend.category.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = CurrencyUtils.format(spend.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LinearProgressIndicator(
                    progress = { spend.percentage / 100f },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = categoryColor,
                    trackColor = categoryColor.copy(alpha = 0.1f),
                    strokeCap = StrokeCap.Round
                )

            }
        }
    }
}
