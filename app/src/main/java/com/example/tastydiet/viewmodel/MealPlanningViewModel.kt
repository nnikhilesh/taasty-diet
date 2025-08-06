package com.example.tastydiet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tastydiet.AppDatabase
import com.example.tastydiet.data.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.abs

data class IntelligentMealSuggestion(
    val recipe: Recipe,
    val isAvailable: Boolean,
    val missingIngredients: List<String>,
    val macroMatch: Float, // 0.0 to 1.0 - how well it matches target macros
    val inventoryScore: Float // 0.0 to 1.0 - how many ingredients are available
)

data class DailyMealPlan(
    val date: LocalDate,
    val breakfast: List<Recipe>,
    val lunch: List<Recipe>,
    val snack: List<Recipe>,
    val dinner: List<Recipe>,
    val totalCalories: Float,
    val totalProtein: Float,
    val totalCarbs: Float,
    val totalFat: Float,
    val targetCalories: Float,
    val targetProtein: Float,
    val targetCarbs: Float,
    val targetFat: Float,
    val macroBalance: Float // How well balanced the day is (0.0 to 1.0)
)

data class InventoryCheck(
    val availableIngredients: List<String>,
    val missingIngredients: List<String>,
    val shoppingList: List<String>
)

class MealPlanningViewModel(application: Application) : AndroidViewModel(application) {
    private val profileDao = AppDatabase.getInstance(application).profileDao()
    private val recipeDao = AppDatabase.getInstance(application).recipeDao()
    private val inventoryDao = AppDatabase.getInstance(application).inventoryDao()
    private val foodLogDao = AppDatabase.getInstance(application).foodLogDao()
    
    private val _profiles = MutableStateFlow<List<Profile>>(emptyList())
    val profiles: StateFlow<List<Profile>> = _profiles.asStateFlow()
    
    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes.asStateFlow()
    
    private val _inventory = MutableStateFlow<List<InventoryItem>>(emptyList())
    val inventory: StateFlow<List<InventoryItem>> = _inventory.asStateFlow()
    
    private val _dailyFoodLogs = MutableStateFlow<List<FoodLog>>(emptyList())
    val dailyFoodLogs: StateFlow<List<FoodLog>> = _dailyFoodLogs.asStateFlow()
    
    private val _mealSuggestions = MutableStateFlow<List<IntelligentMealSuggestion>>(emptyList())
    val mealSuggestions: StateFlow<List<IntelligentMealSuggestion>> = _mealSuggestions.asStateFlow()
    
    private val _dailyMealPlan = MutableStateFlow<DailyMealPlan?>(null)
    val dailyMealPlan: StateFlow<DailyMealPlan?> = _dailyMealPlan.asStateFlow()
    
    private val _inventoryCheck = MutableStateFlow<InventoryCheck?>(null)
    val inventoryCheck: StateFlow<InventoryCheck?> = _inventoryCheck.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    init {
        loadAllData()
    }
    
    fun loadAllData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Load all data in parallel
                val profilesDeferred = profileDao.getAllProfiles()
                val recipesDeferred = recipeDao.getAll()
                val inventoryDeferred = inventoryDao.getAllInventoryItems()
                val foodLogsDeferred = foodLogDao.getAllFoodLogs()
                
                // Collect all data
                profilesDeferred.collect { profiles ->
                    _profiles.value = profiles
                }
                
                recipesDeferred.collect { recipes ->
                    _recipes.value = recipes
                }
                
                inventoryDeferred.collect { inventory ->
                    _inventory.value = inventory
                }
                
                foodLogsDeferred.collect { foodLogs ->
                    // Filter for today's food logs
                    val today = LocalDate.now()
                    val todayFoodLogs = foodLogs.filter { foodLog ->
                        try {
                            val foodLogDate = java.time.Instant.ofEpochMilli(foodLog.timestamp)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                            foodLogDate == today
                        } catch (e: Exception) {
                            false // Skip food logs with invalid dates
                        }
                    }
                    _dailyFoodLogs.value = todayFoodLogs
                }
                
