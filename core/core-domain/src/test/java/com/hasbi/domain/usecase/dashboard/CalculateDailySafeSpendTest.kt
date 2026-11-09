package com.hasbi.domain.usecase.dashboard

import com.hasbi.database.entity.BudgetCycleEntity
import com.hasbi.database.entity.CycleType
import com.hasbi.domain.model.WarningLevel
import org.junit.Assert.*
import org.junit.Test
import java.time.ZoneId

class CalculateDailySafeSpendTest {

    private val useCase = CalculateDailySafeSpendUseCase()
    private val zone = ZoneId.of("Asia/Karachi")

    private fun createCycle(
        budget: Long,
        start: Long,
        end: Long
    ) = BudgetCycleEntity(
        cycleType = CycleType.MONTHLY,
        startDate = start,
        endDate = end,
        totalBudget = budget,
        isCurrent = true,
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )

    @Test
    fun `null cycle returns null`() {
        val result = useCase(null, 0L, zone = zone)
        assertNull(result)
    }

    @Test
    fun `zero budget returns null`() {
        val cycle = createCycle(0, 0, 1000)
        val result = useCase(cycle, 0L, zone = zone)
        assertNull(result)
    }

    @Test
    fun `fresh cycle day 1 with no spending`() {
        // 30-day cycle, budget Rs 30,000 (3000000 paisa)
        val start = 1730764800000L  // Nov 1 2024
        val end = 1733356800000L    // Dec 1 2024
        val cycle = createCycle(3_000_000, start, end)
        val now = start + 1000  // Just after start

        val result = useCase(cycle, 0L, now, zone)
        assertNotNull(result)
        assertEquals(3_000_000, result!!.remainingBudget)
        assertEquals(30, result.remainingDays)  // 30 days remaining
        assertEquals(100_000, result.dailySafeSpend)  // Rs 1,000/day
        assertEquals(WarningLevel.GREEN, result.warningLevel)
    }

    @Test
    fun `mid cycle with spending on track`() {
        val start = 1730764800000L
        val end = 1733356800000L
        val cycle = createCycle(3_000_000, start, end)
        val now = start + (10L * 24 * 60 * 60 * 1000)  // Day 10
        val spent = 1_000_000  // Rs 10,000 spent

        val result = useCase(cycle, spent, now, zone)
        assertNotNull(result)
        assertEquals(2_000_000, result!!.remainingBudget)  // Rs 20,000 left
        assertEquals(20, result.remainingDays)  // 20 days left
        assertEquals(100_000, result.dailySafeSpend)  // Rs 1,000/day
        assertEquals(WarningLevel.GREEN, result.warningLevel)
    }

    @Test
    fun `over budget returns zero daily safe spend`() {
        val start = 1730764800000L
        val end = 1733356800000L
        val cycle = createCycle(3_000_000, start, end)
        val now = start + (15L * 24 * 60 * 60 * 1000)
        val spent = 4_000_000  // Rs 40,000 — over budget of Rs 30,000

        val result = useCase(cycle, spent, now, zone)
        assertNotNull(result)
        assertEquals(-1_000_000, result!!.remainingBudget)  // Negative
        assertEquals(0L, result.dailySafeSpend)  // No more spending
        assertTrue(result.isOverBudget)
        assertEquals(WarningLevel.RED, result.warningLevel)
    }

    @Test
    fun `amber warning when under 10 percent remaining`() {
        val start = 1730764800000L
        val end = 1733356800000L
        val cycle = createCycle(3_000_000, start, end)
        val now = start + (20L * 24 * 60 * 60 * 1000)
        val spent = 2_800_000  // Rs 28,000 — 93% used, only 7% left

        val result = useCase(cycle, spent, now, zone)
        assertNotNull(result)
        assertEquals(200_000, result!!.remainingBudget)  // Rs 2,000 left
        assertEquals(WarningLevel.AMBER, result.warningLevel)
    }

    @Test
    fun `last day of cycle all remaining available`() {
        val start = 1730764800000L
        val end = 1733356800000L
        val cycle = createCycle(3_000_000, start, end)
        val now = end - 1000  // Just before end
        val spent = 2_000_000

        val result = useCase(cycle, spent, now, zone)
        assertNotNull(result)
        assertEquals(1_000_000, result!!.remainingBudget)
        assertEquals(1, result.remainingDays)  // At least 1 day
        assertEquals(1_000_000, result.dailySafeSpend)  // All remaining today
    }

    @Test
    fun `projection uses elapsed days correctly`() {
        val start = 1730764800000L
        val end = 1733356800000L
        val cycle = createCycle(3_000_000, start, end)
        val now = start + (10L * 24 * 60 * 60 * 1000)  // Day 10
        val spent = 1_000_000  // Rs 10,000

        val result = useCase(cycle, spent, now, zone)
        assertNotNull(result)
        // Day 10: elapsedDays=10, dailyRate = 1000000/10 = 100000
        // projectedTotal = 100000 * 30 = 3_000_000
        // projectedEnd = 3_000_000 - 3_000_000 = 0
        assertEquals(0L, result!!.projectedEndOfMonth)
    }
}
