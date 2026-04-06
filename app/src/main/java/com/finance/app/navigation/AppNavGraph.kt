package com.finance.app.navigation


import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.finance.app.ui.screens.charts.ChartsScreen
import com.finance.app.ui.screens.goal.GoalScreen
import com.finance.app.ui.screens.home.HomeScreen
import com.finance.app.ui.screens.insights.InsightsScreen
import com.finance.app.ui.screens.settings.SettingsScreen
import com.finance.app.ui.screens.transactions.AddEditTransactionScreen
import com.finance.app.ui.screens.transactions.TransactionsScreen

private const val NAV_ANIM_DURATION = 300

@Composable
fun AppNavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = Modifier.padding(paddingValues),
        enterTransition = {
            fadeIn(animationSpec = tween(NAV_ANIM_DURATION)) +
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = tween(NAV_ANIM_DURATION)
                    )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(NAV_ANIM_DURATION)) +
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = tween(NAV_ANIM_DURATION)
                    )
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(NAV_ANIM_DURATION)) +
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = tween(NAV_ANIM_DURATION)
                    )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(NAV_ANIM_DURATION)) +
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = tween(NAV_ANIM_DURATION)
                    )
        }
    ) {
        // ── Bottom Nav ────────────────────────────────────────────────────────
        composable(route = Screen.Home.route) {
            val navigateToTab = { screen: Screen ->
                navController.navigate(screen.route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }

            HomeScreen(
                onAddTransaction = {
                    navController.navigate(Screen.AddTransaction.route)
                },
                onSeeAllTransactions = {
                    navigateToTab(Screen.Transactions)
                },
                onTransactionClick = { transactionId ->
                    navController.navigate(
                        Screen.EditTransaction().createRoute(transactionId)
                    )
                },
                onGoalClick = {
                    navigateToTab(Screen.Goal)
                },
                onProfileClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(route = Screen.Transactions.route) {
            TransactionsScreen(
                onAddTransaction = {
                    navController.navigate(Screen.AddTransaction.route)
                },
                onTransactionClick = { transactionId ->
                    navController.navigate(
                        Screen.EditTransaction().createRoute(transactionId)
                    )
                }
            )
        }

        composable(route = Screen.Charts.route) {
            ChartsScreen()
        }

        composable(route = Screen.Insights.route) {
            InsightsScreen()
        }

        // ── Detail Screens ────────────────────────────────────────────────────
        composable(route = Screen.AddTransaction.route) {
            AddEditTransactionScreen(
                transactionId = null,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.EditTransaction.ROUTE,
            arguments = listOf(
                navArgument(Screen.EditTransaction.ARG) {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments
                ?.getLong(Screen.EditTransaction.ARG) ?: 0L
            AddEditTransactionScreen(
                transactionId = transactionId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Goal.route) {
            GoalScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}