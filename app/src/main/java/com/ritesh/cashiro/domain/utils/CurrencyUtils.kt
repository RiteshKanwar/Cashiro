package com.ritesh.cashiro.domain.utils

import com.ritesh.cashiro.data.local.entity.AccountEntity
import com.ritesh.cashiro.data.local.entity.TransactionEntity
import com.ritesh.cashiro.data.local.entity.TransactionType
import com.ritesh.cashiro.data.currency.model.CurrencySymbols
import com.ritesh.cashiro.data.local.entity.RecurrenceFrequency
import kotlin.math.abs

object CurrencyUtils {
    /**
     * Formats a monetary amount with the appropriate currency symbol
     */
    fun formatAmountWithCurrency(amount: Double, currencyCode: String): String {
        val symbol = CurrencySymbols.getSymbol(currencyCode)
        return "$symbol${String.format("%.2f", amount)}"
    }

    /**
     * Converts an amount from one currency to another
     */
    fun convertAmount(
        amount: Double,
        fromCurrency: String,
        toCurrency: String,
        conversionRates: Map<String, Double>
    ): Double {
        if (fromCurrency.equals(toCurrency, ignoreCase = true)) {
            return amount
        }

        val fromCurrencyLower = fromCurrency.lowercase()
        val fromRate = conversionRates[fromCurrencyLower] ?: 1.0
        if (fromRate > 0) {
            return amount / fromRate
        }

        return amount
    }

    /**
     * Calculates total income from a list of transactions, converting all to the main currency
     */
    fun calculateTotalIncome(
        transactions: List<TransactionEntity>,
        mainCurrency: String,
        conversionRates: Map<String, Double>
    ): Double {
        return transactions
            .filter { it.mode == "Income" && (it.isPaid  || it.isSettled || it.isCollected || it.transactionType == TransactionType.DEFAULT)}
            .sumOf { transaction ->
                val fromCurrency = transaction.originalCurrencyCode ?: mainCurrency
                convertAmount(transaction.amount, fromCurrency, mainCurrency, conversionRates)
            }
    }

    /**
     * Calculates total expense from a list of transactions, converting all to the main currency
     */
    fun calculateTotalExpense(
        transactions: List<TransactionEntity>,
        mainCurrency: String,
        conversionRates: Map<String, Double>
    ): Double {
        return transactions
            .filter { it.mode == "Expense" && (it.isPaid  || it.isSettled || it.isCollected || it.transactionType == TransactionType.DEFAULT)}
            .sumOf { transaction ->
                val fromCurrency = transaction.originalCurrencyCode ?: mainCurrency
                abs(convertAmount(transaction.amount, fromCurrency, mainCurrency, conversionRates))
            }
    }

    /**
     * Calculates net worth from a list of accounts, converting all to the main currency
     */
    fun calculateNetWorth(
        accounts: List<AccountEntity>,
        mainCurrency: String,
        conversionRates: Map<String, Double>
    ): Double {
        return accounts.sumOf { account ->
            val accountCurrency = account.currencyCode
            convertAmount(account.balance, accountCurrency, mainCurrency, conversionRates)
        }
    }

    /**
     * Calculates the amount to add to a destination account during a transfer
     * taking currency conversion into account
     */
    fun calculateTransferDestinationAmount(
        amount: Double,
        sourceAccountCurrency: String,
        destAccountCurrency: String,
        conversionRates: Map<String, Double>
    ): Double {
        return convertAmount(amount, sourceAccountCurrency, destAccountCurrency, conversionRates)
    }

