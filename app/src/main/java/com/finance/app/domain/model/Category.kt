package com.finance.app.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.ui.graphics.vector.ImageVector

enum class Category(
    val displayName: String,
    val icon: ImageVector,
    val isIncomeCategory: Boolean = false
) {
    // Expense categories
    FOOD("Food", Icons.Default.Fastfood),
    TRAVEL("Travel", Icons.Default.Flight),
    SHOPPING("Shopping", Icons.Default.ShoppingCart),
    ENTERTAINMENT("Entertainment", Icons.Default.EmojiEvents),
    HEALTH("Health", Icons.Default.HealthAndSafety),
    TRANSPORT("Transport", Icons.Default.DirectionsCar),
    HOUSING("Housing", Icons.Default.Home),
    FITNESS("Fitness", Icons.Default.FitnessCenter),
    SUBSCRIPTION("Subscription", Icons.Default.Subscriptions),
    OTHER("Other", Icons.Default.MoreHoriz),

    // Income categories
    SALARY("Salary", Icons.Default.AttachMoney, isIncomeCategory = true),
    FREELANCE("Freelance", Icons.Default.TrendingUp, isIncomeCategory = true),
    INVESTMENT("Investment", Icons.Default.TrendingUp, isIncomeCategory = true),
    INCOME_OTHER("Other Income", Icons.Default.AttachMoney, isIncomeCategory = true);

    companion object {
        fun expenseCategories() = entries.filter { !it.isIncomeCategory }
        fun incomeCategories() = entries.filter { it.isIncomeCategory }
    }
}