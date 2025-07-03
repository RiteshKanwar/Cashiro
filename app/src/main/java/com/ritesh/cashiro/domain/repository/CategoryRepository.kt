package com.ritesh.cashiro.domain.repository

import android.util.Log
import com.ritesh.cashiro.data.local.dao.CategoryDao
import com.ritesh.cashiro.data.local.entity.CategoryEntity
import com.ritesh.cashiro.data.local.entity.CategoryWithSubCategories
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CategoryRepository(private val categoryDao: CategoryDao) {
    suspend fun addCategory(categories: CategoryEntity): Long = withContext(Dispatchers.IO) {
        categoryDao.insertCategory(categories)
    }
    // Add a category to the database
    suspend fun addCategories(categories: List<CategoryEntity>) = withContext(Dispatchers.IO) {
        categoryDao.insertCategories(categories)
    }

    // Update an existing category
    suspend fun updateCategory(categories: CategoryEntity) = withContext(Dispatchers.IO) {
        categoryDao.updateCategory(categories)
    }

    // Delete a category from the database
    suspend fun deleteCategory(categories: CategoryEntity) = withContext(Dispatchers.IO) {
        categoryDao.deleteCategory(categories)
    }

    // Fetch all categories
    suspend fun getAllCategories(): List<CategoryEntity> = withContext(Dispatchers.IO) {
        categoryDao.getAllCategories()
    }

    suspend fun getCategoryById(categoryId: Int): CategoryEntity? = withContext(Dispatchers.IO) {
        categoryDao.getCategoryById(categoryId)
    }
    // Update the order of multiple categories
    suspend fun updateCategoryOrder(categories: List<CategoryEntity>) = withContext(Dispatchers.IO) {
        categoryDao.updateCategories(categories)
    }

    // New methods for subcategory support
    suspend fun getCategoriesWithSubCategories(): List<CategoryWithSubCategories> {
        return categoryDao.getCategoriesWithSubCategories()
    }

    suspend fun getCategoryWithSubCategories(categoryId: Int): CategoryWithSubCategories? {
        return categoryDao.getCategoryWithSubCategories(categoryId)
    }

suspend fun deleteCategoryWithMigration(
    sourceCategory: CategoryEntity,
    targetCategoryId: Int,
    transactionRepository: TransactionRepository,
    subCategoryRepository: SubCategoryRepository
) = withContext(Dispatchers.IO) {
    try {
        Log.d("CategoryRepository", "Starting category migration from ${sourceCategory.id} to $targetCategoryId")

        // Step 1: Migrate subcategories first (this also handles their transactions)
        val subCategoryMigrations = subCategoryRepository.migrateSubCategoriesWithDuplicateHandling(
            sourceCategory.id,
            targetCategoryId,
            transactionRepository
        )

        Log.d("CategoryRepository", "Migrated ${subCategoryMigrations.size} subcategories")

        // Step 2: Migrate remaining transactions (those without subcategories)
        transactionRepository.migrateTransactionsToCategory(sourceCategory.id, targetCategoryId)

        // Step 3: Delete the source category
        categoryDao.deleteCategory(sourceCategory)

        Log.d("CategoryRepository", "Successfully completed category migration")

    } catch (e: Exception) {
        Log.e("CategoryRepository", "Error in category migration", e)
        throw e
    }
}

    // Delete category with all transactions
    suspend fun deleteCategoryWithTransactions(
        categoryToDelete: CategoryEntity,
        transactionRepository: TransactionRepository
    ) = withContext(Dispatchers.IO) {
        try {
            // First delete all associated transactions
            transactionRepository.deleteTransactionsByCategoryId(categoryToDelete.id)

            // Then delete the category (this will also cascade delete subcategories)
            categoryDao.deleteCategory(categoryToDelete)

            Log.d("CategoryRepository", "Successfully deleted category ${categoryToDelete.id} with all transactions")
        } catch (e: Exception) {
            Log.e("CategoryRepository", "Error deleting category with transactions", e)
            throw e
        }
    }

    suspend fun deleteCategoryWithTransactionsAndBalanceRestore(
        categoryToDelete: CategoryEntity,
        transactionRepository: TransactionRepository,
        accountRepository: AccountRepository
    ) = withContext(Dispatchers.IO) {
        try {
            Log.d("CategoryRepository", "Deleting category ${categoryToDelete.id} with transaction and balance restoration")

            // Delete all associated transactions with balance restoration
            transactionRepository.deleteTransactionsByCategoryIdWithBalanceRestore(
                categoryToDelete.id,
                accountRepository
            )

            // Delete the category (this will cascade delete subcategories due to foreign key constraints)
            categoryDao.deleteCategory(categoryToDelete)

            Log.d("CategoryRepository", "Successfully deleted category ${categoryToDelete.id} with balance restoration")
        } catch (e: Exception) {
            Log.e("CategoryRepository", "Error deleting category with balance restoration", e)
            throw e
        }
    }
}