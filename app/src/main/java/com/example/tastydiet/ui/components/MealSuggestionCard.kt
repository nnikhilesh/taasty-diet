package com.example.tastydiet.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tastydiet.data.models.MealSuggestion

@Composable
fun MealSuggestionCard(
    mealSuggestion: MealSuggestion,
    onAcceptMeal: () -> Unit,
    onRegenerateMeal: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with meal type and recipe name
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = mealSuggestion.mealType,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = mealSuggestion.recipe.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Availability indicator
                if (!mealSuggestion.ingredientsAvailable) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFF5722)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Missing Ingredients",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Portion size
            Text(
                text = "Portion: ${mealSuggestion.portionSize.toInt()}g",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Macro breakdown
            MacroBreakdownRow(
                calories = mealSuggestion.calories,
                protein = mealSuggestion.protein,
                carbs = mealSuggestion.carbs,
                fat = mealSuggestion.fat,
                fiber = mealSuggestion.fiber
            )
            
            // Missing ingredients warning
            if (mealSuggestion.missingIngredients.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text(
                            text = "Missing ingredients:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFE65100)
                        )
                        Text(
                            text = mealSuggestion.missingIngredients.joinToString(", "),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE65100)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = onRegenerateMeal,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Regenerate")
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Button(
                    onClick = onAcceptMeal,
                    modifier = Modifier.weight(1f),
                    enabled = mealSuggestion.ingredientsAvailable
                ) {
                    Text("Accept Meal")
                }
            }
        }
    }
}

@Composable
private fun MacroBreakdownRow(
    calories: Float,
    protein: Float,
    carbs: Float,
    fat: Float,
    fiber: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        MacroItem("Cal", calories.toInt(), "kcal")
        MacroItem("P", protein.toInt(), "g")
        MacroItem("C", carbs.toInt(), "g")
        MacroItem("F", fat.toInt(), "g")
        MacroItem("Fib", fiber.toInt(), "g")
    }
}

@Composable
private fun MacroItem(
    label: String,
    value: Int,
    unit: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "$value$unit",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
} 