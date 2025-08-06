package com.example.tastydiet.util

import com.example.tastydiet.data.RecipeDao
import com.example.tastydiet.data.InventoryDao
import com.example.tastydiet.data.RecipeIngredientDao
import com.example.tastydiet.data.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import kotlin.math.abs
import kotlin.math.min

class SmartMealPlanner(
    private val recipeDao: RecipeDao,
    private val inventoryDao: InventoryDao,
    private val recipeIngredientDao: RecipeIngredientDao
) {

    // Meal type distribution percentages (can be adjusted)
    private val mealDistribution = mapOf(
        "Breakfast" to 0.25f, // 25% of daily calories
        "Lunch" to 0.35f,     // 35% of daily calories
        "Dinner" to 0.30f,    // 30% of daily calories
        "Snack" to 0.10f      // 10% of daily calories
    )

    suspend fun generateSmartMealPlan(
        member: FamilyMember,
        date: String
    ): SmartMealPlan = withContext(Dispatchers.IO) {
        try {
            val mealSuggestions = mutableListOf<MealSuggestion>()
            val availableInventory = inventoryDao.getAllInventoryItems().first().map { it.name.lowercase().trim() }
            
            // Generate suggestions for each meal type
            for ((mealType, caloriePercentage) in mealDistribution) {
                val targetCalories = member.targetCalories * caloriePercentage
                val suggestion = generateMealSuggestion(
                    mealType = mealType,
                    targetCalories = targetCalories,
                    member = member,
                    availableInventory = availableInventory
                )
                mealSuggestions.add(suggestion)
            }

            // Calculate totals with proper error handling
            val totalCalories = mealSuggestions.sumOf { it.calories.toDouble() }.toFloat()
            val totalProtein = mealSuggestions.sumOf { it.protein.toDouble() }.toFloat()
            val totalCarbs = mealSuggestions.sumOf { it.carbs.toDouble() }.toFloat()
            val totalFat = mealSuggestions.sumOf { it.fat.toDouble() }.toFloat()
            val totalFiber = mealSuggestions.sumOf { it.fiber.toDouble() }.toFloat()

            // Create meal plan
            SmartMealPlan(
                date = date,
                memberId = member.id,
                breakfastRecipeId = mealSuggestions.find { it.mealType == "Breakfast" }?.recipe?.id,
                lunchRecipeId = mealSuggestions.find { it.mealType == "Lunch" }?.recipe?.id,
                dinnerRecipeId = mealSuggestions.find { it.mealType == "Dinner" }?.recipe?.id,
                snackRecipeId = mealSuggestions.find { it.mealType == "Snack" }?.recipe?.id,
                totalCalories = totalCalories,
                totalProtein = totalProtein,
                totalCarbs = totalCarbs,
                totalFat = totalFat,
                totalFiber = totalFiber,
                targetCalories = member.targetCalories,
                targetProtein = member.targetProtein,
                targetCarbs = member.targetCarbs,
                targetFat = member.targetFat,
                targetFiber = member.fiberGoal.toFloat()
            )
        } catch (e: Exception) {
            // Return a default meal plan if there's an error
            println("Error generating meal plan: ${e.message}")
            createDefaultMealPlan(member, date)
        }
    }

    private suspend fun generateMealSuggestion(
        mealType: String,
        targetCalories: Float,
        member: FamilyMember,
        availableInventory: List<String>
    ): MealSuggestion {
        try {
            // Get recipes for this meal type
            val recipes = recipeDao.getRandomRecipesForMealType(mealType, 10)
            
            // Filter recipes based on available ingredients
            val feasibleRecipes = recipes.filter { recipe ->
                try {
                    val ingredients = recipeIngredientDao.getIngredientsForRecipe(recipe.id)
                    ingredients.all { ingredient ->
                        availableInventory.any { inventoryItem ->
                            inventoryItem.equals(ingredient.ingredientName.lowercase().trim(), ignoreCase = true) ||
                            ingredient.ingredientName.lowercase().trim().contains(inventoryItem) ||
                            inventoryItem.contains(ingredient.ingredientName.lowercase().trim())
                        }
                    }
                } catch (e: Exception) {
                    // If we can't get ingredients, assume it's feasible
                    true
                }
            }

            if (feasibleRecipes.isEmpty()) {
                // No feasible recipes, return a placeholder
                return createPlaceholderMealSuggestion(mealType)
            }

            // Find the best recipe based on calorie match and macro balance
            val bestRecipe = findBestRecipe(feasibleRecipes, targetCalories, member)

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

            return MealSuggestion(
                recipe = bestRecipe,
                mealType = mealType,
                portionSize = portionSize,
                calories = calories,
                protein = protein,
                carbs = carbs,
                fat = fat,
                fiber = fiber,
                ingredientsAvailable = true
            )
        } catch (e: Exception) {
            println("Error generating meal suggestion for $mealType: ${e.message}")
            return createPlaceholderMealSuggestion(mealType)
        }
    }

    private fun findBestRecipe(recipes: List<Recipe>, targetCalories: Float, member: FamilyMember): Recipe {
        if (recipes.isEmpty()) {
            return createDefaultRecipe()
        }
        
        // Score each recipe based on multiple factors
        val scoredRecipes = recipes.map { recipe ->
            var score = 0.0
            
            // Calorie match (closer to target is better)
            val calorieDiff = abs(recipe.caloriesPer100g - targetCalories)
            score += 100.0 / (1.0 + calorieDiff) // Higher score for closer match
            
            // Protein content (bonus for high protein)
            if (recipe.proteinPer100g > 15) score += 10
            
            // Fiber content (bonus for high fiber)
            if (recipe.fiberPer100g > 5) score += 5
            
            // Macro balance
            val macroBalance = calculateMacroBalance(recipe)
            score += macroBalance * 20
            
            recipe to score
        }
        
        return scoredRecipes.maxByOrNull { it.second }?.first ?: recipes.first()
    }

    private fun calculateMacroBalance(recipe: Recipe): Double {
        val totalCalories = recipe.caloriesPer100g * 4.184 // Convert to kJ
        if (totalCalories <= 0) return 0.0
        
        val proteinCalories = recipe.proteinPer100g * 4.0
        val carbCalories = recipe.carbsPer100g * 4.0
        val fatCalories = recipe.fatPer100g * 9.0
        
        val totalMacroCalories = proteinCalories + carbCalories + fatCalories
        if (totalMacroCalories <= 0) return 0.0
        
        val proteinRatio = proteinCalories / totalMacroCalories
        val carbRatio = carbCalories / totalMacroCalories
        val fatRatio = fatCalories / totalMacroCalories
        
        // Ideal ratios: Protein 20-30%, Carbs 45-65%, Fat 20-35%
        val proteinScore = when {
            proteinRatio in 0.2..0.3 -> 1.0
            proteinRatio in 0.15..0.35 -> 0.8
            else -> 0.5
        }
        
        val carbScore = when {
            carbRatio in 0.45..0.65 -> 1.0
            carbRatio in 0.35..0.75 -> 0.8
            else -> 0.5
        }
        
        val fatScore = when {
            fatRatio in 0.2..0.35 -> 1.0
            fatRatio in 0.15..0.45 -> 0.8
            else -> 0.5
        }
        
        return (proteinScore + carbScore + fatScore) / 3.0
    }

    private fun createPlaceholderMealSuggestion(mealType: String): MealSuggestion {
        return MealSuggestion(
            recipe = createDefaultRecipe(),
            mealType = mealType,
            portionSize = 0f,
            calories = 0f,
            protein = 0f,
            carbs = 0f,
            fat = 0f,
            fiber = 0f,
            ingredientsAvailable = false,
            missingIngredients = listOf("Ingredients not available")
        )
    }

    private fun createDefaultRecipe(): Recipe {
        return Recipe(
            name = "No suitable recipe found",
            cuisine = "Unknown",
            category = "General",
            caloriesPer100g = 0,
            proteinPer100g = 0f,
            carbsPer100g = 0f,
            fatPer100g = 0f,
            fiberPer100g = 0f,
            instructions = "No ingredients available for this meal type"
        )
    }

    private fun createDefaultMealPlan(member: FamilyMember, date: String): SmartMealPlan {
        return SmartMealPlan(
            date = date,
            memberId = member.id,
            breakfastRecipeId = null,
            lunchRecipeId = null,
            dinnerRecipeId = null,
            snackRecipeId = null,
            totalCalories = 0f,
            totalProtein = 0f,
            totalCarbs = 0f,
            totalFat = 0f,
            totalFiber = 0f,
            targetCalories = member.targetCalories,
            targetProtein = member.targetProtein,
            targetCarbs = member.targetCarbs,
            targetFat = member.targetFat,
            targetFiber = member.fiberGoal.toFloat()
        )
    }

    suspend fun regenerateMealSuggestion(
        mealType: String,
        member: FamilyMember,
        currentMealPlan: SmartMealPlan
    ): MealSuggestion = withContext(Dispatchers.IO) {
        try {
            val targetCalories = member.targetCalories * (mealDistribution[mealType] ?: 0.25f)
            val availableInventory = inventoryDao.getAllInventoryItems().first().map { it.name.lowercase().trim() }
            
            generateMealSuggestion(
                mealType = mealType,
                targetCalories = targetCalories,
                member = member,
                availableInventory = availableInventory
            )
        } catch (e: Exception) {
            println("Error regenerating meal suggestion: ${e.message}")
            createPlaceholderMealSuggestion(mealType)
        }
    }

    suspend fun getDailyMacroSummary(mealPlan: SmartMealPlan): DailyMacroSummary {
        return DailyMacroSummary(
            totalCalories = mealPlan.totalCalories,
            totalProtein = mealPlan.totalProtein,
            totalCarbs = mealPlan.totalCarbs,
            totalFat = mealPlan.totalFat,
            totalFiber = mealPlan.totalFiber,
            targetCalories = mealPlan.targetCalories,
            targetProtein = mealPlan.targetProtein,
            targetCarbs = mealPlan.targetCarbs,
            targetFat = mealPlan.targetFat,
            targetFiber = mealPlan.targetFiber
        )
    }

    suspend fun checkIngredientAvailability(recipeId: Int): Pair<Boolean, List<String>> {
        try {
            val ingredients = recipeIngredientDao.getIngredientsForRecipe(recipeId)
            val availableInventory = inventoryDao.getAllInventoryItems().first().map { it.name.lowercase().trim() }
            
            val missingIngredients = ingredients.filter { ingredient ->
                availableInventory.none { inventoryItem ->
                    inventoryItem.equals(ingredient.ingredientName.lowercase().trim(), ignoreCase = true) ||
                    ingredient.ingredientName.lowercase().trim().contains(inventoryItem) ||
                    inventoryItem.contains(ingredient.ingredientName.lowercase().trim())
                }
            }.map { it.ingredientName }
            
            return Pair(missingIngredients.isEmpty(), missingIngredients)
        } catch (e: Exception) {
            println("Error checking ingredient availability: ${e.message}")
            return Pair(false, listOf("Unable to check ingredients"))
        }
    }
} 