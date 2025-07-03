package com.ritesh.cashiro.presentation.ui.extras.components.extras

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.ritesh.cashiro.presentation.ui.theme.iosFont
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HorizontalMonthCalendar(
    modifier: Modifier = Modifier,
    selectedDate: LocalDate = LocalDate.now(),
    onMonthSelected: (LocalDate) -> Unit = {},
    content: @Composable (LocalDate) -> Unit = {}
) {
    val currentDate = remember { LocalDate.now() }
    val coroutineScope = rememberCoroutineScope()

    // Use selectedDate to set initial state
    var selectedYear by remember { mutableIntStateOf(selectedDate.year) }
    var selectedMonth by remember { mutableIntStateOf(selectedDate.monthValue - 1) }

    // Keep track of user interaction type
    var userInteractionType by remember { mutableStateOf<String?>(null) }

    // Update internal state when selectedDate changes from outside
    LaunchedEffect(selectedDate) {
        // Skip if we're handling a tab click or swipe internally
        if (userInteractionType == null) {
            selectedYear = selectedDate.year
            selectedMonth = selectedDate.monthValue - 1
        }
    }

    // Create tabPagerState with the correct initial page
    val tabPagerState = rememberPagerState(initialPage = selectedMonth) { 12 }

    // This is crucial - sync the tabPagerState with the selectedMonth when tabs are clicked
    LaunchedEffect(selectedMonth) {
        // Only synchronize if this change came from a tab click (not a swipe)
        if (userInteractionType == "tab_click" && tabPagerState.currentPage != selectedMonth) {
            tabPagerState.animateScrollToPage(selectedMonth)
//            delay(300) // Wait for animation to finish before allowing other interactions
            userInteractionType = null
        }
    }

    // Sync in the other direction - when pager changes due to swipe, update selected month
    LaunchedEffect(tabPagerState.currentPage) {
        // Only synchronize if this change came from a swipe (not a tab click)
        if (userInteractionType == "swipe" || userInteractionType == null) {
            if (selectedMonth != tabPagerState.currentPage) {
                selectedMonth = tabPagerState.currentPage
                onMonthSelected(LocalDate.of(selectedYear, selectedMonth + 1, 1))
//                delay(300) // Wait for animation to finish
                userInteractionType = null
            }
        }
    }

    // Listen for swipe gestures on the pager
    LaunchedEffect(Unit) {
        snapshotFlow { tabPagerState.currentPage }
            .collect { newPage ->
                // If we're not handling a tab click, this must be a swipe
                if (userInteractionType != "tab_click" && newPage != selectedMonth) {
                    userInteractionType = "swipe"
                }
            }
    }

    // Format month names
    val months = Month.entries.map {
        it.getDisplayName(TextStyle.FULL, Locale.getDefault())
    }

    // Calculate button visibility based on ONLY the current month selection
    // Show previous year button ONLY in January or February (months 0-1)
    val showPreviousYearButton = selectedMonth <= 1

    // Show next year button ONLY in November or December (months 10-11)
    val showNextYearButton = selectedMonth >= 10

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Month tabs with custom indicator
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            // Previous year button - corrected visibility logic
            if (showPreviousYearButton) {
                IconButton(
                    onClick = {
                        userInteractionType = "year_change"
                        coroutineScope.launch {
                            selectedYear--
                            // When going to previous year, select December
                            selectedMonth = 11
                            val newDate = LocalDate.of(selectedYear, 12, 1)
                            onMonthSelected(newDate)
                            tabPagerState.animateScrollToPage(11)
//                            delay(300)
                            userInteractionType = null
                        }
                    },
                    modifier = Modifier
                        .zIndex(4f)
                        .align(Alignment.CenterStart)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.background,
                                    MaterialTheme.colorScheme.background,
                                    Color.Transparent
                                )
                            )
                        )
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous Year",
                        tint = MaterialTheme.colorScheme.inverseSurface
                    )
                }
            }

            Spacer(modifier = Modifier
                .zIndex(3f)
                .height(60.dp)
                .width(30.dp)
                .align(Alignment.CenterStart)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background,
                            Color.Transparent
                        )
                    )
                )
            )
            Spacer(modifier = Modifier
                .zIndex(3f)
                .height(60.dp)
                .width(30.dp)
                .align(Alignment.CenterEnd)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
            )

            // Month tabs
            ScrollableTabRow(
                selectedTabIndex = selectedMonth,
                edgePadding = 20.dp,
                indicator = @Composable { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier
                            .tabIndicatorOffset(tabPositions[selectedMonth])
                            .clip(RoundedCornerShape(topEnd = 20.dp, topStart = 20.dp))
                            .animateContentSize()
                    )
                },
                divider = {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(
                                color = MaterialTheme.colorScheme.inverseSurface.copy(0.2f),
                                shape = RoundedCornerShape(10.dp)
                            )
                    )
                },
                containerColor = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxWidth()
            ) {
                Month.entries.forEachIndexed { index, month ->
                    val isSelected = selectedMonth == index
                    val isCurrentMonth = index == currentDate.monthValue - 1 && selectedYear == currentDate.year

                    Tab(
                        selected = isSelected,
                        onClick = {
                            // Ignore clicks if an interaction is already in progress
                            if (userInteractionType == null && !isSelected) {
                                // Mark that we're handling a tab click
                                userInteractionType = "tab_click"

                                // Update selected month
                                selectedMonth = index

                                // Create new date and notify parent
                                val newSelectedDate = LocalDate.of(selectedYear, index + 1, 1)
                                onMonthSelected(newSelectedDate)

                                // Use coroutineScope to ensure smooth animation
                                coroutineScope.launch {
                                    tabPagerState.animateScrollToPage(index)
//                                    delay(300) // This delay is important for synchronization
                                    userInteractionType = null
                                }
                            }
                        },
                        modifier = Modifier.padding(horizontal = 10.dp),
                        interactionSource = object : MutableInteractionSource {
                            override val interactions: Flow<Interaction> = emptyFlow()
                            override suspend fun emit(interaction: Interaction) {}
                            override fun tryEmit(interaction: Interaction): Boolean = true
                        },
                        text = {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = months[index],
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontFamily = iosFont,
                                    fontSize = 14.sp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else if (isCurrentMonth) MaterialTheme.colorScheme.primary.copy(0.5f)
                                    else MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                                    modifier = Modifier.animateContentSize()
                                )

                                // Show year indicator for non-current years
                                AnimatedVisibility(selectedYear != currentDate.year) {
                                    Text(
                                        text = selectedYear.toString(),
                                        fontSize = 10.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.inverseSurface.copy(0.4f)
                                    )
                                }
                            }
                        }
                    )
                }
            }

            // Next year button - corrected visibility logic
            if (showNextYearButton) {
                IconButton(
                    onClick = {
                        userInteractionType = "year_change"
                        coroutineScope.launch {
                            selectedYear++
                            // When going to next year, select January
                            selectedMonth = 0
                            val newDate = LocalDate.of(selectedYear, 1, 1)
                            onMonthSelected(newDate)
                            tabPagerState.animateScrollToPage(0)
//                            delay(300)
                            userInteractionType = null
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .zIndex(4f)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.background,
                                    MaterialTheme.colorScheme.background
                                )
                            )
                        )
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next Year",
                        tint = MaterialTheme.colorScheme.inverseSurface
                    )
                }
            }
        }

        // Add a HorizontalPager to handle month swiping
        HorizontalPager(
            state = tabPagerState,
            modifier = Modifier.fillMaxWidth(),
            userScrollEnabled = userInteractionType == null // Only allow swiping when no interaction is in progress
        ) { page ->
            // Render month content here
            content(LocalDate.of(selectedYear, page + 1, 1))
        }
    }
}