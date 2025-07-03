package com.ritesh.cashiro.data.local.entity

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey

@Keep
@Entity(tableName = "account_entity")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val accountName: String,
    val cardColor1: Int,
    val cardColor2: Int,
    val balance: Double,
    val isMainAccount: Boolean,
    val position: Int,
    var currencyCode: String
)