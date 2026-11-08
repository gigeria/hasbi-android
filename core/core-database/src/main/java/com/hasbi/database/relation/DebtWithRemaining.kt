package com.hasbi.database.relation

import com.hasbi.database.entity.DebtEntity
import com.hasbi.database.entity.DebtType

/**
 * Debt with computed remaining amount.
 *
 * Computed via SQL from linked transactions:
 *   - For LENT: originalAmount - SUM(DEBT_REPAYMENT_IN linked to this debt)
 *   - For BORROWED: originalAmount - SUM(DEBT_REPAYMENT_OUT linked to this debt)
 *
 * `isSettled` = (remainingAmount == 0)
 */
data class DebtWithRemaining(
    val debt: DebtEntity,
    val remainingAmount: Long
) {
    val isSettled: Boolean get() = remainingAmount <= 0
    val type: DebtType get() = debt.type
    val personName: String get() = debt.personName
    val originalAmount: Long get() = debt.originalAmount
}
