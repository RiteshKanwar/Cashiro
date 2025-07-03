package com.ritesh.cashiro.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ritesh.cashiro.domain.repository.AccountRepository
import com.ritesh.cashiro.domain.repository.TransactionRepository
import com.ritesh.cashiro.domain.utils.AccountEvent
import com.ritesh.cashiro.domain.utils.AppEventBus
import com.ritesh.cashiro.domain.utils.CurrencyEvent
import com.ritesh.cashiro.domain.utils.TransactionEvent
import com.ritesh.cashiro.presentation.ui.features.appearance.SettingsEvent
import com.ritesh.cashiro.receivers.WidgetUpdateBroadcastReceiver
import com.ritesh.cashiro.widgets.providers.AddTransactionWidgetProvider
import com.ritesh.cashiro.widgets.providers.DashboardWidgetProvider
import com.ritesh.cashiro.widgets.providers.NetWorthWidgetProvider
import com.ritesh.cashiro.widgets.providers.SmallAddTransactionWidgetProvider
import com.ritesh.cashiro.widgets.providers.WideBalanceWidgetProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.jvm.java

@Singleton
class WidgetUpdateUtil @Inject constructor(
    @ApplicationContext val context: Context,
    private val widgetThemeHelper: WidgetThemeHelper // Direct injection now works!
) {

    companion object {
        const val TAG = "WidgetUpdateUtil"
    }

    init {
        // Listen for settings changes and update widgets accordingly
        CoroutineScope(Dispatchers.IO).launch {
            AppEventBus.events.collect { event ->
                when (event) {
                    is SettingsEvent.ThemeChanged,
                    is SettingsEvent.PrimaryColorChanged -> {
                        Log.d(TAG, "Settings changed, updating widget themes")
                        updateWidgetsTheme()
                    }

                    is AccountEvent.BalanceUpdated,
                    is AccountEvent.AccountsUpdated,
                    is AccountEvent.AccountDeleted,
                    is TransactionEvent.TransactionsUpdated -> {
                        Log.d(TAG, "Financial data changed, updating widgets")
                        updateAllFinancialWidgets()
                    }

                    is CurrencyEvent.AccountCurrencyChanged,
                    is CurrencyEvent.MainAccountCurrencyChanged,
                    is CurrencyEvent.ConversionRatesUpdated -> {
                        Log.d(TAG, "Currency data changed, updating widgets")
                        updateAllFinancialWidgets()
                    }
                }
            }
        }
    }

    /**
     * Update all widgets with latest data and theme
     */
    fun updateAllWidgets() {
        Log.d(TAG, "Updating all widgets with latest data and theme")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Clear theme cache to ensure fresh theme data
                widgetThemeHelper.clearCache()

                // Send explicit broadcast to update all widgets
                sendExplicitBroadcast(WidgetUpdateBroadcastReceiver.ACTION_UPDATE_WIDGETS)

                // Also force update each widget type individually for reliability
                forceUpdateAllWidgetTypes()

            } catch (e: Exception) {
                Log.e(TAG, "Error updating all widgets", e)
            }
        }
    }

    /**
     * Update widgets when theme changes
     */
    fun updateWidgetsTheme() {
        Log.d(TAG, "Updating widgets theme")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Clear cached theme colors
                widgetThemeHelper.clearCache()

                // Send explicit theme update broadcast
                sendExplicitBroadcast(WidgetUpdateBroadcastReceiver.ACTION_UPDATE_THEME)

                // Force update all widgets with new theme
                forceUpdateAllWidgetTypes()

            } catch (e: Exception) {
                Log.e(TAG, "Error updating widgets theme", e)
            }
        }
    }

    /**
     * Update widgets when financial data changes
     */
    fun updateAllFinancialWidgets() {
        Log.d(TAG, "Updating financial widgets with latest data")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Send explicit data change broadcast
                sendExplicitBroadcast(WidgetUpdateBroadcastReceiver.ACTION_DATA_CHANGED)

                // Force update financial widgets specifically
                forceUpdateWidgetType<DashboardWidgetProvider>()
                forceUpdateWidgetType<NetWorthWidgetProvider>()
                forceUpdateWidgetType<WideBalanceWidgetProvider>()

            } catch (e: Exception) {
                Log.e(TAG, "Error updating financial widgets", e)
            }
        }
    }

    /**
     * Send explicit broadcast to avoid implicit intent issues
     */
    private fun sendExplicitBroadcast(action: String) {
        try {
            val intent = Intent(action).apply {
                // Make intent explicit by setting the component
                component = ComponentName(context, WidgetUpdateBroadcastReceiver::class.java)
            }
            context.sendBroadcast(intent)
            Log.d(TAG, "Sent explicit broadcast: $action")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending explicit broadcast: $action", e)
        }
    }

    /**
     * Force update all widget types
     */
    private suspend fun forceUpdateAllWidgetTypes() {
        try {
            forceUpdateWidgetType<DashboardWidgetProvider>()
            forceUpdateWidgetType<NetWorthWidgetProvider>()
            forceUpdateWidgetType<WideBalanceWidgetProvider>()
            forceUpdateWidgetType<AddTransactionWidgetProvider>()
            forceUpdateWidgetType<SmallAddTransactionWidgetProvider>()
        } catch (e: Exception) {
            Log.e(TAG, "Error force updating widget types", e)
        }
    }

    /**
     * Force update specific widget type using explicit intents
     */
    inline fun <reified T : AppWidgetProvider> forceUpdateWidgetType() {
        try {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, T::class.java)
            val widgetIds = appWidgetManager.getAppWidgetIds(componentName)

            if (widgetIds.isNotEmpty()) {
                val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE).apply {
                    // Make intent explicit by setting the component
                    component = componentName
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
                }
                context.sendBroadcast(intent)
                Log.d(TAG, "Force updated ${T::class.simpleName} widgets: ${widgetIds.contentToString()}")
            } else {
                Log.d(TAG, "No ${T::class.simpleName} widgets found to update")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error force updating ${T::class.simpleName}", e)
        }
    }

    /**
     * Manual trigger methods for testing or specific scenarios
     */
    fun triggerThemeUpdate() {
        Log.d(TAG, "Manual theme update triggered")
        updateWidgetsTheme()
    }

    fun triggerDataUpdate() {
        Log.d(TAG, "Manual data update triggered")
        updateAllFinancialWidgets()
    }

    fun triggerFullUpdate() {
        Log.d(TAG, "Manual full update triggered")
        updateAllWidgets()
    }
}

