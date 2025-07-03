package com.ritesh.cashiro.presentation.ui.features.currency

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults.DragHandle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritesh.cashiro.R
import com.ritesh.cashiro.data.currency.model.Currency
import com.ritesh.cashiro.data.currency.model.CurrencyConversion
import com.ritesh.cashiro.presentation.effects.BlurredAnimatedVisibility
import com.ritesh.cashiro.presentation.ui.extras.components.extras.SearchBar
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenEvent
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenViewModel
import com.ritesh.cashiro.presentation.ui.theme.iosFont
import kotlinx.coroutines.launch
import kotlin.text.contains
import kotlin.text.uppercase

//@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
//@Composable
//fun CurrencyBottomSheet(
//    accountViewModel: AccountScreenViewModel,
//    currencyViewModel: CurrencyViewModel,
//    currencyCodeState: MutableState<String>,
//    sheetState: SheetState,
//    onDismiss: () -> Unit,
//    usedScreenWidth: Dp,
//    usedScreenHeight: Dp,
//    accountId: Int,
//    onCurrencyCardClick: () -> Unit = {}
//) {
//    val uiState by currencyViewModel.uiState.collectAsState()
//    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
//    var showAllCurrencies by rememberSaveable { mutableStateOf(false) }
//    var showExchangeRateInfo by rememberSaveable { mutableStateOf(false) }
//    var showExchangeRateSheet by rememberSaveable { mutableStateOf(false) }
//    val exchangeRatesSheetState = rememberModalBottomSheetState(skipPartiallyExpanded= true, confirmValueChange = { true })
//
//    ModalBottomSheet(
//        sheetState = sheetState,
//        onDismissRequest = onDismiss,
//        sheetMaxWidth = usedScreenWidth - 10.dp,
//        containerColor = MaterialTheme.colorScheme.background,
//        dragHandle = {
//            DragHandle(
//                color = MaterialTheme.colorScheme.inverseSurface.copy(0.3f)
//            )
//        },
//        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
//    ) {
//        Box(modifier = Modifier
//            .fillMaxWidth()
//            .height(usedScreenHeight)
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 16.dp)
//                    .verticalScroll(rememberScrollState())
//            ) {
//                Text(
//                    text = "Currencies",
//                    textAlign = TextAlign.Center,
//                    fontFamily = iosFont,
//                    fontWeight = FontWeight.Bold,
//                    color = MaterialTheme.colorScheme.onSurface,
//                    modifier = Modifier
//                        .padding(bottom = 16.dp)
//                        .fillMaxWidth()
//                )
//
//                Row(modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceBetween,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//
//                    SearchBar(
//                        searchQuery = searchQuery,
//                        onSearchQueryChange = { searchQuery = it },
//                        trailingIcon = {
//                            Icon(
//                                painter = painterResource(R.drawable.information_bulk),
//                                contentDescription = "warning",
//                                tint = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
//                                modifier = Modifier.size(24.dp).clickable(
//                                    onClick = { showExchangeRateInfo = true }
//                                )
//                            )
//                        },
//                        modifier = Modifier.padding(10.dp).fillMaxWidth().weight(1f),
//                        label = "SEARCH"
//                    )
//
//
//
//                }
//
//                // Filter currencies based on search query
//                val filteredCurrencies = uiState.currencies.filter {
//                    it.code.contains(searchQuery.text, ignoreCase = true) ||
//                            it.name.contains(searchQuery.text, ignoreCase = true) ||
//                            it.symbol.contains(searchQuery.text, ignoreCase = true)
//                }
//
//                if (filteredCurrencies.isEmpty()) {
//                    Text(
//                        text = "Currency not available",
//                        style = MaterialTheme.typography.bodyLarge,
//                        modifier = Modifier.padding(vertical = 16.dp)
//                    )
//                } else {
//                    // Display currencies in a FlowRow
//                    val displayedCurrencies = if (showAllCurrencies) {
//                        filteredCurrencies
//                    } else {
//                        // Show top 15 most popular currencies
//                        val popularCurrencyCodes = listOf("usd", "eur", "gbp", "jpy", "aud", "cad", "chf", "cny", "inr", "sek", "nzd", "rub", "mxn", "nok", "krw")
//                        filteredCurrencies.filter { currency ->
//                            popularCurrencyCodes.contains(currency.code)
//                        }.take(15)
//                    }
//
//                    val scope = rememberCoroutineScope()
//                    FlowRow(
//                        modifier = Modifier.fillMaxWidth(),
//                        maxItemsInEachRow = 3,
//                        horizontalArrangement = Arrangement.SpaceEvenly,
//                        verticalArrangement = Arrangement.Center,
//                    ) {
//                        displayedCurrencies.forEach { currency ->
//                            CurrencyCard(
//                                currency = currency,
//                                onCurrencyCardClick = {
//                                    scope.launch { sheetState.hide() }
//                                        .invokeOnCompletion {
//                                            if (!sheetState.isVisible) {
//                                                currencyCodeState.value = currency.code
//                                                currencyViewModel.selectCurrency(currency.code)
//                                                accountViewModel.updateAccountCurrency(accountId,currency.code)
//                                                accountViewModel.onEvent(AccountScreenEvent.UpdateCurrencyCode(currency.code))
//                                                onCurrencyCardClick()
//                                            }
//                                        }
//                                }
//                            )
//                        }
//                    }
//
//                    // View All Currencies button
//                    if (!showAllCurrencies && filteredCurrencies.size > 15) {
//                        Box(modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(top = 16.dp),
//                            contentAlignment = Alignment.Center
//                        ){
//                            TextButton(
//                                onClick = { showAllCurrencies = true },
//                                colors = ButtonDefaults.buttonColors(
//                                    containerColor = Color.Transparent,
//                                    contentColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f)
//                                ),
//                            ) {
//                                Text(
//                                    text = "View All Currencies",
//                                    fontFamily = iosFont,
//                                    fontSize = 12.sp,
//                                    modifier = Modifier
//                                        .background(
//                                            color = MaterialTheme.colorScheme.surface,
//                                            shape = RoundedCornerShape(15.dp)
//                                        )
//                                        .padding(horizontal = 10.dp, vertical = 5.dp)
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//            BlurredAnimatedVisibility (
//                visible = showExchangeRateInfo,
//                enter= fadeIn() + scaleIn(
//                    animationSpec = tween(durationMillis = 300),
//                    initialScale = 0f,)  ,
//                exit = fadeOut() + scaleOut(
//                    animationSpec = tween(durationMillis = 100),
//                    targetScale = 0f,) ,
//
//                ) {
//                // Dialog Box
//                Box(modifier = Modifier.fillMaxSize().clickable(
//                    interactionSource = remember { MutableInteractionSource() },
//                    indication = null,
//                    onClick = { showExchangeRateInfo = false }
//                ),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Box(
//                        modifier = Modifier
//                            .shadow(elevation = 25.dp, RoundedCornerShape(15.dp), clip = true,
//                                spotColor = Color.Black,
//                                ambientColor = Color.Black)
//                            .width(usedScreenWidth - 60.dp)
//                            .clip(RoundedCornerShape(15.dp))
//                            .background(
//                                MaterialTheme.colorScheme.surfaceBright,
//                                RoundedCornerShape(15.dp)
//                            ).clickable(onClick = {})
//                    ) {
//                        Column(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(15.dp),
//                            horizontalAlignment = Alignment.CenterHorizontally,
//                            verticalArrangement = Arrangement.spacedBy(16.dp)
//                        ) {
//                            Icon(
//                                painter =  painterResource(R.drawable.information_bulk),
//                                contentDescription = "warning",
//                                tint = MaterialTheme.colorScheme.primary.copy(0.5f),
//                                modifier = Modifier.size(50.dp)
//                            )
//                            Text(
//                                text = "Exchange Rates Notice",
//                                textAlign = TextAlign.Center,
//                                fontSize = 18.sp,
//                                fontWeight = FontWeight.Bold,
//                                fontFamily = iosFont,
//                                color = MaterialTheme.colorScheme.inverseSurface,
//                                modifier = Modifier.fillMaxWidth()
//                            )
//                            Text(
//                                text = "The exchange rates displayed within this app are for informational purpose only and should not be used for investment decisions. " +
//                                        "These rates are estimates and may not reflect actual rates. " +
//                                        "By using this app you acknowledge that you understand and accept these limitations and that you assume full responsibility for any decisions made based on the information provided within the app ",
//                                textAlign = TextAlign.Center,
//                                fontSize = 12.sp,
//                                fontFamily = iosFont,
//                                color = MaterialTheme.colorScheme.inverseSurface.copy(0.8f),
//                                modifier = Modifier.fillMaxWidth()
//                            )
//                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
//                                Button(
//                                    onClick = { showExchangeRateInfo = false },
//                                    colors = ButtonDefaults.buttonColors(
//                                        containerColor = MaterialTheme.colorScheme.surface,
//                                        contentColor = MaterialTheme.colorScheme.inverseSurface
//                                    ),
//                                ) {
//                                    Text(
//                                        text = "Ok",
//                                        textAlign = TextAlign.Center,
//                                        fontFamily = iosFont,
//                                        modifier = Modifier
//                                    )
//                                }
//                                Button(
//                                    onClick = {showExchangeRateSheet = true
//                                        showExchangeRateInfo = false},
//                                    colors = ButtonDefaults.buttonColors(
//                                        containerColor = MaterialTheme.colorScheme.primary,
//                                        contentColor = Color.White
//                                    ),
//                                ) {
//                                    Text(
//                                        text = "Exchange Rates",
//                                        textAlign = TextAlign.Center,
//                                        fontFamily = iosFont,
//                                        color = Color.White,
//                                        modifier = Modifier
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//
//            if (showExchangeRateSheet) {
//                ExchangeRatesBottomSheet(
//                    currencyViewModel = currencyViewModel,
//                    onDismiss = { showExchangeRateSheet = false },
//                    exchangeSheetState =  exchangeRatesSheetState,
//                    usedScreenWidth = usedScreenWidth,
//                    usedScreenHeight = usedScreenHeight - 10.dp
//                )
//            }
//        }
//
//    }
//}
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CurrencyBottomSheet(
    onAccountEvent: (AccountScreenEvent) -> Unit,
    updateAccountCurrency: (Int, String) -> Unit,
    currencyUiState: CurrencyViewModel.CurrencyUiState,
    selectCurrency: (String) -> Unit,
    currencyCodeState: MutableState<String>,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    usedScreenWidth: Dp,
    usedScreenHeight: Dp,
    accountId: Int,
    onCurrencyCardClick: () -> Unit = {},
    onCurrencySelected: ((String) -> Unit)? = null // Add callback parameter
) {
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    var showAllCurrencies by rememberSaveable { mutableStateOf(false) }
    var showExchangeRateInfo by rememberSaveable { mutableStateOf(false) }
    var showExchangeRateSheet by rememberSaveable { mutableStateOf(false) }
    val exchangeRatesSheetState = rememberModalBottomSheetState(skipPartiallyExpanded= true, confirmValueChange = { true })

    ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = onDismiss,
        sheetMaxWidth = usedScreenWidth - 10.dp,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = {
            DragHandle(
                color = MaterialTheme.colorScheme.inverseSurface.copy(0.3f)
            )
        },
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(usedScreenHeight)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Currencies",
                    textAlign = TextAlign.Center,
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SearchBar(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        trailingIcon = {
                            Icon(
                                painter = painterResource(R.drawable.information_bulk),
                                contentDescription = "warning",
                                tint = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                                modifier = Modifier.size(24.dp).clickable(
                                    onClick = { showExchangeRateInfo = true }
                                )
                            )
                        },
                        modifier = Modifier.padding(10.dp).fillMaxWidth().weight(1f),
                        label = "SEARCH"
                    )
                }

                // Filter currencies based on search query
                val filteredCurrencies = currencyUiState.currencies.filter {
                    it.code.contains(searchQuery.text, ignoreCase = true) ||
                            it.name.contains(searchQuery.text, ignoreCase = true) ||
                            it.symbol.contains(searchQuery.text, ignoreCase = true)
                }

                if (filteredCurrencies.isEmpty()) {
                    Text(
                        text = "Currency not available",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    // Display currencies in a FlowRow
                    val displayedCurrencies = if (showAllCurrencies) {
                        filteredCurrencies
                    } else {
                        // Show top 15 most popular currencies
                        val popularCurrencyCodes = listOf("usd", "eur", "gbp", "jpy", "aud", "cad", "chf", "cny", "inr", "sek", "nzd", "rub", "mxn", "nok", "krw")
                        filteredCurrencies.filter { currency ->
                            popularCurrencyCodes.contains(currency.code)
                        }.take(15)
                    }

                    val scope = rememberCoroutineScope()
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        maxItemsInEachRow = 3,
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        displayedCurrencies.forEach { currency ->
                            CurrencyCard(
                                currency = currency,
                                onCurrencyCardClick = {
                                    scope.launch { sheetState.hide() }
                                        .invokeOnCompletion {
                                            if (!sheetState.isVisible) {
                                                // Use callback if provided, otherwise use default behavior
                                                if (onCurrencySelected != null) {
                                                    onCurrencySelected(currency.code)
                                                } else {
                                                    currencyCodeState.value = currency.code
                                                    selectCurrency(currency.code)
                                                    updateAccountCurrency(accountId, currency.code)
                                                    onAccountEvent(AccountScreenEvent.UpdateCurrencyCode(currency.code))
                                                }
                                                onCurrencyCardClick()
                                            }
                                        }
                                }
                            )
                        }
                    }

                    // View All Currencies button
                    if (!showAllCurrencies && filteredCurrencies.size > 15) {
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                            contentAlignment = Alignment.Center
                        ){
                            TextButton(
                                onClick = { showAllCurrencies = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = MaterialTheme.colorScheme.inverseSurface.copy(0.5f)
                                ),
                            ) {
                                Text(
                                    text = "View All Currencies",
                                    fontFamily = iosFont,
                                    fontSize = 12.sp,
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(15.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }
            BlurredAnimatedVisibility (
                visible = showExchangeRateInfo,
                enter= fadeIn() + scaleIn(
                    animationSpec = tween(durationMillis = 300),
                    initialScale = 0f,)  ,
                exit = fadeOut() + scaleOut(
                    animationSpec = tween(durationMillis = 100),
                    targetScale = 0f,) ,

                ) {
                // Dialog Box
                Box(modifier = Modifier.fillMaxSize().clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { showExchangeRateInfo = false }
                ),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .shadow(elevation = 25.dp, RoundedCornerShape(15.dp), clip = true,
                                spotColor = Color.Black,
                                ambientColor = Color.Black)
                            .width(usedScreenWidth - 60.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(
                                MaterialTheme.colorScheme.surfaceBright,
                                RoundedCornerShape(15.dp)
                            ).clickable(onClick = {})
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(15.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                painter =  painterResource(R.drawable.information_bulk),
                                contentDescription = "warning",
                                tint = MaterialTheme.colorScheme.primary.copy(0.5f),
                                modifier = Modifier.size(50.dp)
                            )
                            Text(
                                text = "Exchange Rates Notice",
                                textAlign = TextAlign.Center,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = iosFont,
                                color = MaterialTheme.colorScheme.inverseSurface,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "The exchange rates displayed within this app are for informational purpose only and should not be used for investment decisions. " +
                                        "These rates are estimates and may not reflect actual rates. " +
                                        "By using this app you acknowledge that you understand and accept these limitations and that you assume full responsibility for any decisions made based on the information provided within the app ",
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp,
                                fontFamily = iosFont,
                                color = MaterialTheme.colorScheme.inverseSurface.copy(0.8f),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { showExchangeRateInfo = false },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        contentColor = MaterialTheme.colorScheme.inverseSurface
                                    ),
                                ) {
                                    Text(
                                        text = "Ok",
                                        textAlign = TextAlign.Center,
                                        fontFamily = iosFont,
                                        modifier = Modifier
                                    )
                                }
                                Button(
                                    onClick = {showExchangeRateSheet = true
                                        showExchangeRateInfo = false},
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = Color.White
                                    ),
                                ) {
                                    Text(
                                        text = "Exchange Rates",
                                        textAlign = TextAlign.Center,
                                        fontFamily = iosFont,
                                        color = Color.White,
                                        modifier = Modifier
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (showExchangeRateSheet) {
                ExchangeRatesBottomSheet(
                    currencyUiState = currencyUiState,
                    onDismiss = { showExchangeRateSheet = false },
                    exchangeSheetState = exchangeRatesSheetState,
                    usedScreenWidth = usedScreenWidth,
                    usedScreenHeight = usedScreenHeight - 10.dp,
                )
            }
        }
    }
}

@Composable
fun CurrencyCard(
    currency: Currency,
    onCurrencyCardClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .sizeIn(minWidth = 90.dp, minHeight = 70.dp, maxHeight = 90.dp, maxWidth = 110.dp)
            .clip(RoundedCornerShape(15.dp))
            .clickable(onClick = onCurrencyCardClick)
            .padding(5.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.inverseSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (currency.symbol.isEmpty()) "" else currency.code.uppercase(),
                lineHeight = 12.sp,
                fontSize = 12.sp,
                fontFamily = iosFont,
                color = MaterialTheme.colorScheme.inverseSurface.copy(0.5f)
            )
            Text(
                text = currency.symbol,
                fontFamily = iosFont,
                lineHeight = 20.sp,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.inverseSurface,
                modifier = Modifier.padding(vertical = 5.dp)
            )
            Text(
                text = currency.name,
                fontFamily = iosFont,
                lineHeight = 10.sp,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .basicMarquee(
                        iterations = Int.MAX_VALUE,
                        animationMode = MarqueeAnimationMode.Immediately,
                        initialDelayMillis = 1000,
                        velocity = 30.dp
                    )
            )
        }
    }
}

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ExchangeRatesBottomSheet(
//    currencyViewModel: CurrencyViewModel,
//    exchangeSheetState: SheetState,
//    onDismiss: () -> Unit,
//    usedScreenWidth: Dp,
//    usedScreenHeight: Dp,
//) {
//    val uiState by currencyViewModel.uiState.collectAsState()
//    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
//    val filteredConversions = uiState.conversions.filter {
//        it.currencyCode.contains(searchQuery.text, ignoreCase = true) ||
//                it.symbol.contains(searchQuery.text, ignoreCase = true)
//    }
//
//    ModalBottomSheet(
//        sheetState = exchangeSheetState,
//        onDismissRequest = onDismiss,
//        sheetMaxWidth = usedScreenWidth - 5.dp,
//        containerColor = MaterialTheme.colorScheme.background,
//        dragHandle = {
//            DragHandle(
//                color = MaterialTheme.colorScheme.inverseSurface.copy(0.3f)
//            )
//        },
//        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
//    ) {
//        Box(modifier = Modifier
//            .fillMaxWidth()
//            .height(usedScreenHeight)
//        ) {
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(horizontal = 16.dp)
//            ) {
//                Text(
//                    text = "Exchange Rates",
//                    textAlign = TextAlign.Center,
//                    fontFamily = iosFont,
//                    fontWeight = FontWeight.Bold,
//                    color = MaterialTheme.colorScheme.onSurface,
//                    modifier = Modifier
//                        .padding(bottom = 16.dp)
//                        .fillMaxWidth()
//                )
//
//                SearchBar(
//                    searchQuery = searchQuery,
//                    onSearchQueryChange = { searchQuery = it },
//                    label = "SEARCH",
//                    modifier = Modifier.padding(bottom = 10.dp)
//                )
//
//                // Exchange rates list
//                if (uiState.isLoadingConversions) {
//                    CircularProgressIndicator(
//                        modifier = Modifier.align(Alignment.CenterHorizontally)
//                    )
//                } else if (uiState.conversionError != null) {
//                    Text(
//                        text = uiState.conversionError!!,
//                        color = MaterialTheme.colorScheme.error,
//                        modifier = Modifier.padding(vertical = 16.dp)
//                    )
//                } else {
//                    if(filteredConversions.isEmpty() && searchQuery.text.isNotEmpty()) {
//                        Text(
//                            text = "No matching currencies found",
//                            textAlign = TextAlign.Center,
//                            color = MaterialTheme.colorScheme.onSurfaceVariant,
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(vertical = 16.dp)
//                        )
//                    } else {
//                        AnimatedContent(
//                            targetState = filteredConversions,
//                            transitionSpec = {
//                                (fadeIn(animationSpec = tween(220, delayMillis = 90)) +
//                                        scaleIn(initialScale = 0.92f, animationSpec = tween(220, delayMillis = 90)))
//                                    .togetherWith(fadeOut(animationSpec = tween(90)))
//                            }, label = "Filtered Icon animated"
//                        ) { filteredConversions ->
//                            LazyColumn(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .background(MaterialTheme.colorScheme.background)
//                                    .clip(RoundedCornerShape(15.dp))
//                            ) {
//                                item{
//                                    Text(
//                                        text = "All exchange rate calculations use USD as a reference",
//                                        fontFamily = iosFont,
//                                        textAlign = TextAlign.Center,
//                                        fontSize = 12.sp,
//                                        color = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
//                                        modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp)
//                                    )
//
//                                }
//                                itemsIndexed(filteredConversions) { index, conversion ->
//                                    val isFirstItem = index == 0
//                                    val isLastItem = index == filteredConversions.size - 1
//
//                                    ExchangeRateItem(
//                                        conversion = conversion,
//                                        baseCurrency = uiState.selectedCurrency,
//                                        isFirstItem = isFirstItem,
//                                        isLastItem = isLastItem
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExchangeRatesBottomSheet(
    currencyUiState: CurrencyViewModel.CurrencyUiState,
    exchangeSheetState: SheetState,
    onDismiss: () -> Unit,
    usedScreenWidth: Dp,
    usedScreenHeight: Dp,
) {
    var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
    val filteredConversions = currencyUiState.conversions.filter {
        it.currencyCode.contains(searchQuery.text, ignoreCase = true) ||
                it.symbol.contains(searchQuery.text, ignoreCase = true)
    }

    ModalBottomSheet(
        sheetState = exchangeSheetState,
        onDismissRequest = onDismiss,
        sheetMaxWidth = usedScreenWidth - 5.dp,
        containerColor = MaterialTheme.colorScheme.background,
        dragHandle = {
            DragHandle(
                color = MaterialTheme.colorScheme.inverseSurface.copy(0.3f)
            )
        },
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
    ) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(usedScreenHeight)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Exchange Rates",
                    textAlign = TextAlign.Center,
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .fillMaxWidth()
                )

                SearchBar(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    label = "SEARCH",
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                // Exchange rates list
                if (currencyUiState.isLoadingConversions) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else if (currencyUiState.conversionError != null) {
                    Text(
                        text = currencyUiState.conversionError!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    if(filteredConversions.isEmpty() && searchQuery.text.isNotEmpty()) {
                        Text(
                            text = "No matching currencies found",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        )
                    } else {
                        AnimatedContent(
                            targetState = filteredConversions,
                            transitionSpec = {
                                (fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                                        scaleIn(initialScale = 0.92f, animationSpec = tween(220, delayMillis = 90)))
                                    .togetherWith(fadeOut(animationSpec = tween(90)))
                            }, label = "Filtered Icon animated"
                        ) { filteredConversions ->
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background)
                                    .clip(RoundedCornerShape(15.dp))
                            ) {
                                item{
                                    Text(
                                        text = "All exchange rate calculations use USD as a reference",
                                        fontFamily = iosFont,
                                        textAlign = TextAlign.Center,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp)
                                    )

                                }
                                itemsIndexed(filteredConversions) { index, conversion ->
                                    val isFirstItem = index == 0
                                    val isLastItem = index == filteredConversions.size - 1

                                    ExchangeRateItem(
                                        conversion = conversion,
                                        baseCurrency = currencyUiState.selectedCurrency,
                                        isFirstItem = isFirstItem,
                                        isLastItem = isLastItem
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExchangeRateItem(
    conversion: CurrencyConversion,
    baseCurrency: Currency?,
    isFirstItem: Boolean = false,
    isLastItem: Boolean = false
) {
    val baseSymbol = baseCurrency?.symbol ?: ""
    val targetSymbol = conversion.symbol

    // Define the corner shape based on position
    val cornerShape = when {
        isFirstItem -> RoundedCornerShape(
            topStart = 15.dp,
            topEnd = 15.dp,
            bottomStart = 5.dp,
            bottomEnd = 5.dp
        )
        isLastItem -> RoundedCornerShape(
            topStart = 5.dp,
            topEnd = 5.dp,
            bottomStart = 15.dp,
            bottomEnd = 15.dp
        )
        else -> RoundedCornerShape(5.dp) // Middle items have small rounded corners
    }

    ListItem(
        headlineContent = {
            Text(
                text = "${conversion.currencyCode} ($targetSymbol)",
                fontFamily = iosFont,
                color = MaterialTheme.colorScheme.inverseSurface,
                fontWeight = FontWeight.Bold
            )
        },
        supportingContent = {
            Text(
                text = "1 $baseSymbol = ${conversion.displayRate} $targetSymbol",
                fontSize = 12.sp,
                fontFamily = iosFont,
                color = MaterialTheme.colorScheme.inverseSurface.copy(0.5f)
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier
            .padding(bottom = 1.dp)
            .clip(cornerShape)
    )
}