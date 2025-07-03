package com.ritesh.cashiro.presentation.ui.features.home

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.ritesh.cashiro.data.local.entity.AccountEntity
import com.ritesh.cashiro.data.local.entity.TransactionEntity
import com.ritesh.cashiro.data.currency.model.CurrencySymbols
import com.ritesh.cashiro.domain.utils.CurrencyUtils
import com.ritesh.cashiro.domain.utils.icons
import com.ritesh.cashiro.presentation.effects.BlurAnimatedTextCountUpWithCurrency
import com.ritesh.cashiro.presentation.effects.BlurredAnimatedVisibility
import com.ritesh.cashiro.presentation.effects.VerticalStretchedOverscroll
import com.ritesh.cashiro.presentation.ui.extras.components.account.AccountSwipeableCards
import com.ritesh.cashiro.presentation.ui.extras.components.extras.CustomTitleTopAppBar
import com.ritesh.cashiro.presentation.ui.extras.components.charts.TransactionDataType
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenViewModel
import com.ritesh.cashiro.presentation.ui.features.add_transaction.AddTransactionScreenViewModel
import com.ritesh.cashiro.presentation.ui.features.appearance.AppearanceViewModel
import com.ritesh.cashiro.presentation.ui.features.categories.CategoryScreenEvent
import com.ritesh.cashiro.presentation.ui.features.categories.CategoryScreenViewModel
import com.ritesh.cashiro.presentation.ui.features.categories.SubCategoryViewModel
import com.ritesh.cashiro.presentation.ui.features.profile.ProfileScreenState
import com.ritesh.cashiro.presentation.ui.features.profile.ProfileScreenViewModel
import com.ritesh.cashiro.presentation.ui.features.transactions.TransactionsDisplayList
import com.ritesh.cashiro.presentation.ui.navigation.NavGraph
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Mauve_Dim
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Red_Dim
import com.ritesh.cashiro.presentation.ui.theme.iosFont
import com.ritesh.cashiro.R
import com.ritesh.cashiro.domain.repository.Settings
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenEvent
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenState
import com.ritesh.cashiro.presentation.ui.features.add_transaction.AddTransactionEvent
import com.ritesh.cashiro.presentation.ui.features.categories.CategoryScreenState
import com.ritesh.cashiro.presentation.ui.features.categories.SubCategoryEvent
import com.ritesh.cashiro.presentation.ui.features.categories.SubCategoryState
import dev.chrisbanes.haze.HazeDefaults
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun SharedTransitionScope.HomeScreen(
    accountViewModel: AccountScreenViewModel,
    categoryViewModel: CategoryScreenViewModel,
    subCategoryViewModel: SubCategoryViewModel,
    transactionViewModel: AddTransactionScreenViewModel,
    profileScreenViewModel: ProfileScreenViewModel,
    appearanceViewModel: AppearanceViewModel,
    navController: NavController,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onBackClicked: () -> Unit,
    screenTitle: String,
) {
    // Create HomeScreenViewModel using factory
    val factory = HomeScreenViewModelFactory(
        accountViewModel,
        transactionViewModel,
        profileScreenViewModel
    )
    val homeViewModel: HomeScreenViewModel = viewModel(factory = factory)
    val homeUiState by homeViewModel.state.collectAsState()
    val categoryUiState by categoryViewModel.state.collectAsState()
    val subCategoryUiState by subCategoryViewModel.state.collectAsState()
    val accountUiState by accountViewModel.state.collectAsState()
    val profileUiState by profileScreenViewModel.state.collectAsState()
    val appearanceUiState by appearanceViewModel.settings.collectAsState()

    HomeScreenContent(
        onBackClicked = onBackClicked,
        screenTitle = screenTitle,
        homeUiState = homeUiState,
        updateCollapsingFraction = homeViewModel::updateCollapsingFraction,
        updateSelectedDataType = homeViewModel::updateSelectedDataType,
        getUpcomingTransactionCount = homeViewModel::getUpcomingTransactionCount,
        getOverdueTransactionCount = homeViewModel::getOverdueTransactionCount,
        toggleExpanded = homeViewModel::toggleExpanded,
        getSelectedTypeName = homeViewModel::getSelectedTypeName,
        getSelectedTransactions = homeViewModel::getSelectedTransactions,
        categoryUiState = categoryUiState,
        subCategoryUiState = subCategoryUiState,
        accountUiState = accountUiState,
        setAccountAsMain = accountViewModel::setAccountAsMain,
        updateAccounts = accountViewModel::updateAccounts,
        profileUiState = profileUiState,
        appearanceUiState = appearanceUiState,
        animatedVisibilityScope = animatedVisibilityScope,
        navController = navController,
        onCategoryEvent = categoryViewModel::onEvent,
        onSubCategoryEvent = subCategoryViewModel::onEvent,
        onAccountEvent = accountViewModel::onEvent,
        onAddTransactionEvent = transactionViewModel::onEvent,
    )

}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.HomeScreenContent(
    onBackClicked: () -> Unit,
    screenTitle: String,
    homeUiState: HomeScreenState,
    updateCollapsingFraction: (Float) -> Unit,
    updateSelectedDataType: (TransactionDataType) -> Unit,
    getUpcomingTransactionCount: () -> Int,
    getOverdueTransactionCount: () -> Int,
    toggleExpanded: () -> Unit,
    getSelectedTypeName: () -> String,
    getSelectedTransactions: () -> List<TransactionEntity>,
    categoryUiState: CategoryScreenState,
    onCategoryEvent: (CategoryScreenEvent) -> Unit,
    subCategoryUiState: SubCategoryState,
    onSubCategoryEvent: (SubCategoryEvent) -> Unit,
    accountUiState: AccountScreenState,
    onAccountEvent: (AccountScreenEvent) -> Unit,
    setAccountAsMain: (AccountEntity) -> Unit,
    updateAccounts: (List<AccountEntity>) -> Unit,
    onAddTransactionEvent: (AddTransactionEvent) -> Unit,
    profileUiState: ProfileScreenState,
    appearanceUiState: Settings,
    animatedVisibilityScope: AnimatedVisibilityScope,
    navController: NavController,
){
    val conversionRates = accountUiState.mainCurrencyConversionRates
    val categories = categoryUiState.categories
    val subCategories = subCategoryUiState.subCategories


    // Theme colors for easy access
    val themeColors = MaterialTheme.colorScheme

    // Scaffold State for Scroll Behavior
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scrollBehaviorSmall = TopAppBarDefaults.pinnedScrollBehavior()
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val overscrollEffect = remember(coroutineScope) { VerticalStretchedOverscroll(coroutineScope) }

    // For haptic feedback
    val haptic = LocalHapticFeedback.current

    // Get the current height of the TopAppBar from the scroll behavior
    val topAppBarHeightPx = scrollBehavior.state.heightOffset

    // Update collapsing fraction in ViewModel
    LaunchedEffect(scrollBehavior.state.collapsedFraction) {
        updateCollapsingFraction(scrollBehavior.state.collapsedFraction)
    }

    // Function to handle transaction selection - simplified in this state management approach
    val toggleTransactionSelection: (TransactionEntity) -> Unit = { transaction ->
        Log.d("Selection", "Regular click, not in selection mode")
    }

    val hazeState = remember { HazeState() }
    val hazeStateForPopupButton = remember { HazeState() }

    Box(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        contentAlignment = Alignment.TopCenter
    ) {

        Box(
            modifier = Modifier.fillMaxWidth().haze(state = hazeState)
        ) {

            AnimatedVisibility(
                visible = appearanceUiState.showProfileBanner,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                ) {
                    ProfileBannerPhoto(
                        profileState = profileUiState,
                        navController = navController,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .padding(
                        start = 15.dp,
                        end = 15.dp
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(Modifier.offset(y = homeUiState.currentOffset)) {
                        AccountSwipeableCards(
                            modifier = Modifier,
                            accounts = homeUiState.accounts,
                            themeColors = themeColors,
                            onAccountReordered = { reorderedAccounts ->
                                updateAccounts(reorderedAccounts)
                            },
                            navController = navController,
                            animatedVisibilityScope = animatedVisibilityScope,
                            setAccountAsMain = setAccountAsMain,
                            accountUiState = accountUiState,
                        )

                        UpdatedCardsSection(
                            homeUiState = homeUiState,
                            getUpcomingTransactionCount = getUpcomingTransactionCount,
                            getOverdueTransactionCount = getOverdueTransactionCount,
                            themeColors = themeColors,
                            onCategoryEvent = onCategoryEvent,
                            onSubCategoryEvent = onSubCategoryEvent,
                        )

                        RecentTransactions(
                            homeUiState = homeUiState,
                            toggleExpanded = toggleExpanded,
                            getSelectedTypeName = getSelectedTypeName,
                            getSelectedTransactions = getSelectedTransactions,
                            overscrollEffect = overscrollEffect,
                            animatedVisibilityScope = animatedVisibilityScope,
                            toggleTransactionSelection = toggleTransactionSelection,
                            navController = navController,
                            hazeStateForPopupButton = hazeStateForPopupButton,
                            themeColors = themeColors,
                            accountUiState = accountUiState,
                            onAccountEvent = onAccountEvent,
                            onAddTransactionEvent = onAddTransactionEvent,
                            onCategoryEvent = onCategoryEvent,
                            onSubCategoryEvent = onSubCategoryEvent,
                        )

                    }
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }

            // Dropdown menu for chart type selection (existing code remains the same)
            BlurredAnimatedVisibility(
                visible = homeUiState.expanded,
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
                    .padding(top = 450.dp + homeUiState.currentOffset, end = 28.dp)
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
                        .sizeIn(maxWidth = 120.dp, maxHeight = 220.dp)
                        .align(Alignment.TopEnd),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceBright.copy(0.8f),
                        contentColor = MaterialTheme.colorScheme.inverseSurface.copy(0.6f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .wrapContentWidth()
                            .heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp)
                    ) {
                        // Dropdown menu options (existing code remains the same)
                        item {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "All Recent",
                                        fontSize = 12.sp,
                                        fontFamily = iosFont,
                                        overflow = TextOverflow.Ellipsis,
                                        maxLines = 1,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                },
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    updateSelectedDataType(TransactionDataType.ALL)
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = MaterialTheme.colorScheme.inverseSurface.copy(0.7f),
                                )
                            )
                        }
                        item { HorizontalDivider(color = MaterialTheme.colorScheme.inverseSurface.copy(0.2f)) }
                        item {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Expenses",
                                        fontSize = 12.sp,
                                        fontFamily = iosFont,
                                        overflow = TextOverflow.Ellipsis,
                                        maxLines = 1,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                },
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    updateSelectedDataType(TransactionDataType.EXPENSES)
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = MaterialTheme.colorScheme.inverseSurface.copy(0.7f),
                                )
                            )
                        }
                        item { HorizontalDivider(color = MaterialTheme.colorScheme.inverseSurface.copy(0.2f)) }
                        item {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Income",
                                        fontSize = 12.sp,
                                        fontFamily = iosFont,
                                        overflow = TextOverflow.Ellipsis,
                                        maxLines = 1,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                },
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    updateSelectedDataType(TransactionDataType.INCOME)
                                },
                                colors = MenuDefaults.itemColors(
                                    textColor = MaterialTheme.colorScheme.inverseSurface.copy(0.7f),
                                )
                            )
                        }
                    }
                }
            }
        }
        CustomTitleTopAppBar(
            scrollBehaviorLarge = scrollBehavior,
            scrollBehaviorSmall = scrollBehaviorSmall,
            title = screenTitle,
            onBackClick = onBackClicked,
            greetingCard = {
                GreetingCard(
                    profileUiState = profileUiState,
                    navController = navController,
                    accounts = homeUiState.accounts,
                    mainCurrencyCode = homeUiState.mainCurrencyCode,
                    conversionRates = conversionRates,
                    themeColors = themeColors
                )
            },
            profilePhoto = {
                ProfilePhoto(
                    profileState = profileUiState,
                    navController = navController,
                    size = 40.dp,
                    showBorder = true
                )
            },
            modifier = Modifier,
            hazeState = hazeState,
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.RecentTransactions(
    homeUiState: HomeScreenState,
    toggleExpanded:() -> Unit,
    getSelectedTypeName: () -> String,
    getSelectedTransactions: () -> List<TransactionEntity>,
    accountUiState: AccountScreenState,
    onAccountEvent: (AccountScreenEvent) -> Unit,
    onAddTransactionEvent: (AddTransactionEvent) -> Unit,
    onCategoryEvent: (CategoryScreenEvent) -> Unit,
    onSubCategoryEvent: (SubCategoryEvent) -> Unit,
    overscrollEffect: VerticalStretchedOverscroll,
    animatedVisibilityScope: AnimatedVisibilityScope,
    toggleTransactionSelection: (TransactionEntity) -> Unit,
    navController: NavController,
    hazeStateForPopupButton: HazeState,
    themeColors: ColorScheme,
){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .zIndex(2f)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            themeColors.background
                        )
                    )
                )
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Transactions",
                textAlign = TextAlign.Start,
                fontSize = 16.sp,
                fontFamily = iosFont,
                fontWeight = FontWeight.Medium,
                color = themeColors.inverseSurface,
            )

            Box(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {toggleExpanded() }
                    )
            ) {
                Row(
                    modifier = Modifier
                        .background(
                            color = themeColors.surface,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = getSelectedTypeName(),
                        fontFamily = iosFont,
                        lineHeight = 12.sp,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = themeColors.inverseSurface.copy(0.7f)
                    )
                }
            }
        }

        TransactionsDisplayList(
            modifier = Modifier
                .zIndex(1f)
                .height(400.dp + homeUiState.currentOffset)
                .clipToBounds()
                .background(color = themeColors.background)
                .haze(hazeStateForPopupButton),
            transactions = getSelectedTransactions(),
            accounts = homeUiState.accounts,
            themeColors = themeColors,
            onTransactionClick = toggleTransactionSelection,
            scrollOverScrollState = rememberLazyListState(),
            overscrollEffect = overscrollEffect,
            navController = navController,
            animatedVisibilityScope = animatedVisibilityScope,
            showTransactionCashFlowHeader = false,
            showTransactionCashFlowBottomDetails = false,
            showViewAllTransactionButton = true,
            showMonthInDateHeader = true,
            accountUiState = accountUiState,
            onAccountEvent = onAccountEvent,
            onAddTransactionEvent = onAddTransactionEvent,
            onCategoryEvent = onCategoryEvent,
            onSubCategoryEvent = onSubCategoryEvent,
        )
    }

}

