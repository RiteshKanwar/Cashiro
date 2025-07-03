package com.ritesh.cashiro.presentation.ui.features.subscriptions

import androidx.compose.ui.text.input.TextFieldValue
import com.ritesh.cashiro.data.local.entity.AccountEntity
import com.ritesh.cashiro.data.local.entity.RecurrenceFrequency
import com.ritesh.cashiro.data.local.entity.TransactionEntity
import com.ritesh.cashiro.data.local.entity.TransactionType

data class SubscriptionScreenState(
    val subscriptions: List<TransactionEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedPeriod: SubscriptionPeriod = SubscriptionPeriod.ALL,
    val filterState: SubscriptionFilterState = SubscriptionFilterState(),
    val selectedSubscriptions: Set<Int> = emptySet(),
    val isInSelectionMode: Boolean = false,
    val totalAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val unpaidAmount: Double = 0.0,
    val monthlyTotal: Double = 0.0,
    val yearlyTotal: Double = 0.0,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val filteredSubscriptions: List<TransactionEntity>
        get() = when (selectedPeriod) {
            SubscriptionPeriod.ALL -> subscriptions
            SubscriptionPeriod.MONTHLY -> subscriptions.filter { isMonthlySubscription(it) }
            SubscriptionPeriod.YEARLY -> subscriptions.filter { isYearlySubscription(it) }
        }

    private fun isMonthlySubscription(transaction: TransactionEntity): Boolean {
        return transaction.recurrence?.frequency == RecurrenceFrequency.MONTHLY
    }

    private fun isYearlySubscription(transaction: TransactionEntity): Boolean {
        return transaction.recurrence?.frequency == RecurrenceFrequency.YEARLY
    }

    fun isValidState(): Boolean {
        return error == null
    }

    fun hasSubscriptions(): Boolean {
        return subscriptions.isNotEmpty()
    }

    fun getSelectedCount(): Int {
        return selectedSubscriptions.size
    }
}

enum class SubscriptionPeriod {
    ALL,
    MONTHLY,
    YEARLY
}

data class SubscriptionFilterState(
    val selectedPeriod: SubscriptionPeriod = SubscriptionPeriod.ALL,
    val selectedAccounts: Set<Int> = emptySet(),
    val paymentStatus: Set<String> = emptySet(), // "Paid", "Unpaid"
    val subscriptionTypes: Set<TransactionType> = emptySet(),
    val minAmount: Double = 0.0,
    val maxAmount: Double = Double.MAX_VALUE,
    val searchText: TextFieldValue = TextFieldValue(""),
    val sortBy: SubscriptionSortBy = SubscriptionSortBy.DATE_DESC,
    val showActiveOnly: Boolean = false,
    val showExpiredOnly: Boolean = false
) {
    fun isFilterActive(): Boolean {
        return selectedPeriod != SubscriptionPeriod.ALL ||
                selectedAccounts.isNotEmpty() ||
                paymentStatus.isNotEmpty() ||
                subscriptionTypes.isNotEmpty() ||
                minAmount > 0.0 ||
                maxAmount < Double.MAX_VALUE ||
                searchText.text.isNotBlank() ||
                sortBy != SubscriptionSortBy.DATE_DESC ||
                showActiveOnly ||
                showExpiredOnly
    }

    fun clearAll(): SubscriptionFilterState {
        return SubscriptionFilterState()
    }
}

enum class SubscriptionSortBy {
    DATE_ASC,
    DATE_DESC,
    AMOUNT_ASC,
    AMOUNT_DESC,
    NAME_ASC,
    NAME_DESC,
    STATUS
}

object SubscriptionFilterUtils {
    fun applyFilters(
        subscriptions: List<TransactionEntity>,
        filterState: SubscriptionFilterState,
        accounts: List<AccountEntity>
    ): List<TransactionEntity> {
        if (!filterState.isFilterActive()) return subscriptions

        var filtered = subscriptions

        // Period filter
        filtered = when (filterState.selectedPeriod) {
            SubscriptionPeriod.ALL -> filtered
            SubscriptionPeriod.MONTHLY -> filtered.filter {
                it.recurrence?.frequency == RecurrenceFrequency.MONTHLY
            }
            SubscriptionPeriod.YEARLY -> filtered.filter {
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
            filtered = filtered.filter { subscription ->
                when {
                    filterState.paymentStatus.contains("Paid") && subscription.isPaid -> true
                    filterState.paymentStatus.contains("Unpaid") && !subscription.isPaid -> true
                    else -> false
                }
            }
        }

        // Subscription types filter
        if (filterState.subscriptionTypes.isNotEmpty()) {
            filtered = filtered.filter {
                filterState.subscriptionTypes.contains(it.transactionType)
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

        // Active/Expired filter
        val currentTime = System.currentTimeMillis()
        when {
            filterState.showActiveOnly -> {
                filtered = filtered.filter { subscription ->
                    val endDate = subscription.recurrence?.endRecurrenceDate
                    endDate == null || endDate > currentTime
                }
            }
            filterState.showExpiredOnly -> {
                filtered = filtered.filter { subscription ->
                    val endDate = subscription.recurrence?.endRecurrenceDate
                    endDate != null && endDate <= currentTime
                }
            }
        }

        // Sort
        filtered = when (filterState.sortBy) {
            SubscriptionSortBy.DATE_ASC -> filtered.sortedBy { it.date }
            SubscriptionSortBy.DATE_DESC -> filtered.sortedByDescending { it.date }
            SubscriptionSortBy.AMOUNT_ASC -> filtered.sortedBy { it.amount }
            SubscriptionSortBy.AMOUNT_DESC -> filtered.sortedByDescending { it.amount }
            SubscriptionSortBy.NAME_ASC -> filtered.sortedBy { it.title }
            SubscriptionSortBy.NAME_DESC -> filtered.sortedByDescending { it.title }
            SubscriptionSortBy.STATUS -> filtered.sortedWith(
                compareByDescending<TransactionEntity> { it.isPaid }
                    .thenByDescending { it.date }
            )
        }

        return filtered
    }
}