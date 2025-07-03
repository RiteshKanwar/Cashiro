package com.ritesh.cashiro.data.local.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ritesh.cashiro.presentation.ui.features.activity_logs.ActivityActionType

@Entity(
    tableName = "activity_logs",
    indices = [
        Index(value = ["timestamp"]), // For efficient date-based queries
        Index(value = ["actionType"]), // For filtering by action type
        Index(value = ["relatedAccountId"]), // For account-specific queries
        Index(value = ["relatedTransactionId"]), // For transaction-specific queries
        Index(value = ["relatedCategoryId"]) // For category-specific queries
    ],
//    foreignKeys = [
//        ForeignKey(
//            entity = TransactionEntity::class,
//            parentColumns = ["id"],
//            childColumns = ["relatedTransactionId"],
//            onDelete = ForeignKey.SET_NULL
//        ),
//        ForeignKey(
//            entity = AccountEntity::class,
//            parentColumns = ["id"],
//            childColumns = ["relatedAccountId"],
//            onDelete = ForeignKey.SET_NULL
//        ),
//        ForeignKey(
//            entity = CategoryEntity::class,
//            parentColumns = ["id"],
//            childColumns = ["relatedCategoryId"],
//            onDelete = ForeignKey.SET_NULL
//        )
//    ]
)
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val actionType: ActivityActionType,

    val title: String,

    val description: String = "",

    val timestamp: Long = System.currentTimeMillis(),

    val relatedTransactionId: Int? = null,

    val relatedAccountId: Int? = null,

    val relatedCategoryId: Int? = null,

    val amount: Double? = null,

    val oldValue: String? = null,

    val newValue: String? = null,

    val metadata: Map<String, String> = emptyMap()
)