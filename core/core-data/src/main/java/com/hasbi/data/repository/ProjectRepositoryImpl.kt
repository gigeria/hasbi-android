package com.hasbi.data.repository

import com.hasbi.database.dao.ProjectDao
import com.hasbi.database.entity.ProjectEntity
import com.hasbi.domain.repository.ProjectRepository
import com.hasbi.util.result.HasbiError
import com.hasbi.util.result.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectRepositoryImpl @Inject constructor(
    private val projectDao: ProjectDao
) : ProjectRepository {

    override fun observeAll(): Flow<List<ProjectEntity>> = projectDao.observeAll()

    override suspend fun getById(id: Long): ProjectEntity? = projectDao.getById(id)

    override suspend fun create(project: ProjectEntity): Result<Long> = try {
        val id = projectDao.insert(project)
        Result.Success(id)
    } catch (e: Exception) {
        Result.Failure(HasbiError.DuplicateCategoryError(project.name))
    }

    override suspend fun archive(id: Long): Result<Unit> = try {
        projectDao.archive(id, System.currentTimeMillis())
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Failure(HasbiError.DatabaseError(e.message ?: "Archive project failed"))
    }

    override suspend fun seedDefaultsIfEmpty(): Result<Unit> = try {
        if (projectDao.count() == 0) {
            val now = System.currentTimeMillis()
            projectDao.insertAll(listOf(
                ProjectEntity(name = "Personal", icon = "🏠", colorHex = "#1B5E43", sortOrder = 0, createdAt = now, updatedAt = now),
                ProjectEntity(name = "Business", icon = "💼", colorHex = "#2196F3", sortOrder = 1, createdAt = now, updatedAt = now)
            ))
        }
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Failure(HasbiError.DatabaseError(e.message ?: "Seed projects failed"))
    }
}
