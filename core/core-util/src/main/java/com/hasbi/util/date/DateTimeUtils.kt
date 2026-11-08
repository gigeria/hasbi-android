package com.hasbi.util.date

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Date/time utilities — Hasbi standardizes on UTC storage + local timezone display.
 *
 * Critical rules:
 * - All DB timestamps: UTC epoch millis (Long)
 * - Default timezone: Asia/Karachi (configurable)
 * - Reporting periods: based on user's local timezone
 * - Date ranges: INCLUSIVE start, EXCLUSIVE end
 *   (WHERE date >= :start AND date < :end)
 */
object DateTimeUtils {

    const val DEFAULT_TIMEZONE = "Asia/Karachi"

    /** 30 days in millis — for soft delete purgeAfter calculation. */
    const val THIRTY_DAYS_MILLIS = 30L * 24 * 60 * 60 * 1000

    fun defaultZone(): ZoneId = ZoneId.of(DEFAULT_TIMEZONE)

    fun zone(id: String = DEFAULT_TIMEZONE): ZoneId = ZoneId.of(id)

    /**
     * Get start of day (00:00:00) for a given UTC millis in user's timezone.
     * Returns UTC millis.
     */
    fun startOfDay(epochMillis: Long, zone: ZoneId = defaultZone()): Long {
        val localDate = Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()
        return localDate.atStartOfDay(zone).toInstant().toEpochMilli()
    }

    /**
     * Get end of day (next day 00:00:00) for a given UTC millis.
     * EXCLUSIVE — used in date range queries.
     */
    fun endOfDay(epochMillis: Long, zone: ZoneId = defaultZone()): Long {
        val localDate = Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate().plusDays(1)
        return localDate.atStartOfDay(zone).toInstant().toEpochMilli()
    }

    /**
     * Get start of month (1st day, 00:00:00) for a given date.
     */
    fun startOfMonth(year: Int, month: Int, zone: ZoneId = defaultZone()): Long {
        val localDate = LocalDate.of(year, month, 1)
        return localDate.atStartOfDay(zone).toInstant().toEpochMilli()
    }

    /**
     * Get end of month (1st of next month, 00:00:00).
     * EXCLUSIVE — used in date range queries.
     */
    fun endOfMonth(year: Int, month: Int, zone: ZoneId = defaultZone()): Long {
        val nextMonth = LocalDate.of(year, month, 1).plusMonths(1)
        return nextMonth.atStartOfDay(zone).toInstant().toEpochMilli()
    }

    /**
     * Calculate cycle boundaries for MONTHLY cycle starting 1st.
     *
     * @param anchor Any date in the cycle
     * @return Pair(cycleStart, cycleEnd) — start INCLUSIVE, end EXCLUSIVE
     */
    fun monthlyCycle(anchor: LocalDate, zone: ZoneId = defaultZone()): Pair<Long, Long> {
        val startDate = anchor.withDayOfMonth(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val endDate = anchor.withDayOfMonth(1).plusMonths(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return startDate to endDate
    }

    /**
     * Calculate cycle boundaries for SALARY_DATE cycle.
     *
     * Handles month-end edge cases:
     * - Day 31 in February → uses Feb 28/29
     * - Day 31 in April → uses April 30
     *
     * @param salaryDay Day of month (1-31)
     * @param anchor Any date in the cycle
     * @return Pair(cycleStart, cycleEnd) — start INCLUSIVE, end EXCLUSIVE
     */
    fun salaryDateCycle(salaryDay: Int, anchor: LocalDate, zone: ZoneId = defaultZone()): Pair<Long, Long> {
        require(salaryDay in 1..31) { "salaryDay must be 1-31, got $salaryDay" }

        val maxDayThisMonth = anchor.lengthOfMonth()
        val actualDayThisMonth = minOf(salaryDay, maxDayThisMonth)
        val startDate = anchor.withDayOfMonth(actualDayThisMonth)
            .atStartOfDay(zone).toInstant().toEpochMilli()

        val nextMonth = anchor.plusMonths(1)
        val maxDayNextMonth = nextMonth.lengthOfMonth()
        val actualDayNextMonth = minOf(salaryDay, maxDayNextMonth)
        val endDate = nextMonth.withDayOfMonth(actualDayNextMonth)
            .atStartOfDay(zone).toInstant().toEpochMilli()

        return startDate to endDate
    }

    /**
     * Days remaining until cycle end (INCLUSIVE of today).
     *
     * @param cycleEnd Epoch millis (exclusive end)
     * @param now Current epoch millis
     * @return Days remaining, min 1 (today always counts)
     */
    fun daysRemaining(cycleEnd: Long, now: Long = System.currentTimeMillis(), zone: ZoneId = defaultZone()): Int {
        val endLocal = Instant.ofEpochMilli(cycleEnd).atZone(zone).toLocalDate()
        val nowLocal = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        val days = ChronoUnit.DAYS.between(nowLocal, endLocal).toInt()
        return days.coerceAtLeast(1)  // include today
    }

    /**
     * Total days in cycle (inclusive of start day).
     */
    fun totalDaysInCycle(cycleStart: Long, cycleEnd: Long, zone: ZoneId = defaultZone()): Int {
        val startLocal = Instant.ofEpochMilli(cycleStart).atZone(zone).toLocalDate()
        val endLocal = Instant.ofEpochMilli(cycleEnd).atZone(zone).toLocalDate()
        return ChronoUnit.DAYS.between(startLocal, endLocal).toInt().coerceAtLeast(1)
    }

    /**
     * Days elapsed in cycle (including today).
     */
    fun daysElapsed(cycleStart: Long, now: Long = System.currentTimeMillis(), zone: ZoneId = defaultZone()): Int {
        val startLocal = Instant.ofEpochMilli(cycleStart).atZone(zone).toLocalDate()
        val nowLocal = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        return ChronoUnit.DAYS.between(startLocal, nowLocal).toInt() + 1  // include today
    }

    /**
     * Convert epoch millis to LocalDate in given timezone.
     */
    fun toLocalDate(epochMillis: Long, zone: ZoneId = defaultZone()): LocalDate {
        return Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()
    }

    /**
     * Convert epoch millis to LocalDateTime in given timezone.
     */
    fun toLocalDateTime(epochMillis: Long, zone: ZoneId = defaultZone()): LocalDateTime {
        return Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDateTime()
    }
}
