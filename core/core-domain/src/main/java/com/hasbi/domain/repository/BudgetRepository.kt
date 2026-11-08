package com.hasbi.domain.repository

import com.hasbi.database.entity.BudgetCycleEntity
import com.hasbi.database.relation.BudgetWithSpent
import com.hasbi.database.entity.BudgetEntity
import com.hasbi.util.result.Result
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {

    // === Cycle ===
    fun observeCurrentCycle(): Flow<BudgetCycleEntity?>
    fun observeAllCycles(): Flow<List<BudgetCycleEntity>>
    fun observeCycleContaining(dateEpoch: Long): Flow<BudgetCycleEntity?>

    suspend fun getCurrentCycle(): BudgetCycleEntity?
    suspend fun getCycleContaining(dateEpoch: Long): BudgetCycleEntity?
    suspend fun getLatestCycle(): BudgetCycleEntity?

    suspend fun createCycle(cycle: BudgetCycleEntity): Result<Long>
    suspend fun setCurrentCycle(id: Long): Result<Unit>
    suspend fun closeCycle(id: Long): Result<Unit>

    // === Budgets ===
    fun observeBudgetsWithSpent(cycleId: Long, cycleStart: Long, cycleEnd: Long): Flow<List<BudgetWithSpent>>
    fun observeByCycle(cycleId: Long): Flow<List<BudgetEntity>>
    fun observeOverallBudget(cycleId: Long): Flow<BudgetEntity?>
    fun observeCategoryBudget(cycleId: Long, categoryId: Long): Flow<BudgetEntity?>

    suspend fun getOverallBudget(cycleId: Long): BudgetEntity?
    suspend fun getCategoryBudget(cycleId: Long, categoryId: Long): BudgetEntity?

    suspend fun createBudget(budget: BudgetEntity): Result<Long>
    suspend fun updateBudget(budget: BudgetEntity): Result<Unit>
    suspend fun deleteBudget(id: Long): Result<Unit>
}
