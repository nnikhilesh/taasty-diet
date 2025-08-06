package com.example.tastydiet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tastydiet.data.FoodLogDao
import com.example.tastydiet.data.NutritionalInfoDao
import com.example.tastydiet.data.ProfileDao
import com.example.tastydiet.data.models.FoodLog
import com.example.tastydiet.data.models.NutritionalInfo
import com.example.tastydiet.data.models.Profile
import com.example.tastydiet.util.EnhancedMacroCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import android.util.Log

data class MacroTotals(
    val calories: Float = 0f,
    val protein: Float = 0f,
    val carbs: Float = 0f,
    val fat: Float = 0f,
    val fiber: Float = 0f
)

class SimpleFoodLogViewModel(
    private val foodLogDao: FoodLogDao,
    private val nutritionalInfoDao: NutritionalInfoDao,
    private val profileDao: ProfileDao
) : ViewModel() {

    // State
    private val _selectedProfile = MutableStateFlow<Profile?>(null)
    val selectedProfile: StateFlow<Profile?> = _selectedProfile.asStateFlow()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _foodLogs = MutableStateFlow<List<FoodLog>>(emptyList())
    val foodLogs: StateFlow<List<FoodLog>> = _foodLogs.asStateFlow()

    private val _totals = MutableStateFlow(MacroTotals())
    val totals: StateFlow<MacroTotals> = _totals.asStateFlow()

    private val _profiles = MutableStateFlow<List<Profile>>(emptyList())
    val profiles: StateFlow<List<Profile>> = _profiles.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val enhancedMacroCalculator = EnhancedMacroCalculator(nutritionalInfoDao)

    init {
        Log.d("SimpleFoodLogViewModel", "Initializing...")
        initializeDatabase()
        loadProfiles()
    }

    // 1. DATABASE INITIALIZATION
    private fun initializeDatabase() {
        viewModelScope.launch {
            try {
                Log.d("SimpleFoodLogViewModel", "Initializing database...")
                
                // Check if nutritional data exists
                val nutritionalCount = nutritionalInfoDao.getCount()
                if (nutritionalCount == 0) {
                    Log.d("SimpleFoodLogViewModel", "No nutritional data found, inserting default data...")
                    val defaultData = com.example.tastydiet.data.NutritionalDatabase.getDefaultNutritionalData()
                    nutritionalInfoDao.insertAll(defaultData)
                    Log.d("SimpleFoodLogViewModel", "Default nutritional data inserted")
                }

                // Check if profiles exist
                val profileCount = profileDao.getProfileCount()
                if (profileCount == 0) {
                    Log.d("SimpleFoodLogViewModel", "No profiles found, creating default profile...")
                    val defaultProfile = Profile(
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
                    val profileId = profileDao.insertProfile(defaultProfile)
                    Log.d("SimpleFoodLogViewModel", "Default profile created with ID: $profileId")
                }

                Log.d("SimpleFoodLogViewModel", "Database initialization complete")
            } catch (e: Exception) {
                Log.e("SimpleFoodLogViewModel", "Database initialization failed: ${e.message}", e)
                _message.value = "Database initialization failed: ${e.message}"
            }
        }
    }

    // 2. PROFILE MANAGEMENT
    private fun loadProfiles() {
        viewModelScope.launch {
            try {
                Log.d("SimpleFoodLogViewModel", "Loading profiles...")
                profileDao.getAllProfiles().first().let { profiles ->
                    _profiles.value = profiles
                    
                    if (profiles.isNotEmpty() && _selectedProfile.value == null) {
                        _selectedProfile.value = profiles.first()
                        Log.d("SimpleFoodLogViewModel", "Auto-selected profile: ${profiles.first().name}")
                        loadFoodLogsForCurrentDate()
                    }
                    
                    Log.d("SimpleFoodLogViewModel", "Loaded ${profiles.size} profiles")
                }
            } catch (e: Exception) {
                Log.e("SimpleFoodLogViewModel", "Failed to load profiles: ${e.message}", e)
                _message.value = "Failed to load profiles: ${e.message}"
            }
        }
    }

    fun setSelectedProfile(profile: Profile) {
        Log.d("SimpleFoodLogViewModel", "Setting selected profile: ${profile.name}")
        _selectedProfile.value = profile
        loadFoodLogsForCurrentDate()
    }

    // 3. DATE MANAGEMENT
    fun setSelectedDate(date: LocalDate) {
        Log.d("SimpleFoodLogViewModel", "Setting selected date: $date")
        _selectedDate.value = date
        loadFoodLogsForCurrentDate()
    }

    // 4. FOOD LOGGING - CORE FUNCTION
    fun logFood(foodName: String, quantity: Float, unit: String, mealType: String) {
        val profile = _selectedProfile.value
        if (profile == null) {
            _message.value = "Please select a profile first"
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d("SimpleFoodLogViewModel", "Logging food: $foodName, $quantity $unit, $mealType")

                // Calculate macros
                val quantityInGrams = convertToGrams(quantity, unit)
                val macros = enhancedMacroCalculator.calculateMacrosForFood(foodName, quantityInGrams)

                if (macros == null) {
                    _message.value = "Food '$foodName' not found in database"
                    return@launch
                }

                // Create timestamp for selected date
                val selectedDate = _selectedDate.value
                val timestamp = selectedDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()

                val foodLog = FoodLog(
                    profileId = profile.id,
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

                Log.d("SimpleFoodLogViewModel", "Created food log: ${foodLog.foodName}, calories: ${foodLog.calories}, timestamp: $timestamp")

                // Save to database
                val insertedId = foodLogDao.insertFoodLog(foodLog)
                Log.d("SimpleFoodLogViewModel", "Food log saved with ID: $insertedId")

                // Reload data
                loadFoodLogsForCurrentDate()

                _message.value = "Added $foodName: ${macros.calories.toInt()} cal, ${macros.protein.toInt()}g protein"
                Log.d("SimpleFoodLogViewModel", "Food logging successful")

            } catch (e: Exception) {
                Log.e("SimpleFoodLogViewModel", "Failed to log food: ${e.message}", e)
                _message.value = "Failed to log food: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 5. DATA LOADING
    private fun loadFoodLogsForCurrentDate() {
        val profile = _selectedProfile.value
        if (profile == null) return

        viewModelScope.launch {
            try {
                _isLoading.value = true
                val currentDate = _selectedDate.value
                val startOfDay = currentDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                val endOfDay = currentDate.plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
                
                Log.d("SimpleFoodLogViewModel", "Loading food logs for profile ${profile.id}, date: $currentDate")

                foodLogDao.getFoodLogsByDateRange(profile.id, startOfDay, endOfDay).first().let { foodLogs ->
                    _foodLogs.value = foodLogs

                    // Calculate totals
                    val totals = MacroTotals(
                        calories = foodLogs.sumOf { it.getTotalCalories().toDouble() }.toFloat(),
                        protein = foodLogs.sumOf { it.getTotalProtein().toDouble() }.toFloat(),
                        carbs = foodLogs.sumOf { it.getTotalCarbs().toDouble() }.toFloat(),
                        fat = foodLogs.sumOf { it.getTotalFat().toDouble() }.toFloat(),
                        fiber = foodLogs.sumOf { it.fiber.toDouble() }.toFloat()
                    )
                    _totals.value = totals

                    Log.d("SimpleFoodLogViewModel", "Loaded ${foodLogs.size} food logs, totals: ${totals.calories} cal, ${totals.protein}g protein")
                    _message.value = "Loaded ${foodLogs.size} food logs for $currentDate"
                }

            } catch (e: Exception) {
                Log.e("SimpleFoodLogViewModel", "Failed to load food logs: ${e.message}", e)
                _message.value = "Failed to load food logs: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 6. UPDATE AND DELETE
    fun updateFoodLog(foodLog: FoodLog) {
        viewModelScope.launch {
            try {
                Log.d("SimpleFoodLogViewModel", "Updating food log: ${foodLog.foodName}")
                
                // Recalculate macros
                val quantityInGrams = convertToGrams(foodLog.quantity, foodLog.unit)
                val macros = enhancedMacroCalculator.calculateMacrosForFood(foodLog.foodName, quantityInGrams)

                if (macros != null) {
                    val updatedFoodLog = foodLog.copy(
                        calories = macros.calories.toFloat(),
                        protein = macros.protein.toFloat(),
                        carbs = macros.carbs.toFloat(),
                        fat = macros.fat.toFloat(),
                        fiber = macros.fiber.toFloat()
                    )
                    foodLogDao.updateFoodLog(updatedFoodLog)
                } else {
                    foodLogDao.updateFoodLog(foodLog)
                }

                loadFoodLogsForCurrentDate()
                _message.value = "Updated ${foodLog.foodName}"

            } catch (e: Exception) {
                Log.e("SimpleFoodLogViewModel", "Failed to update food log: ${e.message}", e)
                _message.value = "Failed to update food log: ${e.message}"
            }
        }
    }

    fun deleteFoodLog(foodLog: FoodLog) {
        viewModelScope.launch {
            try {
                Log.d("SimpleFoodLogViewModel", "Deleting food log: ${foodLog.foodName}")
                foodLogDao.deleteFoodLog(foodLog)
                loadFoodLogsForCurrentDate()
                _message.value = "Deleted ${foodLog.foodName}"

            } catch (e: Exception) {
                Log.e("SimpleFoodLogViewModel", "Failed to delete food log: ${e.message}", e)
                _message.value = "Failed to delete food log: ${e.message}"
            }
        }
    }

    // 7. UTILITY FUNCTIONS
    private fun convertToGrams(quantity: Float, unit: String): Float {
        return when (unit.lowercase()) {
            "g", "grams" -> quantity
            "pieces", "piece" -> quantity * 100f // Assume 100g per piece
            "cups", "cup" -> quantity * 240f // 240g per cup
            "tbsp", "tablespoon" -> quantity * 15f // 15g per tbsp
            "tsp", "teaspoon" -> quantity * 5f // 5g per tsp
            "ml", "milliliters" -> quantity * 1f // 1g per ml (approximate)
            "oz", "ounces" -> quantity * 28.35f // 28.35g per oz
            else -> quantity // Default to grams
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    suspend fun searchFood(query: String): List<NutritionalInfo> {
        return enhancedMacroCalculator.searchFoodItems(query)
    }
} 