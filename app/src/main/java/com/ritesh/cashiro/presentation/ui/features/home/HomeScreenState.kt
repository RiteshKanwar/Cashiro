package com.ritesh.cashiro.presentation.ui.features.home

import android.net.Uri
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ritesh.cashiro.data.local.entity.AccountEntity
import com.ritesh.cashiro.data.local.entity.TransactionEntity
import com.ritesh.cashiro.presentation.ui.extras.components.charts.TransactionDataType
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Blue

@Immutable
data class HomeScreenState(
    // Account related state
    val accounts: List<AccountEntity> = emptyList(),
    val mainCurrencyCode: String = "",

    // Transaction related state
    val transactions: List<TransactionEntity> = emptyList(),
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val recentExpenses: List<TransactionEntity> = emptyList(),
    val recentIncomes: List<TransactionEntity> = emptyList(),

    // NEW: Upcoming and Overdue transactions
    val upcomingTransactions: List<TransactionEntity> = emptyList(),
    val overdueTransactions: List<TransactionEntity> = emptyList(),

    // UI state
    val selectedDataType: TransactionDataType = TransactionDataType.ALL,
    val expanded: Boolean = false,
    val collapsingFraction: Float = 0f,
    val currentOffset: Dp = 180.dp,

    // User profile
    val userName: String = "",
    val profileImageUri: Uri? = null,
    val profileBackgroundColor: Color = Macchiato_Blue,

    // Loading state
    val isLoading: Boolean = true
)

//@Immutable
//data class HomeScreenState(
//    // Account related state
//    val accounts: List<AccountEntity> = emptyList(),
//    val mainCurrencyCode: String = "",
//
//    // Transaction related state
//    val transactions: List<TransactionEntity> = emptyList(),
//    val recentTransactions: List<TransactionEntity> = emptyList(),
//    val recentExpenses: List<TransactionEntity> = emptyList(),
//    val recentIncomes: List<TransactionEntity> = emptyList(),
//
//    // UI state
//    val selectedDataType: TransactionDataType = TransactionDataType.ALL,
//    val expanded: Boolean = false,
//    val collapsingFraction: Float = 0f,
//    val currentOffset: Dp = 180.dp,
//
//    // User profile
//    val userName: String = "",
//    val profileImageUri: Uri? = null,
//    val profileBackgroundColor: Color = Macchiato_Blue,
//
//    // Loading state
//    val isLoading: Boolean = true
//)
