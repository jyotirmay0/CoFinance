package com.finance.app.ui.screens.transactions

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finance.app.R
import com.finance.app.domain.model.Category
import com.finance.app.domain.model.TransactionType
import com.finance.app.ui.components.CategoryChip
import com.finance.app.ui.theme.FinanceTheme
import com.finance.app.ui.viewmodel.TransactionUiEvent
import com.finance.app.ui.viewmodel.TransactionViewModel
import com.finance.app.utils.DateUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionScreen(
    transactionId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: TransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.addEditState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val isEditMode = transactionId != null && transactionId > 0L
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Load transaction for edit mode
    LaunchedEffect(transactionId) {
        if (isEditMode) viewModel.loadTransaction(transactionId!!)
        else viewModel.resetAddEditState()
    }

    // Handle events
    LaunchedEffect(uiState.event) {
        when (val event = uiState.event) {
            is TransactionUiEvent.SaveSuccess -> {
                viewModel.resetEvent()
                onNavigateBack()
            }
            is TransactionUiEvent.DeleteSuccess -> {
                viewModel.resetEvent()
                onNavigateBack()
            }
            is TransactionUiEvent.Error -> {
                snackbarHostState.showSnackbar(event.message)
                viewModel.resetEvent()
            }
            TransactionUiEvent.Idle -> Unit
        }
    }

    // Date picker
    val calendar = Calendar.getInstance().apply {
        timeInMillis = uiState.date
    }
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth)
            viewModel.onDateChange(cal.timeInMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode)
                            stringResource(R.string.edit_transaction)
                        else stringResource(R.string.add_transaction),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    if (isEditMode && uiState.transaction != null) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.delete_transaction),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Amount section
            AmountSection(
                amount = uiState.amount,
                onAmountChange = viewModel::onAmountChange,
                amountError = uiState.amountError
            )

            // Type selector
            TypeSelector(
                selectedType = uiState.type,
                onTypeChange = viewModel::onTypeChange
            )

            // Category selector
            CategorySection(
                selectedType = uiState.type,
                selectedCategory = uiState.category,
                onCategoryChange = viewModel::onCategoryChange,
                categoryError = uiState.categoryError
            )

            // Date picker
            DateSection(
                date = uiState.date,
                onDateClick = { datePickerDialog.show() }
            )

            // Notes
            NotesSection(
                notes = uiState.notes,
                onNotesChange = viewModel::onNotesChange
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Save button
            Button(
                onClick = viewModel::saveTransaction,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !uiState.isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = stringResource(R.string.save_transaction),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog && uiState.transaction != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.confirm_delete)) },
            text = { Text(stringResource(R.string.delete_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteTransaction(uiState.transaction!!)
                    }
                ) {
                    Text(
                        stringResource(R.string.delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun AmountSection(
    amount: String,
    onAmountChange: (String) -> Unit,
    amountError: String?
) {
    val financeColors = FinanceTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                financeColors.cardBackground,
                RoundedCornerShape(20.dp)
            )
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.transaction_amount).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = financeColors.textSecondary
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "₹",
                style = MaterialTheme.typography.displaySmall,
                color = financeColors.textSecondary,
                fontWeight = FontWeight.Light
            )
            OutlinedTextField(
                value = amount,
                onValueChange = { value ->
                    if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                        onAmountChange(value)
                    }
                },
                textStyle = MaterialTheme.typography.displaySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                placeholder = {
                    Text(
                        "0.00",
                        style = MaterialTheme.typography.displaySmall,
                        color = financeColors.textSecondary.copy(alpha = 0.5f)
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                    focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent
                ),
                isError = amountError != null
            )
        }

        amountError?.let { error ->
            Text(
                text = error,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TypeSelector(
    selectedType: TransactionType,
    onTypeChange: (TransactionType) -> Unit
) {
    val types = listOf(TransactionType.EXPENSE, TransactionType.INCOME)

    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        types.forEachIndexed { index, type ->
            SegmentedButton(
                selected = selectedType == type,
                onClick = { onTypeChange(type) },
                shape = SegmentedButtonDefaults.itemShape(index, types.size),
                label = {
                    Text(
                        text = when (type) {
                            TransactionType.EXPENSE -> stringResource(R.string.expense)
                            TransactionType.INCOME -> stringResource(R.string.income)
                        },
                        fontWeight = FontWeight.SemiBold
                    )
                }
            )
        }
    }
}

@Composable
private fun CategorySection(
    selectedType: TransactionType,
    selectedCategory: Category,
    onCategoryChange: (Category) -> Unit,
    categoryError: String?
) {
    val financeColors = FinanceTheme.colors
    val categories = remember(selectedType) {
        if (selectedType == TransactionType.INCOME) Category.incomeCategories()
        else Category.expenseCategories()
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.category),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(items = categories, key = { it.name }) { category ->
                CategoryChip(
                    category = category,
                    isSelected = selectedCategory == category,
                    onClick = { onCategoryChange(category) }
                )
            }
        }
        categoryError?.let { error ->
            Text(
                text = error,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun DateSection(
    date: Long,
    onDateClick: () -> Unit
) {
    val financeColors = FinanceTheme.colors

    Surface(
        onClick = onDateClick,
        shape = RoundedCornerShape(16.dp),
        color = financeColors.cardBackground,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Column {
                Text(
                    text = stringResource(R.string.date).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = financeColors.textSecondary
                )
                Text(
                    text = DateUtils.formatDate(date),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun NotesSection(
    notes: String,
    onNotesChange: (String) -> Unit
) {
    val financeColors = FinanceTheme.colors

    OutlinedTextField(
        value = notes,
        onValueChange = { if (it.length <= 200) onNotesChange(it) },
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                stringResource(R.string.notes_hint),
                color = financeColors.textSecondary
            )
        },
        label = {
            Text(stringResource(R.string.notes))
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = financeColors.textSecondary
            )
        },
        supportingText = {
            Text(
                text = "${notes.length}/200",
                color = financeColors.textSecondary
            )
        },
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = financeColors.divider,
            focusedContainerColor = financeColors.cardBackground,
            unfocusedContainerColor = financeColors.cardBackground
        ),
        maxLines = 3
    )
}