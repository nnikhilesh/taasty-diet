package com.example.tastydiet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tastydiet.AppDatabase
import com.example.tastydiet.data.RecipeDatabase
import com.example.tastydiet.data.RecipeManager
import com.example.tastydiet.data.models.Recipe
import com.example.tastydiet.data.models.InventoryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecipeViewModel(application: Application) : AndroidViewModel(application) {
    private val recipeDao = AppDatabase.getInstance(application).recipeDao()
    private val ingredientDao = AppDatabase.getInstance(application).ingredientDao()
    private val recipeManager = RecipeManager(application)
    
    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes.asStateFlow()
    
    private val _inventory = MutableStateFlow<List<InventoryItem>>(emptyList())
    val inventory: StateFlow<List<InventoryItem>> = _inventory.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    init {
        loadRecipes()
        loadInventory()
    }
    
    fun loadRecipes() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Load all recipes from JSON files (1,000 + 9 offline recipes)
                val allRecipes = RecipeDatabase.loadAllRecipes(getApplication())
                
                // Load custom recipes from RecipeManager
                val customRecipes = recipeManager.getAllCustomRecipes()
                
                // Combine both lists
                val combinedRecipes = allRecipes + customRecipes
                _recipes.value = combinedRecipes
                
                _errorMessage.value = "Successfully loaded ${combinedRecipes.size} recipes (${allRecipes.size} default + ${customRecipes.size} custom)!"
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load recipes: ${e.message}"
                // Fallback to database recipes
                recipeDao.getAll().collect { recipes ->
                    _recipes.value = recipes
                }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadInventory() {
        viewModelScope.launch {
            try {
                ingredientDao.getAllIngredients().collect { ingredients ->
                    // Convert ingredients to inventory items
                    val inventoryItems = ingredients.map { ingredient ->
                        com.example.tastydiet.data.models.InventoryItem(
                            id = ingredient.id,
                            name = ingredient.name,
                            quantity = ingredient.quantity,
                            unit = ingredient.unit,
                            category = ingredient.category
                        )
                    }
                    _inventory.value = inventoryItems
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load inventory: ${e.message}"
            }
        }
    }
    
    fun getRecipesByCategory(category: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                recipeDao.getByCategory(category).collect { recipes ->
                    _recipes.value = recipes
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load recipes by category: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun getRecipesByCuisine(cuisine: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                recipeDao.getByCuisine(cuisine).collect { recipes ->
                    _recipes.value = recipes
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load recipes by cuisine: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun searchRecipes(query: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                recipeDao.searchByNameOrIngredient(query).collect { recipes ->
                    _recipes.value = recipes
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to search recipes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun reloadAllRecipes() {
        loadRecipes()
    }
    
    fun refreshRecipes() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Clear existing recipes and reload all from JSON files
                _recipes.value = emptyList()
                val allRecipes = RecipeDatabase.loadAllRecipes(getApplication())
                
                // Load custom recipes from RecipeManager
                val customRecipes = recipeManager.getAllCustomRecipes()
                
                // Combine both lists
                val combinedRecipes = allRecipes + customRecipes
                _recipes.value = combinedRecipes
                
                _errorMessage.value = "Successfully reloaded ${combinedRecipes.size} recipes (${allRecipes.size} default + ${customRecipes.size} custom)!"
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to reload recipes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
    
    fun getRecipeById(id: Int): Recipe? {
        return _recipes.value.find { it.id == id }
    }
    
    fun getAvailableRecipes(): List<Recipe> {
        val inventoryNames = _inventory.value.map { it.name.lowercase() }
        return _recipes.value.filter { recipe ->
            // Simple check - in a real app, you'd check actual ingredients
            recipe.name.lowercase().contains("salad") ||
            recipe.name.lowercase().contains("oat") ||
            recipe.name.lowercase().contains("paneer") ||
            recipe.name.lowercase().contains("rice") ||
            recipe.name.lowercase().contains("bread") ||
            recipe.name.lowercase().contains("dal") ||
            recipe.name.lowercase().contains("vegetable")
        }
    }
    
    fun getHighProteinRecipes(): List<Recipe> {
        return _recipes.value.filter { it.proteinPer100g > 15 }
    }
    
    fun getLowCalorieRecipes(): List<Recipe> {
        return _recipes.value.filter { it.caloriesPer100g < 200 }
    }
    
    fun getHighFiberRecipes(): List<Recipe> {
        return _recipes.value.filter { it.fiberPer100g > 5 }
    }
} 