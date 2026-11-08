package com.hasbi.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hasbi.design.theme.HasbiTheme
import com.hasbi.domain.model.WalletWithBalance
import com.hasbi.util.money.CurrencyInfo
import com.hasbi.util.money.MoneyFormatter
import com.hasbi.util.money.NumberFormatStyle

/**
 * Horizontal scrollable row of wallet mini-cards.
 *
 * Shows on Dashboard below income/expense stats.
 * Each card: icon, name, balance.
 */
@Composable
fun WalletMiniRow(
    wallets: List<WalletWithBalance>,
    currency: CurrencyInfo
) {
    val colors = HasbiTheme.colors
    val formatter = remember { MoneyFormatter() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 0.dp)
    ) {
        wallets.forEach { walletWithBalance ->
            WalletMiniCard(
                name = walletWithBalance.wallet.name,
                icon = walletWithBalance.wallet.icon,
                balance = walletWithBalance.balance,
                colors = colors,
                formatter = formatter,
                currency = currency
            )
        }
    }
}

@Composable
private fun WalletMiniCard(
    name: String,
    icon: String,
    balance: Long,
    colors: com.hasbi.design.theme.HasbiColorPalette,
    formatter: MoneyFormatter,
    currency: CurrencyInfo
) {
    Box(
        modifier = Modifier
            .width(110.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column {
            Text(
                text = icon,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = name,
                color = colors.textSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = formatter.format(balance, currency, NumberFormatStyle.PAKISTANI),
                color = colors.textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private inline fun <T> remember(crossinline calculation: () -> T): T =
    androidx.compose.runtime.remember(calculation)
