package com.ritesh.cashiro.presentation.ui.features.notifications

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ritesh.cashiro.data.local.entity.TransactionEntity
import com.ritesh.cashiro.data.local.entity.TransactionType
import com.ritesh.cashiro.domain.repository.ReminderType
import com.ritesh.cashiro.domain.repository.SettingsRepository
import com.ritesh.cashiro.domain.repository.TransactionRepository
import com.ritesh.cashiro.domain.utils.AppEventBus
import com.ritesh.cashiro.domain.utils.TransactionEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    val settingsRepository: SettingsRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    val settingsFlow = settingsRepository.settingsFlow

    // Use the new NotificationState for better state management
    private val _state = MutableStateFlow(NotificationState())
    val state: StateFlow<NotificationState> = _state.asStateFlow()

    // Backward compatibility - expose individual state properties
    val upcomingTransactions: StateFlow<List<TransactionEntity>> =
        _state.map { it.upcomingTransactions }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val isLoading: StateFlow<Boolean> =
        _state.map { it.isLoading }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        // Load initial data
        loadInitialData()

        // Listen to settings changes
        viewModelScope.launch {
            settingsFlow.collect { settings ->
                updateState { it.copy(settings = settings) }
            }
        }

        // Listen to transaction events for automatic updates
        viewModelScope.launch {
            AppEventBus.events.collect { event ->
                when (event) {
                    is TransactionEvent.TransactionsUpdated -> {
                        Log.d("NotificationsViewModel", "Received TransactionsUpdated event, refreshing upcoming transactions")
                        handleEvent(NotificationEvent.LoadUpcomingTransactions)
                    }
                    // You can add other event types here if needed
                    else -> {
                        // Handle other events if needed
                    }
                }
            }
        }
    }

    /**
     * Handle events from the UI
     */
    fun handleEvent(event: NotificationEvent) {
        when (event) {
            is NotificationEvent.LoadUpcomingTransactions -> loadUpcomingTransactions()
            is NotificationEvent.RefreshData -> refreshData()
            is NotificationEvent.UpdateAddTransactionReminder -> updateAddTransactionReminderInternal(event.enabled)
            is NotificationEvent.UpdateUpcomingTransactions -> updateUpcomingTransactionsInternal(event.enabled)
            is NotificationEvent.UpdateReminderType -> updateReminderTypeInternal(event.reminderType)
            is NotificationEvent.UpdateAlertTime -> updateAlertTimeInternal(event.hour, event.minute)
            is NotificationEvent.UpdateSipNotifications -> updateSipNotificationsInternal(event.sipNotifications)
            is NotificationEvent.ToggleTransactionNotification -> toggleTransactionNotification(event.transactionId, event.enabled)
            is NotificationEvent.MarkTransactionAsPaid -> markTransactionAsPaid(event.transaction)
            is NotificationEvent.MarkTransactionAsUnpaid -> markTransactionAsUnpaid(event.transaction)
            is NotificationEvent.SetReminderTypeModalVisible -> updateState { it.copy(showReminderTypeModal = event.visible) }
            is NotificationEvent.SetTimePickerVisible -> updateState { it.copy(showTimePicker = event.visible) }
            is NotificationEvent.SetPermissionDialogVisible -> updateState { it.copy(showPermissionDialog = event.visible) }
            is NotificationEvent.SetFilterOnlyUnpaid -> updateState { it.copy(showOnlyUnpaidTransactions = event.onlyUnpaid) }
            is NotificationEvent.SetSortByDueDate -> updateState { it.copy(sortByDueDate = event.sortByDueDate) }
            is NotificationEvent.ShowError -> updateState { it.copy(error = event.message) }
            is NotificationEvent.ClearError -> updateState { it.copy(error = null) }
            is NotificationEvent.RequestNotificationPermissions -> {
                // Handle permission request - this would typically be handled in the UI
            }
            is NotificationEvent.CheckNotificationPermissions -> {
                // Check permission status - this would typically be handled in the UI
            }
        }
    }

    private fun updateState(update: (NotificationState) -> NotificationState) {
        _state.update(update)
    }

    private fun loadInitialData() {
        loadUpcomingTransactions()
    }

    private fun refreshData() {
        Log.d("NotificationsViewModel", "Refreshing all notification data")
        loadUpcomingTransactions()
    }

    private fun loadUpcomingTransactions() {
        viewModelScope.launch {
            try {
                updateState { it.copy(isLoading = true, error = null) }
                Log.d("NotificationsViewModel", "Loading upcoming transactions...")

                // Get all transactions that are:
                // 1. Upcoming type OR
                // 2. Subscription/Repetitive with next due date
                val allTransactions = transactionRepository.getAllTransactions()
                val currentTime = System.currentTimeMillis()

                val upcomingList = allTransactions.filter { transaction ->
                    when (transaction.transactionType) {
                        TransactionType.UPCOMING -> {
                            // Include upcoming transactions that have a due date
                            // Show both paid and unpaid, but filter by due date
                            transaction.nextDueDate != null &&
                                    transaction.nextDueDate > currentTime - (24 * 60 * 60 * 1000) // Include transactions from yesterday
                        }
                        TransactionType.SUBSCRIPTION, TransactionType.REPETITIVE -> {
                            // Include subscription/repetitive transactions that have a next due date in the future
                            transaction.nextDueDate != null &&
                                    transaction.nextDueDate > currentTime - (24 * 60 * 60 * 1000) // Include from yesterday
                        }
                        else -> false
                    }
                }.sortedBy { it.nextDueDate ?: it.date }

                updateState {
                    it.copy(
                        upcomingTransactions = upcomingList,
                        isLoading = false
                    )
                }

                Log.d("NotificationsViewModel", "Loaded ${upcomingList.size} upcoming transactions")

                // Log each transaction for debugging
                upcomingList.forEach { transaction ->
                    Log.d("NotificationsViewModel", "Upcoming: ${transaction.title}, Due: ${transaction.nextDueDate}, Type: ${transaction.transactionType}, Paid: ${transaction.isPaid}")
                }

            } catch (e: Exception) {
                Log.e("NotificationsViewModel", "Error loading upcoming transactions: ${e.message}", e)
                updateState {
                    it.copy(
                        upcomingTransactions = emptyList(),
                        isLoading = false,
                        error = "Error loading transactions: ${e.message}"
                    )
                }
            }
        }
    }

    private fun toggleTransactionNotification(transactionId: Int, enabled: Boolean) {
        viewModelScope.launch {
            try {
                val currentSettings = _state.value.notificationSettings
                if (currentSettings != null) {
                    val updatedNotifications = currentSettings.sipNotifications.toMutableMap()
                    updatedNotifications["transaction_$transactionId"] = enabled
                    updateSipNotificationsInternal(updatedNotifications)
                }
            } catch (e: Exception) {
                Log.e("NotificationsViewModel", "Error toggling transaction notification: ${e.message}", e)
                updateState { it.copy(error = "Error updating notification: ${e.message}") }
            }
        }
    }

    private fun markTransactionAsPaid(transaction: TransactionEntity) {
        viewModelScope.launch {
            try {
                // This would typically call the transaction repository to mark as paid
                // For now, just refresh the data
                loadUpcomingTransactions()
            } catch (e: Exception) {
                Log.e("NotificationsViewModel", "Error marking transaction as paid: ${e.message}", e)
                updateState { it.copy(error = "Error updating transaction: ${e.message}") }
            }
        }
    }

    private fun markTransactionAsUnpaid(transaction: TransactionEntity) {
        viewModelScope.launch {
            try {
                // This would typically call the transaction repository to mark as unpaid
                // For now, just refresh the data
                loadUpcomingTransactions()
            } catch (e: Exception) {
                Log.e("NotificationsViewModel", "Error marking transaction as unpaid: ${e.message}", e)
                updateState { it.copy(error = "Error updating transaction: ${e.message}") }
            }
        }
    }

    // Existing methods updated to work with new state
    private fun updateAddTransactionReminderInternal(enabled: Boolean) {
        viewModelScope.launch {
            try {
                settingsRepository.updateAddTransactionReminder(enabled)
            } catch (e: Exception) {
                updateState { it.copy(error = "Error updating reminder: ${e.message}") }
            }
        }
    }

    private fun updateReminderTypeInternal(reminderType: ReminderType) {
        viewModelScope.launch {
            try {
                settingsRepository.updateReminderType(reminderType)
            } catch (e: Exception) {
                updateState { it.copy(error = "Error updating reminder type: ${e.message}") }
            }
        }
    }

    private fun updateAlertTimeInternal(hour: Int, minute: Int) {
        viewModelScope.launch {
            try {
                settingsRepository.updateAlertTime(hour, minute)
                updateState { it.copy(selectedHour = hour, selectedMinute = minute) }
            } catch (e: Exception) {
                updateState { it.copy(error = "Error updating alert time: ${e.message}") }
            }
        }
    }

    private fun updateUpcomingTransactionsInternal(enabled: Boolean) {
        viewModelScope.launch {
            try {
                settingsRepository.updateUpcomingTransactions(enabled)
            } catch (e: Exception) {
                updateState { it.copy(error = "Error updating upcoming transactions: ${e.message}") }
            }
        }
    }

    private fun updateSipNotificationsInternal(sipNotifications: Map<String, Boolean>) {
        viewModelScope.launch {
            try {
                settingsRepository.updateSipNotifications(sipNotifications)
            } catch (e: Exception) {
                updateState { it.copy(error = "Error updating notifications: ${e.message}") }
            }
        }
    }

    // Backward compatibility methods
    fun refreshUpcomingTransactions() {
        handleEvent(NotificationEvent.LoadUpcomingTransactions)
    }

    fun forceRefresh() {
        handleEvent(NotificationEvent.RefreshData)
    }

    // Expose these as suspend functions for existing code compatibility
    suspend fun updateAddTransactionReminder(enabled: Boolean) {
        updateAddTransactionReminder(enabled)
    }

    suspend fun updateReminderType(reminderType: ReminderType) {
        updateReminderType(reminderType)
    }

    suspend fun updateAlertTime(hour: Int, minute: Int) {
        updateAlertTime(hour, minute)
    }

    suspend fun updateUpcomingTransactions(enabled: Boolean) {
        updateUpcomingTransactions(enabled)
    }

    suspend fun updateSipNotifications(sipNotifications: Map<String, Boolean>) {
        updateSipNotifications(sipNotifications)
    }
}

