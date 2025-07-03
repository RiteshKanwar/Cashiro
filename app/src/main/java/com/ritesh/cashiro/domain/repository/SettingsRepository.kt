package com.ritesh.cashiro.domain.repository
import androidx.compose.ui.graphics.Color
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ritesh.cashiro.domain.utils.ActivityLogUtils
import com.ritesh.cashiro.domain.utils.AppEventBus
import com.ritesh.cashiro.presentation.ui.features.activity_logs.ActivityActionType
import com.ritesh.cashiro.presentation.ui.features.appearance.SettingsEvent
import com.ritesh.cashiro.presentation.ui.theme.Latte_Blue
import com.ritesh.cashiro.widgets.WidgetUpdateUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class ThemeMode {
    Light, Dark, Black, System, SystemBlack
}

enum class LabelVisibility {
    AlwaysShow,
    SelectedOnly,
    NeverShow;

    companion object {
        // This function will be used to convert string values to the corresponding enum
        fun fromString(value: String?): LabelVisibility? {
            return when (value) {
                "AlwaysShow" -> AlwaysShow
                "SelectedOnly" -> SelectedOnly
                "NeverShow" -> NeverShow
                else -> null // Return null if the value doesn't match any enum value
            }
        }
    }
}

enum class ReminderType {
    IF_NOT_OPENED_TODAY,
    HOURS_FROM_OPENED,
    EVERYDAY;

    fun getDisplayName(): String {
        return when (this) {
            IF_NOT_OPENED_TODAY -> "If the app was not already opened today"
            HOURS_FROM_OPENED -> "24 hours from app opened"
            EVERYDAY -> "Everyday"
        }
    }

    companion object {
        fun fromString(value: String?): ReminderType? {
            return when (value) {
                "IF_NOT_OPENED_TODAY" -> IF_NOT_OPENED_TODAY
                "HOURS_FROM_OPENED" -> HOURS_FROM_OPENED
                "EVERYDAY" -> EVERYDAY
                else -> null
            }
        }
    }
}

data class NotificationSettings(
    val addTransactionReminder: Boolean = false,
    val reminderType: ReminderType = ReminderType.IF_NOT_OPENED_TODAY,
    val alertHour: Int = 20, // 8 PM
    val alertMinute: Int = 0,
    val upcomingTransactions: Boolean = false,
    val sipNotifications: Map<String, Boolean> = emptyMap()
)

