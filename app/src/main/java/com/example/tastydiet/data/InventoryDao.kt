package com.example.tastydiet.data

import androidx.room.*
import com.example.tastydiet.data.models.InventoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Query("SELECT * FROM inventory_items ORDER BY (dateAdded + shelfLifeDays * 86400000) ASC, category, name")
    fun getAll(): Flow<List<InventoryItem>>

    @Query("SELECT * FROM inventory_items WHERE id = :id")
    suspend fun getById(id: Int): InventoryItem?

    @Query("SELECT * FROM inventory_items WHERE name LIKE :name LIMIT 1")
    suspend fun getByName(name: String): InventoryItem?
    
    @Query("SELECT * FROM inventory_items WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun getByNameCaseInsensitive(name: String): InventoryItem?
    
    @Query("SELECT * FROM inventory_items WHERE name LIKE :searchQuery ORDER BY name")
    suspend fun searchByName(searchQuery: String): List<InventoryItem>

    @Query("SELECT * FROM inventory_items WHERE category = :category")
    suspend fun getByCategory(category: String): List<InventoryItem>

    @Query("SELECT * FROM inventory_items WHERE quantity < :threshold")
    suspend fun getLowStockItems(threshold: Float): List<InventoryItem>

    @Query("SELECT * FROM inventory_items WHERE (dateAdded + shelfLifeDays * 86400000) <= :expiryDate")
    suspend fun getExpiringItems(expiryDate: Long): List<InventoryItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: InventoryItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<InventoryItem>)

    @Update
    suspend fun update(item: InventoryItem)

    @Delete
    suspend fun delete(item: InventoryItem)

    @Query("DELETE FROM inventory_items")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM inventory_items")
    suspend fun getCount(): Int

    @Query("SELECT DISTINCT category FROM inventory_items ORDER BY category")
    suspend fun getCategories(): List<String>
    
    // Alias for getAll() to maintain compatibility
    fun getAllInventoryItems(): Flow<List<InventoryItem>> = getAll()
    
    suspend fun insertAllInventoryItems(items: List<InventoryItem>) {
        insertAll(items)
    }

    // Smart meal planning queries
    @Query("SELECT * FROM inventory_items WHERE name IN (:ingredientNames)")
    suspend fun getItemsByNames(ingredientNames: List<String>): List<InventoryItem>

    @Query("SELECT quantity FROM inventory_items WHERE name = :itemName LIMIT 1")
    suspend fun getQuantityForItem(itemName: String): Float?

    @Query("SELECT * FROM inventory_items WHERE name LIKE '%' || :query || '%'")
    suspend fun searchItemsByName(query: String): List<InventoryItem>

    @Query("SELECT name FROM inventory_items WHERE quantity > 0")
    suspend fun getAvailableItemNames(): List<String>
} 