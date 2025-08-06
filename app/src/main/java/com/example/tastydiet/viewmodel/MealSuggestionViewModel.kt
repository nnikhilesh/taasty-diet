package com.example.tastydiet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tastydiet.AppDatabase
import com.example.tastydiet.data.models.Ingredient
import com.example.tastydiet.data.models.Recipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class MealSuggestionViewModel(application: Application) : AndroidViewModel(application) {
    private val ingredientDao = AppDatabase.getInstance(application).ingredientDao()
    private val recipeDao = AppDatabase.getInstance(application).recipeDao()
    
    private val _ingredients = MutableStateFlow<List<Ingredient>>(emptyList())
    val ingredients: StateFlow<List<Ingredient>> = _ingredients.asStateFlow()
    
    private val _recipes = MutableStateFlow<List<Recipe>>(emptyList())
    val recipes: StateFlow<List<Recipe>> = _recipes.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _filteredRecipes = MutableStateFlow<List<Recipe>>(emptyList())
    val filteredRecipes: StateFlow<List<Recipe>> = _filteredRecipes.asStateFlow()
    
    private val _currentSuggestion = MutableStateFlow<String>("")
    val currentSuggestion: StateFlow<String> = _currentSuggestion.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    init {
        loadIngredients()
        loadRecipes()
    }
    
    fun loadIngredients() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                ingredientDao.getAllIngredients().collect { ingredients ->
                    _ingredients.value = ingredients
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load ingredients: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadRecipes() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                recipeDao.getAll().collect { recipes ->
                    _recipes.value = recipes
                    _filteredRecipes.value = recipes
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load recipes: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun searchRecipes(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            try {
                if (query.isBlank()) {
                    _filteredRecipes.value = _recipes.value
                } else {
                    recipeDao.searchByNameOrIngredient(query).collect { recipes ->
                        _filteredRecipes.value = recipes
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to search recipes: ${e.message}"
            }
        }
    }
    
    fun suggestMeal() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                val availableIngredients = _ingredients.value.filter { it.quantity > 0 }
                
                if (availableIngredients.isEmpty()) {
                    _currentSuggestion.value = "No ingredients available. Please add some ingredients to your inventory first."
                    return@launch
                }
                
                // Try to find recipes that match available ingredients
                val ingredientNames = availableIngredients.map { it.name.lowercase() }
                val matchingRecipes = _recipes.value.filter { recipe ->
                    ingredientNames.any { ingredientName ->
                        recipe.name.lowercase().contains(ingredientName) ||
                        recipe.instructions.lowercase().contains(ingredientName)
                    }
                }
                
                if (matchingRecipes.isNotEmpty()) {
                    val selectedRecipe = matchingRecipes.random()
                    _currentSuggestion.value = "Try making: ${selectedRecipe.name}\n\nCalories: ${selectedRecipe.caloriesPer100g} kcal/100g\nProtein: ${selectedRecipe.proteinPer100g}g\nCarbs: ${selectedRecipe.carbsPer100g}g\nFat: ${selectedRecipe.fatPer100g}g"
                } else {
                    // Fallback to basic meal suggestion
                    val topIngredients = availableIngredients.take(3)
                    val mealName = generateMealName(topIngredients)
                    _currentSuggestion.value = mealName
                }
                
                _errorMessage.value = "Meal suggestion generated successfully!"
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to generate meal suggestion: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun suggestMealByCategory(category: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                val categoryIngredients = _ingredients.value.filter { 
                    it.category.equals(category, ignoreCase = true) && it.quantity > 0 
                }
                
                if (categoryIngredients.isEmpty()) {
                    _currentSuggestion.value = "No $category ingredients available."
                    return@launch
                }
                
                // Try to find recipes for this category
                val categoryRecipes = _recipes.value.filter { recipe ->
                    recipe.category.equals(category, ignoreCase = true) ||
                    recipe.cuisine.equals(category, ignoreCase = true)
                }
                
                if (categoryRecipes.isNotEmpty()) {
                    val selectedRecipe = categoryRecipes.random()
                    _currentSuggestion.value = "Perfect $category recipe: ${selectedRecipe.name}\n\nCalories: ${selectedRecipe.caloriesPer100g} kcal/100g\nProtein: ${selectedRecipe.proteinPer100g}g\nCarbs: ${selectedRecipe.carbsPer100g}g\nFat: ${selectedRecipe.fatPer100g}g"
                } else {
                    // Fallback to category-based meal name
                    val topIngredients = categoryIngredients.take(3)
                    val mealName = generateCategoryMealName(category, topIngredients)
                    _currentSuggestion.value = mealName
                }
                
                _errorMessage.value = "$category meal suggestion generated!"
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to generate $category meal suggestion: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun suggestMealByRecipe(recipe: Recipe) {
        _currentSuggestion.value = "Selected Recipe: ${recipe.name}\n\nCalories: ${recipe.caloriesPer100g} kcal/100g\nProtein: ${recipe.proteinPer100g}g\nCarbs: ${recipe.carbsPer100g}g\nFat: ${recipe.fatPer100g}g\nFiber: ${recipe.fiberPer100g}g\n\nInstructions: ${recipe.instructions}"
        _errorMessage.value = "Recipe selected successfully!"
    }
    
    private fun generateMealName(ingredients: List<Ingredient>): String {
        if (ingredients.isEmpty()) return "No ingredients available"
        
        val mealTemplates = listOf(
            "Delicious %s with %s and %s",
            "Healthy %s %s with %s",
            "Tasty %s and %s %s",
            "Fresh %s with %s %s",
            "Simple %s %s %s"
        )
        
        val template = mealTemplates.random()
        val ingredientNames = ingredients.map { it.name.lowercase().replaceFirstChar { it.uppercase() } }
        
        return when (ingredientNames.size) {
            1 -> "Simple ${ingredientNames[0]} dish"
            2 -> "Delicious ${ingredientNames[0]} with ${ingredientNames[1]}"
            3 -> template.format(ingredientNames[0], ingredientNames[1], ingredientNames[2])
            else -> "Mixed dish with ${ingredientNames.take(3).joinToString(", ")}"
        }
    }
    
    private fun generateCategoryMealName(category: String, ingredients: List<Ingredient>): String {
        val categoryMeals = mapOf(
            "Vegetables" to listOf("Fresh Vegetable Stir Fry", "Roasted Vegetable Medley", "Vegetable Soup"),
            "Proteins" to listOf("Protein Bowl", "Grilled Protein Plate", "Protein Stir Fry"),
            "Grains" to listOf("Grain Bowl", "Grain Salad", "Grain Pilaf"),
            "Fats" to listOf("Healthy Fat Dish", "Oil-based Dressing", "Fat-rich Sauce")
        )
        
        val baseMeal = categoryMeals[category]?.random() ?: "$category Dish"
        val ingredientNames = ingredients.map { it.name.lowercase().replaceFirstChar { it.uppercase() } }
        
        return "$baseMeal with ${ingredientNames.joinToString(", ")}"
    }
    
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
    
    fun clearSuggestion() {
        _currentSuggestion.value = ""
    }
    
    fun clearSearch() {
        _searchQuery.value = ""
        _filteredRecipes.value = _recipes.value
    }
    
    fun getAvailableCategories(): List<String> {
        return _ingredients.value
            .filter { it.quantity > 0 }
            .map { it.category }
            .distinct()
            .sorted()
    }
    
    fun getIngredientCount(): Int {
        return _ingredients.value.size
    }
    
    fun getAvailableIngredientCount(): Int {
        return _ingredients.value.count { it.quantity > 0 }
    }
    
    fun getRecipeCategories(): List<String> {
        return _recipes.value
            .map { it.category }
            .distinct()
            .sorted()
    }
    
    fun getRecipeCuisines(): List<String> {
        return _recipes.value
            .map { it.cuisine }
            .distinct()
            .sorted()
    }
} 