package com.example.tastydiet.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "analytics_data")
@Serializable
data class AnalyticsData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val profileId: Int,
    val date: String, // YYYY-MM-DD format
    val totalCalories: Float,
    val totalProtein: Float,
    val totalCarbs: Float,
    val totalFat: Float,
    val totalFiber: Float,
    val mealCount: Int,
    val timestamp: Long = System.currentTimeMillis()
)

data class WeeklyAnalytics(
    val weekStart: String,
    val weekEnd: String,
    val dailyData: List<AnalyticsData>,
    val averageCalories: Float,
    val averageProtein: Float,
    val averageCarbs: Float,
    val averageFat: Float,
    val totalMeals: Int
)

data class MonthlyAnalytics(
    val month: String, // YYYY-MM format
    val weeklyData: List<WeeklyAnalytics>,
    val totalCalories: Float,
    val totalProtein: Float,
    val totalCarbs: Float,
    val totalFat: Float,
    val averageCaloriesPerDay: Float,
    val averageProteinPerDay: Float,
    val averageCarbsPerDay: Float,
    val averageFatPerDay: Float
)

data class ProgressSummary(
    val currentBMI: Float,
    val bmiCategory: String,
    val weightTrend: String, // "Gaining", "Losing", "Stable"
    val topFoods: List<TopFood>,
    val skippedMeals: List<SkippedMeal>,
    val averageCaloriesPerDay: Float,
    val daysLogged: Int
)

data class TopFood(
    val foodName: String,
    val frequency: Int,
    val totalCalories: Float
)

data class SkippedMeal(
    val mealType: String,
    val date: String,
    val frequency: Int
) 