package com.hasbi.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Budget cycle entity — recurring budget period.
 *
 * - `startDate` inclusive, `endDate` EXCLUSIVE (standard pattern)
 * - Both stored as UTC epoch millis
 * - Reporting periods computed using user's local timezone (Asia/Karathi default)
 * - `isCurrent` true for active cycle (only one at a time)
 * - `isClosed` true for manually closed cycles (immutable snapshot)
 * - `carryOver` computed from previous cycle (savings/overspend)
 *
 * Cycle types:
 * - MONTHLY: 1st to end-of-month (default)
 * - SALARY_DATE: user-defined (e.g., 25th to 24th next month)
 *   - Day 29/30/31 edge case: use last day of month if not available
 * - BI_WEEKLY: 14 days
 * - WEEKLY: 7 days
 * - CUSTOM: user picks start + end dates
 */
@Entity(tableName = "budget_cycles")
data class BudgetCycleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cycleType: CycleType,
    val startDate: Long,                          // UTC millis, INCLUSIVE
    val endDate: Long,                            // UTC millis, EXCLUSIVE
    val totalBudget: Long,                        // in minor units
    val isCurrent: Boolean = false,
    val isClosed: Boolean = false,                // manually closed (immutable)
    val carryOver: Long = 0,                      // from previous cycle
    val createdAt: Long,
    val updatedAt: Long
)

enum class CycleType {
    MONTHLY, SALARY_DATE, BI_WEEKLY, WEEKLY, CUSTOM
}
