package com.hasbi.debts.list

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hasbi.database.relation.DebtWithRemaining
import com.hasbi.design.theme.HasbiTheme

@Composable
fun DebtsScreen(
    onAddDebtClick: () -> Unit,
    onDebtClick: (Long) -> Unit,
    viewModel: DebtsViewModel = hiltViewModel()
) {
    val lentDebts by viewModel.lentDebts.collectAsState()
    val borrowedDebts by viewModel.borrowedDebts.collectAsState()
    val totalLent by viewModel.totalLent.collectAsState()
    val totalBorrowed by viewModel.totalBorrowed.collectAsState()
    val colors = HasbiTheme.colors

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Top bar
            Text(
                text = "Udhaar",
                color = colors.textPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Summary cards (Lent + Borrowed)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryCard(
                    label = "Maine Diya (Lent)",
                    amount = totalLent,
                    icon = "↗",
                    color = colors.expense,
                    bgColor = colors.expense.copy(alpha = 0.15f),
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
                SummaryCard(
                    label = "Mujhe Mila (Borrowed)",
                    amount = totalBorrowed,
                    icon = "↘",
                    color = colors.income,
                    bgColor = colors.income.copy(alpha = 0.15f),
                    colors = colors,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lists
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (lentDebts.isNotEmpty()) {
                    item {
                        SectionHeader(text = "Maine Diya (Lent)", colors = colors)
                    }
                    items(lentDebts) { debt ->
                        DebtCard(
                            debt = debt,
                            onClick = { onDebtClick(debt.debt.id) },
                            colors = colors
                        )
                    }
                }

                if (borrowedDebts.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        SectionHeader(text = "Mujhe Mila (Borrowed)", colors = colors)
                    }
                    items(borrowedDebts) { debt ->
                        DebtCard(
                            debt = debt,
                            onClick = { onDebtClick(debt.debt.id) },
                            colors = colors
                        )
                    }
                }

                if (lentDebts.isEmpty() && borrowedDebts.isEmpty()) {
                    item {
                        EmptyDebtsState(onAddClick = onAddDebtClick, colors = colors)
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = onAddDebtClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = colors.primary,
            contentColor = Color.White,
            shape = RoundedCornerShape(18.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add Debt"
            )
        }
    }
}

@Composable
private fun SummaryCard(
    label: String,
    amount: Long,
    icon: String,
    color: Color,
    bgColor: Color,
    colors: com.hasbi.design.theme.HasbiColorPalette,
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
                        .background(bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = icon, color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
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
                text = "Rs. ${formatLong(amount)}",
                color = color,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SectionHeader(
    text: String,
    colors: com.hasbi.design.theme.HasbiColorPalette
) {
    Text(
        text = text,
        color = colors.textSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
    )
}

@Composable
private fun DebtCard(
    debt: DebtWithRemaining,
    onClick: () -> Unit,
    colors: com.hasbi.design.theme.HasbiColorPalette
) {
    val isLent = debt.type == com.hasbi.database.entity.DebtType.LENT
    val amountColor = if (isLent) colors.expense else colors.income
    val avatarColor = if (isLent) colors.expense.copy(alpha = 0.15f) else colors.income.copy(alpha = 0.15f)
    val firstLetter = debt.personName.firstOrNull()?.uppercase() ?: "?"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surface)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(avatarColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = firstLetter,
                color = amountColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Name + date
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = debt.personName,
                color = colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
            Text(
                text = "Diya: ${sdf.format(java.util.Date(debt.debt.startDate))}",
                color = colors.textSecondary,
                fontSize = 11.sp
            )
        }

        // Remaining amount
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "Rs. ${formatLong(debt.remainingAmount)}",
                color = amountColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (debt.isSettled) "Settled ✓" else "baaki",
                color = if (debt.isSettled) colors.income else colors.textTertiary,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun EmptyDebtsState(
    onAddClick: () -> Unit,
    colors: com.hasbi.design.theme.HasbiColorPalette
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "🤝", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Abhi koi udhaar record nahi",
                color = colors.textSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Kisi ko paisay diye ya liye?\nYahan record karein.",
                color = colors.textTertiary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

private fun formatLong(paisa: Long): String {
    val rs = paisa / 100
    return "%,d".format(rs)
}
