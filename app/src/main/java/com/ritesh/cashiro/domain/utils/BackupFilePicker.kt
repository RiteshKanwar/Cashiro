package com.ritesh.cashiro.domain.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.result.ActivityResultLauncher

object BackupFilePicker {

    /**
     * Launches file picker with multiple fallback options to ensure ZIP files can be selected
     */
    fun launchFilePickerForBackup(
        launcher: ActivityResultLauncher<String>,
        context: Context
    ) {
        val mimeTypes = listOf(
            "application/zip",
            "application/x-zip-compressed",
            "application/octet-stream",
            "application/json"
        )

        // Try each MIME type until one works
        var launched = false
        for (mimeType in mimeTypes) {
            try {
                launcher.launch(mimeType)
                launched = true
                Log.d("FilePicker", "Successfully launched with MIME type: $mimeType")
                break
            } catch (e: Exception) {
                Log.w("FilePicker", "Failed to launch with MIME type $mimeType: ${e.message}")
                continue
            }
        }

        // Ultimate fallback - all files
        if (!launched) {
            try {
                launcher.launch("*/*")
                Log.d("FilePicker", "Launched with fallback: all files")
            } catch (e: Exception) {
                Log.e("FilePicker", "Failed to launch file picker entirely: ${e.message}")
                // Could show a toast or error message to user here
            }
        }
    }

    /**
     * Validates if the selected file is a valid backup file
     */
    fun isValidBackupFile(uri: Uri, context: Context): ValidationResult {
        try {
            val fileName = getFileName(uri, context)
            val mimeType = context.contentResolver.getType(uri)

            return when {
                fileName?.endsWith(".zip", ignoreCase = true) == true ->
                    ValidationResult.Valid("ZIP backup file")
                fileName?.endsWith(".json", ignoreCase = true) == true ->
                    ValidationResult.Valid("JSON backup file")
                mimeType == "application/zip" || mimeType == "application/x-zip-compressed" ->
                    ValidationResult.Valid("ZIP backup file")
                mimeType == "application/json" ->
                    ValidationResult.Valid("JSON backup file")
                else ->
                    ValidationResult.Warning("File type not recognized, but will attempt to restore")
            }
        } catch (e: Exception) {
            return ValidationResult.Error("Unable to validate file: ${e.message}")
        }
    }

    private fun getFileName(uri: Uri, context: Context): String? {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                cursor.getString(nameIndex)
            } else null
        }
    }

    sealed class ValidationResult {
        data class Valid(val message: String) : ValidationResult()
        data class Warning(val message: String) : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }
}