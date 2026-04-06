package com.finance.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ── Extended colors exposed via CompositionLocal ──────────────────────────────
data class FinanceColors(
    val income: Color,
    val expense: Color,
    val warning: Color,
    val cardBackground: Color,
    val cardAlt: Color,
    val textSecondary: Color,
    val divider: Color,
    val balanceCardGradientStart: Color,
    val balanceCardGradientEnd: Color
)

val LocalFinanceColors = staticCompositionLocalOf {
    FinanceColors(
        income = IncomeGreen,
        expense = ExpenseRed,
        warning = WarningAmber,
        cardBackground = DarkCard,
        cardAlt = DarkCardAlt,
        textSecondary = TextSecondary,
        divider = DividerColor,
        balanceCardGradientStart = BrandBlue,
        balanceCardGradientEnd = BrandBlueDark
    )
}

private val DarkColorScheme = darkColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = BrandBlueDark,
    onPrimaryContainer = Color.White,
    secondary = IncomeGreen,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF1A3D34),
    onSecondaryContainer = IncomeGreen,
    tertiary = ExpenseRed,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVar,
    onSurfaceVariant = TextSecondary,
    outline = DividerColor,
    error = ExpenseRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = BrandBlueLight,
    onPrimaryContainer = Color.White,
    secondary = IncomeGreenDark,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD4F4EC),
    onSecondaryContainer = IncomeGreenDark,
    tertiary = ExpenseRedDark,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurfaceVar,
    onSurfaceVariant = TextSecondaryLight,
    outline = DividerColorLight,
    error = ExpenseRedDark,
    onError = Color.White
)

private val DarkFinanceColors = FinanceColors(
    income = IncomeGreen,
    expense = ExpenseRed,
    warning = WarningAmber,
    cardBackground = DarkCard,
    cardAlt = DarkCardAlt,
    textSecondary = TextSecondary,
    divider = DividerColor,
    balanceCardGradientStart = BrandBlue,
    balanceCardGradientEnd = BrandBlueDark
)

private val LightFinanceColors = FinanceColors(
    income = IncomeGreenDark,
    expense = ExpenseRedDark,
    warning = WarningAmberDark,
    cardBackground = LightCard,
    cardAlt = LightSurfaceVar,
    textSecondary = TextSecondaryLight,
    divider = DividerColorLight,
    balanceCardGradientStart = BrandBlue,
    balanceCardGradientEnd = BrandBlueDark
)

@Composable
fun FinanceCompanionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val financeColors = if (darkTheme) DarkFinanceColors else LightFinanceColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalFinanceColors provides financeColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = FinanceTypography,
            content = content
        )
    }
}

// Convenience accessor
object FinanceTheme {
    val colors: FinanceColors
        @Composable get() = LocalFinanceColors.current
}