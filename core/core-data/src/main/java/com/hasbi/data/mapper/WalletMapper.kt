package com.hasbi.data.mapper

import com.hasbi.database.entity.WalletEntity
import com.hasbi.database.relation.WalletWithBalance
import com.hasbi.domain.model.Wallet
import com.hasbi.domain.model.WalletWithBalance as DomainWalletWithBalance

/**
 * Wallet mapper — Entity ↔ Domain.
 *
 * Extension functions for clean mapping.
 */

fun WalletEntity.toDomain(): Wallet = Wallet(
    id = id,
    name = name,
    type = type,
    icon = icon,
    colorHex = colorHex,
    initialBalance = initialBalance,
    currencyCode = currencyCode,
    sortOrder = sortOrder,
    isHidden = isHidden,
    isArchived = isArchived,
    creditLimit = creditLimit,
    statementDay = statementDay,
    paymentDueDay = paymentDueDay,
    note = note,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Wallet.toEntity(): WalletEntity = WalletEntity(
    id = id,
    name = name,
    type = type,
    icon = icon,
    colorHex = colorHex,
    initialBalance = initialBalance,
    currencyCode = currencyCode,
    sortOrder = sortOrder,
    isHidden = isHidden,
    isArchived = isArchived,
    creditLimit = creditLimit,
    statementDay = statementDay,
    paymentDueDay = paymentDueDay,
    note = note,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun WalletWithBalance.toDomain(): DomainWalletWithBalance {
    val walletEntity = WalletEntity(
        id = id,
        name = name,
        icon = icon,
        colorHex = colorHex,
        type = type,
        currencyCode = currencyCode,
        initialBalance = 0,  // not in relation POJO
        sortOrder = 0,
        createdAt = 0,
        updatedAt = 0
    )
    return DomainWalletWithBalance(
        wallet = walletEntity.toDomain(),
        balance = balance
    )
}
