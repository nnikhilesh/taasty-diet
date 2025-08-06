package com.example.tastydiet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tastydiet.AppDatabase
import com.example.tastydiet.data.models.FoodLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class FoodLogViewModel(application: Application) : AndroidViewModel(application) {
    private val foodLogDao = AppDatabase.getInstance(application).foodLogDao()
    
    private val _todayFoodLogs = MutableStateFlow<List<FoodLog>>(emptyList())
    val todayFoodLogs: StateFlow<List<FoodLog>> = _todayFoodLogs.asStateFlow()
    
    private val _selectedProfileId = MutableStateFlow<Int?>(null)
    val selectedProfileId: StateFlow<Int?> = _selectedProfileId.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _todayTotals = MutableStateFlow(TodayTotals())
    val todayTotals: StateFlow<TodayTotals> = _todayTotals.asStateFlow()
    
    data class TodayTotals(
        val calories: Float = 0f,
        val protein: Float = 0f,
        val carbs: Float = 0f,
        val fat: Float = 0f
    )
    
    fun setSelectedProfile(profileId: Int) {
        _selectedProfileId.value = profileId
        loadTodayFoodLogs(profileId)
    }
    
    fun loadTodayFoodLogs(profileId: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                foodLogDao.getTodayFoodLogs(profileId).collect { foodLogs ->
                    _todayFoodLogs.value = foodLogs
                    calculateTodayTotals(foodLogs)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load food logs: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun calculateTodayTotals(foodLogs: List<FoodLog>) {
        val totals = TodayTotals(
            calories = foodLogs.sumOf { it.getTotalCalories().toDouble() }.toFloat(),
            protein = foodLogs.sumOf { it.getTotalProtein().toDouble() }.toFloat(),
            carbs = foodLogs.sumOf { it.getTotalCarbs().toDouble() }.toFloat(),
            fat = foodLogs.sumOf { it.getTotalFat().toDouble() }.toFloat()
        )
        _todayTotals.value = totals
    }
    
    fun addFoodLog(foodLog: FoodLog) {
        viewModelScope.launch {
            try {
                // Validate food log data
                if (foodLog.foodName.isBlank()) {
                    _errorMessage.value = "Food name cannot be empty"
                    return@launch
                }
                
                if (foodLog.quantity <= 0) {
                    _errorMessage.value = "Quantity must be greater than 0"
                    return@launch
                }
                
                if (foodLog.calories < 0) {
                    _errorMessage.value = "Calories cannot be negative"
                    return@launch
                }
                
                foodLogDao.insertFoodLog(foodLog)
                _errorMessage.value = "Food logged successfully"
                // Reload today's logs
                _selectedProfileId.value?.let { profileId ->
                    loadTodayFoodLogs(profileId)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to log food: ${e.message}"
            }
        }
    }
    
    fun deleteFoodLog(foodLog: FoodLog) {
        viewModelScope.launch {
            try {
                foodLogDao.deleteFoodLog(foodLog)
                _errorMessage.value = "Food log deleted successfully"
                // Reload today's logs
                _selectedProfileId.value?.let { profileId ->
                    loadTodayFoodLogs(profileId)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete food log: ${e.message}"
            }
        }
    }
    
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
    
    fun setErrorMessage(message: String) {
        _errorMessage.value = message
    }
    

    
    fun getFoodLogsByMealType(profileId: Int, mealType: String): List<FoodLog> {
        return _todayFoodLogs.value.filter { it.mealType == mealType }
    }
    
    fun getTotalCaloriesForMeal(profileId: Int, mealType: String): Float {
        return getFoodLogsByMealType(profileId, mealType)
            .sumOf { it.getTotalCalories().toDouble() }.toFloat()
    }
} 