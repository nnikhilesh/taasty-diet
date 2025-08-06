package com.example.tastydiet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tastydiet.data.NotificationDao
import com.example.tastydiet.data.models.NotificationLog
import com.example.tastydiet.data.models.NotificationSettings
import com.example.tastydiet.data.models.ReminderSchedule
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class NotificationViewModel(
    private val notificationDao: NotificationDao
) : ViewModel() {
    
    private val _notificationSettings = MutableStateFlow<NotificationSettings?>(null)
    val notificationSettings: StateFlow<NotificationSettings?> = _notificationSettings.asStateFlow()
    
    private val _notificationLogs = MutableStateFlow<List<NotificationLog>>(emptyList())
    val notificationLogs: StateFlow<List<NotificationLog>> = _notificationLogs.asStateFlow()
    
    private val _unreadNotifications = MutableStateFlow<List<NotificationLog>>(emptyList())
    val unreadNotifications: StateFlow<List<NotificationLog>> = _unreadNotifications.asStateFlow()
    
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    fun loadNotificationSettings(profileId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val settings = notificationDao.getNotificationSettings(profileId)
                _notificationSettings.value = settings
                
                if (settings == null) {
                    // Create default settings
                    val defaultSettings = NotificationSettings(profileId = profileId)
                    notificationDao.insertNotificationSettings(defaultSettings)
                    _notificationSettings.value = defaultSettings
                }
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load notification settings: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadNotificationLogs(profileId: Int) {
        viewModelScope.launch {
            try {
                notificationDao.getNotificationLogs(profileId).collect { logs ->
                    _notificationLogs.value = logs
                }
                
                notificationDao.getUnreadNotifications(profileId).collect { unread ->
                    _unreadNotifications.value = unread
                    _unreadCount.value = unread.size
                }
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load notification logs: ${e.message}"
            }
        }
    }
    
    fun updateNotificationSettings(settings: NotificationSettings) {
        viewModelScope.launch {
            try {
                notificationDao.updateNotificationSettings(settings)
                _notificationSettings.value = settings
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update notification settings: ${e.message}"
            }
        }
    }
    
    fun toggleMealReminders(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val currentSettings = _notificationSettings.value
                if (currentSettings != null) {
                    val updatedSettings = currentSettings.copy(mealRemindersEnabled = enabled)
                    notificationDao.updateNotificationSettings(updatedSettings)
                    _notificationSettings.value = updatedSettings
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to toggle meal reminders: ${e.message}"
            }
        }
    }
    
    fun toggleWaterReminders(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val currentSettings = _notificationSettings.value
                if (currentSettings != null) {
                    val updatedSettings = currentSettings.copy(waterRemindersEnabled = enabled)
                    notificationDao.updateNotificationSettings(updatedSettings)
                    _notificationSettings.value = updatedSettings
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to toggle water reminders: ${e.message}"
            }
        }
    }
    
    fun toggleLowStockReminders(enabled: Boolean) {
        viewModelScope.launch {
            try {
                val currentSettings = _notificationSettings.value
                if (currentSettings != null) {
                    val updatedSettings = currentSettings.copy(lowStockRemindersEnabled = enabled)
                    notificationDao.updateNotificationSettings(updatedSettings)
                    _notificationSettings.value = updatedSettings
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to toggle low stock reminders: ${e.message}"
            }
        }
    }
    
    fun updateMealTime(mealType: String, time: String) {
        viewModelScope.launch {
            try {
                val currentSettings = _notificationSettings.value
                if (currentSettings != null) {
                    val updatedSettings = when (mealType.lowercase()) {
                        "breakfast" -> currentSettings.copy(breakfastTime = time)
                        "lunch" -> currentSettings.copy(lunchTime = time)
                        "dinner" -> currentSettings.copy(dinnerTime = time)
                        "snack" -> currentSettings.copy(snackTime = time)
                        else -> currentSettings
                    }
                    notificationDao.updateNotificationSettings(updatedSettings)
                    _notificationSettings.value = updatedSettings
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update meal time: ${e.message}"
            }
        }
    }
    
    fun updateWaterReminderInterval(minutes: Int) {
        viewModelScope.launch {
            try {
                val currentSettings = _notificationSettings.value
                if (currentSettings != null) {
                    val updatedSettings = currentSettings.copy(waterReminderInterval = minutes)
                    notificationDao.updateNotificationSettings(updatedSettings)
                    _notificationSettings.value = updatedSettings
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update water reminder interval: ${e.message}"
            }
        }
    }
    
    fun updateLowStockThreshold(threshold: Int) {
        viewModelScope.launch {
            try {
                val currentSettings = _notificationSettings.value
                if (currentSettings != null) {
                    val updatedSettings = currentSettings.copy(lowStockThreshold = threshold)
                    notificationDao.updateNotificationSettings(updatedSettings)
                    _notificationSettings.value = updatedSettings
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update low stock threshold: ${e.message}"
            }
        }
    }
    
    fun addNotificationLog(
        type: String,
        title: String,
        message: String,
        profileId: Int
    ) {
        viewModelScope.launch {
            try {
                val log = NotificationLog(
                    type = type,
                    title = title,
                    message = message,
                    profileId = profileId
                )
                notificationDao.insertNotificationLog(log)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add notification log: ${e.message}"
            }
        }
    }
    
    fun markNotificationAsRead(logId: Int) {
        viewModelScope.launch {
            try {
                notificationDao.markNotificationAsRead(logId)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to mark notification as read: ${e.message}"
            }
        }
    }
    
    fun markAllNotificationsAsRead(profileId: Int) {
        viewModelScope.launch {
            try {
                notificationDao.markAllNotificationsAsRead(profileId)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to mark all notifications as read: ${e.message}"
            }
        }
    }
    
    fun deleteNotificationLog(log: NotificationLog) {
        viewModelScope.launch {
            try {
                notificationDao.deleteNotificationLog(log)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete notification log: ${e.message}"
            }
        }
    }
    
    fun clearOldNotificationLogs(daysOld: Int) {
        viewModelScope.launch {
            try {
                val cutoffTime = System.currentTimeMillis() - (daysOld * 24 * 60 * 60 * 1000L)
                notificationDao.deleteOldNotificationLogs(cutoffTime)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to clear old notification logs: ${e.message}"
            }
        }
    }
    
    fun getReminderSchedules(): List<ReminderSchedule> {
        val settings = _notificationSettings.value ?: return emptyList()
        
        return listOf(
            ReminderSchedule(
                mealType = "Breakfast",
                time = settings.breakfastTime,
                isEnabled = settings.mealRemindersEnabled
            ),
            ReminderSchedule(
                mealType = "Lunch",
                time = settings.lunchTime,
                isEnabled = settings.mealRemindersEnabled
            ),
            ReminderSchedule(
                mealType = "Dinner",
                time = settings.dinnerTime,
                isEnabled = settings.mealRemindersEnabled
            ),
            ReminderSchedule(
                mealType = "Snack",
                time = settings.snackTime,
                isEnabled = settings.mealRemindersEnabled
            )
        )
    }
    
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
} 