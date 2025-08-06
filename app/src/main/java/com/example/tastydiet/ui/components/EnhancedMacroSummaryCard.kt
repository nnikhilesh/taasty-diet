package com.example.tastydiet.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tastydiet.data.models.Profile
import com.example.tastydiet.viewmodel.EnhancedFoodLogViewModel

@Composable
fun EnhancedMacroSummaryCard(
    todayTotals: EnhancedFoodLogViewModel.TodayTotals,
    profile: Profile,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "macro_animation")
    
    // Animated values for the card
    val cardElevation by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "card_elevation"
    )
    
    // Get target macros from profile or use defaults
    val targetCalories = profile.targetCalories
    val targetProtein = profile.targetProtein
    val targetCarbs = profile.targetCarbs
    val targetFat = profile.targetFat
    val targetFiber = profile.targetFiber
    
    // Animated progress for each macro
    val caloriesProgress by animateFloatAsState(
        targetValue = (todayTotals.calories / targetCalories).coerceIn(0f, 1f),
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "calories_progress"
    )
    
    val proteinProgress by animateFloatAsState(
        targetValue = (todayTotals.protein / targetProtein).coerceIn(0f, 1f),
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "protein_progress"
    )
    
    val carbsProgress by animateFloatAsState(
        targetValue = (todayTotals.carbs / targetCarbs).coerceIn(0f, 1f),
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "carbs_progress"
    )
    
    val fatProgress by animateFloatAsState(
        targetValue = (todayTotals.fat / targetFat).coerceIn(0f, 1f),
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "fat_progress"
    )
    
    val fiberProgress by animateFloatAsState(
        targetValue = (todayTotals.fiber / targetFiber).coerceIn(0f, 1f),
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "fiber_progress"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation.dp)
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
                Text(
                    "Today's Summary for ${profile.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Icon(
                    Icons.Filled.TrendingUp,
                    contentDescription = "Progress",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // BMI Status
            val bmi = profile.calculateBMI()
            val bmiCategory = profile.getBMICategory()
            val bmiColor = when {
                bmi < 18.5 -> Color(0xFF2196F3) // Blue for underweight
                bmi < 25 -> Color(0xFF4CAF50)   // Green for normal
                bmi < 30 -> Color(0xFFFF9800)   // Orange for overweight
                else -> Color(0xFFF44336)       // Red for obese
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "BMI: ${String.format("%.1f", bmi)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = bmiColor.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        bmiCategory,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = bmiColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Macro Progress Bars
            EnhancedMacroProgressItem(
                label = "Calories",
                value = todayTotals.calories.toInt(),
                unit = "kcal",
                progress = caloriesProgress,
                target = targetCalories.toInt(),
                icon = Icons.Default.LocalFireDepartment,
                color = Color(0xFFFF5722)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            EnhancedMacroProgressItem(
                label = "Protein",
                value = todayTotals.protein.toInt(),
                unit = "g",
                progress = proteinProgress,
                target = targetProtein.toInt(),
                icon = Icons.Default.FitnessCenter,
                color = Color(0xFF2196F3)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            EnhancedMacroProgressItem(
                label = "Carbs",
                value = todayTotals.carbs.toInt(),
                unit = "g",
                progress = carbsProgress,
                target = targetCarbs.toInt(),
                icon = Icons.Default.Grain,
                color = Color(0xFFFF9800)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            EnhancedMacroProgressItem(
                label = "Fat",
                value = todayTotals.fat.toInt(),
                unit = "g",
                progress = fatProgress,
                target = targetFat.toInt(),
                icon = Icons.Default.Opacity,
                color = Color(0xFF9C27B0)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            EnhancedMacroProgressItem(
                label = "Fiber",
                value = todayTotals.fiber.toInt(),
                unit = "g",
                progress = fiberProgress,
                target = targetFiber.toInt(),
                icon = Icons.Default.Grass,
                color = Color(0xFF4CAF50)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Summary text
            val totalPercentage = ((todayTotals.calories / targetCalories) * 100).toInt()
            val statusText = when {
                totalPercentage < 80 -> "You're under your daily target"
                totalPercentage <= 120 -> "Great! You're on track"
                else -> "You're over your daily target"
            }
            
            Text(
                statusText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun EnhancedMacroProgressItem(
    label: String,
    value: Int,
    unit: String,
    progress: Float,
    target: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Icon(
            icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Label and value
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Text(
                    "$value/$target $unit",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Progress bar
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = color,
                trackColor = color.copy(alpha = 0.2f)
            )
        }
    }
} 