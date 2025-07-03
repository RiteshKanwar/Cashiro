package com.ritesh.cashiro.domain.utils

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

interface AppEvent

/**
 * Transaction-related events
 */
sealed class TransactionEvent : AppEvent {
    object TransactionsUpdated : TransactionEvent()
    data class TransactionAdded(val transactionId: Int) : TransactionEvent()
    data class TransactionDeleted(val transactionId: Int) : TransactionEvent()
    data class TransactionUpdated(val transactionId: Int) : TransactionEvent()
}

/**
 * Currency-related events
 */
sealed class CurrencyEvent : AppEvent {
    data class AccountCurrencyChanged(
        val accountId: Int,
        val oldCurrencyCode: String,
        val newCurrencyCode: String,
        val conversionRate: Double
    ) : CurrencyEvent()

    data class MainAccountCurrencyChanged(
        val mainAccountId: Int,
        val oldCurrencyCode: String,
        val newCurrencyCode: String
    ) : CurrencyEvent()

    data class ConversionRatesUpdated(
        val baseCurrency: String,
        val rates: Map<String, Double>
    ) : CurrencyEvent()
}

/**
 * Account-related events
 */
sealed class AccountEvent : AppEvent {
    object AccountsUpdated : AccountEvent()
    data class AccountAdded(val accountId: Int) : AccountEvent()
    data class AccountDeleted(val accountId: Int) : AccountEvent()
    data class AccountUpdated(val accountId: Int) : AccountEvent()
    data class BalanceUpdated(val accountId: Int, val newBalance: Double) : AccountEvent()
}

/**
 * Category-related events
 */
sealed class CategoryEvent : AppEvent {
    object CategoriesUpdated : CategoryEvent()
    data class CategoryAdded(val categoryId: Int) : CategoryEvent()
    data class CategoryDeleted(val categoryId: Int) : CategoryEvent()
    data class CategoryUpdated(val categoryId: Int) : CategoryEvent()
    data class SubCategoriesUpdated(val categoryId: Int) : CategoryEvent()
    data class SubCategoryAdded(val categoryId: Int, val subCategoryId: Int) : CategoryEvent()
    data class SubCategoryDeleted(val categoryId: Int, val subCategoryId: Int) : CategoryEvent()
    data class SubCategoryUpdated(val categoryId: Int, val subCategoryId: Int) : CategoryEvent()
    data class SubCategoryMerged(val sourceCategoryId: Int, val targetCategoryId: Int) : CategoryEvent()
    data class SubCategoryConvertedToMain(val oldCategoryId: Int, val newCategoryId: Int) : CategoryEvent()
}

/**
 * Centralized EventBus for the entire application
 */
object AppEventBus {
    private val _events = MutableSharedFlow<AppEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<AppEvent> = _events.asSharedFlow()

    /**
     * Emit an event to all observers
     */
    suspend fun emitEvent(event: AppEvent) {
        _events.emit(event)
    }

    /**
     * Emit an event without suspending (for use in non-coroutine contexts)
     * Returns true if the event was emitted, false if the buffer was full
     */
    fun tryEmitEvent(event: AppEvent): Boolean {
        return _events.tryEmit(event)
    }
}