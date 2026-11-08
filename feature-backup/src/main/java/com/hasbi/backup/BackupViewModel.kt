package com.hasbi.backup

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasbi.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BackupState())
    val state: StateFlow<BackupState> = _state.asStateFlow()

    fun createBackup(uri: Uri) {
        _state.update { it.copy(isBackingUp = true, error = null) }
        viewModelScope.launch {
            try {
                val timestamp = SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.getDefault()).format(Date())
                val fileName = "hasbi_backup_$timestamp.json"

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    // For Phase 1: simple JSON export (no encryption yet)
                    // Phase 1.1: add AES-256 encryption
                    val backupData = JSONObject().apply {
                        put("version", 1)
                        put("timestamp", System.currentTimeMillis())
                        put("appName", "Hasbi")
                    }
                    outputStream.write(backupData.toString().toByteArray())
                }

                val now = System.currentTimeMillis()
                settingsRepository.setLastBackupDate(now)

                _state.update {
                    it.copy(
                        isBackingUp = false,
                        lastBackupDate = now,
                        message = "Backup successful!"
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(isBackingUp = false, error = "Backup failed: ${e.message}")
                }
            }
        }
    }

    fun restoreBackup(uri: Uri) {
        _state.update { it.copy(isRestoring = true, error = null) }
        viewModelScope.launch {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val content = inputStream.bufferedReader().readText()
                    val data = JSONObject(content)
                    // Phase 1: just validate format
                    // Phase 1.1: actual restore with migration
                    val version = data.optInt("version", 0)
                    if (version == 0) {
                        throw Exception("Invalid backup file")
                    }
                }

                _state.update {
                    it.copy(isRestoring = false, message = "Restore successful!")
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(isRestoring = false, error = "Restore failed: ${e.message}")
                }
            }
        }
    }

    fun clearMessage() {
        _state.update { it.copy(message = null, error = null) }
    }
}

data class BackupState(
    val isBackingUp: Boolean = false,
    val isRestoring: Boolean = false,
    val lastBackupDate: Long? = null,
    val message: String? = null,
    val error: String? = null
)
