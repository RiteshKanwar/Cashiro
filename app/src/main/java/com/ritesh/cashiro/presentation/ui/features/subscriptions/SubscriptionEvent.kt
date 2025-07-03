package com.ritesh.cashiro.presentation.ui.features.subscriptions

import com.ritesh.cashiro.data.local.entity.TransactionEntity
import com.ritesh.cashiro.data.local.entity.TransactionType

sealed class SubscriptionEvent {
    // Data Loading Events
    object LoadSubscriptions : SubscriptionEvent()
    object RefreshSubscriptions : SubscriptionEvent()

    // Filter Events
    data class UpdatePeriodFilter(val period: SubscriptionPeriod) : SubscriptionEvent()
    data class UpdateFilterState(val filterState: SubscriptionFilterState) : SubscriptionEvent()
    data class UpdateAccountFilter(val accountIds: Set<Int>) : SubscriptionEvent()
    data class UpdatePaymentStatusFilter(val statuses: Set<String>) : SubscriptionEvent()
    data class UpdateAmountFilter(val minAmount: Double, val maxAmount: Double) : SubscriptionEvent()
    data class UpdateSearchFilter(val searchText: androidx.compose.ui.text.input.TextFieldValue) : SubscriptionEvent()
    data class UpdateSortBy(val sortBy: SubscriptionSortBy) : SubscriptionEvent()
    data class UpdateActiveFilter(val showActiveOnly: Boolean) : SubscriptionEvent()
    data class UpdateExpiredFilter(val showExpiredOnly: Boolean) : SubscriptionEvent()
    object ClearAllFilters : SubscriptionEvent()

    // Selection Events
    data class ToggleSubscriptionSelection(val subscriptionId: Int) : SubscriptionEvent()
    data class SelectSubscription(val subscriptionId: Int) : SubscriptionEvent()
    data class DeselectSubscription(val subscriptionId: Int) : SubscriptionEvent()
    object SelectAllSubscriptions : SubscriptionEvent()
    object ClearSelection : SubscriptionEvent()
    data class SetSelectionMode(val isEnabled: Boolean) : SubscriptionEvent()

    // Subscription Management Events
    data class MarkAsPaid(val subscription: TransactionEntity) : SubscriptionEvent()
    data class MarkAsUnpaid(val subscription: TransactionEntity) : SubscriptionEvent()
    data class PauseSubscription(val subscriptionId: Int) : SubscriptionEvent()
    data class ResumeSubscription(val subscriptionId: Int) : SubscriptionEvent()
    data class CancelSubscription(val subscriptionId: Int) : SubscriptionEvent()
    data class UpdateSubscription(val subscription: TransactionEntity) : SubscriptionEvent()
    data class DeleteSubscription(val subscriptionId: Int) : SubscriptionEvent()
    data class DeleteSubscriptions(val subscriptionIds: List<Int>) : SubscriptionEvent()

    // Navigation Events
    data class NavigateToAddSubscription(val defaultType: TransactionType = TransactionType.SUBSCRIPTION) : SubscriptionEvent()
    data class NavigateToEditSubscription(val subscriptionId: Int) : SubscriptionEvent()
    data class NavigateToSubscriptionDetails(val subscriptionId: Int) : SubscriptionEvent()

    // Calculation Events
    object RecalculateTotals : SubscriptionEvent()
    data class UpdateConversionRates(val rates: Map<String, Double>) : SubscriptionEvent()

    // Error Handling Events
    data class ShowError(val message: String) : SubscriptionEvent()
    object ClearError : SubscriptionEvent()

    // UI State Events
    data class SetLoading(val isLoading: Boolean) : SubscriptionEvent()
    data class UpdateLastRefresh(val timestamp: Long = System.currentTimeMillis()) : SubscriptionEvent()

    // Bulk Operations Events
    data class BulkMarkAsPaid(val subscriptionIds: List<Int>) : SubscriptionEvent()
    data class BulkMarkAsUnpaid(val subscriptionIds: List<Int>) : SubscriptionEvent()
    data class BulkPauseSubscriptions(val subscriptionIds: List<Int>) : SubscriptionEvent()
    data class BulkResumeSubscriptions(val subscriptionIds: List<Int>) : SubscriptionEvent()
    data class BulkCancelSubscriptions(val subscriptionIds: List<Int>) : SubscriptionEvent()

    // Subscription Analysis Events
    object AnalyzeSpending : SubscriptionEvent()
    data class GetUpcomingPayments(val days: Int = 30) : SubscriptionEvent()
    data class GetExpiredSubscriptions(val days: Int = 30) : SubscriptionEvent()
    object GetMostExpensiveSubscriptions : SubscriptionEvent()
    object GetLeastUsedSubscriptions : SubscriptionEvent()

