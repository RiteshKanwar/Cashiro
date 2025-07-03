package com.ritesh.cashiro.widgets.providers

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import com.ritesh.cashiro.MainActivity
import com.ritesh.cashiro.R
import com.ritesh.cashiro.domain.repository.AccountRepository
import com.ritesh.cashiro.domain.repository.TransactionRepository
import com.ritesh.cashiro.receivers.WidgetUpdateBroadcastReceiver
import com.ritesh.cashiro.widgets.BaseWidgetProvider
import com.ritesh.cashiro.widgets.WidgetThemeColors
import com.ritesh.cashiro.widgets.WidgetThemeHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import java.text.NumberFormat
import java.util.Locale

@AndroidEntryPoint
class DashboardWidgetProvider : BaseWidgetProvider() {

    @Inject
    lateinit var accountRepository: AccountRepository

    @Inject
    lateinit var transactionRepository: TransactionRepository

    override fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.dashboard_2x2_widget)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val themeColors = widgetThemeHelper.getThemeColors()

                withContext(Dispatchers.Main) {
                    applyThemeToViews(views, themeColors) { remoteViews, colors ->
                        // Dashboard-specific theming
                        remoteViews.setTextColor(R.id.widget_header_title, colors.textColorPrimary)
                        remoteViews.setTextColor(R.id.widget_net_worth_label, colors.textColorPrimary)
                        remoteViews.setTextColor(R.id.widget_income_label, colors.textColorSecondary)
                        remoteViews.setTextColor(R.id.widget_expenses_label, colors.textColorSecondary)
                        remoteViews.setInt(R.id.add_button, "setColorFilter", colors.textColorPrimary)
                    }

                    setupWidgetIntents(context, views, appWidgetId)
                }

                updateWidgetData(context, views, appWidgetManager, appWidgetId, themeColors)

            } catch (e: Exception) {
                Log.e("DashboardWidget", "Error updating widget: ${e.message}", e)
                updateWidgetDataFallback(context, views, appWidgetManager, appWidgetId)
            }
        }
    }

    private fun setupWidgetIntents(context: Context, views: RemoteViews, appWidgetId: Int) {
        // Main widget click
        setupClickIntent(context, views, R.id.widget_root, MainActivity::class.java, appWidgetId)

        // Add button click
        val addExtras = Bundle().apply {
            putBoolean("openedFromWidget", true)
            putString("widgetAction", "ADD_TRANSACTION")
        }
        setupClickIntent(context, views, R.id.add_button, MainActivity::class.java, appWidgetId, addExtras)
    }

    private suspend fun updateWidgetData(
        context: Context,
        views: RemoteViews,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        themeColors: WidgetThemeColors
    ) {
        try {
            // Get latest financial data
            val accounts = accountRepository.getAllAccounts()
            val transactions = transactionRepository.getAllTransactions()

            val totalBalance = accounts.sumOf { it.balance }
            val incomeTransactions = transactions.filter { it.mode == "Income" }
            val expenseTransactions = transactions.filter { it.mode == "Expense" }

            val totalIncome = incomeTransactions.sumOf { it.amount }
            val totalExpenses = expenseTransactions.sumOf { it.amount }

            // Format currency
            val formatter = NumberFormat.getCurrencyInstance(Locale.US)
            val formattedBalance = formatter.format(totalBalance)
            val formattedIncome = formatter.format(totalIncome)
            val formattedExpenses = formatter.format(totalExpenses)

            withContext(Dispatchers.Main) {
                views.setTextViewText(R.id.widget_net_worth, formattedBalance)
                views.setTextViewText(R.id.widget_income, formattedIncome)
                views.setTextViewText(R.id.widget_expenses, formattedExpenses)

                // Set balance color based on positive/negative
                val balanceColor = if (totalBalance >= 0) {
                    themeColors.positiveColor
                } else {
                    themeColors.negativeColor
                }
                views.setTextColor(R.id.widget_net_worth, balanceColor)
                views.setTextColor(R.id.widget_income, themeColors.positiveColor)
                views.setTextColor(R.id.widget_expenses, themeColors.negativeColor)

                appWidgetManager.updateAppWidget(appWidgetId, views)
                Log.d("DashboardWidget", "Widget $appWidgetId updated successfully")
            }
        } catch (e: Exception) {
            Log.e("DashboardWidget", "Error updating widget data: ${e.message}", e)
            updateWidgetDataFallback(context, views, appWidgetManager, appWidgetId)
        }
    }

    private fun updateWidgetDataFallback(
        context: Context,
        views: RemoteViews,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        views.setTextViewText(R.id.widget_net_worth, "$0.00")
        views.setTextViewText(R.id.widget_income, "$0.00")
        views.setTextViewText(R.id.widget_expenses, "$0.00")
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}