@Singleton
class SettingsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val activityLogUtils: ActivityLogUtils
) {
    companion object {
        private val PRIMARY_COLOR_KEY = stringPreferencesKey("primary_color")
        private val LABEL_VISIBILITY_KEY = stringPreferencesKey("label_visibility")
        private val THEME_MODE_KEY = intPreferencesKey("theme_mode")
        private val SHOW_PROFILE_BANNER_KEY = booleanPreferencesKey("show_profile_banner")

        // Notification preferences keys
        private val ADD_TRANSACTION_REMINDER_KEY = booleanPreferencesKey("add_transaction_reminder")
        private val REMINDER_TYPE_KEY = stringPreferencesKey("reminder_type")
        private val ALERT_HOUR_KEY = intPreferencesKey("alert_hour")
        private val ALERT_MINUTE_KEY = intPreferencesKey("alert_minute")
        private val UPCOMING_TRANSACTIONS_KEY = booleanPreferencesKey("upcoming_transactions")
        private val SIP_NOTIFICATIONS_KEY = stringPreferencesKey("sip_notifications")
    }

    val settingsFlow: Flow<Settings> = dataStore.data.map { preferences ->
        Settings(
            primaryColor = preferences[PRIMARY_COLOR_KEY]?.let { Color(it.toULong()) } ?: Latte_Blue,
            labelVisibility = LabelVisibility.valueOf(
                preferences[LABEL_VISIBILITY_KEY] ?: LabelVisibility.AlwaysShow.name
            ),
            themeMode = preferences[THEME_MODE_KEY]?.let { ThemeMode.entries[it] } ?: ThemeMode.System,
            showProfileBanner = preferences[SHOW_PROFILE_BANNER_KEY] ?: true,
            notificationSettings = NotificationSettings(
                addTransactionReminder = preferences[ADD_TRANSACTION_REMINDER_KEY] ?: false,
                reminderType = ReminderType.fromString(preferences[REMINDER_TYPE_KEY]) ?: ReminderType.IF_NOT_OPENED_TODAY,
                alertHour = preferences[ALERT_HOUR_KEY] ?: 20,
                alertMinute = preferences[ALERT_MINUTE_KEY] ?: 0,
                upcomingTransactions = preferences[UPCOMING_TRANSACTIONS_KEY] ?: false,
                sipNotifications = preferences[SIP_NOTIFICATIONS_KEY]?.let {
                    try {
                        val type = object : TypeToken<Map<String, Boolean>>() {}.type
                        Gson().fromJson(it, type)
                    } catch (e: Exception) {
                        emptyMap()
                    }
                } ?: emptyMap()
            )
        )
    }

    suspend fun updatePrimaryColor(color: Color) {
        dataStore.edit { preferences ->
            preferences[PRIMARY_COLOR_KEY] = color.value.toString()
        }
        // ENHANCED: Log appearance change to ActivityLog
        activityLogUtils.logSystemAction(
            ActivityActionType.SETTINGS_UPDATED,
            "Primary color changed to ${color.value}"
        )

        // Emit event instead of direct widget update
        AppEventBus.tryEmitEvent(SettingsEvent.PrimaryColorChanged(color))
    }

    suspend fun updateLabelVisibility(labelVisibility: LabelVisibility) {
        dataStore.edit { preferences ->
            preferences[LABEL_VISIBILITY_KEY] = labelVisibility.name
        }
        activityLogUtils.logSystemAction(
            ActivityActionType.SETTINGS_UPDATED,
            "Label visibility changed to ${labelVisibility.name}"
        )
        // Emit event for label visibility change
        AppEventBus.tryEmitEvent(SettingsEvent.LabelVisibilityChanged(labelVisibility))
    }

    suspend fun updateThemeMode(themeMode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = themeMode.ordinal
        }
        // ENHANCED: Log theme change to ActivityLog
        activityLogUtils.logSystemAction(
            ActivityActionType.SETTINGS_UPDATED,
            "Theme mode changed to ${themeMode.name}"
        )

        // Emit event instead of direct widget update
        val isDarkTheme = when (themeMode) {
            ThemeMode.Dark, ThemeMode.Black -> true
            ThemeMode.Light -> false
            else -> false // Will be determined by system
        }
        AppEventBus.tryEmitEvent(SettingsEvent.ThemeChanged(themeMode, isDarkTheme))
    }

    suspend fun updateShowProfileBanner(show: Boolean) {
        dataStore.edit { preferences ->
            preferences[SHOW_PROFILE_BANNER_KEY] = show
        }
        // ENHANCED: Log profile banner setting change to ActivityLog
        activityLogUtils.logSystemAction(
            ActivityActionType.SETTINGS_UPDATED,
            "Profile banner ${if (show) "enabled" else "disabled"}"
        )

        // Emit event for profile banner change
        AppEventBus.tryEmitEvent(SettingsEvent.ProfileBannerChanged(show))
    }

    suspend fun updateAddTransactionReminder(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[ADD_TRANSACTION_REMINDER_KEY] = enabled
        }
        // ENHANCED: Log notification setting change to ActivityLog
        activityLogUtils.logSystemAction(
            ActivityActionType.SETTINGS_UPDATED,
            "Transaction reminder notifications ${if (enabled) "enabled" else "disabled"}"
        )
    }

    suspend fun updateReminderType(reminderType: ReminderType) {
        dataStore.edit { preferences ->
            preferences[REMINDER_TYPE_KEY] = reminderType.name
        }
        // ENHANCED: Log reminder type change to ActivityLog
        activityLogUtils.logSystemAction(
            ActivityActionType.SETTINGS_UPDATED,
            "Reminder type changed to ${reminderType.getDisplayName()}"
        )
    }

    suspend fun updateAlertTime(hour: Int, minute: Int) {
        dataStore.edit { preferences ->
            preferences[ALERT_HOUR_KEY] = hour
            preferences[ALERT_MINUTE_KEY] = minute
        }
        // ENHANCED: Log alert time change to ActivityLog
        activityLogUtils.logSystemAction(
            ActivityActionType.SETTINGS_UPDATED,
            "Alert time changed to ${String.format("%02d:%02d", hour, minute)}"
        )
    }

    suspend fun updateUpcomingTransactions(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[UPCOMING_TRANSACTIONS_KEY] = enabled
        }
        // ENHANCED: Log upcoming transaction notifications setting to ActivityLog
        activityLogUtils.logSystemAction(
            ActivityActionType.SETTINGS_UPDATED,
            "Upcoming transaction notifications ${if (enabled) "enabled" else "disabled"}"
        )
    }

    suspend fun updateSipNotifications(sipNotifications: Map<String, Boolean>) {
        dataStore.edit { preferences ->
            val jsonString = Gson().toJson(sipNotifications)
            preferences[SIP_NOTIFICATIONS_KEY] = jsonString
        }
        // ENHANCED: Log SIP notifications setting to ActivityLog
        activityLogUtils.logSystemAction(
            ActivityActionType.SETTINGS_UPDATED,
            "SIP notifications settings updated"
        )
    }
}

