package com.example.tastydiet.ui

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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tastydiet.data.models.FoodLog
import com.example.tastydiet.data.models.Profile
import com.example.tastydiet.viewmodel.FoodLogViewModel
import com.example.tastydiet.viewmodel.ProfileViewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.example.tastydiet.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodLoggingScreen(
    foodLogViewModel: FoodLogViewModel,
    profileViewModel: ProfileViewModel,
    modifier: Modifier = Modifier
) {
    val todayFoodLogs by foodLogViewModel.todayFoodLogs.collectAsStateWithLifecycle()
    val selectedProfileId by foodLogViewModel.selectedProfileId.collectAsStateWithLifecycle()
    val todayTotals by foodLogViewModel.todayTotals.collectAsStateWithLifecycle()
    val isLoading by foodLogViewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by foodLogViewModel.errorMessage.collectAsStateWithLifecycle()
    
    val profiles by profileViewModel.profiles.collectAsStateWithLifecycle()
    val selectedProfile by profileViewModel.selectedProfile.collectAsStateWithLifecycle()
    
    var showAddDialog by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    
    LaunchedEffect(selectedProfile) {
        selectedProfile?.let { profile ->
            foodLogViewModel.setSelectedProfile(profile.id)
        }
    }
    
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            kotlinx.coroutines.delay(3000)
            foodLogViewModel.clearErrorMessage()
        }
    }
    
    Column(modifier = modifier.fillMaxSize()) {
        // Header with Profile Selection
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    "Food Logging",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Profile Selection
                if (profiles.isNotEmpty()) {
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedProfile?.name ?: "Select Profile",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Profile") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            profiles.forEach { profile ->
                                DropdownMenuItem(
                                    text = { Text(profile.name) },
                                    onClick = {
                                        profileViewModel.selectProfile(profile)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        "No profiles available. Please add a profile first.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Macro Summary Cards
        selectedProfile?.let { profile ->
            AnimatedMacroSummaryCards(
                todayTotals = todayTotals,
                profile = profile
            )
        }
        
        // Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { 
                    if (selectedProfile != null) {
                        showAddDialog = true
                    } else {
                        foodLogViewModel.setErrorMessage("Please select a profile first")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedProfile != null
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (selectedProfile != null) "Log Food" else "Select Profile First")
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Today's Food Logs
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (todayFoodLogs.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Restaurant,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "No food logged today",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Start logging your meals to track your nutrition",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(todayFoodLogs) { foodLog ->
                    FoodLogCard(
                        foodLog = foodLog,
                        onDelete = { foodLogViewModel.deleteFoodLog(foodLog) }
                    )
                }
            }
        }
    }
    
    // Add Food Dialog
    if (showAddDialog) {
        AddFoodDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, quantity, mealType, calories, protein, carbs, fat ->
                selectedProfile?.let { profile ->
                    val foodLog = FoodLog(
                        profileId = profile.id,
                        foodName = name,
                        mealType = mealType,
                        quantity = quantity,
                        calories = calories,
                        protein = protein,
                        carbs = carbs,
                        fat = fat
                    )
                    foodLogViewModel.addFoodLog(foodLog)
                    showAddDialog = false
                } ?: run {
                    // Handle case when no profile is selected
                    foodLogViewModel.setErrorMessage("Please select a profile first")
                }
            }
        )
    }
    
    // Error Snackbar
    errorMessage?.let { message ->
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                TextButton(onClick = { foodLogViewModel.clearErrorMessage() }) {
                    Text("Dismiss")
                }
            }
        ) {
            Text(message)
        }
    }
}

