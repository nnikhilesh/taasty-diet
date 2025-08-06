package com.example.tastydiet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tastydiet.AppDatabase
import com.example.tastydiet.data.FoodLogDao
import com.example.tastydiet.data.NutritionalInfoDao
import com.example.tastydiet.data.ProfileDao
import com.example.tastydiet.data.InventoryDao
import com.example.tastydiet.data.models.FoodLog
import com.example.tastydiet.data.models.NutritionalInfo
import com.example.tastydiet.data.models.Profile
import com.example.tastydiet.util.EnhancedMacroCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class EnhancedFoodLogViewModel(application: Application) : AndroidViewModel(application) {
    
    private val foodLogDao = AppDatabase.getInstance(application).foodLogDao()
    private val nutritionalInfoDao = AppDatabase.getInstance(application).nutritionalInfoDao()
    private val profileDao = AppDatabase.getInstance(application).profileDao()
    private val inventoryDao = AppDatabase.getInstance(application).inventoryDao()
    private val enhancedMacroCalculator = EnhancedMacroCalculator(nutritionalInfoDao)
    
    // State management
    private val _currentFoodLogs = MutableStateFlow<List<FoodLog>>(emptyList())
    val currentFoodLogs: StateFlow<List<FoodLog>> = _currentFoodLogs.asStateFlow()
    
    private val _selectedProfileId = MutableStateFlow<Int?>(null)
    val selectedProfileId: StateFlow<Int?> = _selectedProfileId.asStateFlow()
    
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _currentTotals = MutableStateFlow(TodayTotals())
    val currentTotals: StateFlow<TodayTotals> = _currentTotals.asStateFlow()
    
    private val _foodSuggestions = MutableStateFlow<List<NutritionalInfo>>(emptyList())
    val foodSuggestions: StateFlow<List<NutritionalInfo>> = _foodSuggestions.asStateFlow()
    
    private val _nutritionalPreview = MutableStateFlow<String?>(null)
    val nutritionalPreview: StateFlow<String?> = _nutritionalPreview.asStateFlow()
    
    // Database initialization status
    private val _isDatabaseInitialized = MutableStateFlow(false)
    val isDatabaseInitialized: StateFlow<Boolean> = _isDatabaseInitialized.asStateFlow()
    
    data class TodayTotals(
        val calories: Float = 0f,
        val protein: Float = 0f,
        val carbs: Float = 0f,
        val fat: Float = 0f,
        val fiber: Float = 0f
    )
    
    init {
        // Initialize database immediately
        initializeDatabase()
    }
    
    private fun initializeDatabase() {
        viewModelScope.launch {
            try {
                android.util.Log.d("EnhancedFoodLogViewModel", "=== STARTING DATABASE INITIALIZATION ===")
                _isLoading.value = true
                
                // Step 1: Check and initialize nutritional database
                val nutritionalCount = nutritionalInfoDao.getCount()
                android.util.Log.d("EnhancedFoodLogViewModel", "Nutritional data count: $nutritionalCount")
                
                if (nutritionalCount == 0) {
                    android.util.Log.d("EnhancedFoodLogViewModel", "Initializing nutritional database...")
                    val defaultData = com.example.tastydiet.data.NutritionalDatabase.getDefaultNutritionalData()
                    nutritionalInfoDao.insertAll(defaultData)
                    val newCount = nutritionalInfoDao.getCount()
                    android.util.Log.d("EnhancedFoodLogViewModel", "Nutritional database initialized with $newCount items")
                }
                
                // Step 2: Check and create default profiles
                val profileCount = profileDao.getProfileCount()
                android.util.Log.d("EnhancedFoodLogViewModel", "Profile count: $profileCount")
                
                if (profileCount == 0) {
                    android.util.Log.d("EnhancedFoodLogViewModel", "Creating default profiles...")
                    val defaultProfiles = listOf(
                        Profile(
                            name = "Default User",
                            age = 25,
                            gender = "Other",
                            weight = 70.0f,
                            height = 170.0f,
                            goal = "Maintenance",
                            targetCalories = 2000f,
                            targetProtein = 150f,
                            targetCarbs = 200f,
                            targetFat = 67f,
                            targetFiber = 25f,
                            activityLevel = 1.3f
                        )
                    )
                    defaultProfiles.forEach { profile ->
                        val profileId = profileDao.insertProfile(profile)
                        android.util.Log.d("EnhancedFoodLogViewModel", "Created profile: ${profile.name} with ID: $profileId")
                    }
                }
                
                // Step 3: Set initial profile if none selected
                if (_selectedProfileId.value == null) {
                    val profiles = profileDao.getAllProfiles().first()
                    if (profiles.isNotEmpty()) {
                        val firstProfile = profiles.first()
                        _selectedProfileId.value = firstProfile.id
                        android.util.Log.d("EnhancedFoodLogViewModel", "Auto-selected profile: ${firstProfile.name} (ID: ${firstProfile.id})")
                    }
                }
                
                _isDatabaseInitialized.value = true
                // Remove the success message to avoid cluttering the UI
                android.util.Log.d("EnhancedFoodLogViewModel", "=== DATABASE INITIALIZATION COMPLETE ===")
                
                // Step 4: Load initial data
                _selectedProfileId.value?.let { profileId ->
                    loadFoodLogsForCurrentDate(profileId)
                }
                
            } catch (e: Exception) {
                android.util.Log.e("EnhancedFoodLogViewModel", "Database initialization error: ${e.message}", e)
                _errorMessage.value = "Database initialization failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    
    // Force database re-initialization
    fun forceInitializeDatabase() {
        viewModelScope.launch {
            try {
                android.util.Log.d("EnhancedFoodLogViewModel", "=== FORCE INITIALIZING DATABASE ===")
                _isLoading.value = true
                
                // Clear existing data
                nutritionalInfoDao.deleteAll()
                // Note: We can't delete all profiles as we need at least one
                // Instead, we'll just reinitialize the nutritional data
                
                // Re-initialize
                initializeDatabase()
                
                // Remove the success message to avoid cluttering the UI
                android.util.Log.d("EnhancedFoodLogViewModel", "=== FORCE INITIALIZATION COMPLETE ===")
            } catch (e: Exception) {
                android.util.Log.e("EnhancedFoodLogViewModel", "Force initialization failed: ${e.message}", e)
                _errorMessage.value = "Force initialization failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Profile management - CRITICAL: This sets the profile and loads data
    fun setSelectedProfile(profileId: Int) {
        android.util.Log.d("EnhancedFoodLogViewModel", "Setting selected profile: $profileId")
        _selectedProfileId.value = profileId
        // Load data for current date with new profile
        loadFoodLogsForCurrentDate(profileId)
    }
    
    // Date management - CRITICAL: This changes the date and loads data
    fun setSelectedDate(date: LocalDate) {
        android.util.Log.d("EnhancedFoodLogViewModel", "Setting selected date: $date")
        _selectedDate.value = date
        // Load data for new date with current profile
        _selectedProfileId.value?.let { profileId ->
            loadFoodLogsForCurrentDate(profileId)
        }
    }
    
    // Load food logs for specific date and profile - CORE DATA LOADING
    fun loadFoodLogsForCurrentDate(profileId: Int) {
        val currentDate = _selectedDate.value
        android.util.Log.d("EnhancedFoodLogViewModel", "Loading food logs for profile: $profileId, date: $currentDate")
        
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val dateString = currentDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                android.util.Log.d("EnhancedFoodLogViewModel", "DEBUG: Querying for date string: '$dateString'")
                
                // Calculate timestamp range for the entire day
                val startOfDay = currentDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                val endOfDay = currentDate.plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
                
                android.util.Log.d("EnhancedFoodLogViewModel", "DEBUG: Date range - Start: $startOfDay, End: $endOfDay")
                
                // Get food logs for the specific date and profile using timestamp range
                val foodLogs = foodLogDao.getFoodLogsByDateRange(profileId, startOfDay, endOfDay).first()


                

                
                // Update state
                _currentFoodLogs.value = foodLogs
                calculateCurrentTotals(foodLogs)
                

                

            } catch (e: Exception) {
                android.util.Log.e("EnhancedFoodLogViewModel", "Error loading food logs: ${e.message}", e)
                _errorMessage.value = "Failed to load food logs: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Calculate totals for current food logs
    private fun calculateCurrentTotals(foodLogs: List<FoodLog>) {
        val totals = TodayTotals(
            calories = foodLogs.sumOf { it.getTotalCalories().toDouble() }.toFloat(),
            protein = foodLogs.sumOf { it.getTotalProtein().toDouble() }.toFloat(),
            carbs = foodLogs.sumOf { it.getTotalCarbs().toDouble() }.toFloat(),
            fat = foodLogs.sumOf { it.getTotalFat().toDouble() }.toFloat(),
            fiber = foodLogs.sumOf { it.fiber.toDouble() }.toFloat()
        )
        _currentTotals.value = totals
    }
    
    // Add a simple food log entry
    fun addFoodLog(foodLog: FoodLog) {
        viewModelScope.launch {
            try {
                foodLogDao.insertFoodLog(foodLog)
                android.util.Log.d("EnhancedFoodLogViewModel", "Added food log: ${foodLog.foodName}")
                // Reload data for current date and profile
                _selectedProfileId.value?.let { profileId ->
                    loadFoodLogsForCurrentDate(profileId)
                }
            } catch (e: Exception) {
                android.util.Log.e("EnhancedFoodLogViewModel", "Error adding food log: ${e.message}", e)
                _errorMessage.value = "Failed to add food log: ${e.message}"
            }
        }
    }
    
    // Add food log with nutritional data lookup
    fun addFoodLogWithNutritionalData(foodName: String, quantity: Float, unit: String, mealType: String, profileId: Int, selectedDate: java.time.LocalDate? = null) {
        viewModelScope.launch {
            try {

                _isLoading.value = true
                
                // Check if database is initialized
                if (!_isDatabaseInitialized.value) {
                    android.util.Log.w("EnhancedFoodLogViewModel", "Database not initialized, forcing initialization...")
                    forceInitializeDatabase()
                    delay(1000) // Wait for initialization
                }
                
                // Convert quantity to grams for macro calculation
                val quantityInGrams = convertToGrams(quantity, unit)
                
                // Calculate macros
                val macros = enhancedMacroCalculator.calculateMacrosForFood(foodName, quantityInGrams)
                
                if (macros != null) {
                    // Create timestamp for the selected date (not current time)
                    val dateToUse = selectedDate ?: _selectedDate.value
                    val timestamp = dateToUse.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                    
                    val foodLog = FoodLog(
                        profileId = profileId,
                        foodName = foodName,
                        mealType = mealType,
                        quantity = quantity,
                        unit = unit,
                        calories = macros.calories.toFloat(),
                        protein = macros.protein.toFloat(),
                        carbs = macros.carbs.toFloat(),
                        fat = macros.fat.toFloat(),
                        fiber = macros.fiber.toFloat(),
                        timestamp = timestamp
                    )
                    
                    // Save to database
                    foodLogDao.insertFoodLog(foodLog)
                    
                    // Reload data for current date and profile
                    loadFoodLogsForCurrentDate(profileId)
                } else {
                    android.util.Log.w("EnhancedFoodLogViewModel", "Food '$foodName' not found in database")
                    
                    // Create basic food log without nutritional data
                    val dateToUse = selectedDate ?: _selectedDate.value
                    val timestamp = dateToUse.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                    
                    val basicFoodLog = FoodLog(
                        profileId = profileId,
                        foodName = foodName,
                        mealType = mealType,
                        quantity = quantity,
                        unit = unit,
                        calories = 0f,
                        protein = 0f,
                        carbs = 0f,
                        fat = 0f,
                        fiber = 0f,
                        timestamp = timestamp
                    )
                    
                    val insertedId = foodLogDao.insertFoodLog(basicFoodLog)
                    android.util.Log.d("EnhancedFoodLogViewModel", "Basic food log saved with ID: $insertedId")
                    
                    // Add a small delay to ensure database transaction is committed
                    delay(100)
                    
                    // Verify the basic food log was saved
                    val allLogs = foodLogDao.getFoodLogsByProfile(profileId).first()
                    val savedLog = allLogs.find { it.id == insertedId.toInt() }
                    android.util.Log.d("EnhancedFoodLogViewModel", "Verification: Found saved basic log in database: ${savedLog != null}")
                    
                    loadFoodLogsForCurrentDate(profileId)
                    
                    _errorMessage.value = "Added $foodName (no nutritional data available)"
                }
            } catch (e: Exception) {
                android.util.Log.e("EnhancedFoodLogViewModel", "Error adding food log: ${e.message}", e)
                _errorMessage.value = "Failed to add food: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // Update food log - CORE UPDATE FUNCTION
    fun updateFoodLog(foodLog: FoodLog) {
        viewModelScope.launch {
            try {
                // Recalculate macros with new quantity and unit
                val quantityInGrams = convertToGrams(foodLog.quantity, foodLog.unit)
                val macros = enhancedMacroCalculator.calculateMacrosForFood(foodLog.foodName, quantityInGrams)
                
                if (macros != null) {
                    // Update with new macros
                    val updatedFoodLog = foodLog.copy(
                        calories = macros.calories.toFloat(),
                        protein = macros.protein.toFloat(),
                        carbs = macros.carbs.toFloat(),
                        fat = macros.fat.toFloat(),
                        fiber = macros.fiber.toFloat()
                    )
                    
                    // Save to database
                    foodLogDao.updateFoodLog(updatedFoodLog)
                    
                    // Reload data for current date and profile
                    _selectedProfileId.value?.let { profileId ->
                        loadFoodLogsForCurrentDate(profileId)
                    }
                } else {
                    // Update without macros (keep existing values)
                    foodLogDao.updateFoodLog(foodLog)
                    
                    // Reload data
                    _selectedProfileId.value?.let { profileId ->
                        loadFoodLogsForCurrentDate(profileId)
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("EnhancedFoodLogViewModel", "Error updating food log: ${e.message}", e)
                _errorMessage.value = "Failed to update food: ${e.message}"
            }
        }
    }
    
    // Delete food log - CORE DELETE FUNCTION
    fun deleteFoodLog(foodLog: FoodLog) {
        viewModelScope.launch {
            try {
                android.util.Log.d("EnhancedFoodLogViewModel", "Deleting food log: ${foodLog.foodName} with ID: ${foodLog.id}")
                
                // Delete from database
                foodLogDao.deleteFoodLog(foodLog)
                android.util.Log.d("EnhancedFoodLogViewModel", "Food log deleted successfully")
                
                // Reload data for current date and profile
                _selectedProfileId.value?.let { profileId ->
                    loadFoodLogsForCurrentDate(profileId)
                }
                
                _errorMessage.value = "Deleted $foodLog.foodName"
            } catch (e: Exception) {
                android.util.Log.e("EnhancedFoodLogViewModel", "Error deleting food log: ${e.message}", e)
                _errorMessage.value = "Failed to delete food: ${e.message}"
            }
        }
    }
    
    // Unit conversion helper
    private fun convertToGrams(quantity: Float, unit: String): Float {
        val convertedQuantity = when (unit.lowercase()) {
            "g", "gram", "grams" -> quantity
            "kg", "kilogram", "kilograms" -> quantity * 1000f
            "oz", "ounce", "ounces" -> quantity * 28.35f
            "lb", "pound", "pounds" -> quantity * 453.59f
            "cup", "cups" -> quantity * 240f
            "tbsp", "tablespoon", "tablespoons" -> quantity * 15f
            "tsp", "teaspoon", "teaspoons" -> quantity * 5f
            "piece", "pieces", "pc", "pcs" -> quantity * 100f
            "serving", "servings" -> quantity * 100f
            "ml", "milliliter", "milliliters" -> quantity
            else -> {
                android.util.Log.w("EnhancedFoodLogViewModel", "Unknown unit: '$unit', using quantity as-is")
                quantity
            }
        }
        
        return convertedQuantity
    }
    
    // Search functionality
    fun searchFood(query: String) {
        viewModelScope.launch {
            try {
                if (query.isNotEmpty()) {
                    val suggestions = enhancedMacroCalculator.searchFoodItems(query)
                    _foodSuggestions.value = suggestions
                    android.util.Log.d("EnhancedFoodLogViewModel", "Found ${suggestions.size} suggestions for '$query'")
                } else {
                    _foodSuggestions.value = emptyList()
                }
            } catch (e: Exception) {
                android.util.Log.e("EnhancedFoodLogViewModel", "Search error: ${e.message}", e)
                _foodSuggestions.value = emptyList()
            }
        }
    }
    
    // Get common foods for quick selection
    fun getCommonFoods() {
        viewModelScope.launch {
            try {
                val commonFoods = enhancedMacroCalculator.getCommonFoods()
                _foodSuggestions.value = commonFoods
                android.util.Log.d("EnhancedFoodLogViewModel", "Loaded ${commonFoods.size} common foods")
            } catch (e: Exception) {
                android.util.Log.e("EnhancedFoodLogViewModel", "Error loading common foods: ${e.message}", e)
                _foodSuggestions.value = emptyList()
            }
        }
    }
    
    // Nutritional preview
    fun getNutritionalPreview(foodName: String, quantity: Float) {
        viewModelScope.launch {
            try {
                val preview = enhancedMacroCalculator.getNutritionalPreview(foodName, quantity)
                _nutritionalPreview.value = preview
            } catch (e: Exception) {
                _nutritionalPreview.value = null
            }
        }
    }
    
    // Utility methods
    fun clearNutritionalPreview() {
        _nutritionalPreview.value = null
    }
    
    fun clearFoodSuggestions() {
        _foodSuggestions.value = emptyList()
    }
    
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
    

    
    // Get food logs by meal type for current date
    fun getFoodLogsByMealType(mealType: String): List<FoodLog> {
        return _currentFoodLogs.value.filter { it.mealType == mealType }
    }
    
    // Get total calories for meal type
    fun getTotalCaloriesForMeal(mealType: String): Float {
        return getFoodLogsByMealType(mealType)
            .sumOf { it.getTotalCalories().toDouble() }.toFloat()
    }
    
    // Check if food exists in database
    fun isFoodInDatabase(foodName: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val exists = enhancedMacroCalculator.isFoodInDatabase(foodName)
                onResult(exists)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }
}