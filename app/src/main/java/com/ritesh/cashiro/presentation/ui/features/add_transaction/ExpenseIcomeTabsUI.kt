package com.ritesh.cashiro.presentation.ui.features.add_transaction

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults.DragHandle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ritesh.cashiro.R
import com.ritesh.cashiro.data.local.entity.AccountEntity
import com.ritesh.cashiro.data.local.entity.CategoryEntity
import com.ritesh.cashiro.data.local.entity.RecurrenceFrequency
import com.ritesh.cashiro.data.local.entity.SubCategoryEntity
import com.ritesh.cashiro.data.local.entity.TransactionEntity
import com.ritesh.cashiro.data.local.entity.TransactionType
import com.ritesh.cashiro.data.currency.model.CurrencySymbols
import com.ritesh.cashiro.domain.repository.AccountTransactionStats
import com.ritesh.cashiro.domain.repository.TransactionStats
import com.ritesh.cashiro.domain.utils.AccountEvent
import com.ritesh.cashiro.presentation.ui.features.currency.CurrencyViewModel
import com.ritesh.cashiro.domain.utils.calculateRecurrenceCount
import com.ritesh.cashiro.domain.utils.getCurrentDateInMillis
import com.ritesh.cashiro.domain.utils.getDateLabel
import com.ritesh.cashiro.domain.utils.getEndDateLabel
import com.ritesh.cashiro.domain.utils.getLocalDateFromMillis
import com.ritesh.cashiro.domain.utils.getNextDueDate
import com.ritesh.cashiro.domain.utils.icons
import com.ritesh.cashiro.presentation.effects.AnimatedCounterText
import com.ritesh.cashiro.presentation.effects.VerticalStretchedOverscroll
import com.ritesh.cashiro.presentation.ui.extras.components.account.AccountDisplayList
import com.ritesh.cashiro.presentation.ui.extras.components.category.CategoryDisplayList
import com.ritesh.cashiro.presentation.ui.extras.components.settings.OptionsComponent
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenViewModel
import com.ritesh.cashiro.presentation.ui.features.categories.CategoryScreenViewModel
import com.ritesh.cashiro.presentation.ui.extras.components.category.SubCategoriesDisplayList
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenEvent
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenState
import com.ritesh.cashiro.presentation.ui.features.categories.CategoryScreenEvent
import com.ritesh.cashiro.presentation.ui.features.categories.CategoryScreenState
import com.ritesh.cashiro.presentation.ui.features.categories.SubCategoryEvent
import com.ritesh.cashiro.presentation.ui.features.categories.SubCategoryState
import com.ritesh.cashiro.presentation.ui.features.categories.SubCategoryViewModel
import com.ritesh.cashiro.presentation.ui.theme.Latte_Blue
import com.ritesh.cashiro.presentation.ui.theme.Latte_Flamingo
import com.ritesh.cashiro.presentation.ui.theme.Latte_Lavender
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Blue
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Green
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Red
import com.ritesh.cashiro.presentation.ui.theme.iosFont
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.mariuszgromada.math.mxparser.Expression
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlin.String
import kotlin.toString

@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class,
    ExperimentalLayoutApi::class
)
@Composable
fun ExpenseIncomeTabsUI(
    accounts: List<AccountEntity>,
    categories: List<CategoryEntity>,
    subCategories: List<SubCategoryEntity>,
    transaction: TransactionEntity?,
    transactionAccountId: Int,
    transactionCategoryId: Int,
    transactionSubCategoryId: Int,
    transactionTitle: String,
    transactionNote: String,
    transactionType: TransactionType,
    specialKeys: Set<Char>,
    usedScreenWidth: Dp,
    usedScreenHeight: Dp,
    amountSheetState: SheetState,
    isTransactionAmountSheetOpen: Boolean,
    isUpdateTransaction: Boolean = false,
    isEndDateSelected: Boolean,
    isRecurrenceBottomSheetOpen: Boolean,
    isCustomInputBottomSheetOpen: Boolean,
    isTransactionTypeInfoSheetOpen: Boolean,
    transactionTypeMenuOpened: Boolean,
    openDatePickerDialog: MutableState<Boolean>,
    datePickerState: DatePickerState,
    recurrenceMenuOpened: Boolean,
    transactionTypeListItemsColor: List<Color>,
    transactionTypeSheetState: SheetState,
    intervalSheetState: SheetState,
    frequencySheetState: SheetState,
    openEndDatePicker: Boolean,
    endDatePickerState: DatePickerState,
    coroutineScope: CoroutineScope,
    tabScrollState: LazyListState,
    tabOverscrollEffect: VerticalStretchedOverscroll,
    transactionUiState: AddTransactionScreenState,
    onAddTransactionEvent: (AddTransactionEvent) -> Unit,
    accountUiState: AccountScreenState,
    onAccountEvent: (AccountScreenEvent) -> Unit,
    categoryUiState: CategoryScreenState,
    onCategoryEvent: (CategoryScreenEvent) -> Unit,
    subCategoryUiState: SubCategoryState,
    onSubCategoryEvent: (SubCategoryEvent) -> Unit,
    getTransactionStatsForAccount: (Int, (AccountTransactionStats?) -> Unit) -> Unit,
    getTransactionStatsForCategory: (Int, (TransactionStats) -> Unit) -> Unit,
    getTransactionStatsForSubCategory: (Int, (TransactionStats) -> Unit) -> Unit,
    updateAccountCurrency: (Int, String) -> Unit,
    setAccountAsMain: (AccountEntity) -> Unit,
    setSelectedCategoryId: (Int?) -> Unit,
    clearSelection: () -> Unit,
    currencyUiState: CurrencyViewModel.CurrencyUiState,
    selectCurrency: (String) -> Unit,
    themeColors: ColorScheme,
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            state = tabScrollState,
            userScrollEnabled = false,
            modifier = Modifier.imePadding()
                .fillMaxWidth()
                .overscroll(tabOverscrollEffect)
                .scrollable(
                    orientation = Orientation.Vertical,
                    reverseDirection = true,
                    state = tabScrollState,
                    overscrollEffect = tabOverscrollEffect
                ),
        ) {
            // Currency Input collects amount
            item(key = "Amount Input") {
                AmountInput(
                    amountSheetState = amountSheetState,
                    transactionAccountId = transactionAccountId,
                    specialKeys = specialKeys,
                    isTransactionAmountSheetOpen = isTransactionAmountSheetOpen,
                    modifier = Modifier.fillMaxWidth(),
                    isTransactionTypeInfoSheetOpen = isTransactionTypeInfoSheetOpen,
                    transactionTypeSheetState = transactionTypeSheetState,
                    currentTransactionMode = transactionUiState.transactionMode,
                    transactionTypeListItemsColor = transactionTypeListItemsColor,
                    showTransactionTypeSelection = true,
                    usedScreenWidth = usedScreenWidth,
                    usedScreenHeight = usedScreenHeight,
                    themeColors = themeColors,
                    recurrenceMenuOpened = recurrenceMenuOpened,
                    endDatePickerState = endDatePickerState,
                    isRecurrenceBottomSheetOpen = isRecurrenceBottomSheetOpen,
                    isCustomInputBottomSheetOpen = isCustomInputBottomSheetOpen,
                    isEndDateSelected = isEndDateSelected,
                    openEndDatePicker = openEndDatePicker,
                    frequencySheetState = frequencySheetState,
                    intervalSheetState = intervalSheetState,
                    transactionUiState = transactionUiState,
                    onAddTransactionEvent = onAddTransactionEvent,
                    accountUiState = accountUiState,
                )
            }
            // Date And Time Pickers
            item(key = "Date and Time Input") {
                DateAndTimeInput(
                    openDatePickerDialog = openDatePickerDialog,
                    datePickerState = datePickerState,
                    isUpdateTransaction = isUpdateTransaction,
                    transaction = transaction,
                    transactionUiState = transactionUiState,
                    onAddTransactionEvent = onAddTransactionEvent
                )
            }

            item(key = "Transaction Details") {
                TransactionsDetailsInput(
                    usedScreenWidth = usedScreenWidth,
                    usedScreenHeight = usedScreenHeight,
                    accounts = accounts,
                    categories = categories,
                    subCategories = subCategories,
                    transactionAccountId = transactionAccountId,
                    transactionCategoryId = transactionCategoryId,
                    transactionSubCategoryId = transactionSubCategoryId,
                    coroutineScope = coroutineScope,
                    transactionType = transactionType,
                    transactionTypeMenuOpened = transactionTypeMenuOpened,
                    isTransactionTypeInfoSheetOpen = isTransactionTypeInfoSheetOpen,
                    isUpdateTransaction = isUpdateTransaction,
                    transactionTypeListItemsColor = transactionTypeListItemsColor,
                    transactionTypeSheetState = transactionTypeSheetState,
                    recurrenceMenuOpened = recurrenceMenuOpened,
                    isRecurrenceBottomSheetOpen = isRecurrenceBottomSheetOpen,
                    isCustomInputBottomSheetOpen = isCustomInputBottomSheetOpen,
                    isEndDateSelected = isEndDateSelected,
                    openEndDatePicker = openEndDatePicker,
                    endDatePickerState = endDatePickerState,
                    intervalSheetState = intervalSheetState,
                    frequencySheetState = frequencySheetState,
                    themeColors = themeColors,
                    // ADD THIS LINE: Pass the current transaction mode
                    currentTransactionMode = transactionUiState.transactionMode,
                    transactionUiState = transactionUiState,
                    onAddTransactionEvent = onAddTransactionEvent,
                    getTransactionStatsForAccount = getTransactionStatsForAccount,
                    accountUiState = accountUiState,
                    onAccountEvent = onAccountEvent,
                    updateAccountCurrency = updateAccountCurrency,
                    setAccountAsMain = setAccountAsMain,
                    currencyUiState = currencyUiState,
                    selectCurrency = selectCurrency,
                    getTransactionStatsForCategory = getTransactionStatsForCategory,
                    getTransactionStatsForSubCategory = getTransactionStatsForSubCategory,
                    categoryUiState = categoryUiState,
                    onCategoryEvent = onCategoryEvent,
                    setSelectedCategoryId = setSelectedCategoryId,
                    clearSelection = clearSelection,
                    subCategoryUiState = subCategoryUiState,
                    onSubCategoryEvent = onSubCategoryEvent
                )
            }

            item(key = " Title Input") {
                TitleInput(
                    transactionTitle = transactionTitle,
                    themeColors = themeColors,
                    onAddTransactionEvent = onAddTransactionEvent,
                )
            }
            item(key = "Notes Input") {
                NotesInput(
                    transactionNote = transactionNote,
                    themeColors = themeColors,
                    onAddTransactionEvent = onAddTransactionEvent,
                )
            }
        }
        Spacer(
            modifier = Modifier
                .height(35.dp)
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            themeColors.background.copy(0.9f)
                        )
                    )
                )
                .align(Alignment.BottomCenter)
        )
    }
}

