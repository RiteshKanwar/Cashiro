package com.ritesh.cashiro.presentation.ui.features.reports

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ritesh.cashiro.domain.repository.AccountRepository
import com.ritesh.cashiro.domain.repository.CategoryRepository
import com.ritesh.cashiro.domain.repository.TransactionRepository
import com.ritesh.cashiro.domain.utils.AppEventBus
import com.ritesh.cashiro.domain.utils.CurrencyEvent
import com.ritesh.cashiro.presentation.ui.extras.components.charts.MonthPeriod
import com.ritesh.cashiro.presentation.ui.extras.components.charts.WeekPeriod
import com.ritesh.cashiro.presentation.ui.extras.components.charts.YearPeriod
import com.ritesh.cashiro.domain.utils.TransactionEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

//@HiltViewModel
//class ReportScreenViewModel @Inject constructor(
//    private val transactionRepository: TransactionRepository,
//    private val accountRepository: AccountRepository,
//    private val categoryRepository: CategoryRepository
//) : ViewModel() {
//    // Single unified state
//    private val _state = MutableStateFlow(ReportScreenState())
//    val state: StateFlow<ReportScreenState> = _state.asStateFlow()
//
//    init {
//        loadData()
//        resetToCurrentTimePeriod()
//        listenForTransactionEvents()
//        observeEvents()
//    }
//
//    private fun listenForTransactionEvents() {
//        viewModelScope.launch {
//            AppEventBus.events.collect { event ->
//                when (event) {
//                    is TransactionEvent.TransactionsUpdated -> {
//                        // Refresh data when transactions are updated
//                        loadData()
//                    }
//                }
//            }
//        }
//    }
//
//    private fun loadData() {
//        viewModelScope.launch {
//            try {
//                // Set loading state
//                _state.update { it.copy(isLoading = true, error = null) }
//
//                // Concurrently load all data
//                val transactions = transactionRepository.getAllTransactions()
//                val accounts = accountRepository.getAllAccounts()
//                val categories = categoryRepository.getAllCategories()
//
//                // Update state with loaded data
//                _state.update { currentState ->
//                    currentState.copy(
//                        transactions = transactions,
//                        accounts = accounts,
//                        categories = categories,
//                        isLoading = false
//                    )
//                }
//            } catch (e: Exception) {
//                // Handle errors
//                _state.update {
//                    it.copy(
//                        isLoading = false,
//                        error = "Failed to load data: ${e.message}"
//                    )
//                }
//                Log.e("ReportViewModel", "Error loading data", e)
//            }
//        }
//    }
//    fun onEvent(event: ReportScreenEvent) {
//        when (event) {
//            is ReportScreenEvent.TabSelected -> updateSelectedTab(event.index)
//            is ReportScreenEvent.NextPeriod -> goToNextPeriod()
//            is ReportScreenEvent.PreviousPeriod -> goToPreviousPeriod()
//            is ReportScreenEvent.ResetToCurrentPeriod -> resetToCurrentTimePeriod()
//            is ReportScreenEvent.RefreshData -> refreshData()
//        }
//    }
//
//    private fun updateSelectedTab(index: Int) {
//        val currentDate = _state.value.currentDate
//        val timePeriod = when (index) {
//            0 -> WeekPeriod(currentDate)
//            1 -> MonthPeriod(currentDate)
//            2 -> YearPeriod(currentDate)
//            else -> WeekPeriod(currentDate)
//        }
//
//        _state.update {
//            it.copy(
//                selectedTabIndex = index,
//                currentTimePeriod = timePeriod
//            )
//        }
//    }
//
//    private fun goToNextPeriod() {
//        val nextPeriod = _state.value.currentTimePeriod.next()
//        _state.update {
//            it.copy(
//                currentTimePeriod = nextPeriod,
//                currentDate = nextPeriod.getDate()
//            )
//        }
//    }
//
//    private fun goToPreviousPeriod() {
//        val previousPeriod = _state.value.currentTimePeriod.previous()
//        _state.update {
//            it.copy(
//                currentTimePeriod = previousPeriod,
//                currentDate = previousPeriod.getDate()
//            )
//        }
//    }
//
//    private fun resetToCurrentTimePeriod() {
//        val today = LocalDate.now()
//        val currentIndex = _state.value.selectedTabIndex
//        val timePeriod = when (currentIndex) {
//            0 -> WeekPeriod(today)
//            1 -> MonthPeriod(today)
//            2 -> YearPeriod(today)
//            else -> WeekPeriod(today)
//        }
//
//        _state.update {
//            it.copy(
//                currentDate = today,
//                currentTimePeriod = timePeriod
//            )
//        }
//    }
//    private fun observeEvents() {
//        viewModelScope.launch {
//            AppEventBus.events.collect { event ->
//                when (event) {
//                    is TransactionEvent.TransactionsUpdated -> {
//                        // Refresh data when transactions are updated
//                        refreshData()
//                    }
//                    is CurrencyEvent.AccountCurrencyChanged -> {
//                        // Refresh data when an account's currency changes
//                        refreshData()
//                    }
//                    is CurrencyEvent.MainAccountCurrencyChanged -> {
//                        // Refresh data when the main account currency changes
//                        refreshData()
//                    }
//                    is CurrencyEvent.ConversionRatesUpdated -> {
//                        // Refresh data when conversion rates are updated
//                        refreshData()
//                    }
//                }
//            }
//        }
//    }
//
//    fun refreshData() {
//        viewModelScope.launch {
//            _state.update { it.copy(isLoading = true) }
//
//            try {
//                // Force a refresh of transaction data from the repository
//                val transactions = transactionRepository.getAllTransactions()
//                val accounts = accountRepository.getAllAccounts()
//                val categories = categoryRepository.getAllCategories()
//
//                _state.update { currentState ->
//                    currentState.copy(
//                        transactions = transactions,
//                        accounts = accounts,
//                        categories = categories,
//                        isLoading = false,
//                        error = null
//                    )
//                }
//            } catch (e: Exception) {
//                _state.update {
//                    it.copy(
//                        isLoading = false,
//                        error = "Failed to refresh data: ${e.message}"
//                    )
//                }
//                Log.e("ReportViewModel", "Error refreshing data", e)
//            }
//        }
//    }
//}
@HiltViewModel
class ReportScreenViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    // Single unified state
    private val _state = MutableStateFlow(ReportScreenState())
    val state: StateFlow<ReportScreenState> = _state.asStateFlow()

    init {
        loadData()
        resetToCurrentTimePeriod()
        listenForTransactionEvents()
        observeEvents()
    }

    private fun listenForTransactionEvents() {
        viewModelScope.launch {
            AppEventBus.events.collect { event ->
                when (event) {
                    is TransactionEvent.TransactionsUpdated -> {
                        // Refresh data when transactions are updated
                        loadData()
                    }
                }
            }
        }
    }

