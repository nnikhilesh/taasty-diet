package com.example.tastydiet.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "recipe_ingredients",
    foreignKeys = [
        ForeignKey(
            entity = Recipe::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("recipeId")]
)
@Serializable
data class RecipeIngredient(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val recipeId: Int,
    val ingredientName: String,
    val quantity: Float,
    val unit: String // g, ml, piece, cup, tbsp, etc.
) 