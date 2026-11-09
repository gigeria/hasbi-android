package com.hasbi.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hasbi.dashboard.components.AajKaBaqiCard
import com.hasbi.dashboard.components.BalanceSection
import com.hasbi.dashboard.components.IncomeExpenseStats
import com.hasbi.dashboard.components.RecentTransactions
import com.hasbi.dashboard.components.WalletMiniRow
import com.hasbi.design.icons.HasbiArchLogo
import com.hasbi.design.theme.HasbiTheme
import com.hasbi.util.money.CurrencyInfo
import com.hasbi.util.money.NumberFormatStyle
import com.hasbi.util.money.SymbolPosition

/**
 * Real Dashboard screen — Hasbi's home screen.
 *
 * Combines:
 * - Top bar (greeting + settings)
 * - Balance section (with eye toggle)
 * - Aaj Ka Baqi hero card (Daily Safe Spend)
 * - Income/Expense stats (2 cards)
 * - Wallet mini-row (horizontal scroll)
 * - Recent Transactions (5 latest)
 * - FAB (Quick Add — Week 7)
 *
 * Empty state shown when no transactions.
 */
@Composable
fun DashboardScreen(
    onQuickAddClick: () -> Unit = {},
    onSeeAllTransactions: () -> Unit = {},
    onSetBudgetClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isBalanceVisible by viewModel.isBalanceVisible.collectAsState()
    val colors = HasbiTheme.colors

    // Default currency (PKR)
    val currency = CurrencyInfo(
        code = "PKR",
        name = "Pakistani Rupee",
        symbol = "Rs.",
        minorUnitExponent = 2,
        decimalSeparator = ".",
        thousandsSeparator = ",",
        symbolPosition = SymbolPosition.BEFORE,
        isDefault = true
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        when (val state = uiState) {
            is DashboardUiState.Loading -> {
                LoadingState(colors = colors)
            }
            is DashboardUiState.Error -> {
                ErrorState(
                    message = state.message,
                    colors = colors
                )
            }
            is DashboardUiState.Success -> {
                DashboardContent(
                    state = state,
                    currency = currency,
                    colors = colors,
                    isBalanceVisible = isBalanceVisible,
                    onToggleBalance = viewModel::toggleBalanceVisibility,
                    onQuickAddClick = onQuickAddClick,
                    onSeeAllTransactions = onSeeAllTransactions,
                    onSetBudgetClick = onSetBudgetClick,
                    onSettingsClick = onSettingsClick
                )
            }
        }
    }
}

@Composable
private fun DashboardContent(
    state: DashboardUiState.Success,
    currency: CurrencyInfo,
    colors: com.hasbi.design.theme.HasbiColorPalette,
    isBalanceVisible: Boolean,
    onToggleBalance: () -> Unit,
    onQuickAddClick: () -> Unit,
    onSeeAllTransactions: () -> Unit,
    onSetBudgetClick: () -> Unit,
    onSettingsClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HasbiArchLogo(size = 36.dp, showArabic = false)
                    Column {
                        Text(
                            text = "Welcome back",
                            color = colors.textSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Today's Summary",
                            color = colors.primary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Settings",
                        tint = colors.textSecondary
                    )
                }
            }

            // Balance section
            BalanceSection(
                totalBalance = state.totalBalance,
                walletCount = state.wallets.size,
                isBalanceVisible = isBalanceVisible,
                currency = currency,
                onToggleVisibility = onToggleBalance
            )

            // Aaj Ka Baqi hero card
            AajKaBaqiCard(
                aajKaBaqi = state.aajKaBaqi,
                currency = currency,
                onSetBudgetClick = onSetBudgetClick
            )

            // Income + Expense stats
            IncomeExpenseStats(
                monthIncome = state.monthIncome,
                monthExpense = state.monthExpense,
                currency = currency
            )

            // Wallet mini-row (only if wallets exist)
            if (state.hasWallets) {
                Column {
                    Text(
                        text = "My Wallets",
                        color = colors.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    WalletMiniRow(
                        wallets = state.wallets,
                        currency = currency
                    )
                }
            }

            // Recent Transactions
            RecentTransactions(
                transactions = state.recentTransactions,
                currency = currency,
                onSeeAllClick = onSeeAllTransactions
            )

            // Bottom spacing for FAB
            Spacer(modifier = Modifier.height(80.dp))
        }

        // FAB (Quick Add)
        FloatingActionButton(
            onClick = onQuickAddClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = colors.primary,
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

@Composable
private fun LoadingState(colors: com.hasbi.design.theme.HasbiColorPalette) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = colors.primary)
    }
}

@Composable
private fun ErrorState(
    message: String,
    colors: com.hasbi.design.theme.HasbiColorPalette
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = colors.expense,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(32.dp)
        )
    }
}
