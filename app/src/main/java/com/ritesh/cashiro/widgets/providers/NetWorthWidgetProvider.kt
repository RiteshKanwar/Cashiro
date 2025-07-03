package com.ritesh.cashiro.widgets.providers

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.ritesh.cashiro.MainActivity
import com.ritesh.cashiro.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.ritesh.cashiro.domain.repository.AccountRepository
import com.ritesh.cashiro.domain.repository.TransactionRepository
import com.ritesh.cashiro.receivers.WidgetUpdateBroadcastReceiver
import com.ritesh.cashiro.widgets.BaseWidgetProvider
import com.ritesh.cashiro.widgets.FinancialSummary
import com.ritesh.cashiro.widgets.WidgetDataProvider
import com.ritesh.cashiro.widgets.WidgetThemeColors
import com.ritesh.cashiro.widgets.WidgetThemeHelper
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

@AndroidEntryPoint
class NetWorthWidgetProvider : BaseWidgetProvider() {

    @Inject
    lateinit var widgetDataProvider: WidgetDataProvider

    override fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.net_worth_2x1_widget)

        // Create click intent to open main app
        setupClickIntent(context, views, R.id.widget_root, MainActivity::class.java, appWidgetId)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val themeColors = widgetThemeHelper.getThemeColors()
                val financialSummary = widgetDataProvider.getFinancialSummary()

                withContext(Dispatchers.Main) {
                    applyThemeToViews(views, themeColors) { remoteViews, colors ->
                        remoteViews.setTextColor(R.id.widget_title, colors.textColorPrimary)
                        remoteViews.setTextColor(R.id.widget_count, colors.textColorSecondary)
                    }

                    updateWidgetData(views, appWidgetManager, appWidgetId, themeColors, financialSummary)
                }
            } catch (e: Exception) {
                Log.e("NetWorthWidget", "Error updating widget: ${e.message}", e)
                updateWidgetDataFallback(views, appWidgetManager, appWidgetId)
            }
        }
    }

    private fun updateWidgetData(
        views: RemoteViews,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        themeColors: WidgetThemeColors,
        financialSummary: FinancialSummary
    ) {
        try {
            val formatter = NumberFormat.getCurrencyInstance(Locale.US)
            val formattedBalance = formatter.format(financialSummary.totalBalance)

            views.setTextViewText(R.id.widget_amount, formattedBalance)
            views.setTextViewText(
                R.id.widget_count,
                "${financialSummary.transactionCount} transaction${if (financialSummary.transactionCount != 1) "s" else ""}"
            )

            val textColor = if (financialSummary.totalBalance >= 0) {
                themeColors.positiveColor
            } else {
                themeColors.negativeColor
            }
            views.setTextColor(R.id.widget_amount, textColor)

            appWidgetManager.updateAppWidget(appWidgetId, views)
            Log.d("NetWorthWidget", "Widget $appWidgetId updated with balance: $formattedBalance")
        } catch (e: Exception) {
            Log.e("NetWorthWidget", "Error updating widget data: ${e.message}", e)
            updateWidgetDataFallback(views, appWidgetManager, appWidgetId)
        }
    }

    private fun updateWidgetDataFallback(
        views: RemoteViews,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        views.setTextViewText(R.id.widget_amount, "$0.00")
        views.setTextViewText(R.id.widget_count, "0 transactions")
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}


