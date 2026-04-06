package com.finance.app.ui.screens.goal

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.finance.app.R
import com.finance.app.domain.model.Goal
import com.finance.app.domain.model.GoalType
import com.finance.app.ui.components.EmptyState
import com.finance.app.ui.components.FullScreenLoading
import com.finance.app.ui.components.GoalProgressCard
import com.finance.app.ui.theme.FinanceTheme
import com.finance.app.ui.theme.IncomeGreen
import com.finance.app.ui.viewmodel.GoalFormState
import com.finance.app.ui.viewmodel.GoalUiEvent
import com.finance.app.ui.viewmodel.GoalUiState
import com.finance.app.ui.viewmodel.GoalViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalScreen(
    onNavigateBack: () -> Unit,
    viewModel: GoalViewModel = hiltViewModel()
) {
    val goalState by viewModel.goalUiState.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showForm by remember { mutableStateOf(false) }
    var editingGoalId by remember { mutableStateOf<Long?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Long?>(null) }

    // Handle form events
    LaunchedEffect(formState.event) {
        when (val event = formState.event) {
            is GoalUiEvent.SaveSuccess -> {
                viewModel.resetEvent()
                showForm = false
                editingGoalId = null
            }
            is GoalUiEvent.DeleteSuccess -> {
                viewModel.resetEvent()
                showForm = false
                editingGoalId = null
            }
            is GoalUiEvent.Error -> {
                snackbarHostState.showSnackbar(event.message)
                viewModel.resetEvent()
            }
            GoalUiEvent.Idle -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (showForm) {
                            if (editingGoalId != null) "Edit Goal" else "New Goal"
                        } else {
                            "Financial Goals"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (showForm) {
                            showForm = false
                            editingGoalId = null
                            viewModel.resetForm()
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            if (!showForm) {
                FloatingActionButton(
                    onClick = {
                        viewModel.resetForm()
                        showForm = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Goal")
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (val state = goalState) {
                is GoalUiState.Loading -> FullScreenLoading()
                is GoalUiState.HasGoals -> {
                    if (showForm) {
                        GoalForm(
                            formState = formState,
                            isEditing = editingGoalId != null,
                            onNameChange = viewModel::onNameChange,
                            onTargetChange = viewModel::onTargetAmountChange,
                            onCurrentChange = viewModel::onCurrentAmountChange,
                            onTypeChange = viewModel::onTypeChange,
                            onSave = { viewModel.saveGoal(editingGoalId ?: 0L) },
                            onCancel = {
                                showForm = false
                                editingGoalId = null
                                viewModel.resetForm()
                            }
                        )
                    } else {
                        GoalList(
                            goals = state.goals,
                            onEditGoal = { goal ->
                                viewModel.prefillForm(goal)
                                editingGoalId = goal.id
                                showForm = true
                            },
                            onDeleteGoal = { id ->
                                showDeleteDialog = id
                            }
                        )
                    }
                }
                is GoalUiState.NoGoals -> {
                    if (showForm) {
                        GoalForm(
                            formState = formState,
                            isEditing = false,
                            onNameChange = viewModel::onNameChange,
                            onTargetChange = viewModel::onTargetAmountChange,
                            onCurrentChange = viewModel::onCurrentAmountChange,
                            onTypeChange = viewModel::onTypeChange,
                            onSave = { viewModel.saveGoal() },
                            onCancel = {
                                showForm = false
                                viewModel.resetForm()
                            }
                        )
                    } else {
                        EmptyGoalsState(
                            onCreateGoal = {
                                viewModel.resetForm()
                                showForm = true
                            }
                        )
                    }
                }
                is GoalUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

    // Delete dialog
    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Goal") },
            text = { Text("Are you sure you want to delete this goal? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog?.let { viewModel.deleteGoal(it) }
                    showDeleteDialog = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun GoalList(
    goals: List<Goal>,
    onEditGoal: (Goal) -> Unit,
    onDeleteGoal: (Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(goals, key = { it.id }) { goal ->
            GoalItem(
                goal = goal,
                onEdit = { onEditGoal(goal) },
                onDelete = { onDeleteGoal(goal.id) }
            )
        }
    }
}

@Composable
private fun GoalItem(
    goal: Goal,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val financeColors = FinanceTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(financeColors.cardBackground)
            .clickable { expanded = !expanded }
            .padding(16.dp)
    ) {
        GoalProgressCard(
            goal = goal,
            onClick = { expanded = !expanded }
        )

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalForm(
    formState: GoalFormState,
    isEditing: Boolean,
    onNameChange: (String) -> Unit,
    onTargetChange: (String) -> Unit,
    onCurrentChange: (String) -> Unit,
    onTypeChange: (GoalType) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    val financeColors = FinanceTheme.colors
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .imePadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = if (isEditing) "Modify your goal details" else "Define a new financial objective",
            style = MaterialTheme.typography.bodyMedium,
            color = financeColors.textSecondary
        )

        // Goal Type Selector
        Text(
            text = "GOAL CATEGORY",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = financeColors.textSecondary,
            letterSpacing = 1.sp
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.height(260.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(GoalType.entries) { type ->
                val selected = formState.type == type
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            else financeColors.cardBackground
                        )
                        .clickable { onTypeChange(type) }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = type.emoji, fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = type.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center,
                            color = if (selected) MaterialTheme.colorScheme.primary else financeColors.textSecondary
                        )
                    }
                }
            }
        }

        // Goal Name
        OutlinedTextField(
            value = formState.name,
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Goal Name") },
            placeholder = { Text("e.g. New Electric Car") },
            isError = formState.nameError != null,
            supportingText = formState.nameError?.let { { Text(it) } },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = financeColors.cardBackground,
                unfocusedContainerColor = financeColors.cardBackground
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Target Amount
            OutlinedTextField(
                value = formState.targetAmount,
                onValueChange = onTargetChange,
                modifier = Modifier.weight(1f),
                label = { Text("Target") },
                placeholder = { Text("0.00") },
                prefix = { Text("₹") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = formState.targetError != null,
                supportingText = formState.targetError?.let { { Text(it) } },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = financeColors.cardBackground,
                    unfocusedContainerColor = financeColors.cardBackground
                )
            )

            // Current Amount
            OutlinedTextField(
                value = formState.currentAmount,
                onValueChange = onCurrentChange,
                modifier = Modifier.weight(1f),
                label = { Text("Saved") },
                placeholder = { Text("0.00") },
                prefix = { Text("₹") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = formState.currentError != null,
                supportingText = formState.currentError?.let { { Text(it) } },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = financeColors.cardBackground,
                    unfocusedContainerColor = financeColors.cardBackground
                )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = !formState.isSaving
        ) {
            if (formState.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = if (isEditing) "Update Goal" else "Create Goal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        TextButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Cancel", color = financeColors.textSecondary)
        }
        
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun EmptyGoalsState(onCreateGoal: () -> Unit) {
    val financeColors = FinanceTheme.colors
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(52.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No active goals",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Set financial targets and track your progress to stay motivated.",
            style = MaterialTheme.typography.bodyMedium,
            color = financeColors.textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onCreateGoal,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.size(8.dp))
            Text("Set New Goal", fontWeight = FontWeight.Bold)
        }
    }
}