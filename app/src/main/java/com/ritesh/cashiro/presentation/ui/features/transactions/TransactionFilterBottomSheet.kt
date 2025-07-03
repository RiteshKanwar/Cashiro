package com.ritesh.cashiro.presentation.ui.features.transactions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.BottomSheetDefaults.DragHandle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.ritesh.cashiro.R
import com.ritesh.cashiro.data.local.entity.AccountEntity
import com.ritesh.cashiro.data.local.entity.CategoryEntity
import com.ritesh.cashiro.data.local.entity.SubCategoryEntity
import com.ritesh.cashiro.data.local.entity.TransactionType
import com.ritesh.cashiro.domain.utils.icons
import com.ritesh.cashiro.presentation.ui.extras.components.charts.formatCurrency
import com.ritesh.cashiro.presentation.ui.extras.components.extras.SearchBar
import com.ritesh.cashiro.presentation.ui.features.accounts.CompactAnimatedGradientMeshCard
import com.ritesh.cashiro.presentation.ui.features.add_transaction.AddTransactionEvent
import com.ritesh.cashiro.presentation.ui.features.add_transaction.AddTransactionScreenState
import com.ritesh.cashiro.presentation.ui.features.add_transaction.CustomAmountInputBottomSheet
import com.ritesh.cashiro.presentation.ui.theme.ErrorColor
import com.ritesh.cashiro.presentation.ui.theme.Latte_Blue
import com.ritesh.cashiro.presentation.ui.theme.Latte_Flamingo
import com.ritesh.cashiro.presentation.ui.theme.Latte_Lavender
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Blue
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Green
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Red
import com.ritesh.cashiro.presentation.ui.theme.SuccessColor
import com.ritesh.cashiro.presentation.ui.theme.iosFont

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TransactionFilterBottomSheet(
    filterState: TransactionFilterState,
    onFilterStateChange: (TransactionFilterState) -> Unit,
    categories: List<CategoryEntity>,
    subCategories: List<SubCategoryEntity>,
    accounts: List<AccountEntity>,
    onApply: () -> Unit,
    usedScreenWidth: Dp,
    usedScreenHeight: Dp,
    transactionUiState: AddTransactionScreenState,
    onAddTransactionEvent: (AddTransactionEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentFilterState by remember { mutableStateOf(filterState) }

    // NEW: State for amount input bottom sheet
    var showAmountInputSheet by remember { mutableStateOf(false) }
    var editingMinAmount by remember { mutableStateOf(true) } // true for min, false for max
    var tempAmountInput by remember { mutableStateOf("") }

    val amountSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(
        modifier = modifier
    ){
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            item {
                Text(
                    text = "Filters",
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = iosFont,
                    color = MaterialTheme.colorScheme.inverseSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }

            // Text Search Section
            item {
                var searchQuery by remember { mutableStateOf(currentFilterState.searchText) }
                TextSearchFilterSection(
                    searchText = searchQuery,
                    onSearchChange = { text ->
                        searchQuery = text
                        currentFilterState = currentFilterState.copy(searchText = searchQuery)
                    },
                    onClear = { searchQuery = TextFieldValue("") }
                )
            }

            // Categories Section with Subcategory support
            item {
                CategoryFilterSection(
                    categories = categories,
                    subCategories = subCategories, // NEW
                    selectedCategories = currentFilterState.selectedCategories,
                    selectedSubCategories = currentFilterState.selectedSubCategories, // NEW
                    expandedCategories = currentFilterState.expandedCategories, // NEW
                    onCategorySelectionChange = { categoryIds ->
                        currentFilterState = currentFilterState.copy(selectedCategories = categoryIds)
                    },
                    onSubCategorySelectionChange = { subCategoryIds -> // NEW
                        currentFilterState = currentFilterState.copy(selectedSubCategories = subCategoryIds)
                    },
                    onCategoryExpansionChange = { categoryId, isExpanded -> // NEW
                        val newExpandedCategories = if (isExpanded) {
                            currentFilterState.expandedCategories + categoryId
                        } else {
                            currentFilterState.expandedCategories - categoryId
                        }
                        currentFilterState = currentFilterState.copy(expandedCategories = newExpandedCategories)
                    }
                )
            }

            // Amount Range Section with custom input
            item {
                AmountRangeFilterSection(
                    minAmount = currentFilterState.minAmount,
                    maxAmount = currentFilterState.maxAmount,
                    onAmountRangeChange = { min, max ->
                        currentFilterState = currentFilterState.copy(
                            minAmount = min,
                            maxAmount = max
                        )
                    },
                    onMinAmountClick = { // NEW
                        editingMinAmount = true
                        tempAmountInput = currentFilterState.minAmount.toString()
                        showAmountInputSheet = true
                    },
                    onMaxAmountClick = { // NEW
                        editingMinAmount = false
                        tempAmountInput = currentFilterState.maxAmount.toString()
                        showAmountInputSheet = true
                    }
                )
            }

            // Transaction Mode Section
            item {
                Column {
                    Text(
                        text = "Transaction",
                        fontSize = 16.sp,
                        fontFamily = iosFont,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.inverseSurface.copy(0.8f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(16.dp))
                        .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        TransactionModeFilterSection(
                            selectedModes = currentFilterState.transactionMode,
                            onModeSelectionChange = { modes ->
                                currentFilterState = currentFilterState.copy(transactionMode = modes)
                            }
                        )
                        TransactionTypeFilterSection(
                            selectedTypes = currentFilterState.transactionTypes,
                            onTypeSelectionChange = { types ->
                                currentFilterState = currentFilterState.copy(transactionTypes = types)
                            }
                        )
                        PaymentStatusFilterSection(
                            selectedStatuses = currentFilterState.paymentStatus,
                            onStatusSelectionChange = { statuses ->
                                currentFilterState = currentFilterState.copy(paymentStatus = statuses)
                            }
                        )
                    }
                }
            }

            // Account Section
            item {
                AccountFilterSection(
                    accounts = accounts,
                    selectedAccounts = currentFilterState.selectedAccounts,
                    onAccountSelectionChange = { accountIds ->
                        currentFilterState = currentFilterState.copy(selectedAccounts = accountIds)
                    }
                )
            }

            // Bottom spacing for navigation
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                            MaterialTheme.colorScheme.background,
                        )
                    )
                )
                .padding(start = 16.dp, end = 16.dp, top = 22.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    currentFilterState = TransactionFilterState()
                    onFilterStateChange(currentFilterState)
                },
                modifier = Modifier.weight(1f)
                    .shadow(
                        elevation = 5.dp,
                        shape = RoundedCornerShape(12.dp),
                        spotColor = MaterialTheme.colorScheme.surface ,
                        ambientColor = Color.Transparent),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.inverseSurface
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Reset",
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Medium
                )
            }

            Button(
                onClick = {
                    onFilterStateChange(currentFilterState)
                    onApply()
                },
                modifier = Modifier.weight(1f)
                    .shadow(
                        elevation = 5.dp,
                        shape = RoundedCornerShape(12.dp),
                        spotColor = MaterialTheme.colorScheme.primary ,
                        ambientColor = Color.Transparent),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Apply",
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    // NEW: Amount Input Bottom Sheet
    if (showAmountInputSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAmountInputSheet = false },
            sheetState = amountSheetState,
            containerColor = MaterialTheme.colorScheme.background,
            dragHandle = {
                DragHandle(
                    color = MaterialTheme.colorScheme.inverseSurface.copy(0.3f)
                )
            },
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        ) {
            CustomAmountInputBottomSheet(
                amount = tempAmountInput,
                amountSheetState = amountSheetState,
                specialKeys = setOf('.', ','), // For decimal input
                currencyCode = "usd", // Default currency
                onClick = {
                    // Handle amount confirmation
                    val enteredAmount = tempAmountInput.toDoubleOrNull() ?: 0.0
                    currentFilterState = if (editingMinAmount) {
                        currentFilterState.copy(minAmount = enteredAmount)
                    } else {
                        currentFilterState.copy(maxAmount = enteredAmount)
                    }
                    showAmountInputSheet = false
                },
                onDismissRequest = { showAmountInputSheet = false },
                onValueChange = { newValue ->
                    tempAmountInput = newValue
                },
                onResultAmount = { result ->
                    tempAmountInput = result
                },
                // Default values for required parameters
                isTransactionTypeInfoSheetOpen = false,
                currentTransactionMode = "",
                transactionTypeListItemsColor = emptyList(),
                usedScreenWidth = usedScreenWidth,
                usedScreenHeight = usedScreenHeight,
                recurrenceMenuOpened = false,
                isRecurrenceBottomSheetOpen = false,
                isCustomInputBottomSheetOpen = true,
                isEndDateSelected = false,
                openEndDatePicker = false,
                showTransactionTypeSelection = false,
                transactionUiState = transactionUiState,
                onAddTransactionEvent = onAddTransactionEvent // Default state
            )
        }
    }
}
@Composable
private fun CategoryFilterSection(
    categories: List<CategoryEntity>,
    subCategories: List<SubCategoryEntity>, // NEW parameter
    selectedCategories: Set<Int>,
    selectedSubCategories: Set<Int>, // NEW parameter
    expandedCategories: Set<Int>, // NEW parameter
    onCategorySelectionChange: (Set<Int>) -> Unit,
    onSubCategorySelectionChange: (Set<Int>) -> Unit, // NEW parameter
    onCategoryExpansionChange: (Int, Boolean) -> Unit // NEW parameter
) {
    Column(modifier = Modifier.animateContentSize()) {
        Text(
            text = "Categories",
            fontSize = 16.sp,
            fontFamily = iosFont,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.inverseSurface.copy(0.8f),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // All categories option
            item {
                FilterCategoryChip(
                    name = "All",
                    iconId = R.drawable.category_bulk,
                    boxColor = MaterialTheme.colorScheme.surface,
                    isSelected = selectedCategories.isEmpty() && selectedSubCategories.isEmpty(),
                    onClick = {
                        onCategorySelectionChange(emptySet())
                        onSubCategorySelectionChange(emptySet())
                        onCategoryExpansionChange(-1, false) // Clear all expansions
                    },
                    tint = MaterialTheme.colorScheme.inverseSurface
                )
            }

            items(categories) { category ->
                val categorySubCategories = subCategories.filter { it.categoryId == category.id }
                val hasSubCategories = categorySubCategories.isNotEmpty()
                val isExpanded = expandedCategories.contains(category.id)

                FilterCategoryChip(
                    name = category.name,
                    iconId = category.categoryIconId,
                    boxColor = Color(category.boxColor),
                    isSelected = selectedCategories.contains(category.id),
                    hasSubCategories = hasSubCategories, // NEW
                    isExpanded = isExpanded, // NEW
                    onClick = {
                        val newSelection = if (selectedCategories.contains(category.id)) {
                            selectedCategories - category.id
                        } else {
                            selectedCategories + category.id
                        }
                        onCategorySelectionChange(newSelection)
                    },
                    onExpandClick = if (hasSubCategories) { // NEW
                        {
                            onCategoryExpansionChange(category.id, !isExpanded)
                        }
                    } else null
                )
            }
        }

        // NEW: Show subcategories for expanded categories
        expandedCategories.forEach { expandedCategoryId ->
            val category = categories.find { it.id == expandedCategoryId }
            val categorySubCategories = subCategories.filter { it.categoryId == expandedCategoryId }

            AnimatedVisibility(
                visible = category != null && categorySubCategories.isNotEmpty(),
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "${category?.name} subcategories",
                        fontSize = 14.sp,
                        fontFamily = iosFont,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.inverseSurface.copy(0.7f),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categorySubCategories) { subCategory ->
                            FilterSubCategoryChip(
                                subCategory = subCategory,
                                isSelected = selectedSubCategories.contains(subCategory.id),
                                onClick = {
                                    val newSelection = if (selectedSubCategories.contains(subCategory.id)) {
                                        selectedSubCategories - subCategory.id
                                    } else {
                                        selectedSubCategories + subCategory.id
                                    }
                                    onSubCategorySelectionChange(newSelection)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Enhanced FilterCategoryChip with subcategory expansion support
@Composable
private fun FilterCategoryChip(
    name: String,
    iconId: Int,
    boxColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    tint: Color = Color.Transparent,
    hasSubCategories: Boolean = false, // NEW
    isExpanded: Boolean = false, // NEW
    onExpandClick: (() -> Unit)? = null // NEW
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(
                    color = if (isSelected) boxColor else boxColor.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(
                    id = icons.find { it.id == iconId }?.resourceId ?: R.drawable.category_bulk
                ),
                contentDescription = name,
                modifier = Modifier.size(32.dp),
                colorFilter = if (name == "All" && iconId == R.drawable.category_bulk) ColorFilter.tint(tint) else null,
                alpha = if (isSelected) 1f else 0.6f
            )

            // NEW: Show expansion indicator if has subcategories
            if (hasSubCategories && onExpandClick != null) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 4.dp, y = 4.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                        .clickable { onExpandClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        Text(
            text = name,
            fontSize = 10.sp,
            fontFamily = iosFont,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .width(64.dp)
                .padding(top = 4.dp)
        )
    }
}

// NEW: SubCategory chip component
@Composable
private fun FilterSubCategoryChip(
    subCategory: SubCategoryEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    color = if (isSelected)
                        Color(subCategory.boxColor ?: 0)
                    else
                        Color(subCategory.boxColor ?: 0).copy(alpha = 0.3f),
                    shape = RoundedCornerShape(14.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(
                    id = icons.find { it.id == subCategory.subcategoryIconId }?.resourceId
                        ?: R.drawable.type_beverages_beer
                ),
                contentDescription = subCategory.name,
                modifier = Modifier.size(28.dp),
                alpha = if (isSelected) 1f else 0.6f
            )
        }

        Text(
            text = subCategory.name,
            fontSize = 9.sp,
            fontFamily = iosFont,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(0.8f),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .width(56.dp)
                .padding(top = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AmountRangeFilterSection(
    minAmount: Double,
    maxAmount: Double,
    onAmountRangeChange: (Double, Double) -> Unit,
    onMinAmountClick: () -> Unit,
    onMaxAmountClick: () -> Unit
) {
    var sliderPosition by remember { mutableStateOf(minAmount.toFloat()..maxAmount.toFloat()) }

    // FIX: Synchronize slider position with incoming amount values
    LaunchedEffect(minAmount, maxAmount) {
        sliderPosition = minAmount.toFloat()..maxAmount.toFloat()
    }

    Column {
        Text(
            text = "Amount Range",
            fontSize = 16.sp,
            fontFamily = iosFont,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.inverseSurface.copy(0.8f),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Range Slider
        RangeSlider(
            value = sliderPosition,
            onValueChange = { range ->
                sliderPosition = range
                onAmountRangeChange(range.start.toDouble(), range.endInclusive.toDouble())
            },
            valueRange = 0f..10000000f,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surface
            )
        )

        // Clickable amount display with custom input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Minimum amount - clickable
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Min Amount",
                    fontSize = 10.sp,
                    fontFamily = iosFont,
                    color = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                    fontWeight = FontWeight.Normal
                )

                Card(
                    modifier = Modifier
                        .clickable { onMinAmountClick() }
                        .padding(top = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = formatCurrency(minAmount, "$"),
                            fontSize = 14.sp,
                            fontFamily = iosFont,
                            color = MaterialTheme.colorScheme.inverseSurface,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit min amount",
                            tint = MaterialTheme.colorScheme.inverseSurface.copy(0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Maximum amount - clickable
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "Max Amount",
                    fontSize = 10.sp,
                    fontFamily = iosFont,
                    color = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                    fontWeight = FontWeight.Normal
                )

                Card(
                    modifier = Modifier
                        .clickable { onMaxAmountClick() }
                        .padding(top = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = formatCurrency(maxAmount, "$"),
                            fontSize = 14.sp,
                            fontFamily = iosFont,
                            color = MaterialTheme.colorScheme.inverseSurface,
                            fontWeight = FontWeight.Medium
                        )
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit max amount",
                            tint = MaterialTheme.colorScheme.inverseSurface.copy(0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
@Composable
private fun TransactionModeFilterSection(
    selectedModes: Set<String>,
    onModeSelectionChange: (Set<String>) -> Unit
) {
    Column {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Income", "Expense", "Transfer").forEach { mode ->
                FilterChip(
                    selected = selectedModes.contains(mode),
                    onClick = {
                        val newSelection = if (selectedModes.contains(mode)) {
                            selectedModes - mode
                        } else {
                            selectedModes + mode
                        }
                        onModeSelectionChange(newSelection)
                    },
                    label = {
                        Text(
                            text = mode,
                            fontFamily = iosFont,
                            fontSize = 12.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceBright,
                        labelColor = MaterialTheme.colorScheme.inverseSurface,
                        selectedContainerColor = if (mode == "Income") SuccessColor.copy(0.5f) else if (mode == "Transfer")MaterialTheme.colorScheme.primary.copy(0.5f) else ErrorColor.copy(0.5f),
                        selectedLabelColor = Color.White
                    ),
                    border = FilterChipDefaults.filterChipBorder(enabled = false,selected = false,borderWidth = 0.dp),
                    shape = RoundedCornerShape(22.dp)
                )
            }
        }
    }
}

@Composable
private fun TransactionTypeFilterSection(
    selectedTypes: Set<TransactionType>,
    onTypeSelectionChange: (Set<TransactionType>) -> Unit
) {
    Column {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            listOf(
                TransactionType.DEFAULT,
                TransactionType.UPCOMING,
                TransactionType.SUBSCRIPTION,
                TransactionType.REPETITIVE,
                TransactionType.LENT,
                TransactionType.BORROWED
            ).forEach { type ->
                FilterChip(
                    selected = selectedTypes.contains(type),
                    onClick = {
                        val newSelection = if (selectedTypes.contains(type)) {
                            selectedTypes - type
                        } else {
                            selectedTypes + type
                        }
                        onTypeSelectionChange(newSelection)
                    },
                    label = {
                        Text(
                            text = type.name.lowercase().replaceFirstChar { it.uppercase() },
                            fontFamily = iosFont,
                            fontSize = 12.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor =MaterialTheme.colorScheme.surfaceBright,
                        labelColor = MaterialTheme.colorScheme.inverseSurface,
                        selectedContainerColor = if (type == TransactionType.DEFAULT) Latte_Blue.copy(0.4f)
                        else if (type == TransactionType.UPCOMING) Latte_Flamingo.copy(0.4f)
                        else if (type == TransactionType.SUBSCRIPTION) Latte_Lavender.copy(0.4f)
                        else if (type == TransactionType.REPETITIVE) Macchiato_Green.copy(0.4f)
                        else if (type == TransactionType.LENT) Macchiato_Blue.copy(0.4f)
                        else if (type == TransactionType.BORROWED) Macchiato_Red
                        else MaterialTheme.colorScheme.surfaceBright,
                        selectedLabelColor = MaterialTheme.colorScheme.inverseSurface,
                    ),
                    border = FilterChipDefaults.filterChipBorder(enabled = false,selected = false,borderWidth = 0.dp),
                    shape = RoundedCornerShape(22.dp)
                )
            }
        }
    }
}

@Composable
private fun PaymentStatusFilterSection(
    selectedStatuses: Set<String>,
    onStatusSelectionChange: (Set<String>) -> Unit
) {
    Column {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Paid", "Not paid").forEach { status ->
                FilterChip(
                    selected = selectedStatuses.contains(status),
                    onClick = {
                        val newSelection = if (selectedStatuses.contains(status)) {
                            selectedStatuses - status
                        } else {
                            selectedStatuses + status
                        }
                        onStatusSelectionChange(newSelection)
                    },
                    label = {
                        Text(
                            text = status,
                            fontFamily = iosFont,
                            fontSize = 12.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceBright,
                        labelColor = MaterialTheme.colorScheme.inverseSurface,
                        selectedContainerColor = if (status == "Paid") Color(0xFF4CAF50) else MaterialTheme.colorScheme.errorContainer,
                        selectedLabelColor = if (status == "Paid") Color.White else MaterialTheme.colorScheme.onErrorContainer,

                        ),
                    border = FilterChipDefaults.filterChipBorder(enabled = false,selected = false,borderWidth = 0.dp),
                    shape = RoundedCornerShape(22.dp)
                )
            }
        }
    }
}

@Composable
private fun AccountFilterSection(
    accounts: List<AccountEntity>,
    selectedAccounts: Set<Int>,
    onAccountSelectionChange: (Set<Int>) -> Unit
) {
    Column {
        Text(
            text = "Accounts",
            fontSize = 16.sp,
            fontFamily = iosFont,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.inverseSurface.copy(0.8f),
            modifier = Modifier.padding(bottom = 12.dp)
        )
        LazyRow (
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            accounts.forEach { account ->
                val isSelected = selectedAccounts.contains(account.id)

                item {
                    Box(
                        modifier = Modifier
                            .height(80.dp)
                            .width(120.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                val newSelection = if (isSelected) {
                                    selectedAccounts - account.id
                                } else {
                                    selectedAccounts + account.id
                                }
                                onAccountSelectionChange(newSelection)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        CompactAnimatedGradientMeshCard(
                            firstColor = Color(account.cardColor1),
                            secondColor = Color(account.cardColor2),
                            cardHeight = 80.dp,
                            content = {
                                Column(
                                    modifier = Modifier.height(40.dp),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = account.accountName,
                                        textAlign = TextAlign.Center,
                                        fontFamily = iosFont,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.basicMarquee().padding(horizontal = 4.dp)
                                    )
                                    Spacer(
                                        modifier = Modifier
                                            .height(4.dp)
                                            .width(28.dp)
                                            .background(
                                                color = if (isSelected) Color(account.cardColor1).copy(0.5f) else Color.Transparent,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                    )
                                }

                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TextSearchFilterSection(
    searchText: TextFieldValue,
    onSearchChange: (TextFieldValue) -> Unit,
    onClear: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SearchBar(
            searchQuery = searchText,
            onSearchQueryChange = onSearchChange,
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.search_bulk),
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.inverseSurface.copy(0.5f)
                )
            },
            trailingIcon = {
                if (searchText.text.isNotEmpty()) {
                    IconButton(onClick = onClear) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear search",
                            tint = MaterialTheme.colorScheme.inverseSurface.copy(0.5f)
                        )
                    }
                }
            },
            label = "search title, notes...",
            modifier = Modifier.zIndex(3f)
        )
    }
}