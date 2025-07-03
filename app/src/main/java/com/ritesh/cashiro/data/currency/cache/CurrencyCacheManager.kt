package com.ritesh.cashiro.data.currency.cache

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ritesh.cashiro.data.currency.model.Currency
import com.ritesh.cashiro.data.currency.model.CurrencyConversion
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Define DataStore at the file level
private val Context.currencyDataStore: DataStore<Preferences> by preferencesDataStore(name = "currency_cache")

@Singleton
class CurrencyCacheManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    private val dataStore = context.currencyDataStore

    // Keys for DataStore
    private object PreferencesKeys {
        val CURRENCIES_KEY = stringPreferencesKey("currencies_cache")
        val CONVERSIONS_KEY = stringPreferencesKey("conversions_cache")
        val LAST_UPDATED_KEY = stringPreferencesKey("last_updated")
    }

    // Cache timeout (24 hours in milliseconds)
    private val CACHE_TIMEOUT = 24 * 60 * 60 * 1000L

    // Get all cached currencies
    fun getCachedCurrencies(): Flow<List<Currency>> = dataStore.data.map { preferences ->
        val jsonString = preferences[PreferencesKeys.CURRENCIES_KEY] ?: "[]"
        val type = object : TypeToken<List<Currency>>() {}.type
        gson.fromJson(jsonString, type)
    }

    // Get cached conversions for a specific currency
    fun getCachedConversions(currencyCode: String): Flow<List<CurrencyConversion>> = dataStore.data.map { preferences ->
        val key = stringPreferencesKey("${PreferencesKeys.CONVERSIONS_KEY.name}_$currencyCode")
        val jsonString = preferences[key] ?: "[]"
        val type = object : TypeToken<List<CurrencyConversion>>() {}.type
        gson.fromJson(jsonString, type)
    }

    // Check if cache is fresh
    fun isCacheFresh(): Flow<Boolean> = dataStore.data.map { preferences ->
        val lastUpdatedString = preferences[PreferencesKeys.LAST_UPDATED_KEY]
        if (lastUpdatedString == null) {
            false
        } else {
            try {
                val lastUpdated = lastUpdatedString.toLong()
                val currentTime = System.currentTimeMillis()
                (currentTime - lastUpdated) < CACHE_TIMEOUT
            } catch (e: Exception) {
                false
            }
        }
    }

    // Save currencies to cache
    suspend fun cacheCurrencies(currencies: List<Currency>) {
        dataStore.edit { preferences ->
            val jsonString = gson.toJson(currencies)
            preferences[PreferencesKeys.CURRENCIES_KEY] = jsonString
            preferences[PreferencesKeys.LAST_UPDATED_KEY] = System.currentTimeMillis().toString()
        }
    }

    // Save conversions to cache for a specific currency
    suspend fun cacheConversions(currencyCode: String, conversions: List<CurrencyConversion>) {
        dataStore.edit { preferences ->
            val key = stringPreferencesKey("${PreferencesKeys.CONVERSIONS_KEY.name}_$currencyCode")
            val jsonString = gson.toJson(conversions)
            preferences[key] = jsonString
        }
    }

    // Clear all cached data
    suspend fun clearCache() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}