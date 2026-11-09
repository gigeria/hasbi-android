package com.hasbi.transactions.quickadd

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasbi.database.entity.TransactionType
import com.hasbi.domain.model.Transaction
import com.hasbi.domain.repository.TransactionRepository
import com.hasbi.util.result.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditTransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(EditTransactionState())
    val state: StateFlow<EditTransactionState> = _state.asStateFlow()

    fun loadTransaction(id: Long) {
        viewModelScope.launch {
            val tx = transactionRepository.getById(id)
            if (tx != null) {
                _state.update {
                    it.copy(
                        transactionId = tx.id,
                        type = tx.type,
                        amountText = (tx.amount / 100).toString(),
                        amountInPaisa = tx.amount,
                        selectedCategoryId = tx.categoryId,
                        selectedWalletId = tx.walletId,
                        toWalletId = tx.toWalletId,
                        date = tx.date,
                        note = tx.note ?: "",
                        isLoaded = true,
                        error = null
                    )
                }
            } else {
                _state.update { it.copy(error = "Transaction not found") }
            }
        }
    }

    fun setType(type: TransactionType) {
        _state.update { it.copy(type = type, selectedCategoryId = null) }
    }

    fun setAmount(amountText: String) {
        val cleaned = amountText.filter { it.isDigit() }
        val amountInRs = cleaned.toLongOrNull() ?: 0L
        _state.update { it.copy(amountText = cleaned, amountInPaisa = amountInRs * 100) }
    }

    fun setCategoryId(id: Long?) {
        _state.update { it.copy(selectedCategoryId = id) }
    }

    fun setWalletId(id: Long) {
        _state.update { it.copy(selectedWalletId = id) }
    }

    fun setDate(date: Long) {
        _state.update { it.copy(date = date) }
    }

    fun setNote(note: String) {
        _state.update { it.copy(note = note) }
    }

    fun save(onComplete: (Boolean) -> Unit) {
        val current = _state.value
        if (current.amountInPaisa <= 0) {
            _state.update { it.copy(error = "Amount 0 se zyada honi chahiye") }
            return
        }

        _state.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val transaction = Transaction(
                id = current.transactionId,
                type = current.type,
                amount = current.amountInPaisa,
                walletId = current.selectedWalletId,
                categoryId = if (current.type == TransactionType.TRANSFER) null else current.selectedCategoryId,
                toWalletId = if (current.type == TransactionType.TRANSFER) current.toWalletId else null,
                date = current.date,
                note = current.note.ifBlank { null },
                createdAt = now,
                updatedAt = now
            )

            when (val result = transactionRepository.update(transaction)) {
                is Result.Success -> {
                    _state.update { it.copy(isSaving = false) }
                    onComplete(true)
                }
                is Result.Failure -> {
                    _state.update { it.copy(isSaving = false, error = result.error.userMessage) }
                    onComplete(false)
                }
            }
        }
    }

    fun delete(onComplete: () -> Unit) {
        val id = _state.value.transactionId
        viewModelScope.launch {
            transactionRepository.softDelete(id)
            onComplete()
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

data class EditTransactionState(
    val transactionId: Long = 0,
    val type: TransactionType = TransactionType.EXPENSE,
    val amountText: String = "",
    val amountInPaisa: Long = 0L,
    val selectedCategoryId: Long? = null,
    val selectedWalletId: Long? = null,
    val toWalletId: Long? = null,
    val date: Long = System.currentTimeMillis(),
    val note: String = "",
    val isLoaded: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
)
