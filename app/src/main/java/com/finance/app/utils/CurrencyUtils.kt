package com.finance.app.utils

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object CurrencyUtils {

    private val defaultFormatter: NumberFormat by lazy {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 2
            minimumFractionDigits = 2
        }
    }

    fun format(amount: Double): String {
        return try {
            defaultFormatter.format(amount)
        } catch (e: Exception) {
            "₹%.2f".format(amount)
        }
    }

    fun formatWithSign(amount: Double, isIncome: Boolean): String {
        val formatted = format(amount)
        return if (isIncome) "+$formatted" else "-$formatted"
    }

    fun formatCompact(amount: Double): String {
        return when {
            amount >= 1_000_000 -> "${"%.1f".format(amount / 1_000_000)}M"
            amount >= 1_000 -> "${"%.1f".format(amount / 1_000)}K"
            else -> format(amount)
        }
    }

    fun parseAmount(input: String): Double? {
        val cleaned = input
            .replace(",", "")
            .replace("$", "")
            .replace("₹", "")
            .replace("€", "")
            .replace("£", "")
            .trim()
        return cleaned.toDoubleOrNull()
    }
}