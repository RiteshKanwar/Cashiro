package com.ritesh.cashiro.presentation.ui.features.accounts

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ritesh.cashiro.data.local.entity.AccountEntity
import com.ritesh.cashiro.data.currency.model.Currency

/**
 * Data class representing the UI state for the Account Screen
 */
data class AccountScreenState(
    // Account list state
    val accounts: List<AccountEntity> = emptyList(),
    val transactionCounts: Map<Int, Int> = emptyMap(),

    // Form input state
    val accountName: String = "",
    val cardColor1: Color? = null,
    val cardColor2: Color? = null,
    val balance: String = "",
    val isMainAccount: Boolean = false,
    val currencyCode: String = "usd",

    // Currency related state
    val availableCurrencies: List<Currency> = emptyList(),
    val mainAccountCurrencyCode: String = "usd",
    val mainCurrencyConversionRates: Map<String, Double> = emptyMap(),

    // UI state
    val isEditingAccount: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,

    // Account deletion state
    val showAccountDeleteConfirmationDialog: Boolean = false,
    val showAccountDeleteOptionsDialog: Boolean = false,
    val showAccountMoveTransactionsDialog: Boolean = false,
    val showAccountFinalConfirmationDialog: Boolean = false,
    val pendingAccountDeletion: AccountEntity? = null,
    val availableAccountsForMigration: List<AccountEntity> = emptyList(),
    val selectedTargetAccountId: Int? = null,

    // Account merge state
    val showMergeAccountDialog: Boolean = false,
    val showMergeAccountFinalConfirmationDialog: Boolean = false,
    val pendingMergeAccountSource: AccountEntity? = null,
    val availableAccountsForMerge: List<AccountEntity> = emptyList(),
    val selectedMergeAccountTargetId: Int? = null,

    // Used for animation and collapsing
    val collapsingFraction: Float = 0f,
    val currentOffset: Dp = 180.dp,

    // Currency change tracking
    val lastCurrencyUpdateTimestamp: Long = 0L,
    val lastUpdatedAccountId: Int? = null,

    // Transaction currency conversion tracking
    val convertedTransactionAmounts: Map<Int, Double> = emptyMap()
)

/**
 * Sealed class representing all possible events for the Account Screen
 */
sealed class AccountScreenEvent {
    // Form events
    data class UpdateAccountName(val name: String) : AccountScreenEvent()
    data class UpdateCardColor1(val color: Color?) : AccountScreenEvent()
    data class UpdateCardColor2(val color: Color?) : AccountScreenEvent()
    data class UpdateBalance(val balance: String) : AccountScreenEvent()
    data class UpdateIsMainAccount(val isMain: Boolean) : AccountScreenEvent()
    data class UpdateCurrencyCode(val code: String) : AccountScreenEvent()

    // Account management events
    object PrepareForNewAccount : AccountScreenEvent()
    object SaveAccount : AccountScreenEvent()
    data class UpdateExistingAccount(val account: AccountEntity) : AccountScreenEvent()
    data class DeleteAccount(val account: AccountEntity) : AccountScreenEvent()
    data class GetAccountById(val id: Int) : AccountScreenEvent()
    data class OnAccountFetched(val id: Int, val onSuccess: (AccountEntity?) -> Unit) : AccountScreenEvent()
    data class SetMainAccount(val account: AccountEntity) : AccountScreenEvent()
    data class SetMainAccountById(val id: Int) : AccountScreenEvent()

    // List events
    data class ReorderAccounts(val fromIndex: Int, val toIndex: Int) : AccountScreenEvent()
    data class UpdateAccountsList(val accounts: List<AccountEntity>) : AccountScreenEvent()

    // Data loading events
    object FetchAllAccounts : AccountScreenEvent()
    object FetchAllTransactionCounts : AccountScreenEvent()
    data class RefreshTransactionCountForAccount(val accountId: Int) : AccountScreenEvent()

    // Account Deletion Events
    data class ShowAccountDeleteConfirmation(val account: AccountEntity) : AccountScreenEvent()
    data class ShowAccountDeleteOptions(val account: AccountEntity) : AccountScreenEvent()
    data class ShowAccountMoveTransactions(val account: AccountEntity) : AccountScreenEvent()
    data class SelectTargetAccountForMigration(val accountId: Int) : AccountScreenEvent()
    data class ConfirmAccountMigrationAndDeletion(val sourceAccount: AccountEntity, val targetAccountId: Int) : AccountScreenEvent()
    data class ConfirmAccountDeletionWithTransactions(val account: AccountEntity) : AccountScreenEvent()
    object CancelAccountDeletion : AccountScreenEvent()

    // Account Merge Events
    data class ShowMergeAccountDialog(val account: AccountEntity) : AccountScreenEvent()
    data class SelectTargetAccountForMerge(val accountId: Int) : AccountScreenEvent()
    data class ConfirmAccountMerge(val sourceAccount: AccountEntity, val targetAccountId: Int) : AccountScreenEvent()
    object CancelMergeAccount : AccountScreenEvent()

    // Currency-specific events
    data class AccountCurrencyChanged(
        val accountId: Int,
        val oldCurrencyCode: String,
        val newCurrencyCode: String,
        val conversionRate: Double
    ) : AccountScreenEvent()

    // Transaction handling events
    data class UpdateTransactionAmounts(
        val accountId: Int,
        val oldCurrencyCode: String,
        val newCurrencyCode: String,
        val conversionRate: Double
    ) : AccountScreenEvent()

    // Success callbacks
    data class OnAccountSaved(val onSuccess: () -> Unit) : AccountScreenEvent()
    data class OnAccountUpdated(val account: AccountEntity, val onSuccess: () -> Unit) : AccountScreenEvent()
}

/**
 * Interface for handling AccountScreenEvents
 * This will be implemented by the ViewModel
 */
interface AccountScreenEventHandler {
    fun onEvent(event: AccountScreenEvent)
}