package com.example.tastydiet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tastydiet.data.models.Profile
import com.example.tastydiet.util.RemainingMacroCalculator
import com.example.tastydiet.util.RemainingMacros
import com.example.tastydiet.util.DinnerSuggestion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RemainingMacroViewModel(
    private val remainingMacroCalculator: RemainingMacroCalculator
) : ViewModel() {
    
    private val _remainingMacros = MutableStateFlow<RemainingMacros?>(null)
    val remainingMacros: StateFlow<RemainingMacros?> = _remainingMacros.asStateFlow()
    
    private val _dinnerSuggestions = MutableStateFlow<List<DinnerSuggestion>>(emptyList())
    val dinnerSuggestions: StateFlow<List<DinnerSuggestion>> = _dinnerSuggestions.asStateFlow()
    
    private val _macroSummary = MutableStateFlow<Map<String, Any>>(emptyMap())
    val macroSummary: StateFlow<Map<String, Any>> = _macroSummary.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    /**
     * Calculate remaining macros and get dinner suggestions for a profile
     */
    fun calculateRemainingMacrosAndSuggestions(profile: Profile) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                // Calculate remaining macros
                val remaining = remainingMacroCalculator.calculateRemainingMacros(profile)
                _remainingMacros.value = remaining
                
                // Get dinner suggestions based on remaining macros
                val suggestions = remainingMacroCalculator.getDinnerSuggestions(profile, remaining)
                _dinnerSuggestions.value = suggestions
                
                // Get macro summary
                val summary = remainingMacroCalculator.getTodayMacroSummary(profile)
                _macroSummary.value = summary
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to calculate remaining macros: ${e.message}"
                println("Error in calculateRemainingMacrosAndSuggestions: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Refresh calculations (useful after logging a meal)
     */
    fun refreshCalculations(profile: Profile) {
        calculateRemainingMacrosAndSuggestions(profile)
    }
    
    /**
     * Clear error message
     */
    fun clearErrorMessage() {
        _errorMessage.value = null
    }
    
    /**
     * Get progress percentage for a specific macro
     */
    fun getMacroProgress(macroType: String): Float {
        val summary = _macroSummary.value
        val progress = summary["progress"] as? Map<String, Float> ?: emptyMap()
        return progress[macroType.lowercase()] ?: 0f
    }
    
    /**
     * Get consumed macros
     */
    fun getConsumedMacros(): Map<String, Float> {
        val summary = _macroSummary.value
        val consumed = summary["consumed"] as? Map<String, Float> ?: emptyMap()
        return consumed
    }
    
    /**
     * Get number of meals logged today
     */
    fun getMealsLoggedToday(): Int {
        val summary = _macroSummary.value
        return summary["mealsLogged"] as? Int ?: 0
    }
    
    /**
     * Check if user has logged any meals today
     */
    fun hasLoggedMealsToday(): Boolean {
        return getMealsLoggedToday() > 0
    }
    
    /**
     * Get a formatted string for remaining calories
     */
    fun getRemainingCaloriesText(): String {
        val remaining = _remainingMacros.value
        return if (remaining != null) {
            "${remaining.calories.toInt()} kcal remaining"
        } else {
            "Calculating..."
        }
    }
    
    /**
     * Get a formatted string for remaining protein
     */
    fun getRemainingProteinText(): String {
        val remaining = _remainingMacros.value
        return if (remaining != null) {
            "${remaining.protein.toInt()}g protein remaining"
        } else {
            "Calculating..."
        }
    }
    
    /**
     * Check if user is close to meeting their daily targets
     */
    fun isCloseToTargets(): Boolean {
        val remaining = _remainingMacros.value ?: return false
        val summary = _macroSummary.value
        val progress = summary["progress"] as? Map<String, Float> ?: emptyMap()
        
        val calorieProgress = progress["calories"] ?: 0f
        val proteinProgress = progress["protein"] ?: 0f
        
        // Consider close if at least 70% of calories and protein are met
        return calorieProgress >= 70f && proteinProgress >= 70f
    }
    
    /**
     * Check if user has exceeded their daily targets
     */
    fun hasExceededTargets(): Boolean {
        val remaining = _remainingMacros.value ?: return false
        val summary = _macroSummary.value
        val progress = summary["progress"] as? Map<String, Float> ?: emptyMap()
        
        val calorieProgress = progress["calories"] ?: 0f
        val proteinProgress = progress["protein"] ?: 0f
        
        // Consider exceeded if over 120% of targets
        return calorieProgress > 120f || proteinProgress > 120f
    }
    
    /**
     * Get recommendation message based on current progress
     */
    fun getRecommendationMessage(): String {
        return when {
            hasExceededTargets() -> "You've exceeded your daily targets. Consider lighter dinner options."
            isCloseToTargets() -> "Great progress! You're close to meeting your daily targets."
            getMealsLoggedToday() == 0 -> "Start your day by logging your breakfast!"
            getMealsLoggedToday() == 1 -> "Good start! Log your lunch to get dinner suggestions."
            else -> "Log your meals to get personalized dinner suggestions."
        }
    }
    
    /**
     * Get the best dinner suggestion (highest calories)
     */
    fun getBestDinnerSuggestion(): DinnerSuggestion? {
        return _dinnerSuggestions.value.maxByOrNull { it.calories }
    }
    
    /**
     * Check if there are any dinner suggestions available
     */
    fun hasDinnerSuggestions(): Boolean {
        return _dinnerSuggestions.value.isNotEmpty()
    }
    
    /**
     * Get dinner suggestions count
     */
    fun getDinnerSuggestionsCount(): Int {
        return _dinnerSuggestions.value.size
    }
    
    /**
     * Get a summary of what the best dinner suggestion covers
     */
    fun getBestDinnerCoverage(): String {
        val bestSuggestion = getBestDinnerSuggestion() ?: return "No suggestions available"
        
        return buildString {
            append("Best suggestion: ${bestSuggestion.name}\n")
            append("Calories: ${bestSuggestion.calories.toInt()} cal\n")
            append("Protein: ${bestSuggestion.protein.toInt()}g, Carbs: ${bestSuggestion.carbs.toInt()}g, Fat: ${bestSuggestion.fat.toInt()}g")
        }
    }
} 