package com.ritesh.cashiro.domain.repository

import android.util.Log
import com.ritesh.cashiro.data.local.dao.AccountDao
import com.ritesh.cashiro.data.local.entity.AccountEntity
import com.ritesh.cashiro.data.local.entity.TransactionEntity
import com.ritesh.cashiro.data.local.entity.TransactionType
import com.ritesh.cashiro.domain.utils.AccountEvent
import com.ritesh.cashiro.domain.utils.AppEventBus
import com.ritesh.cashiro.domain.utils.TransactionEvent
import com.ritesh.cashiro.widgets.WidgetUpdateUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val accountDao: AccountDao,
    private val transactionRepository: TransactionRepository

) {
    suspend fun addAccount(account: AccountEntity): Int {
        val result = accountDao.insertAccount(account).toInt()

        // Emit event instead of direct widget update
        AppEventBus.tryEmitEvent(AccountEvent.AccountsUpdated)

        return result
    }

    // Add a account to the database
    suspend fun addAccounts(accounts: List<AccountEntity>) = withContext(Dispatchers.IO) {
        accountDao.insertAccounts(accounts)
    }

    // Update an existing account
    suspend fun updateAccount(account: AccountEntity) = withContext(Dispatchers.IO) {
        accountDao.updateAccount(account)

        // Emit event instead of direct widget update
        AppEventBus.tryEmitEvent(AccountEvent.AccountsUpdated)
    }

    // Delete a account from the database
    suspend fun deleteAccount(account: AccountEntity) = withContext(Dispatchers.IO) {
        accountDao.deleteAccount(account)

        // Emit event instead of direct widget update
        AppEventBus.tryEmitEvent(AccountEvent.AccountDeleted(account.id))
    }

    // Fetch all accounts
    suspend fun getAllAccounts(): List<AccountEntity> = withContext(Dispatchers.IO) {
        accountDao.getAllAccounts()
    }

    suspend fun getAccountById(accountId: Int): AccountEntity? = withContext(Dispatchers.IO) {
        accountDao.getAccountById(accountId)
    }

    suspend fun updateAccountOrder(accounts: List<AccountEntity>) = withContext(Dispatchers.IO) {
        accountDao.updateAccounts(accounts)

        // Emit event instead of direct widget update
        AppEventBus.tryEmitEvent(AccountEvent.AccountsUpdated)
    }

    suspend fun updateMainAccount(id: Int, isMain: Boolean) {
        accountDao.updateMainAccount(id, isMain)

        // Emit event instead of direct widget update
        AppEventBus.tryEmitEvent(AccountEvent.AccountsUpdated)
    }

    suspend fun updateAccountBalance(
        accountId: Int,
        amount: Double,
        isExpense: Boolean = true
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d("AccountRepository", "Updating balance for account $accountId: ${if (isExpense) "-" else "+"}$amount")

            val account = accountDao.getAccountById(accountId)
            if (account != null) {
                val newBalance = if (isExpense) {
                    account.balance - amount
                } else {
                    account.balance + amount
                }

                val updatedAccount = account.copy(balance = newBalance)
                accountDao.updateAccount(updatedAccount)

                // Emit events instead of direct widget update
                AppEventBus.tryEmitEvent(AccountEvent.BalanceUpdated(accountId, newBalance))
                AppEventBus.tryEmitEvent(AccountEvent.AccountsUpdated)

                Log.d("AccountRepository", "Successfully updated account $accountId balance to $newBalance")
                return@withContext true
            } else {
                Log.e("AccountRepository", "Account with ID $accountId not found")
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e("AccountRepository", "Error updating account balance: ${e.message}", e)
            return@withContext false
        }
    }

    suspend fun handleTransfer(
        sourceAccountId: Int,
        destinationAccountId: Int,
        amount: Double
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d("AccountRepository", "Handling transfer: $amount from account $sourceAccountId to $destinationAccountId")

            val sourceSuccess = updateAccountBalance(sourceAccountId, amount, isExpense = true)
            val destSuccess = updateAccountBalance(destinationAccountId, amount, isExpense = false)

            val success = sourceSuccess && destSuccess

            if (success) {
                AppEventBus.tryEmitEvent(AccountEvent.AccountsUpdated)
                Log.d("AccountRepository", "Successfully completed transfer")
            } else {
                Log.e("AccountRepository", "Transfer failed - source success: $sourceSuccess, dest success: $destSuccess")
            }

            return@withContext success
        } catch (e: Exception) {
            Log.e("AccountRepository", "Error handling transfer: ${e.message}", e)
            return@withContext false
        }
    }

    suspend fun handleTransferWithConversion(
        sourceAccountId: Int,
        destinationAccountId: Int,
        sourceAmount: Double,
        destinationAmount: Double
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d("AccountRepository", "Handling transfer with conversion: $sourceAmount from account $sourceAccountId, $destinationAmount to account $destinationAccountId")

            val sourceAccount = accountDao.getAccountById(sourceAccountId)
            val destAccount = accountDao.getAccountById(destinationAccountId)

            if (sourceAccount != null && destAccount != null) {
                val newSourceBalance = sourceAccount.balance - sourceAmount
                val updatedSourceAccount = sourceAccount.copy(balance = newSourceBalance)
                accountDao.updateAccount(updatedSourceAccount)

                val newDestBalance = destAccount.balance + destinationAmount
                val updatedDestAccount = destAccount.copy(balance = newDestBalance)
                accountDao.updateAccount(updatedDestAccount)

                // Emit events instead of direct widget updates
                AppEventBus.tryEmitEvent(AccountEvent.BalanceUpdated(sourceAccountId, newSourceBalance))
                AppEventBus.tryEmitEvent(AccountEvent.BalanceUpdated(destinationAccountId, newDestBalance))
                AppEventBus.tryEmitEvent(AccountEvent.AccountsUpdated)

                Log.d("AccountRepository", "Successfully completed transfer with conversion")
                return@withContext true
            } else {
                Log.e("AccountRepository", "One or both accounts not found for transfer")
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e("AccountRepository", "Error handling transfer with conversion: ${e.message}", e)
            return@withContext false
        }
    }

    // Get transaction statistics for an account
    suspend fun getTransactionStatsForAccount(accountId: Int): AccountTransactionStats = withContext(Dispatchers.IO) {
        try {
            // Get all transactions for this account from the DAO
            val transactions = transactionRepository.getTransactionsByAccountId(accountId) // You'll need to add this method to AccountDao

            val totalAmount = transactions.sumOf { it.amount }
            val expenseAmount = transactions.filter { it.mode == "Expense" }.sumOf { it.amount }
            val incomeAmount = transactions.filter { it.mode == "Income" }.sumOf { it.amount }
            val transferAmount = transactions.filter { it.mode == "Transfer" }.sumOf { it.amount }

            AccountTransactionStats(
                count = transactions.size,
                expenseAmount = expenseAmount,
                incomeAmount = incomeAmount,
                transferAmount = transferAmount
            )
        } catch (e: Exception) {
            Log.e("AccountRepository", "Error getting transaction stats for account", e)
            AccountTransactionStats(0, 0.0, 0.0, 0.0)
        }
    }

    // Migrate all transactions from source account to target account
    suspend fun migrateAccountTransactions(
        sourceAccountId: Int,
        targetAccountId: Int,
        transactionRepository: TransactionRepository
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d("AccountMigration", "Starting migration from account $sourceAccountId to $targetAccountId")

            // Get source and target accounts
            val sourceAccount = accountDao.getAccountById(sourceAccountId)
            val targetAccount = accountDao.getAccountById(targetAccountId)

            if (sourceAccount == null || targetAccount == null) {
                Log.e("AccountMigration", "Source or target account not found")
                return@withContext false
            }

            // Get all transactions for the source account
            val transactions = transactionRepository.getTransactionsByAccountId(sourceAccountId)
            Log.d("AccountMigration", "Found ${transactions.size} transactions to migrate")

            var successfulMigrations = 0

            // Process each transaction
            transactions.forEach { transaction ->
                try {
                    // Handle different transaction types and their balance implications
                    val shouldUpdateBalance = when (transaction.transactionType) {
                        TransactionType.DEFAULT -> true
                        TransactionType.UPCOMING, TransactionType.SUBSCRIPTION, TransactionType.REPETITIVE -> transaction.isPaid
                        TransactionType.LENT -> !transaction.isCollected // If not collected, money was deducted
                        TransactionType.BORROWED -> !transaction.isSettled // If not settled, money was added
                    }

                    if (shouldUpdateBalance) {
                        // Calculate balance transfer based on transaction mode and status
                        when (transaction.mode) {
                            "Expense" -> {
                                // Move expense: add to source account (reverse), subtract from target
                                updateAccountBalance(sourceAccountId, transaction.amount, isExpense = false)
                                updateAccountBalance(targetAccountId, transaction.amount, isExpense = true)
                            }
                            "Income" -> {
                                // Move income: subtract from source account (reverse), add to target
                                updateAccountBalance(sourceAccountId, transaction.amount, isExpense = true)
                                updateAccountBalance(targetAccountId, transaction.amount, isExpense = false)
                            }
                            "Transfer" -> {
                                // Handle transfer migrations - more complex as they involve destination accounts
                                if (transaction.destinationAccountId == sourceAccountId) {
                                    // This account was the destination, update the destination
                                    val updatedTransaction = transaction.copy(destinationAccountId = targetAccountId)
                                    transactionRepository.updateTransaction(updatedTransaction)
                                } else {
                                    // This account was the source, update the account ID
                                    val updatedTransaction = transaction.copy(accountId = targetAccountId)
                                    transactionRepository.updateTransaction(updatedTransaction)
                                }
                            }
                        }
                    }

                    // Update the transaction to point to the target account (if not a transfer)
                    if (transaction.mode != "Transfer") {
                        val updatedTransaction = transaction.copy(accountId = targetAccountId)
                        transactionRepository.updateTransaction(updatedTransaction)
                    }

                    successfulMigrations++
                    Log.d("AccountMigration", "Successfully migrated transaction ${transaction.id}")

                } catch (e: Exception) {
                    Log.e("AccountMigration", "Error migrating transaction ${transaction.id}: ${e.message}", e)
                }
            }

            Log.d("AccountMigration", "Migration completed: $successfulMigrations/${transactions.size} transactions migrated")

            // Emit events to update UI
            AppEventBus.tryEmitEvent(AccountEvent.AccountsUpdated)
            AppEventBus.tryEmitEvent(TransactionEvent.TransactionsUpdated)

            return@withContext successfulMigrations == transactions.size

        } catch (e: Exception) {
            Log.e("AccountMigration", "Error during account migration: ${e.message}", e)
            return@withContext false
        }
    }

    // Delete account with all its transactions and balance restoration
    suspend fun deleteAccountWithTransactions(
        accountId: Int,
        transactionRepository: TransactionRepository
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d("AccountDeletion", "Deleting account $accountId with all transactions")

            val account = accountDao.getAccountById(accountId)
            if (account == null) {
                Log.e("AccountDeletion", "Account not found")
                return@withContext false
            }

            // Get all transactions for this account
            val transactions = transactionRepository.getTransactionsByAccountId(accountId)
            Log.d("AccountDeletion", "Found ${transactions.size} transactions to delete")

            // Delete transactions and restore balances
            transactions.forEach { transaction ->
                // Restore account balances based on transaction type and status
                restoreBalanceForDeletedTransaction(transaction)
                transactionRepository.deleteTransaction(transaction)
            }

            // Delete the account
            deleteAccount(account)

            // Emit events
            AppEventBus.tryEmitEvent(AccountEvent.AccountDeleted(accountId))
            AppEventBus.tryEmitEvent(TransactionEvent.TransactionsUpdated)

            Log.d("AccountDeletion", "Successfully deleted account and ${transactions.size} transactions")
            return@withContext true

        } catch (e: Exception) {
            Log.e("AccountDeletion", "Error deleting account with transactions: ${e.message}", e)
            return@withContext false
        }
    }

    private suspend fun restoreBalanceForDeletedTransaction(transaction: TransactionEntity) {
        // This logic should match the existing transaction deletion balance restoration
        // Implementation would be similar to the one in TransactionRepository
    }
}

