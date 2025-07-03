package com.ritesh.cashiro.presentation.ui.features.activity_logs

import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.ritesh.cashiro.R
import com.ritesh.cashiro.domain.utils.ActivityLogFilterUtils

data class ActivityLogScreenState(
    val activities: List<ActivityLogEntry> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val filterState: ActivityLogFilterState = ActivityLogFilterState(),
    val selectedActivities: Set<Int> = emptySet(),
    val isInSelectionMode: Boolean = false,
    val totalActivities: Int = 0,
    val todayActivities: Int = 0,
    val weekActivities: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val filteredActivities: List<ActivityLogEntry>
        get() = ActivityLogFilterUtils.applyFilters(activities, filterState, emptyList())

    fun isValidState(): Boolean {
        return error == null
    }

    fun hasActivities(): Boolean {
        return activities.isNotEmpty()
    }

    fun getSelectedCount(): Int {
        return selectedActivities.size
    }

    fun getActivitiesForToday(): List<ActivityLogEntry> {
        val today = System.currentTimeMillis()
        val startOfDay = today - (today % (24 * 60 * 60 * 1000))
        val endOfDay = startOfDay + (24 * 60 * 60 * 1000) - 1

        return activities.filter { activity ->
            activity.timestamp in startOfDay..endOfDay
        }
    }

    fun getActivitiesForWeek(): List<ActivityLogEntry> {
        val now = System.currentTimeMillis()
        val weekAgo = now - (7 * 24 * 60 * 60 * 1000)

        return activities.filter { activity ->
            activity.timestamp >= weekAgo
        }
    }
}

data class ActivityLogEntry(
    val id: Int = 0,
    val actionType: ActivityActionType,
    val title: String,
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val relatedTransactionId: Int? = null,
    val relatedAccountId: Int? = null,
    val relatedCategoryId: Int? = null,
    val amount: Double? = null,
    val oldValue: String? = null,
    val newValue: String? = null,
    val metadata: Map<String, String> = emptyMap()
) {
    fun getFormattedTimestamp(): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun getTimeAgo(): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp

        return when {
            diff < 60 * 1000 -> "Just now"
            diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} minutes ago"
            diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} hours ago"
            diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)} days ago"
            else -> getFormattedTimestamp()
        }
    }
}

enum class ActivityActionType {
    // Transaction Actions
    TRANSACTION_ADDED_EXPENSE,
    TRANSACTION_ADDED_INCOME,
    TRANSACTION_ADDED_TRANSFER,
    TRANSACTION_UPDATED,
    TRANSACTION_DELETED_EXPENSE,
    TRANSACTION_DELETED_INCOME,
    TRANSACTION_DELETED_TRANSFER,
    TRANSACTION_MARKED_PAID,
    TRANSACTION_MARKED_UNPAID,
    TRANSACTION_MARKED_COLLECTED,
    TRANSACTION_MARKED_SETTLED,

    // Account Actions
    ACCOUNT_CREATED,
    ACCOUNT_UPDATED,
    ACCOUNT_DELETED,
    ACCOUNT_BALANCE_UPDATED,
    ACCOUNT_CURRENCY_CHANGED,
    ACCOUNT_SET_AS_MAIN,

    // Category Actions
    CATEGORY_CREATED,
    CATEGORY_UPDATED,
    CATEGORY_DELETED,
    SUBCATEGORY_CREATED,
    SUBCATEGORY_UPDATED,
    SUBCATEGORY_DELETED,

    // System Actions
    DATA_BACKUP_CREATED,
    DATA_RESTORED,
    SETTINGS_UPDATED,
    CURRENCY_RATES_UPDATED,

    // Subscription/Recurring Actions
    SUBSCRIPTION_CREATED,
    SUBSCRIPTION_CANCELLED,
    RECURRING_TRANSACTION_GENERATED,
    PAYMENT_REMINDER_SENT;

