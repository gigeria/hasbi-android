package com.hasbi.transactions.quickadd

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import com.hasbi.database.entity.TransactionType
import com.hasbi.design.theme.HasbiTheme
import com.hasbi.domain.model.Category
import com.hasbi.domain.model.Wallet
import com.hasbi.domain.repository.CategoryRepository
import com.hasbi.domain.repository.WalletRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class EditScreenDependencies @Inject constructor(
    categoryRepository: CategoryRepository,
    walletRepository: WalletRepository
) : ViewModel() {
    val expenseCategories: StateFlow<List<Category>> = categoryRepository
        .observeByGroup(com.hasbi.database.entity.CategoryGroup.EXPENSE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incomeCategories: StateFlow<List<Category>> = categoryRepository
        .observeByGroup(com.hasbi.database.entity.CategoryGroup.INCOME)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wallets: StateFlow<List<Wallet>> = walletRepository.observeVisible()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionScreen(
    transactionId: Long,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: EditTransactionViewModel = hiltViewModel(),
    deps: EditScreenDependencies = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val expenseCategories by deps.expenseCategories.collectAsState()
    val incomeCategories by deps.incomeCategories.collectAsState()
    val wallets by deps.wallets.collectAsState()
    val colors = HasbiTheme.colors

    LaunchedEffect(transactionId) {
        viewModel.loadTransaction(transactionId)
    }

    if (!state.isLoaded) {
        Box(Modifier.fillMaxSize().background(colors.background), Alignment.Center) {
            CircularProgressIndicator(color = colors.primary)
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .imePadding()
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
            Text("Edit Transaction", color = colors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = { viewModel.delete { onSaved() } }) {
                Icon(Icons.Filled.Delete, "Delete", tint = colors.expense)
            }
        }

        Spacer(Modifier.height(20.dp))

        // Type tabs
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(colors.backgroundWarm).padding(4.dp)
        ) {
            listOf(TransactionType.EXPENSE to "Expense", TransactionType.INCOME to "Income", TransactionType.TRANSFER to "Transfer").forEach { (type, label) ->
                val isSelected = state.type == type
                Box(
                    Modifier.weight(1f).clip(RoundedCornerShape(9.dp))
                        .background(if (isSelected) colors.primary else Color.Transparent)
                        .clickable { viewModel.setType(type) }.padding(vertical = 10.dp),
                    Alignment.Center
                ) {
                    Text(label, color = if (isSelected) Color.White else colors.textSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Amount
        Text("Amount", color = colors.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = state.amountText, onValueChange = viewModel::setAmount,
            prefix = { Text("Rs. ", color = colors.textSecondary, fontWeight = FontWeight.Medium) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)
        )

        Spacer(Modifier.height(16.dp))

        // Wallet
        Text("Wallet", color = colors.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            wallets.take(4).forEach { wallet ->
                val isSelected = state.selectedWalletId == wallet.id
                Box(
                    Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) colors.primary else colors.backgroundWarm)
                        .clickable { viewModel.setWalletId(wallet.id) }.padding(vertical = 10.dp),
                    Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(wallet.icon, fontSize = 14.sp)
                        Text(wallet.name, color = if (isSelected) Color.White else colors.textPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Note
        Text("Note", color = colors.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = state.note, onValueChange = viewModel::setNote,
            placeholder = { Text("Optional note", color = colors.textTertiary) },
            singleLine = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
        )

        state.error?.let { error ->
            Spacer(Modifier.height(12.dp))
            Text(error, color = colors.expense, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(Modifier.height(24.dp))

        // Save button
        Button(
            onClick = { viewModel.save { success -> if (success) onSaved() } },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            enabled = !state.isSaving,
            colors = ButtonDefaults.buttonColors(containerColor = colors.primary, contentColor = Color.White),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
            } else {
                Text("Save Changes", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
