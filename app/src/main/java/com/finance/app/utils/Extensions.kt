package com.finance.app.utils

import androidx.compose.ui.graphics.Color

fun Double.toCurrencyString(): String = CurrencyUtils.format(this)

fun Double.toCompactCurrencyString(): String = CurrencyUtils.formatCompact(this)

fun Long.toFormattedDate(): String = DateUtils.formatDate(this)

fun Long.toFormattedDateTime(): String = DateUtils.formatDateTime(this)

fun Long.toRelativeDateLabel(): String = DateUtils.getRelativeDateLabel(this)

fun String.toAmountOrNull(): Double? = CurrencyUtils.parseAmount(this)

fun Float.toPercent(): String = "%.0f%%".format(this * 100)

fun Double.toPercent(): String = "%.1f%%".format(this)

fun Color.Companion.fromHex(hex: String): Color {
    val cleaned = hex.trimStart('#')
    return Color(android.graphics.Color.parseColor("#$cleaned"))
}

fun <T> List<T>.indexOfFirstOrNull(predicate: (T) -> Boolean): Int? {
    val idx = indexOfFirst(predicate)
    return if (idx == -1) null else idx
}