data class AccountTransactionStats(
    val count: Int,
    val expenseAmount: Double,
    val incomeAmount: Double,
    val transferAmount: Double
)

//@Singleton
//class AccountRepository @Inject constructor(
//    private val accountDao: AccountDao,
//    private val transactionRepository: TransactionRepository,
//    private val widgetUpdateUtil: WidgetUpdateUtil
//
//) {
//    suspend fun addAccount(account: AccountEntity): Int {
//        // Insert the account and return the new ID
//        return accountDao.insertAccount(account).toInt()
//    }
//
//    // Add a account to the database
//    suspend fun addAccounts(accounts: List<AccountEntity>) = withContext(Dispatchers.IO) {
//        accountDao.insertAccounts(accounts)
//    }
//
//    // Update an existing account
//    suspend fun updateAccount(account: AccountEntity) = withContext(Dispatchers.IO) {
//        accountDao.updateAccount(account)
//    }
//
//    // Delete a account from the database
//    suspend fun deleteAccount(account: AccountEntity) = withContext(Dispatchers.IO) {
//        accountDao.deleteAccount(account)
//    }
//
//    // Fetch all accounts
//    suspend fun getAllAccounts(): List<AccountEntity> = withContext(Dispatchers.IO) {
//        accountDao.getAllAccounts()
//    }
//
//    suspend fun getAccountById(accountId: Int): AccountEntity? = withContext(Dispatchers.IO) {
//        accountDao.getAccountById(accountId)
//    }
//
//    // Update the order of multiple accounts
//    suspend fun updateAccountOrder(accounts: List<AccountEntity>) = withContext(Dispatchers.IO) {
//        accountDao.updateAccounts(accounts)
//    }
//
//    // Makes account a primary account
//    suspend fun updateMainAccount(id: Int, isMain: Boolean) {
//        accountDao.updateMainAccount(id, isMain)
//    }
//
//    suspend fun updateAccountBalance(
//        accountId: Int,
//        amount: Double,
//        isExpense: Boolean = true
//    ): Boolean = withContext(Dispatchers.IO) {
//        try {
//            Log.d(
//                "AccountRepository",
//                "Updating balance for account $accountId: ${if (isExpense) "-" else "+"}$amount"
//            )
//
//            val account = accountDao.getAccountById(accountId)
//            if (account != null) {
//                val newBalance = if (isExpense) {
//                    account.balance - amount
//                } else {
//                    account.balance + amount
//                }
//
//                val updatedAccount = account.copy(balance = newBalance)
//                accountDao.updateAccount(updatedAccount)
//
//                widgetUpdateUtil.updateAllFinancialWidgets()
//
//                // Emit immediate balance update event
//                AppEventBus.tryEmitEvent(AccountEvent.BalanceUpdated(accountId, newBalance))
//
//                Log.d(
//                    "AccountRepository",
//                    "Successfully updated account $accountId balance to $newBalance"
//                )
//                return@withContext true
//            } else {
//                Log.e("AccountRepository", "Account with ID $accountId not found")
//                return@withContext false
//            }
//        } catch (e: Exception) {
//            Log.e("AccountRepository", "Error updating account balance: ${e.message}", e)
//            return@withContext false
//        }
//    }
//
//    // ENHANCED: Handle transfer with immediate UI notification
//    suspend fun handleTransfer(
//        sourceAccountId: Int,
//        destinationAccountId: Int,
//        amount: Double
//    ): Boolean = withContext(Dispatchers.IO) {
//        try {
//            Log.d(
//                "AccountRepository",
//                "Handling transfer: $amount from account $sourceAccountId to $destinationAccountId"
//            )
//
//            val sourceSuccess = updateAccountBalance(sourceAccountId, amount, isExpense = true)
//            val destSuccess = updateAccountBalance(destinationAccountId, amount, isExpense = false)
//
//            val success = sourceSuccess && destSuccess
//
//            if (success) {
//                // Emit event for both accounts updated
//                AppEventBus.tryEmitEvent(AccountEvent.AccountsUpdated)
//                Log.d("AccountRepository", "Successfully completed transfer")
//            } else {
//                Log.e(
//                    "AccountRepository",
//                    "Transfer failed - source success: $sourceSuccess, dest success: $destSuccess"
//                )
//            }
//
//            return@withContext success
//        } catch (e: Exception) {
//            Log.e("AccountRepository", "Error handling transfer: ${e.message}", e)
//            return@withContext false
//        }
//    }
//
//    // ENHANCED: Handle transfer with conversion and immediate UI notification
//    suspend fun handleTransferWithConversion(
//        sourceAccountId: Int,
//        destinationAccountId: Int,
//        sourceAmount: Double,
//        destinationAmount: Double
//    ): Boolean = withContext(Dispatchers.IO) {
//        try {
//            Log.d(
//                "AccountRepository",
//                "Handling transfer with conversion: $sourceAmount from account $sourceAccountId, $destinationAmount to account $destinationAccountId"
//            )
//
//            val sourceAccount = accountDao.getAccountById(sourceAccountId)
//            val destAccount = accountDao.getAccountById(destinationAccountId)
//
//            if (sourceAccount != null && destAccount != null) {
//                // Update source account
//                val newSourceBalance = sourceAccount.balance - sourceAmount
//                val updatedSourceAccount = sourceAccount.copy(balance = newSourceBalance)
//                accountDao.updateAccount(updatedSourceAccount)
//
//                // Update destination account
//                val newDestBalance = destAccount.balance + destinationAmount
//                val updatedDestAccount = destAccount.copy(balance = newDestBalance)
//                accountDao.updateAccount(updatedDestAccount)
//
//                // Emit immediate balance update events for both accounts
//                AppEventBus.tryEmitEvent(
//                    AccountEvent.BalanceUpdated(
//                        sourceAccountId,
//                        newSourceBalance
//                    )
//                )
//                AppEventBus.tryEmitEvent(
//                    AccountEvent.BalanceUpdated(
//                        destinationAccountId,
//                        newDestBalance
//                    )
//                )
//                AppEventBus.tryEmitEvent(AccountEvent.AccountsUpdated)
//
//                Log.d("AccountRepository", "Successfully completed transfer with conversion")
//                return@withContext true
//            } else {
//                Log.e("AccountRepository", "One or both accounts not found for transfer")
//                return@withContext false
//            }
//        } catch (e: Exception) {
//            Log.e("AccountRepository", "Error handling transfer with conversion: ${e.message}", e)
//            return@withContext false
//        }
//    }
//
//    // Get transaction statistics for an account
//    suspend fun getTransactionStatsForAccount(accountId: Int): AccountTransactionStats = withContext(Dispatchers.IO) {
//        try {
//            // Get all transactions for this account from the DAO
//            val transactions = transactionRepository.getTransactionsByAccountId(accountId) // You'll need to add this method to AccountDao
//
//            val totalAmount = transactions.sumOf { it.amount }
//            val expenseAmount = transactions.filter { it.mode == "Expense" }.sumOf { it.amount }
//            val incomeAmount = transactions.filter { it.mode == "Income" }.sumOf { it.amount }
//            val transferAmount = transactions.filter { it.mode == "Transfer" }.sumOf { it.amount }
//
//            AccountTransactionStats(
//                count = transactions.size,
//                expenseAmount = expenseAmount,
//                incomeAmount = incomeAmount,
//                transferAmount = transferAmount
//            )
//        } catch (e: Exception) {
//            Log.e("AccountRepository", "Error getting transaction stats for account", e)
//            AccountTransactionStats(0, 0.0, 0.0, 0.0)
//        }
//    }
//
//    // Migrate all transactions from source account to target account
//    suspend fun migrateAccountTransactions(
//        sourceAccountId: Int,
//        targetAccountId: Int,
//        transactionRepository: TransactionRepository
//    ): Boolean = withContext(Dispatchers.IO) {
//        try {
//            Log.d("AccountMigration", "Starting migration from account $sourceAccountId to $targetAccountId")
//
//            // Get source and target accounts
//            val sourceAccount = accountDao.getAccountById(sourceAccountId)
//            val targetAccount = accountDao.getAccountById(targetAccountId)
//
//            if (sourceAccount == null || targetAccount == null) {
//                Log.e("AccountMigration", "Source or target account not found")
//                return@withContext false
//            }
//
//            // Get all transactions for the source account
//            val transactions = transactionRepository.getTransactionsByAccountId(sourceAccountId)
//            Log.d("AccountMigration", "Found ${transactions.size} transactions to migrate")
//
//            var successfulMigrations = 0
//
//            // Process each transaction
//            transactions.forEach { transaction ->
//                try {
//                    // Handle different transaction types and their balance implications
//                    val shouldUpdateBalance = when (transaction.transactionType) {
//                        TransactionType.DEFAULT -> true
//                        TransactionType.UPCOMING, TransactionType.SUBSCRIPTION, TransactionType.REPETITIVE -> transaction.isPaid
//                        TransactionType.LENT -> !transaction.isCollected // If not collected, money was deducted
//                        TransactionType.BORROWED -> !transaction.isSettled // If not settled, money was added
//                    }
//
//                    if (shouldUpdateBalance) {
//                        // Calculate balance transfer based on transaction mode and status
//                        when (transaction.mode) {
//                            "Expense" -> {
//                                // Move expense: add to source account (reverse), subtract from target
//                                updateAccountBalance(sourceAccountId, transaction.amount, isExpense = false)
//                                updateAccountBalance(targetAccountId, transaction.amount, isExpense = true)
//                            }
//                            "Income" -> {
//                                // Move income: subtract from source account (reverse), add to target
//                                updateAccountBalance(sourceAccountId, transaction.amount, isExpense = true)
//                                updateAccountBalance(targetAccountId, transaction.amount, isExpense = false)
//                            }
//                            "Transfer" -> {
//                                // Handle transfer migrations - more complex as they involve destination accounts
//                                if (transaction.destinationAccountId == sourceAccountId) {
//                                    // This account was the destination, update the destination
//                                    val updatedTransaction = transaction.copy(destinationAccountId = targetAccountId)
//                                    transactionRepository.updateTransaction(updatedTransaction)
//                                } else {
//                                    // This account was the source, update the account ID
//                                    val updatedTransaction = transaction.copy(accountId = targetAccountId)
//                                    transactionRepository.updateTransaction(updatedTransaction)
//                                }
//                            }
//                        }
//                    }
//
//                    // Update the transaction to point to the target account (if not a transfer)
//                    if (transaction.mode != "Transfer") {
//                        val updatedTransaction = transaction.copy(accountId = targetAccountId)
//                        transactionRepository.updateTransaction(updatedTransaction)
//                    }
//
//                    successfulMigrations++
//                    Log.d("AccountMigration", "Successfully migrated transaction ${transaction.id}")
//
//                } catch (e: Exception) {
//                    Log.e("AccountMigration", "Error migrating transaction ${transaction.id}: ${e.message}", e)
//                }
//            }
//
//            Log.d("AccountMigration", "Migration completed: $successfulMigrations/${transactions.size} transactions migrated")
//
//            // Emit events to update UI
//            AppEventBus.tryEmitEvent(AccountEvent.AccountsUpdated)
//            AppEventBus.tryEmitEvent(TransactionEvent.TransactionsUpdated)
//
//            return@withContext successfulMigrations == transactions.size
//
//        } catch (e: Exception) {
//            Log.e("AccountMigration", "Error during account migration: ${e.message}", e)
//            return@withContext false
//        }
//    }
//
//    // Delete account with all its transactions and balance restoration
//    suspend fun deleteAccountWithTransactions(
//        accountId: Int,
//        transactionRepository: TransactionRepository
//    ): Boolean = withContext(Dispatchers.IO) {
//        try {
//            Log.d("AccountDeletion", "Deleting account $accountId with all transactions")
//
//            val account = accountDao.getAccountById(accountId)
//            if (account == null) {
//                Log.e("AccountDeletion", "Account not found")
//                return@withContext false
//            }
//
//            // Get all transactions for this account
//            val transactions = transactionRepository.getTransactionsByAccountId(accountId)
//            Log.d("AccountDeletion", "Found ${transactions.size} transactions to delete")
//
//            // Delete transactions and restore balances
//            transactions.forEach { transaction ->
//                // Restore account balances based on transaction type and status
//                restoreBalanceForDeletedTransaction(transaction)
//                transactionRepository.deleteTransaction(transaction)
//            }
//
//            // Delete the account
//            deleteAccount(account)
//
//            // Emit events
//            AppEventBus.tryEmitEvent(AccountEvent.AccountDeleted(accountId))
//            AppEventBus.tryEmitEvent(TransactionEvent.TransactionsUpdated)
//
//            Log.d("AccountDeletion", "Successfully deleted account and ${transactions.size} transactions")
//            return@withContext true
//
//        } catch (e: Exception) {
//            Log.e("AccountDeletion", "Error deleting account with transactions: ${e.message}", e)
//            return@withContext false
//        }
//    }
//
//    private suspend fun restoreBalanceForDeletedTransaction(transaction: TransactionEntity) {
//        // This logic should match the existing transaction deletion balance restoration
//        // Implementation would be similar to the one in TransactionRepository
//    }
//}
//
//data class AccountTransactionStats(
//    val count: Int,
//    val expenseAmount: Double,
//    val incomeAmount: Double,
//    val transferAmount: Double
//)