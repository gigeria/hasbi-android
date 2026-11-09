package com.hasbi.budgets.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hasbi.database.entity.BudgetEntity
import com.hasbi.domain.repository.BudgetRepository
import com.hasbi.util.result.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddBudgetViewModel @Inject constructor(
    private val budgetRepository: BudgetRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AddBudgetState())
    val state: StateFlow<AddBudgetState> = _state.asStateFlow()

    fun setBudgetType(isOverall: Boolean) {
        _state.update { it.copy(isOverall = isOverall, categoryId = if (isOverall) null else it.categoryId) }
    }

    fun setCategoryId(id: Long) {
        _state.update { it.copy(isOverall = false, categoryId = id) }
    }

    fun setAmount(text: String) {
        val cleaned = text.filter { it.isDigit() }
        val amount = cleaned.toLongOrNull() ?: 0L
        _state.update { it.copy(amountText = cleaned, amountInPaisa = amount * 100) }
    }

    fun setThreshold(value: Float) {
        _state.update { it.copy(warningThreshold = value) }
    }

    fun save(onComplete: (Boolean) -> Unit) {
        val current = _state.value
        if (current.amountInPaisa <= 0) {
            _state.update { it.copy(error = "Amount 0 se zyada honi chahiye") }
            return
        }

        viewModelScope.launch {
            val cycle = budgetRepository.getCurrentCycle()
            if (cycle == null) {
                _state.update { it.copy(error = "Budget cycle nahi mila") }
                return@launch
            }

            val now = System.currentTimeMillis()
            val budget = BudgetEntity(
                cycleId = cycle.id,
                categoryId = if (current.isOverall) null else current.categoryId,
                limit = current.amountInPaisa,
                warningThreshold = current.warningThreshold,
                createdAt = now,
                updatedAt = now
            )

            when (val result = budgetRepository.createBudget(budget)) {
                is Result.Success -> {
                    _state.update { it.copy(isSaving = false) }
                    onComplete(true)
                }
                is Result.Failure -> {
                    _state.update { it.copy(isSaving = false, error = result.error.userMessage) }
                    onComplete(false)
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

data class AddBudgetState(
    val isOverall: Boolean = true,
    val categoryId: Long? = null,
    val amountText: String = "",
    val amountInPaisa: Long = 0L,
    val warningThreshold: Float = 0.8f,
    val isSaving: Boolean = false,
    val error: String? = null
)
