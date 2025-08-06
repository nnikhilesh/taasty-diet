package com.example.tastydiet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tastydiet.AppDatabase
import com.example.tastydiet.data.GroceryDatabase
import com.example.tastydiet.data.models.GroceryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GroceryViewModel(application: Application) : AndroidViewModel(application) {
    private val groceryDao = AppDatabase.getInstance(application).groceryDao()
    
    private val _groceryItems = MutableStateFlow<List<GroceryItem>>(emptyList())
    val groceryItems: StateFlow<List<GroceryItem>> = _groceryItems.asStateFlow()
    
    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    init {
        loadGroceryItems()
    }
    
    fun loadGroceryItems() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Load grocery items from CSV file
                val items = GroceryDatabase.loadGroceryItems(getApplication())
                
                // Insert into database
                groceryDao.insertAllGroceryItems(items)
                
                // Load from database
                groceryDao.getAllGroceryItems().collect { groceryItems ->
                    _groceryItems.value = groceryItems
                    _categories.value = GroceryDatabase.getCategories(groceryItems)
                }
                
                _errorMessage.value = "Successfully loaded ${items.size} grocery items!"
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load grocery items: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun searchGroceryItems(query: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                groceryDao.searchGroceryItems(query).collect { items ->
                    _groceryItems.value = items
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to search grocery items: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun getGroceryItemsByCategory(category: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                groceryDao.getGroceryItemsByCategory(category).collect { items ->
                    _groceryItems.value = items
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load grocery items by category: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun reloadGroceryItems() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Clear existing items and reload from CSV
                groceryDao.deleteAllGroceryItems()
                val items = GroceryDatabase.loadGroceryItems(getApplication())
                groceryDao.insertAllGroceryItems(items)
                
                _errorMessage.value = "Successfully reloaded ${items.size} grocery items!"
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to reload grocery items: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
    
    fun getGroceryItemCount(): Int {
        return _groceryItems.value.size
    }
} 