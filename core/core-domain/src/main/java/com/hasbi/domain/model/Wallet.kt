package com.hasbi.domain.model

import com.hasbi.database.entity.WalletType

/**
 * Wallet domain model — pure Kotlin, no Android deps.
 *
 * Used by Use Cases + ViewModels.
 * Mapped from WalletEntity in core-data layer.
 */
data class Wallet(
    val id: Long = 0,
    val name: String,
    val type: WalletType,
    val icon: String,
    val colorHex: String,
    val initialBalance: Long,           // in minor units (paisa)
    val currencyCode: String,
    val sortOrder: Int,
    val isHidden: Boolean = false,
    val isArchived: Boolean = false,
    val creditLimit: Long? = null,
    val statementDay: Int? = null,
    val paymentDueDay: Int? = null,
    val note: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)

/**
 * Wallet with computed balance.
 * Balance = initial + income - expense + transfers_in - transfers_out + debt movements
 */
data class WalletWithBalance(
    val wallet: Wallet,
    val balance: Long
)
