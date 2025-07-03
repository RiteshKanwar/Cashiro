package com.ritesh.cashiro.presentation.ui.features.home

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.ritesh.cashiro.R
import com.ritesh.cashiro.data.local.entity.AccountEntity
import com.ritesh.cashiro.data.local.entity.TransactionEntity
import com.ritesh.cashiro.domain.repository.AccountTransactionStats
import com.ritesh.cashiro.presentation.ui.features.currency.CurrencyViewModel
import com.ritesh.cashiro.presentation.effects.BlurredAnimatedVisibility
import com.ritesh.cashiro.presentation.effects.VerticalStretchedOverscroll
import com.ritesh.cashiro.presentation.ui.extras.components.account.AccountCardLayout
import com.ritesh.cashiro.presentation.ui.extras.components.account.EditAccountSheet
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenEvent
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenState
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenViewModel
import com.ritesh.cashiro.presentation.ui.features.add_transaction.AddTransactionEvent
import com.ritesh.cashiro.presentation.ui.features.add_transaction.AddTransactionScreenState
import com.ritesh.cashiro.presentation.ui.features.add_transaction.AddTransactionScreenViewModel
import com.ritesh.cashiro.presentation.ui.features.categories.CategoryScreenEvent
import com.ritesh.cashiro.presentation.ui.features.categories.CategoryScreenViewModel
import com.ritesh.cashiro.presentation.ui.features.categories.SubCategoryEvent
import com.ritesh.cashiro.presentation.ui.features.categories.SubCategoryViewModel
import com.ritesh.cashiro.presentation.ui.features.transactions.TransactionsDisplayList
import com.ritesh.cashiro.presentation.ui.theme.iosFont
@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.AccountDetailsScreen(
    accountUiState: AccountScreenState,
    setAccountAsMain: (AccountEntity) -> Unit,
    transactionUiState: AddTransactionScreenState,
    onAddTransactionEvent: (AddTransactionEvent) -> Unit,
    onAccountEvent: (AccountScreenEvent) -> Unit,
    onCategoryEvent: (CategoryScreenEvent) -> Unit,
    onSubCategoryEvent: (SubCategoryEvent) -> Unit,
    deleteTransactions: (List<Int>) -> Unit,
    getTransactionStatsForAccount: (Int, (AccountTransactionStats?) -> Unit) -> Unit,
    updateAccountCurrency: (Int, String) -> Unit,
    currencyUiState: CurrencyViewModel.CurrencyUiState,
    selectCurrency: (String) -> Unit,
    account: AccountEntity,
    themeColors: ColorScheme,
    screenTitle: String,
    navController: NavController,
    sharedKey: String,
    animatedVisibilityScope: AnimatedVisibilityScope

){
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val usedScreenHeight =  screenHeight
    val usedScreenWidth = screenWidth
    val editAccountSheetState = rememberModalBottomSheetState(skipPartiallyExpanded= true)

//    val screenState by transactionViewModel.state.collectAsState()
    val transactions = transactionUiState.transactions

    val accounts = accountUiState.accounts
    val scrollState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val overscrollEffect = remember(coroutineScope) { VerticalStretchedOverscroll(coroutineScope) }


    val filteredTransactions = transactions.filter { it.accountId == account.id }
    var isSheetOpen = rememberSaveable { mutableStateOf(false) }
    // Selection state management
    var selectedTransactions by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var isInSelectionMode by remember { mutableStateOf(false) }

    // Function to toggle selection of a transaction
    val toggleTransactionSelection: (TransactionEntity) -> Unit = { transaction ->
        if (isInSelectionMode) {
            selectedTransactions = if (selectedTransactions.contains(transaction.id)) {
                val updatedSelection = selectedTransactions - transaction.id
                if (updatedSelection.isEmpty()) {
                    isInSelectionMode = false
                }
                updatedSelection
            } else {
                selectedTransactions + transaction.id
            }
        } else {
            // Regular click behavior when not in selection mode
            // Could navigate to transaction details if needed
            // navController.navigate("transaction/${transaction.id}")
        }
    }

    // Function to handle long press on transaction item
    val onTransactionLongPress: (TransactionEntity) -> Unit = { transaction ->
        isInSelectionMode = true
        selectedTransactions = selectedTransactions + transaction.id
    }

    // Function to clear all selections and exit selection mode
    val clearSelections = {
        selectedTransactions = emptySet()
        isInSelectionMode = false
    }

    // Function to delete selected transactions
    val deleteSelectedTransactions = {
        deleteTransactions(selectedTransactions.toList())
        clearSelections()
    }

    // Listen for deletion events from the ViewModel
    LaunchedEffect(filteredTransactions) {
        if (selectedTransactions.isNotEmpty()) {
            // Check if any of the selected transactions no longer exist
            val currentIds = filteredTransactions.map { it.id }.toSet()
            val deletedSelections = selectedTransactions.filter { !currentIds.contains(it) }

            if (deletedSelections.isNotEmpty()) {
                // Some selected transactions were deleted, update the selection set
                selectedTransactions = selectedTransactions - deletedSelections.toSet()

                // If no items remain selected, exit selection mode
                if (selectedTransactions.isEmpty()) {
                    isInSelectionMode = false
                }
            }
        }
    }
    // Select all transactions from search results
    val selectAllTransactions = {
        if (!isInSelectionMode) {
            isInSelectionMode = true
        }
        // Get all transaction IDs from current filtered results
        val searchResultIds = filteredTransactions.map { it.id }.toSet()
        selectedTransactions = searchResultIds
        Log.d("SelectAll", "Selected all ${searchResultIds.size} transactions from search results")
        Unit
    }
    // Back handler for selection mode
    BackHandler(enabled = isInSelectionMode) {
        clearSelections()
    }

    // Collect all transactions, accounts, and categories

    Box(
        modifier = Modifier
            .sharedBounds(
                sharedContentState = rememberSharedContentState(key = sharedKey),
                animatedVisibilityScope = animatedVisibilityScope
            )
            .fillMaxSize()
            .padding(top = 50.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.size(20.dp))
            Box(modifier = Modifier.animateContentSize()) {
                BlurredAnimatedVisibility (isInSelectionMode) {
                    // Show selection mode toolbar
                    AccountDetailsScreenTopAppBar(
                        selectedCount = selectedTransactions.size,
                        onClearSelection = clearSelections,
                        onDeleteSelected = deleteSelectedTransactions,
                        onSelectAll = selectAllTransactions
                    )
                }
                BlurredAnimatedVisibility (!isInSelectionMode){
                    Text(
                        text = screenTitle,
                        textAlign = TextAlign.Center,
                        fontFamily = iosFont,
                        fontSize = 20.sp,
                        color = themeColors.inverseSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(themeColors.background)
                            .zIndex(6f)
                    )
                }
            }

            Text(
                text = "All transactions made from ${account.accountName}",
                textAlign = TextAlign.Center,
                fontFamily = iosFont,
                fontSize = 12.sp,
                color = themeColors.inverseSurface.copy(0.5f),
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(themeColors.background)
                    .zIndex(6f)
                    .padding(bottom = 10.dp)
            )
            Spacer(modifier = Modifier.size(15.dp))
            AccountCardLayout(
                item = account,
                themeColors = themeColors,
                modifier = Modifier.padding(horizontal = 10.dp).zIndex(2f).clickable(
                    onClick = {
                        isSheetOpen.value = true
                    }
                ),
                accountUiState = accountUiState,
                setAccountAsMain = setAccountAsMain,
            )
            Spacer(modifier = Modifier.size(25.dp))
            Text(
                text = "Transactions",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                fontFamily = iosFont,
                fontSize = 16.sp,
                color = themeColors.inverseSurface,
                modifier = Modifier.fillMaxWidth().zIndex(2f)
            )
            if (filteredTransactions.isEmpty()) {
                Text(
                    text = "No Transactions available",
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    fontFamily = iosFont,
                    fontSize = 16.sp,
                    color = themeColors.inverseSurface.copy(0.5f),
                    modifier = Modifier.fillMaxWidth().zIndex(0f)
                )
            }

            TransactionsDisplayList(
                transactions = filteredTransactions,
                accounts = accounts,
                themeColors = themeColors,
                selectedTransactionIds = selectedTransactions,
                onTransactionClick = toggleTransactionSelection,
                onTransactionLongPress = onTransactionLongPress,
                isInSelectionMode = isInSelectionMode,
                scrollOverScrollState = scrollState,
                overscrollEffect = overscrollEffect,
                navController = navController,
                animatedVisibilityScope = animatedVisibilityScope,
                showTransactionCashFlowHeader = false,
                showMonthInDateHeader = true,
                modifier = Modifier.zIndex(0f),
                accountUiState = accountUiState,
                onAccountEvent = onAccountEvent,
                onAddTransactionEvent = onAddTransactionEvent,
                onCategoryEvent = onCategoryEvent,
                onSubCategoryEvent = onSubCategoryEvent,
            )

        }
    }
    if (isSheetOpen.value) {
        EditAccountSheet(
            sheetState = editAccountSheetState,
            isSheetOpen = isSheetOpen,
            balance = account.balance.toString(),
            accountName = account.accountName,
            isMainAccount = account.isMainAccount,
            cardColor1 = Color(account.cardColor1),
            cardColor2 = Color(account.cardColor2),
            currencyCode = account.currencyCode,
            usedScreenWidth = usedScreenWidth - 10.dp,
            usedScreenHeight = (usedScreenHeight / 1.2f) + 10.dp,
            currentAccountEntity = account,
            isUpdate = true,
            accountUiState = accountUiState,
            onAccountEvent = onAccountEvent,
            getTransactionStatsForAccount = getTransactionStatsForAccount,
            updateAccountCurrency = updateAccountCurrency,
            currencyUiState = currencyUiState,
            selectCurrency = selectCurrency,
            onAddTransactionEvent = onAddTransactionEvent,
            transactionUiState = transactionUiState,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailsScreenTopAppBar(
    selectedCount: Int,
    onClearSelection: () -> Unit,
    onDeleteSelected: () -> Unit,
    onSelectAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .animateContentSize()
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onClearSelection) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear Selection",
                    tint = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                )
            }

            Text(
                text = "$selectedCount Selected",
                fontFamily = iosFont,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.inverseSurface
            )
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onSelectAll) {
                Icon(
                    painter = painterResource(R.drawable.select_all),
                    tint = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                    contentDescription = "Select All"
                )
            }
            IconButton(onClick = onDeleteSelected) {
                Icon(
                    painter = painterResource(R.drawable.delete_bulk),
                    tint = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                    contentDescription = "Delete Selected"
                )
            }
        }
    }
}