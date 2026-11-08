package com.hasbi.budgets.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hasbi.database.relation.BudgetWithSpent
import com.hasbi.design.theme.HasbiTheme
import com.hasbi.util.money.CurrencyInfo
import com.hasbi.util.money.MoneyFormatter
import com.hasbi.util.money.NumberFormatStyle
import com.hasbi.util.money.SymbolPosition

/**
 * Cycle card with circular progress ring showing overall budget usage.
 *
 * - Circular ring: spent / limit (capped at 100%)
 * - Spent amount (large)
 * - Limit + remaining
 * - Cycle type pill
 */
@Composable
fun CycleCard(
    budget: BudgetWithSpent?,
    cycleName: String,
    cycleType: String,
    currency: CurrencyInfo
) {
    val colors = HasbiTheme.colors
    val formatter = MoneyFormatter()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.surface)
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular progress ring
            val progress = if (budget != null) {
                (budget.spent.toFloat() / budget.limit.toFloat()).coerceIn(0f, 1f)
            } else 0f

            CircularProgressRing(
                progress = progress,
                color = if (budget?.isOverBudget == true) colors.expense
                        else if (budget?.isWarning == true) colors.warning
                        else colors.primary,
                trackColor = colors.borderSoft,
                size = 90.dp,
                strokeWidth = 8.dp
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        color = colors.primary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "used",
                        color = colors.textSecondary,
                        fontSize = 9.sp
                    )
                }
            }

            // Cycle info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = cycleName,
                        color = colors.textPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(colors.backgroundWarm)
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = cycleType,
                            color = colors.textSecondary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (budget != null) {
                    Text(
                        text = formatter.format(budget.spent, currency, NumberFormatStyle.PAKISTANI),
                        color = colors.textPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "of ${formatter.format(budget.limit, currency, NumberFormatStyle.PAKISTANI)}",
                        color = colors.textSecondary,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    val remainingText = if (budget.remaining >= 0) {
                        "Rs. ${formatLong(budget.remaining)} baaki"
                    } else {
                        "Rs. ${formatLong(-budget.remaining)} over"
                    }
                    Text(
                        text = remainingText,
                        color = if (budget.remaining >= 0) colors.income else colors.expense,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Text(
                        text = "No budget set",
                        color = colors.textSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Tap + to add budget",
                        color = colors.textTertiary,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun CircularProgressRing(
    progress: Float,
    color: Color,
    trackColor: Color,
    size: androidx.compose.ui.unit.Dp,
    strokeWidth: androidx.compose.ui.unit.Dp,
    content: @Composable () -> Unit
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 600),
        label = "ring_progress"
    )

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val diameter = this.size.minDimension - strokePx
            val topLeft = Offset(
                x = (this.size.width - diameter) / 2,
                y = (this.size.height - diameter) / 2
            )
            val arcSize = Size(diameter, diameter)

            // Track (background circle)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx)
            )

            // Progress arc
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx)
            )
        }
        content()
    }
}

private fun formatLong(paisa: Long): String {
    val rs = paisa / 100
    return "%,d".format(rs)
}
