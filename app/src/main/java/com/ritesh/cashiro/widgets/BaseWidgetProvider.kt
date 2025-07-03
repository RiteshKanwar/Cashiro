package com.ritesh.cashiro.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import com.ritesh.cashiro.receivers.WidgetUpdateBroadcastReceiver
import com.ritesh.cashiro.R
import javax.inject.Inject

abstract class BaseWidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var widgetThemeHelper: WidgetThemeHelper

    companion object {
        private const val TAG = "BaseWidgetProvider"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(TAG, "${this::class.simpleName} onUpdate called for ${appWidgetIds.size} widgets")
        appWidgetIds.forEach { appWidgetId ->
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            WidgetUpdateBroadcastReceiver.ACTION_UPDATE_WIDGETS,
            WidgetUpdateBroadcastReceiver.ACTION_DATA_CHANGED,
            WidgetUpdateBroadcastReceiver.ACTION_UPDATE_THEME -> {
                Log.d(TAG, "${this::class.simpleName} received update intent: ${intent.action}")
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, this::class.java)
                val widgetIds = appWidgetManager.getAppWidgetIds(componentName)
                onUpdate(context, appWidgetManager, widgetIds)
            }
        }
    }

    protected abstract fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    )

    /**
     * Apply theme consistently across all widgets
     */
    protected suspend fun applyThemeToViews(
        views: RemoteViews,
        themeColors: WidgetThemeColors,
        customizations: (RemoteViews, WidgetThemeColors) -> Unit = { _, _ -> }
    ) {
        try {
            // Apply basic theme colors
            views.setInt(R.id.widget_root, "setBackgroundColor", themeColors.backgroundColor)

            // Apply custom theme elements specific to each widget
            customizations(views, themeColors)

        } catch (e: Exception) {
            Log.e(TAG, "Error applying theme to views", e)
        }
    }

    /**
     * Setup click intents with proper flags
     */
    protected fun setupClickIntent(
        context: Context,
        views: RemoteViews,
        viewId: Int,
        targetActivity: Class<*>,
        appWidgetId: Int,
        extras: Bundle? = null
    ) {
        val intent = Intent(context, targetActivity).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            extras?.let { putExtras(it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            appWidgetId + viewId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        views.setOnClickPendingIntent(viewId, pendingIntent)
    }
}