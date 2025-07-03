package com.ritesh.cashiro.presentation.ui.features.subscriptions

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
import com.ritesh.cashiro.domain.utils.TransactionEvent
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
class SubscriptionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val widgetUpdateUtil: WidgetUpdateUtil,
    application: Application
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(SubscriptionScreenState())
    val state: StateFlow<SubscriptionScreenState> = _state.asStateFlow()

    private val _uiEvents = MutableSharedFlow<SubscriptionUiEvent>()
    val uiEvents: SharedFlow<SubscriptionUiEvent> = _uiEvents.asSharedFlow()

    init {
        // Load subscriptions on initialization
        onEvent(SubscriptionEvent.LoadSubscriptions)

        // Listen for app-wide events
        viewModelScope.launch {
            AppEventBus.events.collect { event ->
                when (event) {
                    is TransactionEvent.TransactionsUpdated -> {
                        onEvent(SubscriptionEvent.RefreshSubscriptions)
                    }
                    is CurrencyEvent.ConversionRatesUpdated -> {
                        onEvent(SubscriptionEvent.UpdateConversionRates(event.rates))
                        onEvent(SubscriptionEvent.RecalculateTotals)
                    }
                    is AccountEvent.AccountsUpdated -> {
                        onEvent(SubscriptionEvent.RecalculateTotals)
                    }
                }
            }
        }
    }

    fun onEvent(event: SubscriptionEvent) {
        when (event) {
            // Data Loading Events
            is SubscriptionEvent.LoadSubscriptions -> loadSubscriptions()
            is SubscriptionEvent.RefreshSubscriptions -> refreshSubscriptions()

            // Filter Events
            is SubscriptionEvent.UpdatePeriodFilter -> updatePeriodFilter(event.period)
            is SubscriptionEvent.UpdateFilterState -> updateFilterState(event.filterState)
            is SubscriptionEvent.UpdateAccountFilter -> updateAccountFilter(event.accountIds)
            is SubscriptionEvent.UpdatePaymentStatusFilter -> updatePaymentStatusFilter(event.statuses)
            is SubscriptionEvent.UpdateAmountFilter -> updateAmountFilter(event.minAmount, event.maxAmount)
            is SubscriptionEvent.UpdateSearchFilter -> updateSearchFilter(event.searchText)
            is SubscriptionEvent.UpdateSortBy -> updateSortBy(event.sortBy)
            is SubscriptionEvent.UpdateActiveFilter -> updateActiveFilter(event.showActiveOnly)
            is SubscriptionEvent.UpdateExpiredFilter -> updateExpiredFilter(event.showExpiredOnly)
            is SubscriptionEvent.ClearAllFilters -> clearAllFilters()

            // Selection Events
            is SubscriptionEvent.ToggleSubscriptionSelection -> toggleSubscriptionSelection(event.subscriptionId)
            is SubscriptionEvent.SelectSubscription -> selectSubscription(event.subscriptionId)
            is SubscriptionEvent.DeselectSubscription -> deselectSubscription(event.subscriptionId)
            is SubscriptionEvent.SelectAllSubscriptions -> selectAllSubscriptions()
            is SubscriptionEvent.ClearSelection -> clearSelection()
            is SubscriptionEvent.SetSelectionMode -> setSelectionMode(event.isEnabled)

            // Subscription Management Events
            is SubscriptionEvent.MarkAsPaid -> markAsPaid(event.subscription)
            is SubscriptionEvent.MarkAsUnpaid -> markAsUnpaid(event.subscription)
            is SubscriptionEvent.UpdateSubscription -> updateSubscription(event.subscription)
            is SubscriptionEvent.DeleteSubscription -> deleteSubscription(event.subscriptionId)
            is SubscriptionEvent.DeleteSubscriptions -> deleteSubscriptions(event.subscriptionIds)

            // Bulk Operations Events
            is SubscriptionEvent.BulkMarkAsPaid -> bulkMarkAsPaid(event.subscriptionIds)
            is SubscriptionEvent.BulkMarkAsUnpaid -> bulkMarkAsUnpaid(event.subscriptionIds)

            // Calculation Events
            is SubscriptionEvent.RecalculateTotals -> recalculateTotals()
            is SubscriptionEvent.UpdateConversionRates -> updateConversionRates(event.rates)

            // Error Handling Events
            is SubscriptionEvent.ShowError -> showError(event.message)
            is SubscriptionEvent.ClearError -> clearError()

            // UI State Events
            is SubscriptionEvent.SetLoading -> setLoading(event.isLoading)
            is SubscriptionEvent.UpdateLastRefresh -> updateLastRefresh(event.timestamp)

            // Analysis Events
            is SubscriptionEvent.AnalyzeSpending -> analyzeSpending()
            is SubscriptionEvent.GetUpcomingPayments -> getUpcomingPayments(event.days)

            // Export Events
            is SubscriptionEvent.ExportSubscriptions -> exportSubscriptions()

            // Navigation Events
            is SubscriptionEvent.NavigateToAddSubscription -> navigateToAddSubscription(event.defaultType)
            is SubscriptionEvent.NavigateToEditSubscription -> navigateToEditSubscription(event.subscriptionId)

            // Unhandled events - log for debugging
            else -> {
                Log.d("SubscriptionViewModel", "Unhandled event: ${event::class.simpleName}")
            }
        }
    }

    private fun updateState(update: (SubscriptionScreenState) -> SubscriptionScreenState) {
        _state.update(update)
    }

    private fun emitUiEvent(event: SubscriptionUiEvent) {
        viewModelScope.launch {
            _uiEvents.emit(event)
        }
    }

    private fun loadSubscriptions() {
        viewModelScope.launch {
            try {
                updateState { it.copy(isLoading = true, error = null) }

                val subscriptions = transactionRepository.getSubscriptionTransactions()
                val repetitiveTransactions = transactionRepository.getRepetitiveTransactions()
                val allSubscriptions = subscriptions + repetitiveTransactions

                Log.d("SubscriptionViewModel", "Loaded ${allSubscriptions.size} subscriptions")

                updateState { currentState ->
                    currentState.copy(
                        subscriptions = allSubscriptions,
                        isLoading = false,
                        lastUpdated = System.currentTimeMillis()
                    )
                }

                recalculateTotals()
                emitUiEvent(SubscriptionUiEvent.SubscriptionsLoaded)

            } catch (e: Exception) {
                Log.e("SubscriptionViewModel", "Error loading subscriptions: ${e.message}", e)
                updateState { it.copy(isLoading = false, error = "Failed to load subscriptions: ${e.message}") }
                emitUiEvent(SubscriptionUiEvent.ShowMessage("Failed to load subscriptions", true))
            }
        }
    }

    private fun refreshSubscriptions() {
        viewModelScope.launch {
            try {
                val subscriptions = transactionRepository.getSubscriptionTransactions()
                val repetitiveTransactions = transactionRepository.getRepetitiveTransactions()
                val allSubscriptions = subscriptions + repetitiveTransactions

                updateState { currentState ->
                    currentState.copy(
                        subscriptions = allSubscriptions,
                        lastUpdated = System.currentTimeMillis()
                    )
                }

                recalculateTotals()
                emitUiEvent(SubscriptionUiEvent.SubscriptionsRefreshed)

            } catch (e: Exception) {
                Log.e("SubscriptionViewModel", "Error refreshing subscriptions: ${e.message}", e)
                showError("Failed to refresh subscriptions: ${e.message}")
            }
        }
    }

    private fun updatePeriodFilter(period: SubscriptionPeriod) {
        updateState { currentState ->
            currentState.copy(
                selectedPeriod = period,
                filterState = currentState.filterState.copy(selectedPeriod = period)
            )
        }
        recalculateTotals()
    }

    private fun updateFilterState(filterState: SubscriptionFilterState) {
        updateState { it.copy(filterState = filterState) }
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

    private fun updateSortBy(sortBy: SubscriptionSortBy) {
        updateState { currentState ->
            currentState.copy(
                filterState = currentState.filterState.copy(sortBy = sortBy)
            )
        }
    }

    private fun updateActiveFilter(showActiveOnly: Boolean) {
        updateState { currentState ->
            currentState.copy(
                filterState = currentState.filterState.copy(showActiveOnly = showActiveOnly)
            )
        }
    }

    private fun updateExpiredFilter(showExpiredOnly: Boolean) {
        updateState { currentState ->
            currentState.copy(
                filterState = currentState.filterState.copy(showExpiredOnly = showExpiredOnly)
            )
        }
    }

    private fun clearAllFilters() {
        updateState { currentState ->
            currentState.copy(
                selectedPeriod = SubscriptionPeriod.ALL,
                filterState = SubscriptionFilterState()
            )
        }
        recalculateTotals()
    }

    private fun toggleSubscriptionSelection(subscriptionId: Int) {
        updateState { currentState ->
            val selectedSubscriptions = currentState.selectedSubscriptions
            val newSelection = if (selectedSubscriptions.contains(subscriptionId)) {
                selectedSubscriptions - subscriptionId
            } else {
                selectedSubscriptions + subscriptionId
            }

            currentState.copy(
                selectedSubscriptions = newSelection,
                isInSelectionMode = newSelection.isNotEmpty()
            )
        }
    }

    private fun selectSubscription(subscriptionId: Int) {
        updateState { currentState ->
            currentState.copy(
                selectedSubscriptions = currentState.selectedSubscriptions + subscriptionId,
                isInSelectionMode = true
            )
        }
    }

    private fun deselectSubscription(subscriptionId: Int) {
        updateState { currentState ->
            val newSelection = currentState.selectedSubscriptions - subscriptionId
            currentState.copy(
                selectedSubscriptions = newSelection,
                isInSelectionMode = newSelection.isNotEmpty()
            )
        }
    }

    private fun selectAllSubscriptions() {
        updateState { currentState ->
            val allIds = currentState.filteredSubscriptions.map { it.id }.toSet()
            currentState.copy(
                selectedSubscriptions = allIds,
                isInSelectionMode = allIds.isNotEmpty()
            )
        }
    }

    private fun clearSelection() {
        updateState { currentState ->
            currentState.copy(
                selectedSubscriptions = emptySet(),
                isInSelectionMode = false
            )
        }
    }

    private fun setSelectionMode(isEnabled: Boolean) {
        updateState { currentState ->
            currentState.copy(
                isInSelectionMode = isEnabled,
                selectedSubscriptions = if (!isEnabled) emptySet() else currentState.selectedSubscriptions
            )
        }
    }

    private fun markAsPaid(subscription: TransactionEntity) {
        viewModelScope.launch {
            try {
                when (subscription.transactionType) {
                    TransactionType.SUBSCRIPTION, TransactionType.REPETITIVE -> {
                        val updatedTransaction = subscription.copy(isPaid = true)
                        transactionRepository.updateTransaction(updatedTransaction)

                        // Update account balance
                        val isExpense = subscription.mode == "Expense"
                        accountRepository.updateAccountBalance(
                            subscription.accountId,
                            subscription.amount,
                            isExpense = isExpense
                        )

                        // Generate next recurring transaction if needed
                        if (subscription.recurrence != null) {
                            transactionRepository.generateNextRecurringTransaction(updatedTransaction)
                        }
                    }

                    else -> {
                        val updatedTransaction = subscription.copy(isPaid = true)
                        transactionRepository.updateTransaction(updatedTransaction)
                    }
                }

                refreshSubscriptions()
                widgetUpdateUtil.updateAllFinancialWidgets()
                AppEventBus.tryEmitEvent(TransactionEvent.TransactionsUpdated)
                emitUiEvent(SubscriptionUiEvent.SubscriptionMarkedAsPaid(subscription.id))

            } catch (e: Exception) {
                Log.e("SubscriptionViewModel", "Error marking subscription as paid: ${e.message}", e)
                showError("Failed to mark subscription as paid: ${e.message}")
            }
        }
    }

    private fun markAsUnpaid(subscription: TransactionEntity) {
        viewModelScope.launch {
            try {
                when (subscription.transactionType) {
                    TransactionType.SUBSCRIPTION, TransactionType.REPETITIVE -> {
                        val updatedTransaction = subscription.copy(isPaid = false)
                        transactionRepository.updateTransaction(updatedTransaction)

                        // Reverse account balance update
                        val isExpense = subscription.mode == "Income" // Reverse
                        accountRepository.updateAccountBalance(
                            subscription.accountId,
                            subscription.amount,
                            isExpense = isExpense
                        )
                    }

                    else -> {
                        val updatedTransaction = subscription.copy(isPaid = false)
                        transactionRepository.updateTransaction(updatedTransaction)
                    }
                }

                refreshSubscriptions()
                widgetUpdateUtil.updateAllFinancialWidgets()
                AppEventBus.tryEmitEvent(TransactionEvent.TransactionsUpdated)
                emitUiEvent(SubscriptionUiEvent.SubscriptionMarkedAsUnpaid(subscription.id))

            } catch (e: Exception) {
                Log.e("SubscriptionViewModel", "Error marking subscription as unpaid: ${e.message}", e)
                showError("Failed to mark subscription as unpaid: ${e.message}")
            }
        }
    }

    private fun updateSubscription(subscription: TransactionEntity) {
        viewModelScope.launch {
            try {
                transactionRepository.updateTransaction(subscription)
                refreshSubscriptions()
                widgetUpdateUtil.updateAllFinancialWidgets()
                AppEventBus.tryEmitEvent(TransactionEvent.TransactionUpdated(subscription.id))
                emitUiEvent(SubscriptionUiEvent.SubscriptionUpdated(subscription.id))

            } catch (e: Exception) {
                Log.e("SubscriptionViewModel", "Error updating subscription: ${e.message}", e)
                showError("Failed to update subscription: ${e.message}")
            }
        }
    }

    private fun deleteSubscription(subscriptionId: Int) {
        viewModelScope.launch {
            try {
                val subscription = transactionRepository.getTransactionById(subscriptionId)
                if (subscription != null) {
                    // Restore account balance if subscription was paid
                    if (subscription.isPaid) {
                        val isExpense = subscription.mode == "Income" // Reverse
                        accountRepository.updateAccountBalance(
                            subscription.accountId,
                            subscription.amount,
                            isExpense = isExpense
                        )
                    }

                    transactionRepository.deleteTransactionById(subscriptionId)
                    refreshSubscriptions()
                    widgetUpdateUtil.updateAllFinancialWidgets()
                    AppEventBus.tryEmitEvent(TransactionEvent.TransactionDeleted(subscriptionId))
                    emitUiEvent(SubscriptionUiEvent.SubscriptionDeleted(subscriptionId))
                }

            } catch (e: Exception) {
                Log.e("SubscriptionViewModel", "Error deleting subscription: ${e.message}", e)
                showError("Failed to delete subscription: ${e.message}")
            }
        }
    }

    private fun deleteSubscriptions(subscriptionIds: List<Int>) {
        viewModelScope.launch {
            try {
                var deletedCount = 0

                subscriptionIds.forEach { id ->
                    val subscription = transactionRepository.getTransactionById(id)
                    if (subscription != null) {
                        // Restore account balance if subscription was paid
                        if (subscription.isPaid) {
                            val isExpense = subscription.mode == "Income" // Reverse
                            accountRepository.updateAccountBalance(
                                subscription.accountId,
                                subscription.amount,
                                isExpense = isExpense
                            )
                        }

                        transactionRepository.deleteTransactionById(id)
                        deletedCount++
                    }
                }

                clearSelection()
                refreshSubscriptions()
                widgetUpdateUtil.updateAllFinancialWidgets()
                AppEventBus.tryEmitEvent(TransactionEvent.TransactionsUpdated)
                emitUiEvent(SubscriptionUiEvent.SubscriptionsDeleted(deletedCount))

            } catch (e: Exception) {
                Log.e("SubscriptionViewModel", "Error deleting subscriptions: ${e.message}", e)
                showError("Failed to delete subscriptions: ${e.message}")
            }
        }
    }

    private fun bulkMarkAsPaid(subscriptionIds: List<Int>) {
        viewModelScope.launch {
            try {
                var updatedCount = 0

                subscriptionIds.forEach { id ->
                    val subscription = transactionRepository.getTransactionById(id)
                    if (subscription != null && !subscription.isPaid) {
                        val updatedTransaction = subscription.copy(isPaid = true)
                        transactionRepository.updateTransaction(updatedTransaction)

                        // Update account balance
                        val isExpense = subscription.mode == "Expense"
                        accountRepository.updateAccountBalance(
                            subscription.accountId,
                            subscription.amount,
                            isExpense = isExpense
                        )

                        updatedCount++
                    }
                }

                clearSelection()
                refreshSubscriptions()
                widgetUpdateUtil.updateAllFinancialWidgets()
                AppEventBus.tryEmitEvent(TransactionEvent.TransactionsUpdated)
                emitUiEvent(SubscriptionUiEvent.BulkOperationCompleted("Mark as Paid", updatedCount))

            } catch (e: Exception) {
                Log.e("SubscriptionViewModel", "Error bulk marking as paid: ${e.message}", e)
                showError("Failed to mark subscriptions as paid: ${e.message}")
            }
        }
    }

    private fun bulkMarkAsUnpaid(subscriptionIds: List<Int>) {
        viewModelScope.launch {
            try {
                var updatedCount = 0

                subscriptionIds.forEach { id ->
                    val subscription = transactionRepository.getTransactionById(id)
                    if (subscription != null && subscription.isPaid) {
                        val updatedTransaction = subscription.copy(isPaid = false)
                        transactionRepository.updateTransaction(updatedTransaction)

                        // Reverse account balance
                        val isExpense = subscription.mode == "Income" // Reverse
                        accountRepository.updateAccountBalance(
                            subscription.accountId,
                            subscription.amount,
                            isExpense = isExpense
                        )

                        updatedCount++
                    }
                }

                clearSelection()
                refreshSubscriptions()
                widgetUpdateUtil.updateAllFinancialWidgets()
                AppEventBus.tryEmitEvent(TransactionEvent.TransactionsUpdated)
                emitUiEvent(SubscriptionUiEvent.BulkOperationCompleted("Mark as Unpaid", updatedCount))

            } catch (e: Exception) {
                Log.e("SubscriptionViewModel", "Error bulk marking as unpaid: ${e.message}", e)
                showError("Failed to mark subscriptions as unpaid: ${e.message}")
            }
        }
    }

    private fun recalculateTotals() {
        viewModelScope.launch {
            try {
                val currentState = _state.value
                val subscriptions = currentState.filteredSubscriptions

                // Get conversion rates
                val accounts = accountRepository.getAllAccounts()
                val mainCurrency = accounts.find { it.isMainAccount }?.currencyCode ?: "usd"
                val conversionRates = emptyMap<String, Double>() // You should get this from your currency service

                val totalAmount = CurrencyUtils.calculateTotalExpense(subscriptions, mainCurrency, conversionRates)
                val paidAmount = CurrencyUtils.calculateTotalExpense(
                    subscriptions.filter { it.isPaid },
                    mainCurrency,
                    conversionRates
                )
                val monthlyTotal = CurrencyUtils.calculateMonthlyTotal(subscriptions, mainCurrency, conversionRates)
                val yearlyTotal = CurrencyUtils.calculateYearlyTotal(subscriptions, mainCurrency, conversionRates)

                updateState { state ->
                    state.copy(
                        totalAmount = totalAmount,
                        paidAmount = paidAmount,
                        unpaidAmount = totalAmount - paidAmount,
                        monthlyTotal = monthlyTotal,
                        yearlyTotal = yearlyTotal
                    )
                }

            } catch (e: Exception) {
                Log.e("SubscriptionViewModel", "Error recalculating totals: ${e.message}", e)
            }
        }
    }

    private fun updateConversionRates(rates: Map<String, Double>) {
        // Update conversion rates and recalculate totals
        recalculateTotals()
    }

    private fun analyzeSpending() {
        viewModelScope.launch {
            try {
                val subscriptions = _state.value.subscriptions
                // Implement spending analysis logic
                val analysisData = SubscriptionAnalysisData(
                    totalMonthlySpending = _state.value.monthlyTotal,
                    totalYearlySpending = _state.value.yearlyTotal,
                    averageSubscriptionCost = if (subscriptions.isNotEmpty()) _state.value.totalAmount / subscriptions.size else 0.0,
                    mostExpensiveSubscription = subscriptions.maxByOrNull { it.amount },
                    leastExpensiveSubscription = subscriptions.minByOrNull { it.amount },
                    upcomingPayments = subscriptions.filter { !it.isPaid && (it.nextDueDate ?: 0) > System.currentTimeMillis() },
                    expiredSubscriptions = subscriptions.filter {
                        val endDate = it.recurrence?.endRecurrenceDate
                        endDate != null && endDate <= System.currentTimeMillis()
                    },
                    paymentsByCategory = emptyMap(), // Implement category grouping
                    paymentTrends = emptyMap(), // Implement trend analysis
                    recommendedActions = emptyList() // Implement recommendations
                )

                emitUiEvent(SubscriptionUiEvent.AnalysisComplete(analysisData))

            } catch (e: Exception) {
                Log.e("SubscriptionViewModel", "Error analyzing spending: ${e.message}", e)
                showError("Failed to analyze spending: ${e.message}")
            }
        }
    }

    private fun getUpcomingPayments(days: Int) {
        viewModelScope.launch {
            try {
                val currentTime = System.currentTimeMillis()
                val futureTime = currentTime + (days * 24 * 60 * 60 * 1000L)

                val upcomingPayments = _state.value.subscriptions.filter { subscription ->
                    val nextDue = subscription.nextDueDate ?: subscription.date
                    !subscription.isPaid && nextDue in currentTime..futureTime
                }

                // You can emit this as a UI event or update state
                Log.d("SubscriptionViewModel", "Found ${upcomingPayments.size} upcoming payments in next $days days")

            } catch (e: Exception) {
                Log.e("SubscriptionViewModel", "Error getting upcoming payments: ${e.message}", e)
                showError("Failed to get upcoming payments: ${e.message}")
            }
        }
    }

    private fun exportSubscriptions() {
        viewModelScope.launch {
            try {
                // Implement export functionality
                emitUiEvent(SubscriptionUiEvent.ExportCompleted)

            } catch (e: Exception) {
                Log.e("SubscriptionViewModel", "Error exporting subscriptions: ${e.message}", e)
                showError("Failed to export subscriptions: ${e.message}")
            }
        }
    }

    private fun navigateToAddSubscription(defaultType: TransactionType) {
        emitUiEvent(
            SubscriptionUiEvent.NavigateToScreen(
                "add_transaction",
                mapOf("defaultType" to defaultType.name)
            )
        )
    }

    private fun navigateToEditSubscription(subscriptionId: Int) {
        emitUiEvent(
            SubscriptionUiEvent.NavigateToScreen(
                "edit_transaction",
                mapOf("transactionId" to subscriptionId)
            )
        )
    }

    private fun showError(message: String) {
        updateState { it.copy(error = message) }
        emitUiEvent(SubscriptionUiEvent.ShowMessage(message, true))
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