    // FIXED: Monthly total calculation for filter buttons
    fun calculateMonthlyTotal(
        transactions: List<TransactionEntity>,
        mainCurrencyCode: String,
        conversionRates: Map<String, Double>
    ): Double {
        return transactions
            .filter { transaction ->
                // Only include active (unpaid) recurring transactions
                val isActive = when (transaction.transactionType) {
                    TransactionType.SUBSCRIPTION, TransactionType.REPETITIVE -> !transaction.isPaid
                    TransactionType.UPCOMING -> !transaction.isPaid
                    else -> false
                }

                isActive && transaction.recurrence?.frequency == RecurrenceFrequency.MONTHLY
            }
            .sumOf { transaction ->
                val currencyCode = transaction.originalCurrencyCode ?: mainCurrencyCode
                val convertedAmount = if (currencyCode == mainCurrencyCode) {
                    transaction.amount
                } else {
                    transaction.getConvertedAmount(currencyCode, mainCurrencyCode, conversionRates)
                }

                // For monthly filter: show monthly amount
                convertedAmount
            }
    }

    // FIXED: Yearly total calculation for filter buttons
    fun calculateYearlyTotal(
        transactions: List<TransactionEntity>,
        mainCurrencyCode: String,
        conversionRates: Map<String, Double>
    ): Double {
        return transactions
            .filter { transaction ->
                // Only include active (unpaid) recurring transactions
                val isActive = when (transaction.transactionType) {
                    TransactionType.SUBSCRIPTION, TransactionType.REPETITIVE -> !transaction.isPaid
                    TransactionType.UPCOMING -> !transaction.isPaid
                    else -> false
                }

                isActive
            }
            .sumOf { transaction ->
                val currencyCode = transaction.originalCurrencyCode ?: mainCurrencyCode
                val convertedAmount = if (currencyCode == mainCurrencyCode) {
                    transaction.amount
                } else {
                    transaction.getConvertedAmount(currencyCode, mainCurrencyCode, conversionRates)
                }

                // Convert to yearly amount based on frequency
                when (transaction.recurrence?.frequency) {
                    RecurrenceFrequency.DAILY -> convertedAmount * 365 / (transaction.recurrence?.interval ?: 1)
                    RecurrenceFrequency.WEEKLY -> convertedAmount * 52 / (transaction.recurrence?.interval ?: 1)
                    RecurrenceFrequency.MONTHLY -> convertedAmount * 12 / (transaction.recurrence?.interval ?: 1)
                    RecurrenceFrequency.YEARLY -> convertedAmount / (transaction.recurrence?.interval ?: 1)
                    else -> convertedAmount // For one-time transactions
                }
            }
    }

    // FIXED: Total calculation for filter buttons - should show current period amount
    fun calculateTotalForCurrentPeriod(
        transactions: List<TransactionEntity>,
        mainCurrencyCode: String,
        conversionRates: Map<String, Double>
    ): Double {
        return transactions
            .filter { transaction ->
                // Only include active (unpaid) recurring transactions
                val isActive = when (transaction.transactionType) {
                    TransactionType.SUBSCRIPTION, TransactionType.REPETITIVE -> !transaction.isPaid
                    TransactionType.UPCOMING -> !transaction.isPaid
                    else -> false
                }

                isActive
            }
            .sumOf { transaction ->
                val currencyCode = transaction.originalCurrencyCode ?: mainCurrencyCode
                val convertedAmount = if (currencyCode == mainCurrencyCode) {
                    transaction.amount
                } else {
                    transaction.getConvertedAmount(currencyCode, mainCurrencyCode, conversionRates)
                }

                // For total filter: show the base recurring amount (not annualized)
                convertedAmount
            }
    }

