package com.finance.app.ui.screens.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finance.app.R
import com.finance.app.domain.model.CategorySpend
import com.finance.app.domain.model.InsightData
import com.finance.app.domain.model.MonthlyTrend
import com.finance.app.ui.components.EmptyState
import com.finance.app.ui.components.FullScreenLoading
import com.finance.app.ui.components.SectionHeader
import com.finance.app.ui.theme.ExpenseRed
import com.finance.app.ui.theme.FinanceTheme
import com.finance.app.ui.theme.IncomeGreen
import com.finance.app.ui.theme.toColor
import com.finance.app.ui.viewmodel.InsightsUiState
import com.finance.app.ui.screens.insights.components.SpendingTrendChart
import com.finance.app.ui.viewmodel.InsightsViewModel
import com.finance.app.utils.CurrencyUtils

@Composable
fun InsightsScreen(
    viewModel: InsightsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        InsightsUiState.Loading -> FullScreenLoading()
        InsightsUiState.Empty -> EmptyState(
            icon = Icons.Default.BarChart,
            title = stringResource(R.string.empty_insights_title),
            subtitle = stringResource(R.string.empty_insights_subtitle),
            modifier = Modifier.fillMaxSize()
        )
        is InsightsUiState.Error -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = state.message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
        }
        is InsightsUiState.Success -> InsightsContent(data = state.data)
    }
}

@Composable
private fun InsightsContent(data: InsightData) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Simple Top Header
        item {
            Text(
                text = "Financial Insights",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        // Hero Chart: Monthly Spending Trend
        if (data.monthlyTrends.isNotEmpty()) {
            item {
                SpendingTrendChart(trends = data.monthlyTrends)
            }
        }

        // Weekly comparison card (secondary insight)
        item {
            WeeklyComparisonCard(
                currentWeek = data.currentWeekExpense,
                previousWeek = data.previousWeekExpense,
                changePercent = data.weeklyChangePercent
            )
        }

        // Highest spending category
        data.highestSpendingCategory?.let { top ->
            item {
                SectionHeader(title = stringResource(R.string.highest_spending))
            }
            item {
                HighestSpendingCard(categorySpend = top)
            }
        }



        // Category breakdown
        if (data.categoryBreakdown.isNotEmpty()) {
            item {
                SectionHeader(title = stringResource(R.string.category_breakdown))
            }
            items(
                items = data.categoryBreakdown.take(6),
                key = { it.category.name }
            ) { spend ->
                CategoryBreakdownItem(spend = spend)
            }
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

@Composable
private fun WeeklyComparisonCard(
    currentWeek: Double,
    previousWeek: Double,
    changePercent: Double
) {
    val financeColors = FinanceTheme.colors
    val isImprovement = changePercent <= 0
    val trendColor = if (isImprovement) IncomeGreen else ExpenseRed
    val trendIcon = if (isImprovement) Icons.Default.TrendingDown else Icons.Default.TrendingUp
    val trendLabel = if (isImprovement) "Less than last week" else "More than last week"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(financeColors.cardBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = stringResource(R.string.weekly_comparison).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = financeColors.textSecondary
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            WeekColumn(
                label = stringResource(R.string.this_week),
                amount = currentWeek,
                isHighlighted = true
            )
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(trendColor.copy(alpha = 0.12f))
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = trendIcon,
                    contentDescription = null,
                    tint = trendColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            WeekColumn(
                label = stringResource(R.string.last_week),
                amount = previousWeek,
                isHighlighted = false
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = trendIcon,
                contentDescription = null,
                tint = trendColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "${String.format("%.1f", kotlin.math.abs(changePercent))}% $trendLabel",
                style = MaterialTheme.typography.bodySmall,
                color = trendColor
            )
        }
    }
}

@Composable
private fun WeekColumn(
    label: String,
    amount: Double,
    isHighlighted: Boolean
) {
    val financeColors = FinanceTheme.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = financeColors.textSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = CurrencyUtils.format(amount),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (isHighlighted) MaterialTheme.colorScheme.onSurface
            else financeColors.textSecondary
        )
    }
}

@Composable
private fun HighestSpendingCard(categorySpend: CategorySpend) {
    val financeColors = FinanceTheme.colors
    val categoryColor = categorySpend.category.toColor()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(financeColors.cardBackground)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(categoryColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = categorySpend.category.icon,
                contentDescription = null,
                tint = categoryColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = categorySpend.category.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${categorySpend.percentage.toInt()}% of total spending",
                style = MaterialTheme.typography.bodySmall,
                color = financeColors.textSecondary
            )
        }

        Text(
            text = CurrencyUtils.format(categorySpend.amount),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = categoryColor
        )
    }
}

@Composable
private fun MonthlyTrendChart(trends: List<MonthlyTrend>) {
    val financeColors = FinanceTheme.colors
    val maxAmount = trends.maxOf { maxOf(it.totalExpense, it.totalIncome) }
        .takeIf { it > 0 } ?: 1.0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(financeColors.cardBackground)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Bar chart rows
        trends.forEach { trend ->
            MonthTrendRow(
                trend = trend,
                maxAmount = maxAmount
            )
        }
    }
}

@Composable
private fun MonthTrendRow(
    trend: MonthlyTrend,
    maxAmount: Double
) {
    val financeColors = FinanceTheme.colors
    val expenseRatio = (trend.totalExpense / maxAmount).toFloat().coerceIn(0f, 1f)
    val incomeRatio = (trend.totalIncome / maxAmount).toFloat().coerceIn(0f, 1f)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = trend.monthLabel,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.width(36.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                if (trend.totalIncome > 0) {
                    LinearProgressIndicator(
                        progress = { incomeRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = IncomeGreen,
                        trackColor = IncomeGreen.copy(alpha = 0.1f),
                        strokeCap = StrokeCap.Round
                    )
                }
                if (trend.totalExpense > 0) {
                    LinearProgressIndicator(
                        progress = { expenseRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = ExpenseRed,
                        trackColor = ExpenseRed.copy(alpha = 0.1f),
                        strokeCap = StrokeCap.Round
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = CurrencyUtils.formatCompact(trend.totalExpense),
                style = MaterialTheme.typography.labelSmall,
                color = financeColors.textSecondary,
                modifier = Modifier.width(40.dp)
            )
        }
    }
}

@Composable
private fun CategoryBreakdownItem(spend: CategorySpend) {
    val financeColors = FinanceTheme.colors
    val categoryColor = spend.category.toColor()
    val progress = (spend.percentage / 100f).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(financeColors.cardBackground)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(categoryColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = spend.category.icon,
                        contentDescription = null,
                        tint = categoryColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = spend.category.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyUtils.format(spend.amount),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${spend.percentage.toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = categoryColor
                )
            }
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = categoryColor,
            trackColor = categoryColor.copy(alpha = 0.12f),
            strokeCap = StrokeCap.Round
        )
    }
}