//    private fun loadData() {
//        viewModelScope.launch {
//            try {
//                // Set loading state
//                _state.update { it.copy(isLoading = true, error = null) }
//
//                // Concurrently load all data
//                val transactions = transactionRepository.getAllTransactions()
//                val accounts = accountRepository.getAllAccounts()
//                val categories = categoryRepository.getAllCategories()
//
//                // Update state with loaded data
//                _state.update { currentState ->
//                    currentState.copy(
//                        transactions = transactions,
//                        accounts = accounts,
//                        categories = categories,
//                        isLoading = false
//                    )
//                }
//            } catch (e: Exception) {
//                // Handle errors
//                _state.update {
//                    it.copy(
//                        isLoading = false,
//                        error = "Failed to load data: ${e.message}"
//                    )
//                }
//                Log.e("ReportViewModel", "Error loading data", e)
//            }
//        }
//    }
    private fun loadData() {
        viewModelScope.launch {
            try {
                // Set loading state only if no data exists yet
                if (_state.value.transactions.isEmpty()) {
                    _state.update { it.copy(isLoading = true, error = null) }
                }

                // Concurrently load all data
                val transactions = transactionRepository.getAllTransactions()
                val accounts = accountRepository.getAllAccounts()
                val categories = categoryRepository.getAllCategories()

                // Update state with loaded data
                _state.update { currentState ->
                    currentState.copy(
                        transactions = transactions,
                        accounts = accounts,
                        categories = categories,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                // Handle errors
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load data: ${e.message}"
                    )
                }
                Log.e("ReportViewModel", "Error loading data", e)
            }
        }
    }
    fun onEvent(event: ReportScreenEvent) {
        when (event) {
            is ReportScreenEvent.TabSelected -> updateSelectedTab(event.index)
            is ReportScreenEvent.NextPeriod -> goToNextPeriod()
            is ReportScreenEvent.PreviousPeriod -> goToPreviousPeriod()
            is ReportScreenEvent.ResetToCurrentPeriod -> resetToCurrentTimePeriod()
            is ReportScreenEvent.RefreshData -> refreshData()
        }
    }

    // FIXED: Updated to handle both tab selection and time period update in one place
    private fun updateSelectedTab(index: Int) {
        val currentDate = _state.value.currentDate
        val timePeriod = when (index) {
            0 -> WeekPeriod(currentDate)
            1 -> MonthPeriod(currentDate)
            2 -> YearPeriod(currentDate)
            else -> WeekPeriod(currentDate)
        }

        _state.update {
            it.copy(
                selectedTabIndex = index,
                currentTimePeriod = timePeriod
            )
        }
    }

    private fun goToNextPeriod() {
        val nextPeriod = _state.value.currentTimePeriod.next()
        _state.update {
            it.copy(
                currentTimePeriod = nextPeriod,
                currentDate = nextPeriod.getDate()
            )
        }
    }

    private fun goToPreviousPeriod() {
        val previousPeriod = _state.value.currentTimePeriod.previous()
        _state.update {
            it.copy(
                currentTimePeriod = previousPeriod,
                currentDate = previousPeriod.getDate()
            )
        }
    }

    private fun resetToCurrentTimePeriod() {
        val today = LocalDate.now()
        val currentIndex = _state.value.selectedTabIndex
        val timePeriod = when (currentIndex) {
            0 -> WeekPeriod(today)
            1 -> MonthPeriod(today)
            2 -> YearPeriod(today)
            else -> WeekPeriod(today)
        }

        _state.update {
            it.copy(
                currentDate = today,
                currentTimePeriod = timePeriod
            )
        }
    }

    private fun observeEvents() {
        viewModelScope.launch {
            AppEventBus.events.collect { event ->
                when (event) {
                    is TransactionEvent.TransactionsUpdated -> {
                        // Refresh data when transactions are updated
                        refreshData()
                    }
                    is CurrencyEvent.AccountCurrencyChanged -> {
                        // Refresh data when an account's currency changes
                        refreshData()
                    }
                    is CurrencyEvent.MainAccountCurrencyChanged -> {
                        // Refresh data when the main account currency changes
                        refreshData()
                    }
                    is CurrencyEvent.ConversionRatesUpdated -> {
                        // Refresh data when conversion rates are updated
                        refreshData()
                    }
                }
            }
        }
    }

