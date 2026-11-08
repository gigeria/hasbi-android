package com.hasbi.domain.usecase.debt

import com.hasbi.database.entity.DebtEntity
import com.hasbi.domain.repository.DebtRepository
import com.hasbi.util.result.HasbiError
import com.hasbi.util.result.Result
import javax.inject.Inject

/**
 * Create a new debt (lent or borrowed).
 *
 * Atomically creates:
 * 1. DebtEntity (metadata)
 * 2. TransactionEntity (DEBT_GIVEN or DEBT_RECEIVED) linked to wallet
 *
 * Wallet balance updates. NOT counted as income/expense.
 *
 * Example:
 *   LENT 5000 to Ali from Cash wallet:
 *     → DebtEntity(type=LENT, amount=5000, person=Ali)
 *     → TransactionEntity(type=DEBT_GIVEN, amount=5000, walletId=Cash)
 *     → Cash balance decreases by 5000
 *     → Expense reports unaffected
 */
class CreateDebtUseCase @Inject constructor(
    private val debtRepository: DebtRepository
) {

    suspend operator fun invoke(debt: DebtEntity, fromWalletId: Long): Result<Long> {
        if (debt.originalAmount <= 0) {
            return Result.Failure(HasbiError.InvalidTransferError("negative_amount"))
        }
        if (debt.personName.isBlank()) {
            return Result.Failure(HasbiError.UnknownError("Person name required"))
        }
        return debtRepository.create(debt, fromWalletId)
    }
}
