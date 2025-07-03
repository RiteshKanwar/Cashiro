package com.ritesh.cashiro.presentation.ui.features.appearance

import androidx.compose.ui.graphics.Color
import com.ritesh.cashiro.domain.repository.LabelVisibility
import com.ritesh.cashiro.domain.repository.ThemeMode
import com.ritesh.cashiro.domain.utils.AppEvent

sealed class SettingsEvent : AppEvent {
    data class ThemeChanged(
        val newThemeMode: ThemeMode,
        val isDarkTheme: Boolean
    ) : SettingsEvent()

    data class PrimaryColorChanged(
        val newColor: Color
    ) : SettingsEvent()

    data class LabelVisibilityChanged(
        val labelVisibility: LabelVisibility
    ) : SettingsEvent()

    data class ProfileBannerChanged(
        val showBanner: Boolean
    ) : SettingsEvent()
}