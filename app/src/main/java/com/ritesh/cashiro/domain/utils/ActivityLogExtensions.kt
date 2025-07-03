package com.ritesh.cashiro.domain.utils

import com.ritesh.cashiro.data.local.entity.ActivityLogEntity
import com.ritesh.cashiro.presentation.ui.features.activity_logs.ActivityLogEntry

// Extension functions for conversion between Entity and Entry
fun ActivityLogEntity.toActivityLogEntry(): ActivityLogEntry {
    return ActivityLogEntry(
        id = this.id,
        actionType = this.actionType,
        title = this.title,
        description = this.description,
        timestamp = this.timestamp,
        relatedTransactionId = this.relatedTransactionId,
        relatedAccountId = this.relatedAccountId,
        relatedCategoryId = this.relatedCategoryId,
        amount = this.amount,
        oldValue = this.oldValue,
        newValue = this.newValue,
        metadata = this.metadata
    )
}

fun ActivityLogEntry.toActivityLogEntity(): ActivityLogEntity {
    return ActivityLogEntity(
        id = this.id,
        actionType = this.actionType,
        title = this.title,
        description = this.description,
        timestamp = this.timestamp,
        relatedTransactionId = this.relatedTransactionId,
        relatedAccountId = this.relatedAccountId,
        relatedCategoryId = this.relatedCategoryId,
        amount = this.amount,
        oldValue = this.oldValue,
        newValue = this.newValue,
        metadata = this.metadata
    )
}