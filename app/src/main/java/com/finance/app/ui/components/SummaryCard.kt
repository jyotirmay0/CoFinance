package com.finance.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finance.app.R
import com.finance.app.ui.theme.FinanceTheme
import com.finance.app.utils.CurrencyUtils

@Composable
fun SummaryRow(
    totalIncome: Double,
    totalExpense: Double,
    modifier: Modifier = Modifier
) {
    val financeColors = FinanceTheme.colors

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            label = "Income",
            subLabel = "This Month",
            amount = totalIncome,
            isIncome = true,
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            label = "Expenses",
            subLabel = "This Month",
            amount = totalExpense,
            isIncome = false,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SummaryCard(
    label: String,
    subLabel: String = "",
    amount: Double,
    isIncome: Boolean,
    modifier: Modifier = Modifier
) {
    val financeColors = FinanceTheme.colors
    val accentColor = if (isIncome) financeColors.income else financeColors.expense
    val icon = if (isIncome) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(financeColors.cardBackground)
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f))
                    .padding(4.dp)
            )
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = financeColors.textSecondary,
                letterSpacing = 0.8.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = CurrencyUtils.format(amount),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (subLabel.isNotEmpty()) {
            Text(
                text = subLabel,
                style = MaterialTheme.typography.labelSmall,
                color = financeColors.textSecondary
            )
        }
    }
}

private val androidx.compose.ui.unit.TextUnit.sp: androidx.compose.ui.unit.TextUnit
    get() = this