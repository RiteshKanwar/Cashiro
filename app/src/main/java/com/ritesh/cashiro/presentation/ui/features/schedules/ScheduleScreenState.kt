package com.ritesh.cashiro.presentation.ui.features.schedules

import com.ritesh.cashiro.data.local.entity.TransactionEntity
import com.ritesh.cashiro.data.local.entity.TransactionType

data class ScheduleScreenState(
    val allTransactions: List<TransactionEntity> = emptyList(),
    val upcomingTransactions: List<TransactionEntity> = emptyList(),
    val overdueTransactions: List<TransactionEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedTabIndex: Int = 0,
    val filterState: ScheduleFilterState = ScheduleFilterState(),
    val selectedTransactions: Set<Int> = emptySet(),
    val isInSelectionMode: Boolean = false,
    val totalAmount: Double = 0.0,
    val upcomingAmount: Double = 0.0,
    val overdueAmount: Double = 0.0,
    val monthlyTotal: Double = 0.0,
    val yearlyTotal: Double = 0.0,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val currentTabTransactions: List<TransactionEntity>
        get() = when (selectedTabIndex) {
            0 -> allTransactions
            1 -> upcomingTransactions
            2 -> overdueTransactions
            else -> emptyList()
        }

    fun isValidState(): Boolean {
        return error == null
    }

    fun hasScheduledTransactions(): Boolean {
        return allTransactions.isNotEmpty()
    }

    fun hasUpcomingTransactions(): Boolean {
        return upcomingTransactions.isNotEmpty()
    }

    fun hasOverdueTransactions(): Boolean {
        return overdueTransactions.isNotEmpty()
    }

    fun getSelectedCount(): Int {
        return selectedTransactions.size
    }

    fun getTabTitle(index: Int): String {
        return when (index) {
            0 -> "All (${allTransactions.size})"
            1 -> "Upcoming (${upcomingTransactions.size})"
            2 -> "Overdue (${overdueTransactions.size})"
            else -> "Unknown"
        }
    }
}

enum class SchedulePeriod {
    ALL,
    MONTHLY,
    YEARLY
}

data class ScheduleFilterState(
    val selectedPeriod: SchedulePeriod = SchedulePeriod.ALL,
    val selectedAccounts: Set<Int> = emptySet(),
    val paymentStatus: Set<String> = emptySet(), // "Paid", "Unpaid"
    val transactionTypes: Set<TransactionType> = emptySet(),
    val dateRange: ScheduleDateRange? = null,
    val minAmount: Double = 0.0,
    val maxAmount: Double = Double.MAX_VALUE,
    val searchText: androidx.compose.ui.text.input.TextFieldValue = androidx.compose.ui.text.input.TextFieldValue(""),
    val sortBy: ScheduleSortBy = ScheduleSortBy.DUE_DATE_ASC,
    val showPaidOnly: Boolean = false,
    val showUnpaidOnly: Boolean = false,
    val daysAhead: Int = 30, // For upcoming transactions
    val daysBehind: Int = 30, // For overdue transactions
) {
    fun isFilterActive(): Boolean {
        return selectedPeriod != SchedulePeriod.ALL ||
                selectedAccounts.isNotEmpty() ||
                paymentStatus.isNotEmpty() ||
                transactionTypes.isNotEmpty() ||
                dateRange != null ||
                minAmount > 0.0 ||
                maxAmount < Double.MAX_VALUE ||
                searchText.text.isNotBlank() ||
                sortBy != ScheduleSortBy.DUE_DATE_ASC ||
                showPaidOnly ||
                showUnpaidOnly ||
                daysAhead != 30 ||
                daysBehind != 30
    }

    fun clearAll(): ScheduleFilterState {
        return ScheduleFilterState()
    }
}

data class ScheduleDateRange(
    val startDate: Long,
    val endDate: Long
) {
    fun contains(date: Long): Boolean {
        return date in startDate..endDate
    }

    fun isValid(): Boolean {
        return startDate <= endDate
    }
}

enum class ScheduleSortBy {
    DUE_DATE_ASC,
    DUE_DATE_DESC,
    AMOUNT_ASC,
    AMOUNT_DESC,
    NAME_ASC,
    NAME_DESC,
    ACCOUNT_NAME,
    STATUS
}
