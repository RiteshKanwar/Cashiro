package com.ritesh.cashiro.presentation.ui.features.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log
import androidx.compose.ui.graphics.toArgb
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.ritesh.cashiro.data.currency.model.Currency
import com.ritesh.cashiro.data.currency.repository.CurrencyRepository
import com.ritesh.cashiro.data.local.entity.AccountEntity
import com.ritesh.cashiro.data.local.entity.CategoryEntity
import com.ritesh.cashiro.data.local.entity.SubCategoryEntity
import com.ritesh.cashiro.domain.repository.AccountRepository
import com.ritesh.cashiro.domain.repository.CategoryRepository
import com.ritesh.cashiro.domain.repository.ProfileRepository
import com.ritesh.cashiro.domain.repository.SubCategoryRepository
import kotlinx.coroutines.flow.first

@HiltViewModel
class OnBoardingViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val accountRepository: AccountRepository,
    private val currencyRepository: CurrencyRepository,
    private val categoryRepository: CategoryRepository, // Added
    private val subCategoryRepository: SubCategoryRepository, // Added
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private val _state = MutableStateFlow(OnBoardingState())
    val state: StateFlow<OnBoardingState> = _state.asStateFlow()

    companion object {
        private val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")

        // Default Categories Data
        private val DEFAULT_CATEGORIES = listOf(
            CategoryEntity(
                id = 1,
                name = "Balance Correction",
                categoryIconId = 107,
                boxColor = -14145456,
                position = 0,
                textColor = -3484683
            ),
            CategoryEntity(
                id = 4,
                name = "Food",
                categoryIconId = 38,
                boxColor = -8264,
                position = 1,
                textColor = -3484683
            ),
            CategoryEntity(
                id = 5,
                name = "Drinks",
                categoryIconId = 4,
                boxColor = -9016065,
                position = 2,
                textColor = -3484683
            ),
            CategoryEntity(
                id = 6,
                name = "Shopping",
                categoryIconId = 188,
                boxColor = -13420982,
                position = 3,
                textColor = -3484683
            ),
            CategoryEntity(
                id = 7,
                name = "Groceries",
                categoryIconId = 131,
                boxColor = -12877,
                position = 4,
                textColor = -3484683
            ),
            CategoryEntity(
                id = 8,
                name = "Entertainment",
                categoryIconId = 70,
                boxColor = -7920074,
                position = 5,
                textColor = -3484683
            )
        )

        // Default SubCategories Data
        private val DEFAULT_SUBCATEGORIES = listOf(
            SubCategoryEntity(id = 3, name = "Dining", categoryId = 4, subcategoryIconId = 22, boxColor = -10523265, position = 0),
            SubCategoryEntity(id = 4, name = "Take Out", categoryId = 4, subcategoryIconId = 41, boxColor = -5883582, position = 1),
            SubCategoryEntity(id = 5, name = "FastFood", categoryId = 4, subcategoryIconId = 26, boxColor = -10360, position = 2),
            SubCategoryEntity(id = 6, name = "Snacks", categoryId = 4, subcategoryIconId = 64, boxColor = -8742, position = 3),
            SubCategoryEntity(id = 7, name = "Sweets", categoryId = 4, subcategoryIconId = 89, boxColor = -9208833, position = 4),
            SubCategoryEntity(id = 8, name = "Swiggy", categoryId = 4, subcategoryIconId = 126, boxColor = -14161, position = 5),
            SubCategoryEntity(id = 9, name = "Zomato", categoryId = 4, subcategoryIconId = 128, boxColor = -31103, position = 6),
            SubCategoryEntity(id = 10, name = "Coffee", categoryId = 5, subcategoryIconId = 8, boxColor = -11581, position = 1),
            SubCategoryEntity(id = 11, name = "Tea", categoryId = 5, subcategoryIconId = 10, boxColor = -10515615, position = 0),
            SubCategoryEntity(id = 12, name = "Beverages", categoryId = 5, subcategoryIconId = 12, boxColor = -8820536, position = 2),
            SubCategoryEntity(id = 13, name = "Liquor", categoryId = 5, subcategoryIconId = 1, boxColor = -9747400, position = 3),
            SubCategoryEntity(id = 14, name = "Tiffin", categoryId = 4, subcategoryIconId = 19, boxColor = -16957, position = 4),
            SubCategoryEntity(id = 15, name = "Clothes", categoryId = 6, subcategoryIconId = 182, boxColor = -8017409, position = 0),
            SubCategoryEntity(id = 16, name = "Footwear", categoryId = 6, subcategoryIconId = 174, boxColor = -11597, position = 1),
            SubCategoryEntity(id = 17, name = "Books", categoryId = 6, subcategoryIconId = 248, boxColor = -9888717, position = 2),
            SubCategoryEntity(id = 18, name = "Stationary", categoryId = 6, subcategoryIconId = 267, boxColor = -13672, position = 3),
            SubCategoryEntity(id = 19, name = "Glasses", categoryId = 6, subcategoryIconId = 171, boxColor = -5907201, position = 4),
            SubCategoryEntity(id = 20, name = "Vegetables", categoryId = 7, subcategoryIconId = 95, boxColor = -5308454, position = 5),
            SubCategoryEntity(id = 21, name = "Fruits", categoryId = 7, subcategoryIconId = 48, boxColor = -6842881, position = 1),
            SubCategoryEntity(id = 22, name = "Meat", categoryId = 7, subcategoryIconId = 136, boxColor = -10538431, position = 2),
            SubCategoryEntity(id = 23, name = "Eggs", categoryId = 7, subcategoryIconId = 137, boxColor = -11072, position = 3),
            SubCategoryEntity(id = 24, name = "Bakery", categoryId = 7, subcategoryIconId = 132, boxColor = -8237253, position = 4),
            SubCategoryEntity(id = 25, name = "Dairy", categoryId = 7, subcategoryIconId = 129, boxColor = -11174145, position = 5),
            SubCategoryEntity(id = 26, name = "Staples", categoryId = 7, subcategoryIconId = 93, boxColor = -15705, position = 6)
        )
    }

    init {
        loadAvailableCurrencies()
        checkPermissions()
        viewModelScope.launch {
            injectDefaultCategoriesIfNeeded()
        }
    }

    fun handleEvent(event: OnBoardingEvent) {
        when (event) {
            // Navigation events
            is OnBoardingEvent.NextStep -> {
                moveToNextStep()
            }
            is OnBoardingEvent.PreviousStep -> {
                moveToPreviousStep()
            }
            is OnBoardingEvent.GoToStep -> {
                _state.update { it.copy(currentStep = event.step) }
            }

            // Permission events
            is OnBoardingEvent.RequestNotificationPermission -> {
                _state.update { it.copy(isRequestingPermission = true) }
            }
            is OnBoardingEvent.RequestStoragePermission -> {
                _state.update { it.copy(isRequestingPermission = true) }
            }
            is OnBoardingEvent.UpdateNotificationPermission -> {
                _state.update {
                    it.copy(
                        hasNotificationPermission = event.granted,
                        isRequestingPermission = false
                    )
                }
                if (event.granted) {
                    moveToNextStep()
                }
            }
            is OnBoardingEvent.UpdateStoragePermission -> {
                _state.update {
                    it.copy(
                        hasStoragePermission = event.granted,
                        isRequestingPermission = false
                    )
                }
                if (event.granted) {
                    moveToNextStep()
                }
            }

            // Profile events
            is OnBoardingEvent.UpdateUserName -> {
                _state.update { it.copy(userName = event.name) }
            }
            is OnBoardingEvent.UpdateProfileImage -> {
                _state.update { it.copy(profileImageUri = event.uri) }
            }
            is OnBoardingEvent.UpdateProfileBackgroundColor -> {
                _state.update { it.copy(profileBackgroundColor = event.color) }
            }

            // Account events
            is OnBoardingEvent.UpdateAccountName -> {
                _state.update { it.copy(accountName = event.name) }
            }
            is OnBoardingEvent.UpdateAccountBalance -> {
                _state.update { it.copy(accountBalance = event.balance) }
            }
            is OnBoardingEvent.UpdateAccountColor1 -> {
                _state.update { it.copy(accountColor1 = event.color) }
            }
            is OnBoardingEvent.UpdateAccountColor2 -> {
                _state.update { it.copy(accountColor2 = event.color) }
            }
            is OnBoardingEvent.UpdateSelectedCurrency -> {
                _state.update { it.copy(selectedCurrency = event.currency) }
            }

            // Completion events
            OnBoardingEvent.CompleteOnBoarding -> {
                completeOnBoarding()
            }
            is OnBoardingEvent.SkipOnBoarding -> {
                skipOnBoarding()
            }

            // Error handling
            is OnBoardingEvent.ShowError -> {
                _state.update { it.copy(error = event.message) }
            }
            is OnBoardingEvent.ClearError -> {
                _state.update { it.copy(error = null) }
            }
        }
    }

    private fun loadAvailableCurrencies() {
        viewModelScope.launch {
            currencyRepository.getAllCurrencies().collect { result ->
                result.fold(
                    onSuccess = { currenciesMap ->
                        val currencies = currenciesMap.map { (code, name) ->
                            Currency(code, name)
                        }
                        _state.update { it.copy(availableCurrencies = currencies) }
                    },
                    onFailure = { throwable ->
                        Log.e("OnBoardingViewModel", "Failed to load currencies: ${throwable.message}")
                    }
                )
            }
        }
    }

    private suspend fun injectDefaultCategoriesIfNeeded() {
        try {
            // Check if categories already exist to avoid duplicates
            val existingCategories = categoryRepository.getAllCategories()
            if (existingCategories.isEmpty()) {
                Log.d("OnBoardingViewModel", "No existing categories found, injecting default categories...")

                // Inject default categories
                injectDefaultCategories()
                injectDefaultSubCategories()

                Log.d("OnBoardingViewModel", "Default categories injected successfully")
            } else {
                Log.d("OnBoardingViewModel", "Categories already exist (${existingCategories.size} found), skipping injection")
            }
        } catch (e: Exception) {
            Log.e("OnBoardingViewModel", "Error checking/injecting categories: ${e.message}", e)
            // Don't throw here, let onboarding continue even if category injection fails
        }
    }

    private fun checkPermissions() {
        // This will be called from the UI layer to update permission states
        // We can't check permissions directly from ViewModel
    }

    private fun moveToNextStep() {
        val currentStep = _state.value.currentStep
        val nextStep = when (currentStep) {
            OnBoardingStep.WELCOME -> OnBoardingStep.NOTIFICATION_PERMISSION
            OnBoardingStep.NOTIFICATION_PERMISSION -> OnBoardingStep.STORAGE_PERMISSION
            OnBoardingStep.STORAGE_PERMISSION -> OnBoardingStep.USER_PROFILE
            OnBoardingStep.USER_PROFILE -> {
                if (validateProfileData()) {
                    OnBoardingStep.FIRST_ACCOUNT
                } else {
                    currentStep // Stay on current step if validation fails
                }
            }
            OnBoardingStep.FIRST_ACCOUNT -> {
                if (validateAccountData()) {
                    OnBoardingStep.COMPLETION
                } else {
                    currentStep // Stay on current step if validation fails
                }
            }
            OnBoardingStep.COMPLETION -> currentStep // Can't go beyond completion
        }

        if (nextStep != currentStep) {
            _state.update { it.copy(currentStep = nextStep) }
        }
    }

    private fun moveToPreviousStep() {
        val currentStep = _state.value.currentStep
        val previousStep = when (currentStep) {
            OnBoardingStep.WELCOME -> currentStep // Can't go before welcome
            OnBoardingStep.NOTIFICATION_PERMISSION -> OnBoardingStep.WELCOME
            OnBoardingStep.STORAGE_PERMISSION -> OnBoardingStep.NOTIFICATION_PERMISSION
            OnBoardingStep.USER_PROFILE -> OnBoardingStep.STORAGE_PERMISSION
            OnBoardingStep.FIRST_ACCOUNT -> OnBoardingStep.USER_PROFILE
            OnBoardingStep.COMPLETION -> OnBoardingStep.FIRST_ACCOUNT
        }

        _state.update { it.copy(currentStep = previousStep) }
    }

    private fun validateProfileData(): Boolean {
        val state = _state.value
        return state.userName.isNotBlank()
    }

    private fun validateAccountData(): Boolean {
        val state = _state.value
        return state.accountName.isNotBlank() &&
                state.accountBalance.isNotBlank() &&
                state.accountColor1 != null &&
                state.accountColor2 != null
    }

    // Updated completeOnBoarding method with data injection
