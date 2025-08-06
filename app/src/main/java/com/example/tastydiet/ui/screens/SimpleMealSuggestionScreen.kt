package com.example.tastydiet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tastydiet.data.models.FamilyMember
import com.example.tastydiet.data.models.Recipe
import com.example.tastydiet.util.SimpleMealSuggester
import com.example.tastydiet.viewmodel.RecipeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleMealSuggestionScreen(
    recipeViewModel: RecipeViewModel,
    familyMember: FamilyMember? = null
) {
    val recipes by recipeViewModel.recipes.collectAsState()
    val isLoading by recipeViewModel.isLoading.collectAsState()
    val errorMessage by recipeViewModel.errorMessage.collectAsState()
    
    val mealSuggester = remember { SimpleMealSuggester() }
    var mealPlan by remember { mutableStateOf<SimpleMealSuggester.DailyMealPlan?>(null) }
    
    // Generate meal suggestions when recipes are loaded
    LaunchedEffect(recipes) {
        if (recipes.isNotEmpty() && familyMember != null) {
            mealPlan = mealSuggester.generateMealSuggestions(recipes, familyMember)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Meal Suggestions",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Button(
                onClick = { 
                    if (familyMember != null) {
                        mealPlan = mealSuggester.generateMealSuggestions(recipes, familyMember)
                    }
                },
                enabled = !isLoading && recipes.isNotEmpty()
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Refresh")
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Status messages
        if (isLoading) {
            Text(
                text = "Loading recipes...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        errorMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Show recipe search functionality instead of profile requirement
        if (recipes.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Text(
                    text = "No recipes available. Please check your recipe database.",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            // Add search functionality
            var searchQuery by remember { mutableStateOf("") }
            val filteredRecipes = recipes.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.category.contains(searchQuery, ignoreCase = true) ||
                it.cuisine.contains(searchQuery, ignoreCase = true)
            }
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search recipes...") },
                placeholder = { Text("e.g., idli, chicken, vegetarian") },
                modifier = Modifier.fillMaxWidth(),
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
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (searchQuery.isNotEmpty()) {
                Text(
                    "${filteredRecipes.size} recipe${if (filteredRecipes.size != 1) "s" else ""} found",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Show filtered recipes
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredRecipes) { recipe ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { /* Handle recipe selection */ }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    recipe.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "${recipe.category} • ${recipe.cuisine}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "Calories: ${recipe.caloriesPer100g}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Recipe count info
        Text(
            text = "Available Recipes: ${recipes.size}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Meal suggestions
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            mealPlan?.let { plan ->
                // Breakfast
                plan.breakfast?.let { suggestion ->
                    item {
                        MealSuggestionCard(
                            title = "Breakfast",
                            suggestion = suggestion
                        )
                    }
                }
                
                // Lunch
                plan.lunch?.let { suggestion ->
                    item {
                        MealSuggestionCard(
                            title = "Lunch",
                            suggestion = suggestion
                        )
                    }
                }
                
                // Dinner
                plan.dinner?.let { suggestion ->
                    item {
                        MealSuggestionCard(
                            title = "Dinner",
                            suggestion = suggestion
                        )
                    }
                }
                
                // Snack
                plan.snack?.let { suggestion ->
                    item {
                        MealSuggestionCard(
                            title = "Snack",
                            suggestion = suggestion
                        )
                    }
                }
                
                // Daily summary
                item {
                    DailySummaryCard(plan)
                }
            }
        }
    }
}

@Composable
fun MealSuggestionCard(
    title: String,
    suggestion: SimpleMealSuggester.SimpleMealSuggestion
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = suggestion.recipe.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Portion: ${suggestion.portionSize.toInt()}g",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Macros
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MacroItem("Cal", suggestion.calories.toInt())
                MacroItem("P", suggestion.protein.toInt())
                MacroItem("C", suggestion.carbs.toInt())
                MacroItem("F", suggestion.fat.toInt())
                MacroItem("Fiber", suggestion.fiber.toInt())
            }
        }
    }
}

@Composable
fun MacroItem(label: String, value: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun DailySummaryCard(plan: SimpleMealSuggester.DailyMealPlan) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Daily Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MacroItem("Total Cal", plan.totalCalories.toInt())
                MacroItem("Protein", plan.totalProtein.toInt())
                MacroItem("Carbs", plan.totalCarbs.toInt())
                MacroItem("Fat", plan.totalFat.toInt())
                MacroItem("Fiber", plan.totalFiber.toInt())
            }
        }
    }
} 