//@HiltViewModel
//class NotificationsViewModel @Inject constructor(
//    val settingsRepository: SettingsRepository,
//    private val transactionRepository: TransactionRepository
//) : ViewModel() {
//
//    val settingsFlow = settingsRepository.settingsFlow
//
//    // StateFlow for upcoming transactions
//    private val _upcomingTransactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
//    val upcomingTransactions: StateFlow<List<TransactionEntity>> = _upcomingTransactions.asStateFlow()
//
//    init {
//        // Load upcoming transactions when ViewModel is created
//        loadUpcomingTransactions()
//    }
//
//    private fun loadUpcomingTransactions() {
//        viewModelScope.launch {
//            try {
//                // Get all transactions that are:
//                // 1. Upcoming type OR
//                // 2. Subscription/Repetitive with next due date
//                val allTransactions = transactionRepository.getAllTransactions()
//                val currentTime = System.currentTimeMillis()
//
//                val upcomingList = allTransactions.filter { transaction ->
//                    when (transaction.transactionType) {
//                        TransactionType.UPCOMING -> {
//                            // Include upcoming transactions that are not paid and are in the future
//                            !transaction.isPaid && transaction.date > currentTime
//                        }
//                        TransactionType.SUBSCRIPTION, TransactionType.REPETITIVE -> {
//                            // Include subscription/repetitive transactions that have a next due date in the future
//                            transaction.nextDueDate != null &&
//                                    transaction.nextDueDate > currentTime &&
//                                    !transaction.isPaid
//                        }
//                        else -> false
//                    }
//                }.sortedBy { it.nextDueDate ?: it.date }
//
//                _upcomingTransactions.value = upcomingList
//                Log.d("NotificationsViewModel", "Loaded ${upcomingList.size} upcoming transactions")
//            } catch (e: Exception) {
//                Log.e("NotificationsViewModel", "Error loading upcoming transactions: ${e.message}", e)
//                _upcomingTransactions.value = emptyList()
//            }
//        }
//    }
//
//    // Refresh upcoming transactions (call this when transactions are updated)
//    fun refreshUpcomingTransactions() {
//        loadUpcomingTransactions()
//    }
//
//    suspend fun updateAddTransactionReminder(enabled: Boolean) {
//        settingsRepository.updateAddTransactionReminder(enabled)
//    }
//
//    suspend fun updateReminderType(reminderType: ReminderType) {
//        settingsRepository.updateReminderType(reminderType)
//    }
//
//    suspend fun updateAlertTime(hour: Int, minute: Int) {
//        settingsRepository.updateAlertTime(hour, minute)
//    }
//
//    suspend fun updateUpcomingTransactions(enabled: Boolean) {
//        settingsRepository.updateUpcomingTransactions(enabled)
//    }
//
//    suspend fun updateSipNotifications(sipNotifications: Map<String, Boolean>) {
//        settingsRepository.updateSipNotifications(sipNotifications)
//    }
//}

//@HiltViewModel
//class NotificationsViewModel @Inject constructor(
//    val settingsRepository: SettingsRepository
//) : ViewModel() {
//
//    val settingsFlow = settingsRepository.settingsFlow
//
//    suspend fun updateAddTransactionReminder(enabled: Boolean) {
//        settingsRepository.updateAddTransactionReminder(enabled)
//    }
//
//    suspend fun updateReminderType(reminderType: ReminderType) {
//        settingsRepository.updateReminderType(reminderType)
//    }
//
//    suspend fun updateAlertTime(hour: Int, minute: Int) {
//        settingsRepository.updateAlertTime(hour, minute)
//    }
//
//    suspend fun updateUpcomingTransactions(enabled: Boolean) {
//        settingsRepository.updateUpcomingTransactions(enabled)
//    }
//
//    suspend fun updateSipNotifications(sipNotifications: Map<String, Boolean>) {
//        settingsRepository.updateSipNotifications(sipNotifications)
//    }
//}
