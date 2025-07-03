import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritesh.cashiro.data.local.entity.SubCategoryEntity
import com.ritesh.cashiro.presentation.ui.extras.components.category.CategoryBoxColorPicker
import com.ritesh.cashiro.presentation.ui.features.categories.CategoryScreenViewModel
import com.ritesh.cashiro.presentation.ui.features.categories.SubCategoryViewModel
import com.ritesh.cashiro.presentation.ui.theme.iosFont
import com.ritesh.cashiro.domain.utils.icons
import com.ritesh.cashiro.presentation.ui.extras.components.extras.CategoryDeletionDialogs
import com.ritesh.cashiro.presentation.ui.extras.components.category.CategoryIconSelector
import com.ritesh.cashiro.presentation.ui.features.categories.CategoryScreenEvent
import com.ritesh.cashiro.presentation.ui.features.categories.SubCategoryEvent
import com.ritesh.cashiro.R
import com.ritesh.cashiro.domain.repository.TransactionStats
import com.ritesh.cashiro.presentation.ui.features.categories.CategoryScreenState
import com.ritesh.cashiro.presentation.ui.features.categories.SubCategoryState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditSubCategorySheet(
    categoryUiState: CategoryScreenState,
    onCategoryEvent: (CategoryScreenEvent) -> Unit,
    subCategoryUiState: SubCategoryState,
    onSubCategoryEvent: (SubCategoryEvent) -> Unit,
    getTransactionStatsForCategory: (categoryId: Int, onResult: (TransactionStats) -> Unit) -> Unit,
    getTransactionStatsForSubCategory: (subCategoryId: Int, onResult: (TransactionStats) -> Unit) -> Unit,
    showSubCategorySheet: MutableState<Boolean>,
    showSubCategoryIconSheet: MutableState<Boolean>,
    subCategorySheet: SheetState,
    subCategoryIconSheet: SheetState,
    subCategoryId: Int? = null,
    parentCategoryId: Int? = null,
    isNewSubCategory: Boolean = false,
    usedScreenWidth: Dp,
    usedScreenHeight: Dp,
    onDismiss: () -> Unit
) {
    val themeColors = MaterialTheme.colorScheme
    val context = LocalContext.current

    // State to track toast messages
    var showSuccessToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }

    // Observe state from ViewModels
