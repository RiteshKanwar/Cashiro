package com.ritesh.cashiro.permissions
import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
@Composable
fun RequestNotificationPermissions(
    onPermissionsResult: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current

    // Launcher for notification permission (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onPermissionsResult(isGranted)
    }

    // Launcher for exact alarm permission settings
    val exactAlarmSettingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        // Check if exact alarm permission is now granted
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val hasExactAlarmPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
        onPermissionsResult(hasExactAlarmPermission)
    }

    LaunchedEffect(Unit) {
        // Check and request notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)) {
                PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted, check exact alarm permission
                    checkExactAlarmPermission(context, exactAlarmSettingsLauncher, onPermissionsResult)
                }
                else -> {
                    // Request notification permission
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // For older Android versions, check exact alarm permission
            checkExactAlarmPermission(context, exactAlarmSettingsLauncher, onPermissionsResult)
        }
    }
}

private fun checkExactAlarmPermission(
    context: Context,
    exactAlarmSettingsLauncher: androidx.activity.result.ActivityResultLauncher<Intent>,
    onPermissionsResult: (Boolean) -> Unit
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (!alarmManager.canScheduleExactAlarms()) {
            // Request exact alarm permission
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            exactAlarmSettingsLauncher.launch(intent)
        } else {
            onPermissionsResult(true)
        }
    } else {
        onPermissionsResult(true)
    }
}

// Helper function to check if all notification permissions are granted
fun hasNotificationPermissions(context: Context): Boolean {
    val hasPostNotification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }

    val hasExactAlarm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.canScheduleExactAlarms()
    } else {
        true
    }

    return hasPostNotification && hasExactAlarm
}