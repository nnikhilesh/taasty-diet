package com.example.tastydiet.utils

import android.content.Context
import android.os.Environment
import com.example.tastydiet.AppDatabase
import com.example.tastydiet.data.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class BackupManager(private val context: Context) {
    
    data class AppBackup(
        val version: String = "1.0",
        val timestamp: Long = System.currentTimeMillis(),
        val profiles: List<Profile> = emptyList(),
        val foodLogs: List<FoodLog> = emptyList(),
        val foodLogEntries: List<FoodLogEntry> = emptyList(),
        val recipes: List<Recipe> = emptyList(),
        val ingredients: List<Ingredient> = emptyList(),
        val inventoryItems: List<InventoryItem> = emptyList(),
        val mealPlans: List<MealPlan> = emptyList(),
        val meals: List<Meal> = emptyList(),
        val familyMembers: List<FamilyMember> = emptyList(),
        val analyticsData: List<AnalyticsData> = emptyList(),
        val shoppingListItems: List<ShoppingListItem> = emptyList(),
        val notificationSettings: List<NotificationSettings> = emptyList(),
        val notificationLogs: List<NotificationLog> = emptyList()
    )
    
    suspend fun createBackup(): File? = withContext(Dispatchers.IO) {
        try {
            val database = AppDatabase.getInstance(context)
            
            // Collect all data from database
            val backup = AppBackup(
                profiles = database.profileDao().getAllProfiles().first(),
                foodLogs = database.foodLogDao().getAllFoodLogs().first(),
                foodLogEntries = database.foodLogEntryDao().getAllFoodLogEntries().first(),
                recipes = database.recipeDao().getAllRecipes().first(),
                ingredients = database.ingredientDao().getAllIngredients().first(),
                inventoryItems = database.inventoryDao().getAllInventoryItems().first(),
                mealPlans = database.mealPlanDao().getAllMealPlans().first(),
                meals = database.mealDao().getAllMeals().first(),
                familyMembers = database.familyMemberDao().getAllFamilyMembers().first(),
                analyticsData = database.analyticsDao().getAnalyticsByProfile(0).first(),
                shoppingListItems = database.shoppingListDao().getAllItems().first(),
                notificationSettings = emptyList(), // Will be populated when needed
                notificationLogs = database.notificationDao().getNotificationLogs(0).first()
            )
            
            // Convert to JSON
            val jsonString = Json.encodeToString(backup)
            
            // Create backup file
            val backupDir = File(context.getExternalFilesDir(null), "backups")
            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }
            
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val timestamp = dateFormat.format(Date())
            val backupFile = File(backupDir, "tasty_diet_backup_$timestamp.zip")
            
            // Create ZIP file with JSON data
            ZipOutputStream(FileOutputStream(backupFile)).use { zipOut ->
                val entry = ZipEntry("backup.json")
                zipOut.putNextEntry(entry)
                zipOut.write(jsonString.toByteArray())
                zipOut.closeEntry()
            }
            
            backupFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    suspend fun restoreBackup(backupFile: File): Boolean = withContext(Dispatchers.IO) {
        try {
            val database = AppDatabase.getInstance(context)
            
            // Read ZIP file
            ZipInputStream(FileInputStream(backupFile)).use { zipIn ->
                var entry = zipIn.nextEntry
                while (entry != null) {
                    if (entry.name == "backup.json") {
                        val jsonString = zipIn.readBytes().toString(Charsets.UTF_8)
                        val backup = Json.decodeFromString<AppBackup>(jsonString)
                        
                        // Clear existing data
                        database.clearAllTables()
                        
                        // Restore data
                        database.profileDao().insertAllProfiles(backup.profiles)
                        database.foodLogDao().insertAllFoodLogs(backup.foodLogs)
                        database.foodLogEntryDao().insertAllFoodLogEntries(backup.foodLogEntries)
                        database.recipeDao().insertAllRecipes(backup.recipes)
                        database.ingredientDao().insertAllIngredients(backup.ingredients)
                        database.inventoryDao().insertAllInventoryItems(backup.inventoryItems)
                        database.mealPlanDao().insertAllMealPlans(backup.mealPlans)
                        database.mealDao().insertAllMeals(backup.meals)
                        database.familyMemberDao().insertAllFamilyMembers(backup.familyMembers)
                        database.analyticsDao().insertAnalytics(backup.analyticsData)
                        database.shoppingListDao().insertItems(backup.shoppingListItems)
                        database.notificationDao().insertNotificationSettings(backup.notificationSettings)
                        database.notificationDao().insertNotificationLogs(backup.notificationLogs)
                        
                        return@withContext true
                    }
                    entry = zipIn.nextEntry
                }
            }
            
            false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    suspend fun exportToDownloads(): File? = withContext(Dispatchers.IO) {
        try {
            val backupFile = createBackup() ?: return@withContext null
            
            // Copy to Downloads folder
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val downloadsFile = File(downloadsDir, backupFile.name)
            
            backupFile.copyTo(downloadsFile, overwrite = true)
            
            // Delete temporary file
            backupFile.delete()
            
            downloadsFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    suspend fun getBackupFiles(): List<File> = withContext(Dispatchers.IO) {
        try {
            val backupDir = File(context.getExternalFilesDir(null), "backups")
            if (!backupDir.exists()) {
                return@withContext emptyList()
            }
            
            backupDir.listFiles()
                ?.filter { it.extension == "zip" }
                ?.sortedByDescending { it.lastModified() }
                ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    suspend fun deleteBackup(backupFile: File): Boolean = withContext(Dispatchers.IO) {
        try {
            backupFile.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    suspend fun getBackupInfo(backupFile: File): AppBackup? = withContext(Dispatchers.IO) {
        try {
            ZipInputStream(FileInputStream(backupFile)).use { zipIn ->
                var entry = zipIn.nextEntry
                while (entry != null) {
                    if (entry.name == "backup.json") {
                        val jsonString = zipIn.readBytes().toString(Charsets.UTF_8)
                        return@withContext Json.decodeFromString<AppBackup>(jsonString)
                    }
                    entry = zipIn.nextEntry
                }
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    suspend fun createDataExport(): File? = withContext(Dispatchers.IO) {
        try {
            val database = AppDatabase.getInstance(context)
            
            // Create a simple JSON export (not compressed)
            val exportData = mapOf(
                "profiles" to database.profileDao().getAllProfiles().first(),
                "foodLogs" to database.foodLogDao().getAllFoodLogs().first(),
                "recipes" to database.recipeDao().getAllRecipes().first(),
                "ingredients" to database.ingredientDao().getAllIngredients().first(),
                "inventory" to database.inventoryDao().getAllInventoryItems().first(),
                "mealPlans" to database.mealPlanDao().getAllMealPlans().first(),
                "shoppingList" to database.shoppingListDao().getAllItems().first()
            )
            
            val jsonString = Json.encodeToString(exportData)
            
            // Save to Downloads
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val timestamp = dateFormat.format(Date())
            val exportFile = File(downloadsDir, "tasty_diet_export_$timestamp.json")
            
            exportFile.writeText(jsonString)
            
            exportFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    suspend fun validateBackup(backupFile: File): Boolean = withContext(Dispatchers.IO) {
        try {
            val backup = getBackupInfo(backupFile) ?: return@withContext false
            
            // Check if backup has essential data
            backup.profiles.isNotEmpty() || 
                   backup.recipes.isNotEmpty() || 
                   backup.foodLogs.isNotEmpty()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    suspend fun getBackupSize(backupFile: File): Long = withContext(Dispatchers.IO) {
        try {
            backupFile.length()
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        }
    }
    
    suspend fun getBackupDate(backupFile: File): Date? = withContext(Dispatchers.IO) {
        try {
            val backup = getBackupInfo(backupFile) ?: return@withContext null
            Date(backup.timestamp)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
} 