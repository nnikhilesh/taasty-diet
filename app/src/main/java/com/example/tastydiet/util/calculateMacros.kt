package com.example.tastydiet.util

import com.example.tastydiet.data.models.Ingredient
import com.example.tastydiet.data.models.Macros

/**
 * Calculates total macros (calories, protein, carbs) for a list of ingredients.
 * Assumes each ingredient's quantity is in grams unless unit conversion is needed.
 * Sums values proportionally if units differ (basic support for g, kg, mg).
 * Returns rounded Int values.
 */
fun calculateMacros(ingredients: List<Ingredient>): Macros {
    var totalCalories = 0.0
    var totalProtein = 0.0
    var totalCarbs = 0.0
    
    for (ingredient in ingredients) {
        // Convert quantity to grams based on unit
        val grams = when (ingredient.unit.lowercase()) {
            "g", "gram", "grams" -> ingredient.quantity
            "kg", "kilogram", "kilograms" -> ingredient.quantity * 1000f
            "mg", "milligram", "milligrams" -> ingredient.quantity / 1000f
            else -> ingredient.quantity // fallback: treat as grams
        }
        
        // For demo purposes, use placeholder values
        // In a real app, these would come from the ingredient's nutritional data
        totalCalories += grams * 1.0
        totalProtein += grams * 0.1
        totalCarbs += grams * 0.2
    }
    
    return Macros(
        calories = Math.round(totalCalories).toDouble(),
        protein = Math.round(totalProtein).toDouble(),
        carbs = Math.round(totalCarbs).toDouble(),
        fat = 0.0, // Not calculated here
        fiber = 0.0 // Not calculated here
    )
}
