package com.example.tastydiet.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.tastydiet.data.models.InventoryItem
import com.example.tastydiet.data.models.Profile
import com.example.tastydiet.data.models.Recipe

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartMealSuggestionCard(
    profile: Profile,
    inventory: List<InventoryItem>,
    recipes: List<Recipe>,
    onMealSelected: (Recipe) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "suggestion_animation")
    
    val cardScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "card_scale"
    )
    
    val suggestions = remember(profile, inventory, recipes) {
        generateSmartSuggestions(profile, inventory, recipes)
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = "Smart Suggestions",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        "Smart Meal Suggestions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                
                Icon(
                    Icons.Default.Psychology,
                    contentDescription = "AI Powered",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Profile Goal Info
            val bmi = profile.calculateBMI()
            val goal = when {
                bmi < 18.5 -> "Weight Gain"
                bmi < 25 -> "Maintenance"
                bmi < 30 -> "Weight Loss"
                else -> "Weight Loss"
            }
            
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        "${profile.name} - $goal",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Suggestions
            if (suggestions.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Restaurant,
                            contentDescription = "No suggestions",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            "Add more ingredients to inventory for personalized suggestions",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(suggestions.take(3)) { suggestion ->
                        MealSuggestionItem(
                            suggestion = suggestion,
                            onMealSelected = onMealSelected
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealSuggestionItem(
    suggestion: MealSuggestion,
    onMealSelected: (Recipe) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = { onMealSelected(suggestion.recipe) }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Recipe Icon
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = suggestion.color.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    suggestion.icon,
                    contentDescription = suggestion.recipe.name,
                    modifier = Modifier.padding(8.dp),
                    tint = suggestion.color
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Recipe Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    suggestion.recipe.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    suggestion.reason,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocalFireDepartment,
                        contentDescription = "Calories",
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        "${suggestion.recipe.caloriesPer100g} kcal/100g",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Rating",
                        modifier = Modifier.size(12.dp),
                        tint = Color(0xFFFFD700)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        "${suggestion.score}/10",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Arrow
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Select",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

data class MealSuggestion(
    val recipe: Recipe,
    val reason: String,
    val score: Int,
    val color: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

fun generateSmartSuggestions(
    profile: Profile,
    inventory: List<InventoryItem>,
    recipes: List<Recipe>
): List<MealSuggestion> {
    try {
        val bmi = profile.calculateBMI()
        val inventoryNames = inventory.map { it.name.lowercase().trim() }.toSet()
        
        val suggestions = mutableListOf<MealSuggestion>()
        
        for (recipe in recipes) {
            var score = 0
            var reason = ""
            
            // Check if recipe ingredients are available (improved logic)
            val hasIngredients = checkIngredientAvailability(recipe, inventoryNames)
            if (hasIngredients) {
                score += 3
                reason = "Ingredients available"
            }
            
            // Score based on BMI and goals
            when {
                bmi < 18.5 -> {
                    // Weight gain - prefer higher calorie foods
                    if (recipe.caloriesPer100g > 200) {
                        score += 2
                        reason = if (reason.isNotEmpty()) "$reason, high calorie for weight gain" else "High calorie for weight gain"
                    }
                }
                bmi < 25 -> {
                    // Maintenance - balanced approach
                    if (recipe.caloriesPer100g in 100..300) {
                        score += 2
                        reason = if (reason.isNotEmpty()) "$reason, balanced for maintenance" else "Balanced for maintenance"
                    }
                }
                else -> {
                    // Weight loss - prefer lower calorie, high protein
                    if (recipe.caloriesPer100g < 200 && recipe.proteinPer100g > 10) {
                        score += 3
                        reason = if (reason.isNotEmpty()) "$reason, low calorie, high protein for weight loss" else "Low calorie, high protein for weight loss"
                    }
                }
            }
            
            // Score based on protein content (considering profile goals)
            val targetProtein = profile.targetProtein
            if (recipe.proteinPer100g > targetProtein * 0.1f) { // At least 10% of daily target per 100g
                score += 1
                reason = if (reason.isNotEmpty()) "$reason, good protein content" else "Good protein content"
            }
            
            // Score based on fiber content
            if (recipe.fiberPer100g > 5) {
                score += 1
                reason = if (reason.isNotEmpty()) "$reason, high fiber" else "High fiber"
            }
            
            // Score based on macro balance
            val macroBalance = calculateMacroBalance(recipe)
            if (macroBalance > 0.7f) { // Good macro balance
                score += 1
                reason = if (reason.isNotEmpty()) "$reason, good macro balance" else "Good macro balance"
            }
            
            // Score based on calorie density
            val calorieDensity = recipe.caloriesPer100g
            when {
                calorieDensity < 100 -> score += 1 // Low calorie density
                calorieDensity in 100..300 -> score += 2 // Optimal range
                calorieDensity > 500 -> score -= 1 // Very high calorie density
            }
            
            // Score based on cuisine preference (if available)
            if (recipe.cuisine.lowercase().contains("indian")) {
                score += 1 // Bonus for Indian cuisine
            }
            
            if (score > 0) {
                val color = when {
                    score >= 6 -> Color(0xFF4CAF50) // Green for high score
                    score >= 4 -> Color(0xFFFF9800) // Orange for medium score
                    else -> Color(0xFF2196F3) // Blue for low score
                }
                
                val icon = getRecipeIcon(recipe)
                
                suggestions.add(
                    MealSuggestion(
                        recipe = recipe,
                        reason = reason.ifEmpty { "Good nutritional balance" },
                        score = score.coerceIn(1, 10),
                        color = color,
                        icon = icon
                    )
                )
            }
        }
        
        return suggestions.sortedByDescending { it.score }
    } catch (e: Exception) {
        println("Error in generateSmartSuggestions: ${e.message}")
        return emptyList()
    }
}

private fun checkIngredientAvailability(recipe: Recipe, inventoryNames: Set<String>): Boolean {
    // Check for common ingredients that might be available
    val recipeName = recipe.name.lowercase()
    val commonIngredients = listOf(
        "salad", "oat", "paneer", "rice", "bread", "chicken", "fish", "egg", "milk", "cheese",
        "tomato", "onion", "potato", "carrot", "spinach", "lettuce", "cucumber", "bell pepper",
        "garlic", "ginger", "flour", "oil", "salt", "pepper", "curry", "spice"
    )
    
    return commonIngredients.any { ingredient ->
        recipeName.contains(ingredient) && inventoryNames.any { inventoryName ->
            inventoryName.contains(ingredient) || ingredient.contains(inventoryName)
        }
    }
}

private fun calculateMacroBalance(recipe: Recipe): Float {
    val totalCalories = recipe.caloriesPer100g * 4.184f // Convert to kJ for calculation
    if (totalCalories <= 0) return 0f
    
    val proteinCalories = recipe.proteinPer100g * 4f
    val carbCalories = recipe.carbsPer100g * 4f
    val fatCalories = recipe.fatPer100g * 9f
    
    val totalMacroCalories = proteinCalories + carbCalories + fatCalories
    if (totalMacroCalories <= 0) return 0f
    
    // Calculate balance score (closer to 1.0 is better)
    val proteinRatio = proteinCalories / totalMacroCalories
    val carbRatio = carbCalories / totalMacroCalories
    val fatRatio = fatCalories / totalMacroCalories
    
    // Ideal ratios: Protein 20-30%, Carbs 45-65%, Fat 20-35%
    val proteinScore = when {
        proteinRatio in 0.2f..0.3f -> 1f
        proteinRatio in 0.15f..0.35f -> 0.8f
        else -> 0.5f
    }
    
    val carbScore = when {
        carbRatio in 0.45f..0.65f -> 1f
        carbRatio in 0.35f..0.75f -> 0.8f
        else -> 0.5f
    }
    
    val fatScore = when {
        fatRatio in 0.2f..0.35f -> 1f
        fatRatio in 0.15f..0.45f -> 0.8f
        else -> 0.5f
    }
    
    return (proteinScore + carbScore + fatScore) / 3f
}

private fun getRecipeIcon(recipe: Recipe): androidx.compose.ui.graphics.vector.ImageVector {
    val recipeName = recipe.name.lowercase()
    return when {
        recipeName.contains("salad") -> Icons.Default.Eco
        recipeName.contains("oat") -> Icons.Default.Grain
        recipeName.contains("paneer") -> Icons.Default.Restaurant
        recipeName.contains("rice") -> Icons.Default.Grain
        recipeName.contains("bread") -> Icons.Default.BakeryDining
        recipeName.contains("chicken") -> Icons.Default.Restaurant
        recipeName.contains("fish") -> Icons.Default.SetMeal
        recipeName.contains("egg") -> Icons.Default.Restaurant
        recipeName.contains("milk") -> Icons.Default.LocalDrink
        recipeName.contains("cheese") -> Icons.Default.Restaurant
        recipeName.contains("soup") -> Icons.Default.SoupKitchen
        recipeName.contains("curry") -> Icons.Default.Restaurant
        recipeName.contains("pasta") -> Icons.Default.Restaurant
        recipeName.contains("pizza") -> Icons.Default.LocalPizza
        recipeName.contains("burger") -> Icons.Default.Fastfood
        recipeName.contains("sandwich") -> Icons.Default.Restaurant
        recipeName.contains("cake") -> Icons.Default.Cake
        recipeName.contains("cookie") -> Icons.Default.Cookie
        recipeName.contains("ice cream") -> Icons.Default.Icecream
        recipeName.contains("smoothie") -> Icons.Default.LocalDrink
        recipeName.contains("juice") -> Icons.Default.LocalDrink
        else -> Icons.Default.Restaurant
    }
} 