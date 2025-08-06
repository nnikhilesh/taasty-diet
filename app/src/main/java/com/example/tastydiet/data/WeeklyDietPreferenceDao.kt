package com.example.tastydiet.data

import androidx.room.*
import com.example.tastydiet.data.models.WeeklyDietPreference

@Dao
interface WeeklyDietPreferenceDao {
    @Query("SELECT * FROM WeeklyDietPreference LIMIT 1")
    suspend fun getFirst(): WeeklyDietPreference?

    @Query("SELECT * FROM WeeklyDietPreference")
    suspend fun getAll(): List<WeeklyDietPreference>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(preference: WeeklyDietPreference): Long

    @Update
    suspend fun update(preference: WeeklyDietPreference)

    @Delete
    suspend fun delete(preference: WeeklyDietPreference)

    @Query("DELETE FROM WeeklyDietPreference")
    suspend fun clearAll()
    
    // Add missing insertOrUpdate method to prevent NPE
    @Transaction
    suspend fun insertOrUpdate(preference: WeeklyDietPreference) {
        insert(preference)
    }
} 