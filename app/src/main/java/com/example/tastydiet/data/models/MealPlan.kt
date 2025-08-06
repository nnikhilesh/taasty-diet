package com.example.tastydiet.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class MealPlan(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mealTime: String, // Breakfast, Lunch, Dinner
    val recipeIds: String, // comma-separated recipe IDs
    val date: String // e.g., YYYY-MM-DD
) 