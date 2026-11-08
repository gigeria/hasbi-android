package com.hasbi.util.money

import com.hasbi.database.entity.CurrencyEntity
import com.hasbi.database.entity.SymbolPosition
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

/**
 * Money formatter — handles multi-currency precision correctly.
 *
 * Rules:
 * - PKR: exponent 2 → Rs. 1,234.56 (paisa input: 123456)
 * - JPY: exponent 0 → ¥1234 (no decimals)
 * - KWD: exponent 3 → KWD 1.234 (3 decimals, fils)
 *
 * Pakistani number format: 1,00,000 (lakh system)
 * Western number format: 1,000,000
 * Indian number format: 1,00,000 (same as Pakistani)
 */
class MoneyFormatter {

    /**
     * Format amount in minor units to display string.
     *
     * @param minorUnits Amount in paisa/cents/fils (Long)
     * @param currency Currency metadata
     * @param format Number format style
     * @return Formatted string like "Rs. 1,234.56" or "¥1,234"
     */
    fun format(minorUnits: Long, currency: CurrencyEntity, format: NumberFormatStyle = NumberFormatStyle.WESTERN): String {
        val divisor = BigDecimal.TEN.pow(currency.minorUnitExponent)
        val majorAmount = BigDecimal(minorUnits).divide(divisor, currency.minorUnitExponent, RoundingMode.HALF_UP)

        val numberStr = formatNumber(majorAmount, currency.minorUnitExponent, format)

        return when (currency.symbolPosition) {
            SymbolPosition.BEFORE -> "${currency.symbol}$numberStr"
            SymbolPosition.AFTER -> "$numberStr${currency.symbol}"
        }
    }

    /**
     * Format without currency symbol (just the number).
     */
    fun formatNumberOnly(minorUnits: Long, currency: CurrencyEntity, format: NumberFormatStyle = NumberFormatStyle.WESTERN): String {
        val divisor = BigDecimal.TEN.pow(currency.minorUnitExponent)
        val majorAmount = BigDecimal(minorUnits).divide(divisor, currency.minorUnitExponent, RoundingMode.HALF_UP)
        return formatNumber(majorAmount, currency.minorUnitExponent, format)
    }

    /**
     * Format with sign for transaction display.
     * Expense: "-Rs. 1,234"
     * Income: "+Rs. 5,000"
     */
    fun formatWithSign(minorUnits: Long, currency: CurrencyEntity, isIncome: Boolean, format: NumberFormatStyle = NumberFormatStyle.WESTERN): String {
        val formatted = format(minorUnits, currency, format)
        return if (isIncome) "+$formatted" else "−$formatted"
    }

    private fun formatNumber(amount: BigDecimal, decimalPlaces: Int, format: NumberFormatStyle): String {
        val pattern = when (format) {
            NumberFormatStyle.WESTERN -> buildWesternPattern(decimalPlaces)
            NumberFormatStyle.PAKISTANI -> buildPakistaniPattern(decimalPlaces)
            NumberFormatStyle.INDIAN -> buildPakistaniPattern(decimalPlaces)  // same pattern
        }
        val df = DecimalFormat(pattern)
        return df.format(amount)
    }

    /**
     * Western format: 1,000,000.00
     */
    private fun buildWesternPattern(decimalPlaces: Int): String {
        val decimals = if (decimalPlaces > 0) "." + "0".repeat(decimalPlaces) else ""
        return "#,##0$decimals"
    }

    /**
     * Pakistani/Indian format: 10,00,000.00 (lakh system)
     * Last 3 digits before decimal grouped normally, then groups of 2.
     */
    private fun buildPakistaniPattern(decimalPlaces: Int): String {
        val decimals = if (decimalPlaces > 0) "." + "0".repeat(decimalPlaces) else ""
        // #,##,##0 = Pakistani pattern
        return "#,##,##0$decimals"
    }
}

enum class NumberFormatStyle {
    WESTERN,      // 1,000,000.00
    PAKISTANI,    // 10,00,000.00 (lakh)
    INDIAN        // 10,00,000.00 (same as Pakistani)
}
