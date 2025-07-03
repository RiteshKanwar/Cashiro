package com.ritesh.cashiro.presentation.ui.features.settings

import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.ritesh.cashiro.R
import com.ritesh.cashiro.data.local.entity.TransactionEntity
import com.ritesh.cashiro.presentation.effects.VerticalStretchedOverscroll
import com.ritesh.cashiro.presentation.ui.extras.components.extras.CustomTitleTopAppBar
import com.ritesh.cashiro.presentation.ui.extras.components.settings.OptionsComponent
import com.ritesh.cashiro.presentation.ui.features.add_transaction.AddTransactionScreenViewModel
import com.ritesh.cashiro.presentation.ui.features.profile.ProfileScreenState
import com.ritesh.cashiro.presentation.ui.features.profile.ProfileScreenViewModel
import com.ritesh.cashiro.presentation.ui.navigation.NavGraph
import com.ritesh.cashiro.presentation.ui.theme.ErrorColor
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Blue_Dim
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Green_Dim
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Maroon_Dim
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Mauve_Dim
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Red_Dim
import com.ritesh.cashiro.presentation.ui.theme.iosFont

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SettingsScreen (
    profileScreenViewModel: ProfileScreenViewModel,
    transactionViewModel: AddTransactionScreenViewModel,
    navController: NavController,
    screenTitle: String,
) {
    val profileState = profileScreenViewModel.state.collectAsState()
    val screenState by transactionViewModel.state.collectAsState()
    val transactions = screenState.transactions
    SettingsScreenContent(
        profileUiState = profileState.value,
        transactions = transactions,
        navController = navController,
        screenTitle = screenTitle,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsScreenContent(
    profileUiState: ProfileScreenState,
    transactions: List<TransactionEntity>,
    navController: NavController,
    screenTitle: String,
){
    val scrollOverScrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
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
                )
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(16.dp)),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                ProfileCard(
                    userName = profileUiState.userName,
                    profileImageUri = profileUiState.profileImageUri,
                    profileBackgroundColor = profileUiState.profileBackgroundColor,
                    totalTransactions = transactions.size,
                    navController = navController,
                )
            }

            // App Settings Section
            item {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "App Settings",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = iosFont,
                        color = MaterialTheme.colorScheme.inverseSurface.copy(0.7f),
                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                    )

                    OptionsComponent(
                        showIcon = true,
                        iconResID = R.drawable.themes_bulk,
                        iconBackgroundColor = Macchiato_Red_Dim,
                        label = "Appearance",
                        hasArrow = true,
                        isFirstItem = true,
                        modifier = Modifier
                            .clickable(
                                onClick = {
                                    navController.navigate(NavGraph.APPEARANCE) {
                                        launchSingleTop = true
                                    }
                                },
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            )
                    )

                    OptionsComponent(
                        showIcon = true,
                        iconResID = R.drawable.category_bulk,
                        iconBackgroundColor = Macchiato_Blue_Dim,
                        label = "Categories",
                        hasArrow = true,
                        modifier = Modifier
                            .clickable(
                                onClick = {
                                    navController.navigate(NavGraph.CATEGORIES) {
                                        launchSingleTop = true
                                    }
                                },
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            )
                    )

                    OptionsComponent(
                        showIcon = true,
                        iconResID = R.drawable.wallet_bulk,
                        iconBackgroundColor = Macchiato_Mauve_Dim,
                        label = "Accounts",
                        hasArrow = true,
                        modifier = Modifier
                            .clickable(
                                onClick = {
                                    navController.navigate(NavGraph.ACCOUNTS) {
                                        launchSingleTop = true
                                    }
                                },
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            )
                    )

                    OptionsComponent(
                        showIcon = true,
                        iconResID = R.drawable.notification,
                        iconBackgroundColor = Macchiato_Maroon_Dim,
                        label = "Notifications",
                        hasArrow = true,
                        isLastItem = true,
                        modifier = Modifier
                            .clickable(
                                onClick = {
                                    navController.navigate(NavGraph.NOTIFICATIONS) {
                                        launchSingleTop = true
                                    }
                                },
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            )
                    )
                }
            }

            // Financial Management Section
            item {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Financial Management",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = iosFont,
                        color = MaterialTheme.colorScheme.inverseSurface.copy(0.7f),
                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                    )

                    OptionsComponent(
                        showIcon = true,
                        iconResID = R.drawable.notifications_reminder, // Subscription icon
                        iconBackgroundColor = Macchiato_Blue_Dim,
                        label = "Subscriptions",
                        hasArrow = true,
                        isFirstItem = true,
                        modifier = Modifier
                            .clickable(
                                onClick = {
                                    navController.navigate(NavGraph.SUBSCRIPTIONS) {
                                        launchSingleTop = true
                                    }
                                },
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            )

                    )

                    OptionsComponent(
                        showIcon = true,
                        iconResID = R.drawable.notification, // Schedule icon
                        iconBackgroundColor = Macchiato_Green_Dim,
                        label = "Schedule",
                        hasArrow = true,
                        modifier = Modifier
                            .clickable(
                                onClick = {
                                    navController.navigate(NavGraph.SCHEDULE) {
                                        launchSingleTop = true
                                    }
                                },
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            )
                    )

                    OptionsComponent(
                        showIcon = true,
                        iconResID = R.drawable.transactions_filled, // Activity log icon
                        iconBackgroundColor = Macchiato_Mauve_Dim,
                        label = "Activity Log",
                        hasArrow = true,
                        isLastItem = true,
                        modifier = Modifier
                            .clickable(
                                onClick = {
                                    navController.navigate(NavGraph.ACTIVITY_LOG) {
                                        launchSingleTop = true
                                    }
                                },
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            )
                    )
                }
            }


            // Data & Security Section
            item {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Text(
                        text = "Data & Security",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = iosFont,
                        color = MaterialTheme.colorScheme.inverseSurface.copy(0.7f),
                        modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                    )

                    OptionsComponent(
                        showIcon = true,
                        iconResID = R.drawable.archive,
                        iconBackgroundColor = Macchiato_Green_Dim,
                        label = "Backup & Restore",
                        hasArrow = true,
                        isFirstItem = true,
                        modifier = Modifier
                            .clickable(
                                onClick = {
                                    navController.navigate(NavGraph.DATA_MANAGEMENT) {
                                        launchSingleTop = true
                                    }
                                },
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            )
                    )
                    OptionsComponent(
                        showIcon = true,
                        iconResID = R.drawable.delete_bulk,
                        iconBackgroundColor = ErrorColor,
                        label = "Developer Options",
                        isLastItem = true,
                        isDestructive = true,
                        hasArrow = true,
                        modifier = Modifier
                            .clickable(
                                onClick = {
                                    navController.navigate(NavGraph.DEVELOPER_OPTIONS) {
                                        launchSingleTop = true
                                    }
                                },
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            )
                    )
                }
            }
            item {
                 Spacer(modifier = Modifier.padding(bottom = 100.dp))
            }
        }
    }
}