//@Singleton
//class SettingsRepository @Inject constructor(
//    private val dataStore: DataStore<Preferences>,
//    private val widgetUpdateUtil: WidgetUpdateUtil
//) {
//    companion object {
//        private val PRIMARY_COLOR_KEY = stringPreferencesKey("primary_color")
//        private val LABEL_VISIBILITY_KEY = stringPreferencesKey("label_visibility")
//        private val THEME_MODE_KEY = intPreferencesKey("theme_mode")
//        private val SHOW_PROFILE_BANNER_KEY = booleanPreferencesKey("show_profile_banner")
//
//        // Notification preferences keys
//        private val ADD_TRANSACTION_REMINDER_KEY = booleanPreferencesKey("add_transaction_reminder")
//        private val REMINDER_TYPE_KEY = stringPreferencesKey("reminder_type")
//        private val ALERT_HOUR_KEY = intPreferencesKey("alert_hour")
//        private val ALERT_MINUTE_KEY = intPreferencesKey("alert_minute")
//        private val UPCOMING_TRANSACTIONS_KEY = booleanPreferencesKey("upcoming_transactions")
//        private val SIP_NOTIFICATIONS_KEY = stringPreferencesKey("sip_notifications")
//    }
//
//    val settingsFlow: Flow<Settings> = dataStore.data.map { preferences ->
//        Settings(
//            primaryColor = preferences[PRIMARY_COLOR_KEY]?.let { Color(it.toULong()) } ?: Latte_Blue,
//            labelVisibility = LabelVisibility.valueOf(
//                preferences[LABEL_VISIBILITY_KEY] ?: LabelVisibility.AlwaysShow.name
//            ),
//            themeMode = preferences[THEME_MODE_KEY]?.let { ThemeMode.entries[it] } ?: ThemeMode.System,
//            showProfileBanner = preferences[SHOW_PROFILE_BANNER_KEY] ?: true,
//            notificationSettings = NotificationSettings(
//                addTransactionReminder = preferences[ADD_TRANSACTION_REMINDER_KEY] ?: false,
//                reminderType = ReminderType.fromString(preferences[REMINDER_TYPE_KEY]) ?: ReminderType.IF_NOT_OPENED_TODAY,
//                alertHour = preferences[ALERT_HOUR_KEY] ?: 20,
//                alertMinute = preferences[ALERT_MINUTE_KEY] ?: 0,
//                upcomingTransactions = preferences[UPCOMING_TRANSACTIONS_KEY] ?: false,
//                sipNotifications = preferences[SIP_NOTIFICATIONS_KEY]?.let {
//                    // Parse JSON string to Map
//                    try {
//                        val type = object : TypeToken<Map<String, Boolean>>() {}.type
//                        Gson().fromJson(it, type)
//                    } catch (e: Exception) {
//                        emptyMap()
//                    }
//                } ?: emptyMap()
//            )
//        )
//    }
//
//    suspend fun updateLabelVisibility(labelVisibility: LabelVisibility) {
//        dataStore.edit { preferences ->
//            preferences[LABEL_VISIBILITY_KEY] = labelVisibility.name
//        }
//    }
//
//    suspend fun updateShowProfileBanner(show: Boolean) {
//        dataStore.edit { preferences ->
//            preferences[SHOW_PROFILE_BANNER_KEY] = show
//        }
//    }
//
//
//    suspend fun updateAddTransactionReminder(enabled: Boolean) {
//        dataStore.edit { preferences ->
//            preferences[ADD_TRANSACTION_REMINDER_KEY] = enabled
//        }
//    }
//
//    suspend fun updateReminderType(reminderType: ReminderType) {
//        dataStore.edit { preferences ->
//            preferences[REMINDER_TYPE_KEY] = reminderType.name
//        }
//    }
//
//    suspend fun updateAlertTime(hour: Int, minute: Int) {
//        dataStore.edit { preferences ->
//            preferences[ALERT_HOUR_KEY] = hour
//            preferences[ALERT_MINUTE_KEY] = minute
//        }
//    }
//
//    suspend fun updateUpcomingTransactions(enabled: Boolean) {
//        dataStore.edit { preferences ->
//            preferences[UPCOMING_TRANSACTIONS_KEY] = enabled
//        }
//    }
//
//    suspend fun updateSipNotifications(sipNotifications: Map<String, Boolean>) {
//        dataStore.edit { preferences ->
//            // Convert Map to JSON string
//            val jsonString = Gson().toJson(sipNotifications)
//            preferences[SIP_NOTIFICATIONS_KEY] = jsonString
//        }
//    }
//
//    suspend fun updatePrimaryColor(color: Color) {
//        dataStore.edit { preferences ->
//            preferences[PRIMARY_COLOR_KEY] = color.value.toString()
//        }
//        // Update widgets when primary color changes
//        widgetUpdateUtil.updateWidgetsTheme()
//    }
//
//    suspend fun updateThemeMode(themeMode: ThemeMode) {
//        dataStore.edit { preferences ->
//            preferences[THEME_MODE_KEY] = themeMode.ordinal
//        }
//        // Update widgets when theme mode changes
//        widgetUpdateUtil.updateWidgetsTheme()
//    }
//}

