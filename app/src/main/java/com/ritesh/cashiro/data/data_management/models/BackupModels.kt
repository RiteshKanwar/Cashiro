package com.ritesh.cashiro.data.data_management.models

import com.ritesh.cashiro.data.local.entity.AccountEntity
import com.ritesh.cashiro.data.local.entity.CategoryEntity
import com.ritesh.cashiro.data.local.entity.SubCategoryEntity
import com.ritesh.cashiro.data.local.entity.TransactionEntity
import androidx.annotation.Keep
@Keep
data class AppBackupData(
    val version: Int = 2, // Incremented version for new format
    val timestamp: Long,
    val appVersion: String,
    val accounts: List<AccountEntity>,
    val categories: List<CategoryEntity>,
    val subCategories: List<SubCategoryEntity>,
    val transactions: List<TransactionEntity>,

    // New fields for enhanced backup
    val settings: BackupSettings,
    val profile: BackupProfile
)

@Keep
data class BackupSettings(
    val primaryColor: String, // Color value as string
    val labelVisibility: String, // LabelVisibility enum name
    val themeMode: String // ThemeMode enum name
)

@Keep
data class BackupProfile(
    val userName: String,
    val profileImageFileName: String?, // Filename in zip, not URI
    val bannerImageFileName: String?, // Filename in zip, not URI
    val profileBackgroundColor: String // Color value as string
)




//@Keep
//data class AppBackupData(
//    val version: Int = 1,
//    val timestamp: Long,
//    val appVersion: String,
//    val accounts: List<AccountEntity>,
//    val categories: List<CategoryEntity>,
//    val subCategories: List<SubCategoryEntity>,
//    val transactions: List<TransactionEntity>
//)