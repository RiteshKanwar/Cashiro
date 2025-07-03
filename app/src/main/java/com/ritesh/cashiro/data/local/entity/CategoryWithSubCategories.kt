package com.ritesh.cashiro.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

data class CategoryWithSubCategories(
    @Embedded val category: CategoryEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "categoryId"
    )
    val subCategories: List<SubCategoryEntity>
)
