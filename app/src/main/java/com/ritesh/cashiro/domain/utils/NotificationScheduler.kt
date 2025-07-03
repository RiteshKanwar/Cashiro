package com.ritesh.cashiro.domain.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.ritesh.cashiro.domain.repository.ReminderType
import com.ritesh.cashiro.receivers.NotificationReceiver
import java.util.Calendar

object NotificationScheduler {

    private fun canScheduleExactAlarms(alarmManager: AlarmManager): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    private fun scheduleExactAlarm(
        alarmManager: AlarmManager,
        triggerTime: Long,
        pendingIntent: PendingIntent
    ): Boolean {
        return try {
            if (!canScheduleExactAlarms(alarmManager)) {
                Log.w("NotificationScheduler", "Cannot schedule exact alarms - permission not granted")
                return false
            }

            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
                else -> {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            }
            true
        } catch (e: SecurityException) {
            Log.e("NotificationScheduler", "SecurityException when scheduling alarm: ${e.message}", e)
            false
        } catch (e: Exception) {
            Log.e("NotificationScheduler", "Unexpected exception when scheduling alarm: ${e.message}", e)
            false
        }
    }

    fun scheduleTransactionReminder(
        context: Context,
        hour: Int,
        minute: Int,
        reminderType: ReminderType
    ): Boolean {
        return try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = "TRANSACTION_REMINDER"
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                1001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            // If the time has already passed today, schedule for tomorrow
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }

            val triggerTime = when (reminderType) {
                ReminderType.EVERYDAY -> calendar.timeInMillis
                ReminderType.IF_NOT_OPENED_TODAY -> calendar.timeInMillis
                ReminderType.HOURS_FROM_OPENED -> System.currentTimeMillis() + (24 * 60 * 60 * 1000)
            }

            val success = scheduleExactAlarm(alarmManager, triggerTime, pendingIntent)

            if (success) {
                Log.d("NotificationScheduler", "Successfully scheduled transaction reminder for ${reminderType.getDisplayName()}")
            } else {
                Log.w("NotificationScheduler", "Failed to schedule transaction reminder")
            }

            success
        } catch (e: Exception) {
            Log.e("NotificationScheduler", "Error scheduling transaction reminder", e)
            false
        }
    }

    fun scheduleUpcomingTransactionReminder(
        context: Context,
        transactionId: Int,
        transactionTitle: String,
        reminderTime: Long
    ): Boolean {
        return try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = "UPCOMING_TRANSACTION"
                putExtra("transaction_id", transactionId)
                putExtra("transaction_title", transactionTitle)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                transactionId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val success = scheduleExactAlarm(alarmManager, reminderTime, pendingIntent)

            if (success) {
                Log.d("NotificationScheduler", "Successfully scheduled upcoming transaction reminder for $transactionTitle")
            } else {
                Log.w("NotificationScheduler", "Failed to schedule upcoming transaction reminder for $transactionTitle")
            }

            success
        } catch (e: Exception) {
            Log.e("NotificationScheduler", "Error scheduling upcoming transaction reminder", e)
            false
        }
    }

    fun cancelTransactionReminder(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = "TRANSACTION_REMINDER"
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                1001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            Log.d("NotificationScheduler", "Cancelled transaction reminder")
        } catch (e: Exception) {
            Log.e("NotificationScheduler", "Error cancelling transaction reminder", e)
        }
    }

    fun cancelUpcomingTransactionReminder(context: Context, transactionId: Int) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                action = "UPCOMING_TRANSACTION"
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                transactionId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            Log.d("NotificationScheduler", "Cancelled upcoming transaction reminder for $transactionId")
        } catch (e: Exception) {
            Log.e("NotificationScheduler", "Error cancelling upcoming transaction reminder", e)
        }
    }

    fun rescheduleAllNotifications(context: Context) {
        Log.d("NotificationScheduler", "Rescheduling all notifications after boot")
        // This would read from your settings and reschedule all active notifications
        // Implementation depends on how you store notification settings
    }

    fun hasExactAlarmPermission(context: Context): Boolean {
        return try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            canScheduleExactAlarms(alarmManager)
        } catch (e: Exception) {
            Log.e("NotificationScheduler", "Error checking exact alarm permission", e)
            false
        }
    }

    // Test notification function for development
    fun sendTestNotification(context: Context, isUpcoming: Boolean = false) {
        if (isUpcoming) {
            // Test upcoming transaction notification
            val testIntent = Intent(context, NotificationReceiver::class.java).apply {
                action = "UPCOMING_TRANSACTION"
                putExtra("transaction_id", 999)
                putExtra("transaction_title", "Test Upcoming Transaction")
            }
            context.sendBroadcast(testIntent)
        } else {
            // Test add transaction reminder
            val testIntent = Intent(context, NotificationReceiver::class.java).apply {
                action = "TRANSACTION_REMINDER"
            }
            context.sendBroadcast(testIntent)
        }
        Log.d("NotificationScheduler", "Sent test notification: ${if (isUpcoming) "upcoming" else "reminder"}")
    }
}