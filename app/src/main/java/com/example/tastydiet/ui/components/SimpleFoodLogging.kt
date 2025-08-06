package com.example.tastydiet.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tastydiet.data.models.Profile
import com.example.tastydiet.data.models.NutritionalInfo
import com.example.tastydiet.viewmodel.SimpleFoodLogViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleFoodLogging(
    viewModel: SimpleFoodLogViewModel,
    modifier: Modifier = Modifier
) {
    val selectedProfile by viewModel.selectedProfile.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val foodLogs by viewModel.foodLogs.collectAsState()
    val totals by viewModel.totals.collectAsState()
    val profiles by viewModel.profiles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("100") }
    var selectedUnit by remember { mutableStateOf("g") }
    var selectedMealType by remember { mutableStateOf("Snack") }
    var showUnitMenu by remember { mutableStateOf(false) }
    var showProfileMenu by remember { mutableStateOf(false) }
    var showMealTypeMenu by remember { mutableStateOf(false) }
    var nutritionalSuggestions by remember { mutableStateOf<List<NutritionalInfo>>(emptyList()) }

    val mealTypes = listOf("Breakfast", "Lunch", "Dinner", "Snack")
    val units = listOf("g", "pieces", "cups", "tbsp", "tsp", "ml", "oz")

    // Search for food when query changes
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            nutritionalSuggestions = viewModel.searchFood(searchQuery)
        } else {
            nutritionalSuggestions = emptyList()
        }
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Text(
                "Food Logging",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Profile Selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Profile:",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Box {
                    OutlinedButton(
                        onClick = { showProfileMenu = true },
                        enabled = profiles.isNotEmpty()
                    ) {
                        Text(selectedProfile?.name ?: "Select Profile")
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Select Profile",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showProfileMenu,
                        onDismissRequest = { showProfileMenu = false }
                    ) {
                        profiles.forEach { profile ->
                            DropdownMenuItem(
                                text = { Text(profile.name) },
                                onClick = {
                                    viewModel.setSelectedProfile(profile)
                                    showProfileMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Date Display
            Text(
                "Date: ${selectedDate.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy"))}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Food Search
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search food...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Food Suggestions
            if (nutritionalSuggestions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(nutritionalSuggestions.take(5)) { suggestion ->
                        SuggestionChip(
                            onClick = { searchQuery = suggestion.name },
                            label = { Text(suggestion.name) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quantity and Unit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                
                Box {
                    OutlinedButton(
                        onClick = { showUnitMenu = true },
                        modifier = Modifier.width(80.dp)
                    ) {
                        Text(selectedUnit)
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Select Unit",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showUnitMenu,
                        onDismissRequest = { showUnitMenu = false }
                    ) {
                        units.forEach { unit ->
                            DropdownMenuItem(
                                text = { Text(unit) },
                                onClick = {
                                    selectedUnit = unit
                                    showUnitMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Meal Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Meal Type:",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Box {
                    OutlinedButton(
                        onClick = { showMealTypeMenu = true }
                    ) {
                        Text(selectedMealType)
                        Icon(
                            Icons.Default.ArrowDropDown,
                            contentDescription = "Select Meal Type",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showMealTypeMenu,
                        onDismissRequest = { showMealTypeMenu = false }
                    ) {
                        mealTypes.forEach { mealType ->
                            DropdownMenuItem(
                                text = { Text(mealType) },
                                onClick = {
                                    selectedMealType = mealType
                                    showMealTypeMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Log Button
            Button(
                onClick = {
                    val qty = quantity.toFloatOrNull() ?: 100f
                    viewModel.logFood(searchQuery, qty, selectedUnit, selectedMealType)
                    searchQuery = ""
                    quantity = "100"
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = searchQuery.isNotBlank() && quantity.isNotBlank() && !isLoading && selectedProfile != null
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Logging...")
                } else {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Log Food")
                }
            }

            // Test Button
            Button(
                onClick = {
                    viewModel.logFood("Apple", 100f, "g", "Snack")
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && selectedProfile != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Test: Log Apple (100g)")
            }

            // Message Display
            if (message != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Info",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            message!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Current Totals
            if (foodLogs.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            "Today's Totals",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Calories: ${totals.calories.toInt()} cal",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            "Protein: ${totals.protein.toInt()}g",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            "Carbs: ${totals.carbs.toInt()}g",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            "Fat: ${totals.fat.toInt()}g",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
} 