package com.ritesh.cashiro.presentation.ui.features.currency

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ritesh.cashiro.data.currency.model.Currency
import com.ritesh.cashiro.data.currency.model.CurrencyConversion
import com.ritesh.cashiro.data.currency.model.GetAllCurrenciesUseCase
import com.ritesh.cashiro.data.currency.model.GetCurrencyConversionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CurrencyViewModel @Inject constructor(
    private val getAllCurrenciesUseCase: GetAllCurrenciesUseCase,
    private val getCurrencyConversionsUseCase: GetCurrencyConversionsUseCase,
    private val connectivityManager: ConnectivityManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CurrencyUiState())
    val uiState: StateFlow<CurrencyUiState> = _uiState.asStateFlow()

    // Track connectivity state
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    init {
        viewModelScope.launch {
            checkConnectivity()
            loadCurrencies()
        }

        // Monitor network state changes
        viewModelScope.launch {
            monitorNetworkConnectivity()
        }
    }

    private fun checkConnectivity() {
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        _isConnected.value = networkCapabilities != null &&
                (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
    }

    private fun monitorNetworkConnectivity() {
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _isConnected.value = true
                // Refresh data when connection becomes available
                loadCurrencies()
            }

            override fun onLost(network: Network) {
                _isConnected.value = false
            }
        }

        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    fun loadCurrencies() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            getAllCurrenciesUseCase().collect { result ->
                result.fold(
                    onSuccess = { currencies ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                currencies = currencies,
                                selectedCurrency = currencies.firstOrNull { currency -> currency.code == "usd" }
                                    ?: currencies.firstOrNull(),
                                isOfflineMode = !isConnected.value
                            )
                        }

                        // Load conversions for the selected currency
                        _uiState.value.selectedCurrency?.let { selected ->
                            loadConversions(selected.code)
                        }
                    },
                    onFailure = { throwable ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = throwable.message ?: "Failed to load currencies",
                                isOfflineMode = !isConnected.value
                            )
                        }
                    }
                )
            }
        }
    }

    fun loadConversions(currencyCode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingConversions = true, conversionError = null) }

            getCurrencyConversionsUseCase(currencyCode).collect { result ->
                result.fold(
                    onSuccess = { conversions ->
                        _uiState.update {
                            it.copy(
                                isLoadingConversions = false,
                                conversions = conversions,
                                isOfflineMode = !isConnected.value
                            )
                        }
                    },
                    onFailure = { throwable ->
                        _uiState.update {
                            it.copy(
                                isLoadingConversions = false,
                                conversionError = throwable.message ?: "Failed to load conversions",
                                isOfflineMode = !isConnected.value
                            )
                        }
                    }
                )
            }
        }
    }

    fun selectCurrency(currencyCode: String) {
        val currency = _uiState.value.currencies.find { it.code == currencyCode }
        if (currency != null) {
            _uiState.update { it.copy(selectedCurrency = currency) }
            loadConversions(currency.code)
        }
    }

    data class CurrencyUiState(
        val isLoading: Boolean = false,
        val currencies: List<Currency> = emptyList(),
        val selectedCurrency: Currency? = null,
        val error: String? = null,
        val isLoadingConversions: Boolean = false,
        val conversions: List<CurrencyConversion> = emptyList(),
        val conversionError: String? = null,
        val isOfflineMode: Boolean = false
    )
}