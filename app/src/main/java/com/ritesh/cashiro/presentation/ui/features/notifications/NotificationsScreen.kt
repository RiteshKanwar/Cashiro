package com.ritesh.cashiro.presentation.ui.features.notifications

import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritesh.cashiro.domain.repository.LabelVisibility
import com.ritesh.cashiro.domain.repository.ReminderType
import com.ritesh.cashiro.domain.repository.Settings
import com.ritesh.cashiro.presentation.effects.VerticalStretchedOverscroll
import com.ritesh.cashiro.presentation.ui.extras.components.extras.CustomTitleTopAppBar
import com.ritesh.cashiro.presentation.ui.extras.components.settings.OptionsComponent
import com.ritesh.cashiro.presentation.ui.theme.iosFont
import com.ritesh.cashiro.domain.utils.NotificationScheduler
import com.ritesh.cashiro.permissions.RequestNotificationPermissions
import com.ritesh.cashiro.permissions.hasNotificationPermissions
import com.ritesh.cashiro.R
import kotlinx.coroutines.launch
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.ui.draw.shadow
import com.ritesh.cashiro.data.local.entity.TransactionEntity
import com.ritesh.cashiro.presentation.ui.features.add_transaction.TimePickerDialog
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Green_Dim
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Mauve_Dim
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Red_Dim
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NotificationsScreen(
    onBackClicked: () -> Unit,
    screenTitle: String,
    previousScreenTitle: String,
    notificationsViewModel: NotificationsViewModel
) {
    val settings by notificationsViewModel.settingsFlow.collectAsState(initial = Settings(Color.Blue, LabelVisibility.AlwaysShow))
    NotificationScreenContent(
        notificationUiState = notificationsViewModel.state.collectAsState().value,
        onNotificationEvent = notificationsViewModel::handleEvent,
        settings = settings,
        screenTitle = screenTitle,
        onBackClicked = onBackClicked,
        previousScreenTitle = previousScreenTitle,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationScreenContent(
    notificationUiState: NotificationState,
    onNotificationEvent: (NotificationEvent) -> Unit,
    settings: Settings,
    screenTitle: String,
    onBackClicked: () -> Unit,
    previousScreenTitle: String,
){
    val context = LocalContext.current
    val notificationSettings = settings.notificationSettings

    // Get upcoming transactions from the new state
    val upcomingTransactions = notificationUiState.filteredUpcomingTransactions
    val isLoading = notificationUiState.isLoading
    val error = notificationUiState.error

    // State for dialogs and modals - now using the state management
    val showReminderTypeModal = notificationUiState.showReminderTypeModal
    val showTimePicker = notificationUiState.showTimePicker

    val timePickerState = rememberTimePickerState(
        initialHour = notificationSettings.alertHour,
        initialMinute = notificationSettings.alertMinute
    )

    var hasPermissions by remember { mutableStateOf(hasNotificationPermissions(context)) }

    // Request permissions when component loads
    RequestNotificationPermissions { granted ->
        hasPermissions = granted
    }

    // Handle errors from the state
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // Show error message to user
            // You can use a Snackbar or other UI element
            Log.e("NotificationScreen", "Error: $errorMessage")
            // Clear the error after showing it
            onNotificationEvent(NotificationEvent.ClearError)
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val scrollOverScrollState = rememberLazyListState()
    val overscrollEffect = remember(coroutineScope) { VerticalStretchedOverscroll(coroutineScope) }

    // Scaffold State for Scroll Behavior
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scrollBehaviorSmall = TopAppBarDefaults.pinnedScrollBehavior()

    // Refresh data when screen comes back to foreground
    LaunchedEffect(Unit) {
        onNotificationEvent(NotificationEvent.LoadUpcomingTransactions)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CustomTitleTopAppBar(
                scrollBehaviorLarge = scrollBehavior,
                scrollBehaviorSmall = scrollBehaviorSmall,
                title = screenTitle,
                onBackClick = onBackClicked,
                previousScreenTitle = previousScreenTitle,
                hasBackButton = true
            )
        },
    ) { innerPadding ->
        LazyColumn(
            state = scrollOverScrollState,
            userScrollEnabled = false,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .overscroll(overscrollEffect)
                .scrollable(
                    orientation = Orientation.Vertical,
                    reverseDirection = true,
                    state = scrollOverScrollState,
                    overscrollEffect = overscrollEffect
                ),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Show loading indicator
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            // Add Transaction Reminder Section
            item {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    NotificationToggleOption(
                        showIcon = true,
                        iconResID = R.drawable.notification,
                        iconBackgroundColor = MaterialTheme.colorScheme.primary,
                        label = "Add Transaction Reminder",
                        isChecked = notificationSettings.addTransactionReminder,
                        isLast = true,
                        onToggle = { enabled ->
                            // Use the new event system
                            onNotificationEvent(
                                NotificationEvent.UpdateAddTransactionReminder(enabled)
                            )

                            if (hasPermissions) {
                                coroutineScope.launch {
                                    if (enabled) {
                                        val success = NotificationScheduler.scheduleTransactionReminder(
                                            context,
                                            notificationSettings.alertHour,
                                            notificationSettings.alertMinute,
                                            notificationSettings.reminderType
                                        )
                                        if (!success) {
                                            Log.w("NotificationsScreen", "Failed to schedule reminder")
                                        }
                                    } else {
                                        NotificationScheduler.cancelTransactionReminder(context)
                                    }
                                }
                            } else {
                                Log.w("NotificationsScreen", "Permissions not granted for notifications")
                            }
                        }
                    )

                    AnimatedVisibility(visible = notificationSettings.addTransactionReminder) {
                        Column {
                            // Reminder Type
                            OptionsComponent(
                                showIcon = true,
                                iconResID = R.drawable.notifications_reminder,
                                iconBackgroundColor = Macchiato_Mauve_Dim,
                                label = "Reminder Type",
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        onNotificationEvent(
                                            NotificationEvent.SetReminderTypeModalVisible(true)
                                        )
                                    }
                            ) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = notificationSettings.reminderType.getDisplayName(),
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.widthIn(max = 150.dp)
                                    )
                                }
                            }

                            // Alert Time
                            OptionsComponent(
                                showIcon = true,
                                iconResID = R.drawable.alert_timer,
                                iconBackgroundColor = Macchiato_Red_Dim,
                                label = "Alert Time",
                                modifier = Modifier
                                    .padding(vertical = 2.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        onNotificationEvent(
                                            NotificationEvent.SetTimePickerVisible(true)
                                        )
                                    },
                            ) {
                                // Your existing time display button code here
                                Button(
                                    onClick = {
                                        onNotificationEvent(
                                            NotificationEvent.SetTimePickerVisible(true)
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(Color.Transparent),
                                    contentPadding = PaddingValues(8.dp)
                                ) {
                                    // Your existing time display code here
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        // Calculate time values from notification settings
                                        val hour = if (notificationSettings.alertHour == 0 || notificationSettings.alertHour == 12) 12
                                        else notificationSettings.alertHour % 12
                                        val minute = notificationSettings.alertMinute
                                        val amPm = if (notificationSettings.alertHour < 12) "AM" else "PM"
                                        // Hour Box
                                        Box(
                                            modifier = Modifier
                                                .padding(5.dp)
                                                .background(
                                                    color = MaterialTheme.colorScheme.primary.copy(0.2f),
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                        ) {
                                            Text(
                                                text = String.format("%02d", hour),
                                                color = Color.White,
                                                fontFamily = iosFont,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                modifier = Modifier.padding(5.dp)
                                            )
                                        }

                                        // Colon Separator
                                        Text(
                                            text = ":",
                                            fontFamily = iosFont,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.inverseSurface,
                                            fontSize = 16.sp,
                                        )

                                        // Minute Box
                                        Box(
                                            modifier = Modifier
                                                .padding(5.dp)
                                                .background(
                                                    color = MaterialTheme.colorScheme.surfaceBright,
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                        ) {
                                            Text(
                                                text = String.format("%02d", minute),
                                                fontFamily = iosFont,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.inverseSurface,
                                                fontSize = 16.sp,
                                                modifier = Modifier.padding(5.dp)
                                            )
                                        }

                                        // AM/PM Box
                                        Box(modifier = Modifier.padding(5.dp)) {
                                            Text(
                                                text = amPm,
                                                fontFamily = iosFont,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.inverseOnSurface,
                                                fontSize = 14.sp,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Permission Warning Card
            // Permission Warning Card
            item {
                PermissionWarningCard(
                    hasPermissions = hasPermissions,
                    onRequestPermissions = {
                        hasPermissions = hasNotificationPermissions(context)
                    }
                )
            }

            // Upcoming Transactions Section
            item {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    NotificationToggleOption(
                        showIcon = true,
                        iconResID = R.drawable.calendar_tick_upcoming,
                        iconBackgroundColor = Macchiato_Green_Dim,
                        label = "Upcoming Transactions",
                        isChecked = notificationSettings.upcomingTransactions,
                        onToggle = { enabled ->
                            // Use the new event system
                           onNotificationEvent(
                                NotificationEvent.UpdateUpcomingTransactions(enabled)
                            )

                            coroutineScope.launch {
                                if (enabled) {
                                    // Schedule notifications for all enabled upcoming transactions
                                    upcomingTransactions.forEach { transaction ->
                                        val isEnabled = notificationSettings.sipNotifications["transaction_${transaction.id}"] ?: false
                                        if (isEnabled && transaction.nextDueDate != null) {
                                            NotificationScheduler.scheduleUpcomingTransactionReminder(
                                                context,
                                                transaction.id,
                                                transaction.title,
                                                transaction.nextDueDate
                                            )
                                        }
                                    }
                                } else {
                                    // Cancel all upcoming transaction notifications
                                    upcomingTransactions.forEach { transaction ->
                                        NotificationScheduler.cancelUpcomingTransactionReminder(context, transaction.id)
                                    }
                                }
                            }
                        },
                        isLast = true
                    )
                }
            }

            // Real Upcoming Transactions List
            if (notificationSettings.upcomingTransactions && upcomingTransactions.isNotEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(16.dp)
                            )
                    ) {
                        Column {
                            upcomingTransactions.forEachIndexed { index, transaction ->
                                UpcomingTransactionNotificationItem(
                                    transaction = transaction,
                                    isEnabled = notificationSettings.sipNotifications["transaction_${transaction.id}"] ?: false,
                                    onToggle = { enabled ->
                                        // Use the new event system for individual transaction notifications
                                        onNotificationEvent(
                                            NotificationEvent.ToggleTransactionNotification(transaction.id, enabled)
                                        )

                                        coroutineScope.launch {
                                            if (enabled && transaction.nextDueDate != null) {
                                                NotificationScheduler.scheduleUpcomingTransactionReminder(
                                                    context,
                                                    transaction.id,
                                                    transaction.title,
                                                    transaction.nextDueDate
                                                )
                                            } else {
                                                NotificationScheduler.cancelUpcomingTransactionReminder(context, transaction.id)
                                            }
                                        }
                                    },
                                    isLast = index == upcomingTransactions.size - 1
                                )

                                if (index < upcomingTransactions.size - 1) {
                                    Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.1f)))
                                }
                            }
                        }
                    }
                }
            } else if (notificationSettings.upcomingTransactions && upcomingTransactions.isEmpty() && !isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No upcoming transactions found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Reminder Type Modal
    if (showReminderTypeModal) {
        ModalBottomSheet(
            onDismissRequest = {
                onNotificationEvent(
                    NotificationEvent.SetReminderTypeModalVisible(false)
                )
            },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            ReminderTypeModalContent(
                currentReminderType = notificationSettings.reminderType,
                onReminderTypeSelected = { reminderType ->
                    onNotificationEvent(
                        NotificationEvent.UpdateReminderType(reminderType)
                    )

                    coroutineScope.launch {
                        if (notificationSettings.addTransactionReminder && hasPermissions) {
                            NotificationScheduler.cancelTransactionReminder(context)
                            val success = NotificationScheduler.scheduleTransactionReminder(
                                context,
                                notificationSettings.alertHour,
                                notificationSettings.alertMinute,
                                reminderType
                            )
                            if (!success) {
                                Log.w("NotificationsScreen", "Failed to reschedule reminder with new type")
                            }
                        }
                    }

                    onNotificationEvent(
                        NotificationEvent.SetReminderTypeModalVisible(false)
                    )
                }
            )
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = {
                onNotificationEvent(
                    NotificationEvent.SetTimePickerVisible(false)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onNotificationEvent(
                            NotificationEvent.UpdateAlertTime(timePickerState.hour, timePickerState.minute)
                        )

                        coroutineScope.launch {
                            if (notificationSettings.addTransactionReminder && hasPermissions) {
                                NotificationScheduler.cancelTransactionReminder(context)
                                val success = NotificationScheduler.scheduleTransactionReminder(
                                    context,
                                    timePickerState.hour,
                                    timePickerState.minute,
                                    notificationSettings.reminderType
                                )
                                if (!success) {
                                    Log.w("NotificationsScreen", "Failed to reschedule reminder with new time")
                                }
                            }
                        }

                        onNotificationEvent(
                            NotificationEvent.SetTimePickerVisible(false)
                        )
                    },
                    modifier = Modifier
                        .shadow(
                            5.dp,
                            RoundedCornerShape(15.dp),
                            spotColor = MaterialTheme.colorScheme.primary,
                            ambientColor = Color.Transparent
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(15.dp)
                ) {
                    Text(
                        text = "Okay",
                        modifier = Modifier.padding(horizontal = 10.dp),
                        fontFamily = iosFont
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onNotificationEvent(
                            NotificationEvent.SetTimePickerVisible(false)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.inverseSurface
                    ),
                    shape = RoundedCornerShape(15.dp)
                ) {
                    Text(
                        text = "Cancel",
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                }
            }
        ) {
            TimePicker(
                state = timePickerState,
                colors = TimePickerDefaults.colors(
                    clockDialColor = MaterialTheme.colorScheme.surface,
                    clockDialSelectedContentColor = Color.White,
                    clockDialUnselectedContentColor = MaterialTheme.colorScheme.inverseSurface,
                    selectorColor = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.surface,
                    periodSelectorBorderColor = Color.Transparent,
                    periodSelectorSelectedContainerColor = MaterialTheme.colorScheme.onError.copy(
                        0.5f
                    ),
                    periodSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surfaceBright,
                    periodSelectorSelectedContentColor = Color.White,
                    periodSelectorUnselectedContentColor = MaterialTheme.colorScheme.inverseSurface,
                    timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary,
                    timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surfaceBright,
                    timeSelectorSelectedContentColor = MaterialTheme.colorScheme.surface,
                    timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.inverseSurface
                )
            )
        }
    }
}
@Composable
private fun UpcomingTransactionNotificationItem(
    transaction: TransactionEntity,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    isLast: Boolean = false
) {
    val themeColors = MaterialTheme.colorScheme

    // Format the due date
    val dueDateText = transaction.nextDueDate?.let { dueDate ->
        val sdf = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
        sdf.format(Date(dueDate))
    } ?: "No due date"

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.repeat),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = themeColors.inverseSurface.copy(0.7f)
                )

                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = transaction.title,
                        textAlign = TextAlign.Start,
                        fontFamily = iosFont,
                        fontSize = 16.sp,
                        color = themeColors.inverseSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = dueDateText,
                        textAlign = TextAlign.Start,
                        fontFamily = iosFont,
                        fontSize = 12.sp,
                        color = themeColors.inverseSurface.copy(0.6f)
                    )
                }
            }

            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = themeColors.primary,
                    checkedBorderColor = themeColors.primary,
                    uncheckedThumbColor = themeColors.inverseSurface.copy(0.5f),
                    uncheckedTrackColor = themeColors.background,
                    uncheckedBorderColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
private fun NotificationToggleOption(
    showIcon: Boolean = false,
    @DrawableRes iconResID: Int = 0,
    iconBackgroundColor: Color = Color.Transparent,
    label: String,
    isChecked: Boolean,
    onToggle: (Boolean) -> Unit,
    isLast: Boolean = false,
    modifier: Modifier = Modifier
) {
    val themeColors = MaterialTheme.colorScheme

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (showIcon) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = iconBackgroundColor,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = iconResID),
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = Color.White
                        )
                    }
                }
                Text(
                    text = label,
                    textAlign = TextAlign.Start,
                    fontFamily = iosFont,
                    modifier = Modifier.padding(10.dp),
                    color = themeColors.inverseSurface
                )
            }

            Switch(
                checked = isChecked,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = themeColors.primary,
                    checkedBorderColor = themeColors.primary,
                    uncheckedThumbColor = themeColors.inverseSurface.copy(0.5f),
                    uncheckedTrackColor = themeColors.background,
                    uncheckedBorderColor = Color.Transparent
                )
            )
        }

        if (!isLast) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(themeColors.inverseSurface.copy(alpha = 0.1f))
            )
        }
    }
}

