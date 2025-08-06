package com.example.tastydiet.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.example.tastydiet.MainActivity
import com.example.tastydiet.R
import com.example.tastydiet.AppDatabase
import com.example.tastydiet.data.models.NotificationSettings
import java.util.concurrent.TimeUnit

class NotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            val notificationType = inputData.getString("notification_type") ?: return Result.failure()
            val profileId = inputData.getInt("profile_id", 0)
            
            when (notificationType) {
                "meal_reminder" -> {
                    sendMealReminder(profileId)
                    Result.success()
                }
                "water_reminder" -> {
                    sendWaterReminder(profileId)
                    Result.success()
                }
                "low_stock_reminder" -> {
                    sendLowStockReminder(profileId)
                    Result.success()
                }
                else -> Result.failure()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }
    
    private suspend fun sendMealReminder(profileId: Int) {
        val database = AppDatabase.getInstance(applicationContext)
        val notificationDao = database.notificationDao()
        val settings = notificationDao.getNotificationSettings(profileId)
        
        if (settings?.mealRemindersEnabled == true) {
            val mealType = inputData.getString("meal_type") ?: "Meal"
            val title = "Time for $mealType!"
            val message = "Don't forget to log your $mealType and stay on track with your nutrition goals."
            
            showNotification(title, message, "meal_reminder")
            
            // Log notification
            notificationDao.insertNotificationLog(
                com.example.tastydiet.data.models.NotificationLog(
                    type = "meal",
                    title = title,
                    message = message,
                    profileId = profileId
                )
            )
        }
    }
    
    private suspend fun sendWaterReminder(profileId: Int) {
        val database = AppDatabase.getInstance(applicationContext)
        val notificationDao = database.notificationDao()
        val settings = notificationDao.getNotificationSettings(profileId)
        
        if (settings?.waterRemindersEnabled == true) {
            val title = "Stay Hydrated!"
            val message = "Time to drink some water. Staying hydrated is important for your health."
            
            showNotification(title, message, "water_reminder")
            
            // Log notification
            notificationDao.insertNotificationLog(
                com.example.tastydiet.data.models.NotificationLog(
                    type = "water",
                    title = title,
                    message = message,
                    profileId = profileId
                )
            )
        }
    }
    
    private suspend fun sendLowStockReminder(profileId: Int) {
        val database = AppDatabase.getInstance(applicationContext)
        val notificationDao = database.notificationDao()
        val settings = notificationDao.getNotificationSettings(profileId)
        
        if (settings?.lowStockRemindersEnabled == true) {
            val title = "Low Stock Alert"
            val message = "Some items in your inventory are running low. Check your shopping list."
            
            showNotification(title, message, "low_stock_reminder")
            
            // Log notification
            notificationDao.insertNotificationLog(
                com.example.tastydiet.data.models.NotificationLog(
                    type = "low_stock",
                    title = title,
                    message = message,
                    profileId = profileId
                )
            )
        }
    }
    
    private fun showNotification(title: String, message: String, channelId: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Tasty Diet Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications from Tasty Diet app"
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        // Create intent for notification tap
        val intent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Build notification
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
    
    companion object {
        private const val MEAL_REMINDER_WORK = "meal_reminder_work"
        private const val WATER_REMINDER_WORK = "water_reminder_work"
        private const val LOW_STOCK_REMINDER_WORK = "low_stock_reminder_work"
        
        fun scheduleMealReminders(context: Context, profileId: Int, settings: NotificationSettings) {
            if (!settings.mealRemindersEnabled) return
            
            val workManager = WorkManager.getInstance(context)
            
            // Schedule breakfast reminder
            scheduleMealReminder(context, profileId, "breakfast", settings.breakfastTime)
            
            // Schedule lunch reminder
            scheduleMealReminder(context, profileId, "lunch", settings.lunchTime)
            
            // Schedule dinner reminder
            scheduleMealReminder(context, profileId, "dinner", settings.dinnerTime)
            
            // Schedule snack reminder
            scheduleMealReminder(context, profileId, "snack", settings.snackTime)
        }
        
        private fun scheduleMealReminder(context: Context, profileId: Int, mealType: String, time: String) {
            val workManager = WorkManager.getInstance(context)
            
            // Parse time (format: "HH:mm")
            val timeParts = time.split(":")
            val hour = timeParts[0].toInt()
            val minute = timeParts[1].toInt()
            
            // Calculate delay until next occurrence
            val now = java.time.LocalTime.now()
            val targetTime = java.time.LocalTime.of(hour, minute)
            val delay = if (now.isBefore(targetTime)) {
                java.time.Duration.between(now, targetTime)
            } else {
                java.time.Duration.between(now, targetTime.plusHours(24))
            }
            
            val inputData = Data.Builder()
                .putString("notification_type", "meal_reminder")
                .putInt("profile_id", profileId)
                .putString("meal_type", mealType)
                .build()
            
            val mealReminderWork = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInputData(inputData)
                .setInitialDelay(delay.toMillis(), TimeUnit.MILLISECONDS)
                .build()
            
            workManager.enqueueUniqueWork(
                "${MEAL_REMINDER_WORK}_${mealType}_$profileId",
                ExistingWorkPolicy.REPLACE,
                mealReminderWork
            )
        }
        
        fun scheduleWaterReminders(context: Context, profileId: Int, settings: NotificationSettings) {
            if (!settings.waterRemindersEnabled) return
            
            val workManager = WorkManager.getInstance(context)
            
            val inputData = Data.Builder()
                .putString("notification_type", "water_reminder")
                .putInt("profile_id", profileId)
                .build()
            
            val waterReminderWork = PeriodicWorkRequestBuilder<NotificationWorker>(
                settings.waterReminderInterval.toLong(), TimeUnit.MINUTES
            )
                .setInputData(inputData)
                .build()
            
            workManager.enqueueUniquePeriodicWork(
                "${WATER_REMINDER_WORK}_$profileId",
                ExistingPeriodicWorkPolicy.REPLACE,
                waterReminderWork
            )
        }
        
        fun scheduleLowStockReminders(context: Context, profileId: Int, settings: NotificationSettings) {
            if (!settings.lowStockRemindersEnabled) return
            
            val workManager = WorkManager.getInstance(context)
            
            val inputData = Data.Builder()
                .putString("notification_type", "low_stock_reminder")
                .putInt("profile_id", profileId)
                .build()
            
            val lowStockReminderWork = PeriodicWorkRequestBuilder<NotificationWorker>(
                6, TimeUnit.HOURS // Check every 6 hours
            )
                .setInputData(inputData)
                .build()
            
            workManager.enqueueUniquePeriodicWork(
                "${LOW_STOCK_REMINDER_WORK}_$profileId",
                ExistingPeriodicWorkPolicy.REPLACE,
                lowStockReminderWork
            )
        }
        
        fun cancelAllReminders(context: Context, profileId: Int) {
            val workManager = WorkManager.getInstance(context)
            
            workManager.cancelUniqueWork("${MEAL_REMINDER_WORK}_breakfast_$profileId")
            workManager.cancelUniqueWork("${MEAL_REMINDER_WORK}_lunch_$profileId")
            workManager.cancelUniqueWork("${MEAL_REMINDER_WORK}_dinner_$profileId")
            workManager.cancelUniqueWork("${MEAL_REMINDER_WORK}_snack_$profileId")
            workManager.cancelUniqueWork("${WATER_REMINDER_WORK}_$profileId")
            workManager.cancelUniqueWork("${LOW_STOCK_REMINDER_WORK}_$profileId")
        }
    }
} 