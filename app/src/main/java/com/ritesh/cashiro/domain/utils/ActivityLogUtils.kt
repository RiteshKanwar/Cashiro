package com.ritesh.cashiro.domain.utils

import android.util.Log
import com.ritesh.cashiro.data.currency.model.CurrencySymbols
import com.ritesh.cashiro.data.local.entity.AccountEntity
import com.ritesh.cashiro.data.local.entity.TransactionEntity
import com.ritesh.cashiro.data.local.entity.TransactionType
import com.ritesh.cashiro.domain.repository.ActivityLogRepository
import com.ritesh.cashiro.presentation.ui.features.activity_logs.ActivityActionType
import com.ritesh.cashiro.presentation.ui.features.activity_logs.ActivityLogEntry
import com.ritesh.cashiro.presentation.ui.features.activity_logs.ActivityLogFilterState
import com.ritesh.cashiro.presentation.ui.features.activity_logs.ActivityLogSortBy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActivityLogUtils @Inject constructor(
    private val activityLogRepository: ActivityLogRepository
) {
    private val activityScope = CoroutineScope(Dispatchers.IO)

    // Enhanced transaction logging with safe metadata
    fun logTransactionCreated(transaction: TransactionEntity, account: AccountEntity?) {
        val actionType = when (transaction.mode) {
            "Expense" -> ActivityActionType.TRANSACTION_ADDED_EXPENSE
            "Income" -> ActivityActionType.TRANSACTION_ADDED_INCOME
            "Transfer" -> ActivityActionType.TRANSACTION_ADDED_TRANSFER
            else -> ActivityActionType.TRANSACTION_ADDED_EXPENSE
        }

        val activity = ActivityLogEntry(
            actionType = actionType,
            title = "Added ${transaction.mode.lowercase()}: ${transaction.title}",
            description = account?.let { "Account: ${it.accountName}" } ?: "",
            relatedTransactionId = transaction.id,
            relatedAccountId = transaction.accountId,
            relatedCategoryId = transaction.categoryId,
            amount = transaction.amount,
            // FIXED: Always provide a non-null metadata map
            metadata = mapOf(
                "transaction_mode" to transaction.mode,
                "transaction_type" to transaction.transactionType.name,
                "currency" to (account?.currencyCode ?: "usd")
            )
        )

        logActivity(activity)
    }

    fun logTransactionUpdated(transaction: TransactionEntity, account: AccountEntity? = null) {
        val activity = ActivityLogEntry(
            actionType = ActivityActionType.TRANSACTION_UPDATED,
            title = "Updated transaction: ${transaction.title}",
            description = account?.let { "Account: ${it.accountName}" } ?: "",
            relatedTransactionId = transaction.id,
            relatedAccountId = transaction.accountId,
            relatedCategoryId = transaction.categoryId,
            amount = transaction.amount,
            metadata = mapOf(
                "transaction_mode" to transaction.mode,
                "transaction_type" to transaction.transactionType.name
            )
        )

        logActivity(activity)
    }

    fun logTransactionDeleted(transaction: TransactionEntity, account: AccountEntity? = null) {
        val actionType = when (transaction.mode) {
            "Expense" -> ActivityActionType.TRANSACTION_DELETED_EXPENSE
            "Income" -> ActivityActionType.TRANSACTION_DELETED_INCOME
            "Transfer" -> ActivityActionType.TRANSACTION_DELETED_TRANSFER
            else -> ActivityActionType.TRANSACTION_DELETED_EXPENSE
        }

        val activity = ActivityLogEntry(
            actionType = actionType,
            title = "Deleted ${transaction.mode.lowercase()}: ${transaction.title}",
            description = account?.let { "Account: ${it.accountName}" } ?: "",
            relatedTransactionId = transaction.id,
            relatedAccountId = transaction.accountId,
            relatedCategoryId = transaction.categoryId,
            amount = transaction.amount,
            metadata = mapOf(
                "transaction_mode" to transaction.mode,
                "deleted_at" to System.currentTimeMillis().toString()
            )
        )

        logActivity(activity)
    }

    // Account-related activity logging
    fun logAccountCreated(account: AccountEntity) {
        val activity = ActivityLogEntry(
            actionType = ActivityActionType.ACCOUNT_CREATED,
            title = "Created account: ${account.accountName}",
            description = "Currency: ${account.currencyCode}",
            relatedAccountId = account.id,
            amount = account.balance,
            metadata = mapOf(
                "currency_code" to account.currencyCode,
                "is_main_account" to account.isMainAccount.toString(),
                "initial_balance" to account.balance.toString()
            )
        )

        logActivity(activity)
    }

    fun logAccountUpdated(account: AccountEntity, changes: String = "") {
        val activity = ActivityLogEntry(
            actionType = ActivityActionType.ACCOUNT_UPDATED,
            title = "Updated account: ${account.accountName}",
            description = changes.ifEmpty { "Account details updated" },
            relatedAccountId = account.id,
            amount = account.balance,
            metadata = mapOf(
                "currency_code" to account.currencyCode,
                "is_main_account" to account.isMainAccount.toString(),
                "changes" to changes
            )
        )

        logActivity(activity)
    }

    fun logAccountDeleted(account: AccountEntity) {
        val activity = ActivityLogEntry(
            actionType = ActivityActionType.ACCOUNT_DELETED,
            title = "Deleted account: ${account.accountName}",
            description = "Final balance: ${account.balance}",
            relatedAccountId = account.id,
            amount = account.balance,
            metadata = mapOf(
                "currency_code" to account.currencyCode,
                "final_balance" to account.balance.toString(),
                "was_main_account" to account.isMainAccount.toString()
            )
        )

        logActivity(activity)
    }

    fun logAccountBalanceUpdated(account: AccountEntity, oldBalance: Double, newBalance: Double) {
        val activity = ActivityLogEntry(
            actionType = ActivityActionType.ACCOUNT_BALANCE_UPDATED,
            title = "Balance updated: ${account.accountName}",
            description = "From ${oldBalance} to ${newBalance}",
            relatedAccountId = account.id,
            amount = newBalance,
            oldValue = oldBalance.toString(),
            newValue = newBalance.toString(),
            metadata = mapOf(
                "currency_code" to account.currencyCode,
                "balance_change" to (newBalance - oldBalance).toString()
            )
        )

        logActivity(activity)
    }

    fun logAccountSetAsMain(account: AccountEntity) {
        val activity = ActivityLogEntry(
            actionType = ActivityActionType.ACCOUNT_SET_AS_MAIN,
            title = "Set as main account: ${account.accountName}",
            description = "This account is now the primary account",
            relatedAccountId = account.id,
            amount = account.balance,
            metadata = mapOf(
                "currency_code" to account.currencyCode,
                "balance" to account.balance.toString()
            )
        )

        logActivity(activity)
    }

    // Category-related activity logging
    fun logCategoryCreated(categoryName: String, categoryId: Int) {
        val activity = ActivityLogEntry(
            actionType = ActivityActionType.CATEGORY_CREATED,
            title = "Created category: $categoryName",
            description = "New category added to system",
            relatedCategoryId = categoryId,
            metadata = mapOf(
                "category_type" to "main",
                "created_at" to System.currentTimeMillis().toString()
            )
        )

        logActivity(activity)
    }

    fun logCategoryUpdated(categoryName: String, categoryId: Int, changes: String = "") {
        val activity = ActivityLogEntry(
            actionType = ActivityActionType.CATEGORY_UPDATED,
            title = "Updated category: $categoryName",
            description = changes.ifEmpty { "Category details updated" },
            relatedCategoryId = categoryId,
            metadata = mapOf(
                "category_type" to "main",
                "changes" to changes
            )
        )

        logActivity(activity)
    }

    fun logCategoryDeleted(categoryName: String, categoryId: Int) {
        val activity = ActivityLogEntry(
            actionType = ActivityActionType.CATEGORY_DELETED,
            title = "Deleted category: $categoryName",
            description = "Category removed from system",
            relatedCategoryId = categoryId,
            metadata = mapOf(
                "category_type" to "main",
                "deleted_at" to System.currentTimeMillis().toString()
            )
        )

        logActivity(activity)
    }

    // Subcategory-related activity logging
    fun logSubcategoryCreated(subcategoryName: String, categoryId: Int) {
        val activity = ActivityLogEntry(
            actionType = ActivityActionType.SUBCATEGORY_CREATED,
            title = "Created subcategory: $subcategoryName",
            description = "New subcategory added",
            relatedCategoryId = categoryId,
            metadata = mapOf(
                "category_type" to "sub",
                "parent_category_id" to categoryId.toString()
            )
        )

        logActivity(activity)
    }

    fun logSubcategoryUpdated(subcategoryName: String, categoryId: Int) {
        val activity = ActivityLogEntry(
            actionType = ActivityActionType.SUBCATEGORY_UPDATED,
            title = "Updated subcategory: $subcategoryName",
            description = "Subcategory details updated",
            relatedCategoryId = categoryId,
            metadata = mapOf(
                "category_type" to "sub",
                "parent_category_id" to categoryId.toString()
            )
        )

        logActivity(activity)
    }

    fun logSubcategoryDeleted(subcategoryName: String, categoryId: Int) {
        val activity = ActivityLogEntry(
            actionType = ActivityActionType.SUBCATEGORY_DELETED,
            title = "Deleted subcategory: $subcategoryName",
            description = "Subcategory removed from system",
            relatedCategoryId = categoryId,
            metadata = mapOf(
                "category_type" to "sub",
                "parent_category_id" to categoryId.toString(),
                "deleted_at" to System.currentTimeMillis().toString()
            )
        )

        logActivity(activity)
    }

    // System-related activity logging
    fun logSystemAction(actionType: ActivityActionType, description: String = "", metadata: Map<String, String> = emptyMap()) {
        val title = when (actionType) {
            ActivityActionType.DATA_BACKUP_CREATED -> "Data backup created"
            ActivityActionType.DATA_RESTORED -> "Data restored from backup"
            ActivityActionType.SETTINGS_UPDATED -> "App settings updated"
            ActivityActionType.CURRENCY_RATES_UPDATED -> "Currency exchange rates updated"
            else -> "System action performed"
        }

        val activity = ActivityLogEntry(
            actionType = actionType,
            title = title,
            description = description,
            // FIXED: Ensure metadata is never null
            metadata = metadata.ifEmpty {
                mapOf("system_action" to actionType.name.lowercase())
            }
        )

        logActivity(activity)
    }

    // Subscription-related activity logging
    fun logRecurringTransactionGenerated(transactionTitle: String, amount: Double, accountId: Int, transactionId: Int) {
        val activity = ActivityLogEntry(
            actionType = ActivityActionType.RECURRING_TRANSACTION_GENERATED,
            title = "Generated recurring transaction: $transactionTitle",
            description = "Automatic transaction from recurring schedule",
            relatedTransactionId = transactionId,
            relatedAccountId = accountId,
            amount = amount,
            metadata = mapOf(
                "generation_type" to "automatic",
                "parent_transaction_id" to transactionId.toString()
            )
        )

        logActivity(activity)
    }

//    // Generic activity logging with safe metadata
//    private fun logActivity(activity: ActivityLogEntry) {
//        activityScope.launch {
//            try {
//                // Ensure metadata is never null before saving
//                val safeActivity = activity.copy(
//                    metadata = activity.metadata // This should already be non-null due to default
//                )
//                activityLogRepository.addActivity(safeActivity)
//                Log.d("ActivityLogUtils", "Successfully logged activity: ${activity.title}")
//            } catch (e: Exception) {
//                // Log error but don't crash the app
//                Log.e("ActivityLogUtils", "Failed to log activity: ${e.message}", e)
//            }
//        }
//    }
    fun logTransactionWithContext(
        transaction: TransactionEntity,
        account: AccountEntity?,
        context: String = ""
    ) {
        val actionType = when (transaction.mode) {
            "Expense" -> ActivityActionType.TRANSACTION_ADDED_EXPENSE
            "Income" -> ActivityActionType.TRANSACTION_ADDED_INCOME
            "Transfer" -> ActivityActionType.TRANSACTION_ADDED_TRANSFER
            else -> ActivityActionType.TRANSACTION_ADDED_EXPENSE
        }

        val description = buildString {
            if (account != null) {
                append("Account: ${account.accountName}")
            }
            if (context.isNotEmpty()) {
                if (isNotEmpty()) append(" | ")
                append(context)
            }
            if (transaction.transactionType != TransactionType.DEFAULT) {
                if (isNotEmpty()) append(" | ")
                append("Type: ${transaction.transactionType.name}")
            }
        }

        val activity = ActivityLogEntry(
            actionType = actionType,
            title = "${transaction.mode}: ${transaction.title}",
            description = description,
            relatedTransactionId = transaction.id,
            relatedAccountId = transaction.accountId,
            relatedCategoryId = transaction.categoryId,
            amount = transaction.amount
        )

        logActivity(activity)
    }

    // Enhanced subscription/recurring transaction logging
    fun logRecurringTransactionCreated(
        transaction: TransactionEntity,
        account: AccountEntity?,
        isNextInSeries: Boolean = false
    ) {
        val context = if (isNextInSeries) {
            "Generated from recurring schedule"
        } else {
            "Initial recurring transaction setup"
        }

        val actionType = if (isNextInSeries) {
            ActivityActionType.RECURRING_TRANSACTION_GENERATED
        } else {
            when (transaction.mode) {
                "Expense" -> ActivityActionType.TRANSACTION_ADDED_EXPENSE
                "Income" -> ActivityActionType.TRANSACTION_ADDED_INCOME
                else -> ActivityActionType.TRANSACTION_ADDED_EXPENSE
            }
        }

        val description = buildString {
            if (account != null) {
                append("Account: ${account.accountName}")
            }
            append(" | $context")
            transaction.recurrence?.let { recurrence ->
                append(" | Frequency: ${recurrence.frequency.name}")
                append(" | Interval: ${recurrence.interval}")
            }
        }

        val activity = ActivityLogEntry(
            actionType = actionType,
            title = if (isNextInSeries) {
                "Generated: ${transaction.title}"
            } else {
                "Created recurring: ${transaction.title}"
            },
            description = description,
            relatedTransactionId = transaction.id,
            relatedAccountId = transaction.accountId,
            relatedCategoryId = transaction.categoryId,
            amount = transaction.amount
        )

        logActivity(activity)
    }

    // Enhanced currency change logging
    fun logCurrencyChangeWithDetails(
        accountId: Int,
        accountName: String,
        oldCurrency: String,
        newCurrency: String,
        conversionRate: Double,
        transactionsAffected: Int
    ) {
        val description = buildString {
            append("Currency: $oldCurrency → $newCurrency")
            append(" | Rate: $conversionRate")
            append(" | $transactionsAffected transactions updated")
        }

        val activity = ActivityLogEntry(
            actionType = ActivityActionType.ACCOUNT_CURRENCY_CHANGED,
            title = "Currency changed: $accountName",
            description = description,
            relatedAccountId = accountId,
            metadata = mapOf(
                "old_currency" to oldCurrency,
                "new_currency" to newCurrency,
                "conversion_rate" to conversionRate.toString(),
                "transactions_affected" to transactionsAffected.toString()
            )
        )

        logActivity(activity)
    }

    // Enhanced deletion logging with statistics
    fun logBulkDeletionWithStats(
        actionType: ActivityActionType,
        itemType: String,
        deletedCount: Int,
        restoredBalanceAmount: Double? = null,
        affectedAccounts: List<String> = emptyList()
    ) {
        val description = buildString {
            append("$deletedCount $itemType deleted")
            if (restoredBalanceAmount != null) {
                append(" | Balance restored: $restoredBalanceAmount")
            }
            if (affectedAccounts.isNotEmpty()) {
                append(" | Affected accounts: ${affectedAccounts.joinToString(", ")}")
            }
        }

        val activity = ActivityLogEntry(
            actionType = actionType,
            title = "Bulk deletion: $itemType",
            description = description,
            metadata = mapOf(
                "item_type" to itemType,
                "deleted_count" to deletedCount.toString(),
                "affected_accounts" to affectedAccounts.joinToString(",")
            )
        )

        logActivity(activity)
    }

    // System action with performance metrics
    fun logSystemActionWithMetrics(
        actionType: ActivityActionType,
        description: String,
        executionTimeMs: Long? = null,
        itemsProcessed: Int? = null
    ) {
        val enhancedDescription = buildString {
            append(description)
            if (executionTimeMs != null) {
                append(" | Execution time: ${executionTimeMs}ms")
            }
            if (itemsProcessed != null) {
                append(" | Items processed: $itemsProcessed")
            }
        }

        val metadata = mutableMapOf<String, String>()
        executionTimeMs?.let { metadata["execution_time_ms"] = it.toString() }
        itemsProcessed?.let { metadata["items_processed"] = it.toString() }

        val activity = ActivityLogEntry(
            actionType = actionType,
            title = when (actionType) {
                ActivityActionType.DATA_BACKUP_CREATED -> "Data Backup"
                ActivityActionType.DATA_RESTORED -> "Data Restoration"
                ActivityActionType.SETTINGS_UPDATED -> "Settings Update"
                ActivityActionType.CURRENCY_RATES_UPDATED -> "Currency Update"
                else -> "System Action"
            },
            description = enhancedDescription,
            metadata = metadata
        )

        logActivity(activity)
    }

    private fun logActivity(activity: ActivityLogEntry) {
        activityScope.launch {
            try {
                activityLogRepository.addActivity(activity)
                Log.d("ActivityLogUtils", "Logged activity: ${activity.title}")
            } catch (e: Exception) {
                Log.e("ActivityLogUtils", "Failed to log activity: ${e.message}", e)
            }
        }
    }

    // Transaction-related activity logging
//    fun logTransactionCreated(transaction: TransactionEntity, account: AccountEntity? = null) {
//        val actionType = when (transaction.mode) {
//            "Expense" -> ActivityActionType.TRANSACTION_ADDED_EXPENSE
//            "Income" -> ActivityActionType.TRANSACTION_ADDED_INCOME
//            "Transfer" -> ActivityActionType.TRANSACTION_ADDED_TRANSFER
//            else -> ActivityActionType.TRANSACTION_ADDED_EXPENSE
//        }
//
//        val activity = ActivityLogEntry(
//            actionType = actionType,
//            title = "Added ${transaction.mode.lowercase()}: ${transaction.title}",
//            description = account?.let { "Account: ${it.accountName}" } ?: "",
//            relatedTransactionId = transaction.id,
//            relatedAccountId = transaction.accountId,
//            relatedCategoryId = transaction.categoryId,
//            amount = transaction.amount
//        )
//
//        logActivity(activity)
//    }
//
//    fun logTransactionUpdated(transaction: TransactionEntity, account: AccountEntity? = null) {
//        val activity = ActivityLogEntry(
//            actionType = ActivityActionType.TRANSACTION_UPDATED,
//            title = "Updated transaction: ${transaction.title}",
//            description = account?.let { "Account: ${it.accountName}" } ?: "",
//            relatedTransactionId = transaction.id,
//            relatedAccountId = transaction.accountId,
//            relatedCategoryId = transaction.categoryId,
//            amount = transaction.amount
//        )
//
//        logActivity(activity)
//    }
//
//    fun logTransactionDeleted(transaction: TransactionEntity, account: AccountEntity? = null) {
//        val actionType = when (transaction.mode) {
//            "Expense" -> ActivityActionType.TRANSACTION_DELETED_EXPENSE
//            "Income" -> ActivityActionType.TRANSACTION_DELETED_INCOME
//            "Transfer" -> ActivityActionType.TRANSACTION_DELETED_TRANSFER
//            else -> ActivityActionType.TRANSACTION_DELETED_EXPENSE
//        }
//
//        val activity = ActivityLogEntry(
//            actionType = actionType,
//            title = "Deleted ${transaction.mode.lowercase()}: ${transaction.title}",
//            description = account?.let { "Account: ${it.accountName}" } ?: "",
//            relatedTransactionId = transaction.id,
//            relatedAccountId = transaction.accountId,
//            relatedCategoryId = transaction.categoryId,
//            amount = transaction.amount
//        )
//
//        logActivity(activity)
//    }
//
//    // Account-related activity logging
//    fun logAccountCreated(account: AccountEntity) {
//        val activity = ActivityLogEntry(
//            actionType = ActivityActionType.ACCOUNT_CREATED,
//            title = "Created account: ${account.accountName}",
//            description = "Currency: ${account.currencyCode}",
//            relatedAccountId = account.id,
//            amount = account.balance
//        )
//
//        logActivity(activity)
//    }
//
//    fun logAccountUpdated(account: AccountEntity, changes: String = "") {
//        val activity = ActivityLogEntry(
//            actionType = ActivityActionType.ACCOUNT_UPDATED,
//            title = "Updated account: ${account.accountName}",
//            description = changes.ifEmpty { "Account details updated" },
//            relatedAccountId = account.id,
//            amount = account.balance
//        )
//
//        logActivity(activity)
//    }
//
//    fun logAccountDeleted(account: AccountEntity) {
//        val activity = ActivityLogEntry(
//            actionType = ActivityActionType.ACCOUNT_DELETED,
//            title = "Deleted account: ${account.accountName}",
//            description = "Final balance: ${account.balance}",
//            relatedAccountId = account.id,
//            amount = account.balance
//        )
//
//        logActivity(activity)
//    }
//
//    fun logAccountBalanceUpdated(account: AccountEntity, oldBalance: Double, newBalance: Double) {
//        val activity = ActivityLogEntry(
//            actionType = ActivityActionType.ACCOUNT_BALANCE_UPDATED,
//            title = "Balance updated: ${account.accountName}",
//            description = "From ${oldBalance} to ${newBalance}",
//            relatedAccountId = account.id,
//            amount = newBalance,
//            oldValue = oldBalance.toString(),
//            newValue = newBalance.toString()
//        )
//
//        logActivity(activity)
//    }
//
//    fun logAccountSetAsMain(account: AccountEntity) {
//        val activity = ActivityLogEntry(
//            actionType = ActivityActionType.ACCOUNT_SET_AS_MAIN,
//            title = "Set as main account: ${account.accountName}",
//            description = "This account is now the primary account",
//            relatedAccountId = account.id,
//            amount = account.balance
//        )
//
//        logActivity(activity)
//    }
//
//    // Category-related activity logging
//    fun logCategoryCreated(categoryName: String, categoryId: Int) {
//        val activity = ActivityLogEntry(
//            actionType = ActivityActionType.CATEGORY_CREATED,
//            title = "Created category: $categoryName",
//            description = "New category added to system",
//            relatedCategoryId = categoryId
//        )
//
//        logActivity(activity)
//    }
//
//    fun logCategoryUpdated(categoryName: String, categoryId: Int, changes: String = "") {
//        val activity = ActivityLogEntry(
//            actionType = ActivityActionType.CATEGORY_UPDATED,
//            title = "Updated category: $categoryName",
//            description = changes.ifEmpty { "Category details updated" },
//            relatedCategoryId = categoryId
//        )
//
//        logActivity(activity)
//    }
//
//    fun logCategoryDeleted(categoryName: String, categoryId: Int) {
//        val activity = ActivityLogEntry(
//            actionType = ActivityActionType.CATEGORY_DELETED,
//            title = "Deleted category: $categoryName",
//            description = "Category removed from system",
//            relatedCategoryId = categoryId
//        )
//
//        logActivity(activity)
//    }
//
//    // Subcategory-related activity logging
//    fun logSubcategoryCreated(subcategoryName: String, categoryId: Int) {
//        val activity = ActivityLogEntry(
//            actionType = ActivityActionType.SUBCATEGORY_CREATED,
//            title = "Created subcategory: $subcategoryName",
//            description = "New subcategory added",
//            relatedCategoryId = categoryId
//        )
//
//        logActivity(activity)
//    }
//
//    fun logSubcategoryUpdated(subcategoryName: String, categoryId: Int) {
//        val activity = ActivityLogEntry(
//            actionType = ActivityActionType.SUBCATEGORY_UPDATED,
//            title = "Updated subcategory: $subcategoryName",
//            description = "Subcategory details updated",
//            relatedCategoryId = categoryId
//        )
//
//        logActivity(activity)
//    }
//
//    fun logSubcategoryDeleted(subcategoryName: String, categoryId: Int) {
//        val activity = ActivityLogEntry(
//            actionType = ActivityActionType.SUBCATEGORY_DELETED,
//            title = "Deleted subcategory: $subcategoryName",
//            description = "Subcategory removed from system",
//            relatedCategoryId = categoryId
//        )
//
//        logActivity(activity)
//    }
//
//    // System-related activity logging
//    fun logSystemAction(actionType: ActivityActionType, description: String = "", metadata: Map<String, String> = emptyMap()) {
//        val title = when (actionType) {
//            ActivityActionType.DATA_BACKUP_CREATED -> "Data backup created"
//            ActivityActionType.DATA_RESTORED -> "Data restored from backup"
//            ActivityActionType.SETTINGS_UPDATED -> "App settings updated"
//            ActivityActionType.CURRENCY_RATES_UPDATED -> "Currency exchange rates updated"
//            else -> "System action performed"
//        }
//
//        val activity = ActivityLogEntry(
//            actionType = actionType,
//            title = title,
//            description = description,
//            metadata = metadata
//        )
//
//        logActivity(activity)
//    }
//
//    // Subscription-related activity logging
//    fun logSubscriptionCreated(subscriptionName: String, amount: Double, accountId: Int? = null) {
//        val activity = ActivityLogEntry(
//            actionType = ActivityActionType.SUBSCRIPTION_CREATED,
//            title = "Created subscription: $subscriptionName",
//            description = "Recurring subscription added",
//            relatedAccountId = accountId,
//            amount = amount
//        )
//
//        logActivity(activity)
//    }
//
//    fun logSubscriptionCancelled(subscriptionName: String, amount: Double, accountId: Int? = null) {
//        val activity = ActivityLogEntry(
//            actionType = ActivityActionType.SUBSCRIPTION_CANCELLED,
//            title = "Cancelled subscription: $subscriptionName",
//            description = "Subscription has been cancelled",
//            relatedAccountId = accountId,
//            amount = amount
//        )
//
//        logActivity(activity)
//    }
//
//    fun logRecurringTransactionGenerated(transactionTitle: String, amount: Double, accountId: Int, transactionId: Int) {
//        val activity = ActivityLogEntry(
//            actionType = ActivityActionType.RECURRING_TRANSACTION_GENERATED,
//            title = "Generated recurring transaction: $transactionTitle",
//            description = "Automatic transaction from recurring schedule",
//            relatedTransactionId = transactionId,
//            relatedAccountId = accountId,
//            amount = amount
//        )
//
//        logActivity(activity)
//    }
//
//    fun logPaymentReminderSent(transactionTitle: String, amount: Double, accountId: Int? = null) {
//        val activity = ActivityLogEntry(
//            actionType = ActivityActionType.PAYMENT_REMINDER_SENT,
//            title = "Payment reminder sent: $transactionTitle",
//            description = "Reminder notification sent to user",
//            relatedAccountId = accountId,
//            amount = amount
//        )
//
//        logActivity(activity)
//    }
//
//    // Generic activity logging
//    private fun logActivity(activity: ActivityLogEntry) {
//        activityScope.launch {
//            try {
//                activityLogRepository.addActivity(activity)
//            } catch (e: Exception) {
//                // Log error but don't crash the app
//                android.util.Log.e("ActivityLogUtils", "Failed to log activity: ${e.message}", e)
//            }
//        }
//    }

    // Bulk cleanup utilities
    fun cleanupOldActivities(olderThanDays: Int = 90) {
        activityScope.launch {
            try {
                val cutoffDate = System.currentTimeMillis() - (olderThanDays * 24 * 60 * 60 * 1000L)
                activityLogRepository.deleteActivitiesOlderThan(cutoffDate)
            } catch (e: Exception) {
                android.util.Log.e("ActivityLogUtils", "Failed to cleanup old activities: ${e.message}", e)
            }
        }
    }

    // Activity statistics
//    suspend fun getActivityStats(): Map<String, Any> {
//        return try {
//            val totalCount = activityLogRepository.getActivityCount()
//            val latestActivity = activityLogRepository.getLatestActivity()
//            val oldestActivity = activityLogRepository.getOldestActivity()
//
//            mapOf(
//                "totalActivities" to totalCount,
//                "latestActivity" to latestActivity,
//                "oldestActivity" to oldestActivity,
//                "hasActivities" to (totalCount > 0)
//            )
//        } catch (e: Exception) {
//            android.util.Log.e("ActivityLogUtils", "Failed to get activity stats: ${e.message}", e)
//            emptyMap()
//        }
//    }
}

