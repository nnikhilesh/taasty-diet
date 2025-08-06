package com.example.tastydiet.data.models

import androidx.room.*

@Entity(tableName = "recipe_steps")
data class RecipeStep(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,
    
    @ColumnInfo(name = "step_number")
    val stepNumber: Int,
    
    @ColumnInfo(name = "description")
    val description: String,
    
    @ColumnInfo(name = "duration")
    val duration: Int = 0, // in minutes
    
    @ColumnInfo(name = "temperature")
    val temperature: Int? = null, // in Celsius
    
    @ColumnInfo(name = "equipment")
    val equipment: String? = null,
    
    @ColumnInfo(name = "recipe_id")
    val recipeId: Int
)
