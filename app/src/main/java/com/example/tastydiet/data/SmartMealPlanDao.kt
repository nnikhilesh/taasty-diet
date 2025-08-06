package com.example.tastydiet.data

import androidx.room.*
import com.example.tastydiet.data.models.SmartMealPlan
import kotlinx.coroutines.flow.Flow

@Dao
interface SmartMealPlanDao {
    @Query("SELECT * FROM SmartMealPlan WHERE date = :date AND memberId = :memberId")
    suspend fun getMealPlanForDate(date: String, memberId: Int): SmartMealPlan?

    @Query("SELECT * FROM SmartMealPlan WHERE date = :date AND memberId = :memberId")
    fun getMealPlanForDateFlow(date: String, memberId: Int): Flow<SmartMealPlan?>

    @Query("SELECT * FROM SmartMealPlan WHERE memberId = :memberId ORDER BY date DESC")
    fun getMealPlansForMember(memberId: Int): Flow<List<SmartMealPlan>>

    @Query("SELECT * FROM SmartMealPlan WHERE date BETWEEN :startDate AND :endDate AND memberId = :memberId ORDER BY date")
    fun getMealPlansForDateRange(startDate: String, endDate: String, memberId: Int): Flow<List<SmartMealPlan>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mealPlan: SmartMealPlan): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(mealPlans: List<SmartMealPlan>)

    @Update
    suspend fun update(mealPlan: SmartMealPlan)

    @Delete
    suspend fun delete(mealPlan: SmartMealPlan)

    @Query("DELETE FROM SmartMealPlan WHERE date = :date AND memberId = :memberId")
    suspend fun deleteMealPlanForDate(date: String, memberId: Int)

    @Query("UPDATE SmartMealPlan SET isAccepted = :isAccepted WHERE id = :mealPlanId")
    suspend fun updateAcceptanceStatus(mealPlanId: Int, isAccepted: Boolean)
} 