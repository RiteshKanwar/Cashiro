package com.ritesh.cashiro.presentation.ui.extras.components.category

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat
import com.ritesh.cashiro.R
import com.ritesh.cashiro.data.local.entity.SubCategoryEntity
import com.ritesh.cashiro.domain.repository.TransactionStats
import com.ritesh.cashiro.domain.utils.icons
import com.ritesh.cashiro.presentation.ui.extras.components.extras.CategoryDeletionDialogs
import com.ritesh.cashiro.presentation.ui.features.categories.CategoryScreenEvent
import com.ritesh.cashiro.presentation.ui.features.categories.CategoryScreenState
import com.ritesh.cashiro.presentation.ui.features.categories.CategoryScreenViewModel
import com.ritesh.cashiro.presentation.ui.features.categories.SubCategoryEvent
import com.ritesh.cashiro.presentation.ui.features.categories.SubCategoryState
import com.ritesh.cashiro.presentation.ui.features.categories.SubCategoryViewModel
import com.ritesh.cashiro.presentation.ui.theme.iosFont
import kotlinx.coroutines.delay
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SubcategoriesListItem(
    subCategories: List<SubCategoryEntity>,
    categoryUiState: CategoryScreenState,
    onCategoryEvent: (CategoryScreenEvent) -> Unit,
    subCategoryUiState: SubCategoryState,
    onSubCategoryEvent: (SubCategoryEvent) -> Unit,
    getTransactionStatsForCategory: (categoryId: Int, onResult: (TransactionStats) -> Unit) -> Unit,
    getTransactionStatsForSubCategory: (subCategoryId: Int, onResult: (TransactionStats) -> Unit) -> Unit,
    onMove: (Int, Int) -> Unit,
    onSubCategoryClick: (SubCategoryEntity) -> Unit,
    onAddNewSubCategory: () -> Unit,
    themeColors: ColorScheme
) {
    val pendingDeletion = subCategoryUiState.pendingDeletion
//    val categoryState by categoryViewModel.state.collectAsState()
    var countdown by remember { mutableIntStateOf(5) }

    // Start countdown when pendingDeletion is set
    LaunchedEffect(pendingDeletion) {
        if (pendingDeletion != null) {
            // Start countdown from 5
            countdown = 5

            // Create a separate coroutine for the countdown
            while (countdown > 0) {
                delay(1000) // Wait 1 second
                countdown -= 1
            }
        } else {
            // Reset countdown when pendingDeletion is cleared
            countdown = 0
        }
    }

    // For haptic feedback
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = themeColors.surface, shape = RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 5.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                // Existing Manage Subcategories section
                Text(
                    text = "Manage Subcategories",
                    fontSize = 16.sp,
                    color = themeColors.inverseSurface,
                    fontFamily = iosFont,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth().padding( bottom = 8.dp)
                )
                Text(
                    text = "Reorder, Add, Delete or Edit subcategories",
                    fontSize = 12.sp,
                    color = themeColors.inverseSurface.copy(0.7f),
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Medium
                )
            }

            // Show undo button if there's a pending deletion
            if (pendingDeletion != null) {
                TextButton(
                    onClick = {
                        onSubCategoryEvent(SubCategoryEvent.UndoSubCategoryDeletion)
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.refresh_bulk), // You'll need this icon
                            contentDescription = "Undo",
                            tint = themeColors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Undo",
                            color = themeColors.primary,
                            fontSize = 14.sp,
                            fontFamily = iosFont,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
        // Show pending deletion message with countdown
        pendingDeletion?.let {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = themeColors.errorContainer.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "\"${pendingDeletion!!.name}\" will be deleted in",
                        color = themeColors.onErrorContainer,
                        fontSize = 12.sp,
                        fontFamily = iosFont,
                        modifier = Modifier.weight(1f)
                    )

                    // Countdown badge
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                color = themeColors.error,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = countdown.toString(),
                            color = themeColors.surface,
                            fontSize = 12.sp,
                            fontFamily = iosFont,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        if (subCategories.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No subcategories available",
                    fontSize = 14.sp,
                    color = themeColors.inverseOnSurface.copy(alpha = 0.7f),
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            // Setup reorderable state
            val lazyListState = rememberLazyListState()
            val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
                onMove(from.index, to.index)
                ViewCompat.performHapticFeedback(
                    view,
                    HapticFeedbackConstantsCompat.SEGMENT_FREQUENT_TICK
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp),
                state = lazyListState,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = subCategories,
                    key = { item -> item.id } // Unique key for item tracking
                ) { item ->
                    ReorderableItem(reorderableLazyListState, key = item.id) { isDragging ->
                        // Animated colors for dragging effect
                        val infiniteTransition = rememberInfiniteTransition(label = "Drag animation")
                        val animatedColor by infiniteTransition.animateColor(
                            initialValue = Color(item.boxColor ?: themeColors.primary.toArgb()),
                            targetValue = themeColors.primary,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "Color animation"
                        )

                        SubcategoryCardLayout(
                            item = item,
                            isDragging = isDragging,
                            borderColor = if (isDragging) animatedColor else null,
                            onDelete = {
                                onCategoryEvent(
                                    CategoryScreenEvent.InitiateSubCategoryDeletion(item)
                                )
                                       },
                            onClick = { onSubCategoryClick(item) },
                            themeColors = themeColors,
                            modifier = Modifier.longPressDraggableHandle(
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
                        )
                    }
                }
            }
        }
        Button(
            onClick = onAddNewSubCategory, // Call the provided callback
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = themeColors.surfaceBright,
                contentColor = themeColors.inverseSurface
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.add_circle_bulk),
                    contentDescription = "Add Subcategory",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add New Subcategory",
                    fontSize = 14.sp,
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
    CategoryDeletionDialogs(
        state = categoryUiState,
        onEvent = onCategoryEvent,
        subCategories = subCategories,
        themeColors = themeColors,
        getTransactionStatsForCategory = getTransactionStatsForCategory,
        getTransactionStatsForSubCategory = getTransactionStatsForSubCategory
    )
}

