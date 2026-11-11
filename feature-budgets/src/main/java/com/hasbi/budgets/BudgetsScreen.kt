package com.hasbi.budgets

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.hasbi.budgets.components.CategoryBudgetBar
import com.hasbi.budgets.components.CycleCard
import com.hasbi.database.entity.CycleType
import com.hasbi.database.relation.BudgetWithSpent
import com.hasbi.design.theme.HasbiTheme
import com.hasbi.util.money.CurrencyInfo
import com.hasbi.util.money.SymbolPosition

@Composable
fun BudgetsScreen(
    onAddBudgetClick: () -> Unit,
    viewModel: BudgetsViewModel = hiltViewModel()
) {
    val currentCycle by viewModel.currentCycle.collectAsState()
    val budgetsWithSpent by viewModel.budgetsWithSpent.collectAsState()
    val colors = HasbiTheme.colors

    val currency = CurrencyInfo(
        code = "PKR",
        name = "Pakistani Rupee",
        symbol = "Rs.",
        minorUnitExponent = 2,
        decimalSeparator = ".",
        thousandsSeparator = ",",
        symbolPosition = SymbolPosition.BEFORE,
        isDefault = true
    )

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
            // Top bar with Add Budget button
            androidx.compose.foundation.layout.Row(
                modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = "Budgets",
                    color = colors.textPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                androidx.compose.material3.TextButton(onClick = onAddBudgetClick) {
                    androidx.compose.material3.Text(
                        text = "+ Add Budget",
                        color = colors.primary,
                        fontSize = 14.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (currentCycle == null && budgetsWithSpent.isEmpty()) {
                EmptyBudgetsState(
                    onAddClick = onAddBudgetClick,
                    colors = colors
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cycle card (overall budget)
                    item {
                        val overallBudget = budgetsWithSpent.firstOrNull { it.categoryId == null }
                        CycleCard(
                            budget = overallBudget,
                            cycleName = "Current Cycle",
                            cycleType = currentCycle?.cycleType?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Monthly",
                            currency = currency
                        )
                    }

                    // Category budgets
                    val categoryBudgets = budgetsWithSpent.filter { it.categoryId != null }
                    if (categoryBudgets.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Category Budgets",
                                color = colors.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        items(categoryBudgets) { budget ->
                            CategoryBudgetBar(
                                budget = budget,
                                categoryName = "Category #${budget.categoryId}",
                                categoryIcon = "📦",
                                currency = currency
                            )
                        }
                    }
                }
            }
        }

        // Add Budget button — at top right (not FAB which overlaps bottom nav)
        // Removed FAB to fix navigation issue — button in header instead
    }
}

@Composable
private fun EmptyBudgetsState(
    onAddClick: () -> Unit,
    colors: com.hasbi.design.theme.HasbiColorPalette
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "📊", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Apna budget set karein",
                color = colors.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Taake Hasbi aap ko daily guide kar sake.\nAaj Ka Baqi yahan dikhega.",
                color = colors.textSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(100.dp))
                    .background(colors.primary)
                    .clickable(onClick = onAddClick)
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "+ Set Budget",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
