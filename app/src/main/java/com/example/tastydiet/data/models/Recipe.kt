package com.example.tastydiet.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class Recipe(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val cuisine: String, // South Indian, North Indian, Continental
    val category: String, // Breakfast, Lunch, Dinner, Snack
    val mealType: String = "Lunch", // Breakfast, Lunch, Snack, Dinner
    val caloriesPer100g: Int,
    val proteinPer100g: Float,
    val carbsPer100g: Float,
    val fatPer100g: Float,
    val fiberPer100g: Float,
    val instructions: String,
    val ingredients: String = "", // Ingredients list
    val liked: Boolean? = null,
    val youtubeUrl: String? = null, // Optional YouTube help video
    val servingSize: Int = 4, // Default serving size
    val isMainCourse: Boolean = true, // Whether this is a main course or side dish
    val isVegetarian: Boolean = true, // Vegetarian or non-vegetarian
    val cookingTime: Int = 30, // Cooking time in minutes
    val difficulty: String = "Medium" // Easy, Medium, Hard
) {
    val isVeg: Boolean
        get() = isVegetarian // Use the new field
    
    // Get meal type for filtering (renamed to avoid Room conflict)
    fun getRecipeMealType(): String {
        return when {
            mealType.isNotBlank() -> mealType
            category.contains("Breakfast", ignoreCase = true) -> "Breakfast"
            category.contains("Lunch", ignoreCase = true) -> "Lunch"
            category.contains("Dinner", ignoreCase = true) -> "Dinner"
            category.contains("Snack", ignoreCase = true) -> "Snack"
            else -> "Lunch" // Default
        }
    }
    
    // Check if recipe is suitable for a specific meal
    fun isSuitableForMeal(mealType: String): Boolean {
        val recipeMealType = getRecipeMealType()
        return when (mealType.lowercase()) {
            "breakfast", "morning", "tiffin" -> recipeMealType.equals("Breakfast", ignoreCase = true)
            "lunch" -> recipeMealType.equals("Lunch", ignoreCase = true)
            "snack", "evening" -> recipeMealType.equals("Snack", ignoreCase = true)
            "dinner", "night" -> recipeMealType.equals("Dinner", ignoreCase = true)
            else -> true // Show all for unknown meal types
        }
    }
    
    // Check if recipe matches dietary preference
    fun matchesDietaryPreference(preference: String): Boolean {
        return when (preference.lowercase()) {
            "vegetarian" -> isVegetarian
            "non_vegetarian" -> !isVegetarian
            "mixed" -> true
            else -> true
        }
    }
}

// Utility function to calculate macros from a Recipe
fun Recipe.calculateMacros(): Macros = Macros(
    calories = caloriesPer100g.toDouble(),
    protein = proteinPer100g.toDouble(),
    carbs = carbsPer100g.toDouble(),
    fat = fatPer100g.toDouble(),
    fiber = fiberPer100g.toDouble()
) 