package com.ritesh.cashiro.presentation.ui.features.categories

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.ritesh.cashiro.data.local.entity.CategoryWithSubCategories
import com.ritesh.cashiro.data.local.entity.SubCategoryEntity

data class SubCategoryState(
    // Form input state
    val subCategoryName: String = "",
    val selectedIconId: Int? = null,
    val customBoxColor: Color? = null,
    val customTextColor: Color? = null,
    val selectedCategoryId: Int? = null,

    // SubCategory list state
    val subCategories: List<SubCategoryEntity> = emptyList(),
    val categoriesWithSubCategories: List<CategoryWithSubCategories> = emptyList(),

    // Current editing state
    val currentEditingSubCategoryId: Int? = null,
    val isEditingSubCategory: Boolean = false,

    // Advanced options state
    val isMergeMode: Boolean = false,
    val isConvertToMainMode: Boolean = false,
    val selectedTargetSubCategoryId: Int? = null,
    val availableSubCategoriesForMerge: List<SubCategoryEntity> = emptyList(),

    // Deletion state
    val pendingDeletion: SubCategoryEntity? = null,

    // Enhanced deletion state
    val showSubCategoryDeleteConfirmationDialog: Boolean = false,
    val showSubCategoryDeleteOptionsDialog: Boolean = false,
    val showSubCategoryMoveTransactionsDialog: Boolean = false,
    val showSubCategoryFinalConfirmationDialog: Boolean = false,
    val pendingSubCategoryDeletion: SubCategoryEntity? = null,
    val selectedTargetSubCategoryForMigration: Int? = null,
    val availableSubCategoriesForMigration: List<SubCategoryEntity> = emptyList(),

    // Merge confirmation state
    val showMergeConfirmationDialog: Boolean = false,
    val showConvertConfirmationDialog: Boolean = false,

    // Loading and error state
    val isLoading: Boolean = false,
    val error: String? = null,

    // Convert to main category form state
    val newMainCategoryName: String = "",
    val newMainCategoryIconId: Int = 0,
    val newMainCategoryColor: Color = Color.Gray
)

/**
 * Sealed class representing all possible events for SubCategory operations
 */
sealed class SubCategoryEvent {
    // Form input events
    data class UpdateSubCategoryName(val name: String) : SubCategoryEvent()
    data class UpdateSelectedIconId(val iconId: Int?) : SubCategoryEvent()
    data class UpdateCustomBoxColor(val color: Color?) : SubCategoryEvent()
    data class UpdateCustomTextColor(val color: Color?) : SubCategoryEvent()
    data class UpdateSelectedCategoryId(val categoryId: Int?) : SubCategoryEvent()

    // SubCategory management events
    data class PrepareForNewSubCategory(val categoryId: Int) : SubCategoryEvent()
    data class PrepareForEditingSubCategory(val subCategory: SubCategoryEntity) : SubCategoryEvent()
    data class AddSubCategory(val categoryId: Int) : SubCategoryEvent()
    data class UpdateSubCategory(val subCategory: SubCategoryEntity) : SubCategoryEvent()
    data class DeleteSubCategory(val subCategory: SubCategoryEntity) : SubCategoryEvent()
    data class GetSubCategoryById(val id: Int, val onSuccess: (SubCategoryEntity?) -> Unit) : SubCategoryEvent()
    object ClearSubCategoryFields : SubCategoryEvent()

    // List management events
    data class ReorderSubCategories(val fromIndex: Int, val toIndex: Int, val categoryId: Int) : SubCategoryEvent()
    data class UpdateSubCategoriesList(val subCategories: List<SubCategoryEntity>) : SubCategoryEvent()

    // Data loading events
    data class FetchSubCategoriesForCategory(val categoryId: Int) : SubCategoryEvent()
    object FetchAllCategoriesWithSubCategories : SubCategoryEvent()
    object RefreshAllData : SubCategoryEvent()

    // Advanced options events
    data class SetMergeMode(val enabled: Boolean) : SubCategoryEvent()
    data class SetConvertToMainMode(val enabled: Boolean) : SubCategoryEvent()
    data class SelectTargetSubCategoryForMerge(val targetSubCategoryId: Int) : SubCategoryEvent()
    data class MergeSubCategories(val sourceId: Int, val targetId: Int) : SubCategoryEvent()
    data class ConvertToMainCategory(
        val subCategory: SubCategoryEntity,
        val name: String,
        val iconId: Int,
        val boxColor: Color
    ) : SubCategoryEvent()
    object ResetAdvancedOptions : SubCategoryEvent()

    // Enhanced deletion events
    data class InitiateSubCategoryDeletion(val subCategory: SubCategoryEntity) : SubCategoryEvent()
    data class ShowSubCategoryDeleteOptions(val subCategory: SubCategoryEntity) : SubCategoryEvent()
    data class ShowSubCategoryMoveTransactions(val subCategory: SubCategoryEntity) : SubCategoryEvent()
    data class SelectTargetSubCategoryForMigration(val targetSubCategoryId: Int) : SubCategoryEvent()
    data class ConfirmSubCategoryMigrationAndDeletion(
        val sourceSubCategory: SubCategoryEntity,
        val targetSubCategoryId: Int
    ) : SubCategoryEvent()
    data class ConfirmSubCategoryDeletionWithTransactions(val subCategory: SubCategoryEntity) : SubCategoryEvent()

