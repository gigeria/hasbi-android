package com.hasbi.database.converter

import androidx.room.TypeConverter
import com.hasbi.database.entity.CategoryGroup
import com.hasbi.database.entity.CycleType
import com.hasbi.database.entity.DebtType
import com.hasbi.database.entity.FinancialPurpose
import com.hasbi.database.entity.ReconciliationStatus
import com.hasbi.database.entity.SymbolPosition
import com.hasbi.database.entity.TransactionSource
import com.hasbi.database.entity.TransactionType
import com.hasbi.database.entity.WalletType

/**
 * Room type converters for enums.
 *
 * Stored as TEXT (enum name) for human-readable DB inspection.
 */
class HasbiTypeConverters {

    // === Wallet ===
    @TypeConverter
    fun fromWalletType(value: WalletType?): String? = value?.name

    @TypeConverter
    fun toWalletType(value: String?): WalletType? = value?.let { WalletType.valueOf(it) }

    // === Category ===
    @TypeConverter
    fun fromCategoryGroup(value: CategoryGroup?): String? = value?.name

    @TypeConverter
    fun toCategoryGroup(value: String?): CategoryGroup? = value?.let { CategoryGroup.valueOf(it) }

    // === Transaction ===
    @TypeConverter
    fun fromTransactionType(value: TransactionType?): String? = value?.name

    @TypeConverter
    fun toTransactionType(value: String?): TransactionType? = value?.let { TransactionType.valueOf(it) }

    @TypeConverter
    fun fromTransactionSource(value: TransactionSource?): String? = value?.name

    @TypeConverter
    fun toTransactionSource(value: String?): TransactionSource? = value?.let { TransactionSource.valueOf(it) }

    @TypeConverter
    fun fromFinancialPurpose(value: FinancialPurpose?): String? = value?.name

    @TypeConverter
    fun toFinancialPurpose(value: String?): FinancialPurpose? = value?.let { FinancialPurpose.valueOf(it) }

    @TypeConverter
    fun fromReconciliationStatus(value: ReconciliationStatus?): String? = value?.name

    @TypeConverter
    fun toReconciliationStatus(value: String?): ReconciliationStatus? = value?.let { ReconciliationStatus.valueOf(it) }

    // === Budget Cycle ===
    @TypeConverter
    fun fromCycleType(value: CycleType?): String? = value?.name

    @TypeConverter
    fun toCycleType(value: String?): CycleType? = value?.let { CycleType.valueOf(it) }

    // === Debt ===
    @TypeConverter
    fun fromDebtType(value: DebtType?): String? = value?.name

    @TypeConverter
    fun toDebtType(value: String?): DebtType? = value?.let { DebtType.valueOf(it) }

    // === Currency ===
    @TypeConverter
    fun fromSymbolPosition(value: SymbolPosition?): String? = value?.name

    @TypeConverter
    fun toSymbolPosition(value: String?): SymbolPosition? = value?.let { SymbolPosition.valueOf(it) }
}
