package com.hasbi.domain.repository

import com.hasbi.database.entity.CategoryGroup
import com.hasbi.domain.model.Category
import com.hasbi.util.result.Result
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {

    fun observeByGroup(group: CategoryGroup): Flow<List<Category>>
    fun observeAll(): Flow<List<Category>>
    fun observeById(id: Long): Flow<Category?>

    suspend fun getById(id: Long): Category?
    suspend fun create(category: Category): Result<Long>
    suspend fun update(category: Category): Result<Unit>
    suspend fun archive(id: Long): Result<Unit>
    suspend fun updateKeywords(id: Long, keywords: List<String>): Result<Unit>
    suspend fun reorder(orderedIds: List<Long>): Result<Unit>

    /** Smart Entry: find category by keyword match. */
    suspend fun findByKeyword(keyword: String, group: CategoryGroup): Category?

    /** Idempotent: seeds default categories if not present. */
    suspend fun seedDefaultsIfEmpty(): Result<Unit>
}
