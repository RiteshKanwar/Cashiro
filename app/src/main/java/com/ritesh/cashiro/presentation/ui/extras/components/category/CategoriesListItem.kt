package com.ritesh.cashiro.presentation.ui.extras.components.category

import EditSubCategorySheet
import android.widget.Toast
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat
import com.ritesh.cashiro.R
import com.ritesh.cashiro.data.local.entity.CategoryEntity
import com.ritesh.cashiro.data.local.entity.SubCategoryEntity
import com.ritesh.cashiro.domain.repository.TransactionStats
import com.ritesh.cashiro.domain.utils.AppEventBus
import com.ritesh.cashiro.domain.utils.CategoryEvent
import com.ritesh.cashiro.domain.utils.icons
import com.ritesh.cashiro.presentation.effects.VerticalStretchedOverscroll
import com.ritesh.cashiro.presentation.ui.extras.components.extras.CategoryDeletionDialogs
import com.ritesh.cashiro.presentation.ui.features.categories.CategoryScreenEvent
import com.ritesh.cashiro.presentation.ui.features.categories.CategoryScreenState
import com.ritesh.cashiro.presentation.ui.features.categories.CategoryScreenViewModel
import com.ritesh.cashiro.presentation.ui.features.categories.SubCategoryEvent
import com.ritesh.cashiro.presentation.ui.features.categories.SubCategoryState
import com.ritesh.cashiro.presentation.ui.features.categories.SubCategoryViewModel
import com.ritesh.cashiro.presentation.ui.theme.Latte_Maroon
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Green
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Mauve
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Peach
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Teal
import com.ritesh.cashiro.presentation.ui.theme.iosFont
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CategoryListItem(
    categoryUiState: CategoryScreenState,
    onCategoryEvent: (CategoryScreenEvent) -> Unit,
    subCategoryUiState: SubCategoryState,
    onSubCategoryEvent: (SubCategoryEvent) -> Unit,
    getTransactionStatsForCategory: (categoryId: Int, onResult: (TransactionStats) -> Unit) -> Unit,
    getTransactionStatsForSubCategory: (subCategoryId: Int, onResult: (TransactionStats) -> Unit) -> Unit,
    setSelectedCategoryId: (Int?) -> Unit,
    items: List<CategoryEntity>,
    onMove: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val themeColors = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
//    val state by categoryViewModel.state.collectAsState()
    val categoriesWithSubCategories = categoryUiState.categoriesWithSubCategories

    val coroutineScope = rememberCoroutineScope()
    val overscrollEffect = remember(coroutineScope) { VerticalStretchedOverscroll(coroutineScope) }

    // For haptic feedback
    val view = LocalView.current

    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        onMove(from.index, to.index)
        ViewCompat.performHapticFeedback(
            view,
            HapticFeedbackConstantsCompat.SEGMENT_FREQUENT_TICK
        )
    }

    if (items.isEmpty()) {
        EmptyCategories(modifier)
        return
    }


    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .overscroll(overscrollEffect)
            .scrollable(
                orientation = Orientation.Vertical,
                reverseDirection = true,
                state = lazyListState,
                overscrollEffect = overscrollEffect
            ),
        state = lazyListState,
        userScrollEnabled = false,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        items(
            items = items,
            key = {item -> item.id } // Unique key for item tracking
        ){ item ->
            ReorderableItem(reorderableLazyListState, key = item.id) { isDragging ->

                val swipeOffset = remember { Animatable(0f) }
                val dismissThreshold = 300f

                // Animated gradient for the neon glow effect
                val infiniteTransition = rememberInfiniteTransition(label = "Glow animation")
                val animatedColors = infiniteTransition.animateColor(
                    initialValue = Latte_Maroon,
                    targetValue = Macchiato_Mauve,
                    animationSpec = infiniteRepeatable(
                        animation = keyframes {
                            durationMillis = 3000 // Total duration for the animation cycle
                            Latte_Maroon at 0 using LinearEasing
                            Macchiato_Mauve at 500 using LinearEasing
                            Macchiato_Teal at 1000 using LinearEasing // Add more colors in the sequence
                            Macchiato_Green at 1500 using LinearEasing
                            Macchiato_Peach at 2000 using LinearEasing
                        },
                        repeatMode = RepeatMode.Reverse
                    ), label = "Glow animation"
                )

                // Get half of the screen height
                val configuration = LocalConfiguration.current
                val screenHeight = configuration.screenHeightDp.dp
                val screenWidth = configuration.screenWidthDp.dp
                val usedScreenHeight = screenHeight / 1.2f
                val usedScreenWidth = screenWidth - 10.dp
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                val iconSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                var isSheetOpen = rememberSaveable {
                    mutableStateOf(false)
                }
                var isIconSheetOpen = rememberSaveable {
                    mutableStateOf(false)
                }

                val subCategories = categoriesWithSubCategories
                    .find { it.category.id == item.id }
                    ?.subCategories ?: emptyList()

                // Animate elevation for dragged items
                val elevation by animateDpAsState(
                    targetValue = if (isDragging) 25.dp else 0.dp,
                    label = "ElevationAnimation"
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                        .zIndex(if (isDragging) 10f else 0f)
                        .then(
                            if (subCategories.isNotEmpty()) {
                                Modifier.height(160.dp)
                            } else {
                                Modifier.height(104.dp)
                            }
                        )
                        .clickable { isSheetOpen.value = true }
                ) {
                    // Background for delete action
                    if (!isDragging) {
                        ShowDeleteCategoryButton(translateY = 0f)
                    }
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

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer { translationY = 0f }
                            .offset { IntOffset(swipeOffset.value.toInt(), 0) }
                            .then(
                                if (isDragging) {
                                    Modifier.shadow(
                                        elevation = elevation,
                                        shape = RoundedCornerShape(20.dp),
                                        spotColor = animatedColors.value,
                                        ambientColor = animatedColors.value
                                    )
                                } else {
                                    Modifier
                                }
                            )
                            .background(themeColors.surface, RoundedCornerShape(20.dp))
                            .longPressDraggableHandle(
                                onDragStarted = {
                                    ViewCompat.performHapticFeedback(
                                        view,
                                        HapticFeedbackConstantsCompat.GESTURE_START
                                    )
                                },
                                onDragStopped = {
                                    ViewCompat.performHapticFeedback(
                                        view,
                                        HapticFeedbackConstantsCompat.GESTURE_END
                                    )
                                },
                            )
                            .then(
                                if (isDragging) {
                                    Modifier.border(
                                        width = 2.dp,
                                        color = animatedColors.value,
                                        shape = RoundedCornerShape(20.dp)
                                    )
                                } else {
                                    Modifier
                                }
                            )
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures(
                                    onDragEnd = {
                                        scope.launch {
                                            if (swipeOffset.value < -dismissThreshold && item.id != 1) {
                                                // Use enhanced deletion instead of direct deletion
                                                onCategoryEvent(
                                                    CategoryScreenEvent.InitiateCategoryDeletion(item)
                                                )
                                                swipeOffset.animateTo(0f)
                                            } else if (item.id == 1) {
                                                toastMessage = "Cannot delete, this category is needed for transfer transactions"
                                                showToast = true
                                                swipeOffset.animateTo(0f)
                                            } else {
                                                swipeOffset.animateTo(0f)
                                            }
                                        }
                                    }
                                ) { _, dragAmount ->
                                    scope.launch {
                                        val newOffset = swipeOffset.value + dragAmount
                                        if (newOffset <= 0) swipeOffset.snapTo(newOffset)
                                    }
                                }
                            }
                    ) {
                        CategoryCardLayout(
                            item = item,
                            subCategories = subCategories,
                            categoryUiState = categoryUiState,
                            onCategoryEvent = onCategoryEvent,
                            subCategoryUiState = subCategoryUiState,
                            onSubCategoryEvent = onSubCategoryEvent,
                            setSelectedCategoryId = setSelectedCategoryId,
                            getTransactionStatsForCategory = getTransactionStatsForCategory,
                            getTransactionStatsForSubCategory = getTransactionStatsForSubCategory,
                        )
                    }

                    if (isSheetOpen.value) {
                        EditCategorySheet(
                            sheetState = sheetState,
                            isSheetOpen = isSheetOpen,
                            isIconSheetOpen = isIconSheetOpen,
                            iconSheetState = iconSheetState,
                            selectedIconId = item.categoryIconId,
                            customColor = Color(item.boxColor),
                            categoryName = item.name,
                            usedScreenWidth = usedScreenWidth,
                            usedScreenHeight = usedScreenHeight,
                            isUpdate = true,
                            currentCategoryEntity = item,
                            isSpecialCategory = item.id == 1,
                            categoryUiState = categoryUiState,
                            onCategoryEvent = onCategoryEvent,
                            subCategoryUiState = subCategoryUiState,
                            onSubCategoryEvent = onSubCategoryEvent,
                            getTransactionStatsForCategory = getTransactionStatsForCategory,
                            getTransactionStatsForSubCategory = getTransactionStatsForSubCategory
                        )
                    }
                }
            }
        }
        item {
            Spacer(modifier = Modifier.height(100.dp))
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

@Composable
fun EmptyCategories(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(
                    id =  R.drawable.empty_category_list
                ),
                contentDescription = "Empty Category List Icon",
                modifier = Modifier.size(300.dp)
            )
            Text(
                text = "No categories available",
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontSize = 24.sp,
                    fontFamily = iosFont,
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ShowDeleteCategoryButton(translateY: Float){
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { translationY = translateY }
            .background(color = MaterialTheme.colorScheme.onError, shape = RoundedCornerShape(22.dp))
            .padding(15.dp),
        contentAlignment = Alignment.CenterEnd // Align text to the end
    ) {
        Text(
            text = "Delete",
            color = Color.White,
            fontFamily = iosFont,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CategoryCardLayout(
    item: CategoryEntity,
    subCategories: List<SubCategoryEntity> = emptyList(),
    categoryUiState: CategoryScreenState,
    onCategoryEvent: (CategoryScreenEvent) -> Unit,
    subCategoryUiState: SubCategoryState,
    onSubCategoryEvent: (SubCategoryEvent) -> Unit,
    setSelectedCategoryId: (Int?) -> Unit,
    getTransactionStatsForCategory: (categoryId: Int, onResult: (TransactionStats) -> Unit) -> Unit,
    getTransactionStatsForSubCategory: (subCategoryId: Int, onResult: (TransactionStats) -> Unit) -> Unit,
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val usedScreenHeight = screenHeight / 1.2f
    val usedScreenWidth = screenWidth - 10.dp

    // State variables for subcategory sheet
    val subCategorySheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val subCategoryIconSheet = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSubCategorySheet = remember { mutableStateOf(false) }
    var showSubCategoryIconSheet = remember { mutableStateOf(false) }
    var selectedSubCategory by remember { mutableStateOf<SubCategoryEntity?>(null) }
    var isNewSubCategory by remember { mutableStateOf(false) }

    // FIX: Use local state instead of global SubCategoryViewModel state for display
    // This prevents subcategories from other categories showing up in this card
    var localSubCategories by remember(item.id) { mutableStateOf(subCategories) }

    // Update local state when the parent passes new subcategories
    LaunchedEffect(subCategories) {
        localSubCategories = subCategories
    }

    // Listen for subcategory updates specific to this category
    LaunchedEffect(item.id) {
        AppEventBus.events.collect { event ->
            when (event) {
                is CategoryEvent.SubCategoriesUpdated -> {
                    if (event.categoryId == item.id) {
                        // Refresh subcategories for this specific category only
                        onCategoryEvent(CategoryScreenEvent.FetchAllCategoriesWithSubCategories)
                    }
                }
                is CategoryEvent.SubCategoryAdded -> {
                    if (event.categoryId == item.id) {
                        onCategoryEvent(CategoryScreenEvent.FetchAllCategoriesWithSubCategories)
                    }
                }
                is CategoryEvent.SubCategoryDeleted -> {
                    if (event.categoryId == item.id) {
                        onCategoryEvent(CategoryScreenEvent.FetchAllCategoriesWithSubCategories)
                    }
                }
                is CategoryEvent.SubCategoryUpdated -> {
                    if (event.categoryId == item.id) {
                        onCategoryEvent(CategoryScreenEvent.FetchAllCategoriesWithSubCategories)
                    }
                }
            }
        }
    }

    // Use the local subcategories for display, sorted by position
    val displaySubCategories = localSubCategories.sortedBy { it.position }

    Box() {
        // Subcategories chip list
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = MaterialTheme.colorScheme.surfaceBright,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(start = 12.dp , bottom = 12.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            if (displaySubCategories.isNotEmpty()) {
                // LazyRow for subcategories - now uses local state
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    itemsIndexed(
                        items = displaySubCategories,
                        key = { index, subCategory -> subCategory.id }
                    ) { index, subCategory ->
                        SubCategoryChip(
                            subCategory = subCategory,
                            onSubCategoryClick = { clickedSubCategory ->
                                showSubCategorySheet.value = true
                                onCategoryEvent(CategoryScreenEvent.GetSubCategoryById(clickedSubCategory.id))
                                selectedSubCategory = clickedSubCategory
                                isNewSubCategory = false

                                // FIX: Only set the editing category in SubCategoryViewModel when actually editing
                                setSelectedCategoryId(item.id)
                            }
                        )
                    }
                }
            }
        }

        // Main category row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
                .align(Alignment.TopCenter),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(end = 10.dp)
                    .size(64.dp)
                    .shadow(10.dp, RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(item.boxColor))
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(
                        id = icons.find { it.id == item.categoryIconId }?.resourceId
                            ?: R.drawable.type_beverages_beer
                    ),
                    contentDescription = "Category Icon",
                    modifier = Modifier.size(40.dp)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Main Category Name
                Text(
                    text = item.name,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.inverseSurface,
                    fontSize = 20.sp,
                    maxLines = 2,
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Medium
                )
            }

            Icon(
                painter = painterResource(R.drawable.drag_handle_bulk),
                contentDescription = "Drag Handle",
                tint = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.5f)
            )
        }


    }

    // Show EditSubCategorySheet when needed
    if (showSubCategorySheet.value) {
        EditSubCategorySheet(
            showSubCategorySheet = showSubCategorySheet,
            showSubCategoryIconSheet = showSubCategoryIconSheet,
            subCategorySheet = subCategorySheet,
            subCategoryIconSheet = subCategoryIconSheet,
            subCategoryId = selectedSubCategory?.id,
            parentCategoryId = if (isNewSubCategory) item.id else null,
            isNewSubCategory = isNewSubCategory,
            usedScreenWidth = usedScreenWidth,
            usedScreenHeight = usedScreenHeight,
            onDismiss = {
                showSubCategorySheet.value = false
                // FIX: Clear the selection when closing the sheet
                setSelectedCategoryId(null)
            },
            categoryUiState = categoryUiState,
            onCategoryEvent = onCategoryEvent,
            subCategoryUiState = subCategoryUiState,
            onSubCategoryEvent = onSubCategoryEvent,
            getTransactionStatsForCategory = getTransactionStatsForCategory ,
            getTransactionStatsForSubCategory = getTransactionStatsForSubCategory
        )
    }
}

@Composable
private fun SubCategoryChip(
    subCategory: SubCategoryEntity,
    onSubCategoryClick: (SubCategoryEntity) -> Unit = {}
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(subCategory.boxColor?: 0).copy(alpha = 0.2f))
//            .border(width = 1.dp, color = Color(subCategory.boxColor?: 0), shape = RoundedCornerShape(12.dp))
            .clickable { onSubCategoryClick(subCategory) }

            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Add icon if available
            if (subCategory.subcategoryIconId != null) {
                Image(
                    painter = painterResource(
                        id = icons.find { it.id == subCategory.subcategoryIconId }?.resourceId
                            ?: R.drawable.type_beverages_beer
                    ),
                    contentDescription = "Subcategory Icon",
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = subCategory.name,
                fontSize = 10.sp,
                fontFamily = iosFont,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.inverseSurface
            )
        }
    }
}
