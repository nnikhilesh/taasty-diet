package com.example.tastydiet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tastydiet.AppDatabase
import com.example.tastydiet.data.NutritionalDatabase
import com.example.tastydiet.data.models.MacroResult
import com.example.tastydiet.data.models.MacroRecommendations
import com.example.tastydiet.data.models.MacroProgress
import com.example.tastydiet.data.models.Profile
import com.example.tastydiet.data.models.NutritionalInfo
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class FoodMacroViewModel(application: Application) : AndroidViewModel(application) {
    private val nutritionalInfoDao = AppDatabase.getInstance(application).nutritionalInfoDao()
    private val profileDao = AppDatabase.getInstance(application).profileDao()
    
    // State flows
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _selectedFoodItem = MutableStateFlow<NutritionalInfo?>(null)
    val selectedFoodItem: StateFlow<NutritionalInfo?> = _selectedFoodItem.asStateFlow()
    
    private val _quantity = MutableStateFlow(1f)
    val quantity: StateFlow<Float> = _quantity.asStateFlow()
    
    private val _macroResult = MutableStateFlow<MacroResult?>(null)
    val macroResult: StateFlow<MacroResult?> = _macroResult.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()
    
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()
    
    // Profile flows
    val currentProfile: StateFlow<Profile?> = profileDao.getAllProfiles()
        .map { profiles -> profiles.firstOrNull() }
        .stateIn(viewModelScope, SharingStarted.Lazily, null)
    
    // Food items flows
    val allFoodItems: StateFlow<List<NutritionalInfo>> = nutritionalInfoDao.getAll()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val foodCategories: StateFlow<List<String>> = nutritionalInfoDao.getAll()
        .map { items -> items.map { it.category }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val searchResults: StateFlow<List<NutritionalInfo>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                nutritionalInfoDao.getAll()
            } else {
                flow { emit(nutritionalInfoDao.searchByName(query)) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val filteredFoodItems: StateFlow<List<NutritionalInfo>> = combine(
        searchResults,
        _selectedCategory
    ) { items, category ->
        if (category == null || category == "All") {
            items
        } else {
            items.filter { it.category == category }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    init {
        loadComprehensiveDatabaseIfNeeded()
    }
    
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
    
    fun setSelectedCategory(category: String?) {
        _selectedCategory.value = category
    }
    
    fun selectFoodItem(foodItem: NutritionalInfo) {
        _selectedFoodItem.value = foodItem
        calculateMacros()
    }
    
    fun setQuantity(quantity: Float) {
        _quantity.value = quantity
        calculateMacros()
    }
    
    fun clearSelection() {
        _selectedFoodItem.value = null
        _macroResult.value = null
        _quantity.value = 1f
    }
    
    private fun calculateMacros() {
        val foodItem = _selectedFoodItem.value
        val quantity = _quantity.value
        
        if (foodItem != null) {
            val multiplier = quantity / 100f
            val result = MacroResult(
                calories = foodItem.caloriesPer100g * multiplier,
                protein = foodItem.proteinPer100g * multiplier,
                carbs = foodItem.carbsPer100g * multiplier,
                fat = foodItem.fatPer100g * multiplier,
                fiber = foodItem.fiberPer100g * multiplier
            )
            _macroResult.value = result
        } else {
            _macroResult.value = null
        }
    }
    
    fun addFoodItem(foodItem: NutritionalInfo) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                nutritionalInfoDao.insert(foodItem)
                _errorMessage.value = "Food item added successfully"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add food item: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun updateFoodItem(foodItem: NutritionalInfo) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                nutritionalInfoDao.insert(foodItem) // insert with REPLACE strategy will update if exists
                _errorMessage.value = "Food item updated successfully"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update food item: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun deleteFoodItem(foodItem: NutritionalInfo) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                nutritionalInfoDao.delete(foodItem)
                _errorMessage.value = "Food item deleted successfully"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete food item: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
    
    fun importComprehensiveDatabase() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val defaultData = NutritionalDatabase.getDefaultNutritionalData()
                nutritionalInfoDao.insertAll(defaultData)
                _errorMessage.value = "Successfully imported ${defaultData.size} food items!"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to import database: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun getDatabaseStats(): ImportStats? {
        return try {
            runBlocking {
                val totalItems = nutritionalInfoDao.getAll().first().size
                val categories = nutritionalInfoDao.getAll().first().map { it.category }.distinct().size
                ImportStats(totalItems, categories)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    fun getPersonalizedMacroRecommendations(): MacroRecommendations? {
        val profile = currentProfile.value ?: return null
        
        return MacroRecommendations(
            dailyCalories = profile.targetCalories,
            dailyProtein = profile.targetProtein,
            dailyCarbs = profile.targetCarbs,
            dailyFat = profile.targetFat,
            bmi = profile.bmi,
            bmiCategory = profile.bmiCategory
        )
    }
    
    fun getMacroProgress(macroResult: MacroResult): MacroProgress {
        val recommendations = getPersonalizedMacroRecommendations()
        
        return if (recommendations != null) {
            MacroProgress(
                caloriesProgress = (macroResult.calories / recommendations.dailyCalories) * 100,
                proteinProgress = (macroResult.protein / recommendations.dailyProtein) * 100,
                carbsProgress = (macroResult.carbs / recommendations.dailyCarbs) * 100,
                fatProgress = (macroResult.fat / recommendations.dailyFat) * 100
            )
        } else {
            MacroProgress(0f, 0f, 0f, 0f)
        }
    }
    
    private fun loadComprehensiveDatabaseIfNeeded() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Check if database already has comprehensive data
                val existingCount = nutritionalInfoDao.getAll().first().size
                
                if (existingCount < 100) { // If less than 100 items, load comprehensive database
                    val comprehensiveData = NutritionalDatabase.loadComprehensiveDatabase(getApplication())
                    nutritionalInfoDao.insertAll(comprehensiveData)
                    _errorMessage.value = "Successfully loaded ${comprehensiveData.size} food items from comprehensive database!"
                } else {
                    _errorMessage.value = "Database ready: $existingCount food items available"
                }
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load comprehensive database: ${e.message}"
                // Fallback to default data
                val defaultData = NutritionalDatabase.getDefaultNutritionalData()
                nutritionalInfoDao.insertAll(defaultData)
                _errorMessage.value = "Loaded ${defaultData.size} default food items"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private suspend fun loadSampleFoodItems() {
        val sampleFoodItems = listOf(
            // Grains
            NutritionalInfo(
                name = "Rice (Cooked)",
                category = "Grains",
                caloriesPer100g = 130f,
                proteinPer100g = 0.3f,
                carbsPer100g = 28f,
                fatPer100g = 0.3f,
                fiberPer100g = 1f
            ),
            NutritionalInfo(
                name = "Bread (Whole Wheat)",
                category = "Grains",
                caloriesPer100g = 250f,
                proteinPer100g = 0.9f,
                carbsPer100g = 47f,
                fatPer100g = 3f,
                fiberPer100g = 7f
            ),
            
            // Proteins
            NutritionalInfo(
                name = "Egg (Whole)",
                category = "Proteins",
                caloriesPer100g = 70f,
                proteinPer100g = 6.3f,
                carbsPer100g = 0.4f,
                fatPer100g = 5f,
                fiberPer100g = 0f
            ),
            NutritionalInfo(
                name = "Chicken Breast (Cooked)",
                category = "Proteins",
                caloriesPer100g = 165f,
                proteinPer100g = 3.1f,
                carbsPer100g = 0f,
                fatPer100g = 4f,
                fiberPer100g = 0f
            ),
            
            // Dairy
            NutritionalInfo(
                name = "Milk (Whole)",
                category = "Dairy",
                caloriesPer100g = 61f,
                proteinPer100g = 0.32f,
                carbsPer100g = 4.7f,
                fatPer100g = 3.3f,
                fiberPer100g = 0f
            ),
            NutritionalInfo(
                name = "Yogurt (Plain)",
                category = "Dairy",
                caloriesPer100g = 59f,
                proteinPer100g = 0.35f,
                carbsPer100g = 3.5f,
                fatPer100g = 3.2f,
                fiberPer100g = 0f
            ),
            
            // Vegetables
            NutritionalInfo(
                name = "Broccoli (Raw)",
                category = "Vegetables",
                caloriesPer100g = 34f,
                proteinPer100g = 0.28f,
                carbsPer100g = 7f,
                fatPer100g = 0.4f,
                fiberPer100g = 2.6f
            ),
            NutritionalInfo(
                name = "Spinach (Raw)",
                category = "Vegetables",
                caloriesPer100g = 23f,
                proteinPer100g = 0.29f,
                carbsPer100g = 3.7f,
                fatPer100g = 0.4f,
                fiberPer100g = 2.2f
            ),
            
            // Fruits
            NutritionalInfo(
                name = "Apple (Raw)",
                category = "Fruits",
                caloriesPer100g = 52f,
                proteinPer100g = 0.03f,
                carbsPer100g = 14f,
                fatPer100g = 0.2f,
                fiberPer100g = 2.4f
            ),
            NutritionalInfo(
                name = "Banana (Raw)",
                category = "Fruits",
                caloriesPer100g = 89f,
                proteinPer100g = 0.11f,
                carbsPer100g = 23f,
                fatPer100g = 0.3f,
                fiberPer100g = 2.6f
            ),
            
            // Nuts & Seeds
            NutritionalInfo(
                name = "Almonds (Raw)",
                category = "Nuts & Seeds",
                caloriesPer100g = 580f,
                proteinPer100g = 21f,
                carbsPer100g = 22f,
                fatPer100g = 50f,
                fiberPer100g = 12f
            ),
            NutritionalInfo(
                name = "Peanut Butter",
                category = "Nuts & Seeds",
                caloriesPer100g = 590f,
                proteinPer100g = 25f,
                carbsPer100g = 20f,
                fatPer100g = 50f,
                fiberPer100g = 6f
            )
        )
        
        try {
            nutritionalInfoDao.insertAll(sampleFoodItems)
        } catch (e: Exception) {
            _errorMessage.value = "Failed to load sample data: ${e.message}"
        }
    }
    
    data class ImportStats(
        val totalItems: Int,
        val totalCategories: Int
    )
    
    // Macro Calculator specific methods
    fun searchFoodItems(query: String) {
        _searchQuery.value = query
    }
    
    fun reloadComprehensiveDatabase() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Clear existing data and reload comprehensive database
                nutritionalInfoDao.deleteAll()
                val comprehensiveData = NutritionalDatabase.loadComprehensiveDatabase(getApplication())
                nutritionalInfoDao.insertAll(comprehensiveData)
                _errorMessage.value = "Successfully reloaded ${comprehensiveData.size} food items from comprehensive database!"
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to reload comprehensive database: ${e.message}"
                // Fallback to default data
                val defaultData = NutritionalDatabase.getDefaultNutritionalData()
                nutritionalInfoDao.insertAll(defaultData)
                _errorMessage.value = "Loaded ${defaultData.size} default food items"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun addFoodItemToLog(foodItem: NutritionalInfo, quantity: Float) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Calculate macros for the given quantity
                val calories = (foodItem.caloriesPer100g * quantity) / 100f
                val protein = (foodItem.proteinPer100g * quantity) / 100f
                val carbs = (foodItem.carbsPer100g * quantity) / 100f
                val fat = (foodItem.fatPer100g * quantity) / 100f
                val fiber = (foodItem.fiberPer100g * quantity) / 100f
                
                // Create a macro result
                val macroResult = MacroResult(
                    calories = calories,
                    protein = protein,
                    carbs = carbs,
                    fat = fat,
                    fiber = fiber
                )
                
                _macroResult.value = macroResult
                _errorMessage.value = "Added ${foodItem.name} (${quantity}g) to food log"
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add food item: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // UI State for Macro Calculator
    data class MacroCalculatorUiState(
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val selectedFoodItem: NutritionalInfo? = null,
        val quantity: Float = 100f,
        val macroResult: MacroResult? = null
    )
    
    val uiState: StateFlow<MacroCalculatorUiState> = combine(
        _isLoading,
        _errorMessage,
        _selectedFoodItem,
        _quantity,
        _macroResult
    ) { isLoading, errorMessage, selectedFoodItem, quantity, macroResult ->
        MacroCalculatorUiState(
            isLoading = isLoading,
            errorMessage = errorMessage,
            selectedFoodItem = selectedFoodItem,
            quantity = quantity,
            macroResult = macroResult
        )
    }.stateIn(viewModelScope, SharingStarted.Lazily, MacroCalculatorUiState())
} 