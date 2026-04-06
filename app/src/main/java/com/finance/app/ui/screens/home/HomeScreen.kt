package com.finance.app.ui.screens.home

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finance.app.R
import com.finance.app.ui.components.BalanceCard
import com.finance.app.ui.components.EmptyState
import com.finance.app.ui.components.FullScreenLoading
import com.finance.app.ui.components.GoalProgressCard
import com.finance.app.ui.components.SectionHeader
import com.finance.app.ui.components.SummaryRow
import com.finance.app.ui.components.TransactionItem
import com.finance.app.ui.screens.home.components.SpendingCategorySection
import com.finance.app.ui.theme.FinanceTheme
import com.finance.app.ui.viewmodel.HomeViewModel
import java.util.Calendar

@Composable
fun HomeScreen(
    onAddTransaction: () -> Unit,
    onSeeAllTransactions: () -> Unit,
    onTransactionClick: (Long) -> Unit,
    onGoalClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when {
        uiState.isLoading -> FullScreenLoading()
        uiState.error != null -> ErrorScreen(message = uiState.error!!)
        else -> HomeContent(
            uiState = uiState,
            onAddTransaction = onAddTransaction,
            onSeeAllTransactions = onSeeAllTransactions,
            onTransactionClick = onTransactionClick,
            onGoalClick = onGoalClick,
            onProfileClick = onProfileClick
        )
    }
}

@Composable
private fun HomeContent(
    uiState: com.finance.app.ui.viewmodel.HomeUiState,
    onAddTransaction: () -> Unit,
    onSeeAllTransactions: () -> Unit,
    onTransactionClick: (Long) -> Unit,
    onGoalClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val financeColors = FinanceTheme.colors

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Greeting header
            item {
                HomeHeader(onProfileClick = onProfileClick)
            }

            // Balance card
            item {
                BalanceCard(
                    totalBalance = uiState.balanceSummary.totalBalance,
                    changePercent = uiState.monthlyChangePercent
                )
            }

            // Income / Expense summary
            item {
                SummaryRow(
                    totalIncome = uiState.balanceSummary.totalIncome,
                    totalExpense = uiState.balanceSummary.totalExpense
                )
            }

            // Priority savings goal
            if (uiState.activeGoal != null) {
                item {
                    SectionHeader(title = "Priority Goal")
                }
                item {
                    GoalProgressCard(
                        goal = uiState.activeGoal,
                        onClick = onGoalClick
                    )
                }
            } else {
                item {
                    EmptyState(
                        icon = Icons.Default.EmojiEvents,
                        title = "No Active Goal",
                        subtitle = "Tap Goals to set your first savings goal!"
                    )
                }
            }

            // Spending categories
            if (uiState.recentTransactions.isNotEmpty()) {
                item {
                    SectionHeader(title = stringResource(R.string.spending_categories))
                }
                item {
                    SpendingCategorySection(transactions = uiState.recentTransactions)
                }
            }

            // Recent transactions
            item {
                SectionHeader(
                    title = stringResource(R.string.recent_transactions),
                    showSeeAll = true,
                    onSeeAllClick = onSeeAllTransactions
                )
            }

            if (uiState.recentTransactions.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.SwapHoriz,
                        title = stringResource(R.string.empty_transactions_title),
                        subtitle = stringResource(R.string.empty_transactions_subtitle)
                    )
                }
            } else {
                items(
                    items = uiState.recentTransactions,
                    key = { it.id }
                ) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        onClick = { onTransactionClick(transaction.id) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }

        FloatingActionButton(
            onClick = onAddTransaction,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Transaction")
        }
    }
}

@Composable
private fun HomeHeader(onProfileClick: () -> Unit) {
    val financeColors = FinanceTheme.colors


    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {

            Text(
                text = "CoFinance",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            {
            }
            IconButton(onClick = onProfileClick) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile",
                    tint = financeColors.textSecondary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun ErrorScreen(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error
        )
    }
}

