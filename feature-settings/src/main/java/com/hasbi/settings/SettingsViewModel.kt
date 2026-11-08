package com.hasbi.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasbi.domain.repository.AppSettings
import com.hasbi.domain.repository.AppLanguage
import com.hasbi.domain.repository.BackupFrequency
import com.hasbi.domain.repository.NumberFormatStyle
import com.hasbi.domain.repository.SettingsRepository
import com.hasbi.domain.repository.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(mode) }
    }

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch { settingsRepository.setLanguage(language) }
    }

    fun setNumberFormat(format: NumberFormatStyle) {
        viewModelScope.launch { settingsRepository.setNumberFormat(format) }
    }

    fun setBiometricLock(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setBiometricLockEnabled(enabled) }
    }

    fun setSmartEntry(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setSmartEntryEnabled(enabled) }
    }

    fun setWeeklyNotification(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setWeeklyNotificationEnabled(enabled) }
    }

    fun setStreaks(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setStreaksEnabled(enabled) }
    }

    fun setAutoLocalBackup(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setAutoLocalBackup(enabled) }
    }

    fun setBackupFrequency(freq: BackupFrequency) {
        viewModelScope.launch { settingsRepository.setLocalBackupFrequency(freq) }
    }

    fun setHideBalanceOnLaunch(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setHideBalanceOnLaunch(enabled) }
    }
}
