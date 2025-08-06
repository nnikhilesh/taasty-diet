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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tastydiet.data.models.Recipe
import android.content.Intent
import android.net.Uri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedRecipesScreen(
    recipes: List<Recipe>,
    onRecipeClick: (Recipe) -> Unit,
    onAddRecipeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    
    val filters = listOf("All", "Veg", "Non-Veg", "Mixed")
    
    val filteredRecipes = recipes.filter { recipe ->
        val matchesFilter = when (selectedFilter) {
            "Veg" -> recipe.isVeg
            "Non-Veg" -> !recipe.isVeg
            "Mixed" -> true // Show all for mixed
            else -> true // Show all for "All"
        }
        
        val matchesSearch = searchQuery.isEmpty() || 
            recipe.name.contains(searchQuery, ignoreCase = true)
        
        matchesFilter && matchesSearch
    }
    
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        // Header with Search
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Recipes & Meal Planning",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = onAddRecipeClick) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add Recipe",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search recipes...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Filter Chips
                Text(
                    "Filter by:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filters) { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter) },
                            leadingIcon = {
                                Icon(
                                    getFilterIcon(filter),
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }
            }
        }
        
        // Recipe Count
        Text(
            "${filteredRecipes.size} recipes found",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        // Recipes List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredRecipes) { recipe ->
                EnhancedRecipeCard(
                    recipe = recipe,
                    onClick = { onRecipeClick(recipe) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedRecipeCard(
    recipe: Recipe,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with Recipe Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        recipe.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (recipe.isVeg) Icons.Default.Eco else Icons.Default.Restaurant,
                            contentDescription = if (recipe.isVeg) "Vegetarian" else "Non-Vegetarian",
                            tint = if (recipe.isVeg) Color(0xFF4CAF50) else Color(0xFFFF5722),
                            modifier = Modifier.size(16.dp)
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            if (recipe.isVeg) "Vegetarian" else "Non-Vegetarian",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            "•",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            "${recipe.caloriesPer100g.toInt()} kcal/100g",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // YouTube Search Button
                IconButton(
                    onClick = {
                        val searchQuery = "${recipe.name} recipe"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=$searchQuery"))
                        context.startActivity(intent)
                    }
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Search on YouTube",
                        tint = Color(0xFFFF0000) // YouTube red
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Macro Information
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MacroInfoItem(
                    label = "Protein",
                    value = "${recipe.proteinPer100g.toInt()}g",
                    color = Color(0xFF4CAF50)
                )
                
                MacroInfoItem(
                    label = "Carbs",
                    value = "${recipe.carbsPer100g.toInt()}g",
                    color = Color(0xFFFF9800)
                )
                
                MacroInfoItem(
                    label = "Fat",
                    value = "${recipe.fatPer100g.toInt()}g",
                    color = Color(0xFF9C27B0)
                )
                
                MacroInfoItem(
                    label = "Fiber",
                    value = "${recipe.fiberPer100g.toInt()}g",
                    color = Color(0xFF795548)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = "View Details",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("View Details")
                }
                
                OutlinedButton(
                    onClick = {
                        val searchQuery = "${recipe.name} recipe"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=$searchQuery"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "YouTube",
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFFFF0000)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("YouTube")
                }
            }
        }
    }
}

@Composable
fun MacroInfoItem(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun getFilterIcon(filter: String) = when (filter) {
    "Veg" -> Icons.Default.Eco
    "Non-Veg" -> Icons.Default.Restaurant
    "Mixed" -> Icons.Default.AllInclusive
    else -> Icons.Default.AllInclusive
} 