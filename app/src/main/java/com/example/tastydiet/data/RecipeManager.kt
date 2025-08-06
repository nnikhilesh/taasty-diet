package com.example.tastydiet.data

import android.content.Context
import android.content.SharedPreferences
import com.example.tastydiet.data.models.Recipe
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecipeManager(private val context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("custom_recipes", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    companion object {
        private const val CUSTOM_RECIPES_KEY = "custom_recipes"
        private const val NEXT_RECIPE_ID_KEY = "next_recipe_id"
    }
    
    suspend fun addCustomRecipe(recipe: Recipe): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val customRecipes = getCustomRecipes().toMutableList()
                
                // Generate unique ID for new recipe
                val nextId = getNextRecipeId()
                val newRecipe = recipe.copy(id = nextId)
                
                customRecipes.add(newRecipe)
                saveCustomRecipes(customRecipes)
                incrementNextRecipeId()
                
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
    
    suspend fun updateCustomRecipe(recipe: Recipe): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val customRecipes = getCustomRecipes().toMutableList()
                val index = customRecipes.indexOfFirst { it.id == recipe.id }
                
                if (index != -1) {
                    customRecipes[index] = recipe
                    saveCustomRecipes(customRecipes)
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
    
    suspend fun deleteCustomRecipe(recipeId: Int): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val customRecipes = getCustomRecipes().toMutableList()
                val removed = customRecipes.removeIf { it.id == recipeId }
                
                if (removed) {
                    saveCustomRecipes(customRecipes)
                }
                
                removed
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
    
    suspend fun getAllCustomRecipes(): List<Recipe> {
        return withContext(Dispatchers.IO) {
            getCustomRecipes()
        }
    }
    
    suspend fun getCustomRecipeById(id: Int): Recipe? {
        return withContext(Dispatchers.IO) {
            getCustomRecipes().find { it.id == id }
        }
    }
    
    suspend fun searchCustomRecipes(query: String): List<Recipe> {
        return withContext(Dispatchers.IO) {
            val recipes = getCustomRecipes()
            recipes.filter { recipe ->
                recipe.name.contains(query, ignoreCase = true) ||
                recipe.category.contains(query, ignoreCase = true) ||
                recipe.cuisine.contains(query, ignoreCase = true)
            }
        }
    }
    
    private fun getCustomRecipes(): List<Recipe> {
        val json = sharedPreferences.getString(CUSTOM_RECIPES_KEY, "[]")
        val type = object : TypeToken<List<Recipe>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
    
    private fun saveCustomRecipes(recipes: List<Recipe>) {
        val json = gson.toJson(recipes)
        sharedPreferences.edit().putString(CUSTOM_RECIPES_KEY, json).apply()
    }
    
    private fun getNextRecipeId(): Int {
        return sharedPreferences.getInt(NEXT_RECIPE_ID_KEY, 10000) // Start from 10000 to avoid conflicts
    }
    
    private fun incrementNextRecipeId() {
        val currentId = getNextRecipeId()
        sharedPreferences.edit().putInt(NEXT_RECIPE_ID_KEY, currentId + 1).apply()
    }
    
    // Recipe validation
    fun validateRecipe(recipe: Recipe): RecipeValidationResult {
        return when {
            recipe.name.isBlank() -> RecipeValidationResult.Error("Recipe name cannot be empty")
            recipe.category.isBlank() -> RecipeValidationResult.Error("Category cannot be empty")
            recipe.cuisine.isBlank() -> RecipeValidationResult.Error("Cuisine cannot be empty")
            recipe.caloriesPer100g <= 0 -> RecipeValidationResult.Error("Calories must be greater than 0")
            recipe.proteinPer100g < 0 -> RecipeValidationResult.Error("Protein cannot be negative")
            recipe.carbsPer100g < 0 -> RecipeValidationResult.Error("Carbs cannot be negative")
            recipe.fatPer100g < 0 -> RecipeValidationResult.Error("Fat cannot be negative")
            recipe.fiberPer100g < 0 -> RecipeValidationResult.Error("Fiber cannot be negative")
            recipe.instructions.isBlank() -> RecipeValidationResult.Error("Instructions cannot be empty")
            recipe.ingredients.isBlank() -> RecipeValidationResult.Error("Ingredients cannot be empty")
            else -> RecipeValidationResult.Success
        }
    }
    
    sealed class RecipeValidationResult {
        object Success : RecipeValidationResult()
        data class Error(val message: String) : RecipeValidationResult()
    }
} 