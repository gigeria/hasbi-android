package com.hasbi.data.repository

import com.hasbi.database.dao.CurrencyDao
import com.hasbi.database.entity.CycleType
import com.hasbi.database.entity.CurrencyEntity
import com.hasbi.datastore.SettingsDataStore
import com.hasbi.domain.repository.AppSettings
import com.hasbi.domain.repository.OnboardingState
import com.hasbi.domain.repository.SettingsRepository
import com.hasbi.domain.repository.AppLanguage
import com.hasbi.domain.repository.BackupFrequency
import com.hasbi.domain.repository.NumberFormatStyle
import com.hasbi.domain.repository.ThemeMode
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: SettingsDataStore,
    private val currencyDao: CurrencyDao
) : SettingsRepository {

    override val settings: Flow<AppSettings> = dataStore.settings

    override suspend fun setOnboarded(value: Boolean, version: Int) {
        dataStore.setOnboarded(value, version)
        if (value) dataStore.clearOnboardingState()
    }

    override suspend fun getOnboardingState(): OnboardingState {
        var state: OnboardingState? = null
        dataStore.onboardingState.collect { state = it; return@collect }
        return state ?: OnboardingState()
    }

    override suspend fun setBaseCurrency(code: String) = dataStore.setBaseCurrency(code)

    override suspend fun getDefaultCurrency(): CurrencyEntity? = currencyDao.getDefault()

    override suspend fun setLanguage(language: AppLanguage) = dataStore.setLanguage(language)
    override suspend fun setCycleType(type: CycleType) = dataStore.setCycleType(type)
    override suspend fun setCycleStartDate(day: Int) = dataStore.setCycleStartDate(day)
    override suspend fun setThemeMode(mode: ThemeMode) = dataStore.setThemeMode(mode)
    override suspend fun setAccentColor(hex: String) = dataStore.setAccentColor(hex)
    override suspend fun setUseDynamicColor(value: Boolean) = dataStore.setUseDynamicColor(value)
    override suspend fun setHideBalanceOnLaunch(value: Boolean) = dataStore.setHideBalanceOnLaunch(value)
    override suspend fun setNumberFormat(format: NumberFormatStyle) = dataStore.setNumberFormat(format)
    override suspend fun setBiometricLockEnabled(value: Boolean) = dataStore.setBiometricLockEnabled(value)
    override suspend fun setAutoLockMinutes(minutes: Int) = dataStore.setAutoLockMinutes(minutes)
    override suspend fun setSmartEntryEnabled(value: Boolean) = dataStore.setSmartEntryEnabled(value)
    override suspend fun setWeeklyNotificationEnabled(value: Boolean) = dataStore.setWeeklyNotificationEnabled(value)
    override suspend fun setStreaksEnabled(value: Boolean) = dataStore.setStreaksEnabled(value)
    override suspend fun setAutoLocalBackup(value: Boolean) = dataStore.setAutoLocalBackup(value)
    override suspend fun setLocalBackupFrequency(freq: BackupFrequency) = dataStore.setLocalBackupFrequency(freq)
    override suspend fun setLastBackupDate(date: Long?) = dataStore.setLastBackupDate(date)
}
