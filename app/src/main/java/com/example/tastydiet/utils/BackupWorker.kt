package com.example.tastydiet.utils

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

class BackupWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    
    override suspend fun doWork(): Result {
        return try {
            val backupManager = BackupManager(applicationContext)
            
            // Create backup
            val backupFile = backupManager.createBackup()
            if (backupFile != null) {
                // Export to Downloads
                backupManager.exportToDownloads()
                
                // Clean up old backups (keep only last 5)
                val backupFiles = backupManager.getBackupFiles()
                if (backupFiles.size > 5) {
                    backupFiles.drop(5).forEach { file ->
                        backupManager.deleteBackup(file)
                    }
                }
                
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }
    
    companion object {
        private const val BACKUP_WORK_NAME = "backup_work"
        
        fun scheduleBackup(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(true)
                .build()
            
            val backupWorkRequest = PeriodicWorkRequestBuilder<BackupWorker>(
                1, TimeUnit.DAYS
            )
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.HOURS)
                .build()
            
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    BACKUP_WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    backupWorkRequest
                )
        }
        
        fun cancelBackup(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(BACKUP_WORK_NAME)
        }
    }
} 