package com.hasbi.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hasbi.database.converter.HasbiTypeConverters
import com.hasbi.database.dao.BudgetCycleDao
import com.hasbi.database.dao.BudgetDao
import com.hasbi.database.dao.CategoryDao
import com.hasbi.database.dao.CurrencyDao
import com.hasbi.database.dao.DebtDao
import com.hasbi.database.dao.OnboardingDao
import com.hasbi.database.dao.TransactionDao
import com.hasbi.database.dao.WalletDao
import com.hasbi.database.entity.BudgetCycleEntity
import com.hasbi.database.entity.BudgetEntity
import com.hasbi.database.entity.CategoryEntity
import com.hasbi.database.entity.CurrencyEntity
import com.hasbi.database.entity.DebtEntity
import com.hasbi.database.entity.TransactionEntity
import com.hasbi.database.entity.WalletEntity

/**
 * Hasbi Room database — single source of truth for financial records.
 *
 * Phase 1 entities (7): Currency, Wallet, Category, Transaction, BudgetCycle, Budget, Debt
 *
 * Phase 1.1+ entities (deferred):
 * - TagEntity + TransactionTagCrossRef (Phase 1.1)
 * - SavingsGoalEntity + SavingsContributionEntity (Phase 1.1)
 * - RecurringTransactionEntity (Phase 1.1)
 * - BackupMetadataEntity (Phase 2)
 *
 * Schema is exported to /schemas for migration tracking.
 */
@Database(
    entities = [
        CurrencyEntity::class,
        WalletEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        BudgetCycleEntity::class,
        BudgetEntity::class,
        DebtEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(HasbiTypeConverters::class)
abstract class HasbiDatabase : RoomDatabase() {

    abstract fun currencyDao(): CurrencyDao
    abstract fun walletDao(): WalletDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetCycleDao(): BudgetCycleDao
    abstract fun budgetDao(): BudgetDao
    abstract fun debtDao(): DebtDao
    abstract fun onboardingDao(): OnboardingDao

    companion object {
        const val DATABASE_NAME = "hasbi.db"

        // Future migrations will be added here:
        // val MIGRATION_1_2 = Migration(1, 2) { ... }  // Phase 1.1: add Tag tables
        // val MIGRATION_2_3 = Migration(2, 3) { ... }  // Phase 1.1: add Savings tables
        // val MIGRATION_3_4 = Migration(3, 4) { ... }  // Phase 1.1: add Recurring table
        // val MIGRATION_4_5 = Migration(4, 5) { ... }  // Phase 2: add BackupMetadata table

        // val ALL_MIGRATIONS = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
    }
}
