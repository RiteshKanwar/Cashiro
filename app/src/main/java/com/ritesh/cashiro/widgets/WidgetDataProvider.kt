package com.ritesh.cashiro.widgets

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.ritesh.cashiro.data.local.entity.AccountEntity
import com.ritesh.cashiro.data.local.entity.TransactionEntity
import com.ritesh.cashiro.domain.repository.AccountRepository
import com.ritesh.cashiro.domain.repository.TransactionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetDataProvider @Inject constructor(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) {

    suspend fun getFinancialSummary(): FinancialSummary {
        return try {
            val accounts = accountRepository.getAllAccounts()
            val transactions = transactionRepository.getAllTransactions()

            val totalBalance = accounts.sumOf { it.balance }
            val totalIncome = transactions.filter { it.mode == "Income" }.sumOf { it.amount }
            val totalExpenses = transactions.filter { it.mode == "Expense" }.sumOf { it.amount }
            val transactionCount = transactions.size

            FinancialSummary(
                totalBalance = totalBalance,
                totalIncome = totalIncome,
                totalExpenses = totalExpenses,
                transactionCount = transactionCount,
                accounts = accounts,
                transactions = transactions
            )
        } catch (e: Exception) {
            Log.e("WidgetDataProvider", "Error getting financial summary", e)
            FinancialSummary()
        }
    }
}

data class FinancialSummary(
    val totalBalance: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val transactionCount: Int = 0,
    val accounts: List<AccountEntity> = emptyList(),
    val transactions: List<TransactionEntity> = emptyList()
)

// Enhanced Widget Theme Colors
data class WidgetThemeColors(
    val primaryColor: Int,
    val backgroundColor: Int,
    val surfaceColor: Int,
    val textColorPrimary: Int,
    val textColorSecondary: Int,
    val positiveColor: Int,
    val negativeColor: Int,
    val isDarkTheme: Boolean
) {
    // Helper function to get adaptive icon tint
    fun getIconTint(): Int = if (isDarkTheme) textColorPrimary else primaryColor

    // Helper function to get button background
    fun getButtonBackground(): Int = primaryColor

    // Helper function to get button text color
    fun getButtonTextColor(): Int = if (isDarkTheme) Color.White.toArgb() else Color.White.toArgb()
}