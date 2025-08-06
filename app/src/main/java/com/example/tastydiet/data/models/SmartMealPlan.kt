package com.example.tastydiet.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class SmartMealPlan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // YYYY-MM-DD format
    val memberId: Int,
    val breakfastRecipeId: Int? = null,
    val lunchRecipeId: Int? = null,
    val dinnerRecipeId: Int? = null,
    val snackRecipeId: Int? = null,
    val totalCalories: Float = 0f,
    val totalProtein: Float = 0f,
    val totalCarbs: Float = 0f,
    val totalFat: Float = 0f,
    val totalFiber: Float = 0f,
    val targetCalories: Float = 0f,
    val targetProtein: Float = 0f,
    val targetCarbs: Float = 0f,
    val targetFat: Float = 0f,
    val targetFiber: Float = 0f,
    val isAccepted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

// Data class for meal suggestions with macro breakdown
@Serializable
data class MealSuggestion(
    val recipe: Recipe,
    val mealType: String, // Breakfast, Lunch, Dinner, Snack
    val portionSize: Float, // in grams
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val fiber: Float,
    val ingredientsAvailable: Boolean = true,
    val missingIngredients: List<String> = emptyList()
)

// Data class for daily macro summary
@Serializable
data class DailyMacroSummary(
    val totalCalories: Float,
    val totalProtein: Float,
    val totalCarbs: Float,
    val totalFat: Float,
    val totalFiber: Float,
    val targetCalories: Float,
    val targetProtein: Float,
    val targetCarbs: Float,
    val targetFat: Float,
    val targetFiber: Float
) {
    val caloriesProgress: Float get() = if (targetCalories > 0) (totalCalories / targetCalories) * 100 else 0f
    val proteinProgress: Float get() = if (targetProtein > 0) (totalProtein / targetProtein) * 100 else 0f
    val carbsProgress: Float get() = if (targetCarbs > 0) (totalCarbs / targetCarbs) * 100 else 0f
    val fatProgress: Float get() = if (targetFat > 0) (totalFat / targetFat) * 100 else 0f
    val fiberProgress: Float get() = if (targetFiber > 0) (totalFiber / targetFiber) * 100 else 0f
} 