@Composable
private fun GreetingCard(
    profileUiState: ProfileScreenState,
    navController: NavController,
    accounts: List<AccountEntity>,
    mainCurrencyCode: String,
    conversionRates:  Map<String, Double>,
    themeColors: ColorScheme,
){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ProfilePhoto(
            profileState = profileUiState,
            navController = navController,
            showBorder = true
        )
        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.Start
        ) {
            DynamicGreeting(
                userName = profileUiState.userName,
                themeColors = themeColors,
                totalBalance = CurrencyUtils.calculateNetWorth(accounts, mainCurrencyCode, conversionRates),
                mainCurrencyCode = mainCurrencyCode
            )
        }

    }
}

@Composable
fun ProfilePhoto(
    profileState: ProfileScreenState,
    navController: NavController,
    size: Dp = 56.dp,
    showBorder: Boolean = false
){
    Box(
        modifier = Modifier
            .padding(end = 10.dp)
            .size(size)
            .clip(CircleShape)
            .background(color = profileState.profileBackgroundColor)
            .clickable(
                onClick = { navController.navigate(NavGraph.PROFILE) }
            )
    ){
        AsyncImage(
            model = profileState.profileImageUri?: R.drawable.avatar_1,
            contentDescription = null,
            modifier = Modifier
                .zIndex(5f)
                .align(Alignment.BottomCenter)
                .then(
                    if (showBorder) {
                        Modifier.border(
                            2.dp,
                            color = MaterialTheme.colorScheme.surface.copy(0.5f),
                            shape = CircleShape
                        )
                    } else Modifier
                ),
            contentScale = ContentScale.Crop)
    }
}
@Composable
fun ProfileBannerPhoto(
    profileState: ProfileScreenState,
    navController: NavController,
){

    Box {
        Box(
            modifier = Modifier
                .animateContentSize()
                .sizeIn(maxHeight = 250.dp)
                .fillMaxWidth()
                .wrapContentHeight()
                .clickable(
                    onClick = { navController.navigate(NavGraph.PROFILE) }
                )
        ) {
            AsyncImage(
                model = profileState.bannerImageUri ?: R.drawable.banner_bg_image,
                contentDescription = null,
                modifier = Modifier
                    .zIndex(5f)
                    .align(Alignment.BottomCenter),
                contentScale = ContentScale.Crop
            )
        }
        Box(
            modifier = Modifier
                .animateContentSize()
                .height(250.dp)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background.copy(0.4f),
                            MaterialTheme.colorScheme.background

                        )
                    )
                )
        )
    }
}