//    val categoryUiState by categoryViewModel.state.collectAsState()
    val subCategoryName = categoryUiState.categoryName
    val selectedIconId = categoryUiState.selectedIconId ?: 0
    val customBoxColor = categoryUiState.customBoxColor ?: Color.Gray

    // Observe SubCategory ViewModel states
    val isMergeMode = subCategoryUiState.isMergeMode
    val isConvertToMainMode = subCategoryUiState.isConvertToMainMode
    val availableSubCategoriesForMerge = subCategoryUiState.availableSubCategoriesForMerge
    val selectedTargetSubCategoryId = subCategoryUiState.selectedTargetSubCategoryId
    val subCategories = subCategoryUiState.subCategories

    // Local state to manage form
    val subCategoryNameState = remember { mutableStateOf(subCategoryName) }
    val selectedIconIdState = remember { mutableIntStateOf(selectedIconId) }
    val customColorState = remember { mutableStateOf(customBoxColor) }

    // State for convert to main category form
    val newMainCategoryName = remember { mutableStateOf("") }
    val newMainCategoryIconId = remember { mutableIntStateOf(0) }
    val newMainCategoryColor = remember { mutableStateOf(Color.Gray) }

    // Dialog states
    var showMergeConfirmationDialog by remember { mutableStateOf(false) }
    var showConvertConfirmationDialog by remember { mutableStateOf(false) }

    // Current editing subcategory
    var currentEditingSubCategory by remember { mutableStateOf<SubCategoryEntity?>(null) }

    LaunchedEffect(key1 = subCategoryId, key2 = parentCategoryId, key3 = isNewSubCategory) {
        if (isNewSubCategory && parentCategoryId != null) {
            onCategoryEvent(
                CategoryScreenEvent.PrepareForNewSubCategoryInParent(parentCategoryId)
            )
            onSubCategoryEvent(SubCategoryEvent.PrepareForNewSubCategory(parentCategoryId))
        } else if (subCategoryId != null) {
            onCategoryEvent(
                CategoryScreenEvent.GetSubCategoryById(subCategoryId)
            )
            // FIX: Get the subcategory details for advanced options WITH proper tracking
            onSubCategoryEvent(SubCategoryEvent.GetSubCategoryById(subCategoryId) { subCategory ->
                currentEditingSubCategory = subCategory
                subCategory?.let {
                    // FIX: Use the enhanced method that tracks editing ID
                    onSubCategoryEvent(SubCategoryEvent.PrepareForEditingSubCategory(it))
                    newMainCategoryName.value = it.name
                    newMainCategoryIconId.intValue = it.subcategoryIconId ?: 0
                    newMainCategoryColor.value = it.boxColor?.let { color -> Color(color) } ?: Color.Gray
                }
            }
            )
        }
    }

    // Update local state when ViewModel state changes
    LaunchedEffect(key1 = categoryUiState) {
        subCategoryNameState.value = categoryUiState.categoryName
        selectedIconIdState.intValue = categoryUiState.selectedIconId ?: 0
        categoryUiState.customBoxColor?.let { customColorState.value = it }
    }

    // Show toast when showSuccessToast is true
    LaunchedEffect(showSuccessToast) {
        if (showSuccessToast) {
            Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
            showSuccessToast = false
        }
    }

    if (showSubCategorySheet.value) {
        ModalBottomSheet(
            sheetState = subCategorySheet,
            onDismissRequest = {
                showSubCategorySheet.value = false
                // Reset states when dismissing
                onSubCategoryEvent(SubCategoryEvent.ResetAdvancedOptions)
                onCategoryEvent(CategoryScreenEvent.PrepareForNewCategory) // This resets the category state
                onDismiss()
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
            Box(
                modifier = Modifier
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
                            text = if (isNewSubCategory) "Add Subcategory" else "Edit Subcategory",
                            textAlign = TextAlign.Center,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = iosFont,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 25.dp)
                        )
                    }

                    item(key = "subcategory details input section") {
                        SubCategoryDetailsInput(
                            onCategoryEvent = onCategoryEvent,
                            subCategoryNameState = subCategoryNameState,
                            subCategoryBoxColor = customColorState,
                            selectedIconIdState = selectedIconIdState,
                            isIconSheetOpen = showSubCategoryIconSheet,
                            themeColors = themeColors,
                        )
                    }

                    item(key = "SubCategory Card color selection") {
                        SubCategoryColorSelection(
                            onCategoryEvent = onCategoryEvent,
                            themeColors = themeColors,
                            customColorState = customColorState,
                        )
                    }

                    // Advanced options section - only show when editing existing subcategory
                    if (!isNewSubCategory && currentEditingSubCategory != null) {
                        item(key = "Advanced Options") {
                            Column {
                                Text(
                                    text = "Advanced Options",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = themeColors.inverseSurface.copy(alpha = 0.85f),
                                    fontFamily = iosFont,
                                    modifier = Modifier.padding(start = 20.dp, bottom = 16.dp)
                                )
                                AdvancedOptionsSection(
                                    onSubCategoryEvent = onSubCategoryEvent,
                                    isMergeMode = isMergeMode,
                                    isConvertToMainMode = isConvertToMainMode,
                                    availableSubCategoriesForMerge = availableSubCategoriesForMerge,
                                    selectedTargetSubCategoryId = selectedTargetSubCategoryId,
                                    newMainCategoryName = newMainCategoryName,
                                    themeColors = themeColors,
                                    onMergeClick = { showMergeConfirmationDialog = true },
                                    onConvertClick = { showConvertConfirmationDialog = true }
                                )
                            }

                        }
                    }

                    item(key = "End Space for better scrolling view") {
                        Spacer(modifier = Modifier.height(120.dp))
                    }
                }

                // Main action button (Save/Add)
                if (!isMergeMode && !isConvertToMainMode) {
                    Button(
                        onClick = {
                            onCategoryEvent(CategoryScreenEvent.UpdateCategoryName(subCategoryNameState.value))
                            onCategoryEvent(CategoryScreenEvent.UpdateSelectedIconId(selectedIconIdState.intValue))
                            onCategoryEvent(CategoryScreenEvent.UpdateCustomBoxColor(customColorState.value))

                            if (isNewSubCategory) {
                                onCategoryEvent(
                                    CategoryScreenEvent.OnSubCategorySaved {
                                        toastMessage = "Subcategory Added"
                                        showSuccessToast = true
                                        showSubCategorySheet.value = false
                                        onDismiss()
                                    }
                                )
                            } else if (categoryUiState.currentSubCategoryId != null) {
                                val currentSubCategory = subCategories.find { it.id == categoryUiState.currentSubCategoryId }
                                currentSubCategory?.let { subCategory ->
                                    onCategoryEvent(
                                        CategoryScreenEvent.OnSubCategoryUpdated(
                                            subCategory = subCategory,
                                            onSuccess = {
                                                toastMessage = "Subcategory Updated"
                                                showSuccessToast = true
                                                showSubCategorySheet.value = false
                                                onDismiss()
                                            }
                                        )
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .imePadding()
                            .align(Alignment.BottomCenter)
                            .padding(horizontal = 20.dp, vertical = 5.dp),
                        shape = RoundedCornerShape(15.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (subCategoryNameState.value.isEmpty()) {
                                themeColors.surfaceBright
                            } else {
                                themeColors.primary
                            },
                            contentColor = themeColors.inverseSurface
                        )
                    ) {
                        Text(
                            text = if (isNewSubCategory) "Add Subcategory" else "Save Subcategory",
                            fontFamily = iosFont,
                            fontWeight = FontWeight.Medium,
                            color = if (subCategoryNameState.value.isEmpty()) {
                                themeColors.inverseSurface.copy(0.5f)
                            } else {
                                Color.White
                            }
                        )
                    }
                }

                // Icon selector sheet
                if (showSubCategoryIconSheet.value) {
                    ShowIconSelector(
                        iconSheetState = subCategoryIconSheet,
                        isIconSheetOpen = showSubCategoryIconSheet,
                        usedScreenWidth = usedScreenWidth,
                        usedScreenHeight = usedScreenHeight,
                        selectedIconIdState = selectedIconIdState,
                        onCategoryEvent = onCategoryEvent,
                        themeColors = themeColors,
                    )
                }
            }
        }
    }

    // Confirmation Dialogs
    currentEditingSubCategory?.let { subCategory ->
        MergeSubCategoryConfirmationDialog(
            showDialog = showMergeConfirmationDialog,
            sourceSubCategoryName = subCategory.name,
            targetSubCategoryName = availableSubCategoriesForMerge
                .find { it.id == selectedTargetSubCategoryId }?.name ?: "",
            onConfirm = {
                selectedTargetSubCategoryId?.let { targetId ->
                    onSubCategoryEvent(
                        SubCategoryEvent.MergeSubCategories(subCategory.id, targetId)
                    )
                    toastMessage = "SubCategories Merged Successfully"
                    showSuccessToast = true
                    showMergeConfirmationDialog = false
                    showSubCategorySheet.value = false
                    onDismiss()
                }
            },
            onDismiss = {
                showMergeConfirmationDialog = false
            }
        )

        ConvertToMainCategoryConfirmationDialog(
            showDialog = showConvertConfirmationDialog,
            subCategoryName = subCategory.name,
            newCategoryName = newMainCategoryName.value,
            onConfirm = {
                onSubCategoryEvent(
                    SubCategoryEvent.ConvertToMainCategory(
                        subCategory = subCategory,
                        name = newMainCategoryName.value,
                        iconId = newMainCategoryIconId.intValue,
                        boxColor = newMainCategoryColor.value
                    )
                )
                toastMessage = "Converted to Main Category Successfully"
                showSuccessToast = true
                showConvertConfirmationDialog = false
                showSubCategorySheet.value = false
                onDismiss()
            },
            onDismiss = {
                showConvertConfirmationDialog = false
            }
        )
    }
    CategoryDeletionDialogs(
        state = categoryUiState,
        onEvent = onCategoryEvent,
        getTransactionStatsForCategory = getTransactionStatsForCategory,
        getTransactionStatsForSubCategory = getTransactionStatsForSubCategory,
        themeColors = themeColors
    )
}

