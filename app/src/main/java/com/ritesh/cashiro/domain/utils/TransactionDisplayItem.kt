package com.ritesh.cashiro.domain.utils

import com.ritesh.cashiro.data.local.entity.CategoryEntity
import com.ritesh.cashiro.data.local.entity.Recurrence
import com.ritesh.cashiro.data.local.entity.RecurrenceFrequency
import com.ritesh.cashiro.data.local.entity.SubCategoryEntity
import com.ritesh.cashiro.data.local.entity.TransactionEntity
import com.ritesh.cashiro.data.local.entity.TransactionType

// TransactionDisplayItem.kt - NEW FILE
// This new data class provides a unified way to display transactions in Schedule and Subscription screens

data class TransactionDisplayItem(
    val id: Int,
    val title: String,
    val amount: Double,
    val categoryId: Int? = null,
    val subCategoryId: Int? = null,
    val boxColor: String, // For icon background color
    val transactionDate: Long, // Current transaction date
    val dueDate: Long, // Next due date or current date
    val daysRemaining: Int, // How many days until due (negative for overdue)
    val recurrenceDuration: String, // How long the subscription will continue
    val transactionType: TransactionType,
    val isActive: Boolean, // Whether the transaction is still active (not paid/collected/settled)
    val isPaid: Boolean,
    val isCollected: Boolean = false,
    val isSettled: Boolean = false,
    val accountId: Int,
    val mode: String, // Expense, Income, Transfer
    val originalTransactionId: Int, // Reference to the original transaction
    val recurrenceInfo: RecurrenceInfo? = null
)

data class RecurrenceInfo(
    val frequency: RecurrenceFrequency,
    val interval: Int,
    val endDate: Long?,
    val remainingOccurrences: Int?
)

// Extension functions to convert TransactionEntity to TransactionDisplayItem
fun TransactionEntity.toDisplayItem(
    boxColor: String = "#6366f1", // Default color
    daysRemaining: Int = 0,
    recurrenceDuration: String = ""
): TransactionDisplayItem {
    return TransactionDisplayItem(
        id = this.id,
        title = this.title,
        amount = this.amount,
        categoryId = this.categoryId,
        subCategoryId = this.subCategoryId,
        boxColor = boxColor,
        transactionDate = this.date,
        dueDate = this.nextDueDate ?: this.date,
        daysRemaining = daysRemaining,
        recurrenceDuration = recurrenceDuration,
        transactionType = this.transactionType,
        isActive = when (this.transactionType) {
            TransactionType.UPCOMING, TransactionType.SUBSCRIPTION, TransactionType.REPETITIVE -> !this.isPaid
            TransactionType.LENT -> !this.isCollected
            TransactionType.BORROWED -> !this.isSettled
            else -> true
        },
        isPaid = this.isPaid,
        isCollected = this.isCollected,
        isSettled = this.isSettled,
        accountId = this.accountId,
        mode = this.mode,
        originalTransactionId = this.id,
        recurrenceInfo = this.recurrence?.let { rec ->
            RecurrenceInfo(
                frequency = rec.frequency,
                interval = rec.interval,
                endDate = rec.endRecurrenceDate,
                remainingOccurrences = calculateRemainingOccurrences(rec, this.date)
            )
        }
    )
}

// Helper function to calculate remaining occurrences
private fun calculateRemainingOccurrences(recurrence: Recurrence, startDate: Long): Int? {
    val endDate = recurrence.endRecurrenceDate ?: return null
    val currentDate = System.currentTimeMillis()

    if (currentDate >= endDate) return 0

    return when (recurrence.frequency) {
        RecurrenceFrequency.DAILY -> {
            val daysRemaining = (endDate - currentDate) / (24 * 60 * 60 * 1000L)
            (daysRemaining / recurrence.interval).toInt()
        }
        RecurrenceFrequency.WEEKLY -> {
            val weeksRemaining = (endDate - currentDate) / (7 * 24 * 60 * 60 * 1000L)
            (weeksRemaining / recurrence.interval).toInt()
        }
        RecurrenceFrequency.MONTHLY -> {
            // Approximate calculation for months
            val monthsRemaining = (endDate - currentDate) / (30 * 24 * 60 * 60 * 1000L)
            (monthsRemaining / recurrence.interval).toInt()
        }
        RecurrenceFrequency.YEARLY -> {
            val yearsRemaining = (endDate - currentDate) / (365 * 24 * 60 * 60 * 1000L)
            (yearsRemaining / recurrence.interval).toInt()
        }
        else -> null
    }
}

