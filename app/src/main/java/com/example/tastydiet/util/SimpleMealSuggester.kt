package com.example.tastydiet.util

import com.example.tastydiet.data.models.Recipe
import com.example.tastydiet.data.models.FamilyMember
import kotlin.math.abs

class SimpleMealSuggester {
    
    // Meal type distribution percentages
    private val mealDistribution = mapOf(
        "Breakfast" to 0.25f, // 25% of daily calories
        "Lunch" to 0.35f,     // 35% of daily calories
        "Dinner" to 0.30f,    // 30% of daily calories
        "Snack" to 0.10f      // 10% of daily calories
    )
    
    data class SimpleMealSuggestion(
        val recipe: Recipe,
        val mealType: String,
        val portionSize: Float, // in grams
        val calories: Float,
        val protein: Float,
        val carbs: Float,
        val fat: Float,
        val fiber: Float
    )
    
    data class DailyMealPlan(
        val breakfast: SimpleMealSuggestion?,
        val lunch: SimpleMealSuggestion?,
        val dinner: SimpleMealSuggestion?,
        val snack: SimpleMealSuggestion?,
        val totalCalories: Float,
        val totalProtein: Float,
        val totalCarbs: Float,
        val totalFat: Float,
        val totalFiber: Float
    )
    
    fun generateMealSuggestions(
        allRecipes: List<Recipe>,
        member: FamilyMember
    ): DailyMealPlan {
        
        val suggestions = mutableListOf<SimpleMealSuggestion>()
        
        // Generate suggestions for each meal type
        for ((mealType, caloriePercentage) in mealDistribution) {
            val targetCalories = member.targetCalories * caloriePercentage
            val suggestion = generateMealSuggestion(
                allRecipes = allRecipes,
                mealType = mealType,
                targetCalories = targetCalories
            )
            suggestions.add(suggestion)
        }
        
        // Calculate totals
        val totalCalories = suggestions.sumOf { it.calories.toDouble() }.toFloat()
        val totalProtein = suggestions.sumOf { it.protein.toDouble() }.toFloat()
        val totalCarbs = suggestions.sumOf { it.carbs.toDouble() }.toFloat()
        val totalFat = suggestions.sumOf { it.fat.toDouble() }.toFloat()
        val totalFiber = suggestions.sumOf { it.fiber.toDouble() }.toFloat()
        
        return DailyMealPlan(
            breakfast = suggestions.find { it.mealType == "Breakfast" },
            lunch = suggestions.find { it.mealType == "Lunch" },
            dinner = suggestions.find { it.mealType == "Dinner" },
            snack = suggestions.find { it.mealType == "Snack" },
            totalCalories = totalCalories,
            totalProtein = totalProtein,
            totalCarbs = totalCarbs,
            totalFat = totalFat,
            totalFiber = totalFiber
        )
    }
    
    private fun generateMealSuggestion(
        allRecipes: List<Recipe>,
        mealType: String,
        targetCalories: Float
    ): SimpleMealSuggestion {
        
        // Filter recipes by meal type (using category field)
        val suitableRecipes = allRecipes.filter { recipe ->
            recipe.category.equals(mealType, ignoreCase = true) ||
            recipe.name.lowercase().contains(mealType.lowercase())
        }
        
        // If no specific meal type recipes, use any recipe
        val recipesToUse = if (suitableRecipes.isNotEmpty()) {
            suitableRecipes
        } else {
            allRecipes
        }
        
        // Find the best recipe based on calorie match
        val bestRecipe = recipesToUse.minByOrNull { recipe ->
            abs(recipe.caloriesPer100g - targetCalories)
        } ?: recipesToUse.firstOrNull() ?: createDefaultRecipe(mealType)
        
        // Calculate portion size to match target calories
        val portionSize = if (bestRecipe.caloriesPer100g > 0) {
            (targetCalories / bestRecipe.caloriesPer100g) * 100
        } else {
            100f // Default 100g portion
        }
        
        // Calculate macros for the portion
        val calories = (bestRecipe.caloriesPer100g * portionSize) / 100
        val protein = (bestRecipe.proteinPer100g * portionSize) / 100
        val carbs = (bestRecipe.carbsPer100g * portionSize) / 100
        val fat = (bestRecipe.fatPer100g * portionSize) / 100
        val fiber = (bestRecipe.fiberPer100g * portionSize) / 100
        
        return SimpleMealSuggestion(
            recipe = bestRecipe,
            mealType = mealType,
            portionSize = portionSize,
            calories = calories,
            protein = protein,
            carbs = carbs,
            fat = fat,
            fiber = fiber
        )
    }
    
    private fun createDefaultRecipe(mealType: String): Recipe {
        return Recipe(
            name = "Default $mealType Recipe",
            cuisine = "General",
            category = mealType,
            caloriesPer100g = 200,
            proteinPer100g = 10f,
            carbsPer100g = 20f,
            fatPer100g = 8f,
            fiberPer100g = 3f,
            instructions = "A balanced $mealType option"
        )
    }
    
    fun getHighProteinRecipes(recipes: List<Recipe>): List<Recipe> {
        return recipes.filter { it.proteinPer100g > 15 }
    }
    
    fun getLowCalorieRecipes(recipes: List<Recipe>): List<Recipe> {
        return recipes.filter { it.caloriesPer100g < 200 }
    }
    
    fun getHighFiberRecipes(recipes: List<Recipe>): List<Recipe> {
        return recipes.filter { it.fiberPer100g > 5 }
    }
    
    fun searchRecipes(recipes: List<Recipe>, query: String): List<Recipe> {
        return recipes.filter { recipe ->
            recipe.name.contains(query, ignoreCase = true) ||
            recipe.cuisine.contains(query, ignoreCase = true) ||
            recipe.category.contains(query, ignoreCase = true)
        }
    }
} 