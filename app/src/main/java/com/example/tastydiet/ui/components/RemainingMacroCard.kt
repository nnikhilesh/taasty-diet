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
import com.example.tastydiet.data.models.Profile
import com.example.tastydiet.util.RemainingMacros
import com.example.tastydiet.util.DinnerSuggestion
import androidx.compose.ui.graphics.vector.ImageVector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemainingMacroCard(
    profile: Profile,
    remainingMacros: RemainingMacros,
    dinnerSuggestions: List<DinnerSuggestion>,
    onDinnerSelected: (DinnerSuggestion) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "macro_animation")
    
    val cardScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "card_scale"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
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
                        Icons.Default.Restaurant,
                        contentDescription = "Dinner Suggestions",
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        "Dinner Suggestions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                
                Icon(
                    Icons.Default.Timeline,
                    contentDescription = "Macro Tracking",
                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Profile Info
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
                        "${profile.name} - Remaining Macros",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Remaining Macros Display
            RemainingMacrosDisplay(remainingMacros = remainingMacros)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Dinner Suggestions
            if (dinnerSuggestions.isEmpty()) {
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
                            "No suitable dinner suggestions found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                Text(
                    "Suggested Dinners to Complete Your Day:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(dinnerSuggestions.take(3)) { suggestion ->
                        DinnerSuggestionItem(
                            suggestion = suggestion,
                            onDinnerSelected = onDinnerSelected
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RemainingMacrosDisplay(remainingMacros: RemainingMacros) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Remaining for Today:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MacroItem(
                    label = "Protein",
                    value = remainingMacros.protein,
                    unit = "g",
                    color = Color(0xFF4CAF50),
                    icon = Icons.Default.FitnessCenter,
                    modifier = Modifier.weight(1f)
                )
                
                MacroItem(
                    label = "Carbs",
                    value = remainingMacros.carbs,
                    unit = "g",
                    color = Color(0xFFFF9800),
                    icon = Icons.Default.Grain,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MacroItem(
                    label = "Fat",
                    value = remainingMacros.fat.toFloat(),
                    unit = "g",
                    color = Color(0xFF9C27B0),
                    icon = Icons.Default.Opacity
                )
                
                MacroItem(
                    label = "Fiber",
                    value = remainingMacros.fiber.toFloat(),
                    unit = "g",
                    color = Color(0xFF795548),
                    icon = Icons.Default.Eco
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MacroItem(
                    label = "Calories",
                    value = remainingMacros.calories.toFloat(),
                    unit = "kcal",
                    color = Color(0xFFFF5722),
                    icon = Icons.Default.LocalFireDepartment
                )
                
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun MacroItem(
    label: String,
    value: Float,
    unit: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                "$value $unit",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DinnerSuggestionItem(
    suggestion: DinnerSuggestion,
    onDinnerSelected: (DinnerSuggestion) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = { onDinnerSelected(suggestion) }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Recipe Icon
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    Icons.Default.Restaurant,
                    contentDescription = suggestion.name,
                    modifier = Modifier.padding(8.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Recipe Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    suggestion.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    suggestion.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Scale,
                        contentDescription = "Calories",
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        "${suggestion.calories.toInt()} cal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Protein",
                        modifier = Modifier.size(12.dp),
                        tint = Color(0xFF4CAF50)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        "${suggestion.protein.toInt()}g protein",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Macro coverage preview
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Macros: ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        "${suggestion.carbs.toInt()}g carbs, ",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFF9800)
                    )
                    
                    Text(
                        "${suggestion.fat.toInt()}g fat",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF9C27B0)
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

private fun getScoreColor(score: Int): Color {
    return when {
        score >= 8 -> Color(0xFF4CAF50) // Green for high score
        score >= 6 -> Color(0xFFFF9800) // Orange for medium score
        else -> Color(0xFF2196F3) // Blue for low score
    }
} 