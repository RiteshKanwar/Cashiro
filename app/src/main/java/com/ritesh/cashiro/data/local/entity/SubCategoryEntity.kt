package com.ritesh.cashiro.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.annotation.Keep
@Keep
@Entity(
    tableName = "subcategory_entity",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE // Optional: deletes subcategories when parent category is deleted
        )
    ],
    indices = [Index("categoryId")] // Adding an index on foreign key for query performance
)
data class SubCategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val categoryId: Int, // Foreign key to link to the parent category
    val subcategoryIconId: Int?, // Optional icon, can be null if using parent's icon
    val boxColor: Int?, // Optional color, can be null if using parent's color
    val position: Int, // Order within the specific category
)