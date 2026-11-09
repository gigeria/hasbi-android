package com.hasbi.transactions.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
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

@OptIn(ExperimentalMaterial3Api::class)
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
    val recentlyDeleted by viewModel.recentlyDeleted.collectAsState()
    val colors = HasbiTheme.colors

    val snackbarHostState = remember { SnackbarHostState() }

    // Show undo snackbar when transaction is deleted
    LaunchedEffect(recentlyDeleted) {
        recentlyDeleted?.let { tx ->
            val result = snackbarHostState.showSnackbar(
                message = "Transaction deleted",
                actionLabel = "UNDO",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.undoDelete()
            } else {
                viewModel.clearUndo()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
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
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = viewModel::setSearchQuery,
                        placeholder = { Text("Search...", color = colors.textTertiary) },
                        leadingIcon = { Icon(Icons.outlined.Search, null, tint = colors.textSecondary) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.outlined.Close, "Clear", tint = colors.textSecondary)
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    IconButton(onClick = { viewModel.setSearchActive(false) }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Close", tint = colors.textSecondary)
                    }
                } else {
                    Text("Transactions", color = colors.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Row {
                        IconButton(onClick = { viewModel.setSearchActive(true) }) {
                            Icon(Icons.outlined.Search, "Search", tint = colors.textSecondary)
                        }
                        IconButton(onClick = { /* Filter sheet */ }) {
                            Icon(Icons.outlined.FilterList, "Filter", tint = colors.textSecondary)
                        }
                    }
                }
            }

            // Filter chips
            if (!isSearchActive && typeFilter != null) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
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
                        onClear = { viewModel.setTypeFilter(null) },
                        colors = colors
                    )
                    TextButton(onClick = viewModel::clearFilters) {
                        Text("Clear all", color = colors.primary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // List
            when (val state = uiState) {
                is TransactionsUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator(color = colors.primary)
                    }
                }
                is TransactionsUiState.Error -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Text(state.message, color = colors.expense, textAlign = TextAlign.Center, modifier = Modifier.padding(32.dp))
                    }
                }
                is TransactionsUiState.Success -> {
                    if (state.isEmpty) {
                        Box(Modifier.fillMaxSize(), Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(if (state.isSearching) "🔍" else "📝", fontSize = 48.sp)
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    if (state.isSearching) "Koi result nahi mila" else "Abhi tak koi transaction nahi",
                                    color = colors.textSecondary, fontSize = 14.sp, fontWeight = FontWeight.Medium
                                )
                                if (!state.isSearching) {
                                    Spacer(Modifier.height(4.dp))
                                    Text("Quick Add se pehla kharch add karein", color = colors.textTertiary, fontSize = 12.sp)
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            state.groupedByDate.forEach { (dateLabel, transactions) ->
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
                                    // FIX FEAT-001: Use SwipeableTransactionRow instead of inline
                                    SwipeableTransactionRow(
                                        transaction = tx,
                                        onClick = { onTransactionClick(tx.id) },
                                        onDelete = { viewModel.softDelete(tx) },
                                        onEdit = { onTransactionClick(tx.id) },
                                        colors = colors
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // FIX FEAT-002: Undo snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
        )
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
        Text(label, color = if (isActive) Color.White else colors.textSecondary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        if (isActive) {
            Icon(Icons.outlined.Close, "Remove", tint = Color.White, modifier = Modifier.size(14.dp))
        }
    }
}
