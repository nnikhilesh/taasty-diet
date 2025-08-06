package com.example.tastydiet.data.models

import androidx.room.*
import java.util.*

@Entity(tableName = "meals")
data class Meal(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "date")
    val date: Date,

    @ColumnInfo(name = "meal_type")
    val mealType: String,

    @ColumnInfo(name = "calories")
    val calories: Double = 0.0
) {
    @Ignore
    var macros: Map<String, Double> = emptyMap()

    @Ignore
    var ingredients: List<Ingredient> = emptyList()

    @Ignore
    var steps: List<RecipeStep> = emptyList()
}
