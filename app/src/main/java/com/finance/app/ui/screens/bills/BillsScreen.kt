package com.finance.app.ui.screens.bills

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.finance.app.R
import com.finance.app.domain.model.Bill
import com.finance.app.ui.theme.ExpenseRed
import com.finance.app.ui.theme.FinanceTheme
import com.finance.app.ui.theme.IncomeGreen
import com.finance.app.ui.theme.WarningAmber
import com.finance.app.ui.viewmodel.BillsViewModel
import com.finance.app.utils.CurrencyUtils

@Composable
fun BillsScreen(
    viewModel: BillsViewModel = hiltViewModel()
) {
    val financeColors = FinanceTheme.colors
    val uiState by viewModel.uiState.collectAsState()
    val bills = uiState.bills

    var showAddDialog by remember { mutableStateOf(false) }

    val dueSoonCount = bills.count { !it.isPaid && it.daysUntilDue <= 7 }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Bill")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Text(
                text = stringResource(R.string.bills_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            if (dueSoonCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.bills_due_this_week, dueSoonCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = financeColors.textSecondary
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items = bills, key = { it.id }) { bill ->
                BillCard(
                    bill = bill,
                    onMarkPaid = { id ->
                        viewModel.markPaid(bill)
                    }
                )
            }
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}}

@Composable
 fun BillCard(
    bill: Bill,
    onMarkPaid: (Int) -> Unit
) {
    val financeColors = FinanceTheme.colors
    val isOverdue = bill.daysUntilDue < 0
    val isUrgent = bill.daysUntilDue in 0..2 && !bill.isPaid

    val containerColor =
        if (isOverdue) ExpenseRed.copy(alpha = 0.15f) else if (isUrgent && !bill.isPaid) ExpenseRed.copy(
            alpha = 0.08f
        ) else financeColors.cardBackground

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(containerColor)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ElectricBolt,
                    contentDescription = bill.name,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Name and due date
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bill.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isOverdue && !bill.isPaid) ExpenseRed else MaterialTheme.colorScheme.onSurface
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isOverdue) "Due ${-bill.daysUntilDue} days ago" else if (bill.daysUntilDue == 0) "Due today" else "Due in ${bill.daysUntilDue} days",
                        style = MaterialTheme.typography.bodySmall,
                        color = if ((isOverdue || isUrgent) && !bill.isPaid) ExpenseRed else financeColors.textSecondary
                    )
                    if (bill.isAutoPay) {
                        Text(
                            text = "• Auto-pay",
                            style = MaterialTheme.typography.bodySmall,
                            color = IncomeGreen
                        )
                    }
                }
            }

            // Amount
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyUtils.format(bill.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (bill.isPaid) IncomeGreen
                    else MaterialTheme.colorScheme.onSurface
                )
                if (bill.isPaid) {
                    Text(
                        text = "Paid ✓",
                        style = MaterialTheme.typography.labelSmall,
                        color = IncomeGreen
                    )
                }
            }
        }

        if (!bill.isPaid) {
            Spacer(modifier = Modifier.height(12.dp))

            if (isUrgent || isOverdue) {
                Button(
                    onClick = { onMarkPaid(bill.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        stringResource(R.string.mark_as_paid),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                OutlinedButton(
                    onClick = { onMarkPaid(bill.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        stringResource(R.string.mark_as_paid),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}