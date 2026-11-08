package com.hasbi.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
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

/**
 * Income + Expense stat chips (2 cards side-by-side).
 *
 * Used below Aaj Ka Baqi card.
 */
@Composable
fun IncomeExpenseStats(
    monthIncome: Long,
    monthExpense: Long,
    currency: CurrencyInfo
) {
    val colors = HasbiTheme.colors
    val formatter = remember { MoneyFormatter() }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Income card
        StatCard(
            label = "This month income",
            amount = monthIncome,
            icon = Icons.Filled.ArrowUpward,
            iconColor = colors.income,
            iconBgColor = colors.income.copy(alpha = 0.15f),
            amountColor = colors.income,
            colors = colors,
            formatter = formatter,
            currency = currency,
            modifier = Modifier.weight(1f)
        )

        // Expense card
        StatCard(
            label = "This month spend",
            amount = monthExpense,
            icon = Icons.Filled.ArrowDownward,
            iconColor = colors.expense,
            iconBgColor = colors.expense.copy(alpha = 0.15f),
            amountColor = colors.expense,
            colors = colors,
            formatter = formatter,
            currency = currency,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    amount: Long,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    iconBgColor: Color,
    amountColor: Color,
    colors: com.hasbi.design.theme.HasbiColorPalette,
    formatter: MoneyFormatter,
    currency: CurrencyInfo,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surface)
            .padding(14.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    color = colors.textSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatter.format(amount, currency, NumberFormatStyle.PAKISTANI),
                color = amountColor,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp
            )
        }
    }
}

// Remember helper
@Composable
private inline fun <T> remember(crossinline calculation: () -> T): T =
    androidx.compose.runtime.remember(calculation)
