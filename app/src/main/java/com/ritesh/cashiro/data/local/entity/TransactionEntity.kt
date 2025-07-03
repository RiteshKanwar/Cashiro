package com.ritesh.cashiro.data.local.entity
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

import androidx.annotation.Keep
@Keep
data class Recurrence(
    val frequency: RecurrenceFrequency, // DAILY, WEEKLY, etc.
    val interval: Int = 1, // e.g., Every 1 day, 2 weeks, 3 months
    val endRecurrenceDate: Long? = null, // Optional end date for the recurrence
)
@Keep
@Entity(
    tableName = "transaction_entity",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["destinationAccountId"],
            onDelete = ForeignKey.SET_NULL,
            deferred = true
        ),
        ForeignKey(
            entity = SubCategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["subCategoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("categoryId"),
        Index("accountId"), // Added index for accountId
        Index("destinationAccountId"),
        Index("subCategoryId")
    ]
)
data class TransactionEntity(
    val amount: Double,
    val date: Long,
    val time: Long,
    val recurrence: Recurrence?, // Optional for repetitive/subscription types
    val categoryId: Int,
    val subCategoryId: Int? = null, // field: optional subcategory
    val accountId: Int,
    val destinationAccountId: Int? = null, // Only used for Transfer transactions
    val title: String,
    val note: String, // Description or note
    val mode: String, // "Expense" or "Income"
    val transactionType: TransactionType, // Enum for the transaction types
    val isPaid: Boolean = false, // Status for unpaid/upcoming transactions
    val isCollected: Boolean = false, // Status for Borrowed transactions
    val isSettled: Boolean = false, // Status for Lent transactions
    val nextDueDate: Long? = null, // Next due date for subscriptions or repetitive transactions
    val endDate: Long? = null, // Optional end date for recurring transactions
    var originalCurrencyCode: String? = null, // Original currency when the transaction was created
    val originalAmount: Double? = null, // Original amount in the transaction's currency

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
)
@Keep
enum class TransactionType {
    DEFAULT, // Normal transaction
    UPCOMING, // Unpaid transaction
    SUBSCRIPTION, // Recurring transaction (subscription)
    REPETITIVE, // Recurring transaction (repetitive)
    LENT, // Lent money
    BORROWED // Borrowed money
}
@Keep
enum class RecurrenceFrequency {
    NONE,DAILY, WEEKLY, MONTHLY, YEARLY
}
