package com.hasbi.domain.usecase.dashboard

import com.hasbi.database.relation.BudgetWithSpent
import com.hasbi.domain.model.DailySafeSpend
import com.hasbi.domain.model.Transaction
import com.hasbi.domain.model.WalletWithBalance
import com.hasbi.domain.repository.BudgetRepository
import com.hasbi.domain.repository.TransactionRepository
import com.hasbi.domain.repository.WalletRepository
import com.hasbi.util.date.DateTimeUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.ZoneId
import javax.inject.Inject

/**
 * Aggregate all data needed for Dashboard.
 *
 * Combines:
 * - Wallets with balances
 * - Current budget cycle + spent
 * - Aaj Ka Baqi calculation
 * - Recent transactions (5 latest)
 * - Today's spend
 * - Monthly income + expense
 */
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

        // Use current month for income/expense stats
        val today = java.time.LocalDate.now(zone)
        val monthStart = DateTimeUtils.startOfMonth(today.year, today.monthValue, zone)
        val monthEnd = DateTimeUtils.endOfMonth(today.year, today.monthValue, zone)

        return combine(
            walletRepository.observeAllWithBalance(),
            budgetRepository.observeCurrentCycle(),
            transactionRepository.observeRecent(5),
            transactionRepository.observeTodaySpent(todayStart, todayEnd),
            transactionRepository.observeTotalExpense(monthStart, monthEnd),
            transactionRepository.observeTotalIncome(monthStart, monthEnd)
        ) { values ->
            @Suppress("UNCHECKED_CAST")
            val wallets = values[0] as List<WalletWithBalance>
            val currentCycle = values[1] as com.hasbi.database.entity.BudgetCycleEntity?
            @Suppress("UNCHECKED_CAST")
            val recentTransactions = values[2] as List<Transaction>
            val todaySpent = values[3] as Long
            val monthExpense = values[4] as Long
            val monthIncome = values[5] as Long

            // Calculate Aaj Ka Baqi
            val cycleSpent = if (currentCycle != null) {
                // This is async — we need a different approach
                // For now, use monthExpense as approximation if cycle is monthly
                monthExpense
            } else 0L

            val aajKaBaqi = calculateDailySafeSpend(currentCycle, cycleSpent, now, zone)

            DashboardData(
                wallets = wallets,
                currentCycle = currentCycle,
                aajKaBaqi = aajKaBaqi,
                recentTransactions = recentTransactions,
                todaySpent = todaySpent,
                monthExpense = monthExpense,
                monthIncome = monthIncome,
                totalBalance = wallets.sumOf { it.balance }
            )
        }
    }
}

data class DashboardData(
    val wallets: List<WalletWithBalance>,
    val currentCycle: com.hasbi.database.entity.BudgetCycleEntity?,
    val aajKaBaqi: DailySafeSpend?,
    val recentTransactions: List<Transaction>,
    val todaySpent: Long,
    val monthExpense: Long,
    val monthIncome: Long,
    val totalBalance: Long
)