@Composable
private fun ReminderTypeModalContent(
    currentReminderType: ReminderType,
    onReminderTypeSelected: (ReminderType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Reminder Type",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.inverseSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        ReminderType.entries.forEach { reminderType ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onReminderTypeSelected(reminderType) }
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = currentReminderType == reminderType,
                    onClick = { onReminderTypeSelected(reminderType) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = reminderType.getDisplayName(),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.inverseSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun PermissionWarningCard(
    hasPermissions: Boolean,
    onRequestPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!hasPermissions) {
        Card(
            modifier = modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.warning_bulk),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Permission Required",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "To receive notifications, this app needs permission to send notifications and schedule exact alarms.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onRequestPermissions,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Grant Permissions")
                }
            }
        }
    }
}

//@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
//@Composable
//fun NotificationsScreen(
//    onBackClicked: () -> Unit,
//    screenTitle: String,
//    previousScreenTitle: String,
//    notificationsViewModel: NotificationsViewModel
//) {
//    val context = LocalContext.current
//
//    // Use the new state management approach
//    val notificationState by notificationsViewModel.state.collectAsState()
//    val settings by notificationsViewModel.settingsFlow.collectAsState(initial = Settings(Color.Blue, LabelVisibility.AlwaysShow))
//    val notificationSettings = settings.notificationSettings
//
//    // Get upcoming transactions from the new state
//    val upcomingTransactions = notificationState.filteredUpcomingTransactions
//    val isLoading = notificationState.isLoading
//    val error = notificationState.error
//
//    // State for dialogs and modals - now using the state management
//    val showReminderTypeModal = notificationState.showReminderTypeModal
//    val showTimePicker = notificationState.showTimePicker
//
//    val timePickerState = rememberTimePickerState(
//        initialHour = notificationSettings.alertHour,
//        initialMinute = notificationSettings.alertMinute
//    )
//
//    var hasPermissions by remember { mutableStateOf(hasNotificationPermissions(context)) }
//
//    // Request permissions when component loads
//    RequestNotificationPermissions { granted ->
//        hasPermissions = granted
//    }
//
//    // Handle errors from the state
//    error?.let { errorMessage ->
//        LaunchedEffect(errorMessage) {
//            // Show error message to user
//            // You can use a Snackbar or other UI element
//            Log.e("NotificationScreen", "Error: $errorMessage")
//            // Clear the error after showing it
//            notificationsViewModel.handleEvent(NotificationEvent.ClearError)
//        }
//    }
//
//    val coroutineScope = rememberCoroutineScope()
//    val scrollOverScrollState = rememberLazyListState()
//    val overscrollEffect = remember(coroutineScope) { VerticalStretchedOverscroll(coroutineScope) }
//
//    // Scaffold State for Scroll Behavior
//    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
//    val scrollBehaviorSmall = TopAppBarDefaults.pinnedScrollBehavior()
//
//    // Refresh data when screen comes back to foreground
//    LaunchedEffect(Unit) {
//        notificationsViewModel.handleEvent(NotificationEvent.LoadUpcomingTransactions)
//    }
//
//    Scaffold(
//        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
//        topBar = {
//            CustomTitleTopAppBar(
//                scrollBehaviorLarge = scrollBehavior,
//                scrollBehaviorSmall = scrollBehaviorSmall,
//                title = screenTitle,
//                onBackClick = onBackClicked,
//                previousScreenTitle = previousScreenTitle,
//                hasBackButton = true
//            )
//        },
//    ) { innerPadding ->
//        LazyColumn(
//            state = scrollOverScrollState,
//            userScrollEnabled = false,
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(innerPadding)
//                .background(MaterialTheme.colorScheme.background)
//                .overscroll(overscrollEffect)
//                .scrollable(
//                    orientation = Orientation.Vertical,
//                    reverseDirection = true,
//                    state = scrollOverScrollState,
//                    overscrollEffect = overscrollEffect
//                ),
//            verticalArrangement = Arrangement.spacedBy(10.dp)
//        ) {
//            // Show loading indicator
//            if (isLoading) {
//                item {
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(16.dp),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        CircularProgressIndicator()
//                    }
//                }
//            }
//
//            // Add Transaction Reminder Section
//            item {
//                Column(
//                    modifier = Modifier
//                        .padding(horizontal = 20.dp)
//                        .background(
//                            color = MaterialTheme.colorScheme.surface,
//                            shape = RoundedCornerShape(16.dp)
//                        )
//                ) {
//                    NotificationToggleOption(
//                        showIcon = true,
//                        iconResID = R.drawable.notification,
//                        iconBackgroundColor = MaterialTheme.colorScheme.primary,
//                        label = "Add Transaction Reminder",
//                        isChecked = notificationSettings.addTransactionReminder,
//                        isLast = true,
//                        onToggle = { enabled ->
//                            // Use the new event system
//                            notificationsViewModel.handleEvent(
//                                NotificationEvent.UpdateAddTransactionReminder(enabled)
//                            )
//
//                            if (hasPermissions) {
//                                coroutineScope.launch {
//                                    if (enabled) {
//                                        val success = NotificationScheduler.scheduleTransactionReminder(
//                                            context,
//                                            notificationSettings.alertHour,
//                                            notificationSettings.alertMinute,
//                                            notificationSettings.reminderType
//                                        )
//                                        if (!success) {
//                                            Log.w("NotificationsScreen", "Failed to schedule reminder")
//                                        }
//                                    } else {
//                                        NotificationScheduler.cancelTransactionReminder(context)
//                                    }
//                                }
//                            } else {
//                                Log.w("NotificationsScreen", "Permissions not granted for notifications")
//                            }
//                        }
//                    )
//
//                    AnimatedVisibility(visible = notificationSettings.addTransactionReminder) {
//                        Column {
//                            // Reminder Type
//                            OptionsComponent(
//                                showIcon = true,
//                                iconResID = R.drawable.notifications_reminder,
//                                iconBackgroundColor = Macchiato_Mauve_Dim,
//                                label = "Reminder Type",
//                                modifier = Modifier
//                                    .clip(RoundedCornerShape(10.dp))
//                                    .clickable {
//                                        notificationsViewModel.handleEvent(
//                                            NotificationEvent.SetReminderTypeModalVisible(true)
//                                        )
//                                    }
//                            ) {
//                                Column(horizontalAlignment = Alignment.End) {
//                                    Text(
//                                        text = notificationSettings.reminderType.getDisplayName(),
//                                        fontSize = 12.sp,
//                                        color = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
//                                        maxLines = 1,
//                                        overflow = TextOverflow.Ellipsis,
//                                        modifier = Modifier.widthIn(max = 150.dp)
//                                    )
//                                }
//                            }
//
//                            // Alert Time
//                            OptionsComponent(
//                                showIcon = true,
//                                iconResID = R.drawable.alert_timer,
//                                iconBackgroundColor = Macchiato_Red_Dim,
//                                label = "Alert Time",
//                                modifier = Modifier
//                                    .padding(vertical = 2.dp)
//                                    .clip(RoundedCornerShape(10.dp))
//                                    .clickable {
//                                        notificationsViewModel.handleEvent(
//                                            NotificationEvent.SetTimePickerVisible(true)
//                                        )
//                                    },
//                            ) {
//                                // Your existing time display button code here
//                                Button(
//                                    onClick = {
//                                        notificationsViewModel.handleEvent(
//                                            NotificationEvent.SetTimePickerVisible(true)
//                                        )
//                                    },
//                                    colors = ButtonDefaults.buttonColors(Color.Transparent),
//                                    contentPadding = PaddingValues(8.dp)
//                                ) {
//                                    // Your existing time display code here
//                                    Row(
//                                        verticalAlignment = Alignment.CenterVertically,
//                                        horizontalArrangement = Arrangement.End
//                                    ) {
//                                        // Calculate time values from notification settings
//                                        val hour = if (notificationSettings.alertHour == 0 || notificationSettings.alertHour == 12) 12
//                                        else notificationSettings.alertHour % 12
//                                        val minute = notificationSettings.alertMinute
//                                        val amPm = if (notificationSettings.alertHour < 12) "AM" else "PM"
//                                            // Hour Box
//                                            Box(
//                                                modifier = Modifier
//                                                    .padding(5.dp)
//                                                    .background(
//                                                        color = MaterialTheme.colorScheme.primary.copy(0.2f),
//                                                        shape = RoundedCornerShape(8.dp)
//                                                    )
//                                            ) {
//                                                Text(
//                                                    text = String.format("%02d", hour),
//                                                    color = Color.White,
//                                                    fontFamily = iosFont,
//                                                    fontWeight = FontWeight.Bold,
//                                                    fontSize = 16.sp,
//                                                    modifier = Modifier.padding(5.dp)
//                                                )
//                                            }
//
//                                            // Colon Separator
//                                            Text(
//                                                text = ":",
//                                                fontFamily = iosFont,
//                                                fontWeight = FontWeight.Bold,
//                                                color = MaterialTheme.colorScheme.inverseSurface,
//                                                fontSize = 16.sp,
//                                            )
//
//                                            // Minute Box
//                                            Box(
//                                                modifier = Modifier
//                                                    .padding(5.dp)
//                                                    .background(
//                                                        color = MaterialTheme.colorScheme.surfaceBright,
//                                                        shape = RoundedCornerShape(8.dp)
//                                                    )
//                                            ) {
//                                                Text(
//                                                    text = String.format("%02d", minute),
//                                                    fontFamily = iosFont,
//                                                    fontWeight = FontWeight.Bold,
//                                                    color = MaterialTheme.colorScheme.inverseSurface,
//                                                    fontSize = 16.sp,
//                                                    modifier = Modifier.padding(5.dp)
//                                                )
//                                            }
//
//                                            // AM/PM Box
//                                            Box(modifier = Modifier.padding(5.dp)) {
//                                                Text(
//                                                    text = amPm,
//                                                    fontFamily = iosFont,
//                                                    fontWeight = FontWeight.SemiBold,
//                                                    color = MaterialTheme.colorScheme.inverseOnSurface,
//                                                    fontSize = 14.sp,
//                                                )
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//            }
//
//            // Permission Warning Card
//            // Permission Warning Card
//            item {
//                PermissionWarningCard(
//                    hasPermissions = hasPermissions,
//                    onRequestPermissions = {
//                        hasPermissions = hasNotificationPermissions(context)
//                    }
//                )
//            }
//
//            // Upcoming Transactions Section
//            item {
//                Box(
//                    modifier = Modifier
//                        .padding(horizontal = 20.dp)
//                        .background(
//                            color = MaterialTheme.colorScheme.surface,
//                            shape = RoundedCornerShape(16.dp)
//                        )
//                ) {
//                    NotificationToggleOption(
//                        showIcon = true,
//                        iconResID = R.drawable.calendar_tick_upcoming,
//                        iconBackgroundColor = Macchiato_Green_Dim,
//                        label = "Upcoming Transactions",
//                        isChecked = notificationSettings.upcomingTransactions,
//                        onToggle = { enabled ->
//                            // Use the new event system
//                            notificationsViewModel.handleEvent(
//                                NotificationEvent.UpdateUpcomingTransactions(enabled)
//                            )
//
//                            coroutineScope.launch {
//                                if (enabled) {
//                                    // Schedule notifications for all enabled upcoming transactions
//                                    upcomingTransactions.forEach { transaction ->
//                                        val isEnabled = notificationSettings.sipNotifications["transaction_${transaction.id}"] ?: false
//                                        if (isEnabled && transaction.nextDueDate != null) {
//                                            NotificationScheduler.scheduleUpcomingTransactionReminder(
//                                                context,
//                                                transaction.id,
//                                                transaction.title,
//                                                transaction.nextDueDate
//                                            )
//                                        }
//                                    }
//                                } else {
//                                    // Cancel all upcoming transaction notifications
//                                    upcomingTransactions.forEach { transaction ->
//                                        NotificationScheduler.cancelUpcomingTransactionReminder(context, transaction.id)
//                                    }
//                                }
//                            }
//                        },
//                        isLast = true
//                    )
//                }
//            }
//
//            // Real Upcoming Transactions List
//            if (notificationSettings.upcomingTransactions && upcomingTransactions.isNotEmpty()) {
//                item {
//                    Box(
//                        modifier = Modifier
//                            .padding(horizontal = 20.dp)
//                            .background(
//                                color = MaterialTheme.colorScheme.surface,
//                                shape = RoundedCornerShape(16.dp)
//                            )
//                    ) {
//                        Column {
//                            upcomingTransactions.forEachIndexed { index, transaction ->
//                                UpcomingTransactionNotificationItem(
//                                    transaction = transaction,
//                                    isEnabled = notificationSettings.sipNotifications["transaction_${transaction.id}"] ?: false,
//                                    onToggle = { enabled ->
//                                        // Use the new event system for individual transaction notifications
//                                        notificationsViewModel.handleEvent(
//                                            NotificationEvent.ToggleTransactionNotification(transaction.id, enabled)
//                                        )
//
//                                        coroutineScope.launch {
//                                            if (enabled && transaction.nextDueDate != null) {
//                                                NotificationScheduler.scheduleUpcomingTransactionReminder(
//                                                    context,
//                                                    transaction.id,
//                                                    transaction.title,
//                                                    transaction.nextDueDate
//                                                )
//                                            } else {
//                                                NotificationScheduler.cancelUpcomingTransactionReminder(context, transaction.id)
//                                            }
//                                        }
//                                    },
//                                    isLast = index == upcomingTransactions.size - 1
//                                )
//
//                                if (index < upcomingTransactions.size - 1) {
//                                    Spacer(modifier = Modifier.height(1.dp).fillMaxWidth().background(MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.1f)))
//                                }
//                            }
//                        }
//                    }
//                }
//            } else if (notificationSettings.upcomingTransactions && upcomingTransactions.isEmpty() && !isLoading) {
//                item {
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(horizontal = 20.dp, vertical = 16.dp),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Text(
//                            text = "No upcoming transactions found",
//                            style = MaterialTheme.typography.bodyMedium,
//                            color = MaterialTheme.colorScheme.onSurfaceVariant
//                        )
//                    }
//                }
//            }
//        }
//    }
//
//    // Reminder Type Modal
//    if (showReminderTypeModal) {
//        ModalBottomSheet(
//            onDismissRequest = {
//                notificationsViewModel.handleEvent(
//                    NotificationEvent.SetReminderTypeModalVisible(false)
//                )
//            },
//            containerColor = MaterialTheme.colorScheme.surface
//        ) {
//            ReminderTypeModalContent(
//                currentReminderType = notificationSettings.reminderType,
//                onReminderTypeSelected = { reminderType ->
//                    notificationsViewModel.handleEvent(
//                        NotificationEvent.UpdateReminderType(reminderType)
//                    )
//
//                    coroutineScope.launch {
//                        if (notificationSettings.addTransactionReminder && hasPermissions) {
//                            NotificationScheduler.cancelTransactionReminder(context)
//                            val success = NotificationScheduler.scheduleTransactionReminder(
//                                context,
//                                notificationSettings.alertHour,
//                                notificationSettings.alertMinute,
//                                reminderType
//                            )
//                            if (!success) {
//                                Log.w("NotificationsScreen", "Failed to reschedule reminder with new type")
//                            }
//                        }
//                    }
//
//                    notificationsViewModel.handleEvent(
//                        NotificationEvent.SetReminderTypeModalVisible(false)
//                    )
//                }
//            )
//        }
//    }
//
//    // Time Picker Dialog
//    if (showTimePicker) {
//        TimePickerDialog(
//            onDismissRequest = {
//                notificationsViewModel.handleEvent(
//                    NotificationEvent.SetTimePickerVisible(false)
//                )
//            },
//            confirmButton = {
//                TextButton(
//                    onClick = {
//                        notificationsViewModel.handleEvent(
//                            NotificationEvent.UpdateAlertTime(timePickerState.hour, timePickerState.minute)
//                        )
//
//                        coroutineScope.launch {
//                            if (notificationSettings.addTransactionReminder && hasPermissions) {
//                                NotificationScheduler.cancelTransactionReminder(context)
//                                val success = NotificationScheduler.scheduleTransactionReminder(
//                                    context,
//                                    timePickerState.hour,
//                                    timePickerState.minute,
//                                    notificationSettings.reminderType
//                                )
//                                if (!success) {
//                                    Log.w("NotificationsScreen", "Failed to reschedule reminder with new time")
//                                }
//                            }
//                        }
//
//                        notificationsViewModel.handleEvent(
//                            NotificationEvent.SetTimePickerVisible(false)
//                        )
//                    },
//                    modifier = Modifier
//                        .shadow(
//                            5.dp,
//                            RoundedCornerShape(15.dp),
//                            spotColor = MaterialTheme.colorScheme.primary,
//                            ambientColor = Color.Transparent
//                        ),
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = MaterialTheme.colorScheme.primary,
//                        contentColor = Color.White
//                    ),
//                    shape = RoundedCornerShape(15.dp)
//                ) {
//                    Text(
//                        text = "Okay",
//                        modifier = Modifier.padding(horizontal = 10.dp),
//                        fontFamily = iosFont
//                    )
//                }
//            },
//            dismissButton = {
//                TextButton(
//                    onClick = {
//                        notificationsViewModel.handleEvent(
//                            NotificationEvent.SetTimePickerVisible(false)
//                        )
//                    },
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = MaterialTheme.colorScheme.surface,
//                        contentColor = MaterialTheme.colorScheme.inverseSurface
//                    ),
//                    shape = RoundedCornerShape(15.dp)
//                ) {
//                    Text(
//                        text = "Cancel",
//                        modifier = Modifier.padding(horizontal = 10.dp)
//                    )
//                }
//            }
//        ) {
//            TimePicker(
//                state = timePickerState,
//                colors = TimePickerDefaults.colors(
//                    clockDialColor = MaterialTheme.colorScheme.surface,
//                    clockDialSelectedContentColor = Color.White,
//                    clockDialUnselectedContentColor = MaterialTheme.colorScheme.inverseSurface,
//                    selectorColor = MaterialTheme.colorScheme.primary,
//                    containerColor = MaterialTheme.colorScheme.surface,
//                    periodSelectorBorderColor = Color.Transparent,
//                    periodSelectorSelectedContainerColor = MaterialTheme.colorScheme.onError.copy(
//                        0.5f
//                    ),
//                    periodSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surfaceBright,
//                    periodSelectorSelectedContentColor = Color.White,
//                    periodSelectorUnselectedContentColor = MaterialTheme.colorScheme.inverseSurface,
//                    timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primary,
//                    timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surfaceBright,
//                    timeSelectorSelectedContentColor = MaterialTheme.colorScheme.surface,
//                    timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.inverseSurface
//                )
//            )
//        }
//    }
//}
//
//@Composable
//private fun UpcomingTransactionNotificationItem(
//    transaction: TransactionEntity,
//    isEnabled: Boolean,
//    onToggle: (Boolean) -> Unit,
//    isLast: Boolean = false
//) {
//    val themeColors = MaterialTheme.colorScheme
//
//    // Format the due date
//    val dueDateText = transaction.nextDueDate?.let { dueDate ->
//        val sdf = SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault())
//        sdf.format(Date(dueDate))
//    } ?: "No due date"
//
//    Column(modifier = Modifier.fillMaxWidth()) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(vertical = 2.dp, horizontal = 16.dp),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.spacedBy(10.dp)
//            ) {
//                Icon(
//                    painter = painterResource(id = R.drawable.repeat),
//                    contentDescription = null,
//                    modifier = Modifier.size(24.dp),
//                    tint = themeColors.inverseSurface.copy(0.7f)
//                )
//
//                Column(modifier = Modifier.padding(10.dp)) {
//                    Text(
//                        text = transaction.title,
//                        textAlign = TextAlign.Start,
//                        fontFamily = iosFont,
//                        fontSize = 16.sp,
//                        color = themeColors.inverseSurface,
//                        maxLines = 1,
//                        overflow = TextOverflow.Ellipsis
//                    )
//                    Text(
//                        text = dueDateText,
//                        textAlign = TextAlign.Start,
//                        fontFamily = iosFont,
//                        fontSize = 12.sp,
//                        color = themeColors.inverseSurface.copy(0.6f)
//                    )
//                }
//            }
//
//            Switch(
//                checked = isEnabled,
//                onCheckedChange = onToggle,
//                colors = SwitchDefaults.colors(
//                    checkedThumbColor = Color.White,
//                    checkedTrackColor = themeColors.primary,
//                    checkedBorderColor = themeColors.primary,
//                    uncheckedThumbColor = themeColors.inverseSurface.copy(0.5f),
//                    uncheckedTrackColor = themeColors.background,
//                    uncheckedBorderColor = Color.Transparent
//                )
//            )
//        }
//    }
//}
//
//@Composable
//private fun NotificationToggleOption(
//    showIcon: Boolean = false,
//    @DrawableRes iconResID: Int = 0,
//    iconBackgroundColor: Color = Color.Transparent,
//    label: String,
//    isChecked: Boolean,
//    onToggle: (Boolean) -> Unit,
//    isLast: Boolean = false,
//    modifier: Modifier = Modifier
//) {
//    val themeColors = MaterialTheme.colorScheme
//
//    Column(modifier = modifier.fillMaxWidth()) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 16.dp, vertical = 8.dp),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Row(
//                verticalAlignment = Alignment.CenterVertically,
//                horizontalArrangement = Arrangement.spacedBy(10.dp)
//            ) {
//                if (showIcon) {
//                    Box(
//                        modifier = Modifier
//                            .background(
//                                color = iconBackgroundColor,
//                                shape = RoundedCornerShape(10.dp)
//                            )
//                            .padding(8.dp)
//                    ) {
//                        Icon(
//                            painter = painterResource(id = iconResID),
//                            contentDescription = null,
//                            modifier = Modifier.size(22.dp),
//                            tint = Color.White
//                        )
//                    }
//                }
//                Text(
//                    text = label,
//                    textAlign = TextAlign.Start,
//                    fontFamily = iosFont,
//                    modifier = Modifier.padding(10.dp),
//                    color = themeColors.inverseSurface
//                )
//            }
//
//            Switch(
//                checked = isChecked,
//                onCheckedChange = onToggle,
//                colors = SwitchDefaults.colors(
//                    checkedThumbColor = Color.White,
//                    checkedTrackColor = themeColors.primary,
//                    checkedBorderColor = themeColors.primary,
//                    uncheckedThumbColor = themeColors.inverseSurface.copy(0.5f),
//                    uncheckedTrackColor = themeColors.background,
//                    uncheckedBorderColor = Color.Transparent
//                )
//            )
//        }
//
//        if (!isLast) {
//            Spacer(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(1.dp)
//                    .background(themeColors.inverseSurface.copy(alpha = 0.1f))
//            )
//        }
//    }
//}
//
//@Composable
//private fun ReminderTypeModalContent(
//    currentReminderType: ReminderType,
//    onReminderTypeSelected: (ReminderType) -> Unit
//) {
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(16.dp)
//    ) {
//        Text(
//            text = "Reminder Type",
//            fontSize = 20.sp,
//            fontWeight = FontWeight.SemiBold,
//            color = MaterialTheme.colorScheme.inverseSurface,
//            modifier = Modifier.padding(bottom = 16.dp)
//        )
//
//        ReminderType.entries.forEach { reminderType ->
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .clip(RoundedCornerShape(10.dp))
//                    .clickable { onReminderTypeSelected(reminderType) }
//                    .padding(vertical = 12.dp, horizontal = 8.dp),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                RadioButton(
//                    selected = currentReminderType == reminderType,
//                    onClick = { onReminderTypeSelected(reminderType) },
//                    colors = RadioButtonDefaults.colors(
//                        selectedColor = MaterialTheme.colorScheme.primary
//                    )
//                )
//                Spacer(modifier = Modifier.width(12.dp))
//                Text(
//                    text = reminderType.getDisplayName(),
//                    fontSize = 16.sp,
//                    color = MaterialTheme.colorScheme.inverseSurface
//                )
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//    }
//}
//
//@Composable
//private fun PermissionWarningCard(
//    hasPermissions: Boolean,
//    onRequestPermissions: () -> Unit,
//    modifier: Modifier = Modifier
//) {
//    if (!hasPermissions) {
//        Card(
//            modifier = modifier
//                .padding(horizontal = 20.dp)
//                .fillMaxWidth(),
//            colors = CardDefaults.cardColors(
//                containerColor = MaterialTheme.colorScheme.errorContainer
//            )
//        ) {
//            Column(
//                modifier = Modifier.padding(16.dp)
//            ) {
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    Icon(
//                        painter = painterResource(id = R.drawable.warning_bulk),
//                        contentDescription = null,
//                        tint = MaterialTheme.colorScheme.onErrorContainer,
//                        modifier = Modifier.size(20.dp)
//                    )
//                    Text(
//                        text = "Permission Required",
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.SemiBold,
//                        color = MaterialTheme.colorScheme.onErrorContainer
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(8.dp))
//
//                Text(
//                    text = "To receive notifications, this app needs permission to send notifications and schedule exact alarms.",
//                    fontSize = 14.sp,
//                    color = MaterialTheme.colorScheme.onErrorContainer,
//                    lineHeight = 20.sp
//                )
//
//                Spacer(modifier = Modifier.height(12.dp))
//
//                Button(
//                    onClick = onRequestPermissions,
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = MaterialTheme.colorScheme.error,
//                        contentColor = MaterialTheme.colorScheme.onError
//                    )
//                ) {
//                    Text("Grant Permissions")
//                }
//            }
//        }
//    }
//}