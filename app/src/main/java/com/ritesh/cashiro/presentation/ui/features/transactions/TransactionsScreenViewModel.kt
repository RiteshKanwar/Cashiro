package com.ritesh.cashiro.presentation.ui.features.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ritesh.cashiro.data.local.entity.AccountEntity
import com.ritesh.cashiro.data.local.entity.TransactionEntity
import com.ritesh.cashiro.domain.repository.AccountRepository
import com.ritesh.cashiro.domain.repository.TransactionRepository
import com.ritesh.cashiro.domain.utils.AppEventBus
import com.ritesh.cashiro.domain.utils.CurrencyEvent
import com.ritesh.cashiro.domain.utils.TransactionEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionScreenViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _state = MutableStateFlow(TransactionScreenState())
    val state: StateFlow<TransactionScreenState> = _state.asStateFlow()

    init {
        refreshData()
        observeEvents()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            AppEventBus.events.collect { event ->
                when (event) {
                    is CurrencyEvent.AccountCurrencyChanged -> {
                        // Refresh transactions when currency changes
                        refreshData()
                    }
                    is CurrencyEvent.MainAccountCurrencyChanged -> {
                        refreshData()
                    }
                    is CurrencyEvent.ConversionRatesUpdated -> {
                        refreshData()
                    }
                    is TransactionEvent.TransactionsUpdated -> {
                        refreshData()
                    }
                }
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val transactions = transactionRepository.getAllTransactions()
                val accounts = accountRepository.getAllAccounts()

                _state.update {
                    it.copy(
                        transactions = transactions,
                        accounts = accounts,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

data class TransactionScreenState(
    val transactions: List<TransactionEntity> = emptyList(),
    val accounts: List<AccountEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)