//@AndroidEntryPoint
//class DashboardWidgetProvider : AppWidgetProvider() {
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
//        Log.d("DashboardWidget", "onUpdate called for ${appWidgetIds.size} widgets")
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
//                val componentName = ComponentName(context, DashboardWidgetProvider::class.java)
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
//        val views = RemoteViews(context.packageName, R.layout.dashboard_2x2_widget)
//
//        // Apply theme colors
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                val themeColors = widgetThemeHelper.getThemeColors()
//
//                // Apply theme colors to widget
//                withContext(Dispatchers.Main) {
//                    applyThemeToWidget(views, themeColors)
//                }
//
//                // Set up click intents
//                setupWidgetIntents(context, views, appWidgetId)
//
//                // Update widget data
//                updateWidgetData(context, views, appWidgetManager, appWidgetId, themeColors)
//
//            } catch (e: Exception) {
//                Log.e("DashboardWidget", "Error updating widget: ${e.message}", e)
//                // Fallback to default update
//                setupWidgetIntents(context, views, appWidgetId)
//                updateWidgetDataFallback(context, views, appWidgetManager, appWidgetId)
//            }
//        }
//    }
//
//    private fun applyThemeToWidget(views: RemoteViews, themeColors: WidgetThemeColors) {
//        // Apply background colors
//        views.setInt(R.id.widget_root, "setBackgroundColor", themeColors.surfaceColor)
//
//        // Apply text colors
//        views.setTextColor(R.id.widget_net_worth, themeColors.textColorPrimary)
//        views.setTextColor(R.id.widget_income, themeColors.positiveColor)
//        views.setTextColor(R.id.widget_expenses, themeColors.negativeColor)
//
//        // Apply primary color to add button
//        views.setInt(R.id.add_button, "setColorFilter", themeColors.surfaceColor)
//
//        // Set header text color
//        views.setTextColor(R.id.widget_title, themeColors.textColorPrimary)
//    }
//
//    private fun setupWidgetIntents(context: Context, views: RemoteViews, appWidgetId: Int) {
//        // Main widget click - open app
//        val mainIntent = Intent(context, MainActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
//        }
//        val mainPendingIntent = PendingIntent.getActivity(
//            context, appWidgetId + 6000, mainIntent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//        views.setOnClickPendingIntent(R.id.widget_root, mainPendingIntent)
//
//        // Add button click - open add transaction
//        val addIntent = Intent(context, MainActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
//            putExtra("openedFromWidget", true)
//            putExtra("widgetAction", "ADD_TRANSACTION")
//        }
//        val addPendingIntent = PendingIntent.getActivity(
//            context, appWidgetId + 6100, addIntent,
//            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//        views.setOnClickPendingIntent(R.id.add_button, addPendingIntent)
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
//            // Get transactions and calculate income/expenses
//            val transactions = transactionRepository.getAllTransactions()
//            val incomeTransactions = transactions.filter { it.mode == "Income" }
//            val expenseTransactions = transactions.filter { it.mode == "Expense" }
//
//            val totalIncome = incomeTransactions.sumOf { it.amount }
//            val totalExpenses = expenseTransactions.sumOf { it.amount }
//
//            // Format currency
//            val formatter = NumberFormat.getCurrencyInstance(Locale.US)
//            val formattedBalance = formatter.format(totalBalance)
//            val formattedIncome = formatter.format(totalIncome)
//            val formattedExpenses = formatter.format(totalExpenses)
//
//            withContext(Dispatchers.Main) {
//                // Update views
//                views.setTextViewText(R.id.widget_net_worth, formattedBalance)
//                views.setTextViewText(R.id.widget_income, formattedIncome)
//                views.setTextViewText(R.id.widget_expenses, formattedExpenses)
//
//                // Set balance color based on positive/negative
//                val balanceColor = if (totalBalance >= 0) {
//                    themeColors.positiveColor
//                } else {
//                    themeColors.negativeColor
//                }
//                views.setTextColor(R.id.widget_net_worth, balanceColor)
//
//                appWidgetManager.updateAppWidget(appWidgetId, views)
//                Log.d("DashboardWidget", "Widget $appWidgetId updated successfully with balance: $formattedBalance")
//            }
//        } catch (e: Exception) {
//            Log.e("DashboardWidget", "Error updating widget data: ${e.message}", e)
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
//        // Fallback to default values with system colors
//        views.setTextViewText(R.id.widget_net_worth, "$0.00")
//        views.setTextViewText(R.id.widget_income, "$0.00")
//        views.setTextViewText(R.id.widget_expenses, "$0.00")
//        appWidgetManager.updateAppWidget(appWidgetId, views)
//        Log.d("DashboardWidget", "Widget $appWidgetId updated with fallback values")
//    }
//}
