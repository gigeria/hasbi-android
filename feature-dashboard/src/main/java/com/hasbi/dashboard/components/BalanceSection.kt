package com.hasbi.dashboard.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hasbi.design.theme.HasbiTheme
import com.hasbi.util.money.CurrencyInfo
import com.hasbi.util.money.MoneyFormatter
import com.hasbi.util.money.NumberFormatStyle
import com.hasbi.util.money.SymbolPosition

/**
 * Total Balance card with eye toggle (hide/show balance for privacy).
 *
 * Used at top of Dashboard.
 */
@Composable
fun BalanceSection(
    totalBalance: Long,
    walletCount: Int,
    isBalanceVisible: Boolean,
    currency: CurrencyInfo,
    onToggleVisibility: () -> Unit
) {
    val colors = HasbiTheme.colors
    val formatter = remember { MoneyFormatter() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.surface)
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Total Balance",
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))

                AnimatedContent(
                    targetState = isBalanceVisible,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "balance_visibility"
                ) { visible ->
                    Text(
                        text = if (visible) {
                            formatter.format(totalBalance, currency, NumberFormatStyle.PAKISTANI)
                        } else {
                            "Rs. ••••••"
                        },
                        color = colors.textPrimary,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "across $walletCount ${if (walletCount == 1) "wallet" else "wallets"}",
                    color = colors.textSecondary,
                    fontSize = 11.sp
                )
            }

            // Eye toggle button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(colors.backgroundWarm)
                    .clickable(onClick = onToggleVisibility),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isBalanceVisible) {
                        Icons.Filled.Visibility
                    } else {
                        Icons.Filled.VisibilityOff
                    },
                    contentDescription = if (isBalanceVisible) "Hide balance" else "Show balance",
                    tint = colors.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// Remember helper — using androidx.compose.runtime.remember inline
@Composable
private inline fun <T> remember(crossinline calculation: () -> T): T =
    androidx.compose.runtime.remember(calculation)
