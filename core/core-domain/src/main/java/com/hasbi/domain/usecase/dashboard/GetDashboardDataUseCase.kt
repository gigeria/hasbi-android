package com.hasbi.domain.usecase.dashboard

import com.hasbi.database.entity.BudgetCycleEntity
import com.hasbi.domain.model.DailySafeSpend
import com.hasbi.domain.model.Transaction
import com.hasbi.domain.model.WalletWithBalance
import com.hasbi.domain.repository.BudgetRepository
import com.hasbi.domain.repository.TransactionRepository
import com.hasbi.domain.repository.WalletRepository
import com.hasbi.util.date.DateTimeUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.time.ZoneId
import javax.inject.Inject

/**
 * Aggregate all data needed for Dashboard.
 *
 * FIX BAQI-001: Uses ACTUAL cycle expense (cycleStart to cycleEnd)
 * instead of calendar month expense for Aaj Ka Baqi calculation.
 *
 * Uses flatMapLatest to switch on current cycle — when cycle changes,
 * the cycle expense flow automatically switches to the new date range.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GetDashboardDataUseCase @Inject constructor(
    private val walletRepository: WalletRepository,
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: TransactionRepository,
    private val calculateDailySafeSpend: CalculateDailySafeSpendUseCase
) {

    operator fun invoke(): Flow<DashboardData> {
        val zone = ZoneId.of(DateTimeUtils.DEFAULT_TIMEZONE)
        val now = System.currentTimeMillis()

        val todayStart = DateTimeUtils.startOfDay(now, zone)
        val todayEnd = DateTimeUtils.endOfDay(now, zone)

        val today = java.time.LocalDate.now(zone)
        val monthStart = DateTimeUtils.startOfMonth(today.year, today.monthValue, zone)
        val monthEnd = DateTimeUtils.endOfMonth(today.year, today.monthValue, zone)

        // First observe current cycle, then flatMap to combine all other flows
        return budgetRepository.observeCurrentCycle().flatMapLatest { cycle ->

            // Determine cycle date range for cycleSpent calculation
            val cycleStart = cycle?.startDate ?: 0L
            val cycleEnd = cycle?.endDate ?: Long.MAX_VALUE

            combine(
                walletRepository.observeAllWithBalance(),
                flowOf(cycle),
                transactionRepository.observeRecent(5),
                transactionRepository.observeTodaySpent(todayStart, todayEnd),
                transactionRepository.observeTotalExpense(monthStart, monthEnd),
                transactionRepository.observeTotalIncome(monthStart, monthEnd),
                // FIX: Use actual cycle expense, NOT calendar month expense
                transactionRepository.observeTotalExpense(cycleStart, cycleEnd)
            ) { values ->
                @Suppress("UNCHECKED_CAST")
                val wallets = values[0] as List<WalletWithBalance>
                val currentCycle = values[1] as BudgetCycleEntity?
                @Suppress("UNCHECKED_CAST")
                val recentTransactions = values[2] as List<Transaction>
                val todaySpent = values[3] as Long
                val monthExpense = values[4] as Long
                val monthIncome = values[5] as Long
                // This is the FIX — actual cycle expense, not month expense
                val cycleExpense = values[6] as Long

                // Use cycleExpense for Aaj Ka Baqi (NOT monthExpense)
                val aajKaBaqi = calculateDailySafeSpend(currentCycle, cycleExpense, now, zone)

                DashboardData(
                    wallets = wallets,
                    currentCycle = currentCycle,
                    aajKaBaqi = aajKaBaqi,
                    recentTransactions = recentTransactions,
                    todaySpent = todaySpent,
                    monthExpense = monthExpense,
                    monthIncome = monthIncome,
                    cycleExpense = cycleExpense,
                    totalBalance = wallets.sumOf { it.balance }
                )
            }
        }
    }
}

data class DashboardData(
    val wallets: List<WalletWithBalance>,
    val currentCycle: BudgetCycleEntity?,
    val aajKaBaqi: DailySafeSpend?,
    val recentTransactions: List<Transaction>,
    val todaySpent: Long,
    val monthExpense: Long,
    val monthIncome: Long,
    val cycleExpense: Long = 0L,
    val totalBalance: Long
)
