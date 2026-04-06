package com.finance.app.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.finance.app.R
import com.finance.app.domain.model.Category
import com.finance.app.domain.model.Transaction
import com.finance.app.domain.model.TransactionType
import com.finance.app.ui.theme.FinanceTheme
import com.finance.app.ui.theme.toColor
import com.finance.app.utils.CurrencyUtils

@Composable
fun SpendingCategorySection(
    transactions: List<Transaction>,
    modifier: Modifier = Modifier
) {
    val financeColors = FinanceTheme.colors

    val categoryTotals = remember(transactions) {
        transactions
            .filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { (_, txList) -> txList.sumOf { it.amount } }
            .entries
            .sortedByDescending { it.value }
            .take(3)
    }

    val totalExpense = remember(categoryTotals) {
        categoryTotals.sumOf { it.value }
    }

    if (categoryTotals.isEmpty()) return

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Largest category — big card
        val topEntry = categoryTotals.first()
        val topPercent = if (totalExpense > 0)
            ((topEntry.value / totalExpense) * 100).toInt() else 0

        LargeCategoryCard(
            category = topEntry.key,
            amount = topEntry.value,
            percent = topPercent,
            modifier = Modifier.weight(1.2f)
        )

        // Remaining — stacked small cards
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            categoryTotals.drop(1).forEach { entry ->
                val pct = if (totalExpense > 0)
                    ((entry.value / totalExpense) * 100).toInt() else 0
                SmallCategoryCard(
                    category = entry.key,
                    percent = pct
                )
            }
        }
    }
}

@Composable
private fun LargeCategoryCard(
    category: Category,
    amount: Double,
    percent: Int,
    modifier: Modifier = Modifier
) {
    val financeColors = FinanceTheme.colors
    val categoryColor = category.toColor()

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(financeColors.cardBackground)
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$percent% ${stringResource(R.string.of_spend)}",
                style = MaterialTheme.typography.bodySmall,
                color = financeColors.textSecondary
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = CurrencyUtils.format(amount),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Icon(
                imageVector = category.icon,
                contentDescription = category.displayName,
                tint = categoryColor,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun SmallCategoryCard(
    category: Category,
    percent: Int,
    modifier: Modifier = Modifier
) {
    val financeColors = FinanceTheme.colors
    val categoryColor = category.toColor()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(financeColors.cardAlt)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(categoryColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.displayName,
                tint = categoryColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "$percent%",
                style = MaterialTheme.typography.labelSmall,
                color = categoryColor
            )
        }
    }
}