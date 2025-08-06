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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.tastydiet.data.models.Profile
import com.example.tastydiet.data.models.FoodLog
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ripple.rememberRipple
import kotlinx.coroutines.delay

data class UpdatedMacros(
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyMacroSummary(
    profiles: List<Profile>,
    selectedProfile: Profile?,
    consumedCalories: Float,
    consumedProtein: Float,
    consumedCarbs: Float,
    consumedFat: Float,
    consumedFiber: Float,
    foodLogs: List<FoodLog> = emptyList(),
    onProfileSelected: (Profile) -> Unit = {},
    onDateSelected: (LocalDate) -> Unit = {},
    onFoodLogUpdated: (FoodLog) -> Unit = {},
    onFoodLogDeleted: (FoodLog) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (profiles.isEmpty()) return
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showProfileMenu by remember { mutableStateOf(false) }
    var showMacroBreakdown by remember { mutableStateOf(false) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    
    val currentProfile = selectedProfile ?: profiles.firstOrNull()
    if (currentProfile == null) return
    
    val targetCalories = currentProfile.targetCalories
    val targetProtein = currentProfile.targetProtein
    val targetCarbs = currentProfile.targetCarbs
    val targetFat = currentProfile.targetFat
    val targetFiber = currentProfile.targetFiber
    
    val caloriesProgress = (consumedCalories / targetCalories).coerceIn(0f, 1f)
    val proteinProgress = (consumedProtein / targetProtein).coerceIn(0f, 1f)
    val carbsProgress = (consumedCarbs / targetCarbs).coerceIn(0f, 1f)
    val fatProgress = (consumedFat / targetFat).coerceIn(0f, 1f)
    val fiberProgress = (consumedFiber / targetFiber).coerceIn(0f, 1f)
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // Header with profile and date selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Timeline,
                        contentDescription = "Daily Progress",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(6.dp))
                    
                    Text(
                        "Today's Progress",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Profile selector
                    if (profiles.isNotEmpty()) {
                        TextButton(
                            onClick = { showProfileMenu = true },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            Text(
                                currentProfile.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Select Profile",
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    
                    // Date selector
                    TextButton(
                        onClick = { showDatePicker = true },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Text(
                            selectedDate.format(DateTimeFormatter.ofPattern("MMM dd")),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = "Select Date",
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Compact macro summary with clickable area
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
                ),
                onClick = { showMacroBreakdown = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Calories
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "${consumedCalories.toInt()}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "cal",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        LinearProgressIndicator(
                            progress = caloriesProgress,
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp),
                            color = Color(0xFFFF5722),
                            trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                        )
                    }
                    
                    // Protein
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "${consumedProtein.toInt()}g",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "protein",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        LinearProgressIndicator(
                            progress = proteinProgress,
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp),
                            color = Color(0xFF4CAF50),
                            trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                        )
                    }
                    
                    // Carbs
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "${consumedCarbs.toInt()}g",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "carbs",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        LinearProgressIndicator(
                            progress = carbsProgress,
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp),
                            color = Color(0xFFFF9800),
                            trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                        )
                    }
                    
                    // Fat
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "${consumedFat.toInt()}g",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "fat",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        LinearProgressIndicator(
                            progress = fatProgress,
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp),
                            color = Color(0xFF9C27B0),
                            trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                "Tap to view detailed breakdown",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable { showMacroBreakdown = true }
            )
        }
    }
    
    // Date picker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.toEpochDay() * 24 * 60 * 60 * 1000
        )
        
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val newDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                            selectedDate = newDate
                            onDateSelected(newDate)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                showModeToggle = false
            )
        }
    }
    
    // Profile selection dialog
    if (showProfileMenu) {
        Dialog(
            onDismissRequest = { showProfileMenu = false },
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        "Select Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    profiles.forEach { profile ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (profile.id == currentProfile.id) 
                                    MaterialTheme.colorScheme.primaryContainer 
                                else 
                                    MaterialTheme.colorScheme.surfaceVariant
                            ),
                            onClick = {
                                onProfileSelected(profile)
                                showProfileMenu = false
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (profile.id == currentProfile.id) 
                                        MaterialTheme.colorScheme.onPrimaryContainer 
                                    else 
                                        MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(24.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        profile.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = if (profile.id == currentProfile.id) 
                                            MaterialTheme.colorScheme.onPrimaryContainer 
                                        else 
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                    
                                    val bmi = profile.calculateBMI()
                                    val bmiCategory = profile.getBMICategory()
                                    Text(
                                        "BMI: ${String.format("%.1f", bmi)} ($bmiCategory)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (profile.id == currentProfile.id) 
                                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                        else 
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                if (profile.id == currentProfile.id) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    TextButton(
                        onClick = { showProfileMenu = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
    
    // Macro breakdown dialog
    if (showMacroBreakdown) {
        MacroBreakdownDialog(
            selectedProfile = currentProfile,
            foodLogs = foodLogs,
            consumedCalories = consumedCalories,
            consumedProtein = consumedProtein,
            consumedCarbs = consumedCarbs,
            consumedFat = consumedFat,
            consumedFiber = consumedFiber,
            targetCalories = targetCalories,
            targetProtein = targetProtein,
            targetCarbs = targetCarbs,
            targetFat = targetFat,
            targetFiber = targetFiber,
            onDismiss = { showMacroBreakdown = false },
            onFoodLogUpdated = onFoodLogUpdated,
            onFoodLogDeleted = onFoodLogDeleted
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MacroBreakdownDialog(
    selectedProfile: Profile,
    foodLogs: List<FoodLog>,
    consumedCalories: Float,
    consumedProtein: Float,
    consumedCarbs: Float,
    consumedFat: Float,
    consumedFiber: Float,
    targetCalories: Float,
    targetProtein: Float,
    targetCarbs: Float,
    targetFat: Float,
    targetFiber: Float,
    onDismiss: () -> Unit,
    onFoodLogUpdated: (FoodLog) -> Unit,
    onFoodLogDeleted: (FoodLog) -> Unit
) {
    var selectedMealType by remember { mutableStateOf("All") }
    val mealTypes = listOf("All", "Breakfast", "Lunch", "Dinner", "Snack")
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Macro Breakdown",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Overall Progress Summary
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
                            "Overall Progress",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            MacroProgressItem(
                                label = "Calories",
                                consumed = consumedCalories.toInt(),
                                target = targetCalories.toInt(),
                                progress = (consumedCalories / targetCalories).coerceIn(0f, 1f),
                                color = Color(0xFFFF5722)
                            )
                            
                            MacroProgressItem(
                                label = "Protein",
                                consumed = consumedProtein.toInt(),
                                target = targetProtein.toInt(),
                                progress = (consumedProtein / targetProtein).coerceIn(0f, 1f),
                                color = Color(0xFF4CAF50)
                            )
                            
                            MacroProgressItem(
                                label = "Carbs",
                                consumed = consumedCarbs.toInt(),
                                target = targetCarbs.toInt(),
                                progress = (consumedCarbs / targetCarbs).coerceIn(0f, 1f),
                                color = Color(0xFFFF9800)
                            )
                            
                            MacroProgressItem(
                                label = "Fat",
                                consumed = consumedFat.toInt(),
                                target = targetFat.toInt(),
                                progress = (consumedFat / targetFat).coerceIn(0f, 1f),
                                color = Color(0xFF9C27B0)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Meal Type Filter
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(mealTypes) { mealType ->
                        FilterChip(
                            selected = selectedMealType == mealType,
                            onClick = { selectedMealType = mealType },
                            label = { Text(mealType) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Food Logs by Meal Type
                val filteredLogs = if (selectedMealType == "All") {
                    foodLogs
                } else {
                    foodLogs.filter { it.mealType == selectedMealType }
                }
                
                if (filteredLogs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No food logged for ${selectedMealType.lowercase()}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredLogs) { foodLog ->
                            EditableFoodLogItem(
                                foodLog = foodLog,
                                onUpdate = onFoodLogUpdated,
                                onDelete = onFoodLogDeleted
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditableFoodLogItem(
    foodLog: FoodLog,
    onUpdate: (FoodLog) -> Unit,
    onDelete: (FoodLog) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var editedQuantity by remember { mutableStateOf(foodLog.quantity.toString()) }
    var editedUnit by remember { mutableStateOf(foodLog.unit) }
    var showUnitDropdown by remember { mutableStateOf(false) }
    val units = listOf("g", "pieces", "cups", "tbsp", "tsp", "ml", "oz", "serving", "servings", "kg", "lb")
    
    // Auto-dismiss delete dialog after 5 seconds
    LaunchedEffect(showDeleteDialog) {
        if (showDeleteDialog) {
            delay(5000)
            showDeleteDialog = false
        }
    }
    
    // Debug logging
    LaunchedEffect(showUnitDropdown) {
        android.util.Log.d("EditableFoodLogItem", "Unit dropdown state: $showUnitDropdown")
    }
    
    // Calculate updated macros based on quantity and unit changes
    val updatedMacros = remember(editedQuantity, editedUnit) {
        val newQuantity = editedQuantity.toFloatOrNull() ?: foodLog.quantity
        val originalQuantity = foodLog.quantity
        
        // Calculate per-unit values first
        val caloriesPerUnit = foodLog.calories / originalQuantity
        val proteinPerUnit = foodLog.protein / originalQuantity
        val carbsPerUnit = foodLog.carbs / originalQuantity
        val fatPerUnit = foodLog.fat / originalQuantity
        
        // Calculate new totals based on new quantity
        UpdatedMacros(
            calories = (caloriesPerUnit * newQuantity).toInt(),
            protein = (proteinPerUnit * newQuantity).toInt(),
            carbs = (carbsPerUnit * newQuantity).toInt(),
            fat = (fatPerUnit * newQuantity).toInt()
        )
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    foodLog.foodName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    "${foodLog.quantity} ${foodLog.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Text(
                    "${foodLog.calories.toInt()} cal • ${foodLog.protein.toInt()}g protein • ${foodLog.carbs.toInt()}g carbs • ${foodLog.fat.toInt()}g fat",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    onClick = { 
                        showEditDialog = true 
                    },
                    modifier = Modifier.height(40.dp),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Default.Edit, 
                        contentDescription = "Edit",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit", style = MaterialTheme.typography.bodySmall)
                }
                
                TextButton(
                    onClick = { 
                        showDeleteDialog = true
                    },
                    modifier = Modifier.height(40.dp),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Default.Delete, 
                        contentDescription = "Delete",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
    
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { 
                showEditDialog = false 
            },
            title = { Text("Edit Food Log") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = editedQuantity,
                        onValueChange = { editedQuantity = it },
                        label = { Text("Quantity") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Unit selection dropdown
                    Column {
                        Text(
                            "Unit",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Box {
                            OutlinedButton(
                                onClick = { showUnitDropdown = !showUnitDropdown },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        editedUnit,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Icon(
                                        if (showUnitDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Select unit",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            
                            DropdownMenu(
                                expanded = showUnitDropdown,
                                onDismissRequest = { showUnitDropdown = false },
                                modifier = Modifier.width(IntrinsicSize.Min)
                            ) {
                                units.forEach { unit ->
                                    DropdownMenuItem(
                                        text = { Text(unit) },
                                        onClick = { 
                                            editedUnit = unit
                                            showUnitDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    
                    // Updated macros preview
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(
                                "Updated Macros:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                MacroPreviewItem("Calories", "${updatedMacros.calories} cal", MaterialTheme.colorScheme.primary)
                                MacroPreviewItem("Protein", "${updatedMacros.protein}g", MaterialTheme.colorScheme.secondary)
                                MacroPreviewItem("Carbs", "${updatedMacros.carbs}g", MaterialTheme.colorScheme.tertiary)
                                MacroPreviewItem("Fat", "${updatedMacros.fat}g", MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val newQuantity = editedQuantity.toFloatOrNull() ?: foodLog.quantity
                        val originalQuantity = foodLog.quantity
                        
                        // Calculate per-unit values first
                        val caloriesPerUnit = foodLog.calories / originalQuantity
                        val proteinPerUnit = foodLog.protein / originalQuantity
                        val carbsPerUnit = foodLog.carbs / originalQuantity
                        val fatPerUnit = foodLog.fat / originalQuantity
                        val fiberPerUnit = foodLog.fiber / originalQuantity
                        
                        val updatedFoodLog = foodLog.copy(
                            quantity = newQuantity,
                            unit = editedUnit,
                            calories = caloriesPerUnit * newQuantity,
                            protein = proteinPerUnit * newQuantity,
                            carbs = carbsPerUnit * newQuantity,
                            fat = fatPerUnit * newQuantity,
                            fiber = fiberPerUnit * newQuantity
                        )
                        onUpdate(updatedFoodLog)
                        showEditDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Elegant Delete Confirmation Dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    "Delete Food Item",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    "Are you sure you want to delete \"${foodLog.foodName}\"?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(foodLog)
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun MacroProgressItem(
    label: String,
    consumed: Int,
    target: Int,
    progress: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        
        Text(
            "$consumed/$target",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .width(60.dp)
                .height(6.dp),
            color = color,
            trackColor = color.copy(alpha = 0.3f)
        )
    }
}

@Composable
fun MacroProgressItem(
    label: String,
    consumed: Int,
    target: Int,
    progress: Float,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
        )
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
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                "$consumed/$target",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Progress Bar
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = color,
                trackColor = color.copy(alpha = 0.3f)
            )
        }
    }
} 

@Composable
fun MacroPreviewItem(
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