package com.finance.app.ui.screens.transactions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finance.app.R
import com.finance.app.domain.model.Transaction
import com.finance.app.domain.model.TransactionType
import com.finance.app.ui.components.EmptyState
import com.finance.app.ui.components.FullScreenLoading
import com.finance.app.ui.components.TransactionItem
import com.finance.app.ui.theme.FinanceTheme
import com.finance.app.ui.viewmodel.TransactionFilter
import com.finance.app.ui.viewmodel.TransactionViewModel
import com.finance.app.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    onAddTransaction: () -> Unit,
    onTransactionClick: (Long) -> Unit,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.listUiState.collectAsStateWithLifecycle()
    val financeColors = FinanceTheme.colors
    var showFilters by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransaction,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            // Search bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            placeholder = {
                Text(
                    text = stringResource(R.string.search_transactions),
                    color = financeColors.textSecondary
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Default.Search,
                    contentDescription = null,
                    tint = financeColors.textSecondary
                )
            },
            trailingIcon = {
                Row {
                    if (uiState.searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = if (showFilters) MaterialTheme.colorScheme.primary
                            else financeColors.textSecondary
                        )
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = financeColors.divider,
                focusedContainerColor = financeColors.cardBackground,
                unfocusedContainerColor = financeColors.cardBackground
            ),
            singleLine = true
        )

        // Filter chips
        AnimatedVisibility(
            visible = showFilters,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            FilterRow(
                currentFilter = uiState.filter,
                onFilterChange = viewModel::onFilterChange,
                onClearFilters = viewModel::clearFilters
            )
        }

        when {
            uiState.isLoading -> FullScreenLoading()
            uiState.error != null -> {
                EmptyState(
                    icon = Icons.Default.SwapHoriz,
                    title = "Error",
                    subtitle = uiState.error!!
                )
            }
            uiState.transactions.isEmpty() -> {
                EmptyState(
                    icon = Icons.Default.SwapHoriz,
                    title = if (uiState.searchQuery.isNotBlank())
                        stringResource(R.string.empty_search_title)
                    else stringResource(R.string.empty_transactions_title),
                    subtitle = if (uiState.searchQuery.isNotBlank())
                        stringResource(R.string.empty_search_subtitle)
                    else stringResource(R.string.empty_transactions_subtitle)
                )
            }
            else -> {
                TransactionGroupedList(
                    transactions = uiState.transactions,
                    onTransactionClick = onTransactionClick
                )
            }
            }
        }
    }
}

@Composable
private fun FilterRow(
    currentFilter: TransactionFilter,
    onFilterChange: (TransactionFilter) -> Unit,
    onClearFilters: () -> Unit
) {
    val financeColors = FinanceTheme.colors
    val hasFilters = currentFilter.type != null || currentFilter.category != null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterChip(
            selected = currentFilter.type == TransactionType.EXPENSE,
            onClick = {
                onFilterChange(
                    currentFilter.copy(
                        type = if (currentFilter.type == TransactionType.EXPENSE)
                            null else TransactionType.EXPENSE
                    )
                )
            },
            label = { Text(stringResource(R.string.expense)) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                selectedLabelColor = MaterialTheme.colorScheme.primary
            )
        )

        FilterChip(
            selected = currentFilter.type == TransactionType.INCOME,
            onClick = {
                onFilterChange(
                    currentFilter.copy(
                        type = if (currentFilter.type == TransactionType.INCOME)
                            null else TransactionType.INCOME
                    )
                )
            },
            label = { Text(stringResource(R.string.income)) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = financeColors.income.copy(alpha = 0.15f),
                selectedLabelColor = financeColors.income
            )
        )

        if (hasFilters) {
            TextButton(onClick = onClearFilters) {
                Text(
                    text = "Clear",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun TransactionGroupedList(
    transactions: List<Transaction>,
    onTransactionClick: (Long) -> Unit
) {
    val grouped = remember(transactions) {
        transactions.groupBy { DateUtils.getRelativeDateLabel(it.date) }
    }

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        grouped.forEach { (dateLabel, txList) ->
            item(key = dateLabel) {
                Text(
                    text = dateLabel.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = FinanceTheme.colors.textSecondary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            items(items = txList, key = { it.id }) { transaction ->
                TransactionItem(
                    transaction = transaction,
                    onClick = { onTransactionClick(transaction.id) }
                )
            }
            item { Spacer(modifier = Modifier.height(4.dp)) }
        }
    }
}