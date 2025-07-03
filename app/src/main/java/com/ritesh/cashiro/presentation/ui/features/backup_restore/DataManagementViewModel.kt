package com.ritesh.cashiro.presentation.ui.features.backup_restore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.net.Uri
import android.util.Log
import androidx.compose.ui.unit.dp
import com.ritesh.cashiro.data.data_management.repository.DataManagementRepository
import com.ritesh.cashiro.domain.utils.ActivityLogUtils
import com.ritesh.cashiro.presentation.ui.features.activity_logs.ActivityActionType
import kotlinx.coroutines.flow.update

@HiltViewModel
class DataManagementViewModel @Inject constructor(
    private val dataManagementRepository: DataManagementRepository,
    private val activityLogUtils: ActivityLogUtils
) : ViewModel() {

    private val _state = MutableStateFlow(DataManagementState())
    val state: StateFlow<DataManagementState> = _state.asStateFlow()

    fun createBackup() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                operationInProgress = "Creating complete backup...",
                error = null,
                successMessage = null
            )

            // First verify what we're backing up
            val verification = dataManagementRepository.verifyBackupCompleteness()
            Log.d("BackupViewModel", verification)

            dataManagementRepository.createBackup()
                .onSuccess { successMessage ->
                    // ENHANCED: Log backup creation to ActivityLog
                    activityLogUtils.logSystemAction(
                        ActivityActionType.DATA_BACKUP_CREATED,
                        "Complete data backup created successfully"
                    )

                    _state.value = _state.value.copy(
                        isLoading = false,
                        operationInProgress = null,
                        lastBackupDate = System.currentTimeMillis(),
                        successMessage = successMessage
                    )
                }
                .onFailure { exception ->
                    // ENHANCED: Log backup failure to ActivityLog
                    activityLogUtils.logSystemAction(
                        ActivityActionType.DATA_BACKUP_CREATED,
                        "Backup creation failed: ${exception.message}"
                    )

                    _state.value = _state.value.copy(
                        isLoading = false,
                        operationInProgress = null,
                        error = "Failed to create backup: ${exception.message}"
                    )
                }
        }
    }

    fun restoreBackup(uri: Uri) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                operationInProgress = "Restoring backup...",
                error = null,
                successMessage = null
            )

            dataManagementRepository.restoreBackup(uri)
                .onSuccess {
                    // ENHANCED: Log backup restoration to ActivityLog
                    activityLogUtils.logSystemAction(
                        ActivityActionType.DATA_RESTORED,
                        "Data successfully restored from backup file"
                    )

                    // Create detailed success message for restore
                    val verification = dataManagementRepository.verifyBackupCompleteness()
                    Log.d("RestoreViewModel", verification)

                    val restoreSuccessMessage = buildString {
                        appendLine("✅ Backup Restored Successfully!")
                        appendLine()
                        appendLine("🔄 Your app has been updated with the backup data.")
                        appendLine()
                        appendLine("📊 Current Database State:")
                        appendLine(verification)
                        appendLine()
                        appendLine("💡 All your transactions, accounts, settings, and profile data have been restored!")
                    }

                    _state.value = _state.value.copy(
                        isLoading = false,
                        operationInProgress = null,
                        successMessage = restoreSuccessMessage
                    )
                }
                .onFailure { exception ->
                    // ENHANCED: Log backup restoration failure to ActivityLog
                    activityLogUtils.logSystemAction(
                        ActivityActionType.DATA_RESTORED,
                        "Data restoration failed: ${exception.message}"
                    )

                    val errorMessage = when {
                        exception.message?.contains("zip", ignoreCase = true) == true ->
                            "❌ Failed to restore backup: ${exception.message}\n\n💡 Tip: Make sure you selected a valid backup file (.zip or .json)\n\n📱 Try: Use your file manager to locate the backup file in Downloads folder"
                        exception.message?.contains("json", ignoreCase = true) == true ->
                            "❌ Failed to restore backup: Invalid backup file format.\n\n📁 Please select a valid Ruppin backup file:\n• .zip files (new format)\n• .json files (legacy format)"
                        exception.message?.contains("open", ignoreCase = true) == true ->
                            "❌ Cannot open the selected file.\n\n💡 Possible solutions:\n• Try selecting the file again\n• Make sure the file isn't corrupted\n• Use a file manager to locate the backup file"
                        else -> "❌ Failed to restore backup: ${exception.message}\n\n💡 If you're having trouble selecting ZIP files, try:\n• Using your file manager app\n• Looking in Downloads folder\n• Selecting 'All files' when browsing"
                    }

                    _state.value = _state.value.copy(
                        isLoading = false,
                        operationInProgress = null,
                        error = errorMessage
                    )
                }
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(error = null, successMessage = null)
    }

    fun updateCollapsingFraction(fraction: Float) {
        if (!fraction.isNaN()) {
            val maxOffset = 180.dp
            val minOffset = 0.dp
            val offsetRange = maxOffset - minOffset
            val currentOffset = maxOffset - (offsetRange * fraction)

            _state.update {
                it.copy(
                    collapsingFraction = fraction,
                    currentOffset = currentOffset
                )
            }
        }
    }
}

