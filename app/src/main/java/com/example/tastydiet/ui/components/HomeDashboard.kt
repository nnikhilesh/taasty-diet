package com.example.tastydiet.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tastydiet.data.models.Profile
import com.example.tastydiet.data.models.Recipe
import com.example.tastydiet.data.models.FoodItem
import com.example.tastydiet.data.models.NutritionalInfo
import com.example.tastydiet.data.models.FoodLog
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeDashboard(
    profiles: List<Profile>,
    recentRecipes: List<Recipe>,
    recentFoods: List<FoodItem>,
    nutritionalSuggestions: List<NutritionalInfo>,
    consumedCalories: Float,
    consumedProtein: Float,
    consumedCarbs: Float,
    consumedFat: Float,
    consumedFiber: Float,
    foodLogs: List<FoodLog> = emptyList(),
    onProfileClick: (Profile) -> Unit,
    onRecipeClick: (Recipe) -> Unit,
    onFoodLogged: (String, Float, String, String, Int, java.time.LocalDate) -> Unit,
    onRecipeLogged: (Recipe, Float, Int) -> Unit,
    onSearchQuery: (String) -> Unit,
    onViewAllRecipesClick: () -> Unit,
    onFoodLogUpdated: (FoodLog) -> Unit = {},
    onFoodLogDeleted: (FoodLog) -> Unit = {},
    onDateSelected: (LocalDate) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedProfile by remember { mutableStateOf<Profile?>(profiles.firstOrNull()) }
    var selectedDate by remember { mutableStateOf(java.time.LocalDate.now()) }
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Welcome to Tasty Diet!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        "Track your nutrition, plan meals, and stay healthy",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
        
        // Daily Macro Summary
        item {
            DailyMacroSummary(
                profiles = profiles,
                selectedProfile = selectedProfile,
                consumedCalories = consumedCalories,
                consumedProtein = consumedProtein,
                consumedCarbs = consumedCarbs,
                consumedFat = consumedFat,
                consumedFiber = consumedFiber,
                foodLogs = foodLogs,
                onProfileSelected = { profile ->
                    selectedProfile = profile
                    // Update the ViewModel's selected profile
                    onProfileClick(profile)
                },
                onDateSelected = { date ->
                    // Update the selected date for both DailyMacroSummary and QuickFoodLogging
                    selectedDate = date
                    onDateSelected(date)
                },
                onFoodLogUpdated = onFoodLogUpdated,
                onFoodLogDeleted = onFoodLogDeleted
            )
        }
        
        // Quick Food Logging
        item {
            QuickFoodLogging(
                profiles = profiles,
                recentFoods = recentFoods,
                nutritionalSuggestions = nutritionalSuggestions,
                selectedProfile = selectedProfile,
                selectedDate = selectedDate,
                onFoodLogged = onFoodLogged,
                onSearchQuery = onSearchQuery,

            )
        }
        
        // Meal Suggestions
        item {
            SimpleMealSuggestionsCard(
                selectedProfile = selectedProfile,
                selectedDate = selectedDate,
                consumedCalories = consumedCalories,
                consumedProtein = consumedProtein,
                consumedCarbs = consumedCarbs,
                consumedFat = consumedFat,
                onFoodLogged = { foodName, quantity, unit, mealType, profileId, selectedDate ->
                    onFoodLogged(foodName, quantity, unit, mealType, profileId, selectedDate)
                }
            )
        }
        
        // Quick Tips
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "💡 Quick Tips",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Text(
                        "• Log your meals to track daily nutrition\n" +
                        "• Check inventory before meal planning\n" +
                        "• Use macro calculator to set goals\n" +
                        "• Browse recipes for meal inspiration",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileItem(
    profile: Profile,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = profile.name,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    profile.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    "${profile.age} years • ${profile.weight}kg",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "View Profile",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeItem(
    recipe: Recipe,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Restaurant,
                contentDescription = recipe.name,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    recipe.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    "${recipe.caloriesPer100g.toInt()} kcal • ${if (recipe.isVeg) "Veg" else "Non-Veg"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "View Recipe",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleMealSuggestionsCard(
    selectedProfile: Profile?,
    selectedDate: java.time.LocalDate,
    consumedCalories: Float,
    consumedProtein: Float,
    consumedCarbs: Float,
    consumedFat: Float,
    onFoodLogged: (String, Float, String, String, Int, java.time.LocalDate) -> Unit
) {
    if (selectedProfile == null) return
    
    val targetCalories = selectedProfile.targetCalories
    val targetProtein = selectedProfile.targetProtein
    val targetCarbs = selectedProfile.targetCarbs
    val targetFat = selectedProfile.targetFat
    
    val remainingCalories = (targetCalories - consumedCalories).coerceAtLeast(0f)
    val remainingProtein = (targetProtein - consumedProtein).coerceAtLeast(0f)
    val remainingCarbs = (targetCarbs - consumedCarbs).coerceAtLeast(0f)
    val remainingFat = (targetFat - consumedFat).coerceAtLeast(0f)
    
    // Generate simple meal suggestions based on remaining macros
    val suggestions = generateSimpleMealSuggestions(remainingCalories, remainingProtein, remainingCarbs, remainingFat)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                "🍽️ Meal Suggestions",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            if (suggestions.isEmpty()) {
                Text(
                    "Great job! You've met your daily targets.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(suggestions) { suggestion ->
                        SimpleMealSuggestionChip(
                            suggestion = suggestion,
                                                         onSuggestionClick = {
                                 onFoodLogged(suggestion.name, suggestion.quantity, "Snack", "Snack", selectedProfile.id, selectedDate)
                             }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleMealSuggestionChip(
    suggestion: SimpleMealSuggestion,
    onSuggestionClick: () -> Unit
) {
    Card(
        modifier = Modifier.width(140.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = onSuggestionClick
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Restaurant,
                contentDescription = suggestion.name,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                suggestion.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Text(
                "${suggestion.calories.toInt()} cal",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

data class SimpleMealSuggestion(
    val name: String,
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val quantity: Float
)

private fun generateSimpleMealSuggestions(
    remainingCalories: Float,
    remainingProtein: Float,
    remainingCarbs: Float,
    remainingFat: Float
): List<SimpleMealSuggestion> {
    val suggestions = mutableListOf<SimpleMealSuggestion>()
    
    // High protein suggestions
    if (remainingProtein > 10f) {
        suggestions.add(SimpleMealSuggestion("Boiled Eggs", 140f, 12f, 1f, 10f, 100f))
        suggestions.add(SimpleMealSuggestion("Greek Yogurt", 120f, 15f, 8f, 4f, 150f))
        suggestions.add(SimpleMealSuggestion("Chicken Breast", 165f, 31f, 0f, 3.6f, 100f))
    }
    
    // High carb suggestions
    if (remainingCarbs > 20f) {
        suggestions.add(SimpleMealSuggestion("Banana", 89f, 1.1f, 23f, 0.3f, 100f))
        suggestions.add(SimpleMealSuggestion("Brown Rice", 111f, 2.6f, 23f, 0.9f, 100f))
        suggestions.add(SimpleMealSuggestion("Sweet Potato", 86f, 1.6f, 20f, 0.1f, 100f))
    }
    
    // Balanced suggestions
    if (remainingCalories > 200f) {
        suggestions.add(SimpleMealSuggestion("Mixed Nuts", 607f, 20f, 21f, 54f, 100f))
        suggestions.add(SimpleMealSuggestion("Avocado", 160f, 2f, 9f, 15f, 100f))
        suggestions.add(SimpleMealSuggestion("Salmon", 208f, 25f, 0f, 12f, 100f))
    }
    
    // Quick snacks
    suggestions.add(SimpleMealSuggestion("Apple", 52f, 0.3f, 14f, 0.2f, 100f))
    suggestions.add(SimpleMealSuggestion("Carrots", 41f, 0.9f, 10f, 0.2f, 100f))
    suggestions.add(SimpleMealSuggestion("Cucumber", 16f, 0.7f, 3.6f, 0.1f, 100f))
    
    return suggestions.take(6) // Limit to 6 suggestions
} 