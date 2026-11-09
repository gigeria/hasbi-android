package com.hasbi.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.hasbi.database.entity.CycleType
import com.hasbi.domain.repository.AppSettings
import com.hasbi.domain.repository.AppLanguage
import com.hasbi.domain.repository.BackupFrequency
import com.hasbi.domain.repository.NumberFormatStyle
import com.hasbi.domain.repository.OnboardingState
import com.hasbi.domain.repository.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "hasbi_settings")

/**
 * DataStore-backed settings repository.
 *
 * Persists:
 * - Onboarding state (resume support)
 * - App settings (currency, language, theme, cycle, security, features, backup)
 *
 * DataStore is preferred over SharedPreferences for Jetpack Compose apps:
 * - Async API (suspending)
 * - Type-safe with PreferencesKey
 * - Transactional
 * - No ANRs
 */
@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object Keys {
        // Onboarding
        val ONBOARDED = booleanPreferencesKey("onboarded")
        val ONBOARDING_VERSION = intPreferencesKey("onboarding_version")
        val ONBOARDING_STEP = intPreferencesKey("onboarding_step")
        val ONBOARDING_WALLETS = stringSetPreferencesKey("onboarding_wallets")
        val ONBOARDING_CYCLE_TYPE = stringPreferencesKey("onboarding_cycle_type")
        val ONBOARDING_CYCLE_START = intPreferencesKey("onboarding_cycle_start")
        val ONBOARDING_BUDGET = longPreferencesKey("onboarding_budget")

        // Currency
        val BASE_CURRENCY = stringPreferencesKey("base_currency")

        // Language
        val LANGUAGE = stringPreferencesKey("language")

        // Cycle
        val CYCLE_TYPE = stringPreferencesKey("cycle_type")
        val CYCLE_START_DATE = intPreferencesKey("cycle_start_date")

        // Display
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT_COLOR = stringPreferencesKey("accent_color")
        val USE_DYNAMIC_COLOR = booleanPreferencesKey("use_dynamic_color")
        val HIDE_BALANCE_ON_LAUNCH = booleanPreferencesKey("hide_balance_on_launch")
        val NUMBER_FORMAT = stringPreferencesKey("number_format")

        // Security
        val BIOMETRIC_LOCK = booleanPreferencesKey("biometric_lock")
        val AUTO_LOCK_MINUTES = intPreferencesKey("auto_lock_minutes")

        // Features
        val SMART_ENTRY = booleanPreferencesKey("smart_entry")
        val WEEKLY_NOTIFICATION = booleanPreferencesKey("weekly_notification")
        val STREAKS = booleanPreferencesKey("streaks")

        // Backup
        val AUTO_LOCAL_BACKUP = booleanPreferencesKey("auto_local_backup")
        val LOCAL_BACKUP_FREQ = stringPreferencesKey("local_backup_freq")
        val LAST_BACKUP_DATE = longPreferencesKey("last_backup_date")
    }

    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { prefs ->
        AppSettings(
            isOnboarded = prefs[Keys.ONBOARDED] ?: false,
            onboardingVersion = prefs[Keys.ONBOARDING_VERSION] ?: 0,
            baseCurrency = prefs[Keys.BASE_CURRENCY] ?: "PKR",
            language = AppLanguage.valueOf(prefs[Keys.LANGUAGE] ?: AppLanguage.ENGLISH.name),
            cycleType = CycleType.valueOf(prefs[Keys.CYCLE_TYPE] ?: CycleType.MONTHLY.name),
            cycleStartDate = prefs[Keys.CYCLE_START_DATE] ?: 1,
            themeMode = ThemeMode.valueOf(prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name),
            accentColor = prefs[Keys.ACCENT_COLOR] ?: "#1B5E43",
            useDynamicColor = prefs[Keys.USE_DYNAMIC_COLOR] ?: false,
            hideBalanceOnLaunch = prefs[Keys.HIDE_BALANCE_ON_LAUNCH] ?: false,
            numberFormat = NumberFormatStyle.valueOf(prefs[Keys.NUMBER_FORMAT] ?: NumberFormatStyle.PAKISTANI.name),
            biometricLockEnabled = prefs[Keys.BIOMETRIC_LOCK] ?: false,
            autoLockMinutes = prefs[Keys.AUTO_LOCK_MINUTES] ?: 5,
            smartEntryEnabled = prefs[Keys.SMART_ENTRY] ?: true,
            weeklyNotificationEnabled = prefs[Keys.WEEKLY_NOTIFICATION] ?: true,
            streaksEnabled = prefs[Keys.STREAKS] ?: false,
            autoLocalBackup = prefs[Keys.AUTO_LOCAL_BACKUP] ?: true,
            localBackupFrequency = BackupFrequency.valueOf(prefs[Keys.LOCAL_BACKUP_FREQ] ?: BackupFrequency.WEEKLY.name),
            lastBackupDate = prefs[Keys.LAST_BACKUP_DATE]
        )
    }

    val onboardingState: Flow<OnboardingState> = context.settingsDataStore.data.map { prefs ->
        OnboardingState(
            currentStep = prefs[Keys.ONBOARDING_STEP] ?: 0,
            selectedWalletTypes = prefs[Keys.ONBOARDING_WALLETS] ?: emptySet(),
            cycleType = CycleType.valueOf(prefs[Keys.ONBOARDING_CYCLE_TYPE] ?: CycleType.MONTHLY.name),
            cycleStartDate = prefs[Keys.ONBOARDING_CYCLE_START] ?: 1,
            monthlyBudget = prefs[Keys.ONBOARDING_BUDGET] ?: 0L
        )
    }

    // === Setters ===

    suspend fun setOnboarded(value: Boolean, version: Int = 1) {
        context.settingsDataStore.edit { it[Keys.ONBOARDED] = value; it[Keys.ONBOARDING_VERSION] = version }
    }

    suspend fun setBaseCurrency(code: String) {
        context.settingsDataStore.edit { it[Keys.BASE_CURRENCY] = code }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.settingsDataStore.edit { it[Keys.LANGUAGE] = language.name }
    }

    suspend fun setCycleType(type: CycleType) {
        context.settingsDataStore.edit { it[Keys.CYCLE_TYPE] = type.name }
    }

    suspend fun setCycleStartDate(day: Int) {
        context.settingsDataStore.edit { it[Keys.CYCLE_START_DATE] = day.coerceIn(1, 31) }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsDataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setAccentColor(hex: String) {
        context.settingsDataStore.edit { it[Keys.ACCENT_COLOR] = hex }
    }

    suspend fun setUseDynamicColor(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.USE_DYNAMIC_COLOR] = value }
    }

    suspend fun setHideBalanceOnLaunch(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.HIDE_BALANCE_ON_LAUNCH] = value }
    }

    suspend fun setNumberFormat(format: NumberFormatStyle) {
        context.settingsDataStore.edit { it[Keys.NUMBER_FORMAT] = format.name }
    }

    suspend fun setBiometricLockEnabled(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.BIOMETRIC_LOCK] = value }
    }

    suspend fun setAutoLockMinutes(minutes: Int) {
        context.settingsDataStore.edit { it[Keys.AUTO_LOCK_MINUTES] = minutes }
    }

    suspend fun setSmartEntryEnabled(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.SMART_ENTRY] = value }
    }

    suspend fun setWeeklyNotificationEnabled(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.WEEKLY_NOTIFICATION] = value }
    }

    suspend fun setStreaksEnabled(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.STREAKS] = value }
    }

    suspend fun setAutoLocalBackup(value: Boolean) {
        context.settingsDataStore.edit { it[Keys.AUTO_LOCAL_BACKUP] = value }
    }

    suspend fun setLocalBackupFrequency(freq: BackupFrequency) {
        context.settingsDataStore.edit { it[Keys.LOCAL_BACKUP_FREQ] = freq.name }
    }

    suspend fun setLastBackupDate(date: Long?) {
        context.settingsDataStore.edit {
            if (date == null) it.remove(Keys.LAST_BACKUP_DATE) else it[Keys.LAST_BACKUP_DATE] = date
        }
    }

    // === Onboarding resume support ===

    suspend fun setOnboardingStep(step: Int) {
        context.settingsDataStore.edit { it[Keys.ONBOARDING_STEP] = step }
    }

    suspend fun setOnboardingWallets(types: Set<String>) {
        context.settingsDataStore.edit { it[Keys.ONBOARDING_WALLETS] = types }
    }

    suspend fun setOnboardingCycleType(type: CycleType) {
        context.settingsDataStore.edit { it[Keys.ONBOARDING_CYCLE_TYPE] = type.name }
    }

    suspend fun setOnboardingCycleStart(day: Int) {
        context.settingsDataStore.edit { it[Keys.ONBOARDING_CYCLE_START] = day.coerceIn(1, 31) }
    }

    suspend fun setOnboardingBudget(amount: Long) {
        context.settingsDataStore.edit { it[Keys.ONBOARDING_BUDGET] = amount }
    }

    suspend fun clearOnboardingState() {
        context.settingsDataStore.edit {
            it.remove(Keys.ONBOARDING_STEP)
            it.remove(Keys.ONBOARDING_WALLETS)
            it.remove(Keys.ONBOARDING_CYCLE_TYPE)
            it.remove(Keys.ONBOARDING_CYCLE_START)
            it.remove(Keys.ONBOARDING_BUDGET)
        }
    }
}
