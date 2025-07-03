package com.ritesh.cashiro.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.util.Log
import com.ritesh.cashiro.widgets.WidgetUpdateUtil
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SystemThemeChangeReceiver : BroadcastReceiver() {

    @Inject
    lateinit var widgetUpdateUtil: WidgetUpdateUtil

    companion object {
        private const val TAG = "SystemThemeChangeReceiver"
        private var lastConfigurationChange = 0L
        private const val MIN_UPDATE_INTERVAL = 2000L // 2 seconds to avoid rapid updates
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_CONFIGURATION_CHANGED -> {
                handleConfigurationChanged(context)
            }
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_PACKAGE_REPLACED -> {
                // Handle app updates - ensure widgets are updated with latest theme
                Log.d(TAG, "App updated, refreshing all widgets")
                widgetUpdateUtil.updateAllWidgets()
            }
        }
    }

    private fun handleConfigurationChanged(context: Context) {
        val currentTime = System.currentTimeMillis()

        // Debounce rapid configuration changes
        if (currentTime - lastConfigurationChange < MIN_UPDATE_INTERVAL) {
            Log.d(TAG, "Ignoring rapid configuration change")
            return
        }

        lastConfigurationChange = currentTime

        try {
            Log.d(TAG, "System configuration changed, checking if theme changed")

            // Get current system theme
            val nightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            val isDarkMode = nightMode == Configuration.UI_MODE_NIGHT_YES

            Log.d(TAG, "Current system theme - isDark: $isDarkMode")

            // Update all widgets with new theme
            widgetUpdateUtil.updateWidgetsTheme()

            Log.d(TAG, "Widget theme update triggered")

        } catch (e: Exception) {
            Log.e(TAG, "Error handling configuration change", e)
        }
    }
}