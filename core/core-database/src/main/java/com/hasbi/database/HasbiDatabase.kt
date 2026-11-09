package com.hasbi.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hasbi.database.converter.HasbiTypeConverters
import com.hasbi.database.dao.BudgetCycleDao
import com.hasbi.database.dao.BudgetDao
import com.hasbi.database.dao.CategoryDao
import com.hasbi.database.dao.CurrencyDao
import com.hasbi.database.dao.DebtDao
import com.hasbi.database.dao.OnboardingDao
import com.hasbi.database.dao.ProjectDao
import com.hasbi.database.dao.TransactionDao
import com.hasbi.database.dao.WalletDao
import com.hasbi.database.entity.BudgetCycleEntity
import com.hasbi.database.entity.BudgetEntity
import com.hasbi.database.entity.CategoryEntity
import com.hasbi.database.entity.CurrencyEntity
import com.hasbi.database.entity.DebtEntity
import com.hasbi.database.entity.ProjectEntity
import com.hasbi.database.entity.TransactionEntity
import com.hasbi.database.entity.WalletEntity

@Database(
    entities = [
        CurrencyEntity::class,
        WalletEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        BudgetCycleEntity::class,
        BudgetEntity::class,
        DebtEntity::class,
        ProjectEntity::class
    ],
    version = 2,
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
    abstract fun projectDao(): ProjectDao

    companion object {
        const val DATABASE_NAME = "hasbi.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create projects table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS projects (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        icon TEXT NOT NULL,
                        colorHex TEXT NOT NULL,
                        sortOrder INTEGER NOT NULL,
                        isArchived INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """)
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_projects_name ON projects(name)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_projects_sortOrder ON projects(sortOrder)")

                // Add projectId column to transactions (nullable)
                db.execSQL("ALTER TABLE transactions ADD COLUMN projectId INTEGER DEFAULT NULL")

                // Seed default projects
                val now = System.currentTimeMillis()
                db.execSQL("INSERT OR IGNORE INTO projects (name, icon, colorHex, sortOrder, isArchived, createdAt, updatedAt) VALUES ('Personal', '🏠', '#1B5E43', 0, 0, $now, $now)")
                db.execSQL("INSERT OR IGNORE INTO projects (name, icon, colorHex, sortOrder, isArchived, createdAt, updatedAt) VALUES ('Business', '💼', '#2196F3', 1, 0, $now, $now)")
            }
        }

        // Migrations moved to HasbiMigrations object
    }
}
