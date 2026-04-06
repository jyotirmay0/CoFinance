package com.finance.app.navigation

sealed class Screen(val route: String) {

    // Bottom nav screens
    data object Home : Screen("home")
    data object Transactions : Screen("transactions")
    data object Charts : Screen("charts")
    data object Insights : Screen("insights")
    data object Goal : Screen("goal")
    data object Settings : Screen("settings")

    // Detail screens
    data object AddTransaction : Screen("add_transaction")

    data class EditTransaction(val transactionId: Long = 0L) :
        Screen("edit_transaction/{transactionId}") {
        fun createRoute(id: Long) = "edit_transaction/$id"
        companion object {
            const val ROUTE = "edit_transaction/{transactionId}"
            const val ARG = "transactionId"
        }
    }

    companion object {
        val bottomNavItems = listOf(Home, Transactions, Charts, Insights)
    }
}