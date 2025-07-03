package com.ritesh.cashiro.presentation.ui.features.transactions

import androidx.compose.ui.text.input.TextFieldValue
import com.ritesh.cashiro.data.local.entity.TransactionType

data class TransactionFilterState(
    val selectedCategories: Set<Int> = emptySet(), // Category IDs
    val selectedSubCategories: Set<Int> = emptySet(), // SubCategory IDs - NEW
    val expandedCategories: Set<Int> = emptySet(), // Categories showing subcategories - NEW
    val selectedAccounts: Set<Int> = emptySet(), // Account IDs
    val minAmount: Double = 0.0,
    val maxAmount: Double = 10000000.0, // 10M default max
    val transactionMode: Set<String> = setOf("Income", "Expense", "Transfer"), // All selected by default
    val transactionTypes: Set<TransactionType> = emptySet(),
    val paymentStatus: Set<String> = emptySet(), // "Paid", "Not paid", "Skipped"
    val searchText: TextFieldValue = TextFieldValue(""),
    val isActive: Boolean = false // Whether any filters are applied
) {
    fun isFilterActive(): Boolean {
        return selectedCategories.isNotEmpty() ||
                selectedSubCategories.isNotEmpty() || // NEW
                selectedAccounts.isNotEmpty() ||
                minAmount > 0.0 ||
                maxAmount < 10000000.0 ||
                transactionMode.size < 3 ||
                transactionTypes.isNotEmpty() ||
                paymentStatus.isNotEmpty() ||
                searchText.text.isNotBlank()
    }

    fun reset(): TransactionFilterState {
        return TransactionFilterState()
    }
}