//@Singleton
//class WidgetUpdateUtil @Inject constructor(
//    @ApplicationContext val context: Context,
//    private val accountRepository: AccountRepository,
//    private val transactionRepository: TransactionRepository
//) {
//
//    // Inject WidgetThemeHelper lazily to avoid circular dependency
//    @Inject
//    lateinit var widgetThemeHelper: Lazy<WidgetThemeHelper>
//
//    companion object {
//        const val TAG = "WidgetUpdateUtil"
//    }
//
//    init {
//        // Listen for settings changes and update widgets accordingly
//        CoroutineScope(Dispatchers.IO).launch {
//            AppEventBus.events.collect { event ->
//                when (event) {
//                    is SettingsEvent.ThemeChanged,
//                    is SettingsEvent.PrimaryColorChanged -> {
//                        Log.d(TAG, "Settings changed, updating widget themes")
//                        updateWidgetsTheme()
//                    }
//
//                    is AccountEvent.BalanceUpdated,
//                    is AccountEvent.AccountsUpdated,
//                    is AccountEvent.AccountDeleted,
//                    is TransactionEvent.TransactionsUpdated -> {
//                        Log.d(TAG, "Financial data changed, updating widgets")
//                        updateAllFinancialWidgets()
//                    }
//
//                    is CurrencyEvent.AccountCurrencyChanged,
//                    is CurrencyEvent.MainAccountCurrencyChanged,
//                    is CurrencyEvent.ConversionRatesUpdated -> {
//                        Log.d(TAG, "Currency data changed, updating widgets")
//                        updateAllFinancialWidgets()
//                    }
//                }
//            }
//        }
//    }
//
//    /**
//     * Update all widgets with latest data and theme
//     */
//    fun updateAllWidgets() {
//        Log.d(TAG, "Updating all widgets with latest data and theme")
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                // Clear theme cache to ensure fresh theme data
//                widgetThemeHelper.get().clearCache()
//
//                // Send broadcast to update all widgets
//                val intent = Intent(WidgetUpdateBroadcastReceiver.ACTION_UPDATE_WIDGETS)
//                context.sendBroadcast(intent)
//
//                // Also force update each widget type individually for reliability
//                forceUpdateAllWidgetTypes()
//
//            } catch (e: Exception) {
//                Log.e(TAG, "Error updating all widgets", e)
//            }
//        }
//    }
//
//    /**
//     * Update widgets when theme changes
//     */
//    fun updateWidgetsTheme() {
//        Log.d(TAG, "Updating widgets theme")
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                // Clear cached theme colors
//                widgetThemeHelper.get().clearCache()
//
//                // Send theme update broadcast
//                val intent = Intent(WidgetUpdateBroadcastReceiver.ACTION_UPDATE_THEME)
//                context.sendBroadcast(intent)
//
//                // Force update all widgets with new theme
//                forceUpdateAllWidgetTypes()
//
//            } catch (e: Exception) {
//                Log.e(TAG, "Error updating widgets theme", e)
//            }
//        }
//    }
//
//    /**
//     * Update widgets when financial data changes
//     */
//    fun updateAllFinancialWidgets() {
//        Log.d(TAG, "Updating financial widgets with latest data")
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                // Send data change broadcast
//                val intent = Intent(WidgetUpdateBroadcastReceiver.ACTION_DATA_CHANGED)
//                context.sendBroadcast(intent)
//
//                // Force update financial widgets specifically
//                forceUpdateWidgetType<DashboardWidgetProvider>()
//                forceUpdateWidgetType<NetWorthWidgetProvider>()
//                forceUpdateWidgetType<QuickBalanceWidgetProvider>()
//                forceUpdateWidgetType<WideBalanceWidgetProvider>()
//
//            } catch (e: Exception) {
//                Log.e(TAG, "Error updating financial widgets", e)
//            }
//        }
//    }
//
//    /**
//     * Force update all widget types
//     */
//    private suspend fun forceUpdateAllWidgetTypes() {
//        try {
//            forceUpdateWidgetType<DashboardWidgetProvider>()
//            forceUpdateWidgetType<NetWorthWidgetProvider>()
//            forceUpdateWidgetType<QuickBalanceWidgetProvider>()
//            forceUpdateWidgetType<WideBalanceWidgetProvider>()
//            forceUpdateWidgetType<AddTransactionWidgetProvider>()
//            forceUpdateWidgetType<TransferTransactionWidgetProvider>()
//            forceUpdateWidgetType<SmallAddTransactionWidgetProvider>()
//            forceUpdateWidgetType<SmallTransferWidgetProvider>()
//        } catch (e: Exception) {
//            Log.e(TAG, "Error force updating widget types", e)
//        }
//    }
//
//    /**
//     * Force update specific widget type
//     */
//    inline fun <reified T : AppWidgetProvider> forceUpdateWidgetType() {
//        try {
//            val appWidgetManager = AppWidgetManager.getInstance(context)
//            val componentName = ComponentName(context, T::class.java)
//            val widgetIds = appWidgetManager.getAppWidgetIds(componentName)
//
//            if (widgetIds.isNotEmpty()) {
//                val intent = Intent(context, T::class.java).apply {
//                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
//                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
//                }
//                context.sendBroadcast(intent)
//                Log.d(TAG, "Force updated ${T::class.simpleName} widgets: ${widgetIds.contentToString()}")
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "Error force updating ${T::class.simpleName}", e)
//        }
//    }
//}

