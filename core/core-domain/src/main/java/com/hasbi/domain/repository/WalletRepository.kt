package com.hasbi.domain.repository

import com.hasbi.domain.model.Wallet
import com.hasbi.domain.model.WalletWithBalance
import com.hasbi.util.result.Result
import kotlinx.coroutines.flow.Flow

/**
 * Wallet repository contract.
 *
 * Implemented by WalletRepositoryImpl in core-data.
 * Used by use cases in core-domain.
 */
interface WalletRepository {

    fun observeAll(): Flow<List<Wallet>>
    fun observeVisible(): Flow<List<Wallet>>
    fun observeAllWithBalance(): Flow<List<WalletWithBalance>>
    fun observeById(id: Long): Flow<Wallet?>
    fun observeBalance(id: Long): Flow<Long>

    suspend fun getById(id: Long): Wallet?

    suspend fun create(wallet: Wallet): Result<Long>
    suspend fun update(wallet: Wallet): Result<Unit>
    suspend fun archive(id: Long): Result<Unit>
    suspend fun setHidden(id: Long, isHidden: Boolean): Result<Unit>
    suspend fun reorder(orderedIds: List<Long>): Result<Unit>

    suspend fun getTransactionCount(id: Long): Int
}
