package com.ritesh.cashiro.data.data_management.repository

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.ritesh.cashiro.data.data_management.models.AppBackupData
import com.ritesh.cashiro.data.local.TransactionDatabase
import com.ritesh.cashiro.data.local.dao.AccountDao
import com.ritesh.cashiro.data.local.dao.CategoryDao
import com.ritesh.cashiro.data.local.dao.SubCategoryDao
import com.ritesh.cashiro.data.local.dao.TransactionDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.content.ContentValues
import android.provider.MediaStore
import androidx.compose.ui.graphics.Color
import com.ritesh.cashiro.data.data_management.models.BackupProfile
import com.ritesh.cashiro.data.data_management.models.BackupSettings
import com.ritesh.cashiro.domain.repository.LabelVisibility
import com.ritesh.cashiro.domain.repository.ProfileRepository
import com.ritesh.cashiro.domain.repository.SettingsRepository
import com.ritesh.cashiro.domain.repository.ThemeMode
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat

@Singleton
class DataManagementRepository @Inject constructor(
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val subCategoryDao: SubCategoryDao,
    private val transactionDao: TransactionDao,
    private val database: TransactionDatabase,
    private val settingsRepository: SettingsRepository,
    private val profileRepository: ProfileRepository,
    private val context: Context
) {
    // Enhanced Gson configuration for better type handling
    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .setLenient()
        .serializeNulls()
        .enableComplexMapKeySerialization()
        .create()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())

    suspend fun createBackup(): Result<String> = withContext(Dispatchers.IO) {
        try {
            // Gather database data
            val accounts = accountDao.getAllAccounts()
            val categories = categoryDao.getAllCategories()
            val subCategories = subCategoryDao.getAllSubCategories()
            val transactions = transactionDao.getAllTransactions()

            // Gather settings data
            val settings = settingsRepository.settingsFlow.first()
            val backupSettings = BackupSettings(
                primaryColor = settings.primaryColor.value.toString(),
                labelVisibility = settings.labelVisibility.name,
                themeMode = settings.themeMode.name
            )

            // Gather profile data
            val profileData = profileRepository.getProfileData().first()
            val backupProfile = BackupProfile(
                userName = profileData.userName,
                profileImageFileName = null, // Will be set if image exists
                bannerImageFileName = null, // Will be set if image exists
                profileBackgroundColor = profileData.profileBackgroundColor.value.toString()
            )

            Log.d("Backup", "Backing up: ${accounts.size} accounts, ${categories.size} categories, ${subCategories.size} subcategories, ${transactions.size} transactions")

            // Create temporary directory for zip contents
            val tempDir = File(context.cacheDir, "backup_temp_${System.currentTimeMillis()}")
            tempDir.mkdirs()

            try {
                // Copy images to temp directory and update filenames
                val profileImageFile = copyImageToTemp(profileData.profileImageUri, tempDir, "profile_image")
                val bannerImageFile = copyImageToTemp(profileData.bannerImageUri, tempDir, "banner_image")

                val finalBackupProfile = backupProfile.copy(
                    profileImageFileName = profileImageFile?.name,
                    bannerImageFileName = bannerImageFile?.name
                )

                val backupData = AppBackupData(
                    timestamp = System.currentTimeMillis(),
                    appVersion = getAppVersion(),
                    accounts = accounts,
                    categories = categories,
                    subCategories = subCategories,
                    transactions = transactions,
                    settings = backupSettings,
                    profile = finalBackupProfile
                )

                // Create JSON file in temp directory
                val jsonFile = File(tempDir, "backup_data.json")
                val backupJson = gson.toJson(backupData, AppBackupData::class.java)
                jsonFile.writeText(backupJson, Charsets.UTF_8)

                // Create zip file
                val zipFileName = "Cashiro_Backup_${dateFormat.format(Date())}.zip"
                val zipFilePath = createZipFile(tempDir, zipFileName)

                Log.d("Backup", "Backup created: $zipFileName")

                // Create detailed success message with file info
                val successMessage = buildString {
                    appendLine("✅ Backup Created Successfully!")
                    appendLine()
                    appendLine("📁 File Details:")
                    appendLine("• Name: $zipFileName")
                    appendLine("• Location: Downloads folder")
                    appendLine("• Full Path: $zipFilePath")
                    appendLine()
                    appendLine("📊 Backup Contents:")
                    appendLine("• ${accounts.size} accounts")
                    appendLine("• ${categories.size} categories")
                    appendLine("• ${subCategories.size} subcategories")
                    appendLine("• ${transactions.size} transactions")
                    appendLine("• User settings & preferences")
                    appendLine("• Profile data & images")
                    appendLine()
                    appendLine("💡 You can share this file to transfer data between devices!")
                }

                Result.success(successMessage)
            } finally {
                // Clean up temp directory
                tempDir.deleteRecursively()
            }
        } catch (e: Exception) {
            Log.e("Backup", "Backup failed", e)
            Result.failure(e)
        }
    }

    private fun copyImageToTemp(imageUri: Uri?, tempDir: File, baseName: String): File? {
        if (imageUri == null) return null

        try {
            val inputStream = context.contentResolver.openInputStream(imageUri) ?: return null
            val extension = getImageExtension(imageUri)
            val imageFile = File(tempDir, "$baseName.$extension")

            inputStream.use { input ->
                imageFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            return if (imageFile.exists() && imageFile.length() > 0) imageFile else null
        } catch (e: Exception) {
            Log.e("Backup", "Failed to copy image: $imageUri", e)
            return null
        }
    }

    private fun getImageExtension(uri: Uri): String {
        val mimeType = context.contentResolver.getType(uri)
        return when (mimeType) {
            "image/jpeg", "image/jpg" -> "jpg"
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg" // Default fallback
        }
    }

    private fun createZipFile(sourceDir: File, zipFileName: String): String {
        val zipFilePath = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            createZipFileScoped(sourceDir, zipFileName)
        } else {
            createZipFileLegacy(sourceDir, zipFileName)
        }
        return zipFilePath
    }

    private fun createZipFileScoped(sourceDir: File, zipFileName: String): String {
        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, zipFileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/zip")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    createZipStream(sourceDir, outputStream)
                }
                // For Android 10+, return the Downloads path with filename
                val downloadsPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath
                return "$downloadsPath/$zipFileName"
            }
            return "Failed to save zip file"
        } catch (e: Exception) {
            Log.e("Backup", "Error creating zip file", e)
            return "Error: ${e.message}"
        }
    }

    private fun createZipFileLegacy(sourceDir: File, zipFileName: String): String {
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            val zipFile = File(downloadsDir, zipFileName)
            zipFile.outputStream().use { outputStream ->
                createZipStream(sourceDir, outputStream)
            }
            return zipFile.absolutePath
        } catch (e: Exception) {
            Log.e("Backup", "Error creating zip file legacy", e)
            return "Error: ${e.message}"
        }
    }

    private fun createZipStream(sourceDir: File, outputStream: OutputStream) {
        ZipOutputStream(outputStream).use { zipOut ->
            sourceDir.listFiles()?.forEach { file ->
                if (file.isFile) {
                    val zipEntry = ZipEntry(file.name)
                    zipOut.putNextEntry(zipEntry)
                    file.inputStream().use { input ->
                        input.copyTo(zipOut)
                    }
                    zipOut.closeEntry()
                }
            }
        }
    }

    suspend fun restoreBackup(uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Cannot open backup file"))

            // Check if it's a zip file or JSON file
            val isZipFile = isZipFile(uri)

            if (isZipFile) {
                restoreFromZipFile(inputStream)
            } else {
                // Legacy JSON backup
                restoreFromJsonFile(inputStream)
            }

            Log.d("Restore", "Restore completed successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("Restore", "Restore failed", e)
            Result.failure(Exception("Restore failed: ${e.message}"))
        }
    }

    private fun isZipFile(uri: Uri): Boolean {
        val mimeType = context.contentResolver.getType(uri)
        return mimeType == "application/zip" ||
                mimeType == "application/x-zip-compressed" ||
                uri.toString().endsWith(".zip", ignoreCase = true)
    }

    private suspend fun restoreFromZipFile(inputStream: InputStream) {
        // Create temp directory for extraction
        val tempDir = File(context.cacheDir, "restore_temp_${System.currentTimeMillis()}")
        tempDir.mkdirs()

        try {
            // Extract zip file
            ZipInputStream(inputStream).use { zipIn ->
                var entry = zipIn.nextEntry
                while (entry != null) {
                    val file = File(tempDir, entry.name)
                    if (!entry.isDirectory) {
                        file.outputStream().use { output ->
                            zipIn.copyTo(output)
                        }
                    }
                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                }
            }

            // Read JSON file
            val jsonFile = File(tempDir, "backup_data.json")
            if (!jsonFile.exists()) {
                throw Exception("Backup data file not found in zip")
            }

            val backupJson = jsonFile.readText(Charsets.UTF_8)
            val backupData = parseBackupData(backupJson)

            // Restore images first
            val restoredProfileUri = restoreImageFile(tempDir, backupData.profile.profileImageFileName, "profile_image")
            val restoredBannerUri = restoreImageFile(tempDir, backupData.profile.bannerImageFileName, "banner_image")

            // Restore database data
            restoreDataWithForeignKeyHandling(backupData)

            // Restore settings
            restoreSettings(backupData.settings)

            // Restore profile data
            restoreProfile(backupData.profile, restoredProfileUri, restoredBannerUri)

        } finally {
            // Clean up temp directory
            tempDir.deleteRecursively()
        }
    }

    suspend fun restoreFromJsonFile(inputStream: InputStream) {
            val backupJson = inputStream.bufferedReader().use { it.readText() }

            if (backupJson.isNullOrEmpty()) {
                throw Exception("Invalid backup file - file is empty")
            }

            val backupData = parseBackupData(backupJson)

            // For legacy JSON files, only restore database data
            restoreDataWithForeignKeyHandling(backupData)
    }


    suspend fun verifyBackupCompleteness(): String = withContext(Dispatchers.IO) {
        try {
            val accounts = accountDao.getAllAccounts()
            val categories = categoryDao.getAllCategories()
            val subCategories = subCategoryDao.getAllSubCategories()
            val transactions = transactionDao.getAllTransactions()

            """
            Current Database State:
            =====================
            Accounts: ${accounts.size}
            Categories: ${categories.size}
            SubCategories: ${subCategories.size}
            Transactions: ${transactions.size}
            
            Sample Data:
            - First Account: ${accounts.firstOrNull()?.accountName ?: "None"}
            - First Category: ${categories.firstOrNull()?.name ?: "None"}
            - First Transaction: ${transactions.firstOrNull()?.title ?: "None"}
            """.trimIndent()
        } catch (e: Exception) {
            "Verification failed: ${e.message}"
        }
    }
    private fun parseBackupData(backupJson: String): AppBackupData {
        Log.d("Restore", "Backup JSON preview: ${backupJson.take(200)}...")

        return try {
            val typeToken = object : TypeToken<AppBackupData>() {}.type
            val data = gson.fromJson<AppBackupData>(backupJson, typeToken)

            if (data == null) {
                throw Exception("Invalid backup file - corrupted data")
            }

            if (data.version > 2) {
                throw Exception("Backup version ${data.version} not supported")
            }

            data
        } catch (e: Exception) {
            Log.e("Restore", "JSON parsing error", e)
            throw Exception("Invalid backup file format: ${e.message}")
        }
    }
    private fun restoreImageFile(tempDir: File, fileName: String?, baseName: String): Uri? {
        if (fileName == null) return null

        val sourceFile = File(tempDir, fileName)
        if (!sourceFile.exists()) return null

        try {
            // Copy to internal storage
            val internalDir = File(context.filesDir, "profile_images")
            internalDir.mkdirs()

            val extension = fileName.substringAfterLast(".", "jpg")
            val destFile = File(internalDir, "${baseName}_${System.currentTimeMillis()}.$extension")

            sourceFile.copyTo(destFile, overwrite = true)

            return Uri.fromFile(destFile)
        } catch (e: Exception) {
            Log.e("Restore", "Failed to restore image: $fileName", e)
            return null
        }
    }

    private suspend fun restoreSettings(backupSettings: BackupSettings) {
        try {
            // Parse and restore primary color
            val primaryColor = Color(backupSettings.primaryColor.toULong())
            settingsRepository.updatePrimaryColor(primaryColor)

            // Parse and restore label visibility
            val labelVisibility = LabelVisibility.valueOf(backupSettings.labelVisibility)
            settingsRepository.updateLabelVisibility(labelVisibility)

            // Parse and restore theme mode
            val themeMode = ThemeMode.valueOf(backupSettings.themeMode)
            settingsRepository.updateThemeMode(themeMode)

            Log.d("Restore", "Settings restored successfully")
        } catch (e: Exception) {
            Log.e("Restore", "Failed to restore settings", e)
            // Don't fail the entire restore for settings issues
        }
    }

    private suspend fun restoreProfile(backupProfile: BackupProfile, profileImageUri: Uri?, bannerImageUri: Uri?) {
        try {
            // Restore user name
            profileRepository.saveUserName(backupProfile.userName)

            // Restore profile background color
            val backgroundColor = Color(backupProfile.profileBackgroundColor.toULong())
            profileRepository.saveProfileBackgroundColor(backgroundColor)

            // Restore image URIs
            profileRepository.saveProfileImageUri(profileImageUri)
            profileRepository.saveBannerImageUri(bannerImageUri)

            Log.d("Restore", "Profile data restored successfully")
        } catch (e: Exception) {
            Log.e("Restore", "Failed to restore profile data", e)
            // Don't fail the entire restore for profile issues
        }
    }

    private suspend fun restoreDataWithForeignKeyHandling(backupData: AppBackupData) {
        // Disable foreign key constraints temporarily
        database.openHelper.writableDatabase.execSQL("PRAGMA foreign_keys=OFF;")

        try {
            // Use withTransaction for suspend function support
            database.withTransaction {
                // Clear all existing data first
                clearAllDataForRestore()

                // Create ID mappings for foreign key relationships
                val accountIdMap = mutableMapOf<Int, Int>()
                val categoryIdMap = mutableMapOf<Int, Int>()
                val subCategoryIdMap = mutableMapOf<Int, Int>()

                // Restore accounts first and create ID mapping
                if (backupData.accounts.isNotEmpty()) {
                    backupData.accounts.forEach { account ->
                        val oldId = account.id
                        val newId = accountDao.insertAccount(account.copy(id = 0)).toInt()
                        accountIdMap[oldId] = newId
                        Log.d("Restore", "Account ID mapping: $oldId -> $newId (${account.accountName})")
                    }
                }

                // Restore categories and create ID mapping
                if (backupData.categories.isNotEmpty()) {
                    backupData.categories.forEach { category ->
                        val oldId = category.id
                        val newId = categoryDao.insertCategory(category.copy(id = 0)).toInt()
                        categoryIdMap[oldId] = newId
                        Log.d("Restore", "Category ID mapping: $oldId -> $newId (${category.name})")
                    }
                }

                // Restore subcategories with updated foreign keys
                if (backupData.subCategories.isNotEmpty()) {
                    backupData.subCategories.forEach { subCategory ->
                        val oldId = subCategory.id
                        val newCategoryId = categoryIdMap[subCategory.categoryId] ?: subCategory.categoryId
                        val newId = subCategoryDao.insertSubCategory(
                            subCategory.copy(
                                id = 0,
                                categoryId = newCategoryId
                            )
                        ).toInt()
                        subCategoryIdMap[oldId] = newId
                        Log.d("Restore", "SubCategory ID mapping: $oldId -> $newId (${subCategory.name})")
                    }
                }

                // Restore transactions with updated foreign keys
                if (backupData.transactions.isNotEmpty()) {
                    backupData.transactions.forEach { transaction ->
                        val newAccountId = accountIdMap[transaction.accountId] ?: transaction.accountId
                        val newCategoryId = categoryIdMap[transaction.categoryId] ?: transaction.categoryId
                        val newSubCategoryId = transaction.subCategoryId?.let {
                            subCategoryIdMap[it] ?: it
                        }
                        val newDestinationAccountId = transaction.destinationAccountId?.let {
                            accountIdMap[it] ?: it
                        }

                        val updatedTransaction = transaction.copy(
                            id = 0,
                            accountId = newAccountId,
                            categoryId = newCategoryId,
                            subCategoryId = newSubCategoryId,
                            destinationAccountId = newDestinationAccountId
                        )

                        transactionDao.insertTransaction(updatedTransaction)
                        Log.d("Restore", "Transaction restored: ${transaction.title}")
                    }
                }
            }
        } finally {
            // Re-enable foreign key constraints
            database.openHelper.writableDatabase.execSQL("PRAGMA foreign_keys=ON;")
        }
    }

    private suspend fun clearAllDataForRestore() {
        try {
            // Use raw SQL for faster deletion when foreign keys are disabled
            database.openHelper.writableDatabase.execSQL("DELETE FROM transaction_entity")
            database.openHelper.writableDatabase.execSQL("DELETE FROM subcategory_entity")
            database.openHelper.writableDatabase.execSQL("DELETE FROM category_entity")
            database.openHelper.writableDatabase.execSQL("DELETE FROM account_entity")

            // Reset auto-increment counters
            database.openHelper.writableDatabase.execSQL("UPDATE sqlite_sequence SET seq = 0 WHERE name = 'transaction_entity'")
            database.openHelper.writableDatabase.execSQL("UPDATE sqlite_sequence SET seq = 0 WHERE name = 'subcategory_entity'")
            database.openHelper.writableDatabase.execSQL("UPDATE sqlite_sequence SET seq = 0 WHERE name = 'category_entity'")
            database.openHelper.writableDatabase.execSQL("UPDATE sqlite_sequence SET seq = 0 WHERE name = 'account_entity'")

            Log.d("Restore", "All existing data cleared and sequences reset")
        } catch (e: Exception) {
            Log.e("Restore", "Error clearing data with SQL, using DAO methods", e)
            try {
                val transactions = transactionDao.getAllTransactions()
                transactions.forEach { transactionDao.deleteTransaction(it) }

                val subCategories = subCategoryDao.getAllSubCategories()
                subCategories.forEach { subCategoryDao.deleteSubCategory(it) }

                val categories = categoryDao.getAllCategories()
                categories.forEach { categoryDao.deleteCategory(it) }

                val accounts = accountDao.getAllAccounts()
                accounts.forEach { accountDao.deleteAccount(it) }

                Log.d("Restore", "Data cleared using DAO methods")
            } catch (daoException: Exception) {
                Log.e("Restore", "Error clearing data with DAO methods", daoException)
                throw daoException
            }
        }
    }
    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }
}