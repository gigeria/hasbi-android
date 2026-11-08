package com.hasbi.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hasbi.database.entity.BudgetEntity
import com.hasbi.database.relation.BudgetWithSpent
import kotlinx.coroutines.flow.Flow

/**
 * Budget DAO.
 *
 * Critical rules:
 * - `categoryId = null` represents overall budget (NOT 0)
 * - Application-level rule: one overall budget per cycle (enforced via partial unique index)
 * - NO `spent` field — computed live via SQL aggregation
 */
@Dao
interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(budget: BudgetEntity): Long

    @Update
    suspend fun update(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("SELECT * FROM budgets WHERE cycleId = :cycleId ORDER BY categoryId IS NULL DESC, categoryId ASC")
    fun observeByCycle(cycleId: Long): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE cycleId = :cycleId AND categoryId IS NULL LIMIT 1")
    suspend fun getOverallBudget(cycleId: Long): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE cycleId = :cycleId AND categoryId = :categoryId LIMIT 1")
    suspend fun getCategoryBudget(cycleId: Long, categoryId: Long): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE cycleId = :cycleId AND categoryId IS NULL LIMIT 1")
    fun observeOverallBudget(cycleId: Long): Flow<BudgetEntity?>

    @Query("SELECT * FROM budgets WHERE cycleId = :cycleId AND categoryId = :categoryId LIMIT 1")
    fun observeCategoryBudget(cycleId: Long, categoryId: Long): Flow<BudgetEntity?>

    /**
     * Combined query with computed spent.
     *
     * For overall budget (categoryId IS NULL): sums ALL expenses in cycle.
     * For category budget: sums expenses with matching categoryId in cycle.
     *
     * Note: TRANSFER, DEBT_*, SAVINGS_* types are NOT counted as expenses.
     */
    @Query(
        """
        SELECT
            b.id AS id,
            b.cycleId AS cycleId,
            b.categoryId AS categoryId,
            b.limit AS `limit`,
            b.warningThreshold AS warningThreshold,
            b.createdAt AS createdAt,
            b.updatedAt AS updatedAt,
            COALESCE((
                SELECT SUM(t.amount) FROM transactions t
                WHERE t.isDeleted = 0 AND t.type = 'EXPENSE'
                AND t.date >= :cycleStart AND t.date < :cycleEnd
                AND (b.categoryId IS NULL OR t.categoryId = b.categoryId)
            ), 0) AS spent
        FROM budgets b
        WHERE b.cycleId = :cycleId
        ORDER BY b.categoryId IS NULL DESC, b.categoryId ASC
        """
    )
    fun observeBudgetsWithSpent(cycleId: Long, cycleStart: Long, cycleEnd: Long): Flow<List<BudgetWithSpent>>

    @Query("SELECT COUNT(*) FROM budgets WHERE cycleId = :cycleId AND categoryId IS NULL")
    suspend fun countOverallBudgets(cycleId: Long): Int

    @Query("SELECT COUNT(*) FROM budgets")
    suspend fun count(): Int
}
