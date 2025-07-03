package com.ritesh.cashiro.presentation.ui.features.appearance

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ritesh.cashiro.domain.repository.LabelVisibility
import com.ritesh.cashiro.domain.repository.Settings
import com.ritesh.cashiro.domain.repository.SettingsRepository
import com.ritesh.cashiro.domain.repository.ThemeMode
import com.ritesh.cashiro.presentation.ui.theme.Latte_Blue
import com.ritesh.cashiro.widgets.WidgetUpdateUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppearanceViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<Settings> = settingsRepository.settingsFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        Settings(
            primaryColor = Latte_Blue,
            labelVisibility = LabelVisibility.AlwaysShow,
            themeMode = ThemeMode.System,
            showProfileBanner = true
        )
    )

    fun updatePrimaryColor(color: Color) {
        viewModelScope.launch {
            settingsRepository.updatePrimaryColor(color)
            // Widget updates now happen automatically via events!
        }
    }

    fun updateThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.updateThemeMode(themeMode)
            // Widget updates now happen automatically via events!
        }
    }

    fun updateLabelVisibility(labelVisibility: LabelVisibility) {
        viewModelScope.launch {
            settingsRepository.updateLabelVisibility(labelVisibility)
        }
    }

    fun updateShowProfileBanner(show: Boolean) {
        viewModelScope.launch {
            settingsRepository.updateShowProfileBanner(show)
        }
    }
}

//@HiltViewModel
//class AppearanceViewModel @Inject constructor(
//    private val settingsRepository: SettingsRepository,
//    private val widgetUpdateUtil: WidgetUpdateUtil
//) : ViewModel() {
//    val settings: StateFlow<Settings> = settingsRepository.settingsFlow.stateIn(
//        viewModelScope,
//        SharingStarted.WhileSubscribed(5000),
//        Settings(
//            primaryColor = Latte_Blue,
//            labelVisibility = LabelVisibility.AlwaysShow,
//            themeMode = ThemeMode.System,
//            showProfileBanner = true
//        )
//    )
//
//    fun updatePrimaryColor(color: Color) {
//        viewModelScope.launch {
//            settingsRepository.updatePrimaryColor(color)
//            // Update widgets when theme changes
//            widgetUpdateUtil.updateWidgetsTheme()
//        }
//    }
//
//    fun updateLabelVisibility(labelVisibility: LabelVisibility) {
//        viewModelScope.launch {
//            settingsRepository.updateLabelVisibility(labelVisibility)
//        }
//    }
//
//    fun updateThemeMode(themeMode: ThemeMode) {
//        viewModelScope.launch {
//            settingsRepository.updateThemeMode(themeMode)
//            // Update widgets when theme mode changes
//            widgetUpdateUtil.updateWidgetsTheme()
//        }
//    }
//
//    fun updateShowProfileBanner(show: Boolean) {
//        viewModelScope.launch {
//            settingsRepository.updateShowProfileBanner(show)
//        }
//    }
//}

//@HiltViewModel
//class AppearanceViewModel @Inject constructor(
//    private val settingsRepository: SettingsRepository
//) : ViewModel() {
//    val settings: StateFlow<Settings> = settingsRepository.settingsFlow.stateIn(
//        viewModelScope,
//        SharingStarted.WhileSubscribed(5000),
//        Settings(
//            primaryColor = Latte_Blue,
//            labelVisibility = LabelVisibility.AlwaysShow,
//            themeMode = ThemeMode.System,
//            showProfileBanner = true
//        )
//    )
//
//    fun updatePrimaryColor(color: Color) {
//        viewModelScope.launch {
//            settingsRepository.updatePrimaryColor(color)
//        }
//    }
//
//    fun updateLabelVisibility(labelVisibility: LabelVisibility) {
//        viewModelScope.launch {
//            settingsRepository.updateLabelVisibility(labelVisibility)
//        }
//    }
//    fun updateThemeMode(themeMode: ThemeMode) {
//        viewModelScope.launch {
//            settingsRepository.updateThemeMode(themeMode)
//        }
//    }
//
//    fun updateShowProfileBanner(show: Boolean) {
//        viewModelScope.launch {
//            settingsRepository.updateShowProfileBanner(show)
//        }
//    }
//
//}