object ActivityLogFilterUtils {
    fun applyFilters(
        activities: List<ActivityLogEntry>,
        filterState: ActivityLogFilterState,
        accounts: List<AccountEntity>
    ): List<ActivityLogEntry> {
        if (!filterState.isFilterActive()) return activities

        var filtered = activities

        // Action type filter
        if (filterState.selectedActionTypes.isNotEmpty()) {
            filtered = filtered.filter {
                filterState.selectedActionTypes.contains(it.actionType)
            }
        }

        // Category filter
        if (filterState.selectedCategories.isNotEmpty()) {
            filtered = filtered.filter { activity ->
                filterState.selectedCategories.contains(activity.actionType.getCategory())
            }
        }

        // Account filter
        if (filterState.selectedAccounts.isNotEmpty()) {
            filtered = filtered.filter { activity ->
                activity.relatedAccountId != null &&
                        filterState.selectedAccounts.contains(activity.relatedAccountId)
            }
        }

        // Date range filter
        filterState.dateRange?.let { dateRange ->
            if (dateRange.isValid()) {
                filtered = filtered.filter { activity ->
                    dateRange.contains(activity.timestamp)
                }
            }
        }

        // Search filter
        if (filterState.searchText.text.isNotBlank()) {
            val searchQuery = filterState.searchText.text.lowercase()
            filtered = filtered.filter { activity ->
                activity.title.lowercase().contains(searchQuery) ||
                        activity.description.lowercase().contains(searchQuery)
            }
        }

        // Time period filters
        val currentTime = System.currentTimeMillis()
        when {
            filterState.showOnlyToday -> {
                val startOfDay = currentTime - (currentTime % (24 * 60 * 60 * 1000))
                val endOfDay = startOfDay + (24 * 60 * 60 * 1000) - 1
                filtered = filtered.filter { it.timestamp in startOfDay..endOfDay }
            }
            filterState.showOnlyThisWeek -> {
                val weekAgo = currentTime - (7 * 24 * 60 * 60 * 1000)
                filtered = filtered.filter { it.timestamp >= weekAgo }
            }
            filterState.showOnlyThisMonth -> {
                val monthAgo = currentTime - (30 * 24 * 60 * 60 * 1000)
                filtered = filtered.filter { it.timestamp >= monthAgo }
            }
        }

        // Sort
        filtered = when (filterState.sortBy) {
            ActivityLogSortBy.TIMESTAMP_ASC -> filtered.sortedBy { it.timestamp }
            ActivityLogSortBy.TIMESTAMP_DESC -> filtered.sortedByDescending { it.timestamp }
            ActivityLogSortBy.TYPE_ASC -> filtered.sortedBy { it.actionType.getDisplayName() }
            ActivityLogSortBy.TYPE_DESC -> filtered.sortedByDescending { it.actionType.getDisplayName() }
            ActivityLogSortBy.ACCOUNT_NAME -> {
                val accountMap = accounts.associateBy { it.id }
                filtered.sortedBy { activity ->
                    accountMap[activity.relatedAccountId]?.accountName ?: ""
                }
            }
            ActivityLogSortBy.AMOUNT_ASC -> filtered.sortedBy { it.amount ?: 0.0 }
            ActivityLogSortBy.AMOUNT_DESC -> filtered.sortedByDescending { it.amount ?: 0.0 }
        }

        return filtered
    }

