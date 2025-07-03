package com.ritesh.cashiro.data.currency.repository

import kotlinx.coroutines.flow.Flow

interface CurrencyRepository {
    suspend fun getAllCurrencies(): Flow<Result<Map<String, String>>>
    suspend fun getCurrencyConversions(currencyCode: String): Flow<Result<Map<String, Double>>>
    suspend fun getHistoricalCurrencyConversions(date: String, currencyCode: String): Flow<Result<Map<String, Double>>>
}