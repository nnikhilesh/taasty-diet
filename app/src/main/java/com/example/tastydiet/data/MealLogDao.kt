package com.example.tastydiet.data

import androidx.room.*
import com.example.tastydiet.data.models.MealLog

@Dao
interface MealLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: MealLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<MealLog>)

    @Query("SELECT * FROM MealLog ORDER BY timestamp DESC")
    suspend fun getAll(): List<MealLog>
} 