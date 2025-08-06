package com.example.tastydiet.data

import androidx.room.*
import com.example.tastydiet.data.models.NotificationLog
import com.example.tastydiet.data.models.NotificationSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    // Notification Settings
    @Query("SELECT * FROM notification_settings WHERE profileId = :profileId")
    suspend fun getNotificationSettings(profileId: Int): NotificationSettings?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificationSettings(settings: NotificationSettings): Long
    
    @Update
    suspend fun updateNotificationSettings(settings: NotificationSettings)
    
    @Query("DELETE FROM notification_settings WHERE profileId = :profileId")
    suspend fun deleteNotificationSettings(profileId: Int)
    
    // Notification Logs
    @Query("SELECT * FROM notification_logs WHERE profileId = :profileId ORDER BY timestamp DESC")
    fun getNotificationLogs(profileId: Int): Flow<List<NotificationLog>>
    
    @Query("SELECT * FROM notification_logs WHERE profileId = :profileId AND isRead = 0 ORDER BY timestamp DESC")
    fun getUnreadNotifications(profileId: Int): Flow<List<NotificationLog>>
    
    @Query("SELECT COUNT(*) FROM notification_logs WHERE profileId = :profileId AND isRead = 0")
    suspend fun getUnreadNotificationCount(profileId: Int): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificationLog(log: NotificationLog): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificationLogs(logs: List<NotificationLog>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotificationSettings(settings: List<NotificationSettings>)
    
    @Update
    suspend fun updateNotificationLog(log: NotificationLog)
    
    @Query("UPDATE notification_logs SET isRead = 1 WHERE id = :logId")
    suspend fun markNotificationAsRead(logId: Int)
    
    @Query("UPDATE notification_logs SET isRead = 1 WHERE profileId = :profileId")
    suspend fun markAllNotificationsAsRead(profileId: Int)
    
    @Delete
    suspend fun deleteNotificationLog(log: NotificationLog)
    
    @Query("DELETE FROM notification_logs WHERE profileId = :profileId")
    suspend fun deleteAllNotificationLogs(profileId: Int)
    
    @Query("DELETE FROM notification_logs WHERE timestamp < :timestamp")
    suspend fun deleteOldNotificationLogs(timestamp: Long)
} 