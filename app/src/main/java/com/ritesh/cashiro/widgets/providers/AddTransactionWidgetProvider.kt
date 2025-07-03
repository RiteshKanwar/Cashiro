package com.ritesh.cashiro.widgets.providers

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.util.Log
import android.widget.RemoteViews
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ritesh.cashiro.MainActivity
import com.ritesh.cashiro.R
import com.ritesh.cashiro.domain.repository.ThemeMode
import com.ritesh.cashiro.presentation.ui.theme.Latte_Blue
import com.ritesh.cashiro.presentation.ui.theme.Latte_Surface_0
import com.ritesh.cashiro.presentation.ui.theme.Latte_Text
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Surface_0
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Text
import com.ritesh.cashiro.receivers.WidgetUpdateBroadcastReceiver
import com.ritesh.cashiro.widgets.WidgetThemeHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class AddTransactionWidgetProvider : AppWidgetProvider() {

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
                val componentName = ComponentName(context, AddTransactionWidgetProvider::class.java)
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
                    views.setTextColor(R.id.widget_title, themeColors.textColorPrimary)
                    views.setInt(R.id.widget_icon, "setColorFilter", themeColors.primaryColor)

                    setupIntent(context, views, appWidgetId)
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                    Log.d("AddTransactionWidget", "Widget $appWidgetId updated with theme")
                }
            } catch (e: Exception) {
                Log.e("AddTransactionWidget", "Error applying theme: ${e.message}", e)
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
            val provider = AddTransactionWidgetProvider()
            provider.updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
}