package com.hasbi.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hasbi.database.entity.CycleType
import com.hasbi.design.theme.HasbiTheme

/**
 * Budget setup screen — third screen of onboarding.
 *
 * Features:
 * - Monthly budget input (optional, default 0)
 * - Budget cycle: Monthly / Salary Date / Weekly / Custom
 * - Starting day selector (1-31)
 * - Skip → default monthly cycle, no budget → Dashboard
 * - Start using Hasbi → complete onboarding (atomic)
 */
@Composable
fun BudgetSetupScreen(
    cycleType: CycleType,
    cycleStartDay: Int,
    onCycleTypeChange: (CycleType) -> Unit,
    onCycleStartDayChange: (Int) -> Unit,
    onBudgetChange: (Long) -> Unit,
    onContinue: () -> Unit,
    onSkip: () -> Unit,
    onBack: () -> Unit
) {
    val colors = HasbiTheme.colors
    var budgetText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.textPrimary
                )
            }
            TextButton(onClick = onSkip) {
                Text(
                    text = "Skip →",
                    color = colors.textSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Progress dots
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(3) { index ->
                val isActive = index == 2
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(if (isActive) 24.dp else 8.dp, 8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isActive) colors.primary else colors.border)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Title
        Text(
            text = "Apna budget set karein",
            color = colors.textPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Optional hai. Baad mein bhi set kar sakte hain.",
            color = colors.textSecondary,
            fontSize = 13.sp,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Monthly Budget input
        Text(
            text = "Monthly Budget",
            color = colors.textSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
            value = budgetText,
            onValueChange = { newValue ->
                // Allow only digits
                val cleaned = newValue.filter { it.isDigit() }
                budgetText = cleaned
                // Convert to paisa (user enters Rs, store as paisa)
                val amountInRs = cleaned.toLongOrNull() ?: 0L
                onBudgetChange(amountInRs * 100)
            },
            placeholder = {
                Text(text = "e.g. 50000", color = colors.textTertiary)
            },
            prefix = {
                Text(text = "Rs. ", color = colors.textSecondary, fontWeight = FontWeight.Medium)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Budget Cycle
        Text(
            text = "Budget Cycle",
            color = colors.textSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CycleOptionChip(
                label = "Monthly",
                isSelected = cycleType == CycleType.MONTHLY,
                onClick = { onCycleTypeChange(CycleType.MONTHLY) },
                colors = colors
            )
            CycleOptionChip(
                label = "Salary Date",
                isSelected = cycleType == CycleType.SALARY_DATE,
                onClick = { onCycleTypeChange(CycleType.SALARY_DATE) },
                colors = colors
            )
            CycleOptionChip(
                label = "Weekly",
                isSelected = cycleType == CycleType.WEEKLY,
                onClick = { onCycleTypeChange(CycleType.WEEKLY) },
                colors = colors
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Starting day (only for monthly/salary)
        if (cycleType == CycleType.MONTHLY || cycleType == CycleType.SALARY_DATE) {
            Text(
                text = if (cycleType == CycleType.SALARY_DATE) "Salary Date" else "Starting From",
                color = colors.textSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Day selector chips (1, 5, 10, 15, 20, 25, 1st)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(1, 5, 10, 15, 20, 25).forEach { day ->
                    DayChip(
                        day = day,
                        isSelected = cycleStartDay == day,
                        onClick = { onCycleStartDayChange(day) },
                        colors = colors
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Cycle ${cycleStartDay} se start hoga. Salary date ke liye: jaise 25 se 24 next month.",
                color = colors.textTertiary,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Continue button
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text(
                text = "Start using Hasbi →",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun CycleOptionChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    colors: com.hasbi.design.theme.HasbiColorPalette
) {
    val backgroundColor = if (isSelected) colors.primary else colors.surface
    val textColor = if (isSelected) Color.White else colors.textSecondary

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = if (isSelected) colors.primary else colors.border,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DayChip(
    day: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    colors: com.hasbi.design.theme.HasbiColorPalette
) {
    val backgroundColor = if (isSelected) colors.primary else colors.surface
    val textColor = if (isSelected) Color.White else colors.textSecondary

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .border(
                width = 1.dp,
                color = if (isSelected) colors.primary else colors.border,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            color = textColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
