package com.ritesh.cashiro.presentation.ui.features.categories

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ritesh.cashiro.data.local.entity.CategoryEntity
import com.ritesh.cashiro.data.local.entity.SubCategoryEntity
import com.ritesh.cashiro.domain.repository.AccountRepository
import com.ritesh.cashiro.domain.repository.CategoryRepository
import com.ritesh.cashiro.domain.repository.SubCategoryRepository
import com.ritesh.cashiro.domain.repository.TransactionRepository
import com.ritesh.cashiro.domain.repository.TransactionStats
import com.ritesh.cashiro.domain.utils.AccountEvent
import com.ritesh.cashiro.domain.utils.ActivityLogUtils
import com.ritesh.cashiro.domain.utils.AppEventBus
import com.ritesh.cashiro.domain.utils.CategoryEvent
import com.ritesh.cashiro.domain.utils.TransactionEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


// CategoryScreenViewModel.kt - Fixed State Management
@HiltViewModel
class CategoryScreenViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val subCategoryRepository: SubCategoryRepository,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val activityLogUtils: ActivityLogUtils,
) : ViewModel(), CategoryScreenEventHandler {

    // Main state
    private val _state = MutableStateFlow(CategoryScreenState())
    val state: StateFlow<CategoryScreenState> = _state.asStateFlow()

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

    val categoryName = state.map { it.categoryName }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = ""
    )

    val categories = state.map { it.categories }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    val categoriesWithSubCategories = state.map { it.categoriesWithSubCategories }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    val subCategories = state.map { it.subCategories }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    init {
        onEvent(CategoryScreenEvent.FetchAllCategories)
        onEvent(CategoryScreenEvent.FetchAllCategoriesWithSubCategories)
        viewModelScope.launch {
            AppEventBus.events.collect { event ->
                when (event) {
                    is CategoryEvent.CategoriesUpdated -> {
                        // Refresh all categories when notified
                        fetchAllCategories()
                        fetchAllCategoriesWithSubCategories()
                    }
                    is CategoryEvent.CategoryAdded -> {
                        fetchAllCategories()
                        fetchAllCategoriesWithSubCategories()
                    }
                    is CategoryEvent.CategoryUpdated -> {
                        fetchAllCategories()
                        fetchAllCategoriesWithSubCategories()
                    }
                    is CategoryEvent.CategoryDeleted -> {
                        fetchAllCategories()
                        fetchAllCategoriesWithSubCategories()
                    }
                    is CategoryEvent.SubCategoryConvertedToMain -> {
                        // This is specifically for when subcategories become main categories
                        fetchAllCategories()
                        fetchAllCategoriesWithSubCategories()
                    }
                }
            }
        }
    }

    override fun onEvent(event: CategoryScreenEvent) {
        when (event) {
            // Form events
            is CategoryScreenEvent.UpdateCategoryName -> {
                _state.update { it.copy(categoryName = event.name) }
            }

            is CategoryScreenEvent.UpdateSelectedIconId -> {
                _state.update { it.copy(selectedIconId = event.iconId) }
            }

            is CategoryScreenEvent.UpdateCustomBoxColor -> {
                _state.update { it.copy(customBoxColor = event.color) }
            }

            is CategoryScreenEvent.UpdateCustomTextColor -> {
                _state.update { it.copy(customTextColor = event.color) }
            }

            is CategoryScreenEvent.UpdateIsMainCategory -> {
                _state.update {
                    val newState = it.copy(isMainCategory = event.isMain)
                    // If setting to main category, disable subcategory
                    if (event.isMain) {
                        newState.copy(isSubCategory = false, selectedMainCategoryId = null)
                    } else {
                        newState
                    }
                }
            }

            is CategoryScreenEvent.UpdateIsSubCategory -> {
                _state.update {
                    val newState = it.copy(isSubCategory = event.isSub)
                    // If setting to subcategory, disable main category
                    if (event.isSub) {
                        newState.copy(isMainCategory = false)
                    } else {
                        newState.copy(selectedMainCategoryId = null)
                    }
                }
            }

            is CategoryScreenEvent.UpdateSelectedMainCategoryId -> {
                _state.update {
                    it.copy(
                        selectedMainCategoryId = event.categoryId,
                        // If selecting a parent category, it's a subcategory
                        isSubCategory = event.categoryId != null,
                        // And not a main category
                        isMainCategory = event.categoryId == null
                    )
                }
            }

            // Category management events
            is CategoryScreenEvent.PrepareForNewCategory -> {
                resetInputs() // Use the improved reset method
            }

            is CategoryScreenEvent.SaveCategory -> {
                saveCategory {}
            }

            is CategoryScreenEvent.OnCategorySaved -> {
                saveCategory(event.onSuccess)
            }

            is CategoryScreenEvent.UpdateExistingCategory -> {
                updateCategory(event.category) {}
            }

            is CategoryScreenEvent.OnCategoryUpdated -> {
                updateCategory(event.category, event.onSuccess)
            }

            is CategoryScreenEvent.DeleteCategory -> {
                deleteCategory(event.category)
            }

            is CategoryScreenEvent.GetCategoryById -> {
                getCategoryById(event.id) {}
            }

            is CategoryScreenEvent.OnCategoryFetched -> {
                getCategoryById(event.id, event.onSuccess)
            }

            // Subcategory management events
            is CategoryScreenEvent.PrepareForNewSubCategory -> {
                resetInputsForSubCategory() // Use specific reset for subcategories
            }

            is CategoryScreenEvent.PrepareForNewSubCategoryInParent -> {
                resetInputsForSubCategory()
                _state.update {
                    it.copy(
                        selectedMainCategoryId = event.parentCategoryId,
                        isMainCategory = false,
                        isSubCategory = true
                    )
                }
            }
            is CategoryScreenEvent.SubCategoriesUpdated -> {
                // Refresh subcategories for the specific category
                onEvent(CategoryScreenEvent.FetchSubCategoriesForCategory(event.categoryId))
                onEvent(CategoryScreenEvent.FetchAllCategoriesWithSubCategories)
            }
            is CategoryScreenEvent.CategoriesUpdated -> {
                // Refresh all categories
                onEvent(CategoryScreenEvent.FetchAllCategories)
                onEvent(CategoryScreenEvent.FetchAllCategoriesWithSubCategories)
            }

            is CategoryScreenEvent.SaveSubCategory -> {
                saveSubCategory {}
            }

            is CategoryScreenEvent.OnSubCategorySaved -> {
                saveSubCategory(event.onSuccess)
            }

            is CategoryScreenEvent.UpdateExistingSubCategory -> {
                updateSubCategory(event.subCategory) {}
            }

            is CategoryScreenEvent.OnSubCategoryUpdated -> {
                updateSubCategory(event.subCategory, event.onSuccess)
            }

            is CategoryScreenEvent.DeleteSubCategory -> {
                deleteSubCategory(event.subCategory)
            }

            is CategoryScreenEvent.GetSubCategoryById -> {
                getSubCategoryById(event.id) {}
            }

            is CategoryScreenEvent.OnSubCategoryFetched -> {
                getSubCategoryById(event.id, event.onSuccess)
            }

            // List events
            is CategoryScreenEvent.ReorderCategories -> {
                val currentCategories = _state.value.categories
                if (event.fromIndex in currentCategories.indices && event.toIndex in currentCategories.indices) {
                    val updatedCategories = currentCategories.toMutableList().apply {
                        val itemToMove = removeAt(event.fromIndex)
                        add(event.toIndex, itemToMove)
                    }
                    updateCategories(updatedCategories)
                } else {
                    Log.e("CategoryListItem", "Invalid indices: fromIndex=${event.fromIndex}, toIndex=${event.toIndex}")
                }
            }

            is CategoryScreenEvent.ReorderSubCategories -> {
                reorderSubCategories(event.fromIndex, event.toIndex, event.categoryId)
            }

            is CategoryScreenEvent.UpdateCategoriesList -> {
                updateCategories(event.categories)
            }

            // Data loading events
            is CategoryScreenEvent.FetchAllCategories -> {
                fetchAllCategories()
            }

            is CategoryScreenEvent.FetchAllCategoriesWithSubCategories -> {
                fetchAllCategoriesWithSubCategories()
            }

            is CategoryScreenEvent.FetchSubCategoriesForCategory -> {
                fetchSubCategoriesForCategory(event.categoryId)
            }
            is CategoryScreenEvent.InitiateCategoryDeletion -> {
                initiateEnhancedCategoryDeletion(event.category)
            }

            is CategoryScreenEvent.ShowCategoryDeleteOptions -> {
                showCategoryDeleteOptions(event.category)
            }

            is CategoryScreenEvent.ShowCategoryMoveTransactions -> {
                showCategoryMoveTransactions(event.category)
            }

            is CategoryScreenEvent.SelectTargetCategoryForMigration -> {
                _state.update { it.copy(selectedTargetCategoryId = event.targetCategoryId) }
            }

            is CategoryScreenEvent.ConfirmCategoryMigrationAndDeletion -> {
                confirmCategoryMigrationAndDeletion(event.sourceCategory, event.targetCategoryId)
            }

            is CategoryScreenEvent.ConfirmCategoryDeletionWithTransactions -> {
                confirmCategoryDeletionWithTransactions(event.category)
            }

            // Enhanced subcategory deletion events
            is CategoryScreenEvent.InitiateSubCategoryDeletion -> {
                initiateEnhancedSubCategoryDeletion(event.subCategory)
            }

            is CategoryScreenEvent.ShowSubCategoryDeleteOptions -> {
                showSubCategoryDeleteOptions(event.subCategory)
            }

            is CategoryScreenEvent.ShowSubCategoryMoveTransactions -> {
                showSubCategoryMoveTransactions(event.subCategory)
            }

            is CategoryScreenEvent.SelectTargetSubCategoryForMigration -> {
                _state.update { it.copy(selectedTargetSubCategoryId = event.targetSubCategoryId) }
            }

            is CategoryScreenEvent.ConfirmSubCategoryMigrationAndDeletion -> {
                confirmSubCategoryMigrationAndDeletion(event.sourceSubCategory, event.targetSubCategoryId)
            }

            is CategoryScreenEvent.ConfirmSubCategoryDeletionWithTransactions -> {
                confirmSubCategoryDeletionWithTransactions(event.subCategory)
            }

            // Dialog management
            is CategoryScreenEvent.DismissAllDeletionDialogs -> {
                dismissAllDeletionDialogs()
            }

            is CategoryScreenEvent.CancelDeletion -> {
                cancelDeletion()
            }
            is CategoryScreenEvent.InitiateMergeCategory -> {
                initiateMergeCategory(event.sourceCategory)
            }

            is CategoryScreenEvent.SelectTargetCategoryForMerge -> {
                _state.update { it.copy(selectedMergeCategoryTargetId = event.targetCategoryId) }
            }

            is CategoryScreenEvent.ConfirmCategoryMerge -> {
                confirmCategoryMerge(event.sourceCategory, event.targetCategoryId)
            }

            is CategoryScreenEvent.CancelMergeCategory -> {
                cancelMergeCategory()
            }
        }
    }

    // EXISTING METHODS (keeping all the existing functionality)

    // Fetch all categories from the repository and sort them by position
    private fun fetchAllCategories() {
        viewModelScope.launch {
            val categoriesList = categoryRepository.getAllCategories().sortedBy { it.position }
            _state.update { it.copy(categories = categoriesList) }
        }
    }

    // Fetch categories with their subcategories
    private fun fetchAllCategoriesWithSubCategories() {
        viewModelScope.launch {
            val categoriesWithSubs = categoryRepository.getCategoriesWithSubCategories()
            _state.update { it.copy(categoriesWithSubCategories = categoriesWithSubs) }
        }
    }

    // Fetch subcategories for a specific category
    private fun fetchSubCategoriesForCategory(categoryId: Int) {
        viewModelScope.launch {
            val subCategoriesList = subCategoryRepository.getSubCategoriesByCategoryId(categoryId).sortedBy { it.position }
            _state.update {
                it.copy(
                    subCategories = subCategoriesList,
                    selectedMainCategoryId = categoryId
                )
            }
        }
    }

    // Update the categories and save their new order
    private fun updateCategories(newCategories: List<CategoryEntity>) {
        _state.update { it.copy(categories = newCategories) }
        saveCategoryOrder(newCategories)
    }

    private fun saveCategoryOrder(categories: List<CategoryEntity>) {
        viewModelScope.launch {
            // Update the position field based on the new order
            val updatedCategories = categories.mapIndexed { index, category ->
                category.copy(position = index)
            }
            categoryRepository.updateCategoryOrder(updatedCategories)
            _state.update { it.copy(categories = updatedCategories) } // Update local state
            fetchAllCategoriesWithSubCategories() // Refresh the combined data
        }
    }

    // Delete a category
    private fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch {
            // Log category deletion to ActivityLog before deleting
            activityLogUtils.logCategoryDeleted(category.name, category.id)
            // When deleting a category, all related subcategories will be deleted due to CASCADE
            categoryRepository.deleteCategory(category)
            fetchAllCategories() // Refresh the list after deletion
            fetchAllCategoriesWithSubCategories()
        }
    }

    // Save a new category
    private fun saveCategory(onSuccess: () -> Unit) {
        val currentState = _state.value
        val iconId = currentState.selectedIconId
        val customBoxColor = currentState.customBoxColor
        val customTextColor = currentState.customTextColor
        val categoryName = currentState.categoryName
        val isMainCategory = currentState.isMainCategory
        val isSubCategory = currentState.isSubCategory
        val selectedMainCategoryId = currentState.selectedMainCategoryId

        if (isMainCategory) {
            // Save as main category
            if (iconId != null && customBoxColor != null && customTextColor != null && categoryName.isNotBlank()) {
                viewModelScope.launch {
                    val currentCategories = _state.value.categories
                    val newCategory = CategoryEntity(
                        name = categoryName,
                        boxColor = customBoxColor.toArgb(),
                        categoryIconId = iconId,
                        textColor = customTextColor.toArgb(),
                        position = currentCategories.size // Add new category at the end
                    )
                    val categoryId = categoryRepository.addCategory(newCategory)

                    //  Log category creation to ActivityLog
                    activityLogUtils.logCategoryCreated(categoryName, categoryId.toInt())
                    fetchAllCategories()
                    fetchAllCategoriesWithSubCategories()
                    onSuccess()
                    resetInputs()
                }
            }
        } else if (isSubCategory && selectedMainCategoryId != null) {
            // Save as subcategory
            if (categoryName.isNotBlank()) {
                viewModelScope.launch {
                    val parentCategory = categoryRepository.getCategoryById(selectedMainCategoryId)

                    // Use parent category values as defaults if not specified
                    val newSubCategory = SubCategoryEntity(
                        name = categoryName,
                        categoryId = selectedMainCategoryId,
                        boxColor = customBoxColor?.toArgb() ?: parentCategory?.boxColor,
                        subcategoryIconId = iconId ?: parentCategory?.categoryIconId,
                        position = currentState.subCategories.size // Add new subcategory at the end
                    )

                    subCategoryRepository.insertSubCategory(newSubCategory)
                    //  Log subcategory creation to ActivityLog
                    activityLogUtils.logSubcategoryCreated(categoryName, selectedMainCategoryId)

                    fetchSubCategoriesForCategory(selectedMainCategoryId)
                    fetchAllCategoriesWithSubCategories()
                    onSuccess()
                    resetInputs()
                }
            }
        }
    }

    // Update an existing category
    private fun updateCategory(category: CategoryEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val oldName = category.name
            val updatedCategory = category.copy(
                name = if(_state.value.categoryName.isEmpty()) category.name else _state.value.categoryName,
                categoryIconId = _state.value.selectedIconId ?: category.categoryIconId,
                boxColor = _state.value.customBoxColor?.toArgb() ?: category.boxColor,
                textColor = _state.value.customTextColor?.toArgb() ?: category.textColor
            )
            categoryRepository.updateCategory(updatedCategory)

            val changes = if (oldName != updatedCategory.name) {
                "Name changed from '$oldName' to '${updatedCategory.name}'"
            } else {
                "Category details updated"
            }
            activityLogUtils.logCategoryUpdated(updatedCategory.name, updatedCategory.id, changes)


            fetchAllCategories() // Refresh the list after update
            fetchAllCategoriesWithSubCategories()
            onSuccess()
            resetInputs()
        }
    }

    // Fetch a single category by its ID
    private fun getCategoryById(id: Int, onSuccess: (CategoryEntity?) -> Unit) {
        viewModelScope.launch {
            val category = categoryRepository.getCategoryById(id)
            _state.update {
                it.copy(
                    isEditingCategory = true,
                    currentCategoryId = id
                )
            }

            // If category is found, update state with its values
            category?.let { foundCategory ->
                _state.update { state ->
                    state.copy(
                        categoryName = foundCategory.name,
                        selectedIconId = foundCategory.categoryIconId,
                        customBoxColor = Color(foundCategory.boxColor),
                        customTextColor = Color(foundCategory.textColor),
                        isMainCategory = true,
                        isSubCategory = false
                    )
                }
            }

            onSuccess(category)
        }
    }

    // Save a new subcategory
    private fun saveSubCategory(onSuccess: () -> Unit) {
        val currentState = _state.value
        val categoryId = currentState.selectedMainCategoryId
        val subCategoryName = currentState.categoryName

        if (categoryId != null && subCategoryName.isNotBlank()) {
            viewModelScope.launch {
                val currentSubCategories = currentState.subCategories
                val parentCategory = categoryRepository.getCategoryById(categoryId)

                val newSubCategory = SubCategoryEntity(
                    name = subCategoryName,
                    categoryId = categoryId,
                    boxColor = currentState.customBoxColor?.toArgb() ?: parentCategory?.boxColor,
                    subcategoryIconId = currentState.selectedIconId ?: parentCategory?.categoryIconId,
                    position = currentSubCategories.size
                )

                val insertedSubCategoryId = subCategoryRepository.insertSubCategory(newSubCategory)

                // Log subcategory creation to ActivityLog
                activityLogUtils.logSubcategoryCreated(subCategoryName, categoryId)


                // FIX: Ensure proper refresh of subcategories
                fetchSubCategoriesForCategory(categoryId)
                fetchAllCategoriesWithSubCategories()

                // FIX: Emit specific events for better state management
                AppEventBus.tryEmitEvent(CategoryEvent.SubCategoriesUpdated(categoryId))
                AppEventBus.tryEmitEvent(CategoryEvent.SubCategoryAdded(categoryId, insertedSubCategoryId?.toInt() ?: -1))

                onSuccess()
                resetInputs()
            }
        }
    }

    // Update an existing subcategory
    private fun updateSubCategory(subCategory: SubCategoryEntity, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val oldName = subCategory.name
            val updatedSubCategory = subCategory.copy(
                name = if (_state.value.categoryName.isBlank()) subCategory.name else _state.value.categoryName,
                subcategoryIconId = _state.value.selectedIconId ?: subCategory.subcategoryIconId,
                boxColor = _state.value.customBoxColor?.toArgb() ?: subCategory.boxColor,
            )
            subCategoryRepository.updateSubCategory(updatedSubCategory)

            val changes = if (oldName != updatedSubCategory.name) {
                "Name changed from '$oldName' to '${updatedSubCategory.name}'"
            } else {
                "Subcategory details updated"
            }
            activityLogUtils.logSubcategoryUpdated(updatedSubCategory.name, updatedSubCategory.categoryId)


            // Refresh the subcategories for the current category
            fetchSubCategoriesForCategory(subCategory.categoryId)
            fetchAllCategoriesWithSubCategories()
            onSuccess()
            resetInputs()
        }
    }

    // Delete a subcategory
    private fun deleteSubCategory(subCategory: SubCategoryEntity) {
        viewModelScope.launch {
            //  Log subcategory deletion to ActivityLog before deleting
            activityLogUtils.logSubcategoryDeleted(subCategory.name, subCategory.categoryId)

            subCategoryRepository.deleteSubCategory(subCategory)
            // Refresh the subcategories for the current selected category
            subCategory.categoryId.let { fetchSubCategoriesForCategory(it) }
            fetchAllCategoriesWithSubCategories()
        }
    }

    // Get a subcategory by ID
    private fun getSubCategoryById(id: Int, onSuccess: (SubCategoryEntity?) -> Unit) {
        viewModelScope.launch {
            val subCategory = subCategoryRepository.getSubCategoryById(id)
            _state.update {
                it.copy(
                    isEditingSubCategory = true,
                    currentSubCategoryId = id
                )
            }

            // If subcategory is found, update state with its values
            subCategory?.let { foundSubCategory ->
                _state.update { state ->
                    state.copy(
                        categoryName = foundSubCategory.name,
                        selectedIconId = foundSubCategory.subcategoryIconId,
                        customBoxColor = foundSubCategory.boxColor?.let { Color(it) },
                        isMainCategory = false,
                        isSubCategory = true,
                        selectedMainCategoryId = foundSubCategory.categoryId
                    )
                }
                // Also fetch the parent category's subcategories
                fetchSubCategoriesForCategory(foundSubCategory.categoryId)
            }

            onSuccess(subCategory)
        }
    }

    // Reorder subcategories
    private fun reorderSubCategories(fromIndex: Int, toIndex: Int, categoryId: Int) {
        viewModelScope.launch {
            // Get the current subcategories from state
            val currentSubCategories = _state.value.subCategories.toMutableList()

            if (fromIndex >= 0 && toIndex >= 0 && fromIndex < currentSubCategories.size && toIndex < currentSubCategories.size) {
                // Create a mutable list to perform the reordering
                val movedItem = currentSubCategories.removeAt(fromIndex)
                currentSubCategories.add(toIndex, movedItem)

                // Update positions based on new order
                val updatedSubCategories = currentSubCategories.mapIndexed { index, subCategory ->
                    subCategory.copy(position = index)
                }

                // Update local state first for immediate UI feedback
                _state.update { it.copy(subCategories = updatedSubCategories) }

                // Then update in database
                subCategoryRepository.updateSubCategories(updatedSubCategories)

                // Finally, refresh the combined data
                fetchAllCategoriesWithSubCategories()
            }
        }
    }

    fun updateCollapsingFraction(fraction: Float) {
        if (!fraction.isNaN()) {
            val maxOffset = 180.dp
            val minOffset = 0.dp
            val offsetRange = maxOffset - minOffset
            val currentOffset = maxOffset - (offsetRange * fraction)

            _state.update {
                it.copy(
                    collapsingFraction = fraction,
                    currentOffset = currentOffset
                )
            }
        }
    }

    // Enhanced deletion methods for categories
    private fun initiateEnhancedCategoryDeletion(category: CategoryEntity) {
        _state.update {
            it.copy(
                showCategoryDeleteConfirmationDialog = true,
                pendingCategoryDeletion = category
            )
        }
    }

    private fun showCategoryDeleteOptions(category: CategoryEntity) {
        _state.update {
            it.copy(
                showCategoryDeleteConfirmationDialog = false,
                showCategoryDeleteOptionsDialog = true,
                pendingCategoryDeletion = category
            )
        }
    }

    private fun showCategoryMoveTransactions(category: CategoryEntity) {
        viewModelScope.launch {
            // Get all categories except the one being deleted and the transfer category (id = 1)
            val availableCategories = categoryRepository.getAllCategories()
                .filter { it.id != category.id && it.id != 1 }

            _state.update {
                it.copy(
                    showCategoryDeleteOptionsDialog = false,
                    showCategoryMoveTransactionsDialog = true,
                    availableCategoriesForMigration = availableCategories,
                    pendingCategoryDeletion = category
                )
            }
        }
    }
    private fun confirmCategoryMigrationAndDeletion(sourceCategory: CategoryEntity, targetCategoryId: Int) {
        viewModelScope.launch {
            try {
                Log.d("CategoryMigration", "Starting migration of category '${sourceCategory.name}' to category ID $targetCategoryId")
                // Log category deletion to ActivityLog before migrating
                activityLogUtils.logCategoryDeleted(sourceCategory.name, sourceCategory.id)
                // Use the enhanced migration method that includes subcategories
                categoryRepository.deleteCategoryWithMigration(
                    sourceCategory,
                    targetCategoryId,
                    transactionRepository,
                    subCategoryRepository
                )

                // IMMEDIATE UI UPDATES
                // Refresh data immediately
                fetchAllCategories()
                fetchAllCategoriesWithSubCategories()

                // Force refresh account balances (no balance change, but ensure UI consistency)
                viewModelScope.launch {
                    AppEventBus.tryEmitEvent(AccountEvent.AccountsUpdated)
                    AppEventBus.tryEmitEvent(TransactionEvent.TransactionsUpdated)
                }

                // Emit events for UI updates
                AppEventBus.tryEmitEvent(CategoryEvent.CategoryDeleted(sourceCategory.id))
                AppEventBus.tryEmitEvent(CategoryEvent.CategoriesUpdated)

                // Clear dialog states
                dismissAllDeletionDialogs()

                Log.d("CategoryMigration", "Successfully completed category migration")

            } catch (e: Exception) {
                Log.e("CategoryMigration", "Error migrating category: ${e.message}", e)
                _state.update {
                    it.copy(error = "Error migrating category: ${e.message}")
                }
            }
        }
    }