                _errorMessage.value = "Data loaded successfully"
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun generateMealSuggestions(
        dietaryPreference: String,
        mealType: String,
        profileMode: String,
        selectedProfile: Profile?,
        guestCount: Int = 0
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                val recipes = _recipes.value
                val inventory = _inventory.value
                val profiles = _profiles.value
                val dailyFoodLogs = _dailyFoodLogs.value
                
                if (recipes.isEmpty()) {
                    _errorMessage.value = "No recipes available"
                    return@launch
                }
                
                // Calculate target macros for the meal
                val targetMacros = calculateTargetMacros(mealType, profileMode, selectedProfile, profiles, guestCount)
                
                // Calculate consumed macros for the day
                val consumedMacros = calculateConsumedMacros(dailyFoodLogs)
                
                // Calculate remaining macros for the meal
                val remainingMacros = calculateRemainingMacros(targetMacros, consumedMacros, mealType)
                
                // Filter recipes based on dietary preference and meal type
                val filteredRecipes = recipes.filter { recipe ->
                    val matchesDietary = when (dietaryPreference.lowercase()) {
                        "vegetarian" -> recipe.isVegetarian
                        "non_vegetarian" -> !recipe.isVegetarian
                        "mixed" -> true
                        else -> true
                    }
                    
                    val matchesMealType = recipe.isSuitableForMeal(mealType)
                    
                    matchesDietary && matchesMealType
                }
                
                // Generate meal suggestions with inventory check and macro matching
                val suggestions = filteredRecipes.map { recipe ->
                    val inventoryCheck = checkRecipeInventory(recipe, inventory)
                    val macroMatch = calculateMacroMatch(recipe, remainingMacros)
                    
                    IntelligentMealSuggestion(
                        recipe = recipe,
                        isAvailable = inventoryCheck.missingIngredients.isEmpty(),
                        missingIngredients = inventoryCheck.missingIngredients,
                        macroMatch = macroMatch,
                        inventoryScore = inventoryCheck.availableIngredients.size.toFloat() / 
                                       (inventoryCheck.availableIngredients.size + inventoryCheck.missingIngredients.size)
                    )
                }
                
                // Sort suggestions by availability and macro match
                val sortedSuggestions = suggestions.sortedByDescending { suggestion ->
                    (if (suggestion.isAvailable) 1.0f else 0.0f) * 0.6f + suggestion.macroMatch * 0.4f
                }
                
                _mealSuggestions.value = sortedSuggestions.take(20) // Top 20 suggestions
                
                // Update inventory check
                _inventoryCheck.value = InventoryCheck(
                    availableIngredients = inventory.map { it.name },
                    missingIngredients = sortedSuggestions.flatMap { it.missingIngredients }.distinct(),
                    shoppingList = sortedSuggestions.flatMap { it.missingIngredients }.distinct()
                )
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to generate meal suggestions: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun addMealToPlan(recipes: List<Recipe>, mealType: String) {
        viewModelScope.launch {
            try {
                val currentPlan = _dailyMealPlan.value ?: createEmptyMealPlan()
                val updatedPlan = when (mealType.lowercase()) {
                    "breakfast" -> currentPlan.copy(breakfast = recipes)
                    "lunch" -> currentPlan.copy(lunch = recipes)
                    "snack" -> currentPlan.copy(snack = recipes)
                    "dinner" -> currentPlan.copy(dinner = recipes)
                    else -> currentPlan
                }
                
                // Recalculate daily totals
                val recalculatedPlan = recalculateDailyTotals(updatedPlan)
                _dailyMealPlan.value = recalculatedPlan
                
                // Suggest next meal if this was lunch
                if (mealType.lowercase() == "lunch") {
                    suggestNextMeal("snack")
                } else if (mealType.lowercase() == "snack") {
                    suggestNextMeal("dinner")
                }
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add meal to plan: ${e.message}"
            }
        }
    }
    
    private fun suggestNextMeal(mealType: String) {
        // This will be called after adding a meal to suggest the next meal
        // Implementation will be added based on remaining macros
    }
    
    private fun calculateTargetMacros(
        mealType: String,
        profileMode: String,
        selectedProfile: Profile?,
        profiles: List<Profile>,
        guestCount: Int
    ): MealMacros {
        return when (profileMode.lowercase()) {
            "individual" -> {
                selectedProfile?.getMealMacros(mealType) ?: MealMacros(0f, 0f, 0f, 0f)
            }
                         "family" -> {
                 val familyMacros = profiles.fold(MealMacros(0.0f, 0.0f, 0.0f, 0.0f)) { acc, profile ->
                     val macros = profile.getMealMacros(mealType)
                     MealMacros(
                         calories = acc.calories + macros.calories,
                         protein = acc.protein + macros.protein,
                         carbs = acc.carbs + macros.carbs,
                         fat = acc.fat + macros.fat
                     )
                 }
                 MealMacros(
                     calories = familyMacros.calories,
                     protein = familyMacros.protein,
                     carbs = familyMacros.carbs,
                     fat = familyMacros.fat
                 )
             }
                         "guest" -> {
                 val familyMacros = profiles.fold(MealMacros(0.0f, 0.0f, 0.0f, 0.0f)) { acc, profile ->
                     val macros = profile.getMealMacros(mealType)
                     MealMacros(
                         calories = acc.calories + macros.calories,
                         protein = acc.protein + macros.protein,
                         carbs = acc.carbs + macros.carbs,
                         fat = acc.fat + macros.fat
                     )
                 }
                 val guestMacros = MealMacros(
                     calories = 500.0f * guestCount, // Average guest meal
                     protein = 20.0f * guestCount,
                     carbs = 60.0f * guestCount,
                     fat = 15.0f * guestCount
                 )
                 MealMacros(
                     calories = familyMacros.calories + guestMacros.calories,
                     protein = familyMacros.protein + guestMacros.protein,
                     carbs = familyMacros.carbs + guestMacros.carbs,
                     fat = familyMacros.fat + guestMacros.fat
                 )
             }
            else -> MealMacros(0f, 0f, 0f, 0f)
        }
    }
    
    private fun calculateConsumedMacros(foodLogs: List<FoodLog>): MealMacros {
        return foodLogs.fold(MealMacros(0f, 0f, 0f, 0f)) { acc, foodLog ->
            MealMacros(
                calories = acc.calories + (foodLog.calories ?: 0f),
                protein = acc.protein + (foodLog.protein ?: 0f),
                carbs = acc.carbs + (foodLog.carbs ?: 0f),
                fat = acc.fat + (foodLog.fat ?: 0f)
            )
        }
    }
    
    private fun calculateRemainingMacros(
        targetMacros: MealMacros,
        consumedMacros: MealMacros,
        mealType: String
    ): MealMacros {
        // For now, return target macros for the meal
        // In a more sophisticated system, we'd calculate remaining based on time of day
        return targetMacros
    }
    
    private fun checkRecipeInventory(recipe: Recipe, inventory: List<InventoryItem>): InventoryCheck {
        val recipeIngredients = recipe.ingredients.split(",").map { it.trim() }
        val availableIngredients = mutableListOf<String>()
        val missingIngredients = mutableListOf<String>()
        
        recipeIngredients.forEach { ingredient ->
            val hasIngredient = inventory.any { 
                it.name.contains(ingredient, ignoreCase = true) && it.quantity > 0 
            }
            if (hasIngredient) {
                availableIngredients.add(ingredient)
            } else {
                missingIngredients.add(ingredient)
            }
        }
        
        return InventoryCheck(
            availableIngredients = availableIngredients,
            missingIngredients = missingIngredients,
            shoppingList = missingIngredients
        )
    }
    
    private fun calculateMacroMatch(recipe: Recipe, targetMacros: MealMacros): Float {
        val recipeMacros = MealMacros(
            calories = recipe.caloriesPer100g.toFloat(),
            protein = recipe.proteinPer100g,
            carbs = recipe.carbsPer100g,
            fat = recipe.fatPer100g
        )
        
        // Calculate how well the recipe matches target macros (0.0 to 1.0)
        val calorieMatch = 1.0f - minOf(1.0f, abs(recipeMacros.calories - targetMacros.calories) / targetMacros.calories)
        val proteinMatch = 1.0f - minOf(1.0f, abs(recipeMacros.protein - targetMacros.protein) / targetMacros.protein)
        val carbsMatch = 1.0f - minOf(1.0f, abs(recipeMacros.carbs - targetMacros.carbs) / targetMacros.carbs)
        val fatMatch = 1.0f - minOf(1.0f, abs(recipeMacros.fat - targetMacros.fat) / targetMacros.fat)
        
        return (calorieMatch + proteinMatch + carbsMatch + fatMatch) / 4.0f
    }
    
    private fun createEmptyMealPlan(): DailyMealPlan {
        return DailyMealPlan(
            date = LocalDate.now(),
            breakfast = emptyList(),
            lunch = emptyList(),
            snack = emptyList(),
            dinner = emptyList(),
            totalCalories = 0f,
            totalProtein = 0f,
            totalCarbs = 0f,
            totalFat = 0f,
            targetCalories = 0f,
            targetProtein = 0f,
            targetCarbs = 0f,
            targetFat = 0f,
            macroBalance = 0f
        )
    }
    
         private fun recalculateDailyTotals(plan: DailyMealPlan): DailyMealPlan {
         val allRecipes = plan.breakfast + plan.lunch + plan.snack + plan.dinner
         
         val totalCalories = allRecipes.fold(0.0) { acc, recipe -> acc + recipe.caloriesPer100g }.toFloat()
         val totalProtein = allRecipes.fold(0.0) { acc, recipe -> acc + recipe.proteinPer100g }.toFloat()
         val totalCarbs = allRecipes.fold(0.0) { acc, recipe -> acc + recipe.carbsPer100g }.toFloat()
         val totalFat = allRecipes.fold(0.0) { acc, recipe -> acc + recipe.fatPer100g }.toFloat()
        
        // Calculate macro balance (how well it matches targets)
        val macroBalance = if (plan.targetCalories > 0) {
            val calorieBalance = 1.0f - minOf(1.0f, abs(totalCalories - plan.targetCalories) / plan.targetCalories)
            val proteinBalance = 1.0f - minOf(1.0f, abs(totalProtein - plan.targetProtein) / plan.targetProtein)
            val carbsBalance = 1.0f - minOf(1.0f, abs(totalCarbs - plan.targetCarbs) / plan.targetCarbs)
            val fatBalance = 1.0f - minOf(1.0f, abs(totalFat - plan.targetFat) / plan.targetFat)
            (calorieBalance + proteinBalance + carbsBalance + fatBalance) / 4.0f
        } else {
            0f
        }
        
        return plan.copy(
            totalCalories = totalCalories,
            totalProtein = totalProtein,
            totalCarbs = totalCarbs,
            totalFat = totalFat,
            macroBalance = macroBalance
        )
    }
    
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
} 