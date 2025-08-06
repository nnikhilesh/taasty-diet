package com.example.tastydiet

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tastydiet.AppDatabase
import com.example.tastydiet.data.models.InventoryItem
import com.example.tastydiet.data.models.NutritionalInfo
import com.example.tastydiet.data.DatabaseInitializer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class InventoryViewModel(application: Application) : AndroidViewModel(application) {
    private val inventoryDao = AppDatabase.getInstance(application).inventoryDao()
    private val nutritionalInfoDao = AppDatabase.getInstance(application).nutritionalInfoDao()
    
    init {
        // Initialize with sample data if inventory is empty
        viewModelScope.launch {
            val count = inventoryDao.getCount()
            if (count == 0) {
                DatabaseInitializer.populateInventoryWithSampleData(AppDatabase.getInstance(application))
            }
        }
    }
    
    // Static map for auto-category and unit assignment
    companion object {
        private var itemMetaMap: Map<String, Pair<String, String>>? = null
        
        fun loadItemMetaMap(context: Context): Map<String, Pair<String, String>> {
            if (itemMetaMap != null) {
                return itemMetaMap!!
            }
            
            val map = mutableMapOf<String, Pair<String, String>>()
            try {
                val inputStream = context.assets.open("item_meta.csv")
                inputStream.bufferedReader().use { reader ->
                    reader.readLine() // Skip header
                    reader.forEachLine { line ->
                        val parts = line.split(',')
                        if (parts.size == 3) {
                            val item = parts[0].trim().lowercase()
                            val category = parts[1].trim()
                            val unit = parts[2].trim()
                            map[item] = Pair(category, unit)
                        }
                    }
                }
            } catch (e: Exception) {
                // Fallback to static map if CSV file is not available
                return ITEM_CATEGORY_UNIT_MAP
            }
            
            itemMetaMap = map
            return map
        }
        
        private val ITEM_CATEGORY_UNIT_MAP = mapOf(
            // Fruits
            "apple" to ("Fruits" to "pieces"),
            "banana" to ("Fruits" to "pieces"),
            "orange" to ("Fruits" to "pieces"),
            "mango" to ("Fruits" to "pieces"),
            "grape" to ("Fruits" to "pieces"),
            "strawberry" to ("Fruits" to "pieces"),
            "blueberry" to ("Fruits" to "pieces"),
            "pineapple" to ("Fruits" to "pieces"),
            "watermelon" to ("Fruits" to "pieces"),
            "kiwi" to ("Fruits" to "pieces"),
            
            // Vegetables
            "tomato" to ("Vegetables" to "kg"),
            "onion" to ("Vegetables" to "kg"),
            "potato" to ("Vegetables" to "kg"),
            "carrot" to ("Vegetables" to "kg"),
            "cabbage" to ("Vegetables" to "kg"),
            "broccoli" to ("Vegetables" to "kg"),
            "cauliflower" to ("Vegetables" to "kg"),
            "spinach" to ("Vegetables" to "kg"),
            "lettuce" to ("Vegetables" to "kg"),
            "cucumber" to ("Vegetables" to "kg"),
            "bell pepper" to ("Vegetables" to "kg"),
            "garlic" to ("Vegetables" to "kg"),
            "ginger" to ("Vegetables" to "kg"),
            
            // Non-Veg
            "chicken" to ("Non-Veg" to "kg"),
            "beef" to ("Non-Veg" to "kg"),
            "pork" to ("Non-Veg" to "kg"),
            "fish" to ("Non-Veg" to "kg"),
            "shrimp" to ("Non-Veg" to "kg"),
            "lamb" to ("Non-Veg" to "kg"),
            "turkey" to ("Non-Veg" to "kg"),
            
            // Eggs
            "egg" to ("Eggs" to "pieces"),
            "eggs" to ("Eggs" to "pieces"),
            
            // Dairy
            "milk" to ("Dairy" to "L"),
            "cheese" to ("Dairy" to "kg"),
            "yogurt" to ("Dairy" to "kg"),
            "butter" to ("Dairy" to "kg"),
            "cream" to ("Dairy" to "L"),
            "curd" to ("Dairy" to "kg"),
            
            // Grains
            "rice" to ("Grains" to "kg"),
            "wheat" to ("Grains" to "kg"),
            "bread" to ("Grains" to "pieces"),
            "pasta" to ("Grains" to "kg"),
            "flour" to ("Grains" to "kg"),
            "oats" to ("Grains" to "kg"),
            "quinoa" to ("Grains" to "kg"),
            
            // Dry Fruits
            "almond" to ("Dry Fruits" to "g"),
            "cashew" to ("Dry Fruits" to "g"),
            "raisin" to ("Dry Fruits" to "g"),
            "walnut" to ("Dry Fruits" to "g"),
            "pistachio" to ("Dry Fruits" to "g"),
            "dates" to ("Dry Fruits" to "g"),
            "prunes" to ("Dry Fruits" to "g"),
            
            // Spices
            "salt" to ("Spices" to "g"),
            "pepper" to ("Spices" to "g"),
            "turmeric" to ("Spices" to "g"),
            "cumin" to ("Spices" to "g"),
            "coriander" to ("Spices" to "g"),
            "cardamom" to ("Spices" to "g"),
            "cinnamon" to ("Spices" to "g"),
            "chili powder" to ("Spices" to "g"),
            
            // Oils
            "oil" to ("Oils" to "ml"),
            "olive oil" to ("Oils" to "ml"),
            "coconut oil" to ("Oils" to "ml"),
            "ghee" to ("Oils" to "ml"),
            "butter oil" to ("Oils" to "ml"),
            
            // Beverages
            "tea" to ("Beverages" to "L"),
            "coffee" to ("Beverages" to "L"),
            "juice" to ("Beverages" to "L"),
            "soda" to ("Beverages" to "L"),
            "water" to ("Beverages" to "L"),
            
            // Snacks
            "chips" to ("Snacks" to "pieces"),
            "cookies" to ("Snacks" to "pieces"),
            "crackers" to ("Snacks" to "pieces"),
            "popcorn" to ("Snacks" to "pieces"),
            "nuts" to ("Snacks" to "g")
        )
        
        fun getCategoryAndUnit(itemName: String, context: Context? = null): Pair<String, String> {
            val normalizedName = itemName.trim().lowercase()
            
            // Try to use CSV data if context is available
            if (context != null) {
                try {
                    val csvMap = loadItemMetaMap(context)
                    val result = csvMap[normalizedName]
                    if (result != null) {
                        return result
                    }
                } catch (e: Exception) {
                    // Fallback to static map if CSV loading fails
                }
            }
            
            // Fallback to static map
            return ITEM_CATEGORY_UNIT_MAP[normalizedName] ?: ("Other" to "pieces")
        }
    }
    
    private val _inventoryItems = MutableStateFlow<List<InventoryItem>>(emptyList())
    val inventoryItems: StateFlow<List<InventoryItem>> = _inventoryItems.asStateFlow()
    
    private val _expiringItems = MutableStateFlow<List<InventoryItem>>(emptyList())
    val expiringItems: StateFlow<List<InventoryItem>> = _expiringItems.asStateFlow()
    
    private val _lowStockItems = MutableStateFlow<List<InventoryItem>>(emptyList())
    val lowStockItems: StateFlow<List<InventoryItem>> = _lowStockItems.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadInventory()
        // Initialize nutritional database with test data
        initializeNutritionalDatabase()
    }

    fun loadInventory() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                println("DEBUG: Starting to load inventory")
                inventoryDao.getAll().collect { items ->
                    println("DEBUG: Loaded ${items.size} inventory items")
                    items.forEach { item ->
                        println("DEBUG: Item: ${item.name} - ${item.quantity} ${item.unit}")
                    }
                    _inventoryItems.value = items
                    _expiringItems.value = items.filter { it.isExpiringSoon() }
                    _lowStockItems.value = items.filter { it.quantity < 1 }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                println("DEBUG: Error loading inventory: ${e.message}")
                e.printStackTrace()
                _errorMessage.value = "Failed to load inventory: ${e.message}"
                _isLoading.value = false
                
                // If it's a table not found error, try to reset the database
                if (e.message?.contains("no such table") == true) {
                    Log.w("InventoryViewModel", "Attempting to reset database due to table error")
                    resetDatabase()
                }
            }
        }
    }
    
    // Function to populate inventory with all recipe ingredients for testing
    fun populateInventoryWithTestData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = "Adding test ingredients to inventory..."
                
                val testIngredients = listOf(
                    // Grains
                    InventoryItem(name = "Rice", quantity = 2.0f, unit = "kg", category = "Grains"),
                    InventoryItem(name = "Flour", quantity = 1.5f, unit = "kg", category = "Grains"),
                    InventoryItem(name = "Bread", quantity = 2.0f, unit = "pieces", category = "Grains"),
                    
                    // Proteins
                    InventoryItem(name = "Chicken", quantity = 1.0f, unit = "kg", category = "Non-Veg"),
                    InventoryItem(name = "Eggs", quantity = 12.0f, unit = "pieces", category = "Eggs"),
                    InventoryItem(name = "Paneer", quantity = 0.5f, unit = "kg", category = "Dairy"),
                    InventoryItem(name = "Dal", quantity = 1.0f, unit = "kg", category = "Grains"),
                    
                    // Vegetables
                    InventoryItem(name = "Tomato", quantity = 1.0f, unit = "kg", category = "Vegetables"),
                    InventoryItem(name = "Onion", quantity = 1.0f, unit = "kg", category = "Vegetables"),
                    InventoryItem(name = "Potato", quantity = 2.0f, unit = "kg", category = "Vegetables"),
                    InventoryItem(name = "Garlic", quantity = 0.2f, unit = "kg", category = "Vegetables"),
                    InventoryItem(name = "Ginger", quantity = 0.1f, unit = "kg", category = "Vegetables"),
                    InventoryItem(name = "Vegetables", quantity = 1.0f, unit = "kg", category = "Vegetables"),
                    
                    // Dairy
                    InventoryItem(name = "Milk", quantity = 2.0f, unit = "L", category = "Dairy"),
                    InventoryItem(name = "Curd", quantity = 1.0f, unit = "kg", category = "Dairy"),
                    InventoryItem(name = "Butter", quantity = 0.5f, unit = "kg", category = "Dairy"),
                    
                    // Oils and Spices
                    InventoryItem(name = "Oil", quantity = 1.0f, unit = "L", category = "Oils"),
                    InventoryItem(name = "Sugar", quantity = 0.5f, unit = "kg", category = "Spices"),
                    InventoryItem(name = "Salt", quantity = 0.2f, unit = "kg", category = "Spices"),
                    InventoryItem(name = "Pepper", quantity = 0.1f, unit = "kg", category = "Spices"),
                    InventoryItem(name = "Spices", quantity = 0.3f, unit = "kg", category = "Spices")
                )
                
                var addedCount = 0
                for (ingredient in testIngredients) {
                    try {
                        // Check if item already exists
                        val existingItem = inventoryDao.getByNameCaseInsensitive(ingredient.name)
                        if (existingItem == null) {
                            val id = inventoryDao.insert(ingredient)
                            addedCount++
                            println("DEBUG: Added ingredient: ${ingredient.name} with ID: $id")
                        } else {
                            println("DEBUG: Ingredient already exists: ${ingredient.name}")
                        }
                    } catch (e: Exception) {
                        println("DEBUG: Error adding ingredient ${ingredient.name}: ${e.message}")
                    }
                }
                
                _errorMessage.value = "Added $addedCount new ingredients to inventory"
                _isLoading.value = false
                
                // Reload inventory to show new items
                loadInventory()
                
            } catch (e: Exception) {
                println("DEBUG: Error populating inventory: ${e.message}")
                e.printStackTrace()
                _errorMessage.value = "Failed to populate inventory: ${e.message}"
                _isLoading.value = false
            }
        }
    }
    
    private fun resetDatabase() {
        viewModelScope.launch {
            try {
                Log.i("InventoryViewModel", "Resetting database...")
                AppDatabase.resetDatabase(getApplication())
                Log.i("InventoryViewModel", "Database reset complete, reloading inventory...")
                // Try loading inventory again after reset
                loadInventory()
            } catch (e: Exception) {
                Log.e("InventoryViewModel", "Failed to reset database: ${e.message}")
                _errorMessage.value = "Database reset failed: ${e.message}"
            }
        }
    }

    fun addItem(item: InventoryItem) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                println("DEBUG: Adding item: ${item.name}")
                
                // Input validation
                if (item.name.trim().isEmpty()) {
                    _errorMessage.value = "Item name cannot be empty"
                    return@launch
                }
                
                if (item.name.trim().length > 100) {
                    _errorMessage.value = "Item name is too long (max 100 characters)"
                    return@launch
                }
                
                if (item.quantity < 0) {
                    _errorMessage.value = "Quantity cannot be negative"
                    return@launch
                }
                
                if (item.quantity > 999999) {
                    _errorMessage.value = "Quantity is too large"
                    return@launch
                }
                
                // Check for existing item with same name (case-insensitive)
                val existingItem = inventoryDao.getByNameCaseInsensitive(item.name.trim())
                
                if (existingItem != null) {
                    // Update existing item: increase quantity and update category/unit if changed
                    val updatedItem = existingItem.copy(
                        quantity = existingItem.quantity + item.quantity,
                        category = item.category.ifBlank { existingItem.category },
                        unit = item.unit.ifBlank { existingItem.unit },
                        dateAdded = System.currentTimeMillis() // Update timestamp
                    )
                    
                    inventoryDao.update(updatedItem)
                    println("DEBUG: Updated existing item: ${updatedItem.name}, new quantity: ${updatedItem.quantity}")
                    _errorMessage.value = "Updated existing item: ${updatedItem.name} (quantity: ${updatedItem.quantity})"
                } else {
                    // Insert new item
                    val validItem = item.copy(
                        name = item.name.trim(),
                        quantity = if (item.quantity <= 0) 1f else item.quantity,
                        unit = if (item.unit.isBlank()) "pieces" else item.unit,
                        category = if (item.category.isBlank()) "Other" else item.category,
                        dateAdded = System.currentTimeMillis(),
                        shelfLifeDays = 7
                    )
                    
                    val id = inventoryDao.insert(validItem)
                    println("DEBUG: New item added with ID: $id")
                    _errorMessage.value = "Item added successfully"
                }
                // The flow will automatically update the UI
            } catch (e: Exception) {
                println("DEBUG: Error adding item: ${e.message}")
                e.printStackTrace()
                _errorMessage.value = "Failed to add item: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateItem(item: InventoryItem) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                println("DEBUG: Updating item: ${item.name}")
                
                // Ensure the item has valid data
                val validItem = item.copy(
                    name = item.name.trim(),
                    quantity = if (item.quantity <= 0) 1f else item.quantity,
                    unit = if (item.unit.isBlank()) "pieces" else item.unit,
                    category = if (item.category.isBlank()) "Other" else item.category
                )
                
                inventoryDao.update(validItem)
                println("DEBUG: Item updated successfully")
                _errorMessage.value = "Item updated successfully"
                // The flow will automatically update the UI
            } catch (e: Exception) {
                println("DEBUG: Error updating item: ${e.message}")
                e.printStackTrace()
                _errorMessage.value = "Failed to update item: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteItem(item: InventoryItem) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                inventoryDao.delete(item)
                _errorMessage.value = "Item deleted successfully"
                // The flow will automatically update the UI
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete item: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getItemById(id: Int): InventoryItem? {
        return _inventoryItems.value.find { it.id == id }
    }

    fun getItemsByCategory(category: String): List<InventoryItem> {
        return _inventoryItems.value.filter { it.category == category }
    }

    fun getExpiringItems(): List<InventoryItem> {
        return _expiringItems.value
    }

    fun getLowStockItems(): List<InventoryItem> {
        return _lowStockItems.value
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
    
    // Debug method to check database count
    fun checkDatabaseCount() {
        viewModelScope.launch {
            try {
                val count = inventoryDao.getCount()
                println("DEBUG: Database count: $count")
                _errorMessage.value = "Database count: $count"
            } catch (e: Exception) {
                println("DEBUG: Error getting count: ${e.message}")
                _errorMessage.value = "Error getting count: ${e.message}"
            }
        }
    }
    
    // Debug method to clear database
    fun clearDatabase() {
        viewModelScope.launch {
            try {
                AppDatabase.clearDatabase(getApplication())
                println("DEBUG: Database cleared")
                _errorMessage.value = "Database cleared successfully"
                // Force recreate database instance
                val newDatabase = AppDatabase.getInstance(getApplication())
                // Reload inventory after clearing
                loadInventory()
            } catch (e: Exception) {
                println("DEBUG: Error clearing database: ${e.message}")
                _errorMessage.value = "Error clearing database: ${e.message}"
            }
        }
    }
    
    // Force database recreation
    fun forceDatabaseRecreation() {
        viewModelScope.launch {
            try {
                println("DEBUG: Forcing database recreation")
                AppDatabase.clearDatabase(getApplication())
                // Clear the singleton instance
                AppDatabase::class.java.getDeclaredField("INSTANCE").apply {
                    isAccessible = true
                    set(null, null)
                }
                // Create new instance
                val newDatabase = AppDatabase.getInstance(getApplication())
                println("DEBUG: Database recreated successfully")
                _errorMessage.value = "Database recreated successfully"
                loadInventory()
            } catch (e: Exception) {
                println("DEBUG: Error recreating database: ${e.message}")
                _errorMessage.value = "Error recreating database: ${e.message}"
            }
        }
    }
    
    // Test method to add sample data
    fun addTestData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val testItems = listOf(
                    InventoryItem(name = "Banana", quantity = 5f, unit = "pieces", category = "Fruits"),
                    InventoryItem(name = "Apple", quantity = 3f, unit = "pieces", category = "Fruits"),
                    InventoryItem(name = "Rice", quantity = 2f, unit = "kg", category = "Grains"),
                    InventoryItem(name = "Milk", quantity = 1f, unit = "L", category = "Dairy"),
                    InventoryItem(name = "Eggs", quantity = 12f, unit = "pieces", category = "Eggs")
                )
                
                testItems.forEach { item ->
                    inventoryDao.insert(item)
                }
                
                _errorMessage.value = "Test data added successfully"
                loadInventory()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add test data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Enhanced methods for auto-fill functionality
    suspend fun getCategorySuggestion(itemName: String): String {
        Log.d("DEBUG", "getCategorySuggestion called with: $itemName")
        return try {
            val nutritionalInfo = nutritionalInfoDao.getByName(itemName.trim().lowercase())
            Log.d("DEBUG", "Nutritional info found: ${nutritionalInfo?.category}")
            nutritionalInfo?.category ?: getDefaultCategory(itemName)
        } catch (e: Exception) {
            Log.e("DEBUG", "Error in getCategorySuggestion: ${e.message}", e)
            getDefaultCategory(itemName)
        }
    }

    suspend fun getUnitSuggestion(category: String, itemName: String): String {
        Log.d("DEBUG", "getUnitSuggestion called with category: $category, itemName: $itemName")
        return try {
            val nutritionalInfo = nutritionalInfoDao.getByName(itemName.trim().lowercase())
            Log.d("DEBUG", "Nutritional info found: ${nutritionalInfo?.unit}")
            nutritionalInfo?.unit ?: getDefaultUnit(category, itemName)
        } catch (e: Exception) {
            Log.e("DEBUG", "Error in getUnitSuggestion: ${e.message}", e)
            getDefaultUnit(category, itemName)
        }
    }

    suspend fun getItemNameSuggestions(query: String): List<String> {
        Log.d("DEBUG", "getItemNameSuggestions called with query: $query")
        return if (query.length >= 2) {
            try {
                // Sanitize the query to prevent SQL injection or invalid queries
                val sanitizedQuery = query.trim().take(50) // Limit length
                Log.d("DEBUG", "Sanitized query: '$sanitizedQuery'")
                
                // Check if database is accessible
                val dbCount = nutritionalInfoDao.getCount()
                Log.d("DEBUG", "Database count: $dbCount")
                
                val results = nutritionalInfoDao.searchByName(sanitizedQuery)
                val suggestions = results.mapNotNull { it.name }.take(10) // Limit results
                Log.d("DEBUG", "Found ${suggestions.size} suggestions: $suggestions")
                suggestions
            } catch (e: Exception) {
                Log.e("DEBUG", "Error in getItemNameSuggestions: ${e.message}", e)
                emptyList()
            }
        } else {
            Log.d("DEBUG", "Query too short, returning empty list")
            emptyList()
        }
    }

    suspend fun getNutritionalInfoByName(name: String): NutritionalInfo? {
        Log.d("DEBUG", "getNutritionalInfoByName called with name: $name")
        return try {
            // Sanitize the name to prevent issues
            val sanitizedName = name.trim().lowercase().take(100) // Limit length
            Log.d("DEBUG", "Sanitized name: '$sanitizedName'")
            
            val result = nutritionalInfoDao.getByName(sanitizedName)
            Log.d("DEBUG", "Nutritional info result: ${result?.name}")
            result
        } catch (e: Exception) {
            Log.e("DEBUG", "Error in getNutritionalInfoByName: ${e.message}", e)
            null
        }
    }
    
    // Get auto-category and unit suggestions for an item name
            fun getAutoCategoryAndUnit(itemName: String, context: Context? = null): Pair<String, String> {
            return getCategoryAndUnit(itemName, context)
        }
        
        fun getItemSuggestions(query: String, context: Context? = null, limit: Int = 10): List<String> {
            if (query.isBlank()) return emptyList()
            
            val normalizedQuery = query.trim().lowercase()
            val suggestions = mutableListOf<String>()
            
            try {
                // Try to use CSV data if context is available
                if (context != null) {
                    val csvMap = loadItemMetaMap(context)
                    val matchingItems = csvMap.keys.filter { itemName ->
                        itemName.contains(normalizedQuery) || 
                        itemName.startsWith(normalizedQuery) ||
                        itemName.split(" ").any { word -> word.startsWith(normalizedQuery) }
                    }.sortedBy { itemName ->
                        // Prioritize exact matches, then starts with, then contains
                        when {
                            itemName == normalizedQuery -> 0
                            itemName.startsWith(normalizedQuery) -> 1
                            else -> 2
                        }
                    }.take(limit)
                    
                    suggestions.addAll(matchingItems)
                }
                
                // If we don't have enough suggestions from CSV, add from static map
                if (suggestions.size < limit) {
                    val staticSuggestions = ITEM_CATEGORY_UNIT_MAP.keys.filter { itemName ->
                        itemName.contains(normalizedQuery) && !suggestions.contains(itemName)
                    }.take(limit - suggestions.size)
                    
                    suggestions.addAll(staticSuggestions)
                }
                
            } catch (e: Exception) {
                // Fallback to static map only
                val staticSuggestions = ITEM_CATEGORY_UNIT_MAP.keys.filter { itemName ->
                    itemName.contains(normalizedQuery)
                }.take(limit)
                
                suggestions.addAll(staticSuggestions)
            }
            
            return suggestions.distinct().take(limit)
        }

    private fun getDefaultCategory(itemName: String): String {
        val lowerName = itemName.lowercase()
        return when {
            lowerName.contains("apple") || lowerName.contains("banana") || lowerName.contains("orange") || 
            lowerName.contains("mango") || lowerName.contains("grape") -> "Fruits"
            lowerName.contains("tomato") || lowerName.contains("onion") || lowerName.contains("potato") || 
            lowerName.contains("carrot") || lowerName.contains("cabbage") -> "Vegetables"
            lowerName.contains("chicken") || lowerName.contains("fish") || lowerName.contains("meat") -> "Non-Veg"
            lowerName.contains("egg") -> "Eggs"
            lowerName.contains("milk") || lowerName.contains("yogurt") || lowerName.contains("cheese") -> "Dairy"
            lowerName.contains("rice") || lowerName.contains("wheat") || lowerName.contains("bread") -> "Grains"
            lowerName.contains("almond") || lowerName.contains("cashew") || lowerName.contains("raisin") -> "Dry Fruits"
            lowerName.contains("salt") || lowerName.contains("pepper") || lowerName.contains("turmeric") -> "Spices"
            lowerName.contains("oil") || lowerName.contains("ghee") -> "Oils"
            lowerName.contains("tea") || lowerName.contains("coffee") || lowerName.contains("juice") -> "Beverages"
            else -> "Other"
        }
    }

    private fun getDefaultUnit(category: String, itemName: String): String {
        val lowerName = itemName.lowercase()
        return when (category) {
            "Fruits", "Vegetables" -> if (lowerName.contains("leaf") || lowerName.contains("small")) "pieces" else "kg"
            "Non-Veg", "Eggs" -> "kg"
            "Dairy" -> if (lowerName.contains("milk") || lowerName.contains("juice")) "L" else "kg"
            "Grains" -> "kg"
            "Dry Fruits" -> "g"
            "Spices" -> "g"
            "Oils" -> "ml"
            "Beverages" -> "L"
            else -> "pieces"
        }
    }

    private fun initializeNutritionalDatabase() {
        viewModelScope.launch {
            try {
                Log.d("DEBUG", "Initializing nutritional database")
                // Check if database is empty and add some common items
                val count = nutritionalInfoDao.getCount()
                Log.d("DEBUG", "Current nutritional database count: $count")
                if (count == 0) {
                    Log.d("DEBUG", "Database is empty, adding common items")
                    val commonItems = listOf(
                        NutritionalInfo("apple", 52f, 0.3f, 14f, 0.2f, 2.4f, "Fruits", "pieces"),
                        NutritionalInfo("banana", 89f, 1.1f, 23f, 0.3f, 2.6f, "Fruits", "pieces"),
                        NutritionalInfo("tomato", 18f, 0.9f, 3.9f, 0.2f, 1.2f, "Vegetables", "kg"),
                        NutritionalInfo("onion", 40f, 1.1f, 9.3f, 0.1f, 1.7f, "Vegetables", "kg"),
                        NutritionalInfo("potato", 77f, 2f, 17f, 0.1f, 2.2f, "Vegetables", "kg"),
                        NutritionalInfo("chicken", 165f, 31f, 0f, 3.6f, 0f, "Non-Veg", "kg"),
                        NutritionalInfo("egg", 155f, 13f, 1.1f, 11f, 0f, "Eggs", "pieces"),
                        NutritionalInfo("milk", 42f, 3.4f, 5f, 1f, 0f, "Dairy", "L"),
                        NutritionalInfo("rice", 130f, 2.7f, 28f, 0.3f, 0.4f, "Grains", "kg"),
                        NutritionalInfo("wheat", 327f, 13f, 72f, 1.5f, 12f, "Grains", "kg")
                    )
                    nutritionalInfoDao.insertAll(commonItems)
                    Log.d("DEBUG", "Successfully added ${commonItems.size} common items to database")
                } else {
                    Log.d("DEBUG", "Database already has $count items, skipping initialization")
                }
            } catch (e: Exception) {
                Log.e("DEBUG", "Error initializing nutritional database: ${e.message}", e)
                // Handle initialization errors gracefully
            }
        }
    }
} 