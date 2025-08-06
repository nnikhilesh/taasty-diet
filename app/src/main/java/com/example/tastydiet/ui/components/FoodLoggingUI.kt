package com.example.tastydiet.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tastydiet.data.models.FoodLog
import com.example.tastydiet.data.models.FoodLogEntry
import com.example.tastydiet.data.models.Recipe
import com.example.tastydiet.viewmodel.FoodLogViewModel
import com.example.tastydiet.viewmodel.RecipeViewModel

@Composable
fun FoodLoggingUI(
    foodLogViewModel: FoodLogViewModel,
    recipeViewModel: RecipeViewModel,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedMemberId by remember { mutableStateOf<Int?>(null) }
    
    val todayFoodLogs by foodLogViewModel.todayFoodLogs.collectAsStateWithLifecycle()
    val recipes by recipeViewModel.recipes.collectAsStateWithLifecycle(initialValue = emptyList())
    
    Column(modifier = modifier.fillMaxSize()) {
        // Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Food Logging",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Food Log")
                }
            }
        }
        
        // Today's logs
        TodaysLogSection(
            foodLogViewModel = foodLogViewModel,
            recipeViewModel = recipeViewModel,
            onAddLog = { showAddDialog = true },
            memberId = selectedMemberId
        )
    }
    
    // Add Food Dialog
    if (showAddDialog) {
        AddFoodDialog(
            recipes = recipes,
            onDismiss = { showAddDialog = false },
            onAdd = { recipe, memberId, quantity, mealType ->
                val foodLog = FoodLog(
                    id = 0,
                    profileId = memberId,
                    foodName = recipe.name,
                    mealType = mealType,
                    quantity = quantity,
                    calories = recipe.caloriesPer100g.toFloat(),
                    protein = recipe.proteinPer100g,
                    carbs = recipe.carbsPer100g,
                    fat = recipe.fatPer100g
                )
                foodLogViewModel.addFoodLog(foodLog)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddFoodDialog(
    recipes: List<Recipe>,
    onDismiss: () -> Unit,
    onAdd: (recipe: Recipe, memberId: Int, quantity: Float, mealType: String) -> Unit
) {
    var selectedRecipe by remember { mutableStateOf<Recipe?>(null) }
    var memberId by remember { mutableStateOf<Int?>(null) }
    var quantity by remember { mutableStateOf("1.0") }
    var selectedMealType by remember { mutableStateOf("Breakfast") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Food Log") },
        text = {
            Column {
                // Recipe selection
                Text(
                    "Select Recipe",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(
                    modifier = Modifier.height(200.dp)
                ) {
                    items(recipes) { recipe ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedRecipe?.id == recipe.id) 
                                    MaterialTheme.colorScheme.primaryContainer 
                                else 
                                    MaterialTheme.colorScheme.surface
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = recipe.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${recipe.caloriesPer100g} kcal",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                RadioButton(
                                    selected = selectedRecipe?.id == recipe.id,
                                    onClick = { selectedRecipe = recipe }
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Member ID input (simplified for now)
                OutlinedTextField(
                    value = memberId?.toString() ?: "",
                    onValueChange = { memberId = it.toIntOrNull() },
                    label = { Text("Member ID") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Quantity input
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Meal type selection
                OutlinedTextField(
                    value = selectedMealType,
                    onValueChange = { selectedMealType = it },
                    label = { Text("Meal Type") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (memberId != null && selectedRecipe != null) {
                        val recipe = selectedRecipe!!
                        onAdd(
                            recipe,
                            memberId!!,
                            quantity.toFloatOrNull() ?: 1.0f,
                            selectedMealType
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedRecipe != null && quantity.toFloatOrNull() != null && memberId != null
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Food")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun TodaysLogSection(
    foodLogViewModel: FoodLogViewModel,
    recipeViewModel: RecipeViewModel,
    onAddLog: () -> Unit,
    memberId: Int?
) {
    val todayFoodLogs by foodLogViewModel.todayFoodLogs.collectAsStateWithLifecycle()
    val recipes by recipeViewModel.recipes.collectAsStateWithLifecycle(initialValue = emptyList())
    
    LaunchedEffect(memberId) {
        memberId?.let { id ->
            foodLogViewModel.loadTodayFoodLogs(id)
        }
    }
    
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            "Today's Food Log",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (todayFoodLogs.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Restaurant,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "No food logged today",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.heightIn(max = 400.dp)
            ) {
                items(todayFoodLogs) { foodLog ->
                    FoodLogItem(
                        foodLog = foodLog,
                        onDelete = {
                            foodLogViewModel.deleteFoodLog(foodLog)
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun FoodLogItem(
    foodLog: FoodLog,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = foodLog.foodName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${foodLog.mealType} • ${foodLog.quantity} ${foodLog.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${foodLog.getTotalCalories()} kcal • P: ${foodLog.getTotalProtein().toInt()}g • C: ${foodLog.getTotalCarbs().toInt()}g • F: ${foodLog.getTotalFat().toInt()}g",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = foodLog.getFormattedTime(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
} 