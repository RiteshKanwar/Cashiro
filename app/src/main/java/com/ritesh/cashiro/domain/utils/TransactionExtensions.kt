package com.ritesh.cashiro.domain.utils

import com.ritesh.cashiro.data.local.entity.TransactionEntity
import com.ritesh.cashiro.data.local.entity.TransactionType

/**
 * Extension function to get the converted amount of a transaction
 * based on the currency of the account it belongs to and the display currency.
 */
fun TransactionEntity.getConvertedAmount(
    fromCurrency: String,
    toCurrency: String,
    conversionRates: Map<String, Double>
): Double {
    return CurrencyUtils.convertAmount(this.amount, fromCurrency, toCurrency, conversionRates)
}

/**
 * Extension function to format the amount with appropriate currency symbol
 */
fun TransactionEntity.getFormattedAmount(
    accountCurrency: String,
    displayCurrency: String,
    conversionRates: Map<String, Double>
): String {
    val convertedAmount = getConvertedAmount(accountCurrency, displayCurrency, conversionRates)
    return CurrencyUtils.formatAmountWithCurrency(convertedAmount, displayCurrency)
}


// Extension functions for better transaction categorization
fun TransactionEntity.isScheduled(): Boolean {
    return this.transactionType in listOf(
        TransactionType.UPCOMING,
        TransactionType.SUBSCRIPTION,
        TransactionType.REPETITIVE
    ) || this.nextDueDate != null
}

fun TransactionEntity.isUpcoming(daysAhead: Int = 30): Boolean {
    val currentTime = System.currentTimeMillis()
    val futureThreshold = currentTime + (daysAhead * 24 * 60 * 60 * 1000L)
    val dueDate = this.nextDueDate ?: this.date

    return !this.isPaid && dueDate > currentTime && dueDate <= futureThreshold
}

fun TransactionEntity.isOverdue(daysBehind: Int = 30): Boolean {
    val currentTime = System.currentTimeMillis()
    val pastThreshold = currentTime - (daysBehind * 24 * 60 * 60 * 1000L)
    val dueDate = this.nextDueDate ?: this.date

    return !this.isPaid && dueDate < currentTime && dueDate >= pastThreshold
}

fun TransactionEntity.getDaysUntilDue(): Int {
    val currentTime = System.currentTimeMillis()
    val dueDate = this.nextDueDate ?: this.date
    val diffInMillis = dueDate - currentTime
    return (diffInMillis / (24 * 60 * 60 * 1000)).toInt()
}

fun TransactionEntity.getDaysOverdue(): Int {
    val currentTime = System.currentTimeMillis()
    val dueDate = this.nextDueDate ?: this.date
    val diffInMillis = currentTime - dueDate
    return (diffInMillis / (24 * 60 * 60 * 1000)).toInt()
}

fun TransactionEntity.getStatusText(): String {
    return when {
        this.isPaid -> "Paid"
        this.isOverdue() -> "Overdue (${this.getDaysOverdue()} days)"
        this.isUpcoming() -> {
            val days = this.getDaysUntilDue()
            when {
                days == 0 -> "Due Today"
                days == 1 -> "Due Tomorrow"
                days > 1 -> "Due in $days days"
                else -> "Upcoming"
            }
        }
        else -> "Scheduled"
    }
}