package com.ritesh.cashiro.presentation.ui.features.profile

import android.Manifest
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.ritesh.cashiro.R
import com.ritesh.cashiro.data.local.entity.AccountEntity
import com.ritesh.cashiro.data.local.entity.TransactionEntity
import com.ritesh.cashiro.data.local.entity.TransactionType
import com.ritesh.cashiro.data.currency.model.CurrencySymbols
import com.ritesh.cashiro.domain.utils.AppEventBus
import com.ritesh.cashiro.domain.utils.CurrencyEvent
import com.ritesh.cashiro.domain.utils.CurrencyUtils
import com.ritesh.cashiro.presentation.effects.BlurAnimatedTextCountUpWithCurrency
import com.ritesh.cashiro.presentation.ui.extras.components.extras.CustomTitleTopAppBar
import com.ritesh.cashiro.presentation.ui.extras.components.profile.EditProfileSheet
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenState
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenViewModel
import com.ritesh.cashiro.presentation.ui.features.add_transaction.AddTransactionScreenState
import com.ritesh.cashiro.presentation.ui.features.add_transaction.AddTransactionScreenViewModel
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Green_Dim
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Red_Dim
import com.ritesh.cashiro.presentation.ui.theme.iosFont

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    accountViewModel: AccountScreenViewModel,
    transactionViewModel: AddTransactionScreenViewModel,
    profileViewModel: ProfileScreenViewModel,
    onBackClicked: () -> Unit,
    screenTitle: String,
    previousScreenTitle: String,
) {
    ProfileContent(
        profileUiState = profileViewModel.state.collectAsState().value,
        onProfileEvent = profileViewModel::handleEvent,
        accountUiState = accountViewModel.state.collectAsState().value,
        transactionUiState = transactionViewModel.state.collectAsState().value,
        fetchAllAccounts = accountViewModel::fetchAllAccounts,
        fetchAllTransactions = transactionViewModel::fetchAllTransactions,
        onBackClicked = onBackClicked,
        screenTitle = screenTitle,
        previousScreenTitle = previousScreenTitle,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileContent(
    profileUiState: ProfileScreenState,
    onProfileEvent: (ProfileScreenEvent) -> Unit,
    accountUiState: AccountScreenState,
    transactionUiState: AddTransactionScreenState,
    fetchAllAccounts: () -> Unit,
    fetchAllTransactions: () -> Unit,
    onBackClicked: () -> Unit,
    screenTitle: String,
    previousScreenTitle: String,
){
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scrollBehaviorSmall = TopAppBarDefaults.pinnedScrollBehavior()

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val screenWidth = configuration.screenWidthDp.dp
    val usedScreenHeight = screenHeight / 1.2f
    val usedScreenWidth = screenWidth - 10.dp

    val accounts = accountUiState.accounts
    val transactions = transactionUiState.transactions
    val mainCurrency = accountUiState.mainAccountCurrencyCode
    val conversionRates = accountUiState.mainCurrencyConversionRates

    // Single state object from ViewModel
    val context = LocalContext.current

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            onProfileEvent(ProfileScreenEvent.UpdateStoragePermission(isGranted))
        }
    )

    // Photo picker launchers
    val profilePhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                onProfileEvent(ProfileScreenEvent.UpdateEditProfileImage(uri))
            }
        }
    )

    val bannerPhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                onProfileEvent(ProfileScreenEvent.UpdateEditBannerImage(uri))
            }
        }
    )

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun requestStoragePermission() {
        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CustomTitleTopAppBar(
                scrollBehaviorLarge = scrollBehavior,
                scrollBehaviorSmall = scrollBehaviorSmall,
                title = screenTitle,
                previousScreenTitle = previousScreenTitle,
                onBackClick = onBackClicked,
                onEditClick = { onProfileEvent(ProfileScreenEvent.ToggleEditSheet) },
                hasBackButton = true
            )
        },
    ) { innerPadding ->
        Box(modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
        ) {
            if (profileUiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
                    item(key = "Profile Images card") {
                        DisplayProfileImagesCard(profileState = profileUiState)
                    }
                    item(key = "User Name and Subtitles") {
                        DisplayUserNameAndSubtitles(
                            transactions = transactions,
                            profileState = profileUiState
                        )
                    }
                    item(key = "Financial Overview") {
                        FinancialOverviewCard(
                            accounts = accounts,
                            transactions = transactions,
                            mainCurrency = mainCurrency,
                            conversionRates = conversionRates,
                            fetchAllAccounts = fetchAllAccounts,
                            fetchAllTransactions = fetchAllTransactions,
                        )
                    }
                }
            }
        }

        if (profileUiState.isEditSheetOpen) {
            EditProfileSheet(
                state = profileUiState,
                sheetState = sheetState,
                usedScreenWidth = usedScreenWidth,
                usedScreenHeight = usedScreenHeight,
                onEvent = onProfileEvent,
                profilePhotoPicker = profilePhotoPicker,
                bannerPhotoPicker = bannerPhotoPicker,
                requestStoragePermission = { requestStoragePermission() }
            )
        }
    }
}

