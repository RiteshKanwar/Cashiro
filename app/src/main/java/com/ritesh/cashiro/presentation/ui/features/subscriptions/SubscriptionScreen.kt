package com.ritesh.cashiro.presentation.ui.features.subscriptions

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalSharedTransitionApi::class
)
@Composable
fun SharedTransitionScope.SubscriptionScreen(
    subscriptionViewModel: SubscriptionViewModel,
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
    val subscriptionState by subscriptionViewModel.state.collectAsState()
    val transactions = subscriptionState.subscriptions
    val accounts = accountUiState.accounts

    var selectedTransactions by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var isInSelectionMode by remember { mutableStateOf(false) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

    // Filter state
    var filterState by remember { mutableStateOf(SubscriptionFilterState()) }

    val coroutineScope = rememberCoroutineScope()
    val scrollOverScrollState = rememberLazyListState()
    val overscrollEffect = remember(coroutineScope) { VerticalStretchedOverscroll(coroutineScope) }

    // Scaffold State for Scroll Behavior
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scrollBehaviorSmall = TopAppBarDefaults.pinnedScrollBehavior()

    // Apply filters to transactions
    val filteredTransactions = remember(transactions, filterState) {
        SubscriptionFilterUtils.applyFilters(
            subscriptions = transactions,
            filterState = filterState,
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
        subscriptionViewModel.onEvent(SubscriptionEvent.LoadSubscriptions)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CustomTitleTopAppBar(
                scrollBehaviorLarge = scrollBehavior,
                scrollBehaviorSmall = scrollBehaviorSmall,
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
        // FAB Button
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
                            contentDescription = "Add Subscription"
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Filter buttons
            SubscriptionFilterButtons(
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

            if (filteredTransactions.isEmpty()) {
                EmptySubscriptions()
            } else {
                SubscriptionsDisplayList(
                    transactions = filteredTransactions,
                    accounts = accounts,
                    themeColors = MaterialTheme.colorScheme,
                    selectedTransactionIds = selectedTransactions,
                    onTransactionClick = toggleTransactionSelection,
                    onTransactionLongPress = onTransactionLongPress,
                    isInSelectionMode = isInSelectionMode,
                    scrollOverScrollState = scrollOverScrollState,
                    overscrollEffect = overscrollEffect,
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

    if (showDeleteConfirmationDialog) {
        TransactionDeletionConfirmationDialog(
            showDialog = showDeleteConfirmationDialog,
            transactionCount = selectedTransactions.size,
            onConfirm = {
                subscriptionViewModel.onEvent(
                    SubscriptionEvent.DeleteSubscriptions(selectedTransactions.toList())
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
fun SharedTransitionScope.SubscriptionsDisplayList(
    modifier: Modifier = Modifier,
    transactions: List<TransactionEntity>,
    accounts: List<AccountEntity>,
    accountUiState: AccountScreenState,
    onAccountEvent: (AccountScreenEvent) -> Unit,
    onAddTransactionEvent: (AddTransactionEvent) -> Unit,
    onCategoryEvent: (CategoryScreenEvent) -> Unit,
    onSubCategoryEvent: (SubCategoryEvent) -> Unit,
    themeColors: ColorScheme,
    selectedTransactionIds: Set<Int> = emptySet(),
    onTransactionClick: (TransactionEntity) -> Unit = {},
    onTransactionLongPress: (TransactionEntity) -> Unit = {},
    isInSelectionMode: Boolean = false,
    scrollOverScrollState: LazyListState,
    overscrollEffect: VerticalStretchedOverscroll,
    navController: NavController,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    val mainCurrencySymbol = accounts.find { it.isMainAccount }?.currencyCode ?: "usd"
    val mainCurrency = CurrencySymbols.getSymbol(mainCurrencySymbol)
    val conversionRates = accountUiState.mainCurrencyConversionRates

    if (transactions.isEmpty()) {
        EmptySubscriptions(modifier)
        return
    }

    // Calculate totals
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
        state = scrollOverScrollState,
        userScrollEnabled = true,
        modifier = modifier
            .fillMaxWidth()
            .overscroll(overscrollEffect)
            .clip(RoundedCornerShape(16.dp)),
        verticalArrangement = Arrangement.spacedBy(15.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
//        item(key = "subscription_summary") {
//            SubscriptionSummaryCard(
//                totalAmount = totalAmount,
//                paidAmount = paidAmount,
//                unpaidAmount = unpaidAmount,
//                currency = mainCurrency,
//                totalCount = transactions.size,
//                themeColors = themeColors
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
                themeColors = themeColors,
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

        item(key = "scrolling_end_space") {
            Spacer(modifier = Modifier.height(150.dp))
        }
    }
}

@Composable
fun SubscriptionSummaryCard(
    totalAmount: Double,
    paidAmount: Double,
    unpaidAmount: Double,
    currency: String,
    totalCount: Int,
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
                text = "Subscription Summary",
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
                text = "$totalCount active subscriptions",
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
fun SummaryItem(
    label: String,
    amount: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = amount,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = iosFont,
            color = color
        )
        Text(
            text = label,
            fontSize = 12.sp,
            fontFamily = iosFont,
            color = color.copy(0.7f)
        )
    }
}

@Composable
fun SubscriptionFilterButtons(
    filterState: SubscriptionFilterState,
    onFilterChange: (SubscriptionFilterState) -> Unit,
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
            isSelected = filterState.selectedPeriod == SubscriptionPeriod.MONTHLY,
            onClick = {
                onFilterChange(
                    filterState.copy(
                        selectedPeriod = if (filterState.selectedPeriod == SubscriptionPeriod.MONTHLY)
                            SubscriptionPeriod.ALL else SubscriptionPeriod.MONTHLY
                    )
                )
            },
            modifier = Modifier.weight(1f)
        )

        FilterButton(
            text = "Yearly",
            amount = scheduleAmounts.yearlyTotal,
            currency = mainCurrency,
            isSelected = filterState.selectedPeriod == SubscriptionPeriod.YEARLY,
            onClick = {
                onFilterChange(
                    filterState.copy(
                        selectedPeriod = if (filterState.selectedPeriod == SubscriptionPeriod.YEARLY)
                            SubscriptionPeriod.ALL else SubscriptionPeriod.YEARLY
                    )
                )
            },
            modifier = Modifier.weight(1f)
        )

        FilterButton(
            text = "Total",
            amount = scheduleAmounts.currentTotal,
            currency = mainCurrency,
            isSelected = filterState.selectedPeriod == SubscriptionPeriod.ALL,
            onClick = {
                onFilterChange(filterState.copy(selectedPeriod = SubscriptionPeriod.ALL))
            },
            modifier = Modifier.weight(1f)
        )
    }
}


//@Composable
//fun SubscriptionFilterButtons(
//    filterState: SubscriptionFilterState,
//    onFilterChange: (SubscriptionFilterState) -> Unit,
//    transactions: List<TransactionEntity>,
//    accounts: List<AccountEntity>,
//    modifier: Modifier = Modifier
//) {
//    val mainCurrencySymbol = accounts.find { it.isMainAccount }?.currencyCode ?: "usd"
//    val mainCurrency = CurrencySymbols.getSymbol(mainCurrencySymbol)
//
//    Row(
//        modifier = modifier,
//        horizontalArrangement = Arrangement.spacedBy(8.dp)
//    ) {
//        FilterButton(
//            text = "Monthly",
//            amount = CurrencyUtils.calculateMonthlyTotal(transactions, mainCurrencySymbol, emptyMap()),
//            currency = mainCurrency,
//            isSelected = filterState.selectedPeriod == SubscriptionPeriod.MONTHLY,
//            onClick = {
//                onFilterChange(
//                    filterState.copy(
//                        selectedPeriod = if (filterState.selectedPeriod == SubscriptionPeriod.MONTHLY)
//                            SubscriptionPeriod.ALL else SubscriptionPeriod.MONTHLY
//                    )
//                )
//            },
//            modifier = Modifier.weight(1f)
//        )
//
//        FilterButton(
//            text = "Yearly",
//            amount = CurrencyUtils.calculateYearlyTotal(transactions, mainCurrencySymbol, emptyMap()),
//            currency = mainCurrency,
//            isSelected = filterState.selectedPeriod == SubscriptionPeriod.YEARLY,
//            onClick = {
//                onFilterChange(
//                    filterState.copy(
//                        selectedPeriod = if (filterState.selectedPeriod == SubscriptionPeriod.YEARLY)
//                            SubscriptionPeriod.ALL else SubscriptionPeriod.YEARLY
//                    )
//                )
//            },
//            modifier = Modifier.weight(1f)
//        )
//
//        FilterButton(
//            text = "Total",
//            amount = CurrencyUtils.calculateTotalExpense(transactions, mainCurrencySymbol, emptyMap()),
//            currency = mainCurrency,
//            isSelected = filterState.selectedPeriod == SubscriptionPeriod.ALL,
//            onClick = {
//                onFilterChange(filterState.copy(selectedPeriod = SubscriptionPeriod.ALL))
//            },
//            modifier = Modifier.weight(1f)
//        )
//    }
//}

@Composable
fun FilterButton(
    text: String,
    amount: Double,
    currency: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeColors = MaterialTheme.colorScheme

    Card(
        modifier = modifier
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) themeColors.primary else themeColors.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = text,
                fontSize = 12.sp,
                fontFamily = iosFont,
                color = if (isSelected) Color.White else themeColors.inverseSurface,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = formatCurrency(amount, currency),
                fontSize = 14.sp,
                fontFamily = iosFont,
                color = if (isSelected) Color.White else themeColors.inverseSurface,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

//@Composable
//fun FilterButton(
//    text: String,
//    amount: Double,
//    currency: String,
//    isSelected: Boolean,
//    onClick: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    val themeColors = MaterialTheme.colorScheme
//
//    Card(
//        modifier = modifier
//            .clickable { onClick() },
//        colors = CardDefaults.cardColors(
//            containerColor = if (isSelected) themeColors.primary else themeColors.surface
//        ),
//        shape = RoundedCornerShape(12.dp),
//        elevation = CardDefaults.cardElevation(
//            defaultElevation = if (isSelected) 8.dp else 2.dp
//        )
//    ) {
//        Column(
//            modifier = Modifier.padding(12.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Text(
//                text = text,
//                fontSize = 12.sp,
//                fontFamily = iosFont,
//                color = if (isSelected) Color.White else themeColors.inverseSurface,
//                fontWeight = FontWeight.Medium
//            )
//            Text(
//                text = formatCurrency(amount, currency),
//                fontSize = 14.sp,
//                fontFamily = iosFont,
//                color = if (isSelected) Color.White else themeColors.inverseSurface,
//                fontWeight = FontWeight.SemiBold
//            )
//        }
//    }
//}

@Composable
fun EmptySubscriptions(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.empty_category_list),
                contentDescription = "Empty Subscriptions Icon",
                modifier = Modifier.size(300.dp)
            )
            Text(
                text = "No Subscriptions available",
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