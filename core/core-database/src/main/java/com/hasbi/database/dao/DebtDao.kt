package com.hasbi.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.hasbi.database.entity.DebtEntity
import com.hasbi.database.entity.DebtType
import com.hasbi.database.entity.TransactionEntity
import com.hasbi.database.relation.DebtWithRemaining
import kotlinx.coroutines.flow.Flow

/**
 * Debt DAO.
 *
 * FIX DEBT-001: createDebtWithTransaction() is @Transaction — atomic.
 * If either insert fails, both roll back. No orphaned records.
 *
 * Critical: `remainingAmount` is COMPUTED via SQL, never stored.
 * - For LENT: originalAmount - SUM(DEBT_REPAYMENT_IN linked to this debt)
 * - For BORROWED: originalAmount - SUM(DEBT_REPAYMENT_OUT linked to this debt)
 */
@Dao
abstract class DebtDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insert(debt: DebtEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    abstract suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    abstract suspend fun update(debt: DebtEntity)

    @Query("UPDATE debts SET isArchived = 1, updatedAt = :time WHERE id = :id")
    abstract suspend fun archive(id: Long, time: Long)

    @Query("UPDATE debts SET isSettled = :isSettled, updatedAt = :time WHERE id = :id")
    abstract suspend fun setSettled(id: Long, isSettled: Boolean, time: Long)

    @Query("SELECT * FROM debts WHERE id = :id")
    abstract suspend fun getById(id: Long): DebtEntity?

    @Query("SELECT * FROM debts WHERE id = :id")
    abstract fun observeById(id: Long): Flow<DebtEntity?>

    @Query("SELECT * FROM debts WHERE isArchived = 0 AND type = 'LENT' ORDER BY startDate DESC")
    abstract fun observeLent(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE isArchived = 0 AND type = 'BORROWED' ORDER BY startDate DESC")
    abstract fun observeBorrowed(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE isArchived = 0 ORDER BY startDate DESC")
    abstract fun observeAll(): Flow<List<DebtEntity>>

    /**
     * FIX DEBT-001: Atomic debt creation.
     *
     * Inserts DebtEntity + linked TransactionEntity in single @Transaction.
     * If transaction insert fails, debt insert rolls back.
     * No orphaned records possible.
     */
    @Transaction
    open suspend fun createDebtWithTransaction(
        debt: DebtEntity,
        transaction: TransactionEntity
    ): Long {
        val debtId = insert(debt)
        insertTransaction(transaction.copy(linkedDebtId = debtId))
        return debtId
    }

    /**
     * FIX DEBT-002: Atomic debt payment recording.
     *
     * Inserts repayment transaction atomically.
     * Auto-settles debt if remaining becomes 0.
     */
    @Transaction
    open suspend fun recordPaymentWithSettlement(
        transaction: TransactionEntity,
        debtId: Long,
        debtType: DebtType
    ): Long {
        val txId = insertTransaction(transaction)

        // Check if debt is fully settled
        val remaining = if (debtType == DebtType.LENT) {
            getRemainingForLent(debtId)
        } else {
            getRemainingForBorrowed(debtId)
        }

        if (remaining <= 0) {
            setSettled(debtId, true, System.currentTimeMillis())
        }

        return txId
    }

    @Query("""
        SELECT d.originalAmount - COALESCE((
            SELECT SUM(t.amount) FROM transactions t
            WHERE t.linkedDebtId = d.id
            AND t.type = 'DEBT_REPAYMENT_IN'
            AND t.isDeleted = 0
        ), 0) FROM debts d WHERE d.id = :debtId
    """)
    abstract suspend fun getRemainingForLent(debtId: Long): Long

    @Query("""
        SELECT d.originalAmount - COALESCE((
            SELECT SUM(t.amount) FROM transactions t
            WHERE t.linkedDebtId = d.id
            AND t.type = 'DEBT_REPAYMENT_OUT'
            AND t.isDeleted = 0
        ), 0) FROM debts d WHERE d.id = :debtId
    """)
    abstract suspend fun getRemainingForBorrowed(debtId: Long): Long

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
    abstract fun observeLentWithRemaining(): Flow<List<DebtWithRemaining>>

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
    abstract fun observeBorrowedWithRemaining(): Flow<List<DebtWithRemaining>>

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
    abstract fun observeByIdWithRemaining(id: Long): Flow<DebtWithRemaining?>

    @Query("SELECT COUNT(*) FROM debts WHERE isArchived = 0")
    abstract suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM debts WHERE isArchived = 0 AND type = :type")
    abstract suspend fun countByType(type: DebtType): Int
}
