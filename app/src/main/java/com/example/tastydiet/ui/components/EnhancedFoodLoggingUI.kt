package com.example.tastydiet.ui.components

import androidx.compose.animation.*
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tastydiet.data.models.NutritionalInfo
import com.example.tastydiet.viewmodel.EnhancedFoodLogViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedFoodLoggingUI(
    viewModel: EnhancedFoodLogViewModel,
    profileId: Int,
    modifier: Modifier = Modifier
) {
    var foodName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var selectedMealType by remember { mutableStateOf("Breakfast") }
    var showSuggestions by remember { mutableStateOf(false) }
    
    val mealTypes = listOf("Breakfast", "Lunch", "Dinner", "Snack")
    val foodSuggestions by viewModel.foodSuggestions.collectAsStateWithLifecycle()
    val nutritionalPreview by viewModel.nutritionalPreview.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Header
        Text(
            "Log Your Food",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Food Name Input with Search
        OutlinedTextField(
            value = foodName,
            onValueChange = { 
                foodName = it
                if (it.length >= 2) {
                    viewModel.searchFood(it)
                    showSuggestions = true
                } else {
                    showSuggestions = false
                }
            },
            label = { Text("Food Name") },
            placeholder = { Text("e.g., Rice, Apple, Chicken") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search")
            },
            trailingIcon = {
                if (foodName.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            foodName = ""
                            showSuggestions = false
                            viewModel.clearFoodSuggestions()
                        }
                    ) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            singleLine = true
        )
        
        // Food Suggestions
        AnimatedVisibility(
            visible = showSuggestions && foodSuggestions.isNotEmpty(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 200.dp)
                ) {
                    items(foodSuggestions) { food ->
                        FoodSuggestionItem(
                            food = food,
                            onClick = {
                                foodName = food.name
                                showSuggestions = false
                                // Get nutritional preview
                                quantity.toFloatOrNull()?.let { qty ->
                                    viewModel.getNutritionalPreview(food.name, qty)
                                }
                            }
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Quantity Input
        OutlinedTextField(
            value = quantity,
            onValueChange = { 
                quantity = it
                // Update nutritional preview when quantity changes
                if (foodName.isNotEmpty() && it.isNotEmpty()) {
                    it.toFloatOrNull()?.let { qty ->
                        viewModel.getNutritionalPreview(foodName, qty)
                    }
                }
            },
            label = { Text("Quantity (grams)") },
            placeholder = { Text("e.g., 100") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(Icons.Default.Scale, contentDescription = "Quantity")
            },
            singleLine = true
        )
        
        // Nutritional Preview
        nutritionalPreview?.let { preview ->
            AnimatedVisibility(
                visible = true,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Nutritional Info",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            preview,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Meal Type Selection
        Text(
            "Meal Type",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            mealTypes.forEach { mealType ->
                FilterChip(
                    selected = selectedMealType == mealType,
                    onClick = { selectedMealType = mealType },
                    label = { Text(mealType) },
                    leadingIcon = {
                        Icon(
                            when (mealType) {
                                "Breakfast" -> Icons.Default.WbSunny
                                "Lunch" -> Icons.Default.Restaurant
                                "Dinner" -> Icons.Default.NightsStay
                                else -> Icons.Default.Coffee
                            },
                            contentDescription = mealType
                        )
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Log Food Button
        Button(
            onClick = {
                if (foodName.isNotEmpty() && quantity.isNotEmpty()) {
                    quantity.toFloatOrNull()?.let { qty ->
                        viewModel.addFoodLogWithNutritionalData(foodName, qty, "g", selectedMealType, profileId)
                        // Clear inputs
                        foodName = ""
                        quantity = ""
                        viewModel.clearNutritionalPreview()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = foodName.isNotEmpty() && quantity.isNotEmpty()
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Log Food")
        }
        
        // Quick Add Common Foods
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            "Quick Add Common Foods",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        LaunchedEffect(Unit) {
            viewModel.getCommonFoods()
        }
        
        LazyColumn(
            modifier = Modifier.heightIn(max = 200.dp)
        ) {
            items(foodSuggestions.take(10)) { food ->
                QuickAddFoodItem(
                    food = food,
                    onAdd = { qty ->
                        viewModel.addFoodLogWithNutritionalData(food.name, qty, "g", selectedMealType, profileId)
                    }
                )
            }
        }
        
        // Error Message
        errorMessage?.let { message ->
            AnimatedVisibility(
                visible = true,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FoodSuggestionItem(
    food: NutritionalInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    food.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "${food.caloriesPer100g.toInt()} kcal/100g • ${food.category}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            
            Icon(
                Icons.Default.Add,
                contentDescription = "Add",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun QuickAddFoodItem(
    food: NutritionalInfo,
    onAdd: (Float) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    food.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    "${food.caloriesPer100g.toInt()} kcal/100g",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(50f, 100f, 150f).forEach { qty ->
                    TextButton(
                        onClick = { onAdd(qty) },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    ) {
                        Text("${qty.toInt()}g")
                    }
                }
            }
        }
    }
} 