    fun groupActivitiesByDate(activities: List<ActivityLogEntry>): Map<String, List<ActivityLogEntry>> {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        return activities.groupBy { activity ->
            dateFormat.format(Date(activity.timestamp))
        }.toSortedMap(compareByDescending { dateString ->
            try {
                dateFormat.parse(dateString)?.time ?: 0L
            } catch (e: Exception) {
                0L
            }
        })
    }

    fun getActivitiesForTimeRange(
        activities: List<ActivityLogEntry>,
        timeRange: TimeRange
    ): List<ActivityLogEntry> {
        val currentTime = System.currentTimeMillis()

        val startTime = when (timeRange) {
            TimeRange.TODAY -> {
                val startOfDay = currentTime - (currentTime % (24 * 60 * 60 * 1000))
                startOfDay
            }
            TimeRange.YESTERDAY -> {
                val startOfYesterday = currentTime - (24 * 60 * 60 * 1000)
                startOfYesterday - (startOfYesterday % (24 * 60 * 60 * 1000))
            }
            TimeRange.THIS_WEEK -> currentTime - (7 * 24 * 60 * 60 * 1000)
            TimeRange.THIS_MONTH -> currentTime - (30 * 24 * 60 * 60 * 1000)
            TimeRange.ALL_TIME -> 0L
        }

        val endTime = when (timeRange) {
            TimeRange.YESTERDAY -> {
                val startOfToday = currentTime - (currentTime % (24 * 60 * 60 * 1000))
                startOfToday - 1
            }
            else -> currentTime
        }

        return activities.filter { activity ->
            activity.timestamp in startTime..endTime
        }
    }
}

