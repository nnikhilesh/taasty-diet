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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.StrokeCap
import com.example.tastydiet.data.models.FamilyMember
import com.example.tastydiet.data.models.FoodLog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MacroSummaryCard(
    member: FamilyMember?,
    todayLogs: List<FoodLog>,
    modifier: Modifier = Modifier
) {
    if (member == null) return
    
    val totalCalories = todayLogs.sumOf { it.calories.toDouble() }.toInt()
    val totalProtein = todayLogs.sumOf { it.protein.toDouble() }.toInt()
    val totalCarbs = todayLogs.sumOf { it.carbs.toDouble() }.toInt()
    val totalFat = todayLogs.sumOf { it.fat.toDouble() }.toInt()
    
    val caloriesProgress = (totalCalories.toFloat() / member.targetCalories).coerceIn(0f, 1f)
    val proteinProgress = (totalProtein.toFloat() / member.targetProtein).coerceIn(0f, 1f)
    val carbsProgress = (totalCarbs.toFloat() / member.targetCarbs).coerceIn(0f, 1f)
    val fatProgress = (totalFat.toFloat() / member.targetFat).coerceIn(0f, 1f)
    
    // Animated progress values
    val animatedCaloriesProgress by animateFloatAsState(
        targetValue = caloriesProgress,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "calories"
    )
    val animatedProteinProgress by animateFloatAsState(
        targetValue = proteinProgress,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "protein"
    )
    val animatedCarbsProgress by animateFloatAsState(
        targetValue = carbsProgress,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "carbs"
    )
    val animatedFatProgress by animateFloatAsState(
        targetValue = fatProgress,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "fat"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Today's Progress",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        member.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Icon(
                    Icons.Filled.TrendingUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Macro summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MacroCircle(
                    label = "Calories",
                    current = totalCalories,
                    target = member.targetCalories.toInt(),
                    progress = animatedCaloriesProgress,
                    color = MaterialTheme.colorScheme.primary,
                    unit = "kcal"
                )
                MacroCircle(
                    label = "Protein",
                    current = totalProtein,
                    target = member.targetProtein.toInt(),
                    progress = animatedProteinProgress,
                    color = MaterialTheme.colorScheme.secondary,
                    unit = "g"
                )
                MacroCircle(
                    label = "Carbs",
                    current = totalCarbs,
                    target = member.targetCarbs.toInt(),
                    progress = animatedCarbsProgress,
                    color = MaterialTheme.colorScheme.tertiary,
                    unit = "g"
                )
                MacroCircle(
                    label = "Fat",
                    current = totalFat,
                    target = member.targetFat.toInt(),
                    progress = animatedFatProgress,
                    color = MaterialTheme.colorScheme.error,
                    unit = "g"
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress bars
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimatedMacroProgressBar(
                    label = "Calories",
                    current = totalCalories,
                    target = member.targetCalories.toInt(),
                    progress = animatedCaloriesProgress,
                    color = MaterialTheme.colorScheme.primary,
                    unit = "kcal"
                )
                AnimatedMacroProgressBar(
                    label = "Protein",
                    current = totalProtein,
                    target = member.targetProtein.toInt(),
                    progress = animatedProteinProgress,
                    color = MaterialTheme.colorScheme.secondary,
                    unit = "g"
                )
                AnimatedMacroProgressBar(
                    label = "Carbs",
                    current = totalCarbs,
                    target = member.targetCarbs.toInt(),
                    progress = animatedCarbsProgress,
                    color = MaterialTheme.colorScheme.tertiary,
                    unit = "g"
                )
                AnimatedMacroProgressBar(
                    label = "Fat",
                    current = totalFat,
                    target = member.targetFat.toInt(),
                    progress = animatedFatProgress,
                    color = MaterialTheme.colorScheme.error,
                    unit = "g"
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Summary text
            val remainingCalories = (member.targetCalories - totalCalories).toInt()
            val statusText = when {
                remainingCalories <= 0 -> "Daily goal reached! 🎉"
                remainingCalories < 200 -> "Almost there! ${remainingCalories} kcal left"
                else -> "${remainingCalories} calories remaining"
            }
            
            Text(
                statusText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MacroCircle(
    label: String,
    current: Int,
    target: Int,
    progress: Float,
    color: Color,
    unit: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(60.dp)
        ) {
            CircularProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxSize(),
                color = color,
                strokeWidth = 4.dp,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "$current",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun AnimatedMacroProgressBar(
    label: String,
    current: Int,
    target: Int,
    progress: Float,
    color: Color,
    unit: String
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                "$current/$target $unit",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        LinearProgressIndicator(
                            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickMacroCard(
    member: FamilyMember?,
    todayLogs: List<FoodLog>,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (member == null) return
    
    val totalCalories = todayLogs.sumOf { it.calories.toDouble() }.toInt()
    val caloriesProgress = (totalCalories.toFloat() / member.targetCalories).coerceIn(0f, 1f)
    
    val animatedProgress by animateFloatAsState(
        targetValue = caloriesProgress,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "quick_calories"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        onClick = onTap
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Progress circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(48.dp)
            ) {
                CircularProgressIndicator(
                    progress = animatedProgress,
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 3.dp,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Text(
                    "${(animatedProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Text content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Daily Progress",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "$totalCalories / ${member.targetCalories.toInt()} kcal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "View details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
} 