    // Deletion with undo events
    data class DeleteSubCategoryWithUndo(val subCategory: SubCategoryEntity, val categoryId: Int) : SubCategoryEvent()
    object UndoSubCategoryDeletion : SubCategoryEvent()
    object ClearPendingDeletion : SubCategoryEvent()
    object HandleSheetDismissalDuringCountdown : SubCategoryEvent()

    // Dialog state management
    object DismissAllDeletionDialogs : SubCategoryEvent()
    object CancelDeletion : SubCategoryEvent()
    data class ShowMergeConfirmationDialog(val show: Boolean) : SubCategoryEvent()
    data class ShowConvertConfirmationDialog(val show: Boolean) : SubCategoryEvent()

    // Convert to main category form events
    data class UpdateNewMainCategoryName(val name: String) : SubCategoryEvent()
    data class UpdateNewMainCategoryIconId(val iconId: Int) : SubCategoryEvent()
    data class UpdateNewMainCategoryColor(val color: Color) : SubCategoryEvent()

    // Success callback events
    data class OnSubCategorySaved(val onSuccess: () -> Unit) : SubCategoryEvent()
    data class OnSubCategoryUpdated(val subCategory: SubCategoryEntity, val onSuccess: () -> Unit) : SubCategoryEvent()
    data class OnMergeCompleted(val onSuccess: () -> Unit) : SubCategoryEvent()
    data class OnConvertCompleted(val onSuccess: () -> Unit) : SubCategoryEvent()

    // State management events
    object ClearSelection : SubCategoryEvent()
    object ClearError : SubCategoryEvent()
    data class SetLoading(val isLoading: Boolean) : SubCategoryEvent()
    data class SetError(val error: String?) : SubCategoryEvent()
    object ClearSubCategories : SubCategoryEvent()

}

/**
 * Interface for handling SubCategoryEvents
 * This will be implemented by the SubCategoryViewModel
 */
interface SubCategoryEventHandler {
    fun onEvent(event: SubCategoryEvent)
}

/**
 * Data class for transaction statistics (if needed for UI display)
 */
//data class TransactionStats(
//    val count: Int = 0,
//    val totalIncome: Double = 0.0,
//    val totalExpense: Double = 0.0,
//    val netAmount: Double = 0.0,
//    val averageAmount: Double = 0.0
//)

/**
 * Extension functions for SubCategoryState
 */
fun SubCategoryState.isFormValid(): Boolean {
    return subCategoryName.isNotBlank() && selectedCategoryId != null
}

fun SubCategoryState.isAdvancedOptionsEnabled(): Boolean {
    return isMergeMode || isConvertToMainMode
}

fun SubCategoryState.canMerge(): Boolean {
    return isMergeMode && selectedTargetSubCategoryId != null && availableSubCategoriesForMerge.isNotEmpty()
}

fun SubCategoryState.canConvert(): Boolean {
    return isConvertToMainMode && newMainCategoryName.isNotBlank()
}

fun SubCategoryState.hasFormChanges(original: SubCategoryEntity?): Boolean {
    if (original == null) return subCategoryName.isNotBlank()

    return subCategoryName != original.name ||
            selectedIconId != original.subcategoryIconId ||
            customBoxColor?.toArgb() != original.boxColor
}

fun SubCategoryState.hasPendingDeletion(): Boolean {
    return pendingDeletion != null || pendingSubCategoryDeletion != null
}

fun SubCategoryState.isInDeletionProcess(): Boolean {
    return showSubCategoryDeleteConfirmationDialog ||
            showSubCategoryDeleteOptionsDialog ||
            showSubCategoryMoveTransactionsDialog ||
            showSubCategoryFinalConfirmationDialog
}

/**
 * Helper function to create a SubCategoryEntity from current state
 */
fun SubCategoryState.toSubCategoryEntity(
    categoryId: Int,
    position: Int,
    id: Int = 0
): SubCategoryEntity {
    return SubCategoryEntity(
        id = id,
        name = subCategoryName,
        categoryId = categoryId,
        boxColor = customBoxColor?.toArgb(),
        subcategoryIconId = selectedIconId,
        position = position
    )
}

/**
 * Helper function to reset form fields
 */
fun SubCategoryState.resetForm(): SubCategoryState {
    return copy(
        subCategoryName = "",
        selectedIconId = null,
        customBoxColor = null,
        customTextColor = null,
        currentEditingSubCategoryId = null,
        isEditingSubCategory = false,
        isMergeMode = false,
        isConvertToMainMode = false,
        selectedTargetSubCategoryId = null,
        availableSubCategoriesForMerge = emptyList(),
        newMainCategoryName = "",
        newMainCategoryIconId = 0,
        newMainCategoryColor = Color.Gray
    )
}

/**
 * Helper function to reset advanced options
 */
fun SubCategoryState.resetAdvancedOptions(): SubCategoryState {
    return copy(
        isMergeMode = false,
        isConvertToMainMode = false,
        selectedTargetSubCategoryId = null,
        availableSubCategoriesForMerge = emptyList(),
        showMergeConfirmationDialog = false,
        showConvertConfirmationDialog = false
    )
}

/**
 * Helper function to dismiss all deletion dialogs
 */
fun SubCategoryState.dismissAllDeletionDialogs(): SubCategoryState {
    return copy(
        showSubCategoryDeleteConfirmationDialog = false,
        showSubCategoryDeleteOptionsDialog = false,
        showSubCategoryMoveTransactionsDialog = false,
        showSubCategoryFinalConfirmationDialog = false,
        pendingSubCategoryDeletion = null,
        selectedTargetSubCategoryForMigration = null,
        availableSubCategoriesForMigration = emptyList()
    )
}