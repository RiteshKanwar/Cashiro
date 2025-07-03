package com.ritesh.cashiro.presentation.ui.features.developer_options

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.overscroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.ritesh.cashiro.presentation.effects.VerticalStretchedOverscroll
import com.ritesh.cashiro.presentation.ui.extras.components.extras.CustomTitleTopAppBar
import com.ritesh.cashiro.presentation.ui.features.notifications.NotificationsViewModel
import com.ritesh.cashiro.presentation.ui.features.onboarding.OnBoardingViewModel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import com.ritesh.cashiro.MainActivity
import com.ritesh.cashiro.R
import com.ritesh.cashiro.presentation.ui.navigation.NavGraph
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Blue_Dim
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Green_Dim
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Maroon_Dim
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Mauve_Dim
import com.ritesh.cashiro.presentation.ui.theme.iosFont
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DeveloperOptionsScreen(
    onBackClicked: () -> Unit,
    screenTitle: String,
    previousScreenTitle: String,
    onBoardingViewModel: OnBoardingViewModel,
    navController: NavController
) {
    DeveloperOptionsContent(
        onBackClicked = onBackClicked,
        screenTitle = screenTitle,
        previousScreenTitle = previousScreenTitle,
        resetOnBoarding = onBoardingViewModel::resetOnBoarding,
        navController = navController
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeveloperOptionsContent(
    onBackClicked: () -> Unit,
    screenTitle: String,
    previousScreenTitle: String,
    resetOnBoarding: suspend () -> Unit,
    navController: NavController
){
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // State for toggles
    var testNotificationsEnabled by remember { mutableStateOf(false) }

    val scrollOverScrollState = rememberLazyListState()
    val overscrollEffect = remember(coroutineScope) { VerticalStretchedOverscroll(coroutineScope) }

    // Scaffold State for Scroll Behavior
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scrollBehaviorSmall = TopAppBarDefaults.pinnedScrollBehavior()

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
            // Header Section
            item {
                Card(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
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
                                painter = painterResource(id = R.drawable.settings_bulk),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Developer Options",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "These options are for testing and debugging purposes only. Use with caution.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // Testing Section
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
                        // Test Notifications Toggle
                        DeveloperToggleOption(
                            showIcon = true,
                            iconResID = R.drawable.notification,
                            iconBackgroundColor = Macchiato_Blue_Dim,
                            label = "Test Notification Alerts",
                            subtitle = "Send a test notification immediately",
                            isChecked = testNotificationsEnabled,
                            onToggle = { enabled ->
                                testNotificationsEnabled = enabled
                                if (enabled) {
                                    // Send test notification
                                    sendTestNotification(context)
                                    // Auto-disable after a short delay
                                    coroutineScope.launch {
                                        delay(2000)
                                        testNotificationsEnabled = false
                                    }
                                }
                            },
                            isLast = false
                        )

                        // Reset OnBoarding Button
                        DeveloperActionOption(
                            showIcon = true,
                            iconResID = R.drawable.refresh_bulk,
                            iconBackgroundColor = Macchiato_Maroon_Dim,
                            label = "Reset OnBoarding",
                            subtitle = "Clear onboarding status",
                            buttonText = "Reset",
                            isLast = true,
                            onAction = {
                                coroutineScope.launch {
                                    try {
                                        resetOnBoarding()
                                        // Navigate to onboarding screen
                                        navController.navigate(NavGraph.ONBOARDING) {
                                            popUpTo(0) { inclusive = true } // Clear all back stack
                                        }
                                    } catch (e: Exception) {
                                        Log.e("DeveloperOptions", "Failed to reset onboarding: ${e.message}")
                                    }
                                }
                            }
                        )
                    }
                }
            }

            // Additional Testing Section (Future expansion)
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
                        // Future options can be added here
                        DeveloperInfoOption(
                            showIcon = true,
                            iconResID = R.drawable.information_bulk,
                            iconBackgroundColor = Macchiato_Green_Dim,
                            label = "App Version",
                            value = "1.0.0", // You can make this dynamic
                            isLast = false
                        )

                        DeveloperInfoOption(
                            showIcon = true,
                            iconResID = R.drawable.calendar_tick_upcoming,
                            iconBackgroundColor = Macchiato_Mauve_Dim,
                            label = "Build Date",
                            value = "January 2025", // You can make this dynamic
                            isLast = true
                        )
                    }
                }
            }
        }
    }
}
@Composable
private fun DeveloperToggleOption(
    showIcon: Boolean = false,
    @DrawableRes iconResID: Int = 0,
    iconBackgroundColor: Color = Color.Transparent,
    label: String,
    subtitle: String? = null,
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
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = label,
                        textAlign = TextAlign.Start,
                        fontFamily = iosFont,
                        fontSize = 16.sp,
                        color = themeColors.inverseSurface
                    )
                    subtitle?.let {
                        Text(
                            text = it,
                            textAlign = TextAlign.Start,
                            fontFamily = iosFont,
                            fontSize = 12.sp,
                            color = themeColors.inverseSurface.copy(0.6f)
                        )
                    }
                }
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
private fun DeveloperActionOption(
    showIcon: Boolean = false,
    @DrawableRes iconResID: Int = 0,
    iconBackgroundColor: Color = Color.Transparent,
    label: String,
    subtitle: String? = null,
    buttonText: String,
    isLast: Boolean = false,
    onAction: () -> Unit,
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
                modifier = Modifier.weight(1f),
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
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = label,
                        textAlign = TextAlign.Start,
                        fontFamily = iosFont,
                        fontSize = 16.sp,
                        color = themeColors.inverseSurface
                    )
                    subtitle?.let {
                        Text(
                            text = it,
                            textAlign = TextAlign.Start,
                            fontFamily = iosFont,
                            fontSize = 12.sp,
                            color = themeColors.inverseSurface.copy(0.6f)
                        )
                    }
                }
            }

            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColors.primary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.padding(4.dp).weight(0.5f)
            ) {
                Text(
                    text = buttonText,
                    fontFamily = iosFont,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                    fontSize = 14.sp
                )
            }
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
private fun DeveloperInfoOption(
    showIcon: Boolean = false,
    @DrawableRes iconResID: Int = 0,
    iconBackgroundColor: Color = Color.Transparent,
    label: String,
    value: String,
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
                    fontSize = 16.sp,
                    color = themeColors.inverseSurface,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Text(
                text = value,
                textAlign = TextAlign.End,
                fontFamily = iosFont,
                fontSize = 14.sp,
                color = themeColors.inverseSurface.copy(0.6f),
                modifier = Modifier.padding(10.dp)
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

private fun sendTestNotification(context: Context) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Create notification channel for Android 8.0+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            "developer_test",
            "Developer Test Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Test notifications from developer options"
        }
        notificationManager.createNotificationChannel(channel)
    }

    // Create intent to open the app
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
    }

    val pendingIntent = PendingIntent.getActivity(
        context,
        9999,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // Create test notification
    val notification = NotificationCompat.Builder(context, "developer_test")
        .setSmallIcon(R.drawable.statusbar_notification_icon)
        .setContentTitle("Test Notification")
        .setContentText("This is a test notification from Developer Options!")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)
        .build()

    notificationManager.notify(9999, notification)
}

