package com.example.tastydiet.data

import androidx.room.*
import com.example.tastydiet.data.models.GroceryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface GroceryDao {
    
    @Query("SELECT * FROM grocery_items ORDER BY name ASC")
    fun getAllGroceryItems(): Flow<List<GroceryItem>>
    
    @Query("SELECT * FROM grocery_items WHERE category = :category ORDER BY name ASC")
    fun getGroceryItemsByCategory(category: String): Flow<List<GroceryItem>>
    
    @Query("SELECT DISTINCT category FROM grocery_items ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>
    
    @Query("SELECT * FROM grocery_items WHERE name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchGroceryItems(query: String): Flow<List<GroceryItem>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroceryItem(groceryItem: GroceryItem)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllGroceryItems(groceryItems: List<GroceryItem>)
    
    @Update
    suspend fun updateGroceryItem(groceryItem: GroceryItem)
    
    @Delete
    suspend fun deleteGroceryItem(groceryItem: GroceryItem)
    
    @Query("DELETE FROM grocery_items")
    suspend fun deleteAllGroceryItems()
    
    @Query("SELECT COUNT(*) FROM grocery_items")
    suspend fun getGroceryItemCount(): Int
} 