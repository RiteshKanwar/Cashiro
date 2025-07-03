package com.ritesh.cashiro.presentation.ui.extras.components.charts

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.ritesh.cashiro.data.local.entity.AccountEntity
import com.ritesh.cashiro.data.local.entity.CategoryEntity
import com.ritesh.cashiro.data.local.entity.TransactionEntity
import com.ritesh.cashiro.data.currency.model.CurrencySymbols
import com.ritesh.cashiro.domain.utils.toLocalDate
import com.ritesh.cashiro.presentation.effects.BlurredAnimatedVisibility
import com.ritesh.cashiro.presentation.ui.theme.SuccessColor
import com.ritesh.cashiro.presentation.ui.theme.iosFont
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.DividerProperties
import ir.ehsannarmani.compose_charts.models.DotProperties
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.GridProperties.AxisProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.Line
import ir.ehsannarmani.compose_charts.models.LineProperties
import ir.ehsannarmani.compose_charts.models.Pie
import ir.ehsannarmani.compose_charts.models.StrokeStyle
import ir.ehsannarmani.compose_charts.models.ZeroLineProperties
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.abs

enum class TransactionDataType {
    ALL, EXPENSES, INCOME,
}
@Composable
fun TransactionAnalyticsDashboard(
    accounts: List<AccountEntity>,
    transactions: List<TransactionEntity>,
    categories: List<CategoryEntity>,
    timePeriod: TimePeriod
){
    val themeColors = MaterialTheme.colorScheme

    Column {
        MultiAccountLineChart(
            accounts = accounts,
            transactions = transactions,
            timePeriod = timePeriod,
            themeColors = themeColors
        )
        CategoryPieChart(
            categories = categories,
            transactions = transactions,
            accounts = accounts,
            timePeriod = timePeriod,
            themeColors = themeColors
        )
    }

}

@Composable
fun MultiAccountLineChart(
    modifier: Modifier = Modifier,
    accounts: List<AccountEntity>,
    transactions: List<TransactionEntity>,
    timePeriod: TimePeriod,
    themeColors: ColorScheme
) {
    var expanded by remember { mutableStateOf(false) }
    // Add state for selected account (-1 represents "All Accounts")
    var selectedAccountId by rememberSaveable { mutableIntStateOf(-1) }
    // Get the name of the selected account or "All Accounts" if none selected
    val selectedAccountName = remember(selectedAccountId, accounts) {
        if (selectedAccountId == -1) "All Accounts"
        else accounts.find { it.id == selectedAccountId }?.accountName ?: "All Accounts"
    }
    val selectedAccountColor = remember(selectedAccountId, accounts){
        if (selectedAccountId == -1) themeColors.primary.copy(0.8f)
        else Color(accounts.find { it.id == selectedAccountId }?.cardColor1 ?: 0).copy(0.8f)
    }
    val hazeStateForPopupButton = remember { HazeState() }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp,vertical = 10.dp)
    ) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .zIndex(3f)
            .padding(top = 15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Balance Over Time",
                color = MaterialTheme.colorScheme.inverseSurface,
                fontFamily = iosFont,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(start = 10.dp)
            )

            // Button to open DropDownMenu for Balance Account Chart selection
            Box(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { expanded = !expanded }
                    )
            ) {
                Row(
                    modifier = Modifier
                        .background(
                            color = selectedAccountColor,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = selectedAccountName, // Now displays the selected account name
                        fontFamily = iosFont,
                        lineHeight = 12.sp,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Show chart type options",
                        modifier = Modifier.size(12.dp),
                        tint = Color.White
                    )
                }
            }
        }
        BlurredAnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + scaleIn(animationSpec = spring(
                stiffness = Spring.StiffnessLow,
                dampingRatio = Spring.DampingRatioLowBouncy),
                initialScale = 0f) + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + scaleOut(animationSpec = spring(
                stiffness = Spring.StiffnessLow,
                dampingRatio = Spring.DampingRatioLowBouncy),
                targetScale = 0f) + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .zIndex(2f)
                .padding(top = 30.dp)
        ) {
            val haptic = LocalHapticFeedback.current
            Card(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .hazeChild(
                        state = hazeStateForPopupButton,
                        block = {
                            style = HazeDefaults.style(
                                backgroundColor = Color.Transparent,
                                blurRadius = 20.dp,
                                noiseFactor = -1f,)
                        }
                    )
                    .sizeIn(maxWidth = 150.dp, maxHeight = 280.dp)
                    .wrapContentWidth()
                    .wrapContentHeight()
                    .align(Alignment.TopEnd),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceBright.copy(0.5f),
                    contentColor = MaterialTheme.colorScheme.inverseSurface.copy(0.6f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .wrapContentWidth()
                        .heightIn(max = 280.dp), // Match the Card's max height
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    // Add "All Accounts" option at the top
                    item {
                        DropdownMenuItem(
                            text = {
                                Row(
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Spacer(
                                        modifier = modifier
                                            .size(10.dp)
                                            .background(color = MaterialTheme.colorScheme.primary, shape = CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "All Accounts",
                                        fontSize = 12.sp,
                                        fontFamily = iosFont,
                                        textAlign = TextAlign.Start
                                    )
                                }
                            },
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedAccountId = -1
                                expanded = false
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = MaterialTheme.colorScheme.inverseSurface.copy(0.7f),
                            )
                        )
                    }

                    // Individual account options
                    items(accounts) { item ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Spacer(
                                        modifier = modifier
                                            .size(10.dp)
                                            .background(color = Color(item.cardColor1), shape = CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = item.accountName,
                                        fontSize = 12.sp,
                                        fontFamily = iosFont,
                                        textAlign = TextAlign.Start,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            },
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedAccountId = item.id
                                expanded = false
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = MaterialTheme.colorScheme.inverseSurface.copy(0.7f),
                            )
                        )
                    }
                }
            }
        }
        MultiAccountLineChartContent(
            modifier = modifier.padding(top = 50.dp).haze(hazeStateForPopupButton),
            accounts = accounts,
            transactions = transactions,
            timePeriod = timePeriod,
            selectedAccountId = selectedAccountId
        )
    }
}

