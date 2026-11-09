package com.hasbi.wallets.add

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hasbi.database.entity.WalletType
import com.hasbi.design.theme.HasbiTheme

@Composable
fun AddWalletScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: AddWalletViewModel = hiltViewModel()
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
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = colors.textPrimary
                )
            }
            Text(
                text = "Add Wallet",
                color = colors.textPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.size(40.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Wallet type
        Text(
            text = "Wallet Type",
            color = colors.textSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(120.dp)
        ) {
            items(WalletType.values().toList()) { type ->
                WalletTypeChip(
                    type = type,
                    isSelected = state.type == type,
                    onClick = { viewModel.setType(type) },
                    colors = colors
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Name field
        Text(
            text = "Wallet Name",
            color = colors.textSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = state.name,
            onValueChange = viewModel::setName,
            placeholder = { Text("e.g. Cash, HBL, JazzCash", color = colors.textTertiary) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Initial balance
        Text(
            text = "Initial Balance (Optional)",
            color = colors.textSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = state.balanceText,
            onValueChange = viewModel::setInitialBalance,
            placeholder = { Text("0", color = colors.textTertiary) },
            prefix = { Text("Rs. ", color = colors.textSecondary, fontWeight = FontWeight.Medium) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Icon picker
        Text(
            text = "Icon",
            color = colors.textSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("💵", "🏦", "📱", "💳", "🐷", "💼", "🏠", "⭐").forEach { icon ->
                val isSelected = state.icon == icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) colors.primary.copy(alpha = 0.1f) else colors.surface)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) colors.primary else colors.border,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { viewModel.setIcon(icon) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = icon, fontSize = 18.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Color picker
        Text(
            text = "Color",
            color = colors.textSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("#4CAF80", "#2196F3", "#E91E63", "#FF9800", "#9C27B0", "#607D8B").forEach { colorHex ->
                val isSelected = state.colorHex == colorHex
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(parseColor(colorHex))
                        .border(
                            width = if (isSelected) 3.dp else 0.dp,
                            color = colors.primaryGold,
                            shape = CircleShape
                        )
                        .clickable { viewModel.setColor(colorHex) }
                )
            }
        }

        // Error
        state.error?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                color = colors.expense,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Save button
        Button(
            onClick = {
                viewModel.save { success ->
                    if (success) {
                        viewModel.reset()
                        onSaved()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            enabled = !state.isSaving,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
            } else {
                Text("Save Wallet", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun WalletTypeChip(
    type: WalletType,
    isSelected: Boolean,
    onClick: () -> Unit,
    colors: com.hasbi.design.theme.HasbiColorPalette
) {
    val (icon, label) = when (type) {
        WalletType.CASH -> "💵" to "Cash"
        WalletType.BANK -> "🏦" to "Bank"
        WalletType.MOBILE_WALLET -> "📱" to "JazzCash"
        WalletType.CREDIT_CARD -> "💳" to "Card"
        WalletType.SAVINGS -> "🐷" to "Savings"
        WalletType.CUSTOM -> "⚙️" to "Custom"
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) colors.primary.copy(alpha = 0.05f) else colors.surface)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) colors.primary else colors.border,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = icon, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = if (isSelected) colors.primary else colors.textSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun parseColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color.Gray
    }
}