//    fun refreshData() {
//        viewModelScope.launch {
//            _state.update { it.copy(isLoading = true) }
//
//            try {
//                // Force a refresh of transaction data from the repository
//                val transactions = transactionRepository.getAllTransactions()
//                val accounts = accountRepository.getAllAccounts()
//                val categories = categoryRepository.getAllCategories()
//
//                _state.update { currentState ->
//                    currentState.copy(
//                        transactions = transactions,
//                        accounts = accounts,
//                        categories = categories,
//                        isLoading = false,
//                        error = null
//                    )
//                }
//            } catch (e: Exception) {
//                _state.update {
//                    it.copy(
//                        isLoading = false,
//                        error = "Failed to refresh data: ${e.message}"
//                    )
//                }
//                Log.e("ReportViewModel", "Error refreshing data", e)
//            }
//        }
//    }
    fun refreshData() {
        viewModelScope.launch {
            try {
                // Set loading state
                _state.update { it.copy(isLoading = true, error = null) }

                // Force a refresh of data from the repository
                val transactions = transactionRepository.getAllTransactions()
                val accounts = accountRepository.getAllAccounts()
                val categories = categoryRepository.getAllCategories()

                // FIXED: Remove artificial delay - let the UI handle timing naturally
                _state.update { currentState ->
                    currentState.copy(
                        transactions = transactions,
                        accounts = accounts,
                        categories = categories,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to refresh data: ${e.message}"
                    )
                }
                Log.e("ReportViewModel", "Error refreshing data", e)
            }
        }
    }
}