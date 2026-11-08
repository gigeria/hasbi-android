package com.hasbi.domain.model

import com.hasbi.database.entity.TransactionType

/**
 * Smart Entry parser result.
 *
 * Returned by ParseSmartEntryUseCase.
 * Confidence score 0.0-1.0:
 *   - amount found: +0.4
 *   - category matched: +0.4
 *   - wallet matched: +0.2
 *
 * UI shows confidence indicator if < 0.8.
 * User can always edit before saving.
 */
data class SmartEntryResult(
    val amount: Long,
    val category: Category?,
    val wallet: Wallet?,
    val type: TransactionType,
    val confidence: Float,
    val originalText: String
) {
    val isHighConfidence: Boolean get() = confidence >= 0.8f
    val isPartialMatch: Boolean get() = confidence in 0.4f..0.8f
    val isLowConfidence: Boolean get() = confidence < 0.4f

    companion object {
        fun empty(originalText: String) = SmartEntryResult(
            amount = 0L,
            category = null,
            wallet = null,
            type = TransactionType.EXPENSE,
            confidence = 0f,
            originalText = originalText
        )
    }
}
