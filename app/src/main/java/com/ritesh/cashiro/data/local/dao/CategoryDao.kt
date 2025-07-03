package com.ritesh.cashiro.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.ritesh.cashiro.data.local.entity.CategoryEntity
import com.ritesh.cashiro.data.local.entity.CategoryWithSubCategories

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Query("SELECT * FROM category_entity")
    suspend fun getAllCategories(): List<CategoryEntity>

    @Query("SELECT * FROM category_entity WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: Int): CategoryEntity?

    @Update
    suspend fun updateCategories(categories: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Transaction
    @Query("SELECT * FROM category_entity")
    suspend fun getCategoriesWithSubCategories(): List<CategoryWithSubCategories>

    @Transaction
    @Query("SELECT * FROM category_entity WHERE id = :categoryId")
    suspend fun getCategoryWithSubCategories(categoryId: Int): CategoryWithSubCategories?
}