package com.hasbi.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hasbi.database.entity.BudgetCycleEntity
import kotlinx.coroutines.flow.Flow

/**
 * Budget cycle DAO.
 *
 * Manages recurring budget periods (monthly, salary-date, weekly, bi-weekly, custom).
 * Only ONE cycle can be `isCurrent = true` at a time.
 *
 * Cycle auto-generation: when current cycle expires, EnsureCurrentCycleUseCase
 * creates the next one based on cycleType + startDay.
 */
@Dao
interface BudgetCycleDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(cycle: BudgetCycleEntity): Long

    @Update
    suspend fun update(cycle: BudgetCycleEntity)

    @Query("SELECT * FROM budget_cycles WHERE isCurrent = 1 LIMIT 1")
    fun observeCurrent(): Flow<BudgetCycleEntity?>

    @Query("SELECT * FROM budget_cycles WHERE isCurrent = 1 LIMIT 1")
    suspend fun getCurrent(): BudgetCycleEntity?

    @Query("SELECT * FROM budget_cycles ORDER BY startDate DESC")
    fun observeAll(): Flow<List<BudgetCycleEntity>>

    @Query("SELECT * FROM budget_cycles WHERE :date >= startDate AND :date < endDate LIMIT 1")
    suspend fun getCycleContaining(date: Long): BudgetCycleEntity?

    @Query("SELECT * FROM budget_cycles WHERE :date >= startDate AND :date < endDate LIMIT 1")
    fun observeCycleContaining(date: Long): Flow<BudgetCycleEntity?>

    @Query("UPDATE budget_cycles SET isCurrent = 0")
    suspend fun clearCurrentFlag()

    @Query("UPDATE budget_cycles SET isCurrent = 1, isClosed = 0, updatedAt = :time WHERE id = :id")
    suspend fun setCurrent(id: Long, time: Long)

    @Query("UPDATE budget_cycles SET isClosed = 1, updatedAt = :time WHERE id = :id")
    suspend fun close(id: Long, time: Long)

    @Query("SELECT * FROM budget_cycles ORDER BY startDate DESC LIMIT 1")
    suspend fun getLatest(): BudgetCycleEntity?

    @Query("SELECT COUNT(*) FROM budget_cycles")
    suspend fun count(): Int
}
