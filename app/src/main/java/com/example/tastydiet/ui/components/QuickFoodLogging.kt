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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tastydiet.data.models.FoodItem
import com.example.tastydiet.data.models.Profile
import com.example.tastydiet.data.models.Recipe
import com.example.tastydiet.data.models.NutritionalInfo
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickFoodLogging(
    profiles: List<Profile>,
    recentFoods: List<FoodItem>,
    nutritionalSuggestions: List<NutritionalInfo>,
    selectedProfile: Profile?,
    selectedDate: java.time.LocalDate,
    onFoodLogged: (String, Float, String, String, Int, java.time.LocalDate) -> Unit,
    onSearchQuery: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showSearchResults by remember { mutableStateOf(false) }
    var selectedMealType by remember { mutableStateOf("Breakfast") }
    var quantity by remember { mutableStateOf("100") }
    var selectedUnit by remember { mutableStateOf("g") }
    var showUnitMenu by remember { mutableStateOf(false) }
    var selectedFood by remember { mutableStateOf<NutritionalInfo?>(null) }
    var macroPreview by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLogging by remember { mutableStateOf(false) }
    val mealTypes = listOf("Breakfast", "Lunch", "Dinner", "Snack")
    val units = listOf("g", "pieces", "cups", "tbsp", "tsp", "ml", "oz")
    
    // Calculate macro preview when food and quantity change
    LaunchedEffect(selectedFood, quantity) {
        if (selectedFood != null && quantity.isNotEmpty()) {
            val qty = quantity.toFloatOrNull() ?: 100f
            val multiplier = qty / 100f
            val calories = (selectedFood!!.caloriesPer100g * multiplier).toInt()
            val protein = (selectedFood!!.proteinPer100g * multiplier).toInt()
            val carbs = (selectedFood!!.carbsPer100g * multiplier).toInt()
            val fat = (selectedFood!!.fatPer100g * multiplier).toInt()
            
            macroPreview = "$calories cal • ${protein}g protein • ${carbs}g carbs • ${fat}g fat"
        } else {
            macroPreview = null
        }
    }
    
    // Auto-detect unit based on search query (only if user hasn't manually selected a unit)
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            onSearchQuery(searchQuery)
            showSearchResults = true
            
            // Auto-detect unit based on food type (only if user hasn't manually selected a unit)
            val lowerQuery = searchQuery.lowercase()
            val suggestedUnit = when {
                lowerQuery.contains("egg") || lowerQuery.contains("banana") || lowerQuery.contains("apple") || 
                lowerQuery.contains("orange") || lowerQuery.contains("tomato") -> "pieces"
                lowerQuery.contains("milk") || lowerQuery.contains("water") || lowerQuery.contains("juice") -> "ml"
                lowerQuery.contains("oil") || lowerQuery.contains("sauce") -> "tbsp"
                lowerQuery.contains("salt") || lowerQuery.contains("pepper") || lowerQuery.contains("spice") -> "tsp"
                else -> "g"
            }
            
            // Only auto-select unit if it's still the default "g" or if user hasn't manually changed it
            if (selectedUnit == "g" || selectedUnit == "pieces" && suggestedUnit == "pieces") {
                selectedUnit = suggestedUnit
            }
            
            // Try to find the food in suggestions and set it as selectedFood
            if (nutritionalSuggestions.isNotEmpty()) {
                val matchingFood = nutritionalSuggestions.find { 
                    it.name.lowercase().contains(searchQuery.lowercase()) 
                }
                if (matchingFood != null) {
                    selectedFood = matchingFood
                }
            }
        } else {
            showSearchResults = false
            selectedFood = null
            macroPreview = null
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
            modifier = Modifier.padding(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Smart Food Log",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                
                // Show current profile and date info
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        selectedProfile?.name ?: "No Profile",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        selectedDate.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Meal type selector
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(mealTypes) { mealType ->
                    FilterChip(
                        selected = selectedMealType == mealType,
                        onClick = { selectedMealType = mealType },
                        label = { Text(mealType) },
                        modifier = Modifier.height(32.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Search input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search food...") },
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
            

            
            // Quantity and Unit input
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
                
                // Unit selection dropdown
                Box {
                    OutlinedButton(
                        onClick = { showUnitMenu = true },
                        modifier = Modifier.width(80.dp)
                    ) {
                        Text(
                            selectedUnit,
                            style = MaterialTheme.typography.bodySmall
                        )
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
            
                         // Log button
             Button(
                 onClick = {
                     val currentProfile = selectedProfile
                     if (searchQuery.isNotBlank() && quantity.isNotBlank() && !isLogging && currentProfile != null) {
                         isLogging = true
                         errorMessage = null
                         
                         try {
                             val qty = quantity.toFloatOrNull()
                             if (qty == null || qty <= 0) {
                                 errorMessage = "Please enter a valid quantity (greater than 0)"
                                 isLogging = false
                                 return@Button
                             }
                             
                             val profileId = currentProfile.id
                             
                             // Debug info
                             errorMessage = "Logging: $searchQuery, $qty $selectedUnit, $selectedMealType, Profile: ${currentProfile.name}, Date: ${selectedDate.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd"))}"
                             
                             onFoodLogged(searchQuery, qty, selectedUnit, selectedMealType, profileId, selectedDate)
                             
                             // Clear form after logging
                             searchQuery = ""
                             quantity = ""
                             selectedMealType = "Snack"
                             
                             // Reset logging state after a delay
                             CoroutineScope(Dispatchers.Main).launch {
                                 delay(1000)
                                 isLogging = false
                                 errorMessage = null
                             }
                         } catch (e: Exception) {
                             errorMessage = "Failed to log food: ${e.message}"
                             isLogging = false
                         }
                     } else if (currentProfile == null) {
                         errorMessage = "Please select a profile in Today's Progress first"
                     }
                 },
                modifier = Modifier.fillMaxWidth(),
                enabled = searchQuery.isNotBlank() && quantity.isNotBlank() && !isLogging && selectedProfile != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (searchQuery.isNotBlank() && quantity.isNotBlank() && selectedProfile != null) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            ) {
                if (isLogging) {
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
                    Text("Log ${searchQuery.ifBlank { "Food" }}")
                }
            }
            
            // Error message
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            errorMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            
            // Search results
            if (showSearchResults && nutritionalSuggestions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "Suggestions:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(nutritionalSuggestions.take(5)) { suggestion ->
                        SuggestionChip(
                            onClick = {
                                searchQuery = suggestion.name
                                selectedFood = suggestion
                                showSearchResults = false
                            },
                            label = { Text(suggestion.name) }
                        )
                    }
                }
            }
            
            // Macro preview
            if (macroPreview != null && selectedFood != null) {
                Spacer(modifier = Modifier.height(8.dp))
                
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
                            "Macro Preview for ${selectedFood!!.name}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            macroPreview!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
} 