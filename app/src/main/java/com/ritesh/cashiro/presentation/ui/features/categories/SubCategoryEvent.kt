package com.ritesh.cashiro.presentation.ui.features.categories

import androidx.compose.ui.graphics.Color
import com.ritesh.cashiro.data.local.entity.SubCategoryEntity

// SubCategoryEvent.kt
//sealed class SubCategoryEvent {
//    // Basic subcategory operations
//    data class PrepareForNewSubCategory(val categoryId: Int) : SubCategoryEvent()
//    data class UpdateSubCategoryName(val name: String) : SubCategoryEvent()
//    data class UpdateSelectedIconId(val iconId: Int) : SubCategoryEvent()
//    data class UpdateCustomBoxColor(val color: Color) : SubCategoryEvent()
//    data class AddSubCategory(val categoryId: Int) : SubCategoryEvent()
//    data class UpdateSubCategory(val subCategory: SubCategoryEntity) : SubCategoryEvent()
//    data class DeleteSubCategory(val subCategory: SubCategoryEntity) : SubCategoryEvent()
//    data class ReorderSubCategories(val fromIndex: Int, val toIndex: Int) : SubCategoryEvent()
//    object ClearSubCategoryFields : SubCategoryEvent()
//
//    // New advanced options events
//    data class SetMergeMode(val enabled: Boolean) : SubCategoryEvent()
//    data class SetConvertToMainMode(val enabled: Boolean) : SubCategoryEvent()
//    data class SelectTargetSubCategoryForMerge(val targetSubCategoryId: Int) : SubCategoryEvent()
//    data class MergeSubCategories(val sourceId: Int, val targetId: Int) : SubCategoryEvent()
//    data class ConvertToMainCategory(
//        val subCategory: SubCategoryEntity,
//        val name: String,
//        val iconId: Int,
//        val boxColor: Color
//    ) : SubCategoryEvent()
//
//    // State management events
//    object ResetAdvancedOptions : SubCategoryEvent()
//    data class FetchSubCategoriesForCategory(val categoryId: Int) : SubCategoryEvent()
//    object RefreshAllData : SubCategoryEvent()
//}