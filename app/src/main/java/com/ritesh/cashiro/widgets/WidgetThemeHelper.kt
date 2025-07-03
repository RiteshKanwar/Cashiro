package com.ritesh.cashiro.widgets

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.ritesh.cashiro.domain.repository.SettingsRepository
import com.ritesh.cashiro.domain.repository.ThemeMode
import com.ritesh.cashiro.presentation.ui.theme.Amoled_Base
import com.ritesh.cashiro.presentation.ui.theme.Amoled_Crust
import com.ritesh.cashiro.presentation.ui.theme.Amoled_SubText_0
import com.ritesh.cashiro.presentation.ui.theme.Amoled_Text
import com.ritesh.cashiro.presentation.ui.theme.Latte_Base
import com.ritesh.cashiro.presentation.ui.theme.Latte_Blue
import com.ritesh.cashiro.presentation.ui.theme.Latte_Crust
import com.ritesh.cashiro.presentation.ui.theme.Latte_Green
import com.ritesh.cashiro.presentation.ui.theme.Latte_Red
import com.ritesh.cashiro.presentation.ui.theme.Latte_SubText_0
import com.ritesh.cashiro.presentation.ui.theme.Latte_SubText_1
import com.ritesh.cashiro.presentation.ui.theme.Latte_Surface_0
import com.ritesh.cashiro.presentation.ui.theme.Latte_Text
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Base
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Crust
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Green
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Red
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_SubText_0
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Surface_0
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Text
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetThemeHelper @Inject constructor(
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "WidgetThemeHelper"
        private var cachedThemeColors: WidgetThemeColors? = null
        private var lastThemeUpdate: Long = 0
        private const val CACHE_DURATION = 5000L // 5 seconds cache
    }

    suspend fun getThemeColors(): WidgetThemeColors {
        val currentTime = System.currentTimeMillis()

        // Return cached colors if still valid
        if (cachedThemeColors != null && (currentTime - lastThemeUpdate) < CACHE_DURATION) {
            return cachedThemeColors!!
        }

        return try {
            val settings = settingsRepository.settingsFlow.first()
            val primaryColor = settings.primaryColor
            val themeMode = settings.themeMode

            val isDarkTheme = when (themeMode) {
                ThemeMode.Light -> false
                ThemeMode.Dark, ThemeMode.Black -> true
                ThemeMode.System, ThemeMode.SystemBlack -> {
                    val nightMode = context.resources.configuration.uiMode and
                            Configuration.UI_MODE_NIGHT_MASK
                    nightMode == Configuration.UI_MODE_NIGHT_YES
                }
            }

            val isBlackTheme = when (themeMode) {
                ThemeMode.Black -> true
                ThemeMode.SystemBlack -> {
                    val nightMode = context.resources.configuration.uiMode and
                            Configuration.UI_MODE_NIGHT_MASK
                    nightMode == Configuration.UI_MODE_NIGHT_YES
                }
                else -> false
            }

            val themeColors = when {
                isBlackTheme -> createBlackTheme(primaryColor.toArgb())
                isDarkTheme -> createDarkTheme(primaryColor.toArgb())
                else -> createLightTheme(primaryColor.toArgb())
            }

            // Cache the result
            cachedThemeColors = themeColors
            lastThemeUpdate = currentTime

            Log.d(TAG, "Generated theme colors - isDark: $isDarkTheme, isBlack: $isBlackTheme, primary: ${primaryColor.toArgb()}")
            themeColors

        } catch (e: Exception) {
            Log.e(TAG, "Error getting theme colors, using fallback", e)
            // Fallback to default light theme
            createLightTheme(Latte_Blue.toArgb())
        }
    }

    private fun createLightTheme(primaryColor: Int): WidgetThemeColors {
        return WidgetThemeColors(
            primaryColor = primaryColor,
            backgroundColor = Latte_Crust.toArgb(),
            surfaceColor = Latte_Surface_0.toArgb(),
            textColorPrimary = Latte_Text.toArgb(),
            textColorSecondary = Latte_SubText_0.toArgb(),
            positiveColor = Latte_Green.toArgb(),
            negativeColor = Latte_Red.toArgb(),
            isDarkTheme = false
        )
    }

    private fun createDarkTheme(primaryColor: Int): WidgetThemeColors {
        return WidgetThemeColors(
            primaryColor = primaryColor,
            backgroundColor = Macchiato_Crust.toArgb(),
            surfaceColor = Macchiato_Surface_0.toArgb(),
            textColorPrimary = Macchiato_Text.toArgb(),
            textColorSecondary = Macchiato_SubText_0.toArgb(),
            positiveColor = Macchiato_Green.toArgb(),
            negativeColor = Macchiato_Red.toArgb(),
            isDarkTheme = true
        )
    }

    private fun createBlackTheme(primaryColor: Int): WidgetThemeColors {
        return WidgetThemeColors(
            primaryColor = primaryColor,
            backgroundColor = Amoled_Crust.toArgb(),
            surfaceColor = Amoled_Base.toArgb(),
            textColorPrimary = Amoled_Text.toArgb(),
            textColorSecondary = Amoled_SubText_0.toArgb(),
            positiveColor = Macchiato_Green.toArgb(),
            negativeColor = Macchiato_Red.toArgb(),
            isDarkTheme = true
        )
    }

    // Clear cache when theme changes
    fun clearCache() {
        cachedThemeColors = null
        lastThemeUpdate = 0
    }
}
//data class WidgetThemeColors(
//    val primaryColor: Int,
//    val backgroundColor: Int,
//    val surfaceColor: Int,
//    val textColorPrimary: Int,
//    val textColorSecondary: Int,
//    val positiveColor: Int,
//    val negativeColor: Int,
//    val isDarkTheme: Boolean
//)
//@Singleton
//class WidgetThemeHelper @Inject constructor(
//    private val settingsRepository: SettingsRepository,
//    @ApplicationContext private val context: Context
//) {
//
//    suspend fun getThemeColors(): WidgetThemeColors {
//        return settingsRepository.settingsFlow.first().let { settings ->
//            val primaryColor = settings.primaryColor
//            val themeMode = settings.themeMode
//            val isDarkTheme = when (themeMode) {
//                ThemeMode.Light -> false
//                ThemeMode.Dark, ThemeMode.Black -> true
//                ThemeMode.System, ThemeMode.SystemBlack -> {
//                    val nightMode = context.resources.configuration.uiMode and
//                            Configuration.UI_MODE_NIGHT_MASK
//                    nightMode == Configuration.UI_MODE_NIGHT_YES
//                }
//            }
//
//            WidgetThemeColors(
//                primaryColor = primaryColor.toArgb(),
//                backgroundColor = if (isDarkTheme) Macchiato_Base.toArgb() else Latte_Base.toArgb(),
//                surfaceColor = if (isDarkTheme) Macchiato_Surface_0.toArgb() else Latte_Surface_0.toArgb(),
//                textColorPrimary = if (isDarkTheme) Macchiato_Text.toArgb() else Latte_Text.toArgb(),
//                textColorSecondary = if (isDarkTheme) Macchiato_Text.copy(0.6f).toArgb() else Latte_Text.copy(0.6f).toArgb(),
//                positiveColor = if (isDarkTheme) Macchiato_Green.toArgb() else Latte_Green.toArgb(),
//                negativeColor = if (isDarkTheme) Macchiato_Red.toArgb() else Latte_Red.toArgb(),
//                isDarkTheme = isDarkTheme
//            )
//        }
//    }
//}
//
//data class WidgetThemeColors(
//    val primaryColor: Int,
//    val backgroundColor: Int,
//    val surfaceColor: Int,
//    val textColorPrimary: Int,
//    val textColorSecondary: Int,
//    val positiveColor: Int,
//    val negativeColor: Int,
//    val isDarkTheme: Boolean
//)
