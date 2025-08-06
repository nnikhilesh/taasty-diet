package com.example.tastydiet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.tastydiet.data.models.NutritionalInfo
import com.example.tastydiet.viewmodel.SimpleFoodLogViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodDialog(
    viewModel: SimpleFoodLogViewModel,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFood by remember { mutableStateOf<NutritionalInfo?>(null) }
    var quantity by remember { mutableStateOf("1.0") }
    var selectedUnit by remember { mutableStateOf("g") }
    var selectedMealType by remember { mutableStateOf("Breakfast") }
    var searchResults by remember { mutableStateOf<List<NutritionalInfo>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    
    val coroutineScope = rememberCoroutineScope()
    val mealTypes = listOf("Breakfast", "Lunch", "Dinner", "Snack")
    val units = listOf("g", "pieces", "cups", "tbsp", "tsp", "ml", "oz")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Food",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { query ->
                        searchQuery = query
                        if (query.isNotEmpty()) {
                            isSearching = true
                            coroutineScope.launch {
                                try {
                                    val results = viewModel.searchFood(query)
                                    searchResults = results
                                } catch (e: Exception) {
                                    searchResults = emptyList()
                                } finally {
                                    isSearching = false
                                }
                            }
                        } else {
                            searchResults = emptyList()
                        }
                    },
                    label = { Text("Search for food...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    singleLine = true
                )

                // Content
                if (selectedFood == null) {
                    // Search Results
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isSearching) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        } else if (searchResults.isEmpty() && searchQuery.isNotEmpty()) {
                            item {
                                Text(
                                    text = "No foods found",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        } else {
                            items(searchResults) { food ->
                                FoodSearchItem(
                                    food = food,
                                    onClick = { selectedFood = food }
                                )
                            }
                        }
                    }
                } else {
                    // Food Details and Quantity Selection
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Selected Food Info
                        item {
                            SelectedFoodCard(food = selectedFood!!)
                        }

                        // Meal Type Selection
                        item {
                            Text(
                                text = "Meal Type",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                mealTypes.forEach { mealType ->
                                    FilterChip(
                                        selected = selectedMealType == mealType,
                                        onClick = { selectedMealType = mealType },
                                        label = { Text(mealType) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // Quantity and Unit Selection
                        item {
                            Text(
                                text = "Quantity",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = quantity,
                                    onValueChange = { quantity = it },
                                    label = { Text("Amount") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                                
                                                                 var expanded by remember { mutableStateOf(false) }
                                 Box(
                                     modifier = Modifier.weight(1f)
                                 ) {
                                     OutlinedTextField(
                                         value = selectedUnit,
                                         onValueChange = { },
                                         readOnly = true,
                                         label = { Text("Unit") },
                                         trailingIcon = {
                                             Icon(
                                                 if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                                 contentDescription = "Select Unit"
                                             )
                                         },
                                         modifier = Modifier.clickable { expanded = true }
                                     )
                                     
                                     DropdownMenu(
                                         expanded = expanded,
                                         onDismissRequest = { expanded = false }
                                     ) {
                                         units.forEach { unit ->
                                             DropdownMenuItem(
                                                 text = { Text(unit) },
                                                 onClick = { 
                                                     selectedUnit = unit
                                                     expanded = false
                                                 }
                                             )
                                         }
                                     }
                                 }
                            }
                        }

                        // Nutrition Preview
                        item {
                                                         val qty = quantity.toFloatOrNull() ?: 1f
                             val calories = selectedFood!!.caloriesPer100g * qty / 100f
                             val protein = selectedFood!!.proteinPer100g * qty / 100f
                             val carbs = selectedFood!!.carbsPer100g * qty / 100f
                             val fat = selectedFood!!.fatPer100g * qty / 100f
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp)
                                ) {
                                    Text(
                                        text = "Nutrition Preview",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Calories: ${calories.toInt()} kcal")
                                    Text("Protein: ${protein.toInt()}g")
                                    Text("Carbs: ${carbs.toInt()}g")
                                    Text("Fat: ${fat.toInt()}g")
                                }
                            }
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (selectedFood != null) {
                                selectedFood = null
                            } else {
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (selectedFood != null) "Back" else "Cancel")
                    }
                    
                    Button(
                        onClick = {
                            if (selectedFood != null) {
                                val qty = quantity.toFloatOrNull() ?: 1f
                                viewModel.logFood(
                                    foodName = selectedFood!!.name,
                                    quantity = qty,
                                    unit = selectedUnit,
                                    mealType = selectedMealType
                                )
                                onDismiss()
                            }
                        },
                        enabled = selectedFood != null && quantity.toFloatOrNull() != null,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Add Food")
                    }
                }
            }
        }
    }
}

@Composable
fun FoodSearchItem(
    food: NutritionalInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = food.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                                 Text(
                     text = "${food.caloriesPer100g} kcal • ${food.proteinPer100g}g protein • ${food.carbsPer100g}g carbs • ${food.fatPer100g}g fat",
                     style = MaterialTheme.typography.bodySmall,
                     color = MaterialTheme.colorScheme.onSurfaceVariant
                 )
            }
            
            Icon(
                Icons.Default.Add,
                contentDescription = "Add",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun SelectedFoodCard(food: NutritionalInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = food.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
                         Text(
                 text = "Per 100g: ${food.caloriesPer100g} kcal, ${food.proteinPer100g}g protein, ${food.carbsPer100g}g carbs, ${food.fatPer100g}g fat",
                 style = MaterialTheme.typography.bodySmall,
                 color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
             )
        }
    }
}

 