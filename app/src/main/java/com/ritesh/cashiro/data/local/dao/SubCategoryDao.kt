package com.ritesh.cashiro.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ritesh.cashiro.data.local.entity.SubCategoryEntity

@Dao
interface SubCategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubCategory(subCategory: SubCategoryEntity): Long

    @Delete
    suspend fun deleteSubCategory(subCategory: SubCategoryEntity)

    @Update
    suspend fun updateSubCategory(subCategory: SubCategoryEntity)

    @Query("SELECT * FROM subcategory_entity")
    suspend fun getAllSubCategories(): List<SubCategoryEntity>

    @Query("SELECT * FROM subcategory_entity WHERE categoryId = :categoryId ORDER BY position")
    suspend fun getSubCategoriesByCategoryId(categoryId: Int): List<SubCategoryEntity>

    @Query("SELECT * FROM subcategory_entity WHERE id = :subCategoryId")
    suspend fun getSubCategoryById(subCategoryId: Int): SubCategoryEntity?

    @Update
    suspend fun updateSubCategories(subCategories: List<SubCategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubCategories(subCategories: List<SubCategoryEntity>)

    @Query("DELETE FROM subcategory_entity WHERE categoryId = :categoryId")
    suspend fun deleteSubCategoriesByCategoryId(categoryId: Int)
}