package com.ritesh.cashiro.presentation.ui.features.onboarding

import android.net.Uri
import androidx.compose.ui.graphics.Color
import com.ritesh.cashiro.data.currency.model.Currency
import com.ritesh.cashiro.presentation.ui.theme.Latte_Blue

enum class OnBoardingStep {
    WELCOME,
    NOTIFICATION_PERMISSION,
    STORAGE_PERMISSION,
    USER_PROFILE,
    FIRST_ACCOUNT,
    COMPLETION
}

data class OnBoardingState(
    // Current step
    val currentStep: OnBoardingStep = OnBoardingStep.WELCOME,
    val isLoading: Boolean = false,

    // Permission states
    val hasNotificationPermission: Boolean = false,
    val hasStoragePermission: Boolean = false,
    val isRequestingPermission: Boolean = false,

    // User profile data
    val userName: String = "",
    val profileImageUri: Uri? = null,
    val profileBackgroundColor: Color = Latte_Blue,

    // First account data
    val accountName: String = "",
    val accountBalance: String = "",
    val accountColor1: Color? = null,
    val accountColor2: Color? = null,
    val selectedCurrency: String = "usd",

    // Available currencies
    val availableCurrencies: List<Currency> = emptyList(),

    // Completion state
    val isOnBoardingComplete: Boolean = false,
    val error: String? = null
)

sealed class OnBoardingEvent {
    // Navigation events
    object NextStep : OnBoardingEvent()
    object PreviousStep : OnBoardingEvent()
    data class GoToStep(val step: OnBoardingStep) : OnBoardingEvent()

    // Permission events
    object RequestNotificationPermission : OnBoardingEvent()
    object RequestStoragePermission : OnBoardingEvent()
    data class UpdateNotificationPermission(val granted: Boolean) : OnBoardingEvent()
    data class UpdateStoragePermission(val granted: Boolean) : OnBoardingEvent()

    // Profile events
    data class UpdateUserName(val name: String) : OnBoardingEvent()
    data class UpdateProfileImage(val uri: Uri?) : OnBoardingEvent()
    data class UpdateProfileBackgroundColor(val color: Color) : OnBoardingEvent()

    // Account events
    data class UpdateAccountName(val name: String) : OnBoardingEvent()
    data class UpdateAccountBalance(val balance: String) : OnBoardingEvent()
    data class UpdateAccountColor1(val color: Color) : OnBoardingEvent()
    data class UpdateAccountColor2(val color: Color) : OnBoardingEvent()
    data class UpdateSelectedCurrency(val currency: String) : OnBoardingEvent()

    // Completion events
    object CompleteOnBoarding : OnBoardingEvent()
    object SkipOnBoarding : OnBoardingEvent()

    // Error handling
    data class ShowError(val message: String) : OnBoardingEvent()
    object ClearError : OnBoardingEvent()
}