package com.example.tastydiet.data

import androidx.room.*
import com.example.tastydiet.data.models.Meal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.*

@Dao
interface MealDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(meal: Meal): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(meals: List<Meal>): List<Long>

    @Update
    suspend fun update(meal: Meal)

    @Delete
    suspend fun delete(meal: Meal)

    @Query("SELECT * FROM meals WHERE date = :date")
    suspend fun getMealsByDate(date: Date): List<Meal>

    @Query("SELECT * FROM meals WHERE date = :date AND name = :mealName")
    suspend fun getMealByName(date: Date, mealName: String): Meal?

    @Query("SELECT * FROM meals WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getMealsByDateRange(startDate: Date, endDate: Date): List<Meal>

    @Query("SELECT COUNT(*) FROM meals WHERE date = :date")
    suspend fun getCountByDate(date: Date): Int

    @Query("SELECT DISTINCT date FROM meals ORDER BY date")
    suspend fun getDistinctDates(): List<Date>
    
    fun getAllMeals(): Flow<List<Meal>> {
        return flow { 
            // This is a placeholder - you might need to implement this based on your data structure
            emit(emptyList())
        }
    }
    
    suspend fun insertAllMeals(meals: List<Meal>) {
        insertAll(meals)
    }
}
