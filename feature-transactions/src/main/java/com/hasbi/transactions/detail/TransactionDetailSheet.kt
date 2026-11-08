package com.hasbi.transactions.detail

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasbi.database.entity.TransactionType
import com.hasbi.design.theme.HasbiTheme
import com.hasbi.domain.model.Transaction
import com.hasbi.domain.repository.TransactionRepository
import com.hasbi.domain.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionDetailViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _transaction = MutableStateFlow<Transaction?>(null)
    val transaction: StateFlow<Transaction?> = _transaction.asStateFlow()

    private val _walletName = MutableStateFlow<String?>(null)
    val walletName: StateFlow<String?> = _walletName.asStateFlow()

    private val _toWalletName = MutableStateFlow<String?>(null)
    val toWalletName: StateFlow<String?> = _toWalletName.asStateFlow()

    fun loadTransaction(id: Long) {
        viewModelScope.launch {
            val tx = transactionRepository.getById(id)
            _transaction.value = tx
            tx?.let {
                walletRepository.getById(it.walletId)?.let { w ->
                    _walletName.value = w.name
                }
                it.toWalletId?.let { toId ->
                    walletRepository.getById(toId)?.let { w ->
                        _toWalletName.value = w.name
                    }
                }
            }
        }
    }

    fun delete(onComplete: () -> Unit) {
        val tx = _transaction.value ?: return
        viewModelScope.launch {
            transactionRepository.softDelete(tx.id)
            onComplete()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailSheet(
    transactionId: Long,
    onDismiss: () -> Unit,
    onEdit: (Long) -> Unit = {},
    onDelete: () -> Unit = {},
    viewModel: TransactionDetailViewModel = hiltViewModel()
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val transaction by viewModel.transaction.collectAsState()
    val walletName by viewModel.walletName.collectAsState()
    val toWalletName by viewModel.toWalletName.collectAsState()
    val colors = HasbiTheme.colors

    LaunchedEffect(transactionId) {
        viewModel.loadTransaction(transactionId)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        transaction?.let { tx ->
            TransactionDetailContent(
                transaction = tx,
                walletName = walletName ?: "",
                toWalletName = toWalletName,
                onEdit = { onEdit(tx.id) },
                onDelete = {
                    viewModel.delete {
                        onDelete()
                        onDismiss()
                    }
                },
                colors = colors
            )
        }
    }
}

@Composable
private fun TransactionDetailContent(
    transaction: Transaction,
    walletName: String,
    toWalletName: String?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    colors: com.hasbi.design.theme.HasbiColorPalette
) {
    val isIncome = transaction.type == TransactionType.INCOME
    val amountColor = if (isIncome) colors.income else colors.expense
    val amountSign = if (isIncome) "+" else "−"

    val (icon, bgColor) = when (transaction.type) {
        TransactionType.INCOME -> "💰" to colors.income.copy(alpha = 0.15f)
        TransactionType.TRANSFER -> "⇄" to colors.primary.copy(alpha = 0.15f)
        else -> "💸" to colors.expense.copy(alpha = 0.15f)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Transaction Detail",
                color = colors.textSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Edit",
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = colors.expense,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Icon
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 28.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Amount
        Text(
            text = "$amountSign Rs. ${formatAmount(transaction.amount)}",
            color = amountColor,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Note / type
        Text(
            text = transaction.note ?: typeLabel(transaction.type),
            color = colors.textSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Details card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(colors.backgroundWarm)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailRow(
                label = "Wallet",
                value = walletName,
                colors = colors
            )
            if (transaction.type == TransactionType.TRANSFER && toWalletName != null) {
                DetailRow(
                    label = "To",
                    value = toWalletName,
                    colors = colors
                )
            }
            DetailRow(
                label = "Date",
                value = formatDate(transaction.date),
                colors = colors
            )
            DetailRow(
                label = "Type",
                value = typeLabel(transaction.type),
                colors = colors
            )
            if (transaction.note != null) {
                val noteValue = transaction.note
                DetailRow(
                    label = "Note",
                    value = noteValue,
                    colors = colors
                )
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    colors: com.hasbi.design.theme.HasbiColorPalette
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = colors.textSecondary,
            fontSize = 13.sp
        )
        Text(
            text = value,
            color = colors.textPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private fun typeLabel(type: TransactionType): String = when (type) {
    TransactionType.EXPENSE -> "Expense"
    TransactionType.INCOME -> "Income"
    TransactionType.TRANSFER -> "Transfer"
    else -> "Transaction"
}

private fun formatDate(epochMillis: Long): String {
    val sdf = java.text.SimpleDateFormat("dd MMM yyyy, h:mm a", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(epochMillis))
}

private fun formatAmount(paisa: Long): String {
    val rs = paisa / 100
    return "%,d".format(rs)
}
