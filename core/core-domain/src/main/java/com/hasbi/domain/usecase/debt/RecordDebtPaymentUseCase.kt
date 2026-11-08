package com.hasbi.domain.usecase.debt

import com.hasbi.domain.repository.DebtRepository
import com.hasbi.util.result.HasbiError
import com.hasbi.util.result.Result
import javax.inject.Inject

/**
 * Record a debt repayment (partial or full).
 *
 * For LENT debt (someone repays me):
 *   → TransactionEntity(DEBT_REPAYMENT_IN) to wallet
 *   → Wallet balance increases
 *
 * For BORROWED debt (I repay someone):
 *   → TransactionEntity(DEBT_REPAYMENT_OUT) from wallet
 *   → Wallet balance decreases
 *
 * NOT counted as income/expense (prevents double-counting).
 */
class RecordDebtPaymentUseCase @Inject constructor(
    private val debtRepository: DebtRepository
) {

    suspend operator fun invoke(
        debtId: Long,
        amount: Long,
        walletId: Long,
        date: Long,
        note: String? = null
    ): Result<Long> {
        if (amount <= 0) {
            return Result.Failure(HasbiError.InvalidTransferError("negative_amount"))
        }
        return debtRepository.recordPayment(debtId, amount, walletId, date, note)
    }
}