@Composable
fun MultiAccountLineChartContent(
    modifier: Modifier = Modifier,
    accounts: List<AccountEntity>,
    transactions: List<TransactionEntity>,
    timePeriod: TimePeriod,
    selectedAccountId: Int = -1 // -1 means show all accounts
) {
    val themeColors = MaterialTheme.colorScheme

    // Filter transactions for the current time period
    val periodTransactions by remember(transactions, timePeriod) {
        mutableStateOf(
            transactions.filter { transaction ->
                val transactionDate = transaction.date.toLocalDate()
                (transactionDate.isEqual(timePeriod.getStartDate()) ||
                        transactionDate.isEqual(timePeriod.getEndDate()) ||
                        (transactionDate.isAfter(timePeriod.getStartDate()) &&
                                transactionDate.isBefore(timePeriod.getEndDate())))
            }
        )
    }

    // Filter accounts based on selection
    val filteredAccounts = remember(accounts, selectedAccountId) {
        if (selectedAccountId == -1) accounts
        else accounts.filter { it.id == selectedAccountId }
    }
    val mainCurrencyCode = accounts.find { it.isMainAccount }?.currencyCode ?: "usd"
    val currencyCode = filteredAccounts.find { it.id == selectedAccountId }?.currencyCode ?: mainCurrencyCode
    val currency = CurrencySymbols.getSymbol(currencyCode)

    // Prepare data for each account
    val accountsData = remember(filteredAccounts, periodTransactions, timePeriod) {
        prepareAccountsBalanceData(filteredAccounts, periodTransactions, timePeriod)
    }

    val balanceData = accountsData.first
    val labels = accountsData.second

    Card(
        modifier = modifier
            .height(280.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = themeColors.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Check if there's any data to display
            if (balanceData.isEmpty() || balanceData.all { it.values.isEmpty() || it.values.all { value -> value == 0.0 } }) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No account data for this period",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = iosFont,
                        color = themeColors.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                val lines = remember(balanceData) {
                    balanceData.map { accountData ->
                        Line(
                            label = accountData.accountName,
                            values = accountData.values,
                            color = SolidColor(Color(accountData.color)),
                            firstGradientFillColor = Color(accountData.color).copy(alpha = 0.1f),
                            secondGradientFillColor = Color.Transparent,
                            strokeAnimationSpec = tween(1500, easing = EaseInOutCubic),
                            gradientAnimationDelay = 750,
                            drawStyle = DrawStyle.Stroke(width = 2.dp),
                            curvedEdges = true,
                            dotProperties = DotProperties(
                                enabled = true,
                                color = SolidColor(Color(accountData.color)),
                                strokeWidth = 3.dp,
                                radius = 4.dp,
                                strokeColor = SolidColor(themeColors.surface)
                            )
                        )
                    }
                }

                LineChart(
                    modifier = Modifier.fillMaxSize(),
                    data = lines,
                    dividerProperties = DividerProperties(
                        enabled = true,
                        xAxisProperties = LineProperties(
                            color = SolidColor(themeColors.onSurface.copy(alpha = 0f)),
                            thickness = 0.dp
                        ),
                        yAxisProperties = LineProperties(
                            color = SolidColor(themeColors.onSurface.copy(alpha = 0f)),
                            thickness = 0.dp
                        )
                    ),
                    indicatorProperties = HorizontalIndicatorProperties(
                        enabled = true,
                        textStyle = androidx.compose.ui.text.TextStyle.Default.copy(
                            fontSize = 12.sp,
                            color = themeColors.inverseSurface.copy(0.5f),
                            fontFamily = iosFont,
                            textAlign = TextAlign.Center),
                        contentBuilder = { value -> formatCurrency(value, currency) }
                    ),
                    labelHelperProperties = LabelHelperProperties(
                        enabled = true,
                        textStyle = androidx.compose.ui.text.TextStyle.Default.copy(
                            fontSize = 12.sp,
                            color = themeColors.inverseSurface,
                            fontFamily = iosFont,
                            textAlign = TextAlign.End),
                    ),
                    labelProperties = remember(labels) {
                        LabelProperties(
                            enabled = true,
                            textStyle = androidx.compose.ui.text.TextStyle.Default.copy(
                                fontSize = 10.sp,
                                color = themeColors.inverseSurface.copy(0.5f),
                                fontFamily = iosFont,
                                textAlign = TextAlign.End),
                            labels = labels
                        )
                    },
                    zeroLineProperties = ZeroLineProperties(
                        enabled = true,
                        style = StrokeStyle.Dashed(),
                        color = SolidColor(themeColors.onSurface.copy(alpha = 0.2f)),
                    ),
                    gridProperties = GridProperties(
                        enabled = true,
                        xAxisProperties = AxisProperties(
                            enabled = true,
                            style = StrokeStyle.Dashed(),
                            color = SolidColor(themeColors.onSurface.copy(alpha = 0.2f))),
                        yAxisProperties = AxisProperties(
                            enabled = true,
                            style = StrokeStyle.Dashed(),
                            color = SolidColor(themeColors.onSurface.copy(alpha = 0.2f)))
                    ),
                    animationMode = AnimationMode.Together(delayBuilder = { it * 300L }),
                )
            }
        }
    }
}

