package com.hasbi.budgets.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.hasbi.database.relation.BudgetWithSpent
import com.hasbi.design.theme.HasbiTheme
import com.hasbi.util.money.CurrencyInfo
import com.hasbi.util.money.MoneyFormatter
import com.hasbi.util.money.NumberFormatStyle

/**
 * Category budget bar — shows one category's budget + spent.
 *
 * Color-coded:
 * - Green: < 80% used (on track)
 * - Amber: 80-95% used (warning)
 * - Red: >= 95% used (critical)
 */
@Composable
fun CategoryBudgetBar(
    budget: BudgetWithSpent,
    categoryName: String,
    categoryIcon: String,
    currency: CurrencyInfo
) {
    val colors = HasbiTheme.colors
    val formatter = MoneyFormatter()

    val progressColor = when {
        budget.isOverBudget -> colors.expense
        budget.isWarning -> colors.warning
        else -> colors.income
    }

    val progress = (budget.spent.toFloat() / budget.limit.toFloat()).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surface)
            .padding(14.dp)
    ) {
        Column {
            // Header: name + spent/limit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = categoryIcon, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = categoryName,
                        color = colors.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    text = "Rs. ${formatLong(budget.spent)} / ${formatLong(budget.limit)}",
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(colors.borderSoft)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(5.dp)
                        .clip(RoundedCornerShape(100.dp))
                        .background(progressColor)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Footer: percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${(progress * 100).toInt()}% used",
                    color = progressColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
                val remainingText = if (budget.remaining >= 0) {
                    "Rs. ${formatLong(budget.remaining)} baaki"
                } else {
                    "Rs. ${formatLong(-budget.remaining)} over"
                }
                Text(
                    text = remainingText,
                    color = if (budget.remaining >= 0) colors.textTertiary else colors.expense,
                    fontSize = 10.sp
                )
            }
        }
    }
}

private fun formatLong(paisa: Long): String {
    val rs = paisa / 100
    return "%,d".format(rs)
}
