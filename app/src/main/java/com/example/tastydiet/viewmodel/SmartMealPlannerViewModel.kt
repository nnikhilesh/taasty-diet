package com.example.tastydiet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tastydiet.data.*
import com.example.tastydiet.data.models.*
import com.example.tastydiet.util.SmartMealPlanner
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SmartMealPlannerViewModel(
    private val recipeDao: RecipeDao,
    private val inventoryDao: InventoryDao,
    private val recipeIngredientDao: RecipeIngredientDao,
    private val smartMealPlanDao: SmartMealPlanDao,
    private val familyMemberDao: FamilyMemberDao
) : ViewModel() {

    private val smartMealPlanner = SmartMealPlanner(recipeDao, inventoryDao, recipeIngredientDao)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // UI State
    private val _uiState = MutableStateFlow(SmartMealPlannerUiState())
    val uiState: StateFlow<SmartMealPlannerUiState> = _uiState.asStateFlow()

    // Current date
    private val _currentDate = MutableStateFlow(dateFormat.format(Date()))
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    // Current family member
    private val _selectedMember = MutableStateFlow<FamilyMember?>(null)
    val selectedMember: StateFlow<FamilyMember?> = _selectedMember.asStateFlow()

    init {
        loadFamilyMembers()
        observeCurrentMealPlan()
    }

    fun loadFamilyMembers() {
        viewModelScope.launch {
            val members = familyMemberDao.getAll()
            if (members.isNotEmpty()) {
                _selectedMember.value = members.first()
                generateMealPlan()
            }
        }
    }

    fun selectFamilyMember(member: FamilyMember) {
        _selectedMember.value = member
        generateMealPlan()
    }

    fun setDate(date: String) {
        _currentDate.value = date
        generateMealPlan()
    }

    fun generateMealPlan() {
        val member = _selectedMember.value ?: return
        val date = _currentDate.value

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val mealPlan = smartMealPlanner.generateSmartMealPlan(member, date)
                smartMealPlanDao.insert(mealPlan)
                
                // Load meal suggestions
                loadMealSuggestions(mealPlan)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to generate meal plan: ${e.message}"
                )
            }
        }
    }

    private fun observeCurrentMealPlan() {
        viewModelScope.launch {
            combine(currentDate, selectedMember) { date, member ->
                Pair(date, member)
            }.filter { (_, member) -> member != null }
            .flatMapLatest { (date, member) ->
                smartMealPlanDao.getMealPlanForDateFlow(date, member!!.id)
            }.collect { mealPlan ->
                if (mealPlan != null) {
                    loadMealSuggestions(mealPlan)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        mealSuggestions = emptyList(),
                        macroSummary = null
                    )
                }
            }
        }
    }

    private suspend fun loadMealSuggestions(mealPlan: SmartMealPlan) {
        val member = _selectedMember.value ?: return
        
        val mealSuggestions = mutableListOf<MealSuggestion>()
        
        // Load each meal suggestion
        val mealTypes = listOf("Breakfast", "Lunch", "Dinner", "Snack")
        val recipeIds = listOf(
            mealPlan.breakfastRecipeId,
            mealPlan.lunchRecipeId,
            mealPlan.dinnerRecipeId,
            mealPlan.snackRecipeId
        )

        for (i in mealTypes.indices) {
            val mealType = mealTypes[i]
            val recipeId = recipeIds[i]
            
            if (recipeId != null) {
                val recipe = recipeDao.getById(recipeId)
                if (recipe != null) {
                    val targetCalories = member.targetCalories * getMealDistribution(mealType)
                    val portionSize = if (recipe.caloriesPer100g > 0) {
                        (targetCalories / recipe.caloriesPer100g) * 100
                    } else {
                        100f
                    }

                    val calories = (recipe.caloriesPer100g * portionSize) / 100
                    val protein = (recipe.proteinPer100g * portionSize) / 100
                    val carbs = (recipe.carbsPer100g * portionSize) / 100
                    val fat = (recipe.fatPer100g * portionSize) / 100
                    val fiber = (recipe.fiberPer100g * portionSize) / 100

                    val (ingredientsAvailable, missingIngredients) = 
                        smartMealPlanner.checkIngredientAvailability(recipeId)

                    mealSuggestions.add(
                        MealSuggestion(
                            recipe = recipe,
                            mealType = mealType,
                            portionSize = portionSize,
                            calories = calories,
                            protein = protein,
                            carbs = carbs,
                            fat = fat,
                            fiber = fiber,
                            ingredientsAvailable = ingredientsAvailable,
                            missingIngredients = missingIngredients
                        )
                    )
                }
            }
        }

        val macroSummary = smartMealPlanner.getDailyMacroSummary(mealPlan)

        _uiState.value = _uiState.value.copy(
            isLoading = false,
            mealSuggestions = mealSuggestions,
            macroSummary = macroSummary,
            currentMealPlan = mealPlan
        )
    }

    fun regenerateMealSuggestion(mealType: String) {
        val member = _selectedMember.value ?: return
        val currentMealPlan = _uiState.value.currentMealPlan ?: return

        viewModelScope.launch {
            try {
                val newSuggestion = smartMealPlanner.regenerateMealSuggestion(
                    mealType, member, currentMealPlan
                )

                // Update the meal plan with new recipe
                val updatedMealPlan = when (mealType) {
                    "Breakfast" -> currentMealPlan.copy(breakfastRecipeId = newSuggestion.recipe.id)
                    "Lunch" -> currentMealPlan.copy(lunchRecipeId = newSuggestion.recipe.id)
                    "Dinner" -> currentMealPlan.copy(dinnerRecipeId = newSuggestion.recipe.id)
                    "Snack" -> currentMealPlan.copy(snackRecipeId = newSuggestion.recipe.id)
                    else -> currentMealPlan
                }

                smartMealPlanDao.update(updatedMealPlan)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to regenerate meal: ${e.message}"
                )
            }
        }
    }

    fun acceptMealPlan() {
        val currentMealPlan = _uiState.value.currentMealPlan ?: return

        viewModelScope.launch {
            try {
                smartMealPlanDao.updateAcceptanceStatus(currentMealPlan.id, true)
                _uiState.value = _uiState.value.copy(
                    currentMealPlan = currentMealPlan.copy(isAccepted = true)
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to accept meal plan: ${e.message}"
                )
            }
        }
    }

    private fun getMealDistribution(mealType: String): Float {
        return when (mealType) {
            "Breakfast" -> 0.25f
            "Lunch" -> 0.35f
            "Dinner" -> 0.30f
            "Snack" -> 0.10f
            else -> 0.25f
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

// UI State data class
data class SmartMealPlannerUiState(
    val isLoading: Boolean = false,
    val mealSuggestions: List<MealSuggestion> = emptyList(),
    val macroSummary: DailyMacroSummary? = null,
    val currentMealPlan: SmartMealPlan? = null,
    val error: String? = null
) 