// Update some of the input components to use the state and events

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmountInput(
    accountUiState: AccountScreenState,
    transactionUiState: AddTransactionScreenState,
    onAddTransactionEvent: (AddTransactionEvent) -> Unit,
    transactionAccountId: Int,
    amountSheetState: SheetState,
    specialKeys: Set<Char>,
    isTransactionAmountSheetOpen: Boolean,
    amountFontSize: Dp = 50.dp,
    contentAlignment: Alignment = Alignment.Center,
    modifier: Modifier = Modifier,
    isTransactionTypeInfoSheetOpen: Boolean = false,
    transactionTypeSheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    currentTransactionMode: String  = "",
    transactionTypeListItemsColor: List<Color> = listOf(
        Latte_Blue,
        Latte_Flamingo,
        Latte_Lavender,
        Macchiato_Green,
        Macchiato_Blue,
        Macchiato_Red
    ),
    usedScreenWidth: Dp = 0.dp,
    usedScreenHeight: Dp = 0.dp,
    themeColors: ColorScheme = MaterialTheme.colorScheme,
    recurrenceMenuOpened: Boolean = false,
    endDatePickerState: DatePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis()),
    isRecurrenceBottomSheetOpen: Boolean = false,
    isCustomInputBottomSheetOpen: Boolean = false,
    isEndDateSelected: Boolean = false,
    openEndDatePicker: Boolean = false,
    frequencySheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    intervalSheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    showTransactionTypeSelection: Boolean = false,
) {
    val amount = transactionUiState.transactionAmount
    Log.d("AmountInput", "amount: $amount")
    val scope = rememberCoroutineScope()

    val accountCurrencyCode = accountUiState.accounts.find { it.id == transactionAccountId }?.currencyCode
    val currencyCode = if (accountCurrencyCode.isNullOrBlank()) {
        accountUiState.accounts.find { it.isMainAccount == true }?.currencyCode
    } else {
        accountCurrencyCode
    }

    var calculatedResultAmount by rememberSaveable { mutableStateOf("0") }

    // Amount input container
    Box(
        contentAlignment = contentAlignment,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(
                onClick = {
                    onAddTransactionEvent(AddTransactionEvent.SetTransactionAmountSheetOpen(true))
                },
            )
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(10.dp)
            )
            .padding(10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = CurrencySymbols.getSymbol(currencyCode.toString()),
                fontSize = amountFontSize.value.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontFamily = iosFont,
                color = if (amount.isEmpty()) MaterialTheme.colorScheme.inverseSurface.copy(
                    0.5f
                ) else MaterialTheme.colorScheme.inverseSurface
            )
            AnimatedCounterText(
                amount = amount,
                fontSize = amountFontSize.value.sp,
                specialKeys = specialKeys,
                fontFamily = iosFont
            )
        }
    }

    // BottomSheet for custom Amount input
    if (isTransactionAmountSheetOpen) {
        CustomAmountInputBottomSheet(
            amount = amount,
            amountSheetState = amountSheetState,
            specialKeys = specialKeys,
            currencyCode = currencyCode,
            onDismissRequest = {
                onAddTransactionEvent(AddTransactionEvent.SetTransactionAmountSheetOpen(false))
            },
            onClick = {
                onAddTransactionEvent(AddTransactionEvent.UpdateAmount(calculatedResultAmount))
                scope
                    .launch { amountSheetState.hide() }
                    .invokeOnCompletion {
                        if (!amountSheetState.isVisible) {
                            onAddTransactionEvent(AddTransactionEvent.SetTransactionAmountSheetOpen(false))
                        }
                    }
            },
            onValueChange = { newValue ->
                onAddTransactionEvent(AddTransactionEvent.UpdateAmount(newValue))
            },
            onResultAmount = { result ->
                calculatedResultAmount = result
                Log.d("AmountInput", "Calculated Result: $result")
                onAddTransactionEvent(AddTransactionEvent.UpdateCalculatedResult(calculatedResultAmount))
            },
            isTransactionTypeInfoSheetOpen = isTransactionTypeInfoSheetOpen,
            transactionTypeSheetState = transactionTypeSheetState,
            currentTransactionMode = currentTransactionMode,
            transactionTypeListItemsColor = transactionTypeListItemsColor,
            usedScreenWidth = usedScreenWidth,
            usedScreenHeight = usedScreenHeight,
            themeColors = themeColors,
            recurrenceMenuOpened = recurrenceMenuOpened,
            endDatePickerState = endDatePickerState,
            isRecurrenceBottomSheetOpen = isRecurrenceBottomSheetOpen,
            isCustomInputBottomSheetOpen = isCustomInputBottomSheetOpen,
            isEndDateSelected = isEndDateSelected,
            openEndDatePicker = openEndDatePicker,
            frequencySheetState = frequencySheetState,
            intervalSheetState = intervalSheetState,
            transactionUiState = transactionUiState,
            onAddTransactionEvent = onAddTransactionEvent,
            showTransactionTypeSelection = showTransactionTypeSelection
        )
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateAndTimeInput(
    openDatePickerDialog: MutableState<Boolean>,
    datePickerState: DatePickerState,
    isUpdateTransaction: Boolean = false,
    transaction: TransactionEntity? = null,
    transactionUiState: AddTransactionScreenState,
    onAddTransactionEvent: (AddTransactionEvent) -> Unit,
) {
    // Use the state directly
    val transactionDate = transactionUiState.transactionDate
    val transactionTime = transactionUiState.transactionTime

    // State for time picker
    val showTimePicker = remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour = LocalTime.ofInstant(Instant.ofEpochMilli(transactionTime), ZoneId.systemDefault()).hour,
        initialMinute = LocalTime.ofInstant(Instant.ofEpochMilli(transactionTime), ZoneId.systemDefault()).minute,
        is24Hour = false
    )

    LaunchedEffect(Unit) {
        if (isUpdateTransaction && transaction != null) {
            onAddTransactionEvent(AddTransactionEvent.UpdateDate(transaction.date))
            onAddTransactionEvent(AddTransactionEvent.UpdateTime(transaction.time))
        } else {
            // Ensure the date is set to current date on first load
            val currentDate = getCurrentDateInMillis()
            if (transactionDate != currentDate) {
                onAddTransactionEvent(AddTransactionEvent.UpdateDate(currentDate))
            }
        }
    }

    // Track date picker changes
    LaunchedEffect(datePickerState.selectedDateMillis) {
        if (datePickerState.selectedDateMillis != null && openDatePickerDialog.value) {
            onAddTransactionEvent(AddTransactionEvent.UpdateDate(datePickerState.selectedDateMillis!!))
        }
    }

    // Track time picker changes
    LaunchedEffect(timePickerState.hour, timePickerState.minute) {
        if (showTimePicker.value) {
            val selectedTime = LocalDateTime.of(
                LocalDate.now(),
                LocalTime.of(timePickerState.hour, timePickerState.minute)
            ).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            onAddTransactionEvent(AddTransactionEvent.UpdateTime(selectedTime))
        }
    }

    // Built-in Date Picker Dialog
    if (openDatePickerDialog.value) {
        DatePickerDialog(
            onDismissRequest = { openDatePickerDialog.value = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDate ->
                            onAddTransactionEvent(AddTransactionEvent.UpdateDate(selectedDate))
                        }
                        openDatePickerDialog.value = false
                    },
                    modifier = Modifier
                        .shadow(5.dp, RoundedCornerShape(15.dp), spotColor = MaterialTheme.colorScheme.primary, ambientColor = Color.Transparent),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(15.dp)
                ) {
                    Text(
                        text ="Okay",
                        modifier = Modifier.padding(horizontal = 10.dp),
                        style = TextStyle(
                            fontFamily = iosFont,
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { openDatePickerDialog.value = false },
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
            },
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.background
            ),

            ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                    headlineContentColor = MaterialTheme.colorScheme.inverseSurface,
                    weekdayContentColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                    subheadContentColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                    navigationContentColor = MaterialTheme.colorScheme.inverseSurface,
                    yearContentColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                    currentYearContentColor = MaterialTheme.colorScheme.inverseSurface,
                    selectedYearContentColor = Color.White,
                    selectedYearContainerColor = MaterialTheme.colorScheme.primary,
                    dayContentColor = MaterialTheme.colorScheme.inverseSurface,
                    disabledDayContentColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                    selectedDayContentColor = Color.White,
                    disabledSelectedDayContentColor = MaterialTheme.colorScheme.inverseSurface,
                    selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                    disabledSelectedDayContainerColor = MaterialTheme.colorScheme.onBackground,
                    todayContentColor = MaterialTheme.colorScheme.primary.copy(0.6f),
                    todayDateBorderColor = MaterialTheme.colorScheme.primary,
                    dayInSelectionRangeContentColor = MaterialTheme.colorScheme.primary,
                    dayInSelectionRangeContainerColor = MaterialTheme.colorScheme.primary.copy(0.5f),
                    dividerColor = MaterialTheme.colorScheme.primary,
                    dateTextFieldColors = TextFieldColors(
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = MaterialTheme.colorScheme.inverseSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.inverseSurface,
                        disabledTextColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                        errorTextColor = MaterialTheme.colorScheme.error.copy(0.5f),
                        focusedContainerColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedContainerColor = MaterialTheme.colorScheme.onBackground,
                        disabledContainerColor = MaterialTheme.colorScheme.onBackground.copy(0.5f),
                        errorContainerColor = MaterialTheme.colorScheme.onErrorContainer,
                        errorCursorColor = MaterialTheme.colorScheme.error,
                        textSelectionColors = TextSelectionColors(
                            handleColor = MaterialTheme.colorScheme.primary,
                            backgroundColor = MaterialTheme.colorScheme.primary.copy(0.5f)
                        ),
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.primary.copy(0.3f),
                        disabledIndicatorColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                        errorIndicatorColor = MaterialTheme.colorScheme.error.copy(0.5f),
                        focusedLeadingIconColor = MaterialTheme.colorScheme.inverseSurface,
                        unfocusedLeadingIconColor = MaterialTheme.colorScheme.inverseSurface,
                        disabledLeadingIconColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                        errorLeadingIconColor = MaterialTheme.colorScheme.error.copy(0.5f),
                        focusedTrailingIconColor = MaterialTheme.colorScheme.inverseSurface,
                        unfocusedTrailingIconColor = MaterialTheme.colorScheme.inverseSurface,
                        disabledTrailingIconColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                        errorTrailingIconColor =MaterialTheme.colorScheme.error,
                        focusedLabelColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                        unfocusedLabelColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                        disabledLabelColor = MaterialTheme.colorScheme.inverseSurface.copy(0.3f),
                        errorLabelColor = MaterialTheme.colorScheme.error.copy(0.5f),
                        focusedPlaceholderColor =  MaterialTheme.colorScheme.onBackground,
                        unfocusedPlaceholderColor =  MaterialTheme.colorScheme.onBackground,
                        disabledPlaceholderColor =  MaterialTheme.colorScheme.onBackground.copy(0.5f),
                        errorPlaceholderColor =  MaterialTheme.colorScheme.onBackground,
                        focusedSupportingTextColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                        unfocusedSupportingTextColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                        disabledSupportingTextColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                        errorSupportingTextColor = MaterialTheme.colorScheme.error.copy(0.5f),
                        focusedPrefixColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                        unfocusedPrefixColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                        disabledPrefixColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                        errorPrefixColor = MaterialTheme.colorScheme.error.copy(0.5f),
                        focusedSuffixColor =MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                        unfocusedSuffixColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                        disabledSuffixColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                        errorSuffixColor = MaterialTheme.colorScheme.error.copy(0.5f)
                    )
                )
            )
        }
    }

    // Built-in Time Picker Dialog
    if (showTimePicker.value) {
        TimePickerDialog(
            onDismissRequest = { showTimePicker.value = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedTime = LocalDateTime.of(
                            LocalDate.now(),
                            LocalTime.of(timePickerState.hour, timePickerState.minute)
                        ).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                        onAddTransactionEvent(AddTransactionEvent.UpdateTime(selectedTime))
                        showTimePicker.value = false
                    },
                    modifier = Modifier
                        .shadow(5.dp, RoundedCornerShape(15.dp), spotColor = MaterialTheme.colorScheme.primary, ambientColor = Color.Transparent),
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
                    onClick = { showTimePicker.value = false },
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
                    clockDialSelectedContentColor =  Color.White,
                    clockDialUnselectedContentColor = MaterialTheme.colorScheme.inverseSurface,
                    selectorColor = MaterialTheme.colorScheme.primary,
                    containerColor = MaterialTheme.colorScheme.surface,
                    periodSelectorBorderColor = Color.Transparent,
                    periodSelectorSelectedContainerColor = MaterialTheme.colorScheme.onError.copy(0.5f),
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

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Date Button
        Button(
            onClick = { openDatePickerDialog.value = true },
            colors = ButtonDefaults.buttonColors(Color.Transparent),
            contentPadding = PaddingValues(horizontal = 10.dp),
        ) {
            Row(
                modifier = Modifier.padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Absolute.Left
            ) {
                val themeColors = MaterialTheme.colorScheme
                Icon(
                    painter = painterResource(R.drawable.calendar_bulk),
                    contentDescription = "Date Picker",
                    tint = themeColors.inverseSurface
                )
                Spacer(Modifier.size(5.dp))

                val localDate = getLocalDateFromMillis(transactionDate)
                val dateLabel = getDateLabel(localDate)
                Text(
                    text = dateLabel,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    color = themeColors.inverseSurface,
                    fontFamily = iosFont
                )
            }
        }

        // Time Button
        Button(
            onClick = { showTimePicker.value = true },
            colors = ButtonDefaults.buttonColors(Color.Transparent),
            contentPadding = PaddingValues(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Absolute.Right
            ) {
                val currentTime = LocalTime.ofInstant(Instant.ofEpochMilli(transactionTime), ZoneId.systemDefault())
                val hour = if (currentTime.hour % 12 == 0) 12 else currentTime.hour % 12
                val minute = currentTime.minute
                val amPm = if (currentTime.hour < 12) "AM" else "PM"

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

                Text(
                    text = ":",
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.inverseSurface,
                    fontSize = 16.sp,
                )

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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        text = content,
        containerColor = MaterialTheme.colorScheme.background
    )
}

@Composable
fun TitleInput(
    modifier: Modifier = Modifier,
    transactionTitle: String,
    themeColors: ColorScheme,
    showHeader: Boolean = true,
    onAddTransactionEvent: (AddTransactionEvent) -> Unit,
    focusRequester: FocusRequester = FocusRequester(),
    onComposed: (() -> Unit)? = null
) {
    LaunchedEffect(Unit) {
        onComposed?.invoke()
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(5.dp)
    ) {
        if (showHeader) {
            Text(
                text = "Title",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 5.dp),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = iosFont,
                color = themeColors.inverseSurface
            )
        }
        TextField(
            value = transactionTitle,
            onValueChange = {
                onAddTransactionEvent(AddTransactionEvent.UpdateTitle(it))
            },
            placeholder = {
                Text(
                    text = "Title to this transaction...",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = iosFont
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = themeColors.surface,
                    RoundedCornerShape(10.dp)
                )
                .focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Go
            ),
            maxLines = 2,
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.edit_name_bulk),
                    contentDescription = "Title Icon",
                    tint = themeColors.inverseSurface.copy(
                        alpha = 0.5f
                    )
                )
            },
            singleLine = false,
            colors = TextFieldDefaults.colors(
                focusedPlaceholderColor = themeColors.inverseSurface.copy(0.5f),
                unfocusedPlaceholderColor = themeColors.inverseOnSurface.copy(0.5f),
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                focusedTextColor = themeColors.inverseSurface.copy(0.5f),
                unfocusedTextColor = themeColors.inverseSurface.copy(0.5f)
            ),
            textStyle = TextStyle(
                fontSize = 16.sp,
                textAlign = TextAlign.Start
            )
        )
    }
}

