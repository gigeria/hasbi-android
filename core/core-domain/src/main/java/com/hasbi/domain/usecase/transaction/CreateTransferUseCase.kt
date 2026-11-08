package com.hasbi.domain.usecase.transaction

import com.hasbi.database.entity.TransactionType
import com.hasbi.domain.model.Transaction
import com.hasbi.domain.repository.TransactionRepository
import com.hasbi.domain.repository.WalletRepository
import com.hasbi.util.result.HasbiError
import com.hasbi.util.result.Result
import javax.inject.Inject

/**
 * Create a transfer between wallets.
 *
 * Critical rules:
 * - Source wallet != destination wallet
 * - Both wallets must exist
 * - amount > 0
 * - categoryId = null (transfers have no category)
 * - Does NOT count as income or expense
 *
 * Multi-currency ready:
 * - If wallets have different currencies, exchangeRate + convertedAmount required
 * - Phase 4: full multi-currency conversion support
 */
class CreateTransferUseCase @Inject constructor(
    private val walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository
) {

    suspend operator fun invoke(
        fromWalletId: Long,
        toWalletId: Long,
        amount: Long,
        date: Long,
        note: String? = null,
        exchangeRate: Double? = null,
        convertedAmount: Long? = null
    ): Result<Long> {
        // Validate basic rules
        if (amount <= 0) {
            return Result.Failure(HasbiError.InvalidTransferError("negative_amount"))
        }
        if (fromWalletId == toWalletId) {
            return Result.Failure(HasbiError.InvalidTransferError("same_wallet"))
        }

        // Validate wallets exist
        val fromWallet = walletRepository.getById(fromWalletId)
            ?: return Result.Failure(HasbiError.WalletNotFoundError(fromWalletId))
        val toWallet = walletRepository.getById(toWalletId)
            ?: return Result.Failure(HasbiError.WalletNotFoundError(toWalletId))

        // Multi-currency check (Phase 4 prep)
        val isCrossCurrency = fromWallet.currencyCode != toWallet.currencyCode
        val finalConvertedAmount: Long?
        val finalExchangeRate: Double?

        if (isCrossCurrency) {
            if (exchangeRate == null || convertedAmount == null) {
                return Result.Failure(
                    HasbiError.InvalidTransferError("Cross-currency transfer requires exchangeRate + convertedAmount")
                )
            }
            finalExchangeRate = exchangeRate
            finalConvertedAmount = convertedAmount
        } else {
            finalExchangeRate = null
            finalConvertedAmount = null
        }

        // Build transaction
        val now = System.currentTimeMillis()
        val transaction = Transaction(
            type = TransactionType.TRANSFER,
            amount = amount,
            walletId = fromWalletId,
            categoryId = null,
            toWalletId = toWalletId,
            date = date,
            note = note,
            exchangeRate = finalExchangeRate,
            convertedAmount = finalConvertedAmount,
            createdAt = now,
            updatedAt = now
        )

        return transactionRepository.create(transaction)
    }
}