enum class TimeRange {
    TODAY,
    YESTERDAY,
    THIS_WEEK,
    THIS_MONTH,
    ALL_TIME;

    fun getDisplayName(): String {
        return when (this) {
            TODAY -> "Today"
            YESTERDAY -> "Yesterday"
            THIS_WEEK -> "This Week"
            THIS_MONTH -> "This Month"
            ALL_TIME -> "All Time"
        }
    }
}

object ActivityLogEntryFactory {
    fun createTransactionEntry(
        actionType: ActivityActionType,
        transaction: TransactionEntity,
        account: AccountEntity? = null
    ): ActivityLogEntry {
        val title = when (actionType) {
            ActivityActionType.TRANSACTION_ADDED_EXPENSE -> "Added expense: ${transaction.title}"
            ActivityActionType.TRANSACTION_ADDED_INCOME -> "Added income: ${transaction.title}"
            ActivityActionType.TRANSACTION_ADDED_TRANSFER -> "Added transfer: ${transaction.title}"
            ActivityActionType.TRANSACTION_UPDATED -> "Updated transaction: ${transaction.title}"
            ActivityActionType.TRANSACTION_DELETED_EXPENSE -> "Deleted expense: ${transaction.title}"
            ActivityActionType.TRANSACTION_DELETED_INCOME -> "Deleted income: ${transaction.title}"
            ActivityActionType.TRANSACTION_DELETED_TRANSFER -> "Deleted transfer: ${transaction.title}"
            ActivityActionType.TRANSACTION_MARKED_PAID -> "Marked as paid: ${transaction.title}"
            ActivityActionType.TRANSACTION_MARKED_UNPAID -> "Marked as unpaid: ${transaction.title}"
            else -> "Transaction action: ${transaction.title}"
        }

        val description = account?.let { "Account: ${it.accountName}" } ?: ""

        return ActivityLogEntry(
            actionType = actionType,
            title = title,
            description = description,
            relatedTransactionId = transaction.id,
            relatedAccountId = transaction.accountId,
            relatedCategoryId = transaction.categoryId,
            amount = transaction.amount
        )
    }

