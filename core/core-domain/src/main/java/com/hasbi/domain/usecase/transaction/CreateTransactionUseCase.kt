package com.hasbi.domain.usecase.transaction

import com.hasbi.domain.model.Transaction
import com.hasbi.domain.repository.TransactionRepository
import com.hasbi.util.result.HasbiError
import com.hasbi.util.result.Result
import javax.inject.Inject

/**
 * Create a new transaction (income/expense/transfer).
 *
 * Validates:
 * - amount > 0
 * - For TRANSFER: toWalletId != null, source != dest, categoryId = null
 * - For EXPENSE/INCOME: toWalletId = null
 *
 * Wallet existence check is performed in repository (FK constraint).
 */
class CreateTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {

    suspend operator fun invoke(transaction: Transaction): Result<Long> {
        if (transaction.amount <= 0) {
            return Result.Failure(HasbiError.InvalidTransferError("negative_amount"))
        }
        return transactionRepository.create(transaction)
    }
}
