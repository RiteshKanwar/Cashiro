package com.ritesh.cashiro.presentation.ui.features.schedules

import com.ritesh.cashiro.data.local.entity.Recurrence
import com.ritesh.cashiro.data.local.entity.TransactionEntity
import com.ritesh.cashiro.data.local.entity.TransactionType

sealed class ScheduleEvent {
    // Data Loading Events
    object LoadScheduledTransactions : ScheduleEvent()
    object RefreshScheduledTransactions : ScheduleEvent()

    // Tab Navigation Events
    data class SelectTab(val tabIndex: Int) : ScheduleEvent()

    // Filter Events
    data class UpdatePeriodFilter(val period: SchedulePeriod) : ScheduleEvent()
    data class UpdateFilterState(val filterState: ScheduleFilterState) : ScheduleEvent()
    data class UpdateAccountFilter(val accountIds: Set<Int>) : ScheduleEvent()
    data class UpdatePaymentStatusFilter(val statuses: Set<String>) : ScheduleEvent()
    data class UpdateTransactionTypeFilter(val types: Set<TransactionType>) : ScheduleEvent()
    data class UpdateDateRangeFilter(val dateRange: ScheduleDateRange?) : ScheduleEvent()
    data class UpdateAmountFilter(val minAmount: Double, val maxAmount: Double) : ScheduleEvent()
    data class UpdateSearchFilter(val searchText: androidx.compose.ui.text.input.TextFieldValue) : ScheduleEvent()
    data class UpdateSortBy(val sortBy: ScheduleSortBy) : ScheduleEvent()
    data class UpdatePaidFilter(val showPaidOnly: Boolean) : ScheduleEvent()
    data class UpdateUnpaidFilter(val showUnpaidOnly: Boolean) : ScheduleEvent()
    data class UpdateDaysAhead(val days: Int) : ScheduleEvent()
    data class UpdateDaysBehind(val days: Int) : ScheduleEvent()
    object ClearAllFilters : ScheduleEvent()

    // Selection Events
    data class ToggleTransactionSelection(val transactionId: Int) : ScheduleEvent()
    data class SelectTransaction(val transactionId: Int) : ScheduleEvent()
    data class DeselectTransaction(val transactionId: Int) : ScheduleEvent()
    object SelectAllTransactions : ScheduleEvent()
    object ClearSelection : ScheduleEvent()
    data class SetSelectionMode(val isEnabled: Boolean) : ScheduleEvent()

    // Transaction Management Events
    data class MarkAsPaid(val transaction: TransactionEntity) : ScheduleEvent()
    data class MarkAsUnpaid(val transaction: TransactionEntity) : ScheduleEvent()
    data class SnoozeTransaction(val transactionId: Int, val snoozeUntil: Long) : ScheduleEvent()
    data class RescheduleTransaction(val transactionId: Int, val newDate: Long) : ScheduleEvent()
    data class UpdateTransaction(val transaction: TransactionEntity) : ScheduleEvent()
    data class DeleteTransaction(val transactionId: Int) : ScheduleEvent()
    data class DeleteScheduledTransactions(val transactionIds: List<Int>) : ScheduleEvent()

    // Bulk Operations Events
    data class BulkMarkAsPaid(val transactionIds: List<Int>) : ScheduleEvent()
    data class BulkMarkAsUnpaid(val transactionIds: List<Int>) : ScheduleEvent()
    data class BulkSnooze(val transactionIds: List<Int>, val snoozeUntil: Long) : ScheduleEvent()
    data class BulkReschedule(val transactionIds: List<Int>, val newDate: Long) : ScheduleEvent()
    data class BulkDelete(val transactionIds: List<Int>) : ScheduleEvent()

    // Navigation Events
    data class NavigateToAddTransaction(val defaultType: TransactionType = TransactionType.UPCOMING) : ScheduleEvent()
    data class NavigateToEditTransaction(val transactionId: Int) : ScheduleEvent()
    data class NavigateToTransactionDetails(val transactionId: Int) : ScheduleEvent()

    // Calculation and Analysis Events
    object RecalculateTotals : ScheduleEvent()
    data class UpdateConversionRates(val rates: Map<String, Double>) : ScheduleEvent()
    object AnalyzeSchedule : ScheduleEvent()
    data class GetUpcomingInDays(val days: Int) : ScheduleEvent()
    data class GetOverdueInDays(val days: Int) : ScheduleEvent()

