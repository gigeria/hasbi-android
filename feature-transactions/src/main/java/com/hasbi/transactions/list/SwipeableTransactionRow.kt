package com.hasbi.transactions.list

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hasbi.database.entity.TransactionType
import com.hasbi.design.theme.HasbiTheme
import com.hasbi.domain.model.Transaction
import kotlin.math.abs

/**
 * Swipeable transaction row — swipe left to delete, swipe right to edit.
 *
 * Shows red delete icon when swiped left, blue edit icon when swiped right.
 * Threshold: 150dp swipe triggers action.
 */
@Composable
fun SwipeableTransactionRow(
    transaction: Transaction,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    colors: com.hasbi.design.theme.HasbiColorPalette
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(targetValue = offsetX, label = "swipe")
    val threshold = 300f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surface)
    ) {
        // Background actions (revealed on swipe)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Edit (left side, revealed on swipe right)
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = "Edit",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            // Delete (right side, revealed on swipe left)
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Delete",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // Foreground content (draggable)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = animatedOffsetX.dp)
                .background(colors.surface)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            when {
                                offsetX < -threshold -> {
                                    onDelete()
                                    offsetX = 0f
                                }
                                offsetX > threshold -> {
                                    onEdit()
                                    offsetX = 0f
                                }
                                else -> {
                                    offsetX = 0f
                                }
                            }
                        }
                    ) { _, dragAmount ->
                        offsetX = (offsetX + dragAmount).coerceIn(-400f, 400f)
                    }
                }
                .clickable(onClick = onClick)
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            TransactionContent(transaction, colors)
        }
    }
}

@Composable
private fun TransactionContent(
    transaction: Transaction,
    colors: com.hasbi.design.theme.HasbiColorPalette
) {
    val isIncome = transaction.type == TransactionType.INCOME
    val amountColor = if (isIncome) colors.income else colors.textPrimary
    val amountSign = if (isIncome) "+" else "−"

    val (icon, bgColor) = when (transaction.type) {
        TransactionType.INCOME -> "💰" to colors.income.copy(alpha = 0.15f)
        TransactionType.TRANSFER -> "⇄" to colors.primary.copy(alpha = 0.15f)
        else -> "💸" to colors.expense.copy(alpha = 0.15f)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.note ?: "Transaction",
                color = colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = formatTime(transaction.date),
                color = colors.textSecondary,
                fontSize = 11.sp
            )
        }
        Text(
            text = "$amountSign Rs. ${formatAmount(transaction.amount)}",
            color = amountColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun formatTime(epochMillis: Long): String {
    val sdf = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(epochMillis))
}

private fun formatAmount(paisa: Long): String {
    val rs = paisa / 100
    return "%,d".format(rs)
}
