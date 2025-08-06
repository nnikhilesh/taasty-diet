package com.example.tastydiet

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tastydiet.AppDatabase
import com.example.tastydiet.data.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

class MealPlannerViewModel(application: Application) : AndroidViewModel(application) {
    private val familyMemberDao = AppDatabase.getInstance(application).familyMemberDao()
    private val recipeDao = AppDatabase.getInstance(application).recipeDao()
    private val inventoryDao = AppDatabase.getInstance(application).inventoryDao()
    private val shoppingListDao = AppDatabase.getInstance(application).shoppingListDao()
    
    private val _familyMembers = MutableStateFlow<List<FamilyMember>>(emptyList())
    val familyMembers: StateFlow<List<FamilyMember>> = _familyMembers.asStateFlow()
    
    private val _weeklyMealPlan = MutableStateFlow<Map<String, List<Recipe>>>(emptyMap())
    val weeklyMealPlan: StateFlow<Map<String, List<Recipe>>> = _weeklyMealPlan.asStateFlow()
    
    private val _shoppingList = MutableStateFlow<List<ShoppingListItem>>(emptyList())
    val shoppingList: StateFlow<List<ShoppingListItem>> = _shoppingList.asStateFlow()

    init {
        loadFamilyMembers()
        generateWeeklyMealPlan()
        generateShoppingList()
    }

    fun loadFamilyMembers() {
        viewModelScope.launch {
            try {
                val members = familyMemberDao.getAll()
                _familyMembers.value = members
            } catch (e: Exception) {
                // Handle database errors gracefully
                _familyMembers.value = emptyList()
            }
        }
    }

    fun generateWeeklyMealPlan() {
        viewModelScope.launch {
            try {
                val recipes = recipeDao.getAll().first() // Convert Flow to List
                val totalMacros = mutableMapOf<String, Double>()
                val weekPlan = mutableMapOf<String, List<Recipe>>()
                
                val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
                days.forEach { day ->
                    // Simple meal planning logic - can be enhanced
                    val dayRecipes = recipes.shuffled().take(3) // 3 recipes per day
                    weekPlan[day] = dayRecipes
                }
                
                _weeklyMealPlan.value = weekPlan
            } catch (e: Exception) {
                // Handle database errors gracefully
                _weeklyMealPlan.value = emptyMap()
            }
        }
    }

    fun generateShoppingList() {
        viewModelScope.launch {
            try {
                val mealPlans = _weeklyMealPlan.value.values.flatten()
                val shoppingItems = mutableListOf<ShoppingListItem>()
                
                // Generate shopping list based on meal plans
                // This is a simplified version - you can enhance it
                mealPlans.forEach { recipe ->
                    // Add ingredients to shopping list
                    // For now, just add the recipe name as a placeholder
                    shoppingItems.add(
                        ShoppingListItem(
                            id = 0,
                            name = recipe.name,
                            quantity = 1f,
                            unit = "serving",
                            category = "Recipe",
                            isChecked = false
                        )
                    )
                }
                
                _shoppingList.value = shoppingItems
            } catch (e: Exception) {
                // Handle database errors gracefully
                _shoppingList.value = emptyList()
            }
        }
    }

    suspend fun suggestMealForDay(day: String, preferences: Map<String, String>): List<Recipe> {
        return try {
            val allRecipes = recipeDao.getAll().first() // Convert Flow to List
            val filteredRecipes = allRecipes.filter { recipe ->
                // Apply preference filters
                preferences["cuisine"]?.let { cuisine ->
                    if (cuisine.isNotEmpty() && recipe.cuisine != cuisine) return@filter false
                }
                preferences["category"]?.let { category ->
                    if (category.isNotEmpty() && recipe.category != category) return@filter false
                }
                true
            }
            
            if (filteredRecipes.isEmpty()) allRecipes else filteredRecipes
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun calculateNutritionalNeeds(member: FamilyMember): Map<String, Float> {
        // Basic BMR calculation using Mifflin-St Jeor Equation
        val bmr = when (member.gender.lowercase()) {
            "male" -> (10 * member.weight) + (6.25 * member.height) - (5 * member.age) + 5
            "female" -> (10 * member.weight) + (6.25 * member.height) - (5 * member.age) - 161
            else -> (10 * member.weight) + (6.25 * member.height) - (5 * member.age) - 78
        }
        
        // Activity multiplier (assuming moderate activity)
        val tdee = bmr * 1.55f
        
        return mapOf(
            "calories" to tdee.toFloat(),
            "protein" to (tdee * 0.25f / 4).toFloat(), // 25% protein, 4 calories per gram
            "carbs" to (tdee * 0.45f / 4).toFloat(),   // 45% carbs, 4 calories per gram
            "fat" to (tdee * 0.30f / 9).toFloat()      // 30% fat, 9 calories per gram
        )
    }

    fun exportMealPlanToPdf(): String {
        val stringBuilder = StringBuilder()
        stringBuilder.appendLine("Weekly Meal Plan")
        stringBuilder.appendLine("================")
        
        _weeklyMealPlan.value.forEach { (day, meals) ->
            stringBuilder.appendLine("\n$day:")
            meals.forEach { recipe ->
                stringBuilder.appendLine("  - ${recipe.name}")
            }
        }
        
        return stringBuilder.toString()
    }

    fun getShoppingListSummary(): String {
        val items = _shoppingList.value.take(5).map { it.name }
        return "Shopping List (${items.size} items):\n" + 
               items.joinToString("\n") { itemName -> 
                   val item = _shoppingList.value.find { it.name == itemName }
                   "- $itemName (${item?.quantity} ${item?.unit})" 
               }
    }
}