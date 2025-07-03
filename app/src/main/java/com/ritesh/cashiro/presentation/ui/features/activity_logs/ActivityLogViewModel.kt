package com.ritesh.cashiro.presentation.ui.features.activity_logs

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ritesh.cashiro.data.local.entity.AccountEntity
import com.ritesh.cashiro.data.local.entity.TransactionEntity
import com.ritesh.cashiro.domain.repository.ActivityLogRepository
import com.ritesh.cashiro.domain.utils.AccountEvent
import com.ritesh.cashiro.domain.utils.ActivityLogEntryFactory
import com.ritesh.cashiro.domain.utils.ActivityLogFilterUtils
import com.ritesh.cashiro.domain.utils.AppEventBus
import com.ritesh.cashiro.domain.utils.TimeRange
import com.ritesh.cashiro.domain.utils.TransactionEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ActivityLogViewModel @Inject constructor(
    private val activityLogRepository: ActivityLogRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(ActivityLogScreenState())
    val state: StateFlow<ActivityLogScreenState> = _state.asStateFlow()

    private val _uiEvents = MutableSharedFlow<ActivityLogUiEvent>()
    val uiEvents: SharedFlow<ActivityLogUiEvent> = _uiEvents.asSharedFlow()

    // Settings for activity log
    private var retentionPeriodDays = 90 // Default 90 days
    private var autoCleanupEnabled = true
    private var activityTrackingEnabled = true
    private var detailLevel = ActivityDetailLevel.STANDARD

    init {
        // Load activities on initialization
        onEvent(ActivityLogEvent.LoadActivities)

        // Listen for app-wide events to create activity log entries
        viewModelScope.launch {
            AppEventBus.events.collect { event ->
                when (event) {
                    is TransactionEvent.TransactionAdded -> {
                        // Auto-create activity log entry for new transactions
                        // This would be handled by the repository layer
                    }
                    is TransactionEvent.TransactionsUpdated -> {
                        // Refresh activities if needed
                        onEvent(ActivityLogEvent.RefreshActivities)
                    }
                    is AccountEvent.AccountsUpdated -> {
                        // Refresh activities to get updated account names
                        onEvent(ActivityLogEvent.RefreshActivities)
                    }
                }
            }
        }

        // Set up auto-cleanup if enabled
        if (autoCleanupEnabled) {
            viewModelScope.launch {
                // Run cleanup daily
                while (true) {
                    delay(24 * 60 * 60 * 1000) // 24 hours
                    onEvent(ActivityLogEvent.PerformAutoCleanup)
                }
            }
        }
    }

    fun onEvent(event: ActivityLogEvent) {
        when (event) {
            // Data Loading Events
            is ActivityLogEvent.LoadActivities -> loadActivities()
            is ActivityLogEvent.RefreshActivities -> refreshActivities()

            // Filter Events
            is ActivityLogEvent.UpdateFilterState -> updateFilterState(event.filterState)
            is ActivityLogEvent.UpdateActionTypeFilter -> updateActionTypeFilter(event.actionTypes)
            is ActivityLogEvent.UpdateCategoryFilter -> updateCategoryFilter(event.categories)
            is ActivityLogEvent.UpdateAccountFilter -> updateAccountFilter(event.accountIds)
            is ActivityLogEvent.UpdateDateRangeFilter -> updateDateRangeFilter(event.dateRange)
            is ActivityLogEvent.UpdateSearchFilter -> updateSearchFilter(event.searchText)
            is ActivityLogEvent.UpdateSortBy -> updateSortBy(event.sortBy)
            is ActivityLogEvent.UpdateTimeFilter -> updateTimeFilter(event.timeRange)
            is ActivityLogEvent.UpdateTodayFilter -> updateTodayFilter(event.showOnlyToday)
            is ActivityLogEvent.UpdateWeekFilter -> updateWeekFilter(event.showOnlyThisWeek)
            is ActivityLogEvent.UpdateMonthFilter -> updateMonthFilter(event.showOnlyThisMonth)
            is ActivityLogEvent.ClearAllFilters -> clearAllFilters()

            // Selection Events
            is ActivityLogEvent.ToggleActivitySelection -> toggleActivitySelection(event.activityId)
            is ActivityLogEvent.SelectActivity -> selectActivity(event.activityId)
            is ActivityLogEvent.DeselectActivity -> deselectActivity(event.activityId)
            is ActivityLogEvent.SelectAllActivities -> selectAllActivities()
            is ActivityLogEvent.ClearSelection -> clearSelection()
            is ActivityLogEvent.SetSelectionMode -> setSelectionMode(event.isEnabled)

            // Activity Management Events
            is ActivityLogEvent.DeleteActivity -> deleteActivity(event.activityId)
            is ActivityLogEvent.DeleteActivities -> deleteActivities(event.activityIds)
            is ActivityLogEvent.ClearAllActivities -> clearAllActivities()
            is ActivityLogEvent.AddActivity -> addActivity(event.activity)
            is ActivityLogEvent.UpdateActivity -> updateActivity(event.activity)

            // Bulk Operations Events
            is ActivityLogEvent.BulkDeleteActivities -> bulkDeleteActivities(event.activityIds)
            is ActivityLogEvent.DeleteActivitiesOlderThan -> deleteActivitiesOlderThan(event.days)
            is ActivityLogEvent.DeleteActivitiesByType -> deleteActivitiesByType(event.actionType)
            is ActivityLogEvent.DeleteActivitiesByCategory -> deleteActivitiesByCategory(event.category)

            // Analysis Events
            is ActivityLogEvent.AnalyzeActivityPatterns -> analyzeActivityPatterns()
            is ActivityLogEvent.GetMostActiveTimeOfDay -> getMostActiveTimeOfDay(event.days)
            is ActivityLogEvent.GetActivityTrends -> getActivityTrends(event.timeRange)
            is ActivityLogEvent.GetActivityStatistics -> getActivityStatistics()

            // Export Events
            is ActivityLogEvent.ExportActivities -> exportActivities()
            is ActivityLogEvent.ExportActivitiesForPeriod -> exportActivitiesForPeriod(event.timeRange)
            is ActivityLogEvent.ExportActivitiesByType -> exportActivitiesByType(event.actionType)

            // Settings Events
            is ActivityLogEvent.UpdateRetentionPeriod -> updateRetentionPeriod(event.days)
            is ActivityLogEvent.UpdateAutoCleanup -> updateAutoCleanup(event.enabled)
            is ActivityLogEvent.UpdateActivityTracking -> updateActivityTracking(event.enabled)
            is ActivityLogEvent.UpdateDetailLevel -> updateDetailLevel(event.level)

            // Auto-cleanup Events
            is ActivityLogEvent.PerformAutoCleanup -> performAutoCleanup()
            is ActivityLogEvent.CleanupOldActivities -> cleanupOldActivities(event.cutoffDate)
            is ActivityLogEvent.OptimizeDatabase -> optimizeDatabase()

            // Real-time Events
            is ActivityLogEvent.OnTransactionCreated -> onTransactionCreated(event.transaction, event.account)
            is ActivityLogEvent.OnTransactionUpdated -> onTransactionUpdated(event.oldTransaction, event.newTransaction)
            is ActivityLogEvent.OnTransactionDeleted -> onTransactionDeleted(event.transaction, event.account)
            is ActivityLogEvent.OnAccountCreated -> onAccountCreated(event.account)
            is ActivityLogEvent.OnAccountUpdated -> onAccountUpdated(event.oldAccount, event.newAccount)
            is ActivityLogEvent.OnAccountDeleted -> onAccountDeleted(event.account)
            is ActivityLogEvent.OnCategoryCreated -> onCategoryCreated(event.categoryName, event.categoryId)
            is ActivityLogEvent.OnCategoryUpdated -> onCategoryUpdated(event.categoryName, event.categoryId)
            is ActivityLogEvent.OnCategoryDeleted -> onCategoryDeleted(event.categoryName, event.categoryId)
            is ActivityLogEvent.OnSystemAction -> onSystemAction(event.actionType, event.description)

            // Navigation Events
            is ActivityLogEvent.NavigateToRelatedTransaction -> navigateToRelatedTransaction(event.transactionId)
            is ActivityLogEvent.NavigateToRelatedAccount -> navigateToRelatedAccount(event.accountId)

            // Error Handling Events
            is ActivityLogEvent.ShowError -> showError(event.message)
            is ActivityLogEvent.ClearError -> clearError()

            // UI State Events
            is ActivityLogEvent.SetLoading -> setLoading(event.isLoading)
            is ActivityLogEvent.UpdateLastRefresh -> updateLastRefresh(event.timestamp)

            // Unhandled events - log for debugging
            else -> {
                Log.d("ActivityLogViewModel", "Unhandled event: ${event::class.simpleName}")
            }
        }
    }

    private fun updateState(update: (ActivityLogScreenState) -> ActivityLogScreenState) {
        _state.update(update)
    }

    private fun emitUiEvent(event: ActivityLogUiEvent) {
        viewModelScope.launch {
            _uiEvents.emit(event)
        }
    }

    private fun loadActivities() {
        viewModelScope.launch {
            try {
                updateState { it.copy(isLoading = true, error = null) }

                val activities = activityLogRepository.getAllActivities()

                Log.d("ActivityLogViewModel", "Loaded ${activities.size} activities")

                updateState { currentState ->
                    currentState.copy(
                        activities = activities,
                        totalActivities = activities.size,
                        todayActivities = currentState.getActivitiesForToday().size,
                        weekActivities = currentState.getActivitiesForWeek().size,
                        isLoading = false,
                        lastUpdated = System.currentTimeMillis()
                    )
                }

                emitUiEvent(ActivityLogUiEvent.ActivitiesLoaded)

            } catch (e: Exception) {
                Log.e("ActivityLogViewModel", "Error loading activities: ${e.message}", e)
                updateState { it.copy(isLoading = false, error = "Failed to load activities: ${e.message}") }
                emitUiEvent(ActivityLogUiEvent.ShowMessage("Failed to load activities", true))
            }
        }
    }

    private fun refreshActivities() {
        viewModelScope.launch {
            try {
                val activities = activityLogRepository.getAllActivities()

                updateState { currentState ->
                    currentState.copy(
                        activities = activities,
                        totalActivities = activities.size,
                        todayActivities = currentState.getActivitiesForToday().size,
                        weekActivities = currentState.getActivitiesForWeek().size,
                        lastUpdated = System.currentTimeMillis()
                    )
                }

                emitUiEvent(ActivityLogUiEvent.ActivitiesRefreshed)

            } catch (e: Exception) {
                Log.e("ActivityLogViewModel", "Error refreshing activities: ${e.message}", e)
                showError("Failed to refresh activities: ${e.message}")
            }
        }
    }

    private fun updateFilterState(filterState: ActivityLogFilterState) {
        updateState { it.copy(filterState = filterState) }
    }

    private fun updateActionTypeFilter(actionTypes: Set<ActivityActionType>) {
        updateState { currentState ->
            currentState.copy(
                filterState = currentState.filterState.copy(selectedActionTypes = actionTypes)
            )
        }
    }

    private fun updateCategoryFilter(categories: Set<ActivityCategory>) {
        updateState { currentState ->
            currentState.copy(
                filterState = currentState.filterState.copy(selectedCategories = categories)
            )
        }
    }

    private fun updateAccountFilter(accountIds: Set<Int>) {
        updateState { currentState ->
            currentState.copy(
                filterState = currentState.filterState.copy(selectedAccounts = accountIds)
            )
        }
    }

    private fun updateDateRangeFilter(dateRange: ActivityDateRange?) {
        updateState { currentState ->
            currentState.copy(
                filterState = currentState.filterState.copy(dateRange = dateRange)
            )
        }
    }

    private fun updateSearchFilter(searchText: androidx.compose.ui.text.input.TextFieldValue) {
        updateState { currentState ->
            currentState.copy(
                filterState = currentState.filterState.copy(searchText = searchText)
            )
        }
    }

    private fun updateSortBy(sortBy: ActivityLogSortBy) {
        updateState { currentState ->
            currentState.copy(
                filterState = currentState.filterState.copy(sortBy = sortBy)
            )
        }
    }

    private fun updateTimeFilter(timeRange: TimeRange) {
        val currentTime = System.currentTimeMillis()
        val dateRange = when (timeRange) {
            TimeRange.TODAY -> {
                val startOfDay = currentTime - (currentTime % (24 * 60 * 60 * 1000))
                ActivityDateRange(startOfDay, currentTime)
            }
            TimeRange.YESTERDAY -> {
                val startOfYesterday = currentTime - (24 * 60 * 60 * 1000)
                val startOfYesterdayDay = startOfYesterday - (startOfYesterday % (24 * 60 * 60 * 1000))
                val endOfYesterday = startOfYesterdayDay + (24 * 60 * 60 * 1000) - 1
                ActivityDateRange(startOfYesterdayDay, endOfYesterday)
            }
            TimeRange.THIS_WEEK -> {
                val weekAgo = currentTime - (7 * 24 * 60 * 60 * 1000)
                ActivityDateRange(weekAgo, currentTime)
            }
            TimeRange.THIS_MONTH -> {
                val monthAgo = currentTime - (30 * 24 * 60 * 60 * 1000)
                ActivityDateRange(monthAgo, currentTime)
            }
            TimeRange.ALL_TIME -> null
        }

        updateState { currentState ->
            currentState.copy(
                filterState = currentState.filterState.copy(
                    dateRange = dateRange,
                    showOnlyToday = timeRange == TimeRange.TODAY,
                    showOnlyThisWeek = timeRange == TimeRange.THIS_WEEK,
                    showOnlyThisMonth = timeRange == TimeRange.THIS_MONTH
                )
            )
        }
    }

    private fun updateTodayFilter(showOnlyToday: Boolean) {
        updateState { currentState ->
            currentState.copy(
                filterState = currentState.filterState.copy(showOnlyToday = showOnlyToday)
            )
        }
    }

    private fun updateWeekFilter(showOnlyThisWeek: Boolean) {
        updateState { currentState ->
            currentState.copy(
                filterState = currentState.filterState.copy(showOnlyThisWeek = showOnlyThisWeek)
            )
        }
    }

    private fun updateMonthFilter(showOnlyThisMonth: Boolean) {
        updateState { currentState ->
            currentState.copy(
                filterState = currentState.filterState.copy(showOnlyThisMonth = showOnlyThisMonth)
            )
        }
    }

    private fun clearAllFilters() {
        updateState { currentState ->
            currentState.copy(filterState = ActivityLogFilterState())
        }
    }

    private fun toggleActivitySelection(activityId: Int) {
        updateState { currentState ->
            val selectedActivities = currentState.selectedActivities
            val newSelection = if (selectedActivities.contains(activityId)) {
                selectedActivities - activityId
            } else {
                selectedActivities + activityId
            }

            currentState.copy(
                selectedActivities = newSelection,
                isInSelectionMode = newSelection.isNotEmpty()
            )
        }
    }

    private fun selectActivity(activityId: Int) {
        updateState { currentState ->
            currentState.copy(
                selectedActivities = currentState.selectedActivities + activityId,
                isInSelectionMode = true
            )
        }
    }

    private fun deselectActivity(activityId: Int) {
        updateState { currentState ->
            val newSelection = currentState.selectedActivities - activityId
            currentState.copy(
                selectedActivities = newSelection,
                isInSelectionMode = newSelection.isNotEmpty()
            )
        }
    }

    private fun selectAllActivities() {
        updateState { currentState ->
            val allIds = currentState.filteredActivities.map { it.id }.toSet()
            currentState.copy(
                selectedActivities = allIds,
                isInSelectionMode = allIds.isNotEmpty()
            )
        }
    }

    private fun clearSelection() {
        updateState { currentState ->
            currentState.copy(
                selectedActivities = emptySet(),
                isInSelectionMode = false
            )
        }
    }

    private fun setSelectionMode(isEnabled: Boolean) {
        updateState { currentState ->
            currentState.copy(
                isInSelectionMode = isEnabled,
                selectedActivities = if (!isEnabled) emptySet() else currentState.selectedActivities
            )
        }
    }

    private fun deleteActivity(activityId: Int) {
        viewModelScope.launch {
            try {
                activityLogRepository.deleteActivity(activityId)
                refreshActivities()
                emitUiEvent(ActivityLogUiEvent.ActivityDeleted(activityId))

            } catch (e: Exception) {
                Log.e("ActivityLogViewModel", "Error deleting activity: ${e.message}", e)
                showError("Failed to delete activity: ${e.message}")
            }
        }
    }

    private fun deleteActivities(activityIds: List<Int>) {
        viewModelScope.launch {
            try {
                var deletedCount = 0

                activityIds.forEach { id ->
                    try {
                        activityLogRepository.deleteActivity(id)
                        deletedCount++
                    } catch (e: Exception) {
                        Log.w("ActivityLogViewModel", "Failed to delete activity $id: ${e.message}")
                    }
                }

                clearSelection()
                refreshActivities()
                emitUiEvent(ActivityLogUiEvent.ActivitiesDeleted(deletedCount))

            } catch (e: Exception) {
                Log.e("ActivityLogViewModel", "Error deleting activities: ${e.message}", e)
                showError("Failed to delete activities: ${e.message}")
            }
        }
    }

    private fun clearAllActivities() {
        viewModelScope.launch {
            try {
                val deletedCount = activityLogRepository.deleteAllActivities()
                clearSelection()
                refreshActivities()
                emitUiEvent(ActivityLogUiEvent.AllActivitiesCleared)
                emitUiEvent(ActivityLogUiEvent.ShowMessage("Cleared $deletedCount activities"))

            } catch (e: Exception) {
                Log.e("ActivityLogViewModel", "Error clearing all activities: ${e.message}", e)
                showError("Failed to clear all activities: ${e.message}")
            }
        }
    }

    private fun addActivity(activity: ActivityLogEntry) {
        viewModelScope.launch {
            try {
                if (activityTrackingEnabled) {
                    activityLogRepository.addActivity(activity)
                    refreshActivities()
                    emitUiEvent(ActivityLogUiEvent.ActivityAdded(activity))
                }

            } catch (e: Exception) {
                Log.e("ActivityLogViewModel", "Error adding activity: ${e.message}", e)
                showError("Failed to add activity: ${e.message}")
            }
        }
    }

    private fun updateActivity(activity: ActivityLogEntry) {
        viewModelScope.launch {
            try {
                activityLogRepository.updateActivity(activity)
                refreshActivities()
                emitUiEvent(ActivityLogUiEvent.ActivityUpdated(activity))

            } catch (e: Exception) {
                Log.e("ActivityLogViewModel", "Error updating activity: ${e.message}", e)
                showError("Failed to update activity: ${e.message}")
            }
        }
    }

    private fun bulkDeleteActivities(activityIds: List<Int>) {
        deleteActivities(activityIds)
    }

    private fun deleteActivitiesOlderThan(days: Int) {
        viewModelScope.launch {
            try {
                val cutoffDate = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
                val deletedCount = activityLogRepository.deleteActivitiesOlderThan(cutoffDate)

                refreshActivities()
                emitUiEvent(ActivityLogUiEvent.BulkOperationCompleted("Delete Old Activities", deletedCount))
                emitUiEvent(ActivityLogUiEvent.ShowMessage("Deleted $deletedCount old activities"))

            } catch (e: Exception) {
                Log.e("ActivityLogViewModel", "Error deleting old activities: ${e.message}", e)
                showError("Failed to delete old activities: ${e.message}")
            }
        }
    }

    private fun deleteActivitiesByType(actionType: ActivityActionType) {
        viewModelScope.launch {
            try {
                val deletedCount = activityLogRepository.deleteActivitiesByType(actionType)

                refreshActivities()
                emitUiEvent(ActivityLogUiEvent.BulkOperationCompleted("Delete by Type", deletedCount))
                emitUiEvent(ActivityLogUiEvent.ShowMessage("Deleted $deletedCount ${actionType.getDisplayName()} activities"))

            } catch (e: Exception) {
                Log.e("ActivityLogViewModel", "Error deleting activities by type: ${e.message}", e)
                showError("Failed to delete activities by type: ${e.message}")
            }
        }
    }

    private fun deleteActivitiesByCategory(category: ActivityCategory) {
        viewModelScope.launch {
            try {
                val actionTypes = ActivityActionType.values().filter { it.getCategory() == category }
                var totalDeleted = 0

                actionTypes.forEach { actionType ->
                    totalDeleted += activityLogRepository.deleteActivitiesByType(actionType)
                }

                refreshActivities()
                emitUiEvent(ActivityLogUiEvent.BulkOperationCompleted("Delete by Category", totalDeleted))
                emitUiEvent(ActivityLogUiEvent.ShowMessage("Deleted $totalDeleted ${category.getDisplayName()} activities"))

            } catch (e: Exception) {
                Log.e("ActivityLogViewModel", "Error deleting activities by category: ${e.message}", e)
                showError("Failed to delete activities by category: ${e.message}")
            }
        }
    }

    private fun analyzeActivityPatterns() {
        viewModelScope.launch {
            try {
                val activities = _state.value.activities

                // Perform analysis
                val analysisData = performActivityAnalysis(activities)

                emitUiEvent(ActivityLogUiEvent.AnalysisComplete(analysisData))

            } catch (e: Exception) {
                Log.e("ActivityLogViewModel", "Error analyzing activity patterns: ${e.message}", e)
                showError("Failed to analyze activity patterns: ${e.message}")
            }
        }
    }

    private fun performActivityAnalysis(activities: List<ActivityLogEntry>): ActivityAnalysisData {
        val currentTime = System.currentTimeMillis()
        val weekAgo = currentTime - (7 * 24 * 60 * 60 * 1000)
        val monthAgo = currentTime - (30 * 24 * 60 * 60 * 1000)

        val activitiesThisWeek = activities.count { it.timestamp >= weekAgo }
        val activitiesThisMonth = activities.count { it.timestamp >= monthAgo }

        // Find most active time of day
        val hourCounts = activities.groupBy { activity ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = activity.timestamp
            calendar.get(Calendar.HOUR_OF_DAY)
        }.mapValues { it.value.size }

        val mostActiveHour = hourCounts.maxByOrNull { it.value }?.key ?: 12
        val mostActiveTimeOfDay = when (mostActiveHour) {
            in 6..11 -> "Morning (${String.format("%02d:00", mostActiveHour)})"
            in 12..17 -> "Afternoon (${String.format("%02d:00", mostActiveHour)})"
            in 18..21 -> "Evening (${String.format("%02d:00", mostActiveHour)})"
            else -> "Night (${String.format("%02d:00", mostActiveHour)})"
        }

        // Action type distribution
        val actionTypeDistribution = activities.groupBy { it.actionType }
            .mapValues { it.value.size }

        val mostCommonActionType = actionTypeDistribution.maxByOrNull { it.value }?.key
            ?: ActivityActionType.TRANSACTION_ADDED_EXPENSE

        // Average activities per day
        val daysDiff = if (activities.isNotEmpty()) {
            val oldestTimestamp = activities.minOf { it.timestamp }
            val daysDifference = (currentTime - oldestTimestamp) / (24 * 60 * 60 * 1000)
            maxOf(1, daysDifference.toInt())
        } else 1

        val averageActivitiesPerDay = activities.size.toDouble() / daysDiff

        // Generate insights
        val insights = generateInsights(activities, activitiesThisWeek, activitiesThisMonth, averageActivitiesPerDay)

        return ActivityAnalysisData(
            totalActivities = activities.size,
            activitiesThisWeek = activitiesThisWeek,
            activitiesThisMonth = activitiesThisMonth,
            mostActiveTimeOfDay = mostActiveTimeOfDay,
            mostCommonActionType = mostCommonActionType,
            averageActivitiesPerDay = averageActivitiesPerDay,
            activityTrends = emptyMap(), // Implement trend calculation
            actionTypeDistribution = actionTypeDistribution,
            accountActivityDistribution = emptyMap(), // Implement account distribution
            categoryActivityDistribution = actionTypeDistribution.entries
                .groupBy { it.key.getCategory() }
                .mapValues { entry -> entry.value.sumOf { it.value } },
            peakActivityDays = emptyList(), // Implement peak days calculation
            insights = insights
        )
    }

    private fun generateInsights(
        activities: List<ActivityLogEntry>,
        weekActivities: Int,
        monthActivities: Int,
        avgDaily: Double
    ): List<String> {
        val insights = mutableListOf<String>()

        if (weekActivities > avgDaily * 7 * 1.5) {
            insights.add("You've been very active this week with financial transactions!")
        }

        if (monthActivities < avgDaily * 30 * 0.5) {
            insights.add("Activity has been lower this month compared to your average.")
        }

        val transactionActivities = activities.count {
            it.actionType.getCategory() == ActivityCategory.TRANSACTIONS
        }
        val accountActivities = activities.count {
            it.actionType.getCategory() == ActivityCategory.ACCOUNTS
        }

        if (transactionActivities > accountActivities * 10) {
            insights.add("Most of your activity involves transactions rather than account management.")
        }

        if (insights.isEmpty()) {
            insights.add("Your financial activity patterns look normal and consistent.")
        }

        return insights
    }

    private fun getMostActiveTimeOfDay(days: Int) {
        // Implementation for getting most active time analysis
        viewModelScope.launch {
            try {
                // Perform time-of-day analysis
                Log.d("ActivityLogViewModel", "Analyzing most active time of day for last $days days")

            } catch (e: Exception) {
                Log.e("ActivityLogViewModel", "Error getting most active time: ${e.message}", e)
                showError("Failed to analyze most active time: ${e.message}")
            }
        }
    }

    private fun getActivityTrends(timeRange: TimeRange) {
        // Implementation for activity trends analysis
        viewModelScope.launch {
            try {
                Log.d("ActivityLogViewModel", "Getting activity trends for $timeRange")

            } catch (e: Exception) {
                Log.e("ActivityLogViewModel", "Error getting activity trends: ${e.message}", e)
                showError("Failed to get activity trends: ${e.message}")
            }
        }
    }

    private fun getActivityStatistics() {
        viewModelScope.launch {
            try {
                val activities = _state.value.activities
                val statistics = calculateStatistics(activities)

                emitUiEvent(ActivityLogUiEvent.StatisticsCalculated(statistics))

            } catch (e: Exception) {
                Log.e("ActivityLogViewModel", "Error getting activity statistics: ${e.message}", e)
                showError("Failed to get activity statistics: ${e.message}")
            }
        }
    }

    private fun calculateStatistics(activities: List<ActivityLogEntry>): ActivityStatistics {
        val currentTime = System.currentTimeMillis()
        val todayStart = currentTime - (currentTime % (24 * 60 * 60 * 1000))
        val weekAgo = currentTime - (7 * 24 * 60 * 60 * 1000)
        val monthAgo = currentTime - (30 * 24 * 60 * 60 * 1000)

        val todayActivities = activities.count { it.timestamp >= todayStart }
        val weekActivities = activities.count { it.timestamp >= weekAgo }
        val monthActivities = activities.count { it.timestamp >= monthAgo }

        val actionTypeDistribution = activities.groupBy { it.actionType }.mapValues { it.value.size }
        val mostCommonActionType = actionTypeDistribution.maxByOrNull { it.value }?.key
        val leastCommonActionType = actionTypeDistribution.minByOrNull { it.value }?.key

        val categoryDistribution = activities.groupBy { it.actionType.getCategory() }.mapValues { it.value.size }

        return ActivityStatistics(
            totalActivities = activities.size,
            todayActivities = todayActivities,
            weekActivities = weekActivities,
            monthActivities = monthActivities,
            averageDailyActivities = if (activities.isNotEmpty()) activities.size / 30.0 else 0.0,
            mostActiveDay = "Monday", // Implement actual calculation
            leastActiveDay = "Sunday", // Implement actual calculation
            transactionActivities = categoryDistribution[ActivityCategory.TRANSACTIONS] ?: 0,
            accountActivities = categoryDistribution[ActivityCategory.ACCOUNTS] ?: 0,
            categoryActivities = categoryDistribution[ActivityCategory.CATEGORIES] ?: 0,
            systemActivities = categoryDistribution[ActivityCategory.SYSTEM] ?: 0,
            subscriptionActivities = categoryDistribution[ActivityCategory.SUBSCRIPTIONS] ?: 0,
            oldestActivity = activities.minByOrNull { it.timestamp },
            newestActivity = activities.maxByOrNull { it.timestamp },
            mostCommonActionType = mostCommonActionType,
            leastCommonActionType = leastCommonActionType
        )
    }

    private fun exportActivities() {
        viewModelScope.launch {
            try {
                // Implement export functionality
                emitUiEvent(ActivityLogUiEvent.ExportCompleted)

            } catch (e: Exception) {
                Log.e("ActivityLogViewModel", "Error exporting activities: ${e.message}", e)
                showError("Failed to export activities: ${e.message}")
            }
        }
    }

    private fun exportActivitiesForPeriod(timeRange: TimeRange) {
        viewModelScope.launch {
            try {
                val activities = ActivityLogFilterUtils.getActivitiesForTimeRange(_state.value.activities, timeRange)
                Log.d("ActivityLogViewModel", "Exporting ${activities.size} activities for $timeRange")

                emitUiEvent(ActivityLogUiEvent.ExportCompleted)

            } catch (e: Exception) {
                Log.e("ActivityLogViewModel", "Error exporting activities for period: ${e.message}", e)
                showError("Failed to export activities for period: ${e.message}")
            }
        }
    }

    private fun exportActivitiesByType(actionType: ActivityActionType) {
        viewModelScope.launch {
            try {
                val activities = _state.value.activities.filter { it.actionType == actionType }
                Log.d("ActivityLogViewModel", "Exporting ${activities.size} activities for type ${actionType.getDisplayName()}")

                emitUiEvent(ActivityLogUiEvent.ExportCompleted)

            } catch (e: Exception) {
                Log.e("ActivityLogViewModel", "Error exporting activities by type: ${e.message}", e)
                showError("Failed to export activities by type: ${e.message}")
            }
        }
    }

    private fun updateRetentionPeriod(days: Int) {
        retentionPeriodDays = days
        Log.d("ActivityLogViewModel", "Updated retention period to $days days")
    }

    private fun updateAutoCleanup(enabled: Boolean) {
        autoCleanupEnabled = enabled
        Log.d("ActivityLogViewModel", "Auto cleanup ${if (enabled) "enabled" else "disabled"}")
    }

    private fun updateActivityTracking(enabled: Boolean) {
        activityTrackingEnabled = enabled
        Log.d("ActivityLogViewModel", "Activity tracking ${if (enabled) "enabled" else "disabled"}")
    }

    private fun updateDetailLevel(level: ActivityDetailLevel) {
        detailLevel = level
        Log.d("ActivityLogViewModel", "Detail level updated to $level")
    }

    private fun performAutoCleanup() {
        viewModelScope.launch {
            try {
                val cutoffDate = System.currentTimeMillis() - (retentionPeriodDays * 24 * 60 * 60 * 1000L)
                val deletedCount = activityLogRepository.deleteActivitiesOlderThan(cutoffDate)

                if (deletedCount > 0) {
                    refreshActivities()
                    Log.d("ActivityLogViewModel", "Auto cleanup deleted $deletedCount old activities")
                }

                emitUiEvent(ActivityLogUiEvent.CleanupCompleted)

            } catch (e: Exception) {
                Log.e("ActivityLogViewModel", "Error performing auto cleanup: ${e.message}", e)
            }
        }
    }

    private fun cleanupOldActivities(cutoffDate: Long) {
        viewModelScope.launch {
            try {
                val deletedCount = activityLogRepository.deleteActivitiesOlderThan(cutoffDate)

                refreshActivities()
                emitUiEvent(ActivityLogUiEvent.CleanupCompleted)
                emitUiEvent(ActivityLogUiEvent.ShowMessage("Deleted $deletedCount old activities"))

            } catch (e: Exception) {
                Log.e("ActivityLogViewModel", "Error cleaning up old activities: ${e.message}", e)
                showError("Failed to cleanup old activities: ${e.message}")
            }
        }
    }

    private fun optimizeDatabase() {
        viewModelScope.launch {
            try {
                activityLogRepository.optimizeDatabase()
                emitUiEvent(ActivityLogUiEvent.DatabaseOptimized)
                emitUiEvent(ActivityLogUiEvent.ShowMessage("Database optimized successfully"))

            } catch (e: Exception) {
                Log.e("ActivityLogViewModel", "Error optimizing database: ${e.message}", e)
                showError("Failed to optimize database: ${e.message}")
            }
        }
    }

    // Real-time event handlers
    private fun onTransactionCreated(transaction: TransactionEntity, account: AccountEntity?) {
        if (!activityTrackingEnabled) return

        val actionType = when (transaction.mode) {
            "Expense" -> ActivityActionType.TRANSACTION_ADDED_EXPENSE
            "Income" -> ActivityActionType.TRANSACTION_ADDED_INCOME
            "Transfer" -> ActivityActionType.TRANSACTION_ADDED_TRANSFER
            else -> ActivityActionType.TRANSACTION_ADDED_EXPENSE
        }

        val activity = ActivityLogEntryFactory.createTransactionEntry(actionType, transaction, account)
        addActivity(activity)
    }

    private fun onTransactionUpdated(oldTransaction: TransactionEntity, newTransaction: TransactionEntity) {
        if (!activityTrackingEnabled) return

        val activity = ActivityLogEntryFactory.createTransactionEntry(
            ActivityActionType.TRANSACTION_UPDATED,
            newTransaction
        )
        addActivity(activity)
    }

    private fun onTransactionDeleted(transaction: TransactionEntity, account: AccountEntity?) {
        if (!activityTrackingEnabled) return

        val actionType = when (transaction.mode) {
            "Expense" -> ActivityActionType.TRANSACTION_DELETED_EXPENSE
            "Income" -> ActivityActionType.TRANSACTION_DELETED_INCOME
            "Transfer" -> ActivityActionType.TRANSACTION_DELETED_TRANSFER
            else -> ActivityActionType.TRANSACTION_DELETED_EXPENSE
        }

        val activity = ActivityLogEntryFactory.createTransactionEntry(actionType, transaction, account)
        addActivity(activity)
    }

    private fun onAccountCreated(account: AccountEntity) {
        if (!activityTrackingEnabled) return

        val activity = ActivityLogEntryFactory.createAccountEntry(
            ActivityActionType.ACCOUNT_CREATED,
            account
        )
        addActivity(activity)
    }

    private fun onAccountUpdated(oldAccount: AccountEntity, newAccount: AccountEntity) {
        if (!activityTrackingEnabled) return

        val activity = ActivityLogEntryFactory.createAccountEntry(
            ActivityActionType.ACCOUNT_UPDATED,
            newAccount
        )
        addActivity(activity)
    }

    private fun onAccountDeleted(account: AccountEntity) {
        if (!activityTrackingEnabled) return

        val activity = ActivityLogEntryFactory.createAccountEntry(
            ActivityActionType.ACCOUNT_DELETED,
            account
        )
        addActivity(activity)
    }

    private fun onCategoryCreated(categoryName: String, categoryId: Int) {
        if (!activityTrackingEnabled) return

        val activity = ActivityLogEntryFactory.createCategoryEntry(
            ActivityActionType.CATEGORY_CREATED,
            categoryName,
            categoryId
        )
        addActivity(activity)
    }

    private fun onCategoryUpdated(categoryName: String, categoryId: Int) {
        if (!activityTrackingEnabled) return

        val activity = ActivityLogEntryFactory.createCategoryEntry(
            ActivityActionType.CATEGORY_UPDATED,
            categoryName,
            categoryId
        )
        addActivity(activity)
    }

    private fun onCategoryDeleted(categoryName: String, categoryId: Int) {
        if (!activityTrackingEnabled) return

        val activity = ActivityLogEntryFactory.createCategoryEntry(
            ActivityActionType.CATEGORY_DELETED,
            categoryName,
            categoryId
        )
        addActivity(activity)
    }

    private fun onSystemAction(actionType: ActivityActionType, description: String) {
        if (!activityTrackingEnabled) return

        val activity = ActivityLogEntryFactory.createSystemEntry(actionType, description)
        addActivity(activity)
    }

    private fun navigateToRelatedTransaction(transactionId: Int) {
        emitUiEvent(
            ActivityLogUiEvent.NavigateToScreen(
                "edit_transaction",
                mapOf("transactionId" to transactionId)
            )
        )
    }

    private fun navigateToRelatedAccount(accountId: Int) {
        emitUiEvent(
            ActivityLogUiEvent.NavigateToScreen(
                "account_details",
                mapOf("accountId" to accountId)
            )
        )
    }

    private fun showError(message: String) {
        updateState { it.copy(error = message) }
        emitUiEvent(ActivityLogUiEvent.ShowMessage(message, true))
    }

    private fun clearError() {
        updateState { it.copy(error = null) }
    }

    private fun setLoading(isLoading: Boolean) {
        updateState { it.copy(isLoading = isLoading) }
    }

    private fun updateLastRefresh(timestamp: Long) {
        updateState { it.copy(lastUpdated = timestamp) }
    }
}

//// Note: ActivityLogRepository would need to be implemented separately
//// This is a placeholder interface
//interface ActivityLogRepository {
//    suspend fun getAllActivities(): List<ActivityLogEntry>
//    suspend fun addActivity(activity: ActivityLogEntry): Long
//    suspend fun updateActivity(activity: ActivityLogEntry)
//    suspend fun deleteActivity(activityId: Int)
//    suspend fun deleteAllActivities(): Int
//    suspend fun deleteActivitiesOlderThan(cutoffDate: Long): Int
//    suspend fun deleteActivitiesByType(actionType: ActivityActionType): Int
//    suspend fun optimizeDatabase()
//}