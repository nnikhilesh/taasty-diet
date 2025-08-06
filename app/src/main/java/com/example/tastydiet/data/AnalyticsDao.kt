package com.example.tastydiet.data

import androidx.room.*
import com.example.tastydiet.data.models.AnalyticsData
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalyticsDao {
    @Query("SELECT * FROM analytics_data WHERE profileId = :profileId ORDER BY date DESC")
    fun getAnalyticsByProfile(profileId: Int): Flow<List<AnalyticsData>>
    
    @Query("SELECT * FROM analytics_data WHERE profileId = :profileId AND date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getAnalyticsByDateRange(profileId: Int, startDate: String, endDate: String): Flow<List<AnalyticsData>>
    
    @Query("SELECT * FROM analytics_data WHERE profileId = :profileId AND date >= :startDate ORDER BY date ASC")
    fun getAnalyticsFromDate(profileId: Int, startDate: String): Flow<List<AnalyticsData>>
    
    @Query("SELECT * FROM analytics_data WHERE profileId = :profileId AND date = :date")
    suspend fun getAnalyticsForDate(profileId: Int, date: String): AnalyticsData?
    
    @Query("SELECT AVG(totalCalories) FROM analytics_data WHERE profileId = :profileId AND date BETWEEN :startDate AND :endDate")
    suspend fun getAverageCalories(profileId: Int, startDate: String, endDate: String): Float?
    
    @Query("SELECT AVG(totalProtein) FROM analytics_data WHERE profileId = :profileId AND date BETWEEN :startDate AND :endDate")
    suspend fun getAverageProtein(profileId: Int, startDate: String, endDate: String): Float?
    
    @Query("SELECT AVG(totalCarbs) FROM analytics_data WHERE profileId = :profileId AND date BETWEEN :startDate AND :endDate")
    suspend fun getAverageCarbs(profileId: Int, startDate: String, endDate: String): Float?
    
    @Query("SELECT AVG(totalFat) FROM analytics_data WHERE profileId = :profileId AND date BETWEEN :startDate AND :endDate")
    suspend fun getAverageFat(profileId: Int, startDate: String, endDate: String): Float?
    
    @Query("SELECT MAX(totalCalories) FROM analytics_data WHERE profileId = :profileId AND date BETWEEN :startDate AND :endDate")
    suspend fun getMaxCalories(profileId: Int, startDate: String, endDate: String): Float?
    
    @Query("SELECT MIN(totalCalories) FROM analytics_data WHERE profileId = :profileId AND date BETWEEN :startDate AND :endDate")
    suspend fun getMinCalories(profileId: Int, startDate: String, endDate: String): Float?
    
    @Query("SELECT COUNT(*) FROM analytics_data WHERE profileId = :profileId AND date BETWEEN :startDate AND :endDate")
    suspend fun getDaysLogged(profileId: Int, startDate: String, endDate: String): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalytics(analytics: AnalyticsData): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalytics(analytics: List<AnalyticsData>)
    
    @Update
    suspend fun updateAnalytics(analytics: AnalyticsData)
    
    @Delete
    suspend fun deleteAnalytics(analytics: AnalyticsData)
    
    @Query("DELETE FROM analytics_data WHERE profileId = :profileId")
    suspend fun deleteAllAnalyticsForProfile(profileId: Int)
} 