@Composable
private fun SubCategoryDetailsInput(
    onCategoryEvent: (CategoryScreenEvent) -> Unit,
    subCategoryNameState: MutableState<String>,
    subCategoryBoxColor: MutableState<Color>,
    selectedIconIdState: MutableIntState,
    isIconSheetOpen: MutableState<Boolean>,
    themeColors: ColorScheme
) {
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
                .then(
                    selectedIconIdState.intValue.let { iconId ->
                        if (iconId != 0){
                            Modifier.background(subCategoryBoxColor.value, shape = RoundedCornerShape(20.dp))
                        } else {
                            Modifier.background(themeColors.surface, shape = RoundedCornerShape(20.dp))
                        }
                    }
                )
                .clickable { isIconSheetOpen.value = true }
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            selectedIconIdState.intValue.let { iconId ->
                if (iconId != 0) {
                    Image(
                        painter = painterResource(
                            id = icons.find { it.id == iconId }?.resourceId
                                ?: R.drawable.type_beverages_beer
                        ),
                        contentDescription = "Selected Icon",
                        modifier = Modifier.size(40.dp)
                    )
                } else {
                    Icon(
                        painter = painterResource(R.drawable.category_bulk),
                        contentDescription = "Icon Selector",
                        modifier = Modifier.size(30.dp),
                        tint = themeColors.inverseSurface.copy(0.5f)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.size(10.dp))
        TextField(
            value = subCategoryNameState.value,
            onValueChange = {
                subCategoryNameState.value = it
                onCategoryEvent(CategoryScreenEvent.UpdateCategoryName(it))
            },
            placeholder = {
                Text(
                    text = "Subcategory Name",
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
            trailingIcon = {
                Icon(
                    painter = painterResource(R.drawable.edit_name_bulk),
                    tint = themeColors.inverseSurface.copy(0.5f),
                    contentDescription = null
                )
            },
            modifier = Modifier
                .height(64.dp)
                .fillMaxWidth()
                .weight(1f)
                .background(themeColors.surface, RoundedCornerShape(20.dp)),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                keyboardType = KeyboardType.Text
            ),
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
private fun SubCategoryColorSelection(
    onCategoryEvent: (CategoryScreenEvent) -> Unit,
    themeColors: ColorScheme,
    customColorState: MutableState<Color>
) {
    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
                    customColorState.value = selectedColor
                    onCategoryEvent(CategoryScreenEvent.UpdateCustomBoxColor(selectedColor))
                    onCategoryEvent(CategoryScreenEvent.UpdateCustomTextColor(themeColors.inverseSurface))
                }
            )
        }
        Spacer(modifier = Modifier.size(50.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShowIconSelector(
    iconSheetState: SheetState,
    isIconSheetOpen: MutableState<Boolean>,
    usedScreenWidth: Dp,
    usedScreenHeight: Dp,
    selectedIconIdState: MutableIntState,
    onCategoryEvent: (CategoryScreenEvent) -> Unit,
    themeColors: ColorScheme
) {
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        sheetState = iconSheetState,
        sheetMaxWidth = usedScreenWidth,
        onDismissRequest = { isIconSheetOpen.value = false },
        dragHandle = {
            DragHandle(
                color = MaterialTheme.colorScheme.inverseSurface.copy(0.3f)
            )
        },
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        containerColor = themeColors.background,
        contentColor = themeColors.inverseSurface
    ) {
        Column(modifier = Modifier.height(usedScreenHeight - 10.dp)) {
            CategoryIconSelector(
                selectedIconId = selectedIconIdState.intValue,
                onIconSelected = {
                    selectedIconIdState.intValue = it
                    onCategoryEvent(CategoryScreenEvent.UpdateSelectedIconId(it))
                    if (selectedIconIdState.intValue != 0) {
                        scope.launch { iconSheetState.hide() }
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


@Composable
private fun AdvancedOptionsSection(
    onSubCategoryEvent: (SubCategoryEvent) -> Unit,
    isMergeMode: Boolean,
    isConvertToMainMode: Boolean,
    availableSubCategoriesForMerge: List<SubCategoryEntity>,
    selectedTargetSubCategoryId: Int?,
    newMainCategoryName: MutableState<String>,
    themeColors: ColorScheme,
    onMergeClick: () -> Unit,
    onConvertClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = themeColors.surface, shape = RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {

        // Merge SubCategory Option
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Merge SubCategory",
                    fontSize = 14.sp,
                    color = themeColors.inverseSurface,
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Merge with another subcategory",
                    fontSize = 12.sp,
                    color = themeColors.inverseOnSurface.copy(alpha = 0.7f),
                    fontFamily = iosFont
                )
            }

            Switch(
                checked = isMergeMode,
                onCheckedChange = { enabled ->
                    onSubCategoryEvent(SubCategoryEvent.SetMergeMode(enabled))
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

        // Show subcategory selection when merge mode is enabled
        if (isMergeMode) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Select target subcategory:",
                fontSize = 12.sp,
                color = themeColors.inverseOnSurface.copy(alpha = 0.85f),
                fontFamily = iosFont,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (availableSubCategoriesForMerge.isEmpty()) {
                Text(
                    text = "No other subcategories available for merging",
                    fontSize = 12.sp,
                    color = themeColors.error,
                    fontFamily = iosFont,
                    style = TextStyle(fontStyle = FontStyle.Italic)
                )
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableSubCategoriesForMerge.forEach { subCategory ->
                        SubCategorySelectionChip(
                            subCategory = subCategory,
                            isSelected = selectedTargetSubCategoryId == subCategory.id,
                            onSelected = {
                                onSubCategoryEvent(
                                    SubCategoryEvent.SelectTargetSubCategoryForMerge(subCategory.id)
                                )
                            },
                            themeColors = themeColors
                        )
                    }
                }

                if (selectedTargetSubCategoryId != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onMergeClick,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = themeColors.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Merge SubCategories",
                            fontFamily = iosFont,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Convert to Main Category Option
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Make Main Category",
                    fontSize = 14.sp,
                    color = themeColors.inverseSurface,
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Convert to a main category",
                    fontSize = 12.sp,
                    color = themeColors.inverseOnSurface.copy(alpha = 0.7f),
                    fontFamily = iosFont
                )
            }

            Switch(
                checked = isConvertToMainMode,
                onCheckedChange = { enabled ->
                    onSubCategoryEvent(SubCategoryEvent.SetConvertToMainMode(enabled))
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

        // Show main category form when convert mode is enabled
        if (isConvertToMainMode) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "New Main Category Details:",
                fontSize = 12.sp,
                color = themeColors.inverseOnSurface.copy(alpha = 0.85f),
                fontFamily = iosFont,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Category name input
            TextField(
                value = newMainCategoryName.value,
                onValueChange = { newMainCategoryName.value = it },
                placeholder = { Text("Category Name", fontSize = 12.sp, fontFamily = iosFont) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedContainerColor = themeColors.surfaceBright.copy(alpha = 0.5f),
                    focusedContainerColor = themeColors.surfaceBright.copy(alpha = 0.7f)
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Button(
                onClick = onConvertClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = newMainCategoryName.value.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColors.primary,
                    disabledContainerColor = themeColors.surfaceBright
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Convert to Main Category",
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Medium,
                    color = if (newMainCategoryName.value.isNotBlank()) Color.White
                    else themeColors.inverseSurface.copy(0.5f)
                )
            }
        }
    }
}

@Composable
private fun SubCategorySelectionChip(
    subCategory: SubCategoryEntity,
    isSelected: Boolean,
    onSelected: () -> Unit,
    themeColors: ColorScheme
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected) themeColors.primary else themeColors.surfaceBright
            )
            .clickable { onSelected() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Icon
            subCategory.boxColor?.let { boxColor ->
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(boxColor)),
                    contentAlignment = Alignment.Center
                ) {
                    subCategory.subcategoryIconId?.let { iconId ->
                        Image(
                            painter = painterResource(
                                id = icons.find { it.id == iconId }?.resourceId
                                    ?: R.drawable.type_beverages_beer
                            ),
                            contentDescription = "SubCategory Icon",
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Text(
                text = subCategory.name,
                color = if (isSelected) Color.White else themeColors.inverseSurface,
                fontSize = 12.sp,
                fontFamily = iosFont,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
// ConfirmationDialogs.kt
@Composable
fun MergeSubCategoryConfirmationDialog(
    showDialog: Boolean,
    sourceSubCategoryName: String,
    targetSubCategoryName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Merge SubCategory",
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Are you sure you want to merge \"$sourceSubCategoryName\" with \"$targetSubCategoryName\"?",
                        fontFamily = iosFont,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "This action will:",
                        fontFamily = iosFont,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "• Transfer all transactions from \"$sourceSubCategoryName\" to \"$targetSubCategoryName\"",
                        fontFamily = iosFont,
                        fontSize = 12.sp
                    )

                    Text(
                        text = "• Permanently delete \"$sourceSubCategoryName\"",
                        fontFamily = iosFont,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "This action cannot be undone.",
                        fontFamily = iosFont,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .shadow(
                            elevation = 5.dp,
                            shape = RoundedCornerShape(12.dp),
                            spotColor = MaterialTheme.colorScheme.primary,
                            ambientColor = Color.Transparent
                        ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Merge",
                        fontFamily = iosFont,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.inverseSurface
                    ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = "Cancel",
                        fontFamily = iosFont,
                        fontWeight = FontWeight.Medium,
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surfaceBright,
            titleContentColor = MaterialTheme.colorScheme.inverseSurface,
            textContentColor = MaterialTheme.colorScheme.inverseSurface
        )
    }
}

@Composable
fun ConvertToMainCategoryConfirmationDialog(
    showDialog: Boolean,
    subCategoryName: String,
    newCategoryName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Convert to Main Category",
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Are you sure you want to convert \"$subCategoryName\" to a main category named \"$newCategoryName\"?",
                        fontFamily = iosFont,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "This action will:",
                        fontFamily = iosFont,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "• Create a new main category \"$newCategoryName\"",
                        fontFamily = iosFont,
                        fontSize = 12.sp
                    )

                    Text(
                        text = "• Transfer all transactions from subcategory to the new main category",
                        fontFamily = iosFont,
                        fontSize = 12.sp
                    )

                    Text(
                        text = "• Remove \"$subCategoryName\" from its current parent category",
                        fontFamily = iosFont,
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "This action cannot be undone.",
                        fontFamily = iosFont,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .shadow(
                            elevation = 5.dp,
                            shape = RoundedCornerShape(12.dp),
                            spotColor = MaterialTheme.colorScheme.primary,
                            ambientColor = Color.Transparent
                        ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Convert",
                        fontFamily = iosFont,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.inverseSurface
                    ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = "Cancel",
                        fontFamily = iosFont,
                        fontWeight = FontWeight.Medium,
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surfaceBright,
            titleContentColor = MaterialTheme.colorScheme.inverseSurface,
            textContentColor = MaterialTheme.colorScheme.inverseSurface
        )
    }
}
