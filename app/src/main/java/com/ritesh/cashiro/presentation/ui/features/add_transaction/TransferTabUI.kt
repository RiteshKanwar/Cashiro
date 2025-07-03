package com.ritesh.cashiro.presentation.ui.features.add_transaction

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults.DragHandle
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ritesh.cashiro.R
import com.ritesh.cashiro.data.local.entity.AccountEntity
import com.ritesh.cashiro.domain.repository.AccountTransactionStats
import com.ritesh.cashiro.domain.repository.TransactionStats
import com.ritesh.cashiro.presentation.ui.features.currency.CurrencyViewModel
import com.ritesh.cashiro.presentation.effects.VerticalStretchedOverscroll
import com.ritesh.cashiro.presentation.ui.extras.components.account.AccountCardLayout
import com.ritesh.cashiro.presentation.ui.extras.components.account.AccountDisplayList
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenEvent
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenState
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenViewModel
import com.ritesh.cashiro.presentation.ui.features.categories.CategoryScreenEvent
import com.ritesh.cashiro.presentation.ui.features.categories.CategoryScreenState
import com.ritesh.cashiro.presentation.ui.features.categories.SubCategoryEvent
import com.ritesh.cashiro.presentation.ui.features.categories.SubCategoryState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferTabUI(
    transactionAccountId: Int,
    openDatePickerDialog: MutableState<Boolean>,
    datePickerState: DatePickerState,
    transactionTitle: String,
    transactionNote: String,
    usedScreenWidth: Dp,
    usedScreenHeight: Dp,
    coroutineScope: CoroutineScope,
    amountSheetState: SheetState,
    specialKeys: Set<Char>,
    isTransactionAmountSheetOpen: Boolean,
    themeColors: ColorScheme,
    transactionUiState: AddTransactionScreenState,
    onAddTransactionEvent: (AddTransactionEvent) -> Unit,
    accountUiState: AccountScreenState,
    onAccountEvent: (AccountScreenEvent) -> Unit,
    getTransactionStatsForAccount: (Int, (AccountTransactionStats?) -> Unit) -> Unit,
    updateAccountCurrency: (Int, String) -> Unit,
    setAccountAsMain: (AccountEntity) -> Unit,
    currencyUiState: CurrencyViewModel.CurrencyUiState,
    selectCurrency: (String) -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize().imePadding()
    ) {
        TransferAmountLayout(
            openDatePickerDialog = openDatePickerDialog,
            datePickerState = datePickerState,
            transactionTitle = transactionTitle,
            transactionNote = transactionNote,
            usedScreenWidth = usedScreenWidth,
            usedScreenHeight = usedScreenHeight,
            coroutineScope = coroutineScope,
            amountSheetState = amountSheetState,
            specialKeys = specialKeys,
            transactionAccountId = transactionAccountId,
            isTransactionAmountSheetOpen = isTransactionAmountSheetOpen,
            themeColors = themeColors,
            transactionUiState = transactionUiState,
            accountUiState = accountUiState,
            onAccountEvent = onAccountEvent,
            getTransactionStatsForAccount = getTransactionStatsForAccount,
            updateAccountCurrency = updateAccountCurrency,
            setAccountAsMain = setAccountAsMain,
            currencyUiState = currencyUiState,
            selectCurrency = selectCurrency,
            onAddTransactionEvent = onAddTransactionEvent,
        )
        Spacer(
            modifier = Modifier
                .height(35.dp)
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            themeColors.background.copy(0.5f)
                        )
                    )
                )
                .align(Alignment.BottomCenter)
        )
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun TransferAmountLayout(
    transactionAccountId: Int,
    openDatePickerDialog: MutableState<Boolean>,
    datePickerState: DatePickerState,
    transactionTitle: String,
    transactionNote: String,
    usedScreenWidth: Dp,
    usedScreenHeight: Dp,
    coroutineScope: CoroutineScope,
    amountSheetState: SheetState,
    specialKeys: Set<Char>,
    isTransactionAmountSheetOpen: Boolean,
    themeColors: ColorScheme,
    accountUiState: AccountScreenState,
    onAccountEvent: (AccountScreenEvent) -> Unit,
    getTransactionStatsForAccount: (id: Int, callback: (AccountTransactionStats?) -> Unit) -> Unit,
    updateAccountCurrency: (Int, String) -> Unit,
    setAccountAsMain: (AccountEntity) -> Unit,
    currencyUiState: CurrencyViewModel.CurrencyUiState,
    selectCurrency: (String) -> Unit,
    transactionUiState: AddTransactionScreenState,
    onAddTransactionEvent: (AddTransactionEvent) -> Unit,
) {
    // State to store the selected accounts
    val sourceAccountId = transactionUiState.transactionAccountId
    val destinationAccountId = transactionUiState.transactionDestinationAccountId

    // Get all accounts
    val accounts = accountUiState.accounts

    // Find the source and destination accounts based on IDs
    val sourceAccount = accounts.find { it.id == sourceAccountId } ?: accounts.find { it.isMainAccount } ?: accounts.firstOrNull()
    val destinationAccount = accounts.find { it.id == destinationAccountId }

    val sourceSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val destinationSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var sourceAccountMenuOpened = rememberSaveable { mutableStateOf(false) }
    var destinationAccountMenuOpened = rememberSaveable { mutableStateOf(false) }
    val scrollOverScrollState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val overscrollEffect = remember(scope) { VerticalStretchedOverscroll(coroutineScope) }

    // Set default source account if not selected yet
    LaunchedEffect(sourceAccount) {
        if (sourceAccountId == 0 && sourceAccount != null) {
            onAddTransactionEvent(AddTransactionEvent.UpdateAccountId(sourceAccount.id))
        }
    }

    LazyColumn(
        state = scrollOverScrollState,
        userScrollEnabled = false,
        modifier = Modifier
            .fillMaxWidth()
            .overscroll(overscrollEffect)
            .scrollable(
                orientation = Orientation.Vertical,
                reverseDirection = true,
                state = scrollOverScrollState,
                overscrollEffect = overscrollEffect
            ),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item(key = "Date and time input") {
            DateAndTimeInput(
                openDatePickerDialog = openDatePickerDialog,
                datePickerState = datePickerState,
                transactionUiState = transactionUiState,
                onAddTransactionEvent = onAddTransactionEvent
            )
        }
        item(key = "source Account input") {
            sourceAccount?.let { account ->
                // Use the existing AccountCardLayout but make it clickable to change the source
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 5.dp)
                        .clickable {
                            sourceAccountMenuOpened.value = true
                        }
                ) {
                    AccountCardLayout(
                        item = account,
                        themeColors = themeColors,
                        accountNameFontSize = 22.dp,
                        cardHeight = 140.dp,
                        notAllowSetAccountAsMain = true,
                        accountUiState = accountUiState,
                        setAccountAsMain = setAccountAsMain,
                    )
                }
            } ?: run {
                // Placeholder when no source account is selected
                EmptyAccountCard(
                    onSelectAccount = {
                        sourceAccountMenuOpened.value = true
                    },
                    themeColors = themeColors
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (sourceAccountMenuOpened.value) {
                ModalBottomSheet(
                    sheetState = sourceSheetState,
                    onDismissRequest = {
                        sourceAccountMenuOpened.value = false
                    },
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(usedScreenHeight)
                    ) {
                        AccountDisplayList(
                            items = accounts,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                coroutineScope
                                    .launch { sourceSheetState.hide() }
                                    .invokeOnCompletion {
                                        if (!sourceSheetState.isVisible) {
                                            sourceAccountMenuOpened.value = false
                                        }
                                    }
                            },
                            accountUiState = accountUiState,
                            onAccountEvent = onAccountEvent,
                            getTransactionStatsForAccount = getTransactionStatsForAccount,
                            updateAccountCurrency = updateAccountCurrency,
                            setAccountAsMain = setAccountAsMain,
                            currencyUiState = currencyUiState,
                            selectCurrency = selectCurrency,
                            transactionUiState = transactionUiState,
                            onAddTransactionEvent = onAddTransactionEvent,
                        )
                    }
                }
            }
        }

        item(key = "Swap and Amount Input") {
            // Transfer arrow indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.transfer_bulk),
                    contentDescription = "Transfer",
                    tint = themeColors.primary,
                    modifier = Modifier.size(26.dp)
                )
                AmountInput(
                    transactionAccountId = transactionAccountId,
                    amountSheetState = amountSheetState,
                    specialKeys = specialKeys,
                    isTransactionAmountSheetOpen = isTransactionAmountSheetOpen,
                    usedScreenWidth = usedScreenWidth,
                    amountFontSize = 28.dp,
                    contentAlignment = Alignment.CenterStart,
                    transactionUiState = transactionUiState,
                    accountUiState = accountUiState,
                    onAddTransactionEvent = onAddTransactionEvent,
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        item(key = "destination Account input") {
            destinationAccount?.let { account ->
                // Use the existing AccountCardLayout but make it clickable to change the destination
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 5.dp)
                        .clickable {
                            destinationAccountMenuOpened.value = true
                        }
                ) {
                    AccountCardLayout(
                        item = account,
                        themeColors = themeColors,
                        accountNameFontSize = 22.dp,
                        cardHeight = 140.dp,
                        notAllowSetAccountAsMain = true,
                        accountUiState = accountUiState,
                        setAccountAsMain = setAccountAsMain,
                    )
                }
            } ?: run {
                // Empty card for destination account selection
                EmptyAccountCard(
                    onSelectAccount = {
                        destinationAccountMenuOpened.value = true
                    },
                    themeColors = themeColors
                )
            }

            // Amount input section
            Spacer(modifier = Modifier.height(10.dp))

            if (destinationAccountMenuOpened.value) {
                ModalBottomSheet(
                    sheetState = destinationSheetState,
                    onDismissRequest = {
                        destinationAccountMenuOpened.value = false
                    },
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
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(usedScreenHeight)
                    ) {
                        AccountDisplayList(
                            items = accounts,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                coroutineScope
                                    .launch { destinationSheetState.hide() }
                                    .invokeOnCompletion {
                                        if (!destinationSheetState.isVisible) {
                                            destinationAccountMenuOpened.value = false
                                        }
                                    }
                            },
                            requireSourceAccountID = false,
                            requireDestinationAccountID = true,
                            accountUiState = accountUiState,
                            onAccountEvent = onAccountEvent,
                            getTransactionStatsForAccount = getTransactionStatsForAccount,
                            updateAccountCurrency = updateAccountCurrency,
                            setAccountAsMain = setAccountAsMain,
                            currencyUiState = currencyUiState,
                            selectCurrency = selectCurrency,
                            transactionUiState = transactionUiState,
                            onAddTransactionEvent = onAddTransactionEvent
                        )
                    }
                }
            }
        }
        item(key = "titleInput") {
            TitleInput(
                transactionTitle = transactionTitle,
                themeColors = themeColors,
                showHeader = true,
                modifier = Modifier.padding(top = 20.dp),
                onAddTransactionEvent = onAddTransactionEvent,
            )
        }
        item(key = "noteInput") {
            NotesInput(
                transactionNote = transactionNote,
                themeColors = themeColors,
                onAddTransactionEvent = onAddTransactionEvent,
            )
        }
    }
}
@Composable
private fun EmptyAccountCard(
    onSelectAccount: () -> Unit,
    themeColors: ColorScheme,
    cardHeight: Dp = 140.dp
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 5.dp)
            .height(cardHeight)
            .clip(RoundedCornerShape(16.dp))
            .background(themeColors.surface)
            .clickable(onClick = onSelectAccount),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.add_item_bulk),
                contentDescription = "Select Account",
                tint = themeColors.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Select Account",
                color = themeColors.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}