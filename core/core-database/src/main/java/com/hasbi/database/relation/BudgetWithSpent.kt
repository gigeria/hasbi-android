package com.hasbi.database.relation

/**
 * Budget with computed spent amount.
 *
 * `spent` is calculated via SQL aggregation from transactions:
 *   - For overall budget (categoryId IS NULL): sum of all EXPENSE in cycle
 *   - For category budget: sum of EXPENSE in cycle with matching categoryId
 *
 * Computed live via Flow — no stale cache.
 */
data class BudgetWithSpent(
    val id: Long,
    val cycleId: Long,
    val categoryId: Long?,
    val limit: Long,
    val warningThreshold: Float,
    val createdAt: Long,
    val updatedAt: Long,
    val spent: Long
) {
    /** 0..1 progress. Can exceed 1 if over-budget. */
    val progress: Float get() = if (limit > 0) spent.toFloat() / limit else 0f

    /** Remaining amount (negative if over-budget). */
    val remaining: Long get() = limit - spent

    /** True if spent >= limit. */
    val isOverBudget: Boolean get() = spent >= limit

    /** True if spent >= limit * warningThreshold. */
    val isWarning: Boolean get() = spent >= (limit * warningThreshold).toLong()
}
