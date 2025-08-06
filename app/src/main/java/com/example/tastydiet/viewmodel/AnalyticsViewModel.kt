package com.example.tastydiet.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tastydiet.AppDatabase
import com.example.tastydiet.data.models.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {
    private val analyticsDao = AppDatabase.getInstance(application).analyticsDao()
    private val foodLogDao = AppDatabase.getInstance(application).foodLogDao()
    
    private val _selectedProfileId = MutableStateFlow<Int?>(null)
    val selectedProfileId: StateFlow<Int?> = _selectedProfileId.asStateFlow()
    
    private val _dateRange = MutableStateFlow("week") // "week", "month", "custom"
    val dateRange: StateFlow<String> = _dateRange.asStateFlow()
    
    private val _customStartDate = MutableStateFlow<String>("")
    val customStartDate: StateFlow<String> = _customStartDate.asStateFlow()
    
    private val _customEndDate = MutableStateFlow<String>("")
    val customEndDate: StateFlow<String> = _customEndDate.asStateFlow()
    
    private val _analyticsData = MutableStateFlow<List<AnalyticsData>>(emptyList())
    val analyticsData: StateFlow<List<AnalyticsData>> = _analyticsData.asStateFlow()
    
    private val _weeklyAnalytics = MutableStateFlow<WeeklyAnalytics?>(null)
    val weeklyAnalytics: StateFlow<WeeklyAnalytics?> = _weeklyAnalytics.asStateFlow()
    
    private val _monthlyAnalytics = MutableStateFlow<MonthlyAnalytics?>(null)
    val monthlyAnalytics: StateFlow<MonthlyAnalytics?> = _monthlyAnalytics.asStateFlow()
    
    private val _progressSummary = MutableStateFlow<ProgressSummary?>(null)
    val progressSummary: StateFlow<ProgressSummary?> = _progressSummary.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    fun setSelectedProfile(profileId: Int) {
        _selectedProfileId.value = profileId
        loadAnalytics()
    }
    
    fun setDateRange(range: String) {
        _dateRange.value = range
        loadAnalytics()
    }
    
    fun setCustomDateRange(startDate: String, endDate: String) {
        _customStartDate.value = startDate
        _customEndDate.value = endDate
        _dateRange.value = "custom"
        loadAnalytics()
    }
    
    private fun loadAnalytics() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val profileId = _selectedProfileId.value ?: return@launch
                
                val (startDate, endDate) = getDateRange()
                val data = analyticsDao.getAnalyticsByDateRange(profileId, startDate, endDate).first()
                _analyticsData.value = data
                
                when (_dateRange.value) {
                    "week" -> calculateWeeklyAnalytics(data, startDate, endDate)
                    "month" -> calculateMonthlyAnalytics(data, startDate, endDate)
                    "custom" -> calculateCustomAnalytics(data, startDate, endDate)
                }
                
                loadProgressSummary(profileId)
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load analytics: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    private fun getDateRange(): Pair<String, String> {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        return when (_dateRange.value) {
            "week" -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                val startDate = dateFormat.format(calendar.time)
                calendar.add(Calendar.DAY_OF_YEAR, 7)
                val endDate = dateFormat.format(calendar.time)
                Pair(startDate, endDate)
            }
            "month" -> {
                calendar.add(Calendar.MONTH, -1)
                val startDate = dateFormat.format(calendar.time)
                calendar.add(Calendar.MONTH, 1)
                val endDate = dateFormat.format(calendar.time)
                Pair(startDate, endDate)
            }
            "custom" -> {
                Pair(_customStartDate.value, _customEndDate.value)
            }
            else -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                val startDate = dateFormat.format(calendar.time)
                calendar.add(Calendar.DAY_OF_YEAR, 7)
                val endDate = dateFormat.format(calendar.time)
                Pair(startDate, endDate)
            }
        }
    }
    
    private fun calculateWeeklyAnalytics(data: List<AnalyticsData>, startDate: String, endDate: String) {
        if (data.isEmpty()) {
            _weeklyAnalytics.value = null
            return
        }
        
        val averageCalories = data.map { it.totalCalories }.average().toFloat()
        val averageProtein = data.map { it.totalProtein }.average().toFloat()
        val averageCarbs = data.map { it.totalCarbs }.average().toFloat()
        val averageFat = data.map { it.totalFat }.average().toFloat()
        val totalMeals = data.sumOf { it.mealCount }
        
        _weeklyAnalytics.value = WeeklyAnalytics(
            weekStart = startDate,
            weekEnd = endDate,
            dailyData = data,
            averageCalories = averageCalories,
            averageProtein = averageProtein,
            averageCarbs = averageCarbs,
            averageFat = averageFat,
            totalMeals = totalMeals
        )
    }
    
    private fun calculateMonthlyAnalytics(data: List<AnalyticsData>, startDate: String, endDate: String) {
        if (data.isEmpty()) {
            _monthlyAnalytics.value = null
            return
        }
        
        val totalCalories = data.sumOf { it.totalCalories.toDouble() }.toFloat()
        val totalProtein = data.sumOf { it.totalProtein.toDouble() }.toFloat()
        val totalCarbs = data.sumOf { it.totalCarbs.toDouble() }.toFloat()
        val totalFat = data.sumOf { it.totalFat.toDouble() }.toFloat()
        
        val daysCount = data.size
        val averageCaloriesPerDay = if (daysCount > 0) totalCalories / daysCount else 0f
        val averageProteinPerDay = if (daysCount > 0) totalProtein / daysCount else 0f
        val averageCarbsPerDay = if (daysCount > 0) totalCarbs / daysCount else 0f
        val averageFatPerDay = if (daysCount > 0) totalFat / daysCount else 0f
        
        // Group data by weeks
        val weeklyData = data.chunked(7).map { weekData ->
            val weekStart = weekData.firstOrNull()?.date ?: startDate
            val weekEnd = weekData.lastOrNull()?.date ?: endDate
            val weekAvgCalories = weekData.map { it.totalCalories }.average().toFloat()
            val weekAvgProtein = weekData.map { it.totalProtein }.average().toFloat()
            val weekAvgCarbs = weekData.map { it.totalCarbs }.average().toFloat()
            val weekAvgFat = weekData.map { it.totalFat }.average().toFloat()
            val weekTotalMeals = weekData.sumOf { it.mealCount }
            
            WeeklyAnalytics(
                weekStart = weekStart,
                weekEnd = weekEnd,
                dailyData = weekData,
                averageCalories = weekAvgCalories,
                averageProtein = weekAvgProtein,
                averageCarbs = weekAvgCarbs,
                averageFat = weekAvgFat,
                totalMeals = weekTotalMeals
            )
        }
        
        _monthlyAnalytics.value = MonthlyAnalytics(
            month = startDate.substring(0, 7), // YYYY-MM format
            weeklyData = weeklyData,
            totalCalories = totalCalories,
            totalProtein = totalProtein,
            totalCarbs = totalCarbs,
            totalFat = totalFat,
            averageCaloriesPerDay = averageCaloriesPerDay,
            averageProteinPerDay = averageProteinPerDay,
            averageCarbsPerDay = averageCarbsPerDay,
            averageFatPerDay = averageFatPerDay
        )
    }
    
    private fun calculateCustomAnalytics(data: List<AnalyticsData>, startDate: String, endDate: String) {
        // For custom date range, use the same logic as weekly
        calculateWeeklyAnalytics(data, startDate, endDate)
    }
    
    private fun loadProgressSummary(profileId: Int) {
        viewModelScope.launch {
            try {
                // This would need to be implemented with actual profile and food log data
                // For now, creating a placeholder
                _progressSummary.value = ProgressSummary(
                    currentBMI = 22.5f,
                    bmiCategory = "Normal weight",
                    weightTrend = "Stable",
                    topFoods = listOf(
                        TopFood("Rice", 15, 1200f),
                        TopFood("Dal", 12, 800f),
                        TopFood("Vegetables", 20, 600f),
                        TopFood("Roti", 18, 900f),
                        TopFood("Curd", 10, 400f)
                    ),
                    skippedMeals = listOf(
                        SkippedMeal("Breakfast", "2024-01-15", 2),
                        SkippedMeal("Lunch", "2024-01-16", 1)
                    ),
                    averageCaloriesPerDay = 1800f,
                    daysLogged = 30
                )
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load progress summary: ${e.message}"
            }
        }
    }
    
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
    
    fun generateDailyAnalytics(profileId: Int, date: String) {
        viewModelScope.launch {
            try {
                val foodLogs = foodLogDao.getTodayFoodLogs(profileId).first()
                
                val totalCalories = foodLogs.sumOf { it.getTotalCalories().toDouble() }.toFloat()
                val totalProtein = foodLogs.sumOf { it.getTotalProtein().toDouble() }.toFloat()
                val totalCarbs = foodLogs.sumOf { it.getTotalCarbs().toDouble() }.toFloat()
                val totalFat = foodLogs.sumOf { it.getTotalFat().toDouble() }.toFloat()
                val totalFiber = 0f // Would need to be calculated from food logs
                val mealCount = foodLogs.size
                
                val analyticsData = AnalyticsData(
                    profileId = profileId,
                    date = date,
                    totalCalories = totalCalories,
                    totalProtein = totalProtein,
                    totalCarbs = totalCarbs,
                    totalFat = totalFat,
                    totalFiber = totalFiber,
                    mealCount = mealCount
                )
                
                analyticsDao.insertAnalytics(analyticsData)
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to generate daily analytics: ${e.message}"
            }
        }
    }
} 