@Composable
private fun DynamicGreeting(
    userName: String,
    themeColors: ColorScheme,
    totalBalance: Double? = null,
    mainCurrencyCode: String,
) {
    // Get current time to determine greeting
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    // Use remember with a random value to keep the same variant during recompositions
    val randomVariant = remember { (0..5).random() }

    when {
        // Morning: 5 AM - 11:59 AM
        currentHour in 5..11 && randomVariant <= 2 -> {
            StandardGreeting(
                topText = "Good Morning",
                bottomText = userName,
                themeColors = themeColors
            )
        }
        // Afternoon: 12 PM - 4:59 PM
        currentHour in 12..16 && randomVariant <= 2 -> {
            StandardGreeting(
                topText = "Good Afternoon",
                bottomText = userName,
                themeColors = themeColors
            )
        }
        // Evening: 5 PM - 8:59 PM
        currentHour in 17..20 && randomVariant <= 2 -> {
            StandardGreeting(
                topText = "Good Evening",
                bottomText = userName,
                themeColors = themeColors
            )
        }
        // Night: 9 PM - 4:59 AM
        (currentHour in 21..23 || currentHour in 0..4) && randomVariant <= 2 -> {
            StandardGreeting(
                topText = "Good Night",
                bottomText = userName,
                themeColors = themeColors
            )
        }
        // Random variation 1: Welcome Back
        randomVariant == 3 -> {
            StandardGreeting(
                topText = "Welcome Back",
                bottomText = userName,
                themeColors = themeColors
            )
        }
        // Random variation 2: Days left in month
        randomVariant == 4 -> {
            StandardGreeting(
                topText = "Total Balance",
                bottomText = String.format("%.2f", totalBalance),
                themeColors = themeColors,
                currencySymbol = CurrencySymbols.getSymbol(mainCurrencyCode),
                showBalance = true
            )
        }
        randomVariant == 5 -> {
            val daysLeftInMonth = calculateDaysLeftInMonth()
            StandardGreeting(
                // only use First name
                topText = userName.trim().split("\\s+".toRegex()).firstOrNull() ?: userName,
                bottomText = "$daysLeftInMonth Days left in ${getCurrentMonthName()}",
                isReversed = true,
                themeColors = themeColors
            )
        }
        // Default fallback - could also be a total balance display
        else -> {
            // If totalBalance is provided, show balance, otherwise default greeting
            StandardGreeting(
                topText = "Welcome",
                bottomText = userName,
                themeColors = themeColors
            )
        }
    }
}

