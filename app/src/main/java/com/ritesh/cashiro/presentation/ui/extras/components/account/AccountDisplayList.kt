package com.ritesh.cashiro.presentation.ui.extras.components.account

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowColumn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritesh.cashiro.R
import com.ritesh.cashiro.data.local.entity.AccountEntity
import com.ritesh.cashiro.domain.repository.AccountTransactionStats
import com.ritesh.cashiro.presentation.ui.features.currency.CurrencyViewModel
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenViewModel
import com.ritesh.cashiro.presentation.ui.features.add_transaction.AddTransactionScreenViewModel
import com.ritesh.cashiro.presentation.effects.VerticalStretchedOverscroll
import com.ritesh.cashiro.presentation.ui.extras.components.extras.SearchBar
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenEvent
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenState
import com.ritesh.cashiro.presentation.ui.features.accounts.AddAccountButton
import com.ritesh.cashiro.presentation.ui.features.add_transaction.AddTransactionEvent
import com.ritesh.cashiro.presentation.ui.features.add_transaction.AddTransactionScreenState
import com.ritesh.cashiro.presentation.ui.theme.Latte_Maroon
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Peach
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Teal
import com.ritesh.cashiro.presentation.ui.theme.iosFont
@OptIn(ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalLayoutApi::class
)
@Composable
fun AccountDisplayList(
    items: List<AccountEntity>,
    accountUiState: AccountScreenState,
    onAccountEvent: (AccountScreenEvent) -> Unit,
    getTransactionStatsForAccount: (id: Int, callback: (AccountTransactionStats?) -> Unit) -> Unit,
    updateAccountCurrency: (Int, String) -> Unit,
    setAccountAsMain: (AccountEntity) -> Unit,
    currencyUiState: CurrencyViewModel.CurrencyUiState,
    selectCurrency: (String) -> Unit,
    transactionUiState: AddTransactionScreenState,
    onAddTransactionEvent: (AddTransactionEvent) -> Unit,
    modifier: Modifier = Modifier,
    onClick : () -> Unit,
    requireSourceAccountID : Boolean = true,
    requireDestinationAccountID : Boolean = false
){
    val themeColors = MaterialTheme.colorScheme

    // Get half of the screen height
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val usedScreenHeight = screenHeight / 1.2f
    val usedScreenWidth = screenWidth - 10.dp

    val transactionAccountId = transactionUiState.transactionAccountId
    val transactionDestinationAccountId = transactionUiState.transactionDestinationAccountId
    val accountName = accountUiState.accountName
    val balance = accountUiState.balance
    val cardColor1 = accountUiState.cardColor1
    val cardColor2 = accountUiState.cardColor2
    val account = accountUiState.accounts
    val currencyCode = accountUiState.currencyCode
    val isMainAccount = accountUiState.isMainAccount

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded= true)
    var isSheetOpen = rememberSaveable { mutableStateOf(false) }

    val scrollOverScrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val overscrollEffect = remember(coroutineScope) { VerticalStretchedOverscroll(coroutineScope) }

    AnimatedVisibility(
        visible = account.isEmpty(),
        enter = fadeIn() + slideInVertically() + expandVertically(),
        exit = slideOutVertically() + shrinkVertically() + fadeOut(),
    ) {
        Column (
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            EmptyAccounts(modifier.weight(1f))
            AddAccountButton(
                accountUiState = accountUiState,
                onAccountEvent = onAccountEvent,
                getTransactionStatsForAccount = getTransactionStatsForAccount,
                updateAccountCurrency = updateAccountCurrency,
                currencyUiState = currencyUiState,
                selectCurrency = selectCurrency,
                balance = balance,
                accountName = accountName,
                isMainAccount = isMainAccount,
                cardColor1 = cardColor1 ?: Color.White,
                cardColor2 = cardColor2 ?: Color.Black,
                currencyCode = currencyCode,
                usedScreenWidth = usedScreenWidth,
                usedScreenHeight = usedScreenHeight + 10.dp,
                themeColors = themeColors,
                onAddTransactionEvent = onAddTransactionEvent,
                transactionUiState = transactionUiState,
            )
        }
    }
    AnimatedVisibility(
        visible = account.isNotEmpty(),
        enter = fadeIn() + slideInVertically() + expandVertically() ,
        exit = shrinkVertically() + fadeOut() ,
    ) {
        LazyColumn(
            state = scrollOverScrollState,
            userScrollEnabled = false,
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .background(themeColors.background)
                .overscroll(overscrollEffect)
                .scrollable(
                    orientation = Orientation.Vertical,
                    reverseDirection = true,
                    state = scrollOverScrollState,
                    overscrollEffect = overscrollEffect
                ),
            verticalArrangement =  Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = "Select Account",
                    textAlign = TextAlign.Center,
                    fontFamily = iosFont,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = themeColors.inverseSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                )
            }

            item{
                var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
                val filteredAccount = items.filter {
                    it.accountName.contains(searchQuery.text, ignoreCase = true)
                }
                SearchBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(R.drawable.search_bulk),
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.inverseSurface.copy(0.5f)
                        )
                    },
                    label = "SEARCH",
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Animate the filtered categories
                AnimatedContent(
                    targetState = filteredAccount,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                                scaleIn(initialScale = 0.92f, animationSpec = tween(220, delayMillis = 90)))
                            .togetherWith(fadeOut(animationSpec = tween(90)))
                    }, label = "Filtered Account animated"
                ) { accountsToDisplay ->
                    val infiniteTransition = rememberInfiniteTransition(label = "Selected Glow animation")
                    val animatedColors = infiniteTransition.animateColor(
                        initialValue = Color.Transparent,
                        targetValue = Color.Transparent,
                        animationSpec = infiniteRepeatable(
                            animation = keyframes {
                                durationMillis = 3000 // Total duration for the animation cycle
                                Latte_Maroon at 0 using LinearEasing
                                Macchiato_Teal at 1000 using LinearEasing // Add more colors in the sequence
                                Macchiato_Peach at 2000 using LinearEasing
                            },
                            repeatMode = RepeatMode.Reverse
                        ), label = "Selected Glow animation")
                    FlowColumn(
                        modifier = modifier
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        accountsToDisplay.forEachIndexed { index, item ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .then(
                                        if (requireSourceAccountID && item.id == transactionAccountId){
                                            Modifier
                                                .shadow(elevation = 25.dp,
                                                    shape = RoundedCornerShape(16.dp),
                                                    spotColor = animatedColors.value,
                                                    ambientColor = animatedColors.value)
                                        }
                                        else if (requireDestinationAccountID && item.id == transactionDestinationAccountId){
                                            Modifier
                                                .shadow(elevation = 25.dp,
                                                    shape = RoundedCornerShape(16.dp),
                                                    spotColor = animatedColors.value,
                                                    ambientColor = animatedColors.value)
                                        }
                                        else {
                                            Modifier.shadow(elevation = 10.dp,
                                                shape = RoundedCornerShape(16.dp),
                                                spotColor = Color.Black.copy(0.5f),
                                                ambientColor = Color.Transparent)
                                        }
                                    )
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable{
                                        if (requireDestinationAccountID) {
                                            onAddTransactionEvent(AddTransactionEvent.UpdateDestinationAccountId(item.id))
                                        }
                                        if (requireSourceAccountID) {
                                            onAddTransactionEvent(AddTransactionEvent.UpdateAccountId(item.id))
                                        }
                                        onClick()
                                    }
                                    .then(
                                        if (requireSourceAccountID && item.id == transactionAccountId){
                                            Modifier
                                                .background(
                                                    color = themeColors.surface,
                                                    shape = RoundedCornerShape(16.dp))
                                                .border(
                                                    width = 2.dp,
                                                    shape = RoundedCornerShape(16.dp),
                                                    color = animatedColors.value)
                                        }
                                        else if (requireDestinationAccountID && item.id == transactionDestinationAccountId){
                                            Modifier
                                                .background(
                                                    color = themeColors.surface,
                                                    shape = RoundedCornerShape(16.dp))
                                                .border(
                                                    width = 2.dp,
                                                    shape = RoundedCornerShape(16.dp),
                                                    color = animatedColors.value)
                                        }
                                        else {
                                            Modifier
                                        }
                                    )
                            ) {
                                AccountCardLayout(
                                    accountUiState = accountUiState,
                                    setAccountAsMain = setAccountAsMain,
                                    item = item,
                                    themeColors = themeColors,
                                    notAllowSetAccountAsMain = true,
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable{isSheetOpen.value = true }
                                .padding(bottom = 15.dp),
                            contentAlignment = Alignment.Center
                        ){
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .background(themeColors.onBackground, shape = RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.add_circle_bulk),
                                    contentDescription = "Color Picker",
                                    modifier = Modifier.size(50.dp),
                                    tint = themeColors.inverseSurface.copy(0.5f)
                                )
                            }
                        }
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
                        cardColor1 = cardColor1 ?: Color.White,
                        cardColor2 = cardColor2 ?: Color.Black,
                        currencyCode = currencyCode,
                        usedScreenWidth = usedScreenWidth,
                        usedScreenHeight = usedScreenHeight + 10.dp,
                    )
                }
            }
        }
    }
}
//@OptIn(ExperimentalMaterial3Api::class,
//    ExperimentalFoundationApi::class,
//    ExperimentalLayoutApi::class
//)
//@Composable
//fun AccountDisplayList(
//    items: List<AccountEntity>,
//    accountViewModel: AccountScreenViewModel,
//    currencyViewModel: CurrencyViewModel,
//    transactionViewModel: AddTransactionScreenViewModel,
//    modifier: Modifier = Modifier,
//    onClick : () -> Unit,
//    requireSourceAccountID : Boolean = true,
//    requireDestinationAccountID : Boolean = false
//){
//    val themeColors = MaterialTheme.colorScheme
//
//    // Get half of the screen height
//    val configuration = LocalConfiguration.current
//    val screenHeight = configuration.screenHeightDp.dp
//    val screenWidth = configuration.screenWidthDp.dp
//    val usedScreenHeight = screenHeight / 1.2f
//    val usedScreenWidth = screenWidth - 10.dp
//
//    val transactionAccountId by transactionViewModel.transactionAccountId.collectAsState()
//    val transactionDestinationAccountId by transactionViewModel.transactionDestinationAccountId.collectAsState()
//    val accountName by accountViewModel.accountName.collectAsState()
//    val balance by accountViewModel.balance.collectAsState()
//    val cardColor1 by accountViewModel.cardColor1.collectAsState()
//    val cardColor2 by accountViewModel.cardColor2.collectAsState()
//    val account by accountViewModel.accounts.collectAsState()
//    val currencyCode by accountViewModel.currencyCode.collectAsState()
//    val isMainAccount by accountViewModel.isMainAccount.collectAsState()
//
//    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded= true)
//    var isSheetOpen = rememberSaveable { mutableStateOf(false) }
//
//    val scrollOverScrollState = rememberLazyListState()
//    val coroutineScope = rememberCoroutineScope()
//    val overscrollEffect = remember(coroutineScope) { VerticalStretchedOverscroll(coroutineScope) }
//
//    AnimatedVisibility(
//        visible = account.isEmpty(),
//        enter = fadeIn() + slideInVertically() + expandVertically(),
//        exit = slideOutVertically() + shrinkVertically() + fadeOut(),
//    ) {
//        Column (
//            verticalArrangement = Arrangement.Center,
//            horizontalAlignment = Alignment.CenterHorizontally
//        ){
//            EmptyAccounts(modifier.weight(1f))
//            AddAccountButton(
//                accountViewModel = accountViewModel,
//                currencyViewModel = currencyViewModel,
//                balance = balance,
//                accountName = accountName,
//                isMainAccount = isMainAccount,
//                cardColor1 = cardColor1?:Color.White,
//                cardColor2 = cardColor2?:Color.Black,
//                currencyCode = currencyCode,
//                usedScreenWidth = usedScreenWidth,
//                usedScreenHeight = usedScreenHeight + 10.dp,
//                themeColors = themeColors
//            )
//        }
//    }
//    AnimatedVisibility(
//        visible = account.isNotEmpty(),
//        enter = fadeIn() + slideInVertically() + expandVertically() ,
//        exit = shrinkVertically() + fadeOut() ,
//    ) {
//        LazyColumn(
//            state = scrollOverScrollState,
//            userScrollEnabled = false,
//            modifier = Modifier
//                .padding(horizontal = 20.dp)
//                .fillMaxWidth()
//                .background(themeColors.background)
//                .overscroll(overscrollEffect)
//                .scrollable(
//                    orientation = Orientation.Vertical,
//                    reverseDirection = true,
//                    state = scrollOverScrollState,
//                    overscrollEffect = overscrollEffect
//                ),
//            verticalArrangement =  Arrangement.Top,
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            item {
//                Text(
//                    text = "Select Account",
//                    textAlign = TextAlign.Center,
//                    fontFamily = iosFont,
//                    fontWeight = FontWeight.SemiBold,
//                    fontSize = 18.sp,
//                    color = themeColors.inverseSurface,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(vertical = 10.dp)
//                )
//            }
//
//            item{
//                var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
//                val filteredAccount = items.filter {
//                    it.accountName.contains(searchQuery.text, ignoreCase = true)
//                }
//                SearchBar(
//                    searchQuery = searchQuery,
//                    onSearchQueryChange = { searchQuery = it },
//                    leadingIcon = {
//                        Icon(
//                            painter = painterResource(R.drawable.search_bulk),
//                            contentDescription = "Search",
//                            tint = MaterialTheme.colorScheme.inverseSurface.copy(0.5f)
//                        )
//                    },
//                    label = "SEARCH",
//                )
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Animate the filtered categories
//                AnimatedContent(
//                    targetState = filteredAccount,
//                    transitionSpec = {
//                        (fadeIn(animationSpec = tween(220, delayMillis = 90)) +
//                                scaleIn(initialScale = 0.92f, animationSpec = tween(220, delayMillis = 90)))
//                            .togetherWith(fadeOut(animationSpec = tween(90)))
//                    }, label = "Filtered Account animated"
//                ) { accountsToDisplay ->
//                    val infiniteTransition = rememberInfiniteTransition(label = "Selected Glow animation")
//                    val animatedColors = infiniteTransition.animateColor(
//                        initialValue = Color.Transparent,
//                        targetValue = Color.Transparent,
//                        animationSpec = infiniteRepeatable(
//                            animation = keyframes {
//                                durationMillis = 3000 // Total duration for the animation cycle
//                                Latte_Maroon at 0 using LinearEasing
//                                Macchiato_Teal at 1000 using LinearEasing // Add more colors in the sequence
//                                Macchiato_Peach at 2000 using LinearEasing
//                            },
//                            repeatMode = RepeatMode.Reverse
//                        ), label = "Selected Glow animation")
//                    FlowColumn(
//                        modifier = modifier
//                            .fillMaxWidth(),
//                        verticalArrangement = Arrangement.spacedBy(20.dp)
//                    ) {
//                        accountsToDisplay.forEachIndexed { index, item ->
//                            Box(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .height(200.dp)
//                                    .then(
//                                        if (requireSourceAccountID && item.id == transactionAccountId){
//                                            Modifier
//                                                .shadow(elevation = 25.dp,
//                                                    shape = RoundedCornerShape(16.dp),
//                                                    spotColor = animatedColors.value,
//                                                    ambientColor = animatedColors.value)
//                                        }
//                                        else if (requireDestinationAccountID && item.id == transactionDestinationAccountId){
//                                            Modifier
//                                                .shadow(elevation = 25.dp,
//                                                    shape = RoundedCornerShape(16.dp),
//                                                    spotColor = animatedColors.value,
//                                                    ambientColor = animatedColors.value)
//                                        }
//                                        else {
//                                            Modifier.shadow(elevation = 10.dp,
//                                                shape = RoundedCornerShape(16.dp),
//                                                spotColor = Color.Black.copy(0.5f),
//                                                ambientColor = Color.Transparent)
//                                        }
//                                    )
//                                    .clip(RoundedCornerShape(16.dp))
//                                    .clickable{
//                                        if (requireDestinationAccountID) {
//                                            transactionViewModel.onEvent(AddTransactionEvent.UpdateDestinationAccountId(item.id))
//                                        }
//                                        if (requireSourceAccountID) {
//                                            transactionViewModel.onEvent(AddTransactionEvent.UpdateAccountId(item.id))
//                                        }
//                                        onClick()
//                                    }
//                                    .then(
//                                        if (requireSourceAccountID && item.id == transactionAccountId){
//                                            Modifier
//                                                .background(
//                                                    color = themeColors.surface,
//                                                    shape = RoundedCornerShape(16.dp))
//                                                .border(
//                                                    width = 2.dp,
//                                                    shape = RoundedCornerShape(16.dp),
//                                                    color = animatedColors.value)
//                                        }
//                                        else if (requireDestinationAccountID && item.id == transactionDestinationAccountId){
//                                            Modifier
//                                                .background(
//                                                    color = themeColors.surface,
//                                                    shape = RoundedCornerShape(16.dp))
//                                                .border(
//                                                    width = 2.dp,
//                                                    shape = RoundedCornerShape(16.dp),
//                                                    color = animatedColors.value)
//                                        }
//                                        else {
//                                            Modifier
//                                        }
//                                    )
//                            ) {
//                                AccountCardLayout(
//                                    accountViewModel = accountViewModel,
//                                    item = item,
//                                    themeColors = themeColors,
//                                    notAllowSetAccountAsMain = true
//                                )
//                            }
//                        }
//                        Box(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .height(200.dp)
//                                .clip(RoundedCornerShape(16.dp))
//                                .clickable{isSheetOpen.value = true }
//                                .padding(bottom = 15.dp),
//                            contentAlignment = Alignment.Center
//                        ){
//                            Box(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .height(200.dp)
//                                    .background(themeColors.onBackground, shape = RoundedCornerShape(16.dp)),
//                                contentAlignment = Alignment.Center
//                            ) {
//                                Icon(
//                                    painter = painterResource(R.drawable.add_circle_bulk),
//                                    contentDescription = "Color Picker",
//                                    modifier = Modifier.size(50.dp),
//                                    tint = themeColors.inverseSurface.copy(0.5f)
//                                )
//                            }
//                        }
//                    }
//                }
//                if (isSheetOpen.value){
//                    EditAccountSheet(
//                        accountViewModel = accountViewModel,
//                        sheetState = sheetState,
//                        isSheetOpen = isSheetOpen,
//                        balance = balance,
//                        accountName = accountName,
//                        isMainAccount = isMainAccount,
//                        cardColor1 = cardColor1 ?: Color.White,
//                        cardColor2 = cardColor2 ?: Color.Black,
//                        currencyCode = currencyCode,
//                        usedScreenWidth = usedScreenWidth,
//                        usedScreenHeight = usedScreenHeight + 10.dp,
//                        currencyViewModel = currencyViewModel,
//                    )
//                }
//            }
//        }
//    }
//}