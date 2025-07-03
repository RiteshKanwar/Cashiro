package com.ritesh.cashiro.services

import android.app.Service
import android.content.Intent
import android.os.IBinder

class NotificationService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Handle background notification tasks if needed
        return START_NOT_STICKY
    }
}