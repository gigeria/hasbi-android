package com.hasbi.domain.repository

import com.hasbi.database.entity.CycleType
import com.hasbi.database.entity.CurrencyEntity
import kotlinx.coroutines.flow.Flow

/**
 * App settings — stored in DataStore (not Room).
 *
 * Phase 1: PKR default, English/Roman Urdu, Monthly cycle.
 */
interface SettingsRepository {

    val settings: Flow<AppSettings>

    // Onboarding
    suspend fun setOnboarded(value: Boolean, version: Int = 1)
    suspend fun getOnboardingState(): OnboardingState

    // Currency
    suspend fun setBaseCurrency(code: String)
    suspend fun getDefaultCurrency(): CurrencyEntity?

    // Language
    suspend fun setLanguage(language: AppLanguage)

    // Budget cycle
    suspend fun setCycleType(type: CycleType)
    suspend fun setCycleStartDate(day: Int)

    // Display
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setAccentColor(hex: String)
    suspend fun setUseDynamicColor(value: Boolean)
    suspend fun setHideBalanceOnLaunch(value: Boolean)
    suspend fun setNumberFormat(format: NumberFormatStyle)

    // Security
    suspend fun setBiometricLockEnabled(value: Boolean)
    suspend fun setAutoLockMinutes(minutes: Int)

    // Features
    suspend fun setSmartEntryEnabled(value: Boolean)
    suspend fun setWeeklyNotificationEnabled(value: Boolean)
    suspend fun setStreaksEnabled(value: Boolean)

    // Backup
    suspend fun setAutoLocalBackup(value: Boolean)
    suspend fun setLocalBackupFrequency(freq: BackupFrequency)
    suspend fun setLastBackupDate(date: Long?)
}

/** App settings data class — persisted in DataStore. */
data class AppSettings(
    val isOnboarded: Boolean = false,
    val onboardingVersion: Int = 0,
    val baseCurrency: String = "PKR",
    val language: AppLanguage = AppLanguage.ENGLISH,
    val cycleType: CycleType = CycleType.MONTHLY,
    val cycleStartDate: Int = 1,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentColor: String = "#1B5E43",
    val useDynamicColor: Boolean = false,
    val hideBalanceOnLaunch: Boolean = false,
    val numberFormat: NumberFormatStyle = NumberFormatStyle.PAKISTANI,
    val biometricLockEnabled: Boolean = false,
    val autoLockMinutes: Int = 5,
    val smartEntryEnabled: Boolean = true,
    val weeklyNotificationEnabled: Boolean = true,
    val streaksEnabled: Boolean = false,
    val autoLocalBackup: Boolean = true,
    val localBackupFrequency: BackupFrequency = BackupFrequency.WEEKLY,
    val lastBackupDate: Long? = null
)

/** Onboarding resume support — persisted separately. */
data class OnboardingState(
    val currentStep: Int = 0,  // 0=welcome, 1=wallet, 2=budget
    val selectedWalletTypes: Set<String> = emptySet(),
    val walletInitialBalances: Map<String, Long> = emptyMap(),
    val cycleType: CycleType = CycleType.MONTHLY,
    val cycleStartDate: Int = 1,
    val monthlyBudget: Long = 0
)

enum class AppLanguage { ENGLISH, ROMAN_URDU, URDU }
enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class NumberFormatStyle { WESTERN, PAKISTANI, INDIAN }
enum class BackupFrequency { NEVER, DAILY, WEEKLY, MONTHLY }