@Composable
private fun ProfileCard(
    userName: String?,
    profileImageUri: Uri?,
    profileBackgroundColor: Color,
    totalTransactions: Int,
    navController: NavController,
) {
    Box(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(10.dp)
            .clickable(
                onClick = {
                    navController.navigate(NavGraph.PROFILE) {
                        launchSingleTop = true
                    }
                }
            ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(color = profileBackgroundColor)
            ) {
                AsyncImage(
                    model = profileImageUri ?: R.drawable.avatar_1,
                    contentDescription = null,
                    modifier = Modifier
                        .zIndex(5f)
                        .align(Alignment.BottomCenter),
                    contentScale = ContentScale.Crop
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = userName ?: "User Name",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.inverseSurface
                )
                Text(
                    text = "$totalTransactions Transactions",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.inverseSurface.copy(0.5f)
                )
            }
        }
    }
}

@Preview(
    name = "Settings Screen",
    showBackground = false,
)
@Composable
private fun SplashScreenPreview() {
    MaterialTheme {
        Surface(
            color = MaterialTheme.colorScheme.background
        ){
            SettingsScreenContent(
                profileUiState = ProfileScreenState(),
                transactions = emptyList(),
                navController = NavController(LocalContext.current),
                screenTitle = "Settings"
            )
        }
    }
}