package com.ritesh.cashiro.presentation.ui.features.add_transaction

import com.ritesh.cashiro.data.local.entity.AccountEntity
import com.ritesh.cashiro.data.local.entity.CategoryEntity
import com.ritesh.cashiro.data.local.entity.RecurrenceFrequency
import com.ritesh.cashiro.data.local.entity.TransactionEntity
import com.ritesh.cashiro.data.local.entity.TransactionType
import com.ritesh.cashiro.domain.utils.getCurrentDateInMillis

/**
 * Tab configuration for transaction types
 */
data class TabItems(
    val type: String,
)

/**
 * UI State for the Add Transaction Screen.
 * Encapsulates all the state needed to render the transaction form.
 */
data class AddTransactionScreenState(
    // Basic Transaction Fields
    val transactionTitle: String = "",
    val transactionAmount: String = "",
    val transactionDate: Long = getCurrentDateInMillis(),
    val transactionTime: Long = System.currentTimeMillis(),
    val transactionCategoryId: Int = 0,
    val transactionSubCategoryId: Int = 0,
    val transactionAccountId: Int = 0,
    val transactionDestinationAccountId: Int? = null,
    val transactionNote: String = "",
    val transactionMode: String = "Expense",
    val transactionType: TransactionType = TransactionType.DEFAULT,

    // Recurrence Fields
    val recurrenceFrequency: RecurrenceFrequency = RecurrenceFrequency.NONE,
    val recurrenceInterval: Int = 1,
    val recurrenceEndDate: Long? = null,
    val isEndDateSelected: Boolean = false,
    val isPaid: Boolean = false,
    val nextDueDate: Long? = null,

    val conversionRates: Map<String, Double> = emptyMap(),
    val baseCurrencyCode: String = "usd",
    val transactionCurrencyCode: String = "usd",
    // UI State
    val isTransactionTypeMenuOpened: Boolean = false,
    val isTransactionTypeInfoSheetOpen: Boolean = false,
    val isTransactionAmountSheetOpen: Boolean = false,
    val isRecurrenceMenuOpen: Boolean = false,
    val isRecurrenceBottomSheetOpen: Boolean = false,
    val isCustomInputBottomSheetOpen: Boolean = false,
    val openEndDatePicker: Boolean = false,
    val isTransactionDetailsSheetOpen: Boolean = false,
    val isCategoryMenuOpened: Boolean = false,
    val isAccountMenuOpened: Boolean = false,
    val isAmountMenuOpened: Boolean = false,
    val openDatePickerDialog: Boolean = false,

    // Transaction Validation States
    val isAddFormValid: Boolean = false,
    val isUpdateFormValid: Boolean = false,

    // Data Lists
    val categories: List<CategoryEntity> = emptyList(),
    val accounts: List<AccountEntity> = emptyList(),
    val transactions: List<TransactionEntity> = emptyList(),

    // Tab Selection States
    val selectedTabIndex: Int = 0,
    val tabItems: List<TabItems> = emptyList(),

    // Update Transaction State
    val isUpdateTransaction: Boolean = false,
    val transactionId: Int = 0,
    val currentTransaction: TransactionEntity? = null,

    // Loading States
    val isLoading: Boolean = false,

    // Calculator State
    val calculatedResultAmount: String = "0",

    // Error States
    val validationError: String? = null,
) {
    /**
     * Determines if the transaction form is valid for adding
     */
    fun isValidForAdding(): Boolean {
        val basicValidation = transactionAmount.isNotEmpty() &&
                transactionDate != 0L &&
                ((transactionMode == "Transfer" &&
                        transactionDestinationAccountId != null &&
                        transactionDestinationAccountId != transactionAccountId) ||
                        (transactionMode != "Transfer" && transactionCategoryId != 0))

        // Additional validation for recurring transaction types
        val recurringValidation = if (transactionType == TransactionType.SUBSCRIPTION ||
            transactionType == TransactionType.REPETITIVE) {
            // For subscription/repetitive transactions, recurrence data is required
            recurrenceFrequency != RecurrenceFrequency.NONE &&
                    recurrenceInterval > 0 &&
                    recurrenceEndDate != null
        } else {
            true // No additional validation needed for other types
        }

        return basicValidation && recurringValidation
    }

    /**
     * Determines if the transaction form is valid for updating
     */
    fun isValidForUpdating(): Boolean {
        return currentTransaction != null && (
                transactionTitle != currentTransaction.title ||
                        transactionAmount != currentTransaction.amount.toString() ||
                        transactionAccountId != currentTransaction.accountId ||
                        transactionDate != currentTransaction.date ||
                        transactionTime != currentTransaction.time ||
                        transactionMode != currentTransaction.mode ||
                        transactionType != currentTransaction.transactionType ||
                        transactionNote != currentTransaction.note ||
                        // FIX: Add recurrence field comparisons
                        recurrenceFrequency != (currentTransaction.recurrence?.frequency ?: RecurrenceFrequency.NONE) ||
                        recurrenceInterval != (currentTransaction.recurrence?.interval ?: 1) ||
                        recurrenceEndDate != currentTransaction.recurrence?.endRecurrenceDate ||
                        isPaid != currentTransaction.isPaid ||
                        ((transactionMode == "Transfer" &&
                                transactionDestinationAccountId != currentTransaction.destinationAccountId &&
                                transactionDestinationAccountId != currentTransaction.accountId) ||
                                (transactionMode != "Transfer" && transactionCategoryId != currentTransaction.categoryId)) ||
                        (transactionSubCategoryId != (currentTransaction.subCategoryId ?: 0))
                )
    }

    /**
     * Gets the available tab items based on update mode and transaction type
     */
    fun getAvailableTabItems(): List<TabItems> {
        return when {
            isUpdateTransaction && transactionMode == "Transfer" -> listOf(TabItems("Transfer"))
            isUpdateTransaction && (transactionMode == "Expense" || transactionMode == "Income") ->
                listOf(TabItems("Expense"), TabItems("Income"))
            !isUpdateTransaction -> listOf(TabItems("Expense"), TabItems("Income"), TabItems("Transfer"))
            else -> emptyList()
        }
    }

    /**
     * Gets the initial tab index based on transaction mode
     */
    fun getInitialTabIndex(): Int {
        return if (isUpdateTransaction && currentTransaction != null) {
            when (currentTransaction.mode) {
                "Transfer" -> 0  // Only tab in Transfer update mode
                "Income" -> 1    // Income tab in Expense/Income update mode
                else -> 0        // Expense tab in Expense/Income update mode
            }
        } else {
            0  // Default to first tab for new transactions
        }
    }

    /**
     * Gets the currency code for the selected account
     */
    fun getAccountCurrencyCode(): String? {
        return accounts.find { it.id == transactionAccountId }?.currencyCode
    }
}
