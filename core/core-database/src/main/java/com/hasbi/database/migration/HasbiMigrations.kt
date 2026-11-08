package com.hasbi.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Hasbi database migrations.
 *
 * Phase 1: Version 1 — initial schema. No migrations yet.
 *
 * Future migrations (anticipated):
 *
 * Phase 1.1:
 * - MIGRATION_1_2: Add TagEntity + TransactionTagCrossRef tables
 * - MIGRATION_2_3: Add SavingsGoalEntity + SavingsContributionEntity tables
 * - MIGRATION_3_4: Add RecurringTransactionEntity table
 *
 * Phase 2:
 * - MIGRATION_4_5: Add BackupMetadataEntity table
 *
 * Phase 2.1 (tax classification UI):
 * - No migration needed — `financialPurpose`, `businessPercentage`, etc. already in schema
 * - Just populate nullable fields via UI
 *
 * Phase 2.2 (bank import):
 * - No migration needed — `source`, `externalTransactionId`, etc. already in schema
 *
 * Phase 4 (multi-currency conversion):
 * - MIGRATION_5_6: Add ExchangeRateEntity table
 *
 * Key principle: Future-ready fields in Phase 1 schema make most future migrations
 * no-ops or simple table additions.
 */
object HasbiMigrations {

    val ALL: Array<Migration> = arrayOf(
        // No migrations in Phase 1 (version 1)
    )
}