//    private fun completeOnBoarding() {
//        viewModelScope.launch {
//            try {
//                _state.update { it.copy(isLoading = true) }
//
//                // Save profile data
//                saveProfileData()
//
//                // **INJECT DEFAULT CATEGORIES AND SUBCATEGORIES**
//                injectDefaultCategories()
//                injectDefaultSubCategories()
//
//                // Mark onboarding as completed
//                markOnBoardingComplete()
//
//                _state.update {
//                    it.copy(
//                        isLoading = false,
//                        isOnBoardingComplete = true,
//                        currentStep = OnBoardingStep.COMPLETION
//                    )
//                }
//
//                Log.d("OnBoardingViewModel", "OnBoarding completed successfully with default data")
//
//            } catch (e: Exception) {
//                Log.e("OnBoardingViewModel", "Error completing onboarding: ${e.message}", e)
//                _state.update {
//                    it.copy(
//                        isLoading = false,
//                        error = "Failed to complete setup: ${e.message}"
//                    )
//                }
//            }
//        }
//    }
    private fun completeOnBoarding() {
        viewModelScope.launch {
            try {
                _state.update { it.copy(isLoading = true) }

                // Save profile data
                saveProfileData()

                // CREATE FIRST ACCOUNT WITH SELECTED CURRENCY (FIX FOR ISSUE 2)
                createFirstAccount()

                // Categories are already injected in init, no need to inject again

                // Mark onboarding as completed
                markOnBoardingComplete()

                _state.update {
                    it.copy(
                        isLoading = false,
                        isOnBoardingComplete = true,
                        currentStep = OnBoardingStep.COMPLETION
                    )
                }

                Log.d("OnBoardingViewModel", "OnBoarding completed successfully")

            } catch (e: Exception) {
                Log.e("OnBoardingViewModel", "Error completing onboarding: ${e.message}", e)
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to complete setup: ${e.message}"
                    )
                }
            }
        }
    }

    private suspend fun saveProfileData() {
        val state = _state.value

        if (state.userName.isNotBlank()) {
            profileRepository.saveUserName(state.userName)
        }

        state.profileImageUri?.let { uri ->
            profileRepository.saveProfileImageUri(uri)
        }

        profileRepository.saveProfileBackgroundColor(state.profileBackgroundColor)
    }

