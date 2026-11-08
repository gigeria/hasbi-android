package com.hasbi.domain.usecase.dashboard

import com.hasbi.database.entity.BudgetCycleEntity
import com.hasbi.domain.model.DailySafeSpend
import com.hasbi.domain.model.WarningLevel
import com.hasbi.util.date.DateTimeUtils
import java.time.ZoneId
import javax.inject.Inject

/**
 * Calculate "Aaj Ka Baqi" — Hasbi's hero feature.
 *
 * Formula:
 *   remainingBudget = cycleBudget - cycleSpent
 *   remainingDays = daysBetween(today, cycleEnd) + 1   // include today
 *   dailySafeSpend = if (remainingDays > 0) remainingBudget / remainingDays else remainingBudget
 *
 * Projection:
 *   elapsedDays = totalCycleDays - remainingDays + 1
 *   currentDailyRate = cycleSpent / max(1, elapsedDays - 1)
 *   projectedTotal = currentDailyRate * totalCycleDays
 *   projectedEnd = cycleBudget - projectedTotal
 *
 * Warning levels:
 *   - GREEN: remainingBudget > 10% of totalBudget
 *   - AMBER: 0 < remainingBudget <= 10%
 *   - RED: remainingBudget <= 0
 */
class CalculateDailySafeSpendUseCase @Inject constructor() {

    operator fun invoke(
        cycle: BudgetCycleEntity?,
        cycleSpent: Long,
        now: Long = System.currentTimeMillis(),
        zone: ZoneId = ZoneId.of(DateTimeUtils.DEFAULT_TIMEZONE)
    ): DailySafeSpend? {
        // No budget configured
        if (cycle == null || cycle.totalBudget <= 0) {
            return null
        }

        val totalBudget = cycle.totalBudget
        val cycleStart = cycle.startDate
        val cycleEnd = cycle.endDate

        val remainingBudget = totalBudget - cycleSpent
        val remainingDays = DateTimeUtils.daysRemaining(cycleEnd, now, zone)
        val totalDays = DateTimeUtils.totalDaysInCycle(cycleStart, cycleEnd, zone)
        val elapsedDays = DateTimeUtils.daysElapsed(cycleStart, now, zone)

        val dailySafeSpend = if (remainingDays > 0 && remainingBudget > 0) {
            remainingBudget / remainingDays
        } else {
            0L
        }

        // Projection: if current pace continues, where will we land?
        val effectiveElapsedDays = maxOf(1, elapsedDays - 1)
        val currentDailyRate = if (elapsedDays > 0) cycleSpent / effectiveElapsedDays else 0L
        val projectedTotal = currentDailyRate * totalDays
        val projectedEndOfMonth = totalBudget - projectedTotal

        val isOverBudget = remainingBudget < 0
        val warningLevel = when {
            isOverBudget -> WarningLevel.RED
            remainingBudget < totalBudget * 0.1 -> WarningLevel.AMBER
            else -> WarningLevel.GREEN
        }

        return DailySafeSpend(
            remainingBudget = remainingBudget,
            remainingDays = remainingDays,
            dailySafeSpend = dailySafeSpend,
            totalBudget = totalBudget,
            cycleSpent = cycleSpent,
            projectedEndOfMonth = projectedEndOfMonth,
            isOverBudget = isOverBudget,
            warningLevel = warningLevel,
            cycleStartDate = cycleStart,
            cycleEndDate = cycleEnd
        )
    }
}
