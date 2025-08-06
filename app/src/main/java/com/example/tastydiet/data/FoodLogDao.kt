package com.example.tastydiet.data

import androidx.room.*
import com.example.tastydiet.data.models.FoodLog
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface FoodLogDao {
    @Query("SELECT * FROM food_logs ORDER BY timestamp DESC")
    fun getAllFoodLogs(): Flow<List<FoodLog>>
    
    @Query("SELECT * FROM food_logs WHERE profileId = :profileId ORDER BY timestamp DESC")
    fun getFoodLogsByProfile(profileId: Int): Flow<List<FoodLog>>
    
    @Query("SELECT * FROM food_logs WHERE profileId = :profileId AND date(timestamp/1000, 'unixepoch') = date('now') ORDER BY timestamp DESC")
    fun getTodayFoodLogs(profileId: Int): Flow<List<FoodLog>>
    
    @Query("SELECT * FROM food_logs WHERE date(timestamp/1000, 'unixepoch') = date('now') ORDER BY timestamp DESC")
    fun getTodayAllFoodLogs(): Flow<List<FoodLog>>
    
    @Query("SELECT * FROM food_logs WHERE profileId = :profileId AND mealType = :mealType ORDER BY timestamp DESC")
    fun getFoodLogsByMealType(profileId: Int, mealType: String): Flow<List<FoodLog>>
    
    @Query("SELECT * FROM food_logs WHERE profileId = :profileId AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getFoodLogsByDateRange(profileId: Int, startTime: Long, endTime: Long): Flow<List<FoodLog>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodLog(foodLog: FoodLog): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllFoodLogs(foodLogs: List<FoodLog>)
    
    @Update
    suspend fun updateFoodLog(foodLog: FoodLog)
    
    @Delete
    suspend fun deleteFoodLog(foodLog: FoodLog)
    
    @Query("DELETE FROM food_logs WHERE id = :id")
    suspend fun deleteFoodLogById(id: Int)
    
    @Query("DELETE FROM food_logs WHERE profileId = :profileId")
    suspend fun deleteAllFoodLogsForProfile(profileId: Int)
    
    @Query("SELECT SUM(calories * quantity) FROM food_logs WHERE profileId = :profileId AND date(timestamp/1000, 'unixepoch') = date('now')")
    suspend fun getTodayTotalCalories(profileId: Int): Float?
    
    @Query("SELECT SUM(protein * quantity) FROM food_logs WHERE profileId = :profileId AND date(timestamp/1000, 'unixepoch') = date('now')")
    suspend fun getTodayTotalProtein(profileId: Int): Float?
    
    @Query("SELECT SUM(carbs * quantity) FROM food_logs WHERE profileId = :profileId AND date(timestamp/1000, 'unixepoch') = date('now')")
    suspend fun getTodayTotalCarbs(profileId: Int): Float?
    
    @Query("SELECT SUM(fat * quantity) FROM food_logs WHERE profileId = :profileId AND date(timestamp/1000, 'unixepoch') = date('now')")
    suspend fun getTodayTotalFat(profileId: Int): Float?
    
    @Query("SELECT COUNT(*) FROM food_logs WHERE profileId = :profileId")
    suspend fun getFoodLogCountForProfile(profileId: Int): Int
}