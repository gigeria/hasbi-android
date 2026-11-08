package com.hasbi.transactions.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasbi.database.entity.TransactionType
import com.hasbi.domain.model.Transaction
import com.hasbi.domain.repository.TransactionRepository
import com.hasbi.util.date.DateTimeUtils
import com.hasbi.util.result.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * Transactions List ViewModel.
 *
 * Features:
 * - Search (note + category name)
 * - Filter by type (expense/income/transfer/all)
 * - Filter by wallet
 * - Filter by date range (default: this month)
 * - Soft delete with undo (10s snackbar)
 * - Grouped by date (Today, Yesterday, this week, etc.)
 */
@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _typeFilter = MutableStateFlow<TransactionType?>(null)
    val typeFilter: StateFlow<TransactionType?> = _typeFilter.asStateFlow()

    private val _walletFilter = MutableStateFlow<Long?>(null)
    val walletFilter: StateFlow<Long?> = _walletFilter.asStateFlow()

    private val _categoryFilter = MutableStateFlow<Long?>(null)
    val categoryFilter: StateFlow<Long?> = _categoryFilter.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    private val _recentlyDeleted = MutableStateFlow<Transaction?>(null)
    val recentlyDeleted: StateFlow<Transaction?> = _recentlyDeleted.asStateFlow()

    // Default date range: this month
    private val currentDateRange: Pair<Long, Long>
        get() {
            val zone = ZoneId.of(DateTimeUtils.DEFAULT_TIMEZONE)
            val today = LocalDate.now(zone)
            return DateTimeUtils.startOfMonth(today.year, today.monthValue, zone) to
                DateTimeUtils.endOfMonth(today.year, today.monthValue, zone)
        }

    val uiState: StateFlow<TransactionsUiState> = combine(
        _searchQuery,
        _typeFilter,
        _walletFilter,
        _categoryFilter,
        _isSearchActive
    ) { query, type, wallet, category, isSearching ->
        FilterState(query, type, wallet, category, isSearching)
    }.flatMapLatest { filter ->
        if (filter.isSearching && filter.query.isNotBlank()) {
            transactionRepository.search(filter.query)
        } else {
            val (start, end) = currentDateRange
            transactionRepository.observeFiltered(
                startEpoch = start,
                endEpoch = end,
                typeFilter = filter.type,
                walletFilter = filter.wallet,
                categoryFilter = filter.category
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    ).let { flow ->
        combine(flow, _isSearchActive) { transactions, isSearching ->
            TransactionsUiState.Success(
                transactions = transactions,
                isSearching = isSearching,
                groupedByDate = groupByDate(transactions)
            ) as TransactionsUiState
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TransactionsUiState.Loading
        )
    }

    // === Search ===

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSearchActive(active: Boolean) {
        _isSearchActive.value = active
        if (!active) _searchQuery.value = ""
    }

    // === Filters ===

    fun setTypeFilter(type: TransactionType?) {
        _typeFilter.value = type
    }

    fun setWalletFilter(walletId: Long?) {
        _walletFilter.value = walletId
    }

    fun setCategoryFilter(categoryId: Long?) {
        _categoryFilter.value = categoryId
    }

    fun clearFilters() {
        _typeFilter.value = null
        _walletFilter.value = null
        _categoryFilter.value = null
    }

    // === Delete + Restore ===

    fun softDelete(transaction: Transaction) {
        viewModelScope.launch {
            when (val result = transactionRepository.softDelete(transaction.id)) {
                is Result.Success -> {
                    _recentlyDeleted.value = transaction
                }
                is Result.Failure -> {
                    // Silently fail — UI shows snackbar only on success
                }
            }
        }
    }

    fun undoDelete() {
        val deleted = _recentlyDeleted.value ?: return
        viewModelScope.launch {
            transactionRepository.restore(deleted.id)
            _recentlyDeleted.value = null
        }
    }

    fun clearUndo() {
        _recentlyDeleted.value = null
    }

    // === Helpers ===

    private fun groupByDate(transactions: List<Transaction>): Map<String, List<Transaction>> {
        val zone = ZoneId.of(DateTimeUtils.DEFAULT_TIMEZONE)
        val today = LocalDate.now(zone)
        val yesterday = today.minusDays(1)

        return transactions.groupBy { tx ->
            val txDate = DateTimeUtils.toLocalDate(tx.date, zone)
            when (txDate) {
                today -> "Today"
                yesterday -> "Yesterday"
                else -> {
                    val weekAgo = today.minusDays(7)
                    if (txDate.isAfter(weekAgo)) {
                        txDate.dayOfWeek.name.lowercase()
                            .replaceFirstChar { it.uppercase() }.take(3)
                    } else {
                        "${txDate.dayOfMonth} ${txDate.month.name.lowercase()
                            .replaceFirstChar { it.uppercase() }.take(3)}"
                    }
                }
            }
        }
    }
}

data class FilterState(
    val query: String,
    val type: TransactionType?,
    val wallet: Long?,
    val category: Long?,
    val isSearching: Boolean
)

sealed class TransactionsUiState {
    data object Loading : TransactionsUiState()

    data class Success(
        val transactions: List<Transaction>,
        val isSearching: Boolean,
        val groupedByDate: Map<String, List<Transaction>>
    ) : TransactionsUiState() {
        val isEmpty: Boolean get() = transactions.isEmpty()
    }

    data class Error(val message: String) : TransactionsUiState()
}
