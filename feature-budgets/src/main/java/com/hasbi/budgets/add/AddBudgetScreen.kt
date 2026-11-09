package com.hasbi.budgets.add

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hasbi.design.theme.HasbiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBudgetScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: AddBudgetViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val colors = HasbiTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back", tint = colors.textPrimary)
            }
            Text("Add Budget", color = colors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.size(40.dp))
        }

        Spacer(Modifier.height(20.dp))

        // Budget type: Overall vs Category
        Text("Budget For", color = colors.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(colors.backgroundWarm).padding(4.dp)
        ) {
            listOf(true to "Overall", false to "Category").forEach { (isOverall, label) ->
                val isSelected = state.isOverall == isOverall
                Box(
                    Modifier.weight(1f).clip(RoundedCornerShape(9.dp))
                        .background(if (isSelected) colors.primary else Color.Transparent)
                        .clickable { viewModel.setBudgetType(isOverall) }.padding(vertical = 10.dp),
                    Alignment.Center
                ) {
                    Text(label, color = if (isSelected) Color.White else colors.textSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Amount
        Text("Budget Limit", color = colors.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = state.amountText,
            onValueChange = viewModel::setAmount,
            prefix = { Text("Rs. ", color = colors.textSecondary, fontWeight = FontWeight.Medium) },
            placeholder = { Text("e.g. 50000", color = colors.textTertiary) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(20.dp))

        // Warning threshold slider
        Text("Warning Threshold: ${(state.warningThreshold * 100).toInt()}%", color = colors.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Slider(
            value = state.warningThreshold,
            onValueChange = viewModel::setThreshold,
            valueRange = 0.5f..1.0f,
            colors = SliderDefaults.colors(
                thumbColor = colors.primary,
                activeTrackColor = colors.primary,
                inactiveTrackColor = colors.border
            )
        )
        Text("Is budget ke ${(state.warningThreshold * 100).toInt()}% tak pohnchne par alert milega.", color = colors.textTertiary, fontSize = 11.sp)

        // Error
        state.error?.let { error ->
            Spacer(Modifier.height(16.dp))
            Text(error, color = colors.expense, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(Modifier.height(24.dp))

        // Save button
        Button(
            onClick = { viewModel.save { success -> if (success) onSaved() } },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = Color.White),
            shape = RoundedCornerShape(14.dp)
        ) {
            Text("Save Budget", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
