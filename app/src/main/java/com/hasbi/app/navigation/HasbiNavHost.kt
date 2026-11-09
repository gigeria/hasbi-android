package com.hasbi.app.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hasbi.budgets.BudgetsScreen
import com.hasbi.dashboard.DashboardScreen
import com.hasbi.debts.add.AddDebtScreen
import com.hasbi.debts.list.DebtsScreen
import com.hasbi.onboarding.OnboardingNavHost
import com.hasbi.transactions.detail.TransactionDetailSheet
import com.hasbi.transactions.list.TransactionsListScreen
import com.hasbi.transactions.quickadd.QuickAddSheet
import com.hasbi.wallets.add.AddWalletScreen
import com.hasbi.wallets.list.WalletsScreen

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
    var showQuickAdd by remember { mutableStateOf(false) }

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
                FloatingActionButton(
                    onClick = { showQuickAdd = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
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
                    onSeeAllTransactions = { navController.navigate(Routes.TRANSACTIONS) },
                    onSetBudgetClick = { navController.navigate(Routes.BUDGETS) },
                    onSettingsClick = { navController.navigate("settings") }
                )
            }
            composable(Routes.TRANSACTIONS) {
                TransactionsListScreen(
                    onTransactionClick = { txId ->
                        navController.navigate("edit-transaction/$txId")
                        // Show detail sheet (handled by state in Week 7)
                        // For now, no-op
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Routes.BUDGETS) {
                BudgetsScreen(
                    onAddBudgetClick = { /* TODO: Add budget screen */ }
                )
            }
            composable(Routes.WALLETS) {
                WalletsScreen(
                    onWalletClick = { walletId ->
                        // TODO: Wallet detail screen
                    },
                    onAddWalletClick = { navController.navigate("wallet/add") },
                    onTransferClick = { /* TODO: Transfer sheet */ }
                )
            }
            composable("wallet/add") {
                AddWalletScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }
            composable(Routes.MORE) {
                MoreMenuScreen(
                    onDebtsClick = { navController.navigate("debts") },
                    onSettingsClick = { navController.navigate("settings") },
                    onBackupClick = { navController.navigate("backup") },
                    onAboutClick = { navController.navigate("about") },
                    onReportsClick = { navController.navigate("reports") },
                    onExportClick = { navController.navigate("export") },
                    onProjectsClick = { navController.navigate("projects") }
                )
            }
            composable("debts") {
                DebtsScreen(
                    onAddDebtClick = { navController.navigate("debt/add") },
                    onDebtClick = { /* TODO: Debt detail */ }
                )
            }
            composable("debt/add") {
                AddDebtScreen(
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }
            composable("settings") {
                com.hasbi.settings.SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onCategoriesClick = { navController.navigate("categories") },
                    onProjectsClick = { navController.navigate("projects") },
                    onBackupClick = { navController.navigate("backup") },
                    onAboutClick = { navController.navigate("about") }
                )
            }
            composable("categories") {
            composable("projects") {
                com.hasbi.settings.projects.ProjectsScreen(
                    onBack = { navController.popBackStack() }
                )
            }
                com.hasbi.settings.categories.CategoriesScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable("about") {
                com.hasbi.settings.AboutScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable("backup") {
                com.hasbi.backup.BackupScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable("edit-transaction/{txId}") { backStackEntry ->
                val txId = backStackEntry.arguments?.getString("txId")?.toLongOrNull() ?: 0L
                com.hasbi.transactions.quickadd.EditTransactionScreen(
                    transactionId = txId,
                    onBack = { navController.popBackStack() },
                    onSaved = { navController.popBackStack() }
                )
            }
            composable("export") {
                com.hasbi.transactions.export.ExportScreen(
                    onBack = { navController.popBackStack() }
                )
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

@Composable
private fun MoreMenuScreen(
    onDebtsClick: () -> Unit,
    onSettingsClick: () -> Unit = {},
    onBackupClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    onExportClick: () -> Unit = {},
    onProjectsClick: () -> Unit = {},
    onReportsClick: () -> Unit = {}
) {
    val colors = com.hasbi.design.theme.HasbiTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = "More",
            color = colors.textPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Menu items
        MoreMenuItem(
            icon = "🤝",
            title = "Debt / Udhaar",
            subtitle = "Lent & borrowed records",
            onClick = onDebtsClick,
            colors = colors
        )
        MoreMenuItem(
            icon = "📊",
            title = "Export",
            subtitle = "CSV / PDF report",
            onClick = onExportClick,
            colors = colors
        )
        MoreMenuItem(
            icon = "⚙️",
            title = "Settings",
            subtitle = "Theme, language, security",
            onClick = onSettingsClick,
            colors = colors
        )
        MoreMenuItem(
            icon = "💾",
            title = "Backup & Restore",
            subtitle = "Encrypted local backup",
            onClick = onBackupClick,
            colors = colors
        )
        MoreMenuItem(
            icon = "🔒",
            title = "Security",
            subtitle = "Biometric lock (Coming soon)",
            onClick = { },
            colors = colors
        )
        MoreMenuItem(
            icon = "ℹ️",
            title = "About Hasbi",
            subtitle = "Mera Hisab, Mera Yaqeen",
            onClick = onAboutClick,
            colors = colors
        )
    }
}

@Composable
private fun MoreMenuItem(
    icon: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    colors: com.hasbi.design.theme.HasbiColorPalette
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surface)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.backgroundWarm),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                color = colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = colors.textSecondary,
                fontSize = 11.sp
            )
        }
    }
}