// Helper function to calculate days remaining
fun TransactionDisplayItem.calculateDaysRemaining(): Int {
    val currentTime = System.currentTimeMillis()
    val diffInMillis = this.dueDate - currentTime
    return (diffInMillis / (24 * 60 * 60 * 1000)).toInt()
}

// Helper function to get recurrence duration text
fun TransactionDisplayItem.getRecurrenceDurationText(): String {
    val recurrence = this.recurrenceInfo ?: return ""

    return when {
        recurrence.endDate != null -> {
            val endDateText = getEndDateLabel(recurrence.endDate.toLocalDate())
            "Until $endDateText"
        }
        recurrence.remainingOccurrences != null -> {
            "${recurrence.remainingOccurrences} payments remaining"
        }
        else -> "Ongoing"
    }
}

// Helper function to get status text
fun TransactionDisplayItem.getStatusText(): String {
    return when {
        this.isPaid -> "Paid"
        this.isCollected -> "Collected"
        this.isSettled -> "Settled"
        this.daysRemaining < 0 -> "Overdue (${kotlin.math.abs(this.daysRemaining)} days)"
        this.daysRemaining == 0 -> "Due Today"
        this.daysRemaining == 1 -> "Due Tomorrow"
        this.daysRemaining > 1 -> "Due in ${this.daysRemaining} days"
        else -> "Scheduled"
    }
}

// Utility object to convert lists and apply filters
object TransactionDisplayUtils {

    fun convertToDisplayItems(
        transactions: List<TransactionEntity>,
        categories: List<CategoryEntity> = emptyList(),
        subCategories: List<SubCategoryEntity> = emptyList()
    ): List<TransactionDisplayItem> {
        return transactions.mapNotNull { transaction ->
            // Skip paid/collected/settled transactions for display
            val isActive = when (transaction.transactionType) {
                TransactionType.UPCOMING, TransactionType.SUBSCRIPTION, TransactionType.REPETITIVE -> !transaction.isPaid
                TransactionType.LENT -> !transaction.isCollected
                TransactionType.BORROWED -> !transaction.isSettled
                else -> true
            }

            if (!isActive) return@mapNotNull null

            // Get category color for box color
            val category = categories.find { it.id == transaction.categoryId }
            val boxColor = category?.boxColor ?: "#6366f1"

            // Calculate days remaining
            val currentTime = System.currentTimeMillis()
            val dueDate = transaction.nextDueDate ?: transaction.date
            val daysRemaining = ((dueDate - currentTime) / (24 * 60 * 60 * 1000)).toInt()

            // Get recurrence duration text
            val recurrenceDuration = if (transaction.recurrence != null) {
                getRecurrenceDurationText(transaction.recurrence!!)
            } else ""

            transaction.toDisplayItem(
                boxColor = boxColor.toString(),
                daysRemaining = daysRemaining,
                recurrenceDuration = recurrenceDuration
            )
        }
    }

    private fun getRecurrenceDurationText(recurrence: Recurrence): String {
        return when {
            recurrence.endRecurrenceDate != null -> {
                val endDate = recurrence.endRecurrenceDate
                val endDateText = getEndDateLabel(endDate.toLocalDate())
                "Until $endDateText"
            }
            else -> {
                val frequencyText = when (recurrence.frequency) {
                    RecurrenceFrequency.DAILY -> if (recurrence.interval == 1) "Daily" else "Every ${recurrence.interval} days"
                    RecurrenceFrequency.WEEKLY -> if (recurrence.interval == 1) "Weekly" else "Every ${recurrence.interval} weeks"
                    RecurrenceFrequency.MONTHLY -> if (recurrence.interval == 1) "Monthly" else "Every ${recurrence.interval} months"
                    RecurrenceFrequency.YEARLY -> if (recurrence.interval == 1) "Yearly" else "Every ${recurrence.interval} years"
                    else -> "Ongoing"
                }
                frequencyText
            }
        }
    }

    // Group similar transactions (same title, amount, type) to avoid duplicates
    fun groupSimilarTransactions(
        displayItems: List<TransactionDisplayItem>
    ): List<TransactionDisplayItem> {
        return displayItems
            .groupBy { Triple(it.title, it.amount, it.transactionType) }
            .mapValues { (_, items) ->
                // For each group, keep only the most recent/relevant one
                items.minByOrNull { it.dueDate } ?: items.first()
            }
            .values
            .toList()
            .sortedBy { it.dueDate }
    }
}