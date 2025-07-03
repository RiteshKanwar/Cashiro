package com.ritesh.cashiro.widgets

import android.content.Context
import android.graphics.Color
import android.widget.RemoteViews
import com.ritesh.cashiro.domain.repository.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit
import androidx.core.graphics.toColorInt
import com.ritesh.cashiro.R

// WidgetThemeManager.kt
@Singleton
class WidgetThemeManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    companion object {
        private const val THEME_PREFS = "widget_theme_prefs"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_PRIMARY_COLOR = "primary_color"
        private const val KEY_IS_DARK_THEME = "is_dark_theme"
        private const val KEY_USE_BLACK_THEME = "use_black_theme"
    }

    private val prefs = context.getSharedPreferences(THEME_PREFS, Context.MODE_PRIVATE)

    /**
     * Save current theme settings for widgets to use
     */
    fun saveThemeSettings(
        themeMode: ThemeMode,
        primaryColor: Int,
        isDarkTheme: Boolean,
        useBlackTheme: Boolean,
    ) {
        prefs.edit {
            putString(KEY_THEME_MODE, themeMode.name)
                .putInt(KEY_PRIMARY_COLOR, primaryColor)
                .putBoolean(KEY_IS_DARK_THEME, isDarkTheme)
                .putBoolean(KEY_USE_BLACK_THEME, useBlackTheme)
        }
    }

    /**
     * Get theme colors for widgets
     */
    fun getWidgetTheme(): WidgetTheme {
        val isDark = prefs.getBoolean(KEY_IS_DARK_THEME, false)
        val useBlack = prefs.getBoolean(KEY_USE_BLACK_THEME, false)
        val primaryColor = prefs.getInt(KEY_PRIMARY_COLOR, getDefaultPrimaryColor(isDark))

        return when {
            isDark && useBlack -> createBlackTheme(primaryColor)
            isDark -> createDarkTheme(primaryColor)
            else -> createLightTheme(primaryColor)
        }
    }

    private fun createLightTheme(primaryColor: Int): WidgetTheme {
        return WidgetTheme(
            primary = primaryColor,
            background = "#eff1f5".toColorInt(), // Latte_Base
            surface = "#e6e9ef".toColorInt(), // Latte_Mantle
            onBackground = "#4c4f69".toColorInt(), // Latte_Text
            onSurface = "#4c4f69".toColorInt(), // Latte_Text
            surfaceVariant = "#dce0e8".toColorInt(), // Latte_Crust
            onSurfaceVariant = "#6c6f85".toColorInt(), // Latte_SubText_0
            outline = "#9ca0b0".toColorInt(), // Latte_Overlay_0
            incomeColor = "#40a02b".toColorInt(), // Latte_Green
            expenseColor = "#d20f39".toColorInt(), // Latte_Red
            secondaryText = "#5c5f77".toColorInt(), // Latte_SubText_1
            isDark = false
        )
    }

    private fun createDarkTheme(primaryColor: Int): WidgetTheme {
        return WidgetTheme(
            primary = primaryColor,
            background = "#181926".toColorInt(), // Macchiato_Crust
            surface = "#24273a".toColorInt(), // Macchiato_Base
            onBackground = "#cad3f5".toColorInt(), // Macchiato_Text
            onSurface = "#cad3f5".toColorInt(), // Macchiato_Text
            surfaceVariant = "#1e2030".toColorInt(), // Macchiato_Mantle
            onSurfaceVariant = "#a5adcb".toColorInt(), // Macchiato_SubText_0
            outline = "#6e738d".toColorInt(), // Macchiato_Overlay_0
            incomeColor = "#93B887".toColorInt(), // Macchiato_Green
            expenseColor = "#ed8796".toColorInt(), // Macchiato_Red
            secondaryText = "#b8c0e0".toColorInt(), // Macchiato_SubText_1
            isDark = true
        )
    }

    private fun createBlackTheme(primaryColor: Int): WidgetTheme {
        return WidgetTheme(
            primary = primaryColor,
            background = Color.BLACK, // Amoled_Crust
            surface = "#1c1c1e".toColorInt(), // Amoled_Base
            onBackground = "#cad3f5".toColorInt(), // Amoled_Text
            onSurface = "#cad3f5".toColorInt(), // Amoled_Text
            surfaceVariant = "#141415".toColorInt(), // Amoled_Mantle
            onSurfaceVariant = "#aeaeb2".toColorInt(), // Amoled_SubText_0
            outline = "#636366".toColorInt(), // Amoled_Overlay_0
            incomeColor = "#93B887".toColorInt(), // Same as dark
            expenseColor = "#ed8796".toColorInt(), // Same as dark
            secondaryText = "#d1d1d6".toColorInt(), // Amoled_SubText_1
            isDark = true
        )
    }

    private fun getDefaultPrimaryColor(isDark: Boolean): Int {
        return if (isDark) {
            "#ed8796".toColorInt() // Macchiato_Red
        } else {
            "#d20f39".toColorInt() // Latte_Red
        }
    }

    /**
     * Apply theme to RemoteViews
     */
    fun applyThemeToWidget(views: RemoteViews, theme: WidgetTheme) {
        // Apply background colors
        views.setInt(R.id.widget_root, "setBackgroundColor", theme.background)

        // Note: For drawable backgrounds, we'll need to update the drawable resources
        // or create them programmatically
    }

    /**
     * Get appropriate text color based on balance value
     */
    fun getBalanceTextColor(balance: Double, theme: WidgetTheme): Int {
        return if (balance >= 0) theme.incomeColor else theme.expenseColor
    }
}

/**
 * Data class representing widget theme colors
 */
data class WidgetTheme(
    val primary: Int,
    val background: Int,
    val surface: Int,
    val onBackground: Int,
    val onSurface: Int,
    val surfaceVariant: Int,
    val onSurfaceVariant: Int,
    val outline: Int,
    val incomeColor: Int,
    val expenseColor: Int,
    val secondaryText: Int,
    val isDark: Boolean,
)
