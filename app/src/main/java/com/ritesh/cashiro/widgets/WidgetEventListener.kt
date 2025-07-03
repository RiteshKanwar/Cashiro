package com.ritesh.cashiro.widgets

import com.ritesh.cashiro.domain.utils.AccountEvent
import com.ritesh.cashiro.domain.utils.AppEventBus
import com.ritesh.cashiro.domain.utils.CurrencyEvent
import com.ritesh.cashiro.domain.utils.TransactionEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetEventListener @Inject constructor(
    private val widgetUpdateUtil: WidgetUpdateUtil
) {

    fun startListening() {
        CoroutineScope(Dispatchers.IO).launch {
            AppEventBus.events.collect { event ->
                when (event) {
                    is AccountEvent.BalanceUpdated,
                    is AccountEvent.AccountsUpdated,
                    is AccountEvent.AccountDeleted -> {
                        widgetUpdateUtil.updateAllFinancialWidgets()
                    }

                    is TransactionEvent.TransactionsUpdated -> {
                        widgetUpdateUtil.updateAllFinancialWidgets()
                    }

                    is CurrencyEvent.AccountCurrencyChanged,
                    is CurrencyEvent.MainAccountCurrencyChanged,
                    is CurrencyEvent.ConversionRatesUpdated -> {
                        widgetUpdateUtil.updateAllFinancialWidgets()
                    }
                }
            }
        }
    }
}
