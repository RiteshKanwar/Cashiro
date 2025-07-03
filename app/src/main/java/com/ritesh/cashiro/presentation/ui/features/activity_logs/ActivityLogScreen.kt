package com.ritesh.cashiro.presentation.ui.features.activity_logs

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.ritesh.cashiro.data.currency.model.CurrencySymbols
import com.ritesh.cashiro.data.local.entity.AccountEntity
import com.ritesh.cashiro.presentation.effects.VerticalStretchedOverscroll
import com.ritesh.cashiro.presentation.ui.extras.components.charts.formatCurrency
import com.ritesh.cashiro.presentation.ui.extras.components.extras.CustomTitleTopAppBar
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenEvent
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenState
import com.ritesh.cashiro.presentation.ui.theme.iosFont
import com.ritesh.cashiro.R
import com.ritesh.cashiro.domain.utils.ActivityLogFilterUtils
import com.ritesh.cashiro.domain.utils.formattedTimeText
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalSharedTransitionApi::class
)
@Composable
fun SharedTransitionScope.ActivityLogScreen(
    activityLogViewModel: ActivityLogViewModel,
    accountUiState: AccountScreenState,
    onAccountEvent: (AccountScreenEvent) -> Unit,
    screenTitle: String,
    navController: NavController,
    onBackClicked: () -> Unit,
    previousScreenTitle: String,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    val activityState by activityLogViewModel.state.collectAsState()
    val accounts = accountUiState.accounts

    var selectedActivities by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var isInSelectionMode by remember { mutableStateOf(false) }
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }

    // Filter state
    var filterState by remember { mutableStateOf(ActivityLogFilterState()) }

    val coroutineScope = rememberCoroutineScope()
    val scrollOverScrollState = rememberLazyListState()
    val overscrollEffect = remember(coroutineScope) { VerticalStretchedOverscroll(coroutineScope) }

    // Scaffold State for Scroll Behavior
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scrollBehaviorSmall = TopAppBarDefaults.pinnedScrollBehavior()

    // Apply filters to activities
    val filteredActivities = remember(activityState.activities, filterState) {
        ActivityLogFilterUtils.applyFilters(
            activities = activityState.activities,
            filterState = filterState,
            accounts = accounts
        )
    }

    // Function to toggle selection of an activity
    val toggleActivitySelection: (ActivityLogEntry) -> Unit = { activity ->
        if (isInSelectionMode) {
            selectedActivities = if (selectedActivities.contains(activity.id)) {
                val updatedSelection = selectedActivities - activity.id
                if (updatedSelection.isEmpty()) {
                    isInSelectionMode = false
                }
                updatedSelection
            } else {
                selectedActivities + activity.id
            }
        }
    }

    val onActivityLongPress: (ActivityLogEntry) -> Unit = { activity ->
        isInSelectionMode = true
        selectedActivities = selectedActivities + activity.id
    }

    // Function to clear all selections and exit selection mode
    val clearSelections = {
        selectedActivities = emptySet()
        isInSelectionMode = false
    }

    val deleteSelectedActivities = {
        showDeleteConfirmationDialog = true
    }

    val selectAllActivities = {
        if (!isInSelectionMode) {
            isInSelectionMode = true
        }
        selectedActivities = filteredActivities.map { it.id }.toSet()
    }

    // Modified BackHandler to handle selection mode
    BackHandler(enabled = isInSelectionMode) {
        clearSelections()
    }

    LaunchedEffect(key1 = Unit) {
        onAccountEvent(AccountScreenEvent.FetchAllAccounts)
        activityLogViewModel.onEvent(ActivityLogEvent.LoadActivities)
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
                selectedCount = selectedActivities.size,
                onClearSelection = clearSelections,
                onDeleteSelected = deleteSelectedActivities,
                onSelectAll = selectAllActivities,
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Filter and sorting controls
            ActivityLogControls(
                filterState = filterState,
                onFilterChange = { newFilterState ->
                    filterState = newFilterState
                },
                activities = filteredActivities,
                onClearAll = {
                    activityLogViewModel.onEvent(ActivityLogEvent.ClearAllActivities)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            )

            if (activityState.isLoading && activityState.activities.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (activityState.error != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = activityState.error!!,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(
                            onClick = { activityLogViewModel.onEvent(ActivityLogEvent.RefreshActivities) },
                            modifier = Modifier.padding(top = 16.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }
            } else if (filteredActivities.isEmpty()) {
                EmptyActivityLog()
            } else {
                ActivityLogDisplayList(
                    activities = filteredActivities,
                    accounts = accounts,
                    themeColors = MaterialTheme.colorScheme,
                    selectedActivityIds = selectedActivities,
                    onActivityClick = toggleActivitySelection,
                    onActivityLongPress = onActivityLongPress,
                    isInSelectionMode = isInSelectionMode,
                    scrollOverScrollState = scrollOverScrollState,
                    overscrollEffect = overscrollEffect,
                    navController = navController,
                    animatedVisibilityScope = animatedVisibilityScope,
                    modifier = Modifier.padding(horizontal = 20.dp),
                    accountUiState = accountUiState,
                    onAccountEvent = onAccountEvent,
                )
            }
        }
    }

    if (showDeleteConfirmationDialog) {
        ActivityLogDeletionConfirmationDialog(
            showDialog = showDeleteConfirmationDialog,
            activityCount = selectedActivities.size,
            onConfirm = {
                activityLogViewModel.onEvent(
                    ActivityLogEvent.DeleteActivities(selectedActivities.toList())
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
fun SharedTransitionScope.ActivityLogDisplayList(
    modifier: Modifier = Modifier,
    activities: List<ActivityLogEntry>,
    accounts: List<AccountEntity>,
    accountUiState: AccountScreenState,
    onAccountEvent: (AccountScreenEvent) -> Unit,
    themeColors: ColorScheme,
    selectedActivityIds: Set<Int> = emptySet(),
    onActivityClick: (ActivityLogEntry) -> Unit = {},
    onActivityLongPress: (ActivityLogEntry) -> Unit = {},
    isInSelectionMode: Boolean = false,
    scrollOverScrollState: LazyListState,
    overscrollEffect: VerticalStretchedOverscroll,
    navController: NavController,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    if (activities.isEmpty()) {
        EmptyActivityLog(modifier)
        return
    }

    // Group activities by date for better organization
    val groupedActivities = activities.groupBy { activity ->
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = activity.timestamp
        Triple(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }.toSortedMap(compareByDescending { (year, month, day) ->
        LocalDate.of(year, month + 1, day)
    })

    LazyColumn(
        state = scrollOverScrollState,
        userScrollEnabled = true,
        modifier = modifier
            .fillMaxWidth()
            .overscroll(overscrollEffect),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Iterate through each date group
        groupedActivities.forEach { (dateGroup, activitiesForDate) ->
            val (year, month, day) = dateGroup
            val date = LocalDate.of(year, month + 1, day)
            val dateFormat = date.format(DateTimeFormatter.ofPattern("dd EEEE, MMMM"))

            // Display date header once per group
            stickyHeader(key = "date_${year}_${month}_${day}") {
                ActivityLogDateHeader(
                    date = dateFormat,
                    activityCount = activitiesForDate.size,
                    themeColors = themeColors
                )
            }

            // Display activities for this date
            itemsIndexed(
                items = activitiesForDate.sortedByDescending { it.timestamp },
                key = { _, activity -> activity.id }
            ) { index, activity ->
                val isItemSelected = selectedActivityIds.contains(activity.id)

                ActivityLogCard(
                    activity = activity,
                    accounts = accounts,
                    themeColors = themeColors,
                    isSelected = isItemSelected,
                    isInSelectionMode = isInSelectionMode,
                    onSelectionClick = { onActivityClick(activity) },
                    onLongClick = { onActivityLongPress(activity) },
                    navController = navController,
                    animatedVisibilityScope = animatedVisibilityScope,
                )
            }
        }

        item(key = "scrolling_end_space") {
            Spacer(modifier = Modifier.height(150.dp))
        }
    }
}

@Composable
fun ActivityLogDateHeader(
    date: String,
    activityCount: Int,
    themeColors: ColorScheme
) {
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
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = date,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = iosFont,
            color = themeColors.inverseSurface.copy(0.7f)
        )
        Text(
            text = "$activityCount activities",
            fontSize = 12.sp,
            fontFamily = iosFont,
            color = themeColors.inverseSurface.copy(0.5f)
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.ActivityLogCard(
    activity: ActivityLogEntry,
    accounts: List<AccountEntity>,
    themeColors: ColorScheme,
    isSelected: Boolean = false,
    isInSelectionMode: Boolean = false,
    onSelectionClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    navController: NavController,
    animatedVisibilityScope: AnimatedVisibilityScope,
) {
    val haptic = LocalHapticFeedback.current
    val activityKey = "activity_log_${activity.id}"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .sharedBounds(
                resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds(
                    contentScale = ContentScale.Inside
                ),
                sharedContentState = rememberSharedContentState(key = activityKey),
                animatedVisibilityScope = animatedVisibilityScope
            )
            .pointerInput(isInSelectionMode) {
                detectTapGestures(
                    onLongPress = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongClick()
                    },
                    onTap = {
                        if (isInSelectionMode) {
                            onSelectionClick()
                        } else {
                            // Navigate to related transaction or details if needed
                        }
                    }
                )
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) themeColors.primaryContainer else themeColors.surface
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .animateContentSize()
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Selection indicator
            AnimatedVisibility(
                visible = isInSelectionMode,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
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

            // Activity icon based on type
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = activity.actionType.getIconBackgroundColor(),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = activity.actionType.getIconResource()),
                    contentDescription = activity.actionType.getDisplayName(),
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Activity content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = activity.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = iosFont,
                    color = themeColors.inverseSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (activity.description.isNotBlank()) {
                    Text(
                        text = activity.description,
                        fontSize = 14.sp,
                        fontFamily = iosFont,
                        color = themeColors.inverseSurface.copy(0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = activity.actionType.getDisplayName(),
                        fontSize = 12.sp,
                        fontFamily = iosFont,
                        color = activity.actionType.getIconBackgroundColor(),
                        modifier = Modifier
                            .background(
                                color = activity.actionType.getIconBackgroundColor().copy(0.1f),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )

                    if (activity.relatedAccountId != null) {
                        val account = accounts.find { it.id == activity.relatedAccountId }
                        account?.let {
                            Text(
                                text = it.accountName,
                                fontSize = 12.sp,
                                fontFamily = iosFont,
                                color = themeColors.inverseSurface.copy(0.5f)
                            )
                        }
                    }
                }
            }

            // Timestamp
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = formattedTimeText(activity.timestamp),
                    fontSize = 12.sp,
                    fontFamily = iosFont,
                    color = themeColors.inverseSurface.copy(0.6f)
                )

                if (activity.amount != null) {
                    val account = accounts.find { it.id == activity.relatedAccountId }
                    val currencySymbol = CurrencySymbols.getSymbol(account?.currencyCode ?: "usd")

                    Text(
                        text = formatCurrency(activity.amount, currencySymbol),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = iosFont,
                        color = when (activity.actionType) {
                            ActivityActionType.TRANSACTION_ADDED_EXPENSE,
                            ActivityActionType.TRANSACTION_DELETED_INCOME -> themeColors.onError
                            ActivityActionType.TRANSACTION_ADDED_INCOME,
                            ActivityActionType.TRANSACTION_DELETED_EXPENSE -> Color(0xFF7ACE96)
                            else -> themeColors.inverseSurface
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ActivityLogControls(
    filterState: ActivityLogFilterState,
    onFilterChange: (ActivityLogFilterState) -> Unit,
    activities: List<ActivityLogEntry>,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val showLogFilters = rememberSaveable { mutableStateOf(false) }
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceBright
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clickable(onClick = { showLogFilters.value = !showLogFilters.value }),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Log Filters",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = iosFont,
                        color = MaterialTheme.colorScheme.inverseSurface,
                    )

                    Box(
                        modifier = Modifier.padding(5.dp).background(MaterialTheme.colorScheme.surface, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ){
                        Icon(
                            imageVector = if (showLogFilters.value)Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Filter",
                            tint = MaterialTheme.colorScheme.inverseSurface.copy(0.6f)
                        )
                    }
                }


                Text(
                    text = "${activities.size} activities",
                    fontSize = 14.sp,
                    fontFamily = iosFont,
                    color = MaterialTheme.colorScheme.inverseSurface.copy(0.6f)
                )
            }

            AnimatedVisibility(visible = showLogFilters.value) {
                Column(
                    modifier = Modifier,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Action type filter buttons
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(ActivityActionType.values()) { actionType ->
                            FilterChip(
                                onClick = {
                                    val newTypes =
                                        if (filterState.selectedActionTypes.contains(actionType)) {
                                            filterState.selectedActionTypes - actionType
                                        } else {
                                            filterState.selectedActionTypes + actionType
                                        }
                                    onFilterChange(filterState.copy(selectedActionTypes = newTypes))
                                },
                                label = {
                                    Text(
                                        text = actionType.getDisplayName(),
                                        fontSize = 12.sp,
                                        fontFamily = iosFont,
                                    )
                                },
                                selected = filterState.selectedActionTypes.contains(actionType),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White,
                                    disabledContainerColor = MaterialTheme.colorScheme.surface,
                                    disabledLabelColor = MaterialTheme.colorScheme.inverseSurface.copy(
                                        0.6f
                                    ),
                                    disabledSelectedContainerColor = MaterialTheme.colorScheme.surface,
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    labelColor = MaterialTheme.colorScheme.inverseSurface,
                                ),
                                border = BorderStroke(0.dp, Color.Transparent)
                            )
                        }
                    }

//                    // Bottom row with sort and clear options
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.SpaceBetween,
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Row(
//                            horizontalArrangement = Arrangement.spacedBy(8.dp)
//                        ) {
//                            FilterChip(
//                                onClick = {
//                                    val newSortBy =
//                                        if (filterState.sortBy == ActivityLogSortBy.TIMESTAMP_DESC) {
//                                            ActivityLogSortBy.TIMESTAMP_ASC
//                                        } else {
//                                            ActivityLogSortBy.TIMESTAMP_DESC
//                                        }
//                                    onFilterChange(filterState.copy(sortBy = newSortBy))
//                                },
//                                label = {
//                                    Row(
//                                        verticalAlignment = Alignment.CenterVertically,
//                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
//                                    ) {
//                                        Icon(
//                                            painter = painterResource(
//                                                if (filterState.sortBy == ActivityLogSortBy.TIMESTAMP_DESC)
//                                                    R.drawable.arrow_mini_right_bulk // Use appropriate down arrow
//                                                else
//                                                    R.drawable.arrow_mini_left_bulk // Use appropriate up arrow
//                                            ),
//                                            contentDescription = null,
//                                            modifier = Modifier.size(16.dp)
//                                        )
//                                        Text(
//                                            text = "Time",
//                                            fontSize = 12.sp,
//                                            fontFamily = iosFont
//                                        )
//                                    }
//                                },
//                                selected = false,
//                                colors = FilterChipDefaults.filterChipColors(
//                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
//                                    selectedLabelColor = Color.White,
//                                    disabledContainerColor = MaterialTheme.colorScheme.surface,
//                                    disabledLabelColor = MaterialTheme.colorScheme.inverseSurface.copy(
//                                        0.6f
//                                    ),
//                                    disabledSelectedContainerColor = MaterialTheme.colorScheme.surface,
//                                    containerColor = MaterialTheme.colorScheme.surface,
//                                    labelColor = MaterialTheme.colorScheme.inverseSurface
//                                ),
//                                border = BorderStroke(0.dp, Color.Transparent)
//                            )
//                        }
//
//                        TextButton(
//                            onClick = onClearAll,
//                            colors = ButtonDefaults.textButtonColors(
//                                contentColor = MaterialTheme.colorScheme.onError
//                            )
//                        ) {
//                            Text(
//                                text = "Clear All",
//                                fontSize = 12.sp,
//                                fontFamily = iosFont
//                            )
//                        }
//                    }
                }
            }
        }
    }
}

@Composable
fun EmptyActivityLog(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.empty_category_list),
                contentDescription = "Empty Activity Log Icon",
                modifier = Modifier.size(300.dp)
            )
            Text(
                text = "No activity available",
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

@Composable
fun ActivityLogDeletionConfirmationDialog(
    showDialog: Boolean,
    activityCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Delete Activity Log Entries",
                    fontFamily = iosFont,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete $activityCount activity log ${if (activityCount == 1) "entry" else "entries"}? This action cannot be undone.",
                    fontFamily = iosFont
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text(
                        text = "Delete",
                        color = Color.White,
                        fontFamily = iosFont
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Cancel",
                        fontFamily = iosFont
                    )
                }
            }
        )
    }
}