    // Notification Events
    data class ScheduleReminder(val transactionId: Int, val reminderTime: Long) : ScheduleEvent()
    data class CancelReminder(val transactionId: Int) : ScheduleEvent()
    object ScheduleAllReminders : ScheduleEvent()
    object CancelAllReminders : ScheduleEvent()
    data class UpdateReminderSettings(val settings: ReminderSettings) : ScheduleEvent()

    // Export Events
    object ExportSchedule : ScheduleEvent()
    data class ExportScheduleForPeriod(val period: SchedulePeriod) : ScheduleEvent()
    data class ExportUpcoming(val days: Int = 30) : ScheduleEvent()
    data class ExportOverdue(val days: Int = 30) : ScheduleEvent()

    // Error Handling Events
    data class ShowError(val message: String) : ScheduleEvent()
    object ClearError : ScheduleEvent()

    // UI State Events
    data class SetLoading(val isLoading: Boolean) : ScheduleEvent()
    data class UpdateLastRefresh(val timestamp: Long = System.currentTimeMillis()) : ScheduleEvent()

    // Quick Actions Events
    data class PayNow(val transactionId: Int) : ScheduleEvent()
    data class SkipPayment(val transactionId: Int) : ScheduleEvent()
    data class PayAll(val transactionIds: List<Int>) : ScheduleEvent()
    data class SnoozeAll(val transactionIds: List<Int>, val hours: Int) : ScheduleEvent()

    // Recurring Transaction Events
    data class GenerateNextRecurrence(val transactionId: Int) : ScheduleEvent()
    data class UpdateRecurrenceSettings(val transactionId: Int, val recurrence: Recurrence) : ScheduleEvent()
    data class PauseRecurrence(val transactionId: Int) : ScheduleEvent()
    data class ResumeRecurrence(val transactionId: Int) : ScheduleEvent()
    data class StopRecurrence(val transactionId: Int) : ScheduleEvent()
}

// UI Events that are emitted back to the UI
sealed class ScheduleUiEvent {
    object ScheduledTransactionsLoaded : ScheduleUiEvent()
    object ScheduledTransactionsRefreshed : ScheduleUiEvent()
    data class TabChanged(val tabIndex: Int) : ScheduleUiEvent()
    data class TransactionMarkedAsPaid(val transactionId: Int) : ScheduleUiEvent()
    data class TransactionMarkedAsUnpaid(val transactionId: Int) : ScheduleUiEvent()
    data class TransactionSnoozed(val transactionId: Int, val snoozeUntil: Long) : ScheduleUiEvent()
    data class TransactionRescheduled(val transactionId: Int, val newDate: Long) : ScheduleUiEvent()
    data class TransactionUpdated(val transactionId: Int) : ScheduleUiEvent()
    data class TransactionDeleted(val transactionId: Int) : ScheduleUiEvent()
    data class TransactionsDeleted(val count: Int) : ScheduleUiEvent()
    data class BulkOperationCompleted(val operation: String, val count: Int) : ScheduleUiEvent()
    data class NavigateToScreen(val destination: String, val args: Map<String, Any> = emptyMap()) : ScheduleUiEvent()
    data class ShowMessage(val message: String, val isError: Boolean = false) : ScheduleUiEvent()
    data class ShowSnackbar(val message: String, val actionLabel: String? = null, val onAction: (() -> Unit)? = null) : ScheduleUiEvent()
    data class ShowConfirmationDialog(val title: String, val message: String, val onConfirm: () -> Unit) : ScheduleUiEvent()
    object ExportCompleted : ScheduleUiEvent()
    data class ReminderScheduled(val transactionId: Int) : ScheduleUiEvent()
    data class ReminderCancelled(val transactionId: Int) : ScheduleUiEvent()
    data class AnalysisComplete(val analysisData: ScheduleAnalysisData) : ScheduleUiEvent()
    data class RecurrenceGenerated(val transactionId: Int, val nextTransactionId: Int) : ScheduleUiEvent()
    data class RecurrencePaused(val transactionId: Int) : ScheduleUiEvent()
    data class RecurrenceResumed(val transactionId: Int) : ScheduleUiEvent()
    data class RecurrenceStopped(val transactionId: Int) : ScheduleUiEvent()
}

