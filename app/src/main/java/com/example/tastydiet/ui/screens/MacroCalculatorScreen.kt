package com.example.tastydiet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tastydiet.viewmodel.FoodMacroViewModel
import com.example.tastydiet.data.models.NutritionalInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MacroCalculatorScreen(
    modifier: Modifier = Modifier,
    viewModel: FoodMacroViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFoodItem by remember { mutableStateOf<NutritionalInfo?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showAddFoodDialog by remember { mutableStateOf(false) }
    
    val uiState by viewModel.uiState.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val allFoodItems by viewModel.allFoodItems.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            // Header with reload button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Macro Calculator",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                // Reload database button
                Button(
                    onClick = { viewModel.reloadComprehensiveDatabase() },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("Reload DB")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Status messages
            if (isLoading) {
                Text(
                    text = "Loading database...",
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
            
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    viewModel.searchFoodItems(it)
                },
                label = { Text("Search for food items") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Search Results
            if (searchQuery.isNotBlank()) {
                Text(
                    text = "Search Results",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(searchResults) { foodItem ->
                        FoodItemCard(
                            foodItem = foodItem,
                            isSelected = selectedFoodItem?.name == foodItem.name,
                            onClick = { selectedFoodItem = foodItem }
                        )
                    }
                }
            } else {
                // All Food Items
                Text(
                    text = "All Food Items",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allFoodItems) { foodItem ->
                        FoodItemCard(
                            foodItem = foodItem,
                            isSelected = selectedFoodItem?.name == foodItem.name,
                            onClick = { selectedFoodItem = foodItem }
                        )
                    }
                }
            }
            
            // Selected Food Item Details
            selectedFoodItem?.let { foodItem ->
                Spacer(modifier = Modifier.height(16.dp))
                
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
                            text = foodItem.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text("Calories: ${(foodItem.caloriesPer100g * 100).toInt()} cal/100g")
                        Text("Protein: ${(foodItem.proteinPer100g * 100).toInt()}g/100g")
                        Text("Carbs: ${(foodItem.carbsPer100g * 100).toInt()}g/100g")
                        Text("Fat: ${(foodItem.fatPer100g * 100).toInt()}g/100g")
                        Text("Fiber: ${(foodItem.fiberPer100g * 100).toInt()}g/100g")
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = { showAddDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add to Food Log")
                        }
                    }
                }
            }
        }
        
        // Floating Action Button for adding new food items
        FloatingActionButton(
            onClick = { showAddFoodDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Food Item",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
    
    // Add Food Item Dialog
    if (showAddDialog && selectedFoodItem != null) {
        AddFoodItemDialog(
            foodItem = selectedFoodItem!!,
            onDismiss = { showAddDialog = false },
            onConfirm = { quantity ->
                viewModel.addFoodItemToLog(selectedFoodItem!!, quantity)
                showAddDialog = false
            }
        )
    }
    
    // Add New Food Item Dialog
    if (showAddFoodDialog) {
        AddNewFoodItemDialog(
            onDismiss = { showAddFoodDialog = false },
            onConfirm = { name, calories, protein, carbs, fat, fiber, category ->
                val newFoodItem = NutritionalInfo(
                    name = name,
                    caloriesPer100g = calories,
                    proteinPer100g = protein,
                    carbsPer100g = carbs,
                    fatPer100g = fat,
                    fiberPer100g = fiber,
                    category = category
                )
                viewModel.addFoodItem(newFoodItem)
                showAddFoodDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodItemCard(
    foodItem: NutritionalInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(if (isSelected) 4.dp else 1.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = foodItem.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${(foodItem.caloriesPer100g * 100).toInt()} cal/100g",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "P: ${(foodItem.proteinPer100g * 100).toInt()}g | C: ${(foodItem.carbsPer100g * 100).toInt()}g | F: ${(foodItem.fatPer100g * 100).toInt()}g",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNewFoodItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Float, Float, Float, Float, Float, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }
    var protein by remember { mutableStateOf("") }
    var carbs by remember { mutableStateOf("") }
    var fat by remember { mutableStateOf("") }
    var fiber by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("General") }
    
    val categories = listOf("General", "Grains", "Proteins", "Dairy", "Vegetables", "Fruits", "Nuts", "Oils", "Spices", "Beverages", "Processed", "Indian Dishes")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Food Item") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Food Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = calories,
                    onValueChange = { calories = it },
                    label = { Text("Calories per 100g (e.g., 580 for 580 cal)") },
                    placeholder = { Text("580") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = protein,
                    onValueChange = { protein = it },
                    label = { Text("Protein per 100g (e.g., 20 for 20g)") },
                    placeholder = { Text("20") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = carbs,
                    onValueChange = { carbs = it },
                    label = { Text("Carbs per 100g (e.g., 30 for 30g)") },
                    placeholder = { Text("30") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = fat,
                    onValueChange = { fat = it },
                    label = { Text("Fat per 100g (e.g., 10 for 10g)") },
                    placeholder = { Text("10") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = fiber,
                    onValueChange = { fiber = it },
                    label = { Text("Fiber per 100g (e.g., 5 for 5g)") },
                    placeholder = { Text("5") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = { },
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val nameVal = name.trim()
                    // Convert user-friendly values to decimal format for storage
                    val caloriesVal = (calories.toFloatOrNull() ?: 0f) / 100f  // 580 -> 5.8
                    val proteinVal = (protein.toFloatOrNull() ?: 0f) / 100f    // 20 -> 0.2
                    val carbsVal = (carbs.toFloatOrNull() ?: 0f) / 100f        // 30 -> 0.3
                    val fatVal = (fat.toFloatOrNull() ?: 0f) / 100f           // 10 -> 0.1
                    val fiberVal = (fiber.toFloatOrNull() ?: 0f) / 100f       // 5 -> 0.05
                    
                    if (nameVal.isNotBlank()) {
                        onConfirm(nameVal, caloriesVal, proteinVal, carbsVal, fatVal, fiberVal, category)
                    }
                },
                enabled = name.trim().isNotBlank()
            ) {
                Text("Add")
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
fun AddFoodItemDialog(
    foodItem: NutritionalInfo,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit
) {
    var quantity by remember { mutableStateOf("100") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add ${foodItem.name}") },
        text = {
            Column {
                Text("Enter quantity (grams):")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity (g)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantity.toFloatOrNull() ?: 100f
                    onConfirm(qty)
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
