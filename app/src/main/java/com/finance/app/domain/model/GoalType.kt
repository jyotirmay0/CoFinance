package com.finance.app.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.graphics.vector.ImageVector

enum class GoalType(
    val displayName: String,
    val icon: ImageVector,
    val emoji: String
) {
    SAVINGS("Savings", Icons.Default.AccountBalance, "💰"),
    INVESTMENT("Investment", Icons.Default.TrendingUp, "📈"),
    DEBT_PAYOFF("Debt Payoff", Icons.Default.Shield, "🛡️"),
    EMERGENCY_FUND("Emergency Fund", Icons.Default.HealthAndSafety, "🏥"),
    TRAVEL("Travel", Icons.Default.Flight, "✈️"),
    EDUCATION("Education", Icons.Default.School, "🎓"),
    HOME("Home", Icons.Default.Home, "🏠"),
    VEHICLE("Vehicle", Icons.Default.DirectionsCar, "🚗"),
    OTHER("Other", Icons.Default.MoreHoriz, "🎯");

    companion object {
        fun fromString(value: String): GoalType =
            entries.find { it.name == value } ?: OTHER
    }
}
