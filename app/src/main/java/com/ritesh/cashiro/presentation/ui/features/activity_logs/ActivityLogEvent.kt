package com.ritesh.cashiro.presentation.ui.features.activity_logs

import com.ritesh.cashiro.data.local.entity.AccountEntity
import com.ritesh.cashiro.data.local.entity.TransactionEntity
import com.ritesh.cashiro.domain.utils.TimeRange

sealed class ActivityLogEvent {
    // Data Loading Events
    object LoadActivities : ActivityLogEvent()
    object RefreshActivities : ActivityLogEvent()

    // Filter Events
    data class UpdateFilterState(val filterState: ActivityLogFilterState) : ActivityLogEvent()
    data class UpdateActionTypeFilter(val actionTypes: Set<ActivityActionType>) : ActivityLogEvent()
    data class UpdateCategoryFilter(val categories: Set<ActivityCategory>) : ActivityLogEvent()
    data class UpdateAccountFilter(val accountIds: Set<Int>) : ActivityLogEvent()
    data class UpdateDateRangeFilter(val dateRange: ActivityDateRange?) : ActivityLogEvent()
    data class UpdateSearchFilter(val searchText: androidx.compose.ui.text.input.TextFieldValue) : ActivityLogEvent()
    data class UpdateSortBy(val sortBy: ActivityLogSortBy) : ActivityLogEvent()
    data class UpdateTimeFilter(val timeRange: TimeRange) : ActivityLogEvent()
    data class UpdateTodayFilter(val showOnlyToday: Boolean) : ActivityLogEvent()
    data class UpdateWeekFilter(val showOnlyThisWeek: Boolean) : ActivityLogEvent()
    data class UpdateMonthFilter(val showOnlyThisMonth: Boolean) : ActivityLogEvent()
    object ClearAllFilters : ActivityLogEvent()

    // Selection Events
    data class ToggleActivitySelection(val activityId: Int) : ActivityLogEvent()
    data class SelectActivity(val activityId: Int) : ActivityLogEvent()
    data class DeselectActivity(val activityId: Int) : ActivityLogEvent()
    object SelectAllActivities : ActivityLogEvent()
    object ClearSelection : ActivityLogEvent()
    data class SetSelectionMode(val isEnabled: Boolean) : ActivityLogEvent()

    // Activity Management Events
    data class DeleteActivity(val activityId: Int) : ActivityLogEvent()
    data class DeleteActivities(val activityIds: List<Int>) : ActivityLogEvent()
    object ClearAllActivities : ActivityLogEvent()
    data class AddActivity(val activity: ActivityLogEntry) : ActivityLogEvent()
    data class UpdateActivity(val activity: ActivityLogEntry) : ActivityLogEvent()

    // Bulk Operations Events
    data class BulkDeleteActivities(val activityIds: List<Int>) : ActivityLogEvent()
    data class DeleteActivitiesOlderThan(val days: Int) : ActivityLogEvent()
    data class DeleteActivitiesByType(val actionType: ActivityActionType) : ActivityLogEvent()
    data class DeleteActivitiesByCategory(val category: ActivityCategory) : ActivityLogEvent()

    // Navigation Events
    data class NavigateToRelatedTransaction(val transactionId: Int) : ActivityLogEvent()
    data class NavigateToRelatedAccount(val accountId: Int) : ActivityLogEvent()
    data class NavigateToRelatedCategory(val categoryId: Int) : ActivityLogEvent()

    // Analysis Events
    object AnalyzeActivityPatterns : ActivityLogEvent()
    data class GetMostActiveTimeOfDay(val days: Int = 30) : ActivityLogEvent()
    data class GetActivityTrends(val timeRange: TimeRange) : ActivityLogEvent()
    object GetActivityStatistics : ActivityLogEvent()

    // Export Events
    object ExportActivities : ActivityLogEvent()
    data class ExportActivitiesForPeriod(val timeRange: TimeRange) : ActivityLogEvent()
    data class ExportActivitiesByType(val actionType: ActivityActionType) : ActivityLogEvent()

    // Settings Events
    data class UpdateRetentionPeriod(val days: Int) : ActivityLogEvent()
    data class UpdateAutoCleanup(val enabled: Boolean) : ActivityLogEvent()
    data class UpdateActivityTracking(val enabled: Boolean) : ActivityLogEvent()
    data class UpdateDetailLevel(val level: ActivityDetailLevel) : ActivityLogEvent()

    // Error Handling Events
    data class ShowError(val message: String) : ActivityLogEvent()
    object ClearError : ActivityLogEvent()

