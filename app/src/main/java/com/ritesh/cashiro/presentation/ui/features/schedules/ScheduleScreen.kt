package com.ritesh.cashiro.presentation.ui.features.schedules

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.ritesh.cashiro.data.currency.model.CurrencySymbols
import com.ritesh.cashiro.data.local.entity.AccountEntity
import com.ritesh.cashiro.data.local.entity.TransactionEntity
import com.ritesh.cashiro.domain.utils.CurrencyUtils
import com.ritesh.cashiro.presentation.effects.VerticalStretchedOverscroll
import com.ritesh.cashiro.presentation.ui.extras.components.charts.formatCurrency
import com.ritesh.cashiro.presentation.ui.extras.components.extras.CustomTitleTopAppBar
import com.ritesh.cashiro.presentation.ui.extras.components.extras.TransactionDeletionConfirmationDialog
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenEvent
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenState
import com.ritesh.cashiro.presentation.ui.features.add_transaction.AddTransactionEvent
import com.ritesh.cashiro.presentation.ui.features.categories.CategoryScreenEvent
import com.ritesh.cashiro.presentation.ui.features.categories.SubCategoryEvent
import com.ritesh.cashiro.presentation.ui.features.transactions.TransactionCardLayout
import com.ritesh.cashiro.presentation.ui.navigation.FAB_ADD_TRANSACTION_KEY
import com.ritesh.cashiro.presentation.ui.navigation.NavGraph
import com.ritesh.cashiro.presentation.ui.theme.iosFont
import com.ritesh.cashiro.R
import com.ritesh.cashiro.domain.utils.ScheduleFilterUtils
import com.ritesh.cashiro.presentation.ui.extras.components.extras.CustomTabIndicator
import com.ritesh.cashiro.presentation.ui.features.add_transaction.TabItems
import com.ritesh.cashiro.presentation.ui.features.subscriptions.FilterButton
import com.ritesh.cashiro.presentation.ui.features.subscriptions.SummaryItem

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalSharedTransitionApi::class
)
@Composable
fun SharedTransitionScope.ScheduleScreen(
    scheduleViewModel: ScheduleViewModel,
    accountUiState: AccountScreenState,
    onAccountEvent: (AccountScreenEvent) -> Unit,
    onAddTransactionEvent: (AddTransactionEvent) -> Unit,
    onCategoryEvent: (CategoryScreenEvent) -> Unit,
    onSubCategoryEvent: (SubCategoryEvent) -> Unit,
    screenTitle: String,
    navController: NavController,
    onBackClicked: () -> Unit,
    previousScreenTitle: String,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    val scheduleState by scheduleViewModel.state.collectAsState()
    val accounts = accountUiState.accounts

    var selectedTransactions by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var isInSelectionMode by remember { mutableStateOf(false) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

    // Filter state
    var filterState by remember { mutableStateOf(ScheduleFilterState()) }

    val scheduleTabItems = listOf(
        TabItems(type = "All"),
        TabItems(type = "Upcoming"),
        TabItems(type = "Overdue"),
    )

    // Scroll behavior and pager state
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val pagerState = rememberPagerState { scheduleTabItems.size }
    val coroutineScope = rememberCoroutineScope()
    val scrollStates = remember {
        List(scheduleTabItems.size) { LazyListState() }
    }
    val overscrollEffects = remember(coroutineScope) {
        List(scheduleTabItems.size) { VerticalStretchedOverscroll(coroutineScope) }
    }

    // State to prevent animation conflicts
    var isAnimating by remember { mutableStateOf(false) }

    // Handle user swipe gestures (pager → ViewModel)
    LaunchedEffect(pagerState.currentPage) {
        // Reset overscroll effect for the new tab
        if (pagerState.currentPage < overscrollEffects.size) {
            overscrollEffects[pagerState.currentPage].reset()
        }

        // Only update ViewModel if this change is from user gesture (not programmatic)
        if (scheduleState.selectedTabIndex != pagerState.currentPage && !isAnimating) {
            scheduleViewModel.onEvent(ScheduleEvent.SelectTab(pagerState.currentPage))
        }
    }

    // Handle tab clicks (ViewModel → pager)
    LaunchedEffect(scheduleState.selectedTabIndex) {
        // Only animate if the pager page is different and we're not already animating
        if (scheduleState.selectedTabIndex != pagerState.currentPage && !isAnimating) {
            isAnimating = true
            try {
                pagerState.scrollToPage(scheduleState.selectedTabIndex)
            } catch (e: Exception) {
                Log.w("ScheduleScreen", "Animation interrupted: ${e.message}")
            } finally {
                isAnimating = false
            }
        }
    }

    // Apply filters to get the appropriate transactions for each tab
    val filteredTransactions = remember(scheduleState, filterState) {
        when (scheduleState.selectedTabIndex) {
            0 -> ScheduleFilterUtils.applyFilters(
                scheduleState.allTransactions,
                filterState,
                accounts
            )
            1 -> ScheduleFilterUtils.applyFilters(
                scheduleState.upcomingTransactions,
                filterState,
                accounts
            )
            2 -> ScheduleFilterUtils.applyFilters(
                scheduleState.overdueTransactions,
                filterState,
                accounts
            )
            else -> emptyList<TransactionEntity>()
        }
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
        showDeleteConfirmationDialog = true
    }

    val selectAllTransactions = {
        if (!isInSelectionMode) {
            isInSelectionMode = true
        }
        selectedTransactions = filteredTransactions.map { it.id }.toSet()
    }

    // Modified BackHandler to handle selection mode
    BackHandler(enabled = isInSelectionMode) {
        clearSelections()
    }

    LaunchedEffect(key1 = Unit) {
        onAccountEvent(AccountScreenEvent.FetchAllAccounts)
        scheduleViewModel.onEvent(ScheduleEvent.LoadScheduledTransactions)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CustomTitleTopAppBar(
                scrollBehaviorLarge = scrollBehavior,
                scrollBehaviorSmall = TopAppBarDefaults.pinnedScrollBehavior(),
                title = screenTitle,
                previousScreenTitle = previousScreenTitle,
                onBackClick = onBackClicked,
                hasBackButton = true,
                isInSelectionMode = isInSelectionMode,
                selectedCount = selectedTransactions.size,
                onClearSelection = clearSelections,
                onDeleteSelected = deleteSelectedTransactions,
                onSelectAll = selectAllTransactions,
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = !isInSelectionMode,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    FloatingActionButton(
                        onClick = {
                            navController.currentBackStackEntry?.savedStateHandle?.set("isUpdateTransaction", false)
                            navController.currentBackStackEntry?.savedStateHandle?.set("defaultTab", "Expense")
                            navController.navigate(NavGraph.ADD_TRANSACTION) { launchSingleTop = true }
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.sharedBounds(
                            resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds(
                                contentScale = ContentScale.Inside
                            ),
                            sharedContentState = rememberSharedContentState(
                                key = FAB_ADD_TRANSACTION_KEY
                            ),
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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.Top
        ) {
            // Show loading indicator if loading
            if (scheduleState.isLoading && scheduleState.allTransactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (scheduleState.error != null) {
                // Show error if there's an error
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = scheduleState.error!!,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(
                            onClick = { scheduleViewModel.onEvent(ScheduleEvent.RefreshScheduledTransactions) },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            } else {
                // Tab Row
                TabRow(
                    selectedTabIndex = scheduleState.selectedTabIndex,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp)
                        .shadow(elevation = 10.dp, shape = RoundedCornerShape(15.dp))
                        .background(
                            color = MaterialTheme.colorScheme.surfaceBright,
                            shape = RoundedCornerShape(15.dp)
                        )
                        .zIndex(5f),
                    containerColor = MaterialTheme.colorScheme.surfaceBright,
                    divider = {},
                    indicator = { tabPositions ->
                        CustomTabIndicator(tabPositions, pagerState)
                    }
                ) {
                    scheduleTabItems.forEachIndexed { index, item ->
                        Tab(
                            selected = index == scheduleState.selectedTabIndex,
                            onClick = {
                                scheduleViewModel.onEvent(ScheduleEvent.SelectTab(index))
                            },
                            text = {
                                Text(
                                    text = item.type,
                                    fontFamily = iosFont,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    color = if (scheduleState.selectedTabIndex == index)
                                        MaterialTheme.colorScheme.inverseSurface
                                    else
                                        MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.5f)
                                )
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(15.dp))
                                .weight(1f)
                                .zIndex(2f),
                        )
                    }
                }

                // Filter buttons
                ScheduleFilterButtons(
                    filterState = filterState,
                    onFilterChange = { newFilterState ->
                        filterState = newFilterState
                    },
                    transactions = filteredTransactions,
                    accounts = accounts,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                )

                // Pager content
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .zIndex(0f)
                        .padding(horizontal = 20.dp),
                    userScrollEnabled = true
                ) { index ->
                    val tabScrollState = scrollStates[index]
                    val tabOverscrollEffect = overscrollEffects[index]

                    SchedulePagerContent(
                        transactions = filteredTransactions,
                        accounts = accounts,
                        selectedTabIndex = index,
                        selectedTransactionIds = selectedTransactions,
                        onTransactionClick = toggleTransactionSelection,
                        onTransactionLongPress = onTransactionLongPress,
                        isInSelectionMode = isInSelectionMode,
                        tabScrollState = tabScrollState,
                        tabOverscrollEffect = tabOverscrollEffect,
                        navController = navController,
                        animatedVisibilityScope = animatedVisibilityScope,
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

    if (showDeleteConfirmationDialog) {
        TransactionDeletionConfirmationDialog(
            showDialog = showDeleteConfirmationDialog,
            transactionCount = selectedTransactions.size,
            onConfirm = {
                scheduleViewModel.onEvent(
                    ScheduleEvent.DeleteScheduledTransactions(selectedTransactions.toList())
                )
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
fun SharedTransitionScope.SchedulePagerContent(
    transactions: List<TransactionEntity>,
    accounts: List<AccountEntity>,
    selectedTabIndex: Int,
    selectedTransactionIds: Set<Int>,
    onTransactionClick: (TransactionEntity) -> Unit,
    onTransactionLongPress: (TransactionEntity) -> Unit,
    isInSelectionMode: Boolean,
    tabScrollState: LazyListState,
    tabOverscrollEffect: VerticalStretchedOverscroll,
    navController: NavController,
    animatedVisibilityScope: AnimatedVisibilityScope,
    accountUiState: AccountScreenState,
    onAccountEvent: (AccountScreenEvent) -> Unit,
    onAddTransactionEvent: (AddTransactionEvent) -> Unit,
    onCategoryEvent: (CategoryScreenEvent) -> Unit,
    onSubCategoryEvent: (SubCategoryEvent) -> Unit,
) {
    val mainCurrencySymbol = accounts.find { it.isMainAccount }?.currencyCode ?: "usd"
    val mainCurrency = CurrencySymbols.getSymbol(mainCurrencySymbol)
    val conversionRates = accountUiState.mainCurrencyConversionRates

    if (transactions.isEmpty()) {
        EmptyScheduleContent(selectedTabIndex)
        return
    }

    // Calculate totals based on current tab
    val totalAmount = CurrencyUtils.calculateTotalExpense(
        transactions,
        mainCurrencySymbol,
        conversionRates
    )

    val paidAmount = CurrencyUtils.calculateTotalExpense(
        transactions.filter { it.isPaid },
        mainCurrencySymbol,
        conversionRates
    )

    val unpaidAmount = totalAmount - paidAmount

    LazyColumn(
        state = tabScrollState,
        userScrollEnabled = true,
        modifier = Modifier
            .fillMaxSize()
            .overscroll(tabOverscrollEffect)
            .clip(RoundedCornerShape(16.dp)),
        verticalArrangement = Arrangement.spacedBy(15.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
//        item(key = "schedule_summary_${selectedTabIndex}") {
//            ScheduleSummaryCard(
//                totalAmount = totalAmount,
//                paidAmount = paidAmount,
//                unpaidAmount = unpaidAmount,
//                currency = mainCurrency,
//                totalCount = transactions.size,
//                tabType = when (selectedTabIndex) {
//                    0 -> "All Scheduled"
//                    1 -> "Upcoming"
//                    2 -> "Overdue"
//                    else -> "Scheduled"
//                },
//                themeColors = MaterialTheme.colorScheme
//            )
//        }

        itemsIndexed(
            items = transactions,
            key = { _, transaction -> transaction.id }
        ) { index, transaction ->
            val isItemSelected = selectedTransactionIds.contains(transaction.id)
            val account = accounts.find { it.id == transaction.accountId }

            TransactionCardLayout(
                transaction = transaction,
                account = account,
                themeColors = MaterialTheme.colorScheme,
                isSelected = isItemSelected,
                isInSelectionMode = isInSelectionMode,
                onSelectionClick = { onTransactionClick(transaction) },
                onLongClick = { onTransactionLongPress(transaction) },
                navController = navController,
                animatedVisibilityScope = animatedVisibilityScope,
                accountUiState = accountUiState,
                onCategoryEvent = onCategoryEvent,
                onSubCategoryEvent = onSubCategoryEvent,
                onAddTransactionEvent = onAddTransactionEvent,
            )
        }

        item(key = "scrolling_end_space_${selectedTabIndex}") {
            Spacer(modifier = Modifier.height(150.dp))
        }
    }
}

@Composable
fun ScheduleSummaryCard(
    totalAmount: Double,
    paidAmount: Double,
    unpaidAmount: Double,
    currency: String,
    totalCount: Int,
    tabType: String,
    themeColors: ColorScheme
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = themeColors.surfaceBright
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "$tabType Summary",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = iosFont,
                color = themeColors.inverseSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem(
                    label = "Total",
                    amount = formatCurrency(totalAmount, currency),
                    color = themeColors.inverseSurface
                )
                SummaryItem(
                    label = "Paid",
                    amount = formatCurrency(paidAmount, currency),
                    color = Color(0xFF7ACE96)
                )
                SummaryItem(
                    label = "Unpaid",
                    amount = formatCurrency(unpaidAmount, currency),
                    color = themeColors.onError
                )
            }

            Text(
                text = "$totalCount ${tabType.lowercase()} transactions",
                fontSize = 12.sp,
                fontFamily = iosFont,
                color = themeColors.inverseSurface.copy(0.6f),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ScheduleFilterButtons(
    filterState: ScheduleFilterState,
    onFilterChange: (ScheduleFilterState) -> Unit,
    transactions: List<TransactionEntity>,
    accounts: List<AccountEntity>,
    modifier: Modifier = Modifier
) {
    val mainCurrencySymbol = accounts.find { it.isMainAccount }?.currencyCode ?: "usd"
    val mainCurrency = CurrencySymbols.getSymbol(mainCurrencySymbol)

    // FIXED: Use the corrected calculation method
    val scheduleAmounts = CurrencyUtils.calculateScheduleAmounts(
        transactions, mainCurrencySymbol, emptyMap()
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterButton(
            text = "Monthly",
            amount = scheduleAmounts.monthlyTotal,
            currency = mainCurrency,
            isSelected = filterState.selectedPeriod == SchedulePeriod.MONTHLY,
            onClick = {
                onFilterChange(
                    filterState.copy(
                        selectedPeriod = if (filterState.selectedPeriod == SchedulePeriod.MONTHLY)
                            SchedulePeriod.ALL else SchedulePeriod.MONTHLY
                    )
                )
            },
            modifier = Modifier.weight(1f)
        )

        FilterButton(
            text = "Yearly",
            amount = scheduleAmounts.yearlyTotal,
            currency = mainCurrency,
            isSelected = filterState.selectedPeriod == SchedulePeriod.YEARLY,
            onClick = {
                onFilterChange(
                    filterState.copy(
                        selectedPeriod = if (filterState.selectedPeriod == SchedulePeriod.YEARLY)
                            SchedulePeriod.ALL else SchedulePeriod.YEARLY
                    )
                )
            },
            modifier = Modifier.weight(1f)
        )

        FilterButton(
            text = "Total",
            amount = scheduleAmounts.currentTotal,
            currency = mainCurrency,
            isSelected = filterState.selectedPeriod == SchedulePeriod.ALL,
            onClick = {
                onFilterChange(filterState.copy(selectedPeriod = SchedulePeriod.ALL))
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun EmptyScheduleContent(selectedTabIndex: Int) {
    val message = when (selectedTabIndex) {
        0 -> "No scheduled transactions available"
        1 -> "No upcoming transactions"
        2 -> "No overdue transactions"
        else -> "No transactions available"
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.empty_category_list),
                contentDescription = "Empty Schedule Icon",
                modifier = Modifier.size(300.dp)
            )
            Text(
                text = message,
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