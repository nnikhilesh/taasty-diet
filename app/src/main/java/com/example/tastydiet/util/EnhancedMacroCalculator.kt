package com.example.tastydiet.util

import com.example.tastydiet.data.NutritionalInfoDao
import com.example.tastydiet.data.models.NutritionalInfo
import com.example.tastydiet.data.models.Macros
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Enhanced macro calculator that uses real nutritional data from the database.
 * Provides accurate macro calculations based on food name and quantity.
 */
class EnhancedMacroCalculator(private val nutritionalInfoDao: NutritionalInfoDao) {
    
    /**
     * Calculate macros for a food item by looking up its nutritional info
     * @param foodName Name of the food item
     * @param quantity Quantity in grams
     * @return Macros object with calculated values, or null if food not found
     */
    suspend fun calculateMacrosForFood(foodName: String, quantity: Float): Macros? {
        return withContext(Dispatchers.IO) {
            try {
                // First try exact match
                var nutritionalInfo = nutritionalInfoDao.getByName(foodName)
                
                // If exact match fails, try fuzzy search and use the first result
                if (nutritionalInfo == null) {
                    val searchResults = nutritionalInfoDao.searchByName(foodName)
                    nutritionalInfo = searchResults.firstOrNull()
                }
                
                nutritionalInfo?.let { info ->
                    val multiplier = quantity / 100f
                    val calories = (info.caloriesPer100g * multiplier).toDouble()
                    val protein = (info.proteinPer100g * multiplier).toDouble()
                    val carbs = (info.carbsPer100g * multiplier).toDouble()
                    val fat = (info.fatPer100g * multiplier).toDouble()
                    val fiber = (info.fiberPer100g * multiplier).toDouble()
                    
                    Macros(
                        calories = calories,
                        protein = protein,
                        carbs = carbs,
                        fat = fat,
                        fiber = fiber
                    )
                }
            } catch (e: Exception) {
                null
            }
        }
    }
    
    /**
     * Search for food items by name (fuzzy search)
     * @param query Search query
     * @return List of matching nutritional info items
     */
    suspend fun searchFoodItems(query: String): List<NutritionalInfo> {
        return withContext(Dispatchers.IO) {
            try {
                nutritionalInfoDao.searchByName(query)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    /**
     * Get food items by category
     * @param category Food category
     * @return List of nutritional info items in the category
     */
    suspend fun getFoodItemsByCategory(category: String): List<NutritionalInfo> {
        return withContext(Dispatchers.IO) {
            try {
                nutritionalInfoDao.getByCategory(category)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    /**
     * Get nutritional preview for a food item
     * @param foodName Name of the food item
     * @param quantity Quantity in grams
     * @return Formatted string with nutritional info, or null if not found
     */
    suspend fun getNutritionalPreview(foodName: String, quantity: Float): String? {
        val macros = calculateMacrosForFood(foodName, quantity)
        return macros?.let {
            "${foodName} (${quantity}g) = ${it.calories.toInt()} kcal, ${it.protein.toInt()}g protein, ${it.carbs.toInt()}g carbs, ${it.fat.toInt()}g fat"
        }
    }
    
    /**
     * Calculate total macros for multiple food items
     * @param foodItems List of food items with name and quantity
     * @return Total macros
     */
    suspend fun calculateTotalMacros(foodItems: List<Pair<String, Float>>): Macros {
        var totalCalories = 0.0
        var totalProtein = 0.0
        var totalCarbs = 0.0
        var totalFat = 0.0
        var totalFiber = 0.0
        
        for ((foodName, quantity) in foodItems) {
            val macros = calculateMacrosForFood(foodName, quantity)
            macros?.let {
                totalCalories += it.calories
                totalProtein += it.protein
                totalCarbs += it.carbs
                totalFat += it.fat
                totalFiber += it.fiber
            }
        }
        
        return Macros(
            calories = totalCalories,
            protein = totalProtein,
            carbs = totalCarbs,
            fat = totalFat,
            fiber = totalFiber
        )
    }
    
    /**
     * Get common food suggestions for quick selection
     * @return List of common food items
     */
    suspend fun getCommonFoods(): List<NutritionalInfo> {
        val commonFoodNames = listOf(
            "Rice (White)", "Chapati", "Dal", "Curd", "Potato", "Tomato", 
            "Onion", "Apple", "Banana", "Milk (Whole)", "Egg (Whole)"
        )
        
        return withContext(Dispatchers.IO) {
            try {
                commonFoodNames.mapNotNull { name ->
                    nutritionalInfoDao.getByName(name)
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
    
    /**
     * Validate if a food item exists in the database
     * @param foodName Name of the food item
     * @return True if food exists, false otherwise
     */
    suspend fun isFoodInDatabase(foodName: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                nutritionalInfoDao.getByName(foodName) != null
            } catch (e: Exception) {
                false
            }
        }
    }
} 