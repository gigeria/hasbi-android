package com.hasbi.wallets.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasbi.database.entity.WalletType
import com.hasbi.domain.model.Wallet
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
class AddWalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddWalletState())
    val state: StateFlow<AddWalletState> = _state.asStateFlow()

    fun setType(type: WalletType) {
        val (icon, color, name) = when (type) {
            WalletType.CASH -> Triple("💵", "#4CAF80", "Cash")
            WalletType.BANK -> Triple("🏦", "#2196F3", "Bank Account")
            WalletType.MOBILE_WALLET -> Triple("📱", "#E91E63", "Mobile Wallet")
            WalletType.CREDIT_CARD -> Triple("💳", "#FF9800", "Credit Card")
            WalletType.SAVINGS -> Triple("🐷", "#9C27B0", "Savings")
            WalletType.CUSTOM -> Triple("⚙️", "#607D8B", "Custom")
        }
        _state.update {
            it.copy(
                type = type,
                icon = icon,
                colorHex = color,
                name = if (it.name.isBlank() || it.name in listOf("Cash", "Bank Account", "Mobile Wallet", "Credit Card", "Savings", "Custom")) name else it.name
            )
        }
    }

    fun setName(name: String) {
        _state.update { it.copy(name = name) }
    }

    fun setInitialBalance(balanceText: String) {
        val cleaned = balanceText.filter { it.isDigit() }
        val amountInRs = cleaned.toLongOrNull() ?: 0L
        _state.update {
            it.copy(balanceText = cleaned, initialBalanceInPaisa = amountInRs * 100)
        }
    }

    fun setIcon(icon: String) {
        _state.update { it.copy(icon = icon) }
    }

    fun setColor(hex: String) {
        _state.update { it.copy(colorHex = hex) }
    }

    fun save(onComplete: (Boolean) -> Unit) {
        val current = _state.value

        if (current.name.isBlank()) {
            _state.update { it.copy(error = "Wallet ka naam daalein") }
            return
        }

        _state.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val wallet = Wallet(
                name = current.name,
                type = current.type,
                icon = current.icon,
                colorHex = current.colorHex,
                initialBalance = current.initialBalanceInPaisa,
                currencyCode = "PKR",
                sortOrder = 0,
                createdAt = now,
                updatedAt = now
            )

            when (val result = walletRepository.create(wallet)) {
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
        _state.value = AddWalletState()
    }
}

data class AddWalletState(
    val type: WalletType = WalletType.CASH,
    val name: String = "Cash",
    val icon: String = "💵",
    val colorHex: String = "#4CAF80",
    val balanceText: String = "",
    val initialBalanceInPaisa: Long = 0L,
    val isSaving: Boolean = false,
    val error: String? = null
)
