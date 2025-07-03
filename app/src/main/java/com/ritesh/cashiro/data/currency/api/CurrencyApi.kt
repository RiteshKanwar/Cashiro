package com.ritesh.cashiro.data.currency.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface CurrencyApi {
    @GET("v1/currencies.json")
    suspend fun getAllCurrencies(): Response<Map<String, String>>

    @GET("v1/currencies/{currencyCode}.json")
    suspend fun getCurrencyConversions(
        @Path("currencyCode") currencyCode: String
    ): Response<Map<String, Any>>
}