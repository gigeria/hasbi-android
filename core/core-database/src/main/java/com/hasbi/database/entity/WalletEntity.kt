package com.hasbi.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Wallet entity — represents a financial account.
 *
 * Types: CASH, BANK, MOBILE_WALLET (JazzCash, Easypaisa, SadaPay),
 *        CREDIT_CARD, SAVINGS, CUSTOM
 *
 * - `initialBalance` always Long (minor units) — never Double
 * - Soft delete via `isArchived` (never hard delete wallets with transactions)
 * - `sortOrder` for custom reordering
 * - Credit card fields nullable; only used when type = CREDIT_CARD
 */
@Entity(
    tableName = "wallets",
    foreignKeys = [
        ForeignKey(
            entity = CurrencyEntity::class,
            parentColumns = ["code"],
            childColumns = ["currencyCode"],
            onDelete = ForeignKey.RESTRICT  // Don't delete currency in use
        )
    ],
    indices = [
        Index(value = ["currencyCode"]),
        Index(value = ["sortOrder"])
    ]
)
data class WalletEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,                             // "Cash", "HBL Bank", "JazzCash"
    val type: WalletType,
    val icon: String,                             // emoji "💵"
    val colorHex: String,                         // "#1B5E43"
    val initialBalance: Long,                     // in minor units (paisa)
    val currencyCode: String,                     // FK → CurrencyEntity
    val sortOrder: Int,
    val isHidden: Boolean = false,
    val isArchived: Boolean = false,

    // Credit card specific (nullable for other types)
    val creditLimit: Long? = null,
    val statementDay: Int? = null,                // 1-31
    val paymentDueDay: Int? = null,               // 1-31

    val note: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)

enum class WalletType {
    CASH, BANK, MOBILE_WALLET, CREDIT_CARD, SAVINGS, CUSTOM
}
