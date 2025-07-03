package com.ritesh.cashiro.presentation.ui.features.profile

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import com.ritesh.cashiro.data.local.entity.TransactionType
import com.ritesh.cashiro.domain.repository.ProfileRepository
import com.ritesh.cashiro.domain.utils.toLocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.util.Log
import androidx.lifecycle.ViewModel
import java.time.LocalDate
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel
import android.net.Uri
import com.ritesh.cashiro.domain.repository.AccountRepository
import com.ritesh.cashiro.domain.repository.TransactionRepository
import com.ritesh.cashiro.domain.utils.ActivityLogUtils
import com.ritesh.cashiro.presentation.ui.features.activity_logs.ActivityActionType

// Enhanced ProfileScreenViewModel.kt with ActivityLog Integration
@HiltViewModel
class ProfileScreenViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val activityLogUtils: ActivityLogUtils
) : ViewModel() {

    // Single state object for the entire profile screen
    private val _state = MutableStateFlow(ProfileScreenState())
    val state: StateFlow<ProfileScreenState> = _state.asStateFlow()

    init {
        loadProfileData()
    }

    private fun loadProfileData() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                profileRepository.getProfileData().collect { profileInfo ->
                    val accounts = accountRepository.getAllAccounts()
                    val transactions = transactionRepository.getAllTransactions()

                    val totalNetWorth = accounts.sumOf { it.balance }
                    val currentMonth = LocalDate.now().monthValue
                    val currentYear = LocalDate.now().year

                    val monthlyTransactions = transactions.filter { transaction ->
                        val transactionDate = transaction.date.toLocalDate()
                        transactionDate.monthValue == currentMonth &&
                                transactionDate.year == currentYear &&
                                transaction.mode != "Transfer"
                    }

                    val monthlyIncome = monthlyTransactions
                        .filter { it.mode == "Income" }
                        .sumOf { it.amount }

                    val monthlyExpenses = monthlyTransactions
                        .filter { it.mode == "Expense" }
                        .sumOf { it.amount }

                    val newState = ProfileScreenState(
                        userName = profileInfo.userName,
                        profileImageUri = profileInfo.profileImageUri,
                        bannerImageUri = profileInfo.bannerImageUri,
                        profileBackgroundColor = profileInfo.profileBackgroundColor,
                        totalNetWorth = totalNetWorth,
                        monthlyIncome = monthlyIncome,
                        monthlyExpenses = monthlyExpenses,
                        totalTransactions = transactions.size,
                        upcomingTransactions = transactions.count {
                            it.transactionType == TransactionType.UPCOMING && !it.isPaid
                        },
                        overdueTransactions = transactions.count { transaction ->
                            transaction.transactionType == TransactionType.UPCOMING &&
                                    transaction.nextDueDate?.let { dueDate ->
                                        dueDate < System.currentTimeMillis()
                                    } ?: false
                        },
                        editState = EditProfileState(
                            editedUserName = profileInfo.userName,
                            editedProfileImageUri = profileInfo.profileImageUri,
                            editedBannerImageUri = profileInfo.bannerImageUri,
                            editedProfileBackgroundColor = profileInfo.profileBackgroundColor
                        ),
                        isLoading = false
                    )

                    _state.value = newState
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false) }
                Log.e("ProfileViewModel", "Error loading profile data: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun handleEvent(event: ProfileScreenEvent) {
        when (event) {
            // Profile data updates
            is ProfileScreenEvent.UpdateUserName -> {
                saveUserName(event.name)
            }
            is ProfileScreenEvent.UpdateProfileImage -> {
                saveProfileImage(event.imageUri)
            }
            is ProfileScreenEvent.UpdateBannerImage -> {
                saveBannerImage(event.imageUri)
            }
            is ProfileScreenEvent.UpdateProfileBackgroundColor -> {
                saveProfileBackgroundColor(event.color)
            }

            // UI state events
            is ProfileScreenEvent.ToggleEditSheet -> {
                _state.update {
                    it.copy(isEditSheetOpen = !it.isEditSheetOpen)
                }
            }
            is ProfileScreenEvent.DismissEditSheet -> {
                _state.update {
                    it.copy(isEditSheetOpen = false)
                }
            }
            is ProfileScreenEvent.SaveProfileChanges -> {
                saveProfileChanges()
            }
            is ProfileScreenEvent.RefreshProfileData -> {
                loadProfileData()
            }

            // Permission events
            is ProfileScreenEvent.RequestStoragePermission -> {
                // This would be handled in UI layer
            }
            is ProfileScreenEvent.UpdateStoragePermission -> {
                _state.update {
                    it.copy(hasStoragePermission = event.granted)
                }
            }

            // Edit state events
            is ProfileScreenEvent.UpdateEditUserName -> {
                updateEditState {
                    it.copy(
                        editedUserName = event.name,
                        hasChanges = checkForChanges(
                            it.copy(editedUserName = event.name)
                        )
                    )
                }
            }
            is ProfileScreenEvent.UpdateEditProfileImage -> {
                updateEditState {
                    it.copy(
                        editedProfileImageUri = event.imageUri,
                        hasChanges = checkForChanges(
                            it.copy(editedProfileImageUri = event.imageUri)
                        )
                    )
                }
            }
            is ProfileScreenEvent.UpdateEditBannerImage -> {
                updateEditState {
                    it.copy(
                        editedBannerImageUri = event.imageUri,
                        hasChanges = checkForChanges(
                            it.copy(editedBannerImageUri = event.imageUri)
                        )
                    )
                }
            }
            is ProfileScreenEvent.UpdateEditProfileBackgroundColor -> {
                updateEditState {
                    it.copy(
                        editedProfileBackgroundColor = event.color,
                        hasChanges = checkForChanges(
                            it.copy(editedProfileBackgroundColor = event.color)
                        )
                    )
                }
            }
        }
    }

    private fun updateEditState(update: (EditProfileState) -> EditProfileState) {
        _state.update { it.copy(editState = update(it.editState)) }
    }

    private fun checkForChanges(editState: EditProfileState): Boolean {
        val currentState = _state.value
        return editState.editedUserName != currentState.userName ||
                editState.editedProfileImageUri != currentState.profileImageUri ||
                editState.editedBannerImageUri != currentState.bannerImageUri ||
                editState.editedProfileBackgroundColor != currentState.profileBackgroundColor
    }

    private fun saveProfileChanges() {
        val editState = _state.value.editState

        if (editState.hasChanges) {
            viewModelScope.launch {
                try {
                    val changesList = mutableListOf<String>()

                    // Save each changed value
                    if (editState.editedUserName != _state.value.userName) {
                        profileRepository.saveUserName(editState.editedUserName)
                        changesList.add("Username changed to '${editState.editedUserName}'")
                    }

                    if (editState.editedProfileImageUri != _state.value.profileImageUri) {
                        val validatedUri = profileRepository.validateAndSaveImageUri(editState.editedProfileImageUri)
                        profileRepository.saveProfileImageUri(validatedUri)
                        changesList.add("Profile image updated")
                    }

                    if (editState.editedBannerImageUri != _state.value.bannerImageUri) {
                        val validatedUri = profileRepository.validateAndSaveImageUri(editState.editedBannerImageUri)
                        profileRepository.saveBannerImageUri(validatedUri)
                        changesList.add("Banner image updated")
                    }

                    if (editState.editedProfileBackgroundColor != _state.value.profileBackgroundColor) {
                        profileRepository.saveProfileBackgroundColor(editState.editedProfileBackgroundColor)
                        changesList.add("Background color updated")
                    }

                    // ENHANCED: Log profile changes to ActivityLog
                    if (changesList.isNotEmpty()) {
                        activityLogUtils.logSystemAction(
                            ActivityActionType.SETTINGS_UPDATED,
                            "Profile updated: ${changesList.joinToString(", ")}"
                        )
                    }

                    _state.update { it.copy(isEditSheetOpen = false) }
                    loadProfileData()

                } catch (e: Exception) {
                    Log.e("ProfileViewModel", "Error updating profile: ${e.message}")
                }
            }
        } else {
            _state.update { it.copy(isEditSheetOpen = false) }
        }
    }

    private fun saveUserName(name: String) {
        viewModelScope.launch {
            try {
                val oldName = _state.value.userName
                profileRepository.saveUserName(name)

                // ENHANCED: Log username change to ActivityLog
                activityLogUtils.logSystemAction(
                    ActivityActionType.SETTINGS_UPDATED,
                    "Username changed from '$oldName' to '$name'"
                )

                loadProfileData()
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error updating username: ${e.message}")
            }
        }
    }

    private fun saveProfileImage(imageUri: Uri?) {
        viewModelScope.launch {
            try {
                val validatedUri = profileRepository.validateAndSaveImageUri(imageUri)
                profileRepository.saveProfileImageUri(validatedUri)

                // ENHANCED: Log profile image change to ActivityLog
                activityLogUtils.logSystemAction(
                    ActivityActionType.SETTINGS_UPDATED,
                    "Profile image updated"
                )

                loadProfileData()
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error updating profile image: ${e.message}")
            }
        }
    }

    private fun saveBannerImage(imageUri: Uri?) {
        viewModelScope.launch {
            try {
                val validatedUri = profileRepository.validateAndSaveImageUri(imageUri)
                profileRepository.saveBannerImageUri(validatedUri)

                // ENHANCED: Log banner image change to ActivityLog
                activityLogUtils.logSystemAction(
                    ActivityActionType.SETTINGS_UPDATED,
                    "Banner image updated"
                )

                loadProfileData()
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error updating banner image: ${e.message}")
            }
        }
    }

    private fun saveProfileBackgroundColor(color: Color) {
        viewModelScope.launch {
            try {
                profileRepository.saveProfileBackgroundColor(color)

                // ENHANCED: Log background color change to ActivityLog
                activityLogUtils.logSystemAction(
                    ActivityActionType.SETTINGS_UPDATED,
                    "Profile background color changed"
                )

                loadProfileData()
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "Error updating profile background color: ${e.message}")
            }
        }
    }
}

