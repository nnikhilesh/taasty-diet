package com.example.tastydiet.data

import androidx.room.*
import com.example.tastydiet.data.models.ShoppingListItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListDao {
    @Query("SELECT * FROM shopping_list_items ORDER BY category ASC, name ASC")
    fun getAllItems(): Flow<List<ShoppingListItem>>
    
    @Query("SELECT * FROM shopping_list_items WHERE isChecked = 0 ORDER BY category ASC, name ASC")
    fun getUncheckedItems(): Flow<List<ShoppingListItem>>
    
    @Query("SELECT * FROM shopping_list_items WHERE category = :category ORDER BY name ASC")
    fun getItemsByCategory(category: String): Flow<List<ShoppingListItem>>
    
    @Query("SELECT DISTINCT category FROM shopping_list_items ORDER BY category ASC")
    fun getAllCategories(): Flow<List<String>>
    
    @Query("SELECT COUNT(*) FROM shopping_list_items WHERE isChecked = 0")
    suspend fun getUncheckedItemCount(): Int
    
    @Query("SELECT COUNT(*) FROM shopping_list_items WHERE isChecked = 1")
    suspend fun getCheckedItemCount(): Int
    
    @Query("SELECT SUM(estimatedPrice) FROM shopping_list_items WHERE isChecked = 0")
    suspend fun getTotalEstimatedPrice(): Float?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ShoppingListItem): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ShoppingListItem>)
    
    @Update
    suspend fun updateItem(item: ShoppingListItem)
    
    @Delete
    suspend fun deleteItem(item: ShoppingListItem)
    
    @Query("DELETE FROM shopping_list_items WHERE isChecked = 1")
    suspend fun deleteCheckedItems()
    
    @Query("DELETE FROM shopping_list_items")
    suspend fun deleteAllItems()
    
    @Query("UPDATE shopping_list_items SET isChecked = :isChecked WHERE id = :itemId")
    suspend fun updateItemCheckedStatus(itemId: Int, isChecked: Boolean)
    
    @Query("UPDATE shopping_list_items SET isChecked = 0")
    suspend fun uncheckAllItems()
} 