    // Export Events
    object ExportSubscriptions : SubscriptionEvent()
    data class ExportSubscriptionsForPeriod(val period: SubscriptionPeriod) : SubscriptionEvent()

    // Notification Events
    data class SchedulePaymentReminder(val subscriptionId: Int, val reminderTime: Long) : SubscriptionEvent()
    data class CancelPaymentReminder(val subscriptionId: Int) : SubscriptionEvent()
    object ScheduleAllReminders : SubscriptionEvent()
    object CancelAllReminders : SubscriptionEvent()
}

// UI Events that are emitted back to the UI
sealed class SubscriptionUiEvent {
    object SubscriptionsLoaded : SubscriptionUiEvent()
    object SubscriptionsRefreshed : SubscriptionUiEvent()
    data class SubscriptionMarkedAsPaid(val subscriptionId: Int) : SubscriptionUiEvent()
    data class SubscriptionMarkedAsUnpaid(val subscriptionId: Int) : SubscriptionUiEvent()
    data class SubscriptionPaused(val subscriptionId: Int) : SubscriptionUiEvent()
    data class SubscriptionResumed(val subscriptionId: Int) : SubscriptionUiEvent()
    data class SubscriptionCancelled(val subscriptionId: Int) : SubscriptionUiEvent()
    data class SubscriptionUpdated(val subscriptionId: Int) : SubscriptionUiEvent()
    data class SubscriptionDeleted(val subscriptionId: Int) : SubscriptionUiEvent()
    data class SubscriptionsDeleted(val count: Int) : SubscriptionUiEvent()
    data class BulkOperationCompleted(val operation: String, val count: Int) : SubscriptionUiEvent()
    data class NavigateToScreen(val destination: String, val args: Map<String, Any> = emptyMap()) : SubscriptionUiEvent()
    data class ShowMessage(val message: String, val isError: Boolean = false) : SubscriptionUiEvent()
    data class ShowConfirmationDialog(val title: String, val message: String, val onConfirm: () -> Unit) : SubscriptionUiEvent()
    object ExportCompleted : SubscriptionUiEvent()
    data class ReminderScheduled(val subscriptionId: Int) : SubscriptionUiEvent()
    data class ReminderCancelled(val subscriptionId: Int) : SubscriptionUiEvent()
    data class AnalysisComplete(val analysisData: SubscriptionAnalysisData) : SubscriptionUiEvent()
}

// Data classes for analysis
data class SubscriptionAnalysisData(
    val totalMonthlySpending: Double,
    val totalYearlySpending: Double,
    val averageSubscriptionCost: Double,
    val mostExpensiveSubscription: TransactionEntity?,
    val leastExpensiveSubscription: TransactionEntity?,
    val upcomingPayments: List<TransactionEntity>,
    val expiredSubscriptions: List<TransactionEntity>,
    val paymentsByCategory: Map<String, Double>,
    val paymentTrends: Map<String, Double>, // Month -> Amount
    val recommendedActions: List<String>
)

// Extension functions for better event handling
fun SubscriptionEvent.requiresNetworkOperation(): Boolean {
    return when (this) {
        is SubscriptionEvent.LoadSubscriptions,
        is SubscriptionEvent.RefreshSubscriptions,
        is SubscriptionEvent.UpdateConversionRates -> true
        else -> false
    }
}

fun SubscriptionEvent.requiresConfirmation(): Boolean {
    return when (this) {
        is SubscriptionEvent.DeleteSubscription,
        is SubscriptionEvent.DeleteSubscriptions,
        is SubscriptionEvent.CancelSubscription,
        is SubscriptionEvent.BulkCancelSubscriptions -> true
        else -> false
    }
}

fun SubscriptionEvent.affectsDatabase(): Boolean {
    return when (this) {
        is SubscriptionEvent.MarkAsPaid,
        is SubscriptionEvent.MarkAsUnpaid,
        is SubscriptionEvent.UpdateSubscription,
        is SubscriptionEvent.DeleteSubscription,
        is SubscriptionEvent.DeleteSubscriptions,
        is SubscriptionEvent.BulkMarkAsPaid,
        is SubscriptionEvent.BulkMarkAsUnpaid -> true
        else -> false
    }
}

fun SubscriptionEvent.isBulkOperation(): Boolean {
    return when (this) {
        is SubscriptionEvent.BulkMarkAsPaid,
        is SubscriptionEvent.BulkMarkAsUnpaid,
        is SubscriptionEvent.BulkPauseSubscriptions,
        is SubscriptionEvent.BulkResumeSubscriptions,
        is SubscriptionEvent.BulkCancelSubscriptions,
        is SubscriptionEvent.DeleteSubscriptions -> true
        else -> false
    }
}