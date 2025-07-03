package com.ritesh.cashiro.presentation.ui.extras.components.account

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat
import com.ritesh.cashiro.R
import com.ritesh.cashiro.data.local.entity.AccountEntity
import com.ritesh.cashiro.data.currency.model.CurrencySymbols
import com.ritesh.cashiro.domain.repository.AccountTransactionStats
import com.ritesh.cashiro.presentation.ui.features.currency.CurrencyViewModel
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenViewModel
import com.ritesh.cashiro.presentation.effects.BlurAnimatedTextCountUpWithCurrency
import com.ritesh.cashiro.presentation.effects.VerticalStretchedOverscroll
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenEvent
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenState
import com.ritesh.cashiro.presentation.ui.features.accounts.AnimatedGradientMeshCard
import com.ritesh.cashiro.presentation.ui.features.add_transaction.AddTransactionEvent
import com.ritesh.cashiro.presentation.ui.features.add_transaction.AddTransactionScreenState
import com.ritesh.cashiro.presentation.ui.theme.Latte_Maroon
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Green
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Mauve
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Peach
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Teal
import com.ritesh.cashiro.presentation.ui.theme.iosFont
import kotlinx.coroutines.launch
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AccountsListItem(
    accountUiState: AccountScreenState,
    onAccountEvent: (AccountScreenEvent) -> Unit,
    setAccountAsMain: (AccountEntity) -> Unit,
    getTransactionStatsForAccount: (id: Int, callback: (AccountTransactionStats?) -> Unit) -> Unit,
    updateAccountCurrency: (Int, String) -> Unit,
    currencyUiState: CurrencyViewModel.CurrencyUiState,
    selectCurrency: (String) -> Unit,
    onAddTransactionEvent: (AddTransactionEvent) -> Unit,
    transactionUiState: AddTransactionScreenState,
    items: List<AccountEntity>,
    onMove: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
){

    val themeColors = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    val coroutineScope = rememberCoroutineScope()
    val overscrollEffect = remember(coroutineScope) { VerticalStretchedOverscroll(coroutineScope) }

    val haptic = LocalHapticFeedback.current
    val view = LocalView.current

    Log.d("TestDebug: from AccountsListItem", "items is Empty?: ${items.isEmpty()}")
    if (items.isEmpty()) {
        EmptyAccounts(modifier)
        return
    }
    Log.d("TestDebug: from AccountsListItem", "items is Empty?: ${items.isEmpty()}")


    val lazyListState = rememberLazyListState()
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        onMove(from.index, to.index)
        ViewCompat.performHapticFeedback(
            view,
            HapticFeedbackConstantsCompat.SEGMENT_FREQUENT_TICK
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .overscroll(overscrollEffect)
            .scrollable(
                orientation = Orientation.Vertical,
                reverseDirection = true,
                state = lazyListState,
                overscrollEffect = overscrollEffect
            ),
        state = lazyListState,
        userScrollEnabled = false,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        items(
            items = items,
            key = { item -> item.id } // Unique key for item tracking
        ) { item ->

            ReorderableItem(reorderableLazyListState, key = item.id) { isDragging ->
                val swipeOffset = remember { Animatable(0f) }
                val dismissThreshold = 300f

                // Animated gradient for the neon glow effect
                val infiniteTransition = rememberInfiniteTransition(label = "Glow animation")
                val animatedColors = infiniteTransition.animateColor(
                    initialValue = Latte_Maroon,
                    targetValue = Macchiato_Mauve,
                    animationSpec = infiniteRepeatable(
                        animation = keyframes {
                            durationMillis = 3000 // Total duration for the animation cycle
                            Latte_Maroon at 0 using LinearEasing
                            Macchiato_Mauve at 500 using LinearEasing
                            Macchiato_Teal at 1000 using LinearEasing // Add more colors in the sequence
                            Macchiato_Green at 1500 using LinearEasing
                            Macchiato_Peach at 2000 using LinearEasing
                        },
                        repeatMode = RepeatMode.Reverse
                    ), label = "Glow animation"
                )

                // Get half of the screen height
                val configuration = LocalConfiguration.current
                val screenHeight = configuration.screenHeightDp.dp
                val screenWidth = configuration.screenWidthDp.dp
                val usedScreenHeight = screenHeight / 1.2f
                val usedScreenWidth = screenWidth - 10.dp
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded= true)
                var isSheetOpen = rememberSaveable {
                    mutableStateOf(false)
                }

                // Animate elevation for dragged items
                val elevation by animateDpAsState(
                    targetValue = if (isDragging) 25.dp else 0.dp,
                    label = "ElevationAnimation"
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                        .zIndex(if (isDragging) 10f else 0f) // Ensure dragged item always has the highest z-index
                        .clickable { isSheetOpen.value = true }
                ) {
                    // Background for delete action
                    if (!isDragging) {
                        ShowDeleteAccountButton(
                            modifier = Modifier.fillMaxWidth().height(200.dp)
                        )
                    }

                    val context = LocalContext.current
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .offset { IntOffset(swipeOffset.value.toInt(), 0) }
                            .then(
                                if (isDragging) {
                                    Modifier.shadow(
                                        elevation = elevation,
                                        shape = RoundedCornerShape(16.dp),
                                        spotColor = animatedColors.value,
                                        ambientColor = animatedColors.value
                                    )
                                } else {
                                    Modifier
                                }
                            )
                            .background(themeColors.surface, RoundedCornerShape(16.dp))
                            .longPressDraggableHandle(
                                onDragStarted = {
                                    ViewCompat.performHapticFeedback(
                                        view,
                                        HapticFeedbackConstantsCompat.GESTURE_START
                                    )
                                },
                                onDragStopped = {
                                    ViewCompat.performHapticFeedback(
                                        view,
                                        HapticFeedbackConstantsCompat.GESTURE_END
                                    )
                                },
                            )
                            .pointerInput(Unit) {
                                detectHorizontalDragGestures(
                                    onDragEnd = {
                                        scope.launch {
                                            if (swipeOffset.value < -dismissThreshold) {
                                                onAccountEvent(
                                                    AccountScreenEvent.ShowAccountDeleteConfirmation(item)
                                                )
                                                swipeOffset.animateTo(0f)
                                            } else {
                                                swipeOffset.animateTo(0f)
                                            }
                                        }
                                    }
                                ) { _, dragAmount ->
                                    scope.launch {
                                        val newOffset = swipeOffset.value + dragAmount
                                        if (newOffset <= 0) swipeOffset.snapTo(newOffset)
                                    }
                                }
                            }
                    ) {
                        // Main account card content
                        AccountCardLayout(
//                            accountViewModel = accountViewModel,
                            accountUiState = accountUiState,
                            setAccountAsMain = setAccountAsMain,
                            item = item,
                            themeColors = themeColors,
                        )
                    }

                    if (isSheetOpen.value) {
                        EditAccountSheet(
                            accountUiState = accountUiState,
                            onAccountEvent = onAccountEvent,
                            getTransactionStatsForAccount = getTransactionStatsForAccount,
                            updateAccountCurrency = updateAccountCurrency,
                            currencyUiState = currencyUiState,
                            selectCurrency = selectCurrency,
                            onAddTransactionEvent = onAddTransactionEvent,
                            transactionUiState = transactionUiState,
                            sheetState = sheetState,
                            isSheetOpen = isSheetOpen,
                            balance = item.balance.toString(),
                            accountName = item.accountName,
                            isMainAccount = item.isMainAccount,
                            cardColor1 = Color(item.cardColor1),
                            cardColor2 = Color(item.cardColor2),
                            currencyCode = item.currencyCode,
                            usedScreenWidth = usedScreenWidth,
                            usedScreenHeight = usedScreenHeight + 10.dp,
                            currentAccountEntity = item,
                            isUpdate = true,
                        )
                    }
                }
            }
        }
        item {
            Spacer(
                modifier = Modifier
                    .size(150.dp)
            )
        }
    }
}

