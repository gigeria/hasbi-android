package com.hasbi.domain.usecase.transaction

import com.hasbi.domain.repository.TransactionRepository
import com.hasbi.util.result.HasbiError
import com.hasbi.util.result.Result
import javax.inject.Inject

/**
 * Soft delete a transaction.
 *
 * - Sets isDeleted=true, deletedAt=now, purgeAfter=now+30days
 * - Transaction remains recoverable from "Recently Deleted" for 30 days
 * - WorkManager purges transactions where purgeAfter < now (daily at 3 AM)
 */
class SoftDeleteTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(id: Long): Result<Unit> =
        transactionRepository.softDelete(id)
}

/**
 * Restore a soft-deleted transaction.
 * Clears isDeleted, deletedAt, purgeAfter fields.
 */
class RestoreTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(id: Long): Result<Unit> =
        transactionRepository.restore(id)
}

/**
 * Purge expired soft-deleted transactions.
 * Called by WorkManager daily at 3 AM.
 * Returns count of purged rows.
 */
class PurgeExpiredUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(): Result<Int> =
        transactionRepository.purgeExpired()
}
