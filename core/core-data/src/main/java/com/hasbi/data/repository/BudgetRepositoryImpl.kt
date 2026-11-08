package com.hasbi.data.repository

import com.hasbi.database.dao.BudgetCycleDao
import com.hasbi.database.dao.BudgetDao
import com.hasbi.database.entity.BudgetCycleEntity
import com.hasbi.database.entity.BudgetEntity
import com.hasbi.database.relation.BudgetWithSpent
import com.hasbi.domain.repository.BudgetRepository
import com.hasbi.util.result.HasbiError
import com.hasbi.util.result.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val budgetCycleDao: BudgetCycleDao,
    private val budgetDao: BudgetDao
) : BudgetRepository {

    override fun observeCurrentCycle(): Flow<BudgetCycleEntity?> =
        budgetCycleDao.observeCurrent()

    override fun observeAllCycles(): Flow<List<BudgetCycleEntity>> =
        budgetCycleDao.observeAll()

    override fun observeCycleContaining(dateEpoch: Long): Flow<BudgetCycleEntity?> =
        budgetCycleDao.observeCycleContaining(dateEpoch)

    override suspend fun getCurrentCycle(): BudgetCycleEntity? =
        budgetCycleDao.getCurrent()

    override suspend fun getCycleContaining(dateEpoch: Long): BudgetCycleEntity? =
        budgetCycleDao.getCycleContaining(dateEpoch)

    override suspend fun getLatestCycle(): BudgetCycleEntity? =
        budgetCycleDao.getLatest()

    override suspend fun createCycle(cycle: BudgetCycleEntity): Result<Long> = try {
        budgetCycleDao.clearCurrentFlag()
        val id = budgetCycleDao.insert(cycle)
        budgetCycleDao.setCurrent(id, System.currentTimeMillis())
        Result.Success(id)
    } catch (e: Exception) {
        Result.Failure(HasbiError.DatabaseError(e.message ?: "Create cycle failed"))
    }

    override suspend fun setCurrentCycle(id: Long): Result<Unit> = try {
        budgetCycleDao.clearCurrentFlag()
        budgetCycleDao.setCurrent(id, System.currentTimeMillis())
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Failure(HasbiError.DatabaseError(e.message ?: "Set current cycle failed"))
    }

    override suspend fun closeCycle(id: Long): Result<Unit> = try {
        budgetCycleDao.close(id, System.currentTimeMillis())
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Failure(HasbiError.DatabaseError(e.message ?: "Close cycle failed"))
    }

    override fun observeBudgetsWithSpent(
        cycleId: Long,
        cycleStart: Long,
        cycleEnd: Long
    ): Flow<List<BudgetWithSpent>> =
        budgetDao.observeBudgetsWithSpent(cycleId, cycleStart, cycleEnd)

    override fun observeByCycle(cycleId: Long): Flow<List<BudgetEntity>> =
        budgetDao.observeByCycle(cycleId)

    override fun observeOverallBudget(cycleId: Long): Flow<BudgetEntity?> =
        budgetDao.observeOverallBudget(cycleId)

    override fun observeCategoryBudget(cycleId: Long, categoryId: Long): Flow<BudgetEntity?> =
        budgetDao.observeCategoryBudget(cycleId, categoryId)

    override suspend fun getOverallBudget(cycleId: Long): BudgetEntity? =
        budgetDao.getOverallBudget(cycleId)

    override suspend fun getCategoryBudget(cycleId: Long, categoryId: Long): BudgetEntity? =
        budgetDao.getCategoryBudget(cycleId, categoryId)

    override suspend fun createBudget(budget: BudgetEntity): Result<Long> = try {
        // Check for existing overall budget (only one allowed per cycle)
        if (budget.categoryId == null) {
            val existingCount = budgetDao.countOverallBudgets(budget.cycleId)
            if (existingCount > 0) {
                return Result.Failure(
                    HasbiError.DatabaseError("Overall budget already exists for this cycle")
                )
            }
        }
        val id = budgetDao.insert(budget)
        Result.Success(id)
    } catch (e: Exception) {
        Result.Failure(HasbiError.DatabaseError(e.message ?: "Create budget failed"))
    }

    override suspend fun updateBudget(budget: BudgetEntity): Result<Unit> = try {
        budgetDao.update(budget)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Failure(HasbiError.DatabaseError(e.message ?: "Update budget failed"))
    }

    override suspend fun deleteBudget(id: Long): Result<Unit> = try {
        budgetDao.delete(id)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Failure(HasbiError.DatabaseError(e.message ?: "Delete budget failed"))
    }
}
