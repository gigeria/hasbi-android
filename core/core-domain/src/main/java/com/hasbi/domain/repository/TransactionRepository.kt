package com.hasbi.domain.repository

import com.hasbi.database.entity.TransactionType
import com.hasbi.domain.model.Transaction
import com.hasbi.util.result.Result
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {

    // === Observers ===
    fun observeRecent(limit: Int): Flow<List<Transaction>>
    fun observeByDateRange(startEpoch: Long, endEpoch: Long): Flow<List<Transaction>>
    fun observeByWallet(walletId: Long): Flow<List<Transaction>>
    fun observeRecentlyDeleted(): Flow<List<Transaction>>
    fun search(query: String): Flow<List<Transaction>>

    fun observeFiltered(
        startEpoch: Long,
        endEpoch: Long,
        typeFilter: TransactionType?,
        walletFilter: Long?,
        categoryFilter: Long?
    ): Flow<List<Transaction>>

    // === Single item ===
    suspend fun getById(id: Long): Transaction?

    // === Mutations ===
    suspend fun create(transaction: Transaction): Result<Long>
    suspend fun update(transaction: Transaction): Result<Unit>
    suspend fun softDelete(id: Long): Result<Unit>
    suspend fun restore(id: Long): Result<Unit>
    suspend fun hardDelete(id: Long): Result<Unit>

    /** WorkManager calls daily at 3 AM. Returns count of purged rows. */
    suspend fun purgeExpired(): Result<Int>

    // === Aggregations (Flow for reactive UI) ===
    fun observeTotalExpense(startEpoch: Long, endEpoch: Long): Flow<Long>
    fun observeTotalIncome(startEpoch: Long, endEpoch: Long): Flow<Long>
    fun observeCategoryExpense(categoryId: Long, startEpoch: Long, endEpoch: Long): Flow<Long>
    fun observeWalletExpense(walletId: Long, startEpoch: Long, endEpoch: Long): Flow<Long>
    fun observeTodaySpent(todayStartEpoch: Long, todayEndEpoch: Long): Flow<Long>

    // === Debt linkage ===
    fun observeDebtPayments(debtId: Long): Flow<List<Transaction>>
    fun observeDebtRepaidAmount(debtId: Long): Flow<Long>
    fun observeDebtPaidAmount(debtId: Long): Flow<Long>
}
