package com.example.tastydiet.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.util.*

@Entity(
    tableName = "FoodLogEntry",
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["recipeId"])]
)
@Serializable
data class FoodLogEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val recipeId: Int,
    val memberId: Int, // NEW: link to FamilyMember
    val quantity: Float, // e.g., number of pieces or grams
    val mealType: String, // Breakfast, Lunch, Dinner, Snack
    val timestamp: Long = System.currentTimeMillis()
) 