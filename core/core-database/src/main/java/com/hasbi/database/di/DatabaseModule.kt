package com.hasbi.database.di

import android.content.Context
import androidx.room.Room
import com.hasbi.database.HasbiDatabase
import com.hasbi.database.dao.BudgetCycleDao
import com.hasbi.database.dao.BudgetDao
import com.hasbi.database.dao.CategoryDao
import com.hasbi.database.dao.CurrencyDao
import com.hasbi.database.dao.DebtDao
import com.hasbi.database.dao.OnboardingDao
import com.hasbi.database.dao.TransactionDao
import com.hasbi.database.dao.WalletDao
import com.hasbi.database.migration.HasbiMigrations
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt DI module for database.
 *
 * Provides:
 * - HasbiDatabase (singleton)
 * - All 8 DAOs
 *
 * Database is configured with:
 * - Migrations (none in Phase 1, ready for future)
 * - DatabaseCallback (seeds currencies + categories on first launch)
 * - fallbackToDestructiveMigrationOnDowngrade (allows downgrade during development)
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideHasbiDatabase(
        @ApplicationContext context: Context
    ): HasbiDatabase {
        return Room.databaseBuilder(
            context,
            HasbiDatabase::class.java,
            HasbiDatabase.DATABASE_NAME
        )
            .addMigrations(*HasbiMigrations.ALL)
            .fallbackToDestructiveMigrationOnDowngrade()
            .addCallback(
                object : androidx.room.RoomDatabase.Callback() {
                    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed initial data on first launch
                        // Use static seeders directly to avoid circular dep
                        val now = System.currentTimeMillis()
                        val currencies = com.hasbi.database.seed.CurrencySeeder.getSeedCurrencies(now)
                        val categories = com.hasbi.database.seed.CategorySeeder.getSeedCategories(now)

                        // Insert via raw SQL since DAOs aren't available yet
                        currencies.forEach { currency ->
                            db.execSQL(
                                """
                                INSERT OR IGNORE INTO currencies
                                (code, name, symbol, minorUnitExponent, decimalSeparator,
                                 thousandsSeparator, symbolPosition, isDefault, createdAt)
                                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                                """,
                                arrayOf(
                                    currency.code,
                                    currency.name,
                                    currency.symbol,
                                    currency.minorUnitExponent,
                                    currency.decimalSeparator,
                                    currency.thousandsSeparator,
                                    currency.symbolPosition.name,
                                    if (currency.isDefault) 1 else 0,
                                    currency.createdAt
                                )
                            )
                        }

                        categories.forEach { category ->
                            db.execSQL(
                                """
                                INSERT OR IGNORE INTO categories
                                (name, icon, colorHex, `group`, sortOrder, isArchived,
                                 keywords, isKeywordsUserEdited, createdAt, updatedAt)
                                VALUES (?, ?, ?, ?, ?, 0, ?, 0, ?, ?)
                                """,
                                arrayOf(
                                    category.name,
                                    category.icon,
                                    category.colorHex,
                                    category.group.name,
                                    category.sortOrder,
                                    category.keywords,
                                    category.createdAt,
                                    category.updatedAt
                                )
                            )
                        }

                        // Create partial unique index for overall budget
                        db.execSQL(
                            """
                            CREATE UNIQUE INDEX IF NOT EXISTS idx_one_overall_per_cycle
                            ON budgets(cycleId) WHERE categoryId IS NULL
                            """
                        )
                    }
                }
            )
            .build()
    }

    @Provides
    fun provideCurrencyDao(database: HasbiDatabase): CurrencyDao = database.currencyDao()

    @Provides
    fun provideWalletDao(database: HasbiDatabase): WalletDao = database.walletDao()

    @Provides
    fun provideCategoryDao(database: HasbiDatabase): CategoryDao = database.categoryDao()

    @Provides
    fun provideTransactionDao(database: HasbiDatabase): TransactionDao = database.transactionDao()

    @Provides
    fun provideBudgetCycleDao(database: HasbiDatabase): BudgetCycleDao = database.budgetCycleDao()

    @Provides
    fun provideBudgetDao(database: HasbiDatabase): BudgetDao = database.budgetDao()

    @Provides
    fun provideDebtDao(database: HasbiDatabase): DebtDao = database.debtDao()

    @Provides
    fun provideOnboardingDao(database: HasbiDatabase): OnboardingDao = database.onboardingDao()
}
