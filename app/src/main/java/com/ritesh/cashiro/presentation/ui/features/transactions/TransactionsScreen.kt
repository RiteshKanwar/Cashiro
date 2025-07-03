package com.ritesh.cashiro.presentation.ui.features.transactions

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.ritesh.cashiro.R
import com.ritesh.cashiro.data.local.entity.CategoryEntity
import com.ritesh.cashiro.data.local.entity.TransactionEntity
import com.ritesh.cashiro.presentation.ui.features.add_transaction.AddTransactionScreenViewModel
import com.ritesh.cashiro.presentation.ui.features.categories.CategoryScreenViewModel
import com.ritesh.cashiro.domain.utils.formattedTimeText
import com.ritesh.cashiro.domain.utils.icons
import com.ritesh.cashiro.presentation.effects.VerticalStretchedOverscroll
import com.ritesh.cashiro.presentation.ui.extras.components.extras.CustomTitleTopAppBar
import com.ritesh.cashiro.presentation.ui.extras.components.extras.HorizontalMonthCalendar
import com.ritesh.cashiro.presentation.ui.theme.iosFont
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.BottomSheetDefaults.DragHandle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontStyle
import androidx.navigation.NavController
import com.ritesh.cashiro.data.local.entity.AccountEntity
import com.ritesh.cashiro.data.local.entity.SubCategoryEntity
import com.ritesh.cashiro.data.local.entity.TransactionType
import com.ritesh.cashiro.data.currency.model.CurrencySymbols
import com.ritesh.cashiro.domain.utils.AppEventBus
import com.ritesh.cashiro.domain.utils.CurrencyEvent
import com.ritesh.cashiro.domain.utils.CurrencyUtils
import com.ritesh.cashiro.domain.utils.TransactionFilterUtils
import com.ritesh.cashiro.domain.utils.getConvertedAmount
import com.ritesh.cashiro.presentation.effects.BlurredAnimatedVisibility
import com.ritesh.cashiro.presentation.ui.extras.components.extras.TransactionDeletionConfirmationDialog
import com.ritesh.cashiro.presentation.ui.extras.components.charts.formatCurrency
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenEvent
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenState
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenViewModel
import com.ritesh.cashiro.presentation.ui.features.add_transaction.AddTransactionEvent
import com.ritesh.cashiro.presentation.ui.features.add_transaction.AddTransactionScreenState
import com.ritesh.cashiro.presentation.ui.features.categories.CategoryScreenEvent
import com.ritesh.cashiro.presentation.ui.features.categories.CategoryScreenState
import com.ritesh.cashiro.presentation.ui.features.categories.SubCategoryEvent
import com.ritesh.cashiro.presentation.ui.features.categories.SubCategoryState
import com.ritesh.cashiro.presentation.ui.features.categories.SubCategoryViewModel
import com.ritesh.cashiro.presentation.ui.navigation.FAB_ADD_TRANSACTION_KEY
import com.ritesh.cashiro.presentation.ui.navigation.NavGraph
import com.ritesh.cashiro.presentation.ui.navigation.SEARCH_TRANSACTION_BUTTON_KEY
import com.ritesh.cashiro.presentation.ui.navigation.TRANSACTION_SCREEN_TRANSACTION_CARD_KEY_PREFIX

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalSharedTransitionApi::class
)
@Composable
fun SharedTransitionScope.TransactionsScreen(
    deleteTransactions: (List<Int>) -> Unit,
    accountUiState: AccountScreenState,
    onAccountEvent: (AccountScreenEvent) -> Unit,
    transactionUiState: AddTransactionScreenState,
    onAddTransactionEvent: (AddTransactionEvent) -> Unit,
    categoryUiState: CategoryScreenState,
    onCategoryEvent: (CategoryScreenEvent) -> Unit,
    subCategoryUiState: SubCategoryState,
    onSubCategoryEvent: (SubCategoryEvent) -> Unit,
    screenTitle: String,
    navController: NavController,
    onSearchButtonClick: () -> Unit,
    onFabClick: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
//    val screenState by transactionViewModel.state.collectAsState()
    val transactions = transactionUiState.transactions
    val accounts = accountUiState.accounts
    val categories = categoryUiState.categories
    val categoriesWithSubCategories = subCategoryUiState.categoriesWithSubCategories
    val allSubCategories = categoriesWithSubCategories.flatMap { it.subCategories }

    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

    var selectedTransactions by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var isInSelectionMode by remember { mutableStateOf(false) }

    // Filter state
    var filterState by remember { mutableStateOf(TransactionFilterState()) }
    var showFilterBottomSheet by rememberSaveable { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val usedScreenHeight = screenHeight / 1.2f
    val usedScreenWidth = screenWidth - 10.dp
    // Apply filters to transactions
    val filteredTransactions = remember(transactions, filterState) {
        TransactionFilterUtils.applyFilters(
            transactions = transactions,
            filterState = filterState,
            categories = categories,
            accounts = accounts
        )
    }

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
            Log.d("Selection", "Regular click, not in selection mode")
        }
    }

    val onTransactionLongPress: (TransactionEntity) -> Unit = { transaction ->
        isInSelectionMode = true
        selectedTransactions = selectedTransactions + transaction.id
    }

    // Function to clear all selections and exit selection mode
    val clearSelections = {
        selectedTransactions = emptySet()
        isInSelectionMode = false
    }

    val deleteSelectedTransactions = {
        // Show confirmation dialog instead of deleting immediately
        showDeleteConfirmationDialog = true
    }

    // Listen for deletion events from the ViewModel
    LaunchedEffect(filteredTransactions) {
        // This will trigger when transactions list changes (like after a delete operation)
        // It ensures the UI state is properly reset
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

    val coroutineScope = rememberCoroutineScope()
    val searchSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isSearchButtonClicked by rememberSaveable { mutableStateOf(false) }

    // Filter bottom sheet state
    val filterSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Scaffold State for Scroll Behavior
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scrollBehaviorSmall = TopAppBarDefaults.pinnedScrollBehavior()

    val currentDate = LocalDate.now()

    // Calculate date range (current year and previous year)
    val endDate = currentDate.plusYears(1).withMonth(1).withDayOfMonth(1)
    val startDate = currentDate.minusYears(1).withMonth(1).withDayOfMonth(1)

    // Calculate total month count
    val monthCount = ChronoUnit.MONTHS.between(startDate, endDate.plusMonths(12)).toInt()

    // Calculate initial page for current month
    val initialPage = ChronoUnit.MONTHS.between(startDate, currentDate).toInt()

    // Create a pager state with the initial page set to current month
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { monthCount }
    )

    var selectedDate by remember { mutableStateOf(currentDate.withDayOfMonth(1)) }

    // Update selectedDate when pager changes
    LaunchedEffect(pagerState.currentPage) {
        // Calculate the exact date for the current page
        selectedDate = startDate.plusMonths(pagerState.currentPage.toLong())
        Log.d("PagerEffect", "Changed to page: ${pagerState.currentPage}, date: $selectedDate")
    }

    val scrollStates = remember {
        List(monthCount) { LazyListState() }
    }

    val overscrollEffects = remember(coroutineScope) {
        List(monthCount) { VerticalStretchedOverscroll(coroutineScope) }
    }

    // Reset overscroll effect when page changes
    LaunchedEffect(pagerState.currentPage) {
        overscrollEffects[pagerState.currentPage].reset()
    }

    // Observe changes to the selectedDate from the HorizontalMonthCalendar
    // and update the pager accordingly
    LaunchedEffect(selectedDate) {
        val targetPage = ChronoUnit.MONTHS.between(startDate, selectedDate).toInt()
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    LaunchedEffect(key1 = Unit) {
        onAccountEvent(AccountScreenEvent.FetchAllAccounts)
        onAddTransactionEvent(AddTransactionEvent.FetchAllTransactions)
    }

    // Function to select all transactions for the current month
    val selectAllTransactions = {
        if (!isInSelectionMode) {
            isInSelectionMode = true
        }

        // Get the current visible month
        val pageDate = startDate.plusMonths(pagerState.currentPage.toLong())

        // Find all transaction IDs for this month from filtered transactions
        val monthTransactionIds = filteredTransactions.filter { transaction ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = transaction.date

            val transactionYear = calendar.get(Calendar.YEAR)
            val transactionMonth = calendar.get(Calendar.MONTH) + 1

            transactionYear == pageDate.year &&
                    transactionMonth == pageDate.monthValue
        }.map { it.id }.toSet()

        // Update selected transactions
        selectedTransactions = monthTransactionIds
        Log.d("SelectAll", "Selected all ${monthTransactionIds.size} transactions for ${pageDate.month} ${pageDate.year}")
        Unit
    }

    // Modified BackHandler to handle selection mode
    BackHandler(enabled = isInSelectionMode) {
        clearSelections()
    }

    Scaffold(modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CustomTitleTopAppBar(
                scrollBehaviorLarge = scrollBehavior,
                scrollBehaviorSmall = scrollBehaviorSmall,
                title = screenTitle,
                isInSelectionMode = isInSelectionMode,
                selectedCount = selectedTransactions.size,
                onClearSelection = clearSelections,
                onDeleteSelected = deleteSelectedTransactions,
                onSelectAll = selectAllTransactions,
                onSearchButtonClick = onSearchButtonClick,
                // Add filter-related parameters
                onFilterButtonClick = { showFilterBottomSheet = true },
                hasFilterButton = true,
                filterState = filterState,
                modifier = Modifier.sharedBounds(
                    resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds(
                        contentScale = ContentScale.Inside
                    ),
                    sharedContentState = rememberSharedContentState(
                        key = SEARCH_TRANSACTION_BUTTON_KEY
                    ),
                    animatedVisibilityScope = animatedVisibilityScope
                )
            )
        },
        floatingActionButton = {
            Box(modifier = Modifier.fillMaxWidth().padding(bottom = 80.dp),
                contentAlignment = Alignment.BottomEnd){
                FloatingActionButton(
                    onClick = onFabClick,
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.sharedBounds(
                        resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds(
                            contentScale = ContentScale.Inside
                        ),
                        sharedContentState = rememberSharedContentState(
                            key = FAB_ADD_TRANSACTION_KEY),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        tint = Color.White,
                        contentDescription = "Add Transaction"
                    )
                }
            }
        }
    ) {innerPadding->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HorizontalMonthCalendar(
                selectedDate = selectedDate, // Pass current selected date
                onMonthSelected = { newDate ->
                    selectedDate = newDate
                    // Calculate page index when month is selected from calendar
                    val targetPage = ChronoUnit.MONTHS.between(startDate, newDate).toInt()
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(targetPage)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(2f)
            )

            // HorizontalPager replaces LazyColumn
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Top,
                userScrollEnabled = true // Ensure user can scroll horizontally
            ) { page ->
                // Calculate date for this page
                val pageDate = startDate.plusMonths(page.toLong())
                Log.d("Pager", "Showing page $page for date: $pageDate")

                // Find transactions for this month from filtered transactions
                val monthTransactions = filteredTransactions.filter { transaction ->
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = transaction.date

                    val transactionYear = calendar.get(Calendar.YEAR)
                    val transactionMonth = calendar.get(Calendar.MONTH) + 1

                    transactionYear == pageDate.year &&
                            transactionMonth == pageDate.monthValue
                }

                val pageScrollState = scrollStates[page]
                val pageOverscrollEffect = overscrollEffects[page]

                // If no transactions, show empty state
                if (monthTransactions.isEmpty()) {
                    EmptyTransactions()
                } else {
                    // Display transactions for this page date
                    TransactionsDisplayList(
                        transactions = filteredTransactions, // Pass filtered transactions
                        accounts = accounts,
                        themeColors = MaterialTheme.colorScheme,
                        selectedDate = pageDate,
                        // Pass selection-related parameters and callbacks
                        selectedTransactionIds = selectedTransactions,
                        onTransactionClick = toggleTransactionSelection,
                        onTransactionLongPress = onTransactionLongPress,
                        isInSelectionMode = isInSelectionMode,
                        scrollOverScrollState = pageScrollState,
                        overscrollEffect = pageOverscrollEffect,
                        navController = navController,
                        animatedVisibilityScope = animatedVisibilityScope,
                        modifier = Modifier.padding(horizontal = 20.dp),
                        accountUiState = accountUiState,
                        onAccountEvent = onAccountEvent,
                        onAddTransactionEvent = onAddTransactionEvent,
                        onCategoryEvent = onCategoryEvent,
                        onSubCategoryEvent = onSubCategoryEvent,
                    )
                }
            }
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

    if (showDeleteConfirmationDialog) {
        TransactionDeletionConfirmationDialog(
            showDialog = showDeleteConfirmationDialog,
            transactionCount = selectedTransactions.size,
            onConfirm = {
                // Actually delete the transactions after confirmation
                deleteTransactions(selectedTransactions.toList())
                showDeleteConfirmationDialog = false
                clearSelections()
            },
            onDismiss = {
                showDeleteConfirmationDialog = false
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.TransactionsDisplayList(
    modifier: Modifier = Modifier,
    transactions: List<TransactionEntity>,
    accounts: List<AccountEntity>,
    accountUiState: AccountScreenState,
    onAccountEvent: (AccountScreenEvent) -> Unit,
    onAddTransactionEvent: (AddTransactionEvent) -> Unit,
    onCategoryEvent: (CategoryScreenEvent) -> Unit,
    onSubCategoryEvent: (SubCategoryEvent) -> Unit,
    themeColors: ColorScheme,
    selectedDate: LocalDate? = null, // Make selectedDate optional
    selectedTransactionIds: Set<Int> = emptySet(),
    onTransactionClick: (TransactionEntity) -> Unit = {},
    onTransactionLongPress: (TransactionEntity) -> Unit ={},
    isInSelectionMode: Boolean = false,
    scrollOverScrollState: LazyListState,
    overscrollEffect: VerticalStretchedOverscroll,
    navController: NavController,
    animatedVisibilityScope: AnimatedVisibilityScope,
    showTransactionCashFlowHeader: Boolean = true,
    showTransactionCashFlowBottomDetails: Boolean = true,
    showViewAllTransactionButton: Boolean = false,
    showMonthInDateHeader: Boolean = false,
) {
    val mainCurrencySymbol = accounts.find { it.isMainAccount }?.currencyCode ?: "usd"
    val mainCurrency = CurrencySymbols.getSymbol(mainCurrencySymbol)

    val conversionRates = accountUiState.mainCurrencyConversionRates
    LaunchedEffect(Unit) {
        AppEventBus.events.collect { event ->
            when (event) {
                is CurrencyEvent.AccountCurrencyChanged,
                is CurrencyEvent.MainAccountCurrencyChanged,
                is CurrencyEvent.ConversionRatesUpdated -> {
                    // Force recomposition
                    onAccountEvent(AccountScreenEvent.FetchAllAccounts)
                    onAddTransactionEvent(AddTransactionEvent.FetchAllTransactions)
                }
            }
        }
    }
    if (transactions.isEmpty()){
        EmptyTransactions(modifier)
        return
    }

    // Filter transactions only if selectedDate is provided
    val filteredTransactions = if (selectedDate != null) {
        transactions.filter { transaction ->
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = transaction.date

            val transactionYear = calendar.get(Calendar.YEAR)
            val transactionMonth = calendar.get(Calendar.MONTH) + 1

            transactionYear == selectedDate.year && transactionMonth == selectedDate.monthValue
        }
    } else {
        // If no date is provided, use all transactions
        transactions
    }

    // Calculate totals based on filtered transactions
    val totalExpense = CurrencyUtils.calculateTotalExpense(
        filteredTransactions,
        mainCurrencySymbol,
        conversionRates
    )

    val totalIncome = CurrencyUtils.calculateTotalIncome(
        filteredTransactions,
        mainCurrencySymbol,
        conversionRates
    )


    val totalTransactionCount = filteredTransactions.size
    val totalCashFlow = totalIncome - totalExpense

    // Group transactions by date
    val groupedTransactions = filteredTransactions.groupBy { transaction ->
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = transaction.date
        // Group by year, month, and day
        Triple(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }.toSortedMap(compareByDescending { (year, month, day) ->
        // Sort by date (most recent first)
        LocalDate.of(year, month + 1, day)
    })

    // Modify each group to sort transactions by time in descending order
    val sortedGroupedTransactions = groupedTransactions.mapValues { (_, transactionsForDay) ->
        transactionsForDay.sortedByDescending { transaction ->
            // Extract time portion for sorting
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = transaction.time
            // This will create a value based on hours and minutes for sorting
            calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        }
    }

    LazyColumn(
        state = scrollOverScrollState,
        userScrollEnabled = true,
        modifier = modifier
            .fillMaxWidth()
            .overscroll(overscrollEffect),
//            .scrollable(
//                orientation = Orientation.Vertical,
//                reverseDirection = true,
//                state = scrollOverScrollState,
//                overscrollEffect = overscrollEffect
//            ),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item(key = "Transaction cash flow Details Header"){
            if (showTransactionCashFlowHeader){
                Row(
                    modifier = Modifier.fillMaxWidth().background(themeColors.surfaceBright, RoundedCornerShape(15.dp)).padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        text = formatCurrency(value = totalExpense, currencySymbol = mainCurrency),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(start = 20.dp, bottom = 10.dp, top = 10.dp),
                        textAlign = TextAlign.Start,
                        fontFamily = iosFont,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = formatCurrency(value = totalIncome, currencySymbol = mainCurrency),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(bottom = 10.dp, top = 10.dp),
                        textAlign = TextAlign.Center,
                        fontFamily = iosFont,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = "= ${formatCurrency(value = totalCashFlow, currencySymbol = mainCurrency)}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(end = 20.dp, bottom = 10.dp, top = 10.dp),
                        textAlign = TextAlign.End,
                        fontFamily = iosFont,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = themeColors.inverseSurface
                    )
                }
            }
        }

        // Iterate through each date group
        sortedGroupedTransactions.forEach { (dateGroup, transactionsForDate) ->
            val (year, month, day) = dateGroup
            val date = LocalDate.of(year, month + 1, day)

            val dailyExpense = CurrencyUtils.calculateTotalExpense(
                transactionsForDate,
                mainCurrencySymbol,
                conversionRates
            )

            val dailyIncome = CurrencyUtils.calculateTotalIncome(
                transactionsForDate,
                mainCurrencySymbol,
                conversionRates
            )

            val dailyCashFlow = dailyIncome - dailyExpense
            val dailyTransactionCount = transactionsForDate.size
            val dateFormat = if (showMonthInDateHeader) date.format(DateTimeFormatter.ofPattern("dd EEEE, MMMM"))
            else date.format(DateTimeFormatter.ofPattern("dd EEEE"))
            // Display date header once per group
            stickyHeader() {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    themeColors.background,
                                    themeColors.background,
                                    Color.Transparent,
                                )
                            )
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dateFormat ,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(start = 20.dp, bottom = 10.dp, top = 10.dp),
                        textAlign = TextAlign.Start,
                        fontFamily = iosFont,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = themeColors.inverseSurface.copy(0.6f)
                    )
                    if(dailyTransactionCount > 1) {
                        Text(
                            text = "= ${formatCurrency(value = dailyCashFlow, currencySymbol =  mainCurrency)}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(end = 20.dp, bottom = 10.dp, top = 10.dp),
                            textAlign = TextAlign.End,
                            fontFamily = iosFont,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = themeColors.inverseSurface.copy(0.6f)
                        )
                    }
                }
            }

            // Use itemsIndexed with the proper content parameter format
            itemsIndexed(
                items = transactionsForDate,
                key = { _, transaction -> transaction.id }
            ) { index, transaction ->
                val isItemSelected = selectedTransactionIds.contains(transaction.id)
                val account = accounts.find { it.id == transaction.accountId }
                Log.d("TransactionItem", "Transaction ${transaction.id} selected: $isItemSelected")
                TransactionCardLayout(
                    transaction = transaction,
                    account = account,
                    themeColors = themeColors,
                    isSelected = isItemSelected,
                    isInSelectionMode = isInSelectionMode,
                    onSelectionClick = { onTransactionClick(transaction) },
                    onLongClick = {
                        onTransactionLongPress(transaction)
                    },
                    navController = navController,
                    animatedVisibilityScope = animatedVisibilityScope,
                    accountUiState = accountUiState,
                    onCategoryEvent = onCategoryEvent,
                    onSubCategoryEvent = onSubCategoryEvent,
                    onAddTransactionEvent = onAddTransactionEvent,
                )
                Spacer(modifier = Modifier.height(15.dp))
            }
        }

        item(key = "Transaction cash flow Details"){
            if (showTransactionCashFlowBottomDetails) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Update summary text based on whether filtering by date or showing all transactions
                    Text(
                        text = if (selectedDate != null)
                            "Total cash flow for ${
                                selectedDate.month.toString().lowercase()
                            } = ${formatCurrency(totalCashFlow, mainCurrency)}"
                        else
                            "Total cash flow = ${formatCurrency(totalCashFlow, mainCurrency)}",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontFamily = iosFont,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = themeColors.inverseSurface.copy(0.6f)
                    )
                    Text(
                        text = "$totalTransactionCount transactions",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontFamily = iosFont,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = themeColors.inverseSurface.copy(0.6f)
                    )
                }
            }
        }
        item(key = "view all transactions button") {
            if (showViewAllTransactionButton) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TextButton(
                        onClick = { navController.navigate(NavGraph.TRANSACTIONS){launchSingleTop = true} },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f)
                        ),
                    ) {
                        Text(
                            text = "View All",
                            fontFamily = iosFont,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = RoundedCornerShape(15.dp)
                                )
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }
        item(key = "scrolling end space") {
            Spacer(modifier = Modifier.height(150.dp))
        }
    }
}

