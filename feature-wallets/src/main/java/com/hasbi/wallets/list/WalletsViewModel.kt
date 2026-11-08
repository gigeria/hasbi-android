package com.hasbi.wallets.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasbi.domain.model.Wallet
import com.hasbi.domain.model.WalletWithBalance
import com.hasbi.domain.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletsViewModel @Inject constructor(
    private val walletRepository: WalletRepository
) : ViewModel() {

    val wallets: StateFlow<List<WalletWithBalance>> = walletRepository.observeAllWithBalance()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val visibleWallets: StateFlow<List<Wallet>> = walletRepository.observeVisible()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalBalance: StateFlow<Long> = walletRepository.observeAllWithBalance()
        .map { list -> list.sumOf { it.balance } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0L
        )

    fun hideWallet(id: Long) {
        viewModelScope.launch { walletRepository.setHidden(id, true) }
    }

    fun archiveWallet(id: Long) {
        viewModelScope.launch { walletRepository.archive(id) }
    }
}