// Data classes for analysis and settings
data class ScheduleAnalysisData(
    val totalScheduledAmount: Double,
    val upcomingAmount: Double,
    val overdueAmount: Double,
    val averageTransactionAmount: Double,
    val mostExpensiveTransaction: TransactionEntity?,
    val earliestUpcoming: TransactionEntity?,
    val latestOverdue: TransactionEntity?,
    val transactionsByType: Map<TransactionType, Int>,
    val transactionsByAccount: Map<String, Int>,
    val paymentTrends: Map<String, Double>, // Date -> Amount
    val overdueRisk: OverdueRisk,
    val recommendations: List<String>
)

data class OverdueRisk(
    val riskLevel: RiskLevel,
    val overdueCount: Int,
    val overdueAmount: Double,
    val longestOverdueDays: Int,
    val projectedOverdueNextWeek: Int
)

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

data class ReminderSettings(
    val enabledForUpcoming: Boolean = true,
    val enabledForOverdue: Boolean = true,
    val upcomingReminderDays: Int = 1, // Days before due date
    val overdueReminderDays: Int = 1, // Days after due date
    val reminderTime: String = "09:00", // HH:mm format
    val enablePushNotifications: Boolean = true,
    val enableEmailReminders: Boolean = false,
    val enableSmsReminders: Boolean = false,
    val snoozeOptions: List<Int> = listOf(1, 2, 4, 8, 24), // Hours
    val autoMarkAsPaidWhenDue: Boolean = false,
    val autoGenerateNextRecurrence: Boolean = true
)

// Extension functions for better event handling
fun ScheduleEvent.requiresNetworkOperation(): Boolean {
    return when (this) {
        is ScheduleEvent.LoadScheduledTransactions,
        is ScheduleEvent.RefreshScheduledTransactions,
        is ScheduleEvent.UpdateConversionRates -> true
        else -> false
    }
}

fun ScheduleEvent.requiresConfirmation(): Boolean {
    return when (this) {
        is ScheduleEvent.DeleteTransaction,
        is ScheduleEvent.DeleteScheduledTransactions,
        is ScheduleEvent.BulkDelete,
        is ScheduleEvent.StopRecurrence -> true
        else -> false
    }
}

fun ScheduleEvent.affectsDatabase(): Boolean {
    return when (this) {
        is ScheduleEvent.MarkAsPaid,
        is ScheduleEvent.MarkAsUnpaid,
        is ScheduleEvent.UpdateTransaction,
        is ScheduleEvent.DeleteTransaction,
        is ScheduleEvent.DeleteScheduledTransactions,
        is ScheduleEvent.BulkMarkAsPaid,
        is ScheduleEvent.BulkMarkAsUnpaid,
        is ScheduleEvent.BulkDelete,
        is ScheduleEvent.RescheduleTransaction,
        is ScheduleEvent.SnoozeTransaction -> true
        else -> false
    }
}

fun ScheduleEvent.isBulkOperation(): Boolean {
    return when (this) {
        is ScheduleEvent.BulkMarkAsPaid,
        is ScheduleEvent.BulkMarkAsUnpaid,
        is ScheduleEvent.BulkSnooze,
        is ScheduleEvent.BulkReschedule,
        is ScheduleEvent.BulkDelete,
        is ScheduleEvent.PayAll,
        is ScheduleEvent.SnoozeAll -> true
        else -> false
    }
}

fun ScheduleEvent.isQuickAction(): Boolean {
    return when (this) {
        is ScheduleEvent.PayNow,
        is ScheduleEvent.SkipPayment,
        is ScheduleEvent.PayAll,
        is ScheduleEvent.SnoozeAll -> true
        else -> false
    }
}

fun ScheduleEvent.affectsRecurrence(): Boolean {
    return when (this) {
        is ScheduleEvent.GenerateNextRecurrence,
        is ScheduleEvent.UpdateRecurrenceSettings,
        is ScheduleEvent.PauseRecurrence,
        is ScheduleEvent.ResumeRecurrence,
        is ScheduleEvent.StopRecurrence -> true
        else -> false
    }
}