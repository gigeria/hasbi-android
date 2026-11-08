package com.hasbi.domain.model

import com.hasbi.database.entity.CycleType

/**
 * Aaj Ka Baqi — Hasbi's hero feature.
 *
 * Calculates how much user can safely spend today.
 *
 * Formula:
 *   remainingBudget = cycleBudget - cycleSpent
 *   remainingDays = daysBetween(today, cycleEnd) + 1  // include today
 *   dailySafeSpend = if (remainingDays > 0) remainingBudget / remainingDays else remainingBudget
 *
 * Projection:
 *   elapsedDays = totalCycleDays - remainingDays + 1
 *   currentDailyRate = cycleSpent / max(1, elapsedDays - 1)
 *   projectedTotal = currentDailyRate * totalCycleDays
 *   projectedEnd = cycleBudget - projectedTotal
 */
data class DailySafeSpend(
    val remainingBudget: Long,                 // in minor units
    val remainingDays: Int,
    val dailySafeSpend: Long,                  // in minor units
    val totalBudget: Long,
    val cycleSpent: Long,
    val projectedEndOfMonth: Long,             // projected savings (+) or overspend (-)
    val isOverBudget: Boolean,
    val warningLevel: WarningLevel,
    val cycleStartDate: Long,
    val cycleEndDate: Long
) {
    val hasBudget: Boolean get() = totalBudget > 0
}

enum class WarningLevel { GREEN, AMBER, RED }
