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
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hasbi.dashboard.DashboardScreen

/**
 * Top-level navigation host.
 *
 * Week 1: only Dashboard placeholder is real; rest are text placeholders.
 * Week 4+: real feature screens replace placeholders.
 *
 * Pattern: single Scaffold wraps NavHost. Bottom bar shown only on main routes.
 */
@Composable
fun HasbiNavHost(isOnboarded: Boolean) {
    val navController = rememberNavController()

    val startDestination = if (isOnboarded) Routes.DASHBOARD else Routes.ONBOARDING_WELCOME

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentRoute = currentDestination?.route

    val showBottomBar = currentRoute in mainRoutes

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
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            // === Onboarding ===
            composable(Routes.ONBOARDING_WELCOME) {
                PlaceholderScreen("Welcome — Hasbi mein khush amdeed!\n\n(Week 4)")
            }
            composable(Routes.ONBOARDING_WALLET) {
                PlaceholderScreen("Wallet Setup — Apne wallets choose karein\n\n(Week 4)")
            }
            composable(Routes.ONBOARDING_BUDGET) {
                PlaceholderScreen("Budget Setup — Apna budget set karein\n\n(Week 4)")
            }

            // === Main app (bottom nav) ===
            composable(Routes.DASHBOARD) {
                DashboardScreen()
            }
            composable(Routes.TRANSACTIONS) {
                PlaceholderScreen("Transactions List\n\n(Week 6)")
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
    }
}

private val mainRoutes = setOf(
    Routes.DASHBOARD,
    Routes.TRANSACTIONS,
    Routes.BUDGETS,
    Routes.WALLETS,
    Routes.MORE
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
        Text(text = text, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}