    // NEW: Calculate amounts with proper handling of paid transactions
    fun calculateScheduleAmounts(
        transactions: List<TransactionEntity>,
        mainCurrencyCode: String,
        conversionRates: Map<String, Double>
    ): ScheduleAmounts {
        val activeTransactions = transactions.filter { transaction ->
            when (transaction.transactionType) {
                TransactionType.SUBSCRIPTION, TransactionType.REPETITIVE -> !transaction.isPaid
                TransactionType.UPCOMING -> !transaction.isPaid
                TransactionType.LENT -> !transaction.isCollected
                TransactionType.BORROWED -> !transaction.isSettled
                else -> true
            }
        }

        val paidTransactions = transactions.filter { transaction ->
            when (transaction.transactionType) {
                TransactionType.SUBSCRIPTION, TransactionType.REPETITIVE -> transaction.isPaid
                TransactionType.UPCOMING -> transaction.isPaid
                TransactionType.LENT -> transaction.isCollected
                TransactionType.BORROWED -> transaction.isSettled
                else -> false
            }
        }

        return ScheduleAmounts(
            monthlyTotal = calculateMonthlyTotal(activeTransactions, mainCurrencyCode, conversionRates),
            yearlyTotal = calculateYearlyTotal(activeTransactions, mainCurrencyCode, conversionRates),
            currentTotal = calculateTotalForCurrentPeriod(activeTransactions, mainCurrencyCode, conversionRates),
            monthlyPaid = calculateMonthlyTotal(paidTransactions, mainCurrencyCode, conversionRates),
            yearlyPaid = calculateYearlyTotal(paidTransactions, mainCurrencyCode, conversionRates),
            currentPaid = calculateTotalForCurrentPeriod(paidTransactions, mainCurrencyCode, conversionRates)
        )
    }
}

data class ScheduleAmounts(
    val monthlyTotal: Double,
    val yearlyTotal: Double,
    val currentTotal: Double,
    val monthlyPaid: Double,
    val yearlyPaid: Double,
    val currentPaid: Double
)

//object CurrencyUtils {
//    /**
//     * Formats a monetary amount with the appropriate currency symbol
//     */
//    fun formatAmountWithCurrency(amount: Double, currencyCode: String): String {
//        val symbol = CurrencySymbols.getSymbol(currencyCode)
//        return "$symbol${String.format("%.2f", amount)}"
//    }
//
//    /**
//     * Converts an amount from one currency to another
//     */
//    fun convertAmount(
//        amount: Double,
//        fromCurrency: String,
//        toCurrency: String,
//        conversionRates: Map<String, Double>
//    ): Double {
//        // If currencies are the same, no conversion needed
//        if (fromCurrency.equals(toCurrency, ignoreCase = true)) {
//            return amount
//        }
//
//        // Get the conversion rate from the map
//        val fromCurrencyLower = fromCurrency.lowercase()
//        val toCurrencyLower = toCurrency.lowercase()
//
//        // If we're converting to the base currency (usually the mainCurrency)
//        // Just divide by the rate of the source currency
//        val fromRate = conversionRates[fromCurrencyLower] ?: 1.0
//        if (fromRate > 0) {
//            return amount / fromRate
//        }
//
//        return amount // Fallback if conversion not possible
//    }
//
//    /**
//     * Calculates total income from a list of transactions, converting all to the main currency
//     */
//    fun calculateTotalIncome(
//        transactions: List<TransactionEntity>,
//        mainCurrency: String,
//        conversionRates: Map<String, Double>
//    ): Double {
//        return transactions
//            .filter { it.mode == "Income" && (it.isPaid  || it.isSettled || it.isCollected || it.transactionType == TransactionType.DEFAULT)}
//            .sumOf { transaction ->
//                val fromCurrency = transaction.originalCurrencyCode ?: mainCurrency
//                convertAmount(transaction.amount, fromCurrency, mainCurrency, conversionRates)
//            }
//    }
//
//    /**
//     * Calculates total expense from a list of transactions, converting all to the main currency
//     */
//    fun calculateTotalExpense(
//        transactions: List<TransactionEntity>,
//        mainCurrency: String,
//        conversionRates: Map<String, Double>
//    ): Double {
//        return transactions
//            .filter { it.mode == "Expense" && (it.isPaid  || it.isSettled || it.isCollected || it.transactionType == TransactionType.DEFAULT)}
//            .sumOf { transaction ->
//                val fromCurrency = transaction.originalCurrencyCode ?: mainCurrency
//                abs(convertAmount(transaction.amount, fromCurrency, mainCurrency, conversionRates))
//            }
//    }
//
//    /**
//     * Calculates net worth from a list of accounts, converting all to the main currency
//     */
//    fun calculateNetWorth(
//        accounts: List<AccountEntity>,
//        mainCurrency: String,
//        conversionRates: Map<String, Double>
//    ): Double {
//        return accounts.sumOf { account ->
//            val accountCurrency = account.currencyCode
//            convertAmount(account.balance, accountCurrency, mainCurrency, conversionRates)
//        }
//    }
//
//    /**
//     * Calculates the amount to add to a destination account during a transfer
//     * taking currency conversion into account
//     */
//    fun calculateTransferDestinationAmount(
//        amount: Double,
//        sourceAccountCurrency: String,
//        destAccountCurrency: String,
//        conversionRates: Map<String, Double>
//    ): Double {
//        return convertAmount(amount, sourceAccountCurrency, destAccountCurrency, conversionRates)
//    }
//
//    fun calculateMonthlyTotal(
//        transactions: List<TransactionEntity>,
//        mainCurrencyCode: String,
//        conversionRates: Map<String, Double>
//    ): Double {
//        return transactions
//            .filter { it.recurrence?.frequency == RecurrenceFrequency.MONTHLY }
//            .sumOf { transaction ->
//                val currencyCode = transaction.originalCurrencyCode ?: mainCurrencyCode
//                if (currencyCode == mainCurrencyCode) {
//                    transaction.amount
//                } else {
//                    transaction.getConvertedAmount(currencyCode, mainCurrencyCode, conversionRates)
//                }
//            }
//    }
//
//    fun calculateYearlyTotal(
//        transactions: List<TransactionEntity>,
//        mainCurrencyCode: String,
//        conversionRates: Map<String, Double>
//    ): Double {
//        return transactions
//            .filter { it.recurrence?.frequency == RecurrenceFrequency.YEARLY }
//            .sumOf { transaction ->
//                val currencyCode = transaction.originalCurrencyCode ?: mainCurrencyCode
//                if (currencyCode == mainCurrencyCode) {
//                    transaction.amount
//                } else {
//                    transaction.getConvertedAmount(currencyCode, mainCurrencyCode, conversionRates)
//                }
//            }
//    }

