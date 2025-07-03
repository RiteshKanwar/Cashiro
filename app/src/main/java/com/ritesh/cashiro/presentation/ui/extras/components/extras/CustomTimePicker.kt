package com.ritesh.cashiro.presentation.ui.extras.components.extras

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ritesh.cashiro.data.local.entity.TransactionEntity
import com.ritesh.cashiro.domain.utils.getLocalDateFromMillis
import com.ritesh.cashiro.presentation.ui.features.add_transaction.AddTransactionScreenViewModel
import com.ritesh.cashiro.R
import com.ritesh.cashiro.presentation.ui.features.add_transaction.AddTransactionEvent
import com.ritesh.cashiro.presentation.ui.theme.iosFont
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Locale
// CustomTimePicker.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTimePicker(
    onTimeSelected: (Long) -> Unit,
    isUpdateTransaction: Boolean = false,
    transactionViewModel: AddTransactionScreenViewModel,
    transaction: TransactionEntity? = null
) {
    val transactionDate by transactionViewModel.transactionDate.collectAsState()
    val transactionTime by transactionViewModel.transactionTime.collectAsState()
    LaunchedEffect(transaction) {
        if (isUpdateTransaction && transaction != null) {
            transactionViewModel.onEvent(AddTransactionEvent.UpdateTime(transaction.time))
        }
    }

    val isDialogOpen = rememberSaveable() { mutableStateOf(false) }
    // Calculate transaction time only once when the composition starts
    val transactionLocalDate = getLocalDateFromMillis(transactionDate)
    val transactionLocalTime = remember {
        if (isUpdateTransaction && transaction != null) {
            // Convert transaction.time to LocalTime
            val instant = Instant.ofEpochMilli(transaction.time)
            LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalTime()
        } else {
            null
        }
    }

    val currentTime = if (transactionLocalTime != null) {
        transactionLocalTime
    } else {
        LocalTime.now()
    }
    val initialHour = currentTime.hour
    val initialMinute = currentTime.minute
    val isAM = initialHour < 12
    val formattedHour = if (initialHour % 12 == 0) 12 else initialHour % 12
    val amPm = if (isAM) "Am" else "Pm"
    val defaultTime = String.format(
        Locale.getDefault(),
        "%02d:%02d %s",
        formattedHour,
        initialMinute,
        amPm
    )

    // State to store the selected time
    val selectedTime = remember { mutableStateOf(defaultTime) }

    // Callback for when the user confirms their time selection
    val onConfirm: (TimePickerState) -> Unit = { timePickerState ->
        val hour = timePickerState.hour
        val minute = timePickerState.minute
        val isAMSelected = hour < 12
        val formattedSelectedHour = if (hour % 12 == 0) 12 else hour % 12
        val selectedAmPm = if (isAMSelected) "Am" else "Pm"

        selectedTime.value = String.format(
            Locale.getDefault(),
            "%02d:%02d %s",
            formattedSelectedHour,
            minute,
            selectedAmPm
        )

        val selectedTimeInMillis = LocalDateTime.of(
            LocalDate.now(),
            LocalTime.of(hour, minute)
        ).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        onTimeSelected(selectedTimeInMillis)
        isDialogOpen.value = false
    }

    // Callback for when the dialog is dismissed
    val onDismiss: () -> Unit = {
        isDialogOpen.value = false
    }

    // UI Button to open the time picker dialog
    Button(
        onClick = { isDialogOpen.value = true },
        colors = ButtonDefaults.buttonColors(Color.Transparent),
        contentPadding = PaddingValues(8.dp) // Adjust padding for better layout
    ) {
        val timeParts = selectedTime.value.split(":", " ") // Split into hour, minute, and AM/PM
        if (timeParts.size == 3) {
            Row(modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Absolute.Right) {
                Box(modifier = Modifier
                    .padding(5.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(0.2f),
                        shape = RoundedCornerShape(8.dp)
                    )
                ) {
                    Text(
                        text = timeParts[0], // Hour
                        color = Color.White,
                        fontFamily = iosFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(5.dp)
                    ) }

                Text(
                    text = ":", // Colon
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.inverseSurface,
                    fontSize = 16.sp,
                )

                Box(modifier = Modifier
                    .padding(5.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceBright,
                        shape = RoundedCornerShape(8.dp)
                    )
                ) { Text(
                    text = timeParts[1], // Minute
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.inverseSurface,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(5.dp)
                )}

                Box(modifier = Modifier.padding(5.dp)
                ) { Text(
                    text = timeParts[2], // AM/PM
                    fontFamily = iosFont,
                    fontWeight = FontWeight.SemiBold,
                    color =  MaterialTheme.colorScheme.inverseOnSurface,
                    fontSize = 14.sp,
                )}
            }
        }
    }

    // Show the time picker dialog if `isDialogOpen` is true
    if (isDialogOpen.value) {
        AdvancedTimePicker(
            onConfirm = onConfirm,
            onDismiss = onDismiss
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedTimePicker(
    onConfirm: (TimePickerState) -> Unit,
    onDismiss: () -> Unit,
) {
    val themeColors = MaterialTheme.colorScheme
    val currentTime = Calendar.getInstance()

    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = false,
    )

    /** Determines whether the time picker is dial or input */
    var showDial by remember { mutableStateOf(true) }

    /** The icon used for the icon button that switches from dial to input */
    val toggleIcon = if (showDial) {
        painterResource(R.drawable.edit_bulk)
    } else {
        painterResource(R.drawable.timer_bulk)
    }

    AdvancedTimePickerDialog(
        onDismiss = { onDismiss() },
        onConfirm = { onConfirm(timePickerState) },
        toggle = {
            IconButton(onClick = { showDial = !showDial }) {
                Icon(
                    painter = toggleIcon,
                    contentDescription = "Time picker type toggle",
                    tint = themeColors.inverseSurface,
                )
            }
        },
    ) {
        TimePickerOrInput(
            showDial = showDial,
            timePickerState = timePickerState,
            themeColors = themeColors
        )
    }
}

@Composable
fun AdvancedTimePickerDialog(
    title: String = "Select Time",
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    toggle: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val themeColors = MaterialTheme.colorScheme
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(modifier = Modifier
            .padding(horizontal = 20.dp)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            .background(color = themeColors.background, shape = RoundedCornerShape(30.dp))
        ){
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                tonalElevation = 6.dp,
                color = Color.Transparent,
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        text = title,
                        color = themeColors.inverseSurface,
                        style = MaterialTheme.typography.labelMedium
                    )
                    content()
                    Row(
                        modifier = Modifier
                            .height(40.dp)
                            .fillMaxWidth()
                    ) {
                        toggle()
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.padding(end = 10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.onBackground,
                                contentColor = MaterialTheme.colorScheme.inverseSurface
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) { Text(
                            text ="Cancel",
                            style = TextStyle(
                                fontFamily = iosFont,
                            )
                        ) }
                        TextButton(
                            onClick = onConfirm,
                            modifier = Modifier
                                .shadow(5.dp, RoundedCornerShape(15.dp), spotColor = MaterialTheme.colorScheme.primary, ambientColor = Color.Transparent),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "Okay",
                                modifier = Modifier.padding(horizontal = 10.dp),
                                style = TextStyle(
                                    fontFamily = iosFont,
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
@ExperimentalMaterial3Api
fun TimePickerOrInput(
    showDial: Boolean,
    timePickerState: TimePickerState,
    themeColors: ColorScheme
) {
    // Use AnimatedContent for smooth transitions
    AnimatedContent(
        targetState = showDial,
        transitionSpec = {
            // Slide transition with fade
            slideInVertically(
                initialOffsetY = { if (targetState) -it else it },
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutSlowInEasing
                )
            ) + fadeIn(
                animationSpec = tween(
                    durationMillis = 200,
                    delayMillis = 100,
                    easing = LinearEasing
                )
            ) togetherWith slideOutVertically(
                targetOffsetY = { if (targetState) it else -it },
                animationSpec = tween(
                    durationMillis = 300,
                    easing = FastOutSlowInEasing
                )
            ) + fadeOut(
                animationSpec = tween(
                    durationMillis = 200,
                    easing = LinearEasing
                )
            )
        },
        label = "TimePickerTransition"
    ) { showDialState ->
        if (showDialState) {
            TimePicker(
                state = timePickerState,
                colors = TimePickerDefaults.colors(
                    clockDialColor = themeColors.onBackground,
                    clockDialSelectedContentColor = Color.White,
                    clockDialUnselectedContentColor = themeColors.inverseSurface,
                    selectorColor = themeColors.primary,
                    timeSelectorSelectedContainerColor = themeColors.primary,
                    timeSelectorUnselectedContainerColor = themeColors.surfaceBright,
                    timeSelectorSelectedContentColor = themeColors.surface,
                    periodSelectorBorderColor = Color.Transparent,
                    timeSelectorUnselectedContentColor = themeColors.inverseSurface,
                    periodSelectorUnselectedContainerColor = themeColors.surfaceBright,
                    periodSelectorSelectedContainerColor = themeColors.onError.copy(0.5f),
                    periodSelectorSelectedContentColor = Color.White
                )
            )
        } else {
            TimeInput(
                state = timePickerState,
                colors = TimePickerDefaults.colors(
                    selectorColor = themeColors.primary,
                    timeSelectorSelectedContainerColor = themeColors.primary,
                    timeSelectorUnselectedContainerColor = themeColors.surfaceBright,
                    timeSelectorSelectedContentColor = themeColors.surface,
                    periodSelectorBorderColor = Color.Transparent,
                    timeSelectorUnselectedContentColor = themeColors.inverseSurface,
                    periodSelectorUnselectedContainerColor = themeColors.surfaceBright,
                    periodSelectorSelectedContainerColor = themeColors.onError.copy(0.5f),
                    periodSelectorSelectedContentColor = Color.White
                )
            )
        }
    }
}