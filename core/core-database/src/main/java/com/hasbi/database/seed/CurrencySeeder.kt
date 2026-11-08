package com.hasbi.database.seed

import com.hasbi.database.entity.CurrencyEntity
import com.hasbi.database.entity.SymbolPosition

/**
 * Default currency seed data.
 *
 * PKR is default (Pakistan-first).
 * 10 currencies seeded covering major global + Gulf currencies (Pakistani diaspora).
 *
 * Multi-currency conversion is NOT supported in Phase 1.
 * Schema is ready for Phase 4 conversion support.
 */
object CurrencySeeder {

    fun getSeedCurrencies(now: Long): List<CurrencyEntity> = listOf(
        CurrencyEntity(
            code = CurrencyEntity.PKR,
            name = "Pakistani Rupee",
            symbol = "Rs.",
            minorUnitExponent = 2,
            decimalSeparator = ".",
            thousandsSeparator = ",",
            symbolPosition = SymbolPosition.BEFORE,
            isDefault = true,
            createdAt = now
        ),
        CurrencyEntity(
            code = CurrencyEntity.USD,
            name = "US Dollar",
            symbol = "$",
            minorUnitExponent = 2,
            decimalSeparator = ".",
            thousandsSeparator = ",",
            symbolPosition = SymbolPosition.BEFORE,
            createdAt = now
        ),
        CurrencyEntity(
            code = CurrencyEntity.EUR,
            name = "Euro",
            symbol = "€",
            minorUnitExponent = 2,
            decimalSeparator = ",",
            thousandsSeparator = ".",
            symbolPosition = SymbolPosition.AFTER,
            createdAt = now
        ),
        CurrencyEntity(
            code = CurrencyEntity.GBP,
            name = "British Pound",
            symbol = "£",
            minorUnitExponent = 2,
            decimalSeparator = ".",
            thousandsSeparator = ",",
            symbolPosition = SymbolPosition.BEFORE,
            createdAt = now
        ),
        CurrencyEntity(
            code = CurrencyEntity.AED,
            name = "UAE Dirham",
            symbol = "AED",
            minorUnitExponent = 2,
            decimalSeparator = ".",
            thousandsSeparator = ",",
            symbolPosition = SymbolPosition.BEFORE,
            createdAt = now
        ),
        CurrencyEntity(
            code = CurrencyEntity.SAR,
            name = "Saudi Riyal",
            symbol = "SAR",
            minorUnitExponent = 2,
            decimalSeparator = ".",
            thousandsSeparator = ",",
            symbolPosition = SymbolPosition.BEFORE,
            createdAt = now
        ),
        CurrencyEntity(
            code = CurrencyEntity.JPY,
            name = "Japanese Yen",
            symbol = "¥",
            minorUnitExponent = 0,  // No minor unit
            decimalSeparator = ".",
            thousandsSeparator = ",",
            symbolPosition = SymbolPosition.BEFORE,
            createdAt = now
        ),
        CurrencyEntity(
            code = CurrencyEntity.KWD,
            name = "Kuwaiti Dinar",
            symbol = "KWD",
            minorUnitExponent = 3,  // 3 decimal places (fils)
            decimalSeparator = ".",
            thousandsSeparator = ",",
            symbolPosition = SymbolPosition.BEFORE,
            createdAt = now
        ),
        CurrencyEntity(
            code = CurrencyEntity.BHD,
            name = "Bahraini Dinar",
            symbol = "BHD",
            minorUnitExponent = 3,  // 3 decimal places (fils)
            decimalSeparator = ".",
            thousandsSeparator = ",",
            symbolPosition = SymbolPosition.BEFORE,
            createdAt = now
        ),
        CurrencyEntity(
            code = CurrencyEntity.INR,
            name = "Indian Rupee",
            symbol = "₹",
            minorUnitExponent = 2,
            decimalSeparator = ".",
            thousandsSeparator = ",",
            symbolPosition = SymbolPosition.BEFORE,
            createdAt = now
        )
    )
}
