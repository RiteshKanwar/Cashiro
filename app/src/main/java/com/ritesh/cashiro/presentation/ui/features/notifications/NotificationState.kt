package com.ritesh.cashiro.presentation.ui.features.notifications

import com.ritesh.cashiro.data.local.entity.TransactionEntity
import com.ritesh.cashiro.domain.repository.ReminderType
import com.ritesh.cashiro.domain.repository.Settings

/**
 * Represents the UI state for the Notifications screen
 */
data class NotificationState(
    val isLoading: Boolean = false,
    val upcomingTransactions: List<TransactionEntity> = emptyList(),
    val settings: Settings? = null,
    val hasNotificationPermissions: Boolean = false,
    val error: String? = null,

    // Dialog and Sheet states
    val showReminderTypeModal: Boolean = false,
    val showTimePicker: Boolean = false,
    val showPermissionDialog: Boolean = false,

    // Time picker state
    val selectedHour: Int = 9,
    val selectedMinute: Int = 0,

    // Filter and display options
    val showOnlyUnpaidTransactions: Boolean = true,
    val sortByDueDate: Boolean = true
) {
    /**
     * Returns notification settings from the main settings object
     */
    val notificationSettings
        get() = settings?.notificationSettings

    /**
     * Returns whether upcoming transactions feature is enabled
     */
    val isUpcomingTransactionsEnabled: Boolean
        get() = notificationSettings?.upcomingTransactions ?: false

    /**
     * Returns whether add transaction reminder is enabled
     */
    val isAddTransactionReminderEnabled: Boolean
        get() = notificationSettings?.addTransactionReminder ?: false

    /**
     * Returns filtered upcoming transactions based on current filter settings
     */
    val filteredUpcomingTransactions: List<TransactionEntity>
        get() = upcomingTransactions.filter { transaction ->
            if (showOnlyUnpaidTransactions) {
                !transaction.isPaid
            } else {
                true
            }
        }.let { filtered ->
            if (sortByDueDate) {
                filtered.sortedBy { it.nextDueDate ?: it.date }
            } else {
                filtered.sortedByDescending { it.date }
            }
        }

    /**
     * Returns the count of transactions due today
     */
    val transactionsDueToday: Int
        get() {
            val today = System.currentTimeMillis()
            val todayStart = today - (today % (24 * 60 * 60 * 1000))
            val todayEnd = todayStart + (24 * 60 * 60 * 1000)

            return upcomingTransactions.count { transaction ->
                val dueDate = transaction.nextDueDate ?: transaction.date
                dueDate in todayStart..todayEnd && !transaction.isPaid
            }
        }

    /**
     * Returns the count of overdue transactions
     */
    val overdueTransactions: Int
        get() {
            val currentTime = System.currentTimeMillis()
            return upcomingTransactions.count { transaction ->
                val dueDate = transaction.nextDueDate ?: transaction.date
                dueDate < currentTime && !transaction.isPaid
            }
        }

    /**
     * Returns whether any notifications need attention
     */
    val hasNotificationAlerts: Boolean
        get() = overdueTransactions > 0 || transactionsDueToday > 0
}

/**
 * Events that can be triggered from the Notifications screen
 */
sealed class NotificationEvent {
    // Permission events
    object RequestNotificationPermissions : NotificationEvent()
    object CheckNotificationPermissions : NotificationEvent()

    // Data loading events
    object LoadUpcomingTransactions : NotificationEvent()
    object RefreshData : NotificationEvent()

    // Settings update events
    data class UpdateAddTransactionReminder(val enabled: Boolean) : NotificationEvent()
    data class UpdateUpcomingTransactions(val enabled: Boolean) : NotificationEvent()
    data class UpdateReminderType(val reminderType: ReminderType) : NotificationEvent()
    data class UpdateAlertTime(val hour: Int, val minute: Int) : NotificationEvent()
    data class UpdateSipNotifications(val sipNotifications: Map<String, Boolean>) : NotificationEvent()

    // Transaction events
    data class ToggleTransactionNotification(val transactionId: Int, val enabled: Boolean) : NotificationEvent()
    data class MarkTransactionAsPaid(val transaction: TransactionEntity) : NotificationEvent()
    data class MarkTransactionAsUnpaid(val transaction: TransactionEntity) : NotificationEvent()

    // UI state events
    data class SetReminderTypeModalVisible(val visible: Boolean) : NotificationEvent()
    data class SetTimePickerVisible(val visible: Boolean) : NotificationEvent()
    data class SetPermissionDialogVisible(val visible: Boolean) : NotificationEvent()
    data class SetFilterOnlyUnpaid(val onlyUnpaid: Boolean) : NotificationEvent()
    data class SetSortByDueDate(val sortByDueDate: Boolean) : NotificationEvent()

    // Error handling
    data class ShowError(val message: String) : NotificationEvent()
    object ClearError : NotificationEvent()
}