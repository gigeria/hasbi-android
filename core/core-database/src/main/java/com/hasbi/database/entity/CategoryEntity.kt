package com.hasbi.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Category entity — expense/income classification.
 *
 * - Uniqueness on (name, group) — allows "Other" in both EXPENSE and INCOME
 * - `keywords` is comma-separated string for Smart Entry matching
 *   e.g. "chai,nashta,khana,food,lunch"
 * - `isKeywordsUserEdited` prevents AI overwrite (Phase 2 ML)
 * - Soft delete via `isArchived`
 */
@Entity(
    tableName = "categories",
    indices = [
        Index(value = ["name", "group"], unique = true),
        Index(value = ["group"]),
        Index(value = ["sortOrder"])
    ]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,                             // "Food", "Petrol", "Salary"
    val icon: String,                             // emoji "🍔"
    val colorHex: String,                         // "#F4A340"
    val group: CategoryGroup,                     // EXPENSE, INCOME
    val sortOrder: Int,
    val isArchived: Boolean = false,
    val keywords: String = "",                    // comma-separated for Smart Entry
    val isKeywordsUserEdited: Boolean = false,    // prevents AI overwrite
    val createdAt: Long,
    val updatedAt: Long
)

enum class CategoryGroup { EXPENSE, INCOME }
