package com.ritesh.cashiro.presentation.ui.extras.components.charts

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.ui.graphics.lerp
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.ritesh.cashiro.data.local.entity.TransactionEntity
import com.ritesh.cashiro.data.currency.model.CurrencySymbols
import com.ritesh.cashiro.domain.utils.toLocalDate
import com.ritesh.cashiro.presentation.effects.BlurredAnimatedVisibility
import com.ritesh.cashiro.presentation.ui.theme.iosFont
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
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
import ir.ehsannarmani.compose_charts.models.StrokeStyle
import ir.ehsannarmani.compose_charts.models.ZeroLineProperties
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.abs
import kotlin.math.absoluteValue

// Chart type enum
enum class ChartType {
    LINE, BAR, HEATMAP
}

@Composable
fun TransactionChartScreen(
    transactions: List<TransactionEntity>,
    accounts: List<AccountEntity>,
    timePeriod: TimePeriod,
    selectedChartType: ChartType = ChartType.LINE
) {
    val currencyCode = accounts.find { it.isMainAccount }?.currencyCode ?: "usd"
    val currency = CurrencySymbols.getSymbol(currencyCode)
    var expanded by remember { mutableStateOf(false) }
    var currentChartType by rememberSaveable { mutableStateOf(selectedChartType) }
    val hazeStateForPopupButton = remember { HazeState() }

    // Chart type selection button
    Box(
        modifier = Modifier
            .animateContentSize()
            .fillMaxWidth()
            .padding(horizontal = 0.dp, vertical = 10.dp)
    ) {
        Row(modifier = Modifier
            .fillMaxWidth()
            .zIndex(3f),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Financial Summary",
                color = MaterialTheme.colorScheme.inverseSurface,
                fontFamily = iosFont,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.padding(start = 10.dp)
            )
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
                            color = MaterialTheme.colorScheme.primary.copy(0.8f),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = currentChartType.name.lowercase().replaceFirstChar { it.uppercase() },
                        fontFamily = iosFont,
                        lineHeight = 12.sp,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = Color.White  // Explicitly set text color
                    )
                    Icon(
                        imageVector =  Icons.Default.ArrowDropDown,
                        contentDescription = "Show chart type options",
                        modifier = Modifier.size(12.dp),
                        tint = Color.White  // Explicitly set icon color
                    )
                }
            }
        }

        // Dropdown menu
        BlurredAnimatedVisibility(
            visible = expanded,
            enter= fadeIn() + scaleIn(animationSpec = spring(
                stiffness = Spring.StiffnessLow,
                dampingRatio = Spring.DampingRatioLowBouncy),
                initialScale = 0f) + slideInVertically(initialOffsetY = { -it }) ,
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
                                noiseFactor = -1f,
                            )
                        }
                    )
                    .sizeIn(maxWidth = 80.dp, maxHeight = 280.dp)
                    .wrapContentWidth()
                    .wrapContentHeight()
                    .align(Alignment.TopEnd),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceBright.copy(0.5f),
                    contentColor = MaterialTheme.colorScheme.inverseSurface.copy(0.6f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier
                    .wrapContentWidth()
                    .heightIn(max = 280.dp)) {
                    ChartType.entries.forEach { chartType ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = chartType.name.lowercase().replaceFirstChar { it.uppercase() },
                                    fontSize = 12.sp,
                                    fontFamily = iosFont,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.fillMaxWidth()
                                ) },
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                currentChartType = chartType
                                expanded = false
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = MaterialTheme.colorScheme.inverseSurface.copy(0.7f),
                            )
                        )
                        if (chartType.name == "LINE" || chartType.name == "BAR") {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 10.dp))
                        }
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth().haze(hazeStateForPopupButton)){
            BlurredAnimatedVisibility(
                visible = currentChartType == ChartType.LINE
            ) {
                TransactionLineChart(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 35.dp),
                    transactions = transactions,
                    currency = currency,
                    timePeriod = timePeriod
                )
            }
            BlurredAnimatedVisibility(
                visible = currentChartType == ChartType.BAR
            ) {
                TransactionBarChart(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 35.dp),
                    transactions = transactions,
                    currency = currency,
                    timePeriod = timePeriod
                )
            }
            BlurredAnimatedVisibility(
                visible = currentChartType == ChartType.HEATMAP
            ) {
                TransactionHeatMap(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 35.dp),
                    transactions = transactions,
                    timePeriod = timePeriod
                )
            }
        }
    }
}
@Composable
fun TransactionLineChart(
    modifier: Modifier = Modifier,
    transactions: List<TransactionEntity>,
    currency: String,
    timePeriod: TimePeriod
) {
    val themeColors = MaterialTheme.colorScheme

    // Use remember to properly cache the filtered transactions and calculated data
    val periodTransactions by remember(transactions, timePeriod) {
        mutableStateOf(
            transactions.filter { transaction ->
                val transactionDate = transaction.date.toLocalDate()
                (transactionDate.isEqual(timePeriod.getStartDate()) ||
                        transactionDate.isEqual(timePeriod.getEndDate()) ||
                        (transactionDate.isAfter(timePeriod.getStartDate()) &&
                                transactionDate.isBefore(timePeriod.getEndDate()))) &&
                        transaction.mode != "Transfer"
            }
        )
    }

    // Prepare data based on time period using remember to ensure it's properly recalculated
    val result = remember(periodTransactions, timePeriod) {
        prepareCashFlowData(periodTransactions, timePeriod)
    }

    val cashFlowData = result.first
    val labels = result.second

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
            // If no transactions, show message
            if (cashFlowData.all { it == 0.0 }) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No transactions in this period",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = iosFont,
                        color = themeColors.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                LineChart(
                    modifier = Modifier.fillMaxSize(),
                    data = remember(cashFlowData) {
                        listOf(
                            Line(
                                label = "Cash Flow",
                                values = cashFlowData,
                                color = SolidColor(themeColors.primary),
                                firstGradientFillColor = themeColors.primary.copy(alpha = 0.3f),
                                secondGradientFillColor = Color.Transparent,
                                strokeAnimationSpec = tween(1500, easing = EaseInOutCubic),
                                gradientAnimationDelay = 750,
                                drawStyle = DrawStyle.Stroke(width = 2.dp),
                                curvedEdges = true,
                                dotProperties = DotProperties(
                                    enabled = true,
                                    color = SolidColor(themeColors.primary),
                                    strokeWidth = 3.dp,
                                    radius = 4.dp,
                                    strokeColor = SolidColor(themeColors.surface)
                                )
                            )
                        )
                    },
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

// Helper function to prepare cash flow chart data based on time period
private fun prepareCashFlowData(
    transactions: List<TransactionEntity>,
    timePeriod: TimePeriod
): Pair<List<Double>, List<String>> {
    // First filter out all Transfer transactions before sending to specific period functions
    val nonTransferTransactions = transactions.filter { it.mode != "Transfer" }

    return when (timePeriod) {
        is WeekPeriod -> prepareWeekCashFlowData(nonTransferTransactions, timePeriod)
        is MonthPeriod -> prepareMonthCashFlowData(nonTransferTransactions, timePeriod)
        is YearPeriod -> prepareYearCashFlowData(nonTransferTransactions, timePeriod)
        else -> Pair(emptyList(), emptyList())
    }
}

// Prepare cash flow data for week view
private fun prepareWeekCashFlowData(
    transactions: List<TransactionEntity>,
    timePeriod: WeekPeriod
): Pair<List<Double>, List<String>> {
    val startDate = timePeriod.getStartDate()
    val cashFlowByDay = mutableListOf<Double>()
    val dateLabels = mutableListOf<String>()
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd")

    for (i in 0..6) {
        val currentDate = startDate.plusDays(i.toLong())
        val formattedDate = dateFormatter.format(currentDate)
        dateLabels.add(formattedDate)

        val dayTransactions = transactions.filter { transaction ->
            val transactionDate = transaction.date.toLocalDate()
            transactionDate.isEqual(currentDate)
        }

        val dayCashFlow = dayTransactions.sumOf {
            if (it.mode == "Income") it.amount else -it.amount
        }

        cashFlowByDay.add(dayCashFlow)
    }

    return Pair(cashFlowByDay, dateLabels)
}

// Prepare cash flow data for month view
private fun prepareMonthCashFlowData(
    transactions: List<TransactionEntity>,
    timePeriod: MonthPeriod
): Pair<List<Double>, List<String>> {
    val startDate = timePeriod.getStartDate()
    val yearMonth = YearMonth.of(startDate.year, startDate.month)
    val daysInMonth = yearMonth.lengthOfMonth()
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd")

    // We'll split the month into roughly 5 equal parts for better display
    val dayIncrement = daysInMonth / 5
    val cashFlowBySegment = mutableListOf<Double>()
    val dateLabels = mutableListOf<String>()

    for (i in 0 until 5) {
        val segmentStartDay = 1 + (i * dayIncrement)
        val segmentEndDay = if (i == 4) daysInMonth else segmentStartDay + dayIncrement - 1
        val segmentStartDate = LocalDate.of(startDate.year, startDate.month, segmentStartDay)
        val segmentEndDate = LocalDate.of(startDate.year, startDate.month, segmentEndDay)

        // Use the start date of each segment as the label
        dateLabels.add(dateFormatter.format(segmentStartDate))

        val segmentTransactions = transactions.filter { transaction ->
            val transactionDate = transaction.date.toLocalDate()
            (transactionDate.isEqual(segmentStartDate) || transactionDate.isAfter(segmentStartDate)) &&
                    (transactionDate.isEqual(segmentEndDate) || transactionDate.isBefore(segmentEndDate))
        }

        val segmentCashFlow = segmentTransactions.sumOf {
            if (it.mode == "Income") it.amount else -it.amount
        }

        cashFlowBySegment.add(segmentCashFlow)
    }

    return Pair(cashFlowBySegment, dateLabels)
}

// Prepare cash flow data for year view
private fun prepareYearCashFlowData(
    transactions: List<TransactionEntity>,
    timePeriod: YearPeriod
): Pair<List<Double>, List<String>> {
    val monthLabels = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val cashFlowByMonth = mutableListOf<Double>()

    for (monthIndex in 0..11) {
        val monthStart = LocalDate.of(timePeriod.getStartDate().year, monthIndex + 1, 1)
        val monthEnd = monthStart.plusMonths(1).minusDays(1)

        val monthTransactions = transactions.filter { transaction ->
            val transactionDate = transaction.date.toLocalDate()
            (transactionDate.isEqual(monthStart) || transactionDate.isAfter(monthStart)) &&
                    (transactionDate.isEqual(monthEnd) || transactionDate.isBefore(monthEnd))
        }

        val monthCashFlow = monthTransactions.sumOf {
            if (it.mode == "Income") it.amount else -it.amount
        }

        cashFlowByMonth.add(monthCashFlow)
    }

    return Pair(cashFlowByMonth, monthLabels)
}

fun formatCurrency(value: Double, currencySymbol: String = "$"): String {
    val absValue = kotlin.math.abs(value)
    val prefix = if (value < 0) "-$currencySymbol" else currencySymbol

    return when {
        // Handle millions (1M+)
        absValue >= 1_000_000 -> {
            val millions = absValue / 1_000_000
            if (millions >= 10) {
                // For 10M+ round to nearest million
                "$prefix ${millions.toLong()}M"
            } else {
                // For 1M to 10M, show one decimal place
                "$prefix ${String.format("%.1f", millions)}M"
            }
        }
        // Handle thousands (1k+)
        absValue >= 10_000 -> {
            val thousands = absValue / 1_000
            "$prefix ${thousands.toLong()}k"
        }
        // Handle regular values with appropriate decimal places
        absValue >= 100 -> "${prefix}${absValue.toLong()}"
        else -> {
            // For values less than 100, show up to 2 decimal places but only if needed
            val formatted = absValue.toBigDecimal()
                .setScale(2, java.math.RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString()
            "$prefix $formatted"
        }
    }
}
@Composable
fun TransactionBarChart(
    modifier: Modifier = Modifier,
    transactions: List<TransactionEntity>,
    currency: String,
    timePeriod: TimePeriod
) {
    val themeColors = MaterialTheme.colorScheme

    // Filter transactions based on time period and exclude transfers
    val periodTransactions by remember(transactions, timePeriod) {
        mutableStateOf(
            transactions.filter { transaction ->
                val transactionDate = transaction.date.toLocalDate()
                (transactionDate.isEqual(timePeriod.getStartDate()) ||
                        transactionDate.isEqual(timePeriod.getEndDate()) ||
                        (transactionDate.isAfter(timePeriod.getStartDate()) &&
                                transactionDate.isBefore(timePeriod.getEndDate()))) &&
                        transaction.mode != "Transfer"
            }
        )
    }

    // Prepare data based on time period
    val result = remember(periodTransactions, timePeriod, themeColors) {
        prepareCashFlowDataForColumnChart(periodTransactions, timePeriod, themeColors)
    }

    val columnData = result.first
    val maxValue = result.second
    val minValue = result.third

    Card(
        modifier = modifier
            .height(280.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = themeColors.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // If no transactions, show message
            if (columnData.isEmpty() || columnData.all { it.values.isEmpty() }) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No transactions in this period",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = iosFont,
                        color = themeColors.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                ColumnChart(
                    modifier = Modifier.fillMaxSize(),
                    data = columnData,
                    maxValue = maxValue,
                    minValue = minValue,
                    barProperties = BarProperties(
                        cornerRadius = Bars.Data.Radius.Rectangle(topLeft = 6.dp, topRight = 6.dp, bottomLeft = 6.dp, bottomRight = 6.dp),
                        spacing = 5.dp,
                        thickness = 10.dp
                    ),
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
                    labelProperties = LabelProperties(
                        enabled = true,
                        textStyle = androidx.compose.ui.text.TextStyle.Default.copy(
                            fontSize = 10.sp,
                            color = themeColors.inverseSurface.copy(0.5f),
                            fontFamily = iosFont,
                            textAlign = TextAlign.End),
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
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                )
            }
        }
    }
}

// Helper function to prepare column chart data based on time period
private fun prepareCashFlowDataForColumnChart(
    transactions: List<TransactionEntity>,
    timePeriod: TimePeriod,
    themeColors: ColorScheme,
): Triple<List<Bars>, Double, Double> {
    // Filter out all Transfer transactions
    val nonTransferTransactions = transactions.filter { it.mode != "Transfer" }

    return when (timePeriod) {
        is WeekPeriod -> prepareWeekColumnData(nonTransferTransactions, timePeriod, themeColors)
        is MonthPeriod -> prepareMonthColumnData(nonTransferTransactions, timePeriod, themeColors)
        is YearPeriod -> prepareYearColumnData(nonTransferTransactions, timePeriod, themeColors)
        else -> Triple(emptyList(), 0.0, 0.0)
    }
}

// Prepare column data for week view
private fun prepareWeekColumnData(
    transactions: List<TransactionEntity>,
    timePeriod: WeekPeriod,
    themeColors: ColorScheme,
): Triple<List<Bars>, Double, Double> {
    val startDate = timePeriod.getStartDate()
    val columnData = mutableListOf<Bars>()
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd")

    var maxValue = 0.0
    var minValue = 0.0

    for (i in 0..6) {
        val currentDate = startDate.plusDays(i.toLong())
        val formattedDate = dateFormatter.format(currentDate)

        val dayTransactions = transactions.filter { transaction ->
            val transactionDate = transaction.date.toLocalDate()
            transactionDate.isEqual(currentDate)
        }

        // Separate income and expense transactions
        val incomeAmount = dayTransactions
            .filter { it.mode == "Income" }
            .sumOf { it.amount }

        val expenseAmount = dayTransactions
            .filter { it.mode == "Expense" }
            .sumOf { it.amount }

        // Update max and min values for chart scaling
        maxValue = maxOf(maxValue, incomeAmount)
        minValue = minOf(minValue, -expenseAmount)

        val barValues = mutableListOf<Bars.Data>()

        if (incomeAmount > 0) {
            barValues.add(
                Bars.Data(
                    label = "Income",
                    value = incomeAmount,
                    color = SolidColor(themeColors.tertiary.copy(alpha = 0.7f))
                )
            )
        }

        if (expenseAmount > 0) {
            barValues.add(
                Bars.Data(
                    label = "Expense",
                    value = -expenseAmount,
                    color = SolidColor(themeColors.outline.copy(alpha = 0.7f))
                )
            )
        }

        columnData.add(
            Bars(
                label = formattedDate,
                values = barValues
            )
        )
    }

    // Add 20% padding to max and min values for better visualization
    maxValue *= 1.2
    minValue *= 1.2

    return Triple(columnData, maxValue, minValue)
}

// Prepare column data for month view
private fun prepareMonthColumnData(
    transactions: List<TransactionEntity>,
    timePeriod: MonthPeriod,
    themeColors: ColorScheme
): Triple<List<Bars>, Double, Double> {
    val startDate = timePeriod.getStartDate()
    val yearMonth = YearMonth.of(startDate.year, startDate.month)
    val daysInMonth = yearMonth.lengthOfMonth()
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd")

    val columnData = mutableListOf<Bars>()
    var maxValue = 0.0
    var minValue = 0.0

    // Split the month into roughly 5 equal parts for better display
    val dayIncrement = daysInMonth / 5

    for (i in 0 until 5) {
        val segmentStartDay = 1 + (i * dayIncrement)
        val segmentEndDay = if (i == 4) daysInMonth else segmentStartDay + dayIncrement - 1
        val segmentStartDate = LocalDate.of(startDate.year, startDate.month, segmentStartDay)
        val segmentEndDate = LocalDate.of(startDate.year, startDate.month, segmentEndDay)

        // Use the start date of each segment as the label
        val label = dateFormatter.format(segmentStartDate)

        val segmentTransactions = transactions.filter { transaction ->
            val transactionDate = transaction.date.toLocalDate()
            (transactionDate.isEqual(segmentStartDate) || transactionDate.isAfter(segmentStartDate)) &&
                    (transactionDate.isEqual(segmentEndDate) || transactionDate.isBefore(segmentEndDate))
        }

        // Separate income and expense transactions
        val incomeAmount = segmentTransactions
            .filter { it.mode == "Income" }
            .sumOf { it.amount }

        val expenseAmount = segmentTransactions
            .filter { it.mode == "Expense" }
            .sumOf { it.amount }

        // Update max and min values for chart scaling
        maxValue = maxOf(maxValue, incomeAmount)
        minValue = minOf(minValue, -expenseAmount)

        val barValues = mutableListOf<Bars.Data>()

        if (incomeAmount > 0) {
            barValues.add(
                Bars.Data(
                    label = "Income",
                    value = incomeAmount,
                    color = SolidColor(themeColors.tertiary.copy(alpha = 0.7f))
                )
            )
        }

        if (expenseAmount > 0) {
            barValues.add(
                Bars.Data(
                    label = "Expense",
                    value = -expenseAmount,
                    color = SolidColor(themeColors.outline.copy(alpha = 0.7f))
                )
            )
        }

        columnData.add(
            Bars(
                label = label,
                values = barValues
            )
        )
    }

    // Add 20% padding to max and min values for better visualization
    maxValue *= 1.2
    minValue *= 1.2

    return Triple(columnData, maxValue, minValue)
}

// Prepare column data for year view
private fun prepareYearColumnData(
    transactions: List<TransactionEntity>,
    timePeriod: YearPeriod,
    themeColors: ColorScheme
): Triple<List<Bars>, Double, Double> {
    val columnData = mutableListOf<Bars>()
    val monthLabels = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    var maxValue = 0.0
    var minValue = 0.0

    for (monthIndex in 0..11) {
        val monthStart = LocalDate.of(timePeriod.getStartDate().year, monthIndex + 1, 1)
        val monthEnd = monthStart.plusMonths(1).minusDays(1)

        val monthTransactions = transactions.filter { transaction ->
            val transactionDate = transaction.date.toLocalDate()
            (transactionDate.isEqual(monthStart) || transactionDate.isAfter(monthStart)) &&
                    (transactionDate.isEqual(monthEnd) || transactionDate.isBefore(monthEnd))
        }

        // Separate income and expense transactions
        val incomeAmount = monthTransactions
            .filter { it.mode == "Income" }
            .sumOf { it.amount }

        val expenseAmount = monthTransactions
            .filter { it.mode == "Expense" }
            .sumOf { it.amount }

        // Update max and min values for chart scaling
        maxValue = maxOf(maxValue, incomeAmount)
        minValue = minOf(minValue, -expenseAmount)

        val barValues = mutableListOf<Bars.Data>()

        if (incomeAmount > 0) {
            barValues.add(
                Bars.Data(
                    label = "Income",
                    value = incomeAmount,
                    color = SolidColor(themeColors.tertiary.copy(alpha = 0.7f))
                )
            )
        }

        if (expenseAmount > 0) {
            barValues.add(
                Bars.Data(
                    label = "Expense",
                    value = -expenseAmount,
                    color = SolidColor(themeColors.outline.copy(alpha = 0.7f))
                )
            )
        }

        columnData.add(
            Bars(
                label = monthLabels[monthIndex],
                values = barValues
            )
        )
    }

    // Add 20% padding to max and min values for better visualization
    maxValue *= 1.2
    minValue *= 1.2

    return Triple(columnData, maxValue, minValue)
}
@Composable
fun TransactionHeatMap(
    modifier: Modifier = Modifier,
    transactions: List<TransactionEntity>,
    timePeriod: TimePeriod
) {
    // Filter transactions based on the selected time period
    val filteredTransactions = filterTransactionsByTimePeriodForHeatMap(transactions, timePeriod)

    if (filteredTransactions.isEmpty()) {
        // Show empty state
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(350.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.medium
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No transactions for this period",
                fontWeight = FontWeight.Medium,
                fontFamily = iosFont,
                color = MaterialTheme.colorScheme.inverseSurface
            )
        }
        return
    }

    // Group transactions based on time period type
    val groupedData = when (timePeriod) {
        is WeekPeriod -> groupTransactionsByDayForHeatMap(filteredTransactions)
        is MonthPeriod -> groupTransactionsByDayForHeatMap(filteredTransactions)
        is YearPeriod -> groupTransactionsByMonthForHeatMap(filteredTransactions)
        else -> groupTransactionsByDayForHeatMap(filteredTransactions)
    }

    // Ensure we have data after grouping
    if (groupedData.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(350.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.medium
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No chart data available for this period",
                fontWeight = FontWeight.Medium,
                fontFamily = iosFont,
                color = MaterialTheme.colorScheme.inverseSurface
            )
        }
        return
    }

    // Get min and max transaction amounts to create color scale
    val minAmount = groupedData.values.minOfOrNull { it.toFloat() } ?: 0f
    val maxAmount = groupedData.values.maxOfOrNull { it.toFloat().absoluteValue } ?: 100f
    val range = maxOf(maxAmount, minAmount.absoluteValue).coerceAtLeast(1f) // Ensure non-zero range

    // Animation state
    var animationPlayed by remember { mutableStateOf(false) }
    val animationProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "HeatMap Animation"
    )

    // Trigger animation when the component enters composition
    LaunchedEffect(key1 = groupedData) {
        animationPlayed = false
        delay(10) // Small delay before starting animation
        animationPlayed = true
    }

    // Create structured data based on time period
    val structuredData = prepareStructuredData(timePeriod, groupedData)

    // Chart layout
    Column(modifier = modifier.fillMaxWidth()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    when (timePeriod) {
                        is WeekPeriod -> Modifier.height(140.dp)
                        is MonthPeriod -> Modifier.height(350.dp)
                        is YearPeriod -> Modifier.height(325.dp)
                        else -> Modifier.height(350.dp)
                    }
                ),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Legend on top
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    MaterialTheme.colorScheme.tertiary,
                                    RoundedCornerShape(8.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Income",
                            fontSize = 10.sp,
                            fontFamily = iosFont,
                            color = MaterialTheme.colorScheme.inverseSurface.copy(0.7f)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    MaterialTheme.colorScheme.outline,
                                    RoundedCornerShape(8.dp)
                                )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Expense",
                            fontSize = 10.sp,
                            fontFamily = iosFont,
                            color = MaterialTheme.colorScheme.inverseSurface.copy(0.7f)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Darker = Higher Amount",
                            fontSize = 10.sp,
                            fontFamily = iosFont,
                            color = MaterialTheme.colorScheme.inverseSurface.copy(0.7f)
                        )
                    }
                }

                // HeatMap grid based on time period
                when (timePeriod) {
                    is WeekPeriod -> WeekHeatMap(structuredData, range, animationProgress)
                    is MonthPeriod -> MonthHeatMap(structuredData, range, animationProgress, timePeriod)
                    is YearPeriod -> YearHeatMap(structuredData, range, animationProgress)
                    else -> {
                        Text(
                            text = "Unsupported time period for heatmap",
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.inverseSurface,
                            fontFamily = iosFont
                        )
                    }
                }
            }
        }
    }
}
// Improved color calculation function
@Composable
fun getColorForValue(value: Float, maxRange: Float): Color {
    return when {
        value > 0 -> {
            // Use SuccessColor for maximum positive values, create gradient for lower values
            val intensity = (value / maxRange).coerceIn(0.3f, 1f)

            // Start with a lighter green and blend toward full SuccessColor
            val lightGreen = Color(0xFF8CAA82) // Light green color

            // Interpolate between light green and success color based on intensity
            lerp(
                start = lightGreen,
                stop = MaterialTheme.colorScheme.tertiary,
                fraction = intensity
            )
        }
        value < 0 -> {
            // Use ErrorColor for maximum negative values, create gradient for lower values
            val intensity = (value.absoluteValue / maxRange).coerceIn(0.3f, 1f)

            // Start with a lighter red and blend toward full ErrorColor
            val lightRed = Color(0xFFBF8E92) // Light red color

            // Interpolate between light red and error color based on intensity
            lerp(
                start = lightRed,
                stop = MaterialTheme.colorScheme.outline,
                fraction = intensity
            )
        }
        else -> {
            // Neutral color for zero values - slightly visible
            MaterialTheme.colorScheme.surfaceBright.copy(alpha = 0.2f)
        }
    }
}

