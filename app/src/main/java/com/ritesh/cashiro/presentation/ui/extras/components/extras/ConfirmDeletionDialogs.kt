package com.ritesh.cashiro.presentation.ui.extras.components.extras

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ritesh.cashiro.data.local.entity.CategoryEntity
import com.ritesh.cashiro.data.local.entity.SubCategoryEntity
import com.ritesh.cashiro.domain.utils.icons
import com.ritesh.cashiro.presentation.ui.features.categories.CategoryScreenEvent
import com.ritesh.cashiro.presentation.ui.features.categories.CategoryScreenState
import com.ritesh.cashiro.presentation.ui.theme.iosFont
import com.ritesh.cashiro.data.local.entity.AccountEntity
import com.ritesh.cashiro.data.currency.model.CurrencySymbols
import com.ritesh.cashiro.domain.repository.AccountTransactionStats
import com.ritesh.cashiro.domain.repository.TransactionStats
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenEvent
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenState
import com.ritesh.cashiro.presentation.ui.features.accounts.AccountScreenViewModel
import com.ritesh.cashiro.presentation.ui.features.categories.CategoryScreenViewModel
import com.ritesh.cashiro.presentation.ui.theme.ErrorColor
import com.ritesh.cashiro.R

@Composable
fun CategoryDeletionDialogs(
    state: CategoryScreenState,
    onEvent: (CategoryScreenEvent) -> Unit,
    getTransactionStatsForCategory: (categoryId: Int, onResult: (TransactionStats) -> Unit) -> Unit,
    getTransactionStatsForSubCategory: (subCategoryId: Int, onResult: (TransactionStats) -> Unit) -> Unit,
    subCategories: List<SubCategoryEntity> = emptyList(),
    themeColors: ColorScheme = MaterialTheme.colorScheme
) {
    // State for transaction statistics
    var categoryTransactionStats by remember { mutableStateOf<TransactionStats?>(null) }
    var subCategoryTransactionStats by remember { mutableStateOf<TransactionStats?>(null) }

    // Load transaction statistics when pending deletion changes
    LaunchedEffect(state.pendingCategoryDeletion) {
        state.pendingCategoryDeletion?.let { category ->
            getTransactionStatsForCategory(category.id) { stats ->
                categoryTransactionStats = stats
            }
        }
    }

    LaunchedEffect(state.pendingSubCategoryDeletion) {
        state.pendingSubCategoryDeletion?.let { subCategory ->
            getTransactionStatsForSubCategory(subCategory.id) { stats ->
                subCategoryTransactionStats = stats
            }
        }
    }

    // Category deletion confirmation dialog
    if (state.showCategoryDeleteConfirmationDialog && state.pendingCategoryDeletion != null) {
        CategoryDeleteConfirmationDialog(
            categoryName = state.pendingCategoryDeletion.name,
            onConfirm = {
                onEvent(CategoryScreenEvent.ShowCategoryDeleteOptions(state.pendingCategoryDeletion))
            },
            onDismiss = {
                onEvent(CategoryScreenEvent.CancelDeletion)
            },
            themeColors = themeColors
        )
    }

    // UPDATED: Category delete options dialog with transaction statistics
    if (state.showCategoryDeleteOptionsDialog && state.pendingCategoryDeletion != null) {
        CategoryDeleteOptionsDialogEnhanced(
            categoryName = state.pendingCategoryDeletion.name,
            transactionStats = categoryTransactionStats, // Pass the statistics
            onDelete = {
                onEvent(CategoryScreenEvent.ConfirmCategoryDeletionWithTransactions(state.pendingCategoryDeletion))
            },
            onMoveTransactions = {
                onEvent(CategoryScreenEvent.ShowCategoryMoveTransactions(state.pendingCategoryDeletion))
            },
            onCancel = {
                onEvent(CategoryScreenEvent.CancelDeletion)
            },
            themeColors = themeColors
        )
    }

    // Category move transactions dialog
    if (state.showCategoryMoveTransactionsDialog && state.pendingCategoryDeletion != null) {
        CategoryMoveTransactionsDialog(
            availableCategories = state.availableCategoriesForMigration,
            selectedCategoryId = state.selectedTargetCategoryId,
            onCategorySelected = { categoryId ->
                onEvent(CategoryScreenEvent.SelectTargetCategoryForMigration(categoryId))
            },
            onConfirm = {
                state.selectedTargetCategoryId?.let { targetId ->
                    onEvent(CategoryScreenEvent.ConfirmCategoryMigrationAndDeletion(state.pendingCategoryDeletion, targetId))
                }
            },
            onCancel = {
                onEvent(CategoryScreenEvent.CancelDeletion)
            },
            themeColors = themeColors
        )
    }

    // Category final confirmation dialog
    if (state.showCategoryFinalConfirmationDialog && state.pendingCategoryDeletion != null) {
        val targetCategory = state.availableCategoriesForMigration.find { it.id == state.selectedTargetCategoryId }
        // Get subcategory count for the source category
        val sourceSubCategoryCount = subCategories.count { it.categoryId == state.pendingCategoryDeletion.id }

        CategoryFinalConfirmationDialog(
            sourceCategoryName = state.pendingCategoryDeletion.name,
            targetCategoryName = targetCategory?.name ?: "",
            sourceSubCategoryCount = sourceSubCategoryCount, // Pass subcategory count
            onMerge = {
                state.selectedTargetCategoryId?.let { targetId ->
                    onEvent(CategoryScreenEvent.ConfirmCategoryMigrationAndDeletion(state.pendingCategoryDeletion, targetId))
                }
            },
            onCancel = {
                onEvent(CategoryScreenEvent.CancelDeletion)
            },
            themeColors = themeColors
        )
    }

    // SubCategory deletion confirmation dialog
    if (state.showSubCategoryDeleteConfirmationDialog && state.pendingSubCategoryDeletion != null) {
        SubCategoryDeleteConfirmationDialog(
            subCategoryName = state.pendingSubCategoryDeletion.name,
            onConfirm = {
                onEvent(CategoryScreenEvent.ShowSubCategoryDeleteOptions(state.pendingSubCategoryDeletion))
            },
            onDismiss = {
                onEvent(CategoryScreenEvent.CancelDeletion)
            },
            themeColors = themeColors
        )
    }

    // UPDATED: SubCategory delete options dialog with transaction statistics
    if (state.showSubCategoryDeleteOptionsDialog && state.pendingSubCategoryDeletion != null) {
        SubCategoryDeleteOptionsDialogEnhanced(
            subCategoryName = state.pendingSubCategoryDeletion.name,
            transactionStats = subCategoryTransactionStats, // Pass the statistics
            onDelete = {
                onEvent(CategoryScreenEvent.ConfirmSubCategoryDeletionWithTransactions(state.pendingSubCategoryDeletion))
            },
            onMoveTransactions = {
                onEvent(CategoryScreenEvent.ShowSubCategoryMoveTransactions(state.pendingSubCategoryDeletion))
            },
            onCancel = {
                onEvent(CategoryScreenEvent.CancelDeletion)
            },
            themeColors = themeColors
        )
    }

    // SubCategory move transactions dialog
    if (state.showSubCategoryMoveTransactionsDialog && state.pendingSubCategoryDeletion != null) {
        SubCategoryMoveTransactionsDialog(
            availableSubCategories = state.availableSubCategoriesForMigration,
            selectedSubCategoryId = state.selectedTargetSubCategoryId,
            onSubCategorySelected = { subCategoryId ->
                onEvent(CategoryScreenEvent.SelectTargetSubCategoryForMigration(subCategoryId))
            },
            onConfirm = {
                state.selectedTargetSubCategoryId?.let { targetId ->
                    onEvent(CategoryScreenEvent.ConfirmSubCategoryMigrationAndDeletion(state.pendingSubCategoryDeletion, targetId))
                }
            },
            onCancel = {
                onEvent(CategoryScreenEvent.CancelDeletion)
            },
            themeColors = themeColors
        )
    }

    // SubCategory final confirmation dialog
    if (state.showSubCategoryFinalConfirmationDialog && state.pendingSubCategoryDeletion != null) {
        val targetSubCategory = state.availableSubCategoriesForMigration.find { it.id == state.selectedTargetSubCategoryId }
        SubCategoryFinalConfirmationDialog(
            sourceSubCategoryName = state.pendingSubCategoryDeletion.name,
            targetSubCategoryName = targetSubCategory?.name ?: "",
            onMerge = {
                state.selectedTargetSubCategoryId?.let { targetId ->
                    onEvent(CategoryScreenEvent.ConfirmSubCategoryMigrationAndDeletion(state.pendingSubCategoryDeletion, targetId))
                }
            },
            onCancel = {
                onEvent(CategoryScreenEvent.CancelDeletion)
            },
            themeColors = themeColors
        )
    }

    if (state.showMergeCategoryDialog && state.pendingMergeCategorySource != null) {
        CategoryMoveTransactionsDialog(
            availableCategories = state.availableCategoriesForMerge,
            selectedCategoryId = state.selectedMergeCategoryTargetId,
            onCategorySelected = { categoryId ->
                onEvent(CategoryScreenEvent.SelectTargetCategoryForMerge(categoryId))
            },
            onConfirm = {
                state.selectedMergeCategoryTargetId?.let { targetId ->
                    val targetCategory = state.availableCategoriesForMerge.find { it.id == targetId }
                    val sourceSubCategoryCount = subCategories.count { it.categoryId == state.pendingMergeCategorySource.id }

                    // Show final confirmation dialog
                    onEvent(CategoryScreenEvent.ConfirmCategoryMerge(state.pendingMergeCategorySource, targetId))
                }
            },
            onCancel = {
                onEvent(CategoryScreenEvent.CancelMergeCategory)
            },
            themeColors = themeColors
        )
    }

// Final merge confirmation dialog
    if (state.showMergeCategoryFinalConfirmationDialog && state.pendingMergeCategorySource != null) {
        val targetCategory = state.availableCategoriesForMerge.find { it.id == state.selectedMergeCategoryTargetId }
        val sourceSubCategoryCount = subCategories.count { it.categoryId == state.pendingMergeCategorySource.id }

        CategoryFinalConfirmationDialog(
            sourceCategoryName = state.pendingMergeCategorySource.name,
            targetCategoryName = targetCategory?.name ?: "",
            sourceSubCategoryCount = sourceSubCategoryCount,
            onMerge = {
                state.selectedMergeCategoryTargetId?.let { targetId ->
                    onEvent(CategoryScreenEvent.ConfirmCategoryMerge(state.pendingMergeCategorySource, targetId))
                }
            },
            onCancel = {
                onEvent(CategoryScreenEvent.CancelMergeCategory)
            },
            themeColors = themeColors
        )
    }
}

