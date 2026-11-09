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
 * FIX BAQI-002: Projection now uses elapsedDays directly (not elapsedDays-1).
 * On day 1, projection is conservative (uses full elapsedDays).
 */
class CalculateDailySafeSpendUseCase @Inject constructor() {

    operator fun invoke(
        cycle: BudgetCycleEntity?,
        cycleSpent: Long,
        now: Long = System.currentTimeMillis(),
        zone: ZoneId = ZoneId.of(DateTimeUtils.DEFAULT_TIMEZONE)
    ): DailySafeSpend? {
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

        // FIX BAQI-002: Use elapsedDays directly (min 1) for projection
        // Day 1: elapsedDays=1, rate = cycleSpent/1 (conservative enough)
        // Day 2: elapsedDays=2, rate = cycleSpent/2 (averaged)
        val effectiveElapsedDays = maxOf(1, elapsedDays)
        val currentDailyRate = if (cycleSpent > 0) cycleSpent / effectiveElapsedDays else 0L
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
