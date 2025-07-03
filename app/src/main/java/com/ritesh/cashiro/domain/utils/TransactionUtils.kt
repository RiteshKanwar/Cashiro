package com.ritesh.cashiro.domain.utils

import com.ritesh.cashiro.data.local.entity.AccountEntity
import com.ritesh.cashiro.data.local.entity.CategoryEntity
import com.ritesh.cashiro.data.local.entity.SubCategoryEntity
import com.ritesh.cashiro.data.local.entity.TransactionEntity
import com.ritesh.cashiro.presentation.ui.features.transactions.TransactionFilterState

object TransactionFilterUtils {
    fun applyFilters(
        transactions: List<TransactionEntity>,
        filterState: TransactionFilterState,
        categories: List<CategoryEntity>,
        accounts: List<AccountEntity>
    ): List<TransactionEntity> {
        if (!filterState.isFilterActive()) return transactions

        return transactions.filter { transaction ->
            // ENHANCED: Category and Subcategory filter
            val categoryMatch = if (filterState.selectedCategories.isEmpty() && filterState.selectedSubCategories.isEmpty()) {
                true
            } else {
                // Transaction matches if:
                // 1. Its category is selected, OR
                // 2. Its subcategory is selected (if it has one)
                val categorySelected = filterState.selectedCategories.contains(transaction.categoryId)
                val subCategorySelected = transaction.subCategoryId != null &&
                        filterState.selectedSubCategories.contains(transaction.subCategoryId)

                categorySelected || subCategorySelected
            }

            // Account filter
            val accountMatch = if (filterState.selectedAccounts.isEmpty()) {
                true
            } else {
                filterState.selectedAccounts.contains(transaction.accountId)
            }

            // Amount filter
            val amountMatch = transaction.amount >= filterState.minAmount &&
                    transaction.amount <= filterState.maxAmount

            // Transaction mode filter
            val modeMatch = filterState.transactionMode.contains(transaction.mode)

            // Transaction type filter
            val typeMatch = if (filterState.transactionTypes.isEmpty()) {
                true
            } else {
                filterState.transactionTypes.contains(transaction.transactionType)
            }

            // Payment status filter
            val statusMatch = if (filterState.paymentStatus.isEmpty()) {
                true
            } else {
                when {
                    filterState.paymentStatus.contains("Paid") && transaction.isPaid -> true
                    filterState.paymentStatus.contains("Not paid") && !transaction.isPaid -> true
                    else -> false
                }
            }

            // Title and notes search filter
            val searchMatch = if (filterState.searchText.text.isBlank()) {
                true
            } else {
                val searchQuery = filterState.searchText.text.lowercase()
                transaction.title.lowercase().contains(searchQuery) ||
                        transaction.note.lowercase().contains(searchQuery)
            }

            // All filters must match
            categoryMatch && accountMatch && amountMatch && modeMatch &&
                    typeMatch && statusMatch && searchMatch
        }
    }

    // NEW: Helper function to get subcategories for a category
    fun getSubCategoriesForCategory(
        categoryId: Int,
        subCategories: List<SubCategoryEntity>
    ): List<SubCategoryEntity> {
        return subCategories.filter { it.categoryId == categoryId }
    }

    // NEW: Helper function to check if category has subcategories
    fun categoryHasSubCategories(
        categoryId: Int,
        subCategories: List<SubCategoryEntity>
    ): Boolean {
        return subCategories.any { it.categoryId == categoryId }
    }

    // NEW: Helper function to get all subcategory IDs for selected categories
    fun getSubCategoryIdsForCategories(
        categoryIds: Set<Int>,
        subCategories: List<SubCategoryEntity>
    ): Set<Int> {
        return subCategories
            .filter { categoryIds.contains(it.categoryId) }
            .map { it.id }
            .toSet()
    }
}