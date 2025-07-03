package com.ritesh.cashiro.presentation.ui.features.schedules

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ritesh.cashiro.data.local.entity.TransactionEntity
import com.ritesh.cashiro.data.local.entity.TransactionType
import com.ritesh.cashiro.domain.repository.AccountRepository
import com.ritesh.cashiro.domain.repository.TransactionRepository
import com.ritesh.cashiro.domain.utils.AccountEvent
import com.ritesh.cashiro.domain.utils.AppEventBus
import com.ritesh.cashiro.domain.utils.CurrencyEvent
import com.ritesh.cashiro.domain.utils.CurrencyUtils
import com.ritesh.cashiro.domain.utils.ScheduleFilterUtils
import com.ritesh.cashiro.domain.utils.TransactionEvent
import com.ritesh.cashiro.domain.utils.getDaysOverdue
import com.ritesh.cashiro.domain.utils.getDaysUntilDue
import com.ritesh.cashiro.widgets.WidgetUpdateUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val widgetUpdateUtil: WidgetUpdateUtil,
    application: Application
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(ScheduleScreenState())
    val state: StateFlow<ScheduleScreenState> = _state.asStateFlow()

    private val _uiEvents = MutableSharedFlow<ScheduleUiEvent>()
    val uiEvents: SharedFlow<ScheduleUiEvent> = _uiEvents.asSharedFlow()

    init {
        // Load scheduled transactions on initialization
        onEvent(ScheduleEvent.LoadScheduledTransactions)

        // Listen for app-wide events
        viewModelScope.launch {
            AppEventBus.events.collect { event ->
                when (event) {
                    is TransactionEvent.TransactionsUpdated -> {
                        onEvent(ScheduleEvent.RefreshScheduledTransactions)
                    }
                    is CurrencyEvent.ConversionRatesUpdated -> {
                        onEvent(ScheduleEvent.UpdateConversionRates(event.rates))
                        onEvent(ScheduleEvent.RecalculateTotals)
                    }
                    is AccountEvent.AccountsUpdated -> {
                        onEvent(ScheduleEvent.RecalculateTotals)
                    }
                }
            }
        }
    }

    fun onEvent(event: ScheduleEvent) {
        when (event) {
            // Data Loading Events
            is ScheduleEvent.LoadScheduledTransactions -> loadScheduledTransactions()
            is ScheduleEvent.RefreshScheduledTransactions -> refreshScheduledTransactions()

            // Tab Navigation Events
            is ScheduleEvent.SelectTab -> selectTab(event.tabIndex)

            // Filter Events
            is ScheduleEvent.UpdatePeriodFilter -> updatePeriodFilter(event.period)
            is ScheduleEvent.UpdateFilterState -> updateFilterState(event.filterState)
            is ScheduleEvent.UpdateAccountFilter -> updateAccountFilter(event.accountIds)
            is ScheduleEvent.UpdatePaymentStatusFilter -> updatePaymentStatusFilter(event.statuses)
            is ScheduleEvent.UpdateTransactionTypeFilter -> updateTransactionTypeFilter(event.types)
            is ScheduleEvent.UpdateDateRangeFilter -> updateDateRangeFilter(event.dateRange)
            is ScheduleEvent.UpdateAmountFilter -> updateAmountFilter(event.minAmount, event.maxAmount)
            is ScheduleEvent.UpdateSearchFilter -> updateSearchFilter(event.searchText)
            is ScheduleEvent.UpdateSortBy -> updateSortBy(event.sortBy)
            is ScheduleEvent.UpdatePaidFilter -> updatePaidFilter(event.showPaidOnly)
            is ScheduleEvent.UpdateUnpaidFilter -> updateUnpaidFilter(event.showUnpaidOnly)
            is ScheduleEvent.UpdateDaysAhead -> updateDaysAhead(event.days)
            is ScheduleEvent.UpdateDaysBehind -> updateDaysBehind(event.days)
            is ScheduleEvent.ClearAllFilters -> clearAllFilters()

            // Selection Events
            is ScheduleEvent.ToggleTransactionSelection -> toggleTransactionSelection(event.transactionId)
            is ScheduleEvent.SelectTransaction -> selectTransaction(event.transactionId)
            is ScheduleEvent.DeselectTransaction -> deselectTransaction(event.transactionId)
            is ScheduleEvent.SelectAllTransactions -> selectAllTransactions()
            is ScheduleEvent.ClearSelection -> clearSelection()
            is ScheduleEvent.SetSelectionMode -> setSelectionMode(event.isEnabled)

            // Transaction Management Events
            is ScheduleEvent.MarkAsPaid -> markAsPaid(event.transaction)
            is ScheduleEvent.MarkAsUnpaid -> markAsUnpaid(event.transaction)
            is ScheduleEvent.SnoozeTransaction -> snoozeTransaction(event.transactionId, event.snoozeUntil)
            is ScheduleEvent.RescheduleTransaction -> rescheduleTransaction(event.transactionId, event.newDate)
            is ScheduleEvent.UpdateTransaction -> updateTransaction(event.transaction)
            is ScheduleEvent.DeleteTransaction -> deleteTransaction(event.transactionId)
            is ScheduleEvent.DeleteScheduledTransactions -> deleteScheduledTransactions(event.transactionIds)

            // Bulk Operations Events
            is ScheduleEvent.BulkMarkAsPaid -> bulkMarkAsPaid(event.transactionIds)
            is ScheduleEvent.BulkMarkAsUnpaid -> bulkMarkAsUnpaid(event.transactionIds)
            is ScheduleEvent.BulkSnooze -> bulkSnooze(event.transactionIds, event.snoozeUntil)
            is ScheduleEvent.BulkReschedule -> bulkReschedule(event.transactionIds, event.newDate)
            is ScheduleEvent.BulkDelete -> bulkDelete(event.transactionIds)

            // Quick Actions Events
            is ScheduleEvent.PayNow -> payNow(event.transactionId)
            is ScheduleEvent.SkipPayment -> skipPayment(event.transactionId)
            is ScheduleEvent.PayAll -> payAll(event.transactionIds)
            is ScheduleEvent.SnoozeAll -> snoozeAll(event.transactionIds, event.hours)

            // Calculation Events
            is ScheduleEvent.RecalculateTotals -> recalculateTotals()
            is ScheduleEvent.UpdateConversionRates -> updateConversionRates(event.rates)

            // Analysis Events
            is ScheduleEvent.AnalyzeSchedule -> analyzeSchedule()
            is ScheduleEvent.GetUpcomingInDays -> getUpcomingInDays(event.days)
            is ScheduleEvent.GetOverdueInDays -> getOverdueInDays(event.days)

            // Navigation Events
            is ScheduleEvent.NavigateToAddTransaction -> navigateToAddTransaction(event.defaultType)
            is ScheduleEvent.NavigateToEditTransaction -> navigateToEditTransaction(event.transactionId)

            // Export Events
            is ScheduleEvent.ExportSchedule -> exportSchedule()

            // Error Handling Events
            is ScheduleEvent.ShowError -> showError(event.message)
            is ScheduleEvent.ClearError -> clearError()

            // UI State Events
            is ScheduleEvent.SetLoading -> setLoading(event.isLoading)
            is ScheduleEvent.UpdateLastRefresh -> updateLastRefresh(event.timestamp)

            // Unhandled events - log for debugging
            else -> {
                Log.d("ScheduleViewModel", "Unhandled event: ${event::class.simpleName}")
            }
        }
    }

    private fun updateState(update: (ScheduleScreenState) -> ScheduleScreenState) {
        _state.update(update)
    }

    private fun emitUiEvent(event: ScheduleUiEvent) {
        viewModelScope.launch {
            _uiEvents.emit(event)
        }
    }