@Composable
fun EmptyTransactions(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(
                    id =  R.drawable.empty_category_list
                ),
                contentDescription = "Empty Accounts List Icon",
                modifier = Modifier.size(300.dp)
            )
            Text(
                text = "No Transaction available",
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
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.TransactionCardLayout(
    transaction: TransactionEntity,
    account: AccountEntity?,
    accountUiState: AccountScreenState,
    onCategoryEvent: (CategoryScreenEvent) -> Unit,
    onSubCategoryEvent: (SubCategoryEvent) -> Unit,
    onAddTransactionEvent: (AddTransactionEvent) -> Unit,
    themeColors: ColorScheme,
    isSelected: Boolean = false,
    isInSelectionMode: Boolean = false,
    onSelectionClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    navController: NavController,
    animatedVisibilityScope: AnimatedVisibilityScope
){
    if (accountUiState.accounts.isEmpty()) {
        return
    }
    val transactionKey = "${TRANSACTION_SCREEN_TRANSACTION_CARD_KEY_PREFIX}${transaction.id}"
    // For haptic feedback
    val haptic = LocalHapticFeedback.current

    var retrievedCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    LaunchedEffect(transaction.categoryId) {
        onCategoryEvent(
            CategoryScreenEvent.OnCategoryFetched(
                id = transaction.categoryId,
                onSuccess = { category ->
                    retrievedCategory = category
                }
            )
        )
    }
    var retrievedSubCategory by remember { mutableStateOf<SubCategoryEntity?>(null) }
    LaunchedEffect(transaction.subCategoryId) {
        if (transaction.subCategoryId != null && transaction.subCategoryId != 0) {
            onSubCategoryEvent(SubCategoryEvent.GetSubCategoryById(transaction.subCategoryId) { subCategory ->
                retrievedSubCategory = subCategory
            })
        } else {
            retrievedSubCategory = null
        }
    }

    // Determine which information to display (prioritize subcategory over category)
    val displayName = if (retrievedSubCategory != null) {
        retrievedSubCategory!!.name
    } else {
        retrievedCategory?.name ?: ""
    }

    val displayBoxColor = if (retrievedSubCategory != null) {
        Color(retrievedSubCategory!!.boxColor ?: 0)
    } else {
        Color(retrievedCategory?.boxColor ?: 0)
    }

    val displayIconID = if (retrievedSubCategory != null) {
        retrievedSubCategory!!.subcategoryIconId ?: 0
    } else {
        retrievedCategory?.categoryIconId ?: 0
    }

    // Get the main account for currency conversion
    val mainAccount = remember { derivedStateOf {
        accountUiState.accounts.find { it.isMainAccount }
    }}
    val currencyCode = account?.currencyCode ?: transaction.originalCurrencyCode ?: "usd"
    val currencySymbol = remember(
        account?.id,
        account?.currencyCode,
        transaction.originalCurrencyCode,
        accountUiState.lastCurrencyUpdateTimestamp
    ) {
        CurrencySymbols.getSymbol(currencyCode)
    }

    // Get main account currency code and symbol for conversion display
    val mainCurrencyCode = mainAccount.value?.currencyCode ?: "usd"
    val mainCurrencySymbol = remember(
        mainAccount.value?.id,
        mainAccount.value?.currencyCode,
        accountUiState.lastCurrencyUpdateTimestamp
    ) {
        CurrencySymbols.getSymbol(mainCurrencyCode)
    }

    // Get conversion rates
    val conversionRates = accountUiState.mainCurrencyConversionRates

    // Calculate the converted amount if currencies are different
    val showConvertedAmount = remember(
        currencyCode,
        mainCurrencyCode,
        account?.id,
        accountUiState.lastCurrencyUpdateTimestamp
    ) {
        currencyCode != mainCurrencyCode && account != null && !account.isMainAccount
    }

    val convertedAmount = remember(
        transaction.amount,
        currencyCode,
        mainCurrencyCode,
        conversionRates,
        accountUiState.lastCurrencyUpdateTimestamp
    ) {
        if (showConvertedAmount) {
            transaction.getConvertedAmount(currencyCode, mainCurrencyCode, conversionRates)
        } else {
            transaction.amount
        }
    }

    // Determine status button properties based on transaction type (toggle functionality)
    val statusButtonInfo = remember(transaction.transactionType, transaction.isPaid, transaction.isCollected, transaction.isSettled) {
        when (transaction.transactionType) {
            // SUBSCRIPTION and REPETITIVE: Always show toggle button
            TransactionType.SUBSCRIPTION, TransactionType.REPETITIVE, TransactionType.UPCOMING-> {
                if (!transaction.isPaid) {
                    StatusButtonInfo(
                        text = "Pay",
                        icon = Icons.Default.CheckCircle,
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White,
                        action = {
                            onAddTransactionEvent(
                                AddTransactionEvent.MarkTransactionAsPaid(transaction)
                            )
                        }
                    )
                } else {
                    StatusButtonInfo(
                        text = "Unpaid",
                        icon = Icons.Default.Check,
                        containerColor = Color(0xFFE57373),
                        contentColor = Color.White,
                        action = {
                            onAddTransactionEvent(
                                AddTransactionEvent.MarkTransactionAsUnpaid(transaction)
                            )
                        }
                    )
                }
            }

            // BORROWED: Show toggle button (settled/unsettled)
            TransactionType.BORROWED -> {
                if (!transaction.isSettled) {
                    StatusButtonInfo(
                        text = "Settle",
                        icon = Icons.Default.KeyboardArrowDown,
                        containerColor = Color(0xFF2196F3),
                        contentColor = Color.White,
                        action = {
                            onAddTransactionEvent(
                                AddTransactionEvent.MarkTransactionAsSettled(transaction)
                            )
                        }
                    )
                } else {
                    StatusButtonInfo(
                        text = "Unsettle",
                        icon = Icons.Default.KeyboardArrowUp,
                        containerColor = Color(0xFFFF5722),
                        contentColor = Color.White,
                        action = {
                            onAddTransactionEvent(
                                AddTransactionEvent.MarkTransactionAsUnsettled(transaction)
                            )
                        }
                    )
                }
            }

            // LENT: Show toggle button (collected/not collected)
            TransactionType.LENT -> {
                if (!transaction.isCollected) {
                    StatusButtonInfo(
                        text = "Collect",
                        icon = Icons.Default.KeyboardArrowUp,
                        containerColor = Color(0xFFFF9800),
                        contentColor = Color.White,
                        action = {
                            onAddTransactionEvent(
                                AddTransactionEvent.MarkTransactionAsCollected(transaction)
                            )
                        }
                    )
                } else {
                    StatusButtonInfo(
                        text = "Uncollected",
                        icon = Icons.Default.KeyboardArrowDown,
                        containerColor = Color(0xFF9C27B0),
                        contentColor = Color.White,
                        action = {
                            onAddTransactionEvent(
                                AddTransactionEvent.MarkTransactionAsNotCollected(transaction)
                            )
                        }
                    )
                }
            }

            // For other transaction types, don't show any button
            else -> null
        }
    }

    Column(
        modifier = Modifier.sharedBounds(
            resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds(
                contentScale = ContentScale.Inside
            ),
            sharedContentState = rememberSharedContentState(key = transactionKey),
            animatedVisibilityScope = animatedVisibilityScope
        )
    ) {
        Box(
            modifier = Modifier
                .pointerInput(isInSelectionMode) {
                    detectTapGestures(
                        onLongPress = {
                            // Provide haptic feedback
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            // Activate selection mode
                            onLongClick()
                        },
                        onTap = {
                            if (isInSelectionMode) {
                                // Only handle selection clicks when in selection mode
                                onSelectionClick()
                            } else {
                                navController.currentBackStackEntry?.savedStateHandle?.set("transactionId", transaction.id)
                                navController.currentBackStackEntry?.savedStateHandle?.set("isUpdateTransaction", true)
                                navController.navigate(NavGraph.ADD_TRANSACTION)
                            }
                        }
                    )
                }
        ) {
            Box(
                modifier = Modifier
                    .height(if (statusButtonInfo != null) 160.dp else 104.dp) // Always increase height for toggle button transaction types
                    .fillMaxWidth()
                    .background(
                        if (isSelected) themeColors.primaryContainer else themeColors.surface,
                        shape = RoundedCornerShape(20.dp)
                    )
                    // Add a border when in selection mode to make it more obvious
                    .then(
                        if (isInSelectionMode && !isSelected)
                            Modifier.border(
                                width = 1.dp,
                                color = themeColors.outlineVariant,
                                shape = RoundedCornerShape(20.dp)
                            )
                        else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                val selectionBoxWidth by animateDpAsState(
                    targetValue = if (isInSelectionMode) 34.dp else 0.dp,
                    animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing)
                )
                val spacerWidth by animateDpAsState(
                    targetValue = if (isInSelectionMode) 5.dp else 0.dp,
                    animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing)
                )
                // Create an alpha value for smoother fade in/out
                val selectionAlpha by animateFloatAsState(
                    targetValue = if (isInSelectionMode) 1f else 0f,
                    animationSpec = tween(durationMillis = 250)
                )

                Box() {
                    // Status Button Section
                    statusButtonInfo?.let { buttonInfo ->
//                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    color = themeColors.surfaceBright,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .padding(bottom = 12.dp)
                                .align(Alignment.BottomCenter),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if ((transaction.transactionType == TransactionType.UPCOMING || transaction.transactionType == TransactionType.SUBSCRIPTION || transaction.transactionType == TransactionType.REPETITIVE) && transaction.isPaid) {"This ${transaction.transactionType.toString().lowercase()} transaction has been \"Paid\""}
                                    else if (transaction.transactionType == TransactionType.LENT && transaction.isCollected) "This ${transaction.transactionType.toString().lowercase()} transaction has been \"Collected\""
                                    else if (transaction.transactionType == TransactionType.BORROWED && transaction.isSettled) "This ${transaction.transactionType.toString().lowercase()} transaction has been \"Settled\""
                                    else if (transaction.transactionType == TransactionType.BORROWED && !transaction.isSettled) "This ${transaction.transactionType.toString().lowercase()} transaction is \"Not Settled\""
                                    else if (transaction.transactionType == TransactionType.LENT && !transaction.isCollected) "This ${transaction.transactionType.toString().lowercase()} transaction is \"Not Collected\""
                                    else "This ${transaction.transactionType.toString().lowercase()} transaction is \"Not Paid\"",
                                    color = themeColors.inverseSurface.copy(0.6f),
                                    fontFamily = iosFont,
                                    fontSize = 12.sp,
                                    lineHeight = 13.sp,
                                    fontStyle = FontStyle.Italic,
                                    fontWeight = FontWeight.Normal,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 10.dp).weight(1f)
                                )
                                Button(
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        buttonInfo.action()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = buttonInfo.containerColor,
                                        contentColor = buttonInfo.contentColor
                                    ),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.height(32.dp).padding(start = 10.dp, end = 10.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                ) {
                                    Row(
                                        modifier = Modifier,
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = buttonInfo.icon,
                                            contentDescription = buttonInfo.text,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(Modifier.size(4.dp))
                                        Text(
                                            text = buttonInfo.text,
                                            fontFamily = iosFont,
                                            fontSize = 12.sp,
                                            maxLines = 2,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = if (isSelected) themeColors.primaryContainer else themeColors.surface,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(20.dp)
                            .align(Alignment.TopCenter),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        // Selection indicator with controlled width and alpha
                        Box(
                            modifier = Modifier.width(selectionBoxWidth)
                        ) {
                            BlurredAnimatedVisibility(selectionBoxWidth > 0.dp) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .alpha(selectionAlpha)
                                        .background(
                                            if (isSelected) themeColors.primary else themeColors.surface,
                                            shape = CircleShape
                                        )
                                        .border(
                                            width = 2.dp,
                                            color = themeColors.primary,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = themeColors.onPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(spacerWidth))

                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .shadow(10.dp, RoundedCornerShape(20.dp))
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    color = if (displayBoxColor == Color.Transparent) themeColors.onBackground else displayBoxColor,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Display SubCategory Icon if available, otherwise Category Icon
                            Image(
                                painter = painterResource(
                                    id = icons.find { it.id == displayIconID }?.resourceId
                                        ?: R.drawable.type_beverages_beer
                                ),
                                contentDescription = if (retrievedSubCategory != null) "SubCategory Icon" else "Category Icon",
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = transaction.title,
                                overflow = TextOverflow.Ellipsis,
                                fontFamily = iosFont,
                                maxLines = 1,
                                fontWeight = FontWeight.Medium,
                                fontSize = 18.sp,
                                color = themeColors.inversePrimary,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = displayName,
                                    fontFamily = iosFont,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 12.sp,
                                    lineHeight = 12.sp,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1,
                                    color = themeColors.inverseSurface,
                                    modifier = Modifier
                                        .padding(top = 5.dp)
                                        .background(
                                            color = if (displayBoxColor == Color.Transparent) themeColors.onBackground.copy(
                                                0.5f
                                            ) else displayBoxColor.copy(0.2f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .padding(vertical = 5.dp, horizontal = 10.dp)
                                )
                                // Account name
                                Text(
                                    text = account?.accountName ?: "Unknown",
                                    fontFamily = iosFont,
                                    fontSize = 12.sp,
                                    lineHeight = 12.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = themeColors.inverseSurface.copy(0.6f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(top = 5.dp)
                                )
                            }
                        }

                        // Second column with amount and time
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = AbsoluteAlignment.Right,
                            modifier = Modifier.weight(0.5f)
                        ) {
                            Text(
                                text = if (showConvertedAmount){formatCurrency(value = convertedAmount, currencySymbol = mainCurrencySymbol)}
                                else formatCurrency(value = transaction.amount, currencySymbol = currencySymbol),
                                textAlign = TextAlign.End,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                fontFamily = iosFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = if (transaction.mode == "Expense") themeColors.onError
                                else if (transaction.mode == "Transfer") themeColors.inverseSurface.copy(0.6f)
                                else Color(0xFF7ACE96)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (showConvertedAmount) formatCurrency(value = transaction.amount, currencySymbol = currencySymbol)
                                    else formattedTimeText(transaction.time),
                                    textAlign = TextAlign.End,
                                    fontFamily = iosFont,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (transaction.mode == "Expense" && showConvertedAmount) themeColors.onError.copy(0.6f)
                                    else if (transaction.mode == "Income" && showConvertedAmount) Color(0xFF7ACE96).copy(0.6f)
                                    else themeColors.inverseSurface.copy(0.6f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Data class to hold status button information
private data class StatusButtonInfo(
    val text: String,
    val icon: ImageVector,
    val containerColor: Color,
    val contentColor: Color,
    val action: () -> Unit
)