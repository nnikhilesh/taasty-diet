package com.example.tastydiet.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tastydiet.data.models.AnalyticsData

@Composable
fun SimpleBarChart(
    data: List<AnalyticsData>,
    title: String,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            if (data.isEmpty()) {
                Text(
                    text = "No data available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = surfaceVariantColor
                )
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val barWidth = width / (data.size + 1)
                    val maxValue = data.maxOfOrNull { it.totalCalories } ?: 0f
                    
                    // Draw bars
                    data.forEachIndexed { index, item ->
                        val barHeight = if (maxValue > 0) {
                            (item.totalCalories / maxValue) * height * 0.8f
                        } else {
                            0f
                        }
                        
                        val x = (index + 1) * barWidth
                        val y = height - barHeight
                        
                        // Draw bar
                        drawRect(
                            color = primaryColor,
                            topLeft = Offset(x - barWidth * 0.4f, y),
                            size = androidx.compose.ui.geometry.Size(
                                barWidth * 0.8f,
                                barHeight
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SimpleLineChart(
    data: List<AnalyticsData>,
    title: String,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            if (data.isEmpty()) {
                Text(
                    text = "No data available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = surfaceVariantColor
                )
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val maxValue = data.maxOfOrNull { it.totalCalories } ?: 0f
                    val minValue = data.minOfOrNull { it.totalCalories } ?: 0f
                    val valueRange = maxValue - minValue
                    
                    if (valueRange > 0) {
                        val path = Path()
                        var isFirst = true
                        
                        data.forEachIndexed { index, item ->
                            val x = (index.toFloat() / (data.size - 1)) * width
                            val normalizedValue = (item.totalCalories - minValue) / valueRange
                            val y = height - (normalizedValue * height * 0.8f + height * 0.1f)
                            
                            if (isFirst) {
                                path.moveTo(x, y)
                                isFirst = false
                            } else {
                                path.lineTo(x, y)
                            }
                        }
                        
                        // Draw line
                        drawPath(
                            path = path,
                            color = primaryColor,
                            style = Stroke(width = 3f)
                        )
                        
                        // Draw points
                        data.forEachIndexed { index, item ->
                            val x = (index.toFloat() / (data.size - 1)) * width
                            val normalizedValue = (item.totalCalories - minValue) / valueRange
                            val y = height - (normalizedValue * height * 0.8f + height * 0.1f)
                            
                            drawCircle(
                                color = primaryColor,
                                radius = 4f,
                                center = Offset(x, y)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NutritionSummaryCard(
    calories: Float,
    protein: Float,
    carbs: Float,
    fat: Float,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    
    Card(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Nutrition Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                NutritionItem(
                    label = "Calories",
                    value = "${calories.toInt()}",
                    unit = "kcal",
                    color = primaryColor
                )
                NutritionItem(
                    label = "Protein",
                    value = "${protein.toInt()}",
                    unit = "g",
                    color = Color(0xFF4CAF50)
                )
                NutritionItem(
                    label = "Carbs",
                    value = "${carbs.toInt()}",
                    unit = "g",
                    color = Color(0xFFFF9800)
                )
                NutritionItem(
                    label = "Fat",
                    value = "${fat.toInt()}",
                    unit = "g",
                    color = Color(0xFFF44336)
                )
            }
        }
    }
}

@Composable
fun NutritionItem(
    label: String,
    value: String,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
} 