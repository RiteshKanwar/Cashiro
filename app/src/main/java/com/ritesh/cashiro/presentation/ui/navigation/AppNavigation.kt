package com.ritesh.cashiro.presentation.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ritesh.cashiro.presentation.ui.features.currency.CurrencyViewModel
import com.ritesh.cashiro.domain.repository.LabelVisibility
import com.ritesh.cashiro.presentation.ui.features.profile.ProfileScreen
import com.ritesh.cashiro.presentation.ui.features.profile.ProfileScreenViewModel
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreen
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenViewModel
import com.ritesh.cashiro.presentation.ui.features.activity_logs.ActivityLogScreen
import com.ritesh.cashiro.presentation.ui.features.activity_logs.ActivityLogViewModel
import com.ritesh.cashiro.presentation.ui.features.add_transaction.AddTransactionScreen
import com.ritesh.cashiro.presentation.ui.features.add_transaction.AddTransactionScreenViewModel
import com.ritesh.cashiro.presentation.ui.features.appearance.AppearanceScreen
import com.ritesh.cashiro.presentation.ui.features.appearance.AppearanceViewModel
import com.ritesh.cashiro.presentation.ui.features.categories.CategoriesScreen
import com.ritesh.cashiro.presentation.ui.features.categories.CategoryScreenViewModel
import com.ritesh.cashiro.presentation.ui.features.categories.SubCategoryViewModel
import com.ritesh.cashiro.presentation.ui.features.backup_restore.DataManagementScreen
import com.ritesh.cashiro.presentation.ui.features.backup_restore.DataManagementViewModel
import com.ritesh.cashiro.presentation.ui.features.developer_options.DeveloperOptionsScreen
import com.ritesh.cashiro.presentation.ui.features.home.AccountDetailsScreen
import com.ritesh.cashiro.presentation.ui.features.home.HomeScreen
import com.ritesh.cashiro.presentation.ui.features.notifications.NotificationsScreen
import com.ritesh.cashiro.presentation.ui.features.notifications.NotificationsViewModel
import com.ritesh.cashiro.presentation.ui.features.onboarding.OnBoardingScreen
import com.ritesh.cashiro.presentation.ui.features.onboarding.OnBoardingViewModel
import com.ritesh.cashiro.presentation.ui.features.reports.ReportScreen
import com.ritesh.cashiro.presentation.ui.features.reports.ReportScreenViewModel
import com.ritesh.cashiro.presentation.ui.features.schedules.ScheduleScreen
import com.ritesh.cashiro.presentation.ui.features.schedules.ScheduleViewModel
import com.ritesh.cashiro.presentation.ui.features.settings.SettingsScreen
import com.ritesh.cashiro.presentation.ui.features.splashscreen.SplashScreen
import com.ritesh.cashiro.presentation.ui.features.subscriptions.SubscriptionScreen
import com.ritesh.cashiro.presentation.ui.features.subscriptions.SubscriptionViewModel
import com.ritesh.cashiro.presentation.ui.features.transactions.SearchTransactionScreen
import com.ritesh.cashiro.presentation.ui.features.transactions.TransactionsScreen
import com.ritesh.cashiro.presentation.ui.theme.iosFont
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild


