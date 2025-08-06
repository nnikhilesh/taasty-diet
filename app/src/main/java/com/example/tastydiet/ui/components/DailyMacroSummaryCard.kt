package com.example.tastydiet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tastydiet.data.models.DailyMacroSummary

@Composable
fun DailyMacroSummaryCard(
    macroSummary: DailyMacroSummary,
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
            Text(
                text = "Daily Macro Summary",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Macro progress bars
            MacroProgressBar(
                label = "Calories",
                current = macroSummary.totalCalories,
                target = macroSummary.targetCalories,
                progress = macroSummary.caloriesProgress,
                color = Color(0xFF4CAF50)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            MacroProgressBar(
                label = "Protein",
                current = macroSummary.totalProtein,
                target = macroSummary.targetProtein,
                progress = macroSummary.proteinProgress,
                color = Color(0xFF2196F3)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            MacroProgressBar(
                label = "Carbs",
                current = macroSummary.totalCarbs,
                target = macroSummary.targetCarbs,
                progress = macroSummary.carbsProgress,
                color = Color(0xFFFF9800)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            MacroProgressBar(
                label = "Fat",
                current = macroSummary.totalFat,
                target = macroSummary.targetFat,
                progress = macroSummary.fatProgress,
                color = Color(0xFFE91E63)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            MacroProgressBar(
                label = "Fiber",
                current = macroSummary.totalFiber,
                target = macroSummary.targetFiber,
                progress = macroSummary.fiberProgress,
                color = Color(0xFF9C27B0)
            )
        }
    }
}

@Composable
private fun MacroProgressBar(
    label: String,
    current: Float,
    target: Float,
    progress: Float,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "${current.toInt()}/${target.toInt()}g",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        LinearProgressIndicator(
            progress = (progress / 100f).coerceIn(0f, 1f),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
} 