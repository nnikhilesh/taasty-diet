package com.example.tastydiet.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import com.example.tastydiet.data.models.Profile
import com.example.tastydiet.util.MacroCalculator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProfileScreen(
    profile: Profile? = null,
    onSave: (Profile) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(profile?.name ?: "") }
    var age by remember { mutableStateOf(profile?.age?.toString() ?: "") }
    var height by remember { mutableStateOf(profile?.height?.toString() ?: "") }
    var weight by remember { mutableStateOf(profile?.weight?.toString() ?: "") }
    var gender by remember { mutableStateOf(profile?.gender ?: "Other") }
    var goal by remember { mutableStateOf(profile?.goal ?: "Maintenance") }
    var goalDurationWeeks by remember { mutableStateOf(profile?.goalDurationInWeeks?.toString() ?: "12") }
    var activityLevel by remember { mutableStateOf(profile?.activityLevel?.toString() ?: "1.4") }
    var expanded by remember { mutableStateOf(false) }
    var goalExpanded by remember { mutableStateOf(false) }
    var goalDurationExpanded by remember { mutableStateOf(false) }
    var activityExpanded by remember { mutableStateOf(false) }
    
    // Manual macro editing fields
    var targetCalories by remember { mutableStateOf(profile?.targetCalories?.toString() ?: "") }
    var targetProtein by remember { mutableStateOf(profile?.targetProtein?.toString() ?: "") }
    var targetCarbs by remember { mutableStateOf(profile?.targetCarbs?.toString() ?: "") }
    var targetFat by remember { mutableStateOf(profile?.targetFat?.toString() ?: "") }
    
    val isEditMode = profile != null
    val title = if (isEditMode) "Edit Profile" else "Add Profile"
    
    val goals = listOf(
        "Weight Loss",
        "Muscle Gain", 
        "Maintenance",
        "Fat Loss (High Protein)",
        "Endurance Training",
        "Keto Diet",
        "Diabetes Management",
        "Custom Plan"
    )
    
    val goalDurations = listOf(
        "4" to "1 month (aggressive)",
        "8" to "2 months (moderate)",
        "12" to "3 months (balanced)",
        "16" to "4 months (gradual)",
        "24" to "6 months (sustainable)",
        "52" to "1 year (long-term)"
    )
    
    val activityLevels = listOf(
        "1.2" to "Sedentary (little or no exercise)",
        "1.375" to "Lightly active (light exercise 1-3 days/week)",
        "1.4" to "Moderately active (moderate exercise 3-5 days/week)",
        "1.55" to "Very active (hard exercise 6-7 days/week)",
        "1.725" to "Extra active (very hard exercise, physical job)",
        "1.9" to "Athlete (very hard exercise, physical job, training twice a day)"
    )
    
    val genders = listOf("Male", "Female", "Other")
    
    val isValid = name.isNotBlank() && 
                  age.isNotBlank() && age.toIntOrNull() != null && age.toInt() > 0 &&
                  height.isNotBlank() && height.toFloatOrNull() != null && height.toFloat() > 0 &&
                  weight.isNotBlank() && weight.toFloatOrNull() != null && weight.toFloat() > 0 &&
                  activityLevel.isNotBlank() && activityLevel.toFloatOrNull() != null
    
    // Focus management
    val nameFocusRequester = remember { FocusRequester() }
    
    // Function to recalculate macros and update the text fields
    fun recalculateMacros() {
        if (isValid) {
            val tempProfile = Profile(
                name = name,
                age = age.toInt(),
                height = height.toFloat(),
                weight = weight.toFloat(),
                gender = gender,
                goal = goal,
                goalDurationInWeeks = goalDurationWeeks.toIntOrNull() ?: 12,
                activityLevel = activityLevel.toFloat()
            )
            
            val macroTargets = MacroCalculator.calculateMacroTargets(tempProfile)
            targetCalories = String.format("%.0f", macroTargets.calories)
            targetProtein = String.format("%.1f", macroTargets.protein)
            targetCarbs = String.format("%.1f", macroTargets.carbs)
            targetFat = String.format("%.1f", macroTargets.fat)
        }
    }
    
    // Initialize macro fields with calculated values if they're empty
    LaunchedEffect(isValid, goal, goalDurationWeeks, activityLevel) {
        if (isValid && targetCalories.isBlank() && targetProtein.isBlank() && 
            targetCarbs.isBlank() && targetFat.isBlank()) {
            recalculateMacros()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Auto-focus the name field when screen opens
            LaunchedEffect(Unit) {
                nameFocusRequester.requestFocus()
            }
            
            // Name Input
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(nameFocusRequester),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
            )
            
            // Age Input
            OutlinedTextField(
                value = age,
                onValueChange = { age = it },
                label = { Text("Age") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Cake, contentDescription = null) },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                )
            )
            
            // Gender Selection
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = gender,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Gender") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    genders.forEach { genderOption ->
                        DropdownMenuItem(
                            text = { Text(genderOption) },
                            onClick = {
                                gender = genderOption
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            // Height Input
            OutlinedTextField(
                value = height,
                onValueChange = { height = it },
                label = { Text("Height (cm)") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Height, contentDescription = null) },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                )
            )
            
            // Weight Input
            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it },
                label = { Text("Weight (kg)") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.MonitorWeight, contentDescription = null) },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                )
            )
            
            // Activity Level Selection
            ExposedDropdownMenuBox(
                expanded = activityExpanded,
                onExpandedChange = { activityExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = activityLevels.find { it.first == activityLevel }?.second ?: activityLevel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Activity Level") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = activityExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                                            leadingIcon = { Icon(Icons.Filled.DirectionsRun, contentDescription = null) }
                )
                
                ExposedDropdownMenu(
                    expanded = activityExpanded,
                    onDismissRequest = { activityExpanded = false }
                ) {
                    activityLevels.forEach { (level, description) ->
                        DropdownMenuItem(
                            text = { 
                                Column {
                                    Text(description)
                                    Text(
                                        "Factor: $level",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                activityLevel = level
                                activityExpanded = false
                            }
                        )
                    }
                }
            }
            
            // Goal Selection
            ExposedDropdownMenuBox(
                expanded = goalExpanded,
                onExpandedChange = { goalExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = goal,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Goal") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = goalExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    leadingIcon = { Icon(Icons.Default.Flag, contentDescription = null) }
                )
                
                ExposedDropdownMenu(
                    expanded = goalExpanded,
                    onDismissRequest = { goalExpanded = false }
                ) {
                    goals.forEach { goalOption ->
                        DropdownMenuItem(
                            text = { 
                                Column {
                                    Text(goalOption)
                                    Text(
                                        MacroCalculator.getGoalDescription(goalOption),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                goal = goalOption
                                goalExpanded = false
                            }
                        )
                    }
                }
            }
            
            // Goal Duration Selection
            ExposedDropdownMenuBox(
                expanded = goalDurationExpanded,
                onExpandedChange = { goalDurationExpanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = goalDurations.find { it.first == goalDurationWeeks }?.second ?: goalDurationWeeks,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Target Time to Reach Goal") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = goalDurationExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) }
                )
                
                ExposedDropdownMenu(
                    expanded = goalDurationExpanded,
                    onDismissRequest = { goalDurationExpanded = false }
                ) {
                    goalDurations.forEach { (weeks, description) ->
                        DropdownMenuItem(
                            text = { 
                                Column {
                                    Text(description)
                                    Text(
                                        MacroCalculator.getCalorieAdjustmentDescription(goal, weeks.toInt()),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            onClick = {
                                goalDurationWeeks = weeks
                                goalDurationExpanded = false
                            }
                        )
                    }
                }
            }
            
            // Customize Macros Section
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
                        "Customize Macros (Optional)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Leave unchanged to use recommended values",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Recalculate Macros Button
                    Button(
                        onClick = { recalculateMacros() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isValid
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Recalculate Macros")
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Macro Input Fields
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = targetCalories,
                            onValueChange = { targetCalories = it },
                            label = { Text("Calories (kcal)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            ),
                            singleLine = true
                        )
                        
                        OutlinedTextField(
                            value = targetProtein,
                            onValueChange = { targetProtein = it },
                            label = { Text("Protein (g)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                            ),
                            singleLine = true
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = targetCarbs,
                            onValueChange = { targetCarbs = it },
                            label = { Text("Carbs (g)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                            ),
                            singleLine = true
                        )
                        
                        OutlinedTextField(
                            value = targetFat,
                            onValueChange = { targetFat = it },
                            label = { Text("Fat (g)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                            ),
                            singleLine = true
                        )
                    }
                }
            }
            
            // Macro Preview (if all fields are filled)
            if (isValid) {
                val tempProfile = Profile(
                    name = name,
                    age = age.toInt(),
                    height = height.toFloat(),
                    weight = weight.toFloat(),
                    gender = gender,
                    goal = goal,
                    activityLevel = activityLevel.toFloat()
                )
                
                val macroTargets = MacroCalculator.calculateMacroTargets(tempProfile)
                val bmr = MacroCalculator.calculateBMR(tempProfile)
                val tdee = MacroCalculator.calculateTDEE(tempProfile)
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "Nutrition Plan Preview",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text("BMI: ${String.format("%.1f", tempProfile.calculateBMI())}")
                        Text("Category: ${tempProfile.getBMICategoryWithGoal()}")
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            "Daily Targets",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        // Show manual values if entered, otherwise show calculated values
                        val displayCalories = if (targetCalories.isNotBlank()) {
                            targetCalories.toFloatOrNull() ?: macroTargets.calories
                        } else macroTargets.calories
                        
                        val displayProtein = if (targetProtein.isNotBlank()) {
                            targetProtein.toFloatOrNull() ?: macroTargets.protein
                        } else macroTargets.protein
                        
                        val displayCarbs = if (targetCarbs.isNotBlank()) {
                            targetCarbs.toFloatOrNull() ?: macroTargets.carbs
                        } else macroTargets.carbs
                        
                        val displayFat = if (targetFat.isNotBlank()) {
                            targetFat.toFloatOrNull() ?: macroTargets.fat
                        } else macroTargets.fat
                        
                        Text("Calories: ${String.format("%.0f", displayCalories)} kcal")
                        Text("Protein: ${String.format("%.1f", displayProtein)}g")
                        Text("Carbs: ${String.format("%.1f", displayCarbs)}g")
                        Text("Fat: ${String.format("%.1f", displayFat)}g")
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            "Calculations",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text("BMR: ${String.format("%.0f", bmr)} kcal")
                        Text("TDEE: ${String.format("%.0f", tdee)} kcal")
                        Text("Activity Factor: $activityLevel")
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Save Button
            Button(
                onClick = {
                    if (isValid) {
                        // Parse macro values with validation
                        val calories = targetCalories.toFloatOrNull() ?: 0f
                        val protein = targetProtein.toFloatOrNull() ?: 0f
                        val carbs = targetCarbs.toFloatOrNull() ?: 0f
                        val fat = targetFat.toFloatOrNull() ?: 0f
                        
                        val newProfile = Profile(
                            id = profile?.id ?: 0,
                            name = name,
                            age = age.toInt(),
                            height = height.toFloat(),
                            weight = weight.toFloat(),
                            gender = gender,
                            goal = goal,
                            goalDurationInWeeks = goalDurationWeeks.toIntOrNull() ?: 12,
                            activityLevel = activityLevel.toFloat(),
                            targetCalories = calories,
                            targetProtein = protein,
                            targetCarbs = carbs,
                            targetFat = fat
                        )
                        onSave(newProfile)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isValid
            ) {
                Icon(
                    if (isEditMode) Icons.Default.Edit else Icons.Default.Add,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isEditMode) "Update Profile" else "Add Profile")
            }
        }
    }
} 