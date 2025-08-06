package com.example.tastydiet.data

import androidx.room.*
import com.example.tastydiet.data.models.Ingredient
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {
    @Query("SELECT * FROM ingredients ORDER BY name ASC")
    fun getAllIngredients(): Flow<List<Ingredient>>
    
    @Query("SELECT * FROM ingredients ORDER BY name ASC")
    fun getAll(): Flow<List<Ingredient>>
    
    @Query("SELECT * FROM ingredients WHERE id = :id")
    suspend fun getIngredientById(id: Int): Ingredient?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: Ingredient): Long
    
    @Update
    suspend fun updateIngredient(ingredient: Ingredient)
    
    @Delete
    suspend fun deleteIngredient(ingredient: Ingredient)
    
    @Query("DELETE FROM ingredients WHERE id = :id")
    suspend fun deleteIngredientById(id: Int)
    
    @Query("SELECT * FROM ingredients WHERE category = :category")
    fun getIngredientsByCategory(category: String): Flow<List<Ingredient>>
    
    @Query("SELECT * FROM ingredients WHERE name LIKE '%' || :searchQuery || '%'")
    fun searchIngredients(searchQuery: String): Flow<List<Ingredient>>
    
    @Query("SELECT COUNT(*) FROM ingredients")
    suspend fun getIngredientCount(): Int
    
    @Query("SELECT * FROM ingredients WHERE expiryDate IS NOT NULL AND expiryDate < :currentTime")
    fun getExpiredIngredients(currentTime: Long = System.currentTimeMillis()): Flow<List<Ingredient>>
    
    @Query("SELECT * FROM ingredients WHERE expiryDate IS NOT NULL AND expiryDate > :currentTime AND expiryDate <= :expiryThreshold")
    fun getExpiringSoonIngredients(
        currentTime: Long = System.currentTimeMillis(),
        expiryThreshold: Long = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000) // 7 days
    ): Flow<List<Ingredient>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllIngredients(ingredients: List<Ingredient>)
    
    // New methods for enhanced inventory management
    @Query("SELECT * FROM ingredients WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun findIngredientByName(name: String): Ingredient?
    
    @Query("SELECT * FROM ingredients WHERE LOWER(name) LIKE LOWER(:name) || '%'")
    suspend fun findIngredientsByNamePrefix(name: String): List<Ingredient>
    
    @Query("UPDATE ingredients SET quantity = quantity + :additionalQuantity WHERE LOWER(name) = LOWER(:name)")
    suspend fun updateQuantityByName(name: String, additionalQuantity: Float): Int
} 