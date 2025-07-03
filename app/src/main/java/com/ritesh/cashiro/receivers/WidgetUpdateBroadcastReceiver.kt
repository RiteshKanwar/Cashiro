package com.ritesh.cashiro.receivers

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ritesh.cashiro.widgets.providers.AddTransactionWidgetProvider
import com.ritesh.cashiro.widgets.providers.DashboardWidgetProvider
import com.ritesh.cashiro.widgets.providers.NetWorthWidgetProvider
import com.ritesh.cashiro.widgets.providers.SmallAddTransactionWidgetProvider
import com.ritesh.cashiro.widgets.providers.WideBalanceWidgetProvider

class WidgetUpdateBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_UPDATE_WIDGETS = "com.cashiro.UPDATE_WIDGETS"
        const val ACTION_UPDATE_THEME = "com.cashiro.UPDATE_THEME"
        const val ACTION_DATA_CHANGED = "com.cashiro.DATA_CHANGED"
        private const val TAG = "WidgetUpdateBroadcastReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received broadcast: ${intent.action}")

        try {
            when (intent.action) {
                ACTION_UPDATE_WIDGETS,
                ACTION_UPDATE_THEME,
                ACTION_DATA_CHANGED -> {
                    // Force update all widget providers
                    updateAllWidgetProviders(context, intent.action ?: "unknown")
                }
                Intent.ACTION_CONFIGURATION_CHANGED -> {
                    // Handle system configuration changes
                    Log.d(TAG, "Configuration changed - updating widget themes")
                    updateAllWidgetProviders(context, "theme_change")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling broadcast: ${intent.action}", e)
        }
    }

    private fun updateAllWidgetProviders(context: Context, reason: String) {
        Log.d(TAG, "Updating all widget providers - reason: $reason")

        val appWidgetManager = AppWidgetManager.getInstance(context)

        // List of all widget provider classes
        val widgetProviders = listOf(
            DashboardWidgetProvider::class.java,
            NetWorthWidgetProvider::class.java,
            WideBalanceWidgetProvider::class.java,
            AddTransactionWidgetProvider::class.java,
            SmallAddTransactionWidgetProvider::class.java,
        )

        widgetProviders.forEach { providerClass ->
            try {
                val componentName = ComponentName(context, providerClass)
                val widgetIds = appWidgetManager.getAppWidgetIds(componentName)

                if (widgetIds.isNotEmpty()) {
                    val updateIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
                        component = componentName
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
                        putExtra("update_reason", reason)
                    }
                    context.sendBroadcast(updateIntent)
                    Log.d(TAG, "Updated ${providerClass.simpleName}: ${widgetIds.contentToString()}")
                } else {
                    Log.d(TAG, "No widgets found for ${providerClass.simpleName}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating ${providerClass.simpleName}", e)
            }
        }
    }
}

//class WidgetUpdateBroadcastReceiver : BroadcastReceiver() {
//
//    companion object {
//        const val ACTION_UPDATE_WIDGETS = "com.ritesh.cashiro.UPDATE_WIDGETS"
//        const val ACTION_UPDATE_THEME = "com.ritesh.cashiro.UPDATE_THEME"
//        const val ACTION_DATA_CHANGED = "com.ritesh.cashiro.DATA_CHANGED"
//        private const val TAG = "WidgetUpdateReceiver"
//    }
//
//    override fun onReceive(context: Context, intent: Intent) {
//        Log.d(TAG, "Received explicit broadcast intent: ${intent.action}")
//
//        when (intent.action) {
//            ACTION_UPDATE_WIDGETS, ACTION_DATA_CHANGED -> {
//                Log.d(TAG, "Updating all widgets data")
//                updateAllWidgets(context)
//            }
//            ACTION_UPDATE_THEME -> {
//                Log.d(TAG, "Updating all widgets theme")
//                updateAllWidgetsTheme(context)
//            }
//            // Handle system configuration changes
//            Intent.ACTION_CONFIGURATION_CHANGED -> {
//                Log.d(TAG, "Configuration changed, updating widgets")
//                updateAllWidgetsTheme(context)
//            }
//        }
//    }
//
//    private fun updateAllWidgets(context: Context) {
//        val appWidgetManager = AppWidgetManager.getInstance(context)
//
//        // Update all widget types with latest data
//        updateWidgetType<DashboardWidgetProvider>(context, appWidgetManager)
//        updateWidgetType<NetWorthWidgetProvider>(context, appWidgetManager)
//        updateWidgetType<QuickBalanceWidgetProvider>(context, appWidgetManager)
//        updateWidgetType<WideBalanceWidgetProvider>(context, appWidgetManager)
//        updateWidgetType<AddTransactionWidgetProvider>(context, appWidgetManager)
//        updateWidgetType<TransferTransactionWidgetProvider>(context, appWidgetManager)
//        updateWidgetType<SmallAddTransactionWidgetProvider>(context, appWidgetManager)
//        updateWidgetType<SmallTransferWidgetProvider>(context, appWidgetManager)
//
//        Log.d(TAG, "All widgets updated successfully")
//    }
//
//    private fun updateAllWidgetsTheme(context: Context) {
//        // Theme changes require full widget updates
//        updateAllWidgets(context)
//    }
//
//    private inline fun <reified T : AppWidgetProvider> updateWidgetType(
//        context: Context,
//        appWidgetManager: AppWidgetManager
//    ) {
//        try {
//            val componentName = ComponentName(context, T::class.java)
//            val widgetIds = appWidgetManager.getAppWidgetIds(componentName)
//
//            if (widgetIds.isNotEmpty()) {
//                val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
//                    component = componentName
//                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
//                }
//                context.sendBroadcast(intent)
//                Log.d(TAG, "Updated ${T::class.simpleName}: ${widgetIds.size} widgets")
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "Error updating ${T::class.simpleName}", e)
//        }
//    }
//}