package com.example.tastydiet.data

import androidx.room.*
import com.example.tastydiet.data.models.FoodItem
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodItemDao {
    @Query("SELECT * FROM food_items WHERE isActive = 1 ORDER BY name ASC")
    fun getAllFoodItems(): Flow<List<FoodItem>>
    
    @Query("SELECT * FROM food_items WHERE isActive = 1 AND name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchFoodItems(query: String): Flow<List<FoodItem>>
    
    @Query("SELECT * FROM food_items WHERE isActive = 1 AND category = :category ORDER BY name ASC")
    fun getFoodItemsByCategory(category: String): Flow<List<FoodItem>>
    
    @Query("SELECT * FROM food_items WHERE isActive = 1 AND id = :id")
    suspend fun getFoodItemById(id: Int): FoodItem?
    
    @Query("SELECT * FROM food_items WHERE isActive = 1 AND name = :name LIMIT 1")
    suspend fun getFoodItemByName(name: String): FoodItem?
    
    @Query("SELECT DISTINCT category FROM food_items WHERE isActive = 1 ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>
    
    @Query("SELECT * FROM food_items WHERE isActive = 1 AND (isVeg = 1 OR isGlutenFree = 1 OR isDairyFree = 1)")
    fun getDietaryRestrictedItems(): Flow<List<FoodItem>>
    
    @Query("SELECT * FROM food_items WHERE isActive = 1 AND isVeg = 1")
    fun getVegetarianItems(): Flow<List<FoodItem>>
    
    @Query("SELECT * FROM food_items WHERE isActive = 1 AND isGlutenFree = 1")
    fun getGlutenFreeItems(): Flow<List<FoodItem>>
    
    @Query("SELECT * FROM food_items WHERE isActive = 1 AND isDairyFree = 1")
    fun getDairyFreeItems(): Flow<List<FoodItem>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodItem(foodItem: FoodItem): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFoodItems(foodItems: List<FoodItem>)
    
    @Update
    suspend fun updateFoodItem(foodItem: FoodItem)
    
    @Delete
    suspend fun deleteFoodItem(foodItem: FoodItem)
    
    @Query("DELETE FROM food_items WHERE id = :id")
    suspend fun deleteFoodItemById(id: Int)
    
    @Query("UPDATE food_items SET isActive = 0 WHERE id = :id")
    suspend fun deactivateFoodItem(id: Int)
    
    @Query("SELECT COUNT(*) FROM food_items WHERE isActive = 1")
    suspend fun getFoodItemsCount(): Int
    
    @Query("SELECT * FROM food_items WHERE isActive = 1 ORDER BY name ASC LIMIT :limit")
    suspend fun getRecentFoodItems(limit: Int = 10): List<FoodItem>
    
    @Query("SELECT * FROM food_items WHERE isActive = 1 AND caloriesPerUnit BETWEEN :minCalories AND :maxCalories ORDER BY caloriesPerUnit ASC")
    fun getFoodItemsByCalorieRange(minCalories: Float, maxCalories: Float): Flow<List<FoodItem>>
    
    @Query("SELECT * FROM food_items WHERE isActive = 1 AND proteinPerUnit >= :minProtein ORDER BY proteinPerUnit DESC")
    fun getHighProteinFoodItems(minProtein: Float = 10f): Flow<List<FoodItem>>
} 