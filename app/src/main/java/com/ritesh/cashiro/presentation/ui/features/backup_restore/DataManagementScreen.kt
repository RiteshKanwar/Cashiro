package com.ritesh.cashiro.presentation.ui.features.backup_restore

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.overscroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritesh.cashiro.domain.utils.BackupFilePicker
import com.ritesh.cashiro.domain.utils.PermissionUtils
import com.ritesh.cashiro.presentation.effects.VerticalStretchedOverscroll
import com.ritesh.cashiro.presentation.ui.extras.components.extras.CustomTitleTopAppBar
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import com.ritesh.cashiro.R
import com.ritesh.cashiro.presentation.ui.theme.Macchiato_Green_Dim

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataManagementScreen(
    screenTitle: String,
    previousScreenTitle: String,
    onBackClick: () -> Unit,
    dataManagementViewModel: DataManagementViewModel,
) {
    val state by dataManagementViewModel.state.collectAsState()
    DataManagementContent(
        dataManagementUiState = state,
        restoreBackup = dataManagementViewModel::restoreBackup,
        createBackup = dataManagementViewModel::createBackup,
        clearMessages = dataManagementViewModel::clearMessages,
        updateCollapsingFraction = dataManagementViewModel::updateCollapsingFraction,
        screenTitle = screenTitle,
        previousScreenTitle = previousScreenTitle,
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable

private fun DataManagementContent(
    dataManagementUiState: DataManagementState,
    restoreBackup: (uri: Uri) -> Unit,
    createBackup: () -> Unit,
    clearMessages: () -> Unit,
    updateCollapsingFraction: (Float) -> Unit,
    screenTitle: String,
    previousScreenTitle: String,
    onBackClick: () -> Unit,
){
    val context = LocalContext.current
    val hazeState = remember { HazeState() }

    val backupRestoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { selectedUri ->
            // Validate the selected file
            when (val validation = BackupFilePicker.isValidBackupFile(selectedUri, context)) {
                is BackupFilePicker.ValidationResult.Valid -> {
                    Log.d("FilePicker", "Valid file selected: ${validation.message}")
                    restoreBackup(selectedUri)
                }
                is BackupFilePicker.ValidationResult.Warning -> {
                    Log.w("FilePicker", "Warning for selected file: ${validation.message}")
                    // Still proceed but user might see an error later
                   restoreBackup(selectedUri)
                }
                is BackupFilePicker.ValidationResult.Error -> {
                    Log.e("FilePicker", "Invalid file selected: ${validation.message}")
                    // Could show error to user or still try to restore
                    restoreBackup(selectedUri)
                }
            }
        }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (!allGranted) {
            // Show message about permissions being needed
        }
    }

    // Check and request permissions
    LaunchedEffect(Unit) {
        if (!PermissionUtils.hasStoragePermissions(context)) {
            permissionLauncher.launch(PermissionUtils.getRequiredStoragePermissions())
        }
    }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val scrollBehaviorSmall = TopAppBarDefaults.pinnedScrollBehavior()
    val scrollState = rememberScrollState()
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val overscrollEffect = remember(coroutineScope) { VerticalStretchedOverscroll(coroutineScope) }



    LaunchedEffect(scrollBehavior.state.collapsedFraction) {
        updateCollapsingFraction(scrollBehavior.state.collapsedFraction)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CustomTitleTopAppBar(
                scrollBehaviorLarge = scrollBehavior,
                scrollBehaviorSmall = scrollBehaviorSmall,
                title = screenTitle,
                previousScreenTitle = previousScreenTitle,
                hasBackButton = true,
                onBackClick = onBackClick,
                hazeState = hazeState
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(
                    start = paddingValues.calculateLeftPadding(LayoutDirection.Ltr),
                    end = paddingValues.calculateRightPadding(LayoutDirection.Rtl)
                )
                .verticalScroll(scrollState)
                .haze(state = hazeState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(
                modifier = Modifier
                    .offset(y = dataManagementUiState.currentOffset)
                    .height(screenHeight + dataManagementUiState.currentOffset)
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .overscroll(overscrollEffect)
                    .scrollable(
                        orientation = Orientation.Vertical,
                        reverseDirection = true,
                        state = lazyListState,
                        overscrollEffect = overscrollEffect
                    ),
                state = lazyListState,
                userScrollEnabled = false,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Loading indicator
                if (dataManagementUiState.isLoading) {
                    item {
                        LoadingSection(dataManagementUiState.operationInProgress ?: "Processing...")
                    }
                }

                // Error message
                dataManagementUiState.error?.let { error ->
                    item {
                        ErrorMessage(error) {
                            clearMessages()
                        }
                    }
                }

                // Success message
                dataManagementUiState.successMessage?.let { message ->
                    item {
                        SuccessMessage(message) {
                            clearMessages()
                        }
                    }
                }
                // Information Section
                item {
                    InfoSection()
                }
                // Backup & Restore Section
                item {
                    Column(Modifier.fillMaxWidth()) {
                        DataManagementButton(
                            text = "Create Backup",
                            iconRes = R.drawable.archive,
                            onClick = { createBackup() },
                            enabled = !dataManagementUiState.isLoading
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        DataManagementButton(
                            text = "Restore Backup",
                            iconRes = R.drawable.refresh_bulk,
                            color = Macchiato_Green_Dim,
                            onClick = {
                                BackupFilePicker.launchFilePickerForBackup(backupRestoreLauncher, context)
                            },
                            enabled = !dataManagementUiState.isLoading
                        )
                    }
//                    DataManagementSection(
//                        title = "Backup & Restore",
//                        color = MaterialTheme.colorScheme.primary
//                    ) {
//                        DataManagementButton(
//                            text = "Create Backup",
//                            iconRes = R.drawable.arrow_mini_right_bulk,
//                            onClick = { dataManagementViewModel.createBackup() },
//                            enabled = !state.isLoading
//                        )
//
//                        Spacer(modifier = Modifier.height(8.dp))
//
//                        DataManagementButton(
//                            text = "Restore Backup",
//                            iconRes = R.drawable.arrow_mini_left_bulk,
//                            onClick = {
//                                BackupFilePicker.launchFilePickerForBackup(backupRestoreLauncher, context)
//                            },
//                            enabled = !state.isLoading
//                        )
//                    }
                }
            }
        }
    }
}

@Composable
private fun DataManagementSection(
    title: String,
    color: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = color,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
private fun DataManagementButton(
    text: String,
    iconRes: Int,
    onClick: () -> Unit,
    color: Color = MaterialTheme.colorScheme.primary,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .height(56.dp)
            .fillMaxWidth()
            .shadow(
                10.dp,
                RoundedCornerShape(15.dp),
                spotColor = color,
                ambientColor = Color.Transparent
            ),
        shape = RoundedCornerShape(15.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = Color.White
        )
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text)
    }
}

@Composable
private fun LoadingSection(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ErrorMessage(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    }
}

@Composable
private fun SuccessMessage(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onDismiss) {
                    Text("Dismiss")
                }
            }
        }
    }
}