@Composable
fun NotesInput(
    transactionNote: String,
    themeColors: ColorScheme,
    onAddTransactionEvent: (AddTransactionEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(5.dp)
    ) {
        Text(
            text = "Notes",
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 5.dp),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = iosFont,
            color = themeColors.inverseSurface
        )
        TextField(
            value = transactionNote,
            onValueChange = {
                onAddTransactionEvent(AddTransactionEvent.UpdateNote(it))
            },
            placeholder = {
                Text(
                    text = "Add some notes here .....",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = iosFont
                    ),
                    modifier = Modifier
                        .defaultMinSize(minHeight = 100.dp)
                        .fillMaxWidth()
                )
            },
            modifier = Modifier
                .defaultMinSize(minHeight = 100.dp)
                .fillMaxWidth()
                .background(
                    color = themeColors.surface,
                    RoundedCornerShape(10.dp)
                ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            singleLine = false,
            colors = TextFieldDefaults.colors(
                focusedPlaceholderColor = themeColors.inverseSurface.copy(0.5f),
                unfocusedPlaceholderColor = themeColors.inverseOnSurface.copy(0.5f),
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                focusedTextColor = themeColors.inverseSurface.copy(0.5f),
                unfocusedTextColor = themeColors.inverseSurface.copy(0.5f)
            ),
            textStyle = TextStyle(
                fontSize = 16.sp,
                textAlign = TextAlign.Start
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionsDetailsInput(
    transactionUiState: AddTransactionScreenState,
    onAddTransactionEvent: (AddTransactionEvent) -> Unit,
    getTransactionStatsForAccount: (Int, (AccountTransactionStats?) -> Unit) -> Unit,
    getTransactionStatsForCategory: (Int, (TransactionStats) -> Unit) -> Unit,
    getTransactionStatsForSubCategory: (Int, (TransactionStats) -> Unit) -> Unit,
    accountUiState: AccountScreenState,
    onAccountEvent: (AccountScreenEvent) -> Unit,
    updateAccountCurrency: (Int, String) -> Unit,
    setAccountAsMain: (AccountEntity) -> Unit,
    categoryUiState: CategoryScreenState,
    onCategoryEvent: (CategoryScreenEvent) -> Unit,
    setSelectedCategoryId: (Int?) -> Unit,
    clearSelection: () -> Unit,
    subCategoryUiState: SubCategoryState,
    onSubCategoryEvent: (SubCategoryEvent) -> Unit,
    currencyUiState: CurrencyViewModel.CurrencyUiState,
    selectCurrency: (String) -> Unit,
    usedScreenWidth: Dp,
    usedScreenHeight: Dp,
    accounts: List<AccountEntity>,
    categories: List<CategoryEntity>,
    subCategories: List<SubCategoryEntity>,
    transactionAccountId: Int,
    transactionCategoryId: Int,
    transactionSubCategoryId: Int,
    coroutineScope: CoroutineScope,
    transactionType: TransactionType,
    transactionTypeMenuOpened: Boolean,
    isTransactionTypeInfoSheetOpen: Boolean,
    isUpdateTransaction: Boolean,
    transactionTypeListItemsColor: List<Color>,
    transactionTypeSheetState: SheetState,
    intervalSheetState: SheetState,
    frequencySheetState: SheetState,
    recurrenceMenuOpened: Boolean,
    isRecurrenceBottomSheetOpen: Boolean,
    isCustomInputBottomSheetOpen: Boolean,
    isEndDateSelected: Boolean,
    openEndDatePicker: Boolean,
    endDatePickerState: DatePickerState,
    themeColors: ColorScheme,
    // ADD THIS PARAMETER: Current transaction mode to filter transaction types
    currentTransactionMode: String,
){
    val selectedAccount = accounts.find { it.id == transactionAccountId }
    val selectedCategory = categories.find { it.id == transactionCategoryId }
    val selectedSubCategory = subCategories.find { it.id == transactionSubCategoryId }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
    ) {
        // Account Selection
        OptionsComponent(
            label = "Account",
            modifier = Modifier.padding(horizontal = 5.dp),
            isFirstItem = true
        ) {
            val sheetState =
                rememberModalBottomSheetState(skipPartiallyExpanded = true)
            var accountMenuOpened = rememberSaveable {
                mutableStateOf(false)
            }
            Row(
                modifier = Modifier
                    .padding(vertical = 5.dp)
                    .background(
                        color = themeColors.surface,
                        shape = RoundedCornerShape(10.dp) // Matches shape
                    )
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clickable(
                            onClick = {
                                accountMenuOpened.value = true
                            },
                        )
                ) {
                    Text(
                        text = selectedAccount?.accountName
                            ?: "Select",
                        fontFamily = iosFont,
                        fontSize = 14.sp,
                        color = if (transactionAccountId != 0) themeColors.inverseSurface else themeColors.primary
                    )
                }
            }
            if (accountMenuOpened.value) {
                CustomAccountSelectionSheet(
                    sheetState = sheetState,
                    usedScreenWidth = usedScreenWidth,
                    usedScreenHeight = usedScreenHeight,
                    themeColors = themeColors,
                    accounts = accounts,
                    onClick = {
                        coroutineScope
                            .launch { sheetState.hide() }
                            .invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    accountMenuOpened.value =
                                        false
                                }
                            }
                    },
                    onDismissRequest = {
                        accountMenuOpened.value = false
                    },
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

        // Recurrence Type
        Column(
            modifier = Modifier.padding(horizontal = 5.dp)
        ) {
            OptionsComponent(
                label = "Transaction Type",
            ) {
                TextButton(onClick = {
                    onAddTransactionEvent(AddTransactionEvent.SetTransactionTypeMenuOpened(true))
                }) {
                    Text(
                        text = transactionType.name.lowercase()
                            .replaceFirstChar { it.uppercase() },
                        fontFamily = iosFont,
                        color = themeColors.inverseSurface.copy(
                            alpha = 0.5f
                        )
                    )
                }
            }
            AnimatedVisibility(transactionTypeMenuOpened) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TransactionTypeInfoIcon(
                        isTransactionTypeInfoSheetOpen = isTransactionTypeInfoSheetOpen,
                        onIconClick = {
                            onAddTransactionEvent(AddTransactionEvent.SetTransactionTypeInfoSheetOpen(true))
                        },
                        onSheetDismiss = {
                            onAddTransactionEvent(AddTransactionEvent.SetTransactionTypeInfoSheetOpen(false))
                        },
                        onTransactionTypeSelected = { selectedType ->
                            onAddTransactionEvent(AddTransactionEvent.UpdateType(selectedType))

                            val isRecurringType = selectedType == TransactionType.SUBSCRIPTION || selectedType == TransactionType.REPETITIVE

                            onAddTransactionEvent(AddTransactionEvent.SetTransactionTypeMenuOpened(isRecurringType))
                            onAddTransactionEvent(AddTransactionEvent.SetRecurrenceMenuOpen(isRecurringType))
                        },
                        transactionTypeListItemsColor = transactionTypeListItemsColor,
                        sheetWidth = usedScreenWidth,
                        sheetHeight = usedScreenHeight,
                        sheetState = transactionTypeSheetState,
                        // ADD THIS PARAMETER: Pass the current transaction mode for filtering
                        currentTransactionMode = currentTransactionMode
                    )

                    Box {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // FILTER TRANSACTION TYPES BASED ON CURRENT MODE
                            val availableTransactionTypes = getAvailableTransactionTypes(currentTransactionMode)

                            availableTransactionTypes.forEachIndexed { index, type ->
                                item {
                                    val originalIndex = TransactionType.entries.indexOf(type)
                                    Button(
                                        onClick = {
                                            onAddTransactionEvent(AddTransactionEvent.UpdateType(type))

                                            val isRecurringType = type == TransactionType.SUBSCRIPTION || type == TransactionType.REPETITIVE

                                            onAddTransactionEvent(AddTransactionEvent.SetTransactionTypeMenuOpened(isRecurringType))
                                            onAddTransactionEvent(AddTransactionEvent.SetRecurrenceMenuOpen(isRecurringType))
                                        },
                                        modifier = Modifier
                                            .defaultMinSize(
                                                minHeight = 20.dp
                                            )
                                            .padding(horizontal = 5.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = transactionTypeListItemsColor[originalIndex].copy(
                                                0.1f
                                            ),
                                            contentColor = transactionTypeListItemsColor[originalIndex]
                                        ),
                                        shape = RoundedCornerShape(
                                            10.dp
                                        )
                                    ) {
                                        Text(
                                            text = type.toString()
                                                .lowercase()
                                                .replaceFirstChar { it.uppercase() },
                                            fontFamily = iosFont,
                                            fontSize = 12.sp
                                        )
                                    }
                                    if (index != availableTransactionTypes.lastIndex) {
                                        Spacer(
                                            modifier = Modifier.size(
                                                5.dp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(
                            modifier = Modifier
                                .height(56.dp)
                                .width(20.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            themeColors.background,
                                            Color.Transparent
                                        )
                                    )
                                )
                                .align(Alignment.TopStart)
                        )
                        Spacer(
                            modifier = Modifier
                                .height(56.dp)
                                .width(20.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            themeColors.background
                                        )
                                    )
                                )
                                .align(Alignment.TopEnd)
                        )
                    }
                }
            }
            AnimatedVisibility(visible = recurrenceMenuOpened) {
                RecurrenceSelector(
                    transactionUiState = transactionUiState,
                    onAddTransactionEvent = onAddTransactionEvent,
                    frequency = transactionUiState.recurrenceFrequency,
                    interval = transactionUiState.recurrenceInterval,
                    endDateState = endDatePickerState,
                    isRecurrenceBottomSheetOpen = isRecurrenceBottomSheetOpen,
                    isCustomInputBottomSheetOpen = isCustomInputBottomSheetOpen,
                    isEndDateSelected = isEndDateSelected,
                    openEndDatePicker = openEndDatePicker,
                    frequencySheetState = frequencySheetState,
                    intervalSheetState = intervalSheetState
                )
            }
        }
        // Transaction Category
        OptionsComponent(
            label = "Category",
            modifier = Modifier.padding(horizontal = 5.dp),
            isLastItem = true
        ) {
            val sheetState =
                rememberModalBottomSheetState(skipPartiallyExpanded = true)
            var categoryMenuOpened = rememberSaveable {
                mutableStateOf(false)
            }
            Row(
                modifier = Modifier
                    .padding(vertical = 5.dp)
                    .background(
                        color = themeColors.surface,
                        shape = RoundedCornerShape(10.dp) // Matches shape
                    )
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedVisibility(visible = transactionCategoryId != 0) {
                    Image(
                        painter = painterResource(
                            id = if (selectedSubCategory != null) {
                                // Show SubCategory Icon when SubCategory is selected
                                icons.find { it.id == selectedSubCategory.subcategoryIconId }?.resourceId
                                    ?: R.drawable.type_beverages_beer
                            } else {
                                // Show Category Icon when SubCategory is not available
                                icons.find { it.id == selectedCategory?.categoryIconId }?.resourceId
                                    ?: R.drawable.type_beverages_beer
                            }
                        ),
                        contentDescription = if (selectedSubCategory != null) "SubCategory Icon" else "Category Icon",
                        modifier = Modifier
                            .size(22.dp)
                            .padding(end = 5.dp)
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clickable(
                            onClick = {
                                categoryMenuOpened.value = true
                            },
                        )
                ) {
                    Text(
                        text = selectedSubCategory?.name// Show SubCategory Name when SubCategory is selected
                            ?: (selectedCategory?.name ?: "Select"),
                        fontFamily = iosFont,
                        fontSize = 14.sp,
                        color = if (transactionCategoryId != 0) themeColors.inverseSurface else themeColors.primary
                    )
                }
            }
            if (categoryMenuOpened.value) {
                CustomCategoriesSelectionSheet(
                    sheetState = sheetState,
                    usedScreenWidth = usedScreenWidth,
                    usedScreenHeight = usedScreenHeight,
                    themeColors = themeColors,
                    categories = categories,
                    isUpdateTransaction = isUpdateTransaction,
                    onClick = {
                        coroutineScope
                            .launch { sheetState.hide() }
                            .invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    categoryMenuOpened.value = false
                                }
                            }
                    },
                    onDismissRequest = {
                        categoryMenuOpened.value = false
                    },
                    categoryUiState = categoryUiState,
                    onCategoryEvent = onCategoryEvent,
                    subCategoryUiState = subCategoryUiState,
                    onSubCategoryEvent = onSubCategoryEvent,
                    transactionUiState = transactionUiState,
                    onAddTransactionEvent = onAddTransactionEvent,
                    getTransactionStatsForCategory = getTransactionStatsForCategory,
                    getTransactionStatsForSubCategory = getTransactionStatsForSubCategory,
                    setSelectedCategoryId = setSelectedCategoryId,
                    clearSelection = clearSelection
                )
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TransactionTypeInfoIcon(
    isTransactionTypeInfoSheetOpen: Boolean,
    onIconClick: () -> Unit,
    onSheetDismiss: () -> Unit,
    onTransactionTypeSelected: (TransactionType) -> Unit,
    transactionTypeListItemsColor: List<Color>,
    sheetWidth: Dp,
    sheetHeight: Dp,
    sheetState: SheetState,
    // ADD THIS PARAMETER: Current transaction mode for filtering
    currentTransactionMode: String
) {
    val scrollOverScrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val overscrollEffect = remember(coroutineScope) { VerticalStretchedOverscroll(coroutineScope) }
    val themeColors = MaterialTheme.colorScheme

    Icon(
        imageVector = Icons.Outlined.Info,
        contentDescription = "Info",
        tint = themeColors.inverseSurface.copy(alpha = 0.5f),
        modifier = Modifier.clickable { onIconClick() }
    )

    if (isTransactionTypeInfoSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { onSheetDismiss() },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.background,
            sheetMaxWidth = sheetWidth,
            dragHandle = {
                DragHandle(
                    color = MaterialTheme.colorScheme.inverseSurface.copy(0.3f)
                )
            },
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        ) {
            LazyColumn(
                state = scrollOverScrollState,
                userScrollEnabled = false,
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .height(sheetHeight)
                    .fillMaxWidth()
                    .overscroll(overscrollEffect)
                    .scrollable(
                        orientation = Orientation.Vertical,
                        reverseDirection = true,
                        state = scrollOverScrollState,
                        overscrollEffect = overscrollEffect
                    ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "Transaction Types",
                        textAlign = TextAlign.Center,
                        fontFamily = iosFont,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = themeColors.inverseSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    )
                }

                // FILTER TRANSACTION TYPES BASED ON CURRENT MODE
                val availableTransactionTypes = getAvailableTransactionTypes(currentTransactionMode)

                availableTransactionTypes.forEachIndexed { index, type ->
                    item {
                        val typeDescription = when (type) {
                            TransactionType.DEFAULT -> "• Normal Transaction"
                            TransactionType.UPCOMING -> "• Upcoming transaction\n• Won't be added until marked as 'Paid'."
                            TransactionType.SUBSCRIPTION -> "• Recurring transaction\n• Next entry won't be added until marked as 'Paid'."
                            TransactionType.REPETITIVE -> "• Recurring transaction\n• Next entry won't be added until marked as 'Paid'."
                            TransactionType.LENT -> "• Transaction\n• Added as an expense.\n• Amount returns when marked as 'Collected'."
                            TransactionType.BORROWED -> "• Transaction\n• Added as income.\n• Amount removed when marked as 'Settled'."
                            else -> "• Undefined transaction type"
                        }
                        val coroutineScope = rememberCoroutineScope()
                        val originalIndex = TransactionType.entries.indexOf(type)

                        Box(
                            contentAlignment = Alignment.CenterStart,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    onTransactionTypeSelected(type)
                                    coroutineScope
                                        .launch { sheetState.hide() }
                                        .invokeOnCompletion {
                                            if (!sheetState.isVisible) {
                                                onSheetDismiss()
                                            }
                                        }
                                }
                                .background(
                                    color = transactionTypeListItemsColor[originalIndex].copy(0.1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .padding(10.dp)
                        ) {
                            Column(
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = type.name.lowercase()
                                        .replaceFirstChar { it.uppercase() },
                                    fontFamily = iosFont,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 18.sp,
                                    color = transactionTypeListItemsColor[originalIndex]
                                )
                                Text(
                                    text = typeDescription,
                                    fontFamily = iosFont,
                                    fontSize = 14.sp,
                                    color = themeColors.inverseSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurrenceSelector(
    transactionUiState: AddTransactionScreenState,
    onAddTransactionEvent: (AddTransactionEvent) -> Unit,
    frequency: RecurrenceFrequency,
    interval: Int,
    endDateState: DatePickerState,
    isRecurrenceBottomSheetOpen: Boolean,
    isCustomInputBottomSheetOpen: Boolean,
    isEndDateSelected: Boolean,
    openEndDatePicker: Boolean,
    frequencySheetState: SheetState,
    intervalSheetState: SheetState
) {
    var selectedFrequency by rememberSaveable { mutableStateOf(frequency.toString()) }
    var intervalInput by rememberSaveable { mutableIntStateOf(interval) }
    val currentDate by transactionUiState.transactionDate.let { MutableStateFlow(it) }.collectAsState()
    val endDatePickerState = endDateState
    // FIX: Initialize end date selection state based on existing data
//    val screenState by transactionViewModel.state.collectAsState()

    val selectedDate = endDatePickerState.selectedDateMillis ?: System.currentTimeMillis()
    val endLocalDate = getLocalDateFromMillis(selectedDate)
    val endDateLabel = getEndDateLabel(endLocalDate)
    val shouldShowEndDate = remember(selectedDate, currentDate, transactionUiState.recurrenceEndDate, isEndDateSelected) {
        // Show end date if:
        // 1. There's an end date in the state (existing transaction), OR
        // 2. User has selected an end date and it's different from current date, OR
        // 3. Picker has a date different from current date
        transactionUiState.recurrenceEndDate != null ||
                (isEndDateSelected && selectedDate != currentDate) ||
                (selectedDate != currentDate && selectedDate != System.currentTimeMillis())
    }

    // FIX: Sync the isEndDateSelected state with actual data
    LaunchedEffect(transactionUiState.recurrenceEndDate, selectedDate) {
        val hasEndDate = transactionUiState.recurrenceEndDate != null ||
                (selectedDate != currentDate && selectedDate != System.currentTimeMillis())
        if (hasEndDate && !isEndDateSelected) {
            onAddTransactionEvent(AddTransactionEvent.SetEndDateSelected(true))
        }
    }

    // BottomSheets
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val usedScreenWidth = screenWidth - 10.dp

    val recurrenceCount = calculateRecurrenceCount(
        startDate = getLocalDateFromMillis(currentDate),
        endDate = endLocalDate,
        frequency = RecurrenceFrequency.valueOf(selectedFrequency),
        interval = if(intervalInput == 0) 1 else intervalInput
    )
    val nextDueDate = getNextDueDate(
        currentDate = currentDate,
        frequency = RecurrenceFrequency.valueOf(selectedFrequency),
        interval = intervalInput,
        endDate = selectedDate
    )

    val unit = when (selectedFrequency) {
        RecurrenceFrequency.DAILY.toString() -> "days"
        RecurrenceFrequency.WEEKLY.toString() -> "weeks"
        RecurrenceFrequency.MONTHLY.toString() -> "months"
        RecurrenceFrequency.YEARLY.toString() -> "years"
        else -> "times"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 5.dp, bottom = 10.dp, end = 10.dp)
    ) {
        // Frequency selection row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp)
        ) {
            Text(
                text = "Repeat every",
                fontSize = 14.sp,
                fontFamily = iosFont,
                color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.5f),
                modifier = Modifier
            )

            Spacer(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .fillMaxWidth(1f)
                    .weight(1f)
                    .height(1.dp)
                    .background(color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.1f))
            )

            // Intervals input container
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clickable(
                        onClick = {
                            onAddTransactionEvent(AddTransactionEvent.SetCustomInputBottomSheetOpen(true))
                        },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
                    .height(30.dp)
                    .widthIn(min = 30.dp, max = 110.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceBright,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 10.dp)
            ) {
                Text(
                    text = if(intervalInput.toString() == "0") "1" else intervalInput.toString(),
                    fontSize = 14.sp,
                    lineHeight = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = iosFont,
                    color = if (intervalInput.toString() == "0") MaterialTheme.colorScheme.inverseSurface.copy(0.5f) else MaterialTheme.colorScheme.inverseSurface
                )
            }

            // Frequency Selection Container
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clickable(
                        onClick = {
                            onAddTransactionEvent(AddTransactionEvent.SetRecurrenceBottomSheetOpen(true))
                        },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
                    .background(
                        color = Color.Transparent,
                        shape = RoundedCornerShape(10.dp)
                    )
            ) {
                Text(
                    text = unit,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = iosFont,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 5.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Built-in Date Picker Dialog for End Date
        if (openEndDatePicker) {
            DatePickerDialog(
                onDismissRequest = {
                    onAddTransactionEvent(AddTransactionEvent.SetOpenEndDatePicker(false))
                    endDatePickerState.selectedDateMillis?.let { selectedDateMillis ->
                        onAddTransactionEvent(AddTransactionEvent.UpdateRecurrenceEndDate(selectedDateMillis))
                        onAddTransactionEvent(AddTransactionEvent.SetEndDateSelected(true))
                    }
                    onAddTransactionEvent(AddTransactionEvent.UpdateNextDueDate(nextDueDate))
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onAddTransactionEvent(AddTransactionEvent.SetOpenEndDatePicker(false))
                            endDatePickerState.selectedDateMillis?.let { selectedDateMillis ->
                                onAddTransactionEvent(AddTransactionEvent.UpdateRecurrenceEndDate(selectedDateMillis))
                                onAddTransactionEvent(AddTransactionEvent.SetEndDateSelected(true))
                            }
                            onAddTransactionEvent(AddTransactionEvent.UpdateNextDueDate(nextDueDate))
                        },
                        modifier = Modifier
                            .shadow(5.dp, RoundedCornerShape(15.dp), spotColor = MaterialTheme.colorScheme.primary, ambientColor = Color.Transparent),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(15.dp)

                    ) {
                        Text(
                            text = "OK",
                            modifier = Modifier.padding(horizontal = 10.dp),
                            fontFamily = iosFont
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            onAddTransactionEvent(AddTransactionEvent.SetOpenEndDatePicker(false))
                            onAddTransactionEvent(AddTransactionEvent.UpdateRecurrenceEndDate(endDatePickerState.selectedDateMillis))
                            onAddTransactionEvent(AddTransactionEvent.UpdateNextDueDate(nextDueDate))
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
                },
                colors = DatePickerDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
            ) {
                DatePicker(
                    state = endDatePickerState,
                    colors = DatePickerDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                        headlineContentColor = MaterialTheme.colorScheme.inverseSurface,
                        weekdayContentColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                        subheadContentColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                        navigationContentColor = MaterialTheme.colorScheme.inverseSurface,
                        yearContentColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                        currentYearContentColor = MaterialTheme.colorScheme.inverseSurface,
                        selectedYearContentColor = Color.White,
                        selectedYearContainerColor = MaterialTheme.colorScheme.primary,
                        dayContentColor = MaterialTheme.colorScheme.inverseSurface,
                        disabledDayContentColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                        selectedDayContentColor = Color.White,
                        disabledSelectedDayContentColor = MaterialTheme.colorScheme.inverseSurface,
                        selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                        disabledSelectedDayContainerColor = MaterialTheme.colorScheme.onBackground,
                        todayContentColor = MaterialTheme.colorScheme.primary.copy(0.6f),
                        todayDateBorderColor = MaterialTheme.colorScheme.primary,
                        dayInSelectionRangeContentColor = MaterialTheme.colorScheme.primary,
                        dayInSelectionRangeContainerColor = MaterialTheme.colorScheme.primary.copy(0.5f),
                        dividerColor = MaterialTheme.colorScheme.primary,
                        dateTextFieldColors = TextFieldColors(
                            cursorColor = MaterialTheme.colorScheme.primary,
                            focusedTextColor = MaterialTheme.colorScheme.inverseSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.inverseSurface,
                            disabledTextColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                            errorTextColor = MaterialTheme.colorScheme.error.copy(0.5f),
                            focusedContainerColor = MaterialTheme.colorScheme.onBackground,
                            unfocusedContainerColor = MaterialTheme.colorScheme.onBackground,
                            disabledContainerColor = MaterialTheme.colorScheme.onBackground.copy(0.5f),
                            errorContainerColor = MaterialTheme.colorScheme.onErrorContainer,
                            errorCursorColor = MaterialTheme.colorScheme.error,
                            textSelectionColors = TextSelectionColors(
                                handleColor = MaterialTheme.colorScheme.primary,
                                backgroundColor = MaterialTheme.colorScheme.primary.copy(0.5f)
                            ),
                            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                            unfocusedIndicatorColor = MaterialTheme.colorScheme.primary.copy(0.3f),
                            disabledIndicatorColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                            errorIndicatorColor = MaterialTheme.colorScheme.error.copy(0.5f),
                            focusedLeadingIconColor = MaterialTheme.colorScheme.inverseSurface,
                            unfocusedLeadingIconColor = MaterialTheme.colorScheme.inverseSurface,
                            disabledLeadingIconColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                            errorLeadingIconColor = MaterialTheme.colorScheme.error.copy(0.5f),
                            focusedTrailingIconColor = MaterialTheme.colorScheme.inverseSurface,
                            unfocusedTrailingIconColor = MaterialTheme.colorScheme.inverseSurface,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                            errorTrailingIconColor =MaterialTheme.colorScheme.error,
                            focusedLabelColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                            unfocusedLabelColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                            disabledLabelColor = MaterialTheme.colorScheme.inverseSurface.copy(0.3f),
                            errorLabelColor = MaterialTheme.colorScheme.error.copy(0.5f),
                            focusedPlaceholderColor =  MaterialTheme.colorScheme.onBackground,
                            unfocusedPlaceholderColor =  MaterialTheme.colorScheme.onBackground,
                            disabledPlaceholderColor =  MaterialTheme.colorScheme.onBackground.copy(0.5f),
                            errorPlaceholderColor =  MaterialTheme.colorScheme.onBackground,
                            focusedSupportingTextColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                            unfocusedSupportingTextColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                            disabledSupportingTextColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                            errorSupportingTextColor = MaterialTheme.colorScheme.error.copy(0.5f),
                            focusedPrefixColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                            unfocusedPrefixColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                            disabledPrefixColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                            errorPrefixColor = MaterialTheme.colorScheme.error.copy(0.5f),
                            focusedSuffixColor =MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                            unfocusedSuffixColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                            disabledSuffixColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                            errorSuffixColor = MaterialTheme.colorScheme.error.copy(0.5f)
                        )
                    )
                )
            }
        }

        // End date selection row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp)
        ) {
            Text(
                text = "Until",
                fontSize = 14.sp,
                fontFamily = iosFont,
                color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.5f),
                modifier = Modifier
            )

            Spacer(
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .weight(1f)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.1f))
            )

            // End Date Picker Container
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clickable(
                        onClick = {
                            onAddTransactionEvent(AddTransactionEvent.SetOpenEndDatePicker(true))
                            onAddTransactionEvent(AddTransactionEvent.SetEndDateSelected(true))
                        },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
                    .background(
                        color = Color.Transparent,
                        shape = RoundedCornerShape(10.dp)
                    )
            ) {
                // FIX: Corrected display logic
                Text(
                    text = if (shouldShowEndDate) {
                        endDateLabel
                    } else {
                        "Forever"
                    },
                    fontSize = 14.sp,
                    fontFamily = iosFont,
                    color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.5f),
                )
            }

            // FIX: Recurrence count display with correct condition
            if (shouldShowEndDate) {
                Text(
                    text = "(${recurrenceCount}x)",
                    fontSize = 12.sp,
                    fontFamily = iosFont,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 5.dp)
                )
            }

            // FIX: Clear button with correct visibility condition
            AnimatedVisibility(
                visible = shouldShowEndDate,
                label = "Clear Button"
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .padding(start = 5.dp)
                        .clickable(
                            onClick = {
                                // Clear the end date
                                onAddTransactionEvent(AddTransactionEvent.UpdateRecurrenceEndDate(null))
                                selectedFrequency = RecurrenceFrequency.MONTHLY.toString()
                                onAddTransactionEvent(AddTransactionEvent.UpdateRecurrenceFrequency(RecurrenceFrequency.NONE))
                                intervalInput = 0
                                onAddTransactionEvent(AddTransactionEvent.UpdateRecurrenceInterval(1))
                                onAddTransactionEvent(AddTransactionEvent.SetOpenEndDatePicker(false))
                                onAddTransactionEvent(AddTransactionEvent.SetEndDateSelected(false))
                            },
                        )
                        .background(
                            color = MaterialTheme.colorScheme.onError,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .padding(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear End Date",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // BottomSheet for selecting frequency
    if (isRecurrenceBottomSheetOpen) {
        ModalBottomSheet(
            sheetState = frequencySheetState,
            onDismissRequest = {
                onAddTransactionEvent(AddTransactionEvent.SetRecurrenceBottomSheetOpen(false))
            },
            dragHandle = {
                DragHandle(
                    color = MaterialTheme.colorScheme.inverseSurface.copy(0.3f)
                )
            },
            containerColor = MaterialTheme.colorScheme.background,
            sheetMaxWidth = usedScreenWidth,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                RecurrenceFrequency.entries.forEach { frequency ->
                    AnimatedVisibility(visible = frequency.toString() != "NONE") {
                        Column {
                            TextButton(
                                onClick = {
                                    selectedFrequency = frequency.toString()
                                    onAddTransactionEvent(AddTransactionEvent.UpdateRecurrenceFrequency(frequency))
                                    onAddTransactionEvent(AddTransactionEvent.SetRecurrenceBottomSheetOpen(false))
                                }
                            ) {
                                Text(
                                    text = frequency.toString(),
                                    fontFamily = iosFont,
                                )
                            }
                            if(RecurrenceFrequency.entries.last() != frequency){
                                Spacer(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(
                                            MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.1f)
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // BottomSheet for custom Interval input
    if (isCustomInputBottomSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = {
                onAddTransactionEvent(AddTransactionEvent.SetCustomInputBottomSheetOpen(false))
            },
            sheetState = intervalSheetState,
            containerColor = MaterialTheme.colorScheme.background,
            sheetMaxWidth = usedScreenWidth,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            dragHandle = {
                DragHandle(
                    color = MaterialTheme.colorScheme.inverseSurface.copy(0.3f)
                )
            },
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Enter Intervals",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = iosFont,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.inverseSurface.copy(0.7f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 5.dp, bottom = 10.dp)
                )

                CustomNumberPadOutlinedTextField(
                    intervalInput = intervalInput.toString(),
                    onValueChange = { newValue ->
                        intervalInput = if (newValue.isEmpty()) 0 else newValue.toInt()
                    },
                )

                Spacer(modifier = Modifier.height(16.dp))

                val scope = rememberCoroutineScope()
                AnimatedVisibility(visible = intervalInput != 0) {
                    Button(
                        onClick = {
                            onAddTransactionEvent(AddTransactionEvent.UpdateRecurrenceInterval(intervalInput))
                            scope
                                .launch { intervalSheetState.hide() }
                                .invokeOnCompletion {
                                    if (!intervalSheetState.isVisible) {
                                        onAddTransactionEvent(AddTransactionEvent.SetCustomInputBottomSheetOpen(false))
                                    }
                                }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .shadow(
                                10.dp,
                                RoundedCornerShape(15.dp),
                                spotColor = MaterialTheme.colorScheme.primary,
                                ambientColor = MaterialTheme.colorScheme.primary
                            )
                            .background(
                                color = MaterialTheme.colorScheme.primary,
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomAmountInputBottomSheet(
    amount: String,
    amountSheetState: SheetState,
    specialKeys: Set<Char>,
    currencyCode: String?,
    onClick: () -> Unit,
    onDismissRequest: () -> Unit,
    onValueChange: (String) -> Unit,
    onResultAmount: (String) -> Unit,
    isTransactionTypeInfoSheetOpen: Boolean = false,
    transactionTypeSheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    currentTransactionMode: String  = "",
    transactionTypeListItemsColor: List<Color> = emptyList<Color>(),
    usedScreenWidth: Dp = 0.dp,
    usedScreenHeight: Dp = 0.dp,
    themeColors: ColorScheme = MaterialTheme.colorScheme,
    recurrenceMenuOpened: Boolean = false,
    endDatePickerState: DatePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis()) ,
    isRecurrenceBottomSheetOpen: Boolean = false,
    isCustomInputBottomSheetOpen: Boolean = false,
    isEndDateSelected: Boolean = false,
    openEndDatePicker: Boolean = false,
    frequencySheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    intervalSheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    showTransactionTypeSelection: Boolean = false,
    transactionUiState: AddTransactionScreenState,
    onAddTransactionEvent: (AddTransactionEvent) -> Unit,

){
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = amountSheetState,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = {
            DragHandle(
                color = themeColors.inverseSurface.copy(0.3f)
            )
        },
        sheetMaxWidth = usedScreenWidth,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        Text(
            text = "Enter Amount",
            fontFamily = iosFont,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.inverseSurface,
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
                amountInput = amount,
                specialKeys = specialKeys,
                onValueChange = onValueChange,
                onResultAmount = onResultAmount,
                currencySymbol = CurrencySymbols.getSymbol(currencyCode.toString()),
                isTransactionTypeInfoSheetOpen = isTransactionTypeInfoSheetOpen,
                transactionTypeSheetState = transactionTypeSheetState,
                currentTransactionMode = currentTransactionMode,
                transactionTypeListItemsColor = transactionTypeListItemsColor,
                showTransactionTypeSelection = showTransactionTypeSelection,
                usedScreenWidth = usedScreenWidth,
                usedScreenHeight = usedScreenHeight,
                themeColors = themeColors,
                recurrenceMenuOpened = recurrenceMenuOpened,
                endDatePickerState = endDatePickerState,
                isRecurrenceBottomSheetOpen = isRecurrenceBottomSheetOpen,
                isCustomInputBottomSheetOpen = isCustomInputBottomSheetOpen,
                isEndDateSelected = isEndDateSelected,
                openEndDatePicker = openEndDatePicker,
                frequencySheetState = frequencySheetState,
                intervalSheetState = intervalSheetState,
                transactionUiState = transactionUiState,
                onAddTransactionEvent = onAddTransactionEvent
            )

            Spacer(modifier = Modifier.height(16.dp))


            AnimatedVisibility(visible = amount.isNotEmpty()) {
                Button(
                    onClick = onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(15.dp),
                            spotColor = MaterialTheme.colorScheme.primary,
                            ambientColor = MaterialTheme.colorScheme.primary
                        )
                        .height(50.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
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
fun CustomAccountSelectionSheet(
    sheetState: SheetState,
    accountUiState: AccountScreenState,
    onAccountEvent: (AccountScreenEvent) -> Unit,
    getTransactionStatsForAccount: (id: Int, callback: (AccountTransactionStats?) -> Unit) -> Unit,
    updateAccountCurrency: (Int, String) -> Unit,
    setAccountAsMain: (AccountEntity) -> Unit,
    currencyUiState: CurrencyViewModel.CurrencyUiState,
    selectCurrency: (String) -> Unit,
    transactionUiState: AddTransactionScreenState,
    onAddTransactionEvent: (AddTransactionEvent) -> Unit,
    accounts: List<AccountEntity>,
    usedScreenWidth: Dp,
    usedScreenHeight: Dp,
    themeColors: ColorScheme,
    onClick: () -> Unit,
    onDismissRequest: () -> Unit,
){
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
        sheetMaxWidth = usedScreenWidth - 5.dp,
        dragHandle =  {
            DragHandle(
                color = themeColors.inverseSurface.copy(0.3f)
            )
        },
        containerColor = themeColors.background,
        contentColor = themeColors.inverseSurface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(usedScreenHeight)
        ) {
            AccountDisplayList(
                items = accounts,
                modifier = Modifier.fillMaxWidth(),
                onClick = onClick,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomCategoriesSelectionSheet(
    sheetState: SheetState,
    categoryUiState: CategoryScreenState,
    onCategoryEvent: (CategoryScreenEvent) -> Unit,
    subCategoryUiState: SubCategoryState,
    onSubCategoryEvent: (SubCategoryEvent) -> Unit,
    transactionUiState: AddTransactionScreenState,
    onAddTransactionEvent: (AddTransactionEvent) -> Unit,
    getTransactionStatsForCategory: (categoryId: Int, onResult: (TransactionStats) -> Unit) -> Unit,
    getTransactionStatsForSubCategory: (subCategoryId: Int, onResult: (TransactionStats) -> Unit) -> Unit,
    setSelectedCategoryId: (Int?) -> Unit,
    clearSelection: () -> Unit,
    categories: List<CategoryEntity>,
    usedScreenWidth: Dp,
    usedScreenHeight: Dp,
    onClick: () -> Unit,
    onDismissRequest: () -> Unit,
    themeColors: ColorScheme,
    isUpdateTransaction: Boolean = false
){
    var showSubCategorySheet by remember { mutableStateOf(false) }
    var selectedCategoryId by remember { mutableIntStateOf(0) }
    var pendingCategoryId by remember { mutableIntStateOf(0) } // Track pending category selection
    val subCategorySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Get current transaction state
//    val transactionState by transactionViewModel.state.collectAsState()
    val currentSubCategoryId = transactionUiState.transactionSubCategoryId
    val currentCategoryId = transactionUiState.transactionCategoryId

    // Observe subcategories for reactive behavior
    val subCategories = subCategoryUiState.subCategories
    val observedCategoryId = subCategoryUiState.selectedCategoryId

    val coroutineScope = rememberCoroutineScope()
// Auto-open subcategory sheet when editing transaction with subcategory
    LaunchedEffect(currentSubCategoryId, isUpdateTransaction) {
        if (isUpdateTransaction && currentSubCategoryId != 0 && currentCategoryId != 0) {
            selectedCategoryId = currentCategoryId
            onSubCategoryEvent(SubCategoryEvent.FetchSubCategoriesForCategory(currentCategoryId))
            delay(100)
            showSubCategorySheet = true
        }
    }

    // 🔥 FIX: Watch for subcategory changes and show sheet immediately
    LaunchedEffect(selectedCategoryId, subCategoryUiState.subCategories) {
        if (selectedCategoryId != 0) {
            // Small delay to ensure state is settled
            delay(50)

            if (subCategoryUiState.subCategories.isNotEmpty()) {
                showSubCategorySheet = true
            } else {
                // No subcategories available, proceed to account selection
                onClick()
            }
        }
    }

    // 🔥 FIX: Category selection handler - don't call onClick here
    fun handleCategorySelection(categoryId: Int) {
        selectedCategoryId = categoryId
        setSelectedCategoryId(categoryId)

        // Fetch subcategories
        onSubCategoryEvent(SubCategoryEvent.FetchSubCategoriesForCategory(categoryId))
        // Let the LaunchedEffect above handle what happens next
    }
    // 🔥 FIX: Handle subcategory sheet dismissal - go back to category selection
    fun handleSubCategoryDismiss() {
        showSubCategorySheet = false
        // Reset selected category to allow reselection
        selectedCategoryId = 0
        // Clear the category selection in transaction state so user can select again
        onAddTransactionEvent(AddTransactionEvent.UpdateCategoryId(0))
        onAddTransactionEvent(AddTransactionEvent.UpdateSubCategoryId(0))
        // Don't call onDismissRequest - keep the category sheet open
    }

    // 🔥 FIX: Handle subcategory selection completion
    fun handleSubCategoryComplete() {
        showSubCategorySheet = false
        coroutineScope.launch {
            subCategorySheetState.hide()
        }.invokeOnCompletion {
            onClick() // Only call onClick when we actually want to proceed to next step
        }
    }

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
        sheetMaxWidth = usedScreenWidth - 4.dp,
        dragHandle = {
            DragHandle(
                color = themeColors.inverseSurface.copy(0.3f)
            )
        },
        containerColor = themeColors.background,
        contentColor = themeColors.inverseSurface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(usedScreenHeight)
        ) {
            CategoryDisplayList(
                items = categories,
                modifier = Modifier.fillMaxWidth(),
                onCategorySelected = { categoryId ->
                    handleCategorySelection(categoryId)
                },
                categoryUiState = categoryUiState,
                onCategoryEvent = onCategoryEvent,
                subCategoryUiState = subCategoryUiState,
                onSubCategoryEvent = onSubCategoryEvent,
                transactionUiState = transactionUiState,
                onAddTransactionEvent = onAddTransactionEvent,
                getTransactionStatsForCategory = getTransactionStatsForCategory,
                getTransactionStatsForSubCategory = getTransactionStatsForSubCategory,
                setSelectedCategoryId = setSelectedCategoryId
            )
        }
    }

    Log.d("Show sheet", "sheetBoolean $showSubCategorySheet")

    // Subcategory selection sheet
    if (showSubCategorySheet) {
        CustomSubCategoriesSelectionSheet(
            sheetState = subCategorySheetState,
            selectedCategoryId = selectedCategoryId,
            usedScreenWidth = usedScreenWidth,
            usedScreenHeight = usedScreenHeight,
            themeColors = themeColors,
            onSubCategorySelected = {
                handleSubCategoryComplete()
            },
            onSkipSubCategory = {
                onAddTransactionEvent(AddTransactionEvent.UpdateSubCategoryId(0))
                handleSubCategoryComplete()
            },
            onDismissRequest = {
                handleSubCategoryDismiss()
            },
            categoryUiState = categoryUiState,
            onCategoryEvent = onCategoryEvent,
            getTransactionStatsForCategory = getTransactionStatsForCategory,
            getTransactionStatsForSubCategory = getTransactionStatsForSubCategory,
            subCategoryUiState = subCategoryUiState,
            onSubCategoryEvent = onSubCategoryEvent,
            setSelectedCategoryId = setSelectedCategoryId,
            clearSelection = clearSelection,
            transactionUiState = transactionUiState,
            onAddTransactionEvent = onAddTransactionEvent
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomSubCategoriesSelectionSheet(
    categoryUiState: CategoryScreenState,
    onCategoryEvent: (CategoryScreenEvent) -> Unit,
    getTransactionStatsForCategory: (categoryId: Int, onResult: (TransactionStats) -> Unit) -> Unit,
    getTransactionStatsForSubCategory: (subCategoryId: Int, onResult: (TransactionStats) -> Unit) -> Unit,
    subCategoryUiState: SubCategoryState,
    onSubCategoryEvent: (SubCategoryEvent) -> Unit,
    setSelectedCategoryId: (Int?) -> Unit,
    clearSelection: () -> Unit,
    transactionUiState: AddTransactionScreenState,
    onAddTransactionEvent: (AddTransactionEvent) -> Unit,
    sheetState: SheetState,
    selectedCategoryId: Int,
    usedScreenWidth: Dp,
    usedScreenHeight: Dp,
    themeColors: ColorScheme,
    onSubCategorySelected: () -> Unit,
    onSkipSubCategory: () -> Unit,
    onDismissRequest: () -> Unit,
){
    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismissRequest,
        sheetMaxWidth = usedScreenWidth + 10.dp,
        dragHandle =  {
            DragHandle(
                color = themeColors.inverseSurface.copy(0.3f)
            )
        },
        containerColor = themeColors.background,
        contentColor = themeColors.inverseSurface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(usedScreenHeight - 8.dp)
        ) {
            SubCategoriesDisplayList(
                categoryId = selectedCategoryId,
                modifier = Modifier.fillMaxWidth(),
                onSubCategorySelected = onSubCategorySelected,
                onSkipSubCategory = onSkipSubCategory,
                categoryUiState = categoryUiState,
                onCategoryEvent = onCategoryEvent,
                getTransactionStatsForCategory = getTransactionStatsForCategory,
                getTransactionStatsForSubCategory = getTransactionStatsForSubCategory,
                subCategoryUiState = subCategoryUiState,
                onSubCategoryEvent = onSubCategoryEvent,
                setSelectedCategoryId = setSelectedCategoryId,
                clearSelection = clearSelection,
                transactionUiState = transactionUiState,
                onAddTransactionEvent = onAddTransactionEvent
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun CustomAmountNumberPadOutlinedTextField(
    currencySymbol: String,
    amountInput: String,
    specialKeys: Set<Char>,
    onValueChange: (String) -> Unit,
    onResultAmount: (String) -> Unit,
    isTransactionTypeInfoSheetOpen: Boolean = false,
    transactionTypeSheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    currentTransactionMode: String  = "",
    transactionTypeListItemsColor: List<Color> = listOf(
        Latte_Blue,
        Latte_Flamingo,
        Latte_Lavender,
        Macchiato_Green,
        Macchiato_Blue,
        Macchiato_Red
    ),
    showTransactionTypeSelection: Boolean = false,
    usedScreenWidth: Dp = 0.dp,
    usedScreenHeight: Dp = 0.dp,
    themeColors: ColorScheme = MaterialTheme.colorScheme,
    recurrenceMenuOpened: Boolean = false,
    endDatePickerState: DatePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis()),
    isRecurrenceBottomSheetOpen: Boolean = false,
    isCustomInputBottomSheetOpen: Boolean = false,
    isEndDateSelected: Boolean = false,
    openEndDatePicker: Boolean = false,
    frequencySheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    intervalSheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    transactionUiState: AddTransactionScreenState,
    onAddTransactionEvent: (AddTransactionEvent) -> Unit,
) {

    // Preserve user input as-is (don't auto-format while typing)
    val displayAmount = remember(amountInput) {
        if (amountInput.isEmpty()) "0" else amountInput
    }

    // Real-time calculated result
    val calculatedResultAmount = remember(displayAmount) {
        try {
            if (displayAmount.isEmpty() || displayAmount == "0") {
                "0"
            } else {
                evaluateExpressionFixed(displayAmount)
            }
        } catch (e: Exception) {
            "0"
        }
    }

    // Notify parent about the calculated result
    LaunchedEffect(calculatedResultAmount) {
        onResultAmount(calculatedResultAmount)
    }

    // Dynamic font size calculation based on amount length
    val dynamicFontSize = remember(calculatedResultAmount) {
        calculateDynamicFontSize(calculatedResultAmount)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Display the calculated result with dynamic font size
        Text(
            text = "$currencySymbol $calculatedResultAmount",
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold,
            fontSize = dynamicFontSize.sp,
            lineHeight = (dynamicFontSize + 5).sp,
            maxLines = 5,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        )

        // Show the input expression when using special keys
        AnimatedVisibility(displayAmount.any { it in specialKeys }) {
            OutlinedTextField(
                value = displayAmount,
                onValueChange = {}, // Value changes are handled by the number pad
                readOnly = true,
                singleLine = false,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceBright,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceBright,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                shape = RoundedCornerShape(15.dp),
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    textAlign = TextAlign.End,
                    fontWeight = FontWeight.Medium,
                    fontFamily = iosFont,
                    color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.5f)
                )
            )
        }

        Spacer(modifier = Modifier.height(5.dp))

        AnimatedVisibility(
            visible = showTransactionTypeSelection
        ) {
            Column (
                modifier = Modifier.fillMaxWidth()
            ){
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TransactionTypeInfoIcon(
                        isTransactionTypeInfoSheetOpen = isTransactionTypeInfoSheetOpen,
                        onIconClick = {
                            onAddTransactionEvent(
                                AddTransactionEvent.SetTransactionTypeInfoSheetOpen(true)
                            )
                        },
                        onSheetDismiss = {
                            onAddTransactionEvent(
                                AddTransactionEvent.SetTransactionTypeInfoSheetOpen(false)
                            )
                        },
                        onTransactionTypeSelected = { selectedType ->
                            onAddTransactionEvent(AddTransactionEvent.UpdateType(selectedType))

                            val isRecurringType =
                                selectedType == TransactionType.SUBSCRIPTION || selectedType == TransactionType.REPETITIVE

                            onAddTransactionEvent(
                                AddTransactionEvent.SetTransactionTypeMenuOpened(isRecurringType)
                            )
                            onAddTransactionEvent(
                                AddTransactionEvent.SetRecurrenceMenuOpen(isRecurringType)
                            )
                        },
                        transactionTypeListItemsColor = transactionTypeListItemsColor,
                        sheetWidth = usedScreenWidth,
                        sheetHeight = usedScreenHeight,
                        sheetState = transactionTypeSheetState,
                        currentTransactionMode = currentTransactionMode
                    )

                    Box {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val availableTransactionTypes = getAvailableTransactionTypes(currentTransactionMode)

                            availableTransactionTypes.forEachIndexed { index, type ->
                                item {
                                    val originalIndex = TransactionType.entries.indexOf(type)
                                    Button(
                                        onClick = {
                                            onAddTransactionEvent(AddTransactionEvent.UpdateType(type))

                                            val isRecurringType =
                                                type == TransactionType.SUBSCRIPTION || type == TransactionType.REPETITIVE

                                            onAddTransactionEvent(
                                                AddTransactionEvent.SetTransactionTypeMenuOpened(isRecurringType)
                                            )
                                            onAddTransactionEvent(
                                                AddTransactionEvent.SetRecurrenceMenuOpen(isRecurringType)
                                            )
                                        },
                                        modifier = Modifier
                                            .defaultMinSize(minHeight = 20.dp)
                                            .padding(horizontal = 5.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = transactionTypeListItemsColor[originalIndex].copy(0.1f),
                                            contentColor = transactionTypeListItemsColor[originalIndex]
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(
                                            text = type.toString()
                                                .lowercase()
                                                .replaceFirstChar { it.uppercase() },
                                            fontFamily = iosFont,
                                            fontSize = 12.sp
                                        )
                                    }
                                    if (index != availableTransactionTypes.lastIndex) {
                                        Spacer(modifier = Modifier.size(5.dp))
                                    }
                                }
                            }
                        }
                        Spacer(
                            modifier = Modifier
                                .height(56.dp)
                                .width(20.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            themeColors.background,
                                            Color.Transparent
                                        )
                                    )
                                )
                                .align(Alignment.TopStart)
                        )
                        Spacer(
                            modifier = Modifier
                                .height(56.dp)
                                .width(20.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            themeColors.background
                                        )
                                    )
                                )
                                .align(Alignment.TopEnd)
                        )
                    }
                }
                AnimatedVisibility(visible = recurrenceMenuOpened) {
                    RecurrenceSelector(
                        frequency = transactionUiState.recurrenceFrequency,
                        interval = transactionUiState.recurrenceInterval,
                        endDateState = endDatePickerState,
                        isRecurrenceBottomSheetOpen = isRecurrenceBottomSheetOpen,
                        isCustomInputBottomSheetOpen = isCustomInputBottomSheetOpen,
                        isEndDateSelected = isEndDateSelected,
                        openEndDatePicker = openEndDatePicker,
                        frequencySheetState = frequencySheetState,
                        intervalSheetState = intervalSheetState,
                        transactionUiState = transactionUiState,
                        onAddTransactionEvent = onAddTransactionEvent
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        // Custom Number Pad
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val rows = listOf(
                listOf("AC", "()", "%", "÷"),
                listOf("1", "2", "3", "×"),
                listOf("4", "5", "6", "-"),
                listOf("7", "8", "9", "+"),
                listOf(".", "0", "00", "⌫")
            )

            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { key ->
                        Button(
                            onClick = {
                                when (key) {
                                    "()" -> {
                                        val openCount = displayAmount.count { it == '(' }
                                        val closeCount = displayAmount.count { it == ')' }

                                        if (openCount > closeCount) {
                                            onValueChange("$displayAmount)")
                                        } else {
                                            onValueChange("$displayAmount(")
                                        }
                                    }
                                    "⌫" -> {
                                        val updatedValue = if (displayAmount.isNotEmpty()) displayAmount.dropLast(1) else ""
                                        onValueChange(updatedValue)
                                    }
                                    "AC" -> onValueChange("")
                                    "." -> {
                                        // Allow decimal point if not already present in current number
                                        if (!getCurrentNumber(displayAmount).contains(".")) {
                                            onValueChange("$displayAmount.")
                                        }
                                    }
                                    else -> {
                                        val newValue = displayAmount + key
                                        // Validate the new value for reasonable limits
                                        val validatedValue = validateAmountInput(newValue)
                                        onValueChange(validatedValue)
                                    }
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .size(65.dp)
                                .padding(5.dp)
                                .then(
                                    when (key) {
                                        "AC" -> Modifier.shadow(10.dp, RoundedCornerShape(15.dp))
                                        "⌫" -> Modifier.shadow(
                                            10.dp,
                                            RoundedCornerShape(15.dp),
                                            spotColor = MaterialTheme.colorScheme.onError,
                                            ambientColor = MaterialTheme.colorScheme.onError
                                        )
                                        else -> Modifier
                                    }
                                ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when (key) {
                                    "AC" -> Color.Black
                                    "⌫" -> MaterialTheme.colorScheme.onError
                                    "%", "()", "+", "-", "÷", "×" -> MaterialTheme.colorScheme.surfaceBright
                                    else -> MaterialTheme.colorScheme.surface
                                },
                                contentColor = when (key) {
                                    "AC" -> Color.White
                                    "⌫" -> Color.White
                                    else -> MaterialTheme.colorScheme.inverseSurface
                                }
                            ),
                            shape = RoundedCornerShape(15.dp)
                        ) {
                            Text(
                                text = key,
                                fontFamily = iosFont,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }
        }
    }
}

// Fixed evaluation function that preserves decimals
fun evaluateExpressionFixed(expression: String): String {
    return try {
        // If the input is just a number (no operators), return it as-is if it has decimals
        if (!expression.any { it in "+-×÷*/()" }) {
            val num = expression.toDoubleOrNull()
            return if (num != null) {
                formatResultFixed(num, expression.endsWith(".") || expression.contains("."))
            } else {
                "0"
            }
        }

        // Preprocess the expression to remove trailing operators
        val cleanExpression = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("(", "*(")
            .replace(")", ")*")
            .replace(")(", ")*(")
            .trimEnd { it in "+-*/" }

        // If the cleaned expression is empty, return "0"
        if (cleanExpression.isEmpty()) return "0"

        // Evaluate the cleaned expression
        val result = Expression(cleanExpression).calculate()
        if (result.isNaN()) "Error" else formatResultFixed(result, false)
    } catch (e: Exception) {
        "Error"
    }
}

// Fixed format function that preserves decimals appropriately
fun formatResultFixed(result: Double, preserveDecimal: Boolean = false): String {
    return when {
        preserveDecimal && result == result.toLong().toDouble() -> {
            // If we need to preserve decimal and it's a whole number, show .0
            "${result.toLong()}.0"
        }
        result == result.toLong().toDouble() -> {
            // If it's a whole number and we don't need to preserve decimal
            result.toLong().toString()
        }
        else -> {
            // For decimal numbers, format appropriately
            val formatted = result.toString()
            if (formatted.length > 10) {
                // For very long decimals, round to reasonable precision
                String.format("%.6f", result).trimEnd('0').trimEnd('.')
            } else {
                formatted
            }
        }
    }
}

// Helper function to get current number being typed (after last operator)
fun getCurrentNumber(input: String): String {
    val operators = "+-×÷*/()"
    var lastOperatorIndex = -1

    for (i in input.length - 1 downTo 0) {
        if (input[i] in operators) {
            lastOperatorIndex = i
            break
        }
    }

    return input.substring(lastOperatorIndex + 1)
}

// Validate amount input for reasonable limits
fun validateAmountInput(input: String): String {
    return try {
        // Allow the input as-is if it's reasonable
        if (input.length > 20) {
            // Limit input length to prevent extremely long numbers
            input.take(20)
        } else {
            input
        }
    } catch (e: Exception) {
        input
    }
}

// Calculate dynamic font size based on amount length
fun calculateDynamicFontSize(amount: String): Int {
    val length = amount.length
    return when {
        length <= 5 -> 40      // Small amounts: large font
        length <= 8 -> 35      // Medium amounts: medium-large font
        length <= 10 -> 30     // Longer amounts: medium font
        length <= 12 -> 25     // Long amounts: smaller font
        length <= 15 -> 22     // Very long amounts: small font
        else -> 18             // Extremely long amounts: very small font
    }
}

// Fixed CustomNumberPadOutlinedTextField for interval input
@Composable
fun CustomNumberPadOutlinedTextField(
    intervalInput: String,
    onValueChange: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // OutlinedTextField with custom interaction
        OutlinedTextField(
            value = if(intervalInput == "0") "" else intervalInput,
            onValueChange = {}, // Value changes are handled by the number pad
            readOnly = true,
            placeholder = {
                Text(
                    text = "1",
                    style = TextStyle(
                        fontSize = 35.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        fontFamily = iosFont,
                        color = MaterialTheme.colorScheme.inverseSurface.copy(alpha = 0.5f),
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            ),
            shape = RoundedCornerShape(15.dp),
            textStyle = TextStyle(
                fontSize = 35.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                fontFamily = iosFont,
                color = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Custom Number Pad
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("Clear", "0", "⌫")
            )

            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { key ->
                        Button(
                            onClick = {
                                val newValue = when (key) {
                                    "⌫" -> {
                                        if (intervalInput.isNotEmpty()) intervalInput.dropLast(1) else ""
                                    }
                                    "Clear" -> ""
                                    else -> intervalInput + key
                                }

                                // Validate the new value - removed Int.MAX_VALUE limitation
                                val validatedValue = if (newValue.isNotEmpty()) {
                                    try {
                                        val numericValue = newValue.toLongOrNull() ?: 0L
                                        // Set a reasonable limit for intervals (e.g., 999999)
                                        if (numericValue > 999999) {
                                            "999999"
                                        } else {
                                            newValue
                                        }
                                    } catch (e: Exception) {
                                        intervalInput // Keep the old value if parsing fails
                                    }
                                } else {
                                    ""
                                }

                                onValueChange(validatedValue)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when (key) {
                                    "Clear" -> Color.Black
                                    "⌫" -> MaterialTheme.colorScheme.onError
                                    else -> MaterialTheme.colorScheme.surface
                                },
                                contentColor = when (key) {
                                    "Clear" -> Color.White
                                    "⌫" -> Color.White
                                    else -> MaterialTheme.colorScheme.inverseSurface
                                }
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .size(65.dp)
                                .padding(5.dp)
                                .then(
                                    when (key) {
                                        "Clear" -> Modifier.shadow(10.dp, RoundedCornerShape(15.dp))
                                        "⌫" -> Modifier.shadow(
                                            10.dp,
                                            RoundedCornerShape(15.dp),
                                            spotColor = MaterialTheme.colorScheme.onError,
                                            ambientColor = MaterialTheme.colorScheme.onError
                                        )
                                        else -> Modifier
                                    }
                                ),
                            shape = RoundedCornerShape(15.dp)
                        ) {
                            Text(
                                text = key,
                                fontFamily = iosFont,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Returns the available transaction types based on the current transaction mode
 *
 * @param transactionMode The current transaction mode ("Expense", "Income", "Transfer")
 * @return List of available TransactionType for the given mode
 */
private fun getAvailableTransactionTypes(transactionMode: String): List<TransactionType> {
    return when (transactionMode) {
        "Expense" -> listOf(
            TransactionType.DEFAULT,
            TransactionType.UPCOMING,
            TransactionType.SUBSCRIPTION,
            TransactionType.REPETITIVE,
            TransactionType.LENT  // Only available in Expense mode
        )
        "Income" -> listOf(
            TransactionType.DEFAULT,
            TransactionType.UPCOMING,
            TransactionType.SUBSCRIPTION,
            TransactionType.REPETITIVE,
            TransactionType.BORROWED  // Only available in Income mode
        )
        "Transfer" -> listOf(
            TransactionType.DEFAULT,
            TransactionType.UPCOMING,
            TransactionType.SUBSCRIPTION,
            TransactionType.REPETITIVE
            // No LENT or BORROWED for transfers
        )
        else -> listOf(
            TransactionType.DEFAULT,
            TransactionType.UPCOMING,
            TransactionType.SUBSCRIPTION,
            TransactionType.REPETITIVE
        )
    }
}

