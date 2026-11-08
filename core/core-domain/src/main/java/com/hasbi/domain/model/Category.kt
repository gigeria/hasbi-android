package com.hasbi.domain.model

import com.hasbi.database.entity.CategoryGroup

/**
 * Category domain model — pure Kotlin.
 */
data class Category(
    val id: Long = 0,
    val name: String,
    val icon: String,
    val colorHex: String,
    val group: CategoryGroup,
    val sortOrder: Int,
    val isArchived: Boolean = false,
    val keywords: List<String> = emptyList(),  // parsed from comma-separated string
    val isKeywordsUserEdited: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)
