package com.ritesh.cashiro.widgets.providers

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.content.ComponentName
import android.util.Log
import com.ritesh.cashiro.MainActivity
import com.ritesh.cashiro.R
import com.ritesh.cashiro.receivers.WidgetUpdateBroadcastReceiver
import com.ritesh.cashiro.widgets.WidgetThemeHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class SmallAddTransactionWidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var widgetThemeHelper: WidgetThemeHelper

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            WidgetUpdateBroadcastReceiver.ACTION_UPDATE_THEME,
            WidgetUpdateBroadcastReceiver.ACTION_UPDATE_WIDGETS -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, SmallAddTransactionWidgetProvider::class.java)
                val widgetIds = appWidgetManager.getAppWidgetIds(componentName)
                onUpdate(context, appWidgetManager, widgetIds)
            }
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.add_transaction_1x1_widget)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val themeColors = widgetThemeHelper.getThemeColors()

                withContext(Dispatchers.Main) {
                    // Apply theme colors
                    views.setInt(R.id.widget_root, "setBackgroundColor", themeColors.backgroundColor)
                    views.setInt(R.id.widget_icon, "setColorFilter", themeColors.primaryColor)

                    setupIntent(context, views, appWidgetId)
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                    Log.d("SmallAddWidget", "Widget $appWidgetId updated with theme")
                }
            } catch (e: Exception) {
                Log.e("SmallAddWidget", "Error applying theme: ${e.message}", e)
                // Fallback to default setup
                withContext(Dispatchers.Main) {
                    setupIntent(context, views, appWidgetId)
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                }
            }
        }
    }

    private fun setupIntent(context: Context, views: RemoteViews, appWidgetId: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("openedFromWidget", true)
            putExtra("widgetAction", "ADD_TRANSACTION")
            putExtra("widgetId", appWidgetId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            appWidgetId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val provider = SmallAddTransactionWidgetProvider()
            provider.updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
}