//@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
//@Composable
//fun AccountsListItem(
//    accountViewModel: AccountScreenViewModel,
//    currencyViewModel: CurrencyViewModel,
//    items: List<AccountEntity>,
//    onMove: (Int, Int) -> Unit,
//    modifier: Modifier = Modifier,
//){
//
//    val themeColors = MaterialTheme.colorScheme
//    val scope = rememberCoroutineScope()
//    val coroutineScope = rememberCoroutineScope()
//    val overscrollEffect = remember(coroutineScope) { VerticalStretchedOverscroll(coroutineScope) }
//
//    val haptic = LocalHapticFeedback.current
//    val view = LocalView.current
//
//    Log.d("TestDebug: from AccountsListItem", "items is Empty?: ${items.isEmpty()}")
//    if (items.isEmpty()) {
//        EmptyAccounts(modifier)
//        return
//    }
//    Log.d("TestDebug: from AccountsListItem", "items is Empty?: ${items.isEmpty()}")
//
//
//    val lazyListState = rememberLazyListState()
//    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
//        onMove(from.index, to.index)
//        ViewCompat.performHapticFeedback(
//            view,
//            HapticFeedbackConstantsCompat.SEGMENT_FREQUENT_TICK
//        )
//    }
//
//    LazyColumn(
//        modifier = modifier
//            .fillMaxSize()
//            .padding(horizontal = 20.dp)
//            .overscroll(overscrollEffect)
//            .scrollable(
//                orientation = Orientation.Vertical,
//                reverseDirection = true,
//                state = lazyListState,
//                overscrollEffect = overscrollEffect
//            ),
//        state = lazyListState,
//        userScrollEnabled = false,
//        verticalArrangement = Arrangement.spacedBy(20.dp)
//    ) {
//        items(
//            items = items,
//            key = { item -> item.id } // Unique key for item tracking
//        ) { item ->
//
//            ReorderableItem(reorderableLazyListState, key = item.id) { isDragging ->
//                val swipeOffset = remember { Animatable(0f) }
//                val dismissThreshold = 300f
//
//                // Animated gradient for the neon glow effect
//                val infiniteTransition = rememberInfiniteTransition(label = "Glow animation")
//                val animatedColors = infiniteTransition.animateColor(
//                    initialValue = Latte_Maroon,
//                    targetValue = Macchiato_Mauve,
//                    animationSpec = infiniteRepeatable(
//                        animation = keyframes {
//                            durationMillis = 3000 // Total duration for the animation cycle
//                            Latte_Maroon at 0 using LinearEasing
//                            Macchiato_Mauve at 500 using LinearEasing
//                            Macchiato_Teal at 1000 using LinearEasing // Add more colors in the sequence
//                            Macchiato_Green at 1500 using LinearEasing
//                            Macchiato_Peach at 2000 using LinearEasing
//                        },
//                        repeatMode = RepeatMode.Reverse
//                    ), label = "Glow animation"
//                )
//
//                // Get half of the screen height
//                val configuration = LocalConfiguration.current
//                val screenHeight = configuration.screenHeightDp.dp
//                val screenWidth = configuration.screenWidthDp.dp
//                val usedScreenHeight = screenHeight / 1.2f
//                val usedScreenWidth = screenWidth - 10.dp
//                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded= true)
//                var isSheetOpen = rememberSaveable {
//                    mutableStateOf(false)
//                }
//
//                // Animate elevation for dragged items
//                val elevation by animateDpAsState(
//                    targetValue = if (isDragging) 25.dp else 0.dp,
//                    label = "ElevationAnimation"
//                )
//
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .animateItem()
//                        .zIndex(if (isDragging) 10f else 0f) // Ensure dragged item always has the highest z-index
//                        .clickable { isSheetOpen.value = true }
//                ) {
//                    // Background for delete action
//                    if (!isDragging) {
//                        ShowDeleteAccountButton(
//                            modifier = Modifier.fillMaxWidth().height(200.dp)
//                        )
//                    }
//
//                    val context = LocalContext.current
//                    Box(
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .height(200.dp)
//                            .offset { IntOffset(swipeOffset.value.toInt(), 0) }
//                            .then(
//                                if (isDragging) {
//                                    Modifier.shadow(
//                                        elevation = elevation,
//                                        shape = RoundedCornerShape(16.dp),
//                                        spotColor = animatedColors.value,
//                                        ambientColor = animatedColors.value
//                                    )
//                                } else {
//                                    Modifier
//                                }
//                            )
//                            .background(themeColors.surface, RoundedCornerShape(16.dp))
//                            .longPressDraggableHandle(
//                                onDragStarted = {
//                                    ViewCompat.performHapticFeedback(
//                                        view,
//                                        HapticFeedbackConstantsCompat.GESTURE_START
//                                    )
//                                },
//                                onDragStopped = {
//                                    ViewCompat.performHapticFeedback(
//                                        view,
//                                        HapticFeedbackConstantsCompat.GESTURE_END
//                                    )
//                                },
//                            )
//                            .pointerInput(Unit) {
//                                detectHorizontalDragGestures(
//                                    onDragEnd = {
//                                        scope.launch {
//                                            if (swipeOffset.value < -dismissThreshold) {
//                                                accountViewModel.onEvent(
//                                                    AccountScreenEvent.ShowAccountDeleteConfirmation(item)
//                                                )
//                                                swipeOffset.animateTo(0f)
//                                            } else {
//                                                swipeOffset.animateTo(0f)
//                                            }
//                                        }
//                                    }
//                                ) { _, dragAmount ->
//                                    scope.launch {
//                                        val newOffset = swipeOffset.value + dragAmount
//                                        if (newOffset <= 0) swipeOffset.snapTo(newOffset)
//                                    }
//                                }
//                            }
//                    ) {
//                        // Main account card content
//                        AccountCardLayout(
//                            accountViewModel = accountViewModel,
//                            item = item,
//                            themeColors = themeColors,
//                        )
//                    }
//
//                    if (isSheetOpen.value) {
//                        EditAccountSheet(
//                            accountViewModel = accountViewModel,
//                            currencyViewModel = currencyViewModel,
//                            sheetState = sheetState,
//                            isSheetOpen = isSheetOpen,
//                            balance = item.balance.toString(),
//                            accountName = item.accountName,
//                            isMainAccount = item.isMainAccount,
//                            cardColor1 = Color(item.cardColor1),
//                            cardColor2 = Color(item.cardColor2),
//                            currencyCode = item.currencyCode,
//                            usedScreenWidth = usedScreenWidth,
//                            usedScreenHeight = usedScreenHeight + 10.dp,
//                            currentAccountEntity = item,
//                            isUpdate = true,
//                        )
//                    }
//                }
//            }
//        }
//        item {
//            Spacer(
//                modifier = Modifier
//                    .size(150.dp)
//            )
//        }
//    }
//}
@Composable
fun EmptyAccounts(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(
                    id =  R.drawable.empty_category_list
                ),
                contentDescription = "Empty Accounts List Icon",
                modifier = Modifier.size(300.dp)
            )
            Text(
                text = "No Accounts available",
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
private fun ShowDeleteAccountButton(
    modifier: Modifier
){
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.onError, shape = RoundedCornerShape(16.dp))
            .padding(15.dp),
        contentAlignment = Alignment.CenterEnd // Align text to the end
    ) {
        Text(
            text = "Delete",
            color = Color.White,
            fontFamily = iosFont,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }

}

