package com.ritesh.cashiro.widgets.providers

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.ritesh.cashiro.MainActivity
import com.ritesh.cashiro.R
import com.ritesh.cashiro.domain.repository.AccountRepository
import com.ritesh.cashiro.domain.repository.TransactionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.text.NumberFormat
import java.util.Locale
import android.content.ComponentName
import android.util.Log
import com.ritesh.cashiro.receivers.WidgetUpdateBroadcastReceiver
import com.ritesh.cashiro.widgets.WidgetThemeColors
import com.ritesh.cashiro.widgets.WidgetThemeHelper
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class WideBalanceWidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var accountRepository: AccountRepository

    @Inject
    lateinit var transactionRepository: TransactionRepository

    @Inject
    lateinit var widgetThemeHelper: WidgetThemeHelper

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d("WideBalanceWidget", "onUpdate called for ${appWidgetIds.size} widgets")
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
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, WideBalanceWidgetProvider::class.java)
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
        val views = RemoteViews(context.packageName, R.layout.wide_balance_3x1_widget)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val themeColors = widgetThemeHelper.getThemeColors()

                withContext(Dispatchers.Main) {
                    // Apply theme
                    views.setInt(R.id.widget_root, "setBackgroundColor", themeColors.backgroundColor)
                    views.setTextColor(R.id.widget_title, themeColors.textColorPrimary)
                    views.setTextColor(R.id.widget_count, themeColors.textColorSecondary)
                    views.setInt(R.id.add_button, "setColorFilter", themeColors.textColorPrimary)
                }

                setupIntents(context, views, appWidgetId)
                updateWidgetData(context, views, appWidgetManager, appWidgetId, themeColors)

            } catch (e: Exception) {
                Log.e("WideBalanceWidget", "Error updating widget: ${e.message}", e)
                setupIntents(context, views, appWidgetId)
                updateWidgetDataFallback(context, views, appWidgetManager, appWidgetId)
            }
        }
    }

    private fun setupIntents(context: Context, views: RemoteViews, appWidgetId: Int) {
        // Main widget click
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val mainPendingIntent = PendingIntent.getActivity(
            context, appWidgetId + 5000, mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, mainPendingIntent)

        // Add button click
        val addIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("openedFromWidget", true)
            putExtra("widgetAction", "ADD_TRANSACTION")
        }
        val addPendingIntent = PendingIntent.getActivity(
            context, appWidgetId + 5100, addIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.add_button, addPendingIntent)
    }

    private suspend fun updateWidgetData(
        context: Context,
        views: RemoteViews,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        themeColors: WidgetThemeColors
    ) {
        try {
            val accounts = accountRepository.getAllAccounts()
            val totalBalance = accounts.sumOf { it.balance }
            val transactions = transactionRepository.getAllTransactions()
            val transactionCount = transactions.size

            val formatter = NumberFormat.getCurrencyInstance(Locale.US)
            val formattedBalance = formatter.format(totalBalance)

            withContext(Dispatchers.Main) {
                views.setTextViewText(R.id.widget_amount, formattedBalance)
                views.setTextViewText(
                    R.id.widget_count,
                    "$transactionCount transaction${if (transactionCount != 1) "s" else ""}"
                )

                val textColor = if (totalBalance >= 0) {
                    themeColors.positiveColor
                } else {
                    themeColors.negativeColor
                }
                views.setTextColor(R.id.widget_amount, textColor)

                appWidgetManager.updateAppWidget(appWidgetId, views)
                Log.d("WideBalanceWidget", "Widget $appWidgetId updated successfully")
            }
        } catch (e: Exception) {
            Log.e("WideBalanceWidget", "Error updating widget data: ${e.message}", e)
            updateWidgetDataFallback(context, views, appWidgetManager, appWidgetId)
        }
    }

    private fun updateWidgetDataFallback(
        context: Context,
        views: RemoteViews,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        views.setTextViewText(R.id.widget_amount, "$0.00")
        views.setTextViewText(R.id.widget_count, "0 transactions")
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}