@Composable
fun AnimatedMacroSummaryCards(
    todayTotals: FoodLogViewModel.TodayTotals,
    profile: Profile
) {
    val animatedCalories by animateFloatAsState(
        targetValue = todayTotals.calories,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "calories"
    )
    val animatedProtein by animateFloatAsState(
        targetValue = todayTotals.protein,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "protein"
    )
    val animatedCarbs by animateFloatAsState(
        targetValue = todayTotals.carbs,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "carbs"
    )
    val animatedFat by animateFloatAsState(
        targetValue = todayTotals.fat,
        animationSpec = tween(1000, easing = EaseOutCubic),
        label = "fat"
    )
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            "Today's Progress",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MacroCard(
                label = "Calories",
                value = animatedCalories.toInt(),
                unit = "kcal",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            MacroCard(
                label = "Protein",
                value = animatedProtein.toInt(),
                unit = "g",
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MacroCard(
                label = "Carbs",
                value = animatedCarbs.toInt(),
                unit = "g",
                color = Color(0xFFFF9800),
                modifier = Modifier.weight(1f)
            )
            MacroCard(
                label = "Fat",
                value = animatedFat.toInt(),
                unit = "g",
                color = Color(0xFFE91E63),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun MacroCard(
    label: String,
    value: Int,
    unit: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                "$label ($unit)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FoodLogCard(
    foodLog: FoodLog,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
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
                    foodLog.foodName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${foodLog.quantity} ${foodLog.unit} • ${foodLog.mealType}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${foodLog.getTotalCalories()} kcal",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "P: ${foodLog.getTotalProtein().toInt()}g • C: ${foodLog.getTotalCarbs().toInt()}g • F: ${foodLog.getTotalFat().toInt()}g",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    foodLog.getFormattedTime(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Float, String, Float, Float, Float, Float) -> Unit
) {
    var foodName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1.0") }
    var mealType by remember { mutableStateOf("Breakfast") }
    var calories by remember { mutableStateOf("0") }
    var protein by remember { mutableStateOf("0") }
    var carbs by remember { mutableStateOf("0") }
    var fat by remember { mutableStateOf("0") }
    
    var nameError by remember { mutableStateOf(false) }
    var quantityError by remember { mutableStateOf(false) }
    var caloriesError by remember { mutableStateOf(false) }
    
    // Focus management
    val nameFocusRequester = remember { FocusRequester() }
    
    // Auto-focus the name field when dialog opens
    LaunchedEffect(Unit) {
        nameFocusRequester.requestFocus()
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Food") },
        text = {
            Column {
                OutlinedTextField(
                    value = foodName,
                    onValueChange = { 
                        foodName = it
                        nameError = false
                    },
                    label = { Text("Food Name") },
                    isError = nameError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(nameFocusRequester)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { 
                        quantity = it
                        quantityError = false
                    },
                    label = { Text("Quantity (servings)") },
                    isError = quantityError,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = mealType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Meal Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        listOf("Breakfast", "Lunch", "Dinner", "Snack").forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    mealType = type
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    "Nutritional Information (per serving)",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row {
                    OutlinedTextField(
                        value = calories,
                        onValueChange = { 
                            calories = it
                            caloriesError = false
                        },
                        label = { Text("Calories") },
                        isError = caloriesError,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = protein,
                        onValueChange = { protein = it },
                        label = { Text("Protein (g)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row {
                    OutlinedTextField(
                        value = carbs,
                        onValueChange = { carbs = it },
                        label = { Text("Carbs (g)") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = fat,
                        onValueChange = { fat = it },
                        label = { Text("Fat (g)") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    try {
                        // Validate inputs
                        nameError = foodName.isBlank()
                        quantityError = quantity.toFloatOrNull() == null || quantity.toFloat() <= 0
                        caloriesError = calories.toFloatOrNull() == null || calories.toFloat() < 0
                        
                        if (!nameError && !quantityError && !caloriesError) {
                            val qty = quantity.toFloat()
                            val cal = calories.toFloat()
                            val prot = protein.toFloatOrNull() ?: 0f
                            val carb = carbs.toFloatOrNull() ?: 0f
                            val fatVal = fat.toFloatOrNull() ?: 0f
                            
                            onAdd(foodName.trim(), qty, mealType, cal, prot, carb, fatVal)
                        }
                    } catch (e: Exception) {
                        // Handle any parsing errors
                        nameError = true
                        quantityError = true
                        caloriesError = true
                    }
                }
            ) {
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