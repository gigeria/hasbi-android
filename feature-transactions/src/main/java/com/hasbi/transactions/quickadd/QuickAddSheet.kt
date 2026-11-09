package com.hasbi.transactions.quickadd

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hasbi.database.entity.TransactionType
import com.hasbi.design.theme.HasbiTheme
import com.hasbi.domain.model.Category
import com.hasbi.domain.model.Wallet

/**
 * Quick Add Bottom Sheet — add transaction in <3 seconds.
 *
 * Features:
 * - Type tabs (Expense / Income / Transfer)
 * - Amount input (numeric keyboard)
 * - Smart Entry hint (Roman Urdu parser)
 * - Category grid (4-column)
 * - Wallet selector
 * - Note (optional)
 * - Save button
 *
 * First transaction triggers confetti (TODO: Week 12 polish).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddSheet(
    onDismiss: () -> Unit,
    onSaved: () -> Unit = {},
    viewModel: QuickAddViewModel = hiltViewModel()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val state by viewModel.state.collectAsState()
    val wallets by viewModel.wallets.collectAsState()
    val expenseCategories by viewModel.expenseCategories.collectAsState()
    val incomeCategories by viewModel.incomeCategories.collectAsState()
    val colors = HasbiTheme.colors

    ModalBottomSheet(
        onDismissRequest = {
            viewModel.reset()
            onDismiss()
        },
        sheetState = sheetState,
        containerColor = colors.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        QuickAddContent(
            state = state,
            wallets = wallets,
            categories = if (state.type == TransactionType.INCOME) incomeCategories else expenseCategories,
            onTypeChange = viewModel::setType,
            onAmountChange = viewModel::setAmount,
            onSmartEntryChange = viewModel::parseSmartEntry,
            onCategorySelect = viewModel::selectCategory,
            onWalletSelect = viewModel::selectWallet,
            onNoteChange = viewModel::setNote,
            onDateChange = viewModel::setDate,
            onPhotoChange = viewModel::setPhoto,
            onSave = {
                viewModel.save { success ->
                    if (success) {
                        viewModel.reset()
                        onSaved()
                        onDismiss()
                    }
                }
            },
            colors = colors
        )
    }
}

@Composable
private fun QuickAddContent(
    state: QuickAddState,
    wallets: List<Wallet>,
    categories: List<Category>,
    onTypeChange: (TransactionType) -> Unit,
    onAmountChange: (String) -> Unit,
    onSmartEntryChange: (String) -> Unit,
    onCategorySelect: (Category) -> Unit,
    onWalletSelect: (Wallet) -> Unit,
    onNoteChange: (String) -> Unit,
    onDateChange: (Long) -> Unit,
    onPhotoChange: (String?) -> Unit,
    onSave: () -> Unit,
    colors: com.hasbi.design.theme.HasbiColorPalette
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Title
        Text(
            text = "Add Transaction",
            color = colors.textPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Type tabs
        TypeTabs(
            selectedType = state.type,
            onTypeChange = onTypeChange,
            colors = colors
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Amount input
        AmountInput(
            amountText = state.amountText,
            onAmountChange = onAmountChange,
            colors = colors
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Smart Entry hint
        SmartEntryHint(
            text = state.smartEntryText,
            onTextChange = onSmartEntryChange,
            result = state.smartEntryResult,
            colors = colors
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Category grid (only for expense/income, not transfer)
        if (state.type != TransactionType.TRANSFER) {
            Text(
                text = "Category",
                color = colors.textSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            CategoryGrid(
                categories = categories,
                selectedCategoryId = state.selectedCategoryId,
                onSelect = onCategorySelect,
                colors = colors
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Wallet selector
        Text(
            text = if (state.type == TransactionType.TRANSFER) "From Wallet" else "Wallet",
            color = colors.textSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        WalletSelectorRow(
            wallets = wallets,
            selectedWalletId = state.selectedWalletId,
            onSelect = onWalletSelect,
            colors = colors
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Date selector (FEAT-005: Date picker)
        Text(
            text = "Date",
            color = colors.textSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        DatePickerField(
            selectedDate = state.date,
            onDateSelected = onDateChange,
            colors = colors
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Receipt photo (FEAT-004: Receipt photo integration)
        ReceiptPhotoPicker(
            photoUri = state.attachmentPath,
            onPhotoSelected = onPhotoChange
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Note (optional)
        OutlinedTextField(
            value = state.note,
            onValueChange = onNoteChange,
            placeholder = {
                Text(text = "Note (optional)", color = colors.textTertiary)
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        // Error message
        state.error?.let { error ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                color = colors.expense,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Save button
        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            enabled = !state.isSaving && state.amountInPaisa > 0,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primary,
                contentColor = Color.White,
                disabledContainerColor = colors.primary.copy(alpha = 0.4f),
                disabledContentColor = Color.White.copy(alpha = 0.7f)
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = "Save Transaction",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun TypeTabs(
    selectedType: TransactionType,
    onTypeChange: (TransactionType) -> Unit,
    colors: com.hasbi.design.theme.HasbiColorPalette
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.backgroundWarm)
            .padding(4.dp)
    ) {
        listOf(
            TransactionType.EXPENSE to "Expense",
            TransactionType.INCOME to "Income",
            TransactionType.TRANSFER to "Transfer"
        ).forEach { (type, label) ->
            val isSelected = selectedType == type
            val tabColor = when {
                isSelected && type == TransactionType.EXPENSE -> colors.expense
                isSelected && type == TransactionType.INCOME -> colors.income
                isSelected -> colors.primary
                else -> Color.Transparent
            }
            val textColor = if (isSelected) Color.White else colors.textSecondary

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(tabColor)
                    .clickable { onTypeChange(type) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = textColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun AmountInput(
    amountText: String,
    onAmountChange: (String) -> Unit,
    colors: com.hasbi.design.theme.HasbiColorPalette
) {
    OutlinedTextField(
        value = amountText,
        onValueChange = onAmountChange,
        placeholder = {
            Text(text = "0", color = colors.textTertiary)
        },
        prefix = {
            Text(
                text = "Rs. ",
                color = colors.textSecondary,
                fontWeight = FontWeight.Medium
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        textStyle = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Bold
        )
    )
}

@Composable
private fun SmartEntryHint(
    text: String,
    onTextChange: (String) -> Unit,
    result: com.hasbi.domain.model.SmartEntryResult?,
    colors: com.hasbi.design.theme.HasbiColorPalette
) {
    Column {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = {
                Text(
                    text = "💡 Ya type karein: \"500 chai\"",
                    color = colors.textTertiary
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        // Show parsed result if confidence > 0
        if (result != null && result.confidence > 0) {
            Spacer(modifier = Modifier.height(6.dp))
            val resCategory = result.category
            val resWallet = result.wallet
            Text(
                text = buildString {
                    if (result.amount > 0) append("Rs. ${result.amount / 100}")
                    if (resCategory != null) {
                        if (isNotEmpty()) append(" · ")
                        append(resCategory.icon)
                        append(" ")
                        append(resCategory.name)
                    }
                    if (resWallet != null) {
                        if (isNotEmpty()) append(" · ")
                        append(resWallet.icon)
                        append(" ")
                        append(resWallet.name)
                    }
                    if (isNotEmpty()) {
                        append(" (${(result.confidence * 100).toInt()}% sure)")
                    }
                },
                color = colors.primary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun CategoryGrid(
    categories: List<Category>,
    selectedCategoryId: Long?,
    onSelect: (Category) -> Unit,
    colors: com.hasbi.design.theme.HasbiColorPalette
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.height(180.dp)
    ) {
        items(categories.take(8)) { category ->
            CategoryChip(
                category = category,
                isSelected = category.id == selectedCategoryId,
                onClick = { onSelect(category) },
                colors = colors
            )
        }
    }
}

@Composable
private fun CategoryChip(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit,
    colors: com.hasbi.design.theme.HasbiColorPalette
) {
    val bgColor = if (isSelected) colors.primary else colors.backgroundWarm
    val textColor = if (isSelected) Color.White else colors.textPrimary

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = category.icon,
            fontSize = 22.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = category.name,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun WalletSelectorRow(
    wallets: List<Wallet>,
    selectedWalletId: Long?,
    onSelect: (Wallet) -> Unit,
    colors: com.hasbi.design.theme.HasbiColorPalette
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        wallets.take(4).forEach { wallet ->
            val isSelected = wallet.id == selectedWalletId
            val bgColor = if (isSelected) colors.primary else colors.backgroundWarm
            val textColor = if (isSelected) Color.White else colors.textPrimary

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgColor)
                    .clickable { onSelect(wallet) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = wallet.icon,
                        fontSize = 14.sp
                    )
                    Text(
                        text = wallet.name,
                        color = textColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(
    selectedDate: Long,
    onDateSelected: (Long) -> Unit,
    colors: com.hasbi.design.theme.HasbiColorPalette
) {
    var showPicker by remember { androidx.compose.runtime.mutableStateOf(false) }
    val dateFormat = remember { java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.backgroundWarm)
            .clickable { showPicker = true }
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "📅", fontSize = 16.sp)
            Text(
                text = dateFormat.format(java.util.Date(selectedDate)),
                color = colors.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }

    if (showPicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { onDateSelected(it) }
                        showPicker = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = androidx.compose.ui.graphics.Color.White
                    )
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
