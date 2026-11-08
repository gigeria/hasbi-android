package com.hasbi.data.repository

import com.hasbi.data.mapper.toDomain
import com.hasbi.data.mapper.toEntity
import com.hasbi.database.dao.WalletDao
import com.hasbi.domain.model.Wallet
import com.hasbi.domain.model.WalletWithBalance
import com.hasbi.domain.repository.WalletRepository
import com.hasbi.util.result.HasbiError
import com.hasbi.util.result.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepositoryImpl @Inject constructor(
    private val walletDao: WalletDao
) : WalletRepository {

    override fun observeAll(): Flow<List<Wallet>> =
        walletDao.observeAll().map { list -> list.map { it.toDomain() } }

    override fun observeVisible(): Flow<List<Wallet>> =
        walletDao.observeVisible().map { list -> list.map { it.toDomain() } }

    override fun observeAllWithBalance(): Flow<List<WalletWithBalance>> =
        walletDao.observeAllWithBalance().map { list -> list.map { it.toDomain() } }

    override fun observeById(id: Long): Flow<Wallet?> =
        walletDao.observeById(id).map { it?.toDomain() }

    override fun observeBalance(id: Long): Flow<Long> =
        walletDao.observeBalance(id)

    override suspend fun getById(id: Long): Wallet? =
        walletDao.getById(id)?.toDomain()

    override suspend fun create(wallet: Wallet): Result<Long> = try {
        val id = walletDao.insert(wallet.toEntity())
        Result.Success(id)
    } catch (e: Exception) {
        Result.Failure(HasbiError.DatabaseError(e.message ?: "Create wallet failed"))
    }

    override suspend fun update(wallet: Wallet): Result<Unit> = try {
        walletDao.update(wallet.copy(updatedAt = System.currentTimeMillis()).toEntity())
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Failure(HasbiError.DatabaseError(e.message ?: "Update wallet failed"))
    }

    override suspend fun archive(id: Long): Result<Unit> = try {
        walletDao.archive(id, System.currentTimeMillis())
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Failure(HasbiError.DatabaseError(e.message ?: "Archive wallet failed"))
    }

    override suspend fun setHidden(id: Long, isHidden: Boolean): Result<Unit> = try {
        walletDao.setHidden(id, isHidden, System.currentTimeMillis())
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Failure(HasbiError.DatabaseError(e.message ?: "Hide wallet failed"))
    }

    override suspend fun reorder(orderedIds: List<Long>): Result<Unit> = try {
        val now = System.currentTimeMillis()
        orderedIds.forEachIndexed { index, id ->
            walletDao.updateSortOrder(id, index, now)
        }
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Failure(HasbiError.DatabaseError(e.message ?: "Reorder wallets failed"))
    }

    override suspend fun getTransactionCount(id: Long): Int =
        walletDao.getTransactionCount(id)
}