// Helper data class to organize account data
private data class AccountBalanceData(
    val accountName: String,
    val values: List<Double>,
    val color: Int
)

// Helper function to prepare account balance data
private fun prepareAccountsBalanceData(
    accounts: List<AccountEntity>,
    transactions: List<TransactionEntity>,
    timePeriod: TimePeriod
): Pair<List<AccountBalanceData>, List<String>> {
    val result = when (timePeriod) {
        is WeekPeriod -> prepareWeekBalanceData(accounts, transactions, timePeriod)
        is MonthPeriod -> prepareMonthBalanceData(accounts, transactions, timePeriod)
        is YearPeriod -> prepareYearBalanceData(accounts, transactions, timePeriod)
        else -> Pair(emptyList(), emptyList())
    }

    return result
}

private fun prepareWeekBalanceData(
    accounts: List<AccountEntity>,
    transactions: List<TransactionEntity>,
    timePeriod: WeekPeriod
): Pair<List<AccountBalanceData>, List<String>> {
    val startDate = timePeriod.getStartDate()
    val endDate = timePeriod.getEndDate()
    val dateLabels = mutableListOf<String>()
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd")
    val accountsData = mutableListOf<AccountBalanceData>()

    // Prepare date labels
    for (i in 0..6) {
        val currentDate = startDate.plusDays(i.toLong())
        val formattedDate = dateFormatter.format(currentDate)
        dateLabels.add(formattedDate)
    }

    for (account in accounts) {
        val accountBalances = mutableListOf<Double>()

        // We need to start from the CURRENT balance and work backwards
        val allAccountTransactions = transactions.filter {
            it.accountId == account.id || (it.mode == "Transfer" && it.destinationAccountId == account.id)
        }.sortedBy { it.date }

        // For each day of the week, calculate the balance on that day
        for (i in 0..6) {
            val currentDate = startDate.plusDays(i.toLong())

            // Clone the current balance
            var balanceOnDate = account.balance

            // Apply all transactions that occurred AFTER this date
            // (removing their effect to get the balance on that specific date)
            for (transaction in allAccountTransactions) {
                val transactionDate = transaction.date.toLocalDate()

                // If transaction happened after the current date we're calculating,
                // we need to reverse its effect to get balance on that date
                if (transactionDate.isAfter(currentDate)) {
                    balanceOnDate -= transaction.getEffectOnBalance(account.id)
                }
            }

            accountBalances.add(balanceOnDate)
        }

        accountsData.add(AccountBalanceData(
            accountName = account.accountName,
            values = accountBalances,
            color = account.cardColor1
        ))
    }

    return Pair(accountsData, dateLabels)
}

