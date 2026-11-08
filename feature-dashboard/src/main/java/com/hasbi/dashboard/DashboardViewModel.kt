package com.hasbi.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasbi.database.entity.CycleType
import com.hasbi.domain.model.DailySafeSpend
import com.hasbi.domain.model.Transaction
import com.hasbi.domain.model.WalletWithBalance
import com.hasbi.domain.repository.SettingsRepository
import com.hasbi.domain.usecase.budget.EnsureCurrentCycleUseCase
import com.hasbi.domain.usecase.dashboard.CalculateDailySafeSpendUseCase
import com.hasbi.domain.usecase.dashboard.GetDashboardDataUseCase
import com.hasbi.util.date.DateTimeUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZoneId
import javax.inject.Inject

/**
 * Dashboard ViewModel.
 *
 * Combines multiple data streams:
 * - Wallets with balances
 * - Current budget cycle + spent
 * - Aaj Ka Baqi calculation (Daily Safe Spend)
 * - Recent transactions (5 latest)
 * - Today's spend
 * - Monthly income + expense
 *
 * UI state pattern: Loading → Success / Error
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardDataUseCase: GetDashboardDataUseCase,
    private val calculateDailySafeSpendUseCase: CalculateDailySafeSpendUseCase,
    private val ensureCurrentCycleUseCase: EnsureCurrentCycleUseCase,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _isBalanceVisible = MutableStateFlow(true)
    val isBalanceVisible: StateFlow<Boolean> = _isBalanceVisible.asStateFlow()

    val uiState: StateFlow<DashboardUiState> = combine(
        getDashboardDataUseCase(),
        _isBalanceVisible
    ) { dashboardData, isBalanceVisible ->
        DashboardUiState.Success(
            data = dashboardData,
            isBalanceVisible = isBalanceVisible
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState.Loading
    )

    init {
        // Ensure a current budget cycle exists
        viewModelScope.launch {
            val settings = settingsRepository.settings
            settings.collect { s ->
                ensureCurrentCycleUseCase(
                    cycleType = s.cycleType,
                    cycleStartDay = s.cycleStartDate
                )
            }
        }
    }

    fun toggleBalanceVisibility() {
        _isBalanceVisible.update { !it }
    }
}

/**
 * Dashboard UI state — sealed for Loading/Success/Error pattern.
 */
sealed class DashboardUiState {
    data object Loading : DashboardUiState()

    data class Success(
        val data: com.hasbi.domain.usecase.dashboard.DashboardData,
        val isBalanceVisible: Boolean = true
    ) : DashboardUiState() {
        val totalBalance: Long get() = data.totalBalance
        val wallets: List<WalletWithBalance> get() = data.wallets
        val aajKaBaqi: DailySafeSpend? get() = data.aajKaBaqi
        val recentTransactions: List<Transaction> get() = data.recentTransactions
        val todaySpent: Long get() = data.todaySpent
        val monthExpense: Long get() = data.monthExpense
        val monthIncome: Long get() = data.monthIncome
        val currentCycle get() = data.currentCycle

        val hasWallets: Boolean get() = wallets.isNotEmpty()
        val hasTransactions: Boolean get() = recentTransactions.isNotEmpty()
        val hasBudget: Boolean get() = aajKaBaqi?.hasBudget == true
    }

    data class Error(val message: String) : DashboardUiState()
}