@Composable
private fun SubcategoryCardLayout(
    item: SubCategoryEntity,
    isDragging: Boolean,
    borderColor: Color? = null,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    themeColors: ColorScheme,
    modifier: Modifier,
) {
    // Animate elevation for dragged items
    val elevation by animateDpAsState(
        targetValue = if (isDragging) 8.dp else 2.dp,
        label = "ElevationAnimation"
    )


    Box(
        modifier = modifier
            .shadow(elevation = elevation, shape = RoundedCornerShape(16.dp), spotColor = Color.Black, ambientColor = Color.Black)
            .fillMaxWidth()
            .graphicsLayer {
                alpha = if (isDragging) 0.9f else 1f
            }
            .background(themeColors.surfaceBright, RoundedCornerShape(16.dp))
            .border(
                width = if (isDragging) 2.dp else 0.dp,
                color = borderColor ?: Color(item.boxColor ?: 0).copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick) // Add clickable modifier
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left section - Icon and name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Subcategory icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(item.boxColor ?: 0))
                        .padding(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(
                            id = icons.find { it.id == item.subcategoryIconId }?.resourceId
                                ?: R.drawable.type_beverages_beer
                        ),
                        contentDescription = "Subcategory Icon",
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Subcategory name
                Text(
                    text = item.name,
                    fontSize = 16.sp,
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Medium,
                    color = themeColors.inverseSurface
                )
            }

            // Right section - Delete and drag handle icons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Delete button
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.delete_bulk),
                        contentDescription = "Delete Subcategory",
                        tint = themeColors.error,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Drag handle - Long press to activate
                Box(
                    modifier = modifier
                        .size(32.dp)
                        .background(
                            color = themeColors.surfaceVariant.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.drag_handle_bulk),
                        contentDescription = "Long press to reorder",
                        tint = themeColors.inverseSurface.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}