package com.ritesh.cashiro.presentation.ui.features.categories

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ritesh.cashiro.data.local.entity.CategoryEntity
import com.ritesh.cashiro.data.local.entity.CategoryWithSubCategories
import com.ritesh.cashiro.data.local.entity.SubCategoryEntity
import com.ritesh.cashiro.domain.repository.AccountRepository
import com.ritesh.cashiro.domain.repository.CategoryRepository
import com.ritesh.cashiro.domain.repository.SubCategoryRepository
import com.ritesh.cashiro.domain.repository.TransactionRepository
import com.ritesh.cashiro.domain.utils.AccountEvent
import com.ritesh.cashiro.domain.utils.ActivityLogUtils
import com.ritesh.cashiro.domain.utils.AppEventBus
import com.ritesh.cashiro.domain.utils.CategoryEvent
import com.ritesh.cashiro.domain.utils.TransactionEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// SubCategoryViewModel.kt
@HiltViewModel
class SubCategoryViewModel @Inject constructor(
    private val subCategoryRepository: SubCategoryRepository,
    private val categoryRepository: CategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val activityLogUtils: ActivityLogUtils,
) : ViewModel(), SubCategoryEventHandler {

    // Main state
    private val _state = MutableStateFlow(SubCategoryState())
    val state: StateFlow<SubCategoryState> = _state.asStateFlow()

    // Individual state flows for backward compatibility
    val selectedIconId = state.map { it.selectedIconId }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    val customBoxColor = state.map { it.customBoxColor }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    val customTextColor = state.map { it.customTextColor }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    val subCategoryName = state.map { it.subCategoryName }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ""
    )

    val selectedCategoryId = state.map { it.selectedCategoryId }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    val subCategories = state.map { it.subCategories }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    val categoriesWithSubCategories = state.map { it.categoriesWithSubCategories }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    val pendingDeletion = state.map { it.pendingDeletion }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    val isMergeMode = state.map { it.isMergeMode }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )

    val isConvertToMainMode = state.map { it.isConvertToMainMode }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )

    val selectedTargetSubCategoryId = state.map { it.selectedTargetSubCategoryId }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    val availableSubCategoriesForMerge = state.map { it.availableSubCategoriesForMerge }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    val currentEditingSubCategoryId = state.map { it.currentEditingSubCategoryId }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = null
    )

    private var undoJob: Job? = null

    init {
        onEvent(SubCategoryEvent.FetchAllCategoriesWithSubCategories)

        // Listen for subcategory update events
        viewModelScope.launch {
            AppEventBus.events.collect { event ->
                when (event) {
                    is CategoryScreenEvent.SubCategoriesUpdated -> {
                        if (_state.value.selectedCategoryId == event.categoryId) {
                            onEvent(SubCategoryEvent.FetchSubCategoriesForCategory(event.categoryId))
                        }
                    }
                }
            }
        }
    }

    override fun onEvent(event: SubCategoryEvent) {
        when (event) {
            // Form input events
            is SubCategoryEvent.UpdateSubCategoryName -> {
                _state.update { it.copy(subCategoryName = event.name) }
            }

            is SubCategoryEvent.UpdateSelectedIconId -> {
                _state.update { it.copy(selectedIconId = event.iconId) }
            }

            is SubCategoryEvent.UpdateCustomBoxColor -> {
                _state.update { it.copy(customBoxColor = event.color) }
            }

            is SubCategoryEvent.UpdateCustomTextColor -> {
                _state.update { it.copy(customTextColor = event.color) }
            }

            is SubCategoryEvent.UpdateSelectedCategoryId -> {
                _state.update { it.copy(selectedCategoryId = event.categoryId) }
                event.categoryId?.let { categoryId ->
                    onEvent(SubCategoryEvent.FetchSubCategoriesForCategory(categoryId))
                }
            }

            // SubCategory management events
            is SubCategoryEvent.PrepareForNewSubCategory -> {
                _state.update {
                    it.resetForm().copy(
                        selectedCategoryId = event.categoryId,
                        isEditingSubCategory = false
                    )
                }
            }

            is SubCategoryEvent.PrepareForEditingSubCategory -> {
                prepareForEditingSubCategory(event.subCategory)
            }

            is SubCategoryEvent.AddSubCategory -> {
                saveSubCategory {}
            }

            is SubCategoryEvent.OnSubCategorySaved -> {
                saveSubCategory(event.onSuccess)
            }

            is SubCategoryEvent.UpdateSubCategory -> {
                updateSubCategory(event.subCategory) {}
            }

            is SubCategoryEvent.OnSubCategoryUpdated -> {
                updateSubCategory(event.subCategory, event.onSuccess)
            }

            is SubCategoryEvent.DeleteSubCategory -> {
                deleteSubCategory(event.subCategory)
            }

            is SubCategoryEvent.GetSubCategoryById -> {
                getSubCategoryById(event.id, event.onSuccess)
            }

            is SubCategoryEvent.ClearSubCategoryFields -> {
                _state.update { it.resetForm() }
            }

            // List management events
            is SubCategoryEvent.ReorderSubCategories -> {
                reorderSubCategories(event.fromIndex, event.toIndex, event.categoryId)
            }

            is SubCategoryEvent.UpdateSubCategoriesList -> {
                _state.update { it.copy(subCategories = event.subCategories) }
            }

            // Data loading events
            is SubCategoryEvent.FetchSubCategoriesForCategory -> {
                fetchSubCategoriesForCategory(event.categoryId)
            }

            is SubCategoryEvent.FetchAllCategoriesWithSubCategories -> {
                fetchAllCategoriesWithSubCategories()
            }

            is SubCategoryEvent.RefreshAllData -> {
                fetchAllCategoriesWithSubCategories()
                _state.value.selectedCategoryId?.let { categoryId ->
                    fetchSubCategoriesForCategory(categoryId)
                }
            }

            // Advanced options events
            is SubCategoryEvent.SetMergeMode -> {
                _state.update { currentState ->
                    currentState.copy(
                        isMergeMode = event.enabled,
                        isConvertToMainMode = if (event.enabled) false else currentState.isConvertToMainMode,
                        selectedTargetSubCategoryId = if (event.enabled) null else currentState.selectedTargetSubCategoryId
                    )
                }
                if (event.enabled) {
                    updateAvailableSubCategoriesForMerge()
                }
            }

            is SubCategoryEvent.SetConvertToMainMode -> {
                _state.update { currentState ->
                    currentState.copy(
                        isConvertToMainMode = event.enabled,
                        isMergeMode = if (event.enabled) false else currentState.isMergeMode,
                        selectedTargetSubCategoryId = if (event.enabled) null else currentState.selectedTargetSubCategoryId
                    )
                }
            }

            is SubCategoryEvent.SelectTargetSubCategoryForMerge -> {
                _state.update { it.copy(selectedTargetSubCategoryId = event.targetSubCategoryId) }
            }

            is SubCategoryEvent.MergeSubCategories -> {
                mergeSubCategories(event.sourceId, event.targetId) {
                    _state.update { it.resetAdvancedOptions() }
                }
            }

            is SubCategoryEvent.OnMergeCompleted -> {
                mergeSubCategories(
                    _state.value.currentEditingSubCategoryId ?: return,
                    _state.value.selectedTargetSubCategoryId ?: return,
                    event.onSuccess
                )
            }

            is SubCategoryEvent.ConvertToMainCategory -> {
                convertToMainCategory(event.subCategory, event.name, event.iconId, event.boxColor) {
                    _state.update { it.resetAdvancedOptions() }
                }
            }

            is SubCategoryEvent.OnConvertCompleted -> {
                val currentState = _state.value
                val subCategory = currentState.subCategories.find {
                    it.id == currentState.currentEditingSubCategoryId
                } ?: return

                convertToMainCategory(
                    subCategory,
                    currentState.newMainCategoryName,
                    currentState.newMainCategoryIconId,
                    currentState.newMainCategoryColor,
                    event.onSuccess
                )
            }

            is SubCategoryEvent.ResetAdvancedOptions -> {
                _state.update { it.resetAdvancedOptions() }
            }

            // Enhanced deletion events
            is SubCategoryEvent.InitiateSubCategoryDeletion -> {
                _state.update {
                    it.copy(
                        showSubCategoryDeleteConfirmationDialog = true,
                        pendingSubCategoryDeletion = event.subCategory
                    )
                }
            }

            is SubCategoryEvent.ShowSubCategoryDeleteOptions -> {
                _state.update {
                    it.copy(
                        showSubCategoryDeleteConfirmationDialog = false,
                        showSubCategoryDeleteOptionsDialog = true,
                        pendingSubCategoryDeletion = event.subCategory
                    )
                }
            }

            is SubCategoryEvent.ShowSubCategoryMoveTransactions -> {
                showSubCategoryMoveTransactions(event.subCategory)
            }

            is SubCategoryEvent.SelectTargetSubCategoryForMigration -> {
                _state.update { it.copy(selectedTargetSubCategoryForMigration = event.targetSubCategoryId) }
            }

            is SubCategoryEvent.ConfirmSubCategoryMigrationAndDeletion -> {
                confirmSubCategoryMigrationAndDeletion(event.sourceSubCategory, event.targetSubCategoryId)
            }

            is SubCategoryEvent.ConfirmSubCategoryDeletionWithTransactions -> {
                confirmSubCategoryDeletionWithTransactions(event.subCategory)
            }

            // Deletion with undo events
            is SubCategoryEvent.DeleteSubCategoryWithUndo -> {
                deleteSubCategoryWithUndo(event.subCategory, event.categoryId)
            }

            is SubCategoryEvent.UndoSubCategoryDeletion -> {
                undoSubCategoryDeletion()
            }

            is SubCategoryEvent.ClearPendingDeletion -> {
                clearPendingDeletion()
            }

            is SubCategoryEvent.HandleSheetDismissalDuringCountdown -> {
                handleSheetDismissalDuringCountdown()
            }

            // Dialog state management
            is SubCategoryEvent.DismissAllDeletionDialogs -> {
                _state.update { it.dismissAllDeletionDialogs() }
            }

            is SubCategoryEvent.CancelDeletion -> {
                _state.update { it.dismissAllDeletionDialogs() }
            }

            is SubCategoryEvent.ShowMergeConfirmationDialog -> {
                _state.update { it.copy(showMergeConfirmationDialog = event.show) }
            }

            is SubCategoryEvent.ShowConvertConfirmationDialog -> {
                _state.update { it.copy(showConvertConfirmationDialog = event.show) }
            }

            // Convert to main category form events
            is SubCategoryEvent.UpdateNewMainCategoryName -> {
                _state.update { it.copy(newMainCategoryName = event.name) }
            }

            is SubCategoryEvent.UpdateNewMainCategoryIconId -> {
                _state.update { it.copy(newMainCategoryIconId = event.iconId) }
            }

            is SubCategoryEvent.UpdateNewMainCategoryColor -> {
                _state.update { it.copy(newMainCategoryColor = event.color) }
            }

            // State management events
            is SubCategoryEvent.ClearSelection -> {
                _state.update {
                    it.copy(
                        selectedCategoryId = null,
                        subCategories = emptyList(),
                        currentEditingSubCategoryId = null
                    ).resetAdvancedOptions()
                }
            }

            is SubCategoryEvent.ClearError -> {
                _state.update { it.copy(error = null) }
            }

            is SubCategoryEvent.SetLoading -> {
                _state.update { it.copy(isLoading = event.isLoading) }
            }

            is SubCategoryEvent.SetError -> {
                _state.update { it.copy(error = event.error) }
            }
            is SubCategoryEvent.ClearSubCategories -> {
                _state.value = _state.value.copy(
                    subCategories = emptyList(),
                    selectedCategoryId = null
                )
            }

        }
    }

    // Private methods (keeping existing functionality)
    private fun fetchAllCategoriesWithSubCategories() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val categoriesWithSubs = categoryRepository.getCategoriesWithSubCategories()
                _state.update {
                    it.copy(
                        categoriesWithSubCategories = categoriesWithSubs,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = "Failed to fetch categories with subcategories: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun fetchSubCategoriesForCategory(categoryId: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val subCategoriesList = subCategoryRepository.getSubCategoriesByCategoryId(categoryId)
                    .sortedBy { it.position }
                _state.update {
                    it.copy(
                        subCategories = subCategoriesList,
                        selectedCategoryId = categoryId,
                        isLoading = false
                    )
                }
                if (_state.value.isMergeMode) {
                    updateAvailableSubCategoriesForMerge()
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = "Failed to fetch subcategories: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun saveSubCategory(onSuccess: () -> Unit) {
        val currentState = _state.value
        val categoryId = currentState.selectedCategoryId
        val subCategoryName = currentState.subCategoryName

        if (categoryId != null && subCategoryName.isNotBlank()) {
            viewModelScope.launch {
                _state.update { it.copy(isLoading = true) }
                try {
                    val currentSubCategories = currentState.subCategories
                    val parentCategory = categoryRepository.getCategoryById(categoryId)

                    val newSubCategory = SubCategoryEntity(
                        name = subCategoryName,
                        categoryId = categoryId,
                        boxColor = currentState.customBoxColor?.toArgb() ?: parentCategory?.boxColor,
                        subcategoryIconId = currentState.selectedIconId ?: parentCategory?.categoryIconId,
                        position = currentSubCategories.size
                    )

                    val insertedId = subCategoryRepository.insertSubCategory(newSubCategory)

                    activityLogUtils.logSubcategoryCreated(subCategoryName, categoryId)

                    // Refresh the subcategories immediately
                    fetchSubCategoriesForCategory(categoryId)
                    fetchAllCategoriesWithSubCategories()

                    // Emit events for UI updates
                    AppEventBus.tryEmitEvent(CategoryEvent.SubCategoriesUpdated(categoryId))
                    AppEventBus.tryEmitEvent(CategoryEvent.SubCategoryAdded(categoryId, insertedId.toInt()))

                    _state.update { it.resetForm().copy(isLoading = false) }
                    onSuccess()
                } catch (e: Exception) {
                    _state.update {
                        it.copy(
                            error = "Failed to save subcategory: ${e.message}",
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    private fun updateSubCategory(subCategory: SubCategoryEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val currentState = _state.value
                val oldName = subCategory.name
                val updatedSubCategory = subCategory.copy(
                    name = if (currentState.subCategoryName.isBlank()) subCategory.name else currentState.subCategoryName,
                    subcategoryIconId = currentState.selectedIconId ?: subCategory.subcategoryIconId,
                    boxColor = currentState.customBoxColor?.toArgb() ?: subCategory.boxColor,
                )

                subCategoryRepository.updateSubCategory(updatedSubCategory)

                val changes = if (oldName != updatedSubCategory.name) {
                    "Name changed from '$oldName' to '${updatedSubCategory.name}'"
                } else {
                    "Subcategory details updated"
                }
                activityLogUtils.logSubcategoryUpdated(updatedSubCategory.name, updatedSubCategory.categoryId)


                fetchSubCategoriesForCategory(subCategory.categoryId)
                fetchAllCategoriesWithSubCategories()

                _state.update { it.resetForm().copy(isLoading = false) }
                onSuccess()
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        error = "Failed to update subcategory: ${e.message}",
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun deleteSubCategory(subCategory: SubCategoryEntity) {
        viewModelScope.launch {
            try {
                activityLogUtils.logSubcategoryDeleted(subCategory.name, subCategory.categoryId)
                subCategoryRepository.deleteSubCategory(subCategory)
                _state.value.selectedCategoryId?.let { categoryId ->
                    fetchSubCategoriesForCategory(categoryId)
                }
                fetchAllCategoriesWithSubCategories()
            } catch (e: Exception) {
                _state.update {
                    it.copy(error = "Failed to delete subcategory: ${e.message}")
                }
            }
        }
    }

    private fun getSubCategoryById(id: Int, onSuccess: (SubCategoryEntity?) -> Unit) {
        viewModelScope.launch {
            try {
                val subCategory = subCategoryRepository.getSubCategoryById(id)
                _state.update {
                    it.copy(
                        isEditingSubCategory = true,
                        currentEditingSubCategoryId = id
                    )
                }
                onSuccess(subCategory)
            } catch (e: Exception) {
                _state.update {
                    it.copy(error = "Failed to get subcategory: ${e.message}")
                }
                onSuccess(null)
            }
        }
    }

    private fun reorderSubCategories(fromIndex: Int, toIndex: Int, categoryId: Int) {
        viewModelScope.launch {
            try {
                val currentSubCategories = _state.value.subCategories.toMutableList()

                if (fromIndex >= 0 && toIndex >= 0 && fromIndex < currentSubCategories.size && toIndex < currentSubCategories.size) {
                    val movedItem = currentSubCategories.removeAt(fromIndex)
                    currentSubCategories.add(toIndex, movedItem)

                    val updatedSubCategories = currentSubCategories.mapIndexed { index, subCategory ->
                        subCategory.copy(position = index)
                    }

                    _state.update { it.copy(subCategories = updatedSubCategories) }

                    subCategoryRepository.updateSubCategories(updatedSubCategories)
                    fetchAllCategoriesWithSubCategories()
                }
            } catch (e: Exception) {
                Log.e("SubCategoryViewModel", "Failed to update subcategory order: ${e.message}")
                fetchSubCategoriesForCategory(categoryId)
            }
        }
    }

    private fun mergeSubCategories(sourceSubCategoryId: Int, targetSubCategoryId: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val sourceSubCategory = subCategoryRepository.getSubCategoryById(sourceSubCategoryId)
                val targetSubCategory = subCategoryRepository.getSubCategoryById(targetSubCategoryId)

                if (sourceSubCategory != null && targetSubCategory != null) {
                    activityLogUtils.logSubcategoryDeleted(sourceSubCategory.name, sourceSubCategory.categoryId)

                    // Implement transaction migration
                    transactionRepository.updateTransactionsSubCategory(
                        sourceSubCategoryId,
                        targetSubCategoryId
                    )

                    subCategoryRepository.deleteSubCategory(sourceSubCategory)
                    fetchSubCategoriesForCategory(sourceSubCategory.categoryId)
                    fetchAllCategoriesWithSubCategories()

                    // Notify CategoryScreenViewModel to refresh
                    AppEventBus.tryEmitEvent(CategoryEvent.SubCategoriesUpdated(sourceSubCategory.categoryId))

                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("SubCategoryViewModel", "Error merging subcategories: ${e.message}", e)
                _state.update {
                    it.copy(error = "Failed to merge subcategories: ${e.message}")
                }
            }
        }
    }

    private fun convertToMainCategory(
        subCategory: SubCategoryEntity,
        name: String,
        iconId: Int,
        boxColor: Color,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val categoriesCount = categoryRepository.getAllCategories().size
                val newCategory = CategoryEntity(
                    id = 0,
                    name = name.ifEmpty { subCategory.name },
                    categoryIconId = iconId.takeIf { it != 0 } ?: subCategory.subcategoryIconId ?: 0,
                    boxColor = boxColor.takeIf { it != Color.Transparent }?.toArgb()
                        ?: subCategory.boxColor ?: 0,
                    textColor = Color.White.toArgb(),
                    position = categoriesCount
                )

                val newCategoryId = categoryRepository.addCategory(newCategory)

                activityLogUtils.logCategoryCreated(newCategory.name, newCategoryId.toInt())

                // Transfer transactions from subcategory to new main category
                transactionRepository.updateTransactionsFromSubCategoryToCategory(
                    subCategory.id,
                    newCategoryId.toInt()
                )

                subCategoryRepository.deleteSubCategory(subCategory)
                fetchAllCategoriesWithSubCategories()

                // Emit events for better state management
                AppEventBus.tryEmitEvent(CategoryEvent.SubCategoriesUpdated(subCategory.categoryId))
                AppEventBus.tryEmitEvent(CategoryEvent.CategoriesUpdated)
                AppEventBus.tryEmitEvent(CategoryEvent.SubCategoryConvertedToMain(subCategory.categoryId, newCategoryId.toInt()))

                onSuccess()
            } catch (e: Exception) {
                Log.e("SubCategoryViewModel", "Error converting to main category: ${e.message}", e)
                _state.update {
                    it.copy(error = "Failed to convert to main category: ${e.message}")
                }
            }
        }
    }

    private fun showSubCategoryMoveTransactions(subCategory: SubCategoryEntity) {
        viewModelScope.launch {
            try {
                // Get all subcategories in the same category except the one being deleted
                val availableSubCategories = subCategoryRepository.getSubCategoriesByCategoryId(subCategory.categoryId)
                    .filter { it.id != subCategory.id }

                _state.update {
                    it.copy(
                        showSubCategoryDeleteOptionsDialog = false,
                        showSubCategoryMoveTransactionsDialog = true,
                        availableSubCategoriesForMigration = availableSubCategories,
                        pendingSubCategoryDeletion = subCategory
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(error = "Failed to load subcategories for migration: ${e.message}")
                }
            }
        }
    }

    private fun confirmSubCategoryMigrationAndDeletion(sourceSubCategory: SubCategoryEntity, targetSubCategoryId: Int) {
        viewModelScope.launch {
            try {
                activityLogUtils.logSubcategoryDeleted(sourceSubCategory.name, sourceSubCategory.categoryId)

                // Use the repository method for migration
                subCategoryRepository.deleteSubCategoryWithMigration(
                    sourceSubCategory,
                    targetSubCategoryId,
                    transactionRepository
                )

                // Refresh data
                fetchSubCategoriesForCategory(sourceSubCategory.categoryId)
                fetchAllCategoriesWithSubCategories()

                // Emit event for UI updates
                AppEventBus.tryEmitEvent(CategoryEvent.SubCategoryDeleted(sourceSubCategory.categoryId, sourceSubCategory.id))

                // Clear dialog states
                _state.update { it.dismissAllDeletionDialogs() }
            } catch (e: Exception) {
                Log.e("SubCategoryViewModel", "Error migrating subcategory: ${e.message}", e)
                _state.update {
                    it.copy(error = "Failed to migrate subcategory: ${e.message}")
                }
            }
        }
    }

    private fun confirmSubCategoryDeletionWithTransactions(subCategory: SubCategoryEntity) {
        viewModelScope.launch {
            try {
                Log.d("SubCategoryDeletion", "Starting subcategory deletion with balance restoration for: ${subCategory.name}")
                activityLogUtils.logSubcategoryDeleted(subCategory.name, subCategory.categoryId)

                // Use the enhanced deletion method with balance restoration
                subCategoryRepository.deleteSubCategoryWithTransactionsAndBalanceRestore(
                    subCategory,
                    transactionRepository,
                    accountRepository
                )

                // Refresh data immediately to show updated balances
                fetchSubCategoriesForCategory(subCategory.categoryId)
                fetchAllCategoriesWithSubCategories()

                // Force refresh account balances immediately
                AppEventBus.tryEmitEvent(AccountEvent.AccountsUpdated)
                AppEventBus.tryEmitEvent(TransactionEvent.TransactionsUpdated)

                // Emit event for UI updates
                AppEventBus.tryEmitEvent(CategoryEvent.SubCategoryDeleted(subCategory.categoryId, subCategory.id))

                // Clear dialog states
                _state.update { it.dismissAllDeletionDialogs() }

                Log.d("SubCategoryDeletion", "Successfully completed subcategory deletion with balance restoration")
            } catch (e: Exception) {
                Log.e("SubCategoryDeletion", "Error deleting subcategory with balance restoration: ${e.message}", e)
                _state.update {
                    it.copy(error = "Failed to delete subcategory: ${e.message}")
                }
            }
        }
    }

    private fun deleteSubCategoryWithUndo(subCategory: SubCategoryEntity, categoryId: Int) {
        undoJob?.cancel()
        val currentSubCategories = _state.value.subCategories.toMutableList()
        val removedIndex = currentSubCategories.indexOfFirst { it.id == subCategory.id }

        if (removedIndex != -1) {
            currentSubCategories.removeAt(removedIndex)
            _state.update {
                it.copy(
                    subCategories = currentSubCategories,
                    pendingDeletion = subCategory
                )
            }

            undoJob = viewModelScope.launch {
                delay(5000)
                try {
                    activityLogUtils.logSubcategoryDeleted(subCategory.name, subCategory.categoryId)

                    subCategoryRepository.deleteSubCategory(subCategory)
                    fetchAllCategoriesWithSubCategories()
                    _state.update { it.copy(pendingDeletion = null) }
                } catch (e: Exception) {
                    Log.e("SubCategoryViewModel", "Failed to delete subcategory: ${e.message}")
                    fetchSubCategoriesForCategory(categoryId)
                    _state.update { it.copy(pendingDeletion = null) }
                }
            }
        }
    }

    private fun undoSubCategoryDeletion() {
        val deletedSubCategory = _state.value.pendingDeletion
        if (deletedSubCategory != null) {
            undoJob?.cancel()
            val currentSubCategories = _state.value.subCategories.toMutableList()
            val insertIndex = currentSubCategories.indexOfFirst { it.position > deletedSubCategory.position }
            if (insertIndex == -1) {
                currentSubCategories.add(deletedSubCategory)
            } else {
                currentSubCategories.add(insertIndex, deletedSubCategory)
            }
            _state.update {
                it.copy(
                    subCategories = currentSubCategories,
                    pendingDeletion = null
                )
            }
        }
    }

    private fun clearPendingDeletion() {
        _state.value.pendingDeletion?.let { pendingSubCategory ->
            viewModelScope.launch {
                try {
                    activityLogUtils.logSubcategoryDeleted(pendingSubCategory.name, pendingSubCategory.categoryId)

                    subCategoryRepository.deleteSubCategory(pendingSubCategory)
                    fetchAllCategoriesWithSubCategories()
                } catch (e: Exception) {
                    Log.e("SubCategoryViewModel", "Failed to delete subcategory: ${e.message}")
                }
            }
        }
        undoJob?.cancel()
        _state.update { it.copy(pendingDeletion = null) }
    }

    private fun handleSheetDismissalDuringCountdown() {
        val pendingSubCategory = _state.value.pendingDeletion
        if (pendingSubCategory != null) {
            viewModelScope.launch {
                try {
                    activityLogUtils.logSubcategoryDeleted(pendingSubCategory.name, pendingSubCategory.categoryId)

                    // Immediately delete the subcategory
                    subCategoryRepository.deleteSubCategory(pendingSubCategory)

                    // Refresh data
                    fetchSubCategoriesForCategory(pendingSubCategory.categoryId)

                    // Clear pending deletion
                    _state.update { it.copy(pendingDeletion = null) }
                    undoJob?.cancel()

                    // Emit event
                    AppEventBus.tryEmitEvent(CategoryEvent.SubCategoryDeleted(pendingSubCategory.categoryId, pendingSubCategory.id))
                } catch (e: Exception) {
                    Log.e("SubCategoryViewModel", "Error in immediate deletion: ${e.message}", e)
                }
            }
        }
    }

    private fun updateAvailableSubCategoriesForMerge() {
        val currentEditingId = _state.value.currentEditingSubCategoryId
        val availableSubCategories = _state.value.subCategories.filter {
            it.id != currentEditingId
        }
        _state.update { it.copy(availableSubCategoriesForMerge = availableSubCategories) }
    }

    fun prepareForEditingSubCategory(subCategory: SubCategoryEntity) {
        _state.update {
            it.resetForm().copy(
                currentEditingSubCategoryId = subCategory.id,
                selectedIconId = subCategory.subcategoryIconId,
                customBoxColor = subCategory.boxColor?.let { color -> Color(color) },
                subCategoryName = subCategory.name,
                selectedCategoryId = subCategory.categoryId,
                isEditingSubCategory = true,
                newMainCategoryName = subCategory.name,
                newMainCategoryIconId = subCategory.subcategoryIconId ?: 0,
                newMainCategoryColor = subCategory.boxColor?.let { color -> Color(color) } ?: Color.Gray
            )
        }
        updateAvailableSubCategoriesForMerge()
    }

    // Public methods for backward compatibility
    fun setSelectedIconId(iconId: Int?) {
        onEvent(SubCategoryEvent.UpdateSelectedIconId(iconId))
    }

    fun setCustomColor(color: Color?) {
        onEvent(SubCategoryEvent.UpdateCustomBoxColor(color))
    }

    fun setCustomTextColor(color: Color?) {
        onEvent(SubCategoryEvent.UpdateCustomTextColor(color))
    }

    fun setSubCategoryName(name: String) {
        onEvent(SubCategoryEvent.UpdateSubCategoryName(name))
    }

    fun setSelectedCategoryId(categoryId: Int?) {
        onEvent(SubCategoryEvent.UpdateSelectedCategoryId(categoryId))
    }

    fun clearSelection() {
        onEvent(SubCategoryEvent.ClearSelection)
    }
}
// SubCategoryViewModel.kt
//@HiltViewModel
//class SubCategoryViewModel @Inject constructor(
//    private val subCategoryRepository: SubCategoryRepository,
//    private val categoryRepository: CategoryRepository,
//    private val transactionRepository: TransactionRepository
//) : ViewModel() {
//
//    // Basic state for selected icon, custom color, and subcategory name
//    private val _selectedIconId = MutableStateFlow<Int?>(null)
//    val selectedIconId: StateFlow<Int?> = _selectedIconId.asStateFlow()
//
//    private val _customBoxColor = MutableStateFlow<Color?>(null)
//    val customBoxColor: StateFlow<Color?> = _customBoxColor.asStateFlow()
//
//    private val _customTextColor = MutableStateFlow<Color?>(null)
//    val customTextColor: StateFlow<Color?> = _customTextColor.asStateFlow()
//
//    private val _subCategoryName = MutableStateFlow("")
//    val subCategoryName: StateFlow<String> = _subCategoryName.asStateFlow()
//
//    private val _selectedCategoryId = MutableStateFlow<Int?>(null)
//    val selectedCategoryId: StateFlow<Int?> = _selectedCategoryId.asStateFlow()
//
//    // State to hold the list of subcategories for the selected category
//    private val _subCategories = MutableStateFlow<List<SubCategoryEntity>>(emptyList())
//    val subCategories: StateFlow<List<SubCategoryEntity>> = _subCategories.asStateFlow()
//
//    // State to hold categoriesWithSubcategories
//    private val _categoriesWithSubCategories = MutableStateFlow<List<CategoryWithSubCategories>>(emptyList())
//    val categoriesWithSubCategories: StateFlow<List<CategoryWithSubCategories>> = _categoriesWithSubCategories.asStateFlow()
//
//    private val _pendingDeletion = MutableStateFlow<SubCategoryEntity?>(null)
//    val pendingDeletion: StateFlow<SubCategoryEntity?> = _pendingDeletion.asStateFlow()
//
//    // New states for advanced options
//    private val _isMergeMode = MutableStateFlow(false)
//    val isMergeMode: StateFlow<Boolean> = _isMergeMode.asStateFlow()
//
//    private val _isConvertToMainMode = MutableStateFlow(false)
//    val isConvertToMainMode: StateFlow<Boolean> = _isConvertToMainMode.asStateFlow()
//
//    private val _selectedTargetSubCategoryId = MutableStateFlow<Int?>(null)
//    val selectedTargetSubCategoryId: StateFlow<Int?> = _selectedTargetSubCategoryId.asStateFlow()
//
//    // Available subcategories for merge selection (excluding current one)
//    private val _availableSubCategoriesForMerge = MutableStateFlow<List<SubCategoryEntity>>(emptyList())
//    val availableSubCategoriesForMerge: StateFlow<List<SubCategoryEntity>> = _availableSubCategoriesForMerge.asStateFlow()
//
//    private val _currentEditingSubCategoryId = MutableStateFlow<Int?>(null)
//    val currentEditingSubCategoryId: StateFlow<Int?> = _currentEditingSubCategoryId.asStateFlow()
//
//    private var undoJob: Job? = null
//
//    init {
//        fetchAllCategoriesWithSubCategories()
//        // FIX: Listen for subcategory update events
//        viewModelScope.launch {
//            AppEventBus.events.collect { event ->
//                when (event) {
//                    is CategoryScreenEvent.SubCategoriesUpdated -> {
//                        if (_selectedCategoryId.value == event.categoryId) {
//                            fetchSubCategoriesForCategory(event.categoryId)
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    // Event handler - NEW ADDITION
//    fun onEvent(event: SubCategoryEvent) {
//        when (event) {
//            is SubCategoryEvent.PrepareForNewSubCategory -> {
//                resetInputs()
//                setSelectedCategoryId(event.categoryId)
//            }
//            is SubCategoryEvent.UpdateSubCategoryName -> {
//                _subCategoryName.value = event.name
//            }
//            is SubCategoryEvent.UpdateSelectedIconId -> {
//                _selectedIconId.value = event.iconId
//            }
//            is SubCategoryEvent.UpdateCustomBoxColor -> {
//                _customBoxColor.value = event.color
//            }
//            is SubCategoryEvent.AddSubCategory -> {
//                saveSubCategory {}
//            }
//            is SubCategoryEvent.UpdateSubCategory -> {
//                updateSubCategory(event.subCategory) {}
//            }
//            is SubCategoryEvent.DeleteSubCategory -> {
//                deleteSubCategory(event.subCategory)
//            }
//            is SubCategoryEvent.ReorderSubCategories -> {
//                val categoryId = _selectedCategoryId.value ?: return
//                reorderSubCategories(event.fromIndex, event.toIndex, categoryId)
//            }
//            is SubCategoryEvent.ClearSubCategoryFields -> {
//                resetInputs()
//            }
//            is SubCategoryEvent.SetMergeMode -> {
//                _isMergeMode.value = event.enabled
//                if (event.enabled) {
//                    _isConvertToMainMode.value = false
//                    updateAvailableSubCategoriesForMerge()
//                } else {
//                    _selectedTargetSubCategoryId.value = null
//                }
//            }
//            is SubCategoryEvent.SetConvertToMainMode -> {
//                _isConvertToMainMode.value = event.enabled
//                if (event.enabled) {
//                    _isMergeMode.value = false
//                    _selectedTargetSubCategoryId.value = null
//                }
//            }
//            is SubCategoryEvent.SelectTargetSubCategoryForMerge -> {
//                _selectedTargetSubCategoryId.value = event.targetSubCategoryId
//            }
//            is SubCategoryEvent.MergeSubCategories -> {
//                mergeSubCategories(event.sourceId, event.targetId) {
//                    resetAdvancedOptions()
//                }
//            }
//            is SubCategoryEvent.ConvertToMainCategory -> {
//                convertToMainCategory(event.subCategory, event.name, event.iconId, event.boxColor) {
//                    resetAdvancedOptions()
//                }
//            }
//            is SubCategoryEvent.ResetAdvancedOptions -> {
//                resetAdvancedOptions()
//            }
//            is SubCategoryEvent.FetchSubCategoriesForCategory -> {
//                fetchSubCategoriesForCategory(event.categoryId)
//            }
//            is SubCategoryEvent.RefreshAllData -> {
//                fetchAllCategoriesWithSubCategories()
//                _selectedCategoryId.value?.let { fetchSubCategoriesForCategory(it) }
//            }
//        }
//    }
//
//    // EXISTING METHODS WITH IMPROVEMENTS
//    fun fetchAllCategoriesWithSubCategories() {
//        viewModelScope.launch {
//            _categoriesWithSubCategories.value = categoryRepository.getCategoriesWithSubCategories()
//        }
//    }
//
//    fun fetchSubCategoriesForCategory(categoryId: Int) {
//        viewModelScope.launch {
//            _subCategories.value = subCategoryRepository.getSubCategoriesByCategoryId(categoryId).sortedBy { it.position }
//            _selectedCategoryId.value = categoryId
//            if (_isMergeMode.value) {
//                updateAvailableSubCategoriesForMerge()
//            }
//        }
//    }
//
//    fun setSelectedIconId(iconId: Int?) {
//        _selectedIconId.value = iconId
//    }
//
//    fun setCustomColor(color: Color?) {
//        _customBoxColor.value = color
//    }
//
//    fun setCustomTextColor(color: Color?) {
//        _customTextColor.value = color
//    }
//
//    fun setSubCategoryName(name: String) {
//        _subCategoryName.value = name
//    }
//
//    fun setSelectedCategoryId(categoryId: Int?) {
//        _selectedCategoryId.value = categoryId
//        if (categoryId != null) {
//            fetchSubCategoriesForCategory(categoryId)
//        } else {
//            _subCategories.value = emptyList()
//        }
//    }
//
//    private fun saveSubCategoryOrder(subCategories: List<SubCategoryEntity>) {
//        viewModelScope.launch {
//            val updatedSubCategories = subCategories.mapIndexed { index, subCategory ->
//                subCategory.copy(position = index)
//            }
//            subCategoryRepository.updateSubCategories(updatedSubCategories)
//            _subCategories.value = updatedSubCategories
//            fetchAllCategoriesWithSubCategories()
//        }
//    }
//
//    fun deleteSubCategory(subCategory: SubCategoryEntity) {
//        viewModelScope.launch {
//            subCategoryRepository.deleteSubCategory(subCategory)
//            _selectedCategoryId.value?.let { fetchSubCategoriesForCategory(it) }
//            fetchAllCategoriesWithSubCategories()
//        }
//    }
//
//fun saveSubCategory(onSuccess: () -> Unit) {
//    val categoryId = _selectedCategoryId.value
//    val subCategoryName = _subCategoryName.value
//
//    if (categoryId != null && subCategoryName.isNotBlank()) {
//        viewModelScope.launch {
//            val currentSubCategories = _subCategories.value
//            val parentCategory = categoryRepository.getCategoryById(categoryId)
//
//            val newSubCategory = SubCategoryEntity(
//                name = subCategoryName,
//                categoryId = categoryId,
//                boxColor = _customBoxColor.value?.toArgb() ?: parentCategory?.boxColor,
//                subcategoryIconId = _selectedIconId.value ?: parentCategory?.categoryIconId,
//                position = currentSubCategories.size
//            )
//
//            val insertedId = subCategoryRepository.insertSubCategory(newSubCategory)
//
//            // FIX: Refresh the subcategories immediately
//            fetchSubCategoriesForCategory(categoryId)
//            fetchAllCategoriesWithSubCategories()
//
//            // FIX: Emit events for UI updates
//            AppEventBus.tryEmitEvent(CategoryEvent.SubCategoriesUpdated(categoryId))
//            AppEventBus.tryEmitEvent(CategoryEvent.SubCategoryAdded(categoryId, insertedId.toInt()))
//
//            onSuccess()
//            resetInputs()
//        }
//    }
//}
//
//    fun updateSubCategory(subCategory: SubCategoryEntity, onSuccess: () -> Unit) {
//        viewModelScope.launch {
//            val updatedSubCategory = subCategory.copy(
//                name = if (_subCategoryName.value.isBlank()) subCategory.name else _subCategoryName.value,
//                subcategoryIconId = _selectedIconId.value ?: subCategory.subcategoryIconId,
//                boxColor = _customBoxColor.value?.toArgb() ?: subCategory.boxColor,
//            )
//            subCategoryRepository.updateSubCategory(updatedSubCategory)
//            fetchSubCategoriesForCategory(subCategory.categoryId)
//            fetchAllCategoriesWithSubCategories()
//            onSuccess()
//            resetInputs()
//        }
//    }
//
//    fun getSubCategoryById(id: Int, onSuccess: (SubCategoryEntity?) -> Unit) {
//        viewModelScope.launch {
//            val subCategory = subCategoryRepository.getSubCategoryById(id)
//            onSuccess(subCategory)
//        }
//    }
//
//    fun reorderSubCategories(fromIndex: Int, toIndex: Int, categoryId: Int) {
//        viewModelScope.launch {
//            val currentSubCategories = _subCategories.value.toMutableList()
//
//            if (fromIndex >= 0 && toIndex >= 0 && fromIndex < currentSubCategories.size && toIndex < currentSubCategories.size) {
//                val movedItem = currentSubCategories.removeAt(fromIndex)
//                currentSubCategories.add(toIndex, movedItem)
//
//                val updatedSubCategories = currentSubCategories.mapIndexed { index, subCategory ->
//                    subCategory.copy(position = index)
//                }
//
//                _subCategories.value = updatedSubCategories
//
//                try {
//                    subCategoryRepository.updateSubCategories(updatedSubCategories)
//                    fetchAllCategoriesWithSubCategories()
//                } catch (e: Exception) {
//                    Log.e("SubCategoryViewModel", "Failed to update subcategory order: ${e.message}")
//                    fetchSubCategoriesForCategory(categoryId)
//                }
//            }
//        }
//    }
//
//    fun deleteSubCategoryWithUndo(subCategory: SubCategoryEntity, categoryId: Int) {
//        undoJob?.cancel()
//        val currentSubCategories = _subCategories.value.toMutableList()
//        val removedIndex = currentSubCategories.indexOfFirst { it.id == subCategory.id }
//
//        if (removedIndex != -1) {
//            currentSubCategories.removeAt(removedIndex)
//            _subCategories.value = currentSubCategories
//            _pendingDeletion.value = subCategory
//
//            undoJob = viewModelScope.launch {
//                delay(5000)
//                try {
//                    subCategoryRepository.deleteSubCategory(subCategory)
//                    fetchAllCategoriesWithSubCategories()
//                    _pendingDeletion.value = null
//                } catch (e: Exception) {
//                    Log.e("SubCategoryViewModel", "Failed to delete subcategory: ${e.message}")
//                    fetchSubCategoriesForCategory(categoryId)
//                    _pendingDeletion.value = null
//                }
//            }
//        }
//    }
//
//    fun undoSubCategoryDeletion() {
//        val deletedSubCategory = _pendingDeletion.value
//        if (deletedSubCategory != null) {
//            undoJob?.cancel()
//            val currentSubCategories = _subCategories.value.toMutableList()
//            val insertIndex = currentSubCategories.indexOfFirst { it.position > deletedSubCategory.position }
//            if (insertIndex == -1) {
//                currentSubCategories.add(deletedSubCategory)
//            } else {
//                currentSubCategories.add(insertIndex, deletedSubCategory)
//            }
//            _subCategories.value = currentSubCategories
//            _pendingDeletion.value = null
//        }
//    }
//
//    fun clearPendingDeletion() {
//        _pendingDeletion.value?.let {
//            viewModelScope.launch {
//                try {
//                    subCategoryRepository.deleteSubCategory(it)
//                    fetchAllCategoriesWithSubCategories()
//                } catch (e: Exception) {
//                    Log.e("SubCategoryViewModel", "Failed to delete subcategory: ${e.message}")
//                }
//            }
//        }
//        undoJob?.cancel()
//        _pendingDeletion.value = null
//    }
//
//    fun updateSubCategories(newSubCategories: List<SubCategoryEntity>) {
//        _subCategories.value = newSubCategories
//        saveSubCategoryOrder(newSubCategories)
//    }
//
//    // ENHANCED MERGE AND CONVERT METHODS
//    fun mergeSubCategories(
//        sourceSubCategoryId: Int,
//        targetSubCategoryId: Int,
//        onSuccess: () -> Unit
//    ) {
//        viewModelScope.launch {
//            val sourceSubCategory = subCategoryRepository.getSubCategoryById(sourceSubCategoryId)
//            val targetSubCategory = subCategoryRepository.getSubCategoryById(targetSubCategoryId)
//
//            if (sourceSubCategory != null && targetSubCategory != null) {
//                try {
//                    // FIX: Implement transaction migration
//                    transactionRepository.updateTransactionsSubCategory(
//                        sourceSubCategoryId,
//                        targetSubCategoryId
//                    )
//
//                    subCategoryRepository.deleteSubCategory(sourceSubCategory)
//                    fetchSubCategoriesForCategory(sourceSubCategory.categoryId)
//                    fetchAllCategoriesWithSubCategories()
//
//                    // FIX: Notify CategoryScreenViewModel to refresh
//                    AppEventBus.tryEmitEvent(CategoryEvent.SubCategoriesUpdated(sourceSubCategory.categoryId))
//
//                    onSuccess()
//                } catch (e: Exception) {
//                    Log.e("SubCategoryViewModel", "Error merging subcategories: ${e.message}", e)
//                }
//            }
//        }
//    }
//
//    fun convertToMainCategory(
//        subCategory: SubCategoryEntity,
//        name: String,
//        iconId: Int,
//        boxColor: Color,
//        onSuccess: () -> Unit
//    ) {
//        viewModelScope.launch {
//            try {
//                val categoriesCount = categoryRepository.getAllCategories().size
//                val newCategory = CategoryEntity(
//                    id = 0,
//                    name = name.ifEmpty { subCategory.name },
//                    categoryIconId = iconId.takeIf { it != 0 } ?: subCategory.subcategoryIconId ?: 0,
//                    boxColor = boxColor.takeIf { it != Color.Transparent }?.toArgb()
//                        ?: subCategory.boxColor ?: 0,
//                    textColor = Color.White.toArgb(),
//                    position = categoriesCount
//                )
//
//                val newCategoryId = categoryRepository.addCategory(newCategory)
//
//                // Transfer transactions from subcategory to new main category
//                transactionRepository.updateTransactionsFromSubCategoryToCategory(
//                    subCategory.id,
//                    newCategoryId.toInt()
//                )
//
//                subCategoryRepository.deleteSubCategory(subCategory)
//                fetchAllCategoriesWithSubCategories()
//
//                // FIX: Emit more specific events for better state management
//                AppEventBus.tryEmitEvent(CategoryEvent.SubCategoriesUpdated(subCategory.categoryId))
//                AppEventBus.tryEmitEvent(CategoryEvent.CategoriesUpdated)
//                AppEventBus.tryEmitEvent(CategoryEvent.SubCategoryConvertedToMain(subCategory.categoryId, newCategoryId.toInt()))
//
//                onSuccess()
//            } catch (e: Exception) {
//                Log.e("SubCategoryViewModel", "Error converting to main category: ${e.message}", e)
//            }
//        }
//    }
//
//    fun handleSheetDismissalDuringCountdown() {
//        val pendingSubCategory = _pendingDeletion.value
//        if (pendingSubCategory != null) {
//            viewModelScope.launch {
//                try {
//                    // Immediately delete the subcategory
//                    subCategoryRepository.deleteSubCategory(pendingSubCategory)
//
//                    // Refresh data
//                    fetchSubCategoriesForCategory(pendingSubCategory.categoryId)
//
//                    // Clear pending deletion
//                    _pendingDeletion.value = null
//                    undoJob?.cancel()
//
//                    // Emit event
//                    AppEventBus.tryEmitEvent(CategoryEvent.SubCategoryDeleted(pendingSubCategory.categoryId, pendingSubCategory.id))
//
//                } catch (e: Exception) {
//                    Log.e("SubCategoryViewModel", "Error in immediate deletion: ${e.message}", e)
//                }
//            }
//        }
//    }
//
//    fun clearSelection() {
//        _selectedCategoryId.value = null
//        _subCategories.value = emptyList()
//        resetAdvancedOptions()
//    }
//
//    private fun updateAvailableSubCategoriesForMerge() {
//        val currentEditingId = _currentEditingSubCategoryId.value
//        _availableSubCategoriesForMerge.value = _subCategories.value.filter {
//            it.id != currentEditingId // FIX: Use actual editing subcategory ID
//        }
//    }
//
//    private fun resetAdvancedOptions() {
//        _isMergeMode.value = false
//        _isConvertToMainMode.value = false
//        _selectedTargetSubCategoryId.value = null
//        _availableSubCategoriesForMerge.value = emptyList()
//    }
//
//    private fun resetInputs() {
//        _selectedIconId.value = null
//        _customBoxColor.value = null
//        _customTextColor.value = null
//        _subCategoryName.value = ""
//        _currentEditingSubCategoryId.value = null // FIX: Reset editing ID
//        resetAdvancedOptions()
//    }
//
//    fun prepareForEditingSubCategory(subCategory: SubCategoryEntity) {
//        resetInputs()
//        _currentEditingSubCategoryId.value = subCategory.id // FIX: Track current editing ID
//        _selectedIconId.value = subCategory.subcategoryIconId
//        _customBoxColor.value = subCategory.boxColor?.let { Color(it) }
//        _subCategoryName.value = subCategory.name
//        _selectedCategoryId.value = subCategory.categoryId
//        updateAvailableSubCategoriesForMerge()
//    }
//}
