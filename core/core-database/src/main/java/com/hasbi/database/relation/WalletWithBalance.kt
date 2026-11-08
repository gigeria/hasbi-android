package com.hasbi.database.relation

import com.hasbi.database.entity.WalletType

/**
 * Wallet with computed balance.
 *
 * Balance = initialBalance + income - expense + transfers_in - transfers_out
 *           + debt_received - debt_given + debt_repayment_in - debt_repayment_out
 *
 * Computed via SQL subqueries in WalletDao.
 */
data class WalletWithBalance(
    val id: Long,
    val name: String,
    val icon: String,
    val colorHex: String,
    val type: WalletType,
    val currencyCode: String,
    val balance: Long
)
