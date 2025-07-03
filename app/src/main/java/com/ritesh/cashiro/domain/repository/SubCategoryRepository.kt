package com.ritesh.cashiro.domain.repository

import android.util.Log
import com.ritesh.cashiro.data.local.dao.SubCategoryDao
import com.ritesh.cashiro.data.local.entity.SubCategoryEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SubCategoryRepository @Inject constructor(
    private val subCategoryDao: SubCategoryDao
) {
    suspend fun insertSubCategory(subCategory: SubCategoryEntity): Long {
        return subCategoryDao.insertSubCategory(subCategory)
    }

    suspend fun deleteSubCategory(subCategory: SubCategoryEntity) {
        subCategoryDao.deleteSubCategory(subCategory)
    }

    suspend fun updateSubCategory(subCategory: SubCategoryEntity) {
        subCategoryDao.updateSubCategory(subCategory)
    }

    suspend fun getAllSubCategories(): List<SubCategoryEntity> {
        return subCategoryDao.getAllSubCategories()
    }

    suspend fun getSubCategoriesByCategoryId(categoryId: Int): List<SubCategoryEntity> {
        return subCategoryDao.getSubCategoriesByCategoryId(categoryId)
    }

    suspend fun getSubCategoryById(id: Int): SubCategoryEntity? {
        return subCategoryDao.getSubCategoryById(id)
    }

    suspend fun updateSubCategories(subCategories: List<SubCategoryEntity>) {
        subCategoryDao.updateSubCategories(subCategories)
    }

    suspend fun insertSubCategories(subCategories: List<SubCategoryEntity>) {
        subCategoryDao.insertSubCategories(subCategories)
    }

    suspend fun deleteSubCategoriesByCategoryId(categoryId: Int) {
        subCategoryDao.deleteSubCategoriesByCategoryId(categoryId)
    }

    // Delete subcategory with all transactions
    suspend fun deleteSubCategoryWithTransactions(
        subCategoryToDelete: SubCategoryEntity,
        transactionRepository: TransactionRepository
    ) {
        try {
            // First delete all associated transactions
            transactionRepository.deleteTransactionsBySubCategoryId(subCategoryToDelete.id)

            // Then delete the subcategory
            subCategoryDao.deleteSubCategory(subCategoryToDelete)

            Log.d("SubCategoryRepository", "Successfully deleted subcategory ${subCategoryToDelete.id} with all transactions")
        } catch (e: Exception) {
            Log.e("SubCategoryRepository", "Error deleting subcategory with transactions", e)
            throw e
        }
    }
    suspend fun migrateSubCategoriesToCategory(
        sourceCategoryId: Int,
        targetCategoryId: Int,
        transactionRepository: TransactionRepository
    ): List<Pair<Int, Int>> { // Returns pairs of (oldId, newId)
        try {
            Log.d(
                "SubCategoryRepository",
                "Migrating subcategories from category $sourceCategoryId to $targetCategoryId"
            )

            // Get subcategories to migrate
            val sourceSubCategories = subCategoryDao.getSubCategoriesByCategoryId(sourceCategoryId)

            if (sourceSubCategories.isEmpty()) {
                Log.d("SubCategoryRepository", "No subcategories to migrate")
                return emptyList()
            }

            // Get existing subcategories in target category to determine starting position
            val targetSubCategories = subCategoryDao.getSubCategoriesByCategoryId(targetCategoryId)
            val startingPosition = targetSubCategories.size

            val migrationMapping = mutableListOf<Pair<Int, Int>>()

            // Create new subcategories in target category
            sourceSubCategories.forEachIndexed { index, subCategory ->
                // Create new subcategory with updated categoryId and position
                val newSubCategory = SubCategoryEntity(
                    id = 0, // Let database generate new ID
                    name = subCategory.name,
                    categoryId = targetCategoryId,
                    boxColor = subCategory.boxColor,
                    subcategoryIconId = subCategory.subcategoryIconId,
                    position = startingPosition + index
                )

                // Insert new subcategory and get the generated ID
                val newId = subCategoryDao.insertSubCategory(newSubCategory)
                migrationMapping.add(Pair(subCategory.id, newId.toInt()))

                Log.d(
                    "SubCategoryRepository",
                    "Migrated subcategory '${subCategory.name}' from ID ${subCategory.id} to ID $newId"
                )
            }

            // Update transactions to reference new subcategory IDs
            val oldIds = migrationMapping.map { it.first }
            val newIds = migrationMapping.map { it.second }
            transactionRepository.updateTransactionsForMigratedSubCategories(
                oldIds, newIds, targetCategoryId
            )

            // Delete old subcategories
            sourceSubCategories.forEach { subCategory ->
                subCategoryDao.deleteSubCategory(subCategory)
            }

            Log.d(
                "SubCategoryRepository",
                "Successfully migrated ${sourceSubCategories.size} subcategories"
            )
            return migrationMapping

        } catch (e: Exception) {
            Log.e("SubCategoryRepository", "Error migrating subcategories", e)
            throw e
        }
    }
    suspend fun deleteSubCategoryWithTransactionsAndBalanceRestore(
        subCategory: SubCategoryEntity,
        transactionRepository: TransactionRepository,
        accountRepository: AccountRepository
    ) = withContext(Dispatchers.IO) {
        try {
            Log.d("SubCategoryRepository", "Deleting subcategory ${subCategory.name} with balance restoration")

            // Delete all associated transactions with balance restoration
            transactionRepository.deleteTransactionsBySubCategoryIdWithBalanceRestore(
                subCategory.id,
                accountRepository
            )

            // Delete the subcategory
            subCategoryDao.deleteSubCategory(subCategory)

            Log.d("SubCategoryRepository", "Successfully deleted subcategory ${subCategory.id} with balance restoration")
        } catch (e: Exception) {
            Log.e("SubCategoryRepository", "Error deleting subcategory with balance restoration", e)
            throw e
        }
    }
    suspend fun deleteSubCategoryWithMigration(
        sourceSubCategory: SubCategoryEntity,
        targetSubCategoryId: Int,
        transactionRepository: TransactionRepository
    ) = withContext(Dispatchers.IO) {
        try {
            Log.d("SubCategoryRepository", "Migrating transactions from subcategory ${sourceSubCategory.id} to $targetSubCategoryId")

            // Update all transactions to use the target subcategory
            transactionRepository.updateTransactionsSubCategory(
                sourceSubCategory.id,
                targetSubCategoryId
            )

            // Delete the source subcategory
            subCategoryDao.deleteSubCategory(sourceSubCategory)

            Log.d("SubCategoryRepository", "Successfully migrated subcategory transactions and deleted source")
        } catch (e: Exception) {
            Log.e("SubCategoryRepository", "Error migrating subcategory", e)
            throw e
        }
    }

    // NEW: Migrate subcategories with duplicate handling
    suspend fun migrateSubCategoriesWithDuplicateHandling(
        sourceCategoryId: Int,
        targetCategoryId: Int,
        transactionRepository: TransactionRepository
    ): List<Pair<Int, Int>> = withContext(Dispatchers.IO) {
        try {
            Log.d("SubCategoryRepository", "Migrating subcategories from category $sourceCategoryId to $targetCategoryId")

            val sourceSubCategories = subCategoryDao.getSubCategoriesByCategoryId(sourceCategoryId)
            val targetSubCategories = subCategoryDao.getSubCategoriesByCategoryId(targetCategoryId)
            val migrations = mutableListOf<Pair<Int, Int>>()

            sourceSubCategories.forEach { sourceSubCategory ->
                // Check if a subcategory with the same name exists in target category
                val existingTarget = targetSubCategories.find { it.name.equals(sourceSubCategory.name, ignoreCase = true) }

                val targetSubCategoryId = if (existingTarget != null) {
                    // Use existing subcategory
                    existingTarget.id
                } else {
                    // Create new subcategory in target category
                    val newSubCategory = sourceSubCategory.copy(
                        id = 0, // Let database generate new ID
                        categoryId = targetCategoryId,
                        position = targetSubCategories.size
                    )
                    subCategoryDao.insertSubCategory(newSubCategory).toInt()
                }

                // Migrate transactions
                transactionRepository.updateTransactionsSubCategory(
                    sourceSubCategory.id,
                    targetSubCategoryId
                )

                migrations.add(sourceSubCategory.id to targetSubCategoryId)

                // Delete the source subcategory
                subCategoryDao.deleteSubCategory(sourceSubCategory)
            }

            Log.d("SubCategoryRepository", "Successfully migrated ${migrations.size} subcategories")
            return@withContext migrations
        } catch (e: Exception) {
            Log.e("SubCategoryRepository", "Error migrating subcategories", e)
            throw e
        }
    }

}