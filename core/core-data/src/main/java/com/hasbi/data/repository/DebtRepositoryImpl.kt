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

    override fun observeAll(): Flow<List<DebtEntity>> =
        debtDao.observeAll()

    override fun observeById(id: Long): Flow<DebtEntity?> =
        debtDao.observeById(id)

    override fun observeByIdWithRemaining(id: Long): Flow<DebtWithRemaining?> =
        debtDao.observeByIdWithRemaining(id)

    override fun observePayments(debtId: Long): Flow<List<Transaction>> =
        debtDao.let { transactionDao.observeDebtPayments(debtId).map { list -> list.map { it.toDomain() } } }

    override suspend fun getById(id: Long): DebtEntity? =
        debtDao.getById(id)

    override suspend fun create(debt: DebtEntity, fromWalletId: Long): Result<Long> = try {
        require(debt.originalAmount > 0) { "Amount must be positive" }

        val now = System.currentTimeMillis()
        val debtId = debtDao.insert(debt.copy(createdAt = now, updatedAt = now))

        // Create linked transaction
        val txType = when (debt.type) {
            DebtType.LENT -> TransactionType.DEBT_GIVEN        // money leaves wallet
            DebtType.BORROWED -> TransactionType.DEBT_RECEIVED  // money enters wallet
        }

        val tx = TransactionEntity(
            type = txType,
            amount = debt.originalAmount,
            walletId = fromWalletId,
            categoryId = null,
            toWalletId = null,
            date = debt.startDate,
            note = "Udhaar: ${debt.personName}",
            linkedDebtId = debtId,
            createdAt = now,
            updatedAt = now
        )
        transactionDao.insert(tx)

        Result.Success(debtId)
    } catch (e: Exception) {
        Result.Failure(HasbiError.DatabaseError(e.message ?: "Create debt failed"))
    }

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
            DebtType.LENT -> TransactionType.DEBT_REPAYMENT_IN      // money enters wallet (repayment received)
            DebtType.BORROWED -> TransactionType.DEBT_REPAYMENT_OUT  // money leaves wallet (repayment given)
        }

        val tx = TransactionEntity(
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
        val txId = transactionDao.insert(tx)

        // Auto-settle if fully repaid (async check would be ideal, but ok for now)
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
