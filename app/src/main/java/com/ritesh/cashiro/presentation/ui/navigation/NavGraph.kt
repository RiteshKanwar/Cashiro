package com.ritesh.cashiro.presentation.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavBackStackEntry
import com.ritesh.cashiro.R

object NavGraph {
    // Main routes
    const val HOME = "home_screen"
    const val REPORTS = "reports_screen"
    const val TRANSACTIONS = "transactions_screen"
    const val SETTINGS = "settings_screen"

    // Sub-routes
    const val PROFILE = "profile_screen"
    const val CATEGORIES = "categories_screen"
    const val APPEARANCE = "appearance_screen"
    const val ACCOUNTS = "addAccounts_Screen"
    const val NOTIFICATIONS = "notifications_screen"
    const val ADD_TRANSACTION = "addTransaction_Screen"
    const val SEARCH_TRANSACTION = "searchTransaction_Screen"
    const val ACCOUNT_DETAILS = "accountDetails_Screen"

    // NEW ROUTES FOR THE THREE SCREENS
    const val SUBSCRIPTIONS = "subscriptions_screen"
    const val SCHEDULE = "schedule_screen"
    const val ACTIVITY_LOG = "activity_log_screen"

    const val DATA_MANAGEMENT = "data_management"
    const val DEVELOPER_OPTIONS = "developer_options"

    const val SPLASH = "splash_screen"
    const val ONBOARDING = "onboarding_screen"

    // Route groups for determining UI elements visibility
    val BOTTOM_NAV_ROUTES = listOf(HOME, REPORTS, TRANSACTIONS, SETTINGS)

    // Destination definitions with metadata
    sealed class Destination(
        val route: String,
        val title: String,
        val selectedIcon: Int,
        val unselectedIcon: Int
    ) {
        // Main destinations
        object Home : Destination(HOME, "Home", R.drawable.home_filled, R.drawable.home_filled)
        object Reports : Destination(REPORTS, "Reports",R.drawable.reports_filled, R.drawable.reports_filled)
        object Transactions : Destination(TRANSACTIONS, "Transactions", R.drawable.transactions_filled, R.drawable.transactions_filled)
        object Settings : Destination(SETTINGS, "Settings", R.drawable.settings_bulk,R.drawable.settings_bulk)

        // Sub-destinations
        object Profile : Destination(PROFILE, "Profile",  R.drawable.settings_bulk,R.drawable.settings_bulk)
        object Categories : Destination(CATEGORIES, "Categories",  R.drawable.settings_bulk,R.drawable.settings_bulk)
        object Appearance : Destination(APPEARANCE, "Appearance",  R.drawable.settings_bulk,R.drawable.settings_bulk)
        object Accounts : Destination(ACCOUNTS, "Accounts",  R.drawable.settings_bulk,R.drawable.settings_bulk)
        object Notifications : Destination(NOTIFICATIONS, "Notifications",  R.drawable.system_auto_theme,R.drawable.system_auto_theme)
        object AddTransaction : Destination(ADD_TRANSACTION, "Add Transaction",  R.drawable.settings_bulk,R.drawable.settings_bulk)
        object SearchTransaction : Destination(SEARCH_TRANSACTION, "Search Transaction",  R.drawable.settings_bulk,R.drawable.settings_bulk)
        object AccountDetails : Destination(ACCOUNT_DETAILS, "Account Details",  R.drawable.settings_bulk,R.drawable.settings_bulk)
        object DataManagement : Destination(DATA_MANAGEMENT, "Data Management",  R.drawable.settings_bulk,R.drawable.settings_bulk)
        object DeveloperOptions : Destination(DEVELOPER_OPTIONS, "Developer Options",  R.drawable.settings_bulk,R.drawable.settings_bulk)

        // NEW DESTINATIONS
        object Subscriptions : Destination(SUBSCRIPTIONS, "Subscriptions", R.drawable.notifications_reminder, R.drawable.notifications_reminder)
        object Schedule : Destination(SCHEDULE, "Schedule", R.drawable.notification, R.drawable.notification)
        object ActivityLog : Destination(ACTIVITY_LOG, "Activity Log", R.drawable.transactions_filled, R.drawable.transactions_filled)

        // Helper method to find destination by route
        companion object {
            fun fromRoute(route: String?): Destination? {
                return when (route) {
                    HOME -> Home
                    REPORTS -> Reports
                    TRANSACTIONS -> Transactions
                    SETTINGS -> Settings
                    PROFILE -> Profile
                    CATEGORIES -> Categories
                    APPEARANCE -> Appearance
                    ACCOUNTS -> Accounts
                    NOTIFICATIONS -> Notifications
                    ADD_TRANSACTION -> AddTransaction
                    SEARCH_TRANSACTION -> SearchTransaction
                    ACCOUNT_DETAILS -> AccountDetails
                    DATA_MANAGEMENT -> DataManagement
                    DEVELOPER_OPTIONS -> DeveloperOptions
                    SUBSCRIPTIONS -> Subscriptions
                    SCHEDULE -> Schedule
                    ACTIVITY_LOG -> ActivityLog
                    else -> null
                }
            }
        }
    }

    // Screen transitions definitions remain the same...
    object Transitions {
        val horizontalSlide: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? = {
            slideInHorizontally(initialOffsetX = { it }) + fadeIn()
        }

        val horizontalSlideExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? = {
            slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
        }

        val horizontalSlidePopEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? = {
            slideInHorizontally(initialOffsetX = { -it }) + fadeIn()
        }

        val horizontalSlidePopExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? = {
            slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
        }

        val verticalSlide: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? = {
            slideInVertically(initialOffsetY = { it }) + fadeIn()
        }

        val scaleIn: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? = {
            scaleIn()?.let { fadeIn() + it }
        }

        val scaleOut: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? = {
            scaleOut()?.let { fadeOut() + it }
        }
        val fabToAddScreenEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition? = {
            fadeIn(animationSpec = tween(300)) +
                    scaleIn(initialScale = 0.8f, animationSpec = tween(500, easing = FastOutSlowInEasing))
        }

        val fabToAddScreenExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition? = {
            fadeOut(animationSpec = tween(300)) +
                    scaleOut(targetScale = 1.2f, animationSpec = tween(500, easing = FastOutSlowInEasing))
        }
    }
}