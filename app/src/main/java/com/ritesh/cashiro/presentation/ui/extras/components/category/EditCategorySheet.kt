package com.ritesh.cashiro.presentation.ui.extras.components.category

import EditSubCategorySheet
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BottomSheetDefaults.DragHandle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritesh.cashiro.R
import com.ritesh.cashiro.data.local.entity.CategoryEntity
import com.ritesh.cashiro.data.local.entity.SubCategoryEntity
import com.ritesh.cashiro.domain.repository.TransactionStats
import com.ritesh.cashiro.domain.utils.AppEventBus
import com.ritesh.cashiro.domain.utils.CategoryEvent
import com.ritesh.cashiro.domain.utils.icons
import com.ritesh.cashiro.presentation.effects.BlurredAnimatedVisibility
import com.ritesh.cashiro.presentation.ui.extras.components.extras.CategoryDeletionDialogs
import com.ritesh.cashiro.presentation.ui.features.categories.CategoryScreenEvent
import com.ritesh.cashiro.presentation.ui.features.categories.CategoryScreenState
import com.ritesh.cashiro.presentation.ui.features.categories.CategoryScreenViewModel
import com.ritesh.cashiro.presentation.ui.features.categories.SubCategoryEvent
import com.ritesh.cashiro.presentation.ui.features.categories.SubCategoryState
import com.ritesh.cashiro.presentation.ui.features.categories.SubCategoryViewModel
import com.ritesh.cashiro.presentation.ui.theme.iosFont
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCategorySheet(
    categoryUiState: CategoryScreenState,
    onCategoryEvent: (CategoryScreenEvent) -> Unit,
    subCategoryUiState: SubCategoryState,
    onSubCategoryEvent: (SubCategoryEvent) -> Unit,
    getTransactionStatsForCategory: (categoryId: Int, onResult: (TransactionStats) -> Unit) -> Unit,
    getTransactionStatsForSubCategory: (subCategoryId: Int, onResult: (TransactionStats) -> Unit) -> Unit,
    sheetState: SheetState,
    iconSheetState: SheetState,
    isSheetOpen: MutableState<Boolean>,
    isIconSheetOpen: MutableState<Boolean>,
    selectedIconId: Int,
    customColor: Color,
    categoryName: String,
    isSpecialCategory: Boolean = false,
    usedScreenWidth: Dp,
    usedScreenHeight: Dp,
    isUpdate: Boolean = false,
    currentCategoryEntity: CategoryEntity? = null,
){
    val themeColors = MaterialTheme.colorScheme
//    val state by categoryViewModel.state.collectAsState()
    val selectedIconIdState = remember(currentCategoryEntity?.id, selectedIconId) {
        mutableIntStateOf(selectedIconId)
    }
    val customColorState = remember(currentCategoryEntity?.id, customColor) {
        mutableStateOf(customColor)
    }
    val categoryNameState = remember(currentCategoryEntity?.id, categoryName) {
        mutableStateOf(categoryName)
    }

    LaunchedEffect(currentCategoryEntity, isUpdate) {
        if (isUpdate && currentCategoryEntity != null) {
            // Set initial values from the existing category
            selectedIconIdState.intValue = currentCategoryEntity.categoryIconId
            customColorState.value = Color(currentCategoryEntity.boxColor)
            categoryNameState.value = currentCategoryEntity.name

            // Also update the ViewModel state
            onCategoryEvent(CategoryScreenEvent.UpdateSelectedIconId(currentCategoryEntity.categoryIconId))
            onCategoryEvent(CategoryScreenEvent.UpdateCustomBoxColor(Color(currentCategoryEntity.boxColor)))
            onCategoryEvent(CategoryScreenEvent.UpdateCustomTextColor(Color(currentCategoryEntity.textColor)))
            onCategoryEvent(CategoryScreenEvent.UpdateCategoryName(currentCategoryEntity.name))
        }
    }

    // Add state for main category toggle - enabled by default for new categories
    val isMainCategoryState = remember { mutableStateOf(!isUpdate) }

    // State for subcategory toggle
    val isSubCategoryState = remember { mutableStateOf(false) }
    val selectedMainCategoryId = remember { mutableStateOf<Int?>(null) }

    // State variables for subcategory sheet
    val subCategorySheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val subCategoryIconSheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSubCategorySheet = remember { mutableStateOf(false) }
    var showSubCategoryIconSheet = remember { mutableStateOf(false) }
    var selectedSubCategory by remember { mutableStateOf<SubCategoryEntity?>(null) }
    var isNewSubCategory by remember { mutableStateOf(false) }

    // Collect categories for the main category selection
    val categories = categoryUiState.categories
    val subCategories = subCategoryUiState.subCategories

    LaunchedEffect(currentCategoryEntity?.id) {
        if (isUpdate && currentCategoryEntity != null) {
            onSubCategoryEvent(SubCategoryEvent.FetchSubCategoriesForCategory(currentCategoryEntity.id))

            // Listen for subcategory events specific to this category
            AppEventBus.events.collect { event ->
                when (event) {
                    is CategoryEvent.SubCategoryAdded -> {
                        if (event.categoryId == currentCategoryEntity.id) {
                            // Refresh subcategories when a new one is added
                            onSubCategoryEvent(SubCategoryEvent.FetchSubCategoriesForCategory(currentCategoryEntity.id))
                        }
                    }
                    is CategoryEvent.SubCategoriesUpdated -> {
                        if (event.categoryId == currentCategoryEntity.id) {
                            // Refresh subcategories when they're updated
                            onSubCategoryEvent(SubCategoryEvent.FetchSubCategoriesForCategory(currentCategoryEntity.id))
                        }
                    }
                    is CategoryEvent.SubCategoryDeleted -> {
                        if (event.categoryId == currentCategoryEntity.id) {
                            // Refresh subcategories when one is deleted
                            onSubCategoryEvent(SubCategoryEvent.FetchSubCategoriesForCategory(currentCategoryEntity.id))
                        }
                    }
                    is CategoryEvent.SubCategoryUpdated -> {
                        if (event.categoryId == currentCategoryEntity.id) {
                            // Refresh subcategories when one is updated
                            onSubCategoryEvent(SubCategoryEvent.FetchSubCategoriesForCategory(currentCategoryEntity.id))
                        }
                    }
                }
            }
        }
    }
// Handle dismissal during deletion process
    LaunchedEffect(isSheetOpen.value) {
        if (!isSheetOpen.value) {
            // When sheet is dismissed, check if there's a pending deletion and handle it
            if (categoryUiState.pendingSubCategoryDeletion != null ||
                categoryUiState.pendingCategoryDeletion != null ||
                categoryUiState.showSubCategoryDeleteConfirmationDialog ||
                categoryUiState.showCategoryDeleteConfirmationDialog) {

                // Immediately clear all deletion dialogs and proceed with deletion if needed
                onCategoryEvent(CategoryScreenEvent.DismissAllDeletionDialogs)

                // If there was a pending countdown, immediately delete the item
                val pendingSubCategory = subCategoryUiState.pendingDeletion
                if (pendingSubCategory != null) {
                    onSubCategoryEvent(SubCategoryEvent.ClearPendingDeletion)
                }
            }
        }
    }

    if (isSheetOpen.value){
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = {
                isSheetOpen.value = false
                // Clear any pending deletions when sheet is dismissed
                onCategoryEvent(CategoryScreenEvent.DismissAllDeletionDialogs)
            },
            sheetMaxWidth = usedScreenWidth - 10.dp,
            dragHandle = {
                DragHandle(
                    color = MaterialTheme.colorScheme.inverseSurface.copy(0.3f)
                )
            },
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            containerColor = themeColors.background,
            contentColor = themeColors.inverseSurface
        ) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(usedScreenHeight)
                .navigationBarsPadding()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item(key = "Header text") {
                        Text(
                            text = if (isUpdate) "Edit Category" else "Add Category",
                            textAlign = TextAlign.Center,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = iosFont,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 25.dp)
                        )
                    }

                    item(key = "Preview of Category card"){
                        PreviewCategoryCard(
                            selectedIconIdState = selectedIconIdState,
                            customColorState = customColorState,
                            categoryNameState = categoryNameState,
                            categoryName = categoryName,
                            customColor = customColor,
                            isIconSheetOpen = isIconSheetOpen,
                            themeColors = themeColors,
                            isSpecialCategory = isSpecialCategory
                        )
                    }

                    item(key = "category details input section") {
                        CategoryFillDetailsInput(
                            onCategoryEvent = onCategoryEvent,
                            categoryNameState = categoryNameState,
                            categoryName = categoryName,
                            isIconSheetOpen = isIconSheetOpen,
                            themeColors = themeColors
                        )
                    }

                    item(key = "Category Card color selection") {
                        CategoryColorSelection(
                            themeColors = themeColors,
                            customColorState = customColorState,
                            selectedIconIdState = selectedIconIdState,
                            onCategoryEvent = onCategoryEvent
                        )
                    }

                    // Only show category type selection for new categories
                    if (!isUpdate) {
                        item(key = "Main category toggle") {
                            MainCategoryToggle(
                                isMainCategoryState = isMainCategoryState,
                                isSubCategoryState = isSubCategoryState,
                                themeColors = themeColors,
                                onCategoryEvent = onCategoryEvent
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Only show subcategory selection if Main category is not enabled
                            if (!isMainCategoryState.value) {
                                SubcategoryToggleAndSelection(
                                    isSubCategoryState = isSubCategoryState,
                                    selectedMainCategoryId = selectedMainCategoryId,
                                    categories = categories,
                                    themeColors = themeColors,
                                    onSubcategoryToggled = { isSubcategory ->
                                        // If subcategory is enabled, disable Main category
                                        if (isSubcategory) {
                                            isMainCategoryState.value = false
                                        }
                                    },

                                    onCategoryEvent = onCategoryEvent
                                )
                            }
                        }
                    } else {

                        item(key = "Category management") {
                            if (isUpdate && currentCategoryEntity != null) {
                                Column {
                                    // Merge Category Button
                                    Button(
                                        onClick = {
                                            onCategoryEvent(
                                                CategoryScreenEvent.InitiateMergeCategory(currentCategoryEntity)
                                            )
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = themeColors.surface,
                                            contentColor = themeColors.inverseSurface
                                        ),
                                        shape = RoundedCornerShape(16.dp),
                                        contentPadding = PaddingValues(vertical = 12.dp, horizontal = 21.dp)
                                    ) {
                                        Column(
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.Start
                                        ) {
                                            Text(
                                                text = "Merge Category",
                                                fontSize = 16.sp,
                                                fontFamily = iosFont,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Merge this category and move all transactions of this category with another category",
                                                fontSize = 12.sp,
                                                fontFamily = iosFont,
                                                color = themeColors.inverseSurface.copy(alpha = 0.7f),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))

                                    SubcategoriesListItem(
                                        subCategories = subCategories,
                                        onMove = { fromIndex, toIndex ->
                                            onSubCategoryEvent(
                                                SubCategoryEvent.ReorderSubCategories(
                                                    fromIndex = fromIndex,
                                                    toIndex = toIndex,
                                                    categoryId = currentCategoryEntity?.id ?: -1
                                                )
                                            )
                                        },
                                        themeColors = themeColors,
                                        onSubCategoryClick = { subCategory ->
                                            onCategoryEvent(
                                                CategoryScreenEvent.GetSubCategoryById(
                                                    subCategory.id
                                                )
                                            )
                                            selectedSubCategory = subCategory
                                            isNewSubCategory = false
                                            showSubCategorySheet.value = true
                                        },
                                        onAddNewSubCategory = {
                                            onCategoryEvent(
                                                CategoryScreenEvent.PrepareForNewSubCategoryInParent(
                                                    parentCategoryId = currentCategoryEntity?.id
                                                        ?: -1
                                                )
                                            )
                                            isNewSubCategory = true
                                            selectedSubCategory = null
                                            showSubCategorySheet.value = true
                                        },
                                        categoryUiState = categoryUiState,
                                        onCategoryEvent = onCategoryEvent,
                                        subCategoryUiState = subCategoryUiState,
                                        onSubCategoryEvent = onSubCategoryEvent,
                                        getTransactionStatsForCategory = getTransactionStatsForCategory,
                                        getTransactionStatsForSubCategory = getTransactionStatsForSubCategory,
                                    )
                                }
                            }
                        }
                    }

                    item(key = "End Space for better scrolling view") {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
                AddCategoryBottomButton(
                    onCategoryEvent = onCategoryEvent,
                    sheetState = sheetState,
                    iconSheetState = iconSheetState,
                    isSheetOpen = isSheetOpen,
                    isIconSheetOpen = isIconSheetOpen,
                    selectedIconId = selectedIconIdState.intValue, // Use current state value
                    customColor = customColorState.value, // Use current state value
                    categoryName = categoryNameState.value, // Use current state value
                    usedScreenWidth = usedScreenWidth,
                    usedScreenHeight = usedScreenHeight,
                    isUpdate = isUpdate,
                    currentCategoryEntity = currentCategoryEntity,
                    selectedIconIdState = selectedIconIdState,
                    customColorState = customColorState,
                    categoryNameState = categoryNameState,
                    isMainCategoryState = isMainCategoryState,
                    isSubCategoryState = isSubCategoryState,
                    selectedMainCategoryId = selectedMainCategoryId,
                    themeColors = themeColors,
                    modifier = Modifier.fillMaxWidth().imePadding().align(Alignment.BottomCenter)
                        .padding(bottom = 5.dp),
                )
            }
        }

        CategoryDeletionDialogs(
            state = categoryUiState,
            onEvent = onCategoryEvent,
            themeColors = themeColors,
            getTransactionStatsForCategory = getTransactionStatsForCategory,
            getTransactionStatsForSubCategory = getTransactionStatsForSubCategory,
        )
    }

    if (showSubCategorySheet.value) {
        EditSubCategorySheet(
            showSubCategorySheet = showSubCategorySheet,
            showSubCategoryIconSheet = showSubCategoryIconSheet,
            subCategorySheet = subCategorySheet,
            subCategoryIconSheet = subCategoryIconSheet,
            subCategoryId = selectedSubCategory?.id,
            parentCategoryId = if (isNewSubCategory) currentCategoryEntity?.id else null,
            isNewSubCategory = isNewSubCategory,
            usedScreenWidth = usedScreenWidth,
            usedScreenHeight = usedScreenHeight,
            onDismiss = {
                showSubCategorySheet.value = false
                // FIX: Force refresh subcategories when the sheet is dismissed
                currentCategoryEntity?.let { entity ->
                    onSubCategoryEvent(
                        SubCategoryEvent.FetchSubCategoriesForCategory(
                            entity.id
                        )
                    )
                }
            },
            categoryUiState = categoryUiState,
            onCategoryEvent = onCategoryEvent,
            subCategoryUiState = subCategoryUiState,
            onSubCategoryEvent = onSubCategoryEvent,
            getTransactionStatsForCategory = getTransactionStatsForCategory,
            getTransactionStatsForSubCategory = getTransactionStatsForSubCategory
        )
    }
}

