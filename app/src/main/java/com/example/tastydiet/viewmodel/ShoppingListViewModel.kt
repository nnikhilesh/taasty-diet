package com.example.tastydiet.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tastydiet.AppDatabase
import com.example.tastydiet.data.models.ShoppingListItem
import com.example.tastydiet.data.models.ShoppingListSummary
import com.example.tastydiet.data.models.NutritionalInfo
import com.example.tastydiet.data.models.InventoryItem
import java.util.Date
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LowInventorySuggestion(
    val inventoryItem: InventoryItem,
    val isSelected: Boolean = false,
    val snoozeUntil: Date? = null,
    val snoozeDays: Int = 0
)

class ShoppingListViewModel(application: Application) : AndroidViewModel(application) {
    private val shoppingListDao = AppDatabase.getInstance(application).shoppingListDao()
    private val ingredientDao = AppDatabase.getInstance(application).ingredientDao()
    private val nutritionalInfoDao = AppDatabase.getInstance(application).nutritionalInfoDao()
    private val inventoryDao = AppDatabase.getInstance(application).inventoryDao()
    
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
            "tomato" to ("Vegetables" to "kg"),
            "onion" to ("Vegetables" to "kg"),
            "potato" to ("Vegetables" to "kg"),
            "carrot" to ("Vegetables" to "kg"),
            "milk" to ("Dairy" to "litre"),
            "bread" to ("Bakery" to "packet"),
            "egg" to ("Protein" to "piece"),
            "rice" to ("Grains" to "kg"),
            "sugar" to ("Grocery" to "kg"),
            "salt" to ("Grocery" to "kg"),
            "oil" to ("Grocery" to "litre"),
            "apple" to ("Fruits" to "kg"),
            "banana" to ("Fruits" to "dozen"),
            "chicken" to ("Non-Veg" to "kg"),
            "fish" to ("Non-Veg" to "kg")
        )
    }
    
    private val _shoppingListItems = MutableStateFlow<List<ShoppingListItem>>(emptyList())
    val shoppingListItems: StateFlow<List<ShoppingListItem>> = _shoppingListItems.asStateFlow()
    
    private val _uncheckedItems = MutableStateFlow<List<ShoppingListItem>>(emptyList())
    val uncheckedItems: StateFlow<List<ShoppingListItem>> = _uncheckedItems.asStateFlow()
    
    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()
    
    private val _summary = MutableStateFlow<ShoppingListSummary?>(null)
    val summary: StateFlow<ShoppingListSummary?> = _summary.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _lowInventorySuggestions = MutableStateFlow<List<LowInventorySuggestion>>(emptyList())
    val lowInventorySuggestions: StateFlow<List<LowInventorySuggestion>> = _lowInventorySuggestions.asStateFlow()
    
    init {
        loadShoppingList()
        loadLowInventorySuggestions()
    }
    
    private fun loadShoppingList() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Load initial data
                val allItems = shoppingListDao.getAllItems().first()
                val uncheckedItems = shoppingListDao.getUncheckedItems().first()
                val categories = shoppingListDao.getAllCategories().first()
                
                _shoppingListItems.value = allItems
                _uncheckedItems.value = uncheckedItems
                _categories.value = categories
                
                // Update summary
                updateSummary()
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load shopping list: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun updateSummary() {
        viewModelScope.launch {
            try {
                val uncheckedCount = shoppingListDao.getUncheckedItemCount()
                val checkedCount = shoppingListDao.getCheckedItemCount()
                val totalPrice = shoppingListDao.getTotalEstimatedPrice() ?: 0f
                
                val categories = _categories.value.associateWith { category ->
                    _shoppingListItems.value.count { it.category == category }
                }
                
                _summary.value = ShoppingListSummary(
                    totalItems = uncheckedCount + checkedCount,
                    checkedItems = checkedCount,
                    totalEstimatedPrice = totalPrice,
                    categories = categories
                )
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update summary: ${e.message}"
            }
        }
    }
    
    private fun refreshData() {
        viewModelScope.launch {
            try {
                val allItems = shoppingListDao.getAllItems().first()
                val uncheckedItems = shoppingListDao.getUncheckedItems().first()
                val categories = shoppingListDao.getAllCategories().first()
                
                _shoppingListItems.value = allItems
                _uncheckedItems.value = uncheckedItems
                _categories.value = categories
                updateSummary()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to refresh data: ${e.message}"
            }
        }
    }
    
    private fun loadLowInventorySuggestions() {
        viewModelScope.launch {
            try {
                val inventoryItems = inventoryDao.getAll().first()
                val currentDate = Date()
                
                val lowInventoryItems = inventoryItems.filter { item ->
                    // Check if item is not snoozed
                    val isNotSnoozed = true // TODO: Implement snooze checking logic
                    
                    when (item.unit.lowercase()) {
                        "g", "kg" -> item.quantity < 0.5f // Less than 0.5 kg
                        "pcs", "pieces" -> item.quantity < 2f // Less than 2 pieces
                        "ml", "l", "litre" -> item.quantity < 0.5f // Less than 0.5 litre
                        else -> item.quantity < 1f // Less than 1 unit
                    } && isNotSnoozed
                }
                
                val suggestions = lowInventoryItems.map { item ->
                    LowInventorySuggestion(
                        inventoryItem = item,
                        isSelected = false,
                        snoozeUntil = null,
                        snoozeDays = 0
                    )
                }
                
                _lowInventorySuggestions.value = suggestions
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load low inventory suggestions: ${e.message}"
            }
        }
    }
    
    fun addLowInventoryItemToShoppingList(inventoryItem: InventoryItem) {
        viewModelScope.launch {
            try {
                // Calculate suggested quantity based on current inventory
                val suggestedQuantity = when (inventoryItem.unit.lowercase()) {
                    "g", "kg" -> 1f - inventoryItem.quantity // Add to reach 1 kg
                    "pcs", "pieces" -> 5f - inventoryItem.quantity // Add to reach 5 pieces
                    "ml", "l", "litre" -> 1f - inventoryItem.quantity // Add to reach 1 litre
                    else -> 2f - inventoryItem.quantity // Add to reach 2 units
                }.coerceAtLeast(1f) // Minimum 1 unit
                
                val item = ShoppingListItem(
                    name = inventoryItem.name,
                    quantity = suggestedQuantity,
                    unit = inventoryItem.unit,
                    category = inventoryItem.category,
                    priority = "Medium", // Medium priority for low inventory items
                    estimatedPrice = 0f,
                    notes = "Low inventory - current: ${inventoryItem.quantity} ${inventoryItem.unit}"
                )
                
                shoppingListDao.insertItem(item)
                refreshData()
                _errorMessage.value = "Added ${inventoryItem.name} to shopping list"
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add low inventory item: ${e.message}"
            }
        }
    }
    
    fun getCategoryAndUnit(itemName: String, context: Context? = null): Pair<String, String> {
        val normalizedName = itemName.trim().lowercase()
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
        return ITEM_CATEGORY_UNIT_MAP[normalizedName] ?: ("Other" to "pieces")
    }
    
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
    
    suspend fun getCategorySuggestion(itemName: String): String {
        return try {
            val nutritionalInfo = nutritionalInfoDao.getByName(itemName.trim().lowercase())
            nutritionalInfo?.category ?: "Other"
        } catch (e: Exception) {
            "Other"
        }
    }
    
    suspend fun getUnitSuggestion(category: String, itemName: String): String {
        return com.example.tastydiet.data.models.Ingredient.getDefaultUnit(category, itemName)
    }
    
    suspend fun getItemNameSuggestions(query: String): List<String> {
        return if (query.length >= 2) {
            try {
                nutritionalInfoDao.searchByName(query).map { it.name }
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    
    suspend fun getNutritionalInfoByName(name: String): NutritionalInfo? {
        return try {
            nutritionalInfoDao.getByName(name.trim().lowercase())
        } catch (e: Exception) {
            null
        }
    }
    
    fun formatShoppingListForWhatsApp(): String {
        val uncheckedItems = _shoppingListItems.value.filter { !it.isChecked }
        return if (uncheckedItems.isEmpty()) {
            "Shopping list is empty"
        } else {
            // Group items by category
            val groupedItems = uncheckedItems.groupBy { it.category }
            
            val itemsText = groupedItems.entries.joinToString("\n\n") { (category, items) ->
                val categoryItems = items.joinToString("\n") { item ->
                    val quantityText = when (item.unit.lowercase()) {
                        "kg" -> "${item.quantity} kg"
                        "g" -> "${item.quantity.toInt()} g"
                        "ml" -> "${item.quantity.toInt()} ml"
                        "l" -> "${item.quantity} L"
                        "pcs", "pieces" -> "${item.quantity.toInt()} pcs"
                        else -> "${item.quantity} ${item.unit}"
                    }
                    "• $quantityText ${item.name}"
                }
                "📂 $category\n$categoryItems"
            }
            
            val totalPrice = uncheckedItems.sumOf { it.estimatedPrice.toDouble() }
            val priceText = if (totalPrice > 0) "\n\n💰 Estimated Total: ₹${totalPrice.toInt()}" else ""
            
            "🛒 Shopping List$priceText\n\n$itemsText"
        }
    }
    
    // Enhanced add item method with auto-fill
    fun addItem(name: String, quantity: Float, unit: String, category: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val item = ShoppingListItem(
                    name = name.trim(),
                    quantity = quantity,
                    unit = unit,
                    category = category,
                    isChecked = false,
                    priority = "Medium",
                    estimatedPrice = 0f,
                    notes = ""
                )
                shoppingListDao.insertItem(item)
                refreshData()
                _errorMessage.value = "Added $name to shopping list"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add item: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun addItem(item: ShoppingListItem) {
        viewModelScope.launch {
            try {
                shoppingListDao.insertItem(item)
                refreshData()
                _errorMessage.value = "Added ${item.name} to shopping list"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add item: ${e.message}"
            }
        }
    }
    
    fun updateItem(item: ShoppingListItem) {
        viewModelScope.launch {
            try {
                shoppingListDao.updateItem(item)
                refreshData()
                _errorMessage.value = "Updated ${item.name}"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update item: ${e.message}"
            }
        }
    }
    
    fun deleteItem(item: ShoppingListItem) {
        viewModelScope.launch {
            try {
                shoppingListDao.deleteItem(item)
                refreshData()
                _errorMessage.value = "Deleted ${item.name}"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete item: ${e.message}"
            }
        }
    }
    
    fun toggleItemChecked(itemId: Int, isChecked: Boolean) {
        viewModelScope.launch {
            try {
                shoppingListDao.updateItemCheckedStatus(itemId, isChecked)
                refreshData()
                val status = if (isChecked) "checked" else "unchecked"
                _errorMessage.value = "Item $status"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update item status: ${e.message}"
            }
        }
    }
    
    fun updateItemChecked(itemId: Int, isChecked: Boolean) {
        viewModelScope.launch {
            try {
                shoppingListDao.updateItemCheckedStatus(itemId, isChecked)
                refreshData()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update item status: ${e.message}"
            }
        }
    }
    
    fun clearCompletedItems() {
        viewModelScope.launch {
            try {
                val completedItems = _shoppingListItems.value.filter { it.isChecked }
                
                if (completedItems.isNotEmpty()) {
                    var addedCount = 0
                    var updatedCount = 0
                    
                    // Process each completed item
                    completedItems.forEach { shoppingItem ->
                        // Check if item already exists in inventory
                        val existingItem = inventoryDao.getByNameCaseInsensitive(shoppingItem.name)
                        
                        if (existingItem != null) {
                            // Item exists, update quantity
                            val updatedItem = existingItem.copy(
                                quantity = existingItem.quantity + shoppingItem.quantity
                            )
                            inventoryDao.update(updatedItem)
                            updatedCount++
                        } else {
                            // Item doesn't exist, add new item
                            val newInventoryItem = InventoryItem(
                                name = shoppingItem.name,
                                quantity = shoppingItem.quantity,
                                unit = shoppingItem.unit,
                                category = shoppingItem.category
                            )
                            inventoryDao.insert(newInventoryItem)
                            addedCount++
                        }
                    }
                    
                    // Delete checked items from shopping list
                    shoppingListDao.deleteCheckedItems()
                    
                    refreshData()
                    loadLowInventorySuggestions() // Refresh low inventory suggestions
                    
                    val message = when {
                        addedCount > 0 && updatedCount > 0 -> "Added $addedCount new items and updated $updatedCount existing items in inventory"
                        addedCount > 0 -> "Added $addedCount new items to inventory"
                        updatedCount > 0 -> "Updated $updatedCount existing items in inventory"
                        else -> "No items processed"
                    }
                    _errorMessage.value = message
                } else {
                    _errorMessage.value = "No completed items to clear"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to clear completed items: ${e.message}"
            }
        }
    }
    
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
    
    fun refreshShoppingList() {
        loadShoppingList()
    }
    
    fun refreshLowInventorySuggestions() {
        loadLowInventorySuggestions()
    }
    
    fun toggleLowInventorySelection(itemId: Int) {
        val currentSuggestions = _lowInventorySuggestions.value.toMutableList()
        val index = currentSuggestions.indexOfFirst { it.inventoryItem.id == itemId }
        if (index != -1) {
            currentSuggestions[index] = currentSuggestions[index].copy(
                isSelected = !currentSuggestions[index].isSelected
            )
            _lowInventorySuggestions.value = currentSuggestions
        }
    }
    
    fun snoozeLowInventoryItem(itemId: Int, days: Int) {
        val currentSuggestions = _lowInventorySuggestions.value.toMutableList()
        val index = currentSuggestions.indexOfFirst { it.inventoryItem.id == itemId }
        if (index != -1) {
            val snoozeUntil = Date(System.currentTimeMillis() + (days * 24 * 60 * 60 * 1000L))
            currentSuggestions[index] = currentSuggestions[index].copy(
                snoozeUntil = snoozeUntil,
                snoozeDays = days
            )
            _lowInventorySuggestions.value = currentSuggestions
            // TODO: Save snooze data to database
        }
    }
    
    fun addSelectedLowInventoryItemsToShoppingList() {
        val selectedItems = _lowInventorySuggestions.value.filter { it.isSelected }
        selectedItems.forEach { suggestion ->
            addLowInventoryItemToShoppingList(suggestion.inventoryItem)
        }
        // Clear selections after adding
        val updatedSuggestions = _lowInventorySuggestions.value.map { it.copy(isSelected = false) }
        _lowInventorySuggestions.value = updatedSuggestions
    }
    
    fun addSampleInventoryData() {
        viewModelScope.launch {
            try {
                val sampleItems = listOf(
                    InventoryItem(name = "Milk", quantity = 0.2f, unit = "L", category = "Dairy"),
                    InventoryItem(name = "Bread", quantity = 1f, unit = "pcs", category = "Bakery"),
                    InventoryItem(name = "Tomatoes", quantity = 0.3f, unit = "kg", category = "Vegetables"),
                    InventoryItem(name = "Rice", quantity = 0.4f, unit = "kg", category = "Grains"),
                    InventoryItem(name = "Eggs", quantity = 1f, unit = "pcs", category = "Protein")
                )
                
                inventoryDao.insertAll(sampleItems)
                
                loadLowInventorySuggestions()
                _errorMessage.value = "Added sample inventory data"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add sample data: ${e.message}"
            }
        }
    }
    
    fun getCompletedItems(): List<ShoppingListItem> {
        return _shoppingListItems.value.filter { it.isChecked }
    }
    
    fun getPendingItems(): List<ShoppingListItem> {
        return _shoppingListItems.value.filter { !it.isChecked }
    }
    
    fun getItemsByCategory(category: String): List<ShoppingListItem> {
        return _shoppingListItems.value.filter { it.category == category }
    }
    
    fun searchItems(query: String): List<ShoppingListItem> {
        return _shoppingListItems.value.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.category.contains(query, ignoreCase = true)
        }
    }
    
    fun getTotalItems(): Int {
        return _shoppingListItems.value.size
    }
    
    fun getCompletedCount(): Int {
        return getCompletedItems().size
    }
    
    fun getPendingCount(): Int {
        return getPendingItems().size
    }
    
} 