//@AndroidEntryPoint
//class NetWorthWidgetProvider : AppWidgetProvider() {
//
//    @Inject
//    lateinit var accountRepository: AccountRepository
//
//    @Inject
//    lateinit var transactionRepository: TransactionRepository
//
//    @Inject
//    lateinit var widgetThemeHelper: WidgetThemeHelper
//
//    override fun onUpdate(
//        context: Context,
//        appWidgetManager: AppWidgetManager,
//        appWidgetIds: IntArray
//    ) {
//        Log.d("NetWorthWidget", "onUpdate called for ${appWidgetIds.size} widgets")
//        appWidgetIds.forEach { appWidgetId ->
//            updateAppWidget(context, appWidgetManager, appWidgetId)
//        }
//    }
//
//    override fun onReceive(context: Context, intent: Intent) {
//        super.onReceive(context, intent)
//
//        when (intent.action) {
//            WidgetUpdateBroadcastReceiver.ACTION_UPDATE_WIDGETS,
//            WidgetUpdateBroadcastReceiver.ACTION_DATA_CHANGED,
//            WidgetUpdateBroadcastReceiver.ACTION_UPDATE_THEME -> {
//                val appWidgetManager = AppWidgetManager.getInstance(context)
//                val componentName = ComponentName(context, NetWorthWidgetProvider::class.java)
//                val widgetIds = appWidgetManager.getAppWidgetIds(componentName)
//                onUpdate(context, appWidgetManager, widgetIds)
//            }
//        }
//    }
//
//    private fun updateAppWidget(
//        context: Context,
//        appWidgetManager: AppWidgetManager,
//        appWidgetId: Int
//    ) {
//        val views = RemoteViews(context.packageName, R.layout.net_worth_2x1_widget)
//
//        // Create click intent to open main app
//        val intent = Intent(context, MainActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
//        }
//        val pendingIntent = PendingIntent.getActivity(
//            context, appWidgetId + 3000, intent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//        views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
//
//        // Update widget data with theme
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val themeColors = widgetThemeHelper.getThemeColors()
//
//                // Apply theme
//                withContext(Dispatchers.Main) {
//                    views.setInt(R.id.widget_root, "setBackgroundColor", themeColors.surfaceColor)
//                    views.setTextColor(R.id.widget_title, themeColors.textColorSecondary)
//                    views.setTextColor(R.id.widget_count, themeColors.textColorSecondary)
//                }
//
//                updateWidgetData(context, views, appWidgetManager, appWidgetId, themeColors)
//            } catch (e: Exception) {
//                Log.e("NetWorthWidget", "Error updating widget: ${e.message}", e)
//                updateWidgetDataFallback(context, views, appWidgetManager, appWidgetId)
//            }
//        }
//    }
//
//    private suspend fun updateWidgetData(
//        context: Context,
//        views: RemoteViews,
//        appWidgetManager: AppWidgetManager,
//        appWidgetId: Int,
//        themeColors: WidgetThemeColors
//    ) {
//        try {
//            // Get total balance from accounts
//            val accounts = accountRepository.getAllAccounts()
//            val totalBalance = accounts.sumOf { it.balance }
//
//            // Get transaction count
//            val transactions = transactionRepository.getAllTransactions()
//            val transactionCount = transactions.size
//
//            // Format currency
//            val formatter = NumberFormat.getCurrencyInstance(Locale.US)
//            val formattedBalance = formatter.format(totalBalance)
//
//            withContext(Dispatchers.Main) {
//                // Update views
//                views.setTextViewText(R.id.widget_amount, formattedBalance)
//                views.setTextViewText(
//                    R.id.widget_count,
//                    "$transactionCount transaction${if (transactionCount != 1) "s" else ""}"
//                )
//
//                // Set text color based on balance
//                val textColor = if (totalBalance >= 0) {
//                    themeColors.positiveColor
//                } else {
//                    themeColors.negativeColor
//                }
//                views.setTextColor(R.id.widget_amount, textColor)
//
//                appWidgetManager.updateAppWidget(appWidgetId, views)
//                Log.d("NetWorthWidget", "Widget $appWidgetId updated with balance: $formattedBalance")
//            }
//        } catch (e: Exception) {
//            Log.e("NetWorthWidget", "Error updating widget data: ${e.message}", e)
//            updateWidgetDataFallback(context, views, appWidgetManager, appWidgetId)
//        }
//    }
//
//    private fun updateWidgetDataFallback(
//        context: Context,
//        views: RemoteViews,
//        appWidgetManager: AppWidgetManager,
//        appWidgetId: Int
//    ) {
//        views.setTextViewText(R.id.widget_amount, "$0.00")
//        views.setTextViewText(R.id.widget_count, "0 transactions")
//        appWidgetManager.updateAppWidget(appWidgetId, views)
//    }
//
//    companion object {
//        fun updateWidget(
//            context: Context,
//            appWidgetManager: AppWidgetManager,
//            appWidgetId: Int
//        ) {
//            val provider = NetWorthWidgetProvider()
//            provider.updateAppWidget(context, appWidgetManager, appWidgetId)
//        }
//    }
//}