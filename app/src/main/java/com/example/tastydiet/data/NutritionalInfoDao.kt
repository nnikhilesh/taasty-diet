package com.example.tastydiet.data

import androidx.room.*
import com.example.tastydiet.data.models.NutritionalInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface NutritionalInfoDao {
    @Query("SELECT * FROM nutritional_info WHERE LOWER(name) LIKE LOWER(:foodName) LIMIT 1")
    suspend fun getByName(foodName: String): NutritionalInfo?
    
    @Query("SELECT * FROM nutritional_info WHERE name LIKE '%' || :query || '%'")
    suspend fun searchByName(query: String): List<NutritionalInfo>
    
    @Query("SELECT * FROM nutritional_info WHERE category = :category")
    suspend fun getByCategory(category: String): List<NutritionalInfo>
    
    @Query("SELECT * FROM nutritional_info ORDER BY name ASC")
    fun getAll(): Flow<List<NutritionalInfo>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(nutritionalInfo: NutritionalInfo)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(nutritionalInfoList: List<NutritionalInfo>)
    
    @Delete
    suspend fun delete(nutritionalInfo: NutritionalInfo)
    
    @Query("DELETE FROM nutritional_info")
    suspend fun deleteAll()
    
    // New method for autocomplete suggestions
    @Query("SELECT name FROM nutritional_info ORDER BY name ASC")
    suspend fun getAllNames(): List<String>
    
    @Query("SELECT COUNT(*) FROM nutritional_info")
    suspend fun getCount(): Int
} 