@Composable
private fun StandardGreeting(
    topText: String,
    bottomText: String,
    themeColors: ColorScheme,
    isReversed: Boolean = false,
    showBalance: Boolean = false,
    currencySymbol: String = ""
) {
    // When isReversed is true, the main text (large) appears first, followed by smaller text
    if (isReversed) {
        Text(
            text = topText,
            fontSize = 22.sp,
            fontFamily = iosFont,
            fontWeight = FontWeight.Bold,
            color = themeColors.inverseSurface,
            textAlign = TextAlign.Start
        )
        Text(
            text = bottomText,
            fontSize = 12.sp,
            lineHeight = 24.sp,
            fontFamily = iosFont,
            fontWeight = FontWeight.SemiBold,
            color = themeColors.inverseSurface.copy(0.5f),
            textAlign = TextAlign.Start
        )
    } else {
        // Standard format with small text on top, large text below
        Text(
            text = topText,
            fontSize = 12.sp,
            lineHeight = 24.sp,
            fontFamily = iosFont,
            fontWeight = FontWeight.SemiBold,
            color = themeColors.inverseSurface.copy(0.5f),
            textAlign = TextAlign.Start
        )
        if (showBalance){
            BlurAnimatedTextCountUpWithCurrency(
                text = bottomText,
                currencySymbol = currencySymbol ,
                fontSize = 24.sp,
                fontFamily = iosFont,
                fontWeight = FontWeight.Bold,
                textStyle = TextStyle(textAlign = TextAlign.Start),
                enableDynamicSizing = true
            )
        } else {
            Text(
                text = bottomText,
                fontSize = 24.sp,
                fontFamily = iosFont,
                fontWeight = FontWeight.Bold,
                color = themeColors.inverseSurface,
                textAlign = TextAlign.Start
            )
        }
    }
}

