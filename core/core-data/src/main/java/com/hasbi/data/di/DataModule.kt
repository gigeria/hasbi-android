package com.hasbi.data.di

import com.hasbi.data.repository.BudgetRepositoryImpl
import com.hasbi.data.repository.CategoryRepositoryImpl
import com.hasbi.data.repository.CurrencyRepositoryImpl
import com.hasbi.data.repository.DebtRepositoryImpl
import com.hasbi.data.repository.SettingsRepositoryImpl
import com.hasbi.data.repository.TransactionRepositoryImpl
import com.hasbi.data.repository.WalletRepositoryImpl
import com.hasbi.domain.repository.BudgetRepository
import com.hasbi.domain.repository.CategoryRepository
import com.hasbi.domain.repository.CurrencyRepository
import com.hasbi.domain.repository.DebtRepository
import com.hasbi.domain.repository.SettingsRepository
import com.hasbi.domain.repository.TransactionRepository
import com.hasbi.domain.repository.WalletRepository
import com.hasbi.datastore.SettingsDataStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt DI module for data layer.
 *
 * Binds repository implementations to their interfaces.
 * Provides SettingsDataStore (DataStore-backed).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindWalletRepository(impl: WalletRepositoryImpl): WalletRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindBudgetRepository(impl: BudgetRepositoryImpl): BudgetRepository

    @Binds
    @Singleton
    abstract fun bindDebtRepository(impl: DebtRepositoryImpl): DebtRepository

    @Binds
    @Singleton
    abstract fun bindCurrencyRepository(impl: CurrencyRepositoryImpl): CurrencyRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}
