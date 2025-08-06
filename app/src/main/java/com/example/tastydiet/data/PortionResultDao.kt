package com.example.tastydiet.data

import androidx.room.*
import com.example.tastydiet.data.models.PortionResult

@Dao
interface PortionResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(portionResult: PortionResult)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(portionResults: List<PortionResult>)

    @Update
    suspend fun update(portionResult: PortionResult)

    @Delete
    suspend fun delete(portionResult: PortionResult)

    @Query("SELECT * FROM PortionResult ORDER BY id DESC")
    suspend fun getAll(): List<PortionResult>

    @Query("SELECT * FROM PortionResult WHERE mealId = :mealId")
    suspend fun getByMealId(mealId: Int): List<PortionResult>
} 