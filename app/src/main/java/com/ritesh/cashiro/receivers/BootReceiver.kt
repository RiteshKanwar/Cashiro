package com.ritesh.cashiro.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ritesh.cashiro.domain.utils.NotificationScheduler

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule all notifications after device reboot
            NotificationScheduler.rescheduleAllNotifications(context)
        }
    }
}