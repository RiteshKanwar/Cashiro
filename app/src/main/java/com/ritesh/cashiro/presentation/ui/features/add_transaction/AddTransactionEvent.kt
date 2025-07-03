package com.ritesh.cashiro.presentation.ui.features.add_transaction

import com.ritesh.cashiro.data.local.entity.RecurrenceFrequency
import com.ritesh.cashiro.data.local.entity.TransactionEntity
import com.ritesh.cashiro.data.local.entity.TransactionType

/**
 * Sealed class representing events that can occur in the Add Transaction Screen.
 * Used for unidirectional data flow from UI to ViewModel.
 */
sealed class AddTransactionEvent {

    // Basic Transaction Input Events
    data class UpdateTitle(val title: String) : AddTransactionEvent()
    data class UpdateAmount(val amount: String) : AddTransactionEvent()
    data class UpdateDate(val date: Long) : AddTransactionEvent()
    data class UpdateTime(val time: Long) : AddTransactionEvent()
    data class UpdateCategoryId(val categoryId: Int) : AddTransactionEvent()
    data class UpdateSubCategoryId(val subCategoryId: Int) : AddTransactionEvent()
    data class UpdateAccountId(val accountId: Int) : AddTransactionEvent()
    data class UpdateDestinationAccountId(val accountId: Int?) : AddTransactionEvent()
    data class UpdateNote(val note: String) : AddTransactionEvent()
    data class UpdateMode(val mode: String) : AddTransactionEvent()
    data class UpdateType(val type: TransactionType) : AddTransactionEvent()

    // Recurrence Events
    data class UpdateRecurrenceFrequency(val frequency: RecurrenceFrequency) : AddTransactionEvent()
    data class UpdateRecurrenceInterval(val interval: Int) : AddTransactionEvent()
    data class UpdateRecurrenceEndDate(val endDate: Long?) : AddTransactionEvent()
    data class UpdateIsPaid(val isPaid: Boolean) : AddTransactionEvent()
    data class UpdateNextDueDate(val dueDate: Long?) : AddTransactionEvent()

    // Bottom Sheet State Events
    data class SetTransactionTypeMenuOpened(val isOpen: Boolean) : AddTransactionEvent()
    data class SetTransactionTypeInfoSheetOpen(val isOpen: Boolean) : AddTransactionEvent()
    data class SetTransactionAmountSheetOpen(val isOpen: Boolean) : AddTransactionEvent()
    data class SetRecurrenceMenuOpen(val isOpen: Boolean) : AddTransactionEvent()
    data class SetRecurrenceBottomSheetOpen(val isOpen: Boolean) : AddTransactionEvent()
    data class SetCustomInputBottomSheetOpen(val isOpen: Boolean) : AddTransactionEvent()
    data class SetEndDateSelected(val isSelected: Boolean) : AddTransactionEvent()
    data class SetOpenEndDatePicker(val isOpen: Boolean) : AddTransactionEvent()

    // Dialog State Events
    data class SetOpenDatePickerDialog(val isOpen: Boolean) : AddTransactionEvent()
    data class SetTransactionDetailsSheetOpen(val isOpen: Boolean) : AddTransactionEvent()
    data class SetCategoryMenuOpened(val isOpen: Boolean) : AddTransactionEvent()
    data class SetAccountMenuOpened(val isOpen: Boolean) : AddTransactionEvent()
    data class SetAmountMenuOpened(val isOpen: Boolean) : AddTransactionEvent()

    // Tab Selection Events
    data class SelectTab(val index: Int) : AddTransactionEvent()

    // Transaction Actions
    object AddTransaction : AddTransactionEvent()
    data class UpdateTransaction(val oldTransaction: TransactionEntity, val newTransaction: TransactionEntity) : AddTransactionEvent()
    data class DeleteTransaction(val transaction: TransactionEntity) : AddTransactionEvent()

    // Data Loading Events
    data class LoadTransactionById(val id: Int) : AddTransactionEvent()
    object ClearTransactionFields : AddTransactionEvent()
    object FetchAllTransactions : AddTransactionEvent()

    // Transaction Type Operations
    data class FetchTransactionsByType(val transactionType: TransactionType) : AddTransactionEvent()
    object FetchUnpaidTransactions : AddTransactionEvent()

    // Transaction Status Updates
    data class MarkTransactionAsPaid(val transaction: TransactionEntity) : AddTransactionEvent()
    data class MarkTransactionAsCollected(val transaction: TransactionEntity) : AddTransactionEvent()
    data class MarkTransactionAsSettled(val transaction: TransactionEntity) : AddTransactionEvent()

    data class MarkTransactionAsUnpaid(val transaction: TransactionEntity) : AddTransactionEvent()
    data class MarkTransactionAsNotCollected(val transaction: TransactionEntity) : AddTransactionEvent()
    data class MarkTransactionAsUnsettled(val transaction: TransactionEntity) : AddTransactionEvent()

    // Navigation Events
    object NavigateBack : AddTransactionEvent()

    // Calculator Events
    data class UpdateCalculatedResult(val result: String) : AddTransactionEvent()

    // Validation Events
    object ValidateTransaction : AddTransactionEvent()

    // Error Handling
    data class ShowError(val message: String) : AddTransactionEvent()
    object ClearError : AddTransactionEvent()
}
