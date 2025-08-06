package com.example.tastydiet.util

import com.example.tastydiet.data.models.Profile

object MacroCalculator {
    
    data class MacroTargets(
        val calories: Float,
        val protein: Float, // in grams
        val carbs: Float,   // in grams
        val fat: Float      // in grams
    )
    
    /**
     * Calculate BMR using Mifflin-St Jeor formula
     * BMR = 10×weight + 6.25×height - 5×age + genderFactor
     */
    fun calculateBMR(profile: Profile): Float {
        val genderFactor = when (profile.gender.lowercase()) {
            "male" -> 5f
            "female" -> -161f
            else -> -78f // Average for "Other"
        }
        
        return 10f * profile.weight + 6.25f * profile.height - 5f * profile.age + genderFactor
    }
    
    /**
     * Calculate Total Daily Energy Expenditure (TDEE)
     */
    fun calculateTDEE(profile: Profile): Float {
        val bmr = calculateBMR(profile)
        return bmr * profile.activityLevel
    }
    
    /**
     * Calculate macro targets based on goal and duration
     */
    fun calculateMacroTargets(profile: Profile): MacroTargets {
        val tdee = calculateTDEE(profile)
        val goalDurationWeeks = profile.goalDurationInWeeks
        
        return when (profile.goal) {
            "Weight Loss" -> {
                // Adjust calorie deficit based on goal duration
                val calorieMultiplier = when {
                    goalDurationWeeks <= 4 -> 0.75f // -25% for 1 month or less
                    goalDurationWeeks <= 8 -> 0.8f  // -20% for 2 months
                    goalDurationWeeks <= 12 -> 0.85f // -15% for 3 months
                    goalDurationWeeks <= 16 -> 0.9f  // -10% for 4 months
                    else -> 0.95f // -5% for longer periods
                }
                
                val calories = tdee * calorieMultiplier
                val protein = profile.weight * 2f // 2g/kg
                val fat = calories * 0.25f / 9f // 25% of calories from fat
                val carbs = (calories - (protein * 4f) - (fat * 9f)) / 4f // Remaining calories from carbs
                
                MacroTargets(calories, protein, carbs, fat)
            }
            
            "Muscle Gain" -> {
                // Adjust calorie surplus based on goal duration
                val calorieMultiplier = when {
                    goalDurationWeeks <= 4 -> 1.25f // +25% for aggressive 1-month gain
                    goalDurationWeeks <= 8 -> 1.2f  // +20% for 2 months
                    goalDurationWeeks <= 12 -> 1.15f // +15% for 3 months
                    goalDurationWeeks <= 16 -> 1.1f  // +10% for 4 months
                    else -> 1.05f // +5% for longer periods
                }
                
                val calories = tdee * calorieMultiplier
                val protein = profile.weight * 2.2f // 2.2g/kg
                val fat = calories * 0.25f / 9f // 25% of calories from fat
                val carbs = (calories - (protein * 4f) - (fat * 9f)) / 4f // Remaining calories from carbs
                
                MacroTargets(calories, protein, carbs, fat)
            }
            
            "Fat Loss (High Protein)" -> {
                // Adjust calorie deficit based on goal duration
                val calorieMultiplier = when {
                    goalDurationWeeks <= 4 -> 0.8f // -20% for aggressive 1-month fat loss
                    goalDurationWeeks <= 8 -> 0.85f // -15% for 2 months
                    goalDurationWeeks <= 12 -> 0.9f // -10% for 3 months
                    goalDurationWeeks <= 16 -> 0.95f // -5% for 4 months
                    else -> 0.98f // -2% for longer periods
                }
                
                val calories = tdee * calorieMultiplier
                val protein = profile.weight * 2.5f // 2.5g/kg
                val fat = calories * 0.3f / 9f // 30% of calories from fat
                val carbs = (calories - (protein * 4f) - (fat * 9f)) / 4f // Lower carb
                
                MacroTargets(calories, protein, carbs, fat)
            }
            
            "Endurance Training" -> {
                val calories = tdee * 1.1f // +10% calories for training
                val protein = profile.weight * 1.6f // 1.6g/kg
                val carbs = calories * 0.6f / 4f // 60% of calories from carbs
                val fat = (calories - (protein * 4f) - (carbs * 4f)) / 9f // Remaining calories from fat
                
                MacroTargets(calories, protein, carbs, fat)
            }
            
            "Keto Diet" -> {
                val calories = tdee * 0.9f // -10% calories
                val protein = profile.weight * 1.8f // 1.8g/kg
                val fat = calories * 0.7f / 9f // 70% of calories from fat
                val carbs = profile.weight * 0.5f // 0.5g/kg (very low carb)
                
                MacroTargets(calories, protein, carbs, fat)
            }
            
            "Diabetes Management" -> {
                val calories = tdee // Maintain weight
                val protein = profile.weight * 1.2f // 1.2g/kg
                val carbs = calories * 0.45f / 4f // 45% of calories from carbs
                val fat = (calories - (protein * 4f) - (carbs * 4f)) / 9f // Balanced fat
                
                MacroTargets(calories, protein, carbs, fat)
            }
            
            "Custom Plan" -> {
                // Default to maintenance for custom plans
                val calories = tdee
                val protein = profile.weight * 1.6f // 1.6g/kg
                val fat = calories * 0.25f / 9f // 25% of calories from fat
                val carbs = (calories - (protein * 4f) - (fat * 9f)) / 4f
                
                MacroTargets(calories, protein, carbs, fat)
            }
            
            "Maintenance" -> {
                val calories = tdee
                val protein = profile.weight * 1.6f // 1.6g/kg
                val fat = calories * 0.25f / 9f // 25% of calories from fat
                val carbs = (calories - (protein * 4f) - (fat * 9f)) / 4f
                
                MacroTargets(calories, protein, carbs, fat)
            }
            
            else -> {
                // Default to maintenance
                val calories = tdee
                val protein = profile.weight * 1.6f
                val fat = calories * 0.25f / 9f
                val carbs = (calories - (protein * 4f) - (fat * 9f)) / 4f
                
                MacroTargets(calories, protein, carbs, fat)
            }
        }
    }
    
