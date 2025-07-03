package com.ritesh.cashiro.domain.utils

import android.util.Log
import com.ritesh.cashiro.data.local.entity.AccountEntity
import com.ritesh.cashiro.data.local.entity.RecurrenceFrequency
import com.ritesh.cashiro.data.local.entity.TransactionEntity
import com.ritesh.cashiro.data.local.entity.TransactionType
import com.ritesh.cashiro.presentation.ui.features.schedules.ScheduleFilterState
import com.ritesh.cashiro.presentation.ui.features.schedules.SchedulePeriod
import com.ritesh.cashiro.presentation.ui.features.schedules.ScheduleSortBy

// ScheduleFilterUtils.kt - FIXED VERSION
object ScheduleFilterUtils {
    fun applyFilters(
        transactions: List<TransactionEntity>,
        filterState: ScheduleFilterState,
        accounts: List<AccountEntity>
    ): List<TransactionEntity> {
        if (!filterState.isFilterActive()) return transactions

        var filtered = transactions

        // Period filter
        filtered = when (filterState.selectedPeriod) {
            SchedulePeriod.ALL -> filtered
            SchedulePeriod.MONTHLY -> filtered.filter {
                it.recurrence?.frequency == RecurrenceFrequency.MONTHLY
            }
            SchedulePeriod.YEARLY -> filtered.filter {
                it.recurrence?.frequency == RecurrenceFrequency.YEARLY
            }
        }

        // Account filter
        if (filterState.selectedAccounts.isNotEmpty()) {
            filtered = filtered.filter {
                filterState.selectedAccounts.contains(it.accountId)
            }
        }

        // Payment status filter
        if (filterState.paymentStatus.isNotEmpty()) {
            filtered = filtered.filter { transaction ->
                when {
                    filterState.paymentStatus.contains("Paid") && transaction.isPaid -> true
                    filterState.paymentStatus.contains("Unpaid") && !transaction.isPaid -> true
                    else -> false
                }
            }
        }

        // Transaction types filter
        if (filterState.transactionTypes.isNotEmpty()) {
            filtered = filtered.filter {
                filterState.transactionTypes.contains(it.transactionType)
            }
        }

        // Date range filter
        filterState.dateRange?.let { dateRange ->
            if (dateRange.isValid()) {
                filtered = filtered.filter { transaction ->
                    val dueDate = transaction.nextDueDate ?: transaction.date
                    dateRange.contains(dueDate)
                }
            }
        }

        // Amount filter
        filtered = filtered.filter {
            it.amount >= filterState.minAmount && it.amount <= filterState.maxAmount
        }

        // Search filter
        if (filterState.searchText.text.isNotBlank()) {
            val searchQuery = filterState.searchText.text.lowercase()
            filtered = filtered.filter {
                it.title.lowercase().contains(searchQuery) ||
                        it.note.lowercase().contains(searchQuery)
            }
        }

        // Paid/Unpaid only filters
        when {
            filterState.showPaidOnly -> {
                filtered = filtered.filter { it.isPaid }
            }
            filterState.showUnpaidOnly -> {
                filtered = filtered.filter { !it.isPaid }
            }
        }

        // Sort
        filtered = when (filterState.sortBy) {
            ScheduleSortBy.DUE_DATE_ASC -> filtered.sortedBy { it.nextDueDate ?: it.date }
            ScheduleSortBy.DUE_DATE_DESC -> filtered.sortedByDescending { it.nextDueDate ?: it.date }
            ScheduleSortBy.AMOUNT_ASC -> filtered.sortedBy { it.amount }
            ScheduleSortBy.AMOUNT_DESC -> filtered.sortedByDescending { it.amount }
            ScheduleSortBy.NAME_ASC -> filtered.sortedBy { it.title }
            ScheduleSortBy.NAME_DESC -> filtered.sortedByDescending { it.title }
            ScheduleSortBy.ACCOUNT_NAME -> {
                val accountMap = accounts.associateBy { it.id }
                filtered.sortedBy { transaction ->
                    accountMap[transaction.accountId]?.accountName ?: ""
                }
            }
            ScheduleSortBy.STATUS -> filtered.sortedWith(
                compareByDescending<TransactionEntity> { !it.isPaid } // Unpaid first
                    .thenBy { it.nextDueDate ?: it.date }
            )
        }

        return filtered
    }

