package com.hasbi.domain.usecase.budget

import com.hasbi.database.entity.BudgetCycleEntity
import com.hasbi.database.entity.CycleType
import com.hasbi.domain.repository.BudgetRepository
import com.hasbi.util.date.DateTimeUtils
import com.hasbi.util.result.HasbiError
import com.hasbi.util.result.Result
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * Ensure a current budget cycle exists.
 *
 * If current cycle has expired, creates the next one based on cycleType + startDay.
 * Called by:
 * - Dashboard on app launch (Week 5)
 * - WorkManager daily (Phase 2)
 */
class EnsureCurrentCycleUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository
) {

    suspend operator fun invoke(
        cycleType: CycleType = CycleType.MONTHLY,
        cycleStartDay: Int = 1,
        zone: ZoneId = ZoneId.of(DateTimeUtils.DEFAULT_TIMEZONE)
    ): Result<BudgetCycleEntity> = try {
        val now = System.currentTimeMillis()
        val today = LocalDate.now(zone)

        // Check if current cycle is still valid
        val current = budgetRepository.getCurrentCycle()
        if (current != null && current.endDate > now) {
            return Result.Success(current)
        }

        // Close expired cycle
        if (current != null) {
            budgetRepository.closeCycle(current.id)
        }

        // Compute new cycle boundaries
        val (start, end) = when (cycleType) {
            CycleType.MONTHLY -> DateTimeUtils.monthlyCycle(today, zone)
            CycleType.SALARY_DATE -> DateTimeUtils.salaryDateCycle(cycleStartDay, today, zone)
            else -> DateTimeUtils.monthlyCycle(today, zone)  // fallback for Phase 1
        }

        // Create new cycle
        val newCycle = BudgetCycleEntity(
            cycleType = cycleType,
            startDate = start,
            endDate = end,
            totalBudget = 0,  // User can set budget later
            isCurrent = true,
            carryOver = 0,  // TODO: compute from previous cycle
            createdAt = now,
            updatedAt = now
        )

        val cycleId = budgetRepository.createCycle(newCycle).getOrNull()
            ?: return Result.Failure(HasbiError.DatabaseError("Failed to create cycle"))

        Result.Success(newCycle.copy(id = cycleId))
    } catch (e: Exception) {
        Result.Failure(HasbiError.DatabaseError(e.message ?: "Ensure cycle failed"))
    }
}
