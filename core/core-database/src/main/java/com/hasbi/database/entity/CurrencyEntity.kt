package com.hasbi.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Currency metadata — supports multi-currency precision.
 *
 * Phase 1: PKR is default. Other currencies seeded but conversion not supported.
 * Phase 4: Full multi-currency support.
 *
 * Amounts in TransactionEntity are stored in minor units (paisa/cents/fils).
 * `minorUnitExponent` defines the divisor:
 *   - PKR: exponent 2 → Rs. 100 = 10,000 paisa
 *   - JPY: exponent 0 → ¥100 = 100 (no minor unit)
 *   - KWD: exponent 3 → KWD 1 = 1,000 fils
 */
@Entity(tableName = "currencies")
data class CurrencyEntity(
    @PrimaryKey val code: String,                  // "PKR", "USD", "JPY"
    val name: String,                              // "Pakistani Rupee"
    val symbol: String,                            // "Rs.", "$", "¥"
    val minorUnitExponent: Int,                    // 2 for PKR/USD, 0 for JPY, 3 for KWD
    val decimalSeparator: String,                  // "." or ","
    val thousandsSeparator: String,                // "," or "."
    val symbolPosition: SymbolPosition,            // BEFORE or AFTER
    val isDefault: Boolean = false,
    val createdAt: Long
) {
    companion object {
        const val PKR = "PKR"
        const val USD = "USD"
        const val EUR = "EUR"
        const val GBP = "GBP"
        const val AED = "AED"
        const val SAR = "SAR"
        const val JPY = "JPY"
        const val KWD = "KWD"
        const val BHD = "BHD"
        const val INR = "INR"
    }
}

enum class SymbolPosition { BEFORE, AFTER }
