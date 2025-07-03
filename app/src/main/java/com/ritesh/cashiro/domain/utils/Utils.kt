package com.ritesh.cashiro.domain.utils

import android.util.Log
import com.ritesh.cashiro.data.local.entity.RecurrenceFrequency
import com.ritesh.cashiro.data.local.entity.TransactionEntity
import com.ritesh.cashiro.presentation.ui.extras.components.charts.TimePeriod
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale
import kotlin.math.ceil

fun getLocalDateFromMillis(millis: Long): LocalDate{
    return Instant.ofEpochMilli(millis)
        .atZone(ZoneOffset.UTC)
        .toLocalDate()
}
// Helper function to get the current date in milliseconds (start of the day)
fun getCurrentDateInMillis(): Long {
    val currentDate = LocalDate.now() // Get today's date
    val zonedDateTime = currentDate.atStartOfDay(ZoneOffset.UTC) // Convert to ZonedDateTime at the start of the day
    return zonedDateTime.toInstant().toEpochMilli() // Convert to milliseconds
}

fun getMillisFromLocalDate(localDate: LocalDate): Long{
    return localDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
}

fun getDateLabel(selectedDate: LocalDate): String {
    val currentDate = LocalDate.now()
    return when (selectedDate) {
        currentDate -> "Today"
        currentDate.plusDays(1) -> "Tomorrow"
        currentDate.minusDays(1) -> "Yesterday"
        else -> selectedDate.format(DateTimeFormatter.ofPattern("dd, EEE MMM"))
    }
}
fun getEndDateLabel(selectedDate: LocalDate): String {
    val currentYear = LocalDate.now().year
    val pattern = if (selectedDate.year == currentYear) "MMM dd" else "MMM dd, yyyy"
    return selectedDate.format(DateTimeFormatter.ofPattern(pattern))
}

fun getNextDueDate(
    currentDate: Long,
    frequency: RecurrenceFrequency,
    interval: Int,
    endDate: Long
): Long? {
    val actualInterval = interval.coerceAtLeast(1)
    // Ensure interval is positive
    if (actualInterval <= 0) {
        Log.e("AddScreen", "Interval must be greater than zero.")
        return null
    }

    // Handle case where currentDate is the same as or after endDate
    if (currentDate >= endDate) {
        Log.e("AddScreen", "Current Date is same or after End Date!")
        return null
    }

    val currentLocalDate = Instant.ofEpochMilli(currentDate)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
    Log.d("AddScreen", "current Local date: $currentLocalDate")

    val nextDate: LocalDate = when (frequency) {
        RecurrenceFrequency.DAILY -> currentLocalDate.plusDays(actualInterval.toLong())
        RecurrenceFrequency.WEEKLY -> currentLocalDate.plusWeeks(actualInterval.toLong())
        RecurrenceFrequency.MONTHLY -> currentLocalDate.plusMonths(actualInterval.toLong())
        RecurrenceFrequency.YEARLY -> currentLocalDate.plusYears(actualInterval.toLong())
        else -> {
            Log.e("AddScreen", "Invalid recurrence frequency.")
            return null
        }
    }
    Log.d("AddScreen", "Next date: $nextDate")
    val nextDueMillis = nextDate.atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
    Log.d("AddScreen", "Next due date: $nextDueMillis")
    Log.d("AddScreen", "End date: $endDate")
    if (nextDueMillis > endDate) {
        Log.w("AddScreen", "Next due date exceeds end date. Returning null.")
        return null
    } else {
        Log.d("AddScreen", "Next due date: $nextDueMillis")
        return nextDueMillis
    }
}
fun calculateRecurrenceCount(
    startDate: LocalDate,
    endDate: LocalDate?,
    frequency: RecurrenceFrequency,
    interval: Int
): Int {
    if (endDate == null || interval <= 0) return 0

    val difference = when (frequency) {
        RecurrenceFrequency.NONE -> 0L
        RecurrenceFrequency.DAILY -> ChronoUnit.DAYS.between(startDate, endDate)
        RecurrenceFrequency.WEEKLY -> {
            val daysDifference = ChronoUnit.DAYS.between(startDate, endDate)
            ceil(daysDifference / 7.0).toLong()
        }
        RecurrenceFrequency.MONTHLY -> ChronoUnit.MONTHS.between(startDate, endDate)
        RecurrenceFrequency.YEARLY -> ChronoUnit.YEARS.between(startDate, endDate)
    }

    return ceil(difference.toDouble() / interval).toInt()
}

fun formattedTimeText(transactionTimeMillis: Long): String {
    val pattern = "hh:mm a" // Format for hours:minutes AM/PM
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    val formattedTime = sdf.format(Date(transactionTimeMillis))
    return  formattedTime
}

// Helper functions
// Helper extension functions to convert between LocalDate and Long timestamp
fun LocalDate.toMillis(): Long {
    return this.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

fun Long.toLocalDate(): LocalDate {
    return Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
}
// Extension function to format amount with sign
fun Double.formatWithSign(mode: String): Double {
    return if (mode == "Expense") -this else this
}
// Function to filter transactions based on time period
fun filterTransactionsByTimePeriod(
    transactions: List<TransactionEntity>,
    timePeriod: TimePeriod
): List<TransactionEntity> {
    val startMillis = timePeriod.getStartDate().toMillis()
    val endMillis = timePeriod.getEndDate().plusDays(1).toMillis() - 1 // End of the day

    return transactions.filter {
        it.date in startMillis..endMillis && it.mode != "Transfer"
    }
}
// Helper function to group transactions by day
fun groupTransactionsByDay(transactions: List<TransactionEntity>): Map<String, Double> {
    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())

    return transactions
        .groupBy { dateFormat.format(Date(it.date)) }
        .mapValues { entry ->
            entry.value.sumOf { it.amount.formatWithSign(it.mode) }
        }
        .toSortedMap() // Sort by date
}

// Helper function to group transactions by month
fun groupTransactionsByMonth(transactions: List<TransactionEntity>): Map<String, Double> {
    val dateFormat = SimpleDateFormat("MMM", Locale.getDefault())

    return transactions
        .groupBy { dateFormat.format(Date(it.date)) }
        .mapValues { entry ->
            entry.value.sumOf { it.amount.formatWithSign(it.mode) }
        }
        .toSortedMap() // Sort by month
}