//@Singleton
//class WidgetUpdateUtil @Inject constructor(
//    @ApplicationContext val context: Context,
//    private val widgetThemeHelper: WidgetThemeHelper,
//    private val accountRepository: AccountRepository,
//    private val transactionRepository: TransactionRepository
//) {
//
//    companion object {
//        const val TAG = "WidgetUpdateUtil"
//    }
//
//    /**
//     * Update all widgets with latest data and theme
//     */
//    fun updateAllWidgets() {
//        Log.d(TAG, "Updating all widgets with latest data and theme")
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                // Clear theme cache to ensure fresh theme data
//                widgetThemeHelper.clearCache()
//
//                // Send broadcast to update all widgets
//                val intent = Intent(WidgetUpdateBroadcastReceiver.ACTION_UPDATE_WIDGETS)
//                context.sendBroadcast(intent)
//
//                // Also force update each widget type individually for reliability
//                forceUpdateAllWidgetTypes()
//
//            } catch (e: Exception) {
//                Log.e(TAG, "Error updating all widgets", e)
//            }
//        }
//    }
//
//    /**
//     * Update widgets when theme changes
//     */
//    fun updateWidgetsTheme() {
//        Log.d(TAG, "Updating widgets theme")
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                // Clear cached theme colors
//                widgetThemeHelper.clearCache()
//
//                // Send theme update broadcast
//                val intent = Intent(WidgetUpdateBroadcastReceiver.ACTION_UPDATE_THEME)
//                context.sendBroadcast(intent)
//
//                // Force update all widgets with new theme
//                forceUpdateAllWidgetTypes()
//
//            } catch (e: Exception) {
//                Log.e(TAG, "Error updating widgets theme", e)
//            }
//        }
//    }
//
//    /**
//     * Update widgets when financial data changes
//     */
//    fun updateAllFinancialWidgets() {
//        Log.d(TAG, "Updating financial widgets with latest data")
//        CoroutineScope(Dispatchers.IO).launch {
//            try {
//                // Send data change broadcast
//                val intent = Intent(WidgetUpdateBroadcastReceiver.ACTION_DATA_CHANGED)
//                context.sendBroadcast(intent)
//
//                // Force update financial widgets specifically
//                forceUpdateWidgetType<DashboardWidgetProvider>()
//                forceUpdateWidgetType<NetWorthWidgetProvider>()
//                forceUpdateWidgetType<QuickBalanceWidgetProvider>()
//                forceUpdateWidgetType<WideBalanceWidgetProvider>()
//
//            } catch (e: Exception) {
//                Log.e(TAG, "Error updating financial widgets", e)
//            }
//        }
//    }
//
//    /**
//     * Force update all widget types
//     */
//    private suspend fun forceUpdateAllWidgetTypes() {
//        try {
//            forceUpdateWidgetType<DashboardWidgetProvider>()
//            forceUpdateWidgetType<NetWorthWidgetProvider>()
//            forceUpdateWidgetType<QuickBalanceWidgetProvider>()
//            forceUpdateWidgetType<WideBalanceWidgetProvider>()
//            forceUpdateWidgetType<AddTransactionWidgetProvider>()
//            forceUpdateWidgetType<TransferTransactionWidgetProvider>()
//            forceUpdateWidgetType<SmallAddTransactionWidgetProvider>()
//            forceUpdateWidgetType<SmallTransferWidgetProvider>()
//        } catch (e: Exception) {
//            Log.e(TAG, "Error force updating widget types", e)
//        }
//    }
//
//    /**
//     * Force update specific widget type
//     */
//    inline fun <reified T : AppWidgetProvider> forceUpdateWidgetType() {
//        try {
//            val appWidgetManager = AppWidgetManager.getInstance(context)
//            val componentName = ComponentName(context, T::class.java)
//            val widgetIds = appWidgetManager.getAppWidgetIds(componentName)
//
//            if (widgetIds.isNotEmpty()) {
//                val intent = Intent(context, T::class.java).apply {
//                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
//                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
//                }
//                context.sendBroadcast(intent)
//                Log.d(TAG, "Force updated ${T::class.simpleName} widgets: ${widgetIds.contentToString()}")
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "Error force updating ${T::class.simpleName}", e)
//        }
//    }
//
//    /**
//     * Update widgets when settings change
//     */
//    fun onSettingsChanged() {
//        Log.d(TAG, "Settings changed, updating widgets")
//        updateWidgetsTheme()
//    }
//
//    /**
//     * Update widgets when account data changes
//     */
//    fun onAccountDataChanged() {
//        Log.d(TAG, "Account data changed, updating financial widgets")
//        updateAllFinancialWidgets()
//    }
//
//    /**
//     * Update widgets when transaction data changes
//     */
//    fun onTransactionDataChanged() {
//        Log.d(TAG, "Transaction data changed, updating financial widgets")
//        updateAllFinancialWidgets()
//    }
//}

