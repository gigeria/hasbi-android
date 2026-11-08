package com.hasbi.data.repository

import com.hasbi.data.mapper.toDomain
import com.hasbi.data.mapper.toEntity
import com.hasbi.database.dao.TransactionDao
import com.hasbi.database.entity.TransactionType
import com.hasbi.domain.model.Transaction
import com.hasbi.domain.repository.TransactionRepository
import com.hasbi.util.date.DateTimeUtils
import com.hasbi.util.result.HasbiError
import com.hasbi.util.result.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao
) : TransactionRepository {

    override fun observeRecent(limit: Int): Flow<List<Transaction>> =
        transactionDao.observeRecent(limit).map { list -> list.map { it.toDomain() } }

    override fun observeByDateRange(startEpoch: Long, endEpoch: Long): Flow<List<Transaction>> =
        transactionDao.observeByDateRange(startEpoch, endEpoch).map { list -> list.map { it.toDomain() } }

    override fun observeByWallet(walletId: Long): Flow<List<Transaction>> =
        transactionDao.observeByWallet(walletId).map { list -> list.map { it.toDomain() } }

    override fun observeRecentlyDeleted(): Flow<List<Transaction>> =
        transactionDao.observeRecentlyDeleted().map { list -> list.map { it.toDomain() } }

    override fun search(query: String): Flow<List<Transaction>> =
        transactionDao.search(query).map { list -> list.map { it.toDomain() } }

    override fun observeFiltered(
        startEpoch: Long,
        endEpoch: Long,
        typeFilter: TransactionType?,
        walletFilter: Long?,
        categoryFilter: Long?
    ): Flow<List<Transaction>> =
        transactionDao.observeFiltered(
            startEpoch, endEpoch, typeFilter, walletFilter, categoryFilter
        ).map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): Transaction? =
        transactionDao.getById(id)?.toDomain()

    override suspend fun create(transaction: Transaction): Result<Long> = try {
        validateTransaction(transaction)
        val now = System.currentTimeMillis()
        val entity = transaction.copy(createdAt = now, updatedAt = now).toEntity()
        val id = transactionDao.insert(entity)
        Result.Success(id)
    } catch (e: IllegalArgumentException) {
        Result.Failure(HasbiError.InvalidTransferError(e.message ?: "invalid"))
    } catch (e: Exception) {
        Result.Failure(HasbiError.DatabaseError(e.message ?: "Create transaction failed"))
    }

    override suspend fun update(transaction: Transaction): Result<Unit> = try {
        validateTransaction(transaction)
        transactionDao.update(transaction.copy(updatedAt = System.currentTimeMillis()).toEntity())
        Result.Success(Unit)
    } catch (e: IllegalArgumentException) {
        Result.Failure(HasbiError.InvalidTransferError(e.message ?: "invalid"))
    } catch (e: Exception) {
        Result.Failure(HasbiError.DatabaseError(e.message ?: "Update transaction failed"))
    }

    override suspend fun softDelete(id: Long): Result<Unit> = try {
        val now = System.currentTimeMillis()
        val purgeTime = now + DateTimeUtils.THIRTY_DAYS_MILLIS
        transactionDao.softDelete(id, now, purgeTime)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Failure(HasbiError.DatabaseError(e.message ?: "Soft delete failed"))
    }

    override suspend fun restore(id: Long): Result<Unit> = try {
        transactionDao.restore(id, System.currentTimeMillis())
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Failure(HasbiError.DatabaseError(e.message ?: "Restore failed"))
    }

    override suspend fun hardDelete(id: Long): Result<Unit> = try {
        transactionDao.hardDelete(id)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Failure(HasbiError.DatabaseError(e.message ?: "Hard delete failed"))
    }

    override suspend fun purgeExpired(): Result<Int> = try {
        val count = transactionDao.purgeExpired(System.currentTimeMillis())
        Result.Success(count)
    } catch (e: Exception) {
        Result.Failure(HasbiError.DatabaseError(e.message ?: "Purge failed"))
    }

    override fun observeTotalExpense(startEpoch: Long, endEpoch: Long): Flow<Long> =
        transactionDao.observeTotalExpense(startEpoch, endEpoch)

    override fun observeTotalIncome(startEpoch: Long, endEpoch: Long): Flow<Long> =
        transactionDao.observeTotalIncome(startEpoch, endEpoch)

    override fun observeCategoryExpense(categoryId: Long, startEpoch: Long, endEpoch: Long): Flow<Long> =
        transactionDao.observeCategoryExpense(categoryId, startEpoch, endEpoch)

    override fun observeWalletExpense(walletId: Long, startEpoch: Long, endEpoch: Long): Flow<Long> =
        transactionDao.observeWalletExpense(walletId, startEpoch, endEpoch)

    override fun observeTodaySpent(todayStartEpoch: Long, todayEndEpoch: Long): Flow<Long> =
        transactionDao.observeTodaySpent(todayStartEpoch, todayEndEpoch)

    override fun observeDebtPayments(debtId: Long): Flow<List<Transaction>> =
        transactionDao.observeDebtPayments(debtId).map { list -> list.map { it.toDomain() } }

    override fun observeDebtRepaidAmount(debtId: Long): Flow<Long> =
        transactionDao.observeDebtRepaidAmount(debtId)

    override fun observeDebtPaidAmount(debtId: Long): Flow<Long> =
        transactionDao.observeDebtPaidAmount(debtId)

    /**
     * Validate transaction rules:
     * - amount > 0
     * - TRANSFER: toWalletId required, categoryId must be null, source != dest
     * - EXPENSE/INCOME: toWalletId must be null
     */
    private fun validateTransaction(t: Transaction) {
        require(t.amount > 0) { "negative_amount" }

        when (t.type) {
            TransactionType.TRANSFER -> {
                requireNotNull(t.toWalletId) { "missing_destination" }
                require(t.toWalletId != t.walletId) { "same_wallet" }
                require(t.categoryId == null) { "transfer should not have category" }
            }
            TransactionType.EXPENSE, TransactionType.INCOME -> {
                require(t.toWalletId == null) { "expense/income should not have toWalletId" }
            }
            else -> {
                // DEBT_* and SAVINGS_* types — validate in their use cases
            }
        }
    }
}
