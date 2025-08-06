package com.example.tastydiet.data.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "food_logs",
    foreignKeys = [
        ForeignKey(
            entity = Profile::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["profileId"])]
)
data class FoodLog(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val profileId: Int,
    val foodName: String,
    val mealType: String, // "Breakfast", "Lunch", "Dinner", "Snack"
    val quantity: Float,
    val unit: String = "serving",
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val fiber: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun getFormattedTime(): String {
        val date = java.util.Date(timestamp)
        val formatter = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
        return formatter.format(date)
    }
    
    fun getFormattedDate(): String {
        val date = java.util.Date(timestamp)
        val formatter = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
        return formatter.format(date)
    }
    
    fun getTotalCalories(): Float = calories
    fun getTotalProtein(): Float = protein
    fun getTotalCarbs(): Float = carbs
    fun getTotalFat(): Float = fat
} 