    fun createAccountEntry(
        actionType: ActivityActionType,
        account: AccountEntity,
        oldValue: String? = null,
        newValue: String? = null
    ): ActivityLogEntry {
        val title = when (actionType) {
            ActivityActionType.ACCOUNT_CREATED -> "Created account: ${account.accountName}"
            ActivityActionType.ACCOUNT_UPDATED -> "Updated account: ${account.accountName}"
            ActivityActionType.ACCOUNT_DELETED -> "Deleted account: ${account.accountName}"
            ActivityActionType.ACCOUNT_BALANCE_UPDATED -> "Balance updated: ${account.accountName}"
            ActivityActionType.ACCOUNT_CURRENCY_CHANGED -> "Currency changed: ${account.accountName}"
            ActivityActionType.ACCOUNT_SET_AS_MAIN -> "Set as main account: ${account.accountName}"
            else -> "Account action: ${account.accountName}"
        }

        val description = when (actionType) {
            ActivityActionType.ACCOUNT_BALANCE_UPDATED ->
                "New balance: ${CurrencySymbols.getSymbol(account.currencyCode)}${account.balance}"
            ActivityActionType.ACCOUNT_CURRENCY_CHANGED ->
                if (oldValue != null && newValue != null) "From $oldValue to $newValue" else ""
            else -> ""
        }

        return ActivityLogEntry(
            actionType = actionType,
            title = title,
            description = description,
            relatedAccountId = account.id,
            amount = if (actionType == ActivityActionType.ACCOUNT_BALANCE_UPDATED) account.balance else null,
            oldValue = oldValue,
            newValue = newValue
        )
    }

