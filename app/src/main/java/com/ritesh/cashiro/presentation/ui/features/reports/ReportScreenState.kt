package com.ritesh.cashiro.presentation.ui.features.reports

import com.ritesh.cashiro.data.local.entity.AccountEntity
import com.ritesh.cashiro.data.local.entity.CategoryEntity
import com.ritesh.cashiro.data.local.entity.TransactionEntity
import com.ritesh.cashiro.presentation.ui.extras.components.charts.TimePeriod
import com.ritesh.cashiro.presentation.ui.extras.components.charts.WeekPeriod
import java.time.LocalDate

data class ReportScreenState(
    // UI state
    val selectedTabIndex: Int = 0,
    val currentDate: LocalDate = LocalDate.now(),
    val currentTimePeriod: TimePeriod = WeekPeriod(LocalDate.now()),

    // Data collections
    val transactions: List<TransactionEntity> = emptyList(),
    val accounts: List<AccountEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),

    // Loading state
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class ReportScreenEvent {
    data class TabSelected(val index: Int) : ReportScreenEvent()
    object NextPeriod : ReportScreenEvent()
    object PreviousPeriod : ReportScreenEvent()
    object ResetToCurrentPeriod : ReportScreenEvent()
    object RefreshData : ReportScreenEvent()
}