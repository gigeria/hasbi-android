package com.hasbi.transactions.quickadd

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasbi.database.entity.CategoryGroup
import com.hasbi.database.entity.TransactionType
import com.hasbi.domain.model.Category
import com.hasbi.domain.model.SmartEntryResult
import com.hasbi.domain.model.Transaction
import com.hasbi.domain.model.Wallet
import com.hasbi.domain.repository.CategoryRepository
import com.hasbi.domain.repository.TransactionRepository
import com.hasbi.domain.repository.WalletRepository
import com.hasbi.domain.usecase.transaction.ParseSmartEntryUseCase
import com.hasbi.util.result.HasbiError
import com.hasbi.util.result.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Quick Add ViewModel — manage transaction creation.
 *
 * Features:
 * - Type tabs (Expense / Income / Transfer)
 * - Amount input
 * - Category selection (4-col grid)
 * - Wallet selection
 * - Smart Entry parsing (Roman Urdu + English)
 * - Date (default: today)
 * - Note (optional)
 * - Save / Save + Add Another
 */
@HiltViewModel
class QuickAddViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val walletRepository: WalletRepository,
    private val categoryRepository: CategoryRepository,
    private val parseSmartEntryUseCase: ParseSmartEntryUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(QuickAddState())
    val state: StateFlow<QuickAddState> = _state.asStateFlow()

    val wallets: StateFlow<List<Wallet>> = walletRepository.observeVisible()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val expenseCategories: StateFlow<List<Category>> = categoryRepository
        .observeByGroup(CategoryGroup.EXPENSE)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val incomeCategories: StateFlow<List<Category>> = categoryRepository
        .observeByGroup(CategoryGroup.INCOME)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Set default wallet when wallets load
        viewModelScope.launch {
            wallets.collect { walletList ->
                if (_state.value.selectedWalletId == null && walletList.isNotEmpty()) {
                    _state.update { it.copy(selectedWalletId = walletList.first().id) }
                }
            }
        }
    }

    // === Type ===
    fun setType(type: TransactionType) {
        _state.update {
            it.copy(
                type = type,
                selectedCategoryId = null,  // reset category on type change
                smartEntryResult = null
            )
        }
    }

    // === Amount ===
    fun setAmount(amountText: String) {
        // Allow only digits
        val cleaned = amountText.filter { it.isDigit() }
        val amountInRs = cleaned.toLongOrNull() ?: 0L
        _state.update { it.copy(amountText = cleaned, amountInPaisa = amountInRs * 100) }
    }

    // === Smart Entry ===
    fun parseSmartEntry(text: String) {
        _state.update { it.copy(smartEntryText = text) }
        if (text.isBlank()) {
            _state.update { it.copy(smartEntryResult = null) }
            return
        }
        viewModelScope.launch {
            val result = parseSmartEntryUseCase(text)
            _state.update {
                it.copy(
                    smartEntryResult = result,
                    amountText = if (result.amount > 0) (result.amount / 100).toString() else it.amountText,
                    amountInPaisa = result.amount,
                    selectedCategoryId = result.category?.id,
                    selectedWalletId = result.wallet?.id ?: it.selectedWalletId,
                    type = result.type
                )
            }
        }
    }

    // === Category ===
    fun selectCategory(category: Category) {
        _state.update { it.copy(selectedCategoryId = category.id) }
    }

    // === Wallet ===
    fun selectWallet(wallet: Wallet) {
        _state.update { it.copy(selectedWalletId = wallet.id) }
    }

    // === Transfer-specific ===
    fun setToWallet(walletId: Long) {
        _state.update { it.copy(toWalletId = walletId) }
    }

    // === Note ===
    fun setNote(note: String) {
        _state.update { it.copy(note = note) }
    }

    // === Date ===
    fun setDate(date: Long) {
        _state.update { it.copy(date = date) }
    }

    fun setPhoto(uri: String?) {
        _state.update { it.copy(attachmentPath = uri) }
    }

    // === Save ===
    fun save(onComplete: (Boolean) -> Unit) {
        val current = _state.value

        // Validate
        if (current.amountInPaisa <= 0) {
            _state.update { it.copy(error = "Amount 0 se zyada honi chahiye") }
            return
        }

        val walletId = current.selectedWalletId
        if (walletId == null) {
            _state.update { it.copy(error = "Wallet select karein") }
            return
        }

        if (current.type == TransactionType.TRANSFER && current.toWalletId == null) {
            _state.update { it.copy(error = "Destination wallet select karein") }
            return
        }

        if (current.type == TransactionType.TRANSFER && current.toWalletId == walletId) {
            _state.update { it.copy(error = "Source aur destination alag hone chahiye") }
            return
        }

        _state.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val transaction = Transaction(
                type = current.type,
                amount = current.amountInPaisa,
                walletId = walletId,
                categoryId = if (current.type == TransactionType.TRANSFER) null else current.selectedCategoryId,
                toWalletId = if (current.type == TransactionType.TRANSFER) current.toWalletId else null,
                date = current.date,
                note = current.note.ifBlank { null },
                attachmentPath = current.attachmentPath,
                originalText = current.smartEntryText.ifBlank { null },
                createdAt = now,
                updatedAt = now
            )

            when (val result = transactionRepository.create(transaction)) {
                is Result.Success -> {
                    onComplete(true)
                    _state.update { it.copy(isSaving = false) }
                }
                is Result.Failure -> {
                    _state.update {
                        it.copy(isSaving = false, error = result.error.userMessage)
                    }
                    onComplete(false)
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun reset() {
        _state.value = QuickAddState()
    }
}

data class QuickAddState(
    val type: TransactionType = TransactionType.EXPENSE,
    val amountText: String = "",
    val amountInPaisa: Long = 0L,
    val selectedCategoryId: Long? = null,
    val selectedWalletId: Long? = null,
    val toWalletId: Long? = null,
    val date: Long = System.currentTimeMillis(),
    val note: String = "",
    val attachmentPath: String? = null,
    val smartEntryText: String = "",
    val smartEntryResult: SmartEntryResult? = null,
    val isSaving: Boolean = false,
    val error: String? = null
)