const val FAB_ADD_TRANSACTION_KEY = "fab_add_transaction_key"
const val TRANSACTION_SCREEN_TRANSACTION_CARD_KEY_PREFIX = "transaction_screen_transaction_card_key_"
const val SEARCH_TRANSACTION_BUTTON_KEY = "search_transaction_key"
const val ACCOUNT_CARD_KEY_PREFIX = "account_card_key_"

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun AppNavigation(
    transactionViewModel: AddTransactionScreenViewModel,
    categoryViewModel: CategoryScreenViewModel,
    subCategoryViewModel: SubCategoryViewModel,
    accountViewModel: AccountScreenViewModel,
    appearanceViewModel: AppearanceViewModel,
    currencyViewModel: CurrencyViewModel,
    profileViewModel: ProfileScreenViewModel,
    reportViewModel: ReportScreenViewModel,
    dataManagementViewModel: DataManagementViewModel,
    notificationsViewModel: NotificationsViewModel,
    onBoardingViewModel: OnBoardingViewModel,
    subscriptionViewModel: SubscriptionViewModel,
    scheduleViewModel: ScheduleViewModel,
    activityLogViewModel: ActivityLogViewModel,
    navController: NavHostController,
    labelVisibility: LabelVisibility,
    startDestination: String = NavGraph.SPLASH, // Add parameter with default
    modifier: Modifier = Modifier
) {
    val currentScreenTitle = remember { mutableStateOf("App") }
    val previousScreenTitle = remember { mutableStateOf("App") }

    // Track the current screen for UI state
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route
    val currentDestination = NavGraph.Destination.fromRoute(currentRoute)

    // Determine if we should show the bottom nav
    val showBottomNav = currentRoute in NavGraph.BOTTOM_NAV_ROUTES

    // Maintain selected index state
    val selectedIndex = NavGraph.BOTTOM_NAV_ROUTES.indexOf(currentRoute).takeIf { it >= 0 } ?: 0

    val hazeState = remember{ HazeState() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Main navigation content
        AppNavigationHost(
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
            startDestination = startDestination,
            currentScreenTitle = currentScreenTitle,
            previousScreenTitle = previousScreenTitle,
            modifier = Modifier.fillMaxSize(),
            hazeState = hazeState
        )

        // Bottom navigation bar with animation
        AnimatedVisibility(
            visible = showBottomNav,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            BottomNavigationBar(
                navController = navController,
                selectedIndex = selectedIndex,
                labelVisibility = labelVisibility,
                hazeState = hazeState
            )
        }
    }
}

