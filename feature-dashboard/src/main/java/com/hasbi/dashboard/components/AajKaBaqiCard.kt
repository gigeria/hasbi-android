package com.hasbi.dashboard.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hasbi.design.theme.HasbiTheme
import com.hasbi.domain.model.DailySafeSpend
import com.hasbi.domain.model.WarningLevel
import com.hasbi.util.money.CurrencyInfo
import com.hasbi.util.money.MoneyFormatter
import com.hasbi.util.money.NumberFormatStyle

/**
 * Aaj Ka Baqi card — Hasbi's hero feature.
 *
 * Shows:
 * - Daily Safe Spend (kitna kharch kar sakte hain aaj)
 * - Progress bar (cycle progress)
 * - Projection (on track / over budget)
 * - Friendly message (never guilt-trip)
 *
 * If no budget: shows "Set Budget" CTA.
 */
@Composable
fun AajKaBaqiCard(
    aajKaBaqi: DailySafeSpend?,
    currency: CurrencyInfo,
    onSetBudgetClick: () -> Unit
) {
    val colors = HasbiTheme.colors
    val formatter = remember { MoneyFormatter() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(colors.primary, colors.primaryLight)
                )
            )
            .padding(22.dp)
    ) {
        if (aajKaBaqi == null || !aajKaBaqi.hasBudget) {
            // No budget state — show CTA
            NoBudgetState(
                colors = colors,
                onSetBudgetClick = onSetBudgetClick
            )
        } else {
            // Budget configured — show Aaj Ka Baqi
            BudgetActiveState(
                baqi = aajKaBaqi,
                colors = colors,
                formatter = formatter,
                currency = currency
            )
        }
    }
}

@Composable
private fun NoBudgetState(
    colors: com.hasbi.design.theme.HasbiColorPalette,
    onSetBudgetClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "AAJ KA BAQI",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Apna budget set karein",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Taake Hasbi aap ko daily guide kar sake.",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Set Budget button (clickable card)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(100.dp))
                .background(colors.primaryGold)
                .clickable(onClick = onSetBudgetClick)
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Text(
                text = "Set Budget →",
                color = colors.primaryDark,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun BudgetActiveState(
    baqi: DailySafeSpend,
    colors: com.hasbi.design.theme.HasbiColorPalette,
    formatter: MoneyFormatter,
    currency: CurrencyInfo
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Top row: label + days remaining pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AAJ KA BAQI",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(Color.White.copy(alpha = 0.18f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${baqi.remainingDays} days left",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Amount (big)
        val displayAmount = if (baqi.isOverBudget) 0L else baqi.dailySafeSpend
        Text(
            text = formatter.format(displayAmount, currency, NumberFormatStyle.PAKISTANI),
            color = Color.White,
            fontSize = 44.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-1).sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Friendly message
        val message = when (baqi.warningLevel) {
            WarningLevel.GREEN -> "Aaj itna kharch kar sakte hain"
            WarningLevel.AMBER -> "Sambhal ke kharch karein"
            WarningLevel.RED -> "Budget exceed ho gaya"
        }
        Text(
            text = message,
            color = Color.White.copy(alpha = 0.92f),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Progress bar
        val progress by animateFloatAsState(
            targetValue = calculateProgress(baqi),
            animationSpec = tween(durationMillis = 600),
            label = "progress"
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(100.dp))
                .background(Color.White.copy(alpha = 0.18f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .height(6.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(colors.primaryGold, colors.accent)
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Footer: spent + projection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Spent ${formatter.format(baqi.cycleSpent, currency, NumberFormatStyle.PAKISTANI)} of ${formatter.format(baqi.totalBudget, currency, NumberFormatStyle.PAKISTANI)}",
                color = Color.White.copy(alpha = 0.88f),
                fontSize = 11.sp
            )

            val projectionText = if (baqi.projectedEndOfMonth >= 0) {
                "On track ✓"
            } else {
                "Over budget"
            }
            val projectionColor = if (baqi.projectedEndOfMonth >= 0) {
                Color.White.copy(alpha = 0.92f)
            } else {
                colors.expense
            }
            Text(
                text = projectionText,
                color = projectionColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Calculate progress for the bar (0..1).
 *
 * If over budget, progress stays at 1 (full bar).
 * Otherwise: spent / totalBudget.
 */
private fun calculateProgress(baqi: DailySafeSpend): Float {
    return if (baqi.totalBudget <= 0) 0f
    else (baqi.cycleSpent.toFloat() / baqi.totalBudget.toFloat()).coerceIn(0f, 1f)
}

// Add click modifier
private fun Modifier.clickable(
    onClick: () -> Unit
): Modifier = this.then(androidx.compose.foundation.clickable(onClick = onClick))
