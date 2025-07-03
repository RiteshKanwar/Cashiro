package com.ritesh.cashiro.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ritesh.cashiro.data.local.entity.TransactionEntity
import com.ritesh.cashiro.data.local.entity.TransactionType

@Dao
interface TransactionDao {

    // Existing queries
    @Query("SELECT * FROM transaction_entity")
    suspend fun getAllTransactions(): List<TransactionEntity>

    @Query("SELECT * FROM transaction_entity WHERE mode = :type")
    suspend fun getAllTransactionsByMode(type: String): List<TransactionEntity>

    // Query for UPCOMING transactions (unpaid)
    @Query("SELECT * FROM transaction_entity WHERE transactionType = :type AND isPaid = 0")
    suspend fun getUpcomingTransactions(type: String): List<TransactionEntity>

    // Query for LENT transactions (uncollected)
    @Query("SELECT * FROM transaction_entity WHERE transactionType = :type AND isCollected = 0")
    suspend fun getLentTransactions(type: String): List<TransactionEntity>

    // Query for BORROWED transactions (unsettled)
    @Query("SELECT * FROM transaction_entity WHERE transactionType = :type AND isSettled = 0")
    suspend fun getBorrowedTransactions(type: String): List<TransactionEntity>

    // Query for SUBSCRIPTION transactions
    @Query("SELECT * FROM transaction_entity WHERE transactionType = :type")
    suspend fun getSubscriptionTransactions(type: TransactionType): List<TransactionEntity>

    // Query for REPETITIVE transactions
    @Query("SELECT * FROM transaction_entity WHERE transactionType = :type")
    suspend fun getRepetitiveTransactions(type: TransactionType): List<TransactionEntity>

    // Insert a transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transactionEntity: TransactionEntity)

    // Update a transaction (e.g., to mark it as Paid, Collected, or Settled)
    @Update
    suspend fun updateTransaction(transactionEntity: TransactionEntity)

    // Delete a transaction
    @Delete
    suspend fun deleteTransaction(transactionEntity: TransactionEntity)

    @Query("SELECT * FROM transaction_entity WHERE id = :id")
    suspend fun getTransactionById(id: Int): TransactionEntity?

    @Query("DELETE FROM transaction_entity WHERE id = :id")
    suspend fun deleteTransactionById(id: Int)

    @Query("SELECT * FROM transaction_entity WHERE accountId = :accountId")
    suspend fun getTransactionsByAccountId(accountId: Int): List<TransactionEntity>

    @Query("UPDATE transaction_entity SET amount = :newAmount WHERE id = :transactionId")
    suspend fun updateTransactionAmount(transactionId: Int, newAmount: Double)

    @Query("SELECT COUNT(*) FROM transaction_entity WHERE accountId = :accountId")
    suspend fun getTransactionCountByAccountId(accountId: Int): Int

    @Query("SELECT * FROM transaction_entity WHERE subCategoryId = :subCategoryId")
    suspend fun getTransactionsBySubCategoryId(subCategoryId: Int): List<TransactionEntity>

    @Query("SELECT COUNT(*) FROM transaction_entity WHERE subCategoryId = :subCategoryId")
    suspend fun getTransactionCountBySubCategoryId(subCategoryId: Int): Int

    @Query("UPDATE transaction_entity SET originalCurrencyCode = :newCurrencyCode WHERE id = :transactionId")
    suspend fun updateTransactionCurrency(transactionId: Int, newCurrencyCode: String)


}