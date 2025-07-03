package com.ritesh.cashiro.presentation.ui.features.categories

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ritesh.cashiro.data.local.entity.CategoryEntity
import com.ritesh.cashiro.data.local.entity.CategoryWithSubCategories
import com.ritesh.cashiro.data.local.entity.SubCategoryEntity
data class CategoryScreenState(
    // Category list state
    val categories: List<CategoryEntity> = emptyList(),
    val categoriesWithSubCategories: List<CategoryWithSubCategories> = emptyList(),

    // Form input state
    val categoryName: String = "",
    val selectedIconId: Int? = null,
    val customBoxColor: Color? = null,
    val customTextColor: Color? = null,

    // Subcategory state
    val isMainCategory: Boolean = true,
    val isSubCategory: Boolean = false,
    val selectedMainCategoryId: Int? = null,
    val subCategories: List<SubCategoryEntity> = emptyList(),

    // UI state
    val isEditingCategory: Boolean = false,
    val isEditingSubCategory: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    // Used for animation and collapsing
    val collapsingFraction: Float = 0f,
    val currentOffset: Dp = 180.dp,

    // Current editing state
    val currentCategoryId: Int? = null,
    val currentSubCategoryId: Int? = null,

    // Enhanced deletion state
    val showCategoryDeleteConfirmationDialog: Boolean = false,
    val showCategoryDeleteOptionsDialog: Boolean = false,
    val showCategoryMoveTransactionsDialog: Boolean = false,
    val showCategoryFinalConfirmationDialog: Boolean = false,

    val showSubCategoryDeleteConfirmationDialog: Boolean = false,
    val showSubCategoryDeleteOptionsDialog: Boolean = false,
    val showSubCategoryMoveTransactionsDialog: Boolean = false,
    val showSubCategoryFinalConfirmationDialog: Boolean = false,

    // Pending deletion entities
    val pendingCategoryDeletion: CategoryEntity? = null,
    val pendingSubCategoryDeletion: SubCategoryEntity? = null,

    // Migration targets
    val selectedTargetCategoryId: Int? = null,
    val selectedTargetSubCategoryId: Int? = null,

    // Available targets for migration
    val availableCategoriesForMigration: List<CategoryEntity> = emptyList(),
    val availableSubCategoriesForMigration: List<SubCategoryEntity> = emptyList(),

    // ADDED: Merge Category states
    val showMergeCategoryDialog: Boolean = false,
    val pendingMergeCategorySource: CategoryEntity? = null,
    val selectedMergeCategoryTargetId: Int? = null,
    val availableCategoriesForMerge: List<CategoryEntity> = emptyList(),
    val showMergeCategoryFinalConfirmationDialog: Boolean = false,
)

/**
 * Sealed class representing all possible events for the Category Screen
 */
sealed class CategoryScreenEvent {
    // Form events
    data class UpdateCategoryName(val name: String) : CategoryScreenEvent()
    data class UpdateSelectedIconId(val iconId: Int?) : CategoryScreenEvent()
    data class UpdateCustomBoxColor(val color: Color?) : CategoryScreenEvent()
    data class UpdateCustomTextColor(val color: Color?) : CategoryScreenEvent()
    data class UpdateIsMainCategory(val isMain: Boolean) : CategoryScreenEvent()
    data class UpdateIsSubCategory(val isSub: Boolean) : CategoryScreenEvent()
    data class UpdateSelectedMainCategoryId(val categoryId: Int?) : CategoryScreenEvent()

    // Category management events
    object PrepareForNewCategory : CategoryScreenEvent()
    object SaveCategory : CategoryScreenEvent()
    data class UpdateExistingCategory(val category: CategoryEntity) : CategoryScreenEvent()
    data class DeleteCategory(val category: CategoryEntity) : CategoryScreenEvent()
    data class GetCategoryById(val id: Int) : CategoryScreenEvent()
    data class OnCategoryFetched(val id: Int, val onSuccess: (CategoryEntity?) -> Unit) : CategoryScreenEvent()

