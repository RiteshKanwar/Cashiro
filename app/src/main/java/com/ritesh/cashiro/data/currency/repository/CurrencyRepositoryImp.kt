package com.ritesh.cashiro.data.currency.repository

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.datastore.core.IOException
import com.ritesh.cashiro.data.currency.cache.CurrencyCacheManager
import com.ritesh.cashiro.data.currency.model.Currency
import com.ritesh.cashiro.data.currency.model.CurrencyConversion
import com.ritesh.cashiro.data.currency.api.CurrencyApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Named

class CurrencyRepositoryImpl @Inject constructor(
    @Named("primary") private val primaryApi: CurrencyApi,
    @Named("fallback") private val fallbackApi: CurrencyApi,
    private val cacheManager: CurrencyCacheManager,
    private val connectivityManager: ConnectivityManager  // Add connectivity manager
) : CurrencyRepository {

    // Check if the device is connected to the internet
    private fun isConnected(): Boolean {
        val activeNetwork = connectivityManager.activeNetwork
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        return networkCapabilities != null && (
                networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
    }

    override suspend fun getAllCurrencies(): Flow<Result<Map<String, String>>> = flow {
        // First check if cache is fresh
        val isCacheFresh = cacheManager.isCacheFresh().first()

        // Always emit cached data first (if exists)
        val cachedCurrencies = cacheManager.getCachedCurrencies().first()
        if (cachedCurrencies.isNotEmpty()) {
            val currencyMap = cachedCurrencies.associate { it.code to it.name }
            emit(Result.success(currencyMap))
        }

        // If cache is fresh, or device is offline and we have cached data, don't proceed to network
        if ((isCacheFresh || !isConnected()) && cachedCurrencies.isNotEmpty()) {
            return@flow
        }

        // Try to fetch from network (only if connected)
        if (isConnected()) {
            try {
                // Try primary source
                val response = primaryApi.getAllCurrencies()
                if (response.isSuccessful && response.body() != null) {
                    val currencies = response.body()!!
                    // Cache the result as Currency objects
                    val currencyObjects = currencies.map { (code, name) ->
                        Currency(code, name)
                    }
                    cacheManager.cacheCurrencies(currencyObjects)
                    emit(Result.success(currencies))
                } else {
                    // Try fallback
                    val fallbackResponse = fallbackApi.getAllCurrencies()
                    if (fallbackResponse.isSuccessful && fallbackResponse.body() != null) {
                        val currencies = fallbackResponse.body()!!
                        // Cache the result
                        val currencyObjects = currencies.map { (code, name) ->
                            Currency(code, name)
                        }
                        cacheManager.cacheCurrencies(currencyObjects)
                        emit(Result.success(currencies))
                    } else if (cachedCurrencies.isEmpty()) {
                        emit(Result.failure(HttpException(response)))
                    }
                }
            } catch (e: Exception) {
                try {
                    // Try fallback
                    val fallbackResponse = fallbackApi.getAllCurrencies()
                    if (fallbackResponse.isSuccessful && fallbackResponse.body() != null) {
                        val currencies = fallbackResponse.body()!!
                        // Cache the result
                        val currencyObjects = currencies.map { (code, name) ->
                            Currency(code, name)
                        }
                        cacheManager.cacheCurrencies(currencyObjects)
                        emit(Result.success(currencies))
                    } else if (cachedCurrencies.isEmpty()) {
                        emit(Result.failure(e))
                    }
                } catch (fallbackException: Exception) {
                    if (cachedCurrencies.isEmpty()) {
                        emit(Result.failure(fallbackException))
                    }
                }
            }
        } else if (cachedCurrencies.isEmpty()) {
            // Only emit failure if we have no cached data and we're offline
            emit(Result.failure(IOException("No internet connection and no cached data available")))
        }
    }

    override suspend fun getCurrencyConversions(currencyCode: String): Flow<Result<Map<String, Double>>> = flow {
        // Check if we have cached USD conversions when offline
        val isDeviceOffline = !isConnected()

        // Always emit cached conversions first (if exists)
        val cachedConversions = cacheManager.getCachedConversions(currencyCode).first()
        if (cachedConversions.isNotEmpty()) {
            val conversionMap = cachedConversions.associate { it.currencyCode to it.rate }
            emit(Result.success(conversionMap))
        } else if (isDeviceOffline && currencyCode.lowercase() != "usd") {
            // If offline and requesting non-USD currency with no cache, try to calculate from USD
            val usdCachedConversions = cacheManager.getCachedConversions("usd").first()
            if (usdCachedConversions.isNotEmpty()) {
                // If we have USD cached conversions, calculate the cross rates
                val targetCurrencyInUsd = usdCachedConversions.find { it.currencyCode.lowercase() == currencyCode.lowercase() }
                if (targetCurrencyInUsd != null) {
                    val targetRate = targetCurrencyInUsd.rate
                    // Calculate inverse rate (1/rate) to convert from USD to the target currency
                    val calculatedConversions = usdCachedConversions.map { usdConversion ->
                        // Calculate cross rate: USD→Target = USD→Currency / USD→Target
                        val crossRate = (usdConversion.rate / targetRate)
                        CurrencyConversion(usdConversion.currencyCode, crossRate)
                    }
                    // Add the base currency with rate 1.0
                    val withBaseCurrency = calculatedConversions + CurrencyConversion(currencyCode, 1.0)
                    val conversionMap = withBaseCurrency.associate { it.currencyCode to it.rate }

                    // Cache these calculated conversions for future offline use
                    cacheManager.cacheConversions(currencyCode, withBaseCurrency)

                    emit(Result.success(conversionMap))
                    return@flow
                }
            }
        }

        // Check if cache is fresh
        val isCacheFresh = cacheManager.isCacheFresh().first()

        // If cache is fresh and we have data, or if we're offline and have cached data, don't proceed to network
        if ((isCacheFresh || isDeviceOffline) && cachedConversions.isNotEmpty()) {
            return@flow
        }

        // Only try network if we're online
        if (isConnected()) {
            try {
                // Try primary source
                val response = primaryApi.getCurrencyConversions(currencyCode)
                if (response.isSuccessful && response.body() != null) {
                    val rates = extractRates(response.body()!!, currencyCode)
                    // Cache the result
                    val conversionObjects = rates.map { (code, rate) ->
                        CurrencyConversion(code, rate)
                    }
                    cacheManager.cacheConversions(currencyCode, conversionObjects)
                    emit(Result.success(rates))
                } else {
                    // Try fallback
                    val fallbackResponse = fallbackApi.getCurrencyConversions(currencyCode)
                    if (fallbackResponse.isSuccessful && fallbackResponse.body() != null) {
                        val rates = extractRates(fallbackResponse.body()!!, currencyCode)
                        // Cache the result
                        val conversionObjects = rates.map { (code, rate) ->
                            CurrencyConversion(code, rate)
                        }
                        cacheManager.cacheConversions(currencyCode, conversionObjects)
                        emit(Result.success(rates))
                    } else if (cachedConversions.isEmpty()) {
                        emit(Result.failure(HttpException(response)))
                    }
                }
            } catch (e: Exception) {
                try {
                    // Try fallback
                    val fallbackResponse = fallbackApi.getCurrencyConversions(currencyCode)
                    if (fallbackResponse.isSuccessful && fallbackResponse.body() != null) {
                        val rates = extractRates(fallbackResponse.body()!!, currencyCode)
                        // Cache the result
                        val conversionObjects = rates.map { (code, rate) ->
                            CurrencyConversion(code, rate)
                        }
                        cacheManager.cacheConversions(currencyCode, conversionObjects)
                        emit(Result.success(rates))
                    } else if (cachedConversions.isEmpty()) {
                        emit(Result.failure(e))
                    }
                } catch (fallbackException: Exception) {
                    if (cachedConversions.isEmpty()) {
                        emit(Result.failure(fallbackException))
                    }
                }
            }
        } else if (cachedConversions.isEmpty()) {
            // Only emit failure if we have no cached data and no way to calculate from USD
            emit(Result.failure(IOException("No internet connection and no cached data available for $currencyCode")))
        }
    }

    override suspend fun getHistoricalCurrencyConversions(
        date: String,
        currencyCode: String
    ): Flow<Result<Map<String, Double>>> {
        // This would require a different API instance with a dynamic base URL
        // For now, this is a placeholder that could be implemented later
        return flow { emit(Result.failure(NotImplementedError("Historical data not implemented yet"))) }
    }

    @Suppress("UNCHECKED_CAST")
    private fun extractRates(response: Map<String, Any>, currencyCode: String): Map<String, Double> {
        val rates = response[currencyCode] as? Map<String, Double>
        return rates ?: emptyMap()
    }
}