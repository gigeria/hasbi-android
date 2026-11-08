package com.hasbi.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.hasbi.database.entity.BudgetCycleEntity
import com.hasbi.database.entity.BudgetEntity
import com.hasbi.database.entity.CategoryEntity
import com.hasbi.database.entity.CurrencyEntity
import com.hasbi.database.entity.WalletEntity

/**
 * Onboarding DAO.
 *
 * Critical: `completeOnboarding` is atomic — all inserts in single @Transaction.
 * If any insert fails, all roll back. No partial state on crash.
 *
 * Called by CompleteOnboardingUseCase (Week 4) when user finishes onboarding.
 */
@Dao
abstract class OnboardingDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertCurrencies(currencies: List<CurrencyEntity>)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insertWallet(wallet: WalletEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertCategories(categories: List<CategoryEntity>): LongArray

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insertCycle(cycle: BudgetCycleEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insertBudgets(budgets: List<BudgetEntity>): LongArray

    /**
     * Atomic onboarding completion.
     *
     * @param currencies Default + user-selected currencies (PKR + others)
     * @param wallets User-selected wallets (Cash + Bank + custom)
     * @param categories Default expense + income categories
     * @param cycle Initial budget cycle (typically monthly starting 1st)
     * @param budgets Overall + category-specific budgets (can be empty if user skipped)
     * @return OnboardingResult with created wallet IDs and cycle ID
     */
    @Transaction
    open suspend fun completeOnboarding(
        currencies: List<CurrencyEntity>,
        wallets: List<WalletEntity>,
        categories: List<CategoryEntity>,
        cycle: BudgetCycleEntity,
        budgets: List<BudgetEntity>
    ): OnboardingResult {
        // All inserts in single transaction — atomic, no partial state
        insertCurrencies(currencies)

        val walletIds = wallets.map { insertWallet(it) }

        insertCategories(categories)

        val cycleId = insertCycle(cycle)

        // Attach cycleId to budgets (if any)
        if (budgets.isNotEmpty()) {
            val budgetsWithCycleId = budgets.map { it.copy(cycleId = cycleId) }
            insertBudgets(budgetsWithCycleId)
        }

        return OnboardingResult(
            walletIds = walletIds,
            cycleId = cycleId
        )
    }
}

data class OnboardingResult(
    val walletIds: List<Long>,
    val cycleId: Long
)