//    fun calculateTotalExpense(
//        transactions: List<TransactionEntity>,
//        mainCurrencyCode: String,
//        conversionRates: Map<String, Double>
//    ): Double {
//        return transactions.sumOf { transaction ->
//            val currencyCode = transaction.originalCurrencyCode ?: mainCurrencyCode
//            if (currencyCode == mainCurrencyCode) {
//                transaction.amount
//            } else {
//                transaction.getConvertedAmount(currencyCode, mainCurrencyCode, conversionRates)
//            }
//        }
//    }
//
//    fun calculateTotalIncome(
//        transactions: List<TransactionEntity>,
//        mainCurrencyCode: String,
//        conversionRates: Map<String, Double>
//    ): Double {
//        return transactions
//            .filter { it.mode == "Income" }
//            .sumOf { transaction ->
//                val currencyCode = transaction.originalCurrencyCode ?: mainCurrencyCode
//                if (currencyCode == mainCurrencyCode) {
//                    transaction.amount
//                } else {
//                    transaction.getConvertedAmount(currencyCode, mainCurrencyCode, conversionRates)
//                }
//            }
//    }
//
//    fun calculateTransferDestinationAmount(
//        sourceAmount: Double,
//        sourceCurrency: String,
//        destinationCurrency: String,
//        conversionRates: Map<String, Double>
//    ): Double {
//        if (sourceCurrency == destinationCurrency) {
//            return sourceAmount
//        }
//
//        // Get conversion rate from source to destination currency
//        val rate = conversionRates[destinationCurrency] ?: 1.0
//        val sourceRate = conversionRates[sourceCurrency] ?: 1.0
//
//        // Convert: source amount / source rate * destination rate
//        return (sourceAmount / sourceRate) * rate
//    }
//}
