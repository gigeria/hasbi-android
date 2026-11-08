package com.hasbi.transactions.list

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.hasbi.database.entity.TransactionType
import com.hasbi.design.theme.HasbiTheme
import com.hasbi.domain.model.Transaction

/**
 * Transactions List screen — full transaction history with search + filters.
 *
 * Features:
 * - Search bar (note + category name)
 * - Filter chips (type, wallet, category)
 * - Date-grouped list (Today, Yesterday, etc.)
 * - Tap to view detail
 * - Long press to delete
 */
@Composable
fun TransactionsListScreen(
    onTransactionClick: (Long) -> Unit = {},
    onBackClick: () -> Unit = {},
    viewModel: TransactionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearchActive by viewModel.isSearchActive.collectAsState()
    val typeFilter by viewModel.typeFilter.collectAsState()
    val colors = HasbiTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSearchActive) {
                // Search mode — show search field
                SearchField(
                    query = searchQuery,
                    onQueryChange = viewModel::setSearchQuery,
                    onClose = { viewModel.setSearchActive(false) },
                    colors = colors
                )
            } else {
                // Normal mode — title + icons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Transactions",
                        color = colors.textPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row {
                    IconButton(onClick = { viewModel.setSearchActive(true) }) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = "Search",
                            tint = colors.textSecondary
                        )
                    }
                    IconButton(onClick = { /* Show filter sheet */ }) {
                        Icon(
                            imageVector = Icons.Outlined.FilterList,
                            contentDescription = "Filter",
                            tint = colors.textSecondary
                        )
                    }
                }
            }
        }

        // Filter chips row (only when filters active)
        if (!isSearchActive) {
            FilterChipsRow(
                typeFilter = typeFilter,
                onTypeFilterChange = viewModel::setTypeFilter,
                onClearFilters = viewModel::clearFilters,
                colors = colors
            )
        }

        // List
        when (val state = uiState) {
            is TransactionsUiState.Loading -> {
                LoadingState(colors = colors)
            }
            is TransactionsUiState.Error -> {
                ErrorState(
                    message = state.message,
                    colors = colors
                )
            }
            is TransactionsUiState.Success -> {
                if (state.isEmpty) {
                    EmptyState(
                        isSearching = state.isSearching,
                        colors = colors
                    )
                } else {
                    TransactionsList(
                        groupedByDate = state.groupedByDate,
                        onTransactionClick = onTransactionClick,
                        onTransactionLongPress = viewModel::softDelete,
                        colors = colors
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    colors: com.hasbi.design.theme.HasbiColorPalette
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = {
                Text(text = "Search transactions...", color = colors.textTertiary)
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = colors.textSecondary
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Clear",
                            tint = colors.textSecondary
                        )
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp)
        )
        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Close search",
                tint = colors.textSecondary
            )
        }
    }
}

@Composable
private fun FilterChipsRow(
    typeFilter: TransactionType?,
    onTypeFilterChange: (TransactionType?) -> Unit,
    onClearFilters: () -> Unit,
    colors: com.hasbi.design.theme.HasbiColorPalette
) {
    if (typeFilter == null) return

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            label = when (typeFilter) {
                TransactionType.EXPENSE -> "Expenses"
                TransactionType.INCOME -> "Income"
                TransactionType.TRANSFER -> "Transfers"
                else -> "Other"
            },
            isActive = true,
            onClear = { onTypeFilterChange(null) },
            colors = colors
        )
        TextButton(onClick = onClearFilters) {
            Text(
                text = "Clear all",
                color = colors.primary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    isActive: Boolean,
    onClear: () -> Unit,
    colors: com.hasbi.design.theme.HasbiColorPalette
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(if (isActive) colors.primary else colors.surface)
            .clickable(onClick = onClear)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            color = if (isActive) Color.White else colors.textSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        if (isActive) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "Remove",
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun TransactionsList(
    groupedByDate: Map<String, List<Transaction>>,
    onTransactionClick: (Long) -> Unit,
    onTransactionLongPress: (Transaction) -> Unit,
    colors: com.hasbi.design.theme.HasbiColorPalette
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            horizontal = 16.dp,
            vertical = 8.dp
        )
    ) {
        groupedByDate.forEach { (dateLabel, transactions) ->
            item {
                Text(
                    text = dateLabel,
                    color = colors.textSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)
                )
            }
            items(transactions) { tx ->
                TransactionRow(
                    transaction = tx,
                    onClick = { onTransactionClick(tx.id) },
                    onLongPress = { onTransactionLongPress(tx) },
                    colors = colors
                )
            }
        }
    }
}

@Composable
private fun TransactionRow(
    transaction: Transaction,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
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
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
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

        Column(
            modifier = Modifier.weight(1f)
        ) {
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

@Composable
private fun LoadingState(colors: com.hasbi.design.theme.HasbiColorPalette) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.CircularProgressIndicator(color = colors.primary)
    }
}

@Composable
private fun ErrorState(
    message: String,
    colors: com.hasbi.design.theme.HasbiColorPalette
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            color = colors.expense,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(32.dp)
        )
    }
}

@Composable
private fun EmptyState(
    isSearching: Boolean,
    colors: com.hasbi.design.theme.HasbiColorPalette
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isSearching) "🔍" else "📝",
                fontSize = 48.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (isSearching) "Koi result nahi mila"
                       else "Abhi tak koi transaction nahi",
                color = colors.textSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            if (!isSearching) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Quick Add se pehla kharch add karein",
                    color = colors.textTertiary,
                    fontSize = 12.sp
                )
            }
        }
    }
}
