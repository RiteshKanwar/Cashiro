package com.ritesh.cashiro.presentation.ui.features.transactions

import android.annotation.SuppressLint
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomSheetDefaults.DragHandle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.ritesh.cashiro.R
import com.ritesh.cashiro.data.local.entity.TransactionEntity
import com.ritesh.cashiro.domain.utils.TransactionFilterUtils
import com.ritesh.cashiro.presentation.effects.BlurredAnimatedVisibility
import com.ritesh.cashiro.presentation.effects.VerticalStretchedOverscroll
import com.ritesh.cashiro.presentation.ui.extras.components.extras.SearchBar
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenEvent
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenState
import com.ritesh.cashiro.presentation.ui.features.add_transaction.AddTransactionEvent
import com.ritesh.cashiro.presentation.ui.features.add_transaction.AddTransactionScreenState
import com.ritesh.cashiro.presentation.ui.features.categories.CategoryScreenEvent
import com.ritesh.cashiro.presentation.ui.features.categories.CategoryScreenState
import com.ritesh.cashiro.presentation.ui.features.categories.SubCategoryEvent
import com.ritesh.cashiro.presentation.ui.features.categories.SubCategoryState
import com.ritesh.cashiro.presentation.ui.theme.iosFont

@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SharedTransitionScope.SearchTransactionScreen(
    modifier: Modifier = Modifier,
    deleteTransactions: (List<Int>) -> Unit,
    transactionUiState: AddTransactionScreenState,
    accountUiState: AccountScreenState,
    onAccountEvent: (AccountScreenEvent) -> Unit,
    onAddTransactionEvent: (AddTransactionEvent) -> Unit,
    categoryUiState: CategoryScreenState,
    onCategoryEvent: (CategoryScreenEvent) -> Unit,
    subCategoryUiState: SubCategoryState,
    onSubCategoryEvent: (SubCategoryEvent) -> Unit,
    screenTitle: String,
    navController: NavController,
    sharedKey: String,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    // Collect all transactions and accounts
    val allTransactions = transactionUiState.transactions
    val accounts = accountUiState.accounts
    val categories = categoryUiState.categories

    // NEW: Collect subcategories data
    val categoriesWithSubCategories = subCategoryUiState.categoriesWithSubCategories
    val allSubCategories = categoriesWithSubCategories.flatMap { it.subCategories }

    // Selection state management
    var selectedTransactions by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var isInSelectionMode by remember { mutableStateOf(false) }

    // Filter state
    var filterState by remember { mutableStateOf(TransactionFilterState()) }
    var showFilterBottomSheet by rememberSaveable { mutableStateOf(false) }

    // Search state
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var filteredTransactions by remember { mutableStateOf<List<TransactionEntity>>(emptyList()) }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val usedScreenHeight = screenHeight / 1.2f
    val usedScreenWidth = screenWidth - 10.dp

    // Function to toggle selection of a transaction
    val toggleTransactionSelection: (TransactionEntity) -> Unit = { transaction ->
        if (isInSelectionMode) {
            selectedTransactions = if (selectedTransactions.contains(transaction.id)) {
                val updatedSelection = selectedTransactions - transaction.id
                if (updatedSelection.isEmpty()) {
                    isInSelectionMode = false
                }
                updatedSelection
            } else {
                selectedTransactions + transaction.id
            }
        } else {
            // Regular click behavior when not in selection mode
            // Could navigate to transaction details if needed
            // navController.navigate("transaction/${transaction.id}")
        }
    }

    // Function to handle long press on transaction item
    val onTransactionLongPress: (TransactionEntity) -> Unit = { transaction ->
        isInSelectionMode = true
        selectedTransactions = selectedTransactions + transaction.id
    }

    // Function to clear all selections and exit selection mode
    val clearSelections = {
        selectedTransactions = emptySet()
        isInSelectionMode = false
    }

    // Function to delete selected transactions
    val deleteSelectedTransactions = {
        deleteTransactions(selectedTransactions.toList())
        clearSelections()
    }

    // Listen for deletion events from the ViewModel
    LaunchedEffect(filteredTransactions) {
        if (selectedTransactions.isNotEmpty()) {
            // Check if any of the selected transactions no longer exist
            val currentIds = filteredTransactions.map { it.id }.toSet()
            val deletedSelections = selectedTransactions.filter { !currentIds.contains(it) }

            if (deletedSelections.isNotEmpty()) {
                // Some selected transactions were deleted, update the selection set
                selectedTransactions = selectedTransactions - deletedSelections.toSet()

                // If no items remain selected, exit selection mode
                if (selectedTransactions.isEmpty()) {
                    isInSelectionMode = false
                }
            }
        }
    }

    // Scroll behavior setup
    val searchScrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val searchOverscrollEffect = remember(coroutineScope) { VerticalStretchedOverscroll(coroutineScope) }

    // Filter bottom sheet state
    val filterSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Select all transactions from search results
    val selectAllTransactions = {
        if (!isInSelectionMode) {
            isInSelectionMode = true
        }
        // Get all transaction IDs from current filtered results
        val searchResultIds = filteredTransactions.map { it.id }.toSet()
        selectedTransactions = searchResultIds
        Log.d("SelectAll", "Selected all ${searchResultIds.size} transactions from search results")
        Unit
    }

    // Update filtered transactions whenever search query or filter state changes
    LaunchedEffect(searchQuery.text, allTransactions, filterState) {
        // First apply filters to all transactions
        val filterAppliedTransactions = TransactionFilterUtils.applyFilters(
            transactions = allTransactions,
            filterState = filterState,
            categories = categories,
            accounts = accounts
        )

        // Then apply search query to filtered results
        filteredTransactions = if (searchQuery.text.isBlank()) {
            filterAppliedTransactions
        } else {
            val query = searchQuery.text.lowercase().trim()
            filterAppliedTransactions.filter { transaction ->
                // Find matching account and category for this transaction
                val account = accounts.find { it.id == transaction.accountId }
                val category = categories.find { it.id == transaction.categoryId }

                // Search by transaction properties
                transaction.title.lowercase().contains(query) ||
                        transaction.amount.toString().contains(query) ||
                        transaction.note.lowercase().contains(query) ||
                        transaction.mode.lowercase().contains(query) ||
                        transaction.transactionType.name.lowercase().contains(query) ||
                        // Search by associated account name
                        (account?.accountName?.lowercase()?.contains(query) == true) ||
                        // Search by associated category name
                        (category?.name?.lowercase()?.contains(query) == true)
            }
        }
    }

    // Back handler for selection mode
    BackHandler(enabled = isInSelectionMode) {
        clearSelections()
    }

    Box(
        modifier = modifier.sharedBounds(
            sharedContentState = rememberSharedContentState(
                key = sharedKey),
            animatedVisibilityScope = animatedVisibilityScope
        )
            .fillMaxSize()
            .padding(top = 50.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.size(20.dp))

            // Top bar with filter button
            Box(modifier = Modifier.animateContentSize().background(MaterialTheme.colorScheme.background)
                .zIndex(6f)) {
                BlurredAnimatedVisibility(isInSelectionMode) {
                    // Show selection mode toolbar
                    SearchSelectionTopAppBar(
                        selectedCount = selectedTransactions.size,
                        onClearSelection = clearSelections,
                        onDeleteSelected = deleteSelectedTransactions,
                        onSelectAll = selectAllTransactions
                    )
                }
                BlurredAnimatedVisibility(!isInSelectionMode) {
                    // Show title and filter button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.Center) {
                            Text(
                                text = screenTitle,
                                textAlign = TextAlign.Start,
                                fontFamily = iosFont,
                                fontSize = 24.sp,
                                lineHeight = 24.sp,
                                color = MaterialTheme.colorScheme.inverseSurface,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                            Text(
                                text = "Search all your transactions",
                                textAlign = TextAlign.Start,
                                fontFamily = iosFont,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp)
                            )
                        }

                        // Filter button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(15.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .clickable { showFilterBottomSheet = true }
                                .size(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Show indicator dot if filters are active
                            if (filterState.isFilterActive()) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primary,
                                            CircleShape
                                        )
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-6).dp, y = 6.dp)
                                )
                            }

                            Icon(
                                painter = painterResource(R.drawable.filter_bulk), // You'll need this icon
                                contentDescription = "Filter transactions",
                                tint = if (filterState.isFilterActive())
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.inverseSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }



            // Search bar
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
                    if (searchQuery.text.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = TextFieldValue("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.inverseSurface.copy(0.5f)
                            )
                        }
                    }
                },
                label = "SEARCH",
                modifier = Modifier.zIndex(3f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Results counter with filter indicator
            if (searchQuery.text.isNotEmpty() || filterState.isFilterActive()) {
                Row(
                    modifier = Modifier
                        .zIndex(2f)
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filteredTransactions.size} transactions found",
                        fontFamily = iosFont,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.inverseSurface.copy(0.7f),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(start = 10.dp)
                    )

                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        if (searchQuery.text.isNotEmpty()) {
                            Text(
                                text = "Searching: \"${searchQuery.text}\"",
                                fontFamily = iosFont,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (filterState.isFilterActive()) {
                            Text(
                                text = "Filters applied",
                                fontFamily = iosFont,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.primary.copy(0.7f),
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }
                }
            }

            TransactionsDisplayList(
                transactions = filteredTransactions,
                accounts = accounts,
                themeColors = MaterialTheme.colorScheme,
                selectedTransactionIds = selectedTransactions,
                onTransactionClick = toggleTransactionSelection,
                onTransactionLongPress = onTransactionLongPress,
                isInSelectionMode = isInSelectionMode,
                scrollOverScrollState = searchScrollState,
                overscrollEffect = searchOverscrollEffect,
                navController = navController,
                animatedVisibilityScope = animatedVisibilityScope,
                showTransactionCashFlowHeader = false,
                showMonthInDateHeader = true,
                modifier = Modifier.zIndex(1f),
                accountUiState = accountUiState,
                onAccountEvent = onAccountEvent,
                onAddTransactionEvent = onAddTransactionEvent,
                onCategoryEvent = onCategoryEvent,
                onSubCategoryEvent = onSubCategoryEvent,
            )
        }
    }

    // Filter Bottom Sheet
    if (showFilterBottomSheet) {
        ModalBottomSheet(
            sheetState = filterSheetState,
            onDismissRequest = { showFilterBottomSheet = false },
            sheetMaxWidth = usedScreenWidth - 4.dp,
            dragHandle = {
                DragHandle(
                    color = MaterialTheme.colorScheme.inverseSurface.copy(0.3f)
                )
            },
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            containerColor =  MaterialTheme.colorScheme.background,
            contentColor =  MaterialTheme.colorScheme.inverseSurface
        ) {
            TransactionFilterBottomSheet(
                filterState = filterState,
                onFilterStateChange = { newFilterState ->
                    filterState = newFilterState
                },
                categories = categories,
                accounts = accounts,
                subCategories = allSubCategories,
                onApply = { showFilterBottomSheet = false },
                usedScreenWidth = usedScreenWidth,
                usedScreenHeight = usedScreenHeight,
                modifier = Modifier.height(usedScreenHeight),
                transactionUiState = transactionUiState,
                onAddTransactionEvent = onAddTransactionEvent
            )
        }
    }
}


// Component for selection mode top app bar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchSelectionTopAppBar(
    selectedCount: Int,
    onClearSelection: () -> Unit,
    onDeleteSelected: () -> Unit,
    onSelectAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .animateContentSize()
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClearSelection) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear Selection",
                    tint = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                )
            }

            Text(
                text = "$selectedCount Selected",
                fontFamily = iosFont,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.inverseSurface
            )
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onSelectAll) {
                Icon(
                    painter = painterResource(R.drawable.select_all),
                    tint = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                    contentDescription = "Select All"
                )
            }
            IconButton(onClick = onDeleteSelected) {
                Icon(
                    painter = painterResource(R.drawable.delete_bulk),
                    tint = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                    contentDescription = "Delete Selected"
                )
            }
        }
    }
}