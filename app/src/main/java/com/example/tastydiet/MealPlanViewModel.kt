package com.example.tastydiet

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.tastydiet.AppDatabase
import com.example.tastydiet.data.models.GuestInfo
import com.example.tastydiet.data.models.MealLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MealPlanViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val guestInfoDao = db.guestInfoDao() // Implement this DAO if not present

    private val _guestCounts = MutableLiveData<Map<String, Int>>()
    val guestCounts: LiveData<Map<String, Int>> = _guestCounts

    private fun getToday(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

    fun setGuestCount(mealType: String, count: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val today = getToday()
            guestInfoDao.insertOrUpdateGuestCount(today, mealType, count)
            loadGuestCounts()
        }
    }

    fun getGuestCountForMeal(mealType: String): Int {
        return guestCounts.value?.get(mealType) ?: 0
    }

    fun getTotalPeopleForMeal(mealType: String, familyCount: Int): Int {
        return familyCount + getGuestCountForMeal(mealType)
    }

    fun loadGuestCounts() {
        viewModelScope.launch(Dispatchers.IO) {
            val today = getToday()
            val all = guestInfoDao.getGuestCountsForDate(today)
            val map = all.associate { it.mealTime to it.guestCount }
            _guestCounts.postValue(map)
        }
    }

    fun logMeal(
        mealType: String,
        recipeIds: List<Int>,
        calories: Float,
        protein: Float,
        carbs: Float,
        fat: Float,
        fiber: Float
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val log = MealLog(
                mealType = mealType,
                recipeIds = recipeIds.joinToString(","),
                calories = calories,
                protein = protein,
                carbs = carbs,
                fat = fat,
                fiber = fiber
            )
            db.mealLogDao().insert(log)
        }
    }
} 