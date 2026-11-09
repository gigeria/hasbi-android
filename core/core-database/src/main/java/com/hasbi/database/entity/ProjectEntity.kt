package com.hasbi.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Project entity — user-created work/project tags.
 *
 * Users create their own projects (no hardcoded):
 * - "Dukaan", "Side Business", "School Fees" — whatever they want
 * - Default starters: "Personal" and "Business"
 * - Each transaction can be tagged with a project
 * - Reports/export can be project-wise
 */
@Entity(
    tableName = "projects",
    indices = [
        Index(value = ["name"], unique = true),
        Index(value = ["sortOrder"])
    ]
)
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,                             // "Personal", "Business", "Dukaan"
    val icon: String,                             // emoji "🏠" "💼" "🏪"
    val colorHex: String,                         // "#1B5E43"
    val sortOrder: Int,
    val isArchived: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)