    fun createCategoryEntry(
        actionType: ActivityActionType,
        categoryName: String,
        categoryId: Int
    ): ActivityLogEntry {
        val title = when (actionType) {
            ActivityActionType.CATEGORY_CREATED -> "Created category: $categoryName"
            ActivityActionType.CATEGORY_UPDATED -> "Updated category: $categoryName"
            ActivityActionType.CATEGORY_DELETED -> "Deleted category: $categoryName"
            ActivityActionType.SUBCATEGORY_CREATED -> "Created subcategory: $categoryName"
            ActivityActionType.SUBCATEGORY_UPDATED -> "Updated subcategory: $categoryName"
            ActivityActionType.SUBCATEGORY_DELETED -> "Deleted subcategory: $categoryName"
            else -> "Category action: $categoryName"
        }

        return ActivityLogEntry(
            actionType = actionType,
            title = title,
            relatedCategoryId = categoryId
        )
    }

    fun createSystemEntry(
        actionType: ActivityActionType,
        description: String = ""
    ): ActivityLogEntry {
        val title = when (actionType) {
            ActivityActionType.DATA_BACKUP_CREATED -> "Data backup created"
            ActivityActionType.DATA_RESTORED -> "Data restored from backup"
            ActivityActionType.SETTINGS_UPDATED -> "App settings updated"
            ActivityActionType.CURRENCY_RATES_UPDATED -> "Currency exchange rates updated"
            else -> "System action performed"
        }

        return ActivityLogEntry(
            actionType = actionType,
            title = title,
            description = description
        )
    }
}