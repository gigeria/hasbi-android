package com.hasbi.settings.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasbi.database.entity.ProjectEntity
import com.hasbi.domain.repository.ProjectRepository
import com.hasbi.util.result.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProjectsViewModel @Inject constructor(
    private val projectRepository: ProjectRepository
) : ViewModel() {

    val projects: StateFlow<List<ProjectEntity>> = projectRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        seedDefaults()
    }

    fun seedDefaults() {
        viewModelScope.launch { projectRepository.seedDefaultsIfEmpty() }
    }

    fun showAddDialog() { _showAddDialog.value = true }
    fun hideAddDialog() { _showAddDialog.value = false }

    fun addProject(name: String, icon: String, colorHex: String) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val project = ProjectEntity(
                name = name,
                icon = icon,
                colorHex = colorHex,
                sortOrder = 99,
                createdAt = now,
                updatedAt = now
            )
            when (val result = projectRepository.create(project)) {
                is Result.Success -> _showAddDialog.value = false
                is Result.Failure -> _error.value = result.error.userMessage
            }
        }
    }

    fun archiveProject(id: Long) {
        viewModelScope.launch { projectRepository.archive(id) }
    }

    fun clearError() { _error.value = null }
}
