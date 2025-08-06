package com.example.tastydiet.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class Profile(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val age: Int,
    val weight: Float, // in kg
    val height: Float, // in cm
    val gender: String, // "Male", "Female", "Other"
    val goal: String = "Maintenance", // Updated goal options
    val bmi: Float = 0f,
    val bmiCategory: String = "Unknown",
    // Macro fields
    val targetCalories: Float = 0f,
    val targetProtein: Float = 0f, // in grams
    val targetCarbs: Float = 0f, // in grams
    val targetFat: Float = 0f, // in grams
    val targetFiber: Float = 25f, // in grams, default 25g
    val activityLevel: Float = 1.4f, // Default moderate activity
    val goalDurationInWeeks: Int = 12, // Default 12 weeks (3 months)
    
    // Meal-specific macro distribution (percentages of daily totals)
    val breakfastCaloriesPercent: Float = 25f, // 25% of daily calories
    val breakfastProteinPercent: Float = 25f, // 25% of daily protein
    val breakfastCarbsPercent: Float = 30f, // 30% of daily carbs
    val breakfastFatPercent: Float = 25f, // 25% of daily fat
    
    val lunchCaloriesPercent: Float = 35f, // 35% of daily calories
    val lunchProteinPercent: Float = 35f, // 35% of daily protein
    val lunchCarbsPercent: Float = 35f, // 35% of daily carbs
    val lunchFatPercent: Float = 35f, // 35% of daily fat
    
    val snackCaloriesPercent: Float = 15f, // 15% of daily calories
    val snackProteinPercent: Float = 15f, // 15% of daily protein
    val snackCarbsPercent: Float = 15f, // 15% of daily carbs
    val snackFatPercent: Float = 15f, // 15% of daily fat
    
    val dinnerCaloriesPercent: Float = 25f, // 25% of daily calories
    val dinnerProteinPercent: Float = 25f, // 25% of daily protein
    val dinnerCarbsPercent: Float = 20f, // 20% of daily carbs
    val dinnerFatPercent: Float = 25f // 25% of daily fat
) {
    fun calculateBMI(): Float {
        val heightInMeters = height / 100
        return if (heightInMeters > 0) weight / (heightInMeters * heightInMeters) else 0f
    }
    
    fun getBMICategory(): String {
        val calculatedBMI = calculateBMI()
        return when {
            calculatedBMI < 18.5 -> "Underweight"
            calculatedBMI < 25 -> "Normal weight"
            calculatedBMI < 30 -> "Overweight"
            else -> "Obese"
        }
    }
    
    // Meal-specific macro calculations
    fun getBreakfastCalories(): Float = targetCalories * breakfastCaloriesPercent / 100f
    fun getBreakfastProtein(): Float = targetProtein * breakfastProteinPercent / 100f
    fun getBreakfastCarbs(): Float = targetCarbs * breakfastCarbsPercent / 100f
    fun getBreakfastFat(): Float = targetFat * breakfastFatPercent / 100f
    
    fun getLunchCalories(): Float = targetCalories * lunchCaloriesPercent / 100f
    fun getLunchProtein(): Float = targetProtein * lunchProteinPercent / 100f
    fun getLunchCarbs(): Float = targetCarbs * lunchCarbsPercent / 100f
    fun getLunchFat(): Float = targetFat * lunchFatPercent / 100f
    
    fun getSnackCalories(): Float = targetCalories * snackCaloriesPercent / 100f
    fun getSnackProtein(): Float = targetProtein * snackProteinPercent / 100f
    fun getSnackCarbs(): Float = targetCarbs * snackCarbsPercent / 100f
    fun getSnackFat(): Float = targetFat * snackFatPercent / 100f
    
    fun getDinnerCalories(): Float = targetCalories * dinnerCaloriesPercent / 100f
    fun getDinnerProtein(): Float = targetProtein * dinnerProteinPercent / 100f
    fun getDinnerCarbs(): Float = targetCarbs * dinnerCarbsPercent / 100f
    fun getDinnerFat(): Float = targetFat * dinnerFatPercent / 100f
    
    // Get meal-specific macros based on meal type
    fun getMealMacros(mealType: String): MealMacros {
        return when (mealType.lowercase()) {
            "breakfast", "morning", "tiffin" -> MealMacros(
                calories = getBreakfastCalories(),
                protein = getBreakfastProtein(),
                carbs = getBreakfastCarbs(),
                fat = getBreakfastFat()
            )
            "lunch" -> MealMacros(
                calories = getLunchCalories(),
                protein = getLunchProtein(),
                carbs = getLunchCarbs(),
                fat = getLunchFat()
            )
            "snack", "evening" -> MealMacros(
                calories = getSnackCalories(),
                protein = getSnackProtein(),
                carbs = getSnackCarbs(),
                fat = getSnackFat()
            )
            "dinner", "night" -> MealMacros(
                calories = getDinnerCalories(),
                protein = getDinnerProtein(),
                carbs = getDinnerCarbs(),
                fat = getDinnerFat()
            )
            else -> MealMacros(
                calories = getLunchCalories(), // Default to lunch
                protein = getLunchProtein(),
                carbs = getLunchCarbs(),
                fat = getLunchFat()
            )
        }
    }
    
    fun getBMICategoryWithGoal(): String {
        val calculatedBMI = calculateBMI()
        val baseCategory = getBMICategory()
        
        return when (goal) {
            "Weight Loss" -> {
                when {
                    calculatedBMI < 18.5 -> "$baseCategory - Consider gaining weight"
                    calculatedBMI < 25 -> "$baseCategory - Good for weight loss"
                    calculatedBMI < 30 -> "$baseCategory - Focus on weight loss"
                    else -> "$baseCategory - Prioritize weight loss"
                }
            }
            "Muscle Gain" -> {
                when {
                    calculatedBMI < 18.5 -> "$baseCategory - Focus on muscle gain"
                    calculatedBMI < 25 -> "$baseCategory - Good for muscle gain"
                    calculatedBMI < 30 -> "$baseCategory - Consider weight loss first"
                    else -> "$baseCategory - Focus on weight loss before muscle gain"
                }
            }
            "Fat Loss (High Protein)" -> {
                when {
                    calculatedBMI < 18.5 -> "$baseCategory - Consider gaining weight"
                    calculatedBMI < 25 -> "$baseCategory - Good for fat loss"
                    calculatedBMI < 30 -> "$baseCategory - Focus on fat loss"
                    else -> "$baseCategory - Prioritize fat loss"
                }
            }
            "Endurance Training" -> {
                when {
                    calculatedBMI < 18.5 -> "$baseCategory - Consider gaining weight"
                    calculatedBMI < 25 -> "$baseCategory - Ideal for endurance"
                    calculatedBMI < 30 -> "$baseCategory - Good for endurance"
                    else -> "$baseCategory - Consider weight loss for better performance"
                }
            }
            "Keto Diet" -> {
                when {
                    calculatedBMI < 18.5 -> "$baseCategory - Monitor weight on keto"
                    calculatedBMI < 25 -> "$baseCategory - Good for keto"
                    calculatedBMI < 30 -> "$baseCategory - Keto may help with weight loss"
                    else -> "$baseCategory - Keto may help with weight loss"
                }
            }
            "Diabetes Management" -> {
                when {
                    calculatedBMI < 18.5 -> "$baseCategory - Focus on balanced nutrition"
                    calculatedBMI < 25 -> "$baseCategory - Good for diabetes management"
                    calculatedBMI < 30 -> "$baseCategory - Weight loss may improve diabetes"
                    else -> "$baseCategory - Weight loss may improve diabetes"
                }
            }
            "Custom Plan" -> {
                "$baseCategory - Custom nutrition plan"
            }
            "Maintenance" -> {
                when {
                    calculatedBMI < 18.5 -> "$baseCategory - Consider gaining weight"
                    calculatedBMI < 25 -> "$baseCategory - Ideal for maintenance"
                    calculatedBMI < 30 -> "$baseCategory - Consider weight loss"
                    else -> "$baseCategory - Consider weight loss"
                }
            }
            else -> baseCategory
        }
    }
    
    fun updateBMI() {
        val calculatedBMI = calculateBMI()
        val category = getBMICategory()
        // Note: In a real implementation, you'd need to create a new instance
        // since data classes are immutable
    }
}

// Data class for meal-specific macros
data class MealMacros(
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fat: Float
) 