//@HiltViewModel
//class ProfileScreenViewModel @Inject constructor(
//    private val profileRepository: ProfileRepository,
//    private val transactionRepository: TransactionRepository, // Added for transaction count
//    private val accountRepository: AccountRepository, // Added for account data
//    private val activityLogUtils: ActivityLogUtils
//) : ViewModel() {
//
//    // Single state object for the entire profile screen
//    private val _state = MutableStateFlow(ProfileScreenState())
//    val state: StateFlow<ProfileScreenState> = _state.asStateFlow()
//
//    init {
//        loadProfileData()
//    }
//
//    private fun loadProfileData() {
//        viewModelScope.launch {
//            try {
//                // Set loading state
//                _state.update { it.copy(isLoading = true) }
//
//                // Collect profile data (this is a Flow)
//                profileRepository.getProfileData().collect { profileInfo ->
//                    // Get accounts and transactions (these are suspend functions)
//                    val accounts = accountRepository.getAllAccounts()
//                    val transactions = transactionRepository.getAllTransactions()
//
//                    // Calculate financial metrics
//                    val totalNetWorth = accounts.sumOf { it.balance }
//                    val currentMonth = LocalDate.now().monthValue
//                    val currentYear = LocalDate.now().year
//
//                    val monthlyTransactions = transactions.filter { transaction ->
//                        val transactionDate = transaction.date.toLocalDate()
//                        transactionDate.monthValue == currentMonth &&
//                                transactionDate.year == currentYear &&
//                                transaction.mode != "Transfer"
//                    }
//
//                    val monthlyIncome = monthlyTransactions
//                        .filter { it.mode == "Income" }
//                        .sumOf { it.amount }
//
//                    val monthlyExpenses = monthlyTransactions
//                        .filter { it.mode == "Expense" }
//                        .sumOf { it.amount }
//
//                    val newState = ProfileScreenState(
//                        userName = profileInfo.userName,
//                        profileImageUri = profileInfo.profileImageUri,
//                        bannerImageUri = profileInfo.bannerImageUri,
//                        profileBackgroundColor = profileInfo.profileBackgroundColor,
//                        totalNetWorth = totalNetWorth,
//                        monthlyIncome = monthlyIncome,
//                        monthlyExpenses = monthlyExpenses,
//                        totalTransactions = transactions.size, // Fixed: Now gets actual transaction count
//                        upcomingTransactions = transactions.count {
//                            it.transactionType == TransactionType.UPCOMING && !it.isPaid
//                        },
//                        overdueTransactions = transactions.count { transaction ->
//                            transaction.transactionType == TransactionType.UPCOMING &&
//                                    transaction.nextDueDate?.let { dueDate ->
//                                        dueDate < System.currentTimeMillis()
//                                    } ?: false
//                        },
//                        // Also initialize edit state with current values
//                        editState = EditProfileState(
//                            editedUserName = profileInfo.userName,
//                            editedProfileImageUri = profileInfo.profileImageUri,
//                            editedBannerImageUri = profileInfo.bannerImageUri,
//                            editedProfileBackgroundColor = profileInfo.profileBackgroundColor
//                        ),
//                        isLoading = false
//                    )
//
//                    _state.value = newState
//                }
//            } catch (e: Exception) {
//                // Handle errors
//                _state.update { it.copy(isLoading = false) }
//                Log.e("ProfileViewModel", "Error loading profile data: ${e.message}")
//                e.printStackTrace()
//            }
//        }
//    }
//
//    fun handleEvent(event: ProfileScreenEvent) {
//        when (event) {
//            // Profile data updates
//            is ProfileScreenEvent.UpdateUserName -> {
//                saveUserName(event.name)
//            }
//            is ProfileScreenEvent.UpdateProfileImage -> {
//                saveProfileImage(event.imageUri)
//            }
//            is ProfileScreenEvent.UpdateBannerImage -> {
//                saveBannerImage(event.imageUri)
//            }
//            is ProfileScreenEvent.UpdateProfileBackgroundColor -> {
//                saveProfileBackgroundColor(event.color)
//            }
//
//            // UI state events
//            is ProfileScreenEvent.ToggleEditSheet -> {
//                _state.update {
//                    it.copy(isEditSheetOpen = !it.isEditSheetOpen)
//                }
//            }
//            is ProfileScreenEvent.DismissEditSheet -> {
//                _state.update {
//                    it.copy(isEditSheetOpen = false)
//                }
//            }
//            is ProfileScreenEvent.SaveProfileChanges -> {
//                saveProfileChanges()
//            }
//            is ProfileScreenEvent.RefreshProfileData -> {
//                loadProfileData()
//            }
//
//            // Permission events
//            is ProfileScreenEvent.RequestStoragePermission -> {
//                // This would be handled in UI layer
//            }
//            is ProfileScreenEvent.UpdateStoragePermission -> {
//                _state.update {
//                    it.copy(hasStoragePermission = event.granted)
//                }
//            }
//
//            // Edit state events
//            is ProfileScreenEvent.UpdateEditUserName -> {
//                updateEditState {
//                    it.copy(
//                        editedUserName = event.name,
//                        hasChanges = checkForChanges(
//                            it.copy(editedUserName = event.name)
//                        )
//                    )
//                }
//            }
//            is ProfileScreenEvent.UpdateEditProfileImage -> {
//                updateEditState {
//                    it.copy(
//                        editedProfileImageUri = event.imageUri,
//                        hasChanges = checkForChanges(
//                            it.copy(editedProfileImageUri = event.imageUri)
//                        )
//                    )
//                }
//            }
//            is ProfileScreenEvent.UpdateEditBannerImage -> {
//                updateEditState {
//                    it.copy(
//                        editedBannerImageUri = event.imageUri,
//                        hasChanges = checkForChanges(
//                            it.copy(editedBannerImageUri = event.imageUri)
//                        )
//                    )
//                }
//            }
//            is ProfileScreenEvent.UpdateEditProfileBackgroundColor -> {
//                updateEditState {
//                    it.copy(
//                        editedProfileBackgroundColor = event.color,
//                        hasChanges = checkForChanges(
//                            it.copy(editedProfileBackgroundColor = event.color)
//                        )
//                    )
//                }
//            }
//        }
//    }
//
//    private fun updateEditState(update: (EditProfileState) -> EditProfileState) {
//        _state.update { it.copy(editState = update(it.editState)) }
//    }
//
//    private fun checkForChanges(editState: EditProfileState): Boolean {
//        val currentState = _state.value
//        return editState.editedUserName != currentState.userName ||
//                editState.editedProfileImageUri != currentState.profileImageUri ||
//                editState.editedBannerImageUri != currentState.bannerImageUri ||
//                editState.editedProfileBackgroundColor != currentState.profileBackgroundColor
//    }
//
//    private fun saveProfileChanges() {
//        val editState = _state.value.editState
//
//        if (editState.hasChanges) {
//            viewModelScope.launch {
//                try {
//                    // Save each changed value
//                    if (editState.editedUserName != _state.value.userName) {
//                        profileRepository.saveUserName(editState.editedUserName)
//                    }
//
//                    if (editState.editedProfileImageUri != _state.value.profileImageUri) {
//                        val validatedUri = profileRepository.validateAndSaveImageUri(editState.editedProfileImageUri)
//                        profileRepository.saveProfileImageUri(validatedUri)
//                    }
//
//                    if (editState.editedBannerImageUri != _state.value.bannerImageUri) {
//                        val validatedUri = profileRepository.validateAndSaveImageUri(editState.editedBannerImageUri)
//                        profileRepository.saveBannerImageUri(validatedUri)
//                    }
//
//                    if (editState.editedProfileBackgroundColor != _state.value.profileBackgroundColor) {
//                        profileRepository.saveProfileBackgroundColor(editState.editedProfileBackgroundColor)
//                    }
//
//                    // Close edit sheet and refresh data
//                    _state.update { it.copy(isEditSheetOpen = false) }
//                    loadProfileData()
//
//                } catch (e: Exception) {
//                    Log.e("ProfileViewModel", "Error updating profile: ${e.message}")
//                }
//            }
//        } else {
//            // Just close the sheet if no changes
//            _state.update { it.copy(isEditSheetOpen = false) }
//        }
//    }
//
//    private fun saveUserName(name: String) {
//        viewModelScope.launch {
//            try {
//                profileRepository.saveUserName(name)
//                loadProfileData()
//            } catch (e: Exception) {
//                Log.e("ProfileViewModel", "Error updating username: ${e.message}")
//            }
//        }
//    }
//
//    private fun saveProfileImage(imageUri: Uri?) {
//        viewModelScope.launch {
//            try {
//                val validatedUri = profileRepository.validateAndSaveImageUri(imageUri)
//                profileRepository.saveProfileImageUri(validatedUri)
//                loadProfileData()
//            } catch (e: Exception) {
//                Log.e("ProfileViewModel", "Error updating profile image: ${e.message}")
//            }
//        }
//    }
//
//    private fun saveBannerImage(imageUri: Uri?) {
//        viewModelScope.launch {
//            try {
//                val validatedUri = profileRepository.validateAndSaveImageUri(imageUri)
//                profileRepository.saveBannerImageUri(validatedUri)
//                loadProfileData()
//            } catch (e: Exception) {
//                Log.e("ProfileViewModel", "Error updating banner image: ${e.message}")
//            }
//        }
//    }
//
//    private fun saveProfileBackgroundColor(color: Color) {
//        viewModelScope.launch {
//            try {
//                profileRepository.saveProfileBackgroundColor(color)
//                loadProfileData()
//            } catch (e: Exception) {
//                Log.e("ProfileViewModel", "Error updating profile background color: ${e.message}")
//            }
//        }
//    }
//}