package com.example.tastydiet.data

import android.content.Context
import android.util.Log
import com.example.tastydiet.data.models.FoodItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import java.io.IOException

class FoodDatabaseImporter(private val context: Context) {
    
    companion object {
        private const val TAG = "FoodDatabaseImporter"
        private const val JSON_FILE_NAME = "comprehensive_food_database.json"
    }
    
    /**
     * Imports food items from the JSON file into the database
     */
    suspend fun importFoodDatabase(foodItemDao: FoodItemDao): ImportResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Starting food database import...")
                
                // Read JSON file from assets
                val jsonString = readJsonFromAssets(JSON_FILE_NAME)
                if (jsonString == null) {
                    return@withContext ImportResult.Error("Failed to read JSON file: $JSON_FILE_NAME")
                }
                
                // Parse JSON
                val jsonObject = JSONObject(jsonString)
                val foodItemsArray = jsonObject.getJSONArray("foodItems")
                
                val foodItems = mutableListOf<FoodItem>()
                
                // Convert JSON to FoodItem objects
                for (i in 0 until foodItemsArray.length()) {
                    val item = foodItemsArray.getJSONObject(i)
                    val foodItem = FoodItem(
                        name = item.getString("name"),
                        category = item.getString("category"),
                        unit = item.getString("unit"),
                        caloriesPerUnit = item.getDouble("caloriesPerUnit").toFloat(),
                        proteinPerUnit = item.getDouble("proteinPerUnit").toFloat(),
                        carbsPerUnit = item.getDouble("carbsPerUnit").toFloat(),
                        fatPerUnit = item.getDouble("fatPerUnit").toFloat(),
                        fiberPerUnit = item.optDouble("fiberPerUnit", 0.0).toFloat(),
                        sugarPerUnit = item.optDouble("sugarPerUnit", 0.0).toFloat(),
                        sodiumPerUnit = item.optDouble("sodiumPerUnit", 0.0).toFloat(),
                        isVeg = item.optBoolean("isVeg", true),
                        isGlutenFree = item.optBoolean("isGlutenFree", true),
                        isDairyFree = item.optBoolean("isDairyFree", true),
                        description = item.optString("description", ""),
                        imageUrl = item.optString("imageUrl", ""),
                        isActive = true
                    )
                    foodItems.add(foodItem)
                }
                
                // Insert into database
                foodItemDao.insertFoodItems(foodItems)
                
                Log.d(TAG, "Successfully imported ${foodItems.size} food items")
                ImportResult.Success(foodItems.size)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error importing food database", e)
                ImportResult.Error("Import failed: ${e.message}")
            }
        }
    }
    
    /**
     * Checks if the database has been imported
     */
    suspend fun isDatabaseImported(foodItemDao: FoodItemDao): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val count = foodItemDao.getFoodItemsCount()
                count > 50 // Consider imported if more than 50 items exist
            } catch (e: Exception) {
                Log.e(TAG, "Error checking database import status", e)
                false
            }
        }
    }
    
    /**
     * Gets import statistics
     */
    suspend fun getImportStats(foodItemDao: FoodItemDao): ImportStats {
        return withContext(Dispatchers.IO) {
            try {
                val totalItems = foodItemDao.getFoodItemsCount()
                val categories = foodItemDao.getAllCategories().first()
                
                ImportStats(
                    totalItems = totalItems,
                    categories = categories.size,
                    isImported = totalItems > 50
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error getting import stats", e)
                ImportStats(0, 0, false)
            }
        }
    }
    
    /**
     * Reads JSON file from assets
     */
    private fun readJsonFromAssets(fileName: String): String? {
        return try {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            Log.e(TAG, "Error reading JSON file: $fileName", e)
            null
        }
    }
    
    /**
     * Result of import operation
     */
    sealed class ImportResult {
        data class Success(val itemCount: Int) : ImportResult()
        data class Error(val message: String) : ImportResult()
    }
    
    /**
     * Import statistics
     */
    data class ImportStats(
        val totalItems: Int,
        val categories: Int,
        val isImported: Boolean
    )
} 