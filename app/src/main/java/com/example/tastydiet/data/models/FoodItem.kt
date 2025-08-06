package com.example.tastydiet.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_items")
data class FoodItem(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val category: String,
    val unit: String, // g, ml, piece, cup, tbsp, etc.
    val caloriesPerUnit: Float, // calories per unit
    val proteinPerUnit: Float, // grams of protein per unit
    val carbsPerUnit: Float, // grams of carbs per unit
    val fatPerUnit: Float, // grams of fat per unit
    val fiberPerUnit: Float = 0f, // grams of fiber per unit
    val sugarPerUnit: Float = 0f, // grams of sugar per unit
    val sodiumPerUnit: Float = 0f, // mg of sodium per unit
    val isVeg: Boolean = true,
    val isGlutenFree: Boolean = true,
    val isDairyFree: Boolean = true,
    val description: String = "",
    val imageUrl: String = "",
    val isActive: Boolean = true
) {
    fun calculateMacros(quantity: Float): MacroResult {
        return MacroResult(
            calories = caloriesPerUnit * quantity,
            protein = proteinPerUnit * quantity,
            carbs = carbsPerUnit * quantity,
            fat = fatPerUnit * quantity,
            fiber = fiberPerUnit * quantity,
            sugar = sugarPerUnit * quantity,
            sodium = sodiumPerUnit * quantity
        )
    }
}

data class MacroResult(
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val fiber: Float = 0f,
    val sugar: Float = 0f,
    val sodium: Float = 0f
) {
    fun getTotalCalories(): Float = calories
    
    fun getProteinPercentage(): Float = (protein * 4) / calories * 100 // 4 calories per gram of protein
    
    fun getCarbsPercentage(): Float = (carbs * 4) / calories * 100 // 4 calories per gram of carbs
    
    fun getFatPercentage(): Float = (fat * 9) / calories * 100 // 9 calories per gram of fat
    
    fun getFormattedCalories(): String = String.format("%.0f", calories)
    fun getFormattedProtein(): String = String.format("%.1f", protein)
    fun getFormattedCarbs(): String = String.format("%.1f", carbs)
    fun getFormattedFat(): String = String.format("%.1f", fat)
    fun getFormattedFiber(): String = String.format("%.1f", fiber)
    fun getFormattedSugar(): String = String.format("%.1f", sugar)
    fun getFormattedSodium(): String = String.format("%.0f", sodium)
}

data class MacroRecommendations(
    val dailyCalories: Float,
    val dailyProtein: Float,
    val dailyCarbs: Float,
    val dailyFat: Float,
    val bmi: Float,
    val bmiCategory: String
) {
    fun getFormattedDailyCalories(): String = String.format("%.0f", dailyCalories)
    fun getFormattedDailyProtein(): String = String.format("%.1f", dailyProtein)
    fun getFormattedDailyCarbs(): String = String.format("%.1f", dailyCarbs)
    fun getFormattedDailyFat(): String = String.format("%.1f", dailyFat)
}

data class MacroProgress(
    val caloriesProgress: Float,
    val proteinProgress: Float,
    val carbsProgress: Float,
    val fatProgress: Float
) {
    fun getFormattedCaloriesProgress(): String = String.format("%.1f", caloriesProgress)
    fun getFormattedProteinProgress(): String = String.format("%.1f", proteinProgress)
    fun getFormattedCarbsProgress(): String = String.format("%.1f", carbsProgress)
    fun getFormattedFatProgress(): String = String.format("%.1f", fatProgress)
} 