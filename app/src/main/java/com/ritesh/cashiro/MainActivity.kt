package com.ritesh.cashiro

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenViewModel
import com.ritesh.cashiro.presentation.ui.features.activity_logs.ActivityLogViewModel
import com.ritesh.cashiro.presentation.ui.features.add_transaction.AddTransactionScreenViewModel
import com.ritesh.cashiro.presentation.ui.features.appearance.AppearanceViewModel
import com.ritesh.cashiro.presentation.ui.features.backup_restore.DataManagementViewModel
import com.ritesh.cashiro.presentation.ui.features.categories.CategoryScreenViewModel
import com.ritesh.cashiro.presentation.ui.features.categories.SubCategoryViewModel
import com.ritesh.cashiro.presentation.ui.features.currency.CurrencyViewModel
import com.ritesh.cashiro.presentation.ui.features.notifications.NotificationsViewModel
import com.ritesh.cashiro.presentation.ui.features.onboarding.OnBoardingViewModel
import com.ritesh.cashiro.presentation.ui.features.profile.ProfileScreenViewModel
import com.ritesh.cashiro.presentation.ui.features.reports.ReportScreenViewModel
import com.ritesh.cashiro.presentation.ui.features.schedules.ScheduleViewModel
import com.ritesh.cashiro.presentation.ui.features.subscriptions.SubscriptionViewModel
import com.ritesh.cashiro.presentation.ui.navigation.AppNavigation
import com.ritesh.cashiro.presentation.ui.navigation.NavGraph
import com.ritesh.cashiro.presentation.ui.theme.CashiroTheme
import com.ritesh.cashiro.widgets.WidgetEventListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val appearanceViewModel: AppearanceViewModel by viewModels()
    private val categoryViewModel: CategoryScreenViewModel by viewModels()
    private val subCategoryViewModel: SubCategoryViewModel by viewModels()
    private val accountViewModel: AccountScreenViewModel by viewModels()
    private val transactionViewModel: AddTransactionScreenViewModel by viewModels()
    private val currencyViewModel: CurrencyViewModel by viewModels()
    private val profileViewModel: ProfileScreenViewModel by viewModels()
    private val reportViewModel: ReportScreenViewModel by viewModels()
    private val dataManagementViewModel: DataManagementViewModel by viewModels()
    private val notificationsViewModel: NotificationsViewModel by viewModels()
    private val onBoardingViewModel: OnBoardingViewModel by viewModels()
    private val scheduleViewModel: ScheduleViewModel by viewModels()
    private val activityLogViewModel: ActivityLogViewModel by viewModels()
    private val subscriptionViewModel: SubscriptionViewModel by viewModels()

    private fun linkViewModels() {
        transactionViewModel.setAccountViewModel(accountViewModel)
    }

    // Store navigation data for notifications, tiles, and widgets
    private var externalNavigationData: ExternalNavigationData? = null

    data class ExternalNavigationData(
        val navigateTo: String,
        val isUpdateTransaction: Boolean = false,
        val transactionId: Int = 0,
        val defaultTab: String? = null,
        val openedFromTile: Boolean = false,
        val openedFromWidget: Boolean = false,
        val widgetAction: String? = null
    )
    @Inject
    lateinit var widgetEventListener: WidgetEventListener

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.auto(Color.Transparent.toArgb(), Color.Transparent.toArgb())
        )
        installSplashScreen()
        linkViewModels()

        // Extract navigation data from intent (notifications, tiles, widgets)
        val navigationData = extractExternalNavigationData(intent)
        externalNavigationData = navigationData

        widgetEventListener.startListening()

        lifecycleScope.launch {
            delay(100)

            setContent {
                val appearance by appearanceViewModel.settings.collectAsState()

                CashiroTheme(
                    themeMode = appearance.themeMode,
                    primaryColor = appearance.primaryColor
                ) {
                    val navController = rememberNavController()

                    // Handle navigation after NavController is ready
                    LaunchedEffect(navigationData) {
                        navigationData?.let { data ->
                            when {
                                data.openedFromTile -> {
                                    // Skip splash screen and go directly to HOME, then to AddTransaction
                                    handleTileNavigation(data, navController)
                                }
                                data.openedFromWidget -> {
                                    // Handle widget navigation (similar to tile but with widget-specific logic)
                                    handleWidgetNavigation(data, navController)
                                }
                                else -> {
                                    // Regular notification navigation
                                    handleNotificationNavigation(data, navController)
                                }
                            }
                            externalNavigationData = null // Clear after handling
                        }
                    }

                    // Determine start destination based on whether opened from tile or widget
                    val startDestination = if (navigationData?.openedFromTile == true || navigationData?.openedFromWidget == true) {
                        NavGraph.HOME // Skip splash when opened from tile or widget
                    } else {
                        NavGraph.SPLASH // Normal app startup
                    }

                    // Use our extracted navigation component
                    AppNavigation(
                        transactionViewModel = transactionViewModel,
                        categoryViewModel = categoryViewModel,
                        subCategoryViewModel = subCategoryViewModel,
                        accountViewModel = accountViewModel,
                        appearanceViewModel = appearanceViewModel,
                        currencyViewModel = currencyViewModel,
                        profileViewModel = profileViewModel,
                        reportViewModel = reportViewModel,
                        dataManagementViewModel = dataManagementViewModel,
                        notificationsViewModel = notificationsViewModel,
                        onBoardingViewModel = onBoardingViewModel,
                        subscriptionViewModel = subscriptionViewModel,
                        scheduleViewModel = scheduleViewModel,
                        activityLogViewModel = activityLogViewModel,
                        navController = navController,
                        labelVisibility = appearance.labelVisibility,
                        startDestination = startDestination // Pass the determined start destination
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // Important: update the activity's intent

        // Extract navigation data from new intent (notifications, tiles, widgets)
        externalNavigationData = extractExternalNavigationData(intent)
    }

    private fun extractExternalNavigationData(intent: Intent): ExternalNavigationData? {
        // Check for notification navigation
        val navigateTo = intent.getStringExtra("navigate_to")

        // Check for widget navigation
        val openedFromWidget = intent.getBooleanExtra("openedFromWidget", false)
        val widgetAction = intent.getStringExtra("widgetAction")

        // Check for tile navigation
        val openedFromTile = intent.getBooleanExtra("opened_from_tile", false)

        return when {
            // Handle widget navigation
            openedFromWidget && widgetAction != null -> {
                val defaultTab = intent.getStringExtra("defaultTab")

                // Clear widget intent extras to prevent repeated navigation
                intent.removeExtra("openedFromWidget")
                intent.removeExtra("widgetAction")
                intent.removeExtra("defaultTab")
                intent.removeExtra("widgetId")

                ExternalNavigationData(
                    navigateTo = "ADD_TRANSACTION",
                    isUpdateTransaction = false,
                    transactionId = 0,
                    defaultTab = defaultTab,
                    openedFromTile = false,
                    openedFromWidget = true,
                    widgetAction = widgetAction
                )
            }

            // Handle notification/tile navigation (existing logic)
            navigateTo == "ADD_TRANSACTION" -> {
                val isUpdateTransaction = intent.getBooleanExtra("is_update_transaction", false)
                val transactionId = intent.getIntExtra("transaction_id", 0)
                val defaultTab = intent.getStringExtra("defaultTab")

                // Clear the intent extras to prevent repeated navigation
                intent.removeExtra("navigate_to")
                intent.removeExtra("is_update_transaction")
                intent.removeExtra("transaction_id")
                intent.removeExtra("defaultTab")
                intent.removeExtra("opened_from_tile")

                ExternalNavigationData(
                    navigateTo = navigateTo,
                    isUpdateTransaction = isUpdateTransaction,
                    transactionId = transactionId,
                    defaultTab = defaultTab,
                    openedFromTile = openedFromTile,
                    openedFromWidget = false,
                    widgetAction = null
                )
            }

            else -> null
        }
    }

    private suspend fun handleNotificationNavigation(
        data: ExternalNavigationData,
        navController: NavHostController
    ) {
        if (data.navigateTo == "ADD_TRANSACTION") {
            // Wait a bit to ensure navigation graph is fully initialized
            delay(500)

            try {
                // Set the saved state handle data
                navController.currentBackStackEntry?.savedStateHandle?.apply {
                    set("isUpdateTransaction", data.isUpdateTransaction)
                    if (data.isUpdateTransaction && data.transactionId > 0) {
                        set("transactionId", data.transactionId)
                    }
                }

                // Navigate to the AddTransaction screen
                navController.navigate(NavGraph.ADD_TRANSACTION) {
                    launchSingleTop = true
                    // Clear any existing AddTransaction screens to prevent duplicates
                    popUpTo(NavGraph.ADD_TRANSACTION) {
                        inclusive = true
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to navigate from notification: ${e.message}", e)
            }
        }
    }

    private  fun handleTileNavigation(
        data: ExternalNavigationData,
        navController: NavHostController
    ) {
        if (data.navigateTo == "ADD_TRANSACTION") {
            // Wait a bit to ensure navigation graph is fully initialized
            // delay(800) // Slightly longer delay for tile navigation (commented out as in original)

            try {
                // Set the saved state handle data
                navController.currentBackStackEntry?.savedStateHandle?.apply {
                    set("isUpdateTransaction", data.isUpdateTransaction)
                    if (data.isUpdateTransaction && data.transactionId > 0) {
                        set("transactionId", data.transactionId)
                    }
                    // Set default tab if provided
                    data.defaultTab?.let { tab ->
                        set("defaultTab", tab)
                    }
                    // Mark as opened from tile for special handling in AddTransactionScreen
                    set("openedFromTile", true)
                }

                // Navigate to the AddTransaction screen
                // Since we started at HOME, the back stack will be HOME -> ADD_TRANSACTION
                navController.navigate(NavGraph.ADD_TRANSACTION) {
                    launchSingleTop = true
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to navigate from tile: ${e.message}", e)
            }
        }
    }

    private fun handleWidgetNavigation(
        data: ExternalNavigationData,
        navController: NavHostController
    ) {
        if (data.navigateTo == "ADD_TRANSACTION") {
            try {
                Log.d("MainActivity", "Handling widget navigation - action: ${data.widgetAction}")

                // Set the saved state handle data
                navController.currentBackStackEntry?.savedStateHandle?.apply {
                    set("isUpdateTransaction", false) // Widgets always create new transactions

                    // Set default tab based on widget action with enhanced logic
                    when (data.widgetAction) {
                        "ADD_TRANSACTION" -> {
                            // Default behavior - no specific tab (will default to Expense)
                            Log.d("MainActivity", "Widget navigation: ADD_TRANSACTION")
                        }
                        "TRANSFER_TRANSACTION" -> {
                            set("defaultTab", "Transfer")
                            Log.d("MainActivity", "Widget navigation: TRANSFER_TRANSACTION - setting Transfer tab")
                        }
                    }

                    // Mark as opened from widget for special handling in AddTransactionScreen
                    set("openedFromWidget", true)

                    // Additional debug info
                    set("widgetDebugInfo", "Action: ${data.widgetAction}, Tab: ${get<String>("defaultTab")}")
                }

                // Navigate to the AddTransaction screen
                navController.navigate(NavGraph.ADD_TRANSACTION) {
                    launchSingleTop = true
                }

                Log.d("MainActivity", "Widget navigation completed successfully")

            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to navigate from widget: ${e.message}", e)
            }
        }
    }
}