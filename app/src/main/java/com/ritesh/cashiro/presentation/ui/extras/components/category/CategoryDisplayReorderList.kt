package com.ritesh.cashiro.presentation.ui.extras.components.category

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat
import com.ritesh.cashiro.data.local.entity.CategoryEntity
import com.ritesh.cashiro.domain.repository.TransactionStats
import com.ritesh.cashiro.presentation.effects.VerticalStretchedOverscroll
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
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CategoryDisplayReorderList(
    subCategoryUiState: SubCategoryState,
    categoryUiState: CategoryScreenState,
    onCategoryEvent: (CategoryScreenEvent) -> Unit,
    onSubCategoryEvent: (SubCategoryEvent) -> Unit,
    getTransactionStatsForCategory: (categoryId: Int, onResult: (TransactionStats) -> Unit) -> Unit,
    getTransactionStatsForSubCategory: (subCategoryId: Int, onResult: (TransactionStats) -> Unit) -> Unit,
    setSelectedCategoryId: (Int?) -> Unit,
    items: List<CategoryEntity>,
    onMove: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val themeColors = MaterialTheme.colorScheme

    val coroutineScope = rememberCoroutineScope()
    val overscrollEffect = remember(coroutineScope) { VerticalStretchedOverscroll(coroutineScope) }

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

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top) {
        Spacer(modifier = Modifier.size(20.dp))
        Text(
            text = "Re-Arrange Categories",
            textAlign = TextAlign.Center,
            fontFamily = iosFont,
            fontSize = 20.sp,
            color = themeColors.inverseSurface,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth().background(themeColors.background).zIndex(6f)
        )

        Text(
            text = "Press and Hold to drag categories",
            textAlign = TextAlign.Center,
            fontFamily = iosFont,
            fontSize = 12.sp,
            color = themeColors.inverseSurface.copy(0.5f),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.fillMaxWidth().background(themeColors.background).zIndex(6f).padding(bottom = 10.dp)
        )

        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(16.dp))
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
                key = {  item -> item.id } // Unique key for item tracking
            ){ item ->
                ReorderableItem(reorderableLazyListState, key = item.id) { isDragging ->
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
                    // Animate elevation for dragged items
                    val elevation by animateDpAsState(
                        targetValue = if (isDragging) 25.dp else 0.dp,
                        label = "ElevationAnimation"
                    )

                    // Prepare category details for editing when the sheet is opened
                    LaunchedEffect(isSheetOpen.value) {
                        if (isSheetOpen.value) {
                            onCategoryEvent(CategoryScreenEvent.GetCategoryById(item.id))
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem()
                            .zIndex(if (isDragging) 5f else 0f) // Ensure dragged item always has the highest z-index
                            .height(90.dp)
                            .clickable { isSheetOpen.value = true }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer { translationY = 0f }
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
                        ) {
                            CategoryCardLayout(
                                item = item,
                                subCategories = emptyList(),
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
        }
    }
}