@Composable
fun CategoryDisplayInfoProvider(
    transactions: List<TransactionEntity>,
    onCategoryEvent: (CategoryScreenEvent) -> Unit,
    onSubCategoryEvent: (SubCategoryEvent) -> Unit,
    content: @Composable (List<CategoryDisplayInfo>) -> Unit
) {
    var categoryDisplayInfos by remember { mutableStateOf<List<CategoryDisplayInfo>>(emptyList()) }

    LaunchedEffect(transactions) {
        Log.d("CategoryProvider", "Processing ${transactions.size} transactions")

        val result = mutableListOf<CategoryDisplayInfo>()

        transactions.forEach { transaction ->
            Log.d("CategoryProvider", "Processing transaction: categoryId=${transaction.categoryId}, subCategoryId=${transaction.subCategoryId}")

            if (transaction.subCategoryId != null && transaction.subCategoryId != 0) {
                // Fetch subcategory
                onSubCategoryEvent(SubCategoryEvent.GetSubCategoryById(transaction.subCategoryId) { subCategory ->
                    if (subCategory != null) {
                        Log.d("CategoryProvider", "✓ Found subcategory: ${subCategory.name}")

                        // Get parent category for fallback
                        onCategoryEvent(
                            CategoryScreenEvent.OnCategoryFetched(
                                id = subCategory.categoryId,
                                onSuccess = { parentCategory ->
                                    val info = CategoryDisplayInfo(
                                        id = subCategory.id,
                                        isSubCategory = true,
                                        iconId = subCategory.subcategoryIconId ?: parentCategory?.categoryIconId ?: 1,
                                        boxColor = subCategory.boxColor ?: parentCategory?.boxColor ?: 0xFF4CAF50.toInt(),
                                        name = subCategory.name
                                    )

                                    // Update state
                                    val key = "${info.isSubCategory}-${info.id}"
                                    if (!result.any { "${it.isSubCategory}-${it.id}" == key }) {
                                        result.add(info)
                                        categoryDisplayInfos = result.distinctBy { "${it.isSubCategory}-${it.id}" }.take(4)
                                    }
                                }
                            )
                        )
                    } else {
                        Log.d("CategoryProvider", "Subcategory not found, falling back to category")
                        // Fallback to category
                        onCategoryEvent(
                            CategoryScreenEvent.OnCategoryFetched(
                                id = transaction.categoryId,
                                onSuccess = { category ->
                                    if (category != null) {
                                        val info = CategoryDisplayInfo(
                                            id = category.id,
                                            isSubCategory = false,
                                            iconId = category.categoryIconId,
                                            boxColor = category.boxColor,
                                            name = category.name
                                        )

                                        val key = "${info.isSubCategory}-${info.id}"
                                        if (!result.any { "${it.isSubCategory}-${it.id}" == key }) {
                                            result.add(info)
                                            categoryDisplayInfos = result.distinctBy { "${it.isSubCategory}-${it.id}" }.take(4)
                                        }
                                    }
                                }
                            )
                        )
                    }
                }
                )
            } else {
                // Fetch category directly
                onCategoryEvent(
                    CategoryScreenEvent.OnCategoryFetched(
                        id = transaction.categoryId,
                        onSuccess = { category ->
                            if (category != null) {
                                Log.d("CategoryProvider", "✓ Found category: ${category.name}")

                                val info = CategoryDisplayInfo(
                                    id = category.id,
                                    isSubCategory = false,
                                    iconId = category.categoryIconId,
                                    boxColor = category.boxColor,
                                    name = category.name
                                )

                                val key = "${info.isSubCategory}-${info.id}"
                                if (!result.any { "${it.isSubCategory}-${it.id}" == key }) {
                                    result.add(info)
                                    categoryDisplayInfos = result.distinctBy { "${it.isSubCategory}-${it.id}" }.take(4)
                                }
                            }
                        }
                    )
                )
            }
        }
    }

    content(categoryDisplayInfos)
}