//@Singleton
//class WidgetUpdateUtil @Inject constructor(
//    @ApplicationContext val context: Context,
//    private val widgetThemeHelper: WidgetThemeHelper
//) {
//
//    /**
//     * Send broadcast to update all widgets immediately
//     */
//    fun updateAllWidgets() {
//        Log.d("WidgetUpdate", "Broadcasting widget update to all widgets")
//        val intent = Intent(WidgetUpdateBroadcastReceiver.ACTION_UPDATE_WIDGETS)
//        context.sendBroadcast(intent)
//    }
//
//    /**
//     * Update widgets when theme changes
//     */
//    fun updateWidgetsTheme() {
//        Log.d("WidgetUpdate", "Broadcasting theme update to all widgets")
//        val intent = Intent(WidgetUpdateBroadcastReceiver.ACTION_UPDATE_THEME)
//        context.sendBroadcast(intent)
//    }
//
//    /**
//     * Update widgets when data changes
//     */
//    fun updateAllFinancialWidgets() {
//        Log.d("WidgetUpdate", "Broadcasting data change to financial widgets")
//        val intent = Intent(WidgetUpdateBroadcastReceiver.ACTION_DATA_CHANGED)
//        context.sendBroadcast(intent)
//    }
//
//    /**
//     * Force update specific widget type
//     */
//    inline fun <reified T : AppWidgetProvider> forceUpdateWidgetType() {
//        val appWidgetManager = AppWidgetManager.getInstance(context)
//        val componentName = ComponentName(context, T::class.java)
//        val widgetIds = appWidgetManager.getAppWidgetIds(componentName)
//
//        if (widgetIds.isNotEmpty()) {
//            val intent = Intent(context, T::class.java).apply {
//                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
//                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds)
//            }
//            context.sendBroadcast(intent)
//            Log.d("WidgetUpdate", "Force updated ${T::class.simpleName} widgets: ${widgetIds.contentToString()}")
//        }
//    }
//}