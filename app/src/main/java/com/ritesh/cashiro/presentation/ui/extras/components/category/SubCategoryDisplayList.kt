package com.ritesh.cashiro.presentation.ui.extras.components.category

import EditSubCategorySheet
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritesh.cashiro.R
import com.ritesh.cashiro.domain.repository.TransactionStats
import com.ritesh.cashiro.domain.utils.icons
import com.ritesh.cashiro.presentation.effects.VerticalStretchedOverscroll
import com.ritesh.cashiro.presentation.ui.extras.components.extras.SearchBar
import com.ritesh.cashiro.presentation.ui.features.add_transaction.AddTransactionEvent
import com.ritesh.cashiro.presentation.ui.features.add_transaction.AddTransactionScreenState
import com.ritesh.cashiro.presentation.ui.features.add_transaction.AddTransactionScreenViewModel
import com.ritesh.cashiro.presentation.ui.features.categories.CategoryScreenEvent
import com.ritesh.cashiro.presentation.ui.features.categories.CategoryScreenState
import com.ritesh.cashiro.presentation.ui.features.categories.CategoryScreenViewModel
import com.ritesh.cashiro.presentation.ui.features.categories.SubCategoryEvent
import com.ritesh.cashiro.presentation.ui.features.categories.SubCategoryState
import com.ritesh.cashiro.presentation.ui.features.categories.SubCategoryViewModel
import com.ritesh.cashiro.presentation.ui.theme.Latte_Maroon
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Peach
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Teal
import com.ritesh.cashiro.presentation.ui.theme.iosFont

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun SubCategoriesDisplayList(
    categoryId: Int,
    categoryUiState: CategoryScreenState,
    onCategoryEvent: (CategoryScreenEvent) -> Unit,
    getTransactionStatsForCategory: (categoryId: Int, onResult: (TransactionStats) -> Unit) -> Unit,
    getTransactionStatsForSubCategory: (subCategoryId: Int, onResult: (TransactionStats) -> Unit) -> Unit,
    subCategoryUiState: SubCategoryState,
    onSubCategoryEvent: (SubCategoryEvent) -> Unit,
    setSelectedCategoryId: (Int?) -> Unit,
    clearSelection: () -> Unit,
    transactionUiState: AddTransactionScreenState,
    onAddTransactionEvent: (AddTransactionEvent) -> Unit,
    modifier: Modifier = Modifier,
    onSubCategorySelected: () -> Unit,
    onSkipSubCategory: () -> Unit
) {
    val themeColors = MaterialTheme.colorScheme

    // Get screen dimensions
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val usedScreenHeight = screenHeight / 1.2f
    val usedScreenWidth = screenWidth - 10.dp

    // Collect state from ViewModels

    val transactionSubCategoryId = transactionUiState.transactionSubCategoryId
    val subCategories = subCategoryUiState.subCategories

    val currentCategory = categoryUiState.categories.find { it.id == categoryId }

    LaunchedEffect(categoryId) {
        setSelectedCategoryId(categoryId)
        onSubCategoryEvent(SubCategoryEvent.FetchSubCategoriesForCategory(categoryId))
    }
    DisposableEffect(Unit) {
        onDispose {
            clearSelection()
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val iconSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isSheetOpen = rememberSaveable { mutableStateOf(false) }
    var isIconSheetOpen = rememberSaveable { mutableStateOf(false) }

    val scrollOverScrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val overscrollEffect = remember(coroutineScope) { VerticalStretchedOverscroll(coroutineScope) }

    // Show empty state if no subcategories
    AnimatedVisibility(
        visible = subCategories.isEmpty(),
        enter = fadeIn() + slideInVertically() + expandVertically(),
        exit = slideOutVertically() + shrinkVertically() + fadeOut(),
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
//            Text(
//                text = "Select SubCategories",
//                textAlign = TextAlign.Center,
//                fontFamily = iosFont,
//                fontWeight = FontWeight.SemiBold,
//                fontSize = 18.sp,
//                color = themeColors.inverseSurface,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(vertical = 10.dp)
//            )
            // 🔥 NEW: Enhanced header with category name
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "${currentCategory?.name ?: "Category"} SubCategories",
                    textAlign = TextAlign.Center,
                    fontFamily = iosFont,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = themeColors.inverseSurface,
                )
            }

            // Empty subcategories state
            Column(
                modifier = modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(R.drawable.category_bulk),
                    contentDescription = "No SubCategories",
                    modifier = Modifier.size(64.dp),
                    tint = themeColors.inverseSurface.copy(alpha = 0.5f)
                )

                Text(
                    text = "No SubCategories Available",
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = themeColors.inverseSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 16.dp)
                )

                Text(
                    text = "Add a subcategory or skip to continue",
                    fontFamily = iosFont,
                    fontSize = 14.sp,
                    color = themeColors.inverseSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, start = 32.dp, end= 32.dp)
                )

                // Skip button
                Button(
                    onClick = onSkipSubCategory,
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .fillMaxWidth(0.6f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = themeColors.primary.copy(alpha = 0.1f),
                        contentColor = themeColors.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Skip SubCategory",
                        fontFamily = iosFont,
                        fontSize = 14.sp
                    )
                }
            }

            // Add subcategory button
            Button(
                onClick = { isSheetOpen.value = true },
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 25.dp)
                    .shadow(
                        10.dp,
                        RoundedCornerShape(16.dp),
                        spotColor = themeColors.primary,
                        ambientColor = themeColors.primary
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColors.primary,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    painter = painterResource(R.drawable.add_circle_bulk),
                    contentDescription = "Add SubCategory",
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add SubCategory",
                    fontFamily = iosFont,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            if(isSheetOpen.value) {
                EditSubCategorySheet(
                    showSubCategorySheet = isSheetOpen,
                    showSubCategoryIconSheet = isIconSheetOpen,
                    subCategorySheet = sheetState,
                    subCategoryIconSheet = iconSheetState,
                    parentCategoryId = categoryId,
                    isNewSubCategory = true,
                    usedScreenWidth = usedScreenWidth,
                    usedScreenHeight = usedScreenHeight,
                    onDismiss = {
                        isSheetOpen.value = false
                        // Refresh subcategories after adding
                        onSubCategoryEvent(SubCategoryEvent.FetchSubCategoriesForCategory(categoryId))
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

    // Show subcategories list when available
    AnimatedVisibility(
        visible = subCategories.isNotEmpty(),
        enter = fadeIn() + slideInVertically() + expandVertically(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        LazyColumn(
            state = scrollOverScrollState,
            userScrollEnabled = false,
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(themeColors.background)
                .overscroll(overscrollEffect)
                .scrollable(
                    orientation = Orientation.Vertical,
                    reverseDirection = true,
                    state = scrollOverScrollState,
                    overscrollEffect = overscrollEffect
                ),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
//                Text(
//                    text = "Select SubCategories",
//                    textAlign = TextAlign.Center,
//                    fontFamily = iosFont,
//                    fontWeight = FontWeight.SemiBold,
//                    fontSize = 18.sp,
//                    color = themeColors.inverseSurface,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 10.dp)
//                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${currentCategory?.name ?: "Category"} SubCategories",
                        textAlign = TextAlign.Center,
                        fontFamily = iosFont,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = themeColors.inverseSurface,
                    )
                }
            }


            item {
                var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
                var isBottomSheetVisible by remember { mutableStateOf(false) }

                val filteredSubCategories = subCategories.filter {
                    it.name.contains(searchQuery.text, ignoreCase = true)
                }

                SearchBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.search_bulk),
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.inverseSurface.copy(0.5f)
                        )
                    },
                    trailingIcon = {
                        ReOrderSearchBoxTrailingIcon(
                            onDropDownItemClick = { isBottomSheetVisible = true },
                        )
                    },
                    label = "SEARCH"
                )

                if (isBottomSheetVisible) {
                    BottomSheet(onDismiss = { isBottomSheetVisible = false }) {
                        SubCategoryDisplayReorderList(
                            items = subCategories,
                            onMove = { fromIndex, toIndex ->
                                onSubCategoryEvent(SubCategoryEvent.ReorderSubCategories(fromIndex, toIndex, categoryId))
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Animate the filtered subcategories
                AnimatedContent(
                    targetState = filteredSubCategories,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                                scaleIn(initialScale = 0.92f, animationSpec = tween(220, delayMillis = 90)))
                            .togetherWith(fadeOut(animationSpec = tween(90)))
                    }, label = "Filtered SubCategory animated"
                ) { subCategoriesToDisplay ->
                    val infiniteTransition = rememberInfiniteTransition(label = "Selected Glow animation")
                    val animatedColors = infiniteTransition.animateColor(
                        initialValue = Color.Transparent,
                        targetValue = Color.Transparent,
                        animationSpec = infiniteRepeatable(
                            animation = keyframes {
                                durationMillis = 3000
                                Latte_Maroon at 0 using LinearEasing
                                Macchiato_Teal at 1000 using LinearEasing
                                Macchiato_Peach at 2000 using LinearEasing
                            },
                            repeatMode = RepeatMode.Reverse
                        ), label = "Selected Glow animation")

                    Column {
                        // Skip button at the top
                        Button(
                            onClick = onSkipSubCategory,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = themeColors.primary.copy(alpha = 0.1f),
                                contentColor = themeColors.primary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Skip SubCategory Selection",
                                fontFamily = iosFont,
                                fontSize = 14.sp
                            )
                        }

                        FlowRow(
                            modifier = modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            subCategoriesToDisplay.forEachIndexed { index, item ->
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .then(
                                            if (item.id == transactionSubCategoryId) {
                                                Modifier.shadow(
                                                    25.dp,
                                                    RoundedCornerShape(20.dp),
                                                    spotColor = animatedColors.value,
                                                    ambientColor = animatedColors.value
                                                )
                                            } else {
                                                Modifier
                                            }
                                        )
                                        .clip(RoundedCornerShape(20.dp))
                                        .clickable {
                                            onAddTransactionEvent(
                                                AddTransactionEvent.UpdateSubCategoryId(item.id)
                                            )
                                            onSubCategorySelected()
                                        }
                                        .then(
                                            if (item.id == transactionSubCategoryId) {
                                                Modifier
                                                    .background(
                                                        color = themeColors.surface,
                                                        shape = RoundedCornerShape(20.dp)
                                                    )
                                                    .border(
                                                        width = 2.dp,
                                                        shape = RoundedCornerShape(20.dp),
                                                        color = animatedColors.value
                                                    )
                                            } else {
                                                Modifier
                                            }
                                        )
                                        .padding(top = 10.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Absolute.spacedBy(5.dp),
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(64.dp)
                                                .shadow(10.dp, RoundedCornerShape(20.dp))
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(Color(item.boxColor ?: 0))
                                                .padding(10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Image(
                                                painter = painterResource(
                                                    id = icons.find { it.id == item.subcategoryIconId }?.resourceId
                                                        ?: R.drawable.type_beverages_beer
                                                ),
                                                contentDescription = "SubCategory Icon",
                                                modifier = Modifier.size(40.dp)
                                            )
                                        }

                                        Text(
                                            text = item.name,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.inverseSurface,
                                            overflow = TextOverflow.Ellipsis,
                                            fontSize = 12.sp,
                                            lineHeight = 13.sp,
                                            fontFamily = iosFont,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxWidth()
                                                .padding(horizontal = 5.dp)
                                        )
                                    }
                                }
                            }

                            // Add new subcategory button
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { isSheetOpen.value = true }
                                    .padding(bottom = 15.dp),
                                contentAlignment = Alignment.Center
                            ){
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(
                                            themeColors.onBackground,
                                            shape = RoundedCornerShape(20.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.add_circle_bulk),
                                        contentDescription = "Add subcategory",
                                        modifier = Modifier.size(40.dp),
                                        tint = themeColors.inverseSurface.copy(0.5f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Add subcategory sheet
                if(isSheetOpen.value) {
                    EditSubCategorySheet(
                        showSubCategorySheet = isSheetOpen,
                        showSubCategoryIconSheet = isIconSheetOpen,
                        subCategorySheet = sheetState,
                        subCategoryIconSheet = iconSheetState,
                        parentCategoryId = categoryId,
                        isNewSubCategory = true,
                        usedScreenWidth = usedScreenWidth,
                        usedScreenHeight = usedScreenHeight,
                        onDismiss = {
                            isSheetOpen.value = false
                            // Refresh subcategories after adding
                            onSubCategoryEvent(
                                SubCategoryEvent.FetchSubCategoriesForCategory(
                                    categoryId
                                )
                            )
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
}