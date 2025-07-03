package com.ritesh.cashiro.receivers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.ritesh.cashiro.MainActivity
import com.ritesh.cashiro.R

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "TRANSACTION_REMINDER" -> {
                showTransactionReminderNotification(context)
            }
            "UPCOMING_TRANSACTION" -> {
                val transactionId = intent.getIntExtra("transaction_id", 0)
                val transactionTitle = intent.getStringExtra("transaction_title")
                showUpcomingTransactionNotification(context, transactionId, transactionTitle)
            }
        }
    }

    private fun showTransactionReminderNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "transaction_reminders",
                "Transaction Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to add transactions"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create intent to open AddTransactionScreen for new transaction
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "ADD_TRANSACTION")
            putExtra("is_update_transaction", false)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create notification
        val notification = NotificationCompat.Builder(context, "transaction_reminders")
            .setSmallIcon(R.drawable.statusbar_notification_icon)
            .setContentTitle("Add Transaction Reminder")
            .setContentText("Don't forget to add your transactions for today!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }

    private fun showUpcomingTransactionNotification(context: Context, transactionId: Int, transactionTitle: String?) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android 8.0+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "upcoming_transactions",
                "Upcoming Transactions",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for upcoming transactions"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create intent to open AddTransactionScreen for updating specific transaction
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "ADD_TRANSACTION")
            putExtra("is_update_transaction", true)
            putExtra("transaction_id", transactionId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            transactionId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create notification
        val notification = NotificationCompat.Builder(context, "upcoming_transactions")
            .setSmallIcon(R.drawable.statusbar_notification_icon)
            .setContentTitle("Upcoming Transaction")
            .setContentText("${transactionTitle ?: "Transaction"} is due today!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(transactionId, notification)
    }
}