//    private suspend fun createFirstAccount() {
//        val state = _state.value
//
//        if (validateAccountData()) {
//            val account = AccountEntity(
//                accountName = state.accountName,
//                cardColor1 = state.accountColor1!!.toArgb(),
//                cardColor2 = state.accountColor2!!.toArgb(),
//                balance = state.accountBalance.toDoubleOrNull() ?: 0.0,
//                isMainAccount = true, // First account is always main
//                position = 0,
//                currencyCode = state.selectedCurrency
//            )
//
//            accountRepository.addAccount(account)
//            Log.d("OnBoardingViewModel", "First account created: ${state.accountName}")
//        }
//    }
private suspend fun createFirstAccount() {
    val state = _state.value

    // Validate that we have account data from onboarding
    if (state.accountName.isNotBlank() &&
        state.accountBalance.isNotBlank() &&
        state.accountColor1 != null &&
        state.accountColor2 != null) {

        try {
            val account = AccountEntity(
                accountName = state.accountName,
                cardColor1 = state.accountColor1.toArgb(),
                cardColor2 = state.accountColor2.toArgb(),
                balance = state.accountBalance.toDoubleOrNull() ?: 0.0,
                isMainAccount = true, // First account is always main
                position = 0,
                currencyCode = state.selectedCurrency // USE SELECTED CURRENCY FROM ONBOARDING
            )

            val accountId = accountRepository.addAccount(account)
            Log.d("OnBoardingViewModel", "First account created: ${state.accountName} with currency: ${state.selectedCurrency}")

        } catch (e: Exception) {
            Log.e("OnBoardingViewModel", "Error creating first account: ${e.message}", e)
            throw e
        }
    } else {
        Log.w("OnBoardingViewModel", "Account data incomplete, skipping account creation")
        // Still allow onboarding to complete
    }
}

    // NEW: Inject default categories
    private suspend fun injectDefaultCategories() {
        try {
            Log.d("OnBoardingViewModel", "Injecting default categories...")

            // Check if categories already exist to avoid duplicates
            val existingCategories = categoryRepository.getAllCategories()
            if (existingCategories.isEmpty()) {
                // Create categories without IDs (let database auto-generate)
                val categoriesToInsert = DEFAULT_CATEGORIES.map {
                    it.copy(id = 0) // Reset ID to let database auto-generate
                }
                categoryRepository.addCategories(categoriesToInsert)
                Log.d("OnBoardingViewModel", "Successfully injected ${categoriesToInsert.size} default categories")
            } else {
                Log.d("OnBoardingViewModel", "Categories already exist, skipping injection")
            }
        } catch (e: Exception) {
            Log.e("OnBoardingViewModel", "Error injecting default categories: ${e.message}", e)
            throw e
        }
    }

    // NEW: Inject default subcategories
    private suspend fun injectDefaultSubCategories() {
        try {
            Log.d("OnBoardingViewModel", "Injecting default subcategories...")

            // Get the actual category IDs from database after insertion
            val insertedCategories = categoryRepository.getAllCategories()
            val categoryMapping = mutableMapOf<String, Int>()

            // Create mapping from category name to actual database ID
            insertedCategories.forEach { category ->
                categoryMapping[category.name] = category.id
            }

            // Check if subcategories already exist
            val existingSubCategories = subCategoryRepository.getAllSubCategories()
            if (existingSubCategories.isEmpty()) {
                // Map subcategories to use the actual category IDs
                val subCategoriesToInsert = DEFAULT_SUBCATEGORIES.mapNotNull { defaultSubCategory ->
                    val categoryName = when (defaultSubCategory.categoryId) {
                        4 -> "Food"
                        5 -> "Drinks"
                        6 -> "Shopping"
                        7 -> "Groceries"
                        8 -> "Entertainment"
                        else -> null
                    }

                    categoryName?.let { name ->
                        categoryMapping[name]?.let { actualCategoryId ->
                            defaultSubCategory.copy(
                                id = 0, // Reset ID to let database auto-generate
                                categoryId = actualCategoryId
                            )
                        }
                    }
                }

                if (subCategoriesToInsert.isNotEmpty()) {
                    subCategoryRepository.insertSubCategories(subCategoriesToInsert)
                    Log.d("OnBoardingViewModel", "Successfully injected ${subCategoriesToInsert.size} default subcategories")
                }
            } else {
                Log.d("OnBoardingViewModel", "Subcategories already exist, skipping injection")
            }
        } catch (e: Exception) {
            Log.e("OnBoardingViewModel", "Error injecting default subcategories: ${e.message}", e)
            throw e
        }
    }

    private suspend fun markOnBoardingComplete() {
        dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED_KEY] = true
        }
        Log.d("OnBoardingViewModel", "OnBoarding completion saved to DataStore")
    }

    private fun skipOnBoarding() {
        viewModelScope.launch {
            markOnBoardingComplete()
            _state.update { it.copy(isOnBoardingComplete = true) }
        }
    }

    // Helper function to check if onboarding is completed
    suspend fun isOnBoardingCompleted(): Boolean {
        return try {
            val preferences = dataStore.data.first()
            val isCompleted = preferences[ONBOARDING_COMPLETED_KEY] ?: false
            Log.d("OnBoardingViewModel", "Checking onboarding status: $isCompleted")
            isCompleted
        } catch (e: Exception) {
            Log.e("OnBoardingViewModel", "Error checking onboarding status: ${e.message}")
            false
        }
    }

    // Function to reset onboarding (for testing)
    suspend fun resetOnBoarding() {
        dataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED_KEY] = false
        }
        _state.update { OnBoardingState() }
    }
}

