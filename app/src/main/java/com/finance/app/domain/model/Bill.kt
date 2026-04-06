package com.finance.app.domain.model

data class Bill(
    val id: Int = 0,
    val name: String,
    val amount: Double,
    val dueDate: Long,
    val isAutoPay: Boolean = false,
    val isPaid: Boolean = false
) {
    val daysUntilDue: Int
        get() {
            val now = System.currentTimeMillis()
            val diffMs = dueDate - now
            return (diffMs / (1000 * 60 * 60 * 24)).toInt()
        }
}