    // FIXED: Categorization logic based on user requirements
    fun categorizeScheduledTransactions(
        transactions: List<TransactionEntity>,
        daysAhead: Int = 30,
        daysBehind: Int = 30
    ): Triple<List<TransactionEntity>, List<TransactionEntity>, List<TransactionEntity>> {
        val currentTime = System.currentTimeMillis()

        // FIXED: Filter out already paid/collected/settled transactions from display
        val activeTransactions = transactions.filter { transaction ->
            // Include transactions that are scheduled and not yet paid/collected/settled
            val isScheduledType = transaction.transactionType in listOf(
                TransactionType.UPCOMING,
                TransactionType.SUBSCRIPTION,
                TransactionType.REPETITIVE
            )

            val isActiveTransaction = when (transaction.transactionType) {
                TransactionType.UPCOMING, TransactionType.SUBSCRIPTION, TransactionType.REPETITIVE -> !transaction.isPaid
                TransactionType.LENT -> !transaction.isCollected
                TransactionType.BORROWED -> !transaction.isSettled
                else -> true
            }

            isScheduledType && isActiveTransaction
        }

        // FIXED: Logic as per user requirements
        // Upcoming: Any transaction whose TransactionType is UPCOMING, SUBSCRIPTION, or REPETITIVE
        val upcoming = activeTransactions.filter { transaction ->
            transaction.transactionType in listOf(
                TransactionType.UPCOMING,
                TransactionType.SUBSCRIPTION,
                TransactionType.REPETITIVE
            )
        }

        // Overdue: Any transaction whose TransactionType is UPCOMING, SUBSCRIPTION, or REPETITIVE
        // BUT the transaction date is behind the current date
        val overdue = activeTransactions.filter { transaction ->
            val isScheduledType = transaction.transactionType in listOf(
                TransactionType.UPCOMING,
                TransactionType.SUBSCRIPTION,
                TransactionType.REPETITIVE
            )

            // Check if transaction date (or nextDueDate) is behind current date
            val transactionDate = transaction.nextDueDate ?: transaction.date
            val isOverdue = transactionDate < currentTime

            isScheduledType && isOverdue
        }

        // Remove overdue transactions from upcoming (since they should only be in overdue)
        val upcomingFiltered = upcoming.filter { transaction ->
            val transactionDate = transaction.nextDueDate ?: transaction.date
            transactionDate >= currentTime
        }

        return Triple(activeTransactions, upcomingFiltered, overdue)
    }

    fun getUpcomingInDays(
        transactions: List<TransactionEntity>,
        days: Int
    ): List<TransactionEntity> {
        val currentTime = System.currentTimeMillis()
        val futureTime = currentTime + (days * 24 * 60 * 60 * 1000L)

        return transactions.filter { transaction ->
            // Must be scheduled type
            val isScheduledType = transaction.transactionType in listOf(
                TransactionType.UPCOMING,
                TransactionType.SUBSCRIPTION,
                TransactionType.REPETITIVE
            )

            val isActive = when (transaction.transactionType) {
                TransactionType.UPCOMING, TransactionType.SUBSCRIPTION, TransactionType.REPETITIVE -> !transaction.isPaid
                TransactionType.LENT -> !transaction.isCollected
                TransactionType.BORROWED -> !transaction.isSettled
                else -> true
            }

            val dueDate = transaction.nextDueDate ?: transaction.date
            val isUpcoming = dueDate >= currentTime && dueDate <= futureTime

            isScheduledType && isActive && isUpcoming
        }.sortedBy { it.nextDueDate ?: it.date }
    }

    fun getOverdueInDays(
        transactions: List<TransactionEntity>,
        days: Int
    ): List<TransactionEntity> {
        val currentTime = System.currentTimeMillis()
        val pastTime = currentTime - (days * 24 * 60 * 60 * 1000L)

        return transactions.filter { transaction ->
            // Must be scheduled type
            val isScheduledType = transaction.transactionType in listOf(
                TransactionType.UPCOMING,
                TransactionType.SUBSCRIPTION,
                TransactionType.REPETITIVE
            )

            val isActive = when (transaction.transactionType) {
                TransactionType.UPCOMING, TransactionType.SUBSCRIPTION, TransactionType.REPETITIVE -> !transaction.isPaid
                TransactionType.LENT -> !transaction.isCollected
                TransactionType.BORROWED -> !transaction.isSettled
                else -> true
            }

            val dueDate = transaction.nextDueDate ?: transaction.date
            val isOverdue = dueDate < currentTime && dueDate >= pastTime

            isScheduledType && isActive && isOverdue
        }.sortedByDescending { it.nextDueDate ?: it.date }
    }
}