data class Settings(
    val primaryColor: Color,
    val labelVisibility: LabelVisibility,
    val themeMode: ThemeMode = ThemeMode.System,
    val showProfileBanner: Boolean = true,
    val notificationSettings: NotificationSettings = NotificationSettings()
)

//enum class ThemeMode {
//    Light, Dark, Black, System, SystemBlack
//}
//
//enum class LabelVisibility {
//    AlwaysShow,
//    SelectedOnly,
//    NeverShow;
//
//    companion object {
//        // This function will be used to convert string values to the corresponding enum
//        fun fromString(value: String?): LabelVisibility? {
//            return when (value) {
//                "AlwaysShow" -> AlwaysShow
//                "SelectedOnly" -> SelectedOnly
//                "NeverShow" -> NeverShow
//                else -> null // Return null if the value doesn't match any enum value
//            }
//        }
//    }
//}
//
//enum class ReminderType {
//    IF_NOT_OPENED_TODAY,
//    HOURS_FROM_OPENED,
//    EVERYDAY;
//
//    fun getDisplayName(): String {
//        return when (this) {
//            IF_NOT_OPENED_TODAY -> "If the app was not already opened today"
//            HOURS_FROM_OPENED -> "24 hours from app opened"
//            EVERYDAY -> "Everyday"
//        }
//    }
//
//    companion object {
//        fun fromString(value: String?): ReminderType? {
//            return when (value) {
//                "IF_NOT_OPENED_TODAY" -> IF_NOT_OPENED_TODAY
//                "HOURS_FROM_OPENED" -> HOURS_FROM_OPENED
//                "EVERYDAY" -> EVERYDAY
//                else -> null
//            }
//        }
//    }
//}
//
//data class NotificationSettings(
//    val addTransactionReminder: Boolean = false,
//    val reminderType: ReminderType = ReminderType.IF_NOT_OPENED_TODAY,
//    val alertHour: Int = 20, // 8 PM
//    val alertMinute: Int = 0,
//    val upcomingTransactions: Boolean = false,
//    val sipNotifications: Map<String, Boolean> = emptyMap()
//)
//
//@Singleton
//class SettingsRepository @Inject constructor(
//    private val dataStore: DataStore<Preferences>
//) {
//    companion object {
//        private val PRIMARY_COLOR_KEY = stringPreferencesKey("primary_color")
//        private val LABEL_VISIBILITY_KEY = stringPreferencesKey("label_visibility")
//        private val THEME_MODE_KEY = intPreferencesKey("theme_mode")
//        private val SHOW_PROFILE_BANNER_KEY = booleanPreferencesKey("show_profile_banner")
//
//        // Notification preferences keys
//        private val ADD_TRANSACTION_REMINDER_KEY = booleanPreferencesKey("add_transaction_reminder")
//        private val REMINDER_TYPE_KEY = stringPreferencesKey("reminder_type")
//        private val ALERT_HOUR_KEY = intPreferencesKey("alert_hour")
//        private val ALERT_MINUTE_KEY = intPreferencesKey("alert_minute")
//        private val UPCOMING_TRANSACTIONS_KEY = booleanPreferencesKey("upcoming_transactions")
//        private val SIP_NOTIFICATIONS_KEY = stringPreferencesKey("sip_notifications")
//    }
//
//    val settingsFlow: Flow<Settings> = dataStore.data.map { preferences ->
//        Settings(
//            primaryColor = preferences[PRIMARY_COLOR_KEY]?.let { Color(it.toULong()) } ?: Latte_Blue,
//            labelVisibility = LabelVisibility.valueOf(
//                preferences[LABEL_VISIBILITY_KEY] ?: LabelVisibility.AlwaysShow.name
//            ),
//            themeMode = preferences[THEME_MODE_KEY]?.let { ThemeMode.entries[it] } ?: ThemeMode.System,
//            showProfileBanner = preferences[SHOW_PROFILE_BANNER_KEY] ?: true,
//            notificationSettings = NotificationSettings(
//                addTransactionReminder = preferences[ADD_TRANSACTION_REMINDER_KEY] ?: false,
//                reminderType = ReminderType.fromString(preferences[REMINDER_TYPE_KEY]) ?: ReminderType.IF_NOT_OPENED_TODAY,
//                alertHour = preferences[ALERT_HOUR_KEY] ?: 20,
//                alertMinute = preferences[ALERT_MINUTE_KEY] ?: 0,
//                upcomingTransactions = preferences[UPCOMING_TRANSACTIONS_KEY] ?: false,
//                sipNotifications = preferences[SIP_NOTIFICATIONS_KEY]?.let {
//                    // Parse JSON string to Map
//                    try {
//                        val type = object : TypeToken<Map<String, Boolean>>() {}.type
//                        Gson().fromJson(it, type)
//                    } catch (e: Exception) {
//                        emptyMap()
//                    }
//                } ?: emptyMap()
//            )
//        )
//    }
//
//    suspend fun updatePrimaryColor(color: Color) {
//        dataStore.edit { preferences ->
//            preferences[PRIMARY_COLOR_KEY] = color.value.toString()
//        }
//    }
//
//    suspend fun updateLabelVisibility(labelVisibility: LabelVisibility) {
//        dataStore.edit { preferences ->
//            preferences[LABEL_VISIBILITY_KEY] = labelVisibility.name
//        }
//    }
//
//    suspend fun updateThemeMode(themeMode: ThemeMode) {
//        dataStore.edit { preferences ->
//            preferences[THEME_MODE_KEY] = themeMode.ordinal
//        }
//    }
//
//    suspend fun updateShowProfileBanner(show: Boolean) {
//        dataStore.edit { preferences ->
//            preferences[SHOW_PROFILE_BANNER_KEY] = show
//        }
//    }
//
//
//    suspend fun updateAddTransactionReminder(enabled: Boolean) {
//        dataStore.edit { preferences ->
//            preferences[ADD_TRANSACTION_REMINDER_KEY] = enabled
//        }
//    }
//
//    suspend fun updateReminderType(reminderType: ReminderType) {
//        dataStore.edit { preferences ->
//            preferences[REMINDER_TYPE_KEY] = reminderType.name
//        }
//    }
//
//    suspend fun updateAlertTime(hour: Int, minute: Int) {
//        dataStore.edit { preferences ->
//            preferences[ALERT_HOUR_KEY] = hour
//            preferences[ALERT_MINUTE_KEY] = minute
//        }
//    }
//
//    suspend fun updateUpcomingTransactions(enabled: Boolean) {
//        dataStore.edit { preferences ->
//            preferences[UPCOMING_TRANSACTIONS_KEY] = enabled
//        }
//    }
//
//    suspend fun updateSipNotifications(sipNotifications: Map<String, Boolean>) {
//        dataStore.edit { preferences ->
//            // Convert Map to JSON string
//            val jsonString = Gson().toJson(sipNotifications)
//            preferences[SIP_NOTIFICATIONS_KEY] = jsonString
//        }
//    }
//}
//
//data class Settings(
//    val primaryColor: Color,
//    val labelVisibility: LabelVisibility,
//    val themeMode: ThemeMode = ThemeMode.System,
//    val showProfileBanner: Boolean = true,
//    val notificationSettings: NotificationSettings = NotificationSettings()
//)