    /**
     * Get activity level description
     */
    fun getActivityLevelDescription(activityLevel: Float): String {
        return when {
            activityLevel < 1.2f -> "Sedentary (little or no exercise)"
            activityLevel < 1.375f -> "Lightly active (light exercise 1-3 days/week)"
            activityLevel < 1.55f -> "Moderately active (moderate exercise 3-5 days/week)"
            activityLevel < 1.725f -> "Very active (hard exercise 6-7 days/week)"
            activityLevel < 1.9f -> "Extra active (very hard exercise, physical job)"
            else -> "Athlete (very hard exercise, physical job, training twice a day)"
        }
    }
    
    /**
     * Get goal description
     */
    fun getGoalDescription(goal: String): String {
        return when (goal) {
            "Weight Loss" -> "Reduce calorie intake by 20% with high protein (2g/kg)"
            "Muscle Gain" -> "Increase calories by 15% with high protein (2.2g/kg)"
            "Fat Loss (High Protein)" -> "Moderate calorie reduction with very high protein (2.5g/kg)"
            "Endurance Training" -> "Higher carbs (60%) for sustained energy during training"
            "Keto Diet" -> "High fat (70%), very low carb (<10%) for ketosis"
            "Diabetes Management" -> "Balanced macros with moderate carbs and fiber focus"
            "Custom Plan" -> "Customizable nutrition plan (editable macros)"
            "Maintenance" -> "Maintain current weight with balanced nutrition"
            else -> "Balanced nutrition plan"
        }
    }
    
    /**
     * Get goal duration description
     */
    fun getGoalDurationDescription(weeks: Int): String {
        return when {
            weeks <= 4 -> "1 month (aggressive)"
            weeks <= 8 -> "2 months (moderate)"
            weeks <= 12 -> "3 months (balanced)"
            weeks <= 16 -> "4 months (gradual)"
            weeks <= 24 -> "6 months (sustainable)"
            else -> "${weeks / 4} months (long-term)"
        }
    }
    
    /**
     * Get calorie adjustment description based on goal and duration
     */
    fun getCalorieAdjustmentDescription(goal: String, weeks: Int): String {
        return when (goal) {
            "Weight Loss" -> {
                val adjustment = when {
                    weeks <= 4 -> "25%"
                    weeks <= 8 -> "20%"
                    weeks <= 12 -> "15%"
                    weeks <= 16 -> "10%"
                    else -> "5%"
                }
                "Calorie deficit: $adjustment reduction"
            }
            "Muscle Gain" -> {
                val adjustment = when {
                    weeks <= 4 -> "25%"
                    weeks <= 8 -> "20%"
                    weeks <= 12 -> "15%"
                    weeks <= 16 -> "10%"
                    else -> "5%"
                }
                "Calorie surplus: $adjustment increase"
            }
            "Fat Loss (High Protein)" -> {
                val adjustment = when {
                    weeks <= 4 -> "20%"
                    weeks <= 8 -> "15%"
                    weeks <= 12 -> "10%"
                    weeks <= 16 -> "5%"
                    else -> "2%"
                }
                "Calorie deficit: $adjustment reduction"
            }
            else -> "Standard calorie adjustment"
        }
    }
} 