@Composable
private fun DisplayProfileImagesCard(
    profileState: ProfileScreenState,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 15.dp), contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .animateContentSize()
                .sizeIn(maxHeight = 250.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(15.dp))
        ) {
            AsyncImage(
                model = profileState.bannerImageUri
                    ?: R.drawable.banner_bg_image,
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
        }

        Spacer(
            modifier = Modifier
                .height(150.dp)
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background.copy(0.5f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .align(Alignment.BottomCenter)
        )

        Box(
            modifier = Modifier
                .size(130.dp)
                .clip(CircleShape)
                .background(profileState.profileBackgroundColor)
                .align(Alignment.BottomCenter)
        ) {
            AsyncImage(
                model = profileState.profileImageUri ?: R.drawable.avatar_1,
                contentDescription = null,
                modifier = Modifier.zIndex(5f),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun DisplayUserNameAndSubtitles(
    transactions: List<TransactionEntity>,
    profileState: ProfileScreenState
) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = profileState.userName,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.inverseSurface,
                fontFamily = iosFont,
            )
            Icon(
                painter = painterResource(R.drawable.verify_bulk),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 5.dp),
                contentDescription = ""
            )
        }
        Text(
            text = "${profileState.totalTransactions} Transactions",
            fontSize = 12.sp,
            fontFamily = iosFont,
            color = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
            fontWeight = FontWeight.Medium,
        )
    }
}
@Composable
private fun FinancialOverviewCard(
    accounts: List<AccountEntity>,
    transactions: List<TransactionEntity>,
    fetchAllAccounts: () -> Unit,
    fetchAllTransactions: () -> Unit,
    mainCurrency: String,
    conversionRates: Map<String, Double>,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        AppEventBus.events.collect { event ->
            when (event) {
                is CurrencyEvent.AccountCurrencyChanged,
                is CurrencyEvent.MainAccountCurrencyChanged,
                is CurrencyEvent.ConversionRatesUpdated -> {
                    // Refresh data
                    fetchAllAccounts()
                    fetchAllTransactions()
                }
            }
        }
    }

    // Calculate total net worth
