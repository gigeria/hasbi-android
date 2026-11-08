package com.hasbi.data.repository

import com.hasbi.data.mapper.toDomain
import com.hasbi.data.mapper.toEntity
import com.hasbi.database.dao.CategoryDao
import com.hasbi.database.entity.CategoryGroup
import com.hasbi.database.seed.CategorySeeder
import com.hasbi.domain.model.Category
import com.hasbi.domain.repository.CategoryRepository
import com.hasbi.util.result.HasbiError
import com.hasbi.util.result.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun observeByGroup(group: CategoryGroup): Flow<List<Category>> =
        categoryDao.observeByGroup(group).map { list -> list.map { it.toDomain() } }

    override fun observeAll(): Flow<List<Category>> =
        categoryDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeById(id: Long): Flow<Category?> =
        categoryDao.observeById(id).map { it?.toDomain() }

    override suspend fun getById(id: Long): Category? =
        categoryDao.getById(id)?.toDomain()

    override suspend fun create(category: Category): Result<Long> {
        return try {
        val existing = categoryDao.countByName(category.name, category.group)
        if (existing > 0) {
            return Result.Failure(HasbiError.DuplicateCategoryError(category.name))
        }
        val id = categoryDao.insert(category.toEntity())
        Result.Success(id)
    } catch (e: Exception) {
        Result.Failure(HasbiError.DatabaseError(e.message ?: "Create category failed"))
    }
    }

    override suspend fun update(category: Category): Result<Unit> = try {
        categoryDao.update(category.copy(updatedAt = System.currentTimeMillis()).toEntity())
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Failure(HasbiError.DatabaseError(e.message ?: "Update category failed"))
    }

    override suspend fun archive(id: Long): Result<Unit> = try {
        categoryDao.archive(id, System.currentTimeMillis())
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Failure(HasbiError.DatabaseError(e.message ?: "Archive category failed"))
    }

    override suspend fun updateKeywords(id: Long, keywords: List<String>): Result<Unit> = try {
        categoryDao.updateKeywords(
            id = id,
            keywords = keywords.joinToString(","),
            time = System.currentTimeMillis()
        )
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Failure(HasbiError.DatabaseError(e.message ?: "Update keywords failed"))
    }

    override suspend fun reorder(orderedIds: List<Long>): Result<Unit> = try {
        val now = System.currentTimeMillis()
        orderedIds.forEachIndexed { index, id ->
            categoryDao.updateSortOrder(id, index, now)
        }
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Failure(HasbiError.DatabaseError(e.message ?: "Reorder categories failed"))
    }

    override suspend fun findByKeyword(keyword: String, group: CategoryGroup): Category? =
        categoryDao.findByKeyword(keyword.lowercase().trim(), group)?.toDomain()

    override suspend fun seedDefaultsIfEmpty(): Result<Unit> = try {
        if (categoryDao.count() == 0) {
            val now = System.currentTimeMillis()
            val defaults = CategorySeeder.getSeedCategories(now)
            defaults.forEach { category ->
                try {
                    categoryDao.insert(category)
                } catch (e: Exception) {
                    // already exists — ignore
                }
            }
        }
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Failure(HasbiError.DatabaseError(e.message ?: "Seed categories failed"))
    }
}