private fun confirmCategoryDeletionWithTransactions(category: CategoryEntity) {
    viewModelScope.launch {
        try {
            Log.d("CategoryDeletion", "Starting category deletion with balance restoration for: ${category.name}")

            // Log category deletion to ActivityLog before deleting
            activityLogUtils.logCategoryDeleted(category.name, category.id)

            // Use the enhanced deletion method with balance restoration
            categoryRepository.deleteCategoryWithTransactionsAndBalanceRestore(
                category,
                transactionRepository,
                accountRepository
            )

            // IMMEDIATE UI UPDATES
            // Refresh data immediately to show updated balances
            fetchAllCategories()
            fetchAllCategoriesWithSubCategories()

            // Force refresh account balances immediately
            viewModelScope.launch {
                AppEventBus.tryEmitEvent(AccountEvent.AccountsUpdated)
                AppEventBus.tryEmitEvent(TransactionEvent.TransactionsUpdated)
            }

            // Emit event for UI updates
            AppEventBus.tryEmitEvent(CategoryEvent.CategoryDeleted(category.id))

            // Clear dialog states
            dismissAllDeletionDialogs()

            Log.d("CategoryDeletion", "Successfully completed category deletion with balance restoration")

        } catch (e: Exception) {
            Log.e("CategoryDeletion", "Error deleting category with balance restoration: ${e.message}", e)
            _state.update {
                it.copy(error = "Error deleting category: ${e.message}")
            }
        }
    }
}

    // Enhanced deletion methods for subcategories
    private fun initiateEnhancedSubCategoryDeletion(subCategory: SubCategoryEntity) {
        _state.update {
            it.copy(
                showSubCategoryDeleteConfirmationDialog = true,
                pendingSubCategoryDeletion = subCategory
            )
        }
    }

    private fun showSubCategoryDeleteOptions(subCategory: SubCategoryEntity) {
        _state.update {
            it.copy(
                showSubCategoryDeleteConfirmationDialog = false,
                showSubCategoryDeleteOptionsDialog = true,
                pendingSubCategoryDeletion = subCategory
            )
        }
    }

    private fun showSubCategoryMoveTransactions(subCategory: SubCategoryEntity) {
        viewModelScope.launch {
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
        }
    }

    private fun confirmSubCategoryMigrationAndDeletion(sourceSubCategory: SubCategoryEntity, targetSubCategoryId: Int) {
        viewModelScope.launch {
            try {
                // Log subcategory deletion to ActivityLog before migrating
                activityLogUtils.logSubcategoryDeleted(sourceSubCategory.name, sourceSubCategory.categoryId)


                // Use the new repository method for migration
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
                dismissAllDeletionDialogs()

            } catch (e: Exception) {
                Log.e("SubCategoryMigration", "Error migrating subcategory: ${e.message}", e)
            }
        }
    }
