package com.hasbi.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hasbi.database.entity.TransactionEntity
import com.hasbi.database.entity.TransactionType
import kotlinx.coroutines.flow.Flow

/**
 * Transaction DAO — most critical.
 *
 * Soft delete pattern:
 * - softDelete() sets isDeleted=1, deletedAt=now, purgeAfter=now+30days
 * - restore() clears all soft delete fields
 * - purgeExpired() called by WorkManager daily at 3 AM
 *
 * Aggregations use Flow for reactive UI updates.
 *
 * All date queries use INCLUSIVE start, EXCLUSIVE end:
 *   WHERE date >= :start AND date < :end
 */
@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    // === Soft delete ===
    @Query(
        """
        UPDATE transactions
        SET isDeleted = 1, deletedAt = :now, purgeAfter = :purgeTime, updatedAt = :now
        WHERE id = :id
        """
    )
    suspend fun softDelete(id: Long, now: Long, purgeTime: Long)

    @Query(
        """
        UPDATE transactions
        SET isDeleted = 0, deletedAt = NULL, purgeAfter = NULL, updatedAt = :now
        WHERE id = :id
        """
    )
    suspend fun restore(id: Long, now: Long)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun hardDelete(id: Long)

    /** WorkManager calls this daily at 3 AM. Returns count of purged rows. */
    @Query("DELETE FROM transactions WHERE isDeleted = 1 AND purgeAfter < :now")
    suspend fun purgeExpired(now: Long): Int

    // === Queries ===
    @Query("SELECT * FROM transactions WHERE id = :id AND isDeleted = 0")
    suspend fun getById(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getByIdIncludingDeleted(id: Long): TransactionEntity?

    @Query(
        """
        SELECT * FROM transactions
        WHERE isDeleted = 0
        ORDER BY date DESC, createdAt DESC
        LIMIT :limit
        """
    )
    fun observeRecent(limit: Int): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT * FROM transactions
        WHERE isDeleted = 0
        AND date >= :startDate AND date < :endDate
        ORDER BY date DESC, createdAt DESC
        """
    )
    fun observeByDateRange(startDate: Long, endDate: Long): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT * FROM transactions
        WHERE isDeleted = 0
        AND walletId = :walletId
        ORDER BY date DESC, createdAt DESC
        """
    )
    fun observeByWallet(walletId: Long): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT * FROM transactions
        WHERE isDeleted = 0
        AND (:typeFilter IS NULL OR type = :typeFilter)
        AND (:walletFilter IS NULL OR walletId = :walletFilter)
        AND (:categoryFilter IS NULL OR categoryId = :categoryFilter)
        AND date >= :startDate AND date < :endDate
        ORDER BY date DESC, createdAt DESC
        """
    )
    fun observeFiltered(
        startDate: Long,
        endDate: Long,
        typeFilter: TransactionType?,
        walletFilter: Long?,
        categoryFilter: Long?
    ): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT * FROM transactions
        WHERE isDeleted = 0
        AND (note LIKE '%' || :query || '%'
             OR EXISTS (
                 SELECT 1 FROM categories c
                 WHERE c.id = transactions.categoryId
                 AND c.name LIKE '%' || :query || '%'
             ))
        ORDER BY date DESC
        """
    )
    fun search(query: String): Flow<List<TransactionEntity>>

    // === Aggregations (for Dashboard + Budgets) ===

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE isDeleted = 0 AND type = 'EXPENSE'
        AND date >= :startDate AND date < :endDate
        """
    )
    fun observeTotalExpense(startDate: Long, endDate: Long): Flow<Long>

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE isDeleted = 0 AND type = 'INCOME'
        AND date >= :startDate AND date < :endDate
        """
    )
    fun observeTotalIncome(startDate: Long, endDate: Long): Flow<Long>

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE isDeleted = 0 AND type = 'EXPENSE'
        AND date >= :startDate AND date < :endDate
        AND walletId = :walletId
        """
    )
    fun observeWalletExpense(walletId: Long, startDate: Long, endDate: Long): Flow<Long>

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE isDeleted = 0 AND type = 'EXPENSE'
        AND categoryId = :categoryId
        AND date >= :startDate AND date < :endDate
        """
    )
    fun observeCategoryExpense(categoryId: Long, startDate: Long, endDate: Long): Flow<Long>

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE isDeleted = 0
        AND type IN ('EXPENSE', 'INCOME')
        AND date >= :todayStart AND date < :todayEnd
        """
    )
    fun observeTodaySpent(todayStart: Long, todayEnd: Long): Flow<Long>

    // === Recently deleted (trash bin) ===
    @Query(
        """
        SELECT * FROM transactions
        WHERE isDeleted = 1
        ORDER BY deletedAt DESC
        """
    )
    fun observeRecentlyDeleted(): Flow<List<TransactionEntity>>

    // === Debt linkage ===
    @Query(
        """
        SELECT * FROM transactions
        WHERE isDeleted = 0
        AND linkedDebtId = :debtId
        ORDER BY date DESC
        """
    )
    fun observeDebtPayments(debtId: Long): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE isDeleted = 0
        AND linkedDebtId = :debtId
        AND type = 'DEBT_REPAYMENT_IN'
        """
    )
    fun observeDebtRepaidAmount(debtId: Long): Flow<Long>

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE isDeleted = 0
        AND linkedDebtId = :debtId
        AND type = 'DEBT_REPAYMENT_OUT'
        """
    )
    fun observeDebtPaidAmount(debtId: Long): Flow<Long>

    // === Bank import deduplication (Phase 2.2 — query ready) ===
    @Query("SELECT COUNT(*) FROM transactions WHERE externalTransactionId = :externalId AND externalTransactionId IS NOT NULL")
    suspend fun countByExternalId(externalId: String): Int
}
