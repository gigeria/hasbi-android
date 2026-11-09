package com.hasbi.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hasbi.database.entity.ProjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(project: ProjectEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(projects: List<ProjectEntity>): LongArray

    @Update
    suspend fun update(project: ProjectEntity)

    @Query("UPDATE projects SET isArchived = 1, updatedAt = :time WHERE id = :id")
    suspend fun archive(id: Long, time: Long)

    @Query("UPDATE projects SET sortOrder = :sortOrder, updatedAt = :time WHERE id = :id")
    suspend fun updateSortOrder(id: Long, sortOrder: Int, time: Long)

    @Query("SELECT * FROM projects WHERE isArchived = 0 ORDER BY sortOrder ASC")
    fun observeAll(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getById(id: Long): ProjectEntity?

    @Query("SELECT COUNT(*) FROM projects WHERE isArchived = 0")
    suspend fun count(): Int
}