@Composable
private fun BottomNavigationBar(
    navController: NavHostController,
    selectedIndex: Int,
    labelVisibility: LabelVisibility,
    hazeState: HazeState
) {
    val topLevelDestinations = listOf(
        NavGraph.Destination.Home,
        NavGraph.Destination.Reports,
        NavGraph.Destination.Transactions,
        NavGraph.Destination.Settings
    )

    NavigationBar(
        containerColor = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .hazeChild(
                state = hazeState,
                block = {
                    style = HazeDefaults.style(
                        backgroundColor = Color.Transparent,
                        blurRadius = 20.dp,
                        noiseFactor = -1f,)
                }
            )
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface.copy(0.8f),
                        MaterialTheme.colorScheme.surface.copy(0.8f),
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
    ) {
        topLevelDestinations.forEachIndexed { index, destination ->
            NavigationBarItem(
                selected = index == selectedIndex,
                onClick = {
                    if (destination.route == NavGraph.HOME && navController.currentDestination?.route == NavGraph.TRANSACTIONS) {
                        // If we're on Transactions screen and trying to go Home, pop back to Home
                        navController.popBackStack(NavGraph.HOME, false)
                    } else {
                        // Regular navigation for other cases
                        navController.navigate(destination.route) {
                            // Preserve state when navigating
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(if (index == selectedIndex)
                            destination.unselectedIcon
                        else
                            destination.selectedIcon),
                        contentDescription = destination.title,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .then(
                                when (labelVisibility) {
                                    LabelVisibility.NeverShow -> Modifier.size(28.dp)
                                    else -> Modifier.size(26.dp)
                                }
                            )
                    )
                },
                label = when (labelVisibility) {
                    LabelVisibility.AlwaysShow -> {
                        { Text(text = destination.title, fontFamily = iosFont) }
                    }
                    LabelVisibility.SelectedOnly -> {
                        if (index == selectedIndex) {
                            { Text(text = destination.title, fontFamily = iosFont) }
                        } else null
                    }
                    LabelVisibility.NeverShow -> null
                },
                alwaysShowLabel = labelVisibility == LabelVisibility.AlwaysShow,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.inverseSurface,
                    unselectedTextColor = MaterialTheme.colorScheme.inverseSurface,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun AppNavigationHost(
    transactionViewModel: AddTransactionScreenViewModel,
    categoryViewModel: CategoryScreenViewModel,
    subCategoryViewModel: SubCategoryViewModel,
    accountViewModel: AccountScreenViewModel,
    appearanceViewModel: AppearanceViewModel,
    currencyViewModel: CurrencyViewModel,
    profileViewModel: ProfileScreenViewModel,
    reportViewModel: ReportScreenViewModel,
    dataManagementViewModel: DataManagementViewModel,
    notificationsViewModel: NotificationsViewModel,
    onBoardingViewModel: OnBoardingViewModel,
    subscriptionViewModel: SubscriptionViewModel,
    scheduleViewModel: ScheduleViewModel,
    activityLogViewModel: ActivityLogViewModel,
    startDestination: String,
    navController: NavHostController,
    currentScreenTitle: MutableState<String>,
    previousScreenTitle: MutableState<String>,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    //States
    val accountUiState by accountViewModel.state.collectAsState()
    val categoryUiState by categoryViewModel.state.collectAsState()
    val subCategoryUiState by subCategoryViewModel.state.collectAsState()
    val transactionUiState by transactionViewModel.state.collectAsState()
    val currencyUiState by currencyViewModel.uiState.collectAsState()
    val onBoardingState by onBoardingViewModel.state.collectAsState()
    val profileUiState by profileViewModel.state.collectAsState()

    val accountId = navController.previousBackStackEntry?.savedStateHandle?.get<Int>("accountId") ?: 0
    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = modifier.haze(state = hazeState)
        ) {
            composable(
                route = NavGraph.SPLASH,
                enterTransition = { fadeIn() },
                exitTransition = { fadeOut() }
            ) {
                SplashScreen(
                    onBoardingViewModel = onBoardingViewModel,
                    onNavigateToOnBoarding = {
                        navController.navigate(NavGraph.ONBOARDING) {
                            popUpTo(NavGraph.SPLASH) { inclusive = true }
                        }
                    },
                    onNavigateToHome = {
                        navController.navigate(NavGraph.HOME) {
                            popUpTo(NavGraph.SPLASH) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                route = NavGraph.ONBOARDING,
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut() }
            ) {
                OnBoardingScreen(
                    onComplete = {
                        navController.navigate(NavGraph.HOME) {
                            popUpTo(NavGraph.ONBOARDING) { inclusive = true }
                        }
                    },
                    onBoardingEvent = onBoardingViewModel::handleEvent,
                    onBoardingState = onBoardingState,
                    onProfileEvent = profileViewModel::handleEvent,
                    profileUiState = profileUiState,
                    accountUiState = accountUiState,
                    onCategoryEvent = categoryViewModel::onEvent,
                    onAccountEvent = accountViewModel::onEvent,
                    updateAccountCurrency = accountViewModel::updateAccountCurrency,
                    currencyUiState = currencyUiState,
                    selectCurrency = currencyViewModel::selectCurrency,
                    onAddTransactionEvent = transactionViewModel::onEvent,
                    transactionUiState = transactionUiState
                )
            }

            // Home Screen
            composable(
                route = NavGraph.HOME,
                enterTransition = { slideInVertically(initialOffsetY = { -it }) + scaleIn()+ fadeIn() },
                exitTransition = { slideOutVertically(targetOffsetY = { -it })+ scaleOut() + fadeOut() },
                popEnterTransition = { slideInVertically(initialOffsetY = { -it }) + scaleIn()+ fadeIn() },
                popExitTransition = { slideOutVertically(targetOffsetY = { -it }) + scaleOut() + fadeOut() }
            ) {
                currentScreenTitle.value = "Home"
                HomeScreen(
                    accountViewModel = accountViewModel,
                    categoryViewModel = categoryViewModel,
                    subCategoryViewModel = subCategoryViewModel,
                    profileScreenViewModel = profileViewModel,
                    appearanceViewModel = appearanceViewModel,
                    onBackClicked = { navController.navigateUp() },
                    screenTitle = currentScreenTitle.value,
                    transactionViewModel = transactionViewModel,
                    navController = navController,
                    animatedVisibilityScope = this
                )
            }

            // Reports Screen
            composable(
                route = NavGraph.REPORTS,
                enterTransition = NavGraph.Transitions.verticalSlide,
                exitTransition = { slideOutVertically(targetOffsetY = { it })+ scaleOut() + fadeOut() },
                popEnterTransition = { slideInVertically(initialOffsetY = { it }) + scaleIn()+ fadeIn() },
                popExitTransition = { slideOutVertically(targetOffsetY = { it }) + scaleOut() + fadeOut() }
            ) {
                currentScreenTitle.value = "Reports"
                ReportScreen(
                    reportViewModel = reportViewModel,
                    accountViewModel = accountViewModel,
                    transactionViewModel = transactionViewModel,
                    screenTitle = currentScreenTitle.value
                )
            }
            composable(
                route= NavGraph.TRANSACTIONS,
                enterTransition = NavGraph.Transitions.verticalSlide,
                exitTransition = { slideOutVertically(targetOffsetY = { it })+ scaleOut() + fadeOut() },
                popEnterTransition = { slideInVertically(initialOffsetY = { it }) + scaleIn()+ fadeIn() },
                popExitTransition = { slideOutVertically(targetOffsetY = { it }) + scaleOut() + fadeOut() }
            ){
                currentScreenTitle.value = "Transactions"
                TransactionsScreen(
                    navController = navController,
                    screenTitle = currentScreenTitle.value,
                    onFabClick = {
                        navController.currentBackStackEntry?.savedStateHandle?.set(
                            "isUpdateTransaction",
                            false
                        )
                        navController.navigate(NavGraph.ADD_TRANSACTION) { launchSingleTop = true }
                    },
                    onSearchButtonClick = {
                        navController.navigate(NavGraph.SEARCH_TRANSACTION) {
                            launchSingleTop = true
                        }
                    },
                    animatedVisibilityScope = this,
                    deleteTransactions = transactionViewModel::deleteTransactions,
                    accountUiState = accountUiState,
                    onAccountEvent = accountViewModel::onEvent,
                    transactionUiState = transactionUiState,
                    onAddTransactionEvent = transactionViewModel::onEvent,
                    categoryUiState = categoryUiState,
                    onCategoryEvent = categoryViewModel::onEvent,
                    subCategoryUiState = subCategoryUiState,
                    onSubCategoryEvent = subCategoryViewModel::onEvent
                )
            }
            composable(
                route= NavGraph.SETTINGS,
                enterTransition = NavGraph.Transitions.verticalSlide,
                exitTransition = { slideOutVertically(targetOffsetY = { it })+ scaleOut() + fadeOut() },
                popEnterTransition = { slideInVertically(initialOffsetY = { it }) + scaleIn()+ fadeIn() },
                popExitTransition = { slideOutVertically(targetOffsetY = { it }) + scaleOut() + fadeOut() }
            ){navBackStackEntry ->
                currentScreenTitle.value = "Settings"
                SettingsScreen(
                    profileScreenViewModel = profileViewModel,
                    transactionViewModel = transactionViewModel,
                    navController = navController,
                    screenTitle = currentScreenTitle.value)
            }
            composable(
                route = NavGraph.PROFILE,
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut() },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
            ) {
                previousScreenTitle.value = "Settings"
                currentScreenTitle.value = "Profile"
                ProfileScreen(
                    accountViewModel = accountViewModel,
                    transactionViewModel = transactionViewModel,
                    profileViewModel = profileViewModel,
                    onBackClicked = {navController.navigateUp()},
                    screenTitle = currentScreenTitle.value,
                    previousScreenTitle = previousScreenTitle.value,
                )
            }
            composable(route= NavGraph.CATEGORIES,
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut() },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
            ){ navBackStackEntry ->
                previousScreenTitle.value = "Settings"
                currentScreenTitle.value = "Categories"
                CategoriesScreen(
                    onBackClicked = { navController.navigateUp() },
                    screenTitle = currentScreenTitle.value,
                    previousScreenTitle = previousScreenTitle.value,
                    categoryUiState = categoryUiState,
                    onCategoryEvent = categoryViewModel::onEvent,
                    subCategoryUiState = subCategoryUiState,
                    onSubCategoryEvent = subCategoryViewModel::onEvent,
                    getTransactionStatsForCategory = categoryViewModel::getTransactionStatsForCategory,
                    getTransactionStatsForSubCategory = categoryViewModel::getTransactionStatsForSubCategory,
                    updateCollapsingFraction = categoryViewModel::updateCollapsingFraction,
                    setSelectedCategoryId = subCategoryViewModel::setSelectedCategoryId,
                )

            }
            composable(route= NavGraph.APPEARANCE,
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut() },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
            ){ navBackStackEntry ->
                previousScreenTitle.value = "Settings"
                currentScreenTitle.value = "Appearance"
                AppearanceScreen(
                    onBackClicked = {navController.navigateUp()},
                    screenTitle = currentScreenTitle.value,
                    previousScreenTitle = previousScreenTitle.value,
                    appearanceViewModel = appearanceViewModel
                )
            }
            composable(route = NavGraph.ACCOUNTS,
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut() },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
            ){ navBackStackEntry ->
                previousScreenTitle.value = "Settings"
                currentScreenTitle.value = "Accounts"
                AccountScreen(
                    onBackClicked = { navController.navigateUp() },
                    screenTitle = currentScreenTitle.value,
                    previousScreenTitle = previousScreenTitle.value,
                    accountViewModel = accountViewModel,
                    currencyViewModel = currencyViewModel,
                    transactionViewModel = transactionViewModel
                )
            }
            composable(route = NavGraph.ADD_TRANSACTION,
                enterTransition = { fadeIn() + scaleIn() },
                exitTransition = { fadeOut() + scaleOut() },
                popEnterTransition = { fadeIn() + scaleIn() + slideInVertically(initialOffsetY = { it }) },
                popExitTransition = { fadeOut() + scaleOut() + slideOutVertically(targetOffsetY = { it })}
            ){ navBackStackEntry ->
                previousScreenTitle.value = "Settings"
                currentScreenTitle.value = "Add Transaction"
                val transactionId = navController.previousBackStackEntry?.savedStateHandle?.get<Int>("transactionId") ?: 0
                val isUpdateTransaction = navController.previousBackStackEntry?.savedStateHandle?.get<Boolean>("isUpdateTransaction") == true
                val defaultTab = navController.previousBackStackEntry?.savedStateHandle?.get<String>("defaultTab")

                var sharedKey = if (isUpdateTransaction) {
                    "${TRANSACTION_SCREEN_TRANSACTION_CARD_KEY_PREFIX}${transactionId}"
                } else {
                    FAB_ADD_TRANSACTION_KEY
                }
                val sharedContentKey = remember { sharedKey }
                AddTransactionScreen(
                    onBackClicked = {
                        navController.navigateUp()
                    },
                    screenTitle = currentScreenTitle.value,
                    previousScreenTitle = previousScreenTitle.value,
                    transactionId = transactionId,
                    isUpdateTransaction = isUpdateTransaction,
                    sharedKey = sharedContentKey,
                    animatedVisibilityScope = this,
                    defaultTab = defaultTab,
                    transactionUiState = transactionUiState,
                    transactionUiEvent = transactionViewModel.events.collectAsState(initial = null).value,
                    onAddTransactionEvent = transactionViewModel::onEvent,
                    getTransactionById = transactionViewModel::getTransactionById,
                    onSubCategoryEvent = subCategoryViewModel::onEvent,
                    navController = navController,
                    accountUiState = accountUiState,
                    onAccountEvent = accountViewModel::onEvent,
                    categoryUiState = categoryUiState,
                    onCategoryEvent = categoryViewModel::onEvent,
                    subCategoryUiState = subCategoryUiState,
                    getTransactionStatsForAccount = accountViewModel::getTransactionStatsForAccount,
                    getTransactionStatsForCategory = categoryViewModel::getTransactionStatsForCategory,
                    getTransactionStatsForSubCategory = categoryViewModel::getTransactionStatsForSubCategory,
                    updateAccountCurrency = accountViewModel::updateAccountCurrency,
                    setAccountAsMain = accountViewModel::setAccountAsMain,
                    setSelectedCategoryId = subCategoryViewModel::setSelectedCategoryId,
                    clearSelection = subCategoryViewModel::clearSelection,
                    currencyUiState = currencyUiState,
                    selectCurrency = currencyViewModel::selectCurrency,
                )
            }
            composable(route = NavGraph.SEARCH_TRANSACTION,
                enterTransition = { fadeIn() + scaleIn() },
                exitTransition = { fadeOut() + scaleOut() },
                popEnterTransition = { fadeIn() + scaleIn()  },
                popExitTransition = { fadeOut() + scaleOut()}
            ) { navBackStackEntry ->
                currentScreenTitle.value = "All Transactions"
                var sharedKey = SEARCH_TRANSACTION_BUTTON_KEY
                val sharedContentKey = remember { sharedKey }
                SearchTransactionScreen(
                    screenTitle = currentScreenTitle.value,
                    navController = navController,
                    sharedKey = sharedContentKey,
                    animatedVisibilityScope = this,
                    deleteTransactions = transactionViewModel::deleteTransactions,
                    transactionUiState = transactionUiState,
                    accountUiState = accountUiState,
                    onAccountEvent = accountViewModel::onEvent,
                    onAddTransactionEvent = transactionViewModel::onEvent,
                    onCategoryEvent = categoryViewModel::onEvent,
                    subCategoryUiState = subCategoryUiState,
                    onSubCategoryEvent = subCategoryViewModel::onEvent,
                    categoryUiState = categoryUiState,
                )
            }
            composable(route = NavGraph.ACCOUNT_DETAILS,
                enterTransition = { fadeIn() + scaleIn() },
                exitTransition = { fadeOut() + scaleOut()},
                popEnterTransition = { fadeIn() + scaleIn()  },
                popExitTransition = { fadeOut() +  scaleOut() +slideOutVertically(targetOffsetY = { -it })}
            ){ navBackStackEntry ->
                currentScreenTitle.value = "Account Details"
                val accounts by accountViewModel.accounts.collectAsState()

                // FIX: Find account by ID instead of using ID as array index
                val account = accounts.find { it.id == accountId }

                // Handle case where account is not found (e.g., deleted account)
                if (account == null) {
                    LaunchedEffect(Unit) {
                        navController.navigateUp() // Navigate back if account doesn't exist
                    }
                    return@composable
                }

                val sharedKey = "${ACCOUNT_CARD_KEY_PREFIX}${account.id}"
                val sharedContentKey = remember { sharedKey }

                AccountDetailsScreen(
                    screenTitle = currentScreenTitle.value,
                    navController = navController,
                    sharedKey = sharedContentKey,
                    animatedVisibilityScope = this,
                    account = account,
                    themeColors = MaterialTheme.colorScheme,
                    accountUiState = accountUiState,
                    setAccountAsMain = accountViewModel::setAccountAsMain,
                    transactionUiState = transactionUiState,
                    onAddTransactionEvent = transactionViewModel::onEvent,
                    onAccountEvent = accountViewModel::onEvent,
                    onCategoryEvent = categoryViewModel::onEvent,
                    onSubCategoryEvent = subCategoryViewModel::onEvent,
                    deleteTransactions = transactionViewModel::deleteTransactions,
                    getTransactionStatsForAccount = accountViewModel::getTransactionStatsForAccount,
                    updateAccountCurrency = accountViewModel::updateAccountCurrency,
                    currencyUiState = currencyUiState,
                    selectCurrency = currencyViewModel::selectCurrency,
                )
            }
            composable(route= NavGraph.DATA_MANAGEMENT,
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut() },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
            ) { navBackStackEntry ->
                currentScreenTitle.value = "Backup & Restore"
                previousScreenTitle.value = "Settings"
                DataManagementScreen(
                    screenTitle = currentScreenTitle.value,
                    previousScreenTitle = previousScreenTitle.value,
                    onBackClick = { navController.navigateUp() },
                    dataManagementViewModel = dataManagementViewModel
                )
            }

            // NEW: Developer Options Screen
            composable(route = NavGraph.DEVELOPER_OPTIONS,
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut() },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
            ) { navBackStackEntry ->
                previousScreenTitle.value = "Settings"
                currentScreenTitle.value = "Developer Options"
                DeveloperOptionsScreen(
                    onBackClicked = { navController.navigateUp() },
                    screenTitle = currentScreenTitle.value,
                    previousScreenTitle = previousScreenTitle.value,
                    onBoardingViewModel = onBoardingViewModel,
                    navController = navController
                )
            }

            composable(route = NavGraph.NOTIFICATIONS,
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut() },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
            ){ navBackStackEntry ->
                previousScreenTitle.value = "Settings"
                currentScreenTitle.value = "Notifications"
                NotificationsScreen(
                    notificationsViewModel = notificationsViewModel ,
                    onBackClicked = { navController.navigateUp() },
                    screenTitle = currentScreenTitle.value,
                    previousScreenTitle = previousScreenTitle.value,
                )
            }

            composable(
                route = NavGraph.SUBSCRIPTIONS,
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut() },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
            ) {
                previousScreenTitle.value = "Settings"
                currentScreenTitle.value = "Subscriptions"
                SubscriptionScreen(
                    subscriptionViewModel = subscriptionViewModel, // Need to inject this
                    accountUiState = accountUiState,
                    onAccountEvent = accountViewModel::onEvent,
                    onAddTransactionEvent = transactionViewModel::onEvent,
                    onCategoryEvent = categoryViewModel::onEvent,
                    onSubCategoryEvent = subCategoryViewModel::onEvent,
                    screenTitle = currentScreenTitle.value,
                    navController = navController,
                    onBackClicked = { navController.navigateUp() },
                    previousScreenTitle = previousScreenTitle.value,
                    animatedVisibilityScope = this
                )
            }

            composable(
                route = NavGraph.SCHEDULE,
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut() },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
            ) {
                previousScreenTitle.value = "Settings"
                currentScreenTitle.value = "Schedule"
                ScheduleScreen(
                    scheduleViewModel = scheduleViewModel, // Need to inject this
                    accountUiState = accountUiState,
                    onAccountEvent = accountViewModel::onEvent,
                    onAddTransactionEvent = transactionViewModel::onEvent,
                    onCategoryEvent = categoryViewModel::onEvent,
                    onSubCategoryEvent = subCategoryViewModel::onEvent,
                    screenTitle = currentScreenTitle.value,
                    navController = navController,
                    onBackClicked = { navController.navigateUp() },
                    previousScreenTitle = previousScreenTitle.value,
                    animatedVisibilityScope = this
                )
            }

            composable(
                route = NavGraph.ACTIVITY_LOG,
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut() },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
            ) {
                previousScreenTitle.value = "Settings"
                currentScreenTitle.value = "Activity Log"
                ActivityLogScreen(
                    activityLogViewModel = activityLogViewModel, // Need to inject this
                    accountUiState = accountUiState,
                    onAccountEvent = accountViewModel::onEvent,
                    screenTitle = currentScreenTitle.value,
                    navController = navController,
                    onBackClicked = { navController.navigateUp() },
                    previousScreenTitle = previousScreenTitle.value,
                    animatedVisibilityScope = this
                )
            }
        }
    }
}