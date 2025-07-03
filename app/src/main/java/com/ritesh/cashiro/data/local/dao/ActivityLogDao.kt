package com.ritesh.cashiro.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ritesh.cashiro.data.local.entity.ActivityLogEntity
import com.ritesh.cashiro.presentation.ui.features.activity_logs.ActivityActionType
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityLogDao {

    // Basic CRUD Operations
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC")
    suspend fun getAllActivities(): List<ActivityLogEntity>

    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC")
    fun getAllActivitiesFlow(): Flow<List<ActivityLogEntity>>

    @Query("SELECT * FROM activity_logs WHERE id = :id")
    suspend fun getActivityById(id: Int): ActivityLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ActivityLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivities(activities: List<ActivityLogEntity>): List<Long>

    @Update
    suspend fun updateActivity(activity: ActivityLogEntity)

    @Delete
    suspend fun deleteActivity(activity: ActivityLogEntity)

    @Query("DELETE FROM activity_logs WHERE id = :id")
    suspend fun deleteActivityById(id: Int): Int

    @Query("DELETE FROM activity_logs")
    suspend fun deleteAllActivities(): Int

    // Filter Operations
    @Query("SELECT * FROM activity_logs WHERE actionType = :actionType ORDER BY timestamp DESC")
    suspend fun getActivitiesByType(actionType: ActivityActionType): List<ActivityLogEntity>

    @Query("SELECT * FROM activity_logs WHERE relatedAccountId = :accountId ORDER BY timestamp DESC")
    suspend fun getActivitiesByAccount(accountId: Int): List<ActivityLogEntity>

    @Query("SELECT * FROM activity_logs WHERE relatedTransactionId = :transactionId ORDER BY timestamp DESC")
    suspend fun getActivitiesByTransaction(transactionId: Int): List<ActivityLogEntity>

    @Query("SELECT * FROM activity_logs WHERE relatedCategoryId = :categoryId ORDER BY timestamp DESC")
    suspend fun getActivitiesByCategory(categoryId: Int): List<ActivityLogEntity>

    @Query("SELECT * FROM activity_logs WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    suspend fun getActivitiesByTimeRange(startTime: Long, endTime: Long): List<ActivityLogEntity>

    @Query("SELECT * FROM activity_logs WHERE timestamp >= :startTime ORDER BY timestamp DESC")
    suspend fun getActivitiesAfter(startTime: Long): List<ActivityLogEntity>

    @Query("SELECT * FROM activity_logs WHERE timestamp <= :endTime ORDER BY timestamp DESC")
    suspend fun getActivitiesBefore(endTime: Long): List<ActivityLogEntity>

    // Search Operations
    @Query("""
        SELECT * FROM activity_logs 
        WHERE title LIKE '%' || :query || '%' 
        OR description LIKE '%' || :query || '%' 
        ORDER BY timestamp DESC
    """)
    suspend fun searchActivities(query: String): List<ActivityLogEntity>

    // Bulk Delete Operations
    @Query("DELETE FROM activity_logs WHERE actionType = :actionType")
    suspend fun deleteActivitiesByType(actionType: ActivityActionType): Int

    @Query("DELETE FROM activity_logs WHERE relatedAccountId = :accountId")
    suspend fun deleteActivitiesByAccount(accountId: Int): Int

    @Query("DELETE FROM activity_logs WHERE relatedTransactionId = :transactionId")
    suspend fun deleteActivitiesByTransaction(transactionId: Int): Int

    @Query("DELETE FROM activity_logs WHERE relatedCategoryId = :categoryId")
    suspend fun deleteActivitiesByCategory(categoryId: Int): Int

    @Query("DELETE FROM activity_logs WHERE timestamp < :cutoffDate")
    suspend fun deleteActivitiesOlderThan(cutoffDate: Long): Int

    @Query("DELETE FROM activity_logs WHERE id IN (:ids)")
    suspend fun deleteActivitiesByIds(ids: List<Int>): Int

    // Statistics and Analytics
    @Query("SELECT COUNT(*) FROM activity_logs")
    suspend fun getActivityCount(): Int

    @Query("SELECT COUNT(*) FROM activity_logs WHERE actionType = :actionType")
    suspend fun getActivityCountByType(actionType: ActivityActionType): Int

    @Query("SELECT COUNT(*) FROM activity_logs WHERE relatedAccountId = :accountId")
    suspend fun getActivityCountByAccount(accountId: Int): Int

    @Query("SELECT COUNT(*) FROM activity_logs WHERE timestamp >= :startTime AND timestamp <= :endTime")
    suspend fun getActivityCountInRange(startTime: Long, endTime: Long): Int

    @Query("""
        SELECT actionType, COUNT(*) as count 
        FROM activity_logs 
        GROUP BY actionType 
        ORDER BY count DESC
    """)
    suspend fun getActivityTypeDistribution(): List<ActivityTypeCount>

    @Query("""
        SELECT * FROM activity_logs 
        WHERE timestamp = (SELECT MAX(timestamp) FROM activity_logs)
        LIMIT 1
    """)
    suspend fun getLatestActivity(): ActivityLogEntity?

    @Query("""
        SELECT * FROM activity_logs 
        WHERE timestamp = (SELECT MIN(timestamp) FROM activity_logs)
        LIMIT 1
    """)
    suspend fun getOldestActivity(): ActivityLogEntity?

    // Performance and Maintenance
    // Note: VACUUM and REINDEX are not supported by Room
    // These will be handled at the database level in the repository

    // Today's activities (helper query)
    @Query("""
        SELECT * FROM activity_logs 
        WHERE timestamp >= :startOfDay AND timestamp < :endOfDay
        ORDER BY timestamp DESC
    """)
    suspend fun getTodayActivities(startOfDay: Long, endOfDay: Long): List<ActivityLogEntity>

    // This week's activities
    @Query("""
        SELECT * FROM activity_logs 
        WHERE timestamp >= :weekStart
        ORDER BY timestamp DESC
    """)
    suspend fun getThisWeekActivities(weekStart: Long): List<ActivityLogEntity>

    // This month's activities
    @Query("""
        SELECT * FROM activity_logs 
        WHERE timestamp >= :monthStart
        ORDER BY timestamp DESC
    """)
    suspend fun getThisMonthActivities(monthStart: Long): List<ActivityLogEntity>
}

// Data class for activity type distribution query
data class ActivityTypeCount(
    val actionType: ActivityActionType,
    val count: Int
)