//    private fun loadScheduledTransactions() {
//        viewModelScope.launch {
//            try {
//                updateState { it.copy(isLoading = true, error = null) }
//
//                // Get all scheduled transaction types
//                val upcomingTransactions = transactionRepository.getUnpaidTransactions()
//                val subscriptions = transactionRepository.getSubscriptionTransactions()
//                val repetitiveTransactions = transactionRepository.getRepetitiveTransactions()
//
//                // Combine all scheduled transactions
//                val allScheduledTransactions = (upcomingTransactions + subscriptions + repetitiveTransactions)
//                    .distinctBy { it.id }
//
//                Log.d("ScheduleViewModel", "Loaded ${allScheduledTransactions.size} scheduled transactions")
//
//                // Categorize transactions
//                val (all, upcoming, overdue) = ScheduleFilterUtils.categorizeScheduledTransactions(
//                    allScheduledTransactions,
//                    _state.value.filterState.daysAhead,
//                    _state.value.filterState.daysBehind
//                )
//
//                updateState { currentState ->
//                    currentState.copy(
//                        allTransactions = all,
//                        upcomingTransactions = upcoming,
//                        overdueTransactions = overdue,
//                        isLoading = false,
//                        lastUpdated = System.currentTimeMillis()
//                    )
//                }
//
//                recalculateTotals()
//                emitUiEvent(ScheduleUiEvent.ScheduledTransactionsLoaded)
//
//            } catch (e: Exception) {
//                Log.e("ScheduleViewModel", "Error loading scheduled transactions: ${e.message}", e)
//                updateState { it.copy(isLoading = false, error = "Failed to load scheduled transactions: ${e.message}") }
//                emitUiEvent(ScheduleUiEvent.ShowMessage("Failed to load scheduled transactions", true))
//            }
//        }
//    }
//
//    private fun refreshScheduledTransactions() {
//        viewModelScope.launch {
//            try {
//                // Get all scheduled transaction types
//                val upcomingTransactions = transactionRepository.getUnpaidTransactions()
//                val subscriptions = transactionRepository.getSubscriptionTransactions()
//                val repetitiveTransactions = transactionRepository.getRepetitiveTransactions()
//
//                // Combine all scheduled transactions
//                val allScheduledTransactions = (upcomingTransactions + subscriptions + repetitiveTransactions)
//                    .distinctBy { it.id }
//
//                // Categorize transactions
//                val (all, upcoming, overdue) = ScheduleFilterUtils.categorizeScheduledTransactions(
//                    allScheduledTransactions,
//                    _state.value.filterState.daysAhead,
//                    _state.value.filterState.daysBehind
//                )
//
//                updateState { currentState ->
//                    currentState.copy(
//                        allTransactions = all,
//                        upcomingTransactions = upcoming,
//                        overdueTransactions = overdue,
//                        lastUpdated = System.currentTimeMillis()
//                    )
//                }
//
//                recalculateTotals()
//                emitUiEvent(ScheduleUiEvent.ScheduledTransactionsRefreshed)
//
//            } catch (e: Exception) {
//                Log.e("ScheduleViewModel", "Error refreshing scheduled transactions: ${e.message}", e)
//                showError("Failed to refresh scheduled transactions: ${e.message}")
//            }
//        }
//    }

    private fun loadScheduledTransactions() {
        viewModelScope.launch {
            try {
                updateState { it.copy(isLoading = true, error = null) }

                // FIXED: Use the new method that properly filters active transactions
                val allScheduledTransactions = transactionRepository.getActiveScheduledTransactions()

                Log.d("ScheduleViewModel", "Loaded ${allScheduledTransactions.size} active scheduled transactions")

                // FIXED: Use the improved categorization logic
                val (all, upcoming, overdue) = ScheduleFilterUtils.categorizeScheduledTransactions(
                    allScheduledTransactions,
                    _state.value.filterState.daysAhead,
                    _state.value.filterState.daysBehind
                )

                updateState { currentState ->
                    currentState.copy(
                        allTransactions = all,
                        upcomingTransactions = upcoming,
                        overdueTransactions = overdue,
                        isLoading = false,
                        lastUpdated = System.currentTimeMillis()
                    )
                }

                recalculateTotals()
                emitUiEvent(ScheduleUiEvent.ScheduledTransactionsLoaded)

            } catch (e: Exception) {
                Log.e("ScheduleViewModel", "Error loading scheduled transactions: ${e.message}", e)
                updateState { it.copy(isLoading = false, error = "Failed to load scheduled transactions: ${e.message}") }
                emitUiEvent(ScheduleUiEvent.ShowMessage("Failed to load scheduled transactions", true))
            }
        }
    }

    // FIXED: Refresh with better logic
    private fun refreshScheduledTransactions() {
        viewModelScope.launch {
            try {
                // FIXED: Clean up duplicates first
                transactionRepository.cleanupDuplicateRecurringTransactions()

                // FIXED: Use the new method that properly filters active transactions
                val allScheduledTransactions = transactionRepository.getActiveScheduledTransactions()

                // FIXED: Use the improved categorization logic
                val (all, upcoming, overdue) = ScheduleFilterUtils.categorizeScheduledTransactions(
                    allScheduledTransactions,
                    _state.value.filterState.daysAhead,
                    _state.value.filterState.daysBehind
                )

                updateState { currentState ->
                    currentState.copy(
                        allTransactions = all,
                        upcomingTransactions = upcoming,
                        overdueTransactions = overdue,
                        lastUpdated = System.currentTimeMillis()
                    )
                }

                recalculateTotals()
                emitUiEvent(ScheduleUiEvent.ScheduledTransactionsRefreshed)

            } catch (e: Exception) {
                Log.e("ScheduleViewModel", "Error refreshing scheduled transactions: ${e.message}", e)
                showError("Failed to refresh scheduled transactions: ${e.message}")
            }
        }
    }

    // FIXED: Mark as paid with automatic next generation
    private fun markAsPaid(transaction: TransactionEntity) {
        viewModelScope.launch {
            try {
                when (transaction.transactionType) {
                    TransactionType.UPCOMING, TransactionType.SUBSCRIPTION, TransactionType.REPETITIVE -> {
                        // FIXED: Use the repository method that handles next generation
                        transactionRepository.markTransactionAsPaid(transaction)

                        // Update account balance
                        val isExpense = transaction.mode == "Expense"
                        accountRepository.updateAccountBalance(
                            transaction.accountId,
                            transaction.amount,
                            isExpense = isExpense
                        )
                    }

                    TransactionType.LENT -> {
                        transactionRepository.markTransactionAsCollected(transaction)

                        // Update account balance for collected amount
                        accountRepository.updateAccountBalance(
                            transaction.accountId,
                            transaction.amount,
                            isExpense = false // Adding money back
                        )
                    }

                    TransactionType.BORROWED -> {
                        transactionRepository.markTransactionAsSettled(transaction)

                        // Update account balance for settled amount
                        accountRepository.updateAccountBalance(
                            transaction.accountId,
                            transaction.amount,
                            isExpense = true // Removing money for settlement
                        )
                    }

                    else -> {
                        val updatedTransaction = transaction.copy(isPaid = true)
                        transactionRepository.updateTransaction(updatedTransaction)
                    }
                }

                // FIXED: Refresh to show next occurrence and remove paid transaction
                refreshScheduledTransactions()
                widgetUpdateUtil.updateAllFinancialWidgets()
                AppEventBus.tryEmitEvent(TransactionEvent.TransactionsUpdated)
                emitUiEvent(ScheduleUiEvent.TransactionMarkedAsPaid(transaction.id))

            } catch (e: Exception) {
                Log.e("ScheduleViewModel", "Error marking transaction as paid: ${e.message}", e)
                showError("Failed to mark transaction as paid: ${e.message}")
            }
        }
    }

    // FIXED: Recalculate totals with corrected method
    private fun recalculateTotals() {
        viewModelScope.launch {
            try {
                val currentState = _state.value

                // Get conversion rates
                val accounts = accountRepository.getAllAccounts()
                val mainCurrency = accounts.find { it.isMainAccount }?.currencyCode ?: "usd"
                val conversionRates = emptyMap<String, Double>() // You should get this from your currency service

                // FIXED: Use the corrected calculation methods
                val scheduleAmounts = CurrencyUtils.calculateScheduleAmounts(
                    currentState.allTransactions,
                    mainCurrency,
                    conversionRates
                )

                val upcomingAmount = CurrencyUtils.calculateTotalForCurrentPeriod(
                    currentState.upcomingTransactions,
                    mainCurrency,
                    conversionRates
                )

                val overdueAmount = CurrencyUtils.calculateTotalForCurrentPeriod(
                    currentState.overdueTransactions,
                    mainCurrency,
                    conversionRates
                )

                updateState { state ->
                    state.copy(
                        totalAmount = scheduleAmounts.currentTotal,
                        upcomingAmount = upcomingAmount,
                        overdueAmount = overdueAmount,
                        monthlyTotal = scheduleAmounts.monthlyTotal,
                        yearlyTotal = scheduleAmounts.yearlyTotal
                    )
                }

            } catch (e: Exception) {
                Log.e("ScheduleViewModel", "Error recalculating totals: ${e.message}", e)
            }
        }
    }

    // FIXED: Delete transaction with better cleanup
    private fun deleteTransaction(transactionId: Int) {
        viewModelScope.launch {
            try {
                val transaction = transactionRepository.getTransactionById(transactionId)
                if (transaction != null) {
                    // Restore account balance if transaction was paid/collected/settled
                    when (transaction.transactionType) {
                        TransactionType.UPCOMING, TransactionType.SUBSCRIPTION, TransactionType.REPETITIVE -> {
                            if (transaction.isPaid) {
                                val isExpense = transaction.mode == "Income" // Reverse
                                accountRepository.updateAccountBalance(
                                    transaction.accountId,
                                    transaction.amount,
                                    isExpense = isExpense
                                )
                            }
                        }

                        TransactionType.LENT -> {
                            if (transaction.isCollected) {
                                // Remove the collected amount
                                accountRepository.updateAccountBalance(
                                    transaction.accountId,
                                    transaction.amount,
                                    isExpense = true
                                )
                            } else {
                                // Add back the lent amount
                                accountRepository.updateAccountBalance(
                                    transaction.accountId,
                                    transaction.amount,
                                    isExpense = false
                                )
                            }
                        }

                        TransactionType.BORROWED -> {
                            if (transaction.isSettled) {
                                // Add back the settled amount
                                accountRepository.updateAccountBalance(
                                    transaction.accountId,
                                    transaction.amount,
                                    isExpense = false
                                )
                            } else {
                                // Remove the borrowed amount
                                accountRepository.updateAccountBalance(
                                    transaction.accountId,
                                    transaction.amount,
                                    isExpense = true
                                )
                            }
                        }

                        TransactionType.DEFAULT -> {
                            when (transaction.mode) {
                                "Expense" -> {
                                    // For expenses: add money back to account (reverse the expense)
                                    accountRepository.updateAccountBalance(
                                        transaction.accountId,
                                        transaction.amount,
                                        isExpense = false
                                    )
                                }

                                "Income" -> {
                                    // For income: subtract money from account (reverse the income)
                                    accountRepository.updateAccountBalance(
                                        transaction.accountId,
                                        transaction.amount,
                                        isExpense = true
                                    )
                                }

                                "Transfer" -> {
                                    // For transfers: reverse the transfer
                                    transaction.destinationAccountId?.let { destAccountId ->
                                        // Add money back to source account
                                        accountRepository.updateAccountBalance(
                                            transaction.accountId,
                                            transaction.amount,
                                            isExpense = false
                                        )

                                        // Remove money from destination account
                                        accountRepository.updateAccountBalance(
                                            destAccountId,
                                            transaction.amount,
                                            isExpense = true
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // FIXED: Also delete future recurring transactions if this is a recurring transaction
                    if (transaction.transactionType in listOf(TransactionType.SUBSCRIPTION, TransactionType.REPETITIVE)) {
                        val futureTransactions = transactionRepository.getFutureRecurringTransactions(
                            transaction.id,
                            transaction.title,
                            transaction.accountId,
                            transaction.amount,
                            transaction.transactionType,
                            transaction.date
                        )

                        futureTransactions.forEach { futureTransaction ->
                            transactionRepository.deleteTransaction(futureTransaction)
                        }
                    }

                    transactionRepository.deleteTransactionById(transactionId)
                    refreshScheduledTransactions()
                    widgetUpdateUtil.updateAllFinancialWidgets()
                    AppEventBus.tryEmitEvent(TransactionEvent.TransactionDeleted(transactionId))
                    emitUiEvent(ScheduleUiEvent.TransactionDeleted(transactionId))
                }

            } catch (e: Exception) {
                Log.e("ScheduleViewModel", "Error deleting transaction: ${e.message}", e)
                showError("Failed to delete transaction: ${e.message}")
            }
        }
    }
    private fun selectTab(tabIndex: Int) {
        updateState { it.copy(selectedTabIndex = tabIndex) }
        emitUiEvent(ScheduleUiEvent.TabChanged(tabIndex))
    }

    private fun updatePeriodFilter(period: SchedulePeriod) {
        updateState { currentState ->
            currentState.copy(
                filterState = currentState.filterState.copy(selectedPeriod = period)
            )
        }
        recalculateTotals()
    }

    private fun updateFilterState(filterState: ScheduleFilterState) {
        updateState { it.copy(filterState = filterState) }

        // Re-categorize transactions with new filter settings
        viewModelScope.launch {
            val currentState = _state.value
            val (all, upcoming, overdue) = ScheduleFilterUtils.categorizeScheduledTransactions(
                currentState.allTransactions,
                filterState.daysAhead,
                filterState.daysBehind
            )

            updateState { state ->
                state.copy(
                    upcomingTransactions = upcoming,
                    overdueTransactions = overdue
                )
            }
        }

        recalculateTotals()
    }

    private fun updateAccountFilter(accountIds: Set<Int>) {
        updateState { currentState ->
            currentState.copy(
                filterState = currentState.filterState.copy(selectedAccounts = accountIds)
            )
        }
    }

    private fun updatePaymentStatusFilter(statuses: Set<String>) {
        updateState { currentState ->
            currentState.copy(
                filterState = currentState.filterState.copy(paymentStatus = statuses)
            )
        }
    }

    private fun updateTransactionTypeFilter(types: Set<TransactionType>) {
        updateState { currentState ->
            currentState.copy(
                filterState = currentState.filterState.copy(transactionTypes = types)
            )
        }
    }

    private fun updateDateRangeFilter(dateRange: ScheduleDateRange?) {
        updateState { currentState ->
            currentState.copy(
                filterState = currentState.filterState.copy(dateRange = dateRange)
            )
        }
    }

    private fun updateAmountFilter(minAmount: Double, maxAmount: Double) {
        updateState { currentState ->
            currentState.copy(
                filterState = currentState.filterState.copy(
                    minAmount = minAmount,
                    maxAmount = maxAmount
                )
            )
        }
    }

    private fun updateSearchFilter(searchText: androidx.compose.ui.text.input.TextFieldValue) {
        updateState { currentState ->
            currentState.copy(
                filterState = currentState.filterState.copy(searchText = searchText)
            )
        }
    }

    private fun updateSortBy(sortBy: ScheduleSortBy) {
        updateState { currentState ->
            currentState.copy(
                filterState = currentState.filterState.copy(sortBy = sortBy)
            )
        }
    }

    private fun updatePaidFilter(showPaidOnly: Boolean) {
        updateState { currentState ->
            currentState.copy(
                filterState = currentState.filterState.copy(showPaidOnly = showPaidOnly)
            )
        }
    }

    private fun updateUnpaidFilter(showUnpaidOnly: Boolean) {
        updateState { currentState ->
            currentState.copy(
                filterState = currentState.filterState.copy(showUnpaidOnly = showUnpaidOnly)
            )
        }
    }

    private fun updateDaysAhead(days: Int) {
        updateState { currentState ->
            currentState.copy(
                filterState = currentState.filterState.copy(daysAhead = days)
            )
        }
        // Recategorize transactions with new day range
        refreshScheduledTransactions()
    }

    private fun updateDaysBehind(days: Int) {
        updateState { currentState ->
            currentState.copy(
                filterState = currentState.filterState.copy(daysBehind = days)
            )
        }
        // Recategorize transactions with new day range
        refreshScheduledTransactions()
    }

    private fun clearAllFilters() {
        updateState { currentState ->
            currentState.copy(filterState = ScheduleFilterState())
        }
        refreshScheduledTransactions()
        recalculateTotals()
    }

    private fun toggleTransactionSelection(transactionId: Int) {
        updateState { currentState ->
            val selectedTransactions = currentState.selectedTransactions
            val newSelection = if (selectedTransactions.contains(transactionId)) {
                selectedTransactions - transactionId
            } else {
                selectedTransactions + transactionId
            }

            currentState.copy(
                selectedTransactions = newSelection,
                isInSelectionMode = newSelection.isNotEmpty()
            )
        }
    }

    private fun selectTransaction(transactionId: Int) {
        updateState { currentState ->
            currentState.copy(
                selectedTransactions = currentState.selectedTransactions + transactionId,
                isInSelectionMode = true
            )
        }
    }

    private fun deselectTransaction(transactionId: Int) {
        updateState { currentState ->
            val newSelection = currentState.selectedTransactions - transactionId
            currentState.copy(
                selectedTransactions = newSelection,
                isInSelectionMode = newSelection.isNotEmpty()
            )
        }
    }

    private fun selectAllTransactions() {
        updateState { currentState ->
            val allIds = currentState.currentTabTransactions.map { it.id }.toSet()
            currentState.copy(
                selectedTransactions = allIds,
                isInSelectionMode = allIds.isNotEmpty()
            )
        }
    }

    private fun clearSelection() {
        updateState { currentState ->
            currentState.copy(
                selectedTransactions = emptySet(),
                isInSelectionMode = false
            )
        }
    }

    private fun setSelectionMode(isEnabled: Boolean) {
        updateState { currentState ->
            currentState.copy(
                isInSelectionMode = isEnabled,
                selectedTransactions = if (!isEnabled) emptySet() else currentState.selectedTransactions
            )
        }
    }

//    private fun markAsPaid(transaction: TransactionEntity) {
//        viewModelScope.launch {
//            try {
//                when (transaction.transactionType) {
//                    TransactionType.UPCOMING, TransactionType.SUBSCRIPTION, TransactionType.REPETITIVE -> {
//                        val updatedTransaction = transaction.copy(isPaid = true)
//                        transactionRepository.updateTransaction(updatedTransaction)
//
//                        // Update account balance
//                        val isExpense = transaction.mode == "Expense"
//                        accountRepository.updateAccountBalance(
//                            transaction.accountId,
//                            transaction.amount,
//                            isExpense = isExpense
//                        )
//
//                        // Generate next recurring transaction if needed
//                        if (transaction.recurrence != null &&
//                            (transaction.transactionType == TransactionType.SUBSCRIPTION ||
//                                    transaction.transactionType == TransactionType.REPETITIVE)) {
//                            transactionRepository.generateNextRecurringTransaction(updatedTransaction)
//                        }
//                    }
//
//                    else -> {
//                        val updatedTransaction = transaction.copy(isPaid = true)
//                        transactionRepository.updateTransaction(updatedTransaction)
//                    }
//                }
//
//                refreshScheduledTransactions()
//                widgetUpdateUtil.updateAllFinancialWidgets()
//                AppEventBus.tryEmitEvent(TransactionEvent.TransactionsUpdated)
//                emitUiEvent(ScheduleUiEvent.TransactionMarkedAsPaid(transaction.id))
//
//            } catch (e: Exception) {
//                Log.e("ScheduleViewModel", "Error marking transaction as paid: ${e.message}", e)
//                showError("Failed to mark transaction as paid: ${e.message}")
//            }
//        }
//    }

    private fun markAsUnpaid(transaction: TransactionEntity) {
        viewModelScope.launch {
            try {
                when (transaction.transactionType) {
                    TransactionType.UPCOMING, TransactionType.SUBSCRIPTION, TransactionType.REPETITIVE -> {
                        val updatedTransaction = transaction.copy(isPaid = false)
                        transactionRepository.updateTransaction(updatedTransaction)

                        // Reverse account balance update
                        val isExpense = transaction.mode == "Income" // Reverse
                        accountRepository.updateAccountBalance(
                            transaction.accountId,
                            transaction.amount,
                            isExpense = isExpense
                        )
                    }

                    else -> {
                        val updatedTransaction = transaction.copy(isPaid = false)
                        transactionRepository.updateTransaction(updatedTransaction)
                    }
                }

                refreshScheduledTransactions()
                widgetUpdateUtil.updateAllFinancialWidgets()
                AppEventBus.tryEmitEvent(TransactionEvent.TransactionsUpdated)
                emitUiEvent(ScheduleUiEvent.TransactionMarkedAsUnpaid(transaction.id))

            } catch (e: Exception) {
                Log.e("ScheduleViewModel", "Error marking transaction as unpaid: ${e.message}", e)
                showError("Failed to mark transaction as unpaid: ${e.message}")
            }
        }
    }

    private fun snoozeTransaction(transactionId: Int, snoozeUntil: Long) {
        viewModelScope.launch {
            try {
                val transaction = transactionRepository.getTransactionById(transactionId)
                if (transaction != null) {
                    val updatedTransaction = transaction.copy(nextDueDate = snoozeUntil)
                    transactionRepository.updateTransaction(updatedTransaction)

                    refreshScheduledTransactions()
                    AppEventBus.tryEmitEvent(TransactionEvent.TransactionUpdated(transactionId))
                    emitUiEvent(ScheduleUiEvent.TransactionSnoozed(transactionId, snoozeUntil))
                }

            } catch (e: Exception) {
                Log.e("ScheduleViewModel", "Error snoozing transaction: ${e.message}", e)
                showError("Failed to snooze transaction: ${e.message}")
            }
        }
    }

    private fun rescheduleTransaction(transactionId: Int, newDate: Long) {
        viewModelScope.launch {
            try {
                val transaction = transactionRepository.getTransactionById(transactionId)
                if (transaction != null) {
                    val updatedTransaction = transaction.copy(
                        date = newDate,
                        nextDueDate = newDate
                    )
                    transactionRepository.updateTransaction(updatedTransaction)

                    refreshScheduledTransactions()
                    AppEventBus.tryEmitEvent(TransactionEvent.TransactionUpdated(transactionId))
                    emitUiEvent(ScheduleUiEvent.TransactionRescheduled(transactionId, newDate))
                }

            } catch (e: Exception) {
                Log.e("ScheduleViewModel", "Error rescheduling transaction: ${e.message}", e)
                showError("Failed to reschedule transaction: ${e.message}")
            }
        }
    }

    private fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            try {
                transactionRepository.updateTransaction(transaction)
                refreshScheduledTransactions()
                widgetUpdateUtil.updateAllFinancialWidgets()
                AppEventBus.tryEmitEvent(TransactionEvent.TransactionUpdated(transaction.id))
                emitUiEvent(ScheduleUiEvent.TransactionUpdated(transaction.id))

            } catch (e: Exception) {
                Log.e("ScheduleViewModel", "Error updating transaction: ${e.message}", e)
                showError("Failed to update transaction: ${e.message}")
            }
        }
    }

//    private fun deleteTransaction(transactionId: Int) {
//        viewModelScope.launch {
//            try {
//                val transaction = transactionRepository.getTransactionById(transactionId)
//                if (transaction != null) {
//                    // Restore account balance if transaction was paid
//                    if (transaction.isPaid) {
//                        val isExpense = transaction.mode == "Income" // Reverse
//                        accountRepository.updateAccountBalance(
//                            transaction.accountId,
//                            transaction.amount,
//                            isExpense = isExpense
//                        )
//                    }
//
//                    transactionRepository.deleteTransactionById(transactionId)
//                    refreshScheduledTransactions()
//                    widgetUpdateUtil.updateAllFinancialWidgets()
//                    AppEventBus.tryEmitEvent(TransactionEvent.TransactionDeleted(transactionId))
//                    emitUiEvent(ScheduleUiEvent.TransactionDeleted(transactionId))
//                }
//
//            } catch (e: Exception) {
//                Log.e("ScheduleViewModel", "Error deleting transaction: ${e.message}", e)
//                showError("Failed to delete transaction: ${e.message}")
//            }
//        }
//    }

    private fun deleteScheduledTransactions(transactionIds: List<Int>) {
        viewModelScope.launch {
            try {
                var deletedCount = 0

                transactionIds.forEach { id ->
                    val transaction = transactionRepository.getTransactionById(id)
                    if (transaction != null) {
                        // Restore account balance if transaction was paid
                        if (transaction.isPaid) {
                            val isExpense = transaction.mode == "Income" // Reverse
                            accountRepository.updateAccountBalance(
                                transaction.accountId,
                                transaction.amount,
                                isExpense = isExpense
                            )
                        }

                        transactionRepository.deleteTransactionById(id)
                        deletedCount++
                    }
                }

                clearSelection()
                refreshScheduledTransactions()
                widgetUpdateUtil.updateAllFinancialWidgets()
                AppEventBus.tryEmitEvent(TransactionEvent.TransactionsUpdated)
                emitUiEvent(ScheduleUiEvent.TransactionsDeleted(deletedCount))

            } catch (e: Exception) {
                Log.e("ScheduleViewModel", "Error deleting transactions: ${e.message}", e)
                showError("Failed to delete transactions: ${e.message}")
            }
        }
    }

    private fun bulkMarkAsPaid(transactionIds: List<Int>) {
        viewModelScope.launch {
            try {
                var updatedCount = 0

                transactionIds.forEach { id ->
                    val transaction = transactionRepository.getTransactionById(id)
                    if (transaction != null && !transaction.isPaid) {
                        val updatedTransaction = transaction.copy(isPaid = true)
                        transactionRepository.updateTransaction(updatedTransaction)

                        // Update account balance
                        val isExpense = transaction.mode == "Expense"
                        accountRepository.updateAccountBalance(
                            transaction.accountId,
                            transaction.amount,
                            isExpense = isExpense
                        )

                        updatedCount++
                    }
                }

                clearSelection()
                refreshScheduledTransactions()
                widgetUpdateUtil.updateAllFinancialWidgets()
                AppEventBus.tryEmitEvent(TransactionEvent.TransactionsUpdated)
                emitUiEvent(ScheduleUiEvent.BulkOperationCompleted("Mark as Paid", updatedCount))

            } catch (e: Exception) {
                Log.e("ScheduleViewModel", "Error bulk marking as paid: ${e.message}", e)
                showError("Failed to mark transactions as paid: ${e.message}")
            }
        }
    }

    private fun bulkMarkAsUnpaid(transactionIds: List<Int>) {
        viewModelScope.launch {
            try {
                var updatedCount = 0

                transactionIds.forEach { id ->
                    val transaction = transactionRepository.getTransactionById(id)
                    if (transaction != null && transaction.isPaid) {
                        val updatedTransaction = transaction.copy(isPaid = false)
                        transactionRepository.updateTransaction(updatedTransaction)

                        // Reverse account balance
                        val isExpense = transaction.mode == "Income" // Reverse
                        accountRepository.updateAccountBalance(
                            transaction.accountId,
                            transaction.amount,
                            isExpense = isExpense
                        )

                        updatedCount++
                    }
                }

                clearSelection()
                refreshScheduledTransactions()
                widgetUpdateUtil.updateAllFinancialWidgets()
                AppEventBus.tryEmitEvent(TransactionEvent.TransactionsUpdated)
                emitUiEvent(ScheduleUiEvent.BulkOperationCompleted("Mark as Unpaid", updatedCount))

            } catch (e: Exception) {
                Log.e("ScheduleViewModel", "Error bulk marking as unpaid: ${e.message}", e)
                showError("Failed to mark transactions as unpaid: ${e.message}")
            }
        }
    }

    private fun bulkSnooze(transactionIds: List<Int>, snoozeUntil: Long) {
        viewModelScope.launch {
            try {
                var updatedCount = 0

                transactionIds.forEach { id ->
                    val transaction = transactionRepository.getTransactionById(id)
                    if (transaction != null) {
                        val updatedTransaction = transaction.copy(nextDueDate = snoozeUntil)
                        transactionRepository.updateTransaction(updatedTransaction)
                        updatedCount++
                    }
                }

                clearSelection()
                refreshScheduledTransactions()
                AppEventBus.tryEmitEvent(TransactionEvent.TransactionsUpdated)
                emitUiEvent(ScheduleUiEvent.BulkOperationCompleted("Snooze", updatedCount))

            } catch (e: Exception) {
                Log.e("ScheduleViewModel", "Error bulk snoozing: ${e.message}", e)
                showError("Failed to snooze transactions: ${e.message}")
            }
        }
    }

    private fun bulkReschedule(transactionIds: List<Int>, newDate: Long) {
        viewModelScope.launch {
            try {
                var updatedCount = 0

                transactionIds.forEach { id ->
                    val transaction = transactionRepository.getTransactionById(id)
                    if (transaction != null) {
                        val updatedTransaction = transaction.copy(
                            date = newDate,
                            nextDueDate = newDate
                        )
                        transactionRepository.updateTransaction(updatedTransaction)
                        updatedCount++
                    }
                }

                clearSelection()
                refreshScheduledTransactions()
                AppEventBus.tryEmitEvent(TransactionEvent.TransactionsUpdated)
                emitUiEvent(ScheduleUiEvent.BulkOperationCompleted("Reschedule", updatedCount))

            } catch (e: Exception) {
                Log.e("ScheduleViewModel", "Error bulk rescheduling: ${e.message}", e)
                showError("Failed to reschedule transactions: ${e.message}")
            }
        }
    }

    private fun bulkDelete(transactionIds: List<Int>) {
        deleteScheduledTransactions(transactionIds)
    }

    private fun payNow(transactionId: Int) {
        viewModelScope.launch {
            val transaction = transactionRepository.getTransactionById(transactionId)
            if (transaction != null) {
                markAsPaid(transaction)
            }
        }
    }

    private fun skipPayment(transactionId: Int) {
        viewModelScope.launch {
            try {
                val transaction = transactionRepository.getTransactionById(transactionId)
                if (transaction != null && transaction.recurrence != null) {
                    // Generate next recurring transaction without marking current as paid
                    transactionRepository.generateNextRecurringTransaction(transaction)

                    refreshScheduledTransactions()
                    emitUiEvent(ScheduleUiEvent.ShowMessage("Payment skipped, next occurrence scheduled"))
                }
            } catch (e: Exception) {
                Log.e("ScheduleViewModel", "Error skipping payment: ${e.message}", e)
                showError("Failed to skip payment: ${e.message}")
            }
        }
    }

    private fun payAll(transactionIds: List<Int>) {
        bulkMarkAsPaid(transactionIds)
    }

    private fun snoozeAll(transactionIds: List<Int>, hours: Int) {
        val snoozeUntil = System.currentTimeMillis() + (hours * 60 * 60 * 1000L)
        bulkSnooze(transactionIds, snoozeUntil)
    }

//    private fun recalculateTotals() {
//        viewModelScope.launch {
//            try {
//                val currentState = _state.value
//
//                // Get conversion rates
//                val accounts = accountRepository.getAllAccounts()
//                val mainCurrency = accounts.find { it.isMainAccount }?.currencyCode ?: "usd"
//                val conversionRates = emptyMap<String, Double>() // You should get this from your currency service
//
//                val totalAmount = CurrencyUtils.calculateTotalExpense(
//                    currentState.allTransactions,
//                    mainCurrency,
//                    conversionRates
//                )
//                val upcomingAmount = CurrencyUtils.calculateTotalExpense(
//                    currentState.upcomingTransactions,
//                    mainCurrency,
//                    conversionRates
//                )
//                val overdueAmount = CurrencyUtils.calculateTotalExpense(
//                    currentState.overdueTransactions,
//                    mainCurrency,
//                    conversionRates
//                )
//                val monthlyTotal = CurrencyUtils.calculateMonthlyTotal(
//                    currentState.allTransactions,
//                    mainCurrency,
//                    conversionRates
//                )
//                val yearlyTotal = CurrencyUtils.calculateYearlyTotal(
//                    currentState.allTransactions,
//                    mainCurrency,
//                    conversionRates
//                )
//
//                updateState { state ->
//                    state.copy(
//                        totalAmount = totalAmount,
//                        upcomingAmount = upcomingAmount,
//                        overdueAmount = overdueAmount,
//                        monthlyTotal = monthlyTotal,
//                        yearlyTotal = yearlyTotal
//                    )
//                }
//
//            } catch (e: Exception) {
//                Log.e("ScheduleViewModel", "Error recalculating totals: ${e.message}", e)
//            }
//        }
//    }

    private fun updateConversionRates(rates: Map<String, Double>) {
        // Update conversion rates and recalculate totals
        recalculateTotals()
    }

    private fun analyzeSchedule() {
        viewModelScope.launch {
            try {
                val currentState = _state.value

                // Implement schedule analysis logic
                val analysisData = ScheduleAnalysisData(
                    totalScheduledAmount = currentState.totalAmount,
                    upcomingAmount = currentState.upcomingAmount,
                    overdueAmount = currentState.overdueAmount,
                    averageTransactionAmount = if (currentState.allTransactions.isNotEmpty())
                        currentState.totalAmount / currentState.allTransactions.size else 0.0,
                    mostExpensiveTransaction = currentState.allTransactions.maxByOrNull { it.amount },
                    earliestUpcoming = currentState.upcomingTransactions.minByOrNull { it.nextDueDate ?: it.date },
                    latestOverdue = currentState.overdueTransactions.maxByOrNull { it.nextDueDate ?: it.date },
                    transactionsByType = currentState.allTransactions.groupBy { it.transactionType }
                        .mapValues { it.value.size },
                    transactionsByAccount = emptyMap(), // Implement account grouping
                    paymentTrends = emptyMap(), // Implement trend analysis
                    overdueRisk = calculateOverdueRisk(currentState.overdueTransactions),
                    recommendations = generateRecommendations(currentState)
                )

                emitUiEvent(ScheduleUiEvent.AnalysisComplete(analysisData))

            } catch (e: Exception) {
                Log.e("ScheduleViewModel", "Error analyzing schedule: ${e.message}", e)
                showError("Failed to analyze schedule: ${e.message}")
            }
        }
    }

    private fun calculateOverdueRisk(overdueTransactions: List<TransactionEntity>): OverdueRisk {
        val overdueCount = overdueTransactions.size
        val overdueAmount = overdueTransactions.sumOf { it.amount }
        val longestOverdueDays = overdueTransactions.maxOfOrNull { it.getDaysOverdue() } ?: 0

        val riskLevel = when {
            overdueCount == 0 -> RiskLevel.LOW
            overdueCount <= 2 && overdueAmount < 100 -> RiskLevel.LOW
            overdueCount <= 5 && overdueAmount < 500 -> RiskLevel.MEDIUM
            overdueCount <= 10 && overdueAmount < 1000 -> RiskLevel.HIGH
            else -> RiskLevel.CRITICAL
        }

        return OverdueRisk(
            riskLevel = riskLevel,
            overdueCount = overdueCount,
            overdueAmount = overdueAmount,
            longestOverdueDays = longestOverdueDays,
            projectedOverdueNextWeek = 0 // Implement projection logic
        )
    }

    private fun generateRecommendations(state: ScheduleScreenState): List<String> {
        val recommendations = mutableListOf<String>()

        if (state.overdueTransactions.isNotEmpty()) {
            recommendations.add("You have ${state.overdueTransactions.size} overdue transactions. Consider paying them soon.")
        }

        if (state.upcomingTransactions.filter { it.getDaysUntilDue() <= 3 }.isNotEmpty()) {
            recommendations.add("You have transactions due in the next 3 days. Plan your payments accordingly.")
        }

        // Add more recommendation logic here

        return recommendations
    }

    private fun getUpcomingInDays(days: Int) {
        viewModelScope.launch {
            try {
                val upcomingTransactions = ScheduleFilterUtils.getUpcomingInDays(
                    _state.value.allTransactions,
                    days
                )

                Log.d("ScheduleViewModel", "Found ${upcomingTransactions.size} transactions upcoming in next $days days")
                // You can emit this as a UI event or update state

            } catch (e: Exception) {
                Log.e("ScheduleViewModel", "Error getting upcoming transactions: ${e.message}", e)
                showError("Failed to get upcoming transactions: ${e.message}")
            }
        }
    }

    private fun getOverdueInDays(days: Int) {
        viewModelScope.launch {
            try {
                val overdueTransactions = ScheduleFilterUtils.getOverdueInDays(
                    _state.value.allTransactions,
                    days
                )

                Log.d("ScheduleViewModel", "Found ${overdueTransactions.size} transactions overdue in last $days days")
                // You can emit this as a UI event or update state

            } catch (e: Exception) {
                Log.e("ScheduleViewModel", "Error getting overdue transactions: ${e.message}", e)
                showError("Failed to get overdue transactions: ${e.message}")
            }
        }
    }

    private fun exportSchedule() {
        viewModelScope.launch {
            try {
                // Implement export functionality
                emitUiEvent(ScheduleUiEvent.ExportCompleted)

            } catch (e: Exception) {
                Log.e("ScheduleViewModel", "Error exporting schedule: ${e.message}", e)
                showError("Failed to export schedule: ${e.message}")
            }
        }
    }

    private fun navigateToAddTransaction(defaultType: TransactionType) {
        emitUiEvent(
            ScheduleUiEvent.NavigateToScreen(
                "add_transaction",
                mapOf("defaultType" to defaultType.name)
            )
        )
    }

    private fun navigateToEditTransaction(transactionId: Int) {
        emitUiEvent(
            ScheduleUiEvent.NavigateToScreen(
                "edit_transaction",
                mapOf("transactionId" to transactionId)
            )
        )
    }

    private fun showError(message: String) {
        updateState { it.copy(error = message) }
        emitUiEvent(ScheduleUiEvent.ShowMessage(message, true))
    }

    private fun clearError() {
        updateState { it.copy(error = null) }
    }

    private fun setLoading(isLoading: Boolean) {
        updateState { it.copy(isLoading = isLoading) }
    }

    private fun updateLastRefresh(timestamp: Long) {
        updateState { it.copy(lastUpdated = timestamp) }
    }
}