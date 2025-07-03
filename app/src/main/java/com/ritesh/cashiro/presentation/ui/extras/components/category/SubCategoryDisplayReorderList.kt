package com.ritesh.cashiro.presentation.ui.extras.components.category

import androidx.compose.animation.animateColor
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
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat
import com.ritesh.cashiro.R
import com.ritesh.cashiro.data.local.entity.SubCategoryEntity
import com.ritesh.cashiro.domain.utils.icons
import com.ritesh.cashiro.presentation.effects.VerticalStretchedOverscroll
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
fun SubCategoryDisplayReorderList(
    items: List<SubCategoryEntity>,
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
        EmptySubCategories(modifier)
        return
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.size(20.dp))
        Text(
            text = "Re-Arrange Subcategories",
            textAlign = TextAlign.Center,
            fontFamily = iosFont,
            fontSize = 20.sp,
            color = themeColors.inverseSurface,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .fillMaxWidth()
                .background(themeColors.background)
                .zIndex(6f)
        )

        Text(
            text = "Press and Hold to drag subcategories",
            textAlign = TextAlign.Center,
            fontFamily = iosFont,
            fontSize = 12.sp,
            color = themeColors.inverseSurface.copy(0.5f),
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .fillMaxWidth()
                .background(themeColors.background)
                .zIndex(6f)
                .padding(bottom = 10.dp)
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = items,
                key = { item -> item.id } // Unique key for item tracking
            ) { item ->
                ReorderableItem(reorderableLazyListState, key = item.id) { isDragging ->
                    // Animated gradient for the neon glow effect
                    val infiniteTransition = rememberInfiniteTransition(label = "Glow animation")
                    val animatedColors = infiniteTransition.animateColor(
                        initialValue = Color(item.boxColor ?: Latte_Maroon.toArgb()),
                        targetValue = Macchiato_Mauve,
                        animationSpec = infiniteRepeatable(
                            animation = keyframes {
                                durationMillis = 2500 // Total duration for the animation cycle
                                Color(item.boxColor ?: Latte_Maroon.toArgb()) at 0 using LinearEasing
                                Macchiato_Mauve at 400 using LinearEasing
                                Macchiato_Teal at 800 using LinearEasing
                                Macchiato_Green at 1200 using LinearEasing
                                Macchiato_Peach at 1600 using LinearEasing
                                Color(item.boxColor ?: Latte_Maroon.toArgb()) at 2000 using LinearEasing
                            },
                            repeatMode = RepeatMode.Reverse
                        ), label = "Glow animation"
                    )

                    // Get screen dimensions for sheet sizing
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
                        targetValue = if (isDragging) 20.dp else 0.dp,
                        label = "ElevationAnimation"
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem()
                            .zIndex(if (isDragging) 5f else 0f)
                            .height(80.dp)
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
                                            shape = RoundedCornerShape(16.dp),
                                            spotColor = animatedColors.value,
                                            ambientColor = animatedColors.value
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                                .background(themeColors.surface, RoundedCornerShape(16.dp))
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
                                            shape = RoundedCornerShape(16.dp)
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                        ) {
                            SubCategoryCardLayout(
                                item = item,
                                themeColors = themeColors,
                                isCompact = true
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubCategoryCardLayout(
    item: SubCategoryEntity,
    themeColors: ColorScheme,
    isCompact: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                horizontal = if (isCompact) 16.dp else 20.dp,
                vertical = if (isCompact) 12.dp else 16.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left section - Icon and name
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Subcategory icon
            Box(
                modifier = Modifier
                    .size(if (isCompact) 44.dp else 48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(item.boxColor ?: 0))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(
                        id = icons.find { it.id == item.subcategoryIconId }?.resourceId
                            ?: R.drawable.type_beverages_beer
                    ),
                    contentDescription = "Subcategory Icon",
                    modifier = Modifier.size(if (isCompact) 28.dp else 32.dp)
                )
            }

            // Subcategory name and details
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = item.name,
                    fontSize = if (isCompact) 16.sp else 18.sp,
                    fontFamily = iosFont,
                    fontWeight = FontWeight.SemiBold,
                    color = themeColors.inverseSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (!isCompact) {
                    Text(
                        text = "Subcategory",
                        fontSize = 12.sp,
                        fontFamily = iosFont,
                        fontWeight = FontWeight.Medium,
                        color = themeColors.inverseSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // Right section - Drag handle indicator
        Icon(
            painter = painterResource(R.drawable.drag_handle_bulk),
            contentDescription = "Drag Handle",
            tint = themeColors.inverseSurface.copy(alpha = 0.4f),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun EmptySubCategories(modifier: Modifier = Modifier) {
    val themeColors = MaterialTheme.colorScheme

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.category_bulk),
                contentDescription = "No Subcategories",
                tint = themeColors.inverseSurface.copy(alpha = 0.3f),
                modifier = Modifier.size(64.dp)
            )

            Text(
                text = "No Subcategories",
                fontSize = 20.sp,
                fontFamily = iosFont,
                fontWeight = FontWeight.SemiBold,
                color = themeColors.inverseSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Add some subcategories to start organizing",
                fontSize = 14.sp,
                fontFamily = iosFont,
                fontWeight = FontWeight.Medium,
                color = themeColors.inverseSurface.copy(alpha = 0.4f),
                textAlign = TextAlign.Center
            )
        }
    }
}