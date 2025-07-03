package com.ritesh.cashiro.presentation.ui.features.accounts

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ritesh.cashiro.data.local.entity.AccountEntity
import com.ritesh.cashiro.data.currency.repository.CurrencyRepository
import com.ritesh.cashiro.data.currency.model.Currency
import com.ritesh.cashiro.domain.repository.AccountRepository
import com.ritesh.cashiro.domain.repository.AccountTransactionStats
import com.ritesh.cashiro.domain.repository.TransactionRepository
import com.ritesh.cashiro.domain.utils.ActivityLogUtils
import com.ritesh.cashiro.domain.utils.AppEventBus
import com.ritesh.cashiro.domain.utils.CurrencyEvent
import com.ritesh.cashiro.widgets.WidgetUpdateUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountScreenViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
    private val currencyRepository: CurrencyRepository,
    private val widgetUpdateUtil: WidgetUpdateUtil,
    private val activityLogUtils: ActivityLogUtils,
) : ViewModel(), AccountScreenEventHandler {

    // State for the entire screen
    private val _state = MutableStateFlow(AccountScreenState())
    val state: StateFlow<AccountScreenState> = _state.asStateFlow()

    // Individual state flows for backward compatibility with existing UI
    val accountName: StateFlow<String> = state.map { it.accountName }.stateIn(
        viewModelScope, SharingStarted.Eagerly, ""
    )

    val cardColor1: StateFlow<Color?> = state.map { it.cardColor1 }.stateIn(
        viewModelScope, SharingStarted.Eagerly, null
    )

    val cardColor2: StateFlow<Color?> = state.map { it.cardColor2 }.stateIn(
        viewModelScope, SharingStarted.Eagerly, null
    )

    val balance: StateFlow<String> = state.map { it.balance }.stateIn(
        viewModelScope, SharingStarted.Eagerly, ""
    )

    val isMainAccount: StateFlow<Boolean> = state.map { it.isMainAccount }.stateIn(
        viewModelScope, SharingStarted.Eagerly, false
    )

    val currencyCode: StateFlow<String> = state.map { it.currencyCode }.stateIn(
        viewModelScope, SharingStarted.Eagerly, "usd"
    )

    val availableCurrencies: StateFlow<List<Currency>> = state.map { it.availableCurrencies }.stateIn(
        viewModelScope, SharingStarted.Eagerly, emptyList()
    )

    val accounts: StateFlow<List<AccountEntity>> = state.map { it.accounts }.stateIn(
        viewModelScope, SharingStarted.Eagerly, emptyList()
    )

    val transactionCounts: StateFlow<Map<Int, Int>> = state.map { it.transactionCounts }.stateIn(
        viewModelScope, SharingStarted.Eagerly, emptyMap()
    )

    val mainAccountCurrencyCode: StateFlow<String> = state.map { it.mainAccountCurrencyCode }.stateIn(
        viewModelScope, SharingStarted.Eagerly, "usd"
    )

    val mainCurrencyConversionRates: StateFlow<Map<String, Double>> = state.map { it.mainCurrencyConversionRates }.stateIn(
        viewModelScope, SharingStarted.Eagerly, emptyMap()
    )
    val lastCurrencyUpdateTimestamp: StateFlow<Long> = state.map { it.lastCurrencyUpdateTimestamp }.stateIn(
        viewModelScope, SharingStarted.Eagerly, 0L
    )

    // For observing currency events
    private val _currencyChangeEvents = MutableSharedFlow<CurrencyEvent.AccountCurrencyChanged>()
    val currencyChangeEvents = _currencyChangeEvents.asSharedFlow()

    init {
        onEvent(AccountScreenEvent.FetchAllAccounts)
        onEvent(AccountScreenEvent.FetchAllTransactionCounts)
        loadAvailableCurrencies()
        updateMainAccountCurrency()

        // Listen for currency change events from the event bus
        viewModelScope.launch {
            AppEventBus.events.collect { event ->
                when (event) {
                    is CurrencyEvent.AccountCurrencyChanged -> {
                        Log.d("CurrencyEventBus", "Received AccountCurrencyChanged event: ${event.accountId} from ${event.oldCurrencyCode} to ${event.newCurrencyCode}")
                        // Handle currency change events
                        handleCurrencyChangeFromBus(event)
                    }
                    is CurrencyEvent.MainAccountCurrencyChanged -> {
                        Log.d("CurrencyEventBus", "Received MainAccountCurrencyChanged event: ${event.mainAccountId} from ${event.oldCurrencyCode} to ${event.newCurrencyCode}")
                        // Update main account currency
                        _state.update { it.copy(mainAccountCurrencyCode = event.newCurrencyCode) }
                        fetchMainCurrencyConversionRates()
                    }
                    is CurrencyEvent.ConversionRatesUpdated -> {
                        Log.d("CurrencyEventBus", "Received ConversionRatesUpdated event for ${event.baseCurrency}")
                        // Update conversion rates in state
                        _state.update { it.copy(mainCurrencyConversionRates = event.rates) }
                    }
                }
            }
        }

        viewModelScope.launch {
            mainAccountCurrencyCode.collect { currency ->
                fetchMainCurrencyConversionRates()
            }
        }
    }

    override fun onEvent(event: AccountScreenEvent) {
        when (event) {
            // Form events
            is AccountScreenEvent.UpdateAccountName -> {
                _state.update { it.copy(accountName = event.name) }
            }

            is AccountScreenEvent.UpdateCardColor1 -> {
                _state.update { it.copy(cardColor1 = event.color) }
            }

            is AccountScreenEvent.UpdateCardColor2 -> {
                _state.update { it.copy(cardColor2 = event.color) }
            }

            is AccountScreenEvent.UpdateBalance -> {
                _state.update { it.copy(balance = event.balance) }
            }

            is AccountScreenEvent.UpdateIsMainAccount -> {
                _state.update { it.copy(isMainAccount = event.isMain) }
            }

            is AccountScreenEvent.UpdateCurrencyCode -> {
                _state.update { it.copy(currencyCode = event.code) }
            }

            // Account management events
            is AccountScreenEvent.PrepareForNewAccount -> {
                prepareForNewAccount()
            }

            is AccountScreenEvent.SaveAccount -> {
                saveAccount {}
            }

            is AccountScreenEvent.OnAccountSaved -> {
                saveAccount(event.onSuccess)
            }

            is AccountScreenEvent.UpdateExistingAccount -> {
                updateAccount(event.account) {}
            }

            is AccountScreenEvent.OnAccountUpdated -> {
                updateAccount(event.account, event.onSuccess)
            }

            is AccountScreenEvent.DeleteAccount -> {
                deleteAccount(event.account)
            }

            is AccountScreenEvent.GetAccountById -> {
                getAccountById(event.id) {}
            }

            is AccountScreenEvent.OnAccountFetched -> {
                getAccountById(event.id, event.onSuccess)
            }

            is AccountScreenEvent.SetMainAccount -> {
                setAccountAsMain(event.account)
            }

            is AccountScreenEvent.SetMainAccountById -> {
                setMainAccountById(event.id)
            }

            // List events
            is AccountScreenEvent.ReorderAccounts -> {
                val currentAccounts = _state.value.accounts
                if (event.fromIndex in currentAccounts.indices && event.toIndex in currentAccounts.indices) {
                    val updatedAccounts = currentAccounts.toMutableList().apply {
                        val itemToMove = removeAt(event.fromIndex)
                        add(event.toIndex, itemToMove)
                    }
                    updateAccounts(updatedAccounts)
                } else {
                    Log.e("AccountsListItem", "Invalid indices: fromIndex=${event.fromIndex}, toIndex=${event.toIndex}")
                }
            }

            is AccountScreenEvent.UpdateAccountsList -> {
                updateAccounts(event.accounts)
            }

            // Data loading events
            is AccountScreenEvent.FetchAllAccounts -> {
                fetchAllAccounts()
            }

            is AccountScreenEvent.FetchAllTransactionCounts -> {
                fetchAllTransactionCounts()
            }

            is AccountScreenEvent.RefreshTransactionCountForAccount -> {
                // Make sure this is called within a coroutine
                viewModelScope.launch {
                    val count = transactionRepository.getTransactionCountByAccountId(event.accountId)
                    val currentCounts = _state.value.transactionCounts.toMutableMap()
                    currentCounts[event.accountId] = count
                    _state.update { it.copy(transactionCounts = currentCounts) }
                }
            }

            // Currency change events
            is AccountScreenEvent.AccountCurrencyChanged -> {
                handleAccountCurrencyChanged(event.accountId, event.oldCurrencyCode, event.newCurrencyCode, event.conversionRate)
            }

            // Transaction update events
            is AccountScreenEvent.UpdateTransactionAmounts -> {
                // Make sure suspend function is called within a coroutine
                viewModelScope.launch {
                    updateTransactionAmountsForCurrencyChange(
                        event.accountId,
                        event.oldCurrencyCode,
                        event.newCurrencyCode,
                        event.conversionRate
                    )
                }
            }
            // Account Deletion Events
            is AccountScreenEvent.ShowAccountDeleteConfirmation -> {
                _state.update {
                    it.copy(
                        showAccountDeleteConfirmationDialog = true,
                        pendingAccountDeletion = event.account
                    )
                }
            }

            is AccountScreenEvent.ShowAccountDeleteOptions -> {
                _state.update {
                    it.copy(
                        showAccountDeleteConfirmationDialog = false,
                        showAccountDeleteOptionsDialog = true,
                        pendingAccountDeletion = event.account
                    )
                }
            }

            is AccountScreenEvent.ShowAccountMoveTransactions -> {
                val availableAccounts = _state.value.accounts.filter {
                    it.id != event.account.id
                }
                _state.update {
                    it.copy(
                        showAccountDeleteOptionsDialog = false,
                        showAccountMoveTransactionsDialog = true,
                        availableAccountsForMigration = availableAccounts
                    )
                }
            }

            is AccountScreenEvent.SelectTargetAccountForMigration -> {
                _state.update {
                    it.copy(selectedTargetAccountId = event.accountId)
                }
            }

            is AccountScreenEvent.ConfirmAccountMigrationAndDeletion -> {
                viewModelScope.launch {
                    val success = accountRepository.migrateAccountTransactions(
                        event.sourceAccount.id,
                        event.targetAccountId,
                        transactionRepository
                    )

                    if (success) {
                        // Log account deletion to ActivityLog
                        activityLogUtils.logAccountDeleted(event.sourceAccount)

                        accountRepository.deleteAccount(event.sourceAccount)
                        fetchAllAccounts()
                    }

                    _state.update {
                        it.copy(
                            showAccountMoveTransactionsDialog = false,
                            showAccountFinalConfirmationDialog = false,
                            pendingAccountDeletion = null,
                            selectedTargetAccountId = null
                        )
                    }
                }
            }

            is AccountScreenEvent.ConfirmAccountDeletionWithTransactions -> {
                viewModelScope.launch {
                    // Log account deletion to ActivityLog before deleting
                    activityLogUtils.logAccountDeleted(event.account)

                    val success = accountRepository.deleteAccountWithTransactions(
                        event.account.id,
                        transactionRepository
                    )

                    if (success) {
                        fetchAllAccounts()
                    }

                    _state.update {
                        it.copy(
                            showAccountDeleteOptionsDialog = false,
                            pendingAccountDeletion = null
                        )
                    }
                }
            }

            is AccountScreenEvent.CancelAccountDeletion -> {
                _state.update {
                    it.copy(
                        showAccountDeleteConfirmationDialog = false,
                        showAccountDeleteOptionsDialog = false,
                        showAccountMoveTransactionsDialog = false,
                        showAccountFinalConfirmationDialog = false,
                        pendingAccountDeletion = null,
                        selectedTargetAccountId = null
                    )
                }
            }

            // Account Merge Events
            is AccountScreenEvent.ShowMergeAccountDialog -> {
                val availableAccounts = _state.value.accounts.filter {
                    it.id != event.account.id
                }
                _state.update {
                    it.copy(
                        showMergeAccountDialog = true,
                        pendingMergeAccountSource = event.account,
                        availableAccountsForMerge = availableAccounts
                    )
                }
            }

            is AccountScreenEvent.SelectTargetAccountForMerge -> {
                _state.update {
                    it.copy(selectedMergeAccountTargetId = event.accountId)
                }
            }

            is AccountScreenEvent.ConfirmAccountMerge -> {
                viewModelScope.launch {
                    val success = accountRepository.migrateAccountTransactions(
                        event.sourceAccount.id,
                        event.targetAccountId,
                        transactionRepository
                    )

                    if (success) {
                        // Log account deletion (merge) to ActivityLog
                        activityLogUtils.logAccountDeleted(event.sourceAccount)
                        accountRepository.deleteAccount(event.sourceAccount)
                        fetchAllAccounts()
                    }

                    _state.update {
                        it.copy(
                            showMergeAccountDialog = false,
                            showMergeAccountFinalConfirmationDialog = false,
                            pendingMergeAccountSource = null,
                            selectedMergeAccountTargetId = null
                        )
                    }
                }
            }

            is AccountScreenEvent.CancelMergeAccount -> {
                _state.update {
                    it.copy(
                        showMergeAccountDialog = false,
                        showMergeAccountFinalConfirmationDialog = false,
                        pendingMergeAccountSource = null,
                        selectedMergeAccountTargetId = null
                    )
                }
            }
        }
    }

    private fun handleCurrencyChangeFromBus(event: CurrencyEvent.AccountCurrencyChanged) {
        Log.d("CurrencyChange", "Handling event from event bus for account ${event.accountId}")
        _state.update {
            it.copy(
                lastCurrencyUpdateTimestamp = System.currentTimeMillis(),
                lastUpdatedAccountId = event.accountId
            )
        }

        // Refresh account data to ensure UI is updated
        fetchAllAccounts()

        // Check if this was the main account and update the main currency if needed
        viewModelScope.launch {
            val account = accountRepository.getAccountById(event.accountId)
            if (account?.isMainAccount == true) {
                _state.update { it.copy(mainAccountCurrencyCode = event.newCurrencyCode) }
                fetchMainCurrencyConversionRates()

                // Emit main account currency changed event
                AppEventBus.tryEmitEvent(
                    CurrencyEvent.MainAccountCurrencyChanged(
                        mainAccountId = event.accountId,
                        oldCurrencyCode = event.oldCurrencyCode,
                        newCurrencyCode = event.newCurrencyCode
                    )
                )
            }
        }
    }

    private fun handleAccountCurrencyChanged(
        accountId: Int,
        oldCurrencyCode: String,
        newCurrencyCode: String,
        conversionRate: Double
    ) {
        Log.d("CurrencyChange", "Currency changed for account $accountId from $oldCurrencyCode to $newCurrencyCode")

        // Update state to trigger UI refresh
        _state.update {
            it.copy(
                lastCurrencyUpdateTimestamp = System.currentTimeMillis(),
                lastUpdatedAccountId = accountId
            )
        }

        // Update transaction amounts in database
        viewModelScope.launch {
            updateTransactionAmountsForCurrencyChange(accountId, oldCurrencyCode, newCurrencyCode, conversionRate)

            // Emit currency changed event through the event bus
            com.ritesh.cashiro.domain.utils.AppEventBus.tryEmitEvent(
                CurrencyEvent.AccountCurrencyChanged(
                    accountId = accountId,
                    oldCurrencyCode = oldCurrencyCode,
                    newCurrencyCode = newCurrencyCode,
                    conversionRate = conversionRate
                )
            )

            // Also emit to our local flow for direct observers
            _currencyChangeEvents.emit(
                CurrencyEvent.AccountCurrencyChanged(
                    accountId = accountId,
                    oldCurrencyCode = oldCurrencyCode,
                    newCurrencyCode = newCurrencyCode,
                    conversionRate = conversionRate
                )
            )

            fetchAllAccounts() // Ensure accounts are refreshed immediately

            // Check if this was the main account and update the main currency if needed
            val account = accountRepository.getAccountById(accountId)
            if (account?.isMainAccount == true) {
                _state.update { it.copy(mainAccountCurrencyCode = newCurrencyCode) }
                fetchMainCurrencyConversionRates()

                // Emit main account currency changed event
                AppEventBus.tryEmitEvent(
                    CurrencyEvent.MainAccountCurrencyChanged(
                        mainAccountId = accountId,
                        oldCurrencyCode = oldCurrencyCode,
                        newCurrencyCode = newCurrencyCode
                    )
                )
            }
        }
    }

    // Update currency based on main account and apply it to currencyCode if not editing
    private fun  updateMainAccountCurrency() {
        viewModelScope.launch {
            val accounts = accountRepository.getAllAccounts()
            val mainAccount = accounts.find { it.isMainAccount }
            mainAccount?.let {
                _state.update { state ->
                    state.copy(mainAccountCurrencyCode = it.currencyCode)
                }

                // Only update the current currency code if we're not in edit mode
                // This prevents overriding a user's selection when editing an account
                if (!_state.value.isEditingAccount) {
                    _state.update { state ->
                        state.copy(currencyCode = it.currencyCode)
                    }
                    Log.d("CurrencyUpdate", "Set currency to main account currency: ${it.currencyCode}")
                }
            }
        }
    }
    fun updateAccountCurrency(accountId: Int, newCurrencyCode: String) {
        viewModelScope.launch {
            // Get the current account to access its old currency
            val account = accountRepository.getAccountById(accountId)

            if (account != null) {
                val oldCurrencyCode = account.currencyCode

                // Calculate conversion rate for proper tracking
                val conversionRate = getConversionRateForCurrencies(oldCurrencyCode, newCurrencyCode)

                // Update account currency in database
                account.currencyCode = newCurrencyCode
                accountRepository.updateAccount(account)

                // CRITICAL: Update all transactions associated with this account
                transactionRepository.updateTransactionCurrencies(accountId, newCurrencyCode)
                // Log currency change to ActivityLog
                activityLogUtils.logAccountBalanceUpdated(account, account.balance, account.balance)


                // Emit event with all necessary parameters
                AppEventBus.emitEvent(
                    CurrencyEvent.AccountCurrencyChanged(
                        accountId = accountId,
                        oldCurrencyCode = oldCurrencyCode,
                        newCurrencyCode = newCurrencyCode,
                        conversionRate = conversionRate
                    )
                )

                // Force refresh accounts data
                refreshAccounts()
                widgetUpdateUtil.updateAllFinancialWidgets()
            }
        }
    }

    private fun getConversionRateForCurrencies(fromCurrency: String, toCurrency: String): Double {
        val rates = mainCurrencyConversionRates.value

        // If currencies are the same, no conversion needed
        if (fromCurrency.equals(toCurrency, ignoreCase = true)) {
            return 1.0
        }

        // Calculate conversion rate based on available rates
        val fromRate = rates[fromCurrency.lowercase()] ?: 1.0
        val toRate = rates[toCurrency.lowercase()] ?: 1.0

        return if (fromRate > 0 && toRate > 0) {
            toRate / fromRate
        } else {
            1.0 // Fallback if rates aren't available
        }
    }

    // Helper method to get conversion rate
    private fun getConversionRate(fromCurrency: String, toCurrency: String): Double {
        val rates = mainCurrencyConversionRates.value

        // If currencies are the same, no conversion needed
        if (fromCurrency.equals(toCurrency, ignoreCase = true)) {
            return 1.0
        }

        val fromRate = rates[fromCurrency.lowercase()] ?: 1.0
        val toRate = rates[toCurrency.lowercase()] ?: 1.0

        // Calculate conversion rate
        return if (fromRate > 0 && toRate > 0) {
            toRate / fromRate
        } else {
            1.0 // Fallback
        }
    }

    // Load available currencies
    private fun loadAvailableCurrencies() {
        viewModelScope.launch {
            currencyRepository.getAllCurrencies().collect { result ->
                result.fold(
                    onSuccess = { currenciesMap ->
                        val currencies = currenciesMap.map { (code, name) ->
                            Currency(code, name)
                        }
                        _state.update { it.copy(availableCurrencies = currencies) }
                    },
                    onFailure = {
                        // Handle error if needed
                        _state.update { it.copy(error = "Failed to load currencies") }
                    }
                )
            }
        }
    }

    // Fetch all accounts from the repository
    fun fetchAllAccounts() {
        viewModelScope.launch {
            val accountsList = accountRepository.getAllAccounts().sortedBy { it.position }
            _state.update { it.copy(accounts = accountsList) }
            ensureMainAccountExists()
            updateMainAccountCurrency()
        }
    }

    // Fetch transaction counts for all accounts
    fun fetchAllTransactionCounts() {
        viewModelScope.launch {
            val accounts = accountRepository.getAllAccounts()
            val countMap = mutableMapOf<Int, Int>()

            accounts.forEach { account ->
                account.id.let { accountId ->
                    val count = transactionRepository.getTransactionCountByAccountId(accountId)
                    countMap[accountId] = count
                }
            }

            _state.update { it.copy(transactionCounts = countMap) }
        }
    }

    private fun fetchMainCurrencyConversionRates() {
        viewModelScope.launch {
            val mainCurrency = _state.value.mainAccountCurrencyCode.lowercase()
            currencyRepository.getCurrencyConversions(mainCurrency).collect { result ->
                result.fold(
                    onSuccess = { rates ->
                        _state.update { it.copy(mainCurrencyConversionRates = rates) }

                        // Emit this through the event bus
                        AppEventBus.tryEmitEvent(
                            CurrencyEvent.ConversionRatesUpdated(
                                baseCurrency = mainCurrency,
                                rates = rates
                            )
                        )
                    },
                    onFailure = {
                        // Optionally log the error or handle it
                        Log.e("ConversionRates", "Failed to fetch conversion rates", it)
                        _state.update { it.copy(error = "Failed to fetch conversion rates") }
                    }
                )
            }
        }
    }

    fun refreshAccounts() {
        viewModelScope.launch {
            val accountsList = accountRepository.getAllAccounts().sortedBy { it.position }
            _state.update { it.copy(accounts = accountsList) }
            updateMainAccountCurrency()
        }
    }

    // Update the accounts and save their new order
    fun updateAccounts(newAccounts: List<AccountEntity>) {
        _state.update { it.copy(accounts = newAccounts) }
        saveAccountOrder(newAccounts)
    }

    private fun saveAccountOrder(accounts: List<AccountEntity>) {
        viewModelScope.launch {
            // Update the position field based on the new order
            val updateAccounts = accounts.mapIndexed { index, account ->
                account.copy(position = index)
            }
            accountRepository.updateAccountOrder(updateAccounts)
            _state.update { it.copy(accounts = updateAccounts) } // Update local state
            updateMainAccountCurrency() // Update currency after order changes
        }
    }

    // Prepare for adding a new account (reset edit mode flag and set default currency)
    fun prepareForNewAccount() {
        _state.update { it.copy(isEditingAccount = false) }
        updateMainAccountCurrency() // Set currency to main account's currency
    }

    fun saveAccount(onSuccess: () -> Unit) {
        val currentState = _state.value
        val accountName = currentState.accountName
        val cardColor1 = currentState.cardColor1
        val cardColor2 = currentState.cardColor2
        val balance = currentState.balance
        val isMainAccount = currentState.isMainAccount || currentState.accounts.isEmpty()
        val currencyCode = currentState.currencyCode

        if (accountName.isNotBlank() && cardColor1 != null && cardColor2 != null && balance.isNotBlank() && currencyCode.isNotBlank()) {
            viewModelScope.launch {
                try {
                    val currentAccounts = currentState.accounts
                    val newAccount = AccountEntity(
                        accountName = accountName,
                        cardColor1 = cardColor1.toArgb(),
                        cardColor2 = cardColor2.toArgb(),
                        balance = balance.toDouble(),
                        isMainAccount = isMainAccount,
                        position = currentAccounts.size,
                        currencyCode = currencyCode
                    )

                    val newAccountId = accountRepository.addAccount(newAccount)
                    val savedAccount = newAccount.copy(id = newAccountId)

                    // Log account creation to ActivityLog
                    activityLogUtils.logAccountCreated(savedAccount)

                    if (isMainAccount) {
                        val allAccounts = accountRepository.getAllAccounts()
                        val updatedAccounts = allAccounts.map { account ->
                            account.copy(isMainAccount = account.id == newAccountId)
                        }
                        accountRepository.updateAccountOrder(updatedAccounts)

                        _state.update { it.copy(mainAccountCurrencyCode = currencyCode) }

                        AppEventBus.tryEmitEvent(
                            CurrencyEvent.MainAccountCurrencyChanged(
                                mainAccountId = newAccountId,
                                oldCurrencyCode = currentState.mainAccountCurrencyCode,
                                newCurrencyCode = currencyCode
                            )
                        )
                    }

                    fetchAllAccounts()
                    onSuccess()
                    resetInputs()

                    // Update widgets after account changes
                    widgetUpdateUtil.updateAllFinancialWidgets()

                } catch (e: Exception) {
                    Log.e("AccountSave", "Error saving account: ${e.message}", e)
                    _state.update { it.copy(error = "Error saving account: ${e.message}") }
                }
            }
        }
    }

    fun updateAccount(account: AccountEntity, onSuccess: () -> Unit) {
        val currentState = _state.value
        val accountName = currentState.accountName
        val cardColor1 = currentState.cardColor1
        val cardColor2 = currentState.cardColor2
        val balance = currentState.balance
        val isMainAccount = currentState.isMainAccount
        val currencyCode = currentState.currencyCode

        viewModelScope.launch {
            try {
                val finalCurrencyCode = if (currencyCode == currentState.mainAccountCurrencyCode)
                    account.currencyCode else currencyCode

                val updatedAccount = AccountEntity(
                    id = account.id,
                    accountName = if (accountName.isBlank()) account.accountName else accountName,
                    cardColor1 = cardColor1?.toArgb() ?: account.cardColor1,
                    cardColor2 = cardColor2?.toArgb() ?: account.cardColor2,
                    balance = if (balance.isBlank()) account.balance else balance.toDouble(),
                    isMainAccount = account.isMainAccount, // Keep original status temporarily
                    position = account.position,
                    currencyCode = finalCurrencyCode
                )

                // Check if currency is changing
                val isCurrencyChanged = account.currencyCode != finalCurrencyCode
                if (isCurrencyChanged) {
                    // Get conversion rate for updating transactions
                    val conversionRate = getConversionRate(account.currencyCode, finalCurrencyCode)

                    // Trigger dedicated event for currency change
                    onEvent(AccountScreenEvent.AccountCurrencyChanged(
                        accountId = account.id,
                        oldCurrencyCode = account.currencyCode,
                        newCurrencyCode = finalCurrencyCode,
                        conversionRate = conversionRate
                    ))
                }

                // First, handle the main account status separately
                if (isMainAccount != account.isMainAccount) {
                    if (isMainAccount) {
                        setMainAccountById(account.id)
                        // If this account is becoming the main account, update our stored main currency
                        _state.update { it.copy(mainAccountCurrencyCode = finalCurrencyCode) }

                        // Emit main account currency changed event
                        com.ritesh.cashiro.domain.utils.AppEventBus.tryEmitEvent(
                            CurrencyEvent.MainAccountCurrencyChanged(
                                mainAccountId = account.id,
                                oldCurrencyCode = currentState.mainAccountCurrencyCode,
                                newCurrencyCode = finalCurrencyCode
                            )
                        )
                    } else {
                        val mainAccounts = currentState.accounts.filter { it.isMainAccount }
                        if (mainAccounts.size > 1) {
                            accountRepository.updateMainAccount(account.id, false)
                        } else if (mainAccounts.size == 1 && mainAccounts[0].id == account.id) {
                            updatedAccount.copy(isMainAccount = true)
                        }
                    }
                } else if (account.isMainAccount && isCurrencyChanged) {
                    // If this is already the main account and currency is changing, update our stored value
                    _state.update { it.copy(mainAccountCurrencyCode = finalCurrencyCode) }

                    // Emit main account currency changed event
                    AppEventBus.tryEmitEvent(
                        CurrencyEvent.MainAccountCurrencyChanged(
                            mainAccountId = account.id,
                            oldCurrencyCode = account.currencyCode,
                            newCurrencyCode = finalCurrencyCode
                        )
                    )
                }

                // Update account in database
                accountRepository.updateAccount(updatedAccount)

                // Log account update to ActivityLog
                val changesList = mutableListOf<String>()
                if (accountName.isNotBlank() && accountName != account.accountName) {
                    changesList.add("Name: ${account.accountName} → $accountName")
                }
                if (balance.isNotBlank() && balance.toDoubleOrNull() != account.balance) {
                    changesList.add("Balance: ${account.balance} → $balance")
                }
                if (isCurrencyChanged) {
                    changesList.add("Currency: ${account.currencyCode} → $finalCurrencyCode")
                }
                if (isMainAccount != account.isMainAccount) {
                    changesList.add(if (isMainAccount) "Set as main account" else "Removed as main account")
                }

                val changes = if (changesList.isNotEmpty()) {
                    "Changes: ${changesList.joinToString(", ")}"
                } else {
                    "Account details updated"
                }

                activityLogUtils.logAccountUpdated(updatedAccount, changes)

                if (!isCurrencyChanged) {
                    // Normal fetch without the special currency change handling
                    fetchAllAccounts()
                }

                onSuccess()
                resetInputs()
                widgetUpdateUtil.updateAllFinancialWidgets()

                Log.d("AccountUpdate", "Account update process completed successfully")
            } catch (e: Exception) {
                Log.e("AccountUpdate", "Error updating account: ${e.message}", e)
                _state.update { it.copy(error = "Error updating account: ${e.message}") }
            }
        }
    }

    // Make this function more robust to ensure only one main account
    fun setMainAccountById(accountId: Int) {
        viewModelScope.launch {
            try {
                Log.d("MainAccount", "Setting account $accountId as main")
                // First, get all accounts
                val allAccounts = accountRepository.getAllAccounts()

                // Get current main account to track currency change
                val currentMainAccount = allAccounts.find { it.isMainAccount }
                val oldMainCurrencyCode = currentMainAccount?.currencyCode ?: "usd"

                // Find the account that will become main to get its currency
                val newMainAccount = allAccounts.find { it.id == accountId }
                newMainAccount?.let {
                    val newMainCurrencyCode = it.currencyCode

                    _state.update { state ->
                        state.copy(mainAccountCurrencyCode = it.currencyCode)
                    }
                    //  Log main account change to ActivityLog
                    activityLogUtils.logAccountSetAsMain(it)
                    // Check if the main account currency is changing
                    if (oldMainCurrencyCode != newMainCurrencyCode) {
                        // Emit main account currency changed event
                        AppEventBus.tryEmitEvent(
                            CurrencyEvent.MainAccountCurrencyChanged(
                                mainAccountId = accountId,
                                oldCurrencyCode = oldMainCurrencyCode,
                                newCurrencyCode = newMainCurrencyCode
                            )
                        )
                    }

                    // Update currency code for new accounts immediately
                    if (!_state.value.isEditingAccount) {
                        _state.update { state ->
                            state.copy(currencyCode = it.currencyCode)
                        }
                        Log.d("CurrencyUpdate", "Updated currency to new main account: ${it.currencyCode}")
                    }
                }

                // Update all accounts' isMainAccount status
                val updatedAccounts = allAccounts.map { account ->
                    // Only the specified account should be main
                    val shouldBeMain = account.id == accountId
                    if (account.isMainAccount != shouldBeMain) {
                        Log.d("MainAccount", "Changing account ${account.id} main status to $shouldBeMain")
                    }
                    account.copy(isMainAccount = shouldBeMain)
                }

                // Update all accounts in the database
                accountRepository.updateAccountOrder(updatedAccounts)

                // Make sure our UI state is updated
                fetchAllAccounts()
                Log.d("MainAccount", "Main account setting completed")
                widgetUpdateUtil.updateAllFinancialWidgets()
            } catch (e: Exception) {
                Log.e("MainAccount", "Error setting main account: ${e.message}", e)
                _state.update { it.copy(error = "Error setting main account: ${e.message}") }
            }
        }
    }

    // Update this function to be more robust
    fun setAccountAsMain(accountToMakeMain: AccountEntity) {
        setMainAccountById(accountToMakeMain.id)
    }

    private suspend fun updateTransactionAmountsForCurrencyChange(
        accountId: Int,
        oldCurrencyCode: String,
        newCurrencyCode: String,
        conversionRate: Double
    ) {
        // Get all transactions for this account
        val transactions = transactionRepository.getTransactionsByAccountId(accountId)

        Log.d("CurrencyConversion", "Updating ${transactions.size} transactions for account $accountId")
        Log.d("CurrencyConversion", "Conversion rate: $conversionRate ($oldCurrencyCode -> $newCurrencyCode)")

        // Update each transaction with the new amount
        transactions.forEach { transaction ->
            // Calculate new amount based on the conversion rate
            val updatedAmount = transaction.amount * conversionRate

            Log.d("CurrencyConversion", "Transaction ${transaction.id}: ${transaction.amount} $oldCurrencyCode -> $updatedAmount $newCurrencyCode")

            // Update the transaction in the database
            transactionRepository.updateTransactionAmount(transaction.id, updatedAmount)

            // Also update the transaction's currency code
            transactionRepository.updateTransactionCurrency(transaction.id, newCurrencyCode)

            // Store the converted amount in state for showing in UI
            val convertedTransactionAmounts = _state.value.convertedTransactionAmounts.toMutableMap()
            convertedTransactionAmounts[transaction.id] = updatedAmount
            _state.update { it.copy(convertedTransactionAmounts = convertedTransactionAmounts) }
        }
    }

    // Delete an account
    fun deleteAccount(account: AccountEntity) {
        viewModelScope.launch {
            try {
                // Log account deletion to ActivityLog before deleting
                activityLogUtils.logAccountDeleted(account)
                accountRepository.deleteAccount(account)
                fetchAllAccounts()

                // Update widgets after account deletion
                widgetUpdateUtil.updateAllFinancialWidgets()

            } catch (e: Exception) {
                Log.e("AccountDelete", "Error deleting account: ${e.message}", e)
                _state.update { it.copy(error = "Error deleting account: ${e.message}") }
            }
        }
    }
//    fun deleteAccount(account: AccountEntity) {
//        viewModelScope.launch {
//            accountRepository.deleteAccount(account)
//            fetchAllAccounts() // Refresh the list after deletion
//            widgetUpdateUtil.updateAllFinancialWidgets()
//        }
//    }

    // Fetch a single account by its ID
    fun getAccountById(id: Int, onSuccess: (AccountEntity?) -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isEditingAccount = true) } // Set edit mode to true
            val account = accountRepository.getAccountById(id)
            onSuccess(account)
            // If account is found, update UI state with its values
            account?.let { a ->
                _state.update { state ->
                    state.copy(
                        accountName = a.accountName,
                        cardColor1 = Color(a.cardColor1),
                        cardColor2 = Color(a.cardColor2),
                        balance = a.balance.toString(),
                        isMainAccount = a.isMainAccount,
                        currencyCode = a.currencyCode
                    )
                }
            }
        }
    }
    // Get transaction statistics for an account
    fun getTransactionStatsForAccount(accountId: Int, callback: (AccountTransactionStats) -> Unit) {
        viewModelScope.launch {
            val stats = accountRepository.getTransactionStatsForAccount(accountId)
            callback(stats)
        }
    }

    private fun ensureMainAccountExists() {
        viewModelScope.launch {
            val allAccounts = _state.value.accounts

            // If there's only one account, make it the main account
            if (allAccounts.size == 1 && !allAccounts[0].isMainAccount) {
                val account = allAccounts[0]
                accountRepository.updateMainAccount(account.id, true)
                _state.update { state ->
                    state.copy(mainAccountCurrencyCode = account.currencyCode)
                }

                if (!_state.value.isEditingAccount) {
                    _state.update { state ->
                        state.copy(currencyCode = account.currencyCode)
                    }
                }
                fetchAllAccounts()
            }

            // If there are multiple accounts but none is main, set the first one as main
            if (allAccounts.size > 1 && allAccounts.none { it.isMainAccount }) {
                val firstAccount = allAccounts[0]
                accountRepository.updateMainAccount(firstAccount.id, true)
                _state.update { state ->
                    state.copy(mainAccountCurrencyCode = firstAccount.currencyCode)
                }

                if (!_state.value.isEditingAccount) {
                    _state.update { state ->
                        state.copy(currencyCode = firstAccount.currencyCode)
                    }
                }
                fetchAllAccounts()
            }

            // Update main account currency if a main account exists
            val mainAccount = allAccounts.find { it.isMainAccount }
            mainAccount?.let {
                _state.update { state ->
                    state.copy(mainAccountCurrencyCode = it.currencyCode)
                }

                if (!_state.value.isEditingAccount) {
                    _state.update { state ->
                        state.copy(currencyCode = it.currencyCode)
                    }
                }
            }
        }
    }

    fun updateCollapsingFraction(fraction: Float) {
        if (!fraction.isNaN()) {
            val maxOffset = 180.dp
            val minOffset = 0.dp
            val offsetRange = maxOffset - minOffset
            val currentOffset = maxOffset - (offsetRange * fraction)

            _state.update {
                it.copy(
                    collapsingFraction = fraction,
                    currentOffset = currentOffset
                )
            }
        }
    }

    // Reset all input fields to default values
    private fun resetInputs() {
        _state.update { state ->
            state.copy(
                accountName = "",
                cardColor1 = null,
                cardColor2 = null,
                balance = "",
                isMainAccount = false,
                isEditingAccount = false,
                currencyCode = state.mainAccountCurrencyCode // Use the main account's currency code
            )
        }
        Log.d("CurrencyReset", "Reset currency to main account currency: ${_state.value.mainAccountCurrencyCode}")
    }
}