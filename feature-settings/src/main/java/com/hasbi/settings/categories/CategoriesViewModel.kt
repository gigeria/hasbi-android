package com.hasbi.settings.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasbi.database.entity.CategoryGroup
import com.hasbi.domain.model.Category
import com.hasbi.domain.repository.CategoryRepository
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
class CategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _selectedGroup = MutableStateFlow(CategoryGroup.EXPENSE)
    val selectedGroup: StateFlow<CategoryGroup> = _selectedGroup.asStateFlow()

    val expenseCategories: StateFlow<List<Category>> = categoryRepository
        .observeByGroup(CategoryGroup.EXPENSE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incomeCategories: StateFlow<List<Category>> = categoryRepository
        .observeByGroup(CategoryGroup.INCOME)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun selectGroup(group: CategoryGroup) {
        _selectedGroup.value = group
    }

    fun showAddDialog() {
        _showAddDialog.value = true
    }

    fun hideAddDialog() {
        _showAddDialog.value = false
    }

    fun addCategory(name: String, icon: String, colorHex: String) {
        viewModelScope.launch {
            val group = _selectedGroup.value
            val now = System.currentTimeMillis()
            val category = Category(
                name = name,
                icon = icon,
                colorHex = colorHex,
                group = group,
                sortOrder = 99,
                keywords = name.lowercase(),
                createdAt = now,
                updatedAt = now
            )
            when (val result = categoryRepository.create(category)) {
                is Result.Success -> {
                    _showAddDialog.value = false
                }
                is Result.Failure -> {
                    _error.value = result.error.userMessage
                }
            }
        }
    }

    fun archiveCategory(id: Long) {
        viewModelScope.launch { categoryRepository.archive(id) }
    }

    fun clearError() {
        _error.value = null
    }
}
