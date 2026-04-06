package com.finance.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
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
import com.finance.app.R
import com.finance.app.ui.theme.ExpenseRed
import com.finance.app.ui.theme.WarningAmber
import com.finance.app.utils.CurrencyUtils

@Composable
fun UpcomingBillBanner(
    billName: String,
    amount: Double,
    daysUntilDue: Int,
    modifier: Modifier = Modifier
) {
    val isOverdue = daysUntilDue < 0
    val isUrgent = daysUntilDue in 0..1
    val bgColor = if (isOverdue)
        ExpenseRed.copy(alpha = 0.15f)
    else if (isUrgent)
        ExpenseRed.copy(alpha = 0.08f)
    else
        WarningAmber.copy(alpha = 0.10f)
    val iconColor = if (isOverdue || isUrgent) ExpenseRed else WarningAmber

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = iconColor,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.width(12.dp))

        val titleText = if (isOverdue) "Bill Overdue" else if (isUrgent) "Urgent alert" else stringResource(R.string.upcoming_bill)
        val descText = if (isOverdue) "$billName due ${-daysUntilDue} days ago" else if (daysUntilDue == 0) "$billName due today" else "$billName due in $daysUntilDue days"

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = titleText,
                style = MaterialTheme.typography.titleMedium,
                color = iconColor,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$descText • ${CurrencyUtils.format(amount)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }


    }
}