@Composable
private fun InfoSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Backup & Restore",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.inverseSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "• Complete Backup: Saves all your app data, settings, profile, and images in a ZIP file\n" +
                        "• File Location: Backups are saved to Downloads folder\n" +
                        "• ⚠️ Restore will completely replace your current data",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.inverseSurface.copy(0.7f),
                lineHeight = 18.sp
            )
        }
    }
}
@Composable
private fun RestoreBackupSection(
    isLoading: Boolean,
    onRestoreClick: (String) -> Unit
) {
    var showFileTypeDialog by remember { mutableStateOf(false) }

    // Multiple file picker launchers for different MIME types
    val zipFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { onRestoreClick("zip") } }

    val jsonFileLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { onRestoreClick("json") } }

    val allFilesLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { onRestoreClick("all") } }

    DataManagementButton(
        text = "Restore from Backup",
        iconRes = R.drawable.arrow_mini_left_bulk,
        onClick = { showFileTypeDialog = true },
        enabled = !isLoading
    )

    // File type selection dialog
    if (showFileTypeDialog) {
        AlertDialog(
            onDismissRequest = { showFileTypeDialog = false },
            title = { Text("Select Backup File Type") },
            text = {
                Text("Choose the type of backup file you want to restore:")
            },
            confirmButton = {
                Column {
                    TextButton(
                        onClick = {
                            showFileTypeDialog = false
                            try {
                                zipFileLauncher.launch("application/zip")
                            } catch (e: Exception) {
                                try {
                                    allFilesLauncher.launch("*/*")
                                } catch (fallback: Exception) {
                                    Log.e("FilePicker", "Failed to launch file picker")
                                }
                            }
                        }
                    ) {
                        Text("📦 ZIP File (Recommended)")
                    }

                    TextButton(
                        onClick = {
                            showFileTypeDialog = false
                            try {
                                jsonFileLauncher.launch("application/json")
                            } catch (e: Exception) {
                                try {
                                    allFilesLauncher.launch("*/*")
                                } catch (fallback: Exception) {
                                    Log.e("FilePicker", "Failed to launch file picker")
                                }
                            }
                        }
                    ) {
                        Text("📄 JSON File (Legacy)")
                    }

                    TextButton(
                        onClick = {
                            showFileTypeDialog = false
                            allFilesLauncher.launch("*/*")
                        }
                    ) {
                        Text("📁 Browse All Files")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showFileTypeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun DataManagementScreen(
//    screenTitle: String,
//    previousScreenTitle: String,
//    onBackClick: () -> Unit,
//    dataManagementViewModel: DataManagementViewModel,
//) {
//    val state by dataManagementViewModel.state.collectAsState()
//    val context = LocalContext.current
//    val hazeState = remember { HazeState() }
//
//    val backupRestoreLauncher = rememberLauncherForActivityResult(
//        ActivityResultContracts.GetContent()
//    ) { uri ->
//        uri?.let { selectedUri ->
//            // Validate the selected file
//            when (val validation = BackupFilePicker.isValidBackupFile(selectedUri, context)) {
//                is BackupFilePicker.ValidationResult.Valid -> {
//                    Log.d("FilePicker", "Valid file selected: ${validation.message}")
//                    dataManagementViewModel.restoreBackup(selectedUri)
//                }
//                is BackupFilePicker.ValidationResult.Warning -> {
//                    Log.w("FilePicker", "Warning for selected file: ${validation.message}")
//                    // Still proceed but user might see an error later
//                    dataManagementViewModel.restoreBackup(selectedUri)
//                }
//                is BackupFilePicker.ValidationResult.Error -> {
//                    Log.e("FilePicker", "Invalid file selected: ${validation.message}")
//                    // Could show error to user or still try to restore
//                    dataManagementViewModel.restoreBackup(selectedUri)
//                }
//            }
//        }
//    }
//
//    // Permission launcher
//    val permissionLauncher = rememberLauncherForActivityResult(
//        ActivityResultContracts.RequestMultiplePermissions()
//    ) { permissions ->
//        val allGranted = permissions.values.all { it }
//        if (!allGranted) {
//            // Show message about permissions being needed
//        }
//    }
//
//    // Check and request permissions
//    LaunchedEffect(Unit) {
//        if (!PermissionUtils.hasStoragePermissions(context)) {
//            permissionLauncher.launch(PermissionUtils.getRequiredStoragePermissions())
//        }
//    }
//
//    val configuration = LocalConfiguration.current
//    val screenHeight = configuration.screenHeightDp.dp
//    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
//    val scrollBehaviorSmall = TopAppBarDefaults.pinnedScrollBehavior()
//    val scrollState = rememberScrollState()
//    val lazyListState = rememberLazyListState()
//    val coroutineScope = rememberCoroutineScope()
//    val overscrollEffect = remember(coroutineScope) { VerticalStretchedOverscroll(coroutineScope) }
//
//
//
//    LaunchedEffect(scrollBehavior.state.collapsedFraction) {
//        dataManagementViewModel.updateCollapsingFraction(scrollBehavior.state.collapsedFraction)
//    }
//
//    Scaffold(
//        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
//        topBar = {
//            CustomTitleTopAppBar(
//                scrollBehaviorLarge = scrollBehavior,
//                scrollBehaviorSmall = scrollBehaviorSmall,
//                title = screenTitle,
//                previousScreenTitle = previousScreenTitle,
//                hasBackButton = true,
//                onBackClick = onBackClick,
//                hazeState = hazeState
//            )
//        }
//    ) { paddingValues ->
//        Column(
//            modifier = Modifier
//                .padding(
//                    start = paddingValues.calculateLeftPadding(LayoutDirection.Ltr),
//                    end = paddingValues.calculateRightPadding(LayoutDirection.Rtl)
//                )
//                .verticalScroll(scrollState)
//                .haze(state = hazeState),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            LazyColumn(
//                modifier = Modifier
//                    .offset(y = state.currentOffset)
//                    .height(screenHeight + state.currentOffset)
//                    .fillMaxSize()
//                    .padding(horizontal = 20.dp)
//                    .overscroll(overscrollEffect)
//                    .scrollable(
//                        orientation = Orientation.Vertical,
//                        reverseDirection = true,
//                        state = lazyListState,
//                        overscrollEffect = overscrollEffect
//                    ),
//                state = lazyListState,
//                userScrollEnabled = false,
//                verticalArrangement = Arrangement.spacedBy(16.dp)
//            ) {
//                // Loading indicator
//                if (state.isLoading) {
//                    item {
//                        LoadingSection(state.operationInProgress ?: "Processing...")
//                    }
//                }
//
//                // Error message
//                state.error?.let { error ->
//                    item {
//                        ErrorMessage(error) {
//                            dataManagementViewModel.clearMessages()
//                        }
//                    }
//                }
//
//                // Success message
//                state.successMessage?.let { message ->
//                    item {
//                        SuccessMessage(message) {
//                            dataManagementViewModel.clearMessages()
//                        }
//                    }
//                }
//                // Information Section
//                item {
//                    InfoSection()
//                }
//                // Backup & Restore Section
//                item {
//                    Column(Modifier.fillMaxWidth()) {
//                        DataManagementButton(
//                            text = "Create Backup",
//                            iconRes = R.drawable.archive,
//                            onClick = { dataManagementViewModel.createBackup() },
//                            enabled = !state.isLoading
//                        )
//
//                        Spacer(modifier = Modifier.height(8.dp))
//
//                        DataManagementButton(
//                            text = "Restore Backup",
//                            iconRes = R.drawable.refresh_bulk,
//                            color = Macchiato_Green_Dim,
//                            onClick = {
//                                BackupFilePicker.launchFilePickerForBackup(backupRestoreLauncher, context)
//                            },
//                            enabled = !state.isLoading
//                        )
//                    }
////                    DataManagementSection(
////                        title = "Backup & Restore",
////                        color = MaterialTheme.colorScheme.primary
////                    ) {
////                        DataManagementButton(
////                            text = "Create Backup",
////                            iconRes = R.drawable.arrow_mini_right_bulk,
////                            onClick = { dataManagementViewModel.createBackup() },
////                            enabled = !state.isLoading
////                        )
////
////                        Spacer(modifier = Modifier.height(8.dp))
////
////                        DataManagementButton(
////                            text = "Restore Backup",
////                            iconRes = R.drawable.arrow_mini_left_bulk,
////                            onClick = {
////                                BackupFilePicker.launchFilePickerForBackup(backupRestoreLauncher, context)
////                            },
////                            enabled = !state.isLoading
////                        )
////                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun DataManagementSection(
//    title: String,
//    color: Color,
//    content: @Composable ColumnScope.() -> Unit
//) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        colors = CardDefaults.cardColors(
//            containerColor = MaterialTheme.colorScheme.surface
//        )
//    ) {
//        Column(
//            modifier = Modifier.padding(16.dp)
//        ) {
//            Text(
//                text = title,
//                style = MaterialTheme.typography.titleMedium,
//                fontWeight = FontWeight.SemiBold,
//                color = color,
//                modifier = Modifier.padding(bottom = 12.dp)
//            )
//            content()
//        }
//    }
//}
//
//@Composable
//private fun DataManagementButton(
//    text: String,
//    iconRes: Int,
//    onClick: () -> Unit,
//    color: Color = MaterialTheme.colorScheme.primary,
//    enabled: Boolean = true
//) {
//    Button(
//        onClick = onClick,
//        enabled = enabled,
//        modifier = Modifier
//            .height(56.dp)
//            .fillMaxWidth()
//            .shadow(
//                10.dp,
//                RoundedCornerShape(15.dp),
//                spotColor = color,
//                ambientColor = Color.Transparent
//            ),
//        shape = RoundedCornerShape(15.dp),
//        colors = ButtonDefaults.buttonColors(
//            containerColor = color,
//            contentColor = Color.White
//        )
//    ) {
//        Icon(
//            painter = painterResource(id = iconRes),
//            contentDescription = null,
//            modifier = Modifier.size(18.dp)
//        )
//        Spacer(modifier = Modifier.width(8.dp))
//        Text(text = text)
//    }
//}
//
//@Composable
//private fun LoadingSection(message: String) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        colors = CardDefaults.cardColors(
//            containerColor = MaterialTheme.colorScheme.primaryContainer
//        )
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            CircularProgressIndicator(
//                modifier = Modifier.size(24.dp),
//                strokeWidth = 2.dp
//            )
//            Spacer(modifier = Modifier.width(12.dp))
//            Text(
//                text = message,
//                style = MaterialTheme.typography.bodyMedium
//            )
//        }
//    }
//}
//
//@Composable
//private fun ErrorMessage(
//    message: String,
//    onDismiss: () -> Unit
//) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        colors = CardDefaults.cardColors(
//            containerColor = MaterialTheme.colorScheme.errorContainer
//        )
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Text(
//                text = message,
//                style = MaterialTheme.typography.bodyMedium,
//                color = MaterialTheme.colorScheme.onErrorContainer,
//                modifier = Modifier.weight(1f)
//            )
//            TextButton(onClick = onDismiss) {
//                Text("Dismiss")
//            }
//        }
//    }
//}
//
//@Composable
//private fun SuccessMessage(
//    message: String,
//    onDismiss: () -> Unit
//) {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        colors = CardDefaults.cardColors(
//            containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
//        )
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp)
//        ) {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                verticalAlignment = Alignment.Top,
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                Text(
//                    text = message,
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = Color(0xFF2E7D32),
//                    modifier = Modifier.weight(1f)
//                )
//                TextButton(onClick = onDismiss) {
//                    Text("Dismiss")
//                }
//            }
//        }
//    }
//}
//
//@Composable
//private fun InfoSection() {
//    Card(
//        modifier = Modifier.fillMaxWidth(),
//        colors = CardDefaults.cardColors(
//            containerColor = MaterialTheme.colorScheme.surfaceVariant
//        )
//    ) {
//        Column(
//            modifier = Modifier.padding(16.dp)
//        ) {
//            Text(
//                text = "Backup & Restore",
//                style = MaterialTheme.typography.titleMedium,
//                fontWeight = FontWeight.SemiBold,
//                color = MaterialTheme.colorScheme.inverseSurface,
//                modifier = Modifier.padding(bottom = 8.dp)
//            )
//            Text(
//                text = "• Complete Backup: Saves all your app data, settings, profile, and images in a ZIP file\n" +
//                        "• File Location: Backups are saved to Downloads folder\n" +
//                        "• ⚠️ Restore will completely replace your current data",
//                style = MaterialTheme.typography.bodySmall,
//                color = MaterialTheme.colorScheme.inverseSurface.copy(0.7f),
//                lineHeight = 18.sp
//            )
//        }
//    }
//}
//@Composable
//private fun RestoreBackupSection(
//    isLoading: Boolean,
//    onRestoreClick: (String) -> Unit
//) {
//    var showFileTypeDialog by remember { mutableStateOf(false) }
//
//    // Multiple file picker launchers for different MIME types
//    val zipFileLauncher = rememberLauncherForActivityResult(
//        ActivityResultContracts.GetContent()
//    ) { uri -> uri?.let { onRestoreClick("zip") } }
//
//    val jsonFileLauncher = rememberLauncherForActivityResult(
//        ActivityResultContracts.GetContent()
//    ) { uri -> uri?.let { onRestoreClick("json") } }
//
//    val allFilesLauncher = rememberLauncherForActivityResult(
//        ActivityResultContracts.GetContent()
//    ) { uri -> uri?.let { onRestoreClick("all") } }
//
//    DataManagementButton(
//        text = "Restore from Backup",
//        iconRes = R.drawable.arrow_mini_left_bulk,
//        onClick = { showFileTypeDialog = true },
//        enabled = !isLoading
//    )
//
//    // File type selection dialog
//    if (showFileTypeDialog) {
//        AlertDialog(
//            onDismissRequest = { showFileTypeDialog = false },
//            title = { Text("Select Backup File Type") },
//            text = {
//                Text("Choose the type of backup file you want to restore:")
//            },
//            confirmButton = {
//                Column {
//                    TextButton(
//                        onClick = {
//                            showFileTypeDialog = false
//                            try {
//                                zipFileLauncher.launch("application/zip")
//                            } catch (e: Exception) {
//                                try {
//                                    allFilesLauncher.launch("*/*")
//                                } catch (fallback: Exception) {
//                                    Log.e("FilePicker", "Failed to launch file picker")
//                                }
//                            }
//                        }
//                    ) {
//                        Text("📦 ZIP File (Recommended)")
//                    }
//
//                    TextButton(
//                        onClick = {
//                            showFileTypeDialog = false
//                            try {
//                                jsonFileLauncher.launch("application/json")
//                            } catch (e: Exception) {
//                                try {
//                                    allFilesLauncher.launch("*/*")
//                                } catch (fallback: Exception) {
//                                    Log.e("FilePicker", "Failed to launch file picker")
//                                }
//                            }
//                        }
//                    ) {
//                        Text("📄 JSON File (Legacy)")
//                    }
//
//                    TextButton(
//                        onClick = {
//                            showFileTypeDialog = false
//                            allFilesLauncher.launch("*/*")
//                        }
//                    ) {
//                        Text("📁 Browse All Files")
//                    }
//                }
//            },
//            dismissButton = {
//                TextButton(onClick = { showFileTypeDialog = false }) {
//                    Text("Cancel")
//                }
//            }
//        )
//    }
//}