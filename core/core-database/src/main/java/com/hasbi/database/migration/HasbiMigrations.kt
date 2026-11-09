package com.hasbi.database.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object HasbiMigrations {

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
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
            db.execSQL("ALTER TABLE transactions ADD COLUMN projectId INTEGER DEFAULT NULL")
            
            val now = System.currentTimeMillis()
            db.execSQL("INSERT OR IGNORE INTO projects (name, icon, colorHex, sortOrder, isArchived, createdAt, updatedAt) VALUES ('Personal', '🏠', '#1B5E43', 0, 0, $now, $now)")
            db.execSQL("INSERT OR IGNORE INTO projects (name, icon, colorHex, sortOrder, isArchived, createdAt, updatedAt) VALUES ('Business', '💼', '#2196F3', 1, 0, $now, $now)")
        }
    }

    val ALL: Array<Migration> = arrayOf(MIGRATION_1_2)
}
