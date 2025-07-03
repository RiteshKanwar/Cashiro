package com.ritesh.cashiro.presentation.ui.extras.components.category

import android.util.Log
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.draw.rotate
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
import com.ritesh.cashiro.data.local.entity.CategoryEntity
import com.ritesh.cashiro.domain.repository.TransactionStats
import com.ritesh.cashiro.domain.utils.icons
import com.ritesh.cashiro.presentation.ui.features.add_transaction.AddTransactionScreenViewModel
import com.ritesh.cashiro.presentation.ui.theme.iosFont
import com.ritesh.cashiro.presentation.ui.features.categories.CategoryScreenViewModel
import com.ritesh.cashiro.presentation.effects.VerticalStretchedOverscroll
import com.ritesh.cashiro.presentation.ui.extras.components.extras.SearchBar
import com.ritesh.cashiro.presentation.ui.features.add_transaction.AddTransactionEvent
import com.ritesh.cashiro.presentation.ui.features.add_transaction.AddTransactionScreenState
import com.ritesh.cashiro.presentation.ui.features.categories.AddCategoryBottomButton
import com.ritesh.cashiro.presentation.ui.features.categories.CategoryScreenEvent
import com.ritesh.cashiro.presentation.ui.features.categories.CategoryScreenState
import com.ritesh.cashiro.presentation.ui.features.categories.SubCategoryEvent
import com.ritesh.cashiro.presentation.ui.features.categories.SubCategoryState
import com.ritesh.cashiro.presentation.ui.features.categories.SubCategoryViewModel
import com.ritesh.cashiro.presentation.ui.theme.Latte_Maroon
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Peach
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Teal

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun CategoryDisplayList(
    items: List<CategoryEntity>,
    categoryUiState: CategoryScreenState,
    onCategoryEvent: (CategoryScreenEvent) -> Unit,
    subCategoryUiState: SubCategoryState,
    onSubCategoryEvent: (SubCategoryEvent) -> Unit,
    transactionUiState: AddTransactionScreenState,
    onAddTransactionEvent: (AddTransactionEvent) -> Unit,
    getTransactionStatsForCategory: (categoryId: Int, onResult: (TransactionStats) -> Unit) -> Unit,
    getTransactionStatsForSubCategory: (subCategoryId: Int, onResult: (TransactionStats) -> Unit) -> Unit,
    setSelectedCategoryId: (Int?) -> Unit,
    modifier: Modifier = Modifier,
    onCategorySelected: (categoryId: Int) -> Unit
) {
    val themeColors = MaterialTheme.colorScheme

    // Get half of the screen height
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val usedScreenHeight = screenHeight / 1.2f
    val usedScreenWidth = screenWidth - 10.dp

    // Collect state from ViewModels
    val transactionCategoryId = transactionUiState.transactionCategoryId
//    val state by categoryViewModel.state.collectAsState()
    val selectedIconId = categoryUiState.selectedIconId
    val customColor = categoryUiState.customBoxColor
    val categoryName = categoryUiState.categoryName
    val category = categoryUiState.categories // For compatibility with existing code

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded= true)
    val iconSheetState = rememberModalBottomSheetState(skipPartiallyExpanded= true)
    var isSheetOpen = rememberSaveable { mutableStateOf(false) }
    var isIconSheetOpen = rememberSaveable { mutableStateOf(false) }

    val scrollOverScrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val overscrollEffect = remember(coroutineScope) { VerticalStretchedOverscroll(coroutineScope) }

    // Prepare for adding a new category when the add button is clicked
    LaunchedEffect(isSheetOpen.value) {
        if (isSheetOpen.value) {
            onCategoryEvent(CategoryScreenEvent.PrepareForNewCategory)
        }
    }

    AnimatedVisibility(
        visible = category.isEmpty() || category.size == 1,
        enter = fadeIn() + slideInVertically() + expandVertically(),
        exit = slideOutVertically() + shrinkVertically() + fadeOut(),
    ) {
        Column (verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
        ){
            Box(modifier = Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 10.dp)){
                //DragHandle
                Spacer(
                    modifier = Modifier.height(4.dp).width(35.dp)
                        .background(themeColors.inverseSurface, RoundedCornerShape(20.dp)).align(Alignment.Center)
                )
            }
            Text(
                text = "Select Categories",
                textAlign = TextAlign.Center,
                fontFamily = iosFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = themeColors.inverseSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            )
            EmptyCategories(modifier.weight(1f))
            AddCategoryBottomButton(
                selectedIconId = selectedIconId ?: 0,
                customColor = customColor ?: Color.Transparent,
                categoryName = categoryName,
                usedScreenHeight = usedScreenHeight,
                usedScreenWidth = usedScreenWidth,
                modifier = Modifier.padding(vertical = 5.dp),
                themeColors = themeColors,
                categoryUiState = categoryUiState,
                onCategoryEvent = onCategoryEvent,
                subCategoryUiState = subCategoryUiState,
                onSubCategoryEvent = onSubCategoryEvent,
                getTransactionStatsForCategory = getTransactionStatsForCategory,
                getTransactionStatsForSubCategory = getTransactionStatsForSubCategory,
            )
        }
    }

    AnimatedVisibility(
        visible = category.isNotEmpty() && category.size > 1,
        enter = fadeIn() + slideInVertically() + expandVertically() ,
        exit = shrinkVertically() + fadeOut() ,
    ) {
        LazyColumn(
            state = scrollOverScrollState,
            userScrollEnabled = false,
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(16.dp))
                .fillMaxWidth()
                .background(themeColors.background)
                .overscroll(overscrollEffect)
                .scrollable(
                    orientation = Orientation.Vertical,
                    reverseDirection = true,
                    state = scrollOverScrollState,
                    overscrollEffect = overscrollEffect
                ),
            verticalArrangement =  Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = "Select Categories",
                    textAlign = TextAlign.Center,
                    fontFamily = iosFont,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = themeColors.inverseSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                )
            }

            item {
                var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
                var isBottomSheetVisible by remember { mutableStateOf(false) }

                val filteredCategory = items.filter {
                    it.name.contains(searchQuery.text, ignoreCase = true) && it.id != 1
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
                        CategoryDisplayReorderList(
                            items = category,
                            onMove = { fromIndex, toIndex ->
                                if (fromIndex in category.indices && toIndex in category.indices) {
                                    // Use the event pattern for reordering
                                    onCategoryEvent(
                                        CategoryScreenEvent.ReorderCategories(
                                            fromIndex = fromIndex,
                                            toIndex = toIndex
                                        )
                                    )
                                } else {
                                    Log.e(
                                        "CategoryDisplayReorderList",
                                        "Invalid indices: fromIndex=$fromIndex, toIndex=$toIndex"
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            subCategoryUiState = subCategoryUiState,
                            categoryUiState = categoryUiState,
                            onCategoryEvent = onCategoryEvent,
                            onSubCategoryEvent = onSubCategoryEvent,
                            getTransactionStatsForCategory = getTransactionStatsForCategory,
                            getTransactionStatsForSubCategory = getTransactionStatsForSubCategory,
                            setSelectedCategoryId = setSelectedCategoryId,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Animate the filtered categories
                AnimatedContent(
                    targetState = filteredCategory,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                                scaleIn(initialScale = 0.92f, animationSpec = tween(220, delayMillis = 90)))
                            .togetherWith(fadeOut(animationSpec = tween(90)))
                    }, label = "Filtered Category animated"
                ) { categoriesToDisplay ->
                    val infiniteTransition = rememberInfiniteTransition(label = "Selected Glow animation")
                    val animatedColors = infiniteTransition.animateColor(
                        initialValue = Color.Transparent,
                        targetValue = Color.Transparent,
                        animationSpec = infiniteRepeatable(
                            animation = keyframes {
                                durationMillis = 3000 // Total duration for the animation cycle
                                Latte_Maroon at 0 using LinearEasing
                                Macchiato_Teal at 1000 using LinearEasing // Add more colors in the sequence
                                Macchiato_Peach at 2000 using LinearEasing
                            },
                            repeatMode = RepeatMode.Reverse
                        ), label = "Selected Glow animation")
                    FlowRow(
                        modifier = modifier
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        categoriesToDisplay.forEachIndexed { index, item ->

                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .then(
                                        if (item.id == transactionCategoryId){
                                            Modifier.shadow(25.dp, RoundedCornerShape(20.dp),spotColor = animatedColors.value, ambientColor = animatedColors.value)}
                                        else {
                                            Modifier
                                        }
                                    )
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable{
                                        // 🔥 FIX: Update state and pass ID directly
                                        onAddTransactionEvent(AddTransactionEvent.UpdateCategoryId(item.id))
                                        onAddTransactionEvent(AddTransactionEvent.UpdateSubCategoryId(0))
                                        onSubCategoryEvent(SubCategoryEvent.ClearSubCategories)
                                        onCategorySelected(item.id) // 🔥 CHANGE: Pass the category ID directly
                                    }
                                    .then(
                                        if (item.id == transactionCategoryId){
                                            Modifier.background(color = themeColors.surface, shape = RoundedCornerShape(20.dp))
                                                .border(width = 2.dp, shape = RoundedCornerShape(20.dp),color = animatedColors.value)}
                                        else {
                                            Modifier
                                        }
                                    )
                                    .padding(top = 10.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Absolute.spacedBy(5.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
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

                                    Text(
                                        text = item.name,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.inverseSurface,
                                        overflow = TextOverflow.Ellipsis,
                                        fontSize = 12.sp,
                                        lineHeight = 13.sp,
                                        fontFamily = iosFont,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 5.dp)
                                    )
                                }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .clickable {
                                    // Prepare for new category
                                    onCategoryEvent(CategoryScreenEvent.PrepareForNewCategory)
                                    isSheetOpen.value = true
                                }
                                .padding(bottom = 15.dp),
                            contentAlignment = Alignment.Center
                        ){
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(themeColors.onBackground, shape = RoundedCornerShape(20.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.add_circle_bulk),
                                    contentDescription = "Add category",
                                    modifier = Modifier
                                        .size(40.dp),
                                    tint = themeColors.inverseSurface.copy(0.5f) // Disable tinting
                                )
                            }
                        }
                    }
                }

                if(isSheetOpen.value) {
                    EditCategorySheet(
                        sheetState = sheetState,
                        isSheetOpen = isSheetOpen,
                        isIconSheetOpen = isIconSheetOpen,
                        iconSheetState = iconSheetState,
                        selectedIconId = selectedIconId ?: 0,
                        customColor = customColor ?: Color.Transparent,
                        categoryName = categoryName,
                        usedScreenWidth = usedScreenWidth,
                        usedScreenHeight = usedScreenHeight,
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
}

@Composable
fun ReOrderSearchBoxTrailingIcon(
    onDropDownItemClick: () -> Unit
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { isDropdownExpanded = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Options",
                tint = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                modifier = Modifier.rotate(90f)
            )
        }

        DropdownMenu(
            shape = RoundedCornerShape(15.dp),
            containerColor = MaterialTheme.colorScheme.surfaceBright,
            expanded = isDropdownExpanded,
            onDismissRequest = { isDropdownExpanded = false }
        ) {
            DropdownMenuItem(
                onClick = {
                    isDropdownExpanded = false
                    onDropDownItemClick()
                },
                text = { Text("Reorder List") },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheet(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val usedScreenHeight = screenHeight
    val usedScreenWidth = screenWidth
    ModalBottomSheet(
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(0.dp),
        containerColor = MaterialTheme.colorScheme.background,
        sheetMaxWidth = usedScreenWidth,
        onDismissRequest = onDismiss
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(usedScreenHeight)) {
            content()
        }
    }
}