    // UI State Events
    data class SetLoading(val isLoading: Boolean) : ActivityLogEvent()
    data class UpdateLastRefresh(val timestamp: Long = System.currentTimeMillis()) : ActivityLogEvent()

    // Auto-cleanup Events
    object PerformAutoCleanup : ActivityLogEvent()
    data class CleanupOldActivities(val cutoffDate: Long) : ActivityLogEvent()
    object OptimizeDatabase : ActivityLogEvent()

    // Grouping and Organization Events
    data class GroupByTimeRange(val timeRange: TimeRange) : ActivityLogEvent()
    data class GroupByActionType(val enabled: Boolean) : ActivityLogEvent()
    data class GroupByAccount(val enabled: Boolean) : ActivityLogEvent()

    // Real-time Events (for when activities are created from other parts of the app)
    data class OnTransactionCreated(val transaction: TransactionEntity, val account: AccountEntity?) : ActivityLogEvent()
    data class OnTransactionUpdated(val oldTransaction: TransactionEntity, val newTransaction: TransactionEntity) : ActivityLogEvent()
    data class OnTransactionDeleted(val transaction: TransactionEntity, val account: AccountEntity?) : ActivityLogEvent()
    data class OnAccountCreated(val account: AccountEntity) : ActivityLogEvent()
    data class OnAccountUpdated(val oldAccount: AccountEntity, val newAccount: AccountEntity) : ActivityLogEvent()
    data class OnAccountDeleted(val account: AccountEntity) : ActivityLogEvent()
    data class OnCategoryCreated(val categoryName: String, val categoryId: Int) : ActivityLogEvent()
    data class OnCategoryUpdated(val categoryName: String, val categoryId: Int) : ActivityLogEvent()
    data class OnCategoryDeleted(val categoryName: String, val categoryId: Int) : ActivityLogEvent()
    data class OnSystemAction(val actionType: ActivityActionType, val description: String = "") : ActivityLogEvent()
}

// UI Events that are emitted back to the UI
sealed class ActivityLogUiEvent {
    object ActivitiesLoaded : ActivityLogUiEvent()
    object ActivitiesRefreshed : ActivityLogUiEvent()
    data class ActivityDeleted(val activityId: Int) : ActivityLogUiEvent()
    data class ActivitiesDeleted(val count: Int) : ActivityLogUiEvent()
    object AllActivitiesCleared : ActivityLogUiEvent()
    data class ActivityAdded(val activity: ActivityLogEntry) : ActivityLogUiEvent()
    data class ActivityUpdated(val activity: ActivityLogEntry) : ActivityLogUiEvent()
    data class BulkOperationCompleted(val operation: String, val count: Int) : ActivityLogUiEvent()
    data class NavigateToScreen(val destination: String, val args: Map<String, Any> = emptyMap()) : ActivityLogUiEvent()
    data class ShowMessage(val message: String, val isError: Boolean = false) : ActivityLogUiEvent()
    data class ShowSnackbar(val message: String, val actionLabel: String? = null, val onAction: (() -> Unit)? = null) : ActivityLogUiEvent()
    data class ShowConfirmationDialog(val title: String, val message: String, val onConfirm: () -> Unit) : ActivityLogUiEvent()
    object ExportCompleted : ActivityLogUiEvent()
    data class AnalysisComplete(val analysisData: ActivityAnalysisData) : ActivityLogUiEvent()
    data class StatisticsCalculated(val statistics: ActivityStatistics) : ActivityLogUiEvent()
    object CleanupCompleted : ActivityLogUiEvent()
    object DatabaseOptimized : ActivityLogUiEvent()
}

// Data classes for analysis and statistics
data class ActivityAnalysisData(
    val totalActivities: Int,
    val activitiesThisWeek: Int,
    val activitiesThisMonth: Int,
    val mostActiveTimeOfDay: String,
    val mostCommonActionType: ActivityActionType,
    val averageActivitiesPerDay: Double,
    val activityTrends: Map<String, Int>, // Date -> Count
    val actionTypeDistribution: Map<ActivityActionType, Int>,
    val accountActivityDistribution: Map<String, Int>, // Account name -> Count
    val categoryActivityDistribution: Map<ActivityCategory, Int>,
    val peakActivityDays: List<String>,
    val insights: List<String>
)