@Composable
fun UpdatedCardsSection(
    homeUiState: HomeScreenState,
    getUpcomingTransactionCount: () -> Int,
    getOverdueTransactionCount: () -> Int,
    onCategoryEvent: (CategoryScreenEvent) -> Unit,
    onSubCategoryEvent: (SubCategoryEvent) -> Unit,
    themeColors: ColorScheme
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        UpcomingTransactionsCard(
            count = getUpcomingTransactionCount(),
            upcomingTransactions = homeUiState.upcomingTransactions,
            onCategoryEvent = onCategoryEvent,
            onSubCategoryEvent = onSubCategoryEvent,
            themeColors = themeColors,
            modifier = Modifier.weight(1f),
            onClick = { }
        )

        OverdueTransactionsCard(
            count = getOverdueTransactionCount(),
            overdueTransactions = homeUiState.overdueTransactions,
            onCategoryEvent = onCategoryEvent,
            onSubCategoryEvent = onSubCategoryEvent,
            themeColors = themeColors,
            modifier = Modifier.weight(1f),
            onClick = { }
        )
    }
}
@Composable
fun UpcomingTransactionsCard(
    count: Int,
    upcomingTransactions: List<TransactionEntity>,
    onCategoryEvent: (CategoryScreenEvent) -> Unit,
    onSubCategoryEvent: (SubCategoryEvent) -> Unit,
    themeColors: ColorScheme,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(
                interactionSource = remember{MutableInteractionSource()},
                indication = null
            ) { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = themeColors.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = count.toString(),
                        fontSize = 16.sp,
                        lineHeight = 16.sp,
                        fontFamily = iosFont,
                        fontWeight = FontWeight.Bold,
                        color = Macchiato_Mauve_Dim
                    )
                    Text(
                        text = "Upcoming",
                        fontSize = 16.sp,
                        lineHeight = 16.sp,
                        fontFamily = iosFont,
                        fontWeight = FontWeight.SemiBold,
                        color = themeColors.inverseSurface
                    )
                }
                Text(
                    text = "Transactions",
                    fontSize = 12.sp,
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Medium,
                    color = themeColors.inverseSurface.copy(0.5f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Bottom
            ) {
                // Use the new provider pattern
                CategoryDisplayInfoProvider(
                    transactions = upcomingTransactions,
                    onCategoryEvent = onCategoryEvent,
                    onSubCategoryEvent = onSubCategoryEvent,
                ) { categoryDisplayInfos ->
                    if (categoryDisplayInfos.isNotEmpty()) {
                        StackedCategoryIcons(categoryDisplayInfos = categoryDisplayInfos)
                    }
                }
            }
        }
    }
}

