package com.hasbi.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Debt entity — Udhaar tracking.
 *
 * - LENT: Maine kisi ko diya (others owe me)
 * - BORROWED: Mujhe kisi se mila (I owe others)
 *
 * Critical: NO `remainingAmount` field.
 * Computed via SQL from linked transactions:
 *   remainingAmount = originalAmount - SUM(DEBT_REPAYMENT_IN/OUT linked to this debt)
 *
 * Money movement happens via linked transactions:
 * - LENT created → TransactionEntity (DEBT_GIVEN) decreases wallet, NOT counted as expense
 * - BORROWED created → TransactionEntity (DEBT_RECEIVED) increases wallet, NOT counted as income
 * - Repayment received → TransactionEntity (DEBT_REPAYMENT_IN) increases wallet, NOT counted as income
 * - Repayment given → TransactionEntity (DEBT_REPAYMENT_OUT) decreases wallet, NOT counted as expense
 *
 * This prevents double-counting in income/expense reports.
 *
 * `isSettled` is computed when remaining = 0 (UI flips it via use case).
 */
@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: DebtType,                           // LENT or BORROWED
    val personName: String,
    val personPhone: String? = null,
    val originalAmount: Long,                     // snapshot, in minor units
    val currencyCode: String = CurrencyEntity.PKR,
    val startDate: Long,                          // UTC epoch millis
    val expectedReturnDate: Long? = null,
    val note: String? = null,
    val isSettled: Boolean = false,               // computed when remaining = 0
    val isArchived: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)

enum class DebtType { LENT, BORROWED }
