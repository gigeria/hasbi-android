package com.hasbi.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasbi.database.entity.CycleType
import com.hasbi.domain.usecase.onboarding.CompleteOnboardingUseCase
import com.hasbi.domain.usecase.onboarding.OnboardingRequest
import com.hasbi.domain.usecase.onboarding.OnboardingWallet
import com.hasbi.datastore.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Onboarding ViewModel — manages 3-screen onboarding flow.
 *
 * Features:
 * - State preservation (survives screen rotation + app kill via DataStore)
 * - Skip paths:
 *   - Skip on Welcome → Cash wallet + Dashboard
 *   - Skip on Wallet setup → Cash only + Budget screen
 *   - Skip on Budget → Default monthly cycle + Dashboard
 * - Atomic completion (single Room @Transaction)
 * - Back navigation preserves form state
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val completeOnboardingUseCase: CompleteOnboardingUseCase,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    init {
        // Restore onboarding state if app was killed mid-onboarding
        viewModelScope.launch {
            settingsDataStore.onboardingState.collect { saved ->
                if (saved.currentStep > 0) {
                    _state.update {
                        it.copy(
                            currentStep = saved.currentStep,
                            cycleType = saved.cycleType,
                            cycleStartDay = saved.cycleStartDate,
                            monthlyBudget = saved.monthlyBudget
                        )
                    }
                }
            }
        }
    }

    // === Navigation ===

    fun goToStep(step: Int) {
        _state.update { it.copy(currentStep = step.coerceIn(0, 2)) }
        viewModelScope.launch {
            settingsDataStore.setOnboardingStep(step)
        }
    }

    fun nextStep() = goToStep(_state.value.currentStep + 1)
    fun previousStep() = goToStep(_state.value.currentStep - 1)

    // === Wallet selection ===

    fun toggleWallet(selection: WalletSelection) {
        _state.update { current ->
            val newSelections = if (selection in current.selectedWalletTypes) {
                current.selectedWalletTypes - selection
            } else {
                current.selectedWalletTypes + selection
            }
            current.copy(selectedWalletTypes = newSelections)
        }
        persistWallets()
    }

    fun updateWalletName(selection: WalletSelection, name: String) {
        _state.update { current ->
            current.copy(walletNames = current.walletNames + (selection to name))
        }
    }

    fun updateWalletBalance(selection: WalletSelection, balance: Long) {
        _state.update { current ->
            current.copy(walletBalances = current.walletBalances + (selection to balance))
        }
    }

    // === Budget setup ===

    fun setCycleType(type: CycleType) {
        _state.update { it.copy(cycleType = type) }
        viewModelScope.launch {
            settingsDataStore.setOnboardingCycleType(type)
        }
    }

    fun setCycleStartDay(day: Int) {
        val safeDay = day.coerceIn(1, 31)
        _state.update { it.copy(cycleStartDay = safeDay) }
        viewModelScope.launch {
            settingsDataStore.setOnboardingCycleStart(safeDay)
        }
    }

    fun setMonthlyBudget(amountInPaisa: Long) {
        val safeAmount = amountInPaisa.coerceAtLeast(0)
        _state.update { it.copy(monthlyBudget = safeAmount) }
        viewModelScope.launch {
            settingsDataStore.setOnboardingBudget(safeAmount)
        }
    }

    // === Skip paths ===

    /**
     * Skip on Welcome → creates only Cash wallet + goes to Dashboard.
     */
    fun skipFromWelcome() {
        _state.update {
            it.copy(
                selectedWalletTypes = setOf(WalletSelection.CASH),
                walletNames = mapOf(WalletSelection.CASH to "Cash"),
                cycleType = CycleType.MONTHLY,
                cycleStartDay = 1,
                monthlyBudget = 0L
            )
        }
        completeOnboarding()
    }

    /**
     * Skip on Wallet setup → Cash only (if nothing selected) + go to Budget.
     */
    fun skipFromWalletSetup() {
        if (_state.value.selectedWalletTypes.isEmpty()) {
            _state.update {
                it.copy(
                    selectedWalletTypes = setOf(WalletSelection.CASH),
                    walletNames = mapOf(WalletSelection.CASH to "Cash")
                )
            }
        }
        goToStep(2)
    }

    /**
     * Skip on Budget setup → default monthly cycle, no budget → Dashboard.
     */
    fun skipFromBudgetSetup() {
        _state.update {
            it.copy(
                cycleType = CycleType.MONTHLY,
                cycleStartDay = 1,
                monthlyBudget = 0L
            )
        }
        completeOnboarding()
    }

    // === Completion ===

    fun completeOnboarding() {
        val currentState = _state.value

        if (currentState.selectedWalletTypes.isEmpty()) {
            _state.update { it.copy(error = "Kam az kam ek wallet select karein") }
            return
        }

        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val wallets = currentState.selectedWalletTypes.mapIndexed { index, selection ->
                OnboardingWallet(
                    name = currentState.walletNames[selection] ?: selection.displayName,
                    type = selection.walletType,
                    icon = selection.defaultIcon,
                    colorHex = selection.defaultColorHex,
                    initialBalance = currentState.walletBalances[selection] ?: 0L
                )
            }

            val request = OnboardingRequest(
                wallets = wallets,
                cycleType = currentState.cycleType,
                cycleStartDay = currentState.cycleStartDay,
                monthlyBudget = currentState.monthlyBudget
            )

            when (val result = completeOnboardingUseCase(request)) {
                is com.hasbi.util.result.Result.Success -> {
                    _state.update { it.copy(isLoading = false, isComplete = true) }
                }
                is com.hasbi.util.result.Result.Failure -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = result.error.userMessage
                        )
                    }
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    private fun persistWallets() {
        viewModelScope.launch {
            settingsDataStore.setOnboardingWallets(
                _state.value.selectedWalletTypes.map { it.name }.toSet()
            )
        }
    }
}
