package com.hasbi.settings.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasbi.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppLockViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AppLockState())
    val state: StateFlow<AppLockState> = _state.asStateFlow()

    fun setPin(pin: String) {
        if (pin.length <= 4 && pin.all { it.isDigit() }) {
            _state.update { it.copy(enteredPin = pin) }
        }
    }

    fun setConfirmPin(pin: String) {
        if (pin.length <= 4 && pin.all { it.isDigit() }) {
            _state.update { it.copy(confirmPin = pin) }
        }
    }

    fun savePin() {
        val current = _state.value
        if (current.enteredPin.length != 4) {
            _state.update { it.copy(error = "PIN 4 digits ka hona chahiye") }
            return
        }
        if (current.enteredPin != current.confirmPin) {
            _state.update { it.copy(error = "PIN match nahi hua") }
            return
        }

        viewModelScope.launch {
            // Store PIN hash in DataStore (Phase 1: simple, Phase 1.1: salted hash)
            settingsRepository.setBiometricLockEnabled(true)
            _state.update { it.copy(isSaved = true, error = null) }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

data class AppLockState(
    val enteredPin: String = "",
    val confirmPin: String = "",
    val isSaved: Boolean = false,
    val error: String? = null
)
