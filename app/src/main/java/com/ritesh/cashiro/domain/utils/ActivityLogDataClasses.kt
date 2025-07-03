package com.ritesh.cashiro.domain.utils

import com.ritesh.cashiro.presentation.ui.features.activity_logs.ActivityActionType

/**
 * Result data class for database maintenance operations
 */
data class MaintenanceResult(
    val success: Boolean,
    val message: String,
    val details: Map<String, Any> = emptyMap(),
    val executionTimeMs: Long = 0,
    val operationsPerformed: List<String> = emptyList()
) {
    companion object {
        fun success(
            message: String,
            details: Map<String, Any> = emptyMap(),
            executionTimeMs: Long = 0,
            operations: List<String> = emptyList()
        ) = MaintenanceResult(
            success = true,
            message = message,
            details = details,
            executionTimeMs = executionTimeMs,
            operationsPerformed = operations
        )

        fun failure(
            message: String,
            details: Map<String, Any> = emptyMap(),
            executionTimeMs: Long = 0
        ) = MaintenanceResult(
            success = false,
            message = message,
            details = details,
            executionTimeMs = executionTimeMs
        )
    }
}

/**
 * Data class for activity statistics
 */
data class ActivityStats(
    val totalActivities: Int,
    val todayActivities: Int,
    val weekActivities: Int,
    val monthActivities: Int,
    val averageDailyActivities: Double,
    val mostActiveDay: String,
    val leastActiveDay: String,
    val mostCommonActionType: ActivityActionType?,
    val leastCommonActionType: ActivityActionType?,
    val oldestActivityTimestamp: Long?,
    val newestActivityTimestamp: Long?,
    val databaseSizeBytes: Long,
    val lastMaintenanceTimestamp: Long?
)

/**
 * Data class for activity trends analysis
 */
data class ActivityTrend(
    val period: String, // "2024-01-15", "2024-W03", "2024-01"
    val count: Int,
    val timestamp: Long
)

/**
 * Data class for time-of-day activity analysis
 */
data class HourlyActivityStats(
    val hour: Int, // 0-23
    val count: Int,
    val percentage: Double
)

/**
 * Data class for account activity analysis
 */
data class AccountActivityStats(
    val accountId: Int,
    val accountName: String?,
    val activityCount: Int,
    val lastActivityTimestamp: Long?,
    val mostCommonActionType: ActivityActionType?
)

/**
 * Data class for category activity analysis
 */
data class CategoryActivityStats(
    val categoryId: Int,
    val categoryName: String?,
    val activityCount: Int,
    val lastActivityTimestamp: Long?,
    val mostCommonActionType: ActivityActionType?
)

/**
 * Data class for action type distribution
 */
data class ActionTypeStats(
    val actionType: ActivityActionType,
    val count: Int,
    val percentage: Double,
    val firstOccurrence: Long?,
    val lastOccurrence: Long?
)

/**
 * Data class for database health information
 */
data class DatabaseHealth(
    val isHealthy: Boolean,
    val totalRows: Int,
    val databaseSizeBytes: Long,
    val indexCount: Int,
    val lastVacuumTimestamp: Long?,
    val fragmentationPercentage: Double,
    val issues: List<String> = emptyList(),
    val recommendations: List<String> = emptyList()
)

/**
 * Data class for export configuration
 */
data class ExportConfig(
    val includeMetadata: Boolean = true,
    val includeSystemActions: Boolean = true,
    val dateFormat: String = "yyyy-MM-dd HH:mm:ss",
    val timeZone: String = "UTC",
    val maxRecords: Int? = null,
    val sortOrder: ExportSortOrder = ExportSortOrder.TIMESTAMP_DESC
)

enum class ExportSortOrder {
    TIMESTAMP_ASC,
    TIMESTAMP_DESC,
    TYPE_ASC,
    TYPE_DESC
}

/**
 * Data class for batch processing configuration
 */
data class BatchConfig(
    val batchSize: Int = 100,
    val delayBetweenBatchesMs: Long = 0,
    val maxBatches: Int? = null,
    val continueOnError: Boolean = true
)

/**
 * Data class for cleanup configuration
 */
data class CleanupConfig(
    val retentionDays: Int = 90,
    val cleanupSystemActions: Boolean = false,
    val cleanupDuplicates: Boolean = true,
    val optimizeAfterCleanup: Boolean = true,
    val dryRun: Boolean = false
)

/**
 * Result of cleanup operations
 */
data class CleanupResult(
    val itemsDeleted: Int,
    val duplicatesRemoved: Int,
    val spaceSavedBytes: Long,
    val executionTimeMs: Long,
    val errors: List<String> = emptyList()
)