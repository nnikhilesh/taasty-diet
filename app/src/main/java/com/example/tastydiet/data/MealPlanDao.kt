package com.example.tastydiet.data

import androidx.room.*
import com.example.tastydiet.data.models.MealPlan
import com.example.tastydiet.data.models.Meal
import com.example.tastydiet.data.models.RecipeStep
import com.example.tastydiet.data.models.Ingredient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@Dao
interface MealPlanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mealPlan: MealPlan): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(mealPlans: List<MealPlan>)

    @Update
    suspend fun update(mealPlan: MealPlan)

    @Delete
    suspend fun delete(mealPlan: MealPlan)

    @Query("SELECT * FROM MealPlan ORDER BY date ASC")
    suspend fun getAll(): List<MealPlan>

    @Query("SELECT * FROM MealPlan WHERE date = :date")
    suspend fun getByDate(date: String): List<MealPlan>

    @Query("SELECT * FROM MealPlan WHERE date = :date")
    suspend fun getMealsByDate(date: String): List<MealPlan>

    @Query("SELECT * FROM MealPlan WHERE date = :date AND mealTime = :mealTime")
    suspend fun getMealByName(date: String, mealTime: String): MealPlan?

    @Query("SELECT * FROM MealPlan WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getMealsByDateRange(startDate: String, endDate: String): List<MealPlan>

    @Query("SELECT * FROM MealPlan WHERE date = :date AND mealTime = :mealTime")
    suspend fun getByDateAndMealTime(date: String, mealTime: String): MealPlan?

    @Query("SELECT * FROM MealPlan WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getByDateRange(startDate: String, endDate: String): List<MealPlan>

    @Query("DELETE FROM MealPlan WHERE date = :date")
    suspend fun deleteByDate(date: String)

    @Query("DELETE FROM MealPlan WHERE date = :date AND mealTime = :mealTime")
    suspend fun deleteByDateAndMealTime(date: String, mealTime: String)

    @Query("SELECT COUNT(*) FROM MealPlan WHERE date = :date")
    suspend fun getCountByDate(date: String): Int

    @Query("SELECT DISTINCT date FROM MealPlan ORDER BY date")
    suspend fun getDistinctDates(): List<String>
    
    fun getAllMealPlans(): Flow<List<MealPlan>> {
        return flow { emit(getAll()) }
    }
    
    suspend fun insertAllMealPlans(mealPlans: List<MealPlan>) {
        insertAll(mealPlans)
    }
} 