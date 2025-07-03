package com.ritesh.cashiro.presentation.ui.features.categories

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ritesh.cashiro.domain.repository.TransactionStats
import com.ritesh.cashiro.presentation.ui.extras.components.extras.CategoryDeletionDialogs
import com.ritesh.cashiro.presentation.ui.extras.components.category.CategoryListItem
import com.ritesh.cashiro.presentation.ui.extras.components.extras.CustomTitleTopAppBar
import com.ritesh.cashiro.presentation.ui.theme.iosFont
import com.ritesh.cashiro.presentation.ui.extras.components.category.EditCategorySheet
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import kotlinx.coroutines.delay
import kotlin.Int
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onBackClicked: () -> Unit,
    categoryUiState: CategoryScreenState,
    onCategoryEvent: (CategoryScreenEvent) -> Unit,
    subCategoryUiState: SubCategoryState,
    onSubCategoryEvent: (SubCategoryEvent) -> Unit,
    getTransactionStatsForCategory: (categoryId: Int, onResult: (TransactionStats) -> Unit) -> Unit,
    getTransactionStatsForSubCategory: (subCategoryId: Int, onResult: (TransactionStats) -> Unit) -> Unit,
    updateCollapsingFraction: (Float) -> Unit,
    setSelectedCategoryId: (Int?) -> Unit,
    screenTitle: String,
    previousScreenTitle: String
) {
    val themeColors = MaterialTheme.colorScheme

    // Get half of the screen height
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val usedScreenHeight = screenHeight / 1.2f
    val usedScreenWidth = screenWidth - 10.dp

    // Collect state from the ViewModel
//    val state by categoryViewModel.state.collectAsState()
    val selectedIconId = categoryUiState.selectedIconId
    val customColor = categoryUiState.customBoxColor
    val categoryName = categoryUiState.categoryName
    val categories = categoryUiState.categories

    // Scaffold State for Scroll Behavior
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scrollBehaviorSmall = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val scrollState = rememberScrollState()
    // For haptic feedback
    val haptic = LocalHapticFeedback.current
    val hazeState = remember { HazeState() }

    LaunchedEffect(scrollBehavior.state.collapsedFraction) {
        updateCollapsingFraction(scrollBehavior.state.collapsedFraction)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CustomTitleTopAppBar(
                scrollBehaviorLarge = scrollBehavior,
                scrollBehaviorSmall = scrollBehaviorSmall,
                title = screenTitle,
                previousScreenTitle = previousScreenTitle,
                onBackClick = onBackClicked,
                hasBackButton = true,
                hazeState = hazeState
            )
        },
        bottomBar = {
            AddCategoryBottomButton(
                onCategoryEvent = onCategoryEvent,
                selectedIconId = selectedIconId ?: 0,
                customColor = customColor ?: Color.Transparent,
                categoryName = categoryName,
                usedScreenHeight = usedScreenHeight,
                usedScreenWidth = usedScreenWidth,
                themeColors = themeColors,
                categoryUiState = categoryUiState,
                subCategoryUiState = subCategoryUiState,
                onSubCategoryEvent = onSubCategoryEvent,
                getTransactionStatsForCategory = getTransactionStatsForCategory,
                getTransactionStatsForSubCategory = getTransactionStatsForSubCategory,
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(
                    start = innerPadding.calculateLeftPadding(LayoutDirection.Ltr),
                    end = innerPadding.calculateRightPadding(LayoutDirection.Rtl)
                )
                .haze(state = hazeState)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(Modifier.offset(y = categoryUiState.currentOffset)) {
                    CategoryListItem(
                        items = categories,
                        onMove = { fromIndex, toIndex ->
                            if (fromIndex in categories.indices && toIndex in categories.indices) {
                                // Use the event-based pattern for reordering
                                onCategoryEvent(
                                    CategoryScreenEvent.ReorderCategories(
                                        fromIndex = fromIndex,
                                        toIndex = toIndex
                                    )
                                )
                            } else {
                                Log.e(
                                    "CategoryListItem",
                                    "Invalid indices: fromIndex=$fromIndex, toIndex=$toIndex"
                                )
                            }
                        },
                        modifier = Modifier.height(screenHeight + categoryUiState.currentOffset),
                        categoryUiState = categoryUiState,
                        onCategoryEvent = onCategoryEvent,
                        subCategoryUiState = subCategoryUiState,
                        onSubCategoryEvent = onSubCategoryEvent,
                        getTransactionStatsForCategory = getTransactionStatsForCategory,
                        getTransactionStatsForSubCategory = getTransactionStatsForSubCategory,
                        setSelectedCategoryId = setSelectedCategoryId,
                    )
                }
            }
            Spacer(
                modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                themeColors.background.copy(0.5f),
                                themeColors.background
                            )
                        )
                    )
                    .align(Alignment.BottomCenter)
            )
        }
        CategoryDeletionDialogs(
            state = categoryUiState,
            onEvent = onCategoryEvent,
            themeColors = themeColors,
            getTransactionStatsForCategory = getTransactionStatsForCategory,
            getTransactionStatsForSubCategory = getTransactionStatsForSubCategory,
        )
    }

}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryBottomButton(
    categoryUiState: CategoryScreenState,
    onCategoryEvent: (CategoryScreenEvent) -> Unit,
    subCategoryUiState: SubCategoryState,
    onSubCategoryEvent: (SubCategoryEvent) -> Unit,
    getTransactionStatsForCategory: (categoryId: Int, onResult: (TransactionStats) -> Unit) -> Unit,
    getTransactionStatsForSubCategory: (subCategoryId: Int, onResult: (TransactionStats) -> Unit) -> Unit,
    selectedIconId: Int,
    customColor: Color,
    categoryName: String,
    usedScreenHeight: Dp,
    usedScreenWidth: Dp,
    modifier: Modifier = Modifier,
    themeColors: ColorScheme
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val iconSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isSheetOpen = rememberSaveable {
        mutableStateOf(false)
    }
    var isIconSheetOpen = rememberSaveable {
        mutableStateOf(false)
    }

    // Prepare for adding a new category when the button is clicked
    LaunchedEffect(isSheetOpen.value) {
        if (isSheetOpen.value) {
            onCategoryEvent(CategoryScreenEvent.PrepareForNewCategory)
        }
    }

    Box(modifier = modifier.fillMaxWidth().navigationBarsPadding()) {
        Button(
            onClick = { isSheetOpen.value = true },
            modifier = modifier
                .padding(bottom = 10.dp, start = 20.dp, end = 20.dp)
                .height(56.dp)
                .fillMaxWidth()
                .shadow(
                    10.dp,
                    RoundedCornerShape(15.dp),
                    spotColor = themeColors.primary,
                    ambientColor = Color.Transparent
                ),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = themeColors.primary,
                contentColor = themeColors.inverseSurface
            )
        ) {
            Text(
                text = "Add Category",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = iosFont,
                color = Color.White
            )
        }
    }
    if (isSheetOpen.value) {
        EditCategorySheet(
            sheetState = sheetState,
            isSheetOpen = isSheetOpen,
            isIconSheetOpen = isIconSheetOpen,
            iconSheetState = iconSheetState,
            selectedIconId = selectedIconId,
            customColor = customColor,
            categoryName = categoryName,
            usedScreenWidth = usedScreenWidth,
            usedScreenHeight = usedScreenHeight,
            categoryUiState = categoryUiState,
            onCategoryEvent = onCategoryEvent,
            subCategoryUiState = subCategoryUiState,
            onSubCategoryEvent = onSubCategoryEvent,
            getTransactionStatsForCategory = getTransactionStatsForCategory,
            getTransactionStatsForSubCategory = getTransactionStatsForSubCategory,
        )
    }
}