// Updated helper function for month view
private fun prepareMonthBalanceData(
    accounts: List<AccountEntity>,
    transactions: List<TransactionEntity>,
    timePeriod: MonthPeriod
): Pair<List<AccountBalanceData>, List<String>> {
    val startDate = timePeriod.getStartDate()
    val dateLabels = mutableListOf<String>()
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd")
    val accountsData = mutableListOf<AccountBalanceData>()

    // Determine the number of days in the month
    val yearMonth = YearMonth.of(startDate.year, startDate.month)
    val daysInMonth = yearMonth.lengthOfMonth()

    // We'll split the month into roughly 5 equal parts
    val dayIncrement = daysInMonth / 5

    // Generate date labels and checkpoint dates
    val checkpointDates = mutableListOf<LocalDate>()
    for (i in 0 until 5) {
        val segmentStartDay = 1 + (i * dayIncrement)
        val segmentStartDate = LocalDate.of(startDate.year, startDate.month, segmentStartDay)
        dateLabels.add(dateFormatter.format(segmentStartDate))
        checkpointDates.add(segmentStartDate)
    }

    for (account in accounts) {
        val accountBalances = mutableListOf<Double>()

        // Get all transactions for this account
        val allAccountTransactions = transactions.filter {
            it.accountId == account.id || (it.mode == "Transfer" && it.destinationAccountId == account.id)
        }.sortedBy { it.date }

        // For each checkpoint, calculate the balance on that day
        for (checkpointDate in checkpointDates) {
            // Clone the current balance
            var balanceOnDate = account.balance

            // Apply all transactions that occurred AFTER this date
            // (removing their effect to get the balance on that specific date)
            for (transaction in allAccountTransactions) {
                val transactionDate = transaction.date.toLocalDate()

                // If transaction happened after the checkpoint date we're calculating,
                // we need to reverse its effect to get balance on that date
                if (transactionDate.isAfter(checkpointDate)) {
                    balanceOnDate -= transaction.getEffectOnBalance(account.id)
                }
            }

            accountBalances.add(balanceOnDate)
        }

        accountsData.add(AccountBalanceData(
            accountName = account.accountName,
            values = accountBalances,
            color = account.cardColor1
        ))
    }

    return Pair(accountsData, dateLabels)
}

// Updated helper function for year view
private fun prepareYearBalanceData(
    accounts: List<AccountEntity>,
    transactions: List<TransactionEntity>,
    timePeriod: YearPeriod
): Pair<List<AccountBalanceData>, List<String>> {
    val monthLabels = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val accountsData = mutableListOf<AccountBalanceData>()
    val year = timePeriod.getStartDate().year

    // Create a list of month start dates
    val monthStartDates = mutableListOf<LocalDate>()
    for (month in 1..12) {
        monthStartDates.add(LocalDate.of(year, month, 1))
    }

    for (account in accounts) {
        val accountBalances = mutableListOf<Double>()

        // Get all transactions for this account
        val allAccountTransactions = transactions.filter {
            it.accountId == account.id || (it.mode == "Transfer" && it.destinationAccountId == account.id)
        }.sortedBy { it.date }

        // For each month, calculate the balance on the 1st of that month
        for (monthStart in monthStartDates) {
            // Clone the current balance
            var balanceOnDate = account.balance

            // The last day of the month
            val monthEnd = monthStart.plusMonths(1).minusDays(1)

            // Apply all transactions that occurred AFTER this month
            // (removing their effect to get the balance at the start of this month)
            for (transaction in allAccountTransactions) {
                val transactionDate = transaction.date.toLocalDate()

                // If transaction happened after this month,
                // we need to reverse its effect to get balance for this month
                if (transactionDate.isAfter(monthEnd)) {
                    balanceOnDate -= transaction.getEffectOnBalance(account.id)
                }
            }

            accountBalances.add(balanceOnDate)
        }

        accountsData.add(AccountBalanceData(
            accountName = account.accountName,
            values = accountBalances,
            color = account.cardColor1
        ))
    }

    return Pair(accountsData, monthLabels)
}

