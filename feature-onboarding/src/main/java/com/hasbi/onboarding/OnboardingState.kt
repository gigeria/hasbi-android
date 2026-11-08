package com.hasbi.onboarding

import com.hasbi.database.entity.CycleType
import com.hasbi.database.entity.WalletType

/**
 * Onboarding UI state.
 *
 * Persists across screen rotations + app kills (via DataStore).
 *
 * 3 screens:
 *   - Step 0: Welcome (Assalam greeting + trust badges)
 *   - Step 1: Wallet setup (Cash + Bank pre-selected, others optional)
 *   - Step 2: Budget setup (cycle type + monthly budget amount)
 */
data class OnboardingState(
    val currentStep: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isComplete: Boolean = false,

    // === Wallet selections ===
    val selectedWalletTypes: Set<WalletSelection> = setOf(
        WalletSelection.CASH,
        WalletSelection.BANK
    ),
    val walletNames: Map<WalletSelection, String> = mapOf(
        WalletSelection.CASH to "Cash",
        WalletSelection.BANK to "Bank Account"
    ),
    val walletBalances: Map<WalletSelection, Long> = emptyMap(),

    // === Budget setup ===
    val cycleType: CycleType = CycleType.MONTHLY,
    val cycleStartDay: Int = 1,
    val monthlyBudget: Long = 0L  // in paisa (0 = no budget set)
)

/**
 * Wallet selection options shown on onboarding.
 *
 * Defaults:
 *   - CASH pre-selected (always)
 *   - BANK pre-selected (default name "Bank Account", balance 0)
 *   - Others optional
 */
enum class WalletSelection(
    val walletType: WalletType,
    val displayName: String,
    val defaultIcon: String,
    val defaultColorHex: String
) {
    CASH(WalletType.CASH, "Cash", "💵", "#4CAF80"),
    BANK(WalletType.BANK, "Bank Account", "🏦", "#2196F3"),
    JAZZCASH(WalletType.MOBILE_WALLET, "JazzCash", "📱", "#E91E63"),
    EASYPAY(WalletType.MOBILE_WALLET, "Easypaisa", "📱", "#00BCD4"),
    SADAPAY(WalletType.MOBILE_WALLET, "SadaPay", "💳", "#9C27B0"),
    CREDIT_CARD(WalletType.CREDIT_CARD, "Credit Card", "💳", "#FF9800")
}