data class ActivityStatistics(
    val totalActivities: Int,
    val todayActivities: Int,
    val weekActivities: Int,
    val monthActivities: Int,
    val averageDailyActivities: Double,
    val mostActiveDay: String,
    val leastActiveDay: String,
    val transactionActivities: Int,
    val accountActivities: Int,
    val categoryActivities: Int,
    val systemActivities: Int,
    val subscriptionActivities: Int,
    val oldestActivity: ActivityLogEntry?,
    val newestActivity: ActivityLogEntry?,
    val mostCommonActionType: ActivityActionType?,
    val leastCommonActionType: ActivityActionType?
)

enum class ActivityDetailLevel {
    MINIMAL,    // Only essential actions
    STANDARD,   // Most actions with basic details
    DETAILED,   // All actions with full details
    VERBOSE     // Everything including debug info
}

// Extension functions for better event handling
fun ActivityLogEvent.requiresNetworkOperation(): Boolean {
    return when (this) {
        is ActivityLogEvent.LoadActivities,
        is ActivityLogEvent.RefreshActivities,
        is ActivityLogEvent.ExportActivities -> true
        else -> false
    }
}

fun ActivityLogEvent.requiresConfirmation(): Boolean {
    return when (this) {
        is ActivityLogEvent.DeleteActivity,
        is ActivityLogEvent.DeleteActivities,
        is ActivityLogEvent.ClearAllActivities,
        is ActivityLogEvent.BulkDeleteActivities,
        is ActivityLogEvent.DeleteActivitiesOlderThan,
        is ActivityLogEvent.DeleteActivitiesByType,
        is ActivityLogEvent.DeleteActivitiesByCategory -> true
        else -> false
    }
}

fun ActivityLogEvent.affectsDatabase(): Boolean {
    return when (this) {
        is ActivityLogEvent.DeleteActivity,
        is ActivityLogEvent.DeleteActivities,
        is ActivityLogEvent.ClearAllActivities,
        is ActivityLogEvent.AddActivity,
        is ActivityLogEvent.UpdateActivity,
        is ActivityLogEvent.BulkDeleteActivities,
        is ActivityLogEvent.PerformAutoCleanup,
        is ActivityLogEvent.CleanupOldActivities,
        is ActivityLogEvent.OptimizeDatabase -> true
        else -> false
    }
}

fun ActivityLogEvent.isBulkOperation(): Boolean {
    return when (this) {
        is ActivityLogEvent.BulkDeleteActivities,
        is ActivityLogEvent.DeleteActivitiesOlderThan,
        is ActivityLogEvent.DeleteActivitiesByType,
        is ActivityLogEvent.DeleteActivitiesByCategory,
        is ActivityLogEvent.ClearAllActivities -> true
        else -> false
    }
}

fun ActivityLogEvent.isRealTimeEvent(): Boolean {
    return when (this) {
        is ActivityLogEvent.OnTransactionCreated,
        is ActivityLogEvent.OnTransactionUpdated,
        is ActivityLogEvent.OnTransactionDeleted,
        is ActivityLogEvent.OnAccountCreated,
        is ActivityLogEvent.OnAccountUpdated,
        is ActivityLogEvent.OnAccountDeleted,
        is ActivityLogEvent.OnCategoryCreated,
        is ActivityLogEvent.OnCategoryUpdated,
        is ActivityLogEvent.OnCategoryDeleted,
        is ActivityLogEvent.OnSystemAction -> true
        else -> false
    }
}

fun ActivityLogEvent.isFilterEvent(): Boolean {
    return when (this) {
        is ActivityLogEvent.UpdateFilterState,
        is ActivityLogEvent.UpdateActionTypeFilter,
        is ActivityLogEvent.UpdateCategoryFilter,
        is ActivityLogEvent.UpdateAccountFilter,
        is ActivityLogEvent.UpdateDateRangeFilter,
        is ActivityLogEvent.UpdateSearchFilter,
        is ActivityLogEvent.UpdateSortBy,
        is ActivityLogEvent.UpdateTimeFilter,
        is ActivityLogEvent.UpdateTodayFilter,
        is ActivityLogEvent.UpdateWeekFilter,
        is ActivityLogEvent.UpdateMonthFilter,
        is ActivityLogEvent.ClearAllFilters -> true
        else -> false
    }
}

fun ActivityLogEvent.isAnalysisEvent(): Boolean {
    return when (this) {
        is ActivityLogEvent.AnalyzeActivityPatterns,
        is ActivityLogEvent.GetMostActiveTimeOfDay,
        is ActivityLogEvent.GetActivityTrends,
        is ActivityLogEvent.GetActivityStatistics -> true
        else -> false
    }
}