//object ScheduleFilterUtils {
//    fun applyFilters(
//        transactions: List<TransactionEntity>,
//        filterState: ScheduleFilterState,
//        accounts: List<AccountEntity>
//    ): List<TransactionEntity> {
//        if (!filterState.isFilterActive()) return transactions
//
//        var filtered = transactions
//
//        // Period filter
//        filtered = when (filterState.selectedPeriod) {
//            SchedulePeriod.ALL -> filtered
//            SchedulePeriod.MONTHLY -> filtered.filter {
//                it.recurrence?.frequency == RecurrenceFrequency.MONTHLY
//            }
//            SchedulePeriod.YEARLY -> filtered.filter {
//                it.recurrence?.frequency == RecurrenceFrequency.YEARLY
//            }
//        }
//
//        // Account filter
//        if (filterState.selectedAccounts.isNotEmpty()) {
//            filtered = filtered.filter {
//                filterState.selectedAccounts.contains(it.accountId)
//            }
//        }
//
//        // Payment status filter
//        if (filterState.paymentStatus.isNotEmpty()) {
//            filtered = filtered.filter { transaction ->
//                when {
//                    filterState.paymentStatus.contains("Paid") && transaction.isPaid -> true
//                    filterState.paymentStatus.contains("Unpaid") && !transaction.isPaid -> true
//                    else -> false
//                }
//            }
//        }
//
//        // Transaction types filter
//        if (filterState.transactionTypes.isNotEmpty()) {
//            filtered = filtered.filter {
//                filterState.transactionTypes.contains(it.transactionType)
//            }
//        }
//
//        // Date range filter
//        filterState.dateRange?.let { dateRange ->
//            if (dateRange.isValid()) {
//                filtered = filtered.filter { transaction ->
//                    val dueDate = transaction.nextDueDate ?: transaction.date
//                    dateRange.contains(dueDate)
//                }
//            }
//        }
//
//        // Amount filter
//        filtered = filtered.filter {
//            it.amount >= filterState.minAmount && it.amount <= filterState.maxAmount
//        }
//
//        // Search filter
//        if (filterState.searchText.text.isNotBlank()) {
//            val searchQuery = filterState.searchText.text.lowercase()
//            filtered = filtered.filter {
//                it.title.lowercase().contains(searchQuery) ||
//                        it.note.lowercase().contains(searchQuery)
//            }
//        }
//
//        // Paid/Unpaid only filters
//        when {
//            filterState.showPaidOnly -> {
//                filtered = filtered.filter { it.isPaid }
//            }
//            filterState.showUnpaidOnly -> {
//                filtered = filtered.filter { !it.isPaid }
//            }
//        }
//
//        // Sort
//        filtered = when (filterState.sortBy) {
//            ScheduleSortBy.DUE_DATE_ASC -> filtered.sortedBy { it.nextDueDate ?: it.date }
//            ScheduleSortBy.DUE_DATE_DESC -> filtered.sortedByDescending { it.nextDueDate ?: it.date }
//            ScheduleSortBy.AMOUNT_ASC -> filtered.sortedBy { it.amount }
//            ScheduleSortBy.AMOUNT_DESC -> filtered.sortedByDescending { it.amount }
//            ScheduleSortBy.NAME_ASC -> filtered.sortedBy { it.title }
//            ScheduleSortBy.NAME_DESC -> filtered.sortedByDescending { it.title }
//            ScheduleSortBy.ACCOUNT_NAME -> {
//                val accountMap = accounts.associateBy { it.id }
//                filtered.sortedBy { transaction ->
//                    accountMap[transaction.accountId]?.accountName ?: ""
//                }
//            }
//            ScheduleSortBy.STATUS -> filtered.sortedWith(
//                compareByDescending<TransactionEntity> { !it.isPaid } // Unpaid first
//                    .thenBy { it.nextDueDate ?: it.date }
//            )
//        }
//
//        return filtered
//    }
//
//    // FIXED: Better categorization logic for scheduled transactions
//    fun categorizeScheduledTransactions(
//        transactions: List<TransactionEntity>,
//        daysAhead: Int = 30,
//        daysBehind: Int = 30
//    ): Triple<List<TransactionEntity>, List<TransactionEntity>, List<TransactionEntity>> {
//        val currentTime = System.currentTimeMillis()
//        val currentDate = getLocalDateFromMillis(currentTime)
//        val futureThreshold = currentTime + (daysAhead * 24 * 60 * 60 * 1000L)
//        val pastThreshold = currentTime - (daysBehind * 24 * 60 * 60 * 1000L)
//
//        Log.d("ScheduleFilterUtils", "Categorizing ${transactions.size} transactions")
//        Log.d("ScheduleFilterUtils", "Current time: $currentTime, Future threshold: $futureThreshold, Past threshold: $pastThreshold")
//
//        // Filter scheduled transactions more inclusively
//        val scheduledTransactions = transactions.filter { transaction ->
//            val isScheduledType = transaction.transactionType in listOf(
//                TransactionType.UPCOMING,
//                TransactionType.SUBSCRIPTION,
//                TransactionType.REPETITIVE
//            )
//
//            val hasFutureDueDate = transaction.nextDueDate != null && transaction.nextDueDate!! > currentTime
//            val hasRecurrence = transaction.recurrence != null
//
//            Log.d("ScheduleFilterUtils", "Transaction ${transaction.id}: type=${transaction.transactionType}, " +
//                    "nextDueDate=${transaction.nextDueDate}, hasRecurrence=$hasRecurrence, " +
//                    "isScheduledType=$isScheduledType, hasFutureDueDate=$hasFutureDueDate")
//
//            isScheduledType || hasFutureDueDate || hasRecurrence
//        }
//
//        Log.d("ScheduleFilterUtils", "Found ${scheduledTransactions.size} scheduled transactions")
//
//        // FIXED: Better upcoming logic
//        val upcoming = scheduledTransactions.filter { transaction ->
//            val dueDate = getDueDateForTransaction(transaction, currentTime)
//            val isUpcoming = dueDate != null &&
//                    dueDate > currentTime &&
//                    dueDate <= futureThreshold &&
//                    !transaction.isPaid
//
//            Log.d("ScheduleFilterUtils", "Transaction ${transaction.id}: dueDate=$dueDate, " +
//                    "isUpcoming=$isUpcoming, isPaid=${transaction.isPaid}")
//
//            isUpcoming
//        }
//
//        // FIXED: Better overdue logic
//        val overdue = scheduledTransactions.filter { transaction ->
//            val dueDate = getDueDateForTransaction(transaction, currentTime)
//            val isOverdue = dueDate != null &&
//                    dueDate < currentTime &&
//                    dueDate >= pastThreshold &&
//                    !transaction.isPaid
//
//            Log.d("ScheduleFilterUtils", "Transaction ${transaction.id}: dueDate=$dueDate, " +
//                    "isOverdue=$isOverdue, isPaid=${transaction.isPaid}")
//
//            isOverdue
//        }
//
//        Log.d("ScheduleFilterUtils", "Categorization result - All: ${scheduledTransactions.size}, " +
//                "Upcoming: ${upcoming.size}, Overdue: ${overdue.size}")
//
//        return Triple(scheduledTransactions, upcoming, overdue)
//    }
//
//    // FIXED: Better due date calculation
//    private fun getDueDateForTransaction(transaction: TransactionEntity, currentTime: Long): Long? {
//        return when {
//            // If nextDueDate is set and in the future, use it
//            transaction.nextDueDate != null -> transaction.nextDueDate
//
//            // For subscription/repetitive with recurrence, calculate next due date
//            transaction.recurrence != null &&
//                    transaction.transactionType in listOf(TransactionType.SUBSCRIPTION, TransactionType.REPETITIVE) -> {
//                calculateNextDueDate(transaction, currentTime)
//            }
//
//            // For upcoming transactions, use the original date if it's in the future
//            transaction.transactionType == TransactionType.UPCOMING -> {
//                if (transaction.date > currentTime) transaction.date else null
//            }
//
//            // Fallback to transaction date
//            else -> transaction.date
//        }
//    }
//
//    // Helper function to calculate next due date for recurring transactions
//    private fun calculateNextDueDate(transaction: TransactionEntity, currentTime: Long): Long? {
//        val recurrence = transaction.recurrence ?: return null
//
//        // If we have an end date and it's passed, no next due date
//        if (recurrence.endRecurrenceDate != null && recurrence.endRecurrenceDate!! <= currentTime) {
//            return null
//        }
//
//        // Start from the last due date or transaction date
//        val startDate = transaction.nextDueDate ?: transaction.date
//
//        return getNextDueDate(
//            currentDate = startDate,
//            frequency = recurrence.frequency,
//            interval = recurrence.interval,
//            endDate = recurrence.endRecurrenceDate ?: Long.MAX_VALUE
//        )
//    }
//
//    fun getUpcomingInDays(
//        transactions: List<TransactionEntity>,
//        days: Int
//    ): List<TransactionEntity> {
//        val currentTime = System.currentTimeMillis()
//        val futureTime = currentTime + (days * 24 * 60 * 60 * 1000L)
//
//        return transactions.filter { transaction ->
//            val dueDate = getDueDateForTransaction(transaction, currentTime)
//            dueDate != null && !transaction.isPaid && dueDate in currentTime..futureTime
//        }.sortedBy { getDueDateForTransaction(it, currentTime) }
//    }
//
//    fun getOverdueInDays(
//        transactions: List<TransactionEntity>,
//        days: Int
//    ): List<TransactionEntity> {
//        val currentTime = System.currentTimeMillis()
//        val pastTime = currentTime - (days * 24 * 60 * 60 * 1000L)
//
//        return transactions.filter { transaction ->
//            val dueDate = getDueDateForTransaction(transaction, currentTime)
//            dueDate != null && !transaction.isPaid && dueDate < currentTime && dueDate >= pastTime
//        }.sortedByDescending { getDueDateForTransaction(it, currentTime) }
//    }
//}

