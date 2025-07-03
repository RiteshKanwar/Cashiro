package com.ritesh.cashiro.presentation.ui.features.accounts

import android.util.Log
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.ritesh.cashiro.presentation.ui.extras.components.extras.CustomTitleTopAppBar
import com.ritesh.cashiro.presentation.ui.theme.iosFont
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import com.ritesh.cashiro.data.local.entity.AccountEntity
import com.ritesh.cashiro.domain.repository.AccountTransactionStats
import com.ritesh.cashiro.presentation.ui.features.currency.CurrencyViewModel
import com.ritesh.cashiro.presentation.ui.extras.components.account.AccountsListItem
import com.ritesh.cashiro.presentation.ui.extras.components.account.EditAccountSheet
import com.ritesh.cashiro.presentation.ui.extras.components.extras.AccountDeletionDialogs
import com.ritesh.cashiro.presentation.ui.features.add_transaction.AddTransactionEvent
import com.ritesh.cashiro.presentation.ui.features.add_transaction.AddTransactionScreenState
import com.ritesh.cashiro.presentation.ui.features.add_transaction.AddTransactionScreenViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import kotlin.Boolean

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AccountScreen(
//    accountViewModel: AccountScreenViewModel,
//    currencyViewModel: CurrencyViewModel,
//    onBackClicked: () -> Unit,
//    screenTitle: String,
//    previousScreenTitle: String,
//){
//    // Use the full state instead of individual state flows to access the currency update timestamp
//    val state by accountViewModel.state.collectAsState()
//
//    // Individual state flows for backward compatibility
//    val accounts = state.accounts
//    val accountName = state.accountName
//    val balance = state.balance
//    val isMainAccount = state.isMainAccount
//    val cardColor1 = state.cardColor1
//    val cardColor2 = state.cardColor2
//    val currencyCode = state.currencyCode
//    val transactionCounts = state.transactionCounts
//
//    // Currency change tracking
//    val lastCurrencyUpdateTimestamp = state.lastCurrencyUpdateTimestamp
//    val lastUpdatedAccountId = state.lastUpdatedAccountId
//
//    // Track previous timestamp to detect changes
//    val previousTimestamp = remember { mutableLongStateOf(lastCurrencyUpdateTimestamp) }
//
//    // Effect to detect and respond to currency changes
//    LaunchedEffect(lastCurrencyUpdateTimestamp) {
//        if (lastCurrencyUpdateTimestamp > 0 && lastCurrencyUpdateTimestamp != previousTimestamp.longValue) {
//            // Currency was updated, trigger a refresh
//            Log.d("CurrencyUpdateEffect", "Currency updated at $lastCurrencyUpdateTimestamp for account $lastUpdatedAccountId")
//            accountViewModel.onEvent(AccountScreenEvent.FetchAllAccounts)
//            previousTimestamp.longValue = lastCurrencyUpdateTimestamp
//        }
//    }
//
//    // Scaffold State for Scroll Behavior
//    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
//    val scrollBehaviorSmall = TopAppBarDefaults.pinnedScrollBehavior()
//    val scrollState = rememberScrollState()
//
//    val hazeState = remember { HazeState() }
//
//    val themeColors = MaterialTheme.colorScheme
//    val lazyListState = rememberLazyListState()
//    var isSheetOpen = rememberSaveable {
//        mutableStateOf(false)
//    }
//    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded= true)
//
//    // Get half of the screen height
//    val configuration = LocalConfiguration.current
//    val screenHeight = configuration.screenHeightDp.dp
//    val screenWidth = configuration.screenWidthDp.dp
//    val usedScreenHeight = screenHeight / 1.2f
//    val usedScreenWidth = screenWidth - 10.dp
//
//    // For haptic feedback
//    val haptic = LocalHapticFeedback.current
//
//    LaunchedEffect(scrollBehavior.state.collapsedFraction) {
//        accountViewModel.updateCollapsingFraction(scrollBehavior.state.collapsedFraction)
//    }
//
//    Scaffold(
//        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
//        topBar = {
//            CustomTitleTopAppBar(
//                scrollBehaviorLarge = scrollBehavior,
//                scrollBehaviorSmall =  scrollBehaviorSmall,
//                title = screenTitle,
//                previousScreenTitle = previousScreenTitle,
//                onBackClick = onBackClicked,
//                hasBackButton = true,
//                hazeState = hazeState,
//            )
//        },
//        bottomBar = {
//            AddAccountButton(
//                accountViewModel = accountViewModel,
//                currencyViewModel = currencyViewModel,
//                balance = balance,
//                accountName = accountName,
//                isMainAccount = isMainAccount,
//                cardColor1 = cardColor1?:Color(0xFF696eff),
//                cardColor2 = cardColor2?:Color(0xFF93D7DE),
//                currencyCode = currencyCode,
//                usedScreenWidth = usedScreenWidth,
//                usedScreenHeight = usedScreenHeight + 10.dp,
//                themeColors = themeColors
//            )
//        }
//    ) {innerPadding ->
//        Box(modifier = Modifier
//            .padding(
//                start = innerPadding.calculateLeftPadding(LayoutDirection.Ltr),
//                end = innerPadding.calculateRightPadding(LayoutDirection.Rtl)
//            )
//            .haze(state = hazeState)
//        ){
//            Column(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .verticalScroll(scrollState),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Column(Modifier.offset(y = state.currentOffset)) {
//                    AccountsListItem(
//                        items = accounts,
//                        onMove = { fromIndex, toIndex ->
//                            // Using the new event system
//                            accountViewModel.onEvent(
//                                AccountScreenEvent.ReorderAccounts(
//                                    fromIndex,
//                                    toIndex
//                                )
//                            )
//                        },
//                        modifier = Modifier.height(screenHeight + state.currentOffset),
//                        accountViewModel = accountViewModel,
//                        currencyViewModel = currencyViewModel
//                    )
//                }
//            }
//            Spacer(
//                modifier = Modifier
//                    .height(150.dp)
//                    .fillMaxWidth()
//                    .background(
//                        brush = Brush.verticalGradient(colors = listOf(Color.Transparent, themeColors.background.copy(0.5f), themeColors.background))
//                    ).align(Alignment.BottomCenter)
//            )
//        }
//        AccountDeletionDialogs(
//            state = state,
//            onEvent = accountViewModel::onEvent,
//            getTransactionStatsForAccount = accountViewModel::getTransactionStatsForAccount,
//            themeColors = themeColors
//        )
//    }
//}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    accountViewModel: AccountScreenViewModel,
    currencyViewModel: CurrencyViewModel,
    transactionViewModel: AddTransactionScreenViewModel,
    onBackClicked: () -> Unit,
    screenTitle: String,
    previousScreenTitle: String,
){
    // Use the full state instead of individual state flows to access the currency update timestamp
    val accountUiState by accountViewModel.state.collectAsState()
    val currencyUiState by currencyViewModel.uiState.collectAsState()
    val transactionUiState by transactionViewModel.state.collectAsState()

    AccountScreenContent(
        screenTitle = screenTitle,
        previousScreenTitle = previousScreenTitle,
        onBackClicked = onBackClicked,
        accountUiState = accountUiState,
        onAccountEvent = accountViewModel::onEvent,
        getTransactionStatsForAccount = accountViewModel::getTransactionStatsForAccount,
        updateAccountCurrency = accountViewModel::updateAccountCurrency,
        setAccountAsMain = accountViewModel::setAccountAsMain,
        currencyUiState = currencyUiState,
        selectCurrency = currencyViewModel::selectCurrency,
        onAddTransactionEvent = transactionViewModel::onEvent,
        transactionUiState = transactionUiState,
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreenContent(
    screenTitle: String,
    previousScreenTitle: String,
    onBackClicked: () -> Unit,
    accountUiState: AccountScreenState,
    onAccountEvent: (AccountScreenEvent) -> Unit,
    getTransactionStatsForAccount: (id: Int, callback: (AccountTransactionStats?) -> Unit) -> Unit,
    updateAccountCurrency: (Int, String) -> Unit,
    setAccountAsMain: (AccountEntity) -> Unit,
    currencyUiState: CurrencyViewModel.CurrencyUiState,
    selectCurrency: (String) -> Unit,
    onAddTransactionEvent: (AddTransactionEvent) -> Unit,
    transactionUiState: AddTransactionScreenState,
){
    // Individual state flows for backward compatibility
    val accounts = accountUiState.accounts
    val accountName = accountUiState.accountName
    val balance = accountUiState.balance
    val isMainAccount = accountUiState.isMainAccount
    val cardColor1 = accountUiState.cardColor1
    val cardColor2 = accountUiState.cardColor2
    val currencyCode = accountUiState.currencyCode
    val transactionCounts = accountUiState.transactionCounts

    // Currency change tracking
    val lastCurrencyUpdateTimestamp = accountUiState.lastCurrencyUpdateTimestamp
    val lastUpdatedAccountId = accountUiState.lastUpdatedAccountId

    // Track previous timestamp to detect changes
    val previousTimestamp = remember { mutableLongStateOf(lastCurrencyUpdateTimestamp) }

    // Effect to detect and respond to currency changes
    LaunchedEffect(lastCurrencyUpdateTimestamp) {
        if (lastCurrencyUpdateTimestamp > 0 && lastCurrencyUpdateTimestamp != previousTimestamp.longValue) {
            // Currency was updated, trigger a refresh
            Log.d("CurrencyUpdateEffect", "Currency updated at $lastCurrencyUpdateTimestamp for account $lastUpdatedAccountId")
            onAccountEvent(AccountScreenEvent.FetchAllAccounts)
            previousTimestamp.longValue = lastCurrencyUpdateTimestamp
        }
    }

    // Scaffold State for Scroll Behavior
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scrollBehaviorSmall = TopAppBarDefaults.pinnedScrollBehavior()
    val scrollState = rememberScrollState()

    val hazeState = remember { HazeState() }

    val themeColors = MaterialTheme.colorScheme
    val lazyListState = rememberLazyListState()
    var isSheetOpen = rememberSaveable {
        mutableStateOf(false)
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded= true)

    // Get half of the screen height
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val usedScreenHeight = screenHeight / 1.2f
    val usedScreenWidth = screenWidth - 10.dp

    // For haptic feedback
    val haptic = LocalHapticFeedback.current
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CustomTitleTopAppBar(
                scrollBehaviorLarge = scrollBehavior,
                scrollBehaviorSmall =  scrollBehaviorSmall,
                title = screenTitle,
                previousScreenTitle = previousScreenTitle,
                onBackClick = onBackClicked,
                hasBackButton = true,
                hazeState = hazeState,
            )
        },
        bottomBar = {
            AddAccountButton(
                balance = balance,
                accountName = accountName,
                isMainAccount = isMainAccount,
                cardColor1 = cardColor1 ?: Color(0xFF696eff),
                cardColor2 = cardColor2 ?: Color(0xFF93D7DE),
                currencyCode = currencyCode,
                usedScreenWidth = usedScreenWidth,
                usedScreenHeight = usedScreenHeight + 10.dp,
                themeColors = themeColors,
                accountUiState = accountUiState,
                onAccountEvent = onAccountEvent,
                getTransactionStatsForAccount = getTransactionStatsForAccount,
                updateAccountCurrency = updateAccountCurrency,
                currencyUiState = currencyUiState,
                selectCurrency = selectCurrency,
                transactionUiState = transactionUiState,
                onAddTransactionEvent = onAddTransactionEvent,
            )
        }
    ) {innerPadding ->
        Box(modifier = Modifier
            .padding(
                start = innerPadding.calculateLeftPadding(LayoutDirection.Ltr),
                end = innerPadding.calculateRightPadding(LayoutDirection.Rtl)
            )
            .haze(state = hazeState)
        ){
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(Modifier.offset(y = accountUiState.currentOffset)) {
                    AccountsListItem(
                        items = accounts,
                        onMove = { fromIndex, toIndex ->
                            // Using the new event system
                            onAccountEvent(
                                AccountScreenEvent.ReorderAccounts(
                                    fromIndex,
                                    toIndex
                                )
                            )
                        },
                        modifier = Modifier.height(screenHeight + accountUiState.currentOffset),
                        accountUiState = accountUiState,
                        onAccountEvent = onAccountEvent,
                        setAccountAsMain = setAccountAsMain,
                        getTransactionStatsForAccount = getTransactionStatsForAccount,
                        updateAccountCurrency = updateAccountCurrency,
                        currencyUiState = currencyUiState,
                        selectCurrency = selectCurrency,
                        onAddTransactionEvent = onAddTransactionEvent,
                        transactionUiState = transactionUiState,
                    )
                }
            }
            Spacer(
                modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(colors = listOf(Color.Transparent, themeColors.background.copy(0.5f), themeColors.background))
                    ).align(Alignment.BottomCenter)
            )
        }
        AccountDeletionDialogs(
            state = accountUiState,
            onEvent = onAccountEvent,
            themeColors = themeColors,
            getTransactionStatsForAccount = getTransactionStatsForAccount
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountButton(
    accountUiState: AccountScreenState,
    onAccountEvent: (AccountScreenEvent) -> Unit,
    getTransactionStatsForAccount: (id: Int, callback: (AccountTransactionStats?) -> Unit) -> Unit,
    updateAccountCurrency: (Int, String) -> Unit,
    currencyUiState: CurrencyViewModel.CurrencyUiState,
    selectCurrency: (String) -> Unit,
    onAddTransactionEvent: (AddTransactionEvent) -> Unit,
    transactionUiState: AddTransactionScreenState,
    balance: String,
    accountName: String,
    isMainAccount: Boolean,
    cardColor1: Color,
    cardColor2: Color,
    currencyCode: String,
    usedScreenWidth: Dp,
    usedScreenHeight: Dp,
    themeColors: ColorScheme
){
    val haptic = LocalHapticFeedback.current

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded= true)
    var isSheetOpen = rememberSaveable {
        mutableStateOf(false)
    }

    Box(modifier = Modifier.fillMaxWidth().navigationBarsPadding()) {
        Button(
            onClick = {
                isSheetOpen.value = true
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                // Using the new event system to prepare for a new account
                onAccountEvent(AccountScreenEvent.PrepareForNewAccount)
            },
            modifier = Modifier
                .padding(bottom = 10.dp, start = 20.dp, end = 20.dp)
                .height(56.dp)
                .fillMaxWidth()
                .shadow(10.dp, RoundedCornerShape(15.dp), spotColor = themeColors.primary, ambientColor = Color.Transparent),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = themeColors.primary,
                contentColor = themeColors.inverseSurface
            )
        ) {
            Text(
                text = "Add Account",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                fontFamily = iosFont,
            )
        }
    }
    if (isSheetOpen.value){
        EditAccountSheet(
            accountUiState = accountUiState,
            onAccountEvent = onAccountEvent,
            getTransactionStatsForAccount = getTransactionStatsForAccount,
            updateAccountCurrency = updateAccountCurrency,
            currencyUiState = currencyUiState,
            selectCurrency = selectCurrency,
            transactionUiState = transactionUiState,
            onAddTransactionEvent = onAddTransactionEvent,
            sheetState = sheetState,
            isSheetOpen = isSheetOpen,
            balance = balance,
            accountName = accountName,
            isMainAccount = isMainAccount,
            cardColor1 = cardColor1,
            cardColor2 = cardColor2,
            currencyCode = currencyCode,
            usedScreenWidth = usedScreenWidth,
            usedScreenHeight = usedScreenHeight + 10.dp,
        )
    }
}
    val firstColors = listOf(
        Color(0xFF0061ff),
        Color(0xFF00ff87),
        Color(0xFF696eff),
        Color(0xFFff0f7b),
        Color(0xFFff930f),
        Color(0xFFf9b16e)
    )
    val secondColors = listOf(
        Color(0xFF60efff),
        Color(0xFF60efff),
        Color(0xFFf8acff),
        Color(0xFFf89b29),
        Color(0xFFfff95b),
        Color(0xFFf68080),
    )


