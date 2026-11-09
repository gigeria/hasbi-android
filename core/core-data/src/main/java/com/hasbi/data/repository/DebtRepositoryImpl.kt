package com.hasbi.data.repository

import com.hasbi.data.mapper.toDomain
import com.hasbi.database.dao.DebtDao
import com.hasbi.database.dao.TransactionDao
import com.hasbi.database.entity.DebtEntity
import com.hasbi.database.entity.DebtType
import com.hasbi.database.entity.TransactionEntity
import com.hasbi.database.entity.TransactionType
import com.hasbi.database.relation.DebtWithRemaining
import com.hasbi.domain.model.Transaction
import com.hasbi.domain.repository.DebtRepository
import com.hasbi.util.result.HasbiError
import com.hasbi.util.result.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DebtRepositoryImpl @Inject constructor(
    private val debtDao: DebtDao,
    private val transactionDao: TransactionDao
) : DebtRepository {

    override fun observeLentWithRemaining(): Flow<List<DebtWithRemaining>> =
        debtDao.observeLentWithRemaining()

    override fun observeBorrowedWithRemaining(): Flow<List<DebtWithRemaining>> =
        debtDao.observeBorrowedWithRemaining()

    override fun observeAll(): Flow<List<DebtEntity>> = debtDao.observeAll()

    override fun observeById(id: Long): Flow<DebtEntity?> = debtDao.observeById(id)

    override fun observeByIdWithRemaining(id: Long): Flow<DebtWithRemaining?> =
        debtDao.observeByIdWithRemaining(id)

    override fun observePayments(debtId: Long): Flow<List<Transaction>> =
        transactionDao.observeDebtPayments(debtId).map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): DebtEntity? = debtDao.getById(id)

    /**
     * FIX DEBT-001: Atomic debt creation.
     *
     * Uses @Transaction method — both debt + linked transaction
     * insert atomically. No orphaned records possible.
     */
    override suspend fun create(debt: DebtEntity, fromWalletId: Long): Result<Long> = try {
        require(debt.originalAmount > 0) { "Amount must be positive" }

        val now = System.currentTimeMillis()
        val debtToInsert = debt.copy(createdAt = now, updatedAt = now)

        val txType = when (debt.type) {
            DebtType.LENT -> TransactionType.DEBT_GIVEN
            DebtType.BORROWED -> TransactionType.DEBT_RECEIVED
        }

        val transaction = TransactionEntity(
            type = txType,
            amount = debt.originalAmount,
            walletId = fromWalletId,
            categoryId = null,
            toWalletId = null,
            date = debt.startDate,
            note = "Udhaar: ${debt.personName}",
            linkedDebtId = 0, // will be set by DAO
            createdAt = now,
            updatedAt = now
        )

        // ATOMIC: both inserts in single @Transaction
        val debtId = debtDao.createDebtWithTransaction(debtToInsert, transaction)
        Result.Success(debtId)
    } catch (e: Exception) {
        Result.Failure(HasbiError.DatabaseError(e.message ?: "Create debt failed"))
    }

    /**
     * FIX DEBT-002: Atomic payment recording with auto-settlement.
     *
     * Uses @Transaction method — inserts payment + auto-settles if remaining = 0.
     */
    override suspend fun recordPayment(
        debtId: Long,
        amount: Long,
        walletId: Long,
        date: Long,
        note: String?
    ): Result<Long> = try {
        require(amount > 0) { "Amount must be positive" }

        val debt = debtDao.getById(debtId)
            ?: return Result.Failure(HasbiError.DebtNotFoundError(debtId))

        val now = System.currentTimeMillis()
        val txType = when (debt.type) {
            DebtType.LENT -> TransactionType.DEBT_REPAYMENT_IN
            DebtType.BORROWED -> TransactionType.DEBT_REPAYMENT_OUT
        }

        val transaction = TransactionEntity(
            type = txType,
            amount = amount,
            walletId = walletId,
            categoryId = null,
            toWalletId = null,
            date = date,
            note = note ?: "Udhaar payment: ${debt.personName}",
            linkedDebtId = debtId,
            createdAt = now,
            updatedAt = now
        )

        // ATOMIC: payment + auto-settle check
        val txId = debtDao.recordPaymentWithSettlement(transaction, debtId, debt.type)
        Result.Success(txId)
    } catch (e: Exception) {
        Result.Failure(HasbiError.DatabaseError(e.message ?: "Record payment failed"))
    }

    override suspend fun archive(id: Long): Result<Unit> = try {
        debtDao.archive(id, System.currentTimeMillis())
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Failure(HasbiError.DatabaseError(e.message ?: "Archive debt failed"))
    }

    override suspend fun setSettled(id: Long, isSettled: Boolean): Result<Unit> = try {
        debtDao.setSettled(id, isSettled, System.currentTimeMillis())
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Failure(HasbiError.DatabaseError(e.message ?: "Set settled failed"))
    }
}
