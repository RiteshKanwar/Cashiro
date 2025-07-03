package com.ritesh.cashiro.presentation.ui.features.add_transaction

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults.DragHandle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.ritesh.cashiro.data.local.entity.AccountEntity
import com.ritesh.cashiro.data.local.entity.CategoryEntity
import com.ritesh.cashiro.data.local.entity.Recurrence
import com.ritesh.cashiro.data.local.entity.TransactionEntity
import com.ritesh.cashiro.domain.repository.AccountTransactionStats
import com.ritesh.cashiro.domain.repository.TransactionStats
import com.ritesh.cashiro.presentation.ui.features.currency.CurrencyViewModel
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenViewModel
import com.ritesh.cashiro.presentation.ui.features.categories.CategoryScreenViewModel
import com.ritesh.cashiro.presentation.effects.VerticalStretchedOverscroll
import com.ritesh.cashiro.presentation.ui.extras.components.extras.CustomTabIndicator
import com.ritesh.cashiro.presentation.ui.extras.components.extras.CustomTitleTopAppBar
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenEvent
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenState
import com.ritesh.cashiro.presentation.ui.features.categories.CategoryScreenEvent
import com.ritesh.cashiro.presentation.ui.features.categories.CategoryScreenState
import com.ritesh.cashiro.presentation.ui.features.categories.SubCategoryEvent
import com.ritesh.cashiro.presentation.ui.features.categories.SubCategoryState
import com.ritesh.cashiro.presentation.ui.features.categories.SubCategoryViewModel
import com.ritesh.cashiro.presentation.ui.navigation.NavGraph
import com.ritesh.cashiro.presentation.ui.theme.Latte_Blue
import com.ritesh.cashiro.presentation.ui.theme.Latte_Flamingo
import com.ritesh.cashiro.presentation.ui.theme.Latte_Lavender
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Blue
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Green
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Red
import com.ritesh.cashiro.presentation.ui.theme.iosFont
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlin.Boolean
import kotlin.Char
import kotlin.Int

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalFoundationApi::class, ExperimentalSharedTransitionApi::class
)
@Composable
fun SharedTransitionScope.AddTransactionScreen(
    modifier: Modifier = Modifier,
    onBackClicked: () -> Unit,
    screenTitle: String,
    previousScreenTitle: String,
    isUpdateTransaction: Boolean = false,
    transactionId: Int = 0,
    sharedKey: String,
    animatedVisibilityScope: AnimatedVisibilityScope,
    defaultTab: String? = null,
    navController: NavController? = null,
    transactionUiState: AddTransactionScreenState,
    transactionUiEvent: AddScreenEvent? = null,
    getTransactionById: (Int, (TransactionEntity?) -> Unit) -> Unit,
    onAddTransactionEvent: (AddTransactionEvent) -> Unit,
    accountUiState: AccountScreenState,
    onAccountEvent: (AccountScreenEvent) -> Unit,
    categoryUiState: CategoryScreenState,
    onCategoryEvent: (CategoryScreenEvent) -> Unit,
    subCategoryUiState: SubCategoryState,
    onSubCategoryEvent: (SubCategoryEvent) -> Unit,
    getTransactionStatsForAccount: (Int, (AccountTransactionStats?) -> Unit) -> Unit,
    getTransactionStatsForCategory: (Int, (TransactionStats) -> Unit) -> Unit,
    getTransactionStatsForSubCategory: (Int, (TransactionStats) -> Unit) -> Unit,
    updateAccountCurrency: (Int, String) -> Unit,
    setAccountAsMain: (AccountEntity) -> Unit,
    setSelectedCategoryId: (Int?) -> Unit,
    clearSelection: () -> Unit,
    currencyUiState: CurrencyViewModel.CurrencyUiState,
    selectCurrency: (String) -> Unit,
){
    var transaction by remember { mutableStateOf<TransactionEntity?>(null) }

    // Check for both tile and widget navigation
    val openedFromTile = navController?.previousBackStackEntry?.savedStateHandle?.get<Boolean>("openedFromTile") ?: false
    val openedFromWidget = navController?.previousBackStackEntry?.savedStateHandle?.get<Boolean>("openedFromWidget") ?: false
    val openedFromExternal = openedFromTile || openedFromWidget

    // Load transaction data if in update mode
    LaunchedEffect(transactionId) {
        if (isUpdateTransaction && transactionId > 0) {
            getTransactionById(transactionId) { retrievedTransaction ->
                transaction = retrievedTransaction
                retrievedTransaction?.let { trans ->
                    if (trans.subCategoryId != null && trans.subCategoryId != 0) {
                        // Load subcategories for the parent category
                        onSubCategoryEvent(SubCategoryEvent.FetchSubCategoriesForCategory(trans.categoryId))
                    }
                }
            }
        } else {
            onAddTransactionEvent(AddTransactionEvent.ClearTransactionFields)
        }
    }

    // For backward compatibility, we'll still collect individual flows for now
    // These should be replaced with direct state access over time
    val transactionTitle = transactionUiState.transactionTitle
    val transactionAmount = transactionUiState.transactionAmount
    val transactionDate = transactionUiState.transactionDate
    val transactionCategoryId = transactionUiState.transactionCategoryId
    val transactionSubCategoryId = transactionUiState.transactionSubCategoryId
    val transactionAccountId = transactionUiState.transactionAccountId
    val transactionNote = transactionUiState.transactionNote
    val transactionMode = transactionUiState.transactionMode
    val transactionType = transactionUiState.transactionType

    val categories = categoryUiState.categories
    val subCategories = subCategoryUiState.subCategories
    val accounts = accountUiState.accounts

    // UI state variables
    val isTransactionTypeMenuOpened = transactionUiState.isTransactionTypeMenuOpened
    val isTransactionTypeInfoSheetOpen = transactionUiState.isTransactionTypeInfoSheetOpen
    val isRecurrenceMenuOpen = transactionUiState.isRecurrenceMenuOpen
    val isRecurrenceBottomSheetOpen = transactionUiState.isRecurrenceBottomSheetOpen
    val isTransactionAmountSheetOpen = transactionUiState.isTransactionAmountSheetOpen
    val isCustomInputBottomSheetOpen = transactionUiState.isCustomInputBottomSheetOpen
    val isEndDateSelected = transactionUiState.isEndDateSelected
    val openEndDatePicker = transactionUiState.openEndDatePicker

    val intervalSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val frequencySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // For Color Theme
    val themeColors = MaterialTheme.colorScheme

    // For Date Picker
    val transactionDateMillis = transaction?.date ?: transactionDate
    val initialDateValue = if (isUpdateTransaction && transactionDateMillis != 0L) {
        transactionDateMillis
    } else {
        System.currentTimeMillis()
    }

    val initialEndDate = remember(transaction?.recurrence?.endRecurrenceDate, transactionUiState.recurrenceEndDate) {
        when {
            // For update transactions, use the transaction's end date
            isUpdateTransaction && transaction?.recurrence?.endRecurrenceDate != null ->
                transaction?.recurrence?.endRecurrenceDate
            // For new transactions or when state has end date, use state end date
            transactionUiState.recurrenceEndDate != null ->
                transactionUiState.recurrenceEndDate
            // Default to current time
            else -> System.currentTimeMillis()
        }
    }
    Log.d("DatePicker", "Calculated initial date: $initialDateValue")
    var datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateValue)
    var endDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialEndDate
    )
    // CRITICAL: Synchronize picker state when transaction data loads
    LaunchedEffect(transaction?.recurrence?.endRecurrenceDate) {
        if (isUpdateTransaction && transaction?.recurrence?.endRecurrenceDate != null) {
            val transactionEndDate = transaction?.recurrence?.endRecurrenceDate
            if (endDatePickerState.selectedDateMillis != transactionEndDate) {
                // Force update the picker state to show the correct date
                endDatePickerState.selectedDateMillis = transactionEndDate
            }
        }
    }
    var openDatePickerDialog = rememberSaveable { mutableStateOf(false) }

    val tabItems = if (isUpdateTransaction && transaction != null) {
        when (transaction!!.mode) {
            "Transfer" -> listOf(TabItems("Transfer"))
            "Expense", "Income" -> listOf(TabItems("Expense"), TabItems("Income"))
            else -> listOf(TabItems("Expense"), TabItems("Income"), TabItems("Transfer"))
        }
    } else {
        listOf(TabItems("Expense"), TabItems("Income"), TabItems("Transfer"))
    }

//    var selectedTabIndex by remember {
//        mutableIntStateOf(
//            when (defaultTab) {
//                "Transfer" -> tabItems.indexOfFirst { it.type == "Transfer" }.takeIf { it >= 0 } ?: 0
//                "Income" -> tabItems.indexOfFirst { it.type == "Income" }.takeIf { it >= 0 } ?: 0
//                "Expense" -> tabItems.indexOfFirst { it.type == "Expense" }.takeIf { it >= 0 } ?: 0
//                else -> transactionUiState.selectedTabIndex
//            }
//        )
//    }
    var selectedTabIndex by remember {
        mutableIntStateOf(
            when (defaultTab) {
                "Transfer" -> {
                    val transferIndex = tabItems.indexOfFirst { it.type == "Transfer" }
                    Log.d("AddTransactionScreen", "Transfer tab requested - found at index: $transferIndex")
                    if (transferIndex >= 0) transferIndex else 0
                }
                "Income" -> {
                    val incomeIndex = tabItems.indexOfFirst { it.type == "Income" }
                    Log.d("AddTransactionScreen", "Income tab requested - found at index: $incomeIndex")
                    if (incomeIndex >= 0) incomeIndex else 0
                }
                "Expense" -> {
                    val expenseIndex = tabItems.indexOfFirst { it.type == "Expense" }
                    Log.d("AddTransactionScreen", "Expense tab requested - found at index: $expenseIndex")
                    if (expenseIndex >= 0) expenseIndex else 0
                }
                else -> {
                    Log.d("AddTransactionScreen", "No specific tab requested, using default: 0")
                    0
                }
            }
        )
    }

    // Enhanced LaunchedEffect for widget navigation
    LaunchedEffect(defaultTab, tabItems) {
        // Ensure proper tab selection when coming from widgets
        if (defaultTab != null && tabItems.isNotEmpty()) {
            val targetIndex = when (defaultTab) {
                "Transfer" -> tabItems.indexOfFirst { it.type == "Transfer" }
                "Income" -> tabItems.indexOfFirst { it.type == "Income" }
                "Expense" -> tabItems.indexOfFirst { it.type == "Expense" }
                else -> -1
            }

            if (targetIndex >= 0 && targetIndex != selectedTabIndex) {
                Log.d("AddTransactionScreen", "Switching to tab: $defaultTab (index: $targetIndex)")
                selectedTabIndex = targetIndex

                // Also update the transaction mode
                onAddTransactionEvent(AddTransactionEvent.UpdateMode(tabItems[targetIndex].type))
                onAddTransactionEvent(AddTransactionEvent.SelectTab(targetIndex))
            }
        }
    }


    val transactionTypeListItemsColor = listOf(
        Latte_Blue,
        Latte_Flamingo,
        Latte_Lavender,
        Macchiato_Green,
        Macchiato_Blue,
        Macchiato_Red
    )

//    var selectedTabIndex by remember {
//        mutableIntStateOf(screenState.selectedTabIndex)
//    }
    val pagerState = rememberPagerState { tabItems.size }

    val indicator = @Composable { tabPositions: List<TabPosition> ->
        CustomTabIndicator(tabPositions, pagerState)
    }

    val specialKeys = setOf('+', '-', '*', '/', '(', ')', '%', '×', '÷')

    // Get half of the screen height
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val usedScreenHeight = screenHeight / 1.2f + 10.dp
    val usedScreenWidth = screenWidth - 20.dp
    val transactionTypeSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val coroutineScope = rememberCoroutineScope()
    val scrollStates = remember(tabItems.size) {
        List(tabItems.size) { LazyListState() }
    }
    val overscrollEffects = remember(tabItems.size, coroutineScope) {
        List(tabItems.size) { VerticalStretchedOverscroll(coroutineScope) }
    }

    // Scaffold State for Scroll Behavior
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scrollBehaviorSmall = TopAppBarDefaults.pinnedScrollBehavior()

    LaunchedEffect(transaction) {
        // Set initial tab index based on transaction mode when updating
        if (isUpdateTransaction && transaction != null) {
            val initialPage = when (transaction!!.mode) {
                "Transfer" -> 0  // Only tab in Transfer update mode
                "Income" -> 1  // Income tab in Expense/Income update mode
                "Expense" -> 0
                else -> 0        // Expense tab in Expense/Income update mode
            }
            selectedTabIndex = initialPage
            // Use coroutineScope to ensure this happens in a controlled manner
            coroutineScope.launch {
                pagerState.scrollToPage(initialPage)
            }
        }
    }

    // Monitor and update selectedTabIndex when the page changes
//    LaunchedEffect(pagerState.currentPage) {
//        selectedTabIndex = pagerState.currentPage
//        overscrollEffects[selectedTabIndex].reset()
//        onAddTransactionEvent(AddTransactionEvent.UpdateMode(tabItems[selectedTabIndex].type))
//    }
    LaunchedEffect(pagerState.currentPage) {
        selectedTabIndex = pagerState.currentPage
        overscrollEffects[selectedTabIndex].reset()
        val selectedMode = tabItems[selectedTabIndex].type
        onAddTransactionEvent(AddTransactionEvent.UpdateMode(selectedMode))

        // Additional immediate reset for Transfer mode
        if (selectedMode == "Transfer") {
            onAddTransactionEvent(AddTransactionEvent.UpdateCategoryId(1))
            onAddTransactionEvent(AddTransactionEvent.UpdateSubCategoryId(0))
        }
    }

    // Animate the pager when the tab index changes
    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex != pagerState.currentPage) {
            coroutineScope.launch {
                pagerState.animateScrollToPage(selectedTabIndex)
            }
        }
    }

    val isTransactionDetailsSheetOpen = rememberSaveable { mutableStateOf(!isUpdateTransaction) }
    val categoryMenuOpened = remember { mutableStateOf(false) }
    val accountMenuOpened = remember { mutableStateOf(false) }
    val amountMenuOpened = remember { mutableStateOf(false) }
    val transactionDetailsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val categorySelectionSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val accountsSelectionSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val amountSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)


    // Update the event handler to check for both tile and widget navigation
    LaunchedEffect(transactionUiEvent) {
        when (transactionUiEvent) {
            is AddScreenEvent.TransactionAdded,
            is AddScreenEvent.TransactionUpdated,
            is AddScreenEvent.NavigateBack -> {
                if (openedFromExternal) {
                    // If opened from tile or widget, navigate to HOME instead of just going back
                    navController.navigate(NavGraph.HOME) {
                        popUpTo(NavGraph.HOME) { inclusive = true }
                        launchSingleTop = true
                    }
                } else {
                    // Normal back navigation
                    onBackClicked()
                }
            }
            else -> { /* No action needed */ }
        }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection).sharedBounds(
            sharedContentState = rememberSharedContentState(key = sharedKey),
            animatedVisibilityScope = animatedVisibilityScope
        ),
        topBar = {
            CustomTitleTopAppBar(
                scrollBehaviorLarge = scrollBehavior,
                scrollBehaviorSmall = scrollBehaviorSmall,
                title = if (isUpdateTransaction) "Edit Transaction" else screenTitle,
                previousScreenTitle = if (isUpdateTransaction) "Transactions" else previousScreenTitle,
                onBackClick = {
                    if (openedFromExternal) {
                        // If opened from tile or widget, navigate to HOME
                        navController.navigate(NavGraph.HOME) {
                            popUpTo(NavGraph.HOME) { inclusive = true }
                            launchSingleTop = true
                        }
                    } else {
                        // Normal back navigation
                        onAddTransactionEvent(AddTransactionEvent.NavigateBack)
                    }
                },
                hasBackButton = true
            )
        },
        bottomBar = {
            SubmitTransactionBottomButton(
                transaction = transaction,
                themeColors = themeColors,
                isUpdateTransaction = isUpdateTransaction,
                transactionUiState = transactionUiState,
                onAddTransactionEvent = onAddTransactionEvent,
                openedFromExternal = openedFromExternal
            )
        },
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(themeColors.background)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 50.dp, vertical = 8.dp)
                    .shadow(
                        10.dp,
                        RoundedCornerShape(15.dp),
                        spotColor = themeColors.surfaceBright
                    )
                    .clip(RoundedCornerShape(15.dp))
                    .zIndex(5f),
                containerColor = themeColors.surfaceBright,
                divider = {},
                indicator = indicator
            ) {
                tabItems.forEachIndexed { index, item ->
                    Tab(
                        selected = index == selectedTabIndex,
                        onClick = {
                            selectedTabIndex = index
                            onAddTransactionEvent(AddTransactionEvent.UpdateMode(item.type))
                            onAddTransactionEvent(AddTransactionEvent.SelectTab(index))

                            // Reset category/subcategory when switching to Transfer
                            if (item.type == "Transfer") {
                                onAddTransactionEvent(AddTransactionEvent.UpdateCategoryId(1))
                                onAddTransactionEvent(AddTransactionEvent.UpdateSubCategoryId(0))
                            }
                        },
                        text = {
                            Text(
                                text = item.type,
                                fontFamily = iosFont,
                                color = if (pagerState.currentPage == index) themeColors.inverseSurface
                                else themeColors.inverseSurface.copy(alpha = 0.5f)
                            )
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(15.dp))
                            .zIndex(2f),
                        interactionSource = object : MutableInteractionSource {     // Removes the Ripple Effect
                            override val interactions: Flow<Interaction> = emptyFlow()
                            override suspend fun emit(interaction: Interaction) {}
                            override fun tryEmit(interaction: Interaction): Boolean = true
                        }
                    )
                }
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .zIndex(0f)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.Top
            ) { index ->
                if (index < scrollStates.size && index < overscrollEffects.size && index < tabItems.size) {
                    val tabScrollState = scrollStates[index]
                    val tabOverscrollEffect = overscrollEffects[index]
                    val currentTabType = tabItems[index].type

                    // Determine which UI to show based ONLY on the current tab type
                    when (currentTabType) {
                        "Expense", "Income" -> {
                            ExpenseIncomeTabsUI(
                                accounts = accounts,
                                categories = categories,
                                subCategories = subCategories,
                                transaction = transaction,
                                transactionAccountId = transactionAccountId,
                                transactionCategoryId = transactionCategoryId,
                                transactionSubCategoryId = transactionSubCategoryId,
                                transactionTitle = transactionTitle,
                                transactionNote = transactionNote,
                                transactionType = transactionType,
                                specialKeys = specialKeys,
                                usedScreenWidth = usedScreenWidth,
                                usedScreenHeight = usedScreenHeight,
                                amountSheetState = amountSheetState,
                                isTransactionAmountSheetOpen = isTransactionAmountSheetOpen,
                                isUpdateTransaction = isUpdateTransaction,
                                isEndDateSelected = isEndDateSelected,
                                isRecurrenceBottomSheetOpen = isRecurrenceBottomSheetOpen,
                                isCustomInputBottomSheetOpen = isCustomInputBottomSheetOpen,
                                isTransactionTypeInfoSheetOpen = isTransactionTypeInfoSheetOpen,
                                transactionTypeMenuOpened = isTransactionTypeMenuOpened,
                                openDatePickerDialog = openDatePickerDialog,
                                datePickerState = datePickerState,
                                recurrenceMenuOpened = isRecurrenceMenuOpen,
                                transactionTypeListItemsColor = transactionTypeListItemsColor,
                                transactionTypeSheetState = transactionTypeSheetState,
                                intervalSheetState = intervalSheetState,
                                frequencySheetState = frequencySheetState,
                                openEndDatePicker = openEndDatePicker,
                                endDatePickerState = endDatePickerState,
                                coroutineScope = coroutineScope,
                                tabScrollState = tabScrollState,
                                tabOverscrollEffect = tabOverscrollEffect,
                                themeColors = themeColors,
                                transactionUiState = transactionUiState,
                                onAddTransactionEvent = onAddTransactionEvent,
                                accountUiState = accountUiState,
                                onAccountEvent = onAccountEvent,
                                categoryUiState = categoryUiState,
                                onCategoryEvent = onCategoryEvent,
                                subCategoryUiState = subCategoryUiState,
                                onSubCategoryEvent = onSubCategoryEvent,
                                getTransactionStatsForAccount = getTransactionStatsForAccount,
                                getTransactionStatsForCategory = getTransactionStatsForCategory,
                                getTransactionStatsForSubCategory = getTransactionStatsForSubCategory,
                                updateAccountCurrency = updateAccountCurrency,
                                setAccountAsMain = setAccountAsMain,
                                setSelectedCategoryId = setSelectedCategoryId,
                                clearSelection = clearSelection,
                                currencyUiState = currencyUiState,
                                selectCurrency = selectCurrency
                            )
                        }

                        "Transfer" -> {
                            TransferTabUI(
                                openDatePickerDialog = openDatePickerDialog,
                                datePickerState = datePickerState,
                                transactionTitle = transactionTitle,
                                transactionNote = transactionNote,
                                usedScreenWidth = usedScreenWidth,
                                usedScreenHeight = usedScreenHeight,
                                coroutineScope = coroutineScope,
                                amountSheetState = amountSheetState,
                                specialKeys = specialKeys,
                                transactionAccountId = transactionAccountId,
                                isTransactionAmountSheetOpen = isTransactionAmountSheetOpen,
                                themeColors = themeColors,
                                transactionUiState = transactionUiState,
                                onAddTransactionEvent = onAddTransactionEvent,
                                accountUiState = accountUiState,
                                onAccountEvent = onAccountEvent,
                                getTransactionStatsForAccount = getTransactionStatsForAccount,
                                updateAccountCurrency = updateAccountCurrency,
                                setAccountAsMain = setAccountAsMain,
                                currencyUiState = currencyUiState,
                                selectCurrency = selectCurrency
                            )
                        }

                        else -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Unknown Tab Type: $currentTabType")
                            }
                        }
                    }
                }
                else {
                    // Handle invalid index
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error displaying content for this tab.")
                    }
                }
            }

        }
        if (isTransactionDetailsSheetOpen.value) {
            onStartTransactionDetailsInputSheet(
                accounts = accounts,
                categories = categories,
                transactionTitle = transactionTitle,
                coroutineScope = coroutineScope,
                isSheetOpen = isTransactionDetailsSheetOpen,
                categoryMenuOpened = categoryMenuOpened,
                accountMenuOpened = accountMenuOpened,
                amountMenuOpened = amountMenuOpened,
                transactionDetailsInputSheetState = transactionDetailsSheetState,
                categorySelectionInputSheetState = categorySelectionSheetState,
                accountSelectionInputSheetState = accountsSelectionSheetState,
                openDatePickerDialog = openDatePickerDialog,
                datePickerState = datePickerState,
                transaction = transaction,
                amount = transactionAmount,
                transactionAccountId = transactionAccountId,
                amountInputSheetState = amountSheetState,
                specialKeys = specialKeys,
                isTransactionTypeInfoSheetOpen = isTransactionTypeInfoSheetOpen,
                transactionTypeSheetState = transactionTypeSheetState,
                currentTransactionMode = transactionMode,
                transactionTypeListItemsColor = transactionTypeListItemsColor,
                usedScreenWidth = usedScreenWidth,
                usedScreenHeight = usedScreenHeight,
                themeColors = themeColors,
                recurrenceMenuOpened = isRecurrenceMenuOpen,
                endDatePickerState = endDatePickerState,
                isRecurrenceBottomSheetOpen = isRecurrenceBottomSheetOpen,
                isCustomInputBottomSheetOpen = isCustomInputBottomSheetOpen,
                isEndDateSelected = isEndDateSelected,
                openEndDatePicker = openEndDatePicker,
                frequencySheetState = frequencySheetState,
                intervalSheetState = intervalSheetState,
                transactionUiState = transactionUiState,
                onAddTransactionEvent = onAddTransactionEvent,
                accountUiState = accountUiState,
                onAccountEvent = onAccountEvent,
                categoryUiState = categoryUiState,
                onCategoryEvent = onCategoryEvent,
                subCategoryUiState = subCategoryUiState,
                onSubCategoryEvent = onSubCategoryEvent,
                getTransactionStatsForAccount = getTransactionStatsForAccount,
                getTransactionStatsForCategory = getTransactionStatsForCategory,
                getTransactionStatsForSubCategory = getTransactionStatsForSubCategory,
                updateAccountCurrency = updateAccountCurrency,
                setAccountAsMain = setAccountAsMain,
                setSelectedCategoryId = setSelectedCategoryId,
                clearSelection = clearSelection,
                currencyUiState = currencyUiState,
                selectCurrency = selectCurrency
            )
        }
    }
}

// Updated Submit button that uses the state
@Composable
private fun SubmitTransactionBottomButton(
    onAddTransactionEvent: (AddTransactionEvent) -> Unit,
    transaction: TransactionEntity? = null,
    themeColors: ColorScheme,
    isUpdateTransaction: Boolean,
    transactionUiState: AddTransactionScreenState,
    openedFromExternal: Boolean = false // Add this parameter
) {
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        themeColors.background.copy(0.5f),
                        themeColors.background
                    )
                )
            )
    ) {
        val isAddFormValid = transactionUiState.isValidForAdding()
        val isUpdateFormValid = transactionUiState.isValidForUpdating()

        Button(
            onClick = {
                if (!isUpdateTransaction && isAddFormValid) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onAddTransactionEvent(AddTransactionEvent.AddTransaction)
                } else if (isUpdateTransaction && isUpdateFormValid && transaction != null) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                    // Create new transaction entity with updated values
                    val newTransaction = TransactionEntity(
                        id = transaction.id,
                        title = transactionUiState.transactionTitle,
                        amount = transactionUiState.transactionAmount.toDouble(),
                        date = transactionUiState.transactionDate,
                        accountId = transactionUiState.transactionAccountId,
                        categoryId = transactionUiState.transactionCategoryId,
                        subCategoryId = if (transactionUiState.transactionSubCategoryId != 0)
                            transactionUiState.transactionSubCategoryId else null,
                        mode = transactionUiState.transactionMode,
                        destinationAccountId = if (transactionUiState.transactionMode == "Transfer")
                            transactionUiState.transactionDestinationAccountId else null,
                        time = transactionUiState.transactionTime,
                        recurrence = Recurrence(
                            frequency = transactionUiState.recurrenceFrequency,
                            interval = transactionUiState.recurrenceInterval,
                            endRecurrenceDate = transactionUiState.recurrenceEndDate
                        ),
                        note = transactionUiState.transactionNote,
                        transactionType = transactionUiState.transactionType,
                        isPaid = transactionUiState.isPaid,
                        nextDueDate = transactionUiState.nextDueDate,
                    )

                    onAddTransactionEvent(AddTransactionEvent.UpdateTransaction(transaction, newTransaction))
                }
            },
            modifier = Modifier
                .height(60.dp)
                .fillMaxWidth()
                .padding(bottom = 10.dp, start = 25.dp, end = 25.dp)
                .then(
                    if (!isAddFormValid && !isUpdateFormValid) {
                        Modifier.shadow(
                            10.dp,
                            RoundedCornerShape(16.dp),
                            spotColor = themeColors.surfaceBright,
                            ambientColor = themeColors.surfaceBright
                        )
                    } else {
                        Modifier.shadow(
                            10.dp,
                            RoundedCornerShape(16.dp),
                            spotColor = themeColors.primary,
                            ambientColor = themeColors.primary
                        )
                    }
                ),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (!isAddFormValid && !isUpdateFormValid) themeColors.surfaceBright
                else themeColors.primary,
                contentColor = themeColors.inverseSurface
            )
        ) {
            Text(
                text = if (isUpdateTransaction) "Save ${transactionUiState.transactionMode}"
                else if (openedFromExternal) "Add & Stay"
                else "Add ${transactionUiState.transactionMode}",
                fontFamily = iosFont,
                color = if (!isAddFormValid && !isUpdateFormValid) themeColors.inverseSurface.copy(alpha = 0.5f)
                else Color.White
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun onStartTransactionDetailsInputSheet(
    accounts: List<AccountEntity>,
    categories: List<CategoryEntity>,
    transactionTitle: String,
    currentTabType: String = "Expense",
    amount: String,
    transactionAccountId: Int,
    coroutineScope: CoroutineScope,
    transactionDetailsInputSheetState: SheetState,
    categorySelectionInputSheetState: SheetState,
    accountSelectionInputSheetState: SheetState,
    amountInputSheetState: SheetState,
    isSheetOpen: MutableState<Boolean>,
    categoryMenuOpened: MutableState<Boolean>,
    accountMenuOpened: MutableState<Boolean>,
    amountMenuOpened: MutableState<Boolean>,
    openDatePickerDialog: MutableState<Boolean>,
    datePickerState: DatePickerState,
    isUpdateTransaction: Boolean = false,
    transaction: TransactionEntity? = null,
    specialKeys: Set<Char>,
    isTransactionTypeInfoSheetOpen: Boolean = false,
    transactionTypeSheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    currentTransactionMode: String  = "",
    transactionTypeListItemsColor: List<Color> = listOf(
        Latte_Blue,
        Latte_Flamingo,
        Latte_Lavender,
        Macchiato_Green,
        Macchiato_Blue,
        Macchiato_Red
    ),
    usedScreenWidth: Dp = 0.dp,
    usedScreenHeight: Dp = 0.dp,
    themeColors: ColorScheme = MaterialTheme.colorScheme,
    recurrenceMenuOpened: Boolean = false,
    endDatePickerState: DatePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis()) ,
    isRecurrenceBottomSheetOpen: Boolean = false,
    isCustomInputBottomSheetOpen: Boolean = false,
    isEndDateSelected: Boolean = false,
    openEndDatePicker: Boolean = false,
    frequencySheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    intervalSheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    transactionUiState: AddTransactionScreenState,
    onAddTransactionEvent: (AddTransactionEvent) -> Unit,
    accountUiState: AccountScreenState,
    onAccountEvent: (AccountScreenEvent) -> Unit,
    categoryUiState: CategoryScreenState,
    onCategoryEvent: (CategoryScreenEvent) -> Unit,
    subCategoryUiState: SubCategoryState,
    onSubCategoryEvent: (SubCategoryEvent) -> Unit,
    getTransactionStatsForAccount: (Int, (AccountTransactionStats?) -> Unit) -> Unit,
    getTransactionStatsForCategory: (Int, (TransactionStats) -> Unit) -> Unit,
    getTransactionStatsForSubCategory: (Int, (TransactionStats) -> Unit) -> Unit,
    updateAccountCurrency: (Int, String) -> Unit,
    setAccountAsMain: (AccountEntity) -> Unit,
    setSelectedCategoryId: (Int?) -> Unit,
    clearSelection: () -> Unit,
    currencyUiState: CurrencyViewModel.CurrencyUiState,
    selectCurrency: (String) -> Unit,
){
    val scope = rememberCoroutineScope()
    val accountCurrencyCode = accountUiState.accounts.find { it.id == transactionAccountId }?.currencyCode
    val currencyCode = if (accountCurrencyCode.isNullOrBlank()) {
        accountUiState.accounts.find { it.isMainAccount == true }?.currencyCode
    } else {
        accountCurrencyCode
    }
    var calculatedResultAmount by rememberSaveable { mutableStateOf("0") }
    val keyboardController = LocalSoftwareKeyboardController.current

    val focusRequester = remember { FocusRequester() }

    if (isSheetOpen.value) {
        ModalBottomSheet(
            sheetState = transactionDetailsInputSheetState,
            onDismissRequest = {
                isSheetOpen.value = false
            },
            dragHandle = {
                DragHandle(
                    color = themeColors.inverseSurface.copy(0.3f)
                )
            },
            sheetMaxWidth = usedScreenWidth - 4.dp,
            containerColor = themeColors.background,
            contentColor = themeColors.inverseSurface,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            modifier = Modifier
        ) {
            Box(
                modifier = Modifier
                    .imePadding()
                    .padding(horizontal = 15.dp)
                    .fillMaxWidth()
                    .height(250.dp)

            ) {

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Text(
                            text = "Enter Title",
                            textAlign = TextAlign.Start,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = iosFont,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 10.dp)
                        )
                    }
                    item {
                        DateAndTimeInput(
                            openDatePickerDialog = openDatePickerDialog,
                            datePickerState = datePickerState,
                            isUpdateTransaction = isUpdateTransaction,
                            transaction = transaction,
                            transactionUiState = transactionUiState,
                            onAddTransactionEvent = onAddTransactionEvent
                        )
                    }
                    item{
                        TitleInput(
                            transactionTitle = transactionTitle,
                            showHeader = false,
                            themeColors = themeColors,
                            focusRequester = focusRequester,
                            onComposed = {
                                focusRequester.requestFocus()
                                keyboardController?.show()
                            },
                            onAddTransactionEvent = onAddTransactionEvent
                        )
                    }

                    item {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clickable(
                                    onClick = {
                                        categoryMenuOpened.value = true
                                    },
                                )
                        ) {
                            Text(
                                text = "Select Category",
                                textAlign = TextAlign.Center,
                                fontFamily = iosFont,
                                fontSize = 14.sp,
                                color = Color.White,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = themeColors.primary,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .padding(vertical = 10.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (categoryMenuOpened.value) {
        CustomCategoriesSelectionSheet(
            sheetState = categorySelectionInputSheetState,
            usedScreenWidth = usedScreenWidth,
            usedScreenHeight = usedScreenHeight,
            isUpdateTransaction = isUpdateTransaction,
            themeColors = themeColors,
            categories = categories,
            onClick = {
                coroutineScope
                    .launch {
                        categorySelectionInputSheetState.hide()
                    }
                    .invokeOnCompletion {
                        if (!categorySelectionInputSheetState.isVisible) {
                            categoryMenuOpened.value = false
                            accountMenuOpened.value = true
                        }
                    }
            },
            onDismissRequest = {
                categoryMenuOpened.value = false
                isSheetOpen.value = false
            },
            categoryUiState = categoryUiState,
            onCategoryEvent = onCategoryEvent,
            subCategoryUiState = subCategoryUiState,
            onSubCategoryEvent = onSubCategoryEvent,
            transactionUiState = transactionUiState,
            onAddTransactionEvent = onAddTransactionEvent,
            getTransactionStatsForCategory = getTransactionStatsForCategory,
            getTransactionStatsForSubCategory = getTransactionStatsForSubCategory,
            setSelectedCategoryId = setSelectedCategoryId,
            clearSelection = clearSelection
        )
    }

    if (accountMenuOpened.value) {
        CustomAccountSelectionSheet(
            sheetState = accountSelectionInputSheetState,
            usedScreenWidth = usedScreenWidth,
            usedScreenHeight = usedScreenHeight,
            themeColors = themeColors,
            accounts = accounts,
            onClick = {
                coroutineScope
                    .launch {
                        accountSelectionInputSheetState.hide()
                    }
                    .invokeOnCompletion {
                        if (!accountSelectionInputSheetState.isVisible) {
                            accountMenuOpened.value = false
                            amountMenuOpened.value = true
                        }
                    }
            },
            onDismissRequest = {
                accountMenuOpened.value = false
                isSheetOpen.value = false
            },
            accountUiState = accountUiState,
            onAccountEvent = onAccountEvent,
            getTransactionStatsForAccount = getTransactionStatsForAccount,
            updateAccountCurrency = updateAccountCurrency,
            setAccountAsMain = setAccountAsMain ,
            currencyUiState = currencyUiState,
            selectCurrency = selectCurrency ,
            transactionUiState = transactionUiState,
            onAddTransactionEvent = onAddTransactionEvent
        )
    }

    if (amountMenuOpened.value) {
        CustomAmountInputBottomSheet(
            amount = amount,
            amountSheetState = amountInputSheetState,
            specialKeys = specialKeys,
            currencyCode = currencyCode,
            onClick = {
                // Using the event system instead of direct assignment
                onAddTransactionEvent(AddTransactionEvent.UpdateAmount(calculatedResultAmount))
                scope
                    .launch {
                        amountInputSheetState.hide()
                        transactionDetailsInputSheetState.hide()
                    }
                    .invokeOnCompletion {
                        if (!amountInputSheetState.isVisible) {
                            onAddTransactionEvent(
                                AddTransactionEvent.SetTransactionAmountSheetOpen(
                                    false
                                )
                            )
                            isSheetOpen.value = false
                        }
                    }
            },
            onDismissRequest = {
                onAddTransactionEvent(AddTransactionEvent.SetTransactionAmountSheetOpen(false))
                isSheetOpen.value = false
            },
            onValueChange = { newValue ->
                // Using the event system instead of direct assignment
                onAddTransactionEvent(AddTransactionEvent.UpdateAmount(newValue))
            },
            onResultAmount = { result ->
                calculatedResultAmount = result
            },
            isTransactionTypeInfoSheetOpen = isTransactionTypeInfoSheetOpen,
            transactionTypeSheetState = transactionTypeSheetState,
            currentTransactionMode = currentTransactionMode,
            transactionTypeListItemsColor = transactionTypeListItemsColor,
            showTransactionTypeSelection = true,
            usedScreenWidth = usedScreenWidth,
            usedScreenHeight = usedScreenHeight,
            themeColors = themeColors,
            recurrenceMenuOpened = recurrenceMenuOpened,
            endDatePickerState = endDatePickerState,
            isRecurrenceBottomSheetOpen = isRecurrenceBottomSheetOpen,
            isCustomInputBottomSheetOpen = isCustomInputBottomSheetOpen,
            isEndDateSelected = isEndDateSelected,
            openEndDatePicker = openEndDatePicker,
            frequencySheetState = frequencySheetState,
            intervalSheetState = intervalSheetState,
            transactionUiState = transactionUiState,
            onAddTransactionEvent = onAddTransactionEvent,
        )
    }
}