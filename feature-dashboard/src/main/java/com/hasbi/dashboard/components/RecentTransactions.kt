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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hasbi.database.entity.TransactionType
import com.hasbi.design.theme.HasbiTheme
import com.hasbi.domain.model.Transaction
import com.hasbi.util.money.CurrencyInfo
import com.hasbi.util.money.MoneyFormatter
import com.hasbi.util.money.NumberFormatStyle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Recent Transactions list (5 latest) for Dashboard.
 *
 * Each item: icon, title, subtitle (wallet + time), amount with sign.
 */
@Composable
fun RecentTransactions(
    transactions: List<Transaction>,
    currency: CurrencyInfo,
    onSeeAllClick: () -> Unit
) {
    val colors = HasbiTheme.colors

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Transactions",
                color = colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "See all →",
                color = colors.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onSeeAllClick)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (transactions.isEmpty()) {
            EmptyTransactionsCard(colors = colors)
        } else {
            transactions.take(5).forEach { tx ->
                TransactionItem(
                    transaction = tx,
                    colors = colors,
                    currency = currency
                )
            }
        }
    }
}

@Composable
private fun TransactionItem(
    transaction: Transaction,
    colors: com.hasbi.design.theme.HasbiColorPalette,
    currency: CurrencyInfo
) {
    val formatter = remember { MoneyFormatter() }
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    val isIncome = transaction.type == TransactionType.INCOME
    val amountColor = if (isIncome) colors.income else colors.textPrimary
    val amountSign = if (isIncome) "+" else "−"

    // Determine icon + bg color based on transaction type
    val (icon, bgColor) = when (transaction.type) {
        TransactionType.INCOME -> "💰" to colors.income.copy(alpha = 0.15f)
        TransactionType.TRANSFER -> "⇄" to colors.primary.copy(alpha = 0.15f)
        else -> "💸" to colors.expense.copy(alpha = 0.15f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title + subtitle
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = transaction.note ?: "Transaction",
                color = colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = timeFormat.format(Date(transaction.date)),
                color = colors.textSecondary,
                fontSize = 11.sp
            )
        }

        // Amount
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "$amountSign${formatter.format(transaction.amount, currency, NumberFormatStyle.PAKISTANI)}",
                color = amountColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EmptyTransactionsCard(colors: com.hasbi.design.theme.HasbiColorPalette) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surface)
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📝",
                fontSize = 32.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Abhi tak koi transaction nahi",
                color = colors.textSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Pehla kharch add karein",
                color = colors.textTertiary,
                fontSize = 11.sp
            )
        }
    }
}

// Helpers
@Composable
private inline fun <T> remember(crossinline calculation: () -> T): T =
    androidx.compose.runtime.remember(calculation)

private fun Modifier.clickable(onClick: () -> Unit): Modifier =
    this.then(androidx.compose.foundation.clickable(onClick = onClick))