private fun confirmSubCategoryDeletionWithTransactions(subCategory: SubCategoryEntity) {
    viewModelScope.launch {
        try {
            Log.d("SubCategoryDeletion", "Starting subcategory deletion with balance restoration for: ${subCategory.name}")

            //Log subcategory deletion to ActivityLog before deleting
            activityLogUtils.logSubcategoryDeleted(subCategory.name, subCategory.categoryId)

            // Use the enhanced deletion method with balance restoration
            subCategoryRepository.deleteSubCategoryWithTransactionsAndBalanceRestore(
                subCategory,
                transactionRepository,
                accountRepository
            )

            // IMMEDIATE UI UPDATES
            // Refresh data immediately to show updated balances
            fetchSubCategoriesForCategory(subCategory.categoryId)
            fetchAllCategoriesWithSubCategories()

            // Force refresh account balances immediately
            viewModelScope.launch {
                AppEventBus.tryEmitEvent(AccountEvent.AccountsUpdated)
                AppEventBus.tryEmitEvent(TransactionEvent.TransactionsUpdated)
            }

            // Emit event for UI updates
            AppEventBus.tryEmitEvent(CategoryEvent.SubCategoryDeleted(subCategory.categoryId, subCategory.id))

            // Clear dialog states
            dismissAllDeletionDialogs()

            Log.d("SubCategoryDeletion", "Successfully completed subcategory deletion with balance restoration")

        } catch (e: Exception) {
            Log.e("SubCategoryDeletion", "Error deleting subcategory with balance restoration: ${e.message}", e)
            _state.update {
                it.copy(error = "Error deleting subcategory: ${e.message}")
            }
        }
    }
}

    // NEW: Get transaction statistics for category (for UI display)
    fun getTransactionStatsForCategory(categoryId: Int, onResult: (TransactionStats) -> Unit) {
        viewModelScope.launch {
            try {
                val stats = transactionRepository.getTransactionStatsForCategory(categoryId)
                onResult(stats)
            } catch (e: Exception) {
                Log.e("CategoryStats", "Error getting transaction stats for category: ${e.message}", e)
                onResult(TransactionStats(0, 0.0, 0.0, 0.0, 0.0))
            }
        }
    }

    // NEW: Get transaction statistics for subcategory (for UI display)
    fun getTransactionStatsForSubCategory(subCategoryId: Int, onResult: (TransactionStats) -> Unit) {
        viewModelScope.launch {
            try {
                val stats = transactionRepository.getTransactionStatsForSubCategory(subCategoryId)
                onResult(stats)
            } catch (e: Exception) {
                Log.e("SubCategoryStats", "Error getting transaction stats for subcategory: ${e.message}", e)
                onResult(TransactionStats(0, 0.0, 0.0, 0.0, 0.0))
            }
        }
    }

    // Dialog management methods
    private fun dismissAllDeletionDialogs() {
        _state.update {
            it.copy(
                showCategoryDeleteConfirmationDialog = false,
                showCategoryDeleteOptionsDialog = false,
                showCategoryMoveTransactionsDialog = false,
                showCategoryFinalConfirmationDialog = false,
                showSubCategoryDeleteConfirmationDialog = false,
                showSubCategoryDeleteOptionsDialog = false,
                showSubCategoryMoveTransactionsDialog = false,
                showSubCategoryFinalConfirmationDialog = false,
                pendingCategoryDeletion = null,
                pendingSubCategoryDeletion = null,
                selectedTargetCategoryId = null,
                selectedTargetSubCategoryId = null,
                availableCategoriesForMigration = emptyList(),
                availableSubCategoriesForMigration = emptyList()
            )
        }
    }
    fun handleImmediateDeletion() {
        viewModelScope.launch {
            val currentState = _state.value

            // Handle pending category deletion
            currentState.pendingCategoryDeletion?.let { category ->
                try {
                    // Log category deletion to ActivityLog before deleting
                    activityLogUtils.logCategoryDeleted(category.name, category.id)

                    categoryRepository.deleteCategory(category)
                    fetchAllCategories()
                    fetchAllCategoriesWithSubCategories()
                    AppEventBus.tryEmitEvent(CategoryEvent.CategoryDeleted(category.id))
                } catch (e: Exception) {
                    Log.e("ImmediateDeletion", "Error in immediate category deletion: ${e.message}", e)
                }
            }

            // Handle pending subcategory deletion
            currentState.pendingSubCategoryDeletion?.let { subCategory ->
                try {
                    //  Log subcategory deletion to ActivityLog before deleting
                    activityLogUtils.logSubcategoryDeleted(subCategory.name, subCategory.categoryId)
                    subCategoryRepository.deleteSubCategory(subCategory)
                    fetchSubCategoriesForCategory(subCategory.categoryId)
                    fetchAllCategoriesWithSubCategories()
                    AppEventBus.tryEmitEvent(CategoryEvent.SubCategoryDeleted(subCategory.categoryId, subCategory.id))
                } catch (e: Exception) {
                    Log.e("ImmediateDeletion", "Error in immediate subcategory deletion: ${e.message}", e)
                }
            }

            // Clear all dialog states
            dismissAllDeletionDialogs()
        }
    }

    private fun cancelDeletion() {
        dismissAllDeletionDialogs()
    }
    private fun initiateMergeCategory(sourceCategory: CategoryEntity) {
        viewModelScope.launch {
            // Get all categories except the source category and transfer category (id = 1)
            val availableCategories = categoryRepository.getAllCategories()
                .filter { it.id != sourceCategory.id && it.id != 1 }

            _state.update {
                it.copy(
                    showMergeCategoryDialog = true,
                    pendingMergeCategorySource = sourceCategory,
                    availableCategoriesForMerge = availableCategories
                )
            }
        }
    }

    private fun confirmCategoryMerge(sourceCategory: CategoryEntity, targetCategoryId: Int) {
        viewModelScope.launch {
            try {
                Log.d("CategoryMerge", "Starting merge of category '${sourceCategory.name}' to category ID $targetCategoryId")

                // Log category deletion (merge) to ActivityLog before merging
                activityLogUtils.logCategoryDeleted(sourceCategory.name, sourceCategory.id)

                // Use the existing migration method from Problem 1
                categoryRepository.deleteCategoryWithMigration(
                    sourceCategory,
                    targetCategoryId,
                    transactionRepository,
                    subCategoryRepository
                )

                // Refresh data immediately
                fetchAllCategories()
                fetchAllCategoriesWithSubCategories()

                // Force refresh account balances
                viewModelScope.launch {
                    AppEventBus.tryEmitEvent(AccountEvent.AccountsUpdated)
                    AppEventBus.tryEmitEvent(TransactionEvent.TransactionsUpdated)
                }

                // Emit events for UI updates
                AppEventBus.tryEmitEvent(CategoryEvent.CategoryDeleted(sourceCategory.id))
                AppEventBus.tryEmitEvent(CategoryEvent.CategoriesUpdated)

                // Clear dialog states
                cancelMergeCategory()

                Log.d("CategoryMerge", "Successfully completed category merge")

            } catch (e: Exception) {
                Log.e("CategoryMerge", "Error merging category: ${e.message}", e)
                _state.update {
                    it.copy(error = "Error merging category: ${e.message}")
                }
            }
        }
    }

    private fun cancelMergeCategory() {
        _state.update {
            it.copy(
                showMergeCategoryDialog = false,
                showMergeCategoryFinalConfirmationDialog = false,
                pendingMergeCategorySource = null,
                selectedMergeCategoryTargetId = null,
                availableCategoriesForMerge = emptyList()
            )
        }
    }

    // IMPROVED RESET INPUT METHODS
    // Reset input fields for new categories
    private fun resetInputs() {
        _state.update {
            it.copy(
                categoryName = "",
                selectedIconId = null,
                customBoxColor = null,
                customTextColor = null,
                isEditingCategory = false,
                isEditingSubCategory = false,
                isMainCategory = true,
                isSubCategory = false,
                selectedMainCategoryId = null,
                currentCategoryId = null,
                currentSubCategoryId = null
            )
        }
    }

    // Reset input fields specifically for subcategories
    private fun resetInputsForSubCategory() {
        _state.update {
            it.copy(
                categoryName = "",
                selectedIconId = null,
                customBoxColor = null,
                customTextColor = null,
                isEditingCategory = false,
                isEditingSubCategory = false,
                isMainCategory = false,
                isSubCategory = true,
                currentCategoryId = null,
                currentSubCategoryId = null
                // Note: Don't reset selectedMainCategoryId here as it may be set by PrepareForNewSubCategoryInParent
            )
        }
    }

    // Public method to force reset (can be called from UI)
    fun forceResetInputs() {
        resetInputs()
    }
}