// Helper function to prepare structured data based on time period
fun prepareStructuredData(timePeriod: TimePeriod, groupedData: Map<String, Double>): Map<String, Float> {
    return when (timePeriod) {
        is WeekPeriod -> {
            // For week, match day of week properly
            val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            val start = timePeriod.getStartDate()

            // Create a map of each day of the week to its corresponding date string
            dayNames.mapIndexed { index, dayName ->
                val date = start.plusDays(index.toLong())
                val formattedDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

                // Look for the exact date in grouped data, or use the day name as fallback
                val value = groupedData[formattedDate]?.toFloat()
                    ?: groupedData[dayName]?.toFloat()
                    ?: 0f

                dayName to value
            }.toMap()
        }
        is MonthPeriod -> {
            // For month, map day numbers to values
            val startDate = timePeriod.getStartDate()
            val daysInMonth = startDate.lengthOfMonth()
            val firstDayOfMonth = startDate.dayOfWeek.value // 1 for Monday, 7 for Sunday

            // Create a map of day numbers to values
            (1..daysInMonth).associate { day ->
                val date = startDate.withDayOfMonth(day)
                val formattedDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                val dayStr = day.toString()

                dayStr to (groupedData[formattedDate]?.toFloat()
                    ?: groupedData[dayStr]?.toFloat()
                    ?: 0f)
            }
        }
        is YearPeriod -> {
            // For year, map month names to values
            val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

            monthNames.associate { month ->
                val monthValue = groupedData.entries.find {
                    it.key.contains(month, ignoreCase = true) ||
                            it.key.contains(getMonthNumber(month), ignoreCase = true)
                }?.value?.toFloat() ?: 0f

                month to monthValue
            }
        }
        else -> groupedData.mapValues { it.value.toFloat() }
    }
}

