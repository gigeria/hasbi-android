package com.hasbi.wallets.list

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hasbi.design.theme.HasbiTheme
import com.hasbi.domain.model.WalletWithBalance

/**
 * Wallets screen — total balance card + list of wallets with balances.
 *
 * Tap wallet → Wallet Detail
 * FAB → Add Wallet
 * Top-right icon → Transfer sheet
 */
@Composable
fun WalletsScreen(
    onWalletClick: (Long) -> Unit,
    onAddWalletClick: () -> Unit,
    onTransferClick: () -> Unit,
    viewModel: WalletsViewModel = hiltViewModel()
) {
    val wallets by viewModel.wallets.collectAsState()
    val totalBalance by viewModel.totalBalance.collectAsState()
    val colors = HasbiTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Wallets",
                    color = colors.textPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                // Transfer icon button
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.surface)
                        .clickable(onClick = onTransferClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CompareArrows,
                        contentDescription = "Transfer",
                        tint = colors.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Total balance card (gradient)
            TotalBalanceCard(
                totalBalance = totalBalance,
                walletCount = wallets.size,
                colors = colors
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Wallets list
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(wallets) { walletWithBalance ->
                    WalletCard(
                        walletWithBalance = walletWithBalance,
                        onClick = { onWalletClick(walletWithBalance.wallet.id) },
                        colors = colors
                    )
                }
            }
        }

        // FAB (Add Wallet)
        FloatingActionButton(
            onClick = onAddWalletClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = colors.primary,
            contentColor = Color.White,
            shape = RoundedCornerShape(18.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add Wallet"
            )
        }
    }
}

@Composable
private fun TotalBalanceCard(
    totalBalance: Long,
    walletCount: Int,
    colors: com.hasbi.design.theme.HasbiColorPalette
) {
    val formatter = remember { com.hasbi.util.money.MoneyFormatter() }
    val currency = com.hasbi.util.money.CurrencyInfo(
        code = "PKR",
        name = "Pakistani Rupee",
        symbol = "Rs.",
        minorUnitExponent = 2,
        decimalSeparator = ".",
        thousandsSeparator = ",",
        symbolPosition = com.hasbi.util.money.SymbolPosition.BEFORE,
        isDefault = true
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(colors.primary, colors.primaryLight)
                )
            )
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Total Balance",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatter.format(totalBalance, currency, com.hasbi.util.money.NumberFormatStyle.PAKISTANI),
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "across $walletCount ${if (walletCount == 1) "wallet" else "wallets"}",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.sp
                )
            }

            // Icon box
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "📊", fontSize = 20.sp)
            }
        }
    }
}

@Composable
private fun WalletCard(
    walletWithBalance: WalletWithBalance,
    onClick: () -> Unit,
    colors: com.hasbi.design.theme.HasbiColorPalette
) {
    val formatter = remember { com.hasbi.util.money.MoneyFormatter() }
    val currency = com.hasbi.util.money.CurrencyInfo(
        code = "PKR",
        name = "Pakistani Rupee",
        symbol = "Rs.",
        minorUnitExponent = 2,
        decimalSeparator = ".",
        thousandsSeparator = ",",
        symbolPosition = com.hasbi.util.money.SymbolPosition.BEFORE,
        isDefault = true
    )
    val wallet = walletWithBalance.wallet

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surface)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.backgroundWarm),
            contentAlignment = Alignment.Center
        ) {
            Text(text = wallet.icon, fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Name + subtitle
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = wallet.name,
                color = colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${wallet.currencyCode} · ${wallet.type.name.lowercase()}",
                color = colors.textSecondary,
                fontSize = 11.sp
            )
        }

        // Balance
        Text(
            text = formatter.format(walletWithBalance.balance, currency, com.hasbi.util.money.NumberFormatStyle.PAKISTANI),
            color = colors.textPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private inline fun <T> remember(crossinline calculation: () -> T): T =
    androidx.compose.runtime.remember(calculation)
