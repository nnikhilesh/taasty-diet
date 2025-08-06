package com.example.tastydiet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tastydiet.data.IngredientDao
import com.example.tastydiet.data.InventoryDao
import com.example.tastydiet.data.RecipeDao
import com.example.tastydiet.data.models.*
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.abs
import kotlin.math.min

class SuggestionEngine(
    private val recipeDao: RecipeDao,
    private val ingredientDao: IngredientDao,
    private val inventoryDao: InventoryDao
) {
    suspend fun suggestRecipe(
        familyMembers: List<FamilyMember>,
        inventory: List<InventoryItem>,
        recipes: List<Recipe>,
        ingredientsMap: Map<Int, List<Ingredient>>,
        likedRecipeIds: StateFlow<Set<Int>>,
        recentlySuggested: StateFlow<Set<Int>>,
        dietPref: WeeklyDietPreference?,
        forTomorrow: Boolean = false
    ): List<Recipe> {
        try {
            if (familyMembers.isEmpty() || recipes.isEmpty()) {
                return emptyList()
            }

            val likedIds = likedRecipeIds.value
            val recentIds = recentlySuggested.value

            val scoredRecipes = recipes.mapNotNull { recipe ->
                if (likedIds.contains(recipe.id) || recentIds.contains(recipe.id)) {
                    return@mapNotNull null
                }

                val score = calculateRecipeScore(
                    recipe = recipe,
                    familyMembers = familyMembers,
                    inventory = inventory,
                    ingredientsMap = ingredientsMap,
                    dietPref = dietPref,
                    forTomorrow = forTomorrow
                )

                if (score > 0) {
                    RecipeScore(recipe, score)
                } else {
                    null
                }
            }

            return scoredRecipes
                .sortedByDescending { it.score }
                .take(5)
                .map { it.recipe }

        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }

    private fun calculateRecipeScore(
        recipe: Recipe,
        familyMembers: List<FamilyMember>,
        inventory: List<InventoryItem>,
        ingredientsMap: Map<Int, List<Ingredient>>,
        dietPref: WeeklyDietPreference?,
        forTomorrow: Boolean
    ): Int {
        var score = 0

        // Base score from recipe (using liked status instead of rating)
        score += if (recipe.liked == true) 10 else 0

        // Macro balance score
        score += calculateMacroBalanceScore(recipe, familyMembers)

        // Inventory availability score
        val ingredients = ingredientsMap[recipe.id] ?: emptyList()
        score += calculateInventoryMatch(ingredients, inventory)

        // Diet preference score
        score += calculateDietPreferenceScore(recipe, dietPref, forTomorrow)

        // Calorie range score
        score += calculateCalorieRangeScore(recipe, familyMembers)

        return score
    }

    private fun calculateMacroBalanceScore(recipe: Recipe, familyMembers: List<FamilyMember>): Int {
        val totalProtein = recipe.proteinPer100g * familyMembers.size
        val totalCarbs = recipe.carbsPer100g * familyMembers.size
        val totalFat = recipe.fatPer100g * familyMembers.size

        val avgProteinGoal = familyMembers.map { it.proteinGoal }.average().toFloat()
        val avgCarbGoal = familyMembers.map { it.carbGoal }.average().toFloat()
        val avgFatGoal = familyMembers.map { it.fatGoal }.average().toFloat()

        val proteinDiff = abs(totalProtein - avgProteinGoal)
        val carbsDiff = abs(totalCarbs - avgCarbGoal)
        val fatDiff = abs(totalFat - avgFatGoal)

        return when {
            proteinDiff < 10 && carbsDiff < 20 && fatDiff < 10 -> 20
            proteinDiff < 20 && carbsDiff < 40 && fatDiff < 20 -> 10
            else -> 0
        }
    }

    private fun calculateInventoryMatch(
        ingredients: List<Ingredient>,
        inventory: List<InventoryItem>
    ): Int {
        if (ingredients.isEmpty()) return 0

        val inventoryNames = inventory.map { it.name.lowercase() }
        var availableCount = 0
        val totalIngredients = ingredients.size

        ingredients.forEach { ingredient ->
            val ingredientName = ingredient.name.lowercase()
            
            // Exact match
            if (inventoryNames.contains(ingredientName)) {
                availableCount++
                return@forEach
            }

            // Partial match
            val partialMatch = inventoryNames.any { inventoryName ->
                inventoryName.contains(ingredientName) || ingredientName.contains(inventoryName)
            }
            if (partialMatch) {
                availableCount++
            }
        }

        val availabilityPercentage = availableCount.toFloat() / totalIngredients
        return when {
            availabilityPercentage >= 0.8f -> 5  // 80%+ available
            availabilityPercentage >= 0.6f -> 3  // 60%+ available
            availabilityPercentage >= 0.4f -> 1  // 40%+ available
            else -> 0
        }
    }

    private fun calculateDietPreferenceScore(
        recipe: Recipe,
        dietPref: WeeklyDietPreference?,
        forTomorrow: Boolean
    ): Int {
        if (dietPref == null) return 0

        val dayOfWeek = if (forTomorrow) {
            java.time.LocalDate.now().plusDays(1).dayOfWeek.value
        } else {
            java.time.LocalDate.now().dayOfWeek.value
        }

        val dayPreference = when (dayOfWeek) {
            1 -> dietPref.monday
            2 -> dietPref.tuesday
            3 -> dietPref.wednesday
            4 -> dietPref.thursday
            5 -> dietPref.friday
            6 -> dietPref.saturday
            7 -> dietPref.sunday
            else -> "mixed"
        }

        return when {
            dayPreference == "veg" && recipe.isVeg -> 15
            dayPreference == "non-veg" && !recipe.isVeg -> 15
            dayPreference == "mixed" -> 5
            else -> 0
        }
    }

    private fun calculateCalorieRangeScore(recipe: Recipe, familyMembers: List<FamilyMember>): Int {
        val avgCalorieGoal = familyMembers.map { it.calorieGoal }.average().toFloat()
        val recipeCalories = recipe.caloriesPer100g * familyMembers.size

        val calorieDiff = abs(recipeCalories - avgCalorieGoal)
        val percentageDiff = (calorieDiff / avgCalorieGoal) * 100

        return when {
            percentageDiff < 10 -> 10
            percentageDiff < 20 -> 5
            percentageDiff < 30 -> 2
            else -> 0
        }
    }

    private data class RecipeScore(val recipe: Recipe, val score: Int)
} 