package com.ritesh.cashiro.presentation.ui.features.profile

import android.net.Uri
import androidx.compose.ui.graphics.Color
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Blue

data class ProfileScreenState(
    // User profile details
    val userName: String = "",
    val profileImageUri: Uri? = null,
    val bannerImageUri: Uri? = null,
    val profileBackgroundColor: Color = Macchiato_Blue,

    // Financial metrics
    val totalNetWorth: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlyExpenses: Double = 0.0,

    // Transaction statistics
    val totalTransactions: Int = 0,
    val upcomingTransactions: Int = 0,
    val overdueTransactions: Int = 0,

    // UI state flags
    val isLoading: Boolean = true,
    val isEditSheetOpen: Boolean = false,
    val hasStoragePermission: Boolean = false,

    // Editing state
    val editState: EditProfileState = EditProfileState()
)

/**
 * Data class representing the state for profile editing
 */
data class EditProfileState(
    val editedUserName: String = "",
    val editedProfileImageUri: Uri? = null,
    val editedBannerImageUri: Uri? = null,
    val editedProfileBackgroundColor: Color = Macchiato_Blue,
    val hasChanges: Boolean = false
)

/**
 * Sealed class for profile-related events/actions
 */
sealed class ProfileScreenEvent {
    // Profile data updates
    data class UpdateUserName(val name: String) : ProfileScreenEvent()
    data class UpdateProfileImage(val imageUri: Uri?) : ProfileScreenEvent()
    data class UpdateBannerImage(val imageUri: Uri?) : ProfileScreenEvent()
    data class UpdateProfileBackgroundColor(val color: Color) : ProfileScreenEvent()

    // UI state events
    data object ToggleEditSheet : ProfileScreenEvent()
    data object DismissEditSheet : ProfileScreenEvent()
    data object SaveProfileChanges : ProfileScreenEvent()
    data object RefreshProfileData : ProfileScreenEvent()

    // Permission events
    data object RequestStoragePermission : ProfileScreenEvent()
    data class UpdateStoragePermission(val granted: Boolean) : ProfileScreenEvent()

    // Edit state events
    data class UpdateEditUserName(val name: String) : ProfileScreenEvent()
    data class UpdateEditProfileImage(val imageUri: Uri?) : ProfileScreenEvent()
    data class UpdateEditBannerImage(val imageUri: Uri?) : ProfileScreenEvent()
    data class UpdateEditProfileBackgroundColor(val color: Color) : ProfileScreenEvent()
}

