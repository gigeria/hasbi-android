package com.hasbi.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hasbi.database.entity.CategoryEntity
import com.hasbi.database.entity.CategoryGroup
import kotlinx.coroutines.flow.Flow

/**
 * Category DAO.
 *
 * Critical:
 * - Uniqueness on (name, group) — allows "Other" in both groups
 * - Smart Entry uses findByKeyword() for natural language parsing
 * - Soft delete via `isArchived`
 * - `isKeywordsUserEdited` prevents AI overwrite of user-customized keywords
 */
@Dao
interface CategoryDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(category: CategoryEntity): Long

    @Update
    suspend fun update(category: CategoryEntity)

    @Query("UPDATE categories SET isArchived = 1, updatedAt = :time WHERE id = :id")
    suspend fun archive(id: Long, time: Long)

    @Query("UPDATE categories SET keywords = :keywords, isKeywordsUserEdited = 1, updatedAt = :time WHERE id = :id")
    suspend fun updateKeywords(id: Long, keywords: String, time: Long)

    @Query("UPDATE categories SET sortOrder = :sortOrder, updatedAt = :time WHERE id = :id")
    suspend fun updateSortOrder(id: Long, sortOrder: Int, time: Long)

    @Query("SELECT * FROM categories WHERE isArchived = 0 AND `group` = :group ORDER BY sortOrder ASC")
    fun observeByGroup(group: CategoryGroup): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE isArchived = 0 ORDER BY `group`, sortOrder ASC")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: Long): CategoryEntity?

    @Query("SELECT * FROM categories WHERE id = :id")
    fun observeById(id: Long): Flow<CategoryEntity?>

    /**
     * Find category by keyword match.
     *
     * Used by Smart Entry parser. Searches comma-separated `keywords` field
     * and falls back to `name` partial match.
     *
     * Match priority:
     * 1. Exact keyword in keywords list
     * 2. Keyword as prefix/suffix in keywords list
     * 3. Partial match in category name
     */
    @Query(
        """
        SELECT * FROM categories
        WHERE isArchived = 0
        AND `group` = :group
        AND (
            keywords LIKE '%,' || :keyword || ',%'
            OR keywords LIKE :keyword || ',%'
            OR keywords LIKE '%,' || :keyword
            OR keywords = :keyword
            OR name LIKE '%' || :keyword || '%'
        )
        LIMIT 1
        """
    )
    suspend fun findByKeyword(keyword: String, group: CategoryGroup): CategoryEntity?

    @Query("SELECT COUNT(*) FROM categories WHERE name = :name AND `group` = :group AND isArchived = 0")
    suspend fun countByName(name: String, group: CategoryGroup): Int

    @Query("SELECT COUNT(*) FROM categories WHERE isArchived = 0")
    suspend fun count(): Int
}
