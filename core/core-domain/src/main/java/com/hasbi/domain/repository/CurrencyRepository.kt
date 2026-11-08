package com.hasbi.domain.repository

import com.hasbi.database.entity.CurrencyEntity
import kotlinx.coroutines.flow.Flow

interface CurrencyRepository {

    fun observeAll(): Flow<List<CurrencyEntity>>

    suspend fun getAll(): List<CurrencyEntity>
    suspend fun getByCode(code: String): CurrencyEntity?
    suspend fun getDefault(): CurrencyEntity?

    suspend fun seedDefaultsIfEmpty()
}