    fun getDisplayName(): String {
        return when (this) {
            TRANSACTION_ADDED_EXPENSE -> "Added Expense"
            TRANSACTION_ADDED_INCOME -> "Added Income"
            TRANSACTION_ADDED_TRANSFER -> "Added Transfer"
            TRANSACTION_UPDATED -> "Updated Transaction"
            TRANSACTION_DELETED_EXPENSE -> "Deleted Expense"
            TRANSACTION_DELETED_INCOME -> "Deleted Income"
            TRANSACTION_DELETED_TRANSFER -> "Deleted Transfer"
            TRANSACTION_MARKED_PAID -> "Marked as Paid"
            TRANSACTION_MARKED_UNPAID -> "Marked as Unpaid"
            TRANSACTION_MARKED_COLLECTED -> "Marked as Collected"
            TRANSACTION_MARKED_SETTLED -> "Marked as Settled"
            ACCOUNT_CREATED -> "Created Account"
            ACCOUNT_UPDATED -> "Updated Account"
            ACCOUNT_DELETED -> "Deleted Account"
            ACCOUNT_BALANCE_UPDATED -> "Balance Updated"
            ACCOUNT_CURRENCY_CHANGED -> "Currency Changed"
            ACCOUNT_SET_AS_MAIN -> "Set as Main Account"
            CATEGORY_CREATED -> "Created Category"
            CATEGORY_UPDATED -> "Updated Category"
            CATEGORY_DELETED -> "Deleted Category"
            SUBCATEGORY_CREATED -> "Created Subcategory"
            SUBCATEGORY_UPDATED -> "Updated Subcategory"
            SUBCATEGORY_DELETED -> "Deleted Subcategory"
            DATA_BACKUP_CREATED -> "Backup Created"
            DATA_RESTORED -> "Data Restored"
            SETTINGS_UPDATED -> "Settings Updated"
            CURRENCY_RATES_UPDATED -> "Currency Updated"
            SUBSCRIPTION_CREATED -> "Subscription Created"
            SUBSCRIPTION_CANCELLED -> "Subscription Cancelled"
            RECURRING_TRANSACTION_GENERATED -> "Recurring Payment"
            PAYMENT_REMINDER_SENT -> "Reminder Sent"
        }
    }

    fun getIconResource(): Int {
        return when (this) {
            TRANSACTION_ADDED_EXPENSE,
            TRANSACTION_DELETED_INCOME -> R.drawable.arrow_mini_right_bulk // Down arrow for expense
            TRANSACTION_ADDED_INCOME,
            TRANSACTION_DELETED_EXPENSE -> R.drawable.arrow_mini_left_bulk // Up arrow for income
            TRANSACTION_ADDED_TRANSFER,
            TRANSACTION_DELETED_TRANSFER -> R.drawable.arrow_mini_right_bulk // Transfer icon
            TRANSACTION_UPDATED -> R.drawable.edit_bulk
            TRANSACTION_MARKED_PAID,
            TRANSACTION_MARKED_COLLECTED,
            TRANSACTION_MARKED_SETTLED -> R.drawable.select_all // Checkmark
            TRANSACTION_MARKED_UNPAID -> R.drawable.delete_bulk // X mark
            ACCOUNT_CREATED,
            ACCOUNT_UPDATED -> R.drawable.wallet_bulk
            ACCOUNT_DELETED -> R.drawable.delete_bulk
            ACCOUNT_BALANCE_UPDATED -> R.drawable.wallet_bulk
            ACCOUNT_CURRENCY_CHANGED -> R.drawable.wallet_bulk
            ACCOUNT_SET_AS_MAIN -> R.drawable.wallet_bulk
            CATEGORY_CREATED,
            CATEGORY_UPDATED,
            SUBCATEGORY_CREATED,
            SUBCATEGORY_UPDATED -> R.drawable.category_bulk
            CATEGORY_DELETED,
            SUBCATEGORY_DELETED -> R.drawable.delete_bulk
            DATA_BACKUP_CREATED -> R.drawable.archive
            DATA_RESTORED -> R.drawable.archive
            SETTINGS_UPDATED -> R.drawable.settings_bulk
            CURRENCY_RATES_UPDATED -> R.drawable.wallet_bulk
            SUBSCRIPTION_CREATED -> R.drawable.notification // Bell icon
            SUBSCRIPTION_CANCELLED -> R.drawable.delete_bulk
            RECURRING_TRANSACTION_GENERATED -> R.drawable.notification // Repeat icon
            PAYMENT_REMINDER_SENT -> R.drawable.notification
        }
    }

    fun getIconBackgroundColor(): Color {
        return when (this) {
            TRANSACTION_ADDED_EXPENSE,
            TRANSACTION_DELETED_INCOME,
            TRANSACTION_MARKED_UNPAID,
            ACCOUNT_DELETED,
            CATEGORY_DELETED,
            SUBCATEGORY_DELETED,
            SUBSCRIPTION_CANCELLED -> Color(0xFFEF4444) // Red
            TRANSACTION_ADDED_INCOME,
            TRANSACTION_DELETED_EXPENSE,
            TRANSACTION_MARKED_PAID,
            TRANSACTION_MARKED_COLLECTED,
            TRANSACTION_MARKED_SETTLED -> Color(0xFF10B981) // Green
            TRANSACTION_ADDED_TRANSFER,
            TRANSACTION_DELETED_TRANSFER,
            ACCOUNT_BALANCE_UPDATED -> Color(0xFF3B82F6) // Blue
            TRANSACTION_UPDATED,
            ACCOUNT_UPDATED,
            CATEGORY_UPDATED,
            SUBCATEGORY_UPDATED,
            SETTINGS_UPDATED -> Color(0xFFF59E0B) // Orange
            ACCOUNT_CREATED,
            ACCOUNT_CURRENCY_CHANGED,
            ACCOUNT_SET_AS_MAIN -> Color(0xFF8B5CF6) // Purple
            CATEGORY_CREATED,
            SUBCATEGORY_CREATED -> Color(0xFF06B6D4) // Cyan
            DATA_BACKUP_CREATED,
            DATA_RESTORED -> Color(0xFF84CC16) // Lime
            CURRENCY_RATES_UPDATED -> Color(0xFFEC4899) // Pink
            SUBSCRIPTION_CREATED,
            RECURRING_TRANSACTION_GENERATED,
            PAYMENT_REMINDER_SENT -> Color(0xFF6366F1) // Indigo
        }
    }