//object ScheduleFilterUtils {
//    fun applyFilters(
//        transactions: List<TransactionEntity>,
//        filterState: ScheduleFilterState,
//        accounts: List<AccountEntity>
//    ): List<TransactionEntity> {
//        if (!filterState.isFilterActive()) return transactions
//
//        var filtered = transactions
//
//        // Period filter
//        filtered = when (filterState.selectedPeriod) {
//            SchedulePeriod.ALL -> filtered
//            SchedulePeriod.MONTHLY -> filtered.filter {
//                it.recurrence?.frequency == RecurrenceFrequency.MONTHLY
//            }
//            SchedulePeriod.YEARLY -> filtered.filter {
//                it.recurrence?.frequency == RecurrenceFrequency.YEARLY
//            }
//        }
//
//        // Account filter
//        if (filterState.selectedAccounts.isNotEmpty()) {
//            filtered = filtered.filter {
//                filterState.selectedAccounts.contains(it.accountId)
//            }
//        }
//
//        // Payment status filter
//        if (filterState.paymentStatus.isNotEmpty()) {
//            filtered = filtered.filter { transaction ->
//                when {
//                    filterState.paymentStatus.contains("Paid") && transaction.isPaid -> true
//                    filterState.paymentStatus.contains("Unpaid") && !transaction.isPaid -> true
//                    else -> false
//                }
//            }
//        }
//
//        // Transaction types filter
//        if (filterState.transactionTypes.isNotEmpty()) {
//            filtered = filtered.filter {
//                filterState.transactionTypes.contains(it.transactionType)
//            }
//        }
//
//        // Date range filter
//        filterState.dateRange?.let { dateRange ->
//            if (dateRange.isValid()) {
//                filtered = filtered.filter { transaction ->
//                    val dueDate = transaction.nextDueDate ?: transaction.date
//                    dateRange.contains(dueDate)
//                }
//            }
//        }
//
//        // Amount filter
//        filtered = filtered.filter {
//            it.amount >= filterState.minAmount && it.amount <= filterState.maxAmount
//        }
//
//        // Search filter
//        if (filterState.searchText.text.isNotBlank()) {
//            val searchQuery = filterState.searchText.text.lowercase()
//            filtered = filtered.filter {
//                it.title.lowercase().contains(searchQuery) ||
//                        it.note.lowercase().contains(searchQuery)
//            }
//        }
//
//        // Paid/Unpaid only filters
//        when {
//            filterState.showPaidOnly -> {
//                filtered = filtered.filter { it.isPaid }
//            }
//            filterState.showUnpaidOnly -> {
//                filtered = filtered.filter { !it.isPaid }
//            }
//        }
//
//        // Sort
//        filtered = when (filterState.sortBy) {
//            ScheduleSortBy.DUE_DATE_ASC -> filtered.sortedBy { it.nextDueDate ?: it.date }
//            ScheduleSortBy.DUE_DATE_DESC -> filtered.sortedByDescending { it.nextDueDate ?: it.date }
//            ScheduleSortBy.AMOUNT_ASC -> filtered.sortedBy { it.amount }
//            ScheduleSortBy.AMOUNT_DESC -> filtered.sortedByDescending { it.amount }
//            ScheduleSortBy.NAME_ASC -> filtered.sortedBy { it.title }
//            ScheduleSortBy.NAME_DESC -> filtered.sortedByDescending { it.title }
//            ScheduleSortBy.ACCOUNT_NAME -> {
//                val accountMap = accounts.associateBy { it.id }
//                filtered.sortedBy { transaction ->
//                    accountMap[transaction.accountId]?.accountName ?: ""
//                }
//            }
//            ScheduleSortBy.STATUS -> filtered.sortedWith(
//                compareByDescending<TransactionEntity> { !it.isPaid } // Unpaid first
//                    .thenBy { it.nextDueDate ?: it.date }
//            )
//        }
//
//        return filtered
//    }
//
//    fun categorizeScheduledTransactions(
//        transactions: List<TransactionEntity>,
//        daysAhead: Int = 30,
//        daysBehind: Int = 30
//    ): Triple<List<TransactionEntity>, List<TransactionEntity>, List<TransactionEntity>> {
//        val currentTime = System.currentTimeMillis()
//        val futureThreshold = currentTime + (daysAhead * 24 * 60 * 60 * 1000L)
//        val pastThreshold = currentTime - (daysBehind * 24 * 60 * 60 * 1000L)
//
//        val scheduledTransactions = transactions.filter { transaction ->
//            // Include transactions that are either scheduled (UPCOMING, SUBSCRIPTION, REPETITIVE)
//            // or have a future due date
//            transaction.transactionType in listOf(
//                TransactionType.UPCOMING,
//                TransactionType.SUBSCRIPTION,
//                TransactionType.REPETITIVE
//            ) || (transaction.nextDueDate != null && transaction.nextDueDate > currentTime)
//        }
//
//        val upcoming = scheduledTransactions.filter { transaction ->
//            val dueDate = transaction.nextDueDate ?: transaction.date
//            dueDate > currentTime && dueDate <= futureThreshold && !transaction.isPaid
//        }
//
//        val overdue = scheduledTransactions.filter { transaction ->
//            val dueDate = transaction.nextDueDate ?: transaction.date
//            dueDate < currentTime && dueDate >= pastThreshold && !transaction.isPaid
//        }
//
//        return Triple(scheduledTransactions, upcoming, overdue)
//    }
//
//    fun getUpcomingInDays(
//        transactions: List<TransactionEntity>,
//        days: Int
//    ): List<TransactionEntity> {
//        val currentTime = System.currentTimeMillis()
//        val futureTime = currentTime + (days * 24 * 60 * 60 * 1000L)
//
//        return transactions.filter { transaction ->
//            val dueDate = transaction.nextDueDate ?: transaction.date
//            !transaction.isPaid && dueDate in currentTime..futureTime
//        }.sortedBy { it.nextDueDate ?: it.date }
//    }
//
//    fun getOverdueInDays(
//        transactions: List<TransactionEntity>,
//        days: Int
//    ): List<TransactionEntity> {
//        val currentTime = System.currentTimeMillis()
//        val pastTime = currentTime - (days * 24 * 60 * 60 * 1000L)
//
//        return transactions.filter { transaction ->
//            val dueDate = transaction.nextDueDate ?: transaction.date
//            !transaction.isPaid && dueDate < currentTime && dueDate >= pastTime
//        }.sortedByDescending { it.nextDueDate ?: it.date }
//    }
//}