@Composable
fun AnimatedGradientMeshCard(
    firstColor: Color,
    secondColor: Color,
    cardHeight: Dp = 200.dp,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val animatedColor by infiniteTransition.animateColor(
        initialValue = firstColor,
        targetValue = secondColor,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )
    val reverseAnimatedColor by infiniteTransition.animateColor(
        initialValue = secondColor,
        targetValue = firstColor,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )
    val whiteBlackColor = MaterialTheme.colorScheme.background
    // Define a list of colors for the gradient
    val colors = listOf(
        Color(0xFFF44336),
        Color(0xFFE91E63),
        Color(0xFF9C27B0),
        Color(0xFF673AB7),
        Color(0xFF3F51B5),
        Color(0xFF2196F3),
        Color(0xFF03A9F4),
        Color(0xFF00BCD4),
        Color(0xFF009688),
        Color(0xFF4CAF50),
        Color(0xFF8BC34A),
        Color(0xFFCDDC39),
        Color(0xFFFFEB3B),
        Color(0xFFFFC107),
        Color(0xFFFF9800),
        Color(0xFFFF5722),
        Color(0xFF6200EE),
        Color(0xFF03DAC6)
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(16.dp))
            .height(cardHeight),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Box(contentAlignment = Alignment.BottomCenter){
            Canvas(modifier = Modifier.blur(100.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded).fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                val gradientBrush = Brush.linearGradient(
                    colors = listOf(reverseAnimatedColor, animatedColor, whiteBlackColor),
                    start = Offset(0f, 0f),
                    end = Offset(canvasWidth, canvasHeight),
                    tileMode = TileMode.Mirror
                )

                drawRect(
                    brush = gradientBrush,
                    topLeft = Offset(0f, 0f),
                    size = size
                )
            }
            Spacer(
                modifier = Modifier.height(80.dp).fillMaxWidth().background(Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant.copy(0.5f),
                        MaterialTheme.colorScheme.surfaceVariant.copy(0.8f),
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                ))
            )
        }
    }
}
@Composable
fun CompactAnimatedGradientMeshCard(
    firstColor: Color,
    secondColor: Color,
    cardHeight: Dp = 80.dp,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val animatedColor by infiniteTransition.animateColor(
        initialValue = firstColor,
        targetValue = secondColor,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )
    val reverseAnimatedColor by infiniteTransition.animateColor(
        initialValue = secondColor,
        targetValue = firstColor,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )
    Card(
        modifier = modifier
            .wrapContentWidth()
            .clip(shape = RoundedCornerShape(10.dp))
            .height(cardHeight),
        elevation = CardDefaults.cardElevation(0.dp),
    ) {
        Box(contentAlignment = Alignment.BottomCenter){
            Canvas(modifier = Modifier
                .blur(50.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
                .size(height = cardHeight, width = cardHeight + 50.dp)
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                val gradientBrush = Brush.linearGradient(
                    colors = listOf(reverseAnimatedColor, animatedColor),
                    start = Offset(0f, 0f),
                    end = Offset(canvasWidth, canvasHeight),
                    tileMode = TileMode.Mirror
                )

                drawRect(
                    brush = gradientBrush,
                    topLeft = Offset(0f, 0f),
                    size = size
                )
            }
            Spacer(
                modifier = Modifier.height(40.dp).width(cardHeight + 50.dp).background(Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceVariant.copy(0.5f),
                        MaterialTheme.colorScheme.surfaceVariant
                    )
                ))
            )
            content()
        }
    }
}
val gradientColors = listOf(
    Color(0xFFff9a9e),
    Color(0xFFfad0c4),
    Color(0xFFfbc2eb),
    Color(0xFFa6c1ee),
    Color(0xFF84fab0),
    Color(0xFF8fd3f4),
)