    fun getCategory(): ActivityCategory {
        return when (this) {
            TRANSACTION_ADDED_EXPENSE,
            TRANSACTION_ADDED_INCOME,
            TRANSACTION_ADDED_TRANSFER,
            TRANSACTION_UPDATED,
            TRANSACTION_DELETED_EXPENSE,
            TRANSACTION_DELETED_INCOME,
            TRANSACTION_DELETED_TRANSFER,
            TRANSACTION_MARKED_PAID,
            TRANSACTION_MARKED_UNPAID,
            TRANSACTION_MARKED_COLLECTED,
            TRANSACTION_MARKED_SETTLED -> ActivityCategory.TRANSACTIONS

            ACCOUNT_CREATED,
            ACCOUNT_UPDATED,
            ACCOUNT_DELETED,
            ACCOUNT_BALANCE_UPDATED,
            ACCOUNT_CURRENCY_CHANGED,
            ACCOUNT_SET_AS_MAIN -> ActivityCategory.ACCOUNTS

            CATEGORY_CREATED,
            CATEGORY_UPDATED,
            CATEGORY_DELETED,
            SUBCATEGORY_CREATED,
            SUBCATEGORY_UPDATED,
            SUBCATEGORY_DELETED -> ActivityCategory.CATEGORIES

            DATA_BACKUP_CREATED,
            DATA_RESTORED,
            SETTINGS_UPDATED,
            CURRENCY_RATES_UPDATED -> ActivityCategory.SYSTEM

            SUBSCRIPTION_CREATED,
            SUBSCRIPTION_CANCELLED,
            RECURRING_TRANSACTION_GENERATED,
            PAYMENT_REMINDER_SENT -> ActivityCategory.SUBSCRIPTIONS
        }
    }
}

enum class ActivityCategory {
    ALL,
    TRANSACTIONS,
    ACCOUNTS,
    CATEGORIES,
    SUBSCRIPTIONS,
    SYSTEM;

    fun getDisplayName(): String {
        return when (this) {
            ALL -> "All"
            TRANSACTIONS -> "Transactions"
            ACCOUNTS -> "Accounts"
            CATEGORIES -> "Categories"
            SUBSCRIPTIONS -> "Subscriptions"
            SYSTEM -> "System"
        }
    }
}

data class ActivityLogFilterState(
    val selectedActionTypes: Set<ActivityActionType> = emptySet(),
    val selectedCategories: Set<ActivityCategory> = emptySet(),
    val selectedAccounts: Set<Int> = emptySet(),
    val dateRange: ActivityDateRange? = null,
    val searchText: androidx.compose.ui.text.input.TextFieldValue = androidx.compose.ui.text.input.TextFieldValue(""),
    val sortBy: ActivityLogSortBy = ActivityLogSortBy.TIMESTAMP_DESC,
    val showOnlyToday: Boolean = false,
    val showOnlyThisWeek: Boolean = false,
    val showOnlyThisMonth: Boolean = false
) {
    fun isFilterActive(): Boolean {
        return selectedActionTypes.isNotEmpty() ||
                selectedCategories.isNotEmpty() ||
                selectedAccounts.isNotEmpty() ||
                dateRange != null ||
                searchText.text.isNotBlank() ||
                sortBy != ActivityLogSortBy.TIMESTAMP_DESC ||
                showOnlyToday ||
                showOnlyThisWeek ||
                showOnlyThisMonth
    }

    fun clearAll(): ActivityLogFilterState {
        return ActivityLogFilterState()
    }
}

data class ActivityDateRange(
    val startDate: Long,
    val endDate: Long
) {
    fun contains(timestamp: Long): Boolean {
        return timestamp in startDate..endDate
    }

    fun isValid(): Boolean {
        return startDate <= endDate
    }
}

enum class ActivityLogSortBy {
    TIMESTAMP_ASC,
    TIMESTAMP_DESC,
    TYPE_ASC,
    TYPE_DESC,
    ACCOUNT_NAME,
    AMOUNT_ASC,
    AMOUNT_DESC
}