    // Subcategory management events
    object PrepareForNewSubCategory : CategoryScreenEvent()
    data class PrepareForNewSubCategoryInParent(val parentCategoryId: Int) : CategoryScreenEvent()
    data class SubCategoriesUpdated(val categoryId: Int) : CategoryScreenEvent()
    object CategoriesUpdated : CategoryScreenEvent()
    object SaveSubCategory : CategoryScreenEvent()
    data class UpdateExistingSubCategory(val subCategory: SubCategoryEntity) : CategoryScreenEvent()
    data class DeleteSubCategory(val subCategory: SubCategoryEntity) : CategoryScreenEvent()
    data class GetSubCategoryById(val id: Int) : CategoryScreenEvent()
    data class OnSubCategoryFetched(val id: Int, val onSuccess: (SubCategoryEntity?) -> Unit) : CategoryScreenEvent()

    // List events
    data class ReorderCategories(val fromIndex: Int, val toIndex: Int) : CategoryScreenEvent()
    data class ReorderSubCategories(val fromIndex: Int, val toIndex: Int, val categoryId: Int) : CategoryScreenEvent()
    data class UpdateCategoriesList(val categories: List<CategoryEntity>) : CategoryScreenEvent()

    // Data loading events
    object FetchAllCategories : CategoryScreenEvent()
    object FetchAllCategoriesWithSubCategories : CategoryScreenEvent()
    data class FetchSubCategoriesForCategory(val categoryId: Int) : CategoryScreenEvent()

    // Success callbacks
    data class OnCategorySaved(val onSuccess: () -> Unit) : CategoryScreenEvent()
    data class OnCategoryUpdated(val category: CategoryEntity, val onSuccess: () -> Unit) : CategoryScreenEvent()
    data class OnSubCategorySaved(val onSuccess: () -> Unit) : CategoryScreenEvent()
    data class OnSubCategoryUpdated(val subCategory: SubCategoryEntity, val onSuccess: () -> Unit) : CategoryScreenEvent()

    // Enhanced deletion events for categories
    data class InitiateCategoryDeletion(val category: CategoryEntity) : CategoryScreenEvent()
    data class ShowCategoryDeleteOptions(val category: CategoryEntity) : CategoryScreenEvent()
    data class ShowCategoryMoveTransactions(val category: CategoryEntity) : CategoryScreenEvent()
    data class SelectTargetCategoryForMigration(val targetCategoryId: Int) : CategoryScreenEvent()
    data class ConfirmCategoryMigrationAndDeletion(val sourceCategory: CategoryEntity, val targetCategoryId: Int) : CategoryScreenEvent()
    data class ConfirmCategoryDeletionWithTransactions(val category: CategoryEntity) : CategoryScreenEvent()

    // Enhanced deletion events for subcategories
    data class InitiateSubCategoryDeletion(val subCategory: SubCategoryEntity) : CategoryScreenEvent()
    data class ShowSubCategoryDeleteOptions(val subCategory: SubCategoryEntity) : CategoryScreenEvent()
    data class ShowSubCategoryMoveTransactions(val subCategory: SubCategoryEntity) : CategoryScreenEvent()
    data class SelectTargetSubCategoryForMigration(val targetSubCategoryId: Int) : CategoryScreenEvent()
    data class ConfirmSubCategoryMigrationAndDeletion(val sourceSubCategory: SubCategoryEntity, val targetSubCategoryId: Int) : CategoryScreenEvent()
    data class ConfirmSubCategoryDeletionWithTransactions(val subCategory: SubCategoryEntity) : CategoryScreenEvent()

    // Dialog state management
    object DismissAllDeletionDialogs : CategoryScreenEvent()
    object CancelDeletion : CategoryScreenEvent()

    // ADDED: Merge Category events
    data class InitiateMergeCategory(val sourceCategory: CategoryEntity) : CategoryScreenEvent()
    data class SelectTargetCategoryForMerge(val targetCategoryId: Int) : CategoryScreenEvent()
    data class ConfirmCategoryMerge(val sourceCategory: CategoryEntity, val targetCategoryId: Int) : CategoryScreenEvent()
    object CancelMergeCategory : CategoryScreenEvent()
}

/**
 * Interface for handling CategoryScreenEvents
 * This will be implemented by the ViewModel
 */
interface CategoryScreenEventHandler {
    fun onEvent(event: CategoryScreenEvent)
}