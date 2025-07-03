package com.ritesh.cashiro.domain.repository

import android.util.Log
import com.ritesh.cashiro.data.local.dao.TransactionDao
import com.ritesh.cashiro.data.local.entity.Recurrence
import com.ritesh.cashiro.data.local.entity.RecurrenceFrequency
import com.ritesh.cashiro.data.local.entity.TransactionEntity
import com.ritesh.cashiro.data.local.entity.TransactionType
import com.ritesh.cashiro.domain.utils.getNextDueDate

class TransactionRepository(private val transactionDao: TransactionDao) {

    suspend fun addTransaction(transaction: TransactionEntity) {
        try {
            Log.d("TransferDebug", "Starting repository.addTransaction")
            val id = transactionDao.insertTransaction(transaction)
            Log.d("TransferDebug", "Transaction inserted with ID: $id")
        } catch (e: Exception) {
            Log.e("TransferDebug", "Error adding transaction: ${e.message}", e)
            throw e
        }
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun deleteTransactionById(id: Int) {
        transactionDao.deleteTransactionById(id)
    }

    suspend fun getTransactionById(id: Int): TransactionEntity? {
        return transactionDao.getTransactionById(id)
    }

    suspend fun getAllTransactions(): List<TransactionEntity> {
        return transactionDao.getAllTransactions()
    }

    suspend fun getAllTransactionsByMode(mode: String): List<TransactionEntity> {
        return transactionDao.getAllTransactionsByMode(mode)
    }

    // FIXED: Get unpaid transactions with better filtering
    suspend fun getUnpaidTransactions(): List<TransactionEntity> {
        return transactionDao.getUpcomingTransactions(TransactionType.UPCOMING.name)
            .filter { !it.isPaid } // Only show unpaid upcoming transactions
    }

    suspend fun getLentTransactions(): List<TransactionEntity> {
        return transactionDao.getLentTransactions(TransactionType.LENT.name)
            .filter { !it.isCollected } // Only show uncollected lent transactions
    }

    suspend fun getBorrowedTransactions(): List<TransactionEntity> {
        return transactionDao.getBorrowedTransactions(TransactionType.BORROWED.name)
            .filter { !it.isSettled } // Only show unsettled borrowed transactions
    }

    // FIXED: Get subscription transactions with better filtering
    suspend fun getSubscriptionTransactions(): List<TransactionEntity> {
        return transactionDao.getSubscriptionTransactions(TransactionType.SUBSCRIPTION)
            .filter { !it.isPaid } // Only show unpaid subscriptions
    }

    // FIXED: Get repetitive transactions with better filtering
    suspend fun getRepetitiveTransactions(): List<TransactionEntity> {
        return transactionDao.getRepetitiveTransactions(TransactionType.REPETITIVE)
            .filter { !it.isPaid } // Only show unpaid repetitive transactions
    }

    suspend fun getTransactionsByAccountId(accountId: Int): List<TransactionEntity> {
        return transactionDao.getTransactionsByAccountId(accountId)
    }

    suspend fun updateTransactionAmount(transactionId: Int, newAmount: Double) {
        transactionDao.updateTransactionAmount(transactionId, newAmount)
    }

    suspend fun getTransactionCountByAccountId(accountId: Int): Int {
        return transactionDao.getTransactionCountByAccountId(accountId)
    }

    fun handleTransactionCashFlow(transaction: TransactionEntity): Double {
        return when (transaction.transactionType) {
            TransactionType.DEFAULT -> transaction.amount
            TransactionType.UPCOMING -> if (transaction.isPaid) transaction.amount else 0.0
            TransactionType.LENT -> if (transaction.isCollected) transaction.amount else -transaction.amount
            TransactionType.BORROWED -> if (transaction.isSettled) 0.0 else transaction.amount
            else -> 0.0
        }
    }

    fun updateTransactionStatus(transaction: TransactionEntity): TransactionEntity {
        return when (transaction.transactionType) {
            TransactionType.UPCOMING -> transaction.copy(isPaid = !transaction.isPaid)
            TransactionType.LENT -> transaction.copy(isCollected = !transaction.isCollected)
            TransactionType.BORROWED -> transaction.copy(isSettled = !transaction.isSettled)
            TransactionType.SUBSCRIPTION, TransactionType.REPETITIVE -> transaction.copy(isPaid = !transaction.isPaid)
            else -> transaction
        }
    }

    // FIXED: Mark transaction as paid with automatic next generation
    suspend fun markTransactionAsPaid(transaction: TransactionEntity) {
        val updatedTransaction = transaction.copy(isPaid = true)
        transactionDao.updateTransaction(updatedTransaction)

        // FIXED: Automatically generate next recurring transaction when current is paid
        if (transaction.transactionType in listOf(TransactionType.SUBSCRIPTION, TransactionType.REPETITIVE)
            && transaction.recurrence != null) {
            generateNextRecurringTransaction(updatedTransaction)
        }
    }

    suspend fun markTransactionAsCollected(transaction: TransactionEntity) {
        val updatedTransaction = transaction.copy(isCollected = true)
        transactionDao.updateTransaction(updatedTransaction)
    }

    suspend fun markTransactionAsSettled(transaction: TransactionEntity) {
        val updatedTransaction = transaction.copy(isSettled = true)
        transactionDao.updateTransaction(updatedTransaction)
    }

    suspend fun markTransactionAsUnpaid(transaction: TransactionEntity) {
        val updatedTransaction = transaction.copy(isPaid = false)
        transactionDao.updateTransaction(updatedTransaction)
    }

    suspend fun markTransactionAsNotCollected(transaction: TransactionEntity) {
        val updatedTransaction = transaction.copy(isCollected = false)
        transactionDao.updateTransaction(updatedTransaction)
    }

    suspend fun markTransactionAsUnsettled(transaction: TransactionEntity) {
        val updatedTransaction = transaction.copy(isSettled = false)
        transactionDao.updateTransaction(updatedTransaction)
    }

    // FIXED: Improved recurring transaction generation
    suspend fun generateNextRecurringTransaction(transaction: TransactionEntity) {
        try {
            val recurrence = transaction.recurrence
            if (recurrence == null || recurrence.frequency == RecurrenceFrequency.NONE) {
                Log.d("RecurringTransaction", "No recurrence found for transaction ${transaction.id}")
                return
            }

            // Calculate the next due date from the current transaction's date or nextDueDate
            val currentDueDate = transaction.nextDueDate ?: transaction.date
            val nextDueDate = getNextDueDate(
                currentDueDate,
                recurrence.frequency,
                recurrence.interval,
                recurrence.endRecurrenceDate ?: Long.MAX_VALUE
            )

            if (nextDueDate != null) {
                // Check if a transaction for this next due date already exists
                val existingNextTransaction = getAllTransactions().find { existingTransaction ->
                    existingTransaction.title == transaction.title &&
                            existingTransaction.amount == transaction.amount &&
                            existingTransaction.accountId == transaction.accountId &&
                            existingTransaction.transactionType == transaction.transactionType &&
                            existingTransaction.date == nextDueDate &&
                            !existingTransaction.isPaid
                }

                if (existingNextTransaction != null) {
                    Log.d("RecurringTransaction", "Next occurrence already exists for ${transaction.title} on date: $nextDueDate")
                    return
                }

                // Calculate the due date after the next one (for the new transaction's nextDueDate)
                val followingDueDate = getNextDueDate(
                    nextDueDate,
                    recurrence.frequency,
                    recurrence.interval,
                    recurrence.endRecurrenceDate ?: Long.MAX_VALUE
                )

                // Create a completely new transaction
                val newTransaction = TransactionEntity(
                    id = 0, // Let database generate new ID
                    title = transaction.title,
                    amount = transaction.amount,
                    date = nextDueDate,
                    time = transaction.time,
                    recurrence = recurrence,
                    categoryId = transaction.categoryId,
                    subCategoryId = transaction.subCategoryId,
                    accountId = transaction.accountId,
                    note = transaction.note,
                    mode = transaction.mode,
                    transactionType = transaction.transactionType,
                    isPaid = false, // New recurring transaction starts as unpaid
                    isCollected = false,
                    isSettled = false,
                    nextDueDate = followingDueDate,
                    destinationAccountId = transaction.destinationAccountId,
                    originalCurrencyCode = transaction.originalCurrencyCode,
                    endDate = transaction.endDate
                )

                // Insert the new recurring transaction
                addTransaction(newTransaction)

                Log.d("RecurringTransaction", "Created next recurring transaction for ${transaction.title} on date: $nextDueDate")
            } else {
                Log.d("RecurringTransaction", "No more recurring transactions to create for ${transaction.title} - end date reached")
            }
        } catch (e: Exception) {
            Log.e("RecurringTransaction", "Error generating next recurring transaction: ${e.message}", e)
            throw e
        }
    }

    // FIXED: Better method to get active scheduled transactions
    suspend fun getActiveScheduledTransactions(): List<TransactionEntity> {
        val upcomingTransactions = getUnpaidTransactions()
        val subscriptions = getSubscriptionTransactions()
        val repetitiveTransactions = getRepetitiveTransactions()
        val lentTransactions = getLentTransactions()
        val borrowedTransactions = getBorrowedTransactions()

        return (upcomingTransactions + subscriptions + repetitiveTransactions + lentTransactions + borrowedTransactions)
            .distinctBy { it.id }
    }

    // NEW: Method to clean up duplicate future transactions
    suspend fun cleanupDuplicateRecurringTransactions() {
        try {
            Log.d("CleanupRecurring", "Starting cleanup of duplicate recurring transactions")

            val allTransactions = getAllTransactions()
            val recurringTransactions = allTransactions.filter {
                it.transactionType in listOf(TransactionType.SUBSCRIPTION, TransactionType.REPETITIVE)
            }

            // Group by title, amount, account, and type
            val groups = recurringTransactions.groupBy {
                Triple(it.title, it.amount, it.accountId)
            }

            groups.forEach { (key, transactions) ->
                if (transactions.size > 1) {
                    // Sort by date and keep only the earliest unpaid transaction
                    val sortedTransactions = transactions.sortedBy { it.date }
                    val unpaidTransactions = sortedTransactions.filter { !it.isPaid }

                    if (unpaidTransactions.size > 1) {
                        // Keep the earliest unpaid transaction, delete the rest
                        val toKeep = unpaidTransactions.first()
                        val toDelete = unpaidTransactions.drop(1)

                        Log.d("CleanupRecurring", "Keeping transaction ${toKeep.id} (${toKeep.title}) dated ${toKeep.date}")

                        toDelete.forEach { duplicate ->
                            Log.d("CleanupRecurring", "Deleting duplicate transaction ${duplicate.id} (${duplicate.title}) dated ${duplicate.date}")
                            deleteTransaction(duplicate)
                        }
                    }
                }
            }

            Log.d("CleanupRecurring", "Completed cleanup of duplicate recurring transactions")
        } catch (e: Exception) {
            Log.e("CleanupRecurring", "Error during cleanup: ${e.message}", e)
        }
    }

    suspend fun getFutureRecurringTransactions(
        originalTransactionId: Int,
        title: String,
        accountId: Int,
        amount: Double,
        transactionType: TransactionType,
        currentDate: Long
    ): List<TransactionEntity> {
        return transactionDao.getAllTransactions().filter { transaction ->
            transaction.id != originalTransactionId &&
                    transaction.title == title &&
                    transaction.accountId == accountId &&
                    transaction.amount == amount &&
                    transaction.transactionType == transactionType &&
                    transaction.date > currentDate &&
                    !transaction.isPaid
        }
    }

    suspend fun updateFutureRecurringTransactions(
        originalTransaction: TransactionEntity,
        newRecurrence: Recurrence
    ) {
        try {
            Log.d("RecurrenceUpdate", "Updating future recurring transactions for ${originalTransaction.title}")

            val futureTransactions = getFutureRecurringTransactions(
                originalTransaction.id,
                originalTransaction.title,
                originalTransaction.accountId,
                originalTransaction.amount,
                originalTransaction.transactionType,
                originalTransaction.date
            )

            Log.d("RecurrenceUpdate", "Found ${futureTransactions.size} future transactions to update")

            futureTransactions.forEach { transaction ->
                Log.d("RecurrenceUpdate", "Deleting future transaction with date: ${transaction.date}")
                deleteTransaction(transaction)
            }

            generateUpdatedRecurringSequence(originalTransaction, newRecurrence)

        } catch (e: Exception) {
            Log.e("RecurrenceUpdate", "Error updating future recurring transactions: ${e.message}", e)
            throw e
        }
    }

    private suspend fun generateUpdatedRecurringSequence(
        originalTransaction: TransactionEntity,
        newRecurrence: Recurrence
    ) {
        try {
            val updatedOriginalTransaction = originalTransaction.copy(recurrence = newRecurrence)
            var currentDate = originalTransaction.date
            var sequenceNumber = 1

            while (true) {
                val nextDueDate = getNextDueDate(
                    currentDate,
                    newRecurrence.frequency,
                    newRecurrence.interval,
                    newRecurrence.endRecurrenceDate ?: Long.MAX_VALUE
                )

                if (nextDueDate == null) {
                    Log.d("RecurrenceUpdate", "Reached end of recurrence sequence")
                    break
                }

                val followingDueDate = getNextDueDate(
                    nextDueDate,
                    newRecurrence.frequency,
                    newRecurrence.interval,
                    newRecurrence.endRecurrenceDate ?: Long.MAX_VALUE
                )

                val newTransaction = TransactionEntity(
                    id = 0,
                    title = originalTransaction.title,
                    amount = originalTransaction.amount,
                    date = nextDueDate,
                    time = originalTransaction.time,
                    recurrence = newRecurrence,
                    categoryId = originalTransaction.categoryId,
                    subCategoryId = originalTransaction.subCategoryId,
                    accountId = originalTransaction.accountId,
                    note = originalTransaction.note,
                    mode = originalTransaction.mode,
                    transactionType = originalTransaction.transactionType,
                    isPaid = false,
                    isCollected = false,
                    isSettled = false,
                    nextDueDate = followingDueDate,
                    destinationAccountId = originalTransaction.destinationAccountId,
                    originalCurrencyCode = originalTransaction.originalCurrencyCode,
                    endDate = originalTransaction.endDate
                )

                addTransaction(newTransaction)
                Log.d("RecurrenceUpdate", "Created new recurring transaction #$sequenceNumber for date: $nextDueDate")

                currentDate = nextDueDate
                sequenceNumber++

                if (sequenceNumber > 1000) {
                    Log.w("RecurrenceUpdate", "Stopping sequence generation at 1000 transactions to prevent infinite loop")
                    break
                }
            }

            Log.d("RecurrenceUpdate", "Generated $sequenceNumber new recurring transactions")

        } catch (e: Exception) {
            Log.e("RecurrenceUpdate", "Error generating updated recurring sequence: ${e.message}", e)
            throw e
        }
    }


    suspend fun updateTransactionCurrency(transactionId: Int, newCurrencyCode: String) {
        transactionDao.updateTransactionCurrency(transactionId, newCurrencyCode)
    }

    suspend fun updateTransactionCurrencies(accountId: Int, newCurrencyCode: String) {
        try {
            val accountTransactions = transactionDao.getTransactionsByAccountId(accountId)

            Log.d("TransactionRepository", "Updating currency for ${accountTransactions.size} transactions from account $accountId to $newCurrencyCode")

            accountTransactions.forEach { transaction ->
                Log.d("TransactionRepository", "Updating transaction ${transaction.id} from ${transaction.originalCurrencyCode} to $newCurrencyCode")
                val updatedTransaction = transaction.copy(originalCurrencyCode = newCurrencyCode)
                transactionDao.updateTransaction(updatedTransaction)
            }

            Log.d("TransactionRepository", "Successfully updated currencies for ${accountTransactions.size} transactions")
        } catch (e: Exception) {
            Log.e("TransactionRepository", "Error updating transaction currencies", e)
            throw e
        }
    }
    suspend fun updateTransactionsSubCategory(sourceSubCategoryId: Int, targetSubCategoryId: Int) {
        try {
            Log.d("TransactionRepository", "Moving transactions from subcategory $sourceSubCategoryId to $targetSubCategoryId")

            // Get all transactions with the source subcategory
            val transactionsToUpdate = transactionDao.getTransactionsBySubCategoryId(sourceSubCategoryId)

            // Update each transaction to use the target subcategory
            transactionsToUpdate.forEach { transaction ->
                val updatedTransaction = transaction.copy(subCategoryId = targetSubCategoryId)
                transactionDao.updateTransaction(updatedTransaction)
            }

            Log.d("TransactionRepository", "Successfully moved ${transactionsToUpdate.size} transactions")
        } catch (e: Exception) {
            Log.e("TransactionRepository", "Error updating transactions for subcategory merge", e)
            throw e
        }
    }
    suspend fun updateTransactionsFromSubCategoryToCategory(subCategoryId: Int, newCategoryId: Int) {
        try {
            Log.d("TransactionRepository", "Moving transactions from subcategory $subCategoryId to category $newCategoryId")

            // Get all transactions with the subcategory
            val transactionsToUpdate = transactionDao.getTransactionsBySubCategoryId(subCategoryId)

            // Update each transaction to use the new main category and clear subcategory
            transactionsToUpdate.forEach { transaction ->
                val updatedTransaction = transaction.copy(
                    categoryId = newCategoryId,
                    subCategoryId = null // Clear subcategory since it's now a main category transaction
                )
                transactionDao.updateTransaction(updatedTransaction)
            }

            Log.d("TransactionRepository", "Successfully moved ${transactionsToUpdate.size} transactions to new main category")
        } catch (e: Exception) {
            Log.e("TransactionRepository", "Error updating transactions for convert to main category", e)
            throw e
        }
    }
    // NEW: Update transactions when subcategories are migrated between categories
    suspend fun updateTransactionsForMigratedSubCategories(
        oldSubCategoryIds: List<Int>,
        newSubCategoryIds: List<Int>,
        targetCategoryId: Int
    ) {
        try {
            Log.d("TransactionRepository", "Updating transactions for migrated subcategories")

            // Create mapping of old to new subcategory IDs
            val subCategoryMapping = oldSubCategoryIds.zip(newSubCategoryIds).toMap()

            // Get all transactions that reference the old subcategories
            val transactionsToUpdate = transactionDao.getAllTransactions()
                .filter { it.subCategoryId in oldSubCategoryIds }

            Log.d("TransactionRepository", "Found ${transactionsToUpdate.size} transactions to update for subcategory migration")

            // Update each transaction with new category and subcategory IDs
            transactionsToUpdate.forEach { transaction ->
                val newSubCategoryId = subCategoryMapping[transaction.subCategoryId]
                if (newSubCategoryId != null) {
                    val updatedTransaction = transaction.copy(
                        categoryId = targetCategoryId,
                        subCategoryId = newSubCategoryId
                    )
                    transactionDao.updateTransaction(updatedTransaction)
                }
            }

            Log.d("TransactionRepository", "Successfully updated transactions for migrated subcategories")
        } catch (e: Exception) {
            Log.e("TransactionRepository", "Error updating transactions for migrated subcategories", e)
            throw e
        }
    }


    // Migrate all transactions from one category to another (including subcategories)
    suspend fun migrateTransactionsToCategory(sourceCategoryId: Int, targetCategoryId: Int) {
        try {
            Log.d("TransactionRepository", "Migrating transactions from category $sourceCategoryId to $targetCategoryId")

            // Get all transactions with the source category (including those with subcategories)
            val transactionsToMigrate = transactionDao.getAllTransactions()
                .filter { it.categoryId == sourceCategoryId }

            Log.d("TransactionRepository", "Found ${transactionsToMigrate.size} transactions to migrate")

            // Update each transaction to use the target category
            // Note: We'll update subcategoryId separately when migrating subcategories
            transactionsToMigrate.forEach { transaction ->
                val migratedTransaction = transaction.copy(
                    categoryId = targetCategoryId
                    // Keep the subCategoryId for now - it will be updated during subcategory migration
                )
                transactionDao.updateTransaction(migratedTransaction)
            }

            Log.d("TransactionRepository", "Successfully migrated ${transactionsToMigrate.size} transactions")
        } catch (e: Exception) {
            Log.e("TransactionRepository", "Error migrating transactions to category", e)
            throw e
        }
    }

    // Get transaction count for a specific category (including subcategories)
    suspend fun getTransactionCountByCategoryId(categoryId: Int): Int {
        return transactionDao.getAllTransactions()
            .count { it.categoryId == categoryId }
    }

    // Delete all transactions associated with a category
    suspend fun deleteTransactionsByCategoryId(categoryId: Int) {
        try {
            Log.d("TransactionRepository", "Deleting all transactions for category $categoryId")

            val transactionsToDelete = transactionDao.getAllTransactions()
                .filter { it.categoryId == categoryId }

            Log.d("TransactionRepository", "Found ${transactionsToDelete.size} transactions to delete")

            transactionsToDelete.forEach { transaction ->
                transactionDao.deleteTransaction(transaction)
            }

            Log.d("TransactionRepository", "Successfully deleted ${transactionsToDelete.size} transactions")
        } catch (e: Exception) {
            Log.e("TransactionRepository", "Error deleting transactions for category", e)
            throw e
        }
    }

    // Delete all transactions associated with a subcategory
    suspend fun deleteTransactionsBySubCategoryId(subCategoryId: Int) {
        try {
            Log.d("TransactionRepository", "Deleting all transactions for subcategory $subCategoryId")

            val transactionsToDelete = transactionDao.getTransactionsBySubCategoryId(subCategoryId)

            Log.d("TransactionRepository", "Found ${transactionsToDelete.size} transactions to delete")

            transactionsToDelete.forEach { transaction ->
                transactionDao.deleteTransaction(transaction)
            }

            Log.d("TransactionRepository", "Successfully deleted ${transactionsToDelete.size} transactions")
        } catch (e: Exception) {
            Log.e("TransactionRepository", "Error deleting transactions for subcategory", e)
            throw e
        }
    }

    suspend fun deleteTransactionsByCategoryIdWithBalanceRestore(
        categoryId: Int,
        accountRepository: AccountRepository
    ) {
        try {
            Log.d("TransactionRepository", "Deleting transactions for category $categoryId with balance restoration")

            val transactionsToDelete = transactionDao.getAllTransactions()
                .filter { it.categoryId == categoryId }

            Log.d("TransactionRepository", "Found ${transactionsToDelete.size} transactions to delete and restore balances")

            transactionsToDelete.forEach { transaction ->
                // Restore account balance before deleting transaction
                restoreAccountBalanceForTransaction(transaction, accountRepository)

                // Delete the transaction
                transactionDao.deleteTransaction(transaction)
            }

            Log.d("TransactionRepository", "Successfully deleted ${transactionsToDelete.size} transactions with balance restoration")
        } catch (e: Exception) {
            Log.e("TransactionRepository", "Error deleting transactions with balance restoration", e)
            throw e
        }
    }

    // NEW: Delete transactions by subcategory with account balance restoration
    suspend fun deleteTransactionsBySubCategoryIdWithBalanceRestore(
        subCategoryId: Int,
        accountRepository: AccountRepository
    ) {
        try {
            Log.d("TransactionRepository", "Deleting transactions for subcategory $subCategoryId with balance restoration")

            val transactionsToDelete = transactionDao.getTransactionsBySubCategoryId(subCategoryId)

            Log.d("TransactionRepository", "Found ${transactionsToDelete.size} transactions to delete and restore balances")

            transactionsToDelete.forEach { transaction ->
                // Restore account balance before deleting transaction
                restoreAccountBalanceForTransaction(transaction, accountRepository)

                // Delete the transaction
                transactionDao.deleteTransaction(transaction)
            }

            Log.d("TransactionRepository", "Successfully deleted ${transactionsToDelete.size} transactions with balance restoration")
        } catch (e: Exception) {
            Log.e("TransactionRepository", "Error deleting transactions for subcategory with balance restoration", e)
            throw e
        }
    }

    // NEW: Restore account balance for a specific transaction
    private suspend fun restoreAccountBalanceForTransaction(
        transaction: TransactionEntity,
        accountRepository: AccountRepository
    ) {
        try {
            Log.d("BalanceRestore", "Restoring balance for transaction: ${transaction.title} (${transaction.mode}, ${transaction.transactionType})")

            when (transaction.transactionType) {
                TransactionType.DEFAULT -> {
                    handleDefaultTransactionRestore(transaction, accountRepository)
                }

                TransactionType.UPCOMING -> {
                    handleUpcomingTransactionRestore(transaction, accountRepository)
                }

                TransactionType.SUBSCRIPTION, TransactionType.REPETITIVE -> {
                    handleRecurringTransactionRestore(transaction, accountRepository)
                }

                TransactionType.LENT -> {
                    handleLentTransactionRestore(transaction, accountRepository)
                }

                TransactionType.BORROWED -> {
                    handleBorrowedTransactionRestore(transaction, accountRepository)
                }
            }

        } catch (e: Exception) {
            Log.e("BalanceRestore", "Error restoring balance for transaction ${transaction.id}: ${e.message}", e)
            throw e
        }
    }
    // Handle default transaction balance restoration
    private suspend fun handleDefaultTransactionRestore(
        transaction: TransactionEntity,
        accountRepository: AccountRepository
    ) {
        when (transaction.mode) {
            "Expense" -> {
                // For expenses: add money back to account (reverse the expense)
                accountRepository.updateAccountBalance(
                    transaction.accountId,
                    transaction.amount,
                    isExpense = false
                )
                Log.d("BalanceRestore", "Restored expense: +${transaction.amount} to account ${transaction.accountId}")
            }

            "Income" -> {
                // For income: subtract money from account (reverse the income)
                accountRepository.updateAccountBalance(
                    transaction.accountId,
                    transaction.amount,
                    isExpense = true
                )
                Log.d("BalanceRestore", "Restored income: -${transaction.amount} from account ${transaction.accountId}")
            }

            "Transfer" -> {
                // For transfers: reverse the transfer
                transaction.destinationAccountId?.let { destAccountId ->
                    // Add money back to source account
                    accountRepository.updateAccountBalance(
                        transaction.accountId,
                        transaction.amount,
                        isExpense = false
                    )

                    // Remove money from destination account
                    accountRepository.updateAccountBalance(
                        destAccountId,
                        transaction.amount,
                        isExpense = true
                    )

                    Log.d("BalanceRestore", "Restored transfer: +${transaction.amount} to account ${transaction.accountId}, -${transaction.amount} from account $destAccountId")
                }
            }
        }
    }

    // Handle upcoming transaction balance restoration
    private suspend fun handleUpcomingTransactionRestore(
        transaction: TransactionEntity,
        accountRepository: AccountRepository
    ) {
        // Only restore balance if the transaction was marked as paid
        if (transaction.isPaid) {
            when (transaction.mode) {
                "Expense" -> {
                    accountRepository.updateAccountBalance(
                        transaction.accountId,
                        transaction.amount,
                        isExpense = false
                    )
                    Log.d("BalanceRestore", "Restored paid upcoming expense: +${transaction.amount}")
                }

                "Income" -> {
                    accountRepository.updateAccountBalance(
                        transaction.accountId,
                        transaction.amount,
                        isExpense = true
                    )
                    Log.d("BalanceRestore", "Restored paid upcoming income: -${transaction.amount}")
                }
            }
        } else {
            Log.d("BalanceRestore", "Upcoming transaction was not paid, no balance restoration needed")
        }
    }

    // Handle recurring transaction balance restoration
    private suspend fun handleRecurringTransactionRestore(
        transaction: TransactionEntity,
        accountRepository: AccountRepository
    ) {
        // Only restore balance if the transaction was marked as paid
        if (transaction.isPaid) {
            when (transaction.mode) {
                "Expense" -> {
                    accountRepository.updateAccountBalance(
                        transaction.accountId,
                        transaction.amount,
                        isExpense = false
                    )
                    Log.d("BalanceRestore", "Restored paid recurring expense: +${transaction.amount}")
                }

                "Income" -> {
                    accountRepository.updateAccountBalance(
                        transaction.accountId,
                        transaction.amount,
                        isExpense = true
                    )
                    Log.d("BalanceRestore", "Restored paid recurring income: -${transaction.amount}")
                }
            }
        } else {
            Log.d("BalanceRestore", "Recurring transaction was not paid, no balance restoration needed")
        }
    }

    // Handle lent transaction balance restoration
    private suspend fun handleLentTransactionRestore(
        transaction: TransactionEntity,
        accountRepository: AccountRepository
    ) {
        if (transaction.isCollected) {
            // If money was collected, remove it from account (reverse the collection)
            accountRepository.updateAccountBalance(
                transaction.accountId,
                transaction.amount,
                isExpense = true
            )
            Log.d("BalanceRestore", "Restored collected lent money: -${transaction.amount}")
        } else {
            // If money was not collected, add it back to account (reverse the lending)
            accountRepository.updateAccountBalance(
                transaction.accountId,
                transaction.amount,
                isExpense = false
            )
            Log.d("BalanceRestore", "Restored uncollected lent money: +${transaction.amount}")
        }
    }

    // Handle borrowed transaction balance restoration
    private suspend fun handleBorrowedTransactionRestore(
        transaction: TransactionEntity,
        accountRepository: AccountRepository
    ) {
        if (transaction.isSettled) {
            // If debt was settled, add money back to account (reverse the settlement)
            accountRepository.updateAccountBalance(
                transaction.accountId,
                transaction.amount,
                isExpense = false
            )
            Log.d("BalanceRestore", "Restored settled borrowed money: +${transaction.amount}")
        } else {
            // If debt was not settled, remove money from account (reverse the borrowing)
            accountRepository.updateAccountBalance(
                transaction.accountId,
                transaction.amount,
                isExpense = true
            )
            Log.d("BalanceRestore", "Restored unsettled borrowed money: -${transaction.amount}")
        }
    }

    // NEW: Get transaction count and total amount for category (for UI display)
    suspend fun getTransactionStatsForCategory(categoryId: Int): TransactionStats {
        val transactions = transactionDao.getAllTransactions()
            .filter { it.categoryId == categoryId }

        val totalAmount = transactions.sumOf { it.amount }
        val expenseAmount = transactions.filter { it.mode == "Expense" }.sumOf { it.amount }
        val incomeAmount = transactions.filter { it.mode == "Income" }.sumOf { it.amount }
        val transferAmount = transactions.filter { it.mode == "Transfer" }.sumOf { it.amount }

        return TransactionStats(
            count = transactions.size,
            totalAmount = totalAmount,
            expenseAmount = expenseAmount,
            incomeAmount = incomeAmount,
            transferAmount = transferAmount
        )
    }

    // NEW: Get transaction count and total amount for subcategory (for UI display)
    suspend fun getTransactionStatsForSubCategory(subCategoryId: Int): TransactionStats {
        val transactions = transactionDao.getTransactionsBySubCategoryId(subCategoryId)

        val totalAmount = transactions.sumOf { it.amount }
        val expenseAmount = transactions.filter { it.mode == "Expense" }.sumOf { it.amount }
        val incomeAmount = transactions.filter { it.mode == "Income" }.sumOf { it.amount }
        val transferAmount = transactions.filter { it.mode == "Transfer" }.sumOf { it.amount }

        return TransactionStats(
            count = transactions.size,
            totalAmount = totalAmount,
            expenseAmount = expenseAmount,
            incomeAmount = incomeAmount,
            transferAmount = transferAmount
        )
    }
}

data class TransactionStats(
    val count: Int,
    val totalAmount: Double,
    val expenseAmount: Double,
    val incomeAmount: Double,
    val transferAmount: Double
)

//class TransactionRepository(private val transactionDao: TransactionDao) {
//
//    suspend fun addTransaction(transaction: TransactionEntity) {
//        try {
//            Log.d("TransferDebug", "Starting repository.addTransaction")
//            // Existing code to add transaction
//            val id = transactionDao.insertTransaction(transaction)
//            Log.d("TransferDebug", "Transaction inserted with ID: $id")
//        } catch (e: Exception) {
//            Log.e("TransferDebug", "Error adding transaction: ${e.message}", e)
//            throw e  // Re-throw to preserve the exception
//        }
//    }
//    // Update an existing transaction
//    suspend fun updateTransaction(transaction: TransactionEntity) {
//        transactionDao.updateTransaction(transaction)
//    }
//
//    // Delete a transaction
//    suspend fun deleteTransaction(transaction: TransactionEntity) {
//        transactionDao.deleteTransaction(transaction)
//    }
//
//    suspend fun deleteTransactionById(id: Int) {
//        transactionDao.deleteTransactionById(id)
//    }
//
//    suspend fun getTransactionById(id: Int): TransactionEntity? {
//        return transactionDao.getTransactionById(id)
//    }
//
//    // Get all transactions
//    suspend fun getAllTransactions(): List<TransactionEntity> {
//        return transactionDao.getAllTransactions()
//    }
//
//    // Get transactions by type (Expense, Income, etc.)
//    suspend fun getAllTransactionsByMode(mode: String): List<TransactionEntity> {
//        return transactionDao.getAllTransactionsByMode(mode)
//    }
//
//
//
//    // Get unpaid transactions (UPCOMING)
//    suspend fun getUnpaidTransactions(): List<TransactionEntity> {
//        return transactionDao.getUpcomingTransactions(TransactionType.UPCOMING.name)
//    }
//
//    // Get lent transactions (LENT)
//    suspend fun getLentTransactions(): List<TransactionEntity> {
//        return transactionDao.getLentTransactions(TransactionType.LENT.name)
//    }
//
//    // Get borrowed transactions (BORROWED)
//    suspend fun getBorrowedTransactions(): List<TransactionEntity> {
//        return transactionDao.getBorrowedTransactions(TransactionType.BORROWED.name)
//    }
//
//    // Get subscription transactions
//    suspend fun getSubscriptionTransactions(): List<TransactionEntity> {
//        return transactionDao.getSubscriptionTransactions(TransactionType.SUBSCRIPTION)
//    }
//
//    // Get repetitive transactions
//    suspend fun getRepetitiveTransactions(): List<TransactionEntity> {
//        return transactionDao.getRepetitiveTransactions(TransactionType.REPETITIVE)
//    }
//
//    suspend fun getTransactionsByAccountId(accountId: Int): List<TransactionEntity> {
//        return transactionDao.getTransactionsByAccountId(accountId)
//    }
//    suspend fun updateTransactionAmount(transactionId: Int, newAmount: Double) {
//        transactionDao.updateTransactionAmount(transactionId, newAmount)
//    }
//    suspend fun getTransactionCountByAccountId(accountId: Int): Int {
//        return transactionDao.getTransactionCountByAccountId(accountId)
//    }
//
//    // Handle cash flow based on transaction type
//    fun handleTransactionCashFlow(transaction: TransactionEntity): Double {
//        return when (transaction.transactionType) {
//            TransactionType.DEFAULT -> transaction.amount
//            TransactionType.UPCOMING -> if (transaction.isPaid) transaction.amount else 0.0
//            TransactionType.LENT -> if (transaction.isCollected) transaction.amount else -transaction.amount
//            TransactionType.BORROWED -> if (transaction.isSettled) 0.0 else transaction.amount
//            else -> 0.0
//        }
//    }
//
//    // Update transaction status based on transaction type
//    fun updateTransactionStatus(transaction: TransactionEntity): TransactionEntity {
//        return when (transaction.transactionType) {
//            TransactionType.UPCOMING -> transaction.copy(isPaid = !transaction.isPaid) // Toggle paid status
//            TransactionType.LENT -> transaction.copy(isCollected = !transaction.isCollected) // Toggle collected status
//            TransactionType.BORROWED -> transaction.copy(isSettled = !transaction.isSettled) // Toggle settled status
//            TransactionType.SUBSCRIPTION, TransactionType.REPETITIVE -> transaction.copy(isPaid = !transaction.isPaid) // Toggle paid status
//            else -> transaction
//        }
//    }
//
//    // Mark a transaction as Paid
//    suspend fun markTransactionAsPaid(transaction: TransactionEntity) {
//        val updatedTransaction = transaction.copy(isPaid = true)
//        transactionDao.updateTransaction(updatedTransaction)
//    }
//
//    // Mark a lent transaction as collected
//    suspend fun markTransactionAsCollected(transaction: TransactionEntity) {
//        val updatedTransaction = transaction.copy(isCollected = true)
//        transactionDao.updateTransaction(updatedTransaction)
//    }
//
//    // Mark a borrowed transaction as settled
//    suspend fun markTransactionAsSettled(transaction: TransactionEntity) {
//        val updatedTransaction = transaction.copy(isSettled = true)
//        transactionDao.updateTransaction(updatedTransaction)
//    }
//
//    // Mark a transaction as unpaid (reverse of markTransactionAsPaid)
//    suspend fun markTransactionAsUnpaid(transaction: TransactionEntity) {
//        val updatedTransaction = transaction.copy(isPaid = false)
//        transactionDao.updateTransaction(updatedTransaction)
//    }
//
//    // Mark a lent transaction as not collected (reverse of markTransactionAsCollected)
//    suspend fun markTransactionAsNotCollected(transaction: TransactionEntity) {
//        val updatedTransaction = transaction.copy(isCollected = false)
//        transactionDao.updateTransaction(updatedTransaction)
//    }
//
//    // Mark a borrowed transaction as unsettled (reverse of markTransactionAsSettled)
//    suspend fun markTransactionAsUnsettled(transaction: TransactionEntity) {
//        val updatedTransaction = transaction.copy(isSettled = false)
//        transactionDao.updateTransaction(updatedTransaction)
//    }
//
//    // Generate the next recurring transaction (for subscriptions/repetitive transactions)
//    suspend fun generateNextRecurringTransaction(transaction: TransactionEntity) {
//        try {
//            val recurrence = transaction.recurrence
//            if (recurrence == null || recurrence.frequency == RecurrenceFrequency.NONE) {
//                Log.d("RecurringTransaction", "No recurrence found for transaction ${transaction.id}")
//                return
//            }
//
//            // Calculate the next due date from the current transaction's date
//            val nextDueDate = getNextDueDate(
//                transaction.date,
//                recurrence.frequency,
//                recurrence.interval,
//                recurrence.endRecurrenceDate ?: Long.MAX_VALUE
//            )
//
//            if (nextDueDate != null) {
//                // Calculate the due date after the next one (for the new transaction's nextDueDate)
//                val followingDueDate = getNextDueDate(
//                    nextDueDate,
//                    recurrence.frequency,
//                    recurrence.interval,
//                    recurrence.endRecurrenceDate ?: Long.MAX_VALUE
//                )
//
//                // Create a completely new transaction (not a copy with modified fields)
//                val newTransaction = TransactionEntity(
//                    id = 0, // Let database generate new ID
//                    title = transaction.title,
//                    amount = transaction.amount,
//                    date = nextDueDate,
//                    time = transaction.time, // Keep same time of day
//                    recurrence = recurrence, // Keep the same recurrence pattern
//                    categoryId = transaction.categoryId,
//                    subCategoryId = transaction.subCategoryId,
//                    accountId = transaction.accountId,
//                    note = transaction.note,
//                    mode = transaction.mode,
//                    transactionType = transaction.transactionType,
//                    isPaid = false, // New recurring transaction starts as unpaid
//                    isCollected = false,
//                    isSettled = false,
//                    nextDueDate = followingDueDate, // Set the next due date for this new transaction
//                    destinationAccountId = transaction.destinationAccountId,
//                    originalCurrencyCode = transaction.originalCurrencyCode,
//                    endDate = transaction.endDate
//                )
//
//                // Insert the new recurring transaction
//                addTransaction(newTransaction)
//
//                Log.d("RecurringTransaction", "Created next recurring transaction for ${transaction.title} on date: $nextDueDate")
//            } else {
//                Log.d("RecurringTransaction", "No more recurring transactions to create for ${transaction.title} - end date reached")
//            }
//        } catch (e: Exception) {
//            Log.e("RecurringTransaction", "Error generating next recurring transaction: ${e.message}", e)
//            throw e
//        }
//    }
//
//    // NEW: Find future transactions generated from a specific transaction
//    suspend fun getFutureRecurringTransactions(
//        originalTransactionId: Int,
//        title: String,
//        accountId: Int,
//        amount: Double,
//        transactionType: TransactionType,
//        currentDate: Long
//    ): List<TransactionEntity> {
//        return transactionDao.getAllTransactions().filter { transaction ->
//            transaction.id != originalTransactionId &&
//                    transaction.title == title &&
//                    transaction.accountId == accountId &&
//                    transaction.amount == amount &&
//                    transaction.transactionType == transactionType &&
//                    transaction.date > currentDate &&
//                    !transaction.isPaid // Only unpaid future transactions
//        }
//    }
//
//    // NEW: Update future recurring transactions with new recurrence settings
//    suspend fun updateFutureRecurringTransactions(
//        originalTransaction: TransactionEntity,
//        newRecurrence: Recurrence
//    ) {
//        try {
//            Log.d("RecurrenceUpdate", "Updating future recurring transactions for ${originalTransaction.title}")
//
//            // Find all future transactions generated from this original transaction
//            val futureTransactions = getFutureRecurringTransactions(
//                originalTransaction.id,
//                originalTransaction.title,
//                originalTransaction.accountId,
//                originalTransaction.amount,
//                originalTransaction.transactionType,
//                originalTransaction.date
//            )
//
//            Log.d("RecurrenceUpdate", "Found ${futureTransactions.size} future transactions to update")
//
//            // Delete existing future transactions
//            futureTransactions.forEach { transaction ->
//                Log.d("RecurrenceUpdate", "Deleting future transaction with date: ${transaction.date}")
//                deleteTransaction(transaction)
//            }
//
//            // Generate new sequence of transactions with updated recurrence settings
//            generateUpdatedRecurringSequence(originalTransaction, newRecurrence)
//
//        } catch (e: Exception) {
//            Log.e("RecurrenceUpdate", "Error updating future recurring transactions: ${e.message}", e)
//            throw e
//        }
//    }
//
//    // NEW: Generate updated recurring transaction sequence
//    private suspend fun generateUpdatedRecurringSequence(
//        originalTransaction: TransactionEntity,
//        newRecurrence: Recurrence
//    ) {
//        try {
//            val updatedOriginalTransaction = originalTransaction.copy(recurrence = newRecurrence)
//            var currentDate = originalTransaction.date
//            var sequenceNumber = 1
//
//            // Generate new transactions until end date
//            while (true) {
//                val nextDueDate = getNextDueDate(
//                    currentDate,
//                    newRecurrence.frequency,
//                    newRecurrence.interval,
//                    newRecurrence.endRecurrenceDate ?: Long.MAX_VALUE
//                )
//
//                if (nextDueDate == null) {
//                    Log.d("RecurrenceUpdate", "Reached end of recurrence sequence")
//                    break
//                }
//
//                // Calculate the due date after this next one
//                val followingDueDate = getNextDueDate(
//                    nextDueDate,
//                    newRecurrence.frequency,
//                    newRecurrence.interval,
//                    newRecurrence.endRecurrenceDate ?: Long.MAX_VALUE
//                )
//
//                // Create new recurring transaction
//                val newTransaction = TransactionEntity(
//                    id = 0, // Let database generate new ID
//                    title = originalTransaction.title,
//                    amount = originalTransaction.amount,
//                    date = nextDueDate,
//                    time = originalTransaction.time,
//                    recurrence = newRecurrence,
//                    categoryId = originalTransaction.categoryId,
//                    subCategoryId = originalTransaction.subCategoryId,
//                    accountId = originalTransaction.accountId,
//                    note = originalTransaction.note,
//                    mode = originalTransaction.mode,
//                    transactionType = originalTransaction.transactionType,
//                    isPaid = false, // New recurring transaction starts as unpaid
//                    isCollected = false,
//                    isSettled = false,
//                    nextDueDate = followingDueDate,
//                    destinationAccountId = originalTransaction.destinationAccountId,
//                    originalCurrencyCode = originalTransaction.originalCurrencyCode,
//                    endDate = originalTransaction.endDate
//                )
//
//                addTransaction(newTransaction)
//                Log.d("RecurrenceUpdate", "Created new recurring transaction #$sequenceNumber for date: $nextDueDate")
//
//                currentDate = nextDueDate
//                sequenceNumber++
//
//                // Safety check to prevent infinite loops
//                if (sequenceNumber > 1000) {
//                    Log.w("RecurrenceUpdate", "Stopping sequence generation at 1000 transactions to prevent infinite loop")
//                    break
//                }
//            }
//
//            Log.d("RecurrenceUpdate", "Generated $sequenceNumber new recurring transactions")
//
//        } catch (e: Exception) {
//            Log.e("RecurrenceUpdate", "Error generating updated recurring sequence: ${e.message}", e)
//            throw e
//        }
//    }
//
//
//    suspend fun updateTransactionCurrency(transactionId: Int, newCurrencyCode: String) {
//        transactionDao.updateTransactionCurrency(transactionId, newCurrencyCode)
//    }
//
//    suspend fun updateTransactionCurrencies(accountId: Int, newCurrencyCode: String) {
//        try {
//            // Get all transactions for this account
//            val accountTransactions = transactionDao.getTransactionsByAccountId(accountId)
//
//            // Log for debugging
//            Log.d("TransactionRepository", "Updating currency for ${accountTransactions.size} transactions from account $accountId to $newCurrencyCode")
//
//            // Update their currency codes
//            accountTransactions.forEach { transaction ->
//                Log.d("TransactionRepository", "Updating transaction ${transaction.id} from ${transaction.originalCurrencyCode} to $newCurrencyCode")
//                val updatedTransaction = transaction.copy(originalCurrencyCode = newCurrencyCode)
//                transactionDao.updateTransaction(updatedTransaction)
//            }
//
//            Log.d("TransactionRepository", "Successfully updated currencies for ${accountTransactions.size} transactions")
//        } catch (e: Exception) {
//            Log.e("TransactionRepository", "Error updating transaction currencies", e)
//            throw e
//        }
//    }
//    suspend fun updateTransactionsSubCategory(sourceSubCategoryId: Int, targetSubCategoryId: Int) {
//        try {
//            Log.d("TransactionRepository", "Moving transactions from subcategory $sourceSubCategoryId to $targetSubCategoryId")
//
//            // Get all transactions with the source subcategory
//            val transactionsToUpdate = transactionDao.getTransactionsBySubCategoryId(sourceSubCategoryId)
//
//            // Update each transaction to use the target subcategory
//            transactionsToUpdate.forEach { transaction ->
//                val updatedTransaction = transaction.copy(subCategoryId = targetSubCategoryId)
//                transactionDao.updateTransaction(updatedTransaction)
//            }
//
//            Log.d("TransactionRepository", "Successfully moved ${transactionsToUpdate.size} transactions")
//        } catch (e: Exception) {
//            Log.e("TransactionRepository", "Error updating transactions for subcategory merge", e)
//            throw e
//        }
//    }
//    suspend fun updateTransactionsFromSubCategoryToCategory(subCategoryId: Int, newCategoryId: Int) {
//        try {
//            Log.d("TransactionRepository", "Moving transactions from subcategory $subCategoryId to category $newCategoryId")
//
//            // Get all transactions with the subcategory
//            val transactionsToUpdate = transactionDao.getTransactionsBySubCategoryId(subCategoryId)
//
//            // Update each transaction to use the new main category and clear subcategory
//            transactionsToUpdate.forEach { transaction ->
//                val updatedTransaction = transaction.copy(
//                    categoryId = newCategoryId,
//                    subCategoryId = null // Clear subcategory since it's now a main category transaction
//                )
//                transactionDao.updateTransaction(updatedTransaction)
//            }
//
//            Log.d("TransactionRepository", "Successfully moved ${transactionsToUpdate.size} transactions to new main category")
//        } catch (e: Exception) {
//            Log.e("TransactionRepository", "Error updating transactions for convert to main category", e)
//            throw e
//        }
//    }
//    // NEW: Update transactions when subcategories are migrated between categories
//    suspend fun updateTransactionsForMigratedSubCategories(
//        oldSubCategoryIds: List<Int>,
//        newSubCategoryIds: List<Int>,
//        targetCategoryId: Int
//    ) {
//        try {
//            Log.d("TransactionRepository", "Updating transactions for migrated subcategories")
//
//            // Create mapping of old to new subcategory IDs
//            val subCategoryMapping = oldSubCategoryIds.zip(newSubCategoryIds).toMap()
//
//            // Get all transactions that reference the old subcategories
//            val transactionsToUpdate = transactionDao.getAllTransactions()
//                .filter { it.subCategoryId in oldSubCategoryIds }
//
//            Log.d("TransactionRepository", "Found ${transactionsToUpdate.size} transactions to update for subcategory migration")
//
//            // Update each transaction with new category and subcategory IDs
//            transactionsToUpdate.forEach { transaction ->
//                val newSubCategoryId = subCategoryMapping[transaction.subCategoryId]
//                if (newSubCategoryId != null) {
//                    val updatedTransaction = transaction.copy(
//                        categoryId = targetCategoryId,
//                        subCategoryId = newSubCategoryId
//                    )
//                    transactionDao.updateTransaction(updatedTransaction)
//                }
//            }
//
//            Log.d("TransactionRepository", "Successfully updated transactions for migrated subcategories")
//        } catch (e: Exception) {
//            Log.e("TransactionRepository", "Error updating transactions for migrated subcategories", e)
//            throw e
//        }
//    }
//
//
//    // Migrate all transactions from one category to another (including subcategories)
//    suspend fun migrateTransactionsToCategory(sourceCategoryId: Int, targetCategoryId: Int) {
//        try {
//            Log.d("TransactionRepository", "Migrating transactions from category $sourceCategoryId to $targetCategoryId")
//
//            // Get all transactions with the source category (including those with subcategories)
//            val transactionsToMigrate = transactionDao.getAllTransactions()
//                .filter { it.categoryId == sourceCategoryId }
//
//            Log.d("TransactionRepository", "Found ${transactionsToMigrate.size} transactions to migrate")
//
//            // Update each transaction to use the target category
//            // Note: We'll update subcategoryId separately when migrating subcategories
//            transactionsToMigrate.forEach { transaction ->
//                val migratedTransaction = transaction.copy(
//                    categoryId = targetCategoryId
//                    // Keep the subCategoryId for now - it will be updated during subcategory migration
//                )
//                transactionDao.updateTransaction(migratedTransaction)
//            }
//
//            Log.d("TransactionRepository", "Successfully migrated ${transactionsToMigrate.size} transactions")
//        } catch (e: Exception) {
//            Log.e("TransactionRepository", "Error migrating transactions to category", e)
//            throw e
//        }
//    }
//
//    // Get transaction count for a specific category (including subcategories)
//    suspend fun getTransactionCountByCategoryId(categoryId: Int): Int {
//        return transactionDao.getAllTransactions()
//            .count { it.categoryId == categoryId }
//    }
//
//    // Delete all transactions associated with a category
//    suspend fun deleteTransactionsByCategoryId(categoryId: Int) {
//        try {
//            Log.d("TransactionRepository", "Deleting all transactions for category $categoryId")
//
//            val transactionsToDelete = transactionDao.getAllTransactions()
//                .filter { it.categoryId == categoryId }
//
//            Log.d("TransactionRepository", "Found ${transactionsToDelete.size} transactions to delete")
//
//            transactionsToDelete.forEach { transaction ->
//                transactionDao.deleteTransaction(transaction)
//            }
//
//            Log.d("TransactionRepository", "Successfully deleted ${transactionsToDelete.size} transactions")
//        } catch (e: Exception) {
//            Log.e("TransactionRepository", "Error deleting transactions for category", e)
//            throw e
//        }
//    }
//
//    // Delete all transactions associated with a subcategory
//    suspend fun deleteTransactionsBySubCategoryId(subCategoryId: Int) {
//        try {
//            Log.d("TransactionRepository", "Deleting all transactions for subcategory $subCategoryId")
//
//            val transactionsToDelete = transactionDao.getTransactionsBySubCategoryId(subCategoryId)
//
//            Log.d("TransactionRepository", "Found ${transactionsToDelete.size} transactions to delete")
//
//            transactionsToDelete.forEach { transaction ->
//                transactionDao.deleteTransaction(transaction)
//            }
//
//            Log.d("TransactionRepository", "Successfully deleted ${transactionsToDelete.size} transactions")
//        } catch (e: Exception) {
//            Log.e("TransactionRepository", "Error deleting transactions for subcategory", e)
//            throw e
//        }
//    }
//
//    suspend fun deleteTransactionsByCategoryIdWithBalanceRestore(
//        categoryId: Int,
//        accountRepository: AccountRepository
//    ) {
//        try {
//            Log.d("TransactionRepository", "Deleting transactions for category $categoryId with balance restoration")
//
//            val transactionsToDelete = transactionDao.getAllTransactions()
//                .filter { it.categoryId == categoryId }
//
//            Log.d("TransactionRepository", "Found ${transactionsToDelete.size} transactions to delete and restore balances")
//
//            transactionsToDelete.forEach { transaction ->
//                // Restore account balance before deleting transaction
//                restoreAccountBalanceForTransaction(transaction, accountRepository)
//
//                // Delete the transaction
//                transactionDao.deleteTransaction(transaction)
//            }
//
//            Log.d("TransactionRepository", "Successfully deleted ${transactionsToDelete.size} transactions with balance restoration")
//        } catch (e: Exception) {
//            Log.e("TransactionRepository", "Error deleting transactions with balance restoration", e)
//            throw e
//        }
//    }
//
//    // NEW: Delete transactions by subcategory with account balance restoration
//    suspend fun deleteTransactionsBySubCategoryIdWithBalanceRestore(
//        subCategoryId: Int,
//        accountRepository: AccountRepository
//    ) {
//        try {
//            Log.d("TransactionRepository", "Deleting transactions for subcategory $subCategoryId with balance restoration")
//
//            val transactionsToDelete = transactionDao.getTransactionsBySubCategoryId(subCategoryId)
//
//            Log.d("TransactionRepository", "Found ${transactionsToDelete.size} transactions to delete and restore balances")
//
//            transactionsToDelete.forEach { transaction ->
//                // Restore account balance before deleting transaction
//                restoreAccountBalanceForTransaction(transaction, accountRepository)
//
//                // Delete the transaction
//                transactionDao.deleteTransaction(transaction)
//            }
//
//            Log.d("TransactionRepository", "Successfully deleted ${transactionsToDelete.size} transactions with balance restoration")
//        } catch (e: Exception) {
//            Log.e("TransactionRepository", "Error deleting transactions for subcategory with balance restoration", e)
//            throw e
//        }
//    }
//
//    // NEW: Restore account balance for a specific transaction
//    private suspend fun restoreAccountBalanceForTransaction(
//        transaction: TransactionEntity,
//        accountRepository: AccountRepository
//    ) {
//        try {
//            Log.d("BalanceRestore", "Restoring balance for transaction: ${transaction.title} (${transaction.mode}, ${transaction.transactionType})")
//
//            when (transaction.transactionType) {
//                TransactionType.DEFAULT -> {
//                    handleDefaultTransactionRestore(transaction, accountRepository)
//                }
//
//                TransactionType.UPCOMING -> {
//                    handleUpcomingTransactionRestore(transaction, accountRepository)
//                }
//
//                TransactionType.SUBSCRIPTION, TransactionType.REPETITIVE -> {
//                    handleRecurringTransactionRestore(transaction, accountRepository)
//                }
//
//                TransactionType.LENT -> {
//                    handleLentTransactionRestore(transaction, accountRepository)
//                }
//
//                TransactionType.BORROWED -> {
//                    handleBorrowedTransactionRestore(transaction, accountRepository)
//                }
//            }
//
//        } catch (e: Exception) {
//            Log.e("BalanceRestore", "Error restoring balance for transaction ${transaction.id}: ${e.message}", e)
//            throw e
//        }
//    }
//    // Handle default transaction balance restoration
//    private suspend fun handleDefaultTransactionRestore(
//        transaction: TransactionEntity,
//        accountRepository: AccountRepository
//    ) {
//        when (transaction.mode) {
//            "Expense" -> {
//                // For expenses: add money back to account (reverse the expense)
//                accountRepository.updateAccountBalance(
//                    transaction.accountId,
//                    transaction.amount,
//                    isExpense = false
//                )
//                Log.d("BalanceRestore", "Restored expense: +${transaction.amount} to account ${transaction.accountId}")
//            }
//
//            "Income" -> {
//                // For income: subtract money from account (reverse the income)
//                accountRepository.updateAccountBalance(
//                    transaction.accountId,
//                    transaction.amount,
//                    isExpense = true
//                )
//                Log.d("BalanceRestore", "Restored income: -${transaction.amount} from account ${transaction.accountId}")
//            }
//
//            "Transfer" -> {
//                // For transfers: reverse the transfer
//                transaction.destinationAccountId?.let { destAccountId ->
//                    // Add money back to source account
//                    accountRepository.updateAccountBalance(
//                        transaction.accountId,
//                        transaction.amount,
//                        isExpense = false
//                    )
//
//                    // Remove money from destination account
//                    accountRepository.updateAccountBalance(
//                        destAccountId,
//                        transaction.amount,
//                        isExpense = true
//                    )
//
//                    Log.d("BalanceRestore", "Restored transfer: +${transaction.amount} to account ${transaction.accountId}, -${transaction.amount} from account $destAccountId")
//                }
//            }
//        }
//    }
//
//    // Handle upcoming transaction balance restoration
//    private suspend fun handleUpcomingTransactionRestore(
//        transaction: TransactionEntity,
//        accountRepository: AccountRepository
//    ) {
//        // Only restore balance if the transaction was marked as paid
//        if (transaction.isPaid) {
//            when (transaction.mode) {
//                "Expense" -> {
//                    accountRepository.updateAccountBalance(
//                        transaction.accountId,
//                        transaction.amount,
//                        isExpense = false
//                    )
//                    Log.d("BalanceRestore", "Restored paid upcoming expense: +${transaction.amount}")
//                }
//
//                "Income" -> {
//                    accountRepository.updateAccountBalance(
//                        transaction.accountId,
//                        transaction.amount,
//                        isExpense = true
//                    )
//                    Log.d("BalanceRestore", "Restored paid upcoming income: -${transaction.amount}")
//                }
//            }
//        } else {
//            Log.d("BalanceRestore", "Upcoming transaction was not paid, no balance restoration needed")
//        }
//    }
//
//    // Handle recurring transaction balance restoration
//    private suspend fun handleRecurringTransactionRestore(
//        transaction: TransactionEntity,
//        accountRepository: AccountRepository
//    ) {
//        // Only restore balance if the transaction was marked as paid
//        if (transaction.isPaid) {
//            when (transaction.mode) {
//                "Expense" -> {
//                    accountRepository.updateAccountBalance(
//                        transaction.accountId,
//                        transaction.amount,
//                        isExpense = false
//                    )
//                    Log.d("BalanceRestore", "Restored paid recurring expense: +${transaction.amount}")
//                }
//
//                "Income" -> {
//                    accountRepository.updateAccountBalance(
//                        transaction.accountId,
//                        transaction.amount,
//                        isExpense = true
//                    )
//                    Log.d("BalanceRestore", "Restored paid recurring income: -${transaction.amount}")
//                }
//            }
//        } else {
//            Log.d("BalanceRestore", "Recurring transaction was not paid, no balance restoration needed")
//        }
//    }
//
//    // Handle lent transaction balance restoration
//    private suspend fun handleLentTransactionRestore(
//        transaction: TransactionEntity,
//        accountRepository: AccountRepository
//    ) {
//        if (transaction.isCollected) {
//            // If money was collected, remove it from account (reverse the collection)
//            accountRepository.updateAccountBalance(
//                transaction.accountId,
//                transaction.amount,
//                isExpense = true
//            )
//            Log.d("BalanceRestore", "Restored collected lent money: -${transaction.amount}")
//        } else {
//            // If money was not collected, add it back to account (reverse the lending)
//            accountRepository.updateAccountBalance(
//                transaction.accountId,
//                transaction.amount,
//                isExpense = false
//            )
//            Log.d("BalanceRestore", "Restored uncollected lent money: +${transaction.amount}")
//        }
//    }
//
//    // Handle borrowed transaction balance restoration
//    private suspend fun handleBorrowedTransactionRestore(
//        transaction: TransactionEntity,
//        accountRepository: AccountRepository
//    ) {
//        if (transaction.isSettled) {
//            // If debt was settled, add money back to account (reverse the settlement)
//            accountRepository.updateAccountBalance(
//                transaction.accountId,
//                transaction.amount,
//                isExpense = false
//            )
//            Log.d("BalanceRestore", "Restored settled borrowed money: +${transaction.amount}")
//        } else {
//            // If debt was not settled, remove money from account (reverse the borrowing)
//            accountRepository.updateAccountBalance(
//                transaction.accountId,
//                transaction.amount,
//                isExpense = true
//            )
//            Log.d("BalanceRestore", "Restored unsettled borrowed money: -${transaction.amount}")
//        }
//    }
//
//    // NEW: Get transaction count and total amount for category (for UI display)
//    suspend fun getTransactionStatsForCategory(categoryId: Int): TransactionStats {
//        val transactions = transactionDao.getAllTransactions()
//            .filter { it.categoryId == categoryId }
//
//        val totalAmount = transactions.sumOf { it.amount }
//        val expenseAmount = transactions.filter { it.mode == "Expense" }.sumOf { it.amount }
//        val incomeAmount = transactions.filter { it.mode == "Income" }.sumOf { it.amount }
//        val transferAmount = transactions.filter { it.mode == "Transfer" }.sumOf { it.amount }
//
//        return TransactionStats(
//            count = transactions.size,
//            totalAmount = totalAmount,
//            expenseAmount = expenseAmount,
//            incomeAmount = incomeAmount,
//            transferAmount = transferAmount
//        )
//    }
//
//    // NEW: Get transaction count and total amount for subcategory (for UI display)
//    suspend fun getTransactionStatsForSubCategory(subCategoryId: Int): TransactionStats {
//        val transactions = transactionDao.getTransactionsBySubCategoryId(subCategoryId)
//
//        val totalAmount = transactions.sumOf { it.amount }
//        val expenseAmount = transactions.filter { it.mode == "Expense" }.sumOf { it.amount }
//        val incomeAmount = transactions.filter { it.mode == "Income" }.sumOf { it.amount }
//        val transferAmount = transactions.filter { it.mode == "Transfer" }.sumOf { it.amount }
//
//        return TransactionStats(
//            count = transactions.size,
//            totalAmount = totalAmount,
//            expenseAmount = expenseAmount,
//            incomeAmount = incomeAmount,
//            transferAmount = transferAmount
//        )
//    }
//}
//
//data class TransactionStats(
//    val count: Int,
//    val totalAmount: Double,
//    val expenseAmount: Double,
//    val incomeAmount: Double,
//    val transferAmount: Double
//)