package com.hasbi.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Transaction entity — CORE financial record.
 *
 * Critical rules:
 * - `amount` always positive Long (minor units). Direction determined by `type`.
 * - For TRANSFER: `toWalletId` required, `categoryId` = null
 * - For EXPENSE/INCOME: `toWalletId` = null, `categoryId` optional
 * - Soft delete via `isDeleted` + `deletedAt` + `purgeAfter` (30-day trash)
 *
 * Future-ready fields (Phase 2+, default null in Phase 1):
 * - Bank import: source, externalTransactionId, merchantName, counterparty, bankReference
 * - Tax: financialPurpose, businessPercentage, taxCategory, taxYear, isTaxRelevant
 * - Reconciliation: reconciliationStatus, matchedManualTxId, reviewedAt, reviewedBy
 *
 * All future-ready fields default to null/MANUAL — no UI exposed in Phase 1.
 */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = WalletEntity::class,
            parentColumns = ["id"],
            childColumns = ["walletId"],
            onDelete = ForeignKey.RESTRICT  // Never delete wallet with transactions
        ),
        ForeignKey(
            entity = WalletEntity::class,
            parentColumns = ["id"],
            childColumns = ["toWalletId"],
            onDelete = ForeignKey.RESTRICT,
            deferred = true  // Allows cross-wallet transfers
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL,  // Keep transaction, drop category
            deferred = true
        )
    ],
    indices = [
        // Core indexes for typical queries
        Index(value = ["walletId"]),
        Index(value = ["toWalletId"]),
        Index(value = ["categoryId"]),
        Index(value = ["type"]),
        Index(value = ["date"]),
        Index(value = ["createdAt"]),
        Index(value = ["isDeleted", "purgeAfter"]),  // for purge worker
        Index(value = ["recurringId"]),
        Index(value = ["linkedDebtId"]),

        // Future-ready indexes (Phase 2+)
        Index(value = ["source"]),
        Index(value = ["externalTransactionId"]),
        Index(value = ["taxYear"]),
        Index(value = ["reconciliationStatus"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    // === Core fields ===
    val type: TransactionType,
    val amount: Long,                             // always positive, in minor units
    val walletId: Long,
    val categoryId: Long?,
    val toWalletId: Long? = null,                 // for TRANSFER only
    val date: Long,                               // UTC epoch millis
    val note: String? = null,
    val attachmentPath: String? = null,

    // === Smart Entry ===
    val originalText: String? = null,
    val confidenceScore: Float? = null,

    // === Transfer-specific (multi-currency ready) ===
    val exchangeRate: Double? = null,
    val convertedAmount: Long? = null,

    // === Recurring ===
    val isRecurring: Boolean = false,
    val recurringId: Long? = null,

    // === Debt linkage ===
    val linkedDebtId: Long? = null,

    // === Project linkage (Phase 1.1) ===
    val projectId: Long? = null,

    // === Soft delete (30-day trash) ===
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val purgeAfter: Long? = null,                 // = deletedAt + 30 days

    // === Future-ready: Bank import + Tax (Phase 2+, default null in Phase 1) ===
    val source: TransactionSource = TransactionSource.MANUAL,
    val externalTransactionId: String? = null,
    val merchantName: String? = null,
    val counterparty: String? = null,
    val bankReference: String? = null,

    val financialPurpose: FinancialPurpose? = null,
    val businessPercentage: Int? = null,          // 0-100 if MIXED
    val taxCategory: String? = null,
    val taxYear: Int? = null,
    val isTaxRelevant: Boolean? = null,

    val reconciliationStatus: ReconciliationStatus? = null,
    val matchedManualTxId: Long? = null,
    val reviewedAt: Long? = null,
    val reviewedBy: String? = null,

    // === Audit ===
    val createdAt: Long,
    val updatedAt: Long
)

/**
 * Transaction type.
 *
 * Phase 1: EXPENSE, INCOME, TRANSFER
 * Phase 2+ (debt + savings): DEBT_GIVEN, DEBT_RECEIVED, DEBT_REPAYMENT_OUT, DEBT_REPAYMENT_IN,
 *                             SAVINGS_DEPOSIT, SAVINGS_WITHDRAW
 *
 * Debt/savings types are EXCLUDED from income/expense reports but INCLUDED in wallet balance.
 * This prevents double-counting when tracking debt repayments.
 */
enum class TransactionType {
    EXPENSE, INCOME, TRANSFER,
    DEBT_GIVEN, DEBT_RECEIVED, DEBT_REPAYMENT_OUT, DEBT_REPAYMENT_IN,
    SAVINGS_DEPOSIT, SAVINGS_WITHDRAW
}

enum class TransactionSource { MANUAL, BANK_IMPORT, API_SYNC }

enum class FinancialPurpose { PERSONAL, BUSINESS, MIXED }

enum class ReconciliationStatus { UNMATCHED, MATCHED, REVIEWED, IGNORED }