// Helper function to calculate transaction effect on account balance
private fun TransactionEntity.getEffectOnBalance(forAccountId: Int): Double {
    return when {
        // For income to this account, balance increases
        mode == "Income" && accountId == forAccountId -> amount

        // For expenses from this account, balance decreases
        mode == "Expense" && accountId == forAccountId -> -amount

        // For transfers, need to check if this account is sending or receiving
        mode == "Transfer" && accountId == forAccountId -> -amount
        mode == "Transfer" && destinationAccountId == forAccountId -> amount

        // Otherwise, no effect on this account
        else -> 0.0
    }
}




@Composable
fun CategoryPieChart(
    modifier: Modifier = Modifier,
    transactions: List<TransactionEntity>,
    accounts: List<AccountEntity>,
    categories: List<CategoryEntity>,
    timePeriod: TimePeriod,
    themeColors: ColorScheme
) {
    // State for dropdown menu
    var expanded by remember { mutableStateOf(false) }
    // State for selected data type (Expenses, Income or All)
    var selectedDataType by rememberSaveable { mutableStateOf(TransactionDataType.ALL) }

    // Get the name of the selected chart type
    val selectedTypeName = remember(selectedDataType) {
        when (selectedDataType) {
            TransactionDataType.ALL -> "All Transactions"
            TransactionDataType.EXPENSES -> "Expenses"
            TransactionDataType.INCOME -> "Income"

        }
    }

    val selectedTypeColor = remember(selectedDataType) {
        when (selectedDataType) {
            TransactionDataType.ALL -> themeColors.primary.copy(alpha = 0.8f)
            TransactionDataType.EXPENSES -> themeColors.outline.copy(alpha = 0.8f)
            TransactionDataType.INCOME -> themeColors.tertiary.copy(alpha = 0.8f)

        }
    }

    val hazeStateForPopupButton = remember { HazeState() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp, vertical = 10.dp)
    ) {
        // Title and dropdown selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(3f)
                .padding(top = 15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Category Breakdown",
                color = MaterialTheme.colorScheme.inverseSurface,
                fontFamily = iosFont,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(start = 10.dp)
            )

            // Button to open DropDownMenu for chart type selection
            Box(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { expanded = !expanded }
                    )
            ) {
                Row(
                    modifier = Modifier
                        .background(
                            color = selectedTypeColor,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = selectedTypeName,
                        fontFamily = iosFont,
                        lineHeight = 12.sp,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Show chart type options",
                        modifier = Modifier.size(12.dp),
                        tint = Color.White
                    )
                }
            }
        }

        // Dropdown menu for chart type selection
        BlurredAnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + scaleIn(animationSpec = spring(
                stiffness = Spring.StiffnessLow,
                dampingRatio = Spring.DampingRatioLowBouncy),
                initialScale = 0f) + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + scaleOut(animationSpec = spring(
                stiffness = Spring.StiffnessLow,
                dampingRatio = Spring.DampingRatioLowBouncy),
                targetScale  = 0f) + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .zIndex(2f)
                .padding(top = 30.dp)
        ) {
            val haptic = LocalHapticFeedback.current
            Card(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .hazeChild(
                        state = hazeStateForPopupButton,
                        block = {
                            style = HazeDefaults.style(
                                backgroundColor = Color.Transparent,
                                blurRadius = 20.dp,
                                noiseFactor = -1f,)
                        }
                    )
                    .sizeIn(maxWidth = 150.dp, maxHeight = 280.dp)
                    .wrapContentWidth()
                    .wrapContentHeight()
                    .align(Alignment.TopEnd),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceBright.copy(0.5f),
                    contentColor = MaterialTheme.colorScheme.inverseSurface.copy(0.6f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.wrapContentWidth()
                        .heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    // Dropdown menu options
                    item {
                        DropdownMenuItem(
                            text = {
                                Row(
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Spacer(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(color = themeColors.primary.copy(alpha = 0.8f), shape = CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "All Transactions",
                                        fontSize = 12.sp,
                                        fontFamily = iosFont,
                                        textAlign = TextAlign.Start
                                    )
                                }
                            },
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedDataType = TransactionDataType.ALL
                                expanded = false
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = MaterialTheme.colorScheme.inverseSurface.copy(0.7f),
                            )
                        )
                    }
                    item {
                        DropdownMenuItem(
                            text = {
                                Row(
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Spacer(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(color = themeColors.outline.copy(alpha = 0.8f), shape = CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Expenses",
                                        fontSize = 12.sp,
                                        fontFamily = iosFont,
                                        textAlign = TextAlign.Start
                                    )
                                }
                            },
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedDataType = TransactionDataType.EXPENSES
                                expanded = false
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = MaterialTheme.colorScheme.inverseSurface.copy(0.7f),
                            )
                        )
                    }

                    item {
                        DropdownMenuItem(
                            text = {
                                Row(
                                    horizontalArrangement = Arrangement.Start,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Spacer(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(color = SuccessColor.copy(alpha = 0.8f), shape = CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Income",
                                        fontSize = 12.sp,
                                        fontFamily = iosFont,
                                        textAlign = TextAlign.Start
                                    )
                                }
                            },
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedDataType = TransactionDataType.INCOME
                                expanded = false
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = MaterialTheme.colorScheme.inverseSurface.copy(0.7f),
                            )
                        )
                    }
                }
            }
        }
        Box(modifier = modifier.fillMaxWidth().haze(hazeStateForPopupButton)){
            BlurredAnimatedVisibility(
                visible = selectedDataType == TransactionDataType.ALL
            ) {
                CategoryPieChartContent(
                    modifier = modifier.padding(top = 50.dp),
                    transactions = transactions,
                    accounts = accounts,
                    categories = categories,
                    timePeriod = timePeriod,
                    selectedDataType = selectedDataType,
                    themeColors = themeColors
                )
            }
            BlurredAnimatedVisibility(
                visible = selectedDataType == TransactionDataType.EXPENSES
            ) {
                CategoryPieChartContent(
                    modifier = modifier.padding(top = 50.dp),
                    transactions = transactions,
                    accounts = accounts,
                    categories = categories,
                    timePeriod = timePeriod,
                    selectedDataType = selectedDataType,
                    themeColors = themeColors
                )
            }
            BlurredAnimatedVisibility(
                visible = selectedDataType == TransactionDataType.INCOME
            ) {
                CategoryPieChartContent(
                    modifier = modifier.padding(top = 50.dp),
                    transactions = transactions,
                    accounts = accounts,
                    categories = categories,
                    timePeriod = timePeriod,
                    selectedDataType = selectedDataType,
                    themeColors = themeColors
                )
            }
        }
    }
}

