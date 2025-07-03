package com.ritesh.cashiro.data.currency.model

import com.ritesh.cashiro.data.currency.repository.CurrencyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetAllCurrenciesUseCase @Inject constructor(
    private val repository: CurrencyRepository
) {
    suspend operator fun invoke(): Flow<Result<List<Currency>>> {
        return repository.getAllCurrencies()
            .map { result ->
                result.map { currenciesMap ->
                    currenciesMap.map { (code, name) ->
                        Currency(code, name)
                    }.sortedBy { it.code }
                }
            }
    }
}

class GetCurrencyConversionsUseCase @Inject constructor(
    private val repository: CurrencyRepository
) {
    suspend operator fun invoke(baseCurrency: String): Flow<Result<List<CurrencyConversion>>> {
        return repository.getCurrencyConversions(baseCurrency)
            .map { result ->
                result.map { ratesMap ->
                    ratesMap.map { (code, rate) ->
                        CurrencyConversion(code, rate)
                    }.sortedBy { it.currencyCode }
                }
            }
    }
}