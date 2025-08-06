package com.example.tastydiet.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "notification_settings")
@Serializable
data class NotificationSettings(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val profileId: Int,
    val mealRemindersEnabled: Boolean = true,
    val waterRemindersEnabled: Boolean = true,
    val lowStockRemindersEnabled: Boolean = true,
    val breakfastTime: String = "08:00",
    val lunchTime: String = "13:00",
    val dinnerTime: String = "19:00",
    val snackTime: String = "15:00",
    val waterReminderInterval: Int = 120, // minutes
    val lowStockThreshold: Int = 2, // items
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notification_logs")
@Serializable
data class NotificationLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "meal", "water", "low_stock"
    val title: String,
    val message: String,
    val profileId: Int,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class ReminderSchedule(
    val mealType: String,
    val time: String,
    val isEnabled: Boolean,
    val daysOfWeek: List<Int> = listOf(1, 2, 3, 4, 5, 6, 7) // Monday = 1, Sunday = 7
) 