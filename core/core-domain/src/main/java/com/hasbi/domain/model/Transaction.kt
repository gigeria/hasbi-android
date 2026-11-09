package com.hasbi.domain.model

import com.hasbi.database.entity.FinancialPurpose
import com.hasbi.database.entity.ReconciliationStatus
import com.hasbi.database.entity.TransactionSource
import com.hasbi.database.entity.TransactionType

/**
 * Transaction domain model — pure Kotlin.
 *
 * Includes all future-ready fields (tax + bank import) even though
 * they're null in Phase 1.
 */
data class Transaction(
    val id: Long = 0,
    val type: TransactionType,
    val amount: Long,                         // always positive, in minor units
    val walletId: Long,
    val categoryId: Long?,
    val toWalletId: Long? = null,
    val date: Long,
    val note: String? = null,
    val attachmentPath: String? = null,
    val originalText: String? = null,
    val confidenceScore: Float? = null,
    val exchangeRate: Double? = null,
    val convertedAmount: Long? = null,
    val isRecurring: Boolean = false,
    val recurringId: Long? = null,
    val linkedDebtId: Long? = null,
    val projectId: Long? = null,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val purgeAfter: Long? = null,
    // Future-ready (Phase 2+)
    val source: TransactionSource = TransactionSource.MANUAL,
    val externalTransactionId: String? = null,
    val merchantName: String? = null,
    val counterparty: String? = null,
    val bankReference: String? = null,
    val financialPurpose: FinancialPurpose? = null,
    val businessPercentage: Int? = null,
    val taxCategory: String? = null,
    val taxYear: Int? = null,
    val isTaxRelevant: Boolean? = null,
    val reconciliationStatus: ReconciliationStatus? = null,
    val matchedManualTxId: Long? = null,
    val reviewedAt: Long? = null,
    val reviewedBy: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)
