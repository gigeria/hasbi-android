package com.hasbi.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.hasbi.database.entity.WalletEntity
import com.hasbi.database.relation.WalletWithBalance
import kotlinx.coroutines.flow.Flow

/**
 * Wallet DAO.
 *
 * Critical:
 * - Use @Update (NOT REPLACE strategy) — preserves row identity
 * - Soft delete via `isArchived` (RESTRICT FK prevents hard delete with transactions)
 * - Balance computed via SQL subqueries
 */
@Dao
interface WalletDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(wallet: WalletEntity): Long

    @Update
    suspend fun update(wallet: WalletEntity)

    @Query("UPDATE wallets SET isArchived = 1, updatedAt = :time WHERE id = :id")
    suspend fun archive(id: Long, time: Long)

    @Query("UPDATE wallets SET isHidden = :isHidden, updatedAt = :time WHERE id = :id")
    suspend fun setHidden(id: Long, isHidden: Boolean, time: Long)

    @Query("UPDATE wallets SET sortOrder = :sortOrder, updatedAt = :time WHERE id = :id")
    suspend fun updateSortOrder(id: Long, sortOrder: Int, time: Long)

    @Query("SELECT * FROM wallets WHERE isArchived = 0 ORDER BY sortOrder ASC")
    fun observeAll(): Flow<List<WalletEntity>>

    @Query("SELECT * FROM wallets WHERE isArchived = 0 AND isHidden = 0 ORDER BY sortOrder ASC")
    fun observeVisible(): Flow<List<WalletEntity>>

    @Query("SELECT * FROM wallets WHERE id = :id")
    suspend fun getById(id: Long): WalletEntity?

    @Query("SELECT * FROM wallets WHERE id = :id")
    fun observeById(id: Long): Flow<WalletEntity?>

    /**
     * Get all wallets with computed balance.
     *
     * Balance = initialBalance
     *           + INCOME - EXPENSE (wallet as source)
     *           + TRANSFER_in - TRANSFER_out
     *           + DEBT_RECEIVED - DEBT_GIVEN (debt movements)
     *           + DEBT_REPAYMENT_IN - DEBT_REPAYMENT_OUT
     *           + SAVINGS_WITHDRAW - SAVINGS_DEPOSIT
     *
     * Note: SAVINGS_* and DEBT_* are NOT counted in income/expense reports,
     * but DO affect wallet balance.
     */
    @Query(
        """
        SELECT
            w.id AS id,
            w.name AS name,
            w.icon AS icon,
            w.colorHex AS colorHex,
            w.type AS type,
            w.currencyCode AS currencyCode,
            w.initialBalance
            + COALESCE((SELECT SUM(amount) FROM transactions
                       WHERE walletId = w.id AND type = 'INCOME' AND isDeleted = 0), 0)
            - COALESCE((SELECT SUM(amount) FROM transactions
                       WHERE walletId = w.id AND type = 'EXPENSE' AND isDeleted = 0), 0)
            + COALESCE((SELECT SUM(amount) FROM transactions
                       WHERE toWalletId = w.id AND type = 'TRANSFER' AND isDeleted = 0), 0)
            - COALESCE((SELECT SUM(amount) FROM transactions
                       WHERE walletId = w.id AND type = 'TRANSFER' AND isDeleted = 0), 0)
            + COALESCE((SELECT SUM(amount) FROM transactions
                       WHERE toWalletId = w.id AND type = 'DEBT_GIVEN' AND isDeleted = 0), 0)
            - COALESCE((SELECT SUM(amount) FROM transactions
                       WHERE walletId = w.id AND type = 'DEBT_GIVEN' AND isDeleted = 0), 0)
            + COALESCE((SELECT SUM(amount) FROM transactions
                       WHERE toWalletId = w.id AND type = 'DEBT_RECEIVED' AND isDeleted = 0), 0)
            - COALESCE((SELECT SUM(amount) FROM transactions
                       WHERE walletId = w.id AND type = 'DEBT_RECEIVED' AND isDeleted = 0), 0)
            + COALESCE((SELECT SUM(amount) FROM transactions
                       WHERE toWalletId = w.id AND type = 'DEBT_REPAYMENT_IN' AND isDeleted = 0), 0)
            - COALESCE((SELECT SUM(amount) FROM transactions
                       WHERE walletId = w.id AND type = 'DEBT_REPAYMENT_IN' AND isDeleted = 0), 0)
            + COALESCE((SELECT SUM(amount) FROM transactions
                       WHERE toWalletId = w.id AND type = 'DEBT_REPAYMENT_OUT' AND isDeleted = 0), 0)
            - COALESCE((SELECT SUM(amount) FROM transactions
                       WHERE walletId = w.id AND type = 'DEBT_REPAYMENT_OUT' AND isDeleted = 0), 0)
            + COALESCE((SELECT SUM(amount) FROM transactions
                       WHERE toWalletId = w.id AND type = 'SAVINGS_WITHDRAW' AND isDeleted = 0), 0)
            - COALESCE((SELECT SUM(amount) FROM transactions
                       WHERE walletId = w.id AND type = 'SAVINGS_WITHDRAW' AND isDeleted = 0), 0)
            + COALESCE((SELECT SUM(amount) FROM transactions
                       WHERE toWalletId = w.id AND type = 'SAVINGS_DEPOSIT' AND isDeleted = 0), 0)
            - COALESCE((SELECT SUM(amount) FROM transactions
                       WHERE walletId = w.id AND type = 'SAVINGS_DEPOSIT' AND isDeleted = 0), 0)
            AS balance
        FROM wallets w
        WHERE w.isArchived = 0
        ORDER BY w.sortOrder ASC
        """
    )
    fun observeAllWithBalance(): Flow<List<WalletWithBalance>>

    @Query(
        """
        SELECT
            w.initialBalance
            + COALESCE((SELECT SUM(amount) FROM transactions
                       WHERE walletId = w.id AND type = 'INCOME' AND isDeleted = 0), 0)
            - COALESCE((SELECT SUM(amount) FROM transactions
                       WHERE walletId = w.id AND type = 'EXPENSE' AND isDeleted = 0), 0)
            + COALESCE((SELECT SUM(amount) FROM transactions
                       WHERE toWalletId = w.id AND type = 'TRANSFER' AND isDeleted = 0), 0)
            - COALESCE((SELECT SUM(amount) FROM transactions
                       WHERE walletId = w.id AND type = 'TRANSFER' AND isDeleted = 0), 0)
            AS balance
        FROM wallets w
        WHERE w.id = :walletId
        """
    )
    fun observeBalance(walletId: Long): Flow<Long>

    @Query("SELECT COUNT(*) FROM transactions WHERE walletId = :id AND isDeleted = 0")
    suspend fun getTransactionCount(id: Long): Int
}
