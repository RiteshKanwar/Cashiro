package com.ritesh.cashiro.data.currency.model

data class Currency(
    val code: String,
    val name: String
) {
    val symbol: String get() = CurrencySymbols.getSymbol(code)
    val displayName: String get() = "$name ($code)"
}
data class CurrencyConversion(
    val currencyCode: String,
    val rate: Double
) {
    val symbol: String get() = CurrencySymbols.getSymbol(currencyCode)
    val displayRate: String get() = String.format("%.6f", rate)
}