//    val totalNetWorth = accounts.sumOf { it.balance }
    val totalNetWorth = CurrencyUtils.calculateNetWorth(accounts, mainCurrency, conversionRates)


    // Debug each account
    accounts.forEach { account ->
        Log.d("FinancialOverview", "Account: ${account.accountName}, Balance: ${account.balance}, Currency: ${account.currencyCode}")
    }

    // Filter non-transfer transactions
    val nonTransferTransactions = transactions.filter { it.mode != "Transfer" }
    val totalIncome = CurrencyUtils.calculateTotalIncome(
        nonTransferTransactions.filter { it.mode == "Income" },
        mainCurrency,
        conversionRates
    )
    val totalExpense = CurrencyUtils.calculateTotalExpense(
        nonTransferTransactions.filter { it.mode == "Expense" },
        mainCurrency,
        conversionRates
    )

    // For debugging - examine a few transactions in detail
    nonTransferTransactions.take(5).forEach { transaction ->
        Log.d("FinancialOverview", "Transaction: ${transaction.id}, Title: ${transaction.title}, " +
                "Mode: ${transaction.mode}, Amount: ${transaction.amount}, " +
                "Currency: ${transaction.originalCurrencyCode ?: mainCurrency}")
    }

    Log.d("FinancialOverview", "Raw Total Income: $totalIncome")
    Log.d("FinancialOverview", "Raw Total Expense: $totalExpense")

    // Upcoming transactions count
    val upcomingTransactions = transactions.count {
        it.transactionType == TransactionType.UPCOMING && !it.isPaid
    }

    // Format numbers for display
    val netWorthFormatted = String.format("%.2f", totalNetWorth)
    val totalIncomeFormatted = String.format("%.2f", totalIncome)
    val totalExpenseFormatted = String.format("%.2f", totalExpense)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "Net Worth",
                        fontFamily = iosFont,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.inverseSurface.copy(0.5f),
                        modifier = Modifier
                    )
                    BlurAnimatedTextCountUpWithCurrency(
                        text = netWorthFormatted,
                        currencySymbol = CurrencySymbols.getSymbol(mainCurrency),
                        fontSize = 20.sp,
                        maxLines = 1,
                        fontFamily = iosFont,
                        fontWeight = FontWeight.Bold,
                        enableDynamicSizing = true,
                        modifier = Modifier
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceBright,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(15.dp)
                    ,
                    contentAlignment = Alignment.Center,
                ){
                    Image(
                        painter = painterResource(R.drawable.type_sports_sports_medal),
                        contentDescription = "total net worth",
                        modifier = Modifier
                            .size(42.dp)
                    )
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f).background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceBright,
                                Macchiato_Green_Dim.copy(0.3f)
                            ),
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.Start) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.type_finance_money_bag),
                            contentDescription = "total income",
                            modifier = Modifier
                                .size(26.dp)
                        )
                        Text(
                            text = "Total Income",
                            fontFamily = iosFont,
                            fontWeight = FontWeight.Medium,
                            color = Macchiato_Green_Dim,
                            modifier = Modifier
                        )
                    }

                    BlurAnimatedTextCountUpWithCurrency(
                        text = totalIncomeFormatted,
                        currencySymbol = CurrencySymbols.getSymbol(mainCurrency),
                        fontSize = 20.sp,
                        maxLines = 1,
                        fontFamily = iosFont,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier,
                        enableDynamicSizing = true
                    )
                }
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f).background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceBright,
                                Macchiato_Red_Dim.copy(0.3f)
                            ),
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.Start
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.type_finance_chart_decreasing),
                            contentDescription = "total expense",
                            modifier = Modifier
                                .size(24.dp)
                        )
                        Text(
                            text = "Total Expense",
                            fontFamily = iosFont,
                            fontWeight = FontWeight.Medium,
                            color = Macchiato_Red_Dim,
                            modifier = Modifier
                        )
                    }
                    BlurAnimatedTextCountUpWithCurrency(
                        text = totalExpenseFormatted,
                        currencySymbol = CurrencySymbols.getSymbol(mainCurrency),
                        fontSize = 20.sp,
                        maxLines = 1,
                        fontFamily = iosFont,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier,
                        enableDynamicSizing = true
                    )
                }
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp), horizontalAlignment = Alignment.Start) {
                Text(
                    text = "Upcoming Transactions",
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier
                )
                Text(
                    text = "$upcomingTransactions Transactions",
                    fontSize = 20.sp,
                    maxLines = 1,
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.inverseSurface,
                    modifier = Modifier
                )
            }
        }
    }
}
@Preview(
    name = "Profile Screen",
    showBackground = false,
)
@Composable
private fun ProfileScreenPreview() {
    MaterialTheme {
        Surface(
            color = MaterialTheme.colorScheme.background
        ){
            ProfileContent(
                profileUiState = ProfileScreenState(),
                onProfileEvent = {},
                accountUiState = AccountScreenState(),
                transactionUiState = AddTransactionScreenState(),
                fetchAllAccounts = {},
                fetchAllTransactions = {},
                onBackClicked = {},
                screenTitle = "Profile",
                previousScreenTitle = "Settings",
            )
        }
    }
}

