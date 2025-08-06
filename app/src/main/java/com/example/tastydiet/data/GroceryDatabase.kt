package com.example.tastydiet.data

import android.content.Context
import com.example.tastydiet.data.models.GroceryItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader

object GroceryDatabase {
    
    suspend fun loadGroceryItems(context: Context): List<GroceryItem> {
        return withContext(Dispatchers.IO) {
            try {
                val groceryItems = mutableListOf<GroceryItem>()
                
                context.assets.open("item_meta.csv").use { inputStream ->
                    BufferedReader(inputStream.reader()).use { reader ->
                        // Skip header line
                        reader.readLine()
                        
                        var line: String?
                        var id = 1
                        
                        while (reader.readLine().also { line = it } != null) {
                            line?.let { csvLine ->
                                val parts = csvLine.split(",")
                                if (parts.size >= 3) {
                                    val itemName = parts[0].trim()
                                    val category = parts[1].trim()
                                    val unit = parts[2].trim()
                                    
                                    val groceryItem = GroceryItem(
                                        id = id++,
                                        name = itemName,
                                        category = category,
                                        unit = unit,
                                        quantity = 0f,
                                        isAvailable = false
                                    )
                                    
                                    groceryItems.add(groceryItem)
                                }
                            }
                        }
                    }
                }
                
                groceryItems
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }
    }
    
    fun getCategories(groceryItems: List<GroceryItem>): List<String> {
        return groceryItems.map { it.category }.distinct().sorted()
    }
    
    fun getItemsByCategory(groceryItems: List<GroceryItem>, category: String): List<GroceryItem> {
        return groceryItems.filter { it.category.equals(category, ignoreCase = true) }
    }
    
    fun searchItems(groceryItems: List<GroceryItem>, query: String): List<GroceryItem> {
        return groceryItems.filter { 
            it.name.contains(query, ignoreCase = true) || 
            it.category.contains(query, ignoreCase = true) 
        }
    }
} 