package com.ritesh.cashiro.presentation.ui.features.backup_restore

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class DataManagementState(
    val isLoading: Boolean = false,
    val operationInProgress: String? = null,
    val lastBackupDate: Long? = null,
    val error: String? = null,
    val successMessage: String? = null,
    val collapsingFraction: Float = 0f,
    val currentOffset: Dp = 180.dp,
)
