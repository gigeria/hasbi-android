package com.hasbi.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Budget entity — overall OR per-category limit for a cycle.
 *
 * Critical rules:
 * - `categoryId = null` represents overall budget (NOT 0)
 * - Application-level rule: one overall budget per cycle
 *   (enforced via partial unique index in HasbiDatabase callback)
 * - NO `spent` field — calculated via SQL aggregation
 *   (transactions are source of truth, no stale caches)
 * - `warningThreshold` configurable per budget (default 80%)
 */
@Entity(
    tableName = "budgets",
    foreignKeys = [
        ForeignKey(
            entity = BudgetCycleEntity::class,
            parentColumns = ["id"],
            childColumns = ["cycleId"],
            onDelete = ForeignKey.CASCADE  // Delete cycle → delete budgets
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE,
            deferred = true  // Allows null categoryId
        )
    ],
    indices = [
        Index(value = ["cycleId"]),
        Index(value = ["categoryId"])
    ]
)
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cycleId: Long,
    val categoryId: Long?,                        // null = overall budget
    val limit: Long,                              // in minor units
    val warningThreshold: Float = 0.8f,           // 80% default
    val createdAt: Long,
    val updatedAt: Long
)
