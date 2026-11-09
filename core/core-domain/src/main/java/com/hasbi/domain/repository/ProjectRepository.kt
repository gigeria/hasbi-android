package com.hasbi.domain.repository

import com.hasbi.database.entity.ProjectEntity
import com.hasbi.util.result.Result
import kotlinx.coroutines.flow.Flow

interface ProjectRepository {
    fun observeAll(): Flow<List<ProjectEntity>>
    suspend fun getById(id: Long): ProjectEntity?
    suspend fun create(project: ProjectEntity): Result<Long>
    suspend fun archive(id: Long): Result<Unit>
    suspend fun seedDefaultsIfEmpty(): Result<Unit>
}
