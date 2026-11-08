package com.hasbi.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasbi.database.entity.BudgetEntity
import com.hasbi.database.relation.BudgetWithSpent
import com.hasbi.domain.repository.BudgetRepository
import com.hasbi.domain.usecase.budget.EnsureCurrentCycleUseCase
import com.hasbi.util.date.DateTimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZoneId
import javax.inject.Inject

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class BudgetsViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val ensureCurrentCycleUseCase: EnsureCurrentCycleUseCase
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val currentCycle = budgetRepository.observeCurrentCycle()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val budgetsWithSpent: StateFlow<List<BudgetWithSpent>> = currentCycle
        .flatMapLatest { cycle ->
            if (cycle != null) {
                budgetRepository.observeBudgetsWithSpent(cycle.id, cycle.startDate, cycle.endDate)
            } else {
                kotlinx.coroutines.flow.flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        ensureCycle()
    }

    fun ensureCycle() {
        viewModelScope.launch {
            ensureCurrentCycleUseCase()
        }
    }

    fun deleteBudget(budget: BudgetEntity) {
        viewModelScope.launch {
            budgetRepository.deleteBudget(budget.id)
        }
    }
}
