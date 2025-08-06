package com.example.tastydiet.util

data class RemainingMacros(
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val fiber: Float
)

data class DinnerSuggestion(
    val name: String,
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val description: String = "",
    val imageUrl: String = ""
) 