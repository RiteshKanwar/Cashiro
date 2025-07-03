package com.ritesh.cashiro.presentation.ui.extras.components.charts

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters


sealed class TimePeriod {
    abstract fun getDisplayName(): String
    abstract fun getStartDate(): LocalDate
    abstract fun getEndDate(): LocalDate
    abstract fun getDate(): LocalDate
    abstract fun next(): TimePeriod
    abstract fun previous(): TimePeriod
}

class WeekPeriod(private val date: LocalDate) : TimePeriod() {
    private val startOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    private val endOfWeek = startOfWeek.plusDays(6)

    override fun getDisplayName(): String {
        val formatter = DateTimeFormatter.ofPattern("MMM d")
        val today = LocalDate.now()
        val currentWeekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

        return when {
            startOfWeek.isEqual(currentWeekStart) -> "This Week"
            else -> "${startOfWeek.format(formatter)} - ${endOfWeek.format(formatter)}"
        }
    }

    override fun getStartDate(): LocalDate = startOfWeek
    override fun getEndDate(): LocalDate = endOfWeek
    override fun getDate(): LocalDate = date

    override fun next(): TimePeriod = WeekPeriod(startOfWeek.plusWeeks(1))
    override fun previous(): TimePeriod = WeekPeriod(startOfWeek.minusWeeks(1))
}

class MonthPeriod(private val date: LocalDate) : TimePeriod() {
    private val startOfMonth = date.withDayOfMonth(1)
    private val endOfMonth = date.withDayOfMonth(date.lengthOfMonth())

    override fun getDisplayName(): String {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
        return startOfMonth.format(formatter)
    }


    override fun getStartDate(): LocalDate = startOfMonth
    override fun getEndDate(): LocalDate = endOfMonth
    override fun getDate(): LocalDate = date

    override fun next(): TimePeriod = MonthPeriod(startOfMonth.plusMonths(1))
    override fun previous(): TimePeriod = MonthPeriod(startOfMonth.minusMonths(1))
}

class YearPeriod(private val date: LocalDate) : TimePeriod() {
    private val startOfYear = date.withDayOfYear(1)
    private val endOfYear = date.withDayOfYear(date.lengthOfYear())

    override fun getDisplayName(): String {
        return "Year ${date.year}"
    }

    override fun getStartDate(): LocalDate = startOfYear
    override fun getEndDate(): LocalDate = endOfYear
    override fun getDate(): LocalDate = date

    override fun next(): TimePeriod = YearPeriod(startOfYear.plusYears(1))
    override fun previous(): TimePeriod = YearPeriod(startOfYear.minusYears(1))
}