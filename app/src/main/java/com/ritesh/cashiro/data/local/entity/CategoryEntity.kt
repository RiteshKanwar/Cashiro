package com.ritesh.cashiro.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.annotation.Keep
@Keep
@Entity(tableName = "category_entity")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val categoryIconId: Int, // Store icon as a string (e.g., Unicode emoji or name of vector asset)
    val boxColor: Int, // Store the background color as an integer (e.g., ARGB)
    val position: Int, // Field to store the category order
    val textColor: Int, // Store the text color as an integer (e.g., ARGB)
)