//@HiltViewModel
//class DataManagementViewModel @Inject constructor(
//    private val dataManagementRepository: DataManagementRepository
//) : ViewModel() {
//
//    private val _state = MutableStateFlow(DataManagementState())
//    val state: StateFlow<DataManagementState> = _state.asStateFlow()
//
//    fun createBackup() {
//        viewModelScope.launch {
//            _state.value = _state.value.copy(
//                isLoading = true,
//                operationInProgress = "Creating complete backup...",
//                error = null,
//                successMessage = null
//            )
//
//            // First verify what we're backing up
//            val verification = dataManagementRepository.verifyBackupCompleteness()
//            Log.d("BackupViewModel", verification)
//
//            dataManagementRepository.createBackup()
//                .onSuccess { successMessage ->
//                    _state.value = _state.value.copy(
//                        isLoading = false,
//                        operationInProgress = null,
//                        lastBackupDate = System.currentTimeMillis(),
//                        successMessage = successMessage // Use the detailed message from repository
//                    )
//                }
//                .onFailure { exception ->
//                    _state.value = _state.value.copy(
//                        isLoading = false,
//                        operationInProgress = null,
//                        error = "Failed to create backup: ${exception.message}"
//                    )
//                }
//        }
//    }
//
//    fun restoreBackup(uri: Uri) {
//        viewModelScope.launch {
//            _state.value = _state.value.copy(
//                isLoading = true,
//                operationInProgress = "Restoring backup...",
//                error = null,
//                successMessage = null
//            )
//
//            dataManagementRepository.restoreBackup(uri)
//                .onSuccess {
//                    // Create detailed success message for restore
//                    val verification = dataManagementRepository.verifyBackupCompleteness()
//                    Log.d("RestoreViewModel", verification)
//
//                    val restoreSuccessMessage = buildString {
//                        appendLine("✅ Backup Restored Successfully!")
//                        appendLine()
//                        appendLine("🔄 Your app has been updated with the backup data.")
//                        appendLine()
//                        appendLine("📊 Current Database State:")
//                        appendLine(verification)
//                        appendLine()
//                        appendLine("💡 All your transactions, accounts, settings, and profile data have been restored!")
//                    }
//
//                    _state.value = _state.value.copy(
//                        isLoading = false,
//                        operationInProgress = null,
//                        successMessage = restoreSuccessMessage
//                    )
//                }
//                .onFailure { exception ->
//                    val errorMessage = when {
//                        exception.message?.contains("zip", ignoreCase = true) == true ->
//                            "❌ Failed to restore backup: ${exception.message}\n\n💡 Tip: Make sure you selected a valid backup file (.zip or .json)\n\n📱 Try: Use your file manager to locate the backup file in Downloads folder"
//                        exception.message?.contains("json", ignoreCase = true) == true ->
//                            "❌ Failed to restore backup: Invalid backup file format.\n\n📁 Please select a valid Ruppin backup file:\n• .zip files (new format)\n• .json files (legacy format)"
//                        exception.message?.contains("open", ignoreCase = true) == true ->
//                            "❌ Cannot open the selected file.\n\n💡 Possible solutions:\n• Try selecting the file again\n• Make sure the file isn't corrupted\n• Use a file manager to locate the backup file"
//                        else -> "❌ Failed to restore backup: ${exception.message}\n\n💡 If you're having trouble selecting ZIP files, try:\n• Using your file manager app\n• Looking in Downloads folder\n• Selecting 'All files' when browsing"
//                    }
//
//                    _state.value = _state.value.copy(
//                        isLoading = false,
//                        operationInProgress = null,
//                        error = errorMessage
//                    )
//                }
//        }
//    }
//
//    fun clearMessages() {
//        _state.value = _state.value.copy(error = null, successMessage = null)
//    }
//
//    fun updateCollapsingFraction(fraction: Float) {
//        if (!fraction.isNaN()) {
//            val maxOffset = 180.dp
//            val minOffset = 0.dp
//            val offsetRange = maxOffset - minOffset
//            val currentOffset = maxOffset - (offsetRange * fraction)
//
//            _state.update {
//                it.copy(
//                    collapsingFraction = fraction,
//                    currentOffset = currentOffset
//                )
//            }
//        }
//    }
//}