// Enhanced dialog components that show transaction statistics
@Composable
private fun CategoryDeleteOptionsDialogEnhanced(
    categoryName: String,
    transactionStats: TransactionStats?,
    onDelete: () -> Unit,
    onMoveTransactions: () -> Unit,
    onCancel: () -> Unit,
    themeColors: ColorScheme
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(R.drawable.delete_bulk),
                    contentDescription = "warning",
                    tint = ErrorColor.copy(0.8f),
                    modifier = Modifier.size(50.dp)
                )
                Text(
                    text = "Delete Category",
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Deleting a category will delete all transactions associated with this Category and its subcategories.",
                    fontFamily = iosFont,
                    fontSize = 14.sp,
                    color = themeColors.inverseSurface.copy(0.8f)
                )

                // Show transaction statistics if available
                transactionStats?.let { stats ->
                    if (stats.count > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = ErrorColor.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Impact on your accounts:",
                                    fontFamily = iosFont,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.error
                                )
                                Text(
                                    text = "• ${stats.count} transactions will be deleted",
                                    fontFamily = iosFont,
                                    fontSize = 12.sp,
                                    color = themeColors.onErrorContainer
                                )
                                if (stats.expenseAmount > 0) {
                                    Text(
                                        text = "• $${String.format("%.2f", stats.expenseAmount)} will be added back to accounts (expenses)",
                                        fontFamily = iosFont,
                                        fontSize = 12.sp,
                                        color = themeColors.primary
                                    )
                                }
                                if (stats.incomeAmount > 0) {
                                    Text(
                                        text = "• $${String.format("%.2f", stats.incomeAmount)} will be removed from accounts (income)",
                                        fontFamily = iosFont,
                                        fontSize = 12.sp,
                                        color = themeColors.onErrorContainer
                                    )
                                }
                                if (stats.transferAmount > 0) {
                                    Text(
                                        text = "• $${String.format("%.2f", stats.transferAmount)} in transfers will be reversed",
                                        fontFamily = iosFont,
                                        fontSize = 12.sp,
                                        color = themeColors.onErrorContainer
                                    )
                                }
                            }
                        }
                    } else {
                        // Show when no transactions
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = themeColors.surface.copy(alpha = 0.5f)
                            )
                        ) {
                            Text(
                                text = "No transactions found in this category.",
                                fontFamily = iosFont,
                                fontSize = 12.sp,
                                color = themeColors.inverseSurface,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "If you want to save those transactions, move them to another Category.",
                    fontFamily = iosFont,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = themeColors.inverseSurface.copy(0.8f)
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (transactionStats?.count ?: 0 > 0) {
                    Button(
                        onClick = onMoveTransactions,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = themeColors.primary
                        ),
                        modifier = Modifier
                            .shadow(
                                elevation = 5.dp,
                                shape = RoundedCornerShape(12.dp),
                                spotColor = themeColors.primary,
                                ambientColor = Color.Transparent
                            ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Move Transactions",
                            fontFamily = iosFont,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorColor,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .shadow(
                            elevation = 5.dp,
                            shape = RoundedCornerShape(12.dp),
                            spotColor =ErrorColor,
                            ambientColor = Color.Transparent
                        )
                ) {
                    Text(
                        text = "Delete",
                        fontFamily = iosFont,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        dismissButton = {
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColors.surface,
                    contentColor = themeColors.inverseSurface
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = "Cancel",
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Medium,
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = themeColors.surfaceBright
    )
}

@Composable
private fun SubCategoryDeleteOptionsDialogEnhanced(
    subCategoryName: String,
    transactionStats: TransactionStats?,
    onDelete: () -> Unit,
    onMoveTransactions: () -> Unit,
    onCancel: () -> Unit,
    themeColors: ColorScheme
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(R.drawable.delete_bulk),
                    contentDescription = "warning",
                    tint = ErrorColor.copy(0.8f),
                    modifier = Modifier.size(50.dp)
                )
                Text(
                    text = "Delete SubCategory",
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Deleting a subcategory will remove all the transactions from this subcategory.",
                    fontFamily = iosFont,
                    fontSize = 14.sp,
                    color = themeColors.inverseSurface.copy(0.8f)
                )

                // Show transaction statistics if available
                transactionStats?.let { stats ->
                    if (stats.count > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = ErrorColor.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Impact on your accounts:",
                                    fontFamily = iosFont,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.error
                                )
                                Text(
                                    text = "• ${stats.count} transactions will be deleted",
                                    fontFamily = iosFont,
                                    fontSize = 12.sp,
                                    color = themeColors.onErrorContainer
                                )
                                if (stats.expenseAmount > 0) {
                                    Text(
                                        text = "• $${String.format("%.2f", stats.expenseAmount)} will be added back to accounts",
                                        fontFamily = iosFont,
                                        fontSize = 12.sp,
                                        color = themeColors.primary
                                    )
                                }
                                if (stats.incomeAmount > 0) {
                                    Text(
                                        text = "• $${String.format("%.2f", stats.incomeAmount)} will be removed from accounts",
                                        fontFamily = iosFont,
                                        fontSize = 12.sp,
                                        color = themeColors.onErrorContainer
                                    )
                                }
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = themeColors.surface.copy(alpha = 0.5f)
                            )
                        ) {
                            Text(
                                text = "No transactions found in this subcategory.",
                                fontFamily = iosFont,
                                fontSize = 12.sp,
                                color = themeColors.inverseSurface,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "If you want to save those transactions, move them to another subcategory.",
                    fontFamily = iosFont,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = themeColors.inverseSurface.copy(0.8f)
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if ((transactionStats?.count ?: 0) > 0) {
                    Button(
                        onClick = onMoveTransactions,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = themeColors.primary
                        ),
                        modifier = Modifier
                            .shadow(
                                elevation = 5.dp,
                                shape = RoundedCornerShape(12.dp),
                                spotColor = themeColors.primary,
                                ambientColor = Color.Transparent
                            ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Move Transactions",
                            fontFamily = iosFont,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorColor,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .shadow(
                            elevation = 5.dp,
                            shape = RoundedCornerShape(12.dp),
                            spotColor =ErrorColor,
                            ambientColor = Color.Transparent
                        )
                ) {
                    Text(
                        text = "Delete",
                        fontFamily = iosFont,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        dismissButton = {
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColors.surface,
                    contentColor = themeColors.inverseSurface
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = "Cancel",
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Medium,
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = themeColors.surfaceBright
    )
}

// Update the CategoryFinalConfirmationDialog with subcategory count parameter
@Composable
private fun CategoryFinalConfirmationDialog(
    sourceCategoryName: String,
    targetCategoryName: String,
    sourceSubCategoryCount: Int = 0,
    onMerge: () -> Unit,
    onCancel: () -> Unit,
    themeColors: ColorScheme
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = "Confirm Migration",
                fontFamily = iosFont,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "This will transfer all transactions to \"$targetCategoryName\" and erase \"$sourceCategoryName\".",
                    fontFamily = iosFont,
                    fontSize = 14.sp,
                    color = themeColors.inverseSurface.copy(0.8f)
                )

                if (sourceSubCategoryCount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Additionally, $sourceSubCategoryCount subcategory(ies) will be transferred to \"$targetCategoryName\".",
                        fontFamily = iosFont,
                        fontSize = 12.sp,
                        color = themeColors.primary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "This action cannot be undone.",
                    fontFamily = iosFont,
                    fontSize = 12.sp,
                    color = themeColors.error,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onMerge,
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColors.primary
                ),
                modifier = Modifier
                    .shadow(
                        elevation = 5.dp,
                        shape = RoundedCornerShape(12.dp),
                        spotColor = themeColors.primary,
                        ambientColor = Color.Transparent
                    ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Merge",
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColors.surface,
                    contentColor = themeColors.inverseSurface
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = "Cancel",
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Medium,
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = themeColors.surfaceBright
    )
}

@Composable
private fun CategoryDeleteConfirmationDialog(
    categoryName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    themeColors: ColorScheme
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete Category",
                fontFamily = iosFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )
        },
        text = {
            Text(
                text = "This action will delete all transactions associated with this category.",
                fontFamily = iosFont,
                fontSize = 14.sp,
                color = themeColors.inverseSurface.copy(0.8f),
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorColor,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .shadow(
                        elevation = 5.dp,
                        shape = RoundedCornerShape(12.dp),
                        spotColor =ErrorColor,
                        ambientColor = Color.Transparent
                    )
            ) {
                Text(
                    text = "Delete",
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColors.surface,
                    contentColor = themeColors.inverseSurface
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = "Cancel",
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Medium,
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = themeColors.surfaceBright
    )
}


@Composable
private fun CategoryMoveTransactionsDialog(
    availableCategories: List<CategoryEntity>,
    selectedCategoryId: Int?,
    onCategorySelected: (Int) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    themeColors: ColorScheme
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = "Select Target Category",
                fontFamily = iosFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableCategories) { category ->
                    CategorySelectionItem(
                        category = category,
                        isSelected = selectedCategoryId == category.id,
                        onSelected = { onCategorySelected(category.id) },
                        themeColors = themeColors
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = selectedCategoryId != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColors.primary,
                    disabledContainerColor = themeColors.surface.copy(0.6f)
                ),
                modifier = Modifier
                    .shadow(
                        elevation = 5.dp,
                        shape = RoundedCornerShape(12.dp),
                        spotColor = if (selectedCategoryId!=null) themeColors.primary else themeColors.surface,
                        ambientColor = Color.Transparent
                    ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Continue",
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Medium,
                    color = if (selectedCategoryId!=null)  Color.White else themeColors.inverseSurface.copy(0.6f)
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColors.surface,
                    contentColor = themeColors.inverseSurface
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = "Cancel",
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Medium,
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = themeColors.surfaceBright
    )
}

// SubCategory dialogs follow the same pattern...
@Composable
private fun SubCategoryDeleteConfirmationDialog(
    subCategoryName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    themeColors: ColorScheme
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete SubCategory",
                fontFamily = iosFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )
        },
        text = {
            Text(
                text = "This action will remove the subcategory tag from all transactions associated with this subcategory",
                fontFamily = iosFont,
                fontSize = 14.sp,
                color = themeColors.inverseSurface.copy(0.8f),
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorColor,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .shadow(
                        elevation = 5.dp,
                        shape = RoundedCornerShape(12.dp),
                        spotColor =ErrorColor,
                        ambientColor = Color.Transparent
                    )
            ) {
                Text(
                    text = "Delete",
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColors.surface,
                    contentColor = themeColors.inverseSurface
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = "Cancel",
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Medium,
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = themeColors.surfaceBright
    )
}

@Composable
private fun SubCategoryMoveTransactionsDialog(
    availableSubCategories: List<SubCategoryEntity>,
    selectedSubCategoryId: Int?,
    onSubCategorySelected: (Int) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    themeColors: ColorScheme
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = "Select Target SubCategory",
                fontFamily = iosFont,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableSubCategories) { subCategory ->
                    SubCategorySelectionItem(
                        subCategory = subCategory,
                        isSelected = selectedSubCategoryId == subCategory.id,
                        onSelected = { onSubCategorySelected(subCategory.id) },
                        themeColors = themeColors
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = selectedSubCategoryId != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColors.primary,
                    disabledContainerColor = themeColors.surface.copy(0.6f)
                ),
                modifier = Modifier
                    .shadow(
                        elevation = 5.dp,
                        shape = RoundedCornerShape(12.dp),
                        spotColor = if (selectedSubCategoryId!=null) themeColors.primary else themeColors.surface,
                        ambientColor = Color.Transparent
                    ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Continue",
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Medium,
                    color = if (selectedSubCategoryId!=null)  Color.White else themeColors.inverseSurface.copy(0.6f)
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColors.surface,
                    contentColor = themeColors.inverseSurface
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = "Cancel",
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Medium,
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = themeColors.surfaceBright
    )
}
@Composable
private fun SubCategoryFinalConfirmationDialog(
    sourceSubCategoryName: String,
    targetSubCategoryName: String,
    onMerge: () -> Unit,
    onCancel: () -> Unit,
    themeColors: ColorScheme
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Text(
                text = "Confirm Migration",
                fontFamily = iosFont,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Text(
                text = "This will transfer all transactions to the new subCategory and erase the old subcategory",
                color = themeColors.inverseSurface.copy(0.8f),
                fontFamily = iosFont,
                fontSize = 14.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onMerge,
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColors.primary
                ),
                modifier = Modifier
                    .shadow(
                        elevation = 5.dp,
                        shape = RoundedCornerShape(12.dp),
                        spotColor = themeColors.primary,
                        ambientColor = Color.Transparent
                    ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Merge",
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColors.surface,
                    contentColor = themeColors.inverseSurface
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = "Cancel",
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Medium,
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = themeColors.surfaceBright
    )
}

@Composable
private fun CategorySelectionItem(
    category: CategoryEntity,
    isSelected: Boolean,
    onSelected: () -> Unit,
    themeColors: ColorScheme
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) themeColors.primary.copy(alpha = 0.1f)
                else themeColors.surface
            )
            .clickable { onSelected() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Category icon
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(category.boxColor)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(
                    id = icons.find { it.id == category.categoryIconId }?.resourceId
                        ?:R.drawable.type_beverages_beer
                ),
                contentDescription = "Category Icon",
                modifier = Modifier.size(24.dp)
            )
        }

        // Category name
        Text(
            text = category.name,
            fontSize = 16.sp,
            fontFamily = iosFont,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) themeColors.primary else themeColors.inverseSurface,
            modifier = Modifier.weight(1f)
        )

        // Selection indicator
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.CheckCircle, // You'll need this icon
                contentDescription = "Selected",
                tint = themeColors.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
@Composable
private fun SubCategorySelectionItem(
    subCategory: SubCategoryEntity,
    isSelected: Boolean,
    onSelected: () -> Unit,
    themeColors: ColorScheme
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) themeColors.primary.copy(alpha = 0.1f)
                else themeColors.surface
            )
            .clickable { onSelected() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // SubCategory icon
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(subCategory.boxColor ?: 0)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(
                    id = icons.find { it.id == subCategory.subcategoryIconId }?.resourceId
                        ?:R.drawable.type_beverages_beer
                ),
                contentDescription = "SubCategory Icon",
                modifier = Modifier.size(24.dp)
            )
        }

        // SubCategory name
        Text(
            text = subCategory.name,
            fontSize = 16.sp,
            fontFamily = iosFont,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) themeColors.primary else themeColors.inverseSurface,
            modifier = Modifier.weight(1f)
        )

        // Selection indicator
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.CheckCircle, // You'll need this icon
                contentDescription = "Selected",
                tint = themeColors.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun TransactionDeletionConfirmationDialog(
    showDialog: Boolean,
    transactionCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    themeColors: ColorScheme = MaterialTheme.colorScheme
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Column( verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter =  painterResource(R.drawable.delete_bulk),
                        contentDescription = "warning",
                        tint = ErrorColor.copy(0.8f),
                        modifier = Modifier.size(50.dp)
                    )
                    Text(
                        text = "Delete Transaction${if (transactionCount > 1) "s" else ""}",
                        fontFamily = iosFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        color = themeColors.inverseSurface,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (transactionCount > 1) {
                            "Are you sure you want to delete $transactionCount transactions?"
                        } else {
                            "Are you sure you want to delete this transaction?"
                        },
                        fontFamily = iosFont,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = themeColors.inverseSurface.copy(0.8f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "This action will:",
                        fontFamily = iosFont,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColors.primary
                    )

                    Text(
                        text = "• Permanently delete the selected transaction${if (transactionCount > 1) "s" else ""}",
                        fontFamily = iosFont,
                        fontSize = 12.sp,
                        color = themeColors.inverseSurface.copy(0.8f)
                    )

                    Text(
                        text = "• Restore account balances for paid transactions",
                        fontFamily = iosFont,
                        fontSize = 12.sp,
                        color = themeColors.primary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "This action cannot be undone.",
                        fontFamily = iosFont,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ErrorColor
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorColor,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .shadow(
                            elevation = 5.dp,
                            shape = RoundedCornerShape(12.dp),
                            spotColor =ErrorColor,
                            ambientColor = Color.Transparent
                        )
                ) {
                    Text(
                        text = "Delete",
                        fontFamily = iosFont,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            dismissButton = {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = themeColors.surface,
                        contentColor = themeColors.inverseSurface
                    ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = "Cancel",
                        fontFamily = iosFont,
                        fontWeight = FontWeight.Medium,
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = themeColors.surfaceBright,
        )
    }
}

@Composable
fun AccountDeletionDialogs(
    state: AccountScreenState,
    onEvent: (AccountScreenEvent) -> Unit,
    getTransactionStatsForAccount: (id: Int, callback: (AccountTransactionStats?) -> Unit) -> Unit,
    themeColors: ColorScheme = MaterialTheme.colorScheme
) {
    // State for transaction statistics
    var accountTransactionStats by remember { mutableStateOf<AccountTransactionStats?>(null) }

    // Load transaction statistics when pending deletion changes
    LaunchedEffect(state.pendingAccountDeletion) {
        state.pendingAccountDeletion?.let { account ->
            getTransactionStatsForAccount(account.id) { stats ->
                accountTransactionStats = stats
            }
        }
    }

    // Account deletion confirmation dialog
    if (state.showAccountDeleteConfirmationDialog && state.pendingAccountDeletion != null) {
        AccountDeleteConfirmationDialog(
            accountName = state.pendingAccountDeletion.accountName,
            onConfirm = {
                onEvent(AccountScreenEvent.ShowAccountDeleteOptions(state.pendingAccountDeletion))
            },
            onDismiss = {
                onEvent(AccountScreenEvent.CancelAccountDeletion)
            },
            themeColors = themeColors
        )
    }

    // Account delete options dialog with transaction statistics
    if (state.showAccountDeleteOptionsDialog && state.pendingAccountDeletion != null) {
        AccountDeleteOptionsDialogEnhanced(
            accountName = state.pendingAccountDeletion.accountName,
            transactionStats = accountTransactionStats,
            onDelete = {
                onEvent(AccountScreenEvent.ConfirmAccountDeletionWithTransactions(state.pendingAccountDeletion))
            },
            onMoveTransactions = {
                onEvent(AccountScreenEvent.ShowAccountMoveTransactions(state.pendingAccountDeletion))
            },
            onCancel = {
                onEvent(AccountScreenEvent.CancelAccountDeletion)
            },
            themeColors = themeColors
        )
    }

    // Account move transactions dialog
    if (state.showAccountMoveTransactionsDialog && state.pendingAccountDeletion != null) {
        AccountMoveTransactionsDialog(
            availableAccounts = state.availableAccountsForMigration,
            selectedAccountId = state.selectedTargetAccountId,
            onAccountSelected = { accountId ->
                onEvent(AccountScreenEvent.SelectTargetAccountForMigration(accountId))
            },
            onConfirm = {
                state.selectedTargetAccountId?.let { targetId ->
                    onEvent(AccountScreenEvent.ConfirmAccountMigrationAndDeletion(state.pendingAccountDeletion, targetId))
                }
            },
            onCancel = {
                onEvent(AccountScreenEvent.CancelAccountDeletion)
            },
            themeColors = themeColors
        )
    }

    // Merge account dialog
    if (state.showMergeAccountDialog && state.pendingMergeAccountSource != null) {
        AccountMoveTransactionsDialog(
            availableAccounts = state.availableAccountsForMerge,
            selectedAccountId = state.selectedMergeAccountTargetId,
            onAccountSelected = { accountId ->
                onEvent(AccountScreenEvent.SelectTargetAccountForMerge(accountId))
            },
            onConfirm = {
                state.selectedMergeAccountTargetId?.let { targetId ->
                    onEvent(AccountScreenEvent.ConfirmAccountMerge(state.pendingMergeAccountSource, targetId))
                }
            },
            onCancel = {
                onEvent(AccountScreenEvent.CancelMergeAccount)
            },
            themeColors = themeColors
        )
    }
}

//@Composable
//fun AccountDeletionDialogs(
//    state: AccountScreenState,
//    onEvent: (AccountScreenEvent) -> Unit,
//    accountViewModel: AccountScreenViewModel,
//    accounts: List<AccountEntity> = emptyList(),
//    themeColors: ColorScheme = MaterialTheme.colorScheme
//) {
//    // State for transaction statistics
//    var accountTransactionStats by remember { mutableStateOf<AccountTransactionStats?>(null) }
//
//    // Load transaction statistics when pending deletion changes
//    LaunchedEffect(state.pendingAccountDeletion) {
//        state.pendingAccountDeletion?.let { account ->
//            accountViewModel.getTransactionStatsForAccount(account.id) { stats ->
//                accountTransactionStats = stats
//            }
//        }
//    }
//
//    // Account deletion confirmation dialog
//    if (state.showAccountDeleteConfirmationDialog && state.pendingAccountDeletion != null) {
//        AccountDeleteConfirmationDialog(
//            accountName = state.pendingAccountDeletion.accountName,
//            onConfirm = {
//                onEvent(AccountScreenEvent.ShowAccountDeleteOptions(state.pendingAccountDeletion))
//            },
//            onDismiss = {
//                onEvent(AccountScreenEvent.CancelAccountDeletion)
//            },
//            themeColors = themeColors
//        )
//    }
//
//    // Account delete options dialog with transaction statistics
//    if (state.showAccountDeleteOptionsDialog && state.pendingAccountDeletion != null) {
//        AccountDeleteOptionsDialogEnhanced(
//            accountName = state.pendingAccountDeletion.accountName,
//            transactionStats = accountTransactionStats,
//            onDelete = {
//                onEvent(AccountScreenEvent.ConfirmAccountDeletionWithTransactions(state.pendingAccountDeletion))
//            },
//            onMoveTransactions = {
//                onEvent(AccountScreenEvent.ShowAccountMoveTransactions(state.pendingAccountDeletion))
//            },
//            onCancel = {
//                onEvent(AccountScreenEvent.CancelAccountDeletion)
//            },
//            themeColors = themeColors
//        )
//    }
//
//    // Account move transactions dialog
//    if (state.showAccountMoveTransactionsDialog && state.pendingAccountDeletion != null) {
//        AccountMoveTransactionsDialog(
//            availableAccounts = state.availableAccountsForMigration,
//            selectedAccountId = state.selectedTargetAccountId,
//            onAccountSelected = { accountId ->
//                onEvent(AccountScreenEvent.SelectTargetAccountForMigration(accountId))
//            },
//            onConfirm = {
//                state.selectedTargetAccountId?.let { targetId ->
//                    onEvent(AccountScreenEvent.ConfirmAccountMigrationAndDeletion(state.pendingAccountDeletion, targetId))
//                }
//            },
//            onCancel = {
//                onEvent(AccountScreenEvent.CancelAccountDeletion)
//            },
//            themeColors = themeColors
//        )
//    }
//
//    // Merge account dialog
//    if (state.showMergeAccountDialog && state.pendingMergeAccountSource != null) {
//        AccountMoveTransactionsDialog(
//            availableAccounts = state.availableAccountsForMerge,
//            selectedAccountId = state.selectedMergeAccountTargetId,
//            onAccountSelected = { accountId ->
//                onEvent(AccountScreenEvent.SelectTargetAccountForMerge(accountId))
//            },
//            onConfirm = {
//                state.selectedMergeAccountTargetId?.let { targetId ->
//                    onEvent(AccountScreenEvent.ConfirmAccountMerge(state.pendingMergeAccountSource, targetId))
//                }
//            },
//            onCancel = {
//                onEvent(AccountScreenEvent.CancelMergeAccount)
//            },
//            themeColors = themeColors
//        )
//    }
//}

@Composable
private fun AccountDeleteConfirmationDialog(
    accountName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    themeColors: ColorScheme
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(R.drawable.delete_bulk),
                    contentDescription = "warning",
                    tint = ErrorColor.copy(0.8f),
                    modifier = Modifier.size(50.dp)
                )
                Text(
                    text = "Delete Account",
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = themeColors.inverseSurface,
                    textAlign = TextAlign.Center,
                )
            }
        },
        text = {
            Text(
                text = "This action will delete all transactions associated with this account.",
                fontFamily = iosFont,
                fontSize = 12.sp,
                color = themeColors.inverseSurface.copy(0.8f),
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ErrorColor,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .shadow(
                        elevation = 5.dp,
                        shape = RoundedCornerShape(12.dp),
                        spotColor =ErrorColor,
                        ambientColor = Color.Transparent
                    )
            ) {
                Text(
                    text = "Delete",
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColors.surface,
                    contentColor = themeColors.inverseSurface
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = "Cancel",
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Medium,
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = themeColors.surfaceBright
    )
}

@Composable
private fun AccountDeleteOptionsDialogEnhanced(
    accountName: String,
    transactionStats: AccountTransactionStats?,
    onDelete: () -> Unit,
    onMoveTransactions: () -> Unit,
    onCancel: () -> Unit,
    themeColors: ColorScheme
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(R.drawable.delete_bulk),
                    contentDescription = "warning",
                    tint = ErrorColor.copy(0.8f),
                    modifier = Modifier.size(50.dp)
                )
                Text(
                    text = "Delete Account",
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Deleting an account will delete all transactions associated with this account.",
                    fontFamily = iosFont,
                    fontSize = 14.sp,
                    color = themeColors.inverseSurface.copy(0.8f)
                )

                // Show transaction statistics if available
                transactionStats?.let { stats ->
                    if (stats.count > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = ErrorColor.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Impact on your accounts:",
                                    fontFamily = iosFont,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColors.error
                                )
                                Text(
                                    text = "• ${stats.count} transactions will be deleted",
                                    fontFamily = iosFont,
                                    fontSize = 12.sp,
                                    color = themeColors.onErrorContainer
                                )
                                if (stats.expenseAmount > 0) {
                                    Text(
                                        text = "• $${String.format("%.2f", stats.expenseAmount)} will be restored from expenses",
                                        fontFamily = iosFont,
                                        fontSize = 12.sp,
                                        color = themeColors.primary
                                    )
                                }
                                if (stats.incomeAmount > 0) {
                                    Text(
                                        text = "• $${String.format("%.2f", stats.incomeAmount)} will be removed from income",
                                        fontFamily = iosFont,
                                        fontSize = 12.sp,
                                        color = themeColors.onErrorContainer
                                    )
                                }
                                if (stats.transferAmount > 0) {
                                    Text(
                                        text = "• $${String.format("%.2f", stats.transferAmount)} in transfers will be reversed",
                                        fontFamily = iosFont,
                                        fontSize = 12.sp,
                                        color = themeColors.onErrorContainer
                                    )
                                }
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = themeColors.surface.copy(alpha = 0.5f)
                            )
                        ) {
                            Text(
                                text = "No transactions found in this account.",
                                fontFamily = iosFont,
                                fontSize = 12.sp,
                                color = themeColors.inverseSurface,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "If you want to save those transactions, move them to another account.",
                    fontFamily = iosFont,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = themeColors.inverseSurface.copy(0.8f)
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (transactionStats?.count ?: 0 > 0) {
                    Button(
                        onClick = onMoveTransactions,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = themeColors.primary
                        ),
                        modifier = Modifier
                            .shadow(
                                elevation = 5.dp,
                                shape = RoundedCornerShape(12.dp),
                                spotColor = themeColors.primary,
                                ambientColor = Color.Transparent
                            ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Move Transactions",
                            fontFamily = iosFont,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorColor,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .shadow(
                            elevation = 5.dp,
                            shape = RoundedCornerShape(12.dp),
                            spotColor =ErrorColor,
                            ambientColor = Color.Transparent
                        )
                ) {
                    Text(
                        text = "Delete",
                        fontFamily = iosFont,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        dismissButton = {
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColors.surface,
                    contentColor = themeColors.inverseSurface
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = "Cancel",
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Medium,
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = themeColors.surfaceBright
    )
}

@Composable
private fun AccountMoveTransactionsDialog(
    availableAccounts: List<AccountEntity>,
    selectedAccountId: Int?,
    onAccountSelected: (Int) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    themeColors: ColorScheme
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
//                Icon(
//                    painter = painterResource(R.drawable.delete_bulk),
//                    contentDescription = "warning",
//                    tint = ErrorColor.copy(0.8f),
//                    modifier = Modifier.size(50.dp)
//                )
                Text(
                    text = if(availableAccounts.isNotEmpty()) "Select Target Account" else "No Accounts Available",
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
            }
        },
        text = {
            if (availableAccounts.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableAccounts) { account ->
                        AccountSelectionItem(
                            account = account,
                            isSelected = selectedAccountId == account.id,
                            onSelected = { onAccountSelected(account.id) },
                            themeColors = themeColors
                        )
                    }
                }
            }else {
                Text(
                    text = "No accounts Available, please add a new account to transfer transactions",
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    color = themeColors.inverseSurface.copy(0.8f)
                )
            }
        },
        confirmButton = {
            if (availableAccounts.isNotEmpty()) {
                Button(
                    onClick = onConfirm,
                    enabled = selectedAccountId!= null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ErrorColor,
                        disabledContainerColor = themeColors.surface,
                        contentColor = Color.White,
                        disabledContentColor = themeColors.inverseSurface.copy(0.6f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .shadow(
                            elevation = 5.dp,
                            shape = RoundedCornerShape(12.dp),
                            spotColor = if (selectedAccountId != null) ErrorColor else themeColors.surface,
                            ambientColor = Color.Transparent
                        )
                ) {
                    Text(
                        text = "Continue",
                        fontFamily = iosFont,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        dismissButton = {
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColors.surface,
                    contentColor = themeColors.inverseSurface
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = "Cancel",
                    fontFamily = iosFont,
                    fontWeight = FontWeight.Medium,
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = themeColors.surfaceBright
    )
}

@Composable
private fun AccountSelectionItem(
    account: AccountEntity,
    isSelected: Boolean,
    onSelected: () -> Unit,
    themeColors: ColorScheme
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) themeColors.primary.copy(alpha = 0.1f)
                else themeColors.surface
            )
            .clickable { onSelected() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Account preview
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(account.cardColor1),
                            Color(account.cardColor2)
                        )
                    )
                )
        )

        // Account name and currency
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = account.accountName,
                fontSize = 16.sp,
                fontFamily = iosFont,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) themeColors.primary else themeColors.inverseSurface
            )
            Text(
                text = "${CurrencySymbols.getSymbol(account.currencyCode)}${String.format("%.2f", account.balance)}",
                fontSize = 12.sp,
                fontFamily = iosFont,
                color = themeColors.inverseSurface.copy(0.7f)
            )
        }

        // Selection indicator
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Selected",
                tint = themeColors.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