@Composable
private fun PreviewCategoryCard(
    selectedIconIdState: MutableIntState,
    customColorState:  MutableState<Color>,
    categoryNameState: MutableState<String>,
    categoryName: String,
    customColor: Color,
    isIconSheetOpen: MutableState<Boolean>,
    themeColors: ColorScheme,
    isSpecialCategory: Boolean
){
    Column(verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.CenterHorizontally) {

        Box(){
            Spacer(modifier = Modifier
                .height(120.dp)
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .background(
                    themeColors.surface.copy(0.2f),
                    shape = RoundedCornerShape(20.dp)
                )
            )
            Spacer(modifier = Modifier
                .height(110.dp)
                .padding(horizontal = 10.dp)
                .fillMaxWidth()
                .background(
                    themeColors.surface.copy(0.5f),
                    shape = RoundedCornerShape(20.dp)
                )
            )
            Box(
                modifier = Modifier
                    .height(100.dp)
                    .fillMaxWidth()
                    .background(
                        themeColors.surface,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(20.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .shadow(10.dp, RoundedCornerShape(20.dp))
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                color = if (customColorState.value == Color.Transparent) themeColors.onBackground else customColorState.value,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { isIconSheetOpen.value = true }
                            .padding(10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        selectedIconIdState.intValue.let { iconId ->
                            if(iconId != 0){
                                Image(
                                    painter = painterResource(
                                        id = icons.find { it.id == iconId }?.resourceId ?: R.drawable.type_beverages_beer),
                                    contentDescription = "Selected Icon",
                                    modifier = Modifier.size(40.dp)
                                )
                            } else {
                                Icon(
                                    painter = painterResource(R.drawable.category_bulk),
                                    contentDescription = "Default Icon",
                                    modifier = Modifier.size(30.dp),
                                    tint = themeColors.inverseSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                    Column(modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                        verticalArrangement = Arrangement.Center) {
                        Text(
                            text = "My Transaction",
                            fontFamily = iosFont,
                            maxLines = 1,
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp,
                            color = themeColors.inversePrimary,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = if (categoryNameState.value.isBlank() || categoryNameState.value == "") {
                                if (categoryName.isBlank() || categoryName == "") "Category Name" else categoryName
                            } else {
                                categoryNameState.value
                            },
                            fontFamily = iosFont,
                            fontWeight = FontWeight.Normal,
                            fontSize = 10.sp,
                            lineHeight = 10.sp,
                            color = themeColors.inverseSurface,
                            modifier = Modifier
                                .padding(top = 5.dp)
                                .background(
                                    color = if (customColorState.value == Color.Transparent) themeColors.onBackground.copy(
                                        0.5f
                                    ) else customColor.copy(0.5f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(5.dp)
                        )
                    }
                    Text(
                        text = "$100",
                        textAlign = TextAlign.End,
                        fontFamily = iosFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = themeColors.onError,
                        modifier = Modifier.weight(0.5f)
                    )
                }
            }
        }
        BlurredAnimatedVisibility(
            visible = isSpecialCategory
        ) {
            SpecialCategoryInfoCard()
        }
        Text(
            text = "Preview",
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            color = themeColors.inverseOnSurface.copy(alpha = 0.85f),
            fontFamily = iosFont,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}
@Composable
private fun SpecialCategoryInfoCard(){
    Box(modifier = Modifier.padding(vertical = 10.dp).fillMaxWidth().background(MaterialTheme.colorScheme.onError.copy(0.3f), RoundedCornerShape(15.dp)).border(0.dp, MaterialTheme.colorScheme.onError, RoundedCornerShape(15.dp)).padding(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.information_bulk),
                contentDescription = "Special Category Info",
                tint = MaterialTheme.colorScheme.onError,
            )
            Text(
                text = "This Category is only used when you transfer amount from one account to another. Transactions in this category do not contribute to total income and expense calculations but factor in net total spending. Feel free to customize this category as you like.",
                textAlign = TextAlign.Start,
                fontSize = 12.sp,
                fontFamily = iosFont,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onError,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
@Composable
private fun CategoryFillDetailsInput(
    onCategoryEvent: (CategoryScreenEvent) -> Unit,
    categoryNameState: MutableState<String>,
    categoryName: String,
    isIconSheetOpen: MutableState<Boolean>,
    themeColors: ColorScheme
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(themeColors.surface, shape = RoundedCornerShape(20.dp))
                .clickable { isIconSheetOpen.value = true }
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.category_bulk),
                contentDescription = "Color Picker",
                modifier = Modifier
                    .size(30.dp),
                tint = themeColors.inverseSurface.copy(0.5f) // Disable tinting
            )
        }
        Spacer(modifier = Modifier.size(10.dp))
        TextField(
            value = categoryNameState.value,
            onValueChange = {
                categoryNameState.value = it
                onCategoryEvent(CategoryScreenEvent.UpdateCategoryName(it))
            },
            placeholder = {
                Text(
                    text = if (categoryNameState.value.isBlank() || categoryNameState.value == "") {
                        if (categoryName.isBlank() || categoryName == "") "Category Name" else categoryName
                    } else {
                        categoryNameState.value
                    },
                    style = TextStyle(
                        textAlign = TextAlign.Start,
                        fontSize = 12.sp,
                        fontFamily = iosFont
                    ),
                    modifier = Modifier
                        .height(64.dp)
                        .fillMaxWidth(),
                )
            },
            trailingIcon = {Icon(painter = painterResource(R.drawable.edit_name_bulk), tint = themeColors.inverseSurface.copy(0.5f), contentDescription = null)},
            modifier = Modifier
                .height(64.dp)
                .fillMaxWidth()
                .weight(1f)
                .background(
                    themeColors.surface,
                    RoundedCornerShape(20.dp)
                ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                keyboardType = KeyboardType.Text),
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedPlaceholderColor = themeColors.inverseSurface.copy(0.5f),
                unfocusedPlaceholderColor = themeColors.inverseOnSurface.copy(0.5f),
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent
            ),
            textStyle = TextStyle(fontSize = 25.sp)
        )
    }
}

@Composable
private fun CategoryColorSelection(
    onCategoryEvent: (CategoryScreenEvent) -> Unit,
    themeColors: ColorScheme,
    customColorState: MutableState<Color>,
    selectedIconIdState: MutableIntState
){
    Column(verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Box Color",
            fontSize = 14.sp,
            color = themeColors.inverseOnSurface.copy(alpha = 0.85f),
            fontFamily = iosFont,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    themeColors.surface,
                    shape = RoundedCornerShape(20.dp)
                )
        ) {
            CategoryBoxColorPicker(
                isDarkTheme = isSystemInDarkTheme(),
                initialColor = customColorState.value,
                onPrimaryColorSelected = { selectedColor ->
                    // FIX: Update both local state and ViewModel state immediately
                    customColorState.value = selectedColor
                    onCategoryEvent(CategoryScreenEvent.UpdateCustomBoxColor(selectedColor))
                    onCategoryEvent(CategoryScreenEvent.UpdateCustomTextColor(themeColors.inverseSurface))

                    // Force recomposition by triggering state change
                    selectedIconIdState.intValue = selectedIconIdState.intValue
                }
            )
        }
        Spacer(modifier = Modifier.size(50.dp))
    }
}
@Composable
private fun MainCategoryToggle(
    isMainCategoryState: MutableState<Boolean>,
    isSubCategoryState: MutableState<Boolean>,
    themeColors: ColorScheme,
    onCategoryEvent: (CategoryScreenEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = themeColors.surface, shape = RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Main category",
                fontSize = 16.sp,
                color = themeColors.inverseSurface,
                fontFamily = iosFont,
                fontWeight = FontWeight.Medium
            )

            Switch(
                checked = isMainCategoryState.value,
                onCheckedChange = { checked ->
                    isMainCategoryState.value = checked
                    onCategoryEvent(CategoryScreenEvent.UpdateIsMainCategory(checked))
                    if (checked) {
                        // If Main Category is enabled, disable and clear Subcategory selection
                        isSubCategoryState.value = false
                        onCategoryEvent(CategoryScreenEvent.UpdateIsSubCategory(false))
                    }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = themeColors.primary,
                    checkedBorderColor = themeColors.primary,
                    uncheckedThumbColor = themeColors.inverseSurface.copy(0.5f),
                    uncheckedTrackColor = themeColors.background,
                    uncheckedBorderColor = Color.Transparent
                )
            )
        }

        Text(
            text = "This will be a top-level category",
            fontSize = 12.sp,
            color = themeColors.inverseOnSurface.copy(alpha = 0.7f),
            fontFamily = iosFont,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SubcategoryToggleAndSelection(
    isSubCategoryState: MutableState<Boolean>,
    selectedMainCategoryId: MutableState<Int?>,
    categories: List<CategoryEntity>,
    themeColors: ColorScheme,
    onCategoryEvent: (CategoryScreenEvent) -> Unit,
    onSubcategoryToggled: (Boolean) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .then(
                if (isSubCategoryState.value) {
                    Modifier.border(1.dp, themeColors.inverseSurface.copy(0.5f), RoundedCornerShape(16.dp))
                } else {
                    Modifier
                }
            )
            .fillMaxWidth()
            .background(color = themeColors.surface, shape = RoundedCornerShape(16.dp))
            .clickable(
                onClick = {
                    if (!isSubCategoryState.value) {
                        isSubCategoryState.value = !isSubCategoryState.value
                        onCategoryEvent(CategoryScreenEvent.UpdateIsSubCategory(isSubCategoryState.value))
                        onSubcategoryToggled(isSubCategoryState.value)
                    }
                },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
    ) {
        // Subcategory toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Make as Subcategory",
                        fontSize = 16.sp,
                        color = themeColors.inverseSurface,
                        fontFamily = iosFont,
                        fontWeight = FontWeight.Medium
                    )

                    Switch(
                        checked = isSubCategoryState.value,
                        onCheckedChange = { isEnabled ->
                            isSubCategoryState.value = isEnabled
                            onCategoryEvent(CategoryScreenEvent.UpdateIsSubCategory(isEnabled))
                            onSubcategoryToggled(isEnabled)
                            if (!isEnabled) {
                                selectedMainCategoryId.value = null
                                onCategoryEvent(CategoryScreenEvent.UpdateSelectedMainCategoryId(null))
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = themeColors.primary,
                            checkedBorderColor = themeColors.primary,
                            uncheckedThumbColor = themeColors.inverseSurface.copy(0.5f),
                            uncheckedTrackColor = themeColors.background,
                            uncheckedBorderColor = Color.Transparent
                        )
                    )
                }

                if (isSubCategoryState.value) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "Select Parent Category",
                            fontSize = 12.sp,
                            color = themeColors.inverseOnSurface.copy(alpha = 0.85f),
                            fontFamily = iosFont,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    themeColors.surface,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .padding(vertical = 10.dp)
                        ) {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                categories.forEach { category ->
                                    CategoryChip(
                                        category = category,
                                        isSelected = selectedMainCategoryId.value == category.id,
                                        onSelected = {
                                            // Toggle logic: if selecting the same category again, deselect it
                                            if (selectedMainCategoryId.value == category.id) {
                                                // Deselect this category
                                                selectedMainCategoryId.value = null
                                                onCategoryEvent(CategoryScreenEvent.UpdateSelectedMainCategoryId(null))
                                                isSubCategoryState.value = false
                                                onCategoryEvent(CategoryScreenEvent.UpdateIsSubCategory(false))
                                                onSubcategoryToggled(false)
                                            } else {
                                                // Select this category
                                                selectedMainCategoryId.value = category.id
                                                onCategoryEvent(CategoryScreenEvent.UpdateSelectedMainCategoryId(category.id))
                                                isSubCategoryState.value = true
                                                onCategoryEvent(CategoryScreenEvent.UpdateIsSubCategory(true))
                                                onSubcategoryToggled(true)
                                            }
                                        },
                                        themeColors = themeColors
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Composable for the individual category chips
@Composable
private fun CategoryChip(
    category: CategoryEntity,
    isSelected: Boolean,
    onSelected: () -> Unit,
    themeColors: ColorScheme
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(
                if (isSelected) Color(category.boxColor) else themeColors.surfaceVariant
            )
            .clickable { onSelected() }
            .padding(6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color.White.copy(alpha = 0.2f) else Color(category.boxColor)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(
                        id = icons.find { it.id == category.categoryIconId }?.resourceId
                            ?: R.drawable.type_beverages_beer
                    ),
                    contentDescription = "Category Icon",
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = category.name,
                color = if (isSelected) Color.White else themeColors.inverseSurface,
                fontSize = 12.sp,
                fontFamily = iosFont,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCategoryBottomButton(
    onCategoryEvent: (CategoryScreenEvent) -> Unit,
    sheetState: SheetState,
    iconSheetState: SheetState,
    isSheetOpen: MutableState<Boolean>,
    isIconSheetOpen: MutableState<Boolean>,
    selectedIconId: Int,
    customColor: Color,
    categoryName: String,
    usedScreenWidth: Dp,
    usedScreenHeight: Dp,
    isUpdate: Boolean = false,
    currentCategoryEntity: CategoryEntity? = null,
    selectedIconIdState: MutableIntState,
    customColorState: MutableState<Color>,
    categoryNameState: MutableState<String>,
    isMainCategoryState: MutableState<Boolean>,
    isSubCategoryState: MutableState<Boolean>,
    selectedMainCategoryId: MutableState<Int?>,
    themeColors: ColorScheme,
    modifier: Modifier
){
    Box(modifier = modifier, contentAlignment = Alignment.BottomCenter) {
        val scope = rememberCoroutineScope()
        val context = LocalContext.current

        // State for toast handling
        var showToast by remember { mutableStateOf(false) }
        var toastMessage by remember { mutableStateOf("") }

        // Show toast when needed
        LaunchedEffect(showToast) {
            if (showToast) {
                Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
                showToast = false
            }
        }

        val saveCategory = {
            // Update all state values in the ViewModel first
            onCategoryEvent(CategoryScreenEvent.UpdateCategoryName(categoryNameState.value))
            onCategoryEvent(CategoryScreenEvent.UpdateSelectedIconId(selectedIconIdState.intValue))
            onCategoryEvent(CategoryScreenEvent.UpdateCustomBoxColor(customColorState.value))
            onCategoryEvent(CategoryScreenEvent.UpdateCustomTextColor(themeColors.inverseSurface))
            onCategoryEvent(CategoryScreenEvent.UpdateIsMainCategory(isMainCategoryState.value))
            onCategoryEvent(CategoryScreenEvent.UpdateIsSubCategory(isSubCategoryState.value))
            onCategoryEvent(CategoryScreenEvent.UpdateSelectedMainCategoryId(selectedMainCategoryId.value))

            // Save category using the onEvent pattern

            onCategoryEvent(CategoryScreenEvent.OnCategorySaved(
                onSuccess = {
                    scope.launch { sheetState.hide() }
                        .invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                isSheetOpen.value = false

                                // Set toast message and trigger toast
                                if (selectedIconIdState.intValue != 0 || customColorState.value != Color.Transparent || categoryNameState.value.isNotEmpty()) {
                                    toastMessage = if (isSubCategoryState.value) "Subcategory Added" else "Category Added"
                                    showToast = true
                                }
                            }
                        }
                }
            ))
        }

        // Define the update logic
        val updateCategory = {
            // Update all state values in the ViewModel first
            onCategoryEvent(CategoryScreenEvent.UpdateCategoryName(categoryNameState.value))
            onCategoryEvent(CategoryScreenEvent.UpdateSelectedIconId(selectedIconIdState.intValue))
            onCategoryEvent(CategoryScreenEvent.UpdateCustomBoxColor(customColorState.value))
            onCategoryEvent(CategoryScreenEvent.UpdateCustomTextColor(themeColors.inverseSurface))

            if (isSubCategoryState.value && selectedMainCategoryId.value != null && currentCategoryEntity != null) {
                // Convert a category to subcategory
                onCategoryEvent(CategoryScreenEvent.UpdateSelectedMainCategoryId(selectedMainCategoryId.value))
                onCategoryEvent(CategoryScreenEvent.UpdateIsSubCategory(true))
                onCategoryEvent(CategoryScreenEvent.UpdateIsMainCategory(false))

                // Create new subcategory and then delete the original category
                onCategoryEvent(CategoryScreenEvent.OnCategorySaved(
                    onSuccess = {
                        // Delete the original category after successfully creating subcategory
                        onCategoryEvent(CategoryScreenEvent.DeleteCategory(currentCategoryEntity))

                        scope.launch { sheetState.hide() }
                            .invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    isSheetOpen.value = false

                                    // Set toast message and trigger toast
                                    toastMessage = "Converted to Subcategory"
                                    showToast = true
                                }
                            }
                    }
                ))
            } else {
                // Update existing category
                currentCategoryEntity?.let { entity ->
                    onCategoryEvent(CategoryScreenEvent.OnCategoryUpdated(
                        category = entity,
                        onSuccess = {
                            scope.launch { sheetState.hide() }
                                .invokeOnCompletion {
                                    if (!sheetState.isVisible) {
                                        isSheetOpen.value = false

                                        // Set toast message and trigger toast
                                        if (selectedIconIdState.intValue != selectedIconId ||
                                            customColorState.value != customColor ||
                                            categoryNameState.value != categoryName) {
                                            toastMessage = "Category Updated"
                                            showToast = true
                                        }
                                    }
                                }
                        }
                    ))
                } ?: run {
                    // Handle the case where the category is null
                    toastMessage = "Category not found"
                    showToast = true
                }
            }
        }

        Button(
            onClick = if (isUpdate) updateCategory else saveCategory,
            modifier = Modifier
                .height(50.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedIconIdState.intValue == 0 ||
                    customColorState.value == Color.Transparent ||
                    categoryNameState.value.isEmpty() ||
                    (isSubCategoryState.value && selectedMainCategoryId.value == null)) themeColors.surfaceBright
                else themeColors.primary,
                contentColor = themeColors.inverseSurface
            )
        ) {
            Text(
                text = if (isUpdate) {
                    if (isSubCategoryState.value) "Convert to Subcategory" else "Save Category"
                } else {
                    if (isSubCategoryState.value) "Add Subcategory" else "Add Category"
                },
                fontFamily = iosFont,
                fontWeight = FontWeight.Medium,
                color = if (selectedIconIdState.intValue == 0 ||
                    customColorState.value == Color.Transparent ||
                    categoryNameState.value.isEmpty() ||
                    (isSubCategoryState.value && selectedMainCategoryId.value == null)) themeColors.inverseSurface.copy(0.5f)
                else Color.White
            )
        }

        if (isIconSheetOpen.value) {
            ModalBottomSheet(
                sheetState = iconSheetState,
                sheetMaxWidth = usedScreenWidth,
                onDismissRequest = { isIconSheetOpen.value = false },
                containerColor = themeColors.background,
                contentColor = themeColors.inverseSurface,
                dragHandle = {
                    DragHandle(
                        color = MaterialTheme.colorScheme.inverseSurface.copy(0.3f)
                    )
                },
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            ) {
                val scope = rememberCoroutineScope()
                Column(modifier = Modifier.height(usedScreenHeight - 10.dp)) {
                    CategoryIconSelector(
                        selectedIconId = selectedIconIdState.intValue,
                        onIconSelected = {
                            selectedIconIdState.intValue = it
                           onCategoryEvent(CategoryScreenEvent.UpdateSelectedIconId(it))
                            if (selectedIconIdState.intValue != 0) {
                                scope
                                    .launch { iconSheetState.hide() }
                                    .invokeOnCompletion {
                                        if (!iconSheetState.isVisible) {
                                            isIconSheetOpen.value = false
                                        }
                                    }
                            }
                        }
                    )
                }
            }
        }
    }
}

//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun EditCategorySheet(
//    categoryViewModel: CategoryScreenViewModel,
//    subCategoryViewModel: SubCategoryViewModel,
//    sheetState: SheetState,
//    iconSheetState: SheetState,
//    isSheetOpen: MutableState<Boolean>,
//    isIconSheetOpen: MutableState<Boolean>,
//    selectedIconId: Int,
//    customColor: Color,
//    categoryName: String,
//    isSpecialCategory: Boolean = false,
//    usedScreenWidth: Dp,
//    usedScreenHeight: Dp,
//    isUpdate: Boolean = false,
//    currentCategoryEntity: CategoryEntity? = null,
//){
//    val themeColors = MaterialTheme.colorScheme
//    val state by categoryViewModel.state.collectAsState()
//    val selectedIconIdState = remember(currentCategoryEntity?.id, selectedIconId) {
//        mutableIntStateOf(selectedIconId)
//    }
//    val customColorState = remember(currentCategoryEntity?.id, customColor) {
//        mutableStateOf(customColor)
//    }
//    val categoryNameState = remember(currentCategoryEntity?.id, categoryName) {
//        mutableStateOf(categoryName)
//    }
//
//    LaunchedEffect(currentCategoryEntity, isUpdate) {
//        if (isUpdate && currentCategoryEntity != null) {
//            // Set initial values from the existing category
//            selectedIconIdState.intValue = currentCategoryEntity.categoryIconId
//            customColorState.value = Color(currentCategoryEntity.boxColor)
//            categoryNameState.value = currentCategoryEntity.name
//
//            // Also update the ViewModel state
//            categoryViewModel.onEvent(CategoryScreenEvent.UpdateSelectedIconId(currentCategoryEntity.categoryIconId))
//            categoryViewModel.onEvent(CategoryScreenEvent.UpdateCustomBoxColor(Color(currentCategoryEntity.boxColor)))
//            categoryViewModel.onEvent(CategoryScreenEvent.UpdateCustomTextColor(Color(currentCategoryEntity.textColor)))
//            categoryViewModel.onEvent(CategoryScreenEvent.UpdateCategoryName(currentCategoryEntity.name))
//        }
//    }
//
//    // Add state for main category toggle - enabled by default for new categories
//    val isMainCategoryState = remember { mutableStateOf(!isUpdate) }
//
//    // State for subcategory toggle
//    val isSubCategoryState = remember { mutableStateOf(false) }
//    val selectedMainCategoryId = remember { mutableStateOf<Int?>(null) }
//
//    // State variables for subcategory sheet
//    val subCategorySheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
//    val subCategoryIconSheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
//    var showSubCategorySheet = remember { mutableStateOf(false) }
//    var showSubCategoryIconSheet = remember { mutableStateOf(false) }
//    var selectedSubCategory by remember { mutableStateOf<SubCategoryEntity?>(null) }
//    var isNewSubCategory by remember { mutableStateOf(false) }
//
//    // Collect categories for the main category selection
//    val categories by categoryViewModel.categories.collectAsState()
//    val subCategories by subCategoryViewModel.subCategories.collectAsState()
//
//    LaunchedEffect(currentCategoryEntity?.id) {
//        if (isUpdate && currentCategoryEntity != null) {
//            subCategoryViewModel.fetchSubCategoriesForCategory(currentCategoryEntity.id)
//
//            // Listen for subcategory events specific to this category
//            AppEventBus.events.collect { event ->
//                when (event) {
//                    is CategoryEvent.SubCategoryAdded -> {
//                        if (event.categoryId == currentCategoryEntity.id) {
//                            // Refresh subcategories when a new one is added
//                            subCategoryViewModel.fetchSubCategoriesForCategory(currentCategoryEntity.id)
//                        }
//                    }
//                    is CategoryEvent.SubCategoriesUpdated -> {
//                        if (event.categoryId == currentCategoryEntity.id) {
//                            // Refresh subcategories when they're updated
//                            subCategoryViewModel.fetchSubCategoriesForCategory(currentCategoryEntity.id)
//                        }
//                    }
//                    is CategoryEvent.SubCategoryDeleted -> {
//                        if (event.categoryId == currentCategoryEntity.id) {
//                            // Refresh subcategories when one is deleted
//                            subCategoryViewModel.fetchSubCategoriesForCategory(currentCategoryEntity.id)
//                        }
//                    }
//                    is CategoryEvent.SubCategoryUpdated -> {
//                        if (event.categoryId == currentCategoryEntity.id) {
//                            // Refresh subcategories when one is updated
//                            subCategoryViewModel.fetchSubCategoriesForCategory(currentCategoryEntity.id)
//                        }
//                    }
//                }
//            }
//        }
//    }
//// Handle dismissal during deletion process
//    LaunchedEffect(isSheetOpen.value) {
//        if (!isSheetOpen.value) {
//            // When sheet is dismissed, check if there's a pending deletion and handle it
//            if (state.pendingSubCategoryDeletion != null ||
//                state.pendingCategoryDeletion != null ||
//                state.showSubCategoryDeleteConfirmationDialog ||
//                state.showCategoryDeleteConfirmationDialog) {
//
//                // Immediately clear all deletion dialogs and proceed with deletion if needed
//                categoryViewModel.onEvent(CategoryScreenEvent.DismissAllDeletionDialogs)
//
//                // If there was a pending countdown, immediately delete the item
//                val pendingSubCategory = subCategoryViewModel.pendingDeletion.value
//                if (pendingSubCategory != null) {
//                    subCategoryViewModel.clearPendingDeletion()
//                }
//            }
//        }
//    }
//
//    if (isSheetOpen.value){
//        ModalBottomSheet(
//            sheetState = sheetState,
//            onDismissRequest = {
//                isSheetOpen.value = false
//                // Clear any pending deletions when sheet is dismissed
//                categoryViewModel.onEvent(CategoryScreenEvent.DismissAllDeletionDialogs)
//                               },
//            sheetMaxWidth = usedScreenWidth - 10.dp,
//            dragHandle = {
//                DragHandle(
//                    color = MaterialTheme.colorScheme.inverseSurface.copy(0.3f)
//                )
//            },
//            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
//            containerColor = themeColors.background,
//            contentColor = themeColors.inverseSurface
//        ) {
//            Box(modifier = Modifier
//                .fillMaxWidth()
//                .height(usedScreenHeight)
//                .navigationBarsPadding()
//            ) {
//                LazyColumn(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(horizontal = 20.dp),
//                    verticalArrangement = Arrangement.Top,
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    item(key = "Header text") {
//                        Text(
//                            text = if (isUpdate) "Edit Category" else "Add Category",
//                            textAlign = TextAlign.Center,
//                            fontSize = 18.sp,
//                            fontWeight = FontWeight.SemiBold,
//                            fontFamily = iosFont,
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(bottom = 25.dp)
//                        )
//                    }
//
//                    item(key = "Preview of Category card"){
//                        PreviewCategoryCard(
//                            selectedIconIdState = selectedIconIdState,
//                            customColorState = customColorState,
//                            categoryNameState = categoryNameState,
//                            categoryName = categoryName,
//                            customColor = customColor,
//                            isIconSheetOpen = isIconSheetOpen,
//                            themeColors = themeColors,
//                            isSpecialCategory = isSpecialCategory
//                        )
//                    }
//
//                    item(key = "category details input section") {
//                        CategoryFillDetailsInput(
//                            categoryViewModel = categoryViewModel,
//                            categoryNameState = categoryNameState,
//                            categoryName = categoryName,
//                            isIconSheetOpen = isIconSheetOpen,
//                            themeColors = themeColors
//                        )
//                    }
//
//                    item(key = "Category Card color selection") {
//                        CategoryColorSelection(
//                            categoryViewModel = categoryViewModel,
//                            themeColors = themeColors,
//                            customColorState = customColorState,
//                            selectedIconIdState = selectedIconIdState
//                        )
//                    }
//
//                    // Only show category type selection for new categories
//                    if (!isUpdate) {
//                        item(key = "Main category toggle") {
//                            MainCategoryToggle(
//                                isMainCategoryState = isMainCategoryState,
//                                isSubCategoryState = isSubCategoryState,
//                                themeColors = themeColors,
//                                categoryViewModel = categoryViewModel
//                            )
//
//                            Spacer(modifier = Modifier.height(16.dp))
//
//                            // Only show subcategory selection if Main category is not enabled
//                            if (!isMainCategoryState.value) {
//                                SubcategoryToggleAndSelection(
//                                    isSubCategoryState = isSubCategoryState,
//                                    selectedMainCategoryId = selectedMainCategoryId,
//                                    categories = categories,
//                                    themeColors = themeColors,
//                                    categoryViewModel = categoryViewModel,
//                                    onSubcategoryToggled = { isSubcategory ->
//                                        // If subcategory is enabled, disable Main category
//                                        if (isSubcategory) {
//                                            isMainCategoryState.value = false
//                                        }
//                                    }
//                                )
//                            }
//                        }
//                    } else {
//
//                        item(key = "Category management") {
//                            if (isUpdate && currentCategoryEntity != null) {
//                                Column {
//                                    // Merge Category Button
//                                    Button(
//                                        onClick = {
//                                            categoryViewModel.onEvent(
//                                                CategoryScreenEvent.InitiateMergeCategory(currentCategoryEntity)
//                                            )
//                                        },
//                                        modifier = Modifier
//                                            .fillMaxWidth()
//                                            .padding(vertical = 8.dp),
//                                        colors = ButtonDefaults.buttonColors(
//                                            containerColor = themeColors.surface,
//                                            contentColor = themeColors.inverseSurface
//                                        ),
//                                        shape = RoundedCornerShape(16.dp),
//                                        contentPadding = PaddingValues(vertical = 12.dp, horizontal = 21.dp)
//                                    ) {
//                                        Column(
//                                            verticalArrangement = Arrangement.Center,
//                                            horizontalAlignment = Alignment.Start
//                                        ) {
//                                            Text(
//                                                text = "Merge Category",
//                                                fontSize = 16.sp,
//                                                fontFamily = iosFont,
//                                                fontWeight = FontWeight.SemiBold
//                                            )
//                                            Spacer(modifier = Modifier.height(8.dp))
//                                            Text(
//                                                text = "Merge this category and move all transactions of this category with another category",
//                                                fontSize = 12.sp,
//                                                fontFamily = iosFont,
//                                                color = themeColors.inverseSurface.copy(alpha = 0.7f),
//                                                fontWeight = FontWeight.Medium
//                                            )
//                                        }
//                                    }
//
//                                    Spacer(modifier = Modifier.height(24.dp))
//
//                                    SubcategoriesListItem(
//                                        subCategories = subCategories,
//                                        onMove = { fromIndex, toIndex ->
//                                            subCategoryViewModel.reorderSubCategories(
//                                                fromIndex = fromIndex,
//                                                toIndex = toIndex,
//                                                categoryId = currentCategoryEntity?.id ?: -1
//                                            )
//                                        },
//                                        themeColors = themeColors,
//                                        onSubCategoryClick = { subCategory ->
//                                            categoryViewModel.onEvent(CategoryScreenEvent.GetSubCategoryById(subCategory.id))
//                                            selectedSubCategory = subCategory
//                                            isNewSubCategory = false
//                                            showSubCategorySheet.value = true
//                                        },
//                                        onAddNewSubCategory = {
//                                            categoryViewModel.onEvent(
//                                                CategoryScreenEvent.PrepareForNewSubCategoryInParent(
//                                                    parentCategoryId = currentCategoryEntity?.id ?: -1
//                                                )
//                                            )
//                                            isNewSubCategory = true
//                                            selectedSubCategory = null
//                                            showSubCategorySheet.value = true
//                                        },
//                                        subCategoryViewModel = subCategoryViewModel,
//                                        categoryViewModel = categoryViewModel
//                                    )
//                                }
//                            }
//                        }
//                    }
//
//                    item(key = "End Space for better scrolling view") {
//                        Spacer(modifier = Modifier.height(80.dp))
//                    }
//                }
//                AddCategoryBottomButton(
//                    categoryViewModel = categoryViewModel,
//                    subCategoryViewModel = subCategoryViewModel,
//                    sheetState = sheetState,
//                    iconSheetState = iconSheetState,
//                    isSheetOpen = isSheetOpen,
//                    isIconSheetOpen = isIconSheetOpen,
//                    selectedIconId = selectedIconIdState.intValue, // Use current state value
//                    customColor = customColorState.value, // Use current state value
//                    categoryName = categoryNameState.value, // Use current state value
//                    usedScreenWidth = usedScreenWidth,
//                    usedScreenHeight = usedScreenHeight,
//                    isUpdate = isUpdate,
//                    currentCategoryEntity = currentCategoryEntity,
//                    selectedIconIdState = selectedIconIdState,
//                    customColorState = customColorState,
//                    categoryNameState = categoryNameState,
//                    isMainCategoryState = isMainCategoryState,
//                    isSubCategoryState = isSubCategoryState,
//                    selectedMainCategoryId = selectedMainCategoryId,
//                    themeColors = themeColors,
//                    modifier = Modifier.fillMaxWidth().imePadding().align(Alignment.BottomCenter).padding(bottom = 5.dp)
//                )
//            }
//        }
//
//        CategoryDeletionDialogs(
//            state = state,
//            onEvent = categoryViewModel::onEvent,
//            categoryViewModel = categoryViewModel,
//            themeColors = themeColors
//        )
//    }
//
//    if (showSubCategorySheet.value) {
//        EditSubCategorySheet(
//            categoryViewModel = categoryViewModel,
//            subCategoryViewModel = subCategoryViewModel,
//            showSubCategorySheet = showSubCategorySheet,
//            showSubCategoryIconSheet = showSubCategoryIconSheet,
//            subCategorySheet = subCategorySheet,
//            subCategoryIconSheet = subCategoryIconSheet,
//            subCategoryId = selectedSubCategory?.id,
//            parentCategoryId = if (isNewSubCategory) currentCategoryEntity?.id else null,
//            isNewSubCategory = isNewSubCategory,
//            usedScreenWidth = usedScreenWidth,
//            usedScreenHeight = usedScreenHeight,
//            onDismiss = {
//                showSubCategorySheet.value = false
//                // FIX: Force refresh subcategories when the sheet is dismissed
//                currentCategoryEntity?.let { entity ->
//                    subCategoryViewModel.fetchSubCategoriesForCategory(entity.id)
//                }
//            }
//        )
//    }
//}
//
//@Composable
//private fun PreviewCategoryCard(
//    selectedIconIdState: MutableIntState,
//    customColorState:  MutableState<Color>,
//    categoryNameState: MutableState<String>,
//    categoryName: String,
//    customColor: Color,
//    isIconSheetOpen: MutableState<Boolean>,
//    themeColors: ColorScheme,
//    isSpecialCategory: Boolean
//){
//    Column(verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.CenterHorizontally) {
//
//        Box(){
//            Spacer(modifier = Modifier
//                .height(120.dp)
//                .padding(horizontal = 20.dp)
//                .fillMaxWidth()
//                .background(
//                    themeColors.surface.copy(0.2f),
//                    shape = RoundedCornerShape(20.dp)
//                )
//            )
//            Spacer(modifier = Modifier
//                .height(110.dp)
//                .padding(horizontal = 10.dp)
//                .fillMaxWidth()
//                .background(
//                    themeColors.surface.copy(0.5f),
//                    shape = RoundedCornerShape(20.dp)
//                )
//            )
//            Box(
//                modifier = Modifier
//                    .height(100.dp)
//                    .fillMaxWidth()
//                    .background(
//                        themeColors.surface,
//                        shape = RoundedCornerShape(20.dp)
//                    )
//                    .padding(20.dp)
//            ) {
//                Row(modifier = Modifier.fillMaxWidth(),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
//                    Box(
//                        modifier = Modifier
//                            .size(64.dp)
//                            .shadow(10.dp, RoundedCornerShape(20.dp))
//                            .clip(RoundedCornerShape(20.dp))
//                            .background(
//                                color = if (customColorState.value == Color.Transparent) themeColors.onBackground else customColorState.value,
//                                shape = RoundedCornerShape(20.dp)
//                            )
//                            .clickable { isIconSheetOpen.value = true }
//                            .padding(10.dp),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        selectedIconIdState.intValue.let { iconId ->
//                            if(iconId != 0){
//                                Image(
//                                    painter = painterResource(
//                                        id = icons.find { it.id == iconId }?.resourceId ?: R.drawable.type_beverages_beer),
//                                    contentDescription = "Selected Icon",
//                                    modifier = Modifier.size(40.dp)
//                                )
//                            } else {
//                                Icon(
//                                    painter = painterResource(R.drawable.category_bulk),
//                                    contentDescription = "Default Icon",
//                                    modifier = Modifier.size(30.dp),
//                                    tint = themeColors.inverseSurface.copy(alpha = 0.5f)
//                                )
//                            }
//                        }
//                    }
//                    Column(modifier = Modifier
//                        .weight(1f)
//                        .fillMaxWidth(),
//                        verticalArrangement = Arrangement.Center) {
//                        Text(
//                            text = "My Transaction",
//                            fontFamily = iosFont,
//                            maxLines = 1,
//                            fontWeight = FontWeight.Medium,
//                            fontSize = 18.sp,
//                            color = themeColors.inversePrimary,
//                            modifier = Modifier.fillMaxWidth()
//                        )
//                        Text(
//                            text = if (categoryNameState.value.isBlank() || categoryNameState.value == "") {
//                                if (categoryName.isBlank() || categoryName == "") "Category Name" else categoryName
//                            } else {
//                                categoryNameState.value
//                            },
//                            fontFamily = iosFont,
//                            fontWeight = FontWeight.Normal,
//                            fontSize = 10.sp,
//                            lineHeight = 10.sp,
//                            color = themeColors.inverseSurface,
//                            modifier = Modifier
//                                .padding(top = 5.dp)
//                                .background(
//                                    color = if (customColorState.value == Color.Transparent) themeColors.onBackground.copy(
//                                        0.5f
//                                    ) else customColor.copy(0.5f),
//                                    shape = RoundedCornerShape(8.dp)
//                                )
//                                .padding(5.dp)
//                        )
//                    }
//                    Text(
//                        text = "$100",
//                        textAlign = TextAlign.End,
//                        fontFamily = iosFont,
//                        fontWeight = FontWeight.Bold,
//                        fontSize = 18.sp,
//                        color = themeColors.onError,
//                        modifier = Modifier.weight(0.5f)
//                    )
//                }
//            }
//        }
//        BlurredAnimatedVisibility(
//            visible = isSpecialCategory
//        ) {
//            SpecialCategoryInfoCard()
//        }
//        Text(
//            text = "Preview",
//            textAlign = TextAlign.Center,
//            fontSize = 14.sp,
//            color = themeColors.inverseOnSurface.copy(alpha = 0.85f),
//            fontFamily = iosFont,
//            fontWeight = FontWeight.Medium,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp)
//        )
//    }
//}
//@Composable
//private fun SpecialCategoryInfoCard(){
//    Box(modifier = Modifier.padding(vertical = 10.dp).fillMaxWidth().background(MaterialTheme.colorScheme.onError.copy(0.3f), RoundedCornerShape(15.dp)).border(0.dp, MaterialTheme.colorScheme.onError, RoundedCornerShape(15.dp)).padding(10.dp)) {
//        Row(modifier = Modifier.fillMaxWidth(),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.spacedBy(10.dp)
//        ) {
//            Icon(
//                painter = painterResource(R.drawable.information_bulk),
//                contentDescription = "Special Category Info",
//                tint = MaterialTheme.colorScheme.onError,
//            )
//            Text(
//                text = "This Category is only used when you transfer amount from one account to another. Transactions in this category do not contribute to total income and expense calculations but factor in net total spending. Feel free to customize this category as you like.",
//                textAlign = TextAlign.Start,
//                fontSize = 12.sp,
//                fontFamily = iosFont,
//                fontWeight = FontWeight.Medium,
//                color = MaterialTheme.colorScheme.onError,
//                modifier = Modifier.fillMaxWidth()
//            )
//        }
//    }
//}
//@Composable
//private fun CategoryFillDetailsInput(
//    categoryViewModel: CategoryScreenViewModel,
//    categoryNameState: MutableState<String>,
//    categoryName: String,
//    isIconSheetOpen: MutableState<Boolean>,
//    themeColors: ColorScheme
//){
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(top = 10.dp),
//        verticalAlignment = Alignment.CenterVertically,
//        horizontalArrangement = Arrangement.Center
//    ) {
//        Box(
//            modifier = Modifier
//                .size(64.dp)
//                .clip(RoundedCornerShape(20.dp))
//                .background(themeColors.surface, shape = RoundedCornerShape(20.dp))
//                .clickable { isIconSheetOpen.value = true }
//                .padding(10.dp),
//            contentAlignment = Alignment.Center
//        ) {
//            Icon(
//                painter = painterResource(R.drawable.category_bulk),
//                contentDescription = "Color Picker",
//                modifier = Modifier
//                    .size(30.dp),
//                tint = themeColors.inverseSurface.copy(0.5f) // Disable tinting
//            )
//        }
//        Spacer(modifier = Modifier.size(10.dp))
//        TextField(
//            value = categoryNameState.value,
//            onValueChange = {
//                categoryNameState.value = it
//                categoryViewModel.onEvent(CategoryScreenEvent.UpdateCategoryName(it))
//            },
//            placeholder = {
//                Text(
//                    text = if (categoryNameState.value.isBlank() || categoryNameState.value == "") {
//                        if (categoryName.isBlank() || categoryName == "") "Category Name" else categoryName
//                    } else {
//                        categoryNameState.value
//                    },
//                    style = TextStyle(
//                        textAlign = TextAlign.Start,
//                        fontSize = 12.sp,
//                        fontFamily = iosFont
//                    ),
//                    modifier = Modifier
//                        .height(64.dp)
//                        .fillMaxWidth(),
//                )
//            },
//            trailingIcon = {Icon(painter = painterResource(R.drawable.edit_name_bulk), tint = themeColors.inverseSurface.copy(0.5f), contentDescription = null)},
//            modifier = Modifier
//                .height(64.dp)
//                .fillMaxWidth()
//                .weight(1f)
//                .background(
//                    themeColors.surface,
//                    RoundedCornerShape(20.dp)
//                ),
//            keyboardOptions = KeyboardOptions(
//                capitalization = KeyboardCapitalization.Words,
//                keyboardType = KeyboardType.Text),
//            singleLine = true,
//            colors = TextFieldDefaults.colors(
//                focusedPlaceholderColor = themeColors.inverseSurface.copy(0.5f),
//                unfocusedPlaceholderColor = themeColors.inverseOnSurface.copy(0.5f),
//                unfocusedIndicatorColor = Color.Transparent,
//                focusedIndicatorColor = Color.Transparent,
//                unfocusedContainerColor = Color.Transparent,
//                focusedContainerColor = Color.Transparent
//            ),
//            textStyle = TextStyle(fontSize = 25.sp)
//        )
//    }
//}
//
//@Composable
//private fun CategoryColorSelection(
//    categoryViewModel: CategoryScreenViewModel,
//    themeColors: ColorScheme,
//    customColorState: MutableState<Color>,
//    selectedIconIdState: MutableIntState
//){
//    Column(verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.CenterHorizontally) {
//        Text(
//            text = "Box Color",
//            fontSize = 14.sp,
//            color = themeColors.inverseOnSurface.copy(alpha = 0.85f),
//            fontFamily = iosFont,
//            fontWeight = FontWeight.Medium,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp)
//        )
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .background(
//                    themeColors.surface,
//                    shape = RoundedCornerShape(20.dp)
//                )
//        ) {
//            CategoryBoxColorPicker(
//                isDarkTheme = isSystemInDarkTheme(),
//                initialColor = customColorState.value,
//                onPrimaryColorSelected = { selectedColor ->
//                    // FIX: Update both local state and ViewModel state immediately
//                    customColorState.value = selectedColor
//                    categoryViewModel.onEvent(CategoryScreenEvent.UpdateCustomBoxColor(selectedColor))
//                    categoryViewModel.onEvent(CategoryScreenEvent.UpdateCustomTextColor(themeColors.inverseSurface))
//
//                    // Force recomposition by triggering state change
//                    selectedIconIdState.intValue = selectedIconIdState.intValue
//                }
//            )
//        }
//        Spacer(modifier = Modifier.size(50.dp))
//    }
//}
//@Composable
//private fun MainCategoryToggle(
//    isMainCategoryState: MutableState<Boolean>,
//    isSubCategoryState: MutableState<Boolean>,
//    themeColors: ColorScheme,
//    categoryViewModel: CategoryScreenViewModel
//) {
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .background(color = themeColors.surface, shape = RoundedCornerShape(16.dp))
//            .padding(16.dp)
//    ) {
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Text(
//                text = "Main category",
//                fontSize = 16.sp,
//                color = themeColors.inverseSurface,
//                fontFamily = iosFont,
//                fontWeight = FontWeight.Medium
//            )
//
//            Switch(
//                checked = isMainCategoryState.value,
//                onCheckedChange = { checked ->
//                    isMainCategoryState.value = checked
//                    categoryViewModel.onEvent(CategoryScreenEvent.UpdateIsMainCategory(checked))
//                    if (checked) {
//                        // If Main Category is enabled, disable and clear Subcategory selection
//                        isSubCategoryState.value = false
//                        categoryViewModel.onEvent(CategoryScreenEvent.UpdateIsSubCategory(false))
//                    }
//                },
//                colors = SwitchDefaults.colors(
//                    checkedThumbColor = Color.White,
//                    checkedTrackColor = themeColors.primary,
//                    checkedBorderColor = themeColors.primary,
//                    uncheckedThumbColor = themeColors.inverseSurface.copy(0.5f),
//                    uncheckedTrackColor = themeColors.background,
//                    uncheckedBorderColor = Color.Transparent
//                )
//            )
//        }
//
//        Text(
//            text = "This will be a top-level category",
//            fontSize = 12.sp,
//            color = themeColors.inverseOnSurface.copy(alpha = 0.7f),
//            fontFamily = iosFont,
//            fontWeight = FontWeight.Normal,
//            modifier = Modifier.padding(top = 4.dp)
//        )
//    }
//}
//
//@OptIn(ExperimentalLayoutApi::class)
//@Composable
//private fun SubcategoryToggleAndSelection(
//    isSubCategoryState: MutableState<Boolean>,
//    selectedMainCategoryId: MutableState<Int?>,
//    categories: List<CategoryEntity>,
//    themeColors: ColorScheme,
//    categoryViewModel: CategoryScreenViewModel,
//    onSubcategoryToggled: (Boolean) -> Unit = {}
//) {
//    Column(
//        modifier = Modifier
//            .then(
//                if (isSubCategoryState.value) {
//                    Modifier.border(1.dp, themeColors.inverseSurface.copy(0.5f), RoundedCornerShape(16.dp))
//                } else {
//                    Modifier
//                }
//            )
//            .fillMaxWidth()
//            .background(color = themeColors.surface, shape = RoundedCornerShape(16.dp))
//            .clickable(
//                onClick = {
//                    if (!isSubCategoryState.value) {
//                        isSubCategoryState.value = !isSubCategoryState.value
//                        categoryViewModel.onEvent(CategoryScreenEvent.UpdateIsSubCategory(isSubCategoryState.value))
//                        onSubcategoryToggled(isSubCategoryState.value)
//                    }
//                },
//                indication = null,
//                interactionSource = remember { MutableInteractionSource() }
//            )
//    ) {
//        // Subcategory toggle
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 16.dp),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(vertical = 16.dp)
//            ) {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    Text(
//                        text = "Make as Subcategory",
//                        fontSize = 16.sp,
//                        color = themeColors.inverseSurface,
//                        fontFamily = iosFont,
//                        fontWeight = FontWeight.Medium
//                    )
//
//                    Switch(
//                        checked = isSubCategoryState.value,
//                        onCheckedChange = { isEnabled ->
//                            isSubCategoryState.value = isEnabled
//                            categoryViewModel.onEvent(CategoryScreenEvent.UpdateIsSubCategory(isEnabled))
//                            onSubcategoryToggled(isEnabled)
//                            if (!isEnabled) {
//                                selectedMainCategoryId.value = null
//                                categoryViewModel.onEvent(CategoryScreenEvent.UpdateSelectedMainCategoryId(null))
//                            }
//                        },
//                        colors = SwitchDefaults.colors(
//                            checkedThumbColor = Color.White,
//                            checkedTrackColor = themeColors.primary,
//                            checkedBorderColor = themeColors.primary,
//                            uncheckedThumbColor = themeColors.inverseSurface.copy(0.5f),
//                            uncheckedTrackColor = themeColors.background,
//                            uncheckedBorderColor = Color.Transparent
//                        )
//                    )
//                }
//
//                if (isSubCategoryState.value) {
//                    Column(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                    ) {
//                        Text(
//                            text = "Select Parent Category",
//                            fontSize = 12.sp,
//                            color = themeColors.inverseOnSurface.copy(alpha = 0.85f),
//                            fontFamily = iosFont,
//                            fontWeight = FontWeight.Medium,
//                            modifier = Modifier.padding(bottom = 8.dp, top = 8.dp)
//                        )
//
//                        Box(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .background(
//                                    themeColors.surface,
//                                    shape = RoundedCornerShape(20.dp)
//                                )
//                                .padding(vertical = 10.dp)
//                        ) {
//                            FlowRow(
//                                modifier = Modifier.fillMaxWidth(),
//                                horizontalArrangement = Arrangement.spacedBy(10.dp),
//                                verticalArrangement = Arrangement.spacedBy(10.dp)
//                            ) {
//                                categories.forEach { category ->
//                                    CategoryChip(
//                                        category = category,
//                                        isSelected = selectedMainCategoryId.value == category.id,
//                                        onSelected = {
//                                            // Toggle logic: if selecting the same category again, deselect it
//                                            if (selectedMainCategoryId.value == category.id) {
//                                                // Deselect this category
//                                                selectedMainCategoryId.value = null
//                                                categoryViewModel.onEvent(CategoryScreenEvent.UpdateSelectedMainCategoryId(null))
//                                                isSubCategoryState.value = false
//                                                categoryViewModel.onEvent(CategoryScreenEvent.UpdateIsSubCategory(false))
//                                                onSubcategoryToggled(false)
//                                            } else {
//                                                // Select this category
//                                                selectedMainCategoryId.value = category.id
//                                                categoryViewModel.onEvent(CategoryScreenEvent.UpdateSelectedMainCategoryId(category.id))
//                                                isSubCategoryState.value = true
//                                                categoryViewModel.onEvent(CategoryScreenEvent.UpdateIsSubCategory(true))
//                                                onSubcategoryToggled(true)
//                                            }
//                                        },
//                                        themeColors = themeColors
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
//
//// Composable for the individual category chips
//@Composable
//private fun CategoryChip(
//    category: CategoryEntity,
//    isSelected: Boolean,
//    onSelected: () -> Unit,
//    themeColors: ColorScheme
//) {
//    Box(
//        modifier = Modifier
//            .clip(RoundedCornerShape(50.dp))
//            .background(
//                if (isSelected) Color(category.boxColor) else themeColors.surfaceVariant
//            )
//            .clickable { onSelected() }
//            .padding(6.dp)
//    ) {
//        Row(
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.spacedBy(6.dp)
//        ) {
//            Box(
//                modifier = Modifier
//                    .size(24.dp)
//                    .clip(CircleShape)
//                    .background(if (isSelected) Color.White.copy(alpha = 0.2f) else Color(category.boxColor)),
//                contentAlignment = Alignment.Center
//            ) {
//                Image(
//                    painter = painterResource(
//                        id = icons.find { it.id == category.categoryIconId }?.resourceId
//                            ?: R.drawable.type_beverages_beer
//                    ),
//                    contentDescription = "Category Icon",
//                    modifier = Modifier.size(16.dp)
//                )
//            }
//            Text(
//                text = category.name,
//                color = if (isSelected) Color.White else themeColors.inverseSurface,
//                fontSize = 12.sp,
//                fontFamily = iosFont,
//                fontWeight = FontWeight.Medium,
//            )
//        }
//    }
//}
//
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//private fun AddCategoryBottomButton(
//    categoryViewModel: CategoryScreenViewModel,
//    subCategoryViewModel: SubCategoryViewModel,
//    sheetState: SheetState,
//    iconSheetState: SheetState,
//    isSheetOpen: MutableState<Boolean>,
//    isIconSheetOpen: MutableState<Boolean>,
//    selectedIconId: Int,
//    customColor: Color,
//    categoryName: String,
//    usedScreenWidth: Dp,
//    usedScreenHeight: Dp,
//    isUpdate: Boolean = false,
//    currentCategoryEntity: CategoryEntity? = null,
//    selectedIconIdState: MutableIntState,
//    customColorState: MutableState<Color>,
//    categoryNameState: MutableState<String>,
//    isMainCategoryState: MutableState<Boolean>,
//    isSubCategoryState: MutableState<Boolean>,
//    selectedMainCategoryId: MutableState<Int?>,
//    themeColors: ColorScheme,
//    modifier: Modifier
//){
//    Box(modifier = modifier, contentAlignment = Alignment.BottomCenter) {
//        val scope = rememberCoroutineScope()
//        val context = LocalContext.current
//
//        // State for toast handling
//        var showToast by remember { mutableStateOf(false) }
//        var toastMessage by remember { mutableStateOf("") }
//
//        // Show toast when needed
//        LaunchedEffect(showToast) {
//            if (showToast) {
//                Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
//                showToast = false
//            }
//        }
//
//        val saveCategory = {
//            // Update all state values in the ViewModel first
//            categoryViewModel.onEvent(CategoryScreenEvent.UpdateCategoryName(categoryNameState.value))
//            categoryViewModel.onEvent(CategoryScreenEvent.UpdateSelectedIconId(selectedIconIdState.intValue))
//            categoryViewModel.onEvent(CategoryScreenEvent.UpdateCustomBoxColor(customColorState.value))
//            categoryViewModel.onEvent(CategoryScreenEvent.UpdateCustomTextColor(themeColors.inverseSurface))
//            categoryViewModel.onEvent(CategoryScreenEvent.UpdateIsMainCategory(isMainCategoryState.value))
//            categoryViewModel.onEvent(CategoryScreenEvent.UpdateIsSubCategory(isSubCategoryState.value))
//            categoryViewModel.onEvent(CategoryScreenEvent.UpdateSelectedMainCategoryId(selectedMainCategoryId.value))
//
//            // Save category using the onEvent pattern
//            categoryViewModel.onEvent(CategoryScreenEvent.OnCategorySaved(
//                onSuccess = {
//                    scope.launch { sheetState.hide() }
//                        .invokeOnCompletion {
//                            if (!sheetState.isVisible) {
//                                isSheetOpen.value = false
//
//                                // Set toast message and trigger toast
//                                if (selectedIconIdState.intValue != 0 || customColorState.value != Color.Transparent || categoryNameState.value.isNotEmpty()) {
//                                    toastMessage = if (isSubCategoryState.value) "Subcategory Added" else "Category Added"
//                                    showToast = true
//                                }
//                            }
//                        }
//                }
//            ))
//        }
//
//        // Define the update logic
//        val updateCategory = {
//            // Update all state values in the ViewModel first
//            categoryViewModel.onEvent(CategoryScreenEvent.UpdateCategoryName(categoryNameState.value))
//            categoryViewModel.onEvent(CategoryScreenEvent.UpdateSelectedIconId(selectedIconIdState.intValue))
//            categoryViewModel.onEvent(CategoryScreenEvent.UpdateCustomBoxColor(customColorState.value))
//            categoryViewModel.onEvent(CategoryScreenEvent.UpdateCustomTextColor(themeColors.inverseSurface))
//
//            if (isSubCategoryState.value && selectedMainCategoryId.value != null && currentCategoryEntity != null) {
//                // Convert a category to subcategory
//                categoryViewModel.onEvent(CategoryScreenEvent.UpdateSelectedMainCategoryId(selectedMainCategoryId.value))
//                categoryViewModel.onEvent(CategoryScreenEvent.UpdateIsSubCategory(true))
//                categoryViewModel.onEvent(CategoryScreenEvent.UpdateIsMainCategory(false))
//
//                // Create new subcategory and then delete the original category
//                categoryViewModel.onEvent(CategoryScreenEvent.OnCategorySaved(
//                    onSuccess = {
//                        // Delete the original category after successfully creating subcategory
//                        categoryViewModel.onEvent(CategoryScreenEvent.DeleteCategory(currentCategoryEntity))
//
//                        scope.launch { sheetState.hide() }
//                            .invokeOnCompletion {
//                                if (!sheetState.isVisible) {
//                                    isSheetOpen.value = false
//
//                                    // Set toast message and trigger toast
//                                    toastMessage = "Converted to Subcategory"
//                                    showToast = true
//                                }
//                            }
//                    }
//                ))
//            } else {
//                // Update existing category
//                currentCategoryEntity?.let { entity ->
//                    categoryViewModel.onEvent(CategoryScreenEvent.OnCategoryUpdated(
//                        category = entity,
//                        onSuccess = {
//                            scope.launch { sheetState.hide() }
//                                .invokeOnCompletion {
//                                    if (!sheetState.isVisible) {
//                                        isSheetOpen.value = false
//
//                                        // Set toast message and trigger toast
//                                        if (selectedIconIdState.intValue != selectedIconId ||
//                                            customColorState.value != customColor ||
//                                            categoryNameState.value != categoryName) {
//                                            toastMessage = "Category Updated"
//                                            showToast = true
//                                        }
//                                    }
//                                }
//                        }
//                    ))
//                } ?: run {
//                    // Handle the case where the category is null
//                    toastMessage = "Category not found"
//                    showToast = true
//                }
//            }
//        }
//
//        Button(
//            onClick = if (isUpdate) updateCategory else saveCategory,
//            modifier = Modifier
//                .height(50.dp)
//                .fillMaxWidth()
//                .padding(horizontal = 20.dp)
//                .align(Alignment.BottomCenter),
//            shape = RoundedCornerShape(16.dp),
//            colors = ButtonDefaults.buttonColors(
//                containerColor = if (selectedIconIdState.intValue == 0 ||
//                    customColorState.value == Color.Transparent ||
//                    categoryNameState.value.isEmpty() ||
//                    (isSubCategoryState.value && selectedMainCategoryId.value == null)) themeColors.surfaceBright
//                else themeColors.primary,
//                contentColor = themeColors.inverseSurface
//            )
//        ) {
//            Text(
//                text = if (isUpdate) {
//                    if (isSubCategoryState.value) "Convert to Subcategory" else "Save Category"
//                } else {
//                    if (isSubCategoryState.value) "Add Subcategory" else "Add Category"
//                },
//                fontFamily = iosFont,
//                fontWeight = FontWeight.Medium,
//                color = if (selectedIconIdState.intValue == 0 ||
//                    customColorState.value == Color.Transparent ||
//                    categoryNameState.value.isEmpty() ||
//                    (isSubCategoryState.value && selectedMainCategoryId.value == null)) themeColors.inverseSurface.copy(0.5f)
//                else Color.White
//            )
//        }
//
//        if (isIconSheetOpen.value) {
//            ModalBottomSheet(
//                sheetState = iconSheetState,
//                sheetMaxWidth = usedScreenWidth,
//                onDismissRequest = { isIconSheetOpen.value = false },
//                containerColor = themeColors.background,
//                contentColor = themeColors.inverseSurface,
//                dragHandle = {
//                    DragHandle(
//                        color = MaterialTheme.colorScheme.inverseSurface.copy(0.3f)
//                    )
//                },
//                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
//            ) {
//                val scope = rememberCoroutineScope()
//                Column(modifier = Modifier.height(usedScreenHeight - 10.dp)) {
//                    CategoryIconSelector(
//                        selectedIconId = selectedIconIdState.intValue,
//                        onIconSelected = {
//                            selectedIconIdState.intValue = it
//                            categoryViewModel.onEvent(CategoryScreenEvent.UpdateSelectedIconId(it))
//                            if (selectedIconIdState.intValue != 0) {
//                                scope
//                                    .launch { iconSheetState.hide() }
//                                    .invokeOnCompletion {
//                                        if (!iconSheetState.isVisible) {
//                                            isIconSheetOpen.value = false
//                                        }
//                                    }
//                            }
//                        }
//                    )
//                }
//            }
//        }
//    }
//}