//@HiltViewModel
//class OnBoardingViewModel @Inject constructor(
//    private val profileRepository: ProfileRepository,
//    private val accountRepository: AccountRepository,
//    private val currencyRepository: CurrencyRepository,
//    private val dataStore: DataStore<Preferences>
//) : ViewModel() {
//
//    private val _state = MutableStateFlow(OnBoardingState())
//    val state: StateFlow<OnBoardingState> = _state.asStateFlow()
//
//    companion object {
//        private val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
//    }
//
//    init {
//        loadAvailableCurrencies()
//        checkPermissions()
//    }
//
//    fun handleEvent(event: OnBoardingEvent) {
//        when (event) {
//            // Navigation events
//            is OnBoardingEvent.NextStep -> {
//                moveToNextStep()
//            }
//            is OnBoardingEvent.PreviousStep -> {
//                moveToPreviousStep()
//            }
//            is OnBoardingEvent.GoToStep -> {
//                _state.update { it.copy(currentStep = event.step) }
//            }
//
//            // Permission events
//            is OnBoardingEvent.RequestNotificationPermission -> {
//                _state.update { it.copy(isRequestingPermission = true) }
//            }
//            is OnBoardingEvent.RequestStoragePermission -> {
//                _state.update { it.copy(isRequestingPermission = true) }
//            }
//            is OnBoardingEvent.UpdateNotificationPermission -> {
//                _state.update {
//                    it.copy(
//                        hasNotificationPermission = event.granted,
//                        isRequestingPermission = false
//                    )
//                }
//                if (event.granted) {
//                    moveToNextStep()
//                }
//            }
//            is OnBoardingEvent.UpdateStoragePermission -> {
//                _state.update {
//                    it.copy(
//                        hasStoragePermission = event.granted,
//                        isRequestingPermission = false
//                    )
//                }
//                if (event.granted) {
//                    moveToNextStep()
//                }
//            }
//
//            // Profile events
//            is OnBoardingEvent.UpdateUserName -> {
//                _state.update { it.copy(userName = event.name) }
//            }
//            is OnBoardingEvent.UpdateProfileImage -> {
//                _state.update { it.copy(profileImageUri = event.uri) }
//            }
//            is OnBoardingEvent.UpdateProfileBackgroundColor -> {
//                _state.update { it.copy(profileBackgroundColor = event.color) }
//            }
//
//            // Account events
//            is OnBoardingEvent.UpdateAccountName -> {
//                _state.update { it.copy(accountName = event.name) }
//            }
//            is OnBoardingEvent.UpdateAccountBalance -> {
//                _state.update { it.copy(accountBalance = event.balance) }
//            }
//            is OnBoardingEvent.UpdateAccountColor1 -> {
//                _state.update { it.copy(accountColor1 = event.color) }
//            }
//            is OnBoardingEvent.UpdateAccountColor2 -> {
//                _state.update { it.copy(accountColor2 = event.color) }
//            }
//            is OnBoardingEvent.UpdateSelectedCurrency -> {
//                _state.update { it.copy(selectedCurrency = event.currency) }
//            }
//
//            // Completion events - THIS IS THE FIX!
//            OnBoardingEvent.CompleteOnBoarding -> {
//                completeOnBoarding() // Call the actual completion method
//            }
//            is OnBoardingEvent.SkipOnBoarding -> {
//                skipOnBoarding()
//            }
//
//            // Error handling
//            is OnBoardingEvent.ShowError -> {
//                _state.update { it.copy(error = event.message) }
//            }
//            is OnBoardingEvent.ClearError -> {
//                _state.update { it.copy(error = null) }
//            }
//        }
//    }
//
//    private fun loadAvailableCurrencies() {
//        viewModelScope.launch {
//            currencyRepository.getAllCurrencies().collect { result ->
//                result.fold(
//                    onSuccess = { currenciesMap ->
//                        val currencies = currenciesMap.map { (code, name) ->
//                            Currency(code, name)
//                        }
//                        _state.update { it.copy(availableCurrencies = currencies) }
//                    },
//                    onFailure = { throwable ->
//                        Log.e("OnBoardingViewModel", "Failed to load currencies: ${throwable.message}")
//                    }
//                )
//            }
//        }
//    }
//
//    private fun checkPermissions() {
//        // This will be called from the UI layer to update permission states
//        // We can't check permissions directly from ViewModel
//    }
//
//    private fun moveToNextStep() {
//        val currentStep = _state.value.currentStep
//        val nextStep = when (currentStep) {
//            OnBoardingStep.WELCOME -> OnBoardingStep.NOTIFICATION_PERMISSION
//            OnBoardingStep.NOTIFICATION_PERMISSION -> OnBoardingStep.STORAGE_PERMISSION
//            OnBoardingStep.STORAGE_PERMISSION -> OnBoardingStep.USER_PROFILE
//            OnBoardingStep.USER_PROFILE -> {
//                if (validateProfileData()) {
//                    OnBoardingStep.FIRST_ACCOUNT
//                } else {
//                    currentStep // Stay on current step if validation fails
//                }
//            }
//            OnBoardingStep.FIRST_ACCOUNT -> {
//                if (validateAccountData()) {
//                    OnBoardingStep.COMPLETION
//                } else {
//                    currentStep // Stay on current step if validation fails
//                }
//            }
//            OnBoardingStep.COMPLETION -> currentStep // Can't go beyond completion
//        }
//
//        if (nextStep != currentStep) {
//            _state.update { it.copy(currentStep = nextStep) }
//        }
//    }
//
//    private fun moveToPreviousStep() {
//        val currentStep = _state.value.currentStep
//        val previousStep = when (currentStep) {
//            OnBoardingStep.WELCOME -> currentStep // Can't go before welcome
//            OnBoardingStep.NOTIFICATION_PERMISSION -> OnBoardingStep.WELCOME
//            OnBoardingStep.STORAGE_PERMISSION -> OnBoardingStep.NOTIFICATION_PERMISSION
//            OnBoardingStep.USER_PROFILE -> OnBoardingStep.STORAGE_PERMISSION
//            OnBoardingStep.FIRST_ACCOUNT -> OnBoardingStep.USER_PROFILE
//            OnBoardingStep.COMPLETION -> OnBoardingStep.FIRST_ACCOUNT
//        }
//
//        _state.update { it.copy(currentStep = previousStep) }
//    }
//
//    private fun validateProfileData(): Boolean {
//        val state = _state.value
//        return state.userName.isNotBlank()
//    }
//
//    private fun validateAccountData(): Boolean {
//        val state = _state.value
//        return state.accountName.isNotBlank() &&
//                state.accountBalance.isNotBlank() &&
//                state.accountColor1 != null &&
//                state.accountColor2 != null
//    }
//
//    // THIS IS THE CRITICAL METHOD THAT WAS NOT BEING CALLED
//    private fun completeOnBoarding() {
//        viewModelScope.launch {
//            try {
//                _state.update { it.copy(isLoading = true) }
//
//                // Save profile data
//                saveProfileData()
//
//                // Create first account
//                createFirstAccount()
//
//                // Mark onboarding as completed - THIS SAVES TO DATASTORE
//                markOnBoardingComplete()
//
//                _state.update {
//                    it.copy(
//                        isLoading = false,
//                        isOnBoardingComplete = true,
//                        currentStep = OnBoardingStep.COMPLETION
//                    )
//                }
//
//                Log.d("OnBoardingViewModel", "OnBoarding completed successfully")
//
//            } catch (e: Exception) {
//                Log.e("OnBoardingViewModel", "Error completing onboarding: ${e.message}", e)
//                _state.update {
//                    it.copy(
//                        isLoading = false,
//                        error = "Failed to complete setup: ${e.message}"
//                    )
//                }
//            }
//        }
//    }
//
//    private suspend fun saveProfileData() {
//        val state = _state.value
//
//        if (state.userName.isNotBlank()) {
//            profileRepository.saveUserName(state.userName)
//        }
//
//        state.profileImageUri?.let { uri ->
//            profileRepository.saveProfileImageUri(uri)
//        }
//
//        profileRepository.saveProfileBackgroundColor(state.profileBackgroundColor)
//    }
//
//    private suspend fun createFirstAccount() {
//        val state = _state.value
//
//        if (validateAccountData()) {
//            val account = AccountEntity(
//                accountName = state.accountName,
//                cardColor1 = state.accountColor1!!.toArgb(),
//                cardColor2 = state.accountColor2!!.toArgb(),
//                balance = state.accountBalance.toDoubleOrNull() ?: 0.0,
//                isMainAccount = true, // First account is always main
//                position = 0,
//                currencyCode = state.selectedCurrency
//            )
//
//            accountRepository.addAccount(account)
//            Log.d("OnBoardingViewModel", "First account created: ${state.accountName}")
//        }
//    }
//
//    private suspend fun markOnBoardingComplete() {
//        dataStore.edit { preferences ->
//            preferences[ONBOARDING_COMPLETED_KEY] = true
//        }
//        Log.d("OnBoardingViewModel", "OnBoarding completion saved to DataStore")
//    }
//
//    private fun skipOnBoarding() {
//        viewModelScope.launch {
//            markOnBoardingComplete()
//            _state.update { it.copy(isOnBoardingComplete = true) }
//        }
//    }
//
//    // Helper function to check if onboarding is completed
//    suspend fun isOnBoardingCompleted(): Boolean {
//        return try {
//            val preferences = dataStore.data.first()
//            val isCompleted = preferences[ONBOARDING_COMPLETED_KEY] ?: false
//            Log.d("OnBoardingViewModel", "Checking onboarding status: $isCompleted")
//            isCompleted
//        } catch (e: Exception) {
//            Log.e("OnBoardingViewModel", "Error checking onboarding status: ${e.message}")
//            false
//        }
//    }
//
//    // Function to reset onboarding (for testing)
//    suspend fun resetOnBoarding() {
//        dataStore.edit { preferences ->
//            preferences[ONBOARDING_COMPLETED_KEY] = false
//        }
//        _state.update { OnBoardingState() }
//    }
//}
//@HiltViewModel
//class OnBoardingViewModel @Inject constructor(
//    private val profileRepository: ProfileRepository,
//    private val accountRepository: AccountRepository,
//    private val currencyRepository: CurrencyRepository,
//    private val dataStore: DataStore<Preferences>
//) : ViewModel() {
//
//    private val _state = MutableStateFlow(OnBoardingState())
//    val state: StateFlow<OnBoardingState> = _state.asStateFlow()
//
//    companion object {
//        private val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
//    }
//
//    init {
//        loadAvailableCurrencies()
//        checkPermissions()
//    }
//
//    fun handleEvent(event: OnBoardingEvent) {
//        when (event) {
//            // Navigation events
//            is OnBoardingEvent.NextStep -> {
//                moveToNextStep()
//            }
//            is OnBoardingEvent.PreviousStep -> {
//                moveToPreviousStep()
//            }
//            is OnBoardingEvent.GoToStep -> {
//                _state.update { it.copy(currentStep = event.step) }
//            }
//
//            // Permission events
//            is OnBoardingEvent.RequestNotificationPermission -> {
//                _state.update { it.copy(isRequestingPermission = true) }
//            }
//            is OnBoardingEvent.RequestStoragePermission -> {
//                _state.update { it.copy(isRequestingPermission = true) }
//            }
//            is OnBoardingEvent.UpdateNotificationPermission -> {
//                _state.update {
//                    it.copy(
//                        hasNotificationPermission = event.granted,
//                        isRequestingPermission = false
//                    )
//                }
//                if (event.granted) {
//                    moveToNextStep()
//                }
//            }
//            is OnBoardingEvent.UpdateStoragePermission -> {
//                _state.update {
//                    it.copy(
//                        hasStoragePermission = event.granted,
//                        isRequestingPermission = false
//                    )
//                }
//                if (event.granted) {
//                    moveToNextStep()
//                }
//            }
//
//            // Profile events
//            is OnBoardingEvent.UpdateUserName -> {
//                _state.update { it.copy(userName = event.name) }
//            }
//            is OnBoardingEvent.UpdateProfileImage -> {
//                _state.update { it.copy(profileImageUri = event.uri) }
//            }
//            is OnBoardingEvent.UpdateProfileBackgroundColor -> {
//                _state.update { it.copy(profileBackgroundColor = event.color) }
//            }
//
//            // Account events
//            is OnBoardingEvent.UpdateAccountName -> {
//                _state.update { it.copy(accountName = event.name) }
//            }
//            is OnBoardingEvent.UpdateAccountBalance -> {
//                _state.update { it.copy(accountBalance = event.balance) }
//            }
//            is OnBoardingEvent.UpdateAccountColor1 -> {
//                _state.update { it.copy(accountColor1 = event.color) }
//            }
//            is OnBoardingEvent.UpdateAccountColor2 -> {
//                _state.update { it.copy(accountColor2 = event.color) }
//            }
//            is OnBoardingEvent.UpdateSelectedCurrency -> {
//                _state.update { it.copy(selectedCurrency = event.currency) }
//            }
//
//            // Completion events
////            is OnBoardingEvent.CompleteOnBoarding -> {
////                completeOnBoarding()
////            }
////            OnBoardingEvent.CompleteOnBoarding -> {
////                _state.value = _state.value.copy(
////                    isOnBoardingComplete = true,
////                    isLoading = false
////                )
////            }
//            OnBoardingEvent.CompleteOnBoarding -> {
//                _state.value = _state.value.copy(
//                    currentStep = OnBoardingStep.COMPLETION,  // This was missing!
//                    isOnBoardingComplete = true,
//                    isLoading = false
//                )
//            }
//            is OnBoardingEvent.SkipOnBoarding -> {
//                skipOnBoarding()
//            }
//
//            // Error handling
//            is OnBoardingEvent.ShowError -> {
//                _state.update { it.copy(error = event.message) }
//            }
//            is OnBoardingEvent.ClearError -> {
//                _state.update { it.copy(error = null) }
//            }
//        }
//    }
//
//    private fun loadAvailableCurrencies() {
//        viewModelScope.launch {
//            currencyRepository.getAllCurrencies().collect { result ->
//                result.fold(
//                    onSuccess = { currenciesMap ->
//                        val currencies = currenciesMap.map { (code, name) ->
//                            Currency(code, name)
//                        }
//                        _state.update { it.copy(availableCurrencies = currencies) }
//                    },
//                    onFailure = { throwable ->
//                        Log.e("OnBoardingViewModel", "Failed to load currencies: ${throwable.message}")
//                    }
//                )
//            }
//        }
//    }
//
//    private fun checkPermissions() {
//        // This will be called from the UI layer to update permission states
//        // We can't check permissions directly from ViewModel
//    }
//
//    private fun moveToNextStep() {
//        val currentStep = _state.value.currentStep
//        val nextStep = when (currentStep) {
//            OnBoardingStep.WELCOME -> OnBoardingStep.NOTIFICATION_PERMISSION
//            OnBoardingStep.NOTIFICATION_PERMISSION -> OnBoardingStep.STORAGE_PERMISSION
//            OnBoardingStep.STORAGE_PERMISSION -> OnBoardingStep.USER_PROFILE
//            OnBoardingStep.USER_PROFILE -> {
//                if (validateProfileData()) {
//                    OnBoardingStep.FIRST_ACCOUNT
//                } else {
//                    currentStep // Stay on current step if validation fails
//                }
//            }
//            OnBoardingStep.FIRST_ACCOUNT -> {
//                if (validateAccountData()) {
//                    OnBoardingStep.COMPLETION
//                } else {
//                    currentStep // Stay on current step if validation fails
//                }
//            }
//            OnBoardingStep.COMPLETION -> currentStep // Can't go beyond completion
//        }
//
//        if (nextStep != currentStep) {
//            _state.update { it.copy(currentStep = nextStep) }
//        }
//    }
//
//    private fun moveToPreviousStep() {
//        val currentStep = _state.value.currentStep
//        val previousStep = when (currentStep) {
//            OnBoardingStep.WELCOME -> currentStep // Can't go before welcome
//            OnBoardingStep.NOTIFICATION_PERMISSION -> OnBoardingStep.WELCOME
//            OnBoardingStep.STORAGE_PERMISSION -> OnBoardingStep.NOTIFICATION_PERMISSION
//            OnBoardingStep.USER_PROFILE -> OnBoardingStep.STORAGE_PERMISSION
//            OnBoardingStep.FIRST_ACCOUNT -> OnBoardingStep.USER_PROFILE
//            OnBoardingStep.COMPLETION -> OnBoardingStep.FIRST_ACCOUNT
//        }
//
//        _state.update { it.copy(currentStep = previousStep) }
//    }
//
//    private fun validateProfileData(): Boolean {
//        val state = _state.value
//        return state.userName.isNotBlank()
//    }
//
//    private fun validateAccountData(): Boolean {
//        val state = _state.value
//        return state.accountName.isNotBlank() &&
//                state.accountBalance.isNotBlank() &&
//                state.accountColor1 != null &&
//                state.accountColor2 != null
//    }
//
//    private fun completeOnBoarding() {
//        viewModelScope.launch {
//            try {
//                _state.update { it.copy(isLoading = true) }
//
//                // Save profile data
//                saveProfileData()
//
//                // Create first account
//                createFirstAccount()
//
//                // Mark onboarding as completed
//                markOnBoardingComplete()
//
//                _state.update {
//                    it.copy(
//                        isLoading = false,
//                        isOnBoardingComplete = true,
//                        currentStep = OnBoardingStep.COMPLETION
//                    )
//                }
//
//                Log.d("OnBoardingViewModel", "OnBoarding completed successfully")
//
//            } catch (e: Exception) {
//                Log.e("OnBoardingViewModel", "Error completing onboarding: ${e.message}", e)
//                _state.update {
//                    it.copy(
//                        isLoading = false,
//                        error = "Failed to complete setup: ${e.message}"
//                    )
//                }
//            }
//        }
//    }
//
//    private suspend fun saveProfileData() {
//        val state = _state.value
//
//        if (state.userName.isNotBlank()) {
//            profileRepository.saveUserName(state.userName)
//        }
//
//        state.profileImageUri?.let { uri ->
//            profileRepository.saveProfileImageUri(uri)
//        }
//
//        profileRepository.saveProfileBackgroundColor(state.profileBackgroundColor)
//    }
//
//    private suspend fun createFirstAccount() {
//        val state = _state.value
//
//        if (validateAccountData()) {
//            val account = AccountEntity(
//                accountName = state.accountName,
//                cardColor1 = state.accountColor1!!.toArgb(),
//                cardColor2 = state.accountColor2!!.toArgb(),
//                balance = state.accountBalance.toDoubleOrNull() ?: 0.0,
//                isMainAccount = true, // First account is always main
//                position = 0,
//                currencyCode = state.selectedCurrency
//            )
//
//            accountRepository.addAccount(account)
//            Log.d("OnBoardingViewModel", "First account created: ${state.accountName}")
//        }
//    }
//
//    private suspend fun markOnBoardingComplete() {
//        dataStore.edit { preferences ->
//            preferences[ONBOARDING_COMPLETED_KEY] = true
//        }
//    }
//
//    private fun skipOnBoarding() {
//        viewModelScope.launch {
//            markOnBoardingComplete()
//            _state.update { it.copy(isOnBoardingComplete = true) }
//        }
//    }
//
//    // Helper function to check if onboarding is completed
//    suspend fun isOnBoardingCompleted(): Boolean {
//        return try {
//            val preferences = dataStore.data.first()
//            preferences[ONBOARDING_COMPLETED_KEY] ?: false
//        } catch (e: Exception) {
//            Log.e("OnBoardingViewModel", "Error checking onboarding status: ${e.message}")
//            false
//        }
//    }
//
//    // Function to reset onboarding (for testing)
//    suspend fun resetOnBoarding() {
//        dataStore.edit { preferences ->
//            preferences[ONBOARDING_COMPLETED_KEY] = false
//        }
//        _state.update { OnBoardingState() }
//    }
//}