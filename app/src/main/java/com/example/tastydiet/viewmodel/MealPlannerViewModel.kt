package com.example.tastydiet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.tastydiet.data.models.MealPlan
import com.example.tastydiet.data.MealPlanDao
import java.util.Date

class MealPlannerViewModel(
    private val mealPlanDao: MealPlanDao? = null // Provide DAO via DI or manual injection
) : ViewModel() {
    private val _mealPlans = MutableStateFlow<List<MealPlan>>(emptyList())
    val mealPlans: StateFlow<List<MealPlan>> = _mealPlans.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadMealPlans() {
        viewModelScope.launch {
            _loading.value = true
            try {
                _mealPlans.value = mealPlanDao?.getAll() ?: emptyList()
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _loading.value = false
            }
        }
    }

    fun getMealPlanForDate(date: String): MealPlan? {
        return _mealPlans.value.find { it.date == date }
    }

    fun addMealPlan(mealPlan: MealPlan) {
        viewModelScope.launch {
            try {
                mealPlanDao?.insert(mealPlan)
                loadMealPlans()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun updateMealPlan(mealPlan: MealPlan) {
        viewModelScope.launch {
            try {
                mealPlanDao?.update(mealPlan)
                loadMealPlans()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun deleteMealPlan(mealPlan: MealPlan) {
        viewModelScope.launch {
            try {
                mealPlanDao?.delete(mealPlan)
                loadMealPlans()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}

