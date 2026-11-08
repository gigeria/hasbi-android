package com.hasbi.data.mapper

import com.hasbi.database.entity.CategoryEntity
import com.hasbi.domain.model.Category

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    icon = icon,
    colorHex = colorHex,
    group = group,
    sortOrder = sortOrder,
    isArchived = isArchived,
    keywords = if (keywords.isBlank()) emptyList() else keywords.split(",").map { it.trim() }.filter { it.isNotEmpty() },
    isKeywordsUserEdited = isKeywordsUserEdited,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    icon = icon,
    colorHex = colorHex,
    group = group,
    sortOrder = sortOrder,
    isArchived = isArchived,
    keywords = keywords.joinToString(","),
    isKeywordsUserEdited = isKeywordsUserEdited,
    createdAt = createdAt,
    updatedAt = updatedAt
)
