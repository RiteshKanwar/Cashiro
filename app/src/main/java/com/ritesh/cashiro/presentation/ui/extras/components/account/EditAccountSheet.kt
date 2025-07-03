package com.ritesh.cashiro.presentation.ui.extras.components.account

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BottomSheetDefaults.DragHandle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritesh.cashiro.R
import com.ritesh.cashiro.data.local.entity.AccountEntity
import com.ritesh.cashiro.data.currency.model.CurrencySymbols
import com.ritesh.cashiro.domain.repository.AccountTransactionStats
import com.ritesh.cashiro.presentation.effects.calculateDynamicFontSizeForBlurAnimatedCounterText
import com.ritesh.cashiro.presentation.ui.extras.components.extras.AccountDeletionDialogs
import com.ritesh.cashiro.presentation.ui.features.currency.CurrencyViewModel
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenEvent
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenState
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenViewModel
import com.ritesh.cashiro.presentation.ui.features.accounts.AnimatedGradientMeshCard
import com.ritesh.cashiro.presentation.ui.features.add_transaction.AddTransactionEvent
import com.ritesh.cashiro.presentation.ui.features.add_transaction.AddTransactionScreenState
import com.ritesh.cashiro.presentation.ui.features.add_transaction.CustomAmountNumberPadOutlinedTextField
import com.ritesh.cashiro.presentation.ui.features.currency.CurrencyBottomSheet
import com.ritesh.cashiro.presentation.ui.features.onboarding.OnBoardingEvent
import com.ritesh.cashiro.presentation.ui.theme.iosFont
import kotlinx.coroutines.launch
import kotlin.String

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAccountSheet(
    accountUiState: AccountScreenState,
    onAccountEvent: (AccountScreenEvent) -> Unit,
    getTransactionStatsForAccount: (id: Int, callback: (AccountTransactionStats?) -> Unit) -> Unit,
    updateAccountCurrency: (Int, String) -> Unit,
    currencyUiState: CurrencyViewModel.CurrencyUiState,
    selectCurrency: (String) -> Unit,
    onAddTransactionEvent: (AddTransactionEvent) -> Unit,
    transactionUiState: AddTransactionScreenState,
    sheetState: SheetState,
    isSheetOpen: MutableState<Boolean>,
    balance: String,
    accountName: String,
    isMainAccount: Boolean,
    cardColor1: Color,
    cardColor2: Color,
    currencyCode: String,
    isUpdate: Boolean = false,
    usedScreenWidth: Dp,
    usedScreenHeight: Dp,
    currentAccountEntity: AccountEntity? = null
){
    val themeColors = MaterialTheme.colorScheme
    val accountNameState = remember{ mutableStateOf(accountName) }
    val balanceState = remember { mutableStateOf(balance) }
    val isMainAccountState = remember { mutableStateOf(isMainAccount) }
    val cardColor1State = remember { mutableStateOf(cardColor1) }
    val cardColor2State = remember { mutableStateOf(cardColor2) }
    val currencyCodeState = remember { mutableStateOf(currencyCode) }

    val specialKeys = setOf('+', '-', '*', '/', '(', ')', '%', '×', '÷')

    val balanceSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val currencySheetState = rememberModalBottomSheetState(skipPartiallyExpanded= true)
    val accountBalanceSheetOpen = rememberSaveable { mutableStateOf(false) }
    val showCurrencySheet = rememberSaveable { mutableStateOf(false) }

    val firstColorsList = remember {
        listOf(
            Color(0xFF696eff),
            Color(0xFF6420AA),
            Color(0xFF83C6A4),
            Color(0xFFff0f7b),
            Color(0xFFff930f),
            Color(0xFFf9b16e)
        ) }
    val secondColorsList = remember {
        listOf(
            Color(0xFF93D7DE),
            Color(0xFFFF3EA5),
            Color(0xFFf8acff),
            Color(0xFFf89b29),
            Color(0xFFfff95b),
            Color(0xFFf68080),
        ) }
    val cardFirstColorIndex = rememberSaveable { mutableIntStateOf(0) }
    val cardSecondColorIndex = rememberSaveable { mutableIntStateOf(0) }
    val infiniteTransition = rememberInfiniteTransition(label = "")

    val lazyListState = rememberLazyListState()
    val showLeftSpacer = lazyListState.firstVisibleItemIndex > 0
    val showRightSpacer = lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index != firstColorsList.lastIndex

    LaunchedEffect(Unit) {
        if (isUpdate) {
            // Set initial indices based on passed-in colors
            val index1 = firstColorsList.indexOf(cardColor1)
            cardFirstColorIndex.intValue = if (index1 != -1) index1 else 0
            cardColor1State.value = cardColor1

            val index2 = secondColorsList.indexOf(cardColor2)
            cardSecondColorIndex.intValue = if (index2 != -1) index2 else 0
            cardColor2State.value = cardColor2
        } else {
            // Initialize with default values
            onAccountEvent(AccountScreenEvent.UpdateCardColor1(firstColorsList[cardFirstColorIndex.intValue]))
            cardColor1State.value = firstColorsList[cardFirstColorIndex.intValue]

            onAccountEvent(AccountScreenEvent.UpdateCardColor1(firstColorsList[cardSecondColorIndex.intValue]))
            cardColor2State.value = secondColorsList[cardSecondColorIndex.intValue]
        }
    }

    if (isSheetOpen.value){
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { isSheetOpen.value = false },
            sheetMaxWidth = usedScreenWidth - 10.dp,
            dragHandle = {
                DragHandle(
                    color = MaterialTheme.colorScheme.inverseSurface.copy(0.3f)
                )
            },
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            containerColor = themeColors.background,
            contentColor = themeColors.inverseSurface

        ) {

            AccountSheetContent(
                onAccountEvent = onAccountEvent,
                accountUiState = accountUiState,
                updateAccountCurrency = updateAccountCurrency,
                currencyUiState = currencyUiState,
                selectCurrency = selectCurrency,
                onAddTransactionEvent = onAddTransactionEvent,
                transactionUiState = transactionUiState,
                currentAccountEntity = currentAccountEntity,
                specialKeys = specialKeys,
                accountName = accountName,
                balanceState = balanceState,
                accountNameState = accountNameState,
                currencyCodeState = currencyCodeState,
                cardColor1State = cardColor1State,
                cardColor2State = cardColor2State,
                sheetState = sheetState,
                currencySheetState = currencySheetState,
                balanceSheetState = balanceSheetState,
                infiniteTransition = infiniteTransition,
                lazyListState = lazyListState,
                firstColorsList = firstColorsList,
                secondColorsList = secondColorsList,
                cardFirstColorIndex = cardFirstColorIndex,
                cardSecondColorIndex = cardSecondColorIndex,
                isMainAccountState = isMainAccountState,
                isSheetOpen = isSheetOpen,
                accountBalanceSheetOpen = accountBalanceSheetOpen,
                showCurrencySheet = showCurrencySheet,
                isUpdate = isUpdate,
                showLeftSpacer = showLeftSpacer,
                showRightSpacer = showRightSpacer,
                usedScreenWidth = usedScreenWidth,
                usedScreenHeight = usedScreenHeight,
                themeColors = themeColors,
            )
        }
    }
    // Add account deletion dialogs
    AccountDeletionDialogs(
        state = accountUiState,
        onEvent = onAccountEvent,
        getTransactionStatsForAccount = getTransactionStatsForAccount,
        themeColors = themeColors
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSheetContent(
    accountUiState: AccountScreenState,
    onAccountEvent: (AccountScreenEvent) -> Unit,
    updateAccountCurrency: (Int, String) -> Unit,
    currencyUiState: CurrencyViewModel.CurrencyUiState,
    selectCurrency: (String) -> Unit,
    onAddTransactionEvent: (AddTransactionEvent) -> Unit,
    transactionUiState: AddTransactionScreenState,
    currentAccountEntity: AccountEntity? = null,
    specialKeys: Set<Char>,
    accountName: String,
    balanceState: MutableState<String>,
    accountNameState: MutableState<String>,
    currencyCodeState: MutableState<String>,
    cardColor1State: MutableState<Color>,
    cardColor2State: MutableState<Color>,
    sheetState: SheetState,
    currencySheetState: SheetState,
    balanceSheetState: SheetState,
    infiniteTransition: InfiniteTransition,
    lazyListState: LazyListState,
    firstColorsList: List<Color>,
    secondColorsList: List<Color>,
    cardFirstColorIndex: MutableIntState,
    cardSecondColorIndex: MutableIntState,
    isMainAccountState: MutableState<Boolean>,
    isSheetOpen: MutableState<Boolean>,
    accountBalanceSheetOpen: MutableState<Boolean>,
    showCurrencySheet: MutableState<Boolean>,
    isUpdate: Boolean,
    showLeftSpacer: Boolean,
    showRightSpacer: Boolean,
    usedScreenWidth: Dp,
    usedScreenHeight: Dp,
    themeColors: ColorScheme,
){
    Box(modifier = Modifier
        .fillMaxWidth()
        .height(usedScreenHeight)
        .navigationBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(16.dp)),
            verticalArrangement = Arrangement.spacedBy(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item(key = "Header text") {
                Text(
                    text = if (isUpdate) "Edit Account" else "Add Account",
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = iosFont,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 15.dp)
                )
            }

            item(key = "Account Card Preview") {
                AccountCardPreview(
                    accountUiState = accountUiState,
                    balanceState = balanceState,
                    accountNameState = accountNameState,
                    isMainAccountState = isMainAccountState,
                    currencyCode = currencyCodeState,
                    cardColor1State = cardColor1State,
                    cardColor2State = cardColor2State,
                    accountName = accountName,
                    specialKeys = specialKeys,
                    themeColors = themeColors,
                    currentAccountEntity = currentAccountEntity
                )
            }
            item(key = "Account Card Details Input"){
                AccountDetailsInput(
                    onAccountEvent = onAccountEvent,
                    accountNameState = accountNameState,
                    accountName = accountName,
                    themeColors = themeColors
                )
            }



            item(key = "Balance Input") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AccountBalanceInput(
                        onAccountEvent = onAccountEvent,
                        balanceState = balanceState,
                        balanceSheetState = balanceSheetState,
                        currencyCode = currencyCodeState,
                        specialKeys = specialKeys,
                        accountBalanceSheetOpen = accountBalanceSheetOpen,
                        usedScreenWidth = usedScreenWidth,
                        themeColors = themeColors,
                        modifier = Modifier.weight(1f),
                        onAddTransactionEvent = onAddTransactionEvent,
                        transactionUiState = transactionUiState,
                    )
                    AccountCurrencyInput(
                        onAccountEvent = onAccountEvent,
                        updateAccountCurrency = updateAccountCurrency,
                        currencyUiState = currencyUiState,
                        selectCurrency =  selectCurrency,
                        accountId = currentAccountEntity?.id ?: 0,
                        currencyCode = currencyCodeState,
                        showCurrencySheet = showCurrencySheet,
                        currencySheetState = currencySheetState,
                        onDismiss = { showCurrencySheet.value = false },
                        usedScreenHeight = usedScreenHeight,
                        usedScreenWidth = usedScreenWidth,
                        themeColors = themeColors,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            item(key = "Account Card Color Selection"){
                AccountCardColorSelection(
                    onAccountEvent = onAccountEvent,
                    cardColor1State = cardColor1State,
                    cardColor2State = cardColor2State,
                    firstColorsList = firstColorsList,
                    secondColorsList = secondColorsList,
                    cardFirstColorIndex = cardFirstColorIndex,
                    cardSecondColorIndex = cardSecondColorIndex,
                    lazyListState = lazyListState,
                    infiniteTransition = infiniteTransition,
                    showLeftSpacer = showLeftSpacer,
                    showRightSpacer = showRightSpacer,
                    themeColors = themeColors
                )

            }
            // Add merge button for existing accounts
            if (isUpdate && currentAccountEntity != null) {
                item(key = "Merge Account Button") {
                    Button(
                        onClick = {
                            onAccountEvent(
                                AccountScreenEvent.ShowMergeAccountDialog(currentAccountEntity)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding( vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            contentColor = themeColors.inverseSurface,
                            containerColor = themeColors.surface
                        ),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(vertical = 12.dp, horizontal = 21.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "Merge Account",
                                fontSize = 16.sp,
                                fontFamily = iosFont,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Merge this account and move all transactions of this account with another account",
                                fontSize = 12.sp,
                                fontFamily = iosFont,
                                color = themeColors.inverseSurface.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            item{
                Spacer(modifier = Modifier.size(150.dp))
            }
        }
        AddAccountBottomButton(
            onAccountEvent = onAccountEvent,
            balanceState = balanceState,
            accountNameState = accountNameState ,
            cardColor1State = cardColor1State,
            cardColor2State = cardColor2State,
            isMainAccountState = isMainAccountState,
            currencyCodeState = currencyCodeState,
            currentAccountEntity = currentAccountEntity,
            isUpdate = isUpdate ,
            isSheetOpen = isSheetOpen ,
            sheetState = sheetState ,
            themeColors = themeColors,
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .align(Alignment.BottomCenter)
                .padding(bottom = 5.dp)
        )
    }
}

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun EditAccountSheet(
//    accountViewModel: AccountScreenViewModel,
//    currencyViewModel : CurrencyViewModel,
//    sheetState: SheetState,
//    isSheetOpen: MutableState<Boolean>,
//    balance: String,
//    accountName: String,
//    isMainAccount: Boolean,
//    cardColor1: Color,
//    cardColor2: Color,
//    currencyCode: String,
//    isUpdate: Boolean = false,
//    usedScreenWidth: Dp,
//    usedScreenHeight: Dp,
//    currentAccountEntity: AccountEntity? = null
//){
//    val themeColors = MaterialTheme.colorScheme
//    val accountNameState = remember{ mutableStateOf(accountName) }
//    val balanceState = remember { mutableStateOf(balance) }
//    val isMainAccountState = remember { mutableStateOf(isMainAccount) }
//    val cardColor1State = remember { mutableStateOf(cardColor1) }
//    val cardColor2State = remember { mutableStateOf(cardColor2) }
//    val currencyCodeState = remember { mutableStateOf(currencyCode) }
//
//    val specialKeys = setOf('+', '-', '*', '/', '(', ')', '%', '×', '÷')
//
//    val balanceSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
//    val currencySheetState = rememberModalBottomSheetState(skipPartiallyExpanded= true)
//    val accountBalanceSheetOpen = rememberSaveable { mutableStateOf(false) }
//    val showCurrencySheet = rememberSaveable { mutableStateOf(false) }
//
//    val firstColorsList = remember {
//        listOf(
//            Color(0xFF696eff),
//            Color(0xFF6420AA),
//            Color(0xFF83C6A4),
//            Color(0xFFff0f7b),
//            Color(0xFFff930f),
//            Color(0xFFf9b16e)
//        ) }
//    val secondColorsList = remember {
//        listOf(
//            Color(0xFF93D7DE),
//            Color(0xFFFF3EA5),
//            Color(0xFFf8acff),
//            Color(0xFFf89b29),
//            Color(0xFFfff95b),
//            Color(0xFFf68080),
//        ) }
//    val cardFirstColorIndex = rememberSaveable { mutableIntStateOf(0) }
//    val cardSecondColorIndex = rememberSaveable { mutableIntStateOf(0) }
//    val infiniteTransition = rememberInfiniteTransition(label = "")
//
//    val lazyListState = rememberLazyListState()
//    val showLeftSpacer = lazyListState.firstVisibleItemIndex > 0
//    val showRightSpacer = lazyListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index != firstColorsList.lastIndex
//
//    LaunchedEffect(Unit) {
//        if (isUpdate) {
//            // Set initial indices based on passed-in colors
//            val index1 = firstColorsList.indexOf(cardColor1)
//            cardFirstColorIndex.intValue = if (index1 != -1) index1 else 0
//            cardColor1State.value = cardColor1
//
//            val index2 = secondColorsList.indexOf(cardColor2)
//            cardSecondColorIndex.intValue = if (index2 != -1) index2 else 0
//            cardColor2State.value = cardColor2
//        } else {
//            // Initialize with default values
//            accountViewModel.onEvent(AccountScreenEvent.UpdateCardColor1(firstColorsList[cardFirstColorIndex.intValue]))
//            cardColor1State.value = firstColorsList[cardFirstColorIndex.intValue]
//
//            accountViewModel.onEvent(AccountScreenEvent.UpdateCardColor1(firstColorsList[cardSecondColorIndex.intValue]))
//            cardColor2State.value = secondColorsList[cardSecondColorIndex.intValue]
//        }
//    }
//
//    if (isSheetOpen.value){
//        ModalBottomSheet(
//            sheetState = sheetState,
//            onDismissRequest = { isSheetOpen.value = false },
//            sheetMaxWidth = usedScreenWidth - 10.dp,
//            dragHandle = {
//                DragHandle(
//                    color = MaterialTheme.colorScheme.inverseSurface.copy(0.3f)
//                )
//            },
//            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
//            containerColor = themeColors.background,
//            contentColor = themeColors.inverseSurface
//
//        ) {
//
//            AccountSheetContent(
//                accountViewModel = accountViewModel,
//                currencyViewModel = currencyViewModel,
//                currentAccountEntity =  currentAccountEntity,
//                specialKeys = specialKeys,
//                accountName = accountName,
//                balanceState = balanceState,
//                accountNameState = accountNameState,
//                currencyCodeState = currencyCodeState,
//                cardColor1State = cardColor1State,
//                cardColor2State = cardColor2State,
//                sheetState = sheetState,
//                currencySheetState = currencySheetState,
//                balanceSheetState = balanceSheetState,
//                infiniteTransition = infiniteTransition,
//                lazyListState = lazyListState,
//                firstColorsList = firstColorsList,
//                secondColorsList = secondColorsList,
//                cardFirstColorIndex = cardFirstColorIndex,
//                cardSecondColorIndex = cardSecondColorIndex,
//                isMainAccountState = isMainAccountState,
//                isSheetOpen = isSheetOpen,
//                accountBalanceSheetOpen = accountBalanceSheetOpen,
//                showCurrencySheet = showCurrencySheet,
//                isUpdate = isUpdate,
//                showLeftSpacer = showLeftSpacer,
//                showRightSpacer = showRightSpacer,
//                usedScreenWidth = usedScreenWidth,
//                usedScreenHeight = usedScreenHeight,
//                themeColors = themeColors,
//            )
//        }
//    }
//    // Add account deletion dialogs
//    AccountDeletionDialogs(
//        state = accountViewModel.state.collectAsState().value,
//        onEvent = accountViewModel::onEvent,
//        accountViewModel = accountViewModel,
//        accounts = accountViewModel.accounts.collectAsState().value,
//        themeColors = themeColors
//    )
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AccountSheetContent(
//    accountViewModel: AccountScreenViewModel,
//    currencyViewModel: CurrencyViewModel,
//    currentAccountEntity: AccountEntity? = null,
//    specialKeys: Set<Char>,
//    accountName: String,
//    balanceState: MutableState<String>,
//    accountNameState: MutableState<String>,
//    currencyCodeState: MutableState<String>,
//    cardColor1State: MutableState<Color>,
//    cardColor2State: MutableState<Color>,
//    sheetState: SheetState,
//    currencySheetState: SheetState,
//    balanceSheetState: SheetState,
//    infiniteTransition: InfiniteTransition,
//    lazyListState: LazyListState,
//    firstColorsList: List<Color>,
//    secondColorsList: List<Color>,
//    cardFirstColorIndex: MutableIntState,
//    cardSecondColorIndex: MutableIntState,
//    isMainAccountState: MutableState<Boolean>,
//    isSheetOpen: MutableState<Boolean>,
//    accountBalanceSheetOpen: MutableState<Boolean>,
//    showCurrencySheet: MutableState<Boolean>,
//    isUpdate: Boolean,
//    showLeftSpacer: Boolean,
//    showRightSpacer: Boolean,
//    usedScreenWidth: Dp,
//    usedScreenHeight: Dp,
//    themeColors: ColorScheme,
//){
//    Box(modifier = Modifier
//        .fillMaxWidth()
//        .height(usedScreenHeight)
//        .navigationBarsPadding()
//    ) {
//        LazyColumn(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(horizontal = 20.dp),
//            verticalArrangement = Arrangement.spacedBy(15.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            item(key = "Header text") {
//                Text(
//                    text = if (isUpdate) "Edit Account" else "Add Account",
//                    textAlign = TextAlign.Center,
//                    fontSize = 18.sp,
//                    fontWeight = FontWeight.SemiBold,
//                    fontFamily = iosFont,
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(bottom = 15.dp)
//                )
//            }
//
//            item(key = "Account Card Preview") {
//                AccountCardPreview(
//                    accountViewModel = accountViewModel,
//                    balanceState = balanceState,
//                    accountNameState = accountNameState,
//                    isMainAccountState = isMainAccountState,
//                    currencyCode = currencyCodeState,
//                    cardColor1State = cardColor1State,
//                    cardColor2State = cardColor2State,
//                    accountName = accountName,
//                    specialKeys = specialKeys,
//                    themeColors = themeColors,
//                    currentAccountEntity = currentAccountEntity
//                )
//            }
//            item(key = "Account Card Details Input"){
//                AccountDetailsInput(
//                    accountViewModel = accountViewModel,
//                    accountNameState = accountNameState,
//                    accountName = accountName,
//                    themeColors = themeColors
//                )
//            }
//
//
//
//            item(key = "Balance Input") {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.spacedBy(10.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    AccountBalanceInput(
//                        accountViewModel = accountViewModel,
//                        balanceState =balanceState,
//                        balanceSheetState =balanceSheetState,
//                        currencyCode = currencyCodeState,
//                        specialKeys =specialKeys,
//                        accountBalanceSheetOpen =accountBalanceSheetOpen,
//                        usedScreenWidth =usedScreenWidth,
//                        themeColors =themeColors,
//                        modifier =  Modifier.weight(1f)
//                    )
//                    AccountCurrencyInput(
//                        accountViewModel = accountViewModel,
//                        currencyViewModel = currencyViewModel,
//                        accountId = currentAccountEntity?.id ?: 0,
//                        currencyCode = currencyCodeState,
//                        showCurrencySheet = showCurrencySheet,
//                        currencySheetState = currencySheetState,
//                        onDismiss = { showCurrencySheet.value = false },
//                        usedScreenHeight = usedScreenHeight,
//                        usedScreenWidth = usedScreenWidth,
//                        themeColors = themeColors,
//                        modifier = Modifier.weight(1f),
//
//                        )
//                }
//            }
//
//            item(key = "Account Card Color Selection"){
//                AccountCardColorSelection(
//                    accountViewModel = accountViewModel,
//                    cardColor1State = cardColor1State,
//                    cardColor2State = cardColor2State,
//                    firstColorsList = firstColorsList,
//                    secondColorsList = secondColorsList,
//                    cardFirstColorIndex = cardFirstColorIndex,
//                    cardSecondColorIndex = cardSecondColorIndex,
//                    lazyListState = lazyListState,
//                    infiniteTransition = infiniteTransition,
//                    showLeftSpacer = showLeftSpacer,
//                    showRightSpacer = showRightSpacer,
//                    themeColors = themeColors
//                )
//
//            }
//            // Add merge button for existing accounts
//            if (isUpdate && currentAccountEntity != null) {
//                item(key = "Merge Account Button") {
//                    Button(
//                        onClick = {
//                            accountViewModel.onEvent(
//                                AccountScreenEvent.ShowMergeAccountDialog(currentAccountEntity)
//                            )
//                        },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding( vertical = 8.dp),
//                        colors = ButtonDefaults.buttonColors(
//                            contentColor = themeColors.inverseSurface,
//                            containerColor = themeColors.surface
//                        ),
//                        shape = RoundedCornerShape(16.dp),
//                        contentPadding = PaddingValues(vertical = 12.dp, horizontal = 21.dp)
//                    ) {
//                        Column(
//                            verticalArrangement = Arrangement.Center,
//                            horizontalAlignment = Alignment.Start
//                        ) {
//                            Text(
//                                text = "Merge Account",
//                                fontSize = 16.sp,
//                                fontFamily = iosFont,
//                                fontWeight = FontWeight.SemiBold
//                            )
//                            Spacer(modifier = Modifier.height(8.dp))
//                            Text(
//                                text = "Merge this account and move all transactions of this account with another account",
//                                fontSize = 12.sp,
//                                fontFamily = iosFont,
//                                color = themeColors.inverseSurface.copy(alpha = 0.7f),
//                                fontWeight = FontWeight.Medium
//                            )
//                        }
//                    }
//                }
//            }
//
//            item{
//                Spacer(modifier = Modifier.size(150.dp))
//            }
//        }
//        AddAccountBottomButton(
//            accountViewModel = accountViewModel ,
//            balanceState = balanceState,
//            accountNameState = accountNameState ,
//            cardColor1State = cardColor1State,
//            cardColor2State = cardColor2State,
//            isMainAccountState = isMainAccountState,
//            currencyCodeState = currencyCodeState,
//            currentAccountEntity = currentAccountEntity,
//            isUpdate = isUpdate ,
//            isSheetOpen = isSheetOpen ,
//            sheetState = sheetState ,
//            themeColors = themeColors,
//            modifier = Modifier
//                .fillMaxWidth()
//                .imePadding()
//                .align(Alignment.BottomCenter)
//                .padding(bottom = 5.dp)
//        )
//    }
//}


//@Composable
//private fun AccountCardPreview(
//    accountViewModel: AccountScreenViewModel,
//    balanceState: MutableState<String>,
//    accountNameState: MutableState<String>,
//    accountName: String,
//    isMainAccountState: MutableState<Boolean>,
//    currencyCode: MutableState<String>,
//    cardColor1State: MutableState<Color>,
//    cardColor2State: MutableState<Color>,
//    specialKeys: Set<Char>,
//    themeColors: ColorScheme,
//    currentAccountEntity: AccountEntity?
//){
//    val transactionCounts by accountViewModel.transactionCounts.collectAsState()
//    val transactionCount = transactionCounts[currentAccountEntity?.id ?: 0] ?: 0
//    Column(verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.CenterHorizontally) {
//        Box(contentAlignment = Alignment.BottomStart) {
//            val firstCardColor = cardColor1State.value
//            val secondCardColor = cardColor2State.value
//            AnimatedGradientMeshCard(
//                firstColor = firstCardColor,
//                secondColor = secondCardColor,
//            )
//            Column(
//                horizontalAlignment = Alignment.Start,
//                verticalArrangement = Arrangement.Center,
//                modifier = Modifier.padding(start = 15.dp, bottom = 15.dp)
//            ) {
//                Text(
//                    text = if (accountNameState.value.isBlank() || accountNameState.value == "") {
//                        if (accountName.isBlank() || accountName == "") "Account Name" else accountName
//                    } else {
//                        accountNameState.value
//                    },
//                    fontSize = 34.sp,
//                    lineHeight = 36.sp,
//                    maxLines = 2,
//                    overflow = TextOverflow.Ellipsis,
//                    fontFamily = iosFont,
//                    fontWeight = FontWeight.SemiBold,
//                    modifier = Modifier.padding(bottom = 25.dp),
//                    color = Color.White
//                )
//                Row {
//                    Column(modifier = Modifier.weight(1f)) {
//                        val displayText = if (balanceState.value.isEmpty() || balanceState.value.any { it in specialKeys } || balanceState.value.all { it == '0' }) {
//                            "0"
//                        } else {
//                            val trimmedInput =
//                                balanceState.value.trimStart('0').ifEmpty { "0" }
//                            if (trimmedInput.contains('.')) {
//                                val (integerPart, fractionalPart) = trimmedInput.split('.')
//                                val cleanedFractionalPart = fractionalPart.trimEnd('0')
//                                if (cleanedFractionalPart.isEmpty()) {
//                                    integerPart
//                                } else {
//                                    "$integerPart.$cleanedFractionalPart"
//                                }
//                            } else {
//                                trimmedInput
//                            }
//                        }
//                        val dynamicFontSize = remember(displayText) {
//                                calculateDynamicFontSizeForBlurAnimatedCounterText(displayText)
//                        }
//                        Row(
//                            horizontalArrangement = Arrangement.Center,
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Text(
//                                text = CurrencySymbols.getSymbol(currencyCode.value),
//                                fontSize = 20.sp,
//                                maxLines = 1,
//                                overflow = TextOverflow.Ellipsis,
//                                fontFamily = iosFont,
//                                fontWeight = FontWeight.SemiBold,
//                                color = if (balanceState.value.isEmpty()) themeColors.inverseSurface.copy(
//                                    0.8f
//                                ) else themeColors.inverseSurface,
//                                modifier= Modifier.padding(end = 3.dp)
//                            )
//                            Text(
//                                text = displayText,
//                                fontSize = dynamicFontSize.sp,
//                                fontWeight = FontWeight.SemiBold,
//                                maxLines = 1,
//                                overflow = TextOverflow.Ellipsis,
//                                fontFamily = iosFont,
//                                color = if (balanceState.value.isEmpty()) themeColors.inverseSurface.copy(
//                                    0.8f
//                                ) else themeColors.inverseSurface
//                            )
//                        }
//                        Text(
//                            text = "$transactionCount Transactions",
//                            fontSize = 12.sp,
//                            fontFamily = iosFont,
//                            fontWeight = FontWeight.SemiBold,
//                            color = themeColors.inverseSurface.copy(0.5f)
//                        )
//                    }
//
//                    Box(contentAlignment = Alignment.Center,
//                        modifier = Modifier
//                            .padding(top = 10.dp, end = 10.dp)
//                            .background(
//                                color = if (isMainAccountState.value) themeColors.primary else themeColors.surface,
//                                shape = RoundedCornerShape(10.dp))
//                            .padding(horizontal = 10.dp, vertical = 5.dp)
//                    ){
//                        Row(
//                            horizontalArrangement = Arrangement.Center,
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Icon(
//                                painter = if (isMainAccountState.value) painterResource(R.drawable.star_bulk) else painterResource(R.drawable.star_bulk),
//                                contentDescription = null,
//                                tint = if (isMainAccountState.value) Color.White else themeColors.inverseSurface.copy(alpha = 0.3f),
//                                modifier = Modifier.size(20.dp)
//                            )
//
//                            AnimatedVisibility(visible = isMainAccountState.value) {
//                                Text(
//                                    text = "Main",
//                                    textAlign = TextAlign.Center,
//                                    fontSize = 14.sp,
//                                    lineHeight = 14.sp,
//                                    color = Color.White,
//                                    fontFamily = iosFont,
//                                    fontWeight = FontWeight.Bold,
//                                    modifier = Modifier.padding(horizontal = 5.dp)
//                                )
//                            }
//                        }
//                    }
//                }
//
//            }
//
//        }
//
//
//        Text(
//            text = "Preview",
//            textAlign = TextAlign.Center,
//            fontSize = 14.sp,
//            color = themeColors.inverseOnSurface.copy(alpha = 0.85f),
//            fontFamily = iosFont,
//            fontWeight = FontWeight.Medium,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(top = 16.dp)
//        )
//    }
//
//}
@Composable
private fun AccountCardPreview(
    accountUiState: AccountScreenState,
    balanceState: MutableState<String>,
    accountNameState: MutableState<String>,
    accountName: String,
    isMainAccountState: MutableState<Boolean>,
    currencyCode: MutableState<String>,
    cardColor1State: MutableState<Color>,
    cardColor2State: MutableState<Color>,
    specialKeys: Set<Char>,
    themeColors: ColorScheme,
    currentAccountEntity: AccountEntity?
){
    val transactionCounts = accountUiState.transactionCounts
    val transactionCount = transactionCounts[currentAccountEntity?.id ?: 0] ?: 0
    Column(verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.BottomStart) {
            val firstCardColor = cardColor1State.value
            val secondCardColor = cardColor2State.value
            AnimatedGradientMeshCard(
                firstColor = firstCardColor,
                secondColor = secondCardColor,
            )
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(start = 15.dp, bottom = 15.dp)
            ) {
                Text(
                    text = if (accountNameState.value.isBlank() || accountNameState.value == "") {
                        if (accountName.isBlank() || accountName == "") "Account Name" else accountName
                    } else {
                        accountNameState.value
                    },
                    fontSize = 34.sp,
                    lineHeight = 36.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = iosFont,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 25.dp),
                    color = Color.White
                )
                Row {
                    Column(modifier = Modifier.weight(1f)) {
                        val displayText = if (balanceState.value.isEmpty() || balanceState.value.any { it in specialKeys } || balanceState.value.all { it == '0' }) {
                            "0"
                        } else {
                            val trimmedInput =
                                balanceState.value.trimStart('0').ifEmpty { "0" }
                            if (trimmedInput.contains('.')) {
                                val (integerPart, fractionalPart) = trimmedInput.split('.')
                                val cleanedFractionalPart = fractionalPart.trimEnd('0')
                                if (cleanedFractionalPart.isEmpty()) {
                                    integerPart
                                } else {
                                    "$integerPart.$cleanedFractionalPart"
                                }
                            } else {
                                trimmedInput
                            }
                        }
                        val dynamicFontSize = remember(displayText) {
                            calculateDynamicFontSizeForBlurAnimatedCounterText(displayText)
                        }
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = CurrencySymbols.getSymbol(currencyCode.value),
                                fontSize = 20.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontFamily = iosFont,
                                fontWeight = FontWeight.SemiBold,
                                color = if (balanceState.value.isEmpty()) themeColors.inverseSurface.copy(
                                    0.8f
                                ) else themeColors.inverseSurface,
                                modifier= Modifier.padding(end = 3.dp)
                            )
                            Text(
                                text = displayText,
                                fontSize = dynamicFontSize.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontFamily = iosFont,
                                color = if (balanceState.value.isEmpty()) themeColors.inverseSurface.copy(
                                    0.8f
                                ) else themeColors.inverseSurface
                            )
                        }
                        Text(
                            text = "$transactionCount Transactions",
                            fontSize = 12.sp,
                            fontFamily = iosFont,
                            fontWeight = FontWeight.SemiBold,
                            color = themeColors.inverseSurface.copy(0.5f)
                        )
                    }

                    Box(contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(top = 10.dp, end = 10.dp)
                            .background(
                                color = if (isMainAccountState.value) themeColors.primary else themeColors.surface,
                                shape = RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ){
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = if (isMainAccountState.value) painterResource(R.drawable.star_bulk) else painterResource(R.drawable.star_bulk),
                                contentDescription = null,
                                tint = if (isMainAccountState.value) Color.White else themeColors.inverseSurface.copy(alpha = 0.3f),
                                modifier = Modifier.size(20.dp)
                            )

                            AnimatedVisibility(visible = isMainAccountState.value) {
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


        Text(
            text = "Preview",
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            color = themeColors.inverseOnSurface.copy(alpha = 0.85f),
            fontFamily = iosFont,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )
    }

}

//@Composable
//private fun AccountDetailsInput(
//    accountViewModel: AccountScreenViewModel,
//    accountNameState: MutableState<String>,
//    onAccountNameChange: ((String) -> Unit)? = null,
//    accountName: String,
//    themeColors: ColorScheme
//){
//    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
//        Text(
//            text = "Name",
//            fontSize = 14.sp,
//            fontFamily = iosFont,
//            fontWeight = FontWeight.Medium,
//            modifier = Modifier
//                .padding(start = 16.dp)
//                .fillMaxWidth(),
//            color = themeColors.inverseSurface
//        )
//        OutlinedTextField(
//            value = accountNameState.value,
//            onValueChange = {
//                if (onAccountNameChange != null) {
//                    onAccountNameChange(it)
//                } else {
//                    accountNameState.value = it
//                    accountViewModel.onEvent(AccountScreenEvent.UpdateAccountName(it))
//                }
//            },
//            placeholder = {
//                Text(
//                    text = if (accountNameState.value.isBlank() || accountNameState.value == "") {
//                        if (accountName.isBlank() || accountName == "") "Account Name" else accountName
//                    } else {
//                        accountNameState.value
//                    },
//                    style = TextStyle(
//                        textAlign = TextAlign.Start,
//                        fontSize = 14.sp,
//                        fontFamily = iosFont,
//                        fontWeight = FontWeight.Medium,
//                        color = themeColors.inverseSurface.copy(0.5f)
//                    ),
//                    modifier = Modifier
//                        .height(20.dp)
//                        .fillMaxWidth(),
//                )
//            },
//            trailingIcon = {Icon(painter = painterResource(R.drawable.edit_name_bulk), tint = themeColors.inverseSurface.copy(0.5f), contentDescription = null)},
//            modifier = Modifier
//                .fillMaxWidth()
//                .background(
//                    themeColors.surface,
//                    RoundedCornerShape(15.dp)
//                ),
//            keyboardOptions = KeyboardOptions(
//                capitalization = KeyboardCapitalization.Words,
//                keyboardType = KeyboardType.Text
//            ),
//            singleLine = true,
//            colors = OutlinedTextFieldDefaults.colors(
//                focusedPlaceholderColor = themeColors.inverseSurface.copy(0.5f),
//                unfocusedPlaceholderColor = themeColors.inverseOnSurface.copy(0.5f),
//                unfocusedBorderColor = Color.Transparent,
//                focusedBorderColor = Color.Transparent,
//                unfocusedContainerColor = Color.Transparent,
//                focusedContainerColor = Color.Transparent
//
//            ),
//            textStyle = TextStyle(fontSize = 20.sp, fontFamily = iosFont, fontWeight = FontWeight.Medium)
//        )
//    }
//}
@Composable
private fun AccountDetailsInput(
    onAccountEvent: (AccountScreenEvent) -> Unit,
    accountNameState: MutableState<String>,
    onAccountNameChange: ((String) -> Unit)? = null,
    accountName: String,
    themeColors: ColorScheme
){
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Name",
            fontSize = 14.sp,
            fontFamily = iosFont,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .padding(start = 16.dp)
                .fillMaxWidth(),
            color = themeColors.inverseSurface
        )
        OutlinedTextField(
            value = accountNameState.value,
            onValueChange = {
                if (onAccountNameChange != null) {
                    onAccountNameChange(it)
                } else {
                    accountNameState.value = it
                    onAccountEvent(AccountScreenEvent.UpdateAccountName(it))
                }
            },
            placeholder = {
                Text(
                    text = if (accountNameState.value.isBlank() || accountNameState.value == "") {
                        if (accountName.isBlank() || accountName == "") "Account Name" else accountName
                    } else {
                        accountNameState.value
                    },
                    style = TextStyle(
                        textAlign = TextAlign.Start,
                        fontSize = 14.sp,
                        fontFamily = iosFont,
                        fontWeight = FontWeight.Medium,
                        color = themeColors.inverseSurface.copy(0.5f)
                    ),
                    modifier = Modifier
                        .height(20.dp)
                        .fillMaxWidth(),
                )
            },
            trailingIcon = {Icon(painter = painterResource(R.drawable.edit_name_bulk), tint = themeColors.inverseSurface.copy(0.5f), contentDescription = null)},
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    themeColors.surface,
                    RoundedCornerShape(15.dp)
                ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                keyboardType = KeyboardType.Text
            ),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedPlaceholderColor = themeColors.inverseSurface.copy(0.5f),
                unfocusedPlaceholderColor = themeColors.inverseOnSurface.copy(0.5f),
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent

            ),
            textStyle = TextStyle(fontSize = 20.sp, fontFamily = iosFont, fontWeight = FontWeight.Medium)
        )
    }
}

//@Composable
//private fun AccountCardColorSelection(
//    accountViewModel: AccountScreenViewModel,
//    cardColor1State: MutableState<Color>,
//    cardColor2State: MutableState<Color>,
//    firstColorsList: List<Color>,
//    secondColorsList: List<Color>,
//    cardFirstColorIndex: MutableIntState,
//    cardSecondColorIndex: MutableIntState,
//    onColor1Change: ((Color) -> Unit)? = null,
//    onColor2Change: ((Color) -> Unit)? = null,
//    lazyListState: LazyListState,
//    infiniteTransition: InfiniteTransition,
//    showLeftSpacer: Boolean,
//    showRightSpacer: Boolean,
//    themeColors: ColorScheme
//){
//    Column(
//        horizontalAlignment = Alignment.Start,
//        verticalArrangement = Arrangement.spacedBy(10.dp),) {
//        Text(
//            text = "Colors",
//            maxLines = 1,
//            overflow = TextOverflow.Ellipsis,
//            fontSize = 14.sp,
//            fontFamily = iosFont,
//            fontWeight = FontWeight.Medium,
//            color = themeColors.inverseSurface,
//            modifier = Modifier.padding(start = 15.dp)
//        )
//        Box(contentAlignment = Alignment.Center){
//            LazyRow(state = lazyListState, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
//                itemsIndexed(firstColorsList){ index, color ->
//                    val animatedColor by infiniteTransition.animateColor(
//                        initialValue = firstColorsList[index],
//                        targetValue = secondColorsList[index],
//                        animationSpec = infiniteRepeatable(
//                            animation = tween(3000, easing = FastOutLinearInEasing),
//                            repeatMode = RepeatMode.Reverse
//                        ), label = ""
//                    )
//                    val reverseAnimatedColor by infiniteTransition.animateColor(
//                        initialValue = secondColorsList[index],
//                        targetValue = firstColorsList[index],
//                        animationSpec = infiniteRepeatable(
//                            animation = tween(3000, easing = FastOutLinearInEasing),
//                            repeatMode = RepeatMode.Reverse
//                        ), label = ""
//                    )
//                    Card(
//                        modifier = Modifier.size(100.dp),
//                        elevation = CardDefaults.cardElevation(0.dp),
//                    ) {
//                        Box(modifier = Modifier
//                            .clickable {
//                                cardFirstColorIndex.intValue = index
//                                cardColor1State.value = firstColorsList[index]
//                                cardSecondColorIndex.intValue = index
//                                cardColor2State.value = secondColorsList[index]
//
//                                // Use callbacks if provided, otherwise use default behavior
//                                if (onColor1Change != null && onColor2Change != null) {
//                                    onColor1Change(firstColorsList[index])
//                                    onColor2Change(secondColorsList[index])
//                                } else {
//                                    accountViewModel.onEvent(AccountScreenEvent.UpdateCardColor1(firstColorsList[index]))
//                                    accountViewModel.onEvent(AccountScreenEvent.UpdateCardColor2(secondColorsList[index]))
//                                }
//                            },
//                            contentAlignment = Alignment.BottomCenter
//                        ) {
//                            Canvas(
//                                modifier = Modifier
//                                    .blur(
//                                        40.dp,
//                                        edgeTreatment = BlurredEdgeTreatment.Unbounded
//                                    )
//                                    .fillMaxSize()
//                            ) {
//                                val canvasWidth = size.width
//                                val canvasHeight = size.height
//
//                                val gradientBrush = Brush.linearGradient(
//                                    colors = listOf(
//                                        reverseAnimatedColor,
//                                        animatedColor,
//                                        themeColors.background
//                                    ),
//                                    start = Offset(0f, 0f),
//                                    end = Offset(canvasWidth, canvasHeight),
//                                    tileMode = TileMode.Mirror
//                                )
//
//                                drawRect(
//                                    brush = gradientBrush,
//                                    topLeft = Offset(0f, 0f),
//                                    size = size
//                                )
//                            }
//                            Spacer(
//                                modifier = Modifier
//                                    .height(50.dp)
//                                    .fillMaxWidth()
//                                    .background(
//                                        Brush.verticalGradient(
//                                            colors = listOf(
//                                                themeColors.surfaceVariant.copy(0.5f),
//                                                themeColors.surfaceVariant.copy(0.8f),
//                                                themeColors.surfaceVariant
//                                            )
//                                        )
//                                    )
//                            )
//                            if(cardFirstColorIndex.intValue == index && cardSecondColorIndex.intValue == index) {
//                                Icon(
//                                    painter = painterResource(R.drawable.verify_bulk),
//                                    contentDescription = "color selected icon",
//                                    tint = animatedColor,
//                                    modifier = Modifier.padding(bottom = 15.dp)
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//            LeftSpacerScrollFade(visible = showLeftSpacer,
//                modifier = Modifier.align(Alignment.CenterStart))
//            RightSpacerScrollFade(visible = showRightSpacer,
//                modifier = Modifier.align(Alignment.CenterEnd))
//        }
//    }
//}
@Composable
private fun AccountCardColorSelection(
    onAccountEvent: (AccountScreenEvent) -> Unit,
    cardColor1State: MutableState<Color>,
    cardColor2State: MutableState<Color>,
    firstColorsList: List<Color>,
    secondColorsList: List<Color>,
    cardFirstColorIndex: MutableIntState,
    cardSecondColorIndex: MutableIntState,
    onColor1Change: ((Color) -> Unit)? = null,
    onColor2Change: ((Color) -> Unit)? = null,
    lazyListState: LazyListState,
    infiniteTransition: InfiniteTransition,
    showLeftSpacer: Boolean,
    showRightSpacer: Boolean,
    themeColors: ColorScheme
){
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(10.dp),) {
        Text(
            text = "Colors",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = 14.sp,
            fontFamily = iosFont,
            fontWeight = FontWeight.Medium,
            color = themeColors.inverseSurface,
            modifier = Modifier.padding(start = 15.dp)
        )
        Box(contentAlignment = Alignment.Center){
            LazyRow(state = lazyListState, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                itemsIndexed(firstColorsList){ index, color ->
                    val animatedColor by infiniteTransition.animateColor(
                        initialValue = firstColorsList[index],
                        targetValue = secondColorsList[index],
                        animationSpec = infiniteRepeatable(
                            animation = tween(3000, easing = FastOutLinearInEasing),
                            repeatMode = RepeatMode.Reverse
                        ), label = ""
                    )
                    val reverseAnimatedColor by infiniteTransition.animateColor(
                        initialValue = secondColorsList[index],
                        targetValue = firstColorsList[index],
                        animationSpec = infiniteRepeatable(
                            animation = tween(3000, easing = FastOutLinearInEasing),
                            repeatMode = RepeatMode.Reverse
                        ), label = ""
                    )
                    Card(
                        modifier = Modifier.size(100.dp),
                        elevation = CardDefaults.cardElevation(0.dp),
                    ) {
                        Box(modifier = Modifier
                            .clickable {
                                cardFirstColorIndex.intValue = index
                                cardColor1State.value = firstColorsList[index]
                                cardSecondColorIndex.intValue = index
                                cardColor2State.value = secondColorsList[index]

                                // Use callbacks if provided, otherwise use default behavior
                                if (onColor1Change != null && onColor2Change != null) {
                                    onColor1Change(firstColorsList[index])
                                    onColor2Change(secondColorsList[index])
                                } else {
                                    onAccountEvent(AccountScreenEvent.UpdateCardColor1(firstColorsList[index]))
                                    onAccountEvent(AccountScreenEvent.UpdateCardColor2(secondColorsList[index]))
                                }
                            },
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Canvas(
                                modifier = Modifier
                                    .blur(
                                        40.dp,
                                        edgeTreatment = BlurredEdgeTreatment.Unbounded
                                    )
                                    .fillMaxSize()
                            ) {
                                val canvasWidth = size.width
                                val canvasHeight = size.height

                                val gradientBrush = Brush.linearGradient(
                                    colors = listOf(
                                        reverseAnimatedColor,
                                        animatedColor,
                                        themeColors.background
                                    ),
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
                                modifier = Modifier
                                    .height(50.dp)
                                    .fillMaxWidth()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                themeColors.surfaceVariant.copy(0.5f),
                                                themeColors.surfaceVariant.copy(0.8f),
                                                themeColors.surfaceVariant
                                            )
                                        )
                                    )
                            )
                            if(cardFirstColorIndex.intValue == index && cardSecondColorIndex.intValue == index) {
                                Icon(
                                    painter = painterResource(R.drawable.verify_bulk),
                                    contentDescription = "color selected icon",
                                    tint = animatedColor,
                                    modifier = Modifier.padding(bottom = 15.dp)
                                )
                            }
                        }
                    }
                }
            }
            LeftSpacerScrollFade(visible = showLeftSpacer,
                modifier = Modifier.align(Alignment.CenterStart))
            RightSpacerScrollFade(visible = showRightSpacer,
                modifier = Modifier.align(Alignment.CenterEnd))
        }
    }
}

@Composable
private fun LeftSpacerScrollFade(
    visible: Boolean,
    modifier: Modifier
){
    val themeColors = MaterialTheme.colorScheme
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300)),
        modifier = modifier // Align to the right
    ){
        Spacer(
            modifier = Modifier
                .height(100.dp)
                .width(30.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            themeColors.background,
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
private fun RightSpacerScrollFade(
    visible: Boolean,
    modifier: Modifier
){
    val themeColors = MaterialTheme.colorScheme
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(300)),
        modifier = modifier // Align to the right
    ){
        Spacer(
            modifier = Modifier
                .height(100.dp)
                .width(30.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            themeColors.background,
                        )
                    )
                )
        )
    }
}

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//private fun AccountBalanceInput(
//    accountViewModel: AccountScreenViewModel,
//    currencyCode: MutableState<String>,
//    balanceState: MutableState<String>,
//    balanceSheetState: SheetState,
//    onBalanceChange: ((String) -> Unit)? = null,
//    specialKeys: Set<Char>,
//    accountBalanceSheetOpen: MutableState<Boolean>,
//    usedScreenWidth: Dp,
//    themeColors: ColorScheme,
//    modifier: Modifier = Modifier
//){
//    // Amount input container
//    Box(
//        contentAlignment = Alignment.CenterStart,
//        modifier = modifier
//            .fillMaxWidth()
//            .clip(RoundedCornerShape(15.dp))
//            .clickable(
//                onClick = { accountBalanceSheetOpen.value = true },
//            )
//            .background(
//                color = themeColors.surface,
//                shape = RoundedCornerShape(15.dp)
//            )
//            .padding(vertical = 15.dp, horizontal = 15.dp)
//    ) {
//        Column(modifier = Modifier.fillMaxWidth(),
//            horizontalAlignment = Alignment.Start,
//            verticalArrangement = Arrangement.Center) {
//            Text(
//                text = "Balance",
//                maxLines = 1,
//                overflow = TextOverflow.Ellipsis,
//                fontSize = 14.sp,
//                fontFamily = iosFont,
//                fontWeight = FontWeight.Medium,
//                color = themeColors.inverseSurface,
//            )
//            Row(horizontalArrangement = Arrangement.Start,
//                verticalAlignment = Alignment.CenterVertically) {
//                Text(
//                    text = CurrencySymbols.getSymbol(currencyCode.value),
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis,
//                    fontSize = 24.sp,
//                    fontFamily = iosFont,
//                    fontWeight = FontWeight.Bold,
//                    color = if (balanceState.value.isEmpty()) themeColors.inverseSurface.copy(0.5f) else themeColors.primary,
//                    modifier= Modifier.padding(end = 3.dp)
//                )
//                Text(
//                    text = if (balanceState.value.isEmpty() || balanceState.value.any { it in specialKeys } || balanceState.value.all { it == '0' }) {
//                        "0"
//                    } else {
//                        val trimmedInput = balanceState.value.trimStart('0').ifEmpty { "0" }
//                        if (trimmedInput.contains('.')) {
//                            val (integerPart, fractionalPart) = trimmedInput.split('.')
//                            val cleanedFractionalPart = fractionalPart.trimEnd('0')
//                            if (cleanedFractionalPart.isEmpty()) {
//                                integerPart
//                            } else {
//                                "$integerPart.$cleanedFractionalPart"
//                            }
//                        } else {
//                            trimmedInput
//                        }
//                    },
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis,
//                    fontSize = 24.sp,
//                    fontFamily = iosFont,
//                    fontWeight = FontWeight.Bold,
//                    color = if (balanceState.value.isEmpty()) themeColors.inverseSurface.copy(0.5f) else themeColors.primary,
//                    modifier = Modifier.basicMarquee()
//                )
//            }
//        }
//    }
//
//    // BottomSheet for custom Amount input
//    if (accountBalanceSheetOpen.value) {
//        CustomNumberPadBottomSheet(
//            balanceState = balanceState,
//            balanceSheetState = balanceSheetState,
//            specialKeys = specialKeys,
//            accountBalanceSheetOpen = accountBalanceSheetOpen,
//            usedScreenWidth = usedScreenWidth - 5.dp,
//            accountViewModel = accountViewModel,
//            themeColors = themeColors,
//            currencySymbol = CurrencySymbols.getSymbol(currencyCode.value),
//            onBalanceUpdated = { newBalance ->
//                // Use callback if provided, otherwise use default behavior
//                if (onBalanceChange != null) {
//                    onBalanceChange(newBalance)
//                } else {
//                    accountViewModel.onEvent(AccountScreenEvent.UpdateBalance(newBalance))
//                }
//            }
//        )
//    }
//}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountBalanceInput(
    onAccountEvent: (AccountScreenEvent) -> Unit,
    onAddTransactionEvent: (AddTransactionEvent) -> Unit,
    transactionUiState: AddTransactionScreenState,
    currencyCode: MutableState<String>,
    balanceState: MutableState<String>,
    balanceSheetState: SheetState,
    onBalanceChange: ((String) -> Unit)? = null,
    specialKeys: Set<Char>,
    accountBalanceSheetOpen: MutableState<Boolean>,
    usedScreenWidth: Dp,
    themeColors: ColorScheme,
    modifier: Modifier = Modifier
){
    // Amount input container
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .clickable(
                onClick = { accountBalanceSheetOpen.value = true },
            )
            .background(
                color = themeColors.surface,
                shape = RoundedCornerShape(15.dp)
            )
            .padding(vertical = 15.dp, horizontal = 15.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center) {
            Text(
                text = "Balance",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 14.sp,
                fontFamily = iosFont,
                fontWeight = FontWeight.Medium,
                color = themeColors.inverseSurface,
            )
            Row(horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = CurrencySymbols.getSymbol(currencyCode.value),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 24.sp,
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Bold,
                    color = if (balanceState.value.isEmpty()) themeColors.inverseSurface.copy(0.5f) else themeColors.primary,
                    modifier= Modifier.padding(end = 3.dp)
                )
                Text(
                    text = if (balanceState.value.isEmpty() || balanceState.value.any { it in specialKeys } || balanceState.value.all { it == '0' }) {
                        "0"
                    } else {
                        val trimmedInput = balanceState.value.trimStart('0').ifEmpty { "0" }
                        if (trimmedInput.contains('.')) {
                            val (integerPart, fractionalPart) = trimmedInput.split('.')
                            val cleanedFractionalPart = fractionalPart.trimEnd('0')
                            if (cleanedFractionalPart.isEmpty()) {
                                integerPart
                            } else {
                                "$integerPart.$cleanedFractionalPart"
                            }
                        } else {
                            trimmedInput
                        }
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 24.sp,
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Bold,
                    color = if (balanceState.value.isEmpty()) themeColors.inverseSurface.copy(0.5f) else themeColors.primary,
                    modifier = Modifier.basicMarquee()
                )
            }
        }
    }

    // BottomSheet for custom Amount input
    if (accountBalanceSheetOpen.value) {
        CustomNumberPadBottomSheet(
            onAccountEvent = onAccountEvent,
            balanceState = balanceState,
            balanceSheetState = balanceSheetState,
            specialKeys = specialKeys,
            accountBalanceSheetOpen = accountBalanceSheetOpen,
            usedScreenWidth = usedScreenWidth - 5.dp,
            themeColors = themeColors,
            currencySymbol = CurrencySymbols.getSymbol(currencyCode.value),
            onBalanceUpdated = { newBalance ->
                // Use callback if provided, otherwise use default behavior
                if (onBalanceChange != null) {
                    onBalanceChange(newBalance)
                } else {
                    onAccountEvent(AccountScreenEvent.UpdateBalance(newBalance))
                }
            },
            onAddTransactionEvent = onAddTransactionEvent,
            transactionUiState = transactionUiState
        )
    }
}

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//private fun AccountCurrencyInput(
//    accountViewModel: AccountScreenViewModel,
//    currencyViewModel: CurrencyViewModel,
//    currencySheetState: SheetState,
//    currencyCode: MutableState<String>,
//    showCurrencySheet: MutableState<Boolean>,
//    onCurrencyChange: ((String) -> Unit)? = null,
//    onDismiss: () -> Unit,
//    usedScreenWidth: Dp,
//    usedScreenHeight: Dp,
//    themeColors: ColorScheme,
//    accountId: Int,
//    modifier: Modifier = Modifier
//){
//    // Amount input container
//    Box(
//        contentAlignment = Alignment.CenterStart,
//        modifier = modifier
//            .fillMaxWidth()
//            .clip(RoundedCornerShape(15.dp))
//            .clickable(
//                onClick = { showCurrencySheet.value = true },
//            )
//            .background(
//                color = MaterialTheme.colorScheme.surface,
//                shape = RoundedCornerShape(15.dp)
//            )
//            .padding(vertical = 15.dp, horizontal = 15.dp)
//    ) {
//        Column(modifier = Modifier.fillMaxWidth(),
//            horizontalAlignment = Alignment.Start,
//            verticalArrangement = Arrangement.Center) {
//            Text(
//                text = "Currency",
//                maxLines = 1,
//                overflow = TextOverflow.Ellipsis,
//                fontSize = 14.sp,
//                fontFamily = iosFont,
//                fontWeight = FontWeight.Medium,
//                color = themeColors.inverseSurface,
//            )
//            Row(horizontalArrangement = Arrangement.Start,
//                verticalAlignment = Alignment.CenterVertically) {
//                Text(
//                    text = CurrencySymbols.getSymbolWithCode(currencyCode.value),
//                    maxLines = 1,
//                    overflow = TextOverflow.Ellipsis,
//                    fontSize = 24.sp,
//                    fontFamily = iosFont,
//                    fontWeight = FontWeight.Bold,
//                    color = themeColors.primary
//                )
//            }
//        }
//    }
//
//    // BottomSheet for currency selection
//    if (showCurrencySheet.value) {
//        CurrencyBottomSheet(
//            currencyViewModel = currencyViewModel,
//            accountViewModel = accountViewModel,
//            accountId = accountId,
//            currencyCodeState = currencyCode,
//            sheetState = currencySheetState,
//            onDismiss = onDismiss,
//            usedScreenWidth = usedScreenWidth + 10.dp,
//            usedScreenHeight = usedScreenHeight - 10.dp,
//            onCurrencyCardClick = {
//                showCurrencySheet.value = false
//            },
//            onCurrencySelected = { newCurrency ->
//                // Use callback if provided, otherwise use default behavior
//                if (onCurrencyChange != null) {
//                    onCurrencyChange(newCurrency)
//                } else {
//                    currencyCode.value = newCurrency
//                }
//            }
//        )
//    }
//}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountCurrencyInput(
    onAccountEvent: (AccountScreenEvent) -> Unit,
    updateAccountCurrency: (Int, String) -> Unit,
    currencyUiState: CurrencyViewModel.CurrencyUiState,
    selectCurrency: (String) -> Unit,
    currencySheetState: SheetState,
    currencyCode: MutableState<String>,
    showCurrencySheet: MutableState<Boolean>,
    onCurrencyChange: ((String) -> Unit)? = null,
    onDismiss: () -> Unit,
    usedScreenWidth: Dp,
    usedScreenHeight: Dp,
    themeColors: ColorScheme,
    accountId: Int,
    modifier: Modifier = Modifier
){
    // Amount input container
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .clickable(
                onClick = { showCurrencySheet.value = true },
            )
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(15.dp)
            )
            .padding(vertical = 15.dp, horizontal = 15.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center) {
            Text(
                text = "Currency",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 14.sp,
                fontFamily = iosFont,
                fontWeight = FontWeight.Medium,
                color = themeColors.inverseSurface,
            )
            Row(horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = CurrencySymbols.getSymbolWithCode(currencyCode.value),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 24.sp,
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Bold,
                    color = themeColors.primary
                )
            }
        }
    }

    // BottomSheet for currency selection
    if (showCurrencySheet.value) {
        CurrencyBottomSheet(
            accountId = accountId,
            currencyCodeState = currencyCode,
            sheetState = currencySheetState,
            onDismiss = onDismiss,
            usedScreenWidth = usedScreenWidth + 10.dp,
            usedScreenHeight = usedScreenHeight - 10.dp,
            onCurrencyCardClick = {
                showCurrencySheet.value = false
            },
            onCurrencySelected = { newCurrency ->
                // Use callback if provided, otherwise use default behavior
                if (onCurrencyChange != null) {
                    onCurrencyChange(newCurrency)
                } else {
                    currencyCode.value = newCurrency
                }
            },
            onAccountEvent = onAccountEvent,
            updateAccountCurrency = updateAccountCurrency,
            currencyUiState = currencyUiState,
            selectCurrency = selectCurrency
        )
    }
}

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//private fun CustomNumberPadBottomSheet(
//    accountViewModel: AccountScreenViewModel,
//    balanceState: MutableState<String>,
//    currencySymbol: String,
//    balanceSheetState: SheetState,
//    specialKeys: Set<Char>,
//    accountBalanceSheetOpen: MutableState<Boolean>,
//    usedScreenWidth: Dp,
//    offSet: Dp = 0.dp,
//    themeColors: ColorScheme,
//    onBalanceUpdated: ((String) -> Unit)? = null // Add callback parameter
//){
//    var calculatedResultAmount by rememberSaveable { mutableStateOf("0") }
//    ModalBottomSheet(
//        onDismissRequest = { accountBalanceSheetOpen.value = false },
//        sheetState = balanceSheetState,
//        containerColor = themeColors.background,
//        sheetMaxWidth = usedScreenWidth,
//        dragHandle = {
//            DragHandle(
//                color = MaterialTheme.colorScheme.inverseSurface.copy(0.3f)
//            )
//        },
//        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
//    ) {
//        Text(
//            text = "Enter Amount",
//            fontFamily = iosFont,
//            textAlign = TextAlign.Center,
//            fontWeight = FontWeight.SemiBold,
//            fontSize = 18.sp,
//            color = themeColors.inverseSurface,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(bottom = 10.dp)
//        )
//        Column(
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Top,
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 16.dp)
//        ) {
//            CustomAmountNumberPadOutlinedTextField(
//                amountInput = balanceState.value,
//                specialKeys = specialKeys,
//                onValueChange = { newValue ->
//                    balanceState.value =  newValue },
//                onResultAmount = { result ->
//                    calculatedResultAmount = result
//                },
//                currencySymbol = currencySymbol,
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            val scope = rememberCoroutineScope()
//            AnimatedVisibility(visible = balanceState.value.isNotEmpty()) {
//                Button(
//                    onClick = {
//                        balanceState.value = calculatedResultAmount
//
//                        // Use callback if provided, otherwise use default behavior
//                        if (onBalanceUpdated != null) {
//                            onBalanceUpdated(calculatedResultAmount)
//                        } else {
//                            accountViewModel.onEvent(AccountScreenEvent.UpdateBalance(calculatedResultAmount))
//                        }
//
//                        scope
//                            .launch { balanceSheetState.hide() }
//                            .invokeOnCompletion {
//                                if (!balanceSheetState.isVisible) {
//                                    accountBalanceSheetOpen.value = false
//                                }
//                            }
//                    },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .shadow(
//                            elevation = 10.dp,
//                            shape = RoundedCornerShape(15.dp),
//                            spotColor = themeColors.primary,
//                            ambientColor = themeColors.primary
//                        )
//                        .height(50.dp)
//                        .background(
//                            color = themeColors.primary,
//                            shape = RoundedCornerShape(15.dp)
//                        ),
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = Color.Transparent,
//                        contentColor = Color.Transparent
//                    )
//                ) {
//                    Text(
//                        text = "Done",
//                        fontFamily = iosFont,
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.Medium,
//                        color = Color.White
//                    )
//                }
//            }
//        }
//    }
//}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomNumberPadBottomSheet(
    onAccountEvent: (AccountScreenEvent) -> Unit,
    onAddTransactionEvent: (AddTransactionEvent) -> Unit,
    transactionUiState: AddTransactionScreenState,
    balanceState: MutableState<String>,
    currencySymbol: String,
    balanceSheetState: SheetState,
    specialKeys: Set<Char>,
    accountBalanceSheetOpen: MutableState<Boolean>,
    usedScreenWidth: Dp,
    themeColors: ColorScheme,
    onBalanceUpdated: ((String) -> Unit)? = null // Add callback parameter
){
    var calculatedResultAmount by rememberSaveable { mutableStateOf("0") }
    ModalBottomSheet(
        onDismissRequest = { accountBalanceSheetOpen.value = false },
        sheetState = balanceSheetState,
        containerColor = themeColors.background,
        sheetMaxWidth = usedScreenWidth,
        dragHandle = {
            DragHandle(
                color = MaterialTheme.colorScheme.inverseSurface.copy(0.3f)
            )
        },
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        Text(
            text = "Enter Amount",
            fontFamily = iosFont,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = themeColors.inverseSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            CustomAmountNumberPadOutlinedTextField(
                amountInput = balanceState.value,
                specialKeys = specialKeys,
                onValueChange = { newValue ->
                    balanceState.value = newValue
                },
                onResultAmount = { result ->
                    calculatedResultAmount = result
                },
                currencySymbol = currencySymbol,
                transactionUiState = transactionUiState,
                onAddTransactionEvent = onAddTransactionEvent,
            )

            Spacer(modifier = Modifier.height(16.dp))

            val scope = rememberCoroutineScope()
            AnimatedVisibility(visible = balanceState.value.isNotEmpty()) {
                Button(
                    onClick = {
                        balanceState.value = calculatedResultAmount

                        // Use callback if provided, otherwise use default behavior
                        if (onBalanceUpdated != null) {
                            onBalanceUpdated(calculatedResultAmount)
                        } else {
                            onAccountEvent(AccountScreenEvent.UpdateBalance(calculatedResultAmount))
                        }

                        scope
                            .launch { balanceSheetState.hide() }
                            .invokeOnCompletion {
                                if (!balanceSheetState.isVisible) {
                                    accountBalanceSheetOpen.value = false
                                }
                            }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(15.dp),
                            spotColor = themeColors.primary,
                            ambientColor = themeColors.primary
                        )
                        .height(50.dp)
                        .background(
                            color = themeColors.primary,
                            shape = RoundedCornerShape(15.dp)
                        ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.Transparent
                    )
                ) {
                    Text(
                        text = "Done",
                        fontFamily = iosFont,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddAccountBottomButton(
    onAccountEvent: (AccountScreenEvent) -> Unit,
    balanceState: MutableState<String>,
    accountNameState: MutableState<String>,
    cardColor1State: MutableState<Color>,
    cardColor2State: MutableState<Color>,
    isMainAccountState: MutableState<Boolean>,
    currencyCodeState: MutableState<String>,
    isUpdate: Boolean = false,
    onComplete: () -> Unit = {},
    isSheetOpen: MutableState<Boolean>,
    sheetState: SheetState,
    themeColors: ColorScheme,
    currentAccountEntity: AccountEntity? = null,
    modifier: Modifier = Modifier
){
    Box(modifier = modifier, contentAlignment = Alignment.BottomCenter) {
        val scope = rememberCoroutineScope()
        val context = LocalContext.current

        val saveAccount = {
            // Use the AccountScreenEvent onAccountSaved to save account
           onAccountEvent(AccountScreenEvent.OnAccountSaved(
                onSuccess = {
                    scope.launch { sheetState.hide() }
                        .invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                isSheetOpen.value = false
                            }
                        }
                    if (accountNameState.value.isNotEmpty() || balanceState.value.isNotEmpty()) {
                        Toast.makeText(
                            context,
                            "${accountNameState.value} Account is Added",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    onComplete
                }
            ))
        }

        // Define the update logic
        val updateAccount = {
            currentAccountEntity?.let { entity ->
                // Check if currency is changing
                val isCurrencyChanged = entity.currencyCode != currencyCodeState.value

                val updatedAccount = entity.copy(
                    accountName = accountNameState.value,
                    balance = balanceState.value.toDouble(),
                    cardColor1 = cardColor1State.value.toArgb(),
                    cardColor2 = cardColor2State.value.toArgb(),
                    isMainAccount = isMainAccountState.value,
                    currencyCode = currencyCodeState.value
                )

                // Proceed with updating the account
                onAccountEvent(AccountScreenEvent.OnAccountUpdated(
                    account = updatedAccount,
                    onSuccess = {
                        scope.launch { sheetState.hide() }
                            .invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    isSheetOpen.value = false
                                }
                            }
                        if (accountNameState.value.isNotEmpty() || balanceState.value.isNotEmpty()) {
                            val message = if (isCurrencyChanged)
                                "${accountNameState.value} Account is Updated with new currency"
                            else
                                "${accountNameState.value} Account is Updated"

                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    }
                ))

                // If currency changed, show a specific message
                if (isCurrencyChanged) {
                    Log.d("CurrencyUpdate", "Currency changed in UI from ${entity.currencyCode} to ${currencyCodeState.value}")
                }
            } ?: run {
                // Handle the case where the Account is null
                Toast.makeText(context, "Account not found", Toast.LENGTH_SHORT).show()
            }
        }

        Button(
            onClick = if (isUpdate) updateAccount else saveAccount,
            modifier = Modifier
                .height(50.dp)
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .align(Alignment.BottomCenter),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (accountNameState.value.isEmpty() || balanceState.value.isEmpty()) themeColors.surfaceBright
                else themeColors.primary,
                contentColor = themeColors.inverseSurface
            )
        ) {
            Text(
                text = if (isUpdate) "Save Account" else "Add Account",
                fontFamily = iosFont,
                fontWeight = FontWeight.Medium,
                color = if (accountNameState.value.isEmpty() || balanceState.value.isEmpty()) themeColors.inverseSurface.copy(0.5f)
                else Color.White
            )
        }
    }
}
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun CreateAccountOnBoardingContent(
//    accountViewModel: AccountScreenViewModel,
//    currencyViewModel: CurrencyViewModel,
//    currentAccountEntity: AccountEntity? = null,
//    specialKeys: Set<Char>,
//    accountName: String,
//    balanceState: MutableState<String>,
//    accountNameState: MutableState<String>,
//    currencyCodeState: MutableState<String>,
//    cardColor1State: MutableState<Color>,
//    cardColor2State: MutableState<Color>,
//    currencySheetState: SheetState,
//    balanceSheetState: SheetState,
//    infiniteTransition: InfiniteTransition,
//    lazyListState: LazyListState,
//    firstColorsList: List<Color>,
//    secondColorsList: List<Color>,
//    cardFirstColorIndex: MutableIntState,
//    cardSecondColorIndex: MutableIntState,
//    isMainAccountState: MutableState<Boolean>,
//    accountBalanceSheetOpen: MutableState<Boolean>,
//    showCurrencySheet: MutableState<Boolean>,
//    onNext: () -> Unit,
//    onOnBoardingEvent: ((OnBoardingEvent) -> Unit)? = null,
//    isUpdate: Boolean = false,
//    showLeftSpacer: Boolean,
//    showRightSpacer: Boolean,
//    usedScreenWidth: Dp,
//    usedScreenHeight: Dp,
//    themeColors: ColorScheme,
//){
//    // Check if user has made minimum required changes
//    val hasMinimumData = accountNameState.value.isNotBlank() &&
//            balanceState.value.isNotBlank() &&
//            balanceState.value != "0"
//
//    val context = LocalContext.current
//
//    Box(modifier = Modifier
//        .fillMaxWidth()
//        .height(usedScreenHeight)
//    ) {
//        LazyColumn(
//            modifier = Modifier
//                .clip(RoundedCornerShape(16.dp))
//                .fillMaxSize(),
//            verticalArrangement = Arrangement.spacedBy(15.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            item(key = "Header text") {
//                Text(
//                    text = "Add First Account",
//                    fontSize = 24.sp,
//                    fontWeight = FontWeight.SemiBold,
//                    fontFamily = iosFont,
//                    color = MaterialTheme.colorScheme.inverseSurface,
//                    textAlign = TextAlign.Center,
//                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 22.dp)
//                )
//            }
//
//            item(key = "Account Card Preview") {
//                AccountCardPreview(
//                    accountViewModel = accountViewModel,
//                    balanceState = balanceState,
//                    accountNameState = accountNameState,
//                    isMainAccountState = isMainAccountState,
//                    currencyCode = currencyCodeState,
//                    cardColor1State = cardColor1State,
//                    cardColor2State = cardColor2State,
//                    accountName = accountName,
//                    specialKeys = specialKeys,
//                    themeColors = themeColors,
//                    currentAccountEntity = currentAccountEntity
//                )
//            }
//            item(key = "Account Card Details Input"){
//                AccountDetailsInput(
//                    accountViewModel = accountViewModel,
//                    accountNameState = accountNameState,
//                    accountName = accountName,
//                    themeColors = themeColors,
//                    // Add onboarding sync for account name
//                    onAccountNameChange = { name ->
//                        accountNameState.value = name
//                        accountViewModel.onEvent(AccountScreenEvent.UpdateAccountName(name))
//                        onOnBoardingEvent?.invoke(OnBoardingEvent.UpdateAccountName(name))
//                    }
//                )
//            }
//
//            item(key = "Balance Input") {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.spacedBy(10.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    AccountBalanceInput(
//                        accountViewModel = accountViewModel,
//                        balanceState =balanceState,
//                        balanceSheetState =balanceSheetState,
//                        currencyCode = currencyCodeState,
//                        specialKeys =specialKeys,
//                        accountBalanceSheetOpen =accountBalanceSheetOpen,
//                        usedScreenWidth =usedScreenWidth,
//                        themeColors =themeColors,
//                        modifier =  Modifier.weight(1f),
//                        // Add onboarding sync for balance
//                        onBalanceChange = { balance ->
//                            balanceState.value = balance
//                            accountViewModel.onEvent(AccountScreenEvent.UpdateBalance(balance))
//                            onOnBoardingEvent?.invoke(OnBoardingEvent.UpdateAccountBalance(balance))
//                        }
//                    )
//                    AccountCurrencyInput(
//                        accountViewModel = accountViewModel,
//                        currencyViewModel = currencyViewModel,
//                        accountId = currentAccountEntity?.id ?: 0,
//                        currencyCode = currencyCodeState,
//                        showCurrencySheet = showCurrencySheet,
//                        currencySheetState = currencySheetState,
//                        onDismiss = { showCurrencySheet.value = false },
//                        usedScreenHeight = usedScreenHeight,
//                        usedScreenWidth = usedScreenWidth,
//                        themeColors = themeColors,
//                        modifier = Modifier.weight(1f),
//                        // Add onboarding sync for currency
//                        onCurrencyChange = { currency ->
//                            currencyCodeState.value = currency
//                            onOnBoardingEvent?.invoke(OnBoardingEvent.UpdateSelectedCurrency(currency))
//                        }
//                    )
//                }
//            }
//
//            item(key = "Account Card Color Selection"){
//                AccountCardColorSelection(
//                    accountViewModel = accountViewModel,
//                    cardColor1State = cardColor1State,
//                    cardColor2State = cardColor2State,
//                    firstColorsList = firstColorsList,
//                    secondColorsList = secondColorsList,
//                    cardFirstColorIndex = cardFirstColorIndex,
//                    cardSecondColorIndex = cardSecondColorIndex,
//                    lazyListState = lazyListState,
//                    infiniteTransition = infiniteTransition,
//                    showLeftSpacer = showLeftSpacer,
//                    showRightSpacer = showRightSpacer,
//                    themeColors = themeColors,
//                    // Add onboarding sync for colors
//                    onColor1Change = { color ->
//                        cardColor1State.value = color
//                        accountViewModel.onEvent(AccountScreenEvent.UpdateCardColor1(color))
//                        onOnBoardingEvent?.invoke(OnBoardingEvent.UpdateAccountColor1(color))
//                    },
//                    onColor2Change = { color ->
//                        cardColor2State.value = color
//                        accountViewModel.onEvent(AccountScreenEvent.UpdateCardColor2(color))
//                        onOnBoardingEvent?.invoke(OnBoardingEvent.UpdateAccountColor2(color))
//                    }
//                )
//            }
//
//            // Only show merge button if this is an update (shouldn't happen in onboarding)
//            if (isUpdate && currentAccountEntity != null) {
//                item(key = "Merge Account Button") {
//                    Button(
//                        onClick = {
//                            accountViewModel.onEvent(
//                                AccountScreenEvent.ShowMergeAccountDialog(currentAccountEntity)
//                            )
//                        },
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding( vertical = 8.dp),
//                        colors = ButtonDefaults.buttonColors(
//                            contentColor = themeColors.inverseSurface,
//                            containerColor = themeColors.surface
//                        ),
//                        shape = RoundedCornerShape(16.dp),
//                        contentPadding = PaddingValues(vertical = 12.dp, horizontal = 21.dp)
//                    ) {
//                        Column(
//                            verticalArrangement = Arrangement.Center,
//                            horizontalAlignment = Alignment.Start
//                        ) {
//                            Text(
//                                text = "Merge Account",
//                                fontSize = 16.sp,
//                                fontFamily = iosFont,
//                                fontWeight = FontWeight.SemiBold
//                            )
//                            Spacer(modifier = Modifier.height(8.dp))
//                            Text(
//                                text = "Merge this account and move all transactions of this account with another account",
//                                fontSize = 12.sp,
//                                fontFamily = iosFont,
//                                color = themeColors.inverseSurface.copy(alpha = 0.7f),
//                                fontWeight = FontWeight.Medium
//                            )
//                        }
//                    }
//                }
//            }
//
//            item{
//                Button(
//                    onClick = {
//                        // First sync all state to onboarding
//                        onOnBoardingEvent?.invoke(OnBoardingEvent.UpdateAccountName(accountNameState.value))
//                        onOnBoardingEvent?.invoke(OnBoardingEvent.UpdateAccountBalance(balanceState.value))
//                        onOnBoardingEvent?.invoke(OnBoardingEvent.UpdateSelectedCurrency(currencyCodeState.value))
//                        onOnBoardingEvent?.invoke(OnBoardingEvent.UpdateAccountColor1(cardColor1State.value))
//                        onOnBoardingEvent?.invoke(OnBoardingEvent.UpdateAccountColor2(cardColor2State.value))
//
//                        // Save account using AccountViewModel only
//                        accountViewModel.onEvent(AccountScreenEvent.OnAccountSaved(
//                            onSuccess = {
//                                onNext()
//                            }
//                        ))
//                    },
//                    modifier = Modifier
//                        .padding(top = 24.dp)
//                        .fillMaxWidth()
//                        .height(56.dp)
//                        .shadow(
//                            elevation = 5.dp,
//                            shape = RoundedCornerShape(12.dp),
//                            spotColor = if (hasMinimumData) {
//                                themeColors.primary
//                            } else {
//                                themeColors.surface
//                            },
//                            ambientColor = Color.Transparent
//                        )
//                        .align(Alignment.BottomCenter),
//                    shape = RoundedCornerShape(16.dp),
//                    colors = ButtonDefaults.buttonColors(
//                        containerColor = if (hasMinimumData) {
//                            themeColors.primary
//                        } else {
//                            themeColors.surface
//                        },
//                        contentColor = if (hasMinimumData) {
//                            Color.White
//                        } else {
//                            themeColors.inverseSurface.copy(0.5f)
//                        }
//                    ),
//                    enabled = hasMinimumData
//                ) {
//                    Text(
//                        text = "Complete Setup",
//                        fontFamily = iosFont,
//                        fontWeight = FontWeight.SemiBold,
//                        fontSize = 16.sp
//                    )
//                }
//            }
//        }
//    }
//}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountOnBoardingContent(
    onAddTransactionEvent: (AddTransactionEvent) -> Unit,
    transactionUiState: AddTransactionScreenState,
    accountUiState: AccountScreenState,
    onAccountEvent: (AccountScreenEvent) -> Unit,
    updateAccountCurrency: (Int, String) -> Unit,
    currencyUiState: CurrencyViewModel.CurrencyUiState,
    selectCurrency: (String) -> Unit,
    currentAccountEntity: AccountEntity? = null,
    specialKeys: Set<Char>,
    accountName: String,
    balanceState: MutableState<String>,
    accountNameState: MutableState<String>,
    currencyCodeState: MutableState<String>,
    cardColor1State: MutableState<Color>,
    cardColor2State: MutableState<Color>,
    currencySheetState: SheetState,
    balanceSheetState: SheetState,
    infiniteTransition: InfiniteTransition,
    lazyListState: LazyListState,
    firstColorsList: List<Color>,
    secondColorsList: List<Color>,
    cardFirstColorIndex: MutableIntState,
    cardSecondColorIndex: MutableIntState,
    isMainAccountState: MutableState<Boolean>,
    accountBalanceSheetOpen: MutableState<Boolean>,
    showCurrencySheet: MutableState<Boolean>,
    onNext: () -> Unit,
    onOnBoardingEvent: ((OnBoardingEvent) -> Unit)? = null,
    isUpdate: Boolean = false,
    showLeftSpacer: Boolean,
    showRightSpacer: Boolean,
    usedScreenWidth: Dp,
    usedScreenHeight: Dp,
    themeColors: ColorScheme,
){
    // Check if user has made minimum required changes
    val hasMinimumData = accountNameState.value.isNotBlank() &&
            balanceState.value.isNotBlank() &&
            balanceState.value != "0"

    val context = LocalContext.current

    Box(modifier = Modifier
        .fillMaxWidth()
        .height(usedScreenHeight)
    ) {
        LazyColumn(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item(key = "Header text") {
                Text(
                    text = "Add First Account",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = iosFont,
                    color = MaterialTheme.colorScheme.inverseSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 22.dp)
                )
            }

            item(key = "Account Card Preview") {
                AccountCardPreview(
                    balanceState = balanceState,
                    accountNameState = accountNameState,
                    isMainAccountState = isMainAccountState,
                    currencyCode = currencyCodeState,
                    cardColor1State = cardColor1State,
                    cardColor2State = cardColor2State,
                    accountName = accountName,
                    specialKeys = specialKeys,
                    themeColors = themeColors,
                    currentAccountEntity = currentAccountEntity,
                    accountUiState = accountUiState
                )
            }
            item(key = "Account Card Details Input"){
                AccountDetailsInput(
                    accountNameState = accountNameState,
                    accountName = accountName,
                    themeColors = themeColors,
                    // Add onboarding sync for account name
                    onAccountNameChange = { name ->
                        accountNameState.value = name
                        onAccountEvent(AccountScreenEvent.UpdateAccountName(name))
                        onOnBoardingEvent?.invoke(OnBoardingEvent.UpdateAccountName(name))
                    },
                    onAccountEvent = onAccountEvent
                )
            }

            item(key = "Balance Input") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AccountBalanceInput(
                        balanceState = balanceState,
                        balanceSheetState = balanceSheetState,
                        currencyCode = currencyCodeState,
                        specialKeys = specialKeys,
                        accountBalanceSheetOpen = accountBalanceSheetOpen,
                        usedScreenWidth = usedScreenWidth,
                        themeColors = themeColors,
                        modifier = Modifier.weight(1f),
                        // Add onboarding sync for balance
                        onBalanceChange = { balance ->
                            balanceState.value = balance
                            onAccountEvent(AccountScreenEvent.UpdateBalance(balance))
                            onOnBoardingEvent?.invoke(OnBoardingEvent.UpdateAccountBalance(balance))
                        },
                        onAccountEvent = onAccountEvent,
                        onAddTransactionEvent = onAddTransactionEvent,
                        transactionUiState = transactionUiState
                    )
                    AccountCurrencyInput(
                        accountId = currentAccountEntity?.id ?: 0,
                        currencyCode = currencyCodeState,
                        showCurrencySheet = showCurrencySheet,
                        currencySheetState = currencySheetState,
                        onDismiss = { showCurrencySheet.value = false },
                        usedScreenHeight = usedScreenHeight,
                        usedScreenWidth = usedScreenWidth,
                        themeColors = themeColors,
                        modifier = Modifier.weight(1f),
                        // Add onboarding sync for currency
                        onCurrencyChange = { currency ->
                            currencyCodeState.value = currency
                            onOnBoardingEvent?.invoke(
                                OnBoardingEvent.UpdateSelectedCurrency(
                                    currency
                                )
                            )
                        },
                        onAccountEvent = onAccountEvent,
                        updateAccountCurrency = updateAccountCurrency,
                        currencyUiState = currencyUiState,
                        selectCurrency = selectCurrency
                    )
                }
            }

            item(key = "Account Card Color Selection"){
                AccountCardColorSelection(
                    cardColor1State = cardColor1State,
                    cardColor2State = cardColor2State,
                    firstColorsList = firstColorsList,
                    secondColorsList = secondColorsList,
                    cardFirstColorIndex = cardFirstColorIndex,
                    cardSecondColorIndex = cardSecondColorIndex,
                    lazyListState = lazyListState,
                    infiniteTransition = infiniteTransition,
                    showLeftSpacer = showLeftSpacer,
                    showRightSpacer = showRightSpacer,
                    themeColors = themeColors,
                    // Add onboarding sync for colors
                    onColor1Change = { color ->
                        cardColor1State.value = color
                        onAccountEvent(AccountScreenEvent.UpdateCardColor1(color))
                        onOnBoardingEvent?.invoke(OnBoardingEvent.UpdateAccountColor1(color))
                    },
                    onColor2Change = { color ->
                        cardColor2State.value = color
                        onAccountEvent(AccountScreenEvent.UpdateCardColor2(color))
                        onOnBoardingEvent?.invoke(OnBoardingEvent.UpdateAccountColor2(color))
                    },
                    onAccountEvent = onAccountEvent
                )
            }

            // Only show merge button if this is an update (shouldn't happen in onboarding)
            if (isUpdate && currentAccountEntity != null) {
                item(key = "Merge Account Button") {
                    Button(
                        onClick = {
                            onAccountEvent(
                                AccountScreenEvent.ShowMergeAccountDialog(currentAccountEntity)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding( vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            contentColor = themeColors.inverseSurface,
                            containerColor = themeColors.surface
                        ),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(vertical = 12.dp, horizontal = 21.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "Merge Account",
                                fontSize = 16.sp,
                                fontFamily = iosFont,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Merge this account and move all transactions of this account with another account",
                                fontSize = 12.sp,
                                fontFamily = iosFont,
                                color = themeColors.inverseSurface.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            item{
                Button(
                    onClick = {
                        // First sync all state to onboarding
                        onOnBoardingEvent?.invoke(OnBoardingEvent.UpdateAccountName(accountNameState.value))
                        onOnBoardingEvent?.invoke(OnBoardingEvent.UpdateAccountBalance(balanceState.value))
                        onOnBoardingEvent?.invoke(OnBoardingEvent.UpdateSelectedCurrency(currencyCodeState.value))
                        onOnBoardingEvent?.invoke(OnBoardingEvent.UpdateAccountColor1(cardColor1State.value))
                        onOnBoardingEvent?.invoke(OnBoardingEvent.UpdateAccountColor2(cardColor2State.value))

                        // Save account using AccountViewModel only
                        onAccountEvent(AccountScreenEvent.OnAccountSaved(
                            onSuccess = {
                                onNext()
                            }
                        ))
                    },
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(
                            elevation = 5.dp,
                            shape = RoundedCornerShape(12.dp),
                            spotColor = if (hasMinimumData) {
                                themeColors.primary
                            } else {
                                themeColors.surface
                            },
                            ambientColor = Color.Transparent
                        )
                        .align(Alignment.BottomCenter),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasMinimumData) {
                            themeColors.primary
                        } else {
                            themeColors.surface
                        },
                        contentColor = if (hasMinimumData) {
                            Color.White
                        } else {
                            themeColors.inverseSurface.copy(0.5f)
                        }
                    ),
                    enabled = hasMinimumData
                ) {
                    Text(
                        text = "Complete Setup",
                        fontFamily = iosFont,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}