@Composable
fun OverdueTransactionsCard(
    count: Int,
    overdueTransactions: List<TransactionEntity>,
    onCategoryEvent: (CategoryScreenEvent) -> Unit,
    onSubCategoryEvent: (SubCategoryEvent) -> Unit,
    themeColors: ColorScheme,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(
                interactionSource = remember{MutableInteractionSource()},
                indication = null
            ) { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = themeColors.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = count.toString(),
                        fontSize = 16.sp,
                        lineHeight = 16.sp,
                        fontFamily = iosFont,
                        fontWeight = FontWeight.Bold,
                        color = Macchiato_Red_Dim
                    )
                    Text(
                        text = "Overdue",
                        fontSize = 16.sp,
                        lineHeight = 16.sp,
                        fontFamily = iosFont,
                        fontWeight = FontWeight.SemiBold,
                        color = themeColors.inverseSurface
                    )
                }
                Text(
                    text = "Transactions",
                    fontSize = 12.sp,
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Medium,
                    color = themeColors.inverseSurface.copy(0.5f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Bottom
            ) {
                CategoryDisplayInfoProvider(
                    transactions = overdueTransactions,
                    onCategoryEvent = onCategoryEvent,
                    onSubCategoryEvent = onSubCategoryEvent,
                ) { categoryDisplayInfos ->
                    if (categoryDisplayInfos.isNotEmpty()) {
                        StackedCategoryIcons(categoryDisplayInfos = categoryDisplayInfos)
                    }
                }
            }
        }
    }
}

@Composable
fun StackedCategoryIcons(
    categoryDisplayInfos: List<CategoryDisplayInfo>,
    modifier: Modifier = Modifier
) {
    if (categoryDisplayInfos.isEmpty()) {
        Log.d("SubcategoryDebug", "No icons to display - returning early")
        return
    }

    val iconSize = 42.dp
    val overlapAmount = 18.dp
    val totalWidth = iconSize + (overlapAmount * (categoryDisplayInfos.size - 1))


    Box(
        modifier = modifier
            .width(totalWidth)
            .height(iconSize),
        contentAlignment = Alignment.CenterStart
    ) {
        categoryDisplayInfos.forEachIndexed { index, categoryInfo ->
            val xOffset = index * overlapAmount.value

            Log.d("SubcategoryDebug", "Rendering icon $index: ${categoryInfo.name} at offset ${xOffset}dp")
            Box(
                modifier = Modifier
                    .size(iconSize)
                    .offset(x = xOffset.dp)
                    .zIndex(index.toFloat())
                    // Outer border background
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = CircleShape
                    )
                    // Inner padding for border effect
                    .padding(3.dp)
                    // Inner background
                    .background(
                        color = Color(categoryInfo.boxColor),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                val iconResource = icons.find { icon ->
                    icon.id == categoryInfo.iconId
                }?.resourceId ?: R.drawable.type_beverages_beer

                Image(
                    painter = painterResource(id = iconResource),
                    contentDescription = categoryInfo.name,
                    modifier = Modifier.size(26.dp),
                )
            }
        }
    }
}
// Helper functions
private fun calculateDaysLeftInMonth(): Int {
    val calendar = Calendar.getInstance()
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
    return daysInMonth - currentDay
}

private fun getCurrentMonthName(): String {
    return SimpleDateFormat("MMMM", Locale.getDefault()).format(Date())
}
