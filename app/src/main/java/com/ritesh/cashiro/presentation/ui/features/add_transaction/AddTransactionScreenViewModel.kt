package com.ritesh.cashiro.presentation.ui.features.add_transaction

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ritesh.cashiro.data.local.entity.AccountEntity
import com.ritesh.cashiro.data.local.entity.Recurrence
import com.ritesh.cashiro.data.local.entity.RecurrenceFrequency
import com.ritesh.cashiro.data.local.entity.TransactionEntity
import com.ritesh.cashiro.data.local.entity.TransactionType
import com.ritesh.cashiro.domain.repository.AccountRepository
import com.ritesh.cashiro.domain.repository.CategoryRepository
import com.ritesh.cashiro.domain.repository.TransactionRepository
import com.ritesh.cashiro.domain.utils.AccountEvent
import com.ritesh.cashiro.domain.utils.ActivityLogEntryFactory
import com.ritesh.cashiro.domain.utils.ActivityLogUtils
import com.ritesh.cashiro.domain.utils.AppEventBus
import com.ritesh.cashiro.domain.utils.TransactionEvent
import com.ritesh.cashiro.domain.utils.CurrencyUtils
import com.ritesh.cashiro.domain.utils.getCurrentDateInMillis
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenViewModel
import com.ritesh.cashiro.domain.utils.CurrencyEvent
import com.ritesh.cashiro.domain.utils.getNextDueDate
import com.ritesh.cashiro.presentation.ui.features.activity_logs.ActivityActionType
import com.ritesh.cashiro.widgets.WidgetUpdateUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddTransactionScreenViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository,
    private val widgetUpdateUtil: WidgetUpdateUtil,
    private val activityLogUtils: ActivityLogUtils,
    application: Application
) : AndroidViewModel(application) {
    private var _accountViewModel: AccountScreenViewModel? = null

    // Define the state flow for the transaction screen
    private val _state = MutableStateFlow(AddTransactionScreenState())
    val state: StateFlow<AddTransactionScreenState> = _state

    // Events for UI interactions
    private val _events = MutableSharedFlow<AddScreenEvent>()
    val events: SharedFlow<AddScreenEvent> = _events

    init {
        viewModelScope.launch {
            fetchAllTransactions()

            AppEventBus.events.collect { event ->
                when (event) {
                    is CurrencyEvent.AccountCurrencyChanged -> {
                        Log.d("Transaction ViewModel", "Account currency changed: ${event.accountId} to ${event.newCurrencyCode}")
                        // Use forceRefresh = true to ensure fresh data
                        fetchAllTransactions(forceRefresh = true)

                        // Update UI state if needed
                        if (_state.value.transactionAccountId == event.accountId) {
                            _state.update { it.copy(transactionCurrencyCode = event.newCurrencyCode) }
                        }
                    }

                    is CurrencyEvent.MainAccountCurrencyChanged -> {
                        Log.d("Transaction ViewModel", "Main account currency changed to: ${event.newCurrencyCode}")
                        fetchAllTransactions(forceRefresh = true)
                    }

                    is CurrencyEvent.ConversionRatesUpdated -> {
                        Log.d("Transaction ViewModel", "Conversion rates updated for: ${event.baseCurrency}")
                        updateConversionRates(event.baseCurrency, event.rates)
                        fetchAllTransactions(forceRefresh = true)
                    }
                }
            }
        }
    }

    // Event handler for all transaction events
    fun onEvent(event: AddTransactionEvent) {
        when (event) {
            // Basic Transaction Input Events
            is AddTransactionEvent.UpdateTitle -> updateState { it.copy(transactionTitle = event.title) }
            is AddTransactionEvent.UpdateAmount -> updateState { it.copy(transactionAmount = event.amount) }
            is AddTransactionEvent.UpdateDate -> updateState { it.copy(transactionDate = event.date) }
            is AddTransactionEvent.UpdateTime -> updateState { it.copy(transactionTime = event.time) }
            is AddTransactionEvent.UpdateCategoryId -> updateState { it.copy(transactionCategoryId = event.categoryId) }
            is AddTransactionEvent.UpdateSubCategoryId -> updateState { it.copy(transactionSubCategoryId = event.subCategoryId) }
            is AddTransactionEvent.UpdateAccountId -> updateState { it.copy(transactionAccountId = event.accountId) }
            is AddTransactionEvent.UpdateDestinationAccountId -> updateState { it.copy(transactionDestinationAccountId = event.accountId) }
            is AddTransactionEvent.UpdateNote -> updateState { it.copy(transactionNote = event.note) }
//            is AddTransactionEvent.UpdateMode -> updateState { it.copy(transactionMode = event.mode) }
            is AddTransactionEvent.UpdateMode -> {
                updateState { currentState ->
                    val newState = currentState.copy(transactionMode = event.mode)

                    // If switching to Transfer mode, reset category to ID = 1 and clear subcategory
                    if (event.mode == "Transfer") {
                        newState.copy(
                            transactionCategoryId = 1,
                            transactionSubCategoryId = 0
                        )
                    } else {
                        newState
                    }
                }
            }
            is AddTransactionEvent.UpdateType -> {
                updateState { currentState ->
                    val newState = currentState.copy(transactionType = event.type)

                    // FIX: Set default values for subscription/repetitive transactions
                    if ((event.type == TransactionType.SUBSCRIPTION || event.type == TransactionType.REPETITIVE) &&
                        currentState.recurrenceFrequency == RecurrenceFrequency.NONE) {

                        newState.copy(
                            recurrenceFrequency = RecurrenceFrequency.MONTHLY,
                            recurrenceInterval = if (currentState.recurrenceInterval <= 0) 1 else currentState.recurrenceInterval
                        )
                    } else {
                        newState
                    }
                }
            }

            // Recurrence Events
            is AddTransactionEvent.UpdateRecurrenceFrequency -> updateState { it.copy(recurrenceFrequency = event.frequency) }
            is AddTransactionEvent.UpdateRecurrenceInterval -> updateState { it.copy(recurrenceInterval = event.interval) }
            is AddTransactionEvent.UpdateRecurrenceEndDate -> updateState { it.copy(recurrenceEndDate = event.endDate) }
            is AddTransactionEvent.UpdateIsPaid -> updateState { it.copy(isPaid = event.isPaid) }
            is AddTransactionEvent.UpdateNextDueDate -> updateState { it.copy(nextDueDate = event.dueDate) }

            // Bottom Sheet State Events
            is AddTransactionEvent.SetTransactionTypeMenuOpened -> updateState { it.copy(isTransactionTypeMenuOpened = event.isOpen) }
            is AddTransactionEvent.SetTransactionTypeInfoSheetOpen -> updateState { it.copy(isTransactionTypeInfoSheetOpen = event.isOpen) }
            is AddTransactionEvent.SetTransactionAmountSheetOpen -> updateState { it.copy(isTransactionAmountSheetOpen = event.isOpen) }
            is AddTransactionEvent.SetRecurrenceMenuOpen -> updateState { it.copy(isRecurrenceMenuOpen = event.isOpen) }
            is AddTransactionEvent.SetRecurrenceBottomSheetOpen -> updateState { it.copy(isRecurrenceBottomSheetOpen = event.isOpen) }
            is AddTransactionEvent.SetCustomInputBottomSheetOpen -> updateState { it.copy(isCustomInputBottomSheetOpen = event.isOpen) }
            is AddTransactionEvent.SetEndDateSelected -> updateState { it.copy(isEndDateSelected = event.isSelected) }
            is AddTransactionEvent.SetOpenEndDatePicker -> updateState { it.copy(openEndDatePicker = event.isOpen) }

            // Dialog State Events
            is AddTransactionEvent.SetOpenDatePickerDialog -> updateState { it.copy(openDatePickerDialog = event.isOpen) }
            is AddTransactionEvent.SetTransactionDetailsSheetOpen -> updateState { it.copy(isTransactionDetailsSheetOpen = event.isOpen) }
            is AddTransactionEvent.SetCategoryMenuOpened -> updateState { it.copy(isCategoryMenuOpened = event.isOpen) }
            is AddTransactionEvent.SetAccountMenuOpened -> updateState { it.copy(isAccountMenuOpened = event.isOpen) }
            is AddTransactionEvent.SetAmountMenuOpened -> updateState { it.copy(isAmountMenuOpened = event.isOpen) }

            // Tab Selection Events
            is AddTransactionEvent.SelectTab -> updateState { it.copy(selectedTabIndex = event.index) }

            // Transaction Actions
            is AddTransactionEvent.AddTransaction -> addTransaction()
            is AddTransactionEvent.UpdateTransaction -> updateTransaction(event.oldTransaction, event.newTransaction)
            is AddTransactionEvent.DeleteTransaction -> deleteTransaction(event.transaction)

            // Data Loading Events
            is AddTransactionEvent.LoadTransactionById -> loadTransactionById(event.id)
            is AddTransactionEvent.ClearTransactionFields -> clearTransactionFields()
            is AddTransactionEvent.FetchAllTransactions -> fetchAllTransactions()

            // Transaction Type Operations
            is AddTransactionEvent.FetchTransactionsByType -> fetchTransactionsByType(event.transactionType)
            is AddTransactionEvent.FetchUnpaidTransactions -> fetchUnpaidTransactions()

            // Transaction Status Updates
            is AddTransactionEvent.MarkTransactionAsPaid -> markTransactionAsPaid(event.transaction)
            is AddTransactionEvent.MarkTransactionAsCollected -> markTransactionAsCollected(event.transaction)
            is AddTransactionEvent.MarkTransactionAsSettled -> markTransactionAsSettled(event.transaction)

            is AddTransactionEvent.MarkTransactionAsUnpaid -> markTransactionAsUnpaid(event.transaction)
            is AddTransactionEvent.MarkTransactionAsNotCollected -> markTransactionAsNotCollected(event.transaction)
            is AddTransactionEvent.MarkTransactionAsUnsettled -> markTransactionAsUnsettled(event.transaction)
            // Navigation Events
            is AddTransactionEvent.NavigateBack -> emitNavigateBackEvent()

            // Calculator Events
            is AddTransactionEvent.UpdateCalculatedResult -> updateState { it.copy(calculatedResultAmount = event.result) }

            // Validation Events
            is AddTransactionEvent.ValidateTransaction -> validateTransaction()

            // Error Handling
            is AddTransactionEvent.ShowError -> updateState { it.copy(validationError = event.message) }
            is AddTransactionEvent.ClearError -> updateState { it.copy(validationError = null) }
        }
    }

    private fun updateState(update: (AddTransactionScreenState) -> AddTransactionScreenState) {
        _state.update(update)
    }

    private fun updateConversionRates(baseCurrency: String, rates: Map<String, Double>) {
        _state.update {
            it.copy(
                conversionRates = rates,
                baseCurrencyCode = baseCurrency
            )
        }
    }

    private fun validateTransaction() {
        val currentState = _state.value
        updateState {
            it.copy(
                isAddFormValid = it.isValidForAdding(),
                isUpdateFormValid = it.isValidForUpdating()
            )
        }
    }

    fun setAccountViewModel(viewModel: AccountScreenViewModel) {
        _accountViewModel = viewModel
    }

    // In AddTransactionScreenViewModel
    fun fetchAllTransactions(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            try {
                Log.d("TransactionsScreen", "Loading all transactions, forceRefresh: $forceRefresh")

                // Set loading state if forcing refresh
                if (forceRefresh) {
                    _state.update { it.copy(isLoading = true) }
                }

                // Always get fresh data from the repository
                val allTransactions = transactionRepository.getAllTransactions()
                Log.d("TransactionsScreen", "Loaded ${allTransactions.size} transactions")

                // Update state with new transactions
                updateState { it.copy(
                    transactions = allTransactions,
                    isLoading = false
                ) }

                // If forceRefresh is true, you could also refresh related data
                if (forceRefresh) {
                    // You might want to refresh account data if transactions are refreshed
                    _accountViewModel?.refreshAccounts()
                }
            } catch (e: Exception) {
                Log.e("TransactionsScreen", "Error loading transactions: ${e.message}", e)
                _state.update { it.copy(isLoading = false, validationError = "Error loading data: ${e.message}") }
            }
        }
    }
    fun addTransaction() {
        Log.d("TransactionDebug", "Starting transaction validation")
        val currentState = _state.value
        Log.d("TransactionDebug", "checking Form validation: ${currentState.isValidForAdding()}")

        if (currentState.isValidForAdding()) {
            viewModelScope.launch {
                try {
                    Log.d("TransactionDebug", "Transaction is valid, creating entity")
                    val transaction = createTransactionEntity()
                    Log.d("TransactionDebug", "Transaction entity created: $transaction")

                    // Verify the account exists before adding the transaction
                    val sourceAccount = accountRepository.getAccountById(transaction.accountId)
                    Log.d("TransactionDebug", "sourceAccount: $sourceAccount")
                    if (sourceAccount == null) {
                        updateState { it.copy(validationError = "The selected account does not exist") }
                        _events.emit(AddScreenEvent.ValidationError("The selected account does not exist"))
                        return@launch
                    }

                    // For transfers, verify destination account exists
                    var destinationAccount: AccountEntity? = null
                    if (transaction.mode == "Transfer" && transaction.destinationAccountId != null) {
                        Log.d("TransactionDebug", "Transaction Mode: ${transaction.mode}")
                        Log.d("TransactionDebug", "Transaction Destination Id: ${transaction.destinationAccountId}")
                        destinationAccount = accountRepository.getAccountById(transaction.destinationAccountId)
                        Log.d("TransactionDebug", "destinationAccount: $destinationAccount")
                        if (destinationAccount == null) {
                            updateState { it.copy(validationError = "The destination account does not exist") }
                            _events.emit(AddScreenEvent.ValidationError("The destination account does not exist"))
                            return@launch
                        }
                    }

                    val finalTransaction = when (transaction.transactionType) {
                        TransactionType.SUBSCRIPTION, TransactionType.REPETITIVE -> {
                            // Validate recurrence data
                            val recurrence = transaction.recurrence
                            if (recurrence == null || recurrence.frequency == RecurrenceFrequency.NONE ||
                                recurrence.interval <= 0 || recurrence.endRecurrenceDate == null) {
                                updateState { it.copy(validationError = "Subscription/Repetitive transactions require recurrence settings (frequency, interval, and end date)") }
                                _events.emit(AddScreenEvent.ValidationError("Please set up recurrence settings for this subscription/repetitive transaction"))
                                return@launch
                            }

                            val currentDateMillis = getCurrentDateInMillis()
                            val isCurrentOrPastDate = transaction.date <= currentDateMillis

                            // Calculate nextDueDate for the current transaction
                            val nextDueDate = getNextDueDate(
                                transaction.date,
                                recurrence.frequency,
                                recurrence.interval,
                                recurrence.endRecurrenceDate
                            )

                            Log.d("RecurringTransaction", "Creating subscription/repetitive transaction")
                            Log.d("RecurringTransaction", "Transaction date: ${transaction.date}")
                            Log.d("RecurringTransaction", "Current date: $currentDateMillis")
                            Log.d("RecurringTransaction", "Is current or past: $isCurrentOrPastDate")
                            Log.d("RecurringTransaction", "Next due date: $nextDueDate")

                            // If the transaction date is current or past, mark it as paid
                            // If it's a future date, keep it as unpaid
                            transaction.copy(
                                isPaid = isCurrentOrPastDate,
                                nextDueDate = nextDueDate
                            )
                        }

                        TransactionType.UPCOMING -> {
                            // For upcoming transactions, set the due date to the transaction date/time
                            Log.d("UpcomingTransaction", "Creating upcoming transaction")
                            Log.d("UpcomingTransaction", "Transaction date: ${transaction.date}")
                            Log.d("UpcomingTransaction", "Transaction time: ${transaction.time}")

                            // Set nextDueDate to the transaction date for upcoming transactions
                            transaction.copy(
                                nextDueDate = transaction.date,
                                isPaid = false // Upcoming transactions start as unpaid
                            )
                        }

                        else -> {
                            transaction
                        }
                    }
                    // For transfers, ensure categoryId is valid
                    if (finalTransaction.mode == "Transfer") {
                        val transferCategoryId = 1 // Use the actual ID of a valid Transfer category
                        val modifiedTransaction = finalTransaction.copy(categoryId = transferCategoryId)
                        transactionRepository.addTransaction(modifiedTransaction)
                    } else {
                        transactionRepository.addTransaction(finalTransaction)
                    }

                    // Handle account balance updates based on transaction mode and type
                    val success = when (finalTransaction.mode) {
                        "Expense" -> {
                            // For subscription/repetitive transactions, only update balance if marked as paid
                            val shouldUpdateBalance = when (finalTransaction.transactionType) {
                                TransactionType.SUBSCRIPTION, TransactionType.REPETITIVE -> finalTransaction.isPaid
                                TransactionType.UPCOMING -> false // Don't update balance for future upcoming transactions
                                else -> true // Update balance for default expense transactions
                            }

                            if (shouldUpdateBalance) {
                                accountRepository.updateAccountBalance(
                                    finalTransaction.accountId,
                                    finalTransaction.amount,
                                    isExpense = true // Expense: subtract from balance
                                )
                            } else {
                                true // Success, but no balance update needed
                            }
                        }
                        "Income" -> {
                            // Similar logic for income transactions
                            val shouldUpdateBalance = when (finalTransaction.transactionType) {
                                TransactionType.SUBSCRIPTION, TransactionType.REPETITIVE -> finalTransaction.isPaid
                                TransactionType.UPCOMING -> false
                                else -> true
                            }

                            if (shouldUpdateBalance) {
                                accountRepository.updateAccountBalance(
                                    finalTransaction.accountId,
                                    finalTransaction.amount,
                                    isExpense = false // Income: add to balance
                                )
                            } else {
                                true
                            }
                        }
                        "Transfer" -> {
                            // Transfers always update balance regardless of type
                            finalTransaction.destinationAccountId?.let { destAccountId ->
                                // Get the source and destination accounts
                                val sourceCurrency = sourceAccount.currencyCode
                                val destCurrency = destinationAccount?.currencyCode ?: "usd"

                                // Get conversion rates
                                val conversionRates = _accountViewModel?.mainCurrencyConversionRates?.value ?: emptyMap()

                                // Calculate the converted amount for the destination account
                                val sourceAmount = finalTransaction.amount
                                val destAmount = CurrencyUtils.calculateTransferDestinationAmount(
                                    sourceAmount,
                                    sourceCurrency,
                                    destCurrency,
                                    conversionRates
                                )

                                Log.d("TransferDebug", "Handling transfer from ${finalTransaction.accountId} to $destAccountId")
                                Log.d("TransferDebug", "Source amount: $sourceAmount $sourceCurrency")
                                Log.d("TransferDebug", "Destination amount: $destAmount $destCurrency")

                                accountRepository.handleTransferWithConversion(
                                    finalTransaction.accountId,     // Source account
                                    destAccountId,                  // Destination account
                                    sourceAmount,                   // Amount from source account (original)
                                    destAmount                      // Amount to destination account (converted)
                                )
                            } ?: run {
                                Log.d("TransferDebug", "Destination account ID is null for transfer!")
                                false
                            }
                        }
                        else -> false
                    }

                    Log.d("TransferDebug", "Account balance update success: $success")
                    if (!success) {
                        // If the balance update failed, delete the transaction
                        transactionRepository.deleteTransaction(finalTransaction)
                        updateState { it.copy(validationError = "Failed to update account balance") }
                        _events.emit(AddScreenEvent.ValidationError("Failed to update account balance"))
                        return@launch
                    }

                    logTransactionCreated(finalTransaction, sourceAccount, destinationAccount)

                    _accountViewModel?.refreshAccounts()

                    // Generate next recurring transaction for subscription/repetitive types if current transaction is paid
                    if (isRecurringTransaction(finalTransaction) && finalTransaction.isPaid) {
                        Log.d("RecurringTransaction", "Transaction is paid, generating next recurring transaction")
                        generateNextRecurringTransactionIfNeeded(finalTransaction)
                    }
// NEW: Update widgets after successful transaction creation
                    widgetUpdateUtil.updateAllFinancialWidgets()
                    clearTransactionFields()
                    AppEventBus.tryEmitEvent(TransactionEvent.TransactionsUpdated)
                    _events.emit(AddScreenEvent.TransactionAdded)

                } catch (e: Exception) {
                    updateState { it.copy(validationError = "An error occurred: ${e.message}") }
                    _events.emit(AddScreenEvent.ValidationError("An error occurred: ${e.message}"))
                }
            }
        } else {
            viewModelScope.launch {
                // Enhanced error message for subscription/repetitive transactions
                val errorMessage = if (currentState.transactionType == TransactionType.SUBSCRIPTION ||
                    currentState.transactionType == TransactionType.REPETITIVE) {
                    if (currentState.recurrenceFrequency == RecurrenceFrequency.NONE ||
                        currentState.recurrenceInterval <= 0 ||
                        currentState.recurrenceEndDate == null) {
                        "Please set up recurrence settings (frequency, interval, and end date) for subscription/repetitive transactions"
                    } else {
                        "Please fill all required fields"
                    }
                } else {
                    "Please fill all required fields"
                }

                updateState { it.copy(validationError = errorMessage) }
                _events.emit(AddScreenEvent.ValidationError(errorMessage))
            }
        }
    }

    private fun updateTransaction(oldTransaction: TransactionEntity, newTransaction: TransactionEntity) {
        viewModelScope.launch {
            try {
                // Check if this is a recurrence update for subscription/repetitive transactions
                val isRecurrenceUpdate = (oldTransaction.transactionType == TransactionType.SUBSCRIPTION ||
                        oldTransaction.transactionType == TransactionType.REPETITIVE) &&
                        (oldTransaction.recurrence != newTransaction.recurrence)

                if (isRecurrenceUpdate) {
                    Log.d("RecurrenceUpdate", "Detected recurrence settings change for ${oldTransaction.title}")

                    // Update future recurring transactions with new settings
                    transactionRepository.updateFutureRecurringTransactions(
                        oldTransaction,
                        newTransaction.recurrence ?: Recurrence(RecurrenceFrequency.NONE, 1, null)
                    )
                }

                // Handle account balance updates for special transaction types
                when (oldTransaction.transactionType) {
                    TransactionType.UPCOMING, TransactionType.SUBSCRIPTION, TransactionType.REPETITIVE -> {
                        // For upcoming/subscription/repetitive transactions, we need to consider the isPaid state

                        // First, reverse the old transaction's effect if it was paid
                        if (oldTransaction.isPaid) {
                            val isExpense = oldTransaction.mode == "Income" // Reverse the effect
                            accountRepository.updateAccountBalance(
                                oldTransaction.accountId,
                                oldTransaction.amount,
                                isExpense = isExpense
                            )
                        }

                        // Then apply the new transaction's effect if it should be paid
                        if (newTransaction.isPaid) {
                            val isExpense = newTransaction.mode == "Expense"
                            accountRepository.updateAccountBalance(
                                newTransaction.accountId,
                                newTransaction.amount,
                                isExpense = isExpense
                            )
                        }
                    }

                    TransactionType.LENT -> {
                        // For lent transactions, reverse collection effect if it was collected
                        if (oldTransaction.isCollected) {
                            accountRepository.updateAccountBalance(
                                oldTransaction.accountId,
                                oldTransaction.amount,
                                isExpense = true // Remove the money that was added back
                            )
                        }

                        // Apply new collection effect if needed
                        if (newTransaction.isCollected) {
                            accountRepository.updateAccountBalance(
                                newTransaction.accountId,
                                newTransaction.amount,
                                isExpense = false // Add money back
                            )
                        }
                    }

                    TransactionType.BORROWED -> {
                        // For borrowed transactions, reverse settlement effect if it was settled
                        if (oldTransaction.isSettled) {
                            accountRepository.updateAccountBalance(
                                oldTransaction.accountId,
                                oldTransaction.amount,
                                isExpense = false // Add back the money that was deducted
                            )
                        }

                        // Apply new settlement effect if needed
                        if (newTransaction.isSettled) {
                            accountRepository.updateAccountBalance(
                                newTransaction.accountId,
                                newTransaction.amount,
                                isExpense = true // Deduct money
                            )
                        }
                    }

                    else -> {
                        // For regular transactions, use the original logic
                        when (oldTransaction.mode) {
                            "Expense" -> {
                                accountRepository.updateAccountBalance(
                                    oldTransaction.accountId,
                                    oldTransaction.amount,
                                    isExpense = false  // Reverse the expense
                                )
                            }
                            "Income" -> {
                                accountRepository.updateAccountBalance(
                                    oldTransaction.accountId,
                                    oldTransaction.amount,
                                    isExpense = true  // Reverse the income
                                )
                            }
                            "Transfer" -> {
                                // Reverse the old transfer if it had a destination
                                oldTransaction.destinationAccountId?.let { destAccountId ->
                                    accountRepository.handleTransfer(
                                        destAccountId,                // Source (original destination)
                                        oldTransaction.accountId,     // Destination (original source)
                                        oldTransaction.amount
                                    )
                                }
                            }
                        }

                        // Apply the effect of the new transaction for regular transactions
                        when (newTransaction.mode) {
                            "Expense" -> {
                                accountRepository.updateAccountBalance(
                                    newTransaction.accountId,
                                    newTransaction.amount,
                                    isExpense = true
                                )
                            }
                            "Income" -> {
                                accountRepository.updateAccountBalance(
                                    newTransaction.accountId,
                                    newTransaction.amount,
                                    isExpense = false
                                )
                            }
                            "Transfer" -> {
                                // Apply the new transfer
                                newTransaction.destinationAccountId?.let { destAccountId ->
                                    val sourceAccount = accountRepository.getAccountById(newTransaction.accountId)
                                    val destAccount = accountRepository.getAccountById(destAccountId)

                                    if (sourceAccount != null && destAccount != null) {
                                        // Get the source and destination currencies
                                        val sourceCurrency = sourceAccount.currencyCode
                                        val destCurrency = destAccount.currencyCode

                                        // Get conversion rates
                                        val conversionRates = _state.value.conversionRates

                                        // Calculate the converted amount for the destination account
                                        val sourceAmount = newTransaction.amount
                                        val destAmount = CurrencyUtils.calculateTransferDestinationAmount(
                                            sourceAmount,
                                            sourceCurrency,
                                            destCurrency,
                                            conversionRates
                                        )

                                        accountRepository.handleTransferWithConversion(
                                            newTransaction.accountId,  // Source
                                            destAccountId,             // Destination
                                            sourceAmount,              // Source amount
                                            destAmount                 // Converted destination amount
                                        )
                                    } else {
                                        accountRepository.handleTransfer(
                                            newTransaction.accountId,  // Source
                                            destAccountId,             // Destination
                                            newTransaction.amount
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                val updatedTransaction = if (newTransaction.originalCurrencyCode == null) {
                    val account = accountRepository.getAccountById(newTransaction.accountId)
                    newTransaction.copy(originalCurrencyCode = account?.currencyCode)
                } else {
                    newTransaction
                }

//                // Save the updated transaction
//                _accountViewModel?.refreshAccounts()
//                transactionRepository.updateTransaction(updatedTransaction)
                val finalUpdatedTransaction = if (updatedTransaction.mode == "Transfer") {
                    val transferCategoryId = 1 // Use the actual ID of a valid Transfer category
                    updatedTransaction.copy(
                        categoryId = transferCategoryId,
                        subCategoryId = null // Clear subcategory for transfers
                    )
                } else {
                    updatedTransaction
                }
                _accountViewModel?.refreshAccounts()
                transactionRepository.updateTransaction(finalUpdatedTransaction)
                fetchAllTransactions()

                logTransactionUpdated(finalUpdatedTransaction)

                widgetUpdateUtil.updateAllFinancialWidgets()
                AppEventBus.tryEmitEvent(TransactionEvent.TransactionsUpdated)
                _events.emit(AddScreenEvent.TransactionUpdated)
            } catch (e: Exception) {
                Log.e("TransactionUpdate", "Error updating transaction", e)
                updateState { it.copy(validationError = "Error updating transaction: ${e.message}") }
            }
        }
    }

    private fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(transaction)
            when (transaction.mode) {
                "Expense" -> {
                    accountRepository.updateAccountBalance(
                        transaction.accountId,
                        transaction.amount,
                        isExpense = false  // Reverse the expense
                    )
                }
                "Income" -> {
                    accountRepository.updateAccountBalance(
                        transaction.accountId,
                        transaction.amount,
                        isExpense = true  // Reverse the income
                    )
                }
                "Transfer" -> {
                    // Reverse the transfer
                    transaction.destinationAccountId?.let { destAccountId ->
                        accountRepository.handleTransfer(
                            destAccountId,          // Now the source is the original destination
                            transaction.accountId,  // And destination is the original source
                            transaction.amount
                        )
                    }
                }
            }
            logTransactionDeleted(transaction)
            fetchAllTransactions() // Update the transactions list
            widgetUpdateUtil.updateAllFinancialWidgets()

            AppEventBus.tryEmitEvent(TransactionEvent.TransactionsUpdated)
            _events.emit(AddScreenEvent.TransactionDeleted)
        }
    }
    fun deleteTransactions(transactionIds: List<Int>) {
        viewModelScope.launch {
            try {
                transactionIds.forEach { id ->
                    // Get the transaction before deleting it
                    val transaction = transactionRepository.getTransactionById(id)

                    if (transaction != null) {
                        // Restore account balance based on transaction details
                        restoreAccountBalanceForDeletedTransaction(transaction)

                        // Delete the transaction
                        transactionRepository.deleteTransactionById(id)
                    }
                }

                // Refresh the transactions list and accounts after deletion
                _accountViewModel?.refreshAccounts()
                fetchAllTransactions()

                widgetUpdateUtil.updateAllFinancialWidgets()

                AppEventBus.tryEmitEvent(TransactionEvent.TransactionsUpdated)
                AppEventBus.tryEmitEvent(AccountEvent.AccountsUpdated)

            } catch (e: Exception) {
                Log.e("TransactionDeletion", "Error deleting transactions: ${e.message}", e)
                updateState { it.copy(validationError = "Error deleting transactions: ${e.message}") }
            }
        }
    }

    private suspend fun restoreAccountBalanceForDeletedTransaction(transaction: TransactionEntity) {
        try {
            Log.d("TransactionDeletion", "Restoring balance for transaction: ${transaction.title} (${transaction.mode}, ${transaction.transactionType})")

            when (transaction.transactionType) {
                TransactionType.DEFAULT -> {
                    // For default transactions, restore balance based on mode
                    when (transaction.mode) {
                        "Expense" -> {
                            // Add money back to account (reverse the expense)
                            accountRepository.updateAccountBalance(
                                transaction.accountId,
                                transaction.amount,
                                isExpense = false
                            )
                            Log.d("TransactionDeletion", "Restored expense: +${transaction.amount} to account ${transaction.accountId}")
                        }
                        "Income" -> {
                            // Subtract money from account (reverse the income)
                            accountRepository.updateAccountBalance(
                                transaction.accountId,
                                transaction.amount,
                                isExpense = true
                            )
                            Log.d("TransactionDeletion", "Restored income: -${transaction.amount} from account ${transaction.accountId}")
                        }
                        "Transfer" -> {
                            // Reverse the transfer
                            transaction.destinationAccountId?.let { destAccountId ->
                                accountRepository.handleTransfer(
                                    destAccountId,              // Now source (original destination)
                                    transaction.accountId,      // Now destination (original source)
                                    transaction.amount
                                )
                                Log.d("TransactionDeletion", "Restored transfer: +${transaction.amount} to account ${transaction.accountId}, -${transaction.amount} from account $destAccountId")
                            }
                        }
                    }
                }

                TransactionType.UPCOMING -> {
                    // Only restore if the transaction was marked as paid
                    if (transaction.isPaid) {
                        when (transaction.mode) {
                            "Expense" -> {
                                accountRepository.updateAccountBalance(
                                    transaction.accountId,
                                    transaction.amount,
                                    isExpense = false
                                )
                            }
                            "Income" -> {
                                accountRepository.updateAccountBalance(
                                    transaction.accountId,
                                    transaction.amount,
                                    isExpense = true
                                )
                            }
                        }
                        Log.d("TransactionDeletion", "Restored paid upcoming transaction")
                    }
                }

                TransactionType.SUBSCRIPTION, TransactionType.REPETITIVE -> {
                    // Only restore if the transaction was marked as paid
                    if (transaction.isPaid) {
                        when (transaction.mode) {
                            "Expense" -> {
                                accountRepository.updateAccountBalance(
                                    transaction.accountId,
                                    transaction.amount,
                                    isExpense = false
                                )
                            }
                            "Income" -> {
                                accountRepository.updateAccountBalance(
                                    transaction.accountId,
                                    transaction.amount,
                                    isExpense = true
                                )
                            }
                        }
                        Log.d("TransactionDeletion", "Restored paid recurring transaction")
                    }
                }

                TransactionType.LENT -> {
                    if (transaction.isCollected) {
                        // If money was collected, remove it from account (reverse the collection)
                        accountRepository.updateAccountBalance(
                            transaction.accountId,
                            transaction.amount,
                            isExpense = true
                        )
                    } else {
                        // If money was not collected, add it back to account (reverse the lending)
                        accountRepository.updateAccountBalance(
                            transaction.accountId,
                            transaction.amount,
                            isExpense = false
                        )
                    }
                    Log.d("TransactionDeletion", "Restored lent transaction")
                }

                TransactionType.BORROWED -> {
                    if (transaction.isSettled) {
                        // If debt was settled, add money back to account (reverse the settlement)
                        accountRepository.updateAccountBalance(
                            transaction.accountId,
                            transaction.amount,
                            isExpense = false
                        )
                    } else {
                        // If debt was not settled, remove money from account (reverse the borrowing)
                        accountRepository.updateAccountBalance(
                            transaction.accountId,
                            transaction.amount,
                            isExpense = true
                        )
                    }
                    Log.d("TransactionDeletion", "Restored borrowed transaction")
                }
            }

        } catch (e: Exception) {
            Log.e("TransactionDeletion", "Error restoring balance for transaction ${transaction.id}: ${e.message}", e)
            throw e
        }
    }

    private fun loadTransactionById(id: Int) {
        viewModelScope.launch {
            try {
                val transaction = transactionRepository.getTransactionById(id)
                if (transaction != null) {
                    // Initialize state with the transaction data for editing
                    updateState { currentState ->
                        currentState.copy(
                            transactionTitle = transaction.title,
                            transactionAmount = transaction.amount.toString(),
                            transactionDate = transaction.date,
                            transactionTime = transaction.time,
                            transactionCategoryId = transaction.categoryId,
                            transactionSubCategoryId = transaction.subCategoryId ?: 0,
                            transactionAccountId = transaction.accountId,
                            transactionDestinationAccountId = transaction.destinationAccountId,
                            transactionNote = transaction.note,
                            transactionMode = transaction.mode,
                            transactionType = transaction.transactionType,
                            recurrenceFrequency = transaction.recurrence?.frequency ?: RecurrenceFrequency.NONE,
                            recurrenceInterval = transaction.recurrence?.interval ?: 1,
                            recurrenceEndDate = transaction.recurrence?.endRecurrenceDate,
                            // FIX: Properly set end date selection state
                            isEndDateSelected = transaction.recurrence?.endRecurrenceDate != null,
                            isPaid = transaction.isPaid,
                            nextDueDate = transaction.nextDueDate,
                            isUpdateTransaction = true,
                            transactionId = transaction.id,
                            currentTransaction = transaction,
                            // Set tab items and initial tab index based on loaded transaction
                            tabItems = currentState.getAvailableTabItems(),
                            selectedTabIndex = currentState.getInitialTabIndex()
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("AddScreenViewModel", "Error fetching transaction by ID: ${e.message}", e)
                updateState { it.copy(validationError = "Error loading transaction: ${e.message}") }
            }
        }
    }

    private fun fetchTransactionsByType(transactionType: TransactionType) {
        viewModelScope.launch {
            val filteredTransactions = transactionRepository.getAllTransactionsByMode(transactionType.name)
            updateState { it.copy(transactions = filteredTransactions) }
        }
    }

    private fun fetchUnpaidTransactions() {
        viewModelScope.launch {
            val unpaidTransactions = transactionRepository.getUnpaidTransactions()
            updateState { it.copy(transactions = unpaidTransactions) }
        }
    }

    private fun markTransactionAsPaid(transaction: TransactionEntity) {
        viewModelScope.launch {
            try {
                when (transaction.transactionType) {
                    TransactionType.UPCOMING -> {
                        // For upcoming transactions: mark as paid and update account based on mode
                        val updatedTransaction = transaction.copy(isPaid = true)
                        transactionRepository.updateTransaction(updatedTransaction)

                        // Update account balance based on transaction mode
                        val isExpense = transaction.mode == "Expense"
                        accountRepository.updateAccountBalance(
                            transaction.accountId,
                            transaction.amount,
                            isExpense = isExpense
                        )
                    }

                    TransactionType.SUBSCRIPTION, TransactionType.REPETITIVE -> {
                        // For subscription/repetitive: mark current as paid, update account, create next transaction
                        val updatedTransaction = transaction.copy(isPaid = true)
                        transactionRepository.updateTransaction(updatedTransaction)

                        Log.d("RecurringTransaction", "Marking transaction as paid: ${transaction.title}")
                        Log.d("RecurringTransaction", "Transaction type: ${transaction.transactionType}")
                        Log.d("RecurringTransaction", "Transaction date: ${transaction.date}")

                        // Update account balance based on transaction mode
                        val isExpense = transaction.mode == "Expense"
                        accountRepository.updateAccountBalance(
                            transaction.accountId,
                            transaction.amount,
                            isExpense = isExpense
                        )

                        Log.d("RecurringTransaction", "Account balance updated, now generating next transaction")

                        // Generate next recurring transaction - use updatedTransaction to ensure isPaid = true
                        generateNextRecurringTransactionIfNeeded(updatedTransaction)
                    }

                    else -> {
                        // For other transaction types, just mark as paid without account balance change
                        val updatedTransaction = transaction.copy(isPaid = true)
                        transactionRepository.updateTransaction(updatedTransaction)
                    }
                }
                logTransactionStatusChange(transaction, ActivityActionType.TRANSACTION_MARKED_PAID)
                // Refresh data
                fetchAllTransactions()
                _accountViewModel?.refreshAccounts()
                AppEventBus.tryEmitEvent(TransactionEvent.TransactionsUpdated)
                _events.emit(AddScreenEvent.TransactionMarkedAsPaid)

            } catch (e: Exception) {
                Log.e("MarkPaidError", "Error marking transaction as paid: ${e.message}", e)
                updateState { it.copy(validationError = "Error updating transaction: ${e.message}") }
            }
        }
    }

    private fun markTransactionAsUnpaid(transaction: TransactionEntity) {
        viewModelScope.launch {
            try {
                when (transaction.transactionType) {
                    TransactionType.UPCOMING, TransactionType.SUBSCRIPTION, TransactionType.REPETITIVE -> {
                        // Mark as unpaid and reverse the account balance effect
                        val updatedTransaction = transaction.copy(isPaid = false)
                        transactionRepository.updateTransaction(updatedTransaction)

                        // Reverse account balance based on transaction mode
                        val isExpense = transaction.mode == "Income" // Reverse: Income becomes expense to reverse the effect
                        accountRepository.updateAccountBalance(
                            transaction.accountId,
                            transaction.amount,
                            isExpense = isExpense
                        )
                    }

                    else -> {
                        // For other transaction types, just mark as unpaid without account balance change
                        val updatedTransaction = transaction.copy(isPaid = false)
                        transactionRepository.updateTransaction(updatedTransaction)
                    }
                }
                logTransactionStatusChange(transaction, ActivityActionType.TRANSACTION_MARKED_UNPAID)

                // Refresh data
                fetchAllTransactions()
                _accountViewModel?.refreshAccounts()
                AppEventBus.tryEmitEvent(TransactionEvent.TransactionsUpdated)
                _events.emit(AddScreenEvent.TransactionMarkedAsUnpaid)

            } catch (e: Exception) {
                Log.e("MarkUnpaidError", "Error marking transaction as unpaid: ${e.message}", e)
                updateState { it.copy(validationError = "Error updating transaction: ${e.message}") }
            }
        }
    }

    private fun markTransactionAsCollected(transaction: TransactionEntity) {
        viewModelScope.launch {
            try {
                when (transaction.transactionType) {
                    TransactionType.LENT -> {
                        // For lent transactions: mark as collected and add amount back to account
                        val updatedTransaction = transaction.copy(isCollected = true)
                        transactionRepository.updateTransaction(updatedTransaction)

                        // Add the lent amount back to account (money is being returned)
                        // For LENT transactions, this should always add money back regardless of mode
                        accountRepository.updateAccountBalance(
                            transaction.accountId,
                            transaction.amount,
                            isExpense = false // Always add back for collected lent money
                        )
                    }

                    else -> {
                        val updatedTransaction = transaction.copy(isCollected = true)
                        transactionRepository.updateTransaction(updatedTransaction)
                    }
                }
                logTransactionStatusChange(transaction, ActivityActionType.TRANSACTION_MARKED_COLLECTED)

                fetchAllTransactions()
                _accountViewModel?.refreshAccounts()
                AppEventBus.tryEmitEvent(TransactionEvent.TransactionsUpdated)
                _events.emit(AddScreenEvent.TransactionMarkedAsCollected)

            } catch (e: Exception) {
                Log.e("MarkCollectedError", "Error marking transaction as collected: ${e.message}", e)
                updateState { it.copy(validationError = "Error updating transaction: ${e.message}") }
            }
        }
    }

    private fun markTransactionAsNotCollected(transaction: TransactionEntity) {
        viewModelScope.launch {
            try {
                when (transaction.transactionType) {
                    TransactionType.LENT -> {
                        // Mark as not collected and deduct amount from account (reverse the collection)
                        val updatedTransaction = transaction.copy(isCollected = false)
                        transactionRepository.updateTransaction(updatedTransaction)

                        // Deduct the amount from account (reverse the previous addition)
                        accountRepository.updateAccountBalance(
                            transaction.accountId,
                            transaction.amount,
                            isExpense = true // Remove the money that was added back
                        )
                    }

                    else -> {
                        val updatedTransaction = transaction.copy(isCollected = false)
                        transactionRepository.updateTransaction(updatedTransaction)
                    }
                }

                fetchAllTransactions()
                _accountViewModel?.refreshAccounts()
                AppEventBus.tryEmitEvent(TransactionEvent.TransactionsUpdated)
                _events.emit(AddScreenEvent.TransactionMarkedAsNotCollected)

            } catch (e: Exception) {
                Log.e("MarkNotCollectedError", "Error marking transaction as not collected: ${e.message}", e)
                updateState { it.copy(validationError = "Error updating transaction: ${e.message}") }
            }
        }
    }

    private fun markTransactionAsSettled(transaction: TransactionEntity) {
        viewModelScope.launch {
            try {
                when (transaction.transactionType) {
                    TransactionType.BORROWED -> {
                        // For borrowed transactions: mark as settled and deduct amount from account
                        val updatedTransaction = transaction.copy(isSettled = true)
                        transactionRepository.updateTransaction(updatedTransaction)

                        // Deduct the borrowed amount from account (paying back the borrowed money)
                        // For BORROWED transactions, this should always deduct money regardless of mode
                        accountRepository.updateAccountBalance(
                            transaction.accountId,
                            transaction.amount,
                            isExpense = true // Always deduct for settled borrowed money
                        )
                    }

                    else -> {
                        val updatedTransaction = transaction.copy(isSettled = true)
                        transactionRepository.updateTransaction(updatedTransaction)
                    }
                }
                logTransactionStatusChange(transaction, ActivityActionType.TRANSACTION_MARKED_SETTLED)

                fetchAllTransactions()
                _accountViewModel?.refreshAccounts()
                AppEventBus.tryEmitEvent(TransactionEvent.TransactionsUpdated)
                _events.emit(AddScreenEvent.TransactionMarkedAsSettled)

            } catch (e: Exception) {
                Log.e("MarkSettledError", "Error marking transaction as settled: ${e.message}", e)
                updateState { it.copy(validationError = "Error updating transaction: ${e.message}") }
            }
        }
    }

    private fun markTransactionAsUnsettled(transaction: TransactionEntity) {
        viewModelScope.launch {
            try {
                when (transaction.transactionType) {
                    TransactionType.BORROWED -> {
                        // Mark as unsettled and add amount back to account (reverse the settlement)
                        val updatedTransaction = transaction.copy(isSettled = false)
                        transactionRepository.updateTransaction(updatedTransaction)

                        // Add the amount back to account (reverse the previous deduction)
                        accountRepository.updateAccountBalance(
                            transaction.accountId,
                            transaction.amount,
                            isExpense = false // Add back the money that was deducted
                        )
                    }

                    else -> {
                        val updatedTransaction = transaction.copy(isSettled = false)
                        transactionRepository.updateTransaction(updatedTransaction)
                    }
                }

                fetchAllTransactions()
                _accountViewModel?.refreshAccounts()
                AppEventBus.tryEmitEvent(TransactionEvent.TransactionsUpdated)
                _events.emit(AddScreenEvent.TransactionMarkedAsUnsettled)

            } catch (e: Exception) {
                Log.e("MarkUnsettledError", "Error marking transaction as unsettled: ${e.message}", e)
                updateState { it.copy(validationError = "Error updating transaction: ${e.message}") }
            }
        }
    }

    private fun generateNextRecurringTransactionIfNeeded(transaction: TransactionEntity) {
        viewModelScope.launch {
            try {
                val recurrence = transaction.recurrence
                if (recurrence == null || recurrence.frequency == RecurrenceFrequency.NONE) {
                    Log.d("RecurringTransaction", "No recurrence found for transaction ${transaction.id}")
                    return@launch
                }

                Log.d("RecurringTransaction", "Generating next recurring transaction for ${transaction.title}")
                Log.d("RecurringTransaction", "Current transaction date: ${transaction.date}")
                Log.d("RecurringTransaction", "Recurrence frequency: ${recurrence.frequency}")
                Log.d("RecurringTransaction", "Recurrence interval: ${recurrence.interval}")
                Log.d("RecurringTransaction", "End date: ${recurrence.endRecurrenceDate}")

                // Check if we've reached the end date
                val endDate = recurrence.endRecurrenceDate
                if (endDate != null && transaction.date >= endDate) {
                    Log.d("RecurringTransaction", "End date reached for transaction ${transaction.id}")
                    return@launch
                }

                // Calculate the next due date
                val nextDueDate = getNextDueDate(
                    transaction.date,
                    recurrence.frequency,
                    recurrence.interval,
                    endDate ?: Long.MAX_VALUE
                )

                Log.d("RecurringTransaction", "Calculated next due date: $nextDueDate")

                if (nextDueDate != null) {
                    // Check if a transaction already exists for this next due date
                    val existingTransactions = transactionRepository.getAllTransactions()
                    val hasExistingTransaction = existingTransactions.any {
                        it.date == nextDueDate &&
                                it.title == transaction.title &&
                                it.amount == transaction.amount &&
                                it.accountId == transaction.accountId &&
                                it.transactionType == transaction.transactionType
                    }

                    Log.d("RecurringTransaction", "Existing transaction found: $hasExistingTransaction")

                    if (!hasExistingTransaction) {
                        // Calculate the due date after the next one
                        val followingDueDate = getNextDueDate(
                            nextDueDate,
                            recurrence.frequency,
                            recurrence.interval,
                            endDate ?: Long.MAX_VALUE
                        )

                        Log.d("RecurringTransaction", "Following due date: $followingDueDate")

                        // Create the next recurring transaction
                        val nextTransaction = TransactionEntity(
                            id = 0, // Let database generate new ID
                            title = transaction.title,
                            amount = transaction.amount,
                            date = nextDueDate,
                            time = transaction.time,
                            recurrence = recurrence,
                            categoryId = transaction.categoryId,
                            subCategoryId = transaction.subCategoryId,
                            accountId = transaction.accountId,
                            note = transaction.note,
                            mode = transaction.mode,
                            transactionType = transaction.transactionType,
                            isPaid = false, // New recurring transaction starts as unpaid
                            isCollected = false,
                            isSettled = false,
                            nextDueDate = followingDueDate,
                            destinationAccountId = transaction.destinationAccountId,
                            originalCurrencyCode = transaction.originalCurrencyCode,
                            endDate = transaction.endDate
                        )

                        // Add the next recurring transaction
                        transactionRepository.addTransaction(nextTransaction)
                        Log.d("RecurringTransaction", "Successfully created next recurring transaction for ${transaction.title} on date: $nextDueDate")
                        logRecurringTransactionGenerated(nextTransaction)
                        // Refresh the transactions list to show the new transaction
                        fetchAllTransactions(forceRefresh = true)
                    } else {
                        Log.d("RecurringTransaction", "Transaction already exists for next due date: $nextDueDate")
                    }
                } else {
                    Log.d("RecurringTransaction", "No more recurring transactions to create for ${transaction.title} - end date reached or invalid calculation")
                }
            } catch (e: Exception) {
                Log.e("RecurringTransaction", "Error generating next recurring transaction: ${e.message}", e)
            }
        }
    }
    private suspend fun logRecurringTransactionGenerated(transaction: TransactionEntity) {
        try {
            val account = accountRepository.getAccountById(transaction.accountId)
            activityLogUtils.logRecurringTransactionGenerated(
                transaction.title,
                transaction.amount,
                transaction.accountId,
                transaction.id
            )
            Log.d("ActivityLog", "Logged recurring transaction generation: ${transaction.title}")
        } catch (e: Exception) {
            Log.e("ActivityLog", "Error logging recurring transaction generation: ${e.message}", e)
        }
    }

    private fun emitNavigateBackEvent() {
        viewModelScope.launch {
            _events.emit(AddScreenEvent.NavigateBack)
        }
    }

    private fun clearTransactionFields() {
        // Reset state to default values
        updateState {
            AddTransactionScreenState(
                tabItems = it.getAvailableTabItems(),
                categories = it.categories,
                accounts = it.accounts,
                transactions = it.transactions,
                recurrenceFrequency = RecurrenceFrequency.NONE,
                recurrenceInterval = 1,
                recurrenceEndDate = null,
                isEndDateSelected = false,
            )
        }
    }


    // Create a TransactionEntity from the current state
    private suspend fun createTransactionEntity(): TransactionEntity {
        val currentState = _state.value
        var finalTitle = currentState.transactionTitle

        if (finalTitle.isEmpty() && currentState.transactionCategoryId != 0) {
            val category = categoryRepository.getCategoryById(currentState.transactionCategoryId)
            category?.let {
                finalTitle = it.name
                Log.d("TransactionDebug", "Using category name '${it.name}' as transaction title")
            }
        }

        var finalAccountId = currentState.transactionAccountId
        if (finalAccountId == 0) {
            val accounts = accountRepository.getAllAccounts()
            val mainAccount = accounts.find { it.isMainAccount }
            mainAccount?.let {
                finalAccountId = it.id
                Log.d("TransactionDebug", "Using main account '${it.accountName}' (ID: ${it.id}) as default account")
            }
        }

        var finalTransactionNote = currentState.transactionNote
        val account = accountRepository.getAccountById(currentState.transactionAccountId)
        val destinationAccount = accountRepository.getAccountById(currentState.transactionDestinationAccountId ?: currentState.transactionAccountId)

        if (finalTransactionNote.isEmpty() && currentState.transactionMode == "Transfer") {
            finalTransactionNote = "Amount is Transferred from ${account?.accountName} to ${destinationAccount?.accountName}"
        }

        val transactionCurrencyCode = accountRepository.getAccountById(currentState.transactionAccountId)?.currencyCode ?: "usd"

        // FIX: Use proper defaults for recurrence based on transaction type
        val finalRecurrenceFrequency = if (currentState.recurrenceFrequency == RecurrenceFrequency.NONE &&
            (currentState.transactionType == TransactionType.SUBSCRIPTION ||
                    currentState.transactionType == TransactionType.REPETITIVE)) {
            RecurrenceFrequency.MONTHLY
        } else {
            currentState.recurrenceFrequency
        }

        val finalRecurrenceInterval = if (currentState.recurrenceInterval <= 0 &&
            (currentState.transactionType == TransactionType.SUBSCRIPTION ||
                    currentState.transactionType == TransactionType.REPETITIVE)) {
            1
        } else {
            currentState.recurrenceInterval.coerceAtLeast(1)
        }

        Log.d("TransactionDebug", "Creating transaction with accountId: ${currentState.transactionAccountId}, categoryId: ${currentState.transactionCategoryId}, destinationAccountID: ${currentState.transactionDestinationAccountId}")

        return TransactionEntity(
            title = finalTitle,
            amount = currentState.transactionAmount.toDouble(),
            date = currentState.transactionDate,
            time = currentState.transactionTime,
            recurrence = Recurrence(
                frequency = finalRecurrenceFrequency,
                interval = finalRecurrenceInterval,
                endRecurrenceDate = currentState.recurrenceEndDate
            ),
            categoryId = currentState.transactionCategoryId,
            subCategoryId = if (currentState.transactionSubCategoryId != 0)
                currentState.transactionSubCategoryId else null,
            accountId = finalAccountId,
            note = finalTransactionNote,
            mode = currentState.transactionMode,
            transactionType = currentState.transactionType,
            isPaid = currentState.isPaid,
            nextDueDate = currentState.nextDueDate,
            destinationAccountId = if (currentState.transactionMode == "Transfer") currentState.transactionDestinationAccountId else null,
            originalCurrencyCode = transactionCurrencyCode
        )
    }

    // Check if the transaction is recurring
    private fun isRecurringTransaction(transaction: TransactionEntity): Boolean {
        return transaction.transactionType == TransactionType.SUBSCRIPTION ||
                transaction.transactionType == TransactionType.REPETITIVE
    }

    // Added for backward compatibility with existing code
    // Note: These should be gradually removed as code is migrated to use state and events

    val transactionTitle = state.map { it.transactionTitle }.stateIn(viewModelScope, SharingStarted.Eagerly, "")
    val transactionAmount = state.map { it.transactionAmount }.stateIn(viewModelScope, SharingStarted.Eagerly, "")
    val transactionDate = state.map { it.transactionDate }.stateIn(viewModelScope, SharingStarted.Eagerly, getCurrentDateInMillis())
    val transactionTime = state.map { it.transactionTime }.stateIn(viewModelScope, SharingStarted.Eagerly, System.currentTimeMillis())
    val transactionCategoryId = state.map { it.transactionCategoryId }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    val transactionSubCategoryId = state.map { it.transactionSubCategoryId }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    val transactionAccountId = state.map { it.transactionAccountId }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    val transactionDestinationAccountId = state.map { it.transactionDestinationAccountId }.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val transactionNote = state.map { it.transactionNote }.stateIn(viewModelScope, SharingStarted.Eagerly, "")
    val transactionMode = state.map { it.transactionMode }.stateIn(viewModelScope, SharingStarted.Eagerly, "Expense")
    val transactionType = state.map { it.transactionType }.stateIn(viewModelScope, SharingStarted.Eagerly, TransactionType.DEFAULT)
    val recurrenceFrequency = state.map { it.recurrenceFrequency }.stateIn(viewModelScope, SharingStarted.Eagerly, RecurrenceFrequency.NONE)
    val recurrenceInterval = state.map { it.recurrenceInterval }.stateIn(viewModelScope, SharingStarted.Eagerly, 1)
    val recurrenceEndDate = state.map { it.recurrenceEndDate }.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val isPaid = state.map { it.isPaid }.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val nextDueDate = state.map { it.nextDueDate }.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val transactionTypeMenuOpened = state.map { it.isTransactionTypeMenuOpened }.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val isTransactionTypeInfoSheetOpen = state.map { it.isTransactionTypeInfoSheetOpen }.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val recurrenceMenuOpened = state.map { it.isRecurrenceBottomSheetOpen }.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val isTransactionAmountSheetOpen = state.map { it.isTransactionAmountSheetOpen }.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val isRecurrenceBottomSheetOpen = state.map { it.isRecurrenceBottomSheetOpen }.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val isCustomInputBottomSheetOpen = state.map { it.isCustomInputBottomSheetOpen }.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val isEndDateSelected = state.map { it.isEndDateSelected }.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val openEndDatePicker = state.map { it.openEndDatePicker }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    // Methods to update the state through events - temporary until full migration

    fun setTransactionTypeMenuOpened(isOpen: Boolean) {
        onEvent(AddTransactionEvent.SetTransactionTypeMenuOpened(isOpen))
    }

    fun setTransactionTypeInfoSheetOpen(isOpen: Boolean) {
        onEvent(AddTransactionEvent.SetTransactionTypeInfoSheetOpen(isOpen))
    }

    fun setRecurrenceMenuOpened(isOpen: Boolean) {
        onEvent(AddTransactionEvent.SetRecurrenceMenuOpen(isOpen))
    }

    fun setTransactionAmountSheetOpen(isOpen: Boolean) {
        onEvent(AddTransactionEvent.SetTransactionAmountSheetOpen(isOpen))
    }

    fun setRecurrenceBottomSheetOpen(isOpen: Boolean) {
        onEvent(AddTransactionEvent.SetRecurrenceMenuOpen(isOpen))
    }

    fun setCustomInputBottomSheetOpen(isOpen: Boolean) {
        onEvent(AddTransactionEvent.SetCustomInputBottomSheetOpen(isOpen))
    }

    fun setSelectedFrequency(frequency: String) {
        onEvent(AddTransactionEvent.UpdateRecurrenceFrequency(RecurrenceFrequency.valueOf(frequency)))
    }

    fun setIntervalInput(interval: Int) {
        onEvent(AddTransactionEvent.UpdateRecurrenceInterval(interval))
    }

    fun setEndDateSelected(isSelected: Boolean) {
        onEvent(AddTransactionEvent.SetEndDateSelected(isSelected))
    }

    fun setOpenEndDatePicker(isOpen: Boolean) {
        onEvent(AddTransactionEvent.SetOpenEndDatePicker(isOpen))
    }

    // Backward compatibility methods

    fun getTransactionById(id: Int, onComplete: (TransactionEntity?) -> Unit) {
        viewModelScope.launch {
            try {
                val transaction = transactionRepository.getTransactionById(id)
                if (transaction != null) {
                    onEvent(AddTransactionEvent.LoadTransactionById(id))
                }
                onComplete(transaction)
            } catch (e: Exception) {
                Log.e("AddScreenViewModel", "Error fetching transaction by ID: ${e.message}", e)
                onComplete(null)
            }
        }
    }

    private suspend fun logTransactionCreated(
        transaction: TransactionEntity,
        sourceAccount: AccountEntity?,
        destinationAccount: AccountEntity? = null
    ) {
        try {
            when (transaction.mode) {
                "Expense" -> activityLogUtils.logTransactionCreated(transaction, sourceAccount)
                "Income" -> activityLogUtils.logTransactionCreated(transaction, sourceAccount)
                "Transfer" -> {
                    // Log transfer with both accounts
                    activityLogUtils.logTransactionCreated(transaction, sourceAccount)
                    Log.d("ActivityLog", "Logged transfer transaction: ${transaction.title} from ${sourceAccount?.accountName} to ${destinationAccount?.accountName}")
                }
            }
        } catch (e: Exception) {
            Log.e("ActivityLog", "Error logging transaction creation: ${e.message}", e)
        }
    }

    private suspend fun logTransactionUpdated(transaction: TransactionEntity) {
        try {
            val account = accountRepository.getAccountById(transaction.accountId)
            activityLogUtils.logTransactionUpdated(transaction, account)
            Log.d("ActivityLog", "Logged transaction update: ${transaction.title}")
        } catch (e: Exception) {
            Log.e("ActivityLog", "Error logging transaction update: ${e.message}", e)
        }
    }

    private suspend fun logTransactionDeleted(transaction: TransactionEntity) {
        try {
            val account = accountRepository.getAccountById(transaction.accountId)
            activityLogUtils.logTransactionDeleted(transaction, account)
            Log.d("ActivityLog", "Logged transaction deletion: ${transaction.title}")
        } catch (e: Exception) {
            Log.e("ActivityLog", "Error logging transaction deletion: ${e.message}", e)
        }
    }

    private suspend fun logTransactionStatusChange(transaction: TransactionEntity, actionType: ActivityActionType) {
        try {
            val account = accountRepository.getAccountById(transaction.accountId)
            val activity = ActivityLogEntryFactory.createTransactionEntry(actionType, transaction, account)

            // Use a custom description for status changes
            val description = when (actionType) {
                ActivityActionType.TRANSACTION_MARKED_PAID -> "Transaction marked as paid"
                ActivityActionType.TRANSACTION_MARKED_UNPAID -> "Transaction marked as unpaid"
                ActivityActionType.TRANSACTION_MARKED_COLLECTED -> "Lent money marked as collected"
                ActivityActionType.TRANSACTION_MARKED_SETTLED -> "Borrowed money marked as settled"
                else -> activity.description
            }

            val customActivity = activity.copy(description = description)
            // Assuming ActivityLogUtils has a generic method to add activities
            // You might need to add this method to ActivityLogUtils
            Log.d("ActivityLog", "Logged transaction status change: ${actionType.getDisplayName()} for ${transaction.title}")
        } catch (e: Exception) {
            Log.e("ActivityLog", "Error logging transaction status change: ${e.message}", e)
        }
    }
}

// Events for the UI updates
sealed class AddScreenEvent {
    object TransactionAdded : AddScreenEvent()
    object TransactionUpdated : AddScreenEvent()
    object TransactionDeleted : AddScreenEvent()
    object TransactionMarkedAsPaid : AddScreenEvent()
    object TransactionMarkedAsCollected : AddScreenEvent()
    object TransactionMarkedAsSettled : AddScreenEvent()
    object TransactionMarkedAsUnpaid : AddScreenEvent()
    object TransactionMarkedAsNotCollected : AddScreenEvent()
    object TransactionMarkedAsUnsettled : AddScreenEvent()
    object NavigateBack : AddScreenEvent()
    data class ValidationError(val message: String) : AddScreenEvent()
}