@Composable
fun AccountCardLayout(
//    accountViewModel: AccountScreenViewModel,
    accountUiState: AccountScreenState,
    setAccountAsMain: (AccountEntity) -> Unit,
    item: AccountEntity,
    themeColors: ColorScheme,
    modifier: Modifier = Modifier,
    accountNameFontSize: Dp = 28.dp,
    cardHeight: Dp = 200.dp,
    notAllowSetAccountAsMain: Boolean = false,
){
    val transactionCounts = accountUiState.transactionCounts
    val transactionCount = transactionCounts[item.id] ?: 0

    val currencySymbol = remember(item.id, item.currencyCode,accountUiState.lastCurrencyUpdateTimestamp) {
        CurrencySymbols.getSymbol(item.currencyCode)
    }
    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomStart
    ) {

        AnimatedGradientMeshCard(
            firstColor = Color(item.cardColor1),
            secondColor = Color(item.cardColor2),
            cardHeight = cardHeight,
        )
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(start = 15.dp, bottom = 15.dp)
        ) {
            Text(
                text = item.accountName,
                fontSize = accountNameFontSize.value.sp,
                lineHeight = 30.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontFamily = iosFont,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 30.dp),
                color = Color.White
            )
            Row {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BlurAnimatedTextCountUpWithCurrency(
                            text = item.balance.toString(),
                            currencySymbol = currencySymbol, // this should change immediately when Account's currency is changed
                            fontSize = 20.sp,
                            fontFamily = iosFont,
                            fontWeight = FontWeight.SemiBold,
                            color = if (item.balance.toString() == "0") themeColors.inverseSurface.copy(0.8f)
                            else themeColors.inverseSurface,
                            enableDynamicSizing = true
                        )
                    }

                    Text(
                        text = "$transactionCount Transactions",
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = iosFont,
                        fontWeight = FontWeight.SemiBold,
                        color = themeColors.inverseSurface.copy(0.5f)
                    )


                }

                Box(contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(top = 10.dp, end = 10.dp)
                        .then(
                            if (notAllowSetAccountAsMain){
                                Modifier
                            } else {
                                Modifier.clickable(
                                    onClick = {
                                        // If this account is not already the main account, make it main
                                        if (!item.isMainAccount) {
                                            setAccountAsMain(item)
                                        }
                                        // If it's already main, we don't allow it to be un-set
                                        // (there should always be one main account)
                                    }
                                )
                            }
                        )

                        .background(
                            color = if (item.isMainAccount) themeColors.primary else themeColors.surface,
                            shape = RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ){
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter =  if (item.isMainAccount) painterResource(R.drawable.star_bulk) else painterResource(R.drawable.star_bulk),
                            contentDescription = null,
                            tint = if (item.isMainAccount) Color.White else themeColors.inverseSurface.copy(alpha = 0.3f),
                            modifier = Modifier.size(20.dp)
                        )

                        AnimatedVisibility(visible = item.isMainAccount) {
                            Text(
                                text = "Main",
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp,
                                lineHeight = 14.sp,
                                color = Color.White,
                                fontFamily = iosFont,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

//@Composable
//fun AccountCardLayout(
//    accountViewModel: AccountScreenViewModel,
//    item: AccountEntity,
//    themeColors: ColorScheme,
//    modifier: Modifier = Modifier,
//    accountNameFontSize: Dp = 28.dp,
//    cardHeight: Dp = 200.dp,
//    notAllowSetAccountAsMain: Boolean = false,
//){
//    val transactionCounts by accountViewModel.transactionCounts.collectAsState()
//    val transactionCount = transactionCounts[item.id] ?: 0
//
//    val currencySymbol = remember(item.id, item.currencyCode, accountViewModel.lastCurrencyUpdateTimestamp.collectAsState().value) {
//        CurrencySymbols.getSymbol(item.currencyCode)
//    }
//    Box(
//        modifier = modifier,
//        contentAlignment = Alignment.BottomStart
//    ) {
//
//        AnimatedGradientMeshCard(
//            firstColor = Color(item.cardColor1),
//            secondColor = Color(item.cardColor2),
//            cardHeight = cardHeight,
//        )
//        Column(
//            horizontalAlignment = Alignment.Start,
//            verticalArrangement = Arrangement.Center,
//            modifier = Modifier.padding(start = 15.dp, bottom = 15.dp)
//        ) {
//            Text(
//                text = item.accountName,
//                fontSize = accountNameFontSize.value.sp,
//                lineHeight = 30.sp,
//                maxLines = 2,
//                overflow = TextOverflow.Ellipsis,
//                fontFamily = iosFont,
//                fontWeight = FontWeight.SemiBold,
//                modifier = Modifier.padding(bottom = 30.dp),
//                color = Color.White
//            )
//            Row {
//                Column(modifier = Modifier.weight(1f)) {
//                    Row(
//                        horizontalArrangement = Arrangement.Center,
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        BlurAnimatedTextCountUpWithCurrency(
//                            text = item.balance.toString(),
//                            currencySymbol = currencySymbol, // this should change immediately when Account's currency is changed
//                            fontSize = 20.sp,
//                            fontFamily = iosFont,
//                            fontWeight = FontWeight.SemiBold,
//                            color = if (item.balance.toString() == "0") themeColors.inverseSurface.copy(0.8f)
//                            else themeColors.inverseSurface,
//                            enableDynamicSizing = true
//                        )
//                    }
//
//                    Text(
//                        text = "$transactionCount Transactions",
//                        fontSize = 12.sp,
//                        maxLines = 1,
//                        overflow = TextOverflow.Ellipsis,
//                        fontFamily = iosFont,
//                        fontWeight = FontWeight.SemiBold,
//                        color = themeColors.inverseSurface.copy(0.5f)
//                    )
//
//
//                }
//
//                Box(contentAlignment = Alignment.Center,
//                    modifier = Modifier
//                        .padding(top = 10.dp, end = 10.dp)
//                        .then(
//                            if (notAllowSetAccountAsMain){
//                                Modifier
//                            } else {
//                                Modifier.clickable(
//                                    onClick = {
//                                        // If this account is not already the main account, make it main
//                                        if (!item.isMainAccount) {
//                                            accountViewModel.setAccountAsMain(item)
//                                        }
//                                        // If it's already main, we don't allow it to be un-set
//                                        // (there should always be one main account)
//                                    }
//                                )
//                            }
//                        )
//
//                        .background(
//                            color = if (item.isMainAccount) themeColors.primary else themeColors.surface,
//                            shape = RoundedCornerShape(10.dp))
//                        .padding(horizontal = 10.dp, vertical = 5.dp)
//                ){
//                    Row(
//                        horizontalArrangement = Arrangement.Center,
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        Icon(
//                            painter =  if (item.isMainAccount) painterResource(R.drawable.star_bulk) else painterResource(R.drawable.star_bulk),
//                            contentDescription = null,
//                            tint = if (item.isMainAccount) Color.White else themeColors.inverseSurface.copy(alpha = 0.3f),
//                            modifier = Modifier.size(20.dp)
//                        )
//
//                        AnimatedVisibility(visible = item.isMainAccount) {
//                            Text(
//                                text = "Main",
//                                textAlign = TextAlign.Center,
//                                fontSize = 14.sp,
//                                lineHeight = 14.sp,
//                                color = Color.White,
//                                fontFamily = iosFont,
//                                fontWeight = FontWeight.Bold,
//                                modifier = Modifier.padding(horizontal = 5.dp)
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
//}