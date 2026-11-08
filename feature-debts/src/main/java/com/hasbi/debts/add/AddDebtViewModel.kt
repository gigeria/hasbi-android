package com.hasbi.debts.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasbi.database.entity.DebtEntity
import com.hasbi.database.entity.DebtType
import com.hasbi.domain.repository.DebtRepository
import com.hasbi.domain.repository.WalletRepository
import com.hasbi.util.result.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddDebtViewModel @Inject constructor(
    private val debtRepository: DebtRepository,
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddDebtState())
    val state: StateFlow<AddDebtState> = _state.asStateFlow()

    val wallets = walletRepository.observeVisible()

    fun setType(type: DebtType) {
        _state.update { it.copy(type = type) }
    }

    fun setPersonName(name: String) {
        _state.update { it.copy(personName = name) }
    }

    fun setPersonPhone(phone: String) {
        _state.update { it.copy(personPhone = phone) }
    }

    fun setAmount(amountText: String) {
        val cleaned = amountText.filter { it.isDigit() }
        val amountInRs = cleaned.toLongOrNull() ?: 0L
        _state.update {
            it.copy(amountText = cleaned, amountInPaisa = amountInRs * 100)
        }
    }

    fun setWalletId(walletId: Long) {
        _state.update { it.copy(selectedWalletId = walletId) }
    }

    fun setNote(note: String) {
        _state.update { it.copy(note = note) }
    }

    fun save(onComplete: (Boolean) -> Unit) {
        val current = _state.value

        if (current.personName.isBlank()) {
            _state.update { it.copy(error = "Person ka naam daalein") }
            return
        }
        if (current.amountInPaisa <= 0) {
            _state.update { it.copy(error = "Amount 0 se zyada honi chahiye") }
            return
        }
        if (current.selectedWalletId == null) {
            _state.update { it.copy(error = "Wallet select karein") }
            return
        }

        _state.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val debt = DebtEntity(
                type = current.type,
                personName = current.personName,
                personPhone = current.personPhone.ifBlank { null },
                originalAmount = current.amountInPaisa,
                currencyCode = "PKR",
                startDate = now,
                note = current.note.ifBlank { null },
                createdAt = now,
                updatedAt = now
            )

            when (val result = debtRepository.create(debt, current.selectedWalletId)) {
                is Result.Success -> {
                    _state.update { it.copy(isSaving = false) }
                    onComplete(true)
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
        _state.value = AddDebtState()
    }
}

data class AddDebtState(
    val type: DebtType = DebtType.LENT,
    val personName: String = "",
    val personPhone: String = "",
    val amountText: String = "",
    val amountInPaisa: Long = 0L,
    val selectedWalletId: Long? = null,
    val note: String = "",
    val isSaving: Boolean = false,
    val error: String? = null
)