// Helper function to get month number from name
fun getMonthNumber(monthName: String): String {
    return when (monthName.lowercase().take(3)) {
        "jan" -> "01"
        "feb" -> "02"
        "mar" -> "03"
        "apr" -> "04"
        "may" -> "05"
        "jun" -> "06"
        "jul" -> "07"
        "aug" -> "08"
        "sep" -> "09"
        "oct" -> "10"
        "nov" -> "11"
        "dec" -> "12"
        else -> ""
    }
}
@Composable
fun WeekHeatMap(
    gridData: Map<String, Float>,
    maxRange: Float,
    animationProgress: Float
) {
    // Weekly view - simple 7-day layout
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 30.dp, bottom = 10.dp, start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        days.forEach { day ->
            val value = gridData[day] ?: 0f
            val cellSize = 38.dp

            Column(
                modifier = Modifier
                    .padding(top = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(cellSize)
                        .padding(2.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            getColorForValue(value, maxRange)
                                .copy(alpha = 0.2f + (0.8f * animationProgress))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (value != 0f) {
                        Text(
                            text = "$${abs(value).toInt()}",
                            color = if (value > 0) Color.White else Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = day,
                    color = MaterialTheme.colorScheme.inverseSurface.copy(0.7f),
                    fontSize = 10.sp,
                    fontFamily = iosFont,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun MonthHeatMap(
    gridData: Map<String, Float>,
    maxRange: Float,
    animationProgress: Float,
    monthPeriod: MonthPeriod
) {
    // Get the first day of the month and determine its day of week
    val firstDayOfMonth = monthPeriod.getStartDate().dayOfWeek.value % 7 // Convert to 0-indexed where 0 is Sunday
    val daysInMonth = monthPeriod.getStartDate().lengthOfMonth()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(top = 20.dp)
    ) {
        // Day headers (Sun-Sat)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                Text(
                    text = day,
                    color = MaterialTheme.colorScheme.inverseSurface.copy(0.7f),
                    fontSize = 10.sp,
                    fontFamily = iosFont,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Calendar grid
        val weeks = 6 // Maximum weeks to display
        var dayCounter = 1

        repeat(weeks) { weekIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(7) { dayIndex ->
                    // Calculate if this position should show a day from the current month
                    val showDay = when(weekIndex) {
                        0 -> dayIndex >= firstDayOfMonth
                        else -> dayCounter <= daysInMonth
                    }

                    val dayKey = if (showDay) dayCounter.toString() else ""
                    val value = if (showDay) gridData[dayKey] ?: 0f else 0f

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (showDay) getColorForValue(
                                    value,
                                    maxRange
                                ).copy(alpha = animationProgress)
                                else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (showDay) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = dayCounter.toString(),
                                    color = if (value != 0f) Color.White else MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                if (value != 0f) {
                                    Text(
                                        text = "$${abs(value).toInt()}",
                                        color = Color.White,
                                        fontSize = 7.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // Increment day counter for next cell
                            if (showDay) dayCounter++
                        }
                    }
                }
            }

            // Break early if we've shown all days
            if (dayCounter > daysInMonth) return
        }
    }
}

@Composable
fun YearHeatMap(
    gridData: Map<String, Float>,
    maxRange: Float,
    animationProgress: Float
) {
    // Year view by quarters and months
    val quarters = listOf(
        "Q1" to listOf("Jan", "Feb", "Mar"),
        "Q2" to listOf("Apr", "May", "Jun"),
        "Q3" to listOf("Jul", "Aug", "Sep"),
        "Q4" to listOf("Oct", "Nov", "Dec")
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .padding(top = 20.dp)
    ) {
        quarters.forEach { (quarter, months) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = quarter,
                    color = MaterialTheme.colorScheme.inverseSurface,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = iosFont,
                    modifier = Modifier.width(30.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    months.forEach { month ->
                        val value = gridData[month] ?: 0f

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(4f)
                                    .padding(horizontal = 4.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        getColorForValue(value, maxRange)
                                            .copy(alpha = 0.3f + (0.7f * animationProgress))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (value != 0f) {
                                    Text(
                                        text = "$${abs(value).toInt()}",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = month,
                                color = MaterialTheme.colorScheme.inverseSurface.copy(0.7f),
                                fontSize = 10.sp,
                                fontFamily = iosFont,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
// Function to filter transactions by time period
fun filterTransactionsByTimePeriodForHeatMap(
    transactions: List<TransactionEntity>,
    timePeriod: TimePeriod
): List<TransactionEntity> {
    val startDate = timePeriod.getStartDate()
    val endDate = timePeriod.getEndDate()

    return transactions.filter { transaction ->
        val transactionDate = transaction.date.toLocalDate()
        // Include transactions that fall within the period's date range (inclusive)
        !transactionDate.isBefore(startDate) && !transactionDate.isAfter(endDate)
    }
}

// Function to group transactions by day
fun groupTransactionsByDayForHeatMap(transactions: List<TransactionEntity>): Map<String, Double> {
    val groupedByDay = transactions.groupBy { transaction ->
        // Get day in format "yyyy-MM-dd" for precise matching
        transaction.date.toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }

    return groupedByDay.mapValues { (_, transactionsForDay) ->
        // Sum amounts, considering positive for income and negative for expenses
        transactionsForDay.sumOf { transaction ->
            when (transaction.mode) {
                "Income" -> transaction.amount
                "Expense" -> -transaction.amount // Make expenses negative
                else -> 0.0
            }
        }
    }
}

// Function to group transactions by month
fun groupTransactionsByMonthForHeatMap(transactions: List<TransactionEntity>): Map<String, Double> {
    val groupedByMonth = transactions.groupBy { transaction ->
        // Get month short name (Jan, Feb, etc.)
        transaction.date.toLocalDate().month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
    }

    return groupedByMonth.mapValues { (_, transactionsForMonth) ->
        // Sum amounts, considering positive for income and negative for expenses
        transactionsForMonth.sumOf { transaction ->
            when (transaction.mode) {
                "Income" -> transaction.amount
                "Expense" -> -transaction.amount // Make expenses negative
                else -> 0.0
            }
        }
    }
}