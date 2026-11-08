package com.hasbi.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hasbi.dashboard.DashboardScreen
import com.hasbi.onboarding.OnboardingNavHost
import com.hasbi.transactions.detail.TransactionDetailSheet
import com.hasbi.transactions.list.TransactionsListScreen
import com.hasbi.transactions.quickadd.QuickAddSheet

/**
 * Top-level navigation host.
 *
 * Routes:
 *   - Onboarding (3 screens): Welcome → Wallet → Budget → Dashboard
 *   - Main app (5-tab bottom nav): Home, Transactions, Budgets, Wallets, More
 */
@Composable
fun HasbiNavHost(isOnboarded: Boolean) {
    val navController = rememberNavController()

    val startDestination = if (isOnboarded) Routes.DASHBOARD else Routes.ONBOARDING_WELCOME

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val showBottomBar = currentRoute in mainRoutes
    val showFab = currentRoute in fabRoutes

    // Quick Add sheet state
    var showQuickAdd by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentDestination?.hierarchy?.any {
                            it.route == item.route
                        } == true

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = {
                                Text(
                                    text = item.label,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (showFab) {
                androidx.compose.material3.FloatingActionButton(
                    onClick = { showQuickAdd = true },
                    containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                    contentColor = androidx.compose.ui.graphics.Color.White,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = androidx.compose.material.icons.Icons.Filled.Add,
                        contentDescription = "Quick Add"
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            // === Onboarding ===
            composable(Routes.ONBOARDING_WELCOME) {
                OnboardingNavHost(
                    onComplete = {
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.ONBOARDING_WELCOME) { inclusive = true }
                        }
                    }
                )
            }

            // === Main app (bottom nav) ===
            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    onQuickAddClick = { showQuickAdd = true },
                    onSeeAllTransactions = { navController.navigate(Routes.TRANSACTIONS) }
                )
            }
            composable(Routes.TRANSACTIONS) {
                TransactionsListScreen(
                    onTransactionClick = { txId ->
                        // Show detail sheet (handled by state in Week 7)
                        // For now, no-op
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Routes.BUDGETS) {
                PlaceholderScreen("Budgets — Cycle + Categories\n\n(Week 9)")
            }
            composable(Routes.WALLETS) {
                PlaceholderScreen("Wallets — Cash, Bank, JazzCash, Easypaisa\n\n(Week 8)")
            }
            composable(Routes.MORE) {
                PlaceholderScreen("More — Settings, Backup, About\n\n(Week 11)")
            }
        }

        // Quick Add bottom sheet
        if (showQuickAdd) {
            QuickAddSheet(
                onDismiss = { showQuickAdd = false }
            )
        }
    }
}

private val mainRoutes = setOf(
    Routes.DASHBOARD,
    Routes.TRANSACTIONS,
    Routes.BUDGETS,
    Routes.WALLETS,
    Routes.MORE
)

private val fabRoutes = setOf(
    Routes.DASHBOARD,
    Routes.TRANSACTIONS,
    Routes.WALLETS
)

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val bottomNavItems = listOf(
    BottomNavItem(Routes.DASHBOARD, "Home", Icons.Outlined.Home),
    BottomNavItem(Routes.TRANSACTIONS, "Transactions", Icons.Outlined.ReceiptLong),
    BottomNavItem(Routes.BUDGETS, "Budgets", Icons.Outlined.BarChart),
    BottomNavItem(Routes.WALLETS, "Wallets", Icons.Outlined.AccountBalanceWallet),
    BottomNavItem(Routes.MORE, "More", Icons.Outlined.MoreHoriz)
)

@Composable
private fun PlaceholderScreen(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, textAlign = TextAlign.Center)
    }
}