@Composable
fun CategoryPieChartContent(
    modifier: Modifier = Modifier,
    transactions: List<TransactionEntity>,
    accounts: List<AccountEntity>,
    categories: List<CategoryEntity>,
    timePeriod: TimePeriod,
    selectedDataType: TransactionDataType,
    themeColors: ColorScheme
) {
    // Filter transactions for the selected time period
    val periodTransactions by remember(transactions, timePeriod) {
        mutableStateOf(
            transactions.filter { transaction ->
                val transactionDate = transaction.date.toLocalDate()
                (transactionDate.isEqual(timePeriod.getStartDate()) ||
                        transactionDate.isEqual(timePeriod.getEndDate()) ||
                        (transactionDate.isAfter(timePeriod.getStartDate()) &&
                                transactionDate.isBefore(timePeriod.getEndDate())))
            }
        )
    }

    val currencyCode = accounts.find { it.isMainAccount }?.currencyCode ?: "usd"
    val currency = CurrencySymbols.getSymbol(currencyCode)

    // Prepare pie chart data based on the selected data type
    val pieData = remember(periodTransactions, categories, selectedDataType) {
        prepareCategoryPieData(periodTransactions, categories, selectedDataType)
    }

    // State for the interactive pie chart
    var chartData by remember { mutableStateOf(pieData) }

    // Update chart data when data changes
    LaunchedEffect(pieData) {
        chartData = pieData
    }

    Card(
        modifier = modifier
            .height(180.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = themeColors.background
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            // Show empty state if no data
            if (chartData.isEmpty() || chartData.all { it.data == 0.0 }) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No ${selectedDataType.name.lowercase()} data for this period",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = iosFont,
                        color = themeColors.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Pie chart on the left
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        PieChart(
                            modifier = Modifier.size(180.dp),
                            data = chartData,
                            onPieClick = { clickedPie ->
                                // Update selection state
                                val pieIndex = chartData.indexOf(clickedPie)
                                chartData = chartData.mapIndexed { mapIndex, pie ->
                                    pie.copy(selected = pieIndex == mapIndex)
                                }
                            },
                            selectedScale = 1.1f,
                            scaleAnimEnterSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            colorAnimEnterSpec = tween(300),
                            colorAnimExitSpec = tween(300),
                            scaleAnimExitSpec = tween(300),
                            spaceDegreeAnimExitSpec = tween(300),
                            style = Pie.Style.Stroke(width = 10.dp)
                        )
                    }

                    // Legend on the right
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(start = 8.dp),
                        contentAlignment = Alignment.TopEnd
                    ) {
                        LazyColumn {
                            items(chartData.sortedByDescending { it.data }) { pie ->
                                LegendItem(
                                    label = pie.label?: "Unknown",
                                    value = pie.data,
                                    color = pie.color,
                                    isSelected = pie.selected,
                                    currency = currency,
                                    totalAmount = chartData.sumOf { it.data },
                                    onClick = {
                                        // Update selection state
                                        val pieIndex = chartData.indexOf(pie)
                                        chartData = chartData.mapIndexed { mapIndex, p ->
                                            p.copy(selected = pieIndex == mapIndex)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LegendItem(
    label: String,
    value: Double,
    color: Color,
    currency: String = "",
    isSelected: Boolean,
    totalAmount: Double,
    onClick: () -> Unit
) {
    val percentage = (value / totalAmount * 100).toInt()

    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
            .background(
                color = if (isSelected)
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                else
                    Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp),
        horizontalArrangement = Arrangement.End,
        verticalArrangement = Arrangement.Center
    ) {
        // Color indicator
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            modifier = Modifier
                .padding(start = 8.dp, end = 5.dp),
            style = MaterialTheme.typography.bodySmall,
            fontFamily = iosFont,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Text(
                text = formatCurrency(value, currency),
                style = MaterialTheme.typography.bodySmall,
                fontFamily = iosFont,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.8f)
            )
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = iosFont,
                color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.8f)
            )
        }
    }
}

// Helper function to prepare category data for pie chart
private fun prepareCategoryPieData(
    transactions: List<TransactionEntity>,
    categories: List<CategoryEntity>,
    chartDataType: TransactionDataType
): List<Pie> {
    // Filter transactions based on the selected data type
    val filteredTransactions = when (chartDataType) {
        TransactionDataType.EXPENSES -> transactions.filter { it.mode == "Expense" }
        TransactionDataType.INCOME -> transactions.filter { it.mode == "Income" }
        TransactionDataType.ALL -> transactions.filter { it.mode == "Expense" || it.mode == "Income" }
    }

    if (filteredTransactions.isEmpty()) {
        return emptyList()
    }

    // Group transactions by category
    val categoryAmounts = filteredTransactions
        .groupBy { it.categoryId }
        .mapValues { entry ->
            entry.value.sumOf {
                if (chartDataType == TransactionDataType.ALL && it.mode == "Expense") -it.amount else it.amount
            }
        }
        .filter { it.value != 0.0 }

    // Convert to absolute values for pie chart (we care about proportions)
    val absoluteAmounts = if (chartDataType == TransactionDataType.ALL) {
        categoryAmounts.mapValues { abs(it.value) }
    } else {
        categoryAmounts
    }

    // Create pie slices
    return absoluteAmounts.map { (categoryId, amount) ->
        val category = categories.find { it.id == categoryId }

        Pie(
            label = category?.name ?: "Unknown",
            data = amount,
            color = category?.let { Color(it.boxColor) } ?: Color.Gray,
            selectedColor = category?.let { Color(it.boxColor).copy(alpha = 0.8f) } ?: Color.Gray.copy(alpha = 0.8f),
            selected = false
        )
    }
}