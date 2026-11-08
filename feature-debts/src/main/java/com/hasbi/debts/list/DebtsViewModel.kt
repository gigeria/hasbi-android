package com.hasbi.debts.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasbi.database.relation.DebtWithRemaining
import com.hasbi.domain.repository.DebtRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebtsViewModel @Inject constructor(
    private val debtRepository: DebtRepository
) : ViewModel() {

    val lentDebts: StateFlow<List<DebtWithRemaining>> = debtRepository.observeLentWithRemaining()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val borrowedDebts: StateFlow<List<DebtWithRemaining>> = debtRepository.observeBorrowedWithRemaining()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalLent: StateFlow<Long> = debtRepository.observeLentWithRemaining()
        .map { list -> list.sumOf { it.remainingAmount } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0L
        )

    val totalBorrowed: StateFlow<Long> = debtRepository.observeBorrowedWithRemaining()
        .map { list -> list.sumOf { it.remainingAmount } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0L
        )

    fun archiveDebt(id: Long) {
        viewModelScope.launch { debtRepository.archive(id) }
    }
}
