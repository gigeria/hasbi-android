package com.hasbi.domain.repository

import com.hasbi.database.entity.DebtEntity
import com.hasbi.database.relation.DebtWithRemaining
import com.hasbi.domain.model.Transaction
import com.hasbi.util.result.Result
import kotlinx.coroutines.flow.Flow

interface DebtRepository {

    fun observeLentWithRemaining(): Flow<List<DebtWithRemaining>>
    fun observeBorrowedWithRemaining(): Flow<List<DebtWithRemaining>>
    fun observeAll(): Flow<List<DebtEntity>>
    fun observeById(id: Long): Flow<DebtEntity?>
    fun observeByIdWithRemaining(id: Long): Flow<DebtWithRemaining?>
    fun observePayments(debtId: Long): Flow<List<Transaction>>

    suspend fun getById(id: Long): DebtEntity?

    /**
     * Create debt + linked transaction atomically.
     *
     * - For LENT: creates DebtEntity + TransactionEntity(DEBT_GIVEN) from wallet
     * - For BORROWED: creates DebtEntity + TransactionEntity(DEBT_RECEIVED) to wallet
     *
     * Wallet balance updates. NOT counted as income/expense.
     */
    suspend fun create(debt: DebtEntity, fromWalletId: Long): Result<Long>

    /**
     * Record partial/full repayment.
     *
     * - For LENT (someone repays me): creates TransactionEntity(DEBT_REPAYMENT_IN) to wallet
     * - For BORROWED (I repay someone): creates TransactionEntity(DEBT_REPAYMENT_OUT) from wallet
     */
    suspend fun recordPayment(
        debtId: Long,
        amount: Long,
        walletId: Long,
        date: Long,
        note: String?
    ): Result<Long>

    suspend fun archive(id: Long): Result<Unit>
    suspend fun setSettled(id: Long, isSettled: Boolean): Result<Unit>
}
