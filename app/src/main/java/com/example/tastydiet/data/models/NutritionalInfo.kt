package com.example.tastydiet.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "nutritional_info")
@Serializable
data class NutritionalInfo(
    @PrimaryKey val name: String,
    val caloriesPer100g: Float,
    val proteinPer100g: Float,
    val carbsPer100g: Float,
    val fatPer100g: Float,
    val fiberPer100g: Float = 0f,
    val category: String = "General", // e.g., "Vegetables", "Non-Veg", "Eggs", "Dry Fruits", "Grains", "Dairy", "Spices", "Oils", "Beverages", "Snacks", "Fruits"
    val unit: String = "g" // Default unit for the item
) 