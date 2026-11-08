package com.hasbi.data.repository

import com.hasbi.database.dao.CurrencyDao
import com.hasbi.database.entity.CurrencyEntity
import com.hasbi.database.seed.CurrencySeeder
import com.hasbi.domain.repository.CurrencyRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CurrencyRepositoryImpl @Inject constructor(
    private val currencyDao: CurrencyDao
) : CurrencyRepository {

    override fun observeAll(): Flow<List<CurrencyEntity>> = currencyDao.observeAll()

    override suspend fun getAll(): List<CurrencyEntity> = currencyDao.getAll()

    override suspend fun getByCode(code: String): CurrencyEntity? = currencyDao.getByCode(code)

    override suspend fun getDefault(): CurrencyEntity? = currencyDao.getDefault()

    override suspend fun seedDefaultsIfEmpty() {
        if (currencyDao.count() == 0) {
            val now = System.currentTimeMillis()
            currencyDao.insertAll(CurrencySeeder.getSeedCurrencies(now))
        }
    }
}
