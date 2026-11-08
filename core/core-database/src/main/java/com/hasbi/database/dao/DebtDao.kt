package com.hasbi.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hasbi.database.entity.DebtEntity
import com.hasbi.database.entity.DebtType
import com.hasbi.database.relation.DebtWithRemaining
import kotlinx.coroutines.flow.Flow

/**
 * Debt DAO.
 *
 * Critical: `remainingAmount` is COMPUTED via SQL, never stored.
 * - For LENT: originalAmount - SUM(DEBT_REPAYMENT_IN linked to this debt)
 * - For BORROWED: originalAmount - SUM(DEBT_REPAYMENT_OUT linked to this debt)
 */
@Dao
interface DebtDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(debt: DebtEntity): Long

    @Update
    suspend fun update(debt: DebtEntity)

    @Query("UPDATE debts SET isArchived = 1, updatedAt = :time WHERE id = :id")
    suspend fun archive(id: Long, time: Long)

    @Query("UPDATE debts SET isSettled = :isSettled, updatedAt = :time WHERE id = :id")
    suspend fun setSettled(id: Long, isSettled: Boolean, time: Long)

    @Query("SELECT * FROM debts WHERE id = :id")
    suspend fun getById(id: Long): DebtEntity?

    @Query("SELECT * FROM debts WHERE id = :id")
    fun observeById(id: Long): Flow<DebtEntity?>

    @Query("SELECT * FROM debts WHERE isArchived = 0 AND type = 'LENT' ORDER BY startDate DESC")
    fun observeLent(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE isArchived = 0 AND type = 'BORROWED' ORDER BY startDate DESC")
    fun observeBorrowed(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE isArchived = 0 ORDER BY startDate DESC")
    fun observeAll(): Flow<List<DebtEntity>>

    /**
     * LENT debts with computed remaining.
     *
     * remaining = originalAmount - SUM(DEBT_REPAYMENT_IN)
     * (because when someone repays me, type=DEBT_REPAYMENT_IN)
     */
    @Query(
        """
        SELECT
            d.*,
            d.originalAmount - COALESCE((
                SELECT SUM(t.amount) FROM transactions t
                WHERE t.linkedDebtId = d.id
                AND t.type = 'DEBT_REPAYMENT_IN'
                AND t.isDeleted = 0
            ), 0) AS remainingAmount
        FROM debts d
        WHERE d.isArchived = 0 AND d.type = 'LENT'
        ORDER BY d.startDate DESC
        """
    )
    fun observeLentWithRemaining(): Flow<List<DebtWithRemaining>>

    /**
     * BORROWED debts with computed remaining.
     *
     * remaining = originalAmount - SUM(DEBT_REPAYMENT_OUT)
     * (because when I repay someone, type=DEBT_REPAYMENT_OUT)
     */
    @Query(
        """
        SELECT
            d.*,
            d.originalAmount - COALESCE((
                SELECT SUM(t.amount) FROM transactions t
                WHERE t.linkedDebtId = d.id
                AND t.type = 'DEBT_REPAYMENT_OUT'
                AND t.isDeleted = 0
            ), 0) AS remainingAmount
        FROM debts d
        WHERE d.isArchived = 0 AND d.type = 'BORROWED'
        ORDER BY d.startDate DESC
        """
    )
    fun observeBorrowedWithRemaining(): Flow<List<DebtWithRemaining>>

    @Query(
        """
        SELECT
            d.*,
            d.originalAmount - COALESCE((
                SELECT SUM(t.amount) FROM transactions t
                WHERE t.linkedDebtId = d.id
                AND t.type = CASE WHEN d.type = 'LENT' THEN 'DEBT_REPAYMENT_IN' ELSE 'DEBT_REPAYMENT_OUT' END
                AND t.isDeleted = 0
            ), 0) AS remainingAmount
        FROM debts d
        WHERE d.id = :id
        """
    )
    fun observeByIdWithRemaining(id: Long): Flow<DebtWithRemaining?>

    @Query("SELECT COUNT(*) FROM debts WHERE isArchived = 0")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM debts WHERE isArchived = 0 AND type = :type")
    suspend fun countByType(type: DebtType): Int
}
