package com.ritesh.cashiro.presentation.ui.features.home

import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ritesh.cashiro.data.local.entity.TransactionEntity
import com.ritesh.cashiro.data.local.entity.TransactionType
import com.ritesh.cashiro.presentation.ui.extras.components.charts.TransactionDataType
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenViewModel
import com.ritesh.cashiro.presentation.ui.features.add_transaction.AddTransactionScreenViewModel
import com.ritesh.cashiro.presentation.ui.features.profile.ProfileScreenViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoryDisplayInfo(
    val id: Int,
    val isSubCategory: Boolean = false,
    val iconId: Int,
    val boxColor: Int, // Changed back to Int to match your entities
    val name: String
)

class HomeScreenViewModel(
    private val accountViewModel: AccountScreenViewModel,
    private val transactionViewModel: AddTransactionScreenViewModel,
    private val profileScreenViewModel: ProfileScreenViewModel
) : ViewModel() {

    // State holder for the HomeScreen
    private val _state = MutableStateFlow(HomeScreenState())
    val state: StateFlow<HomeScreenState> = _state.asStateFlow()

    init {
        // Collect and combine data from various ViewModels to update HomeScreenState
        viewModelScope.launch {
            // Create a flow for transactions from the transaction state
            val transactionsFlow = transactionViewModel.state.map { it.transactions }

            combine(
                accountViewModel.accounts,
                accountViewModel.mainAccountCurrencyCode,
                transactionsFlow,
                profileScreenViewModel.state
            ) { accounts, mainCurrencyCode, transactions, profileState ->
                // Process the data to update state
                val recentTransactions = transactions
                    .sortedByDescending { it.date }
                    .take(10)

                val recentExpenses = transactions
                    .sortedByDescending { it.date }
                    .filter { it.mode == "Expense" }
                    .take(10)

                val recentIncomes = transactions
                    .sortedByDescending { it.date }
                    .filter { it.mode == "Income" }
                    .take(10)

                // NEW: Filter upcoming and overdue transactions
                val currentTime = System.currentTimeMillis()
                val upcomingTransactions = transactions.filter { transaction ->
                    (transaction.transactionType == TransactionType.UPCOMING ||
                            transaction.transactionType == TransactionType.SUBSCRIPTION ||
                            transaction.transactionType == TransactionType.REPETITIVE) &&
                            !transaction.isPaid &&
                            transaction.date >= currentTime
                }.sortedBy { it.date }

                val overdueTransactions = transactions.filter { transaction ->
                    (transaction.transactionType == TransactionType.UPCOMING ||
                            transaction.transactionType == TransactionType.SUBSCRIPTION ||
                            transaction.transactionType == TransactionType.REPETITIVE) &&
                            !transaction.isPaid &&
                            transaction.date < currentTime
                }.sortedBy { it.date }

                _state.update { currentState ->
                    currentState.copy(
                        accounts = accounts,
                        mainCurrencyCode = mainCurrencyCode,
                        transactions = transactions,
                        recentTransactions = recentTransactions,
                        recentExpenses = recentExpenses,
                        recentIncomes = recentIncomes,
                        upcomingTransactions = upcomingTransactions,
                        overdueTransactions = overdueTransactions,
                        isLoading = false,
                        userName = profileState.userName,
                        profileImageUri = profileState.profileImageUri,
                        profileBackgroundColor = profileState.profileBackgroundColor
                    )
                }
            }.collect {}
        }
    }
    // NEW: Get upcoming transaction count
    fun getUpcomingTransactionCount(): Int {
        return state.value.upcomingTransactions.size
    }

    // NEW: Get overdue transaction count
    fun getOverdueTransactionCount(): Int {
        return state.value.overdueTransactions.size
    }

    // Existing methods remain the same...
    fun toggleExpanded() {
        _state.update { it.copy(expanded = !it.expanded) }
    }

    fun updateSelectedDataType(dataType: TransactionDataType) {
        _state.update { it.copy(selectedDataType = dataType, expanded = false) }
    }

    fun updateCollapsingFraction(fraction: Float) {
        if (!fraction.isNaN()) {
            val maxOffset = 180.dp
            val minOffset = 110.dp
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

    fun getSelectedTransactions(): List<TransactionEntity> {
        return when (state.value.selectedDataType) {
            TransactionDataType.ALL -> state.value.recentTransactions
            TransactionDataType.EXPENSES -> state.value.recentExpenses
            TransactionDataType.INCOME -> state.value.recentIncomes
        }
    }

    fun getSelectedTypeName(): String {
        return when (state.value.selectedDataType) {
            TransactionDataType.ALL -> "All Recent"
            TransactionDataType.EXPENSES -> "Expenses"
            TransactionDataType.INCOME -> "Income"
        }
    }
}
// Factory for creating HomeScreenViewModel with dependencies
class HomeScreenViewModelFactory(
    private val accountViewModel: AccountScreenViewModel,
    private val transactionViewModel: AddTransactionScreenViewModel,
    private val profileScreenViewModel: ProfileScreenViewModel
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeScreenViewModel::class.java)) {
            return HomeScreenViewModel(
                accountViewModel,
                transactionViewModel,
                profileScreenViewModel
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}