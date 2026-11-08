package com.hasbi.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hasbi.database.entity.CurrencyEntity
import kotlinx.coroutines.flow.Flow

/**
 * Currency DAO.
 *
 * Used for:
 * - Multi-currency support (Phase 4)
 * - MoneyFormatter — needs minorUnitExponent for precision
 */
@Dao
interface CurrencyDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)  // Don't overwrite existing
    suspend fun insertAll(currencies: List<CurrencyEntity>)

    @Query("SELECT * FROM currencies ORDER BY isDefault DESC, code ASC")
    fun observeAll(): Flow<List<CurrencyEntity>>

    @Query("SELECT * FROM currencies ORDER BY isDefault DESC, code ASC")
    suspend fun getAll(): List<CurrencyEntity>

    @Query("SELECT * FROM currencies WHERE code = :code")
    suspend fun getByCode(code: String): CurrencyEntity?

    @Query("SELECT * FROM currencies WHERE isDefault = 1 LIMIT 1")
    suspend fun getDefault(): CurrencyEntity?

    @Query("SELECT COUNT(*) FROM currencies")
    suspend fun count(): Int
}
