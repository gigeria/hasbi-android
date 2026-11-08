package com.hasbi.domain.usecase.onboarding

import com.hasbi.database.dao.OnboardingDao
import com.hasbi.database.dao.OnboardingResult
import com.hasbi.database.entity.BudgetCycleEntity
import com.hasbi.database.entity.BudgetEntity
import com.hasbi.database.entity.CycleType
import com.hasbi.database.entity.WalletEntity
import com.hasbi.database.entity.WalletType
import com.hasbi.database.seed.CategorySeeder
import com.hasbi.database.seed.CurrencySeeder
import com.hasbi.domain.repository.SettingsRepository
import com.hasbi.util.date.DateTimeUtils
import com.hasbi.util.result.HasbiError
import com.hasbi.util.result.Result
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * Atomic onboarding completion.
 *
 * All inserts happen in a single Room @Transaction:
 * 1. Currencies (PKR + others, idempotent)
 * 2. Wallets (Cash + user selections)
 * 3. Categories (15 defaults, idempotent)
 * 4. Budget cycle (monthly starting 1st, or salary-date)
 * 5. Budgets (overall + per-category, if user set any)
 *
 * If any insert fails, all roll back. No partial state on crash.
 *
 * After success, marks onboarding complete in DataStore.
 */
class CompleteOnboardingUseCase @Inject constructor(
    private val onboardingDao: OnboardingDao,
    private val settingsRepository: SettingsRepository
) {

    suspend operator fun invoke(request: OnboardingRequest): Result<OnboardingResult> = try {
        val now = System.currentTimeMillis()
        val zone = ZoneId.of(DateTimeUtils.DEFAULT_TIMEZONE)
        val today = LocalDate.now(zone)

        // 1. Build wallet entities
        val wallets = request.wallets.mapIndexed { index, w ->
            WalletEntity(
                name = w.name,
                type = w.type,
                icon = w.icon,
                colorHex = w.colorHex,
                initialBalance = w.initialBalance,
                currencyCode = "PKR",
                sortOrder = index,
                createdAt = now,
                updatedAt = now
            )
        }

        // 2. Build cycle
        val (cycleStart, cycleEnd) = when (request.cycleType) {
            CycleType.MONTHLY -> DateTimeUtils.monthlyCycle(today, zone)
            CycleType.SALARY_DATE -> DateTimeUtils.salaryDateCycle(
                request.cycleStartDay, today, zone
            )
            else -> DateTimeUtils.monthlyCycle(today, zone)  // fallback
        }

        val cycle = BudgetCycleEntity(
            cycleType = request.cycleType,
            startDate = cycleStart,
            endDate = cycleEnd,
            totalBudget = request.monthlyBudget,
            isCurrent = true,
            createdAt = now,
            updatedAt = now
        )

        // 3. Build budgets (if user set monthly budget)
        val budgets = if (request.monthlyBudget > 0) {
            listOf(
                BudgetEntity(
                    cycleId = 0,  // will be set by DAO
                    categoryId = null,  // overall budget
                    limit = request.monthlyBudget,
                    warningThreshold = 0.8f,
                    createdAt = now,
                    updatedAt = now
                )
            )
        } else {
            emptyList()
        }

        // 4. Atomic insert via DAO @Transaction
        val result = onboardingDao.completeOnboarding(
            currencies = CurrencySeeder.getSeedCurrencies(now),
            wallets = wallets,
            categories = CategorySeeder.getSeedCategories(now),
            cycle = cycle,
            budgets = budgets
        )

        // 5. Mark onboarding complete in DataStore
        settingsRepository.setOnboarded(true, version = 1)

        Result.Success(result)
    } catch (e: Exception) {
        Result.Failure(HasbiError.DatabaseError(e.message ?: "Onboarding failed"))
    }
}

data class OnboardingRequest(
    val wallets: List<OnboardingWallet>,
    val cycleType: CycleType = CycleType.MONTHLY,
    val cycleStartDay: Int = 1,
    val monthlyBudget: Long = 0
)

data class OnboardingWallet(
    val name: String,
    val type: WalletType,
    val icon: String,
    val colorHex: String,
    val initialBalance: Long = 0
)