//@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
//@Composable
//fun DeveloperOptionsScreen(
//    onBackClicked: () -> Unit,
//    screenTitle: String,
//    previousScreenTitle: String,
//    onBoardingViewModel: OnBoardingViewModel,
//    notificationsViewModel: NotificationsViewModel,
//    navController: NavController
//) {
//    val context = LocalContext.current
//    val coroutineScope = rememberCoroutineScope()
//
//    // State for toggles
//    var testNotificationsEnabled by remember { mutableStateOf(false) }
//
//    val scrollOverScrollState = rememberLazyListState()
//    val overscrollEffect = remember(coroutineScope) { VerticalStretchedOverscroll(coroutineScope) }
//
//    // Scaffold State for Scroll Behavior
//    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
//    val scrollBehaviorSmall = TopAppBarDefaults.pinnedScrollBehavior()
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
//            // Header Section
//            item {
//                Card(
//                    modifier = Modifier
//                        .padding(horizontal = 20.dp)
//                        .fillMaxWidth(),
//                    colors = CardDefaults.cardColors(
//                        containerColor = MaterialTheme.colorScheme.primaryContainer
//                    )
//                ) {
//                    Column(
//                        modifier = Modifier.padding(16.dp)
//                    ) {
//                        Row(
//                            verticalAlignment = Alignment.CenterVertically,
//                            horizontalArrangement = Arrangement.spacedBy(8.dp)
//                        ) {
//                            Icon(
//                                painter = painterResource(id = R.drawable.settings_bulk),
//                                contentDescription = null,
//                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
//                                modifier = Modifier.size(20.dp)
//                            )
//                            Text(
//                                text = "Developer Options",
//                                fontSize = 16.sp,
//                                fontWeight = FontWeight.SemiBold,
//                                color = MaterialTheme.colorScheme.onPrimaryContainer
//                            )
//                        }
//
//                        Spacer(modifier = Modifier.height(8.dp))
//
//                        Text(
//                            text = "These options are for testing and debugging purposes only. Use with caution.",
//                            fontSize = 14.sp,
//                            color = MaterialTheme.colorScheme.onPrimaryContainer,
//                            lineHeight = 20.sp
//                        )
//                    }
//                }
//            }
//
//            // Testing Section
//            item {
//                Box(
//                    modifier = Modifier
//                        .padding(horizontal = 20.dp)
//                        .background(
//                            color = MaterialTheme.colorScheme.surface,
//                            shape = RoundedCornerShape(16.dp)
//                        )
//                ) {
//                    Column {
//                        // Test Notifications Toggle
//                        DeveloperToggleOption(
//                            showIcon = true,
//                            iconResID = R.drawable.notification,
//                            iconBackgroundColor = Macchiato_Blue_Dim,
//                            label = "Test Notification Alerts",
//                            subtitle = "Send a test notification immediately",
//                            isChecked = testNotificationsEnabled,
//                            onToggle = { enabled ->
//                                testNotificationsEnabled = enabled
//                                if (enabled) {
//                                    // Send test notification
//                                    sendTestNotification(context)
//                                    // Auto-disable after a short delay
//                                    coroutineScope.launch {
//                                        delay(2000)
//                                        testNotificationsEnabled = false
//                                    }
//                                }
//                            },
//                            isLast = false
//                        )
//
//                        // Reset OnBoarding Button
//                        DeveloperActionOption(
//                            showIcon = true,
//                            iconResID = R.drawable.refresh_bulk,
//                            iconBackgroundColor = Macchiato_Maroon_Dim,
//                            label = "Reset OnBoarding",
//                            subtitle = "Clear onboarding status",
//                            buttonText = "Reset",
//                            isLast = true,
//                            onAction = {
//                                coroutineScope.launch {
//                                    try {
//                                        onBoardingViewModel.resetOnBoarding()
//                                        // Navigate to onboarding screen
//                                        navController.navigate(NavGraph.ONBOARDING) {
//                                            popUpTo(0) { inclusive = true } // Clear all back stack
//                                        }
//                                    } catch (e: Exception) {
//                                        Log.e("DeveloperOptions", "Failed to reset onboarding: ${e.message}")
//                                    }
//                                }
//                            }
//                        )
//                    }
//                }
//            }
//
//            // Additional Testing Section (Future expansion)
//            item {
//                Box(
//                    modifier = Modifier
//                        .padding(horizontal = 20.dp)
//                        .background(
//                            color = MaterialTheme.colorScheme.surface,
//                            shape = RoundedCornerShape(16.dp)
//                        )
//                ) {
//                    Column {
//                        // Future options can be added here
//                        DeveloperInfoOption(
//                            showIcon = true,
//                            iconResID = R.drawable.information_bulk,
//                            iconBackgroundColor = Macchiato_Green_Dim,
//                            label = "App Version",
//                            value = "1.0.0", // You can make this dynamic
//                            isLast = false
//                        )
//
//                        DeveloperInfoOption(
//                            showIcon = true,
//                            iconResID = R.drawable.calendar_tick_upcoming,
//                            iconBackgroundColor = Macchiato_Mauve_Dim,
//                            label = "Build Date",
//                            value = "January 2025", // You can make this dynamic
//                            isLast = true
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun DeveloperToggleOption(
//    showIcon: Boolean = false,
//    @DrawableRes iconResID: Int = 0,
//    iconBackgroundColor: Color = Color.Transparent,
//    label: String,
//    subtitle: String? = null,
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
//                Column(modifier = Modifier.padding(10.dp)) {
//                    Text(
//                        text = label,
//                        textAlign = TextAlign.Start,
//                        fontFamily = iosFont,
//                        fontSize = 16.sp,
//                        color = themeColors.inverseSurface
//                    )
//                    subtitle?.let {
//                        Text(
//                            text = it,
//                            textAlign = TextAlign.Start,
//                            fontFamily = iosFont,
//                            fontSize = 12.sp,
//                            color = themeColors.inverseSurface.copy(0.6f)
//                        )
//                    }
//                }
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
//private fun DeveloperActionOption(
//    showIcon: Boolean = false,
//    @DrawableRes iconResID: Int = 0,
//    iconBackgroundColor: Color = Color.Transparent,
//    label: String,
//    subtitle: String? = null,
//    buttonText: String,
//    isLast: Boolean = false,
//    onAction: () -> Unit,
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
//                modifier = Modifier.weight(1f),
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
//                Column(modifier = Modifier.padding(10.dp)) {
//                    Text(
//                        text = label,
//                        textAlign = TextAlign.Start,
//                        fontFamily = iosFont,
//                        fontSize = 16.sp,
//                        color = themeColors.inverseSurface
//                    )
//                    subtitle?.let {
//                        Text(
//                            text = it,
//                            textAlign = TextAlign.Start,
//                            fontFamily = iosFont,
//                            fontSize = 12.sp,
//                            color = themeColors.inverseSurface.copy(0.6f)
//                        )
//                    }
//                }
//            }
//
//            Button(
//                onClick = onAction,
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = themeColors.primary,
//                    contentColor = Color.White
//                ),
//                shape = RoundedCornerShape(12.dp),
//                modifier = Modifier.padding(4.dp).weight(0.5f)
//            ) {
//                Text(
//                    text = buttonText,
//                    fontFamily = iosFont,
//                    overflow = TextOverflow.Ellipsis,
//                    maxLines = 2,
//                    fontSize = 14.sp
//                )
//            }
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
//private fun DeveloperInfoOption(
//    showIcon: Boolean = false,
//    @DrawableRes iconResID: Int = 0,
//    iconBackgroundColor: Color = Color.Transparent,
//    label: String,
//    value: String,
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
//                    fontSize = 16.sp,
//                    color = themeColors.inverseSurface,
//                    modifier = Modifier.padding(10.dp)
//                )
//            }
//
//            Text(
//                text = value,
//                textAlign = TextAlign.End,
//                fontFamily = iosFont,
//                fontSize = 14.sp,
//                color = themeColors.inverseSurface.copy(0.6f),
//                modifier = Modifier.padding(10.dp)
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
//private fun sendTestNotification(context: Context) {
//    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//    // Create notification channel for Android 8.0+
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//        val channel = NotificationChannel(
//            "developer_test",
//            "Developer Test Notifications",
//            NotificationManager.IMPORTANCE_DEFAULT
//        ).apply {
//            description = "Test notifications from developer options"
//        }
//        notificationManager.createNotificationChannel(channel)
//    }
//
//    // Create intent to open the app
//    val intent = Intent(context, MainActivity::class.java).apply {
//        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
//    }
//
//    val pendingIntent = PendingIntent.getActivity(
//        context,
//        9999,
//        intent,
//        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//    )
//
//    // Create test notification
//    val notification = NotificationCompat.Builder(context, "developer_test")
//        .setSmallIcon(R.drawable.statusbar_notification_icon)
//        .setContentTitle("Test Notification")
//        .setContentText("This is a test notification from Developer Options!")
//        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
//        .setContentIntent(pendingIntent)
//        .setAutoCancel(true)
//        .build()
//
//    notificationManager.notify(9999, notification)
//}