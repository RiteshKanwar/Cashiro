package com.ritesh.cashiro.domain.repository

import com.ritesh.cashiro.domain.utils.MaintenanceResult
import com.ritesh.cashiro.presentation.ui.features.activity_logs.ActivityActionType
import com.ritesh.cashiro.presentation.ui.features.activity_logs.ActivityLogEntry
import kotlinx.coroutines.flow.Flow

interface ActivityLogRepository {

    // Basic CRUD Operations
    suspend fun getAllActivities(): List<ActivityLogEntry>
    suspend fun getAllActivitiesFlow(): Flow<List<ActivityLogEntry>>
    suspend fun getActivityById(id: Int): ActivityLogEntry?
    suspend fun addActivity(activity: ActivityLogEntry): Long
    suspend fun addActivities(activities: List<ActivityLogEntry>): List<Long>
    suspend fun updateActivity(activity: ActivityLogEntry)
    suspend fun deleteActivity(activityId: Int)
    suspend fun deleteAllActivities(): Int

    // Filter Operations
    suspend fun getActivitiesByType(actionType: ActivityActionType): List<ActivityLogEntry>
    suspend fun getActivitiesByAccount(accountId: Int): List<ActivityLogEntry>
    suspend fun getActivitiesByTransaction(transactionId: Int): List<ActivityLogEntry>
    suspend fun getActivitiesByCategory(categoryId: Int): List<ActivityLogEntry>
    suspend fun getActivitiesByTimeRange(startTime: Long, endTime: Long): List<ActivityLogEntry>
    suspend fun getActivitiesAfter(startTime: Long): List<ActivityLogEntry>
    suspend fun getActivitiesBefore(endTime: Long): List<ActivityLogEntry>

    // Search Operations
    suspend fun searchActivities(query: String): List<ActivityLogEntry>

    // Bulk Delete Operations
    suspend fun deleteActivitiesByType(actionType: ActivityActionType): Int
    suspend fun deleteActivitiesByAccount(accountId: Int): Int
    suspend fun deleteActivitiesByTransaction(transactionId: Int): Int
    suspend fun deleteActivitiesByCategory(categoryId: Int): Int
    suspend fun deleteActivitiesOlderThan(cutoffDate: Long): Int
    suspend fun deleteActivitiesByIds(ids: List<Int>): Int

    // Statistics and Analytics Operations
    suspend fun getActivityCount(): Int
    suspend fun getActivityCountByType(actionType: ActivityActionType): Int
    suspend fun getActivityCountByAccount(accountId: Int): Int
    suspend fun getActivityCountInRange(startTime: Long, endTime: Long): Int
    suspend fun getLatestActivity(): ActivityLogEntry?
    suspend fun getOldestActivity(): ActivityLogEntry?

    // Time-based Convenience Methods
    suspend fun getTodayActivities(): List<ActivityLogEntry>
    suspend fun getThisWeekActivities(): List<ActivityLogEntry>
    suspend fun getThisMonthActivities(): List<ActivityLogEntry>

    // Performance and Maintenance Operations
    suspend fun optimizeDatabase()

    // Advanced Analytics (Optional - for future features)
    suspend fun getActivityTypeDistribution(): Map<ActivityActionType, Int>
    suspend fun getActivityTrendsByDay(days: Int = 30): Map<String, Int>
    suspend fun getActivityTrendsByWeek(weeks: Int = 12): Map<String, Int>
    suspend fun getActivityTrendsByMonth(months: Int = 12): Map<String, Int>
    suspend fun getMostActiveTimeOfDay(): Map<Int, Int> // Hour -> Count
    suspend fun getMostActiveAccount(): Pair<Int, Int>? // AccountId -> Count
    suspend fun getMostActiveCategory(): Pair<Int, Int>? // CategoryId -> Count

    // Batch Operations for Performance
    suspend fun getActivitiesInBatches(
        batchSize: Int = 100,
        onBatch: suspend (List<ActivityLogEntry>) -> Unit
    )

    // Data Export Operations
    suspend fun exportActivitiesToJson(): String
    suspend fun exportActivitiesForTimeRange(startTime: Long, endTime: Long): String
    suspend fun exportActivitiesByType(actionType: ActivityActionType): String

    // Health Check Operations
    suspend fun getDatabaseSize(): Long
    suspend fun getTableRowCount(): Int
    suspend fun isHealthy(): Boolean

    // Cleanup Operations
    suspend fun performMaintenance(): MaintenanceResult
    suspend fun cleanupDuplicates(): Int
    suspend fun rebuildIndices()
}