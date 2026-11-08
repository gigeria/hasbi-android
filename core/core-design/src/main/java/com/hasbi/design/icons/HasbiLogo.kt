package com.hasbi.design.icons

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hasbi.design.theme.HasbiTheme

/**
 * Hasbi arch-shaped logo with Arabic calligraphy حسبي inside.
 *
 * The arch represents Islamic architectural element (mosque arch).
 * Gold border on emerald gradient = premium Islamic identity.
 *
 * Used on: Splash, Onboarding Welcome, App icon
 */
@Composable
fun HasbiArchLogo(
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    showArabic: Boolean = true
) {
    val colors = HasbiTheme.colors

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(topStart = size / 2, topEnd = size / 2, bottomStart = size / 6, bottomEnd = size / 6))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.White, Color(0xFFF5F1E8))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Gold border drawn via Canvas
        Canvas(modifier = Modifier.size(size)) {
            val canvasWidth = this.size.width
            val canvasHeight = this.size.height
            val strokePx = (size.toPx() * 0.04f).coerceAtLeast(2f)

            // Arch path: top semicircle + rectangular bottom
            val archPath = Path().apply {
                val archRadius = canvasWidth / 2 - strokePx
                val archCenterY = archRadius + strokePx
                val bottomY = canvasHeight - strokePx

                moveTo(strokePx, bottomY)
                lineTo(strokePx, archCenterY)
                arcTo(
                    rect = androidx.compose.ui.geometry.Rect(
                        left = strokePx,
                        top = strokePx,
                        right = canvasWidth - strokePx,
                        bottom = archCenterY + archRadius
                    ),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = 180f,
                    forceMoveTo = false
                )
                lineTo(canvasWidth - strokePx, bottomY)
                close()
            }

            drawPath(
                path = archPath,
                color = colors.primaryGold,
                style = Stroke(width = strokePx)
            )

            // Subtle inner accent line
            val innerPadding = strokePx * 2.5f
            val innerArchPath = Path().apply {
                val archRadius = canvasWidth / 2 - innerPadding
                val archCenterY = archRadius + innerPadding
                val bottomY = canvasHeight - innerPadding

                moveTo(innerPadding, bottomY)
                lineTo(innerPadding, archCenterY)
                arcTo(
                    rect = androidx.compose.ui.geometry.Rect(
                        left = innerPadding,
                        top = innerPadding,
                        right = canvasWidth - innerPadding,
                        bottom = archCenterY + archRadius
                    ),
                    startAngleDegrees = 180f,
                    sweepAngleDegrees = 180f,
                    forceMoveTo = false
                )
                lineTo(canvasWidth - innerPadding, bottomY)
                close()
            }
            drawPath(
                path = innerArchPath,
                color = colors.primaryGold.copy(alpha = 0.3f),
                style = Stroke(width = strokePx * 0.5f)
            )
        }

        // Arabic calligraphy حسبي in center
        if (showArabic) {
            Text(
                text = "حسبي",
                color = colors.primary,
                fontSize = (size.value * 0.32f).sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = size * 0.1f)
            )
        }
    }
}

/**
 * Compact Hasbi logo — letter H inside emerald rounded square.
 * Used on: app icon, FAB, splash, About screen.
 */
@Composable
fun HasbiCompactLogo(
    modifier: Modifier = Modifier,
    size: Dp = 56.dp
) {
    val colors = HasbiTheme.colors

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.28f))
            .background(
                Brush.verticalGradient(
                    colors = listOf(colors.primary, colors.primaryLight)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "ح",
            color = Color.White,
            fontSize = (size.value * 0.55f).sp,
            fontWeight = FontWeight.Bold
        )
    }
}
