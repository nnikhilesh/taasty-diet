package com.example.tastydiet.data

import android.content.Context
import com.example.tastydiet.data.models.Recipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

object RecipeDatabase {
    
    suspend fun loadAllRecipes(context: Context): List<Recipe> {
        return withContext(Dispatchers.IO) {
            try {
                val allRecipes = mutableListOf<Recipe>()
                
                // Load 9 detailed offline recipes first (better names)
                val offlineRecipes = loadOfflineRecipes(context)
                allRecipes.addAll(offlineRecipes)
                
                // Load 10 authentic Andhra Pradesh recipes
                val andhraRecipes = loadAndhraRecipes(context)
                allRecipes.addAll(andhraRecipes)
                
                // Load comprehensive authentic Indian recipes (no generic recipes)
                val authenticRecipes = loadAuthenticIndianRecipes(context)
                allRecipes.addAll(authenticRecipes)
                
                // Load custom recipes added by user
                val recipeManager = RecipeManager(context)
                val customRecipes = recipeManager.getAllCustomRecipes()
                allRecipes.addAll(customRecipes)
                
                allRecipes
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }
    
    // Removed generic recipes loading - now only authentic recipes
    
    private suspend fun loadOfflineRecipes(context: Context): List<Recipe> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonString = context.assets.open("full_offline_recipes.json").bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(jsonString)
                
                val recipes = mutableListOf<Recipe>()
                
                for (i in 0 until jsonArray.length()) {
                    val recipeObj = jsonArray.getJSONObject(i)
                    
                    val recipe = Recipe(
                        id = 1000 + i + 1, // Start from 1001 to avoid conflicts
                        name = recipeObj.getString("name"),
                        category = recipeObj.getString("mealType"),
                        cuisine = recipeObj.getString("cuisine"),
                        caloriesPer100g = recipeObj.getJSONObject("macros").getDouble("calories").toInt(),
                        proteinPer100g = recipeObj.getJSONObject("macros").getDouble("protein").toFloat(),
                        carbsPer100g = recipeObj.getJSONObject("macros").getDouble("carbs").toFloat(),
                        fatPer100g = recipeObj.getJSONObject("macros").getDouble("fat").toFloat(),
                        fiberPer100g = recipeObj.getJSONObject("macros").getDouble("fiber").toFloat(),
                        instructions = recipeObj.getString("instructions"),
                        liked = null,
                        youtubeUrl = null
                    )
                    
                    recipes.add(recipe)
                }
                
                recipes
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }
    
    private suspend fun loadAndhraRecipes(context: Context): List<Recipe> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonString = context.assets.open("andhra_recipes.json").bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(jsonString)
                
                val recipes = mutableListOf<Recipe>()
                
                for (i in 0 until jsonArray.length()) {
                    val recipeObj = jsonArray.getJSONObject(i)
                    
                    val recipe = Recipe(
                        id = 2000 + i + 1, // Start from 2001 to avoid conflicts
                        name = recipeObj.getString("name"),
                        category = recipeObj.getString("mealType"),
                        cuisine = recipeObj.getString("cuisine"),
                        caloriesPer100g = recipeObj.getJSONObject("macros").getDouble("calories").toInt(),
                        proteinPer100g = recipeObj.getJSONObject("macros").getDouble("protein").toFloat(),
                        carbsPer100g = recipeObj.getJSONObject("macros").getDouble("carbs").toFloat(),
                        fatPer100g = recipeObj.getJSONObject("macros").getDouble("fat").toFloat(),
                        fiberPer100g = recipeObj.getJSONObject("macros").getDouble("fiber").toFloat(),
                        instructions = recipeObj.getString("instructions"),
                        liked = null,
                        youtubeUrl = null
                    )
                    
                    recipes.add(recipe)
                }
                
                recipes
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }
    
    private suspend fun loadAuthenticIndianRecipes(context: Context): List<Recipe> {
        return withContext(Dispatchers.IO) {
            try {
                val jsonString = context.assets.open("authentic_andhra_telangana_north_recipes.json").bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(jsonString)
                
                val recipes = mutableListOf<Recipe>()
                
                for (i in 0 until jsonArray.length()) {
                    val recipeObj = jsonArray.getJSONObject(i)
                    
                    val recipe = Recipe(
                        id = 3000 + i + 1, // Start from 3001 to avoid conflicts
                        name = recipeObj.getString("name"),
                        category = recipeObj.getString("mealType"),
                        cuisine = recipeObj.getString("cuisine"),
                        caloriesPer100g = recipeObj.getJSONObject("macros").getDouble("calories").toInt(),
                        proteinPer100g = recipeObj.getJSONObject("macros").getDouble("protein").toFloat(),
                        carbsPer100g = recipeObj.getJSONObject("macros").getDouble("carbs").toFloat(),
                        fatPer100g = recipeObj.getJSONObject("macros").getDouble("fat").toFloat(),
                        fiberPer100g = recipeObj.getJSONObject("macros").getDouble("fiber").toFloat(),
                        instructions = recipeObj.getString("instructions"),
                        liked = null,
                        youtubeUrl = null
                    )
                    
                    recipes.add(recipe)
                }
                
                recipes
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }
    

} 