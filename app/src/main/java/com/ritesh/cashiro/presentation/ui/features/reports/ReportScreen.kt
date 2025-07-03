package com.ritesh.cashiro.presentation.ui.features.reports

import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.ritesh.cashiro.data.local.entity.AccountEntity
import com.ritesh.cashiro.data.local.entity.TransactionEntity
import com.ritesh.cashiro.data.currency.model.CurrencySymbols
import com.ritesh.cashiro.domain.utils.toLocalDate
import com.ritesh.cashiro.presentation.effects.BlurAnimatedTextCountUpWithCurrency
import com.ritesh.cashiro.presentation.effects.BlurredAnimatedVisibility
import com.ritesh.cashiro.presentation.ui.extras.components.extras.CustomTabIndicator
import com.ritesh.cashiro.presentation.ui.extras.components.extras.CustomTitleTopAppBar
import com.ritesh.cashiro.presentation.ui.extras.components.charts.MonthPeriod
import com.ritesh.cashiro.presentation.ui.extras.components.charts.TimePeriod
import com.ritesh.cashiro.presentation.ui.extras.components.charts.TransactionAnalyticsDashboard
import com.ritesh.cashiro.presentation.ui.extras.components.charts.TransactionChartScreen
import com.ritesh.cashiro.presentation.ui.extras.components.charts.WeekPeriod
import com.ritesh.cashiro.presentation.ui.extras.components.charts.YearPeriod
import com.ritesh.cashiro.presentation.ui.features.add_transaction.TabItems
import com.ritesh.cashiro.data.local.entity.CategoryEntity
import com.ritesh.cashiro.data.local.entity.TransactionType
import com.ritesh.cashiro.domain.utils.AppEventBus
import com.ritesh.cashiro.domain.utils.CurrencyEvent
import com.ritesh.cashiro.domain.utils.CurrencyUtils
import com.ritesh.cashiro.presentation.effects.VerticalStretchedOverscroll
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenViewModel
import com.ritesh.cashiro.domain.utils.TransactionEvent
import com.ritesh.cashiro.presentation.ui.features.add_transaction.AddTransactionScreenViewModel
import com.ritesh.cashiro.presentation.ui.theme.iosFont
import com.ritesh.cashiro.R
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ReportScreen(
    screenTitle: String,
    reportViewModel: ReportScreenViewModel,
    accountViewModel: AccountScreenViewModel,
    transactionViewModel: AddTransactionScreenViewModel,
) {
    // Collect the single unified state
    val reportUiState by reportViewModel.state.collectAsState()
    ReportContent(
        screenTitle = screenTitle,
        reportUiState = reportUiState,
        onReportEvent = reportViewModel::onEvent,
        fetchAllTransactions = transactionViewModel::fetchAllTransactions,
        fetchAllAccounts = accountViewModel::fetchAllAccounts,
        accountUiState = accountViewModel.state.collectAsState().value
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun  ReportContent(
    screenTitle: String,
    reportUiState: ReportScreenState,
    onReportEvent: (ReportScreenEvent) -> Unit,
    fetchAllTransactions: () -> Unit,
    fetchAllAccounts: () -> Unit,
    accountUiState: AccountScreenState,

){
    val isRefreshing by remember(reportUiState.isLoading) {
        derivedStateOf { reportUiState.isLoading }
    }

    // Listen for transaction events and auto refresh
    LaunchedEffect(Unit) {
        AppEventBus.events.collect { event ->
            when (event) {
                is TransactionEvent.TransactionsUpdated -> {
                    // FIXED: Direct refresh without manual state management
                    onReportEvent(ReportScreenEvent.RefreshData)
                }
            }
        }
    }


    val reportsTabItems = listOf(
        TabItems(type = "Weeks"),
        TabItems(type = "Months"),
        TabItems(type = "Years"),
    )

    // Scroll behavior and pager state
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val pagerState = rememberPagerState { reportsTabItems.size }
    val coroutineScope = rememberCoroutineScope()
    val scrollStates = remember {
        List(reportsTabItems.size) { LazyListState() }
    }
    val overscrollEffects = remember(coroutineScope) {
        List(reportsTabItems.size) { VerticalStretchedOverscroll(coroutineScope) }
    }

    // State to prevent animation conflicts
    var isAnimating by remember { mutableStateOf(false) }

    // FIXED: Handle user swipe gestures (pager → ViewModel)
    LaunchedEffect(pagerState.currentPage) {
        // Reset overscroll effect for the new tab
        if (pagerState.currentPage < overscrollEffects.size) {
            overscrollEffects[pagerState.currentPage].reset()
        }

        // Only update ViewModel if this change is from user gesture (not programmatic)
        if (reportUiState.selectedTabIndex != pagerState.currentPage && !isAnimating) {
            onReportEvent(ReportScreenEvent.TabSelected(pagerState.currentPage))
        }
    }

    // FIXED: Handle tab clicks (ViewModel → pager)
    LaunchedEffect(reportUiState.selectedTabIndex) {
        // Only animate if the pager page is different and we're not already animating
        if (reportUiState.selectedTabIndex != pagerState.currentPage && !isAnimating) {
            isAnimating = true
            try {
                pagerState.animateScrollToPage(reportUiState.selectedTabIndex)
            } catch (e: Exception) {
                // Handle any animation exceptions gracefully
                Log.w("ReportScreen", "Animation interrupted: ${e.message}")
            } finally {
                isAnimating = false
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CustomTitleTopAppBar(
                scrollBehaviorLarge = scrollBehavior,
                scrollBehaviorSmall = TopAppBarDefaults.pinnedScrollBehavior(),
                title = screenTitle,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.Top
        ) {
            // Show loading indicator if loading
            if (reportUiState.isLoading && reportUiState.transactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (reportUiState.error != null) {
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
                            text = reportUiState.error,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(
                            onClick = { onReportEvent(ReportScreenEvent.RefreshData) },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            } else {
                // Tab Row
                TabRow(
                    selectedTabIndex = reportUiState.selectedTabIndex,
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
                    reportsTabItems.forEachIndexed { index, item ->
                        Tab(
                            selected = index == reportUiState.selectedTabIndex,
                            onClick = {
                                onReportEvent(ReportScreenEvent.TabSelected(index))
                            },
                            text = {
                                Text(
                                    text = item.type,
                                    fontFamily = iosFont,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    color = if (reportUiState.selectedTabIndex == index)
                                        MaterialTheme.colorScheme.inverseSurface
                                    else
                                        MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.5f)
                                )
                            },
                            modifier = Modifier
                                .clip(RoundedCornerShape(15.dp))
                                .weight(1f)
                                .zIndex(2f),
                            interactionSource = NoRippleInteractionSource()
                        )
                    }
                }

                // Pager content
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .zIndex(0f)
                        .padding(horizontal = 20.dp),
                    // FIXED: Re-enabled user swipe gestures with proper synchronization
                    userScrollEnabled = true
                ) { index ->
                    val tabScrollState = scrollStates[index]
                    val tabOverscrollEffect = overscrollEffects[index]
                    ReportPagerContent(
                        transactions = reportUiState.transactions,
                        accounts = reportUiState.accounts,
                        categories = reportUiState.categories,
                        accountUiState = accountUiState,
                        fetchAllAccounts = fetchAllAccounts,
                        fetchAllTransactions = fetchAllTransactions,
                        timePeriod = reportUiState.currentTimePeriod,
                        currentDate = reportUiState.currentDate,
                        selectedTabIndex = reportUiState.selectedTabIndex,
                        tabScrollState = tabScrollState,
                        tabOverscrollEffect = tabOverscrollEffect,
                        onPreviousPeriod = { onReportEvent(ReportScreenEvent.PreviousPeriod) },
                        onNextPeriod = { onReportEvent(ReportScreenEvent.NextPeriod) },
                        onResetPeriod = { onReportEvent(ReportScreenEvent.ResetToCurrentPeriod) },
                        isRefreshing = isRefreshing,
                        onRefresh = {
                            onReportEvent(ReportScreenEvent.RefreshData)
                        }
                    )
                }
            }
        }
    }
}

// No ripple interaction source
private class NoRippleInteractionSource : MutableInteractionSource {
    override val interactions: Flow<Interaction> = emptyFlow()
    override suspend fun emit(interaction: Interaction) {}
    override fun tryEmit(interaction: Interaction): Boolean = true
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ReportPagerContent(
    transactions: List<TransactionEntity>,
    accounts: List<AccountEntity>,
    accountUiState: AccountScreenState,
    fetchAllAccounts: () -> Unit,
    fetchAllTransactions: () -> Unit,
    categories: List<CategoryEntity>,
    timePeriod: TimePeriod,
    currentDate: LocalDate,
    selectedTabIndex: Int,
    tabScrollState: LazyListState,
    tabOverscrollEffect: VerticalStretchedOverscroll,
    onPreviousPeriod: () -> Unit,
    onNextPeriod: () -> Unit,
    onResetPeriod: () -> Unit,
    onRefresh: () -> Unit,
    isRefreshing: Boolean
) {
    val themeColors = MaterialTheme.colorScheme
    val refreshState = rememberPullToRefreshState()

    val isCurrentlyRefreshing by remember(isRefreshing) {
        derivedStateOf { isRefreshing }
    }
    PullToRefreshBox(
        state = refreshState,
        indicator ={
            Indicator(
                modifier = Modifier.align(Alignment.TopCenter),
                isRefreshing = isRefreshing,
                state = refreshState,
                containerColor = themeColors.surfaceBright,
                color = themeColors.inverseSurface.copy(0.5f),
            )},
        isRefreshing = isCurrentlyRefreshing,
        onRefresh = onRefresh,
    ){

        LazyColumn(
            state = tabScrollState,
            userScrollEnabled = true,
            modifier = Modifier
                .fillMaxSize()
                .background(themeColors.background)
                .clip(RoundedCornerShape(16.dp))
                .overscroll(tabOverscrollEffect)
//                .scrollable(
//                    orientation = Orientation.Vertical,
//                    reverseDirection = true,
//                    state = tabScrollState,
//                    overscrollEffect = tabOverscrollEffect
//                )
                .padding(horizontal = 2.dp)
        ) {
            item(key = "Time Period Header") {
                TimePeriodHeader(
                    timePeriod = timePeriod,
                    currentDate = currentDate,
                    selectedTabIndex = selectedTabIndex,
                    themeColors = themeColors,
                    onPreviousPeriod = onPreviousPeriod,
                    onNextPeriod = onNextPeriod,
                    onResetPeriod = onResetPeriod
                )
            }

            item(key = "Transaction Summary Chart") {
                TransactionChartScreen(
                    transactions = transactions,
                    accounts = accounts,
                    timePeriod = timePeriod,
                )
            }

            item(key = "Transaction Summary Stats") {
                FinancialSummaryCards(
                    transactions = transactions,
                    accounts = accounts,
                    timePeriod = timePeriod,
                    accountUiState = accountUiState,
                    fetchAllAccounts = fetchAllAccounts,
                    fetchAllTransactions = fetchAllTransactions,
                )
            }

            item(key = "Transaction Dashboard Charts") {
                TransactionAnalyticsDashboard(
                    transactions = transactions,
                    accounts = accounts,
                    timePeriod = timePeriod,
                    categories = categories
                )
            }

            item {
                Spacer(modifier = Modifier.size(250.dp))
            }
        }

        // Fade effect at the top
        Spacer(
            modifier = Modifier
                .height(15.dp)
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            themeColors.background,
                            themeColors.background.copy(0.5f),
                            Color.Transparent,
                        )
                    )
                )
        )
    }
}

@Composable
private fun TimePeriodHeader(
    timePeriod: TimePeriod,
    currentDate: LocalDate,
    selectedTabIndex: Int,
    themeColors: ColorScheme,
    onPreviousPeriod: () -> Unit,
    onNextPeriod: () -> Unit,
    onResetPeriod: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = timePeriod.getDisplayName(),
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(start = 10.dp),
                fontFamily = iosFont,
                fontWeight = FontWeight.SemiBold,
                color = themeColors.onSurface,
            )

            // Check if time period is not current
            val today = LocalDate.now()
            val isNotCurrentPeriod = when (timePeriod) {
                is WeekPeriod -> {
                    val currentWeekStart = today.with(
                        TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
                    )
                    !timePeriod.getStartDate().isEqual(currentWeekStart)
                }
                is MonthPeriod -> {
                    val currentMonthStart = today.withDayOfMonth(1)
                    !timePeriod.getStartDate().isEqual(currentMonthStart)
                }
                is YearPeriod -> {
                    val currentYearStart = today.withDayOfYear(1)
                    !timePeriod.getStartDate().isEqual(currentYearStart)
                }
                else -> false
            }

            BlurredAnimatedVisibility(visible = isNotCurrentPeriod) {
                IconButton(
                    onClick = onResetPeriod,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = themeColors.surfaceBright,
                        contentColor = themeColors.inverseSurface.copy(0.5f)
                    ),
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .size(30.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.refresh_bulk),
                        contentDescription = "Reset Selected",
                        tint = themeColors.inverseSurface.copy(0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Navigation buttons
        Row(horizontalArrangement = Arrangement.End) {
            IconButton(
                onClick = onPreviousPeriod,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = themeColors.surfaceBright,
                    contentColor = themeColors.inverseSurface.copy(0.5f)
                ),
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(40.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.arrow_mini_left_bulk),
                    contentDescription = "Previous Period",
                    tint = themeColors.inverseSurface.copy(0.5f),
                    modifier = Modifier.size(28.dp)
                )
            }

            IconButton(
                onClick = onNextPeriod,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = themeColors.surfaceBright,
                    contentColor = themeColors.inverseSurface.copy(0.5f)
                ),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.arrow_mini_right_bulk),
                    contentDescription = "Next Period",
                    tint = themeColors.inverseSurface.copy(0.5f),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun FinancialSummaryCards(
    transactions: List<TransactionEntity>,
    accounts: List<AccountEntity>,
    accountUiState: AccountScreenState,
    fetchAllAccounts: () -> Unit,
    fetchAllTransactions: () -> Unit,
    timePeriod: TimePeriod
) {
    val themeColors = MaterialTheme.colorScheme
    val mainCurrencySymbol = accounts.find { it.isMainAccount }?.currencyCode ?: "usd"
    val mainCurrency = CurrencySymbols.getSymbol(mainCurrencySymbol)
    val conversionRates = accountUiState.mainCurrencyConversionRates
    LaunchedEffect(Unit) {
        AppEventBus.events.collect { event ->
            when (event) {
                is CurrencyEvent.AccountCurrencyChanged,
                is CurrencyEvent.MainAccountCurrencyChanged,
                is CurrencyEvent.ConversionRatesUpdated -> {
                    // Refresh data
                    fetchAllAccounts()
                    fetchAllTransactions()
                }
            }
        }
    }
    // Filter transactions for current time period
    val startDate = timePeriod.getStartDate()
    val endDate = timePeriod.getEndDate()

    // Debug logging
    Log.d("ReportScreen", "Time period: $startDate to $endDate")
    Log.d("ReportScreen", "All transactions count: ${transactions.size}")

    val periodTransactions = transactions.filter { transaction ->
        val transactionDate = transaction.date.toLocalDate()
        !transactionDate.isBefore(startDate) && !transactionDate.isAfter(endDate)
    }

    Log.d("ReportScreen", "Filtered transactions count: ${periodTransactions.size}")

    // Calculate financial metrics
    val incomeTransactions = periodTransactions.filter {
        it.mode == "Income" && (it.isPaid || it.isSettled || it.isCollected || it.transactionType == TransactionType.DEFAULT)
    }
    val expenseTransactions = periodTransactions.filter {
        it.mode == "Expense" && (it.isPaid || it.isSettled || it.isCollected || it.transactionType == TransactionType.DEFAULT)
    }

    Log.d("ReportScreen", "Income transactions: ${incomeTransactions.size}")
    Log.d("ReportScreen", "Expense transactions: ${expenseTransactions.size}")


    val totalIncome = CurrencyUtils.calculateTotalIncome(
        incomeTransactions,
        mainCurrencySymbol,
        conversionRates
    )
    val totalExpense = CurrencyUtils.calculateTotalExpense(
        expenseTransactions,
        mainCurrencySymbol,
        conversionRates
    )
    val netCashFlow = totalIncome - totalExpense

    Log.d("ReportScreen", "Total income: $totalIncome")
    Log.d("ReportScreen", "Total expense: $totalExpense")
    Log.d("ReportScreen", "Net cash flow: $netCashFlow")

    val netCashFlowFormatted = String.format("%.2f", netCashFlow)
    val totalIncomeFormatted = String.format("%.2f", totalIncome)
    val totalExpenseFormatted = String.format("%.2f", totalExpense)
    // Calculate average values
    val avgIncome = if (incomeTransactions.isNotEmpty())
        totalIncome / incomeTransactions.size else 0.0
    val avgExpense = if (expenseTransactions.isNotEmpty())
        totalExpense / expenseTransactions.size else 0.0

    val periodText = when(timePeriod) {
        is WeekPeriod -> "week's"
        is MonthPeriod -> "month's"
        is YearPeriod -> "year's"
        else -> ""
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp).background(themeColors.surfaceBright,
            RoundedCornerShape(16.dp)).padding(12.dp)
    ) {
        // Net Flow Card
        FinancialCard(
            title = "Net Flow",
            value = netCashFlowFormatted.toDouble(),
            subtitle = "(total)",
            currency = mainCurrency,
            cardColor = themeColors.surfaceBright,
            iconRes = R.drawable.netflow_bulk,
            iconTint = if (netCashFlow >= 0) themeColors.primary else themeColors.error,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Income Card
            FinancialCard(
                title = "Income",
                value = totalIncomeFormatted.toDouble(), // Show total income instead of average
                subtitle = "($periodText total)",
                currency = mainCurrency,
                iconRes = R.drawable.trend_up_bulk,
                iconTint = themeColors.primary,
                modifier = Modifier.weight(1f)
            )

            // Expense Card
            FinancialCard(
                title = "Expense",
                value = totalExpenseFormatted.toDouble(), // Show total expense instead of average
                subtitle = "($periodText total)",
                currency = mainCurrency,
                iconRes = R.drawable.trend_down_bulk,
                iconTint = themeColors.error,
                modifier = Modifier.weight(1f)
            )
        }
    }

}

@Composable
private fun FinancialCard(
    title: String,
    value: Double,
    subtitle: String,
    currency: String,
    cardColor: Color = MaterialTheme.colorScheme.surface,
    @DrawableRes iconRes: Int,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    val themeColors = MaterialTheme.colorScheme

    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Title and icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = iosFont,
                    color = themeColors.onSurface.copy(alpha = 0.7f)
                )

                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Amount value
            BlurAnimatedTextCountUpWithCurrency(
                text = "$value",
                fontSize = 18.sp,
                currencySymbol = currency,
                fontFamily = iosFont,
                fontWeight = FontWeight.Bold,
                color = themeColors.onSurface
            )

            // Subtitle
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                fontFamily = iosFont,
                color = themeColors.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Preview(
    name = "Reports Screen - Light Theme",
    showBackground = true,
    backgroundColor = 0xFF000000
)
@Composable
private fun SplashScreenLightPreview() {
    MaterialTheme {
        ReportContent(
            reportUiState = ReportScreenState(),
            fetchAllAccounts = {},
            fetchAllTransactions = {},
            onReportEvent = {},
            screenTitle = "Reports",
            accountUiState = AccountScreenState(),
        )
    }
}

@Preview(
    name = "Reports Screen - Dark Theme",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun SplashScreenDarkPreview() {
    MaterialTheme {
        ReportContent(
            reportUiState = ReportScreenState(),
            fetchAllAccounts = {},
            fetchAllTransactions = {},
            onReportEvent = {},
            screenTitle = "Reports",
            accountUiState = AccountScreenState(),
        )
    }
}
//@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
//@Composable
//fun ReportScreen(
//    screenTitle: String,
//    reportViewModel: ReportScreenViewModel,
//    accountViewModel: AccountScreenViewModel,
//    transactionViewModel: AddTransactionScreenViewModel,
//) {
//    // Collect the single unified state
//    val screenState by reportViewModel.state.collectAsState()
//    val isRefreshing = remember { mutableStateOf(false) }
//
//    // Listen for transaction events and auto refresh
//    LaunchedEffect(Unit) {
//        AppEventBus.events.collect { event ->
//            when (event) {
//                is TransactionEvent.TransactionsUpdated -> {
//                    // Auto refresh when transactions are updated
//                    isRefreshing.value = true
//                    reportViewModel.onEvent(ReportScreenEvent.RefreshData)
//                    delay(1000)
//                    isRefreshing.value = false
//                }
//            }
//        }
//    }
//
//    val reportsTabItems = listOf(
//        TabItems(type = "Weeks"),
//        TabItems(type = "Months"),
//        TabItems(type = "Years"),
//    )
//
//    // Scroll behavior and pager state
//    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
//    val pagerState = rememberPagerState { reportsTabItems.size }
//    val coroutineScope = rememberCoroutineScope()
//    val scrollStates = remember {
//        List(reportsTabItems.size) { LazyListState() }
//    }
//    val overscrollEffects = remember(coroutineScope) {
//        List(reportsTabItems.size) { VerticalStretchedOverscroll(coroutineScope) }
//    }
//
//    // State to prevent animation conflicts
//    var isAnimating by remember { mutableStateOf(false) }
//
//    // FIXED: Handle user swipe gestures (pager → ViewModel)
//    LaunchedEffect(pagerState.currentPage) {
//        // Reset overscroll effect for the new tab
//        if (pagerState.currentPage < overscrollEffects.size) {
//            overscrollEffects[pagerState.currentPage].reset()
//        }
//
//        // Only update ViewModel if this change is from user gesture (not programmatic)
//        if (screenState.selectedTabIndex != pagerState.currentPage && !isAnimating) {
//            reportViewModel.onEvent(ReportScreenEvent.TabSelected(pagerState.currentPage))
//        }
//    }
//
//    // FIXED: Handle tab clicks (ViewModel → pager)
//    LaunchedEffect(screenState.selectedTabIndex) {
//        // Only animate if the pager page is different and we're not already animating
//        if (screenState.selectedTabIndex != pagerState.currentPage && !isAnimating) {
//            isAnimating = true
//            try {
//                pagerState.animateScrollToPage(screenState.selectedTabIndex)
//            } catch (e: Exception) {
//                // Handle any animation exceptions gracefully
//                Log.w("ReportScreen", "Animation interrupted: ${e.message}")
//            } finally {
//                isAnimating = false
//            }
//        }
//    }
//
//    Scaffold(
//        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
//        topBar = {
//            CustomTitleTopAppBar(
//                scrollBehaviorLarge = scrollBehavior,
//                scrollBehaviorSmall = TopAppBarDefaults.pinnedScrollBehavior(),
//                title = screenTitle,
//            )
//        },
//    ) { innerPadding ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(innerPadding)
//                .background(MaterialTheme.colorScheme.background),
//            verticalArrangement = Arrangement.Top
//        ) {
//            // Show loading indicator if loading
//            if (screenState.isLoading) {
//                Box(
//                    modifier = Modifier.fillMaxSize(),
//                    contentAlignment = Alignment.Center
//                ) {
//                    CircularProgressIndicator()
//                }
//            } else if (screenState.error != null) {
//                // Show error if there's an error
//                Box(
//                    modifier = Modifier.fillMaxSize(),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Column(
//                        horizontalAlignment = Alignment.CenterHorizontally,
//                        verticalArrangement = Arrangement.Center
//                    ) {
//                        Text(
//                            text = screenState.error ?: "An error occurred",
//                            style = MaterialTheme.typography.bodyLarge,
//                            color = MaterialTheme.colorScheme.error
//                        )
//                        Button(
//                            onClick = { reportViewModel.onEvent(ReportScreenEvent.RefreshData) },
//                            modifier = Modifier.padding(top = 16.dp)
//                        ) {
//                            Text("Retry")
//                        }
//                    }
//                }
//            } else {
//                // Tab Row
//                TabRow(
//                    selectedTabIndex = screenState.selectedTabIndex,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(horizontal = 22.dp)
//                        .shadow(elevation = 10.dp, shape = RoundedCornerShape(15.dp))
//                        .background(
//                            color = MaterialTheme.colorScheme.surfaceBright,
//                            shape = RoundedCornerShape(15.dp)
//                        )
//                        .zIndex(5f),
//                    containerColor = MaterialTheme.colorScheme.surfaceBright,
//                    divider = {},
//                    indicator = { tabPositions ->
//                        CustomTabIndicator(tabPositions, pagerState)
//                    }
//                ) {
//                    reportsTabItems.forEachIndexed { index, item ->
//                        Tab(
//                            selected = index == screenState.selectedTabIndex,
//                            onClick = {
//                                reportViewModel.onEvent(ReportScreenEvent.TabSelected(index))
//                            },
//                            text = {
//                                Text(
//                                    text = item.type,
//                                    fontFamily = iosFont,
//                                    fontSize = 13.sp,
//                                    maxLines = 1,
//                                    color = if (screenState.selectedTabIndex == index)
//                                        MaterialTheme.colorScheme.inverseSurface
//                                    else
//                                        MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.5f)
//                                )
//                            },
//                            modifier = Modifier
//                                .clip(RoundedCornerShape(15.dp))
//                                .weight(1f)
//                                .zIndex(2f),
//                            interactionSource = NoRippleInteractionSource()
//                        )
//                    }
//                }
//
//                // Pager content
//                HorizontalPager(
//                    state = pagerState,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .weight(1f)
//                        .zIndex(0f)
//                        .padding(horizontal = 20.dp),
//                    // FIXED: Re-enabled user swipe gestures with proper synchronization
//                    userScrollEnabled = true
//                ) { index ->
//                    val tabScrollState = scrollStates[index]
//                    val tabOverscrollEffect = overscrollEffects[index]
//                    ReportPagerContent(
//                        transactions = screenState.transactions,
//                        accounts = screenState.accounts,
//                        accountViewModel = accountViewModel,
//                        transactionViewModel = transactionViewModel,
//                        categories = screenState.categories,
//                        timePeriod = screenState.currentTimePeriod,
//                        currentDate = screenState.currentDate,
//                        selectedTabIndex = screenState.selectedTabIndex,
//                        tabScrollState = tabScrollState,
//                        tabOverscrollEffect = tabOverscrollEffect,
//                        onPreviousPeriod = { reportViewModel.onEvent(ReportScreenEvent.PreviousPeriod) },
//                        onNextPeriod = { reportViewModel.onEvent(ReportScreenEvent.NextPeriod) },
//                        onResetPeriod = { reportViewModel.onEvent(ReportScreenEvent.ResetToCurrentPeriod) },
//                        isRefreshing = isRefreshing.value,
//                        onRefresh = {
//                            isRefreshing.value = true
//                            reportViewModel.onEvent(ReportScreenEvent.RefreshData)
//                            // Reset the refreshing state after a delay
//                            CoroutineScope(Dispatchers.Main).launch {
//                                delay(1000)
//                                isRefreshing.value = false
//                            }
//                        }
//                    )
//                }
//            }
//        }
//    }
//}
//// No ripple interaction source
//private class NoRippleInteractionSource : MutableInteractionSource {
//    override val interactions: Flow<Interaction> = emptyFlow()
//    override suspend fun emit(interaction: Interaction) {}
//    override fun tryEmit(interaction: Interaction): Boolean = true
//}
//
//@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
//@Composable
//private fun ReportPagerContent(
//    transactions: List<TransactionEntity>,
//    accounts: List<AccountEntity>,
//    accountViewModel: AccountScreenViewModel,
//    transactionViewModel: AddTransactionScreenViewModel,
//    categories: List<CategoryEntity>,
//    timePeriod: TimePeriod,
//    currentDate: LocalDate,
//    selectedTabIndex: Int,
//    tabScrollState: LazyListState,
//    tabOverscrollEffect: VerticalStretchedOverscroll,
//    onPreviousPeriod: () -> Unit,
//    onNextPeriod: () -> Unit,
//    onResetPeriod: () -> Unit,
//    onRefresh: () -> Unit,
//    isRefreshing: Boolean
//) {
//    val themeColors = MaterialTheme.colorScheme
//    val refreshState = rememberPullToRefreshState()
//
//    PullToRefreshBox(
//        state = refreshState,
//        indicator ={
//            Indicator(
//                modifier = Modifier.align(Alignment.TopCenter),
//                isRefreshing = isRefreshing,
//                state = refreshState,
//                containerColor = themeColors.surfaceBright,
//                color = themeColors.inverseSurface.copy(0.5f),
//            )},
//        isRefreshing = isRefreshing,
//        onRefresh = onRefresh,
//    ){
//
//        LazyColumn(
//            state = tabScrollState,
//            userScrollEnabled = false,
//            modifier = Modifier
//                .fillMaxSize()
//                .background(themeColors.background)
//                .clip(RoundedCornerShape(16.dp))
//                .overscroll(tabOverscrollEffect)
//                .scrollable(
//                    orientation = Orientation.Vertical,
//                    reverseDirection = true,
//                    state = tabScrollState,
//                    overscrollEffect = tabOverscrollEffect
//                )
//                .padding(horizontal = 2.dp)
//        ) {
//            item(key = "Time Period Header") {
//                TimePeriodHeader(
//                    timePeriod = timePeriod,
//                    currentDate = currentDate,
//                    selectedTabIndex = selectedTabIndex,
//                    themeColors = themeColors,
//                    onPreviousPeriod = onPreviousPeriod,
//                    onNextPeriod = onNextPeriod,
//                    onResetPeriod = onResetPeriod
//                )
//            }
//
//            item(key = "Transaction Summary Chart") {
//                TransactionChartScreen(
//                    transactions = transactions,
//                    accounts = accounts,
//                    timePeriod = timePeriod,
//                )
//            }
//
//            item(key = "Transaction Summary Stats") {
//                FinancialSummaryCards(
//                    transactions = transactions,
//                    accounts = accounts,
//                    accountViewModel = accountViewModel,
//                    transactionViewModel = transactionViewModel,
//                    timePeriod = timePeriod,
//                )
//            }
//
//            item(key = "Transaction Dashboard Charts") {
//                TransactionAnalyticsDashboard(
//                    transactions = transactions,
//                    accounts = accounts,
//                    timePeriod = timePeriod,
//                    categories = categories
//                )
//            }
//
//            item {
//                Spacer(modifier = Modifier.size(250.dp))
//            }
//        }
//
//        // Fade effect at the top
//        Spacer(
//            modifier = Modifier
//                .height(15.dp)
//                .fillMaxWidth()
//                .background(
//                    brush = Brush.verticalGradient(
//                        colors = listOf(
//                            themeColors.background,
//                            themeColors.background.copy(0.5f),
//                            Color.Transparent,
//                        )
//                    )
//                )
//        )
//    }
//}
//
//@Composable
//private fun TimePeriodHeader(
//    timePeriod: TimePeriod,
//    currentDate: LocalDate,
//    selectedTabIndex: Int,
//    themeColors: ColorScheme,
//    onPreviousPeriod: () -> Unit,
//    onNextPeriod: () -> Unit,
//    onResetPeriod: () -> Unit
//) {
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(top = 10.dp),
//        horizontalArrangement = Arrangement.SpaceBetween,
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        Row(
//            horizontalArrangement = Arrangement.Start,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(
//                text = timePeriod.getDisplayName(),
//                textAlign = TextAlign.Start,
//                modifier = Modifier.padding(start = 10.dp),
//                fontFamily = iosFont,
//                fontWeight = FontWeight.SemiBold,
//                color = themeColors.onSurface,
//            )
//
//            // Check if time period is not current
//            val today = LocalDate.now()
//            val isNotCurrentPeriod = when (timePeriod) {
//                is WeekPeriod -> {
//                    val currentWeekStart = today.with(
//                        TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)
//                    )
//                    !timePeriod.getStartDate().isEqual(currentWeekStart)
//                }
//                is MonthPeriod -> {
//                    val currentMonthStart = today.withDayOfMonth(1)
//                    !timePeriod.getStartDate().isEqual(currentMonthStart)
//                }
//                is YearPeriod -> {
//                    val currentYearStart = today.withDayOfYear(1)
//                    !timePeriod.getStartDate().isEqual(currentYearStart)
//                }
//                else -> false
//            }
//
//            BlurredAnimatedVisibility(visible = isNotCurrentPeriod) {
//                IconButton(
//                    onClick = onResetPeriod,
//                    colors = IconButtonDefaults.iconButtonColors(
//                        containerColor = themeColors.surfaceBright,
//                        contentColor = themeColors.inverseSurface.copy(0.5f)
//                    ),
//                    modifier = Modifier
//                        .padding(start = 10.dp)
//                        .size(30.dp)
//                ) {
//                    Icon(
//                        painter = painterResource(R.drawable.refresh_bulk),
//                        contentDescription = "Reset Selected",
//                        tint = themeColors.inverseSurface.copy(0.5f),
//                        modifier = Modifier.size(16.dp)
//                    )
//                }
//            }
//        }
//
//        // Navigation buttons
//        Row(horizontalArrangement = Arrangement.End) {
//            IconButton(
//                onClick = onPreviousPeriod,
//                colors = IconButtonDefaults.iconButtonColors(
//                    containerColor = themeColors.surfaceBright,
//                    contentColor = themeColors.inverseSurface.copy(0.5f)
//                ),
//                modifier = Modifier
//                    .padding(end = 16.dp)
//                    .size(40.dp)
//            ) {
//                Icon(
//                    painter = painterResource(R.drawable.arrow_mini_left_bulk),
//                    contentDescription = "Previous Period",
//                    tint = themeColors.inverseSurface.copy(0.5f),
//                    modifier = Modifier.size(28.dp)
//                )
//            }
//
//            IconButton(
//                onClick = onNextPeriod,
//                colors = IconButtonDefaults.iconButtonColors(
//                    containerColor = themeColors.surfaceBright,
//                    contentColor = themeColors.inverseSurface.copy(0.5f)
//                ),
//                modifier = Modifier.size(40.dp)
//            ) {
//                Icon(
//                    painter = painterResource(R.drawable.arrow_mini_right_bulk),
//                    contentDescription = "Next Period",
//                    tint = themeColors.inverseSurface.copy(0.5f),
//                    modifier = Modifier.size(28.dp)
//                )
//            }
//        }
//    }
//}
//
//@Composable
//private fun FinancialSummaryCards(
//    transactions: List<TransactionEntity>,
//    accounts: List<AccountEntity>,
//    accountViewModel: AccountScreenViewModel,
//    transactionViewModel: AddTransactionScreenViewModel,
//    timePeriod: TimePeriod
//) {
//    val themeColors = MaterialTheme.colorScheme
//    val mainCurrencySymbol = accounts.find { it.isMainAccount }?.currencyCode ?: "usd"
//    val mainCurrency = CurrencySymbols.getSymbol(mainCurrencySymbol)
//    val conversionRates = accountViewModel.mainCurrencyConversionRates.collectAsState().value
//    LaunchedEffect(Unit) {
//        AppEventBus.events.collect { event ->
//            when (event) {
//                is CurrencyEvent.AccountCurrencyChanged,
//                is CurrencyEvent.MainAccountCurrencyChanged,
//                is CurrencyEvent.ConversionRatesUpdated -> {
//                    // Refresh data
//                    accountViewModel.fetchAllAccounts()
//                    transactionViewModel.fetchAllTransactions()
//                }
//            }
//        }
//    }
//    // Filter transactions for current time period
//    val startDate = timePeriod.getStartDate()
//    val endDate = timePeriod.getEndDate()
//
//    // Debug logging
//    Log.d("ReportScreen", "Time period: $startDate to $endDate")
//    Log.d("ReportScreen", "All transactions count: ${transactions.size}")
//
//    val periodTransactions = transactions.filter { transaction ->
//        val transactionDate = transaction.date.toLocalDate()
//        !transactionDate.isBefore(startDate) && !transactionDate.isAfter(endDate)
//    }
//
//    Log.d("ReportScreen", "Filtered transactions count: ${periodTransactions.size}")
//
//    // Calculate financial metrics
//    val incomeTransactions = periodTransactions.filter {
//        it.mode == "Income" && (it.isPaid || it.isSettled || it.isCollected || it.transactionType == TransactionType.DEFAULT)
//    }
//    val expenseTransactions = periodTransactions.filter {
//        it.mode == "Expense" && (it.isPaid || it.isSettled || it.isCollected || it.transactionType == TransactionType.DEFAULT)
//    }
//
//    Log.d("ReportScreen", "Income transactions: ${incomeTransactions.size}")
//    Log.d("ReportScreen", "Expense transactions: ${expenseTransactions.size}")
//
//
//    val totalIncome = CurrencyUtils.calculateTotalIncome(
//        incomeTransactions,
//        mainCurrencySymbol,
//        conversionRates
//    )
//    val totalExpense = CurrencyUtils.calculateTotalExpense(
//        expenseTransactions,
//        mainCurrencySymbol,
//        conversionRates
//    )
//    val netCashFlow = totalIncome - totalExpense
//
//    Log.d("ReportScreen", "Total income: $totalIncome")
//    Log.d("ReportScreen", "Total expense: $totalExpense")
//    Log.d("ReportScreen", "Net cash flow: $netCashFlow")
//
//    val netCashFlowFormatted = String.format("%.2f", netCashFlow)
//    val totalIncomeFormatted = String.format("%.2f", totalIncome)
//    val totalExpenseFormatted = String.format("%.2f", totalExpense)
//    // Calculate average values
//    val avgIncome = if (incomeTransactions.isNotEmpty())
//        totalIncome / incomeTransactions.size else 0.0
//    val avgExpense = if (expenseTransactions.isNotEmpty())
//        totalExpense / expenseTransactions.size else 0.0
//
//    val periodText = when(timePeriod) {
//        is WeekPeriod -> "week's"
//        is MonthPeriod -> "month's"
//        is YearPeriod -> "year's"
//        else -> ""
//    }
//
//    Row(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 12.dp),
//        horizontalArrangement = Arrangement.spacedBy(10.dp)
//    ) {
//        // Income Card
//        FinancialCard(
//            title = "Income",
//            value = totalIncomeFormatted.toDouble(), // Show total income instead of average
//            subtitle = "($periodText total)",
//            currency = mainCurrency,
//            iconRes = R.drawable.trend_up_bulk,
//            iconTint = themeColors.primary,
//            modifier = Modifier.weight(1f)
//        )
//
//        // Expense Card
//        FinancialCard(
//            title = "Expense",
//            value = totalExpenseFormatted.toDouble(), // Show total expense instead of average
//            subtitle = "($periodText total)",
//            currency = mainCurrency,
//            iconRes = R.drawable.trend_down_bulk,
//            iconTint = themeColors.error,
//            modifier = Modifier.weight(1f)
//        )
//
//        // Net Flow Card
//        FinancialCard(
//            title = "Net Flow",
//            value = netCashFlowFormatted.toDouble(),
//            subtitle = "(total)",
//            currency = mainCurrency,
//            iconRes = R.drawable.netflow_bulk,
//            iconTint = if (netCashFlow >= 0) themeColors.primary else themeColors.error,
//            modifier = Modifier.weight(1f),
//        )
//    }
//}
//
//@Composable
//private fun FinancialCard(
//    title: String,
//    value: Double,
//    subtitle: String,
//    currency: String,
//    @DrawableRes iconRes: Int,
//    iconTint: Color,
//    modifier: Modifier = Modifier
//) {
//    val themeColors = MaterialTheme.colorScheme
//
//    Card(
//        modifier = modifier.height(100.dp),
//        shape = RoundedCornerShape(16.dp),
//        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
//        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(12.dp),
//            verticalArrangement = Arrangement.SpaceBetween
//        ) {
//            // Title and icon
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = title,
//                    style = MaterialTheme.typography.bodySmall,
//                    fontFamily = iosFont,
//                    color = themeColors.onSurface.copy(alpha = 0.7f)
//                )
//
//                Icon(
//                    painter = painterResource(iconRes),
//                    contentDescription = null,
//                    tint = iconTint,
//                    modifier = Modifier.size(20.dp)
//                )
//            }
//
//            // Amount value
//            BlurAnimatedTextCountUpWithCurrency(
//                text = "$value",
//                fontSize = 18.sp,
//                currencySymbol = currency,
//                fontFamily = iosFont,
//                fontWeight = FontWeight.Bold,
//                color = themeColors.onSurface
//            )
//
//            // Subtitle
//            Text(
//                text = subtitle,
//                style = MaterialTheme.typography.bodySmall,
//                fontFamily = iosFont,
//                color = themeColors